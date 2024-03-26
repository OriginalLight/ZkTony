<template>
  <a-card>
    <a-space direction="vertical" size="medium" fill>
      <a-radio-group v-model="switchIndex" type="button" size="large" style="width: 100%">
        <a-tooltip :content="t('home.camera.options.model')">
          <a-radio value="0" style="width: 100%; text-align: center">{{
            options.mode === 'manual'
              ? t('home.camera.options.manual')
              : t('home.camera.options.auto')
          }}</a-radio>
        </a-tooltip>
        <a-tooltip :content="t('home.camera.options.pixel')">
          <a-radio value="1" style="width: 100%; text-align: center">{{
            options.quality === '0'
              ? t('home.camera.options.3000')
              : options.quality === '1'
                ? t('home.camera.options.1500')
                : t('home.camera.options.1000')
          }}</a-radio>
        </a-tooltip>
        <a-tooltip :content="t('home.camera.options.exposure')">
          <a-radio
            value="2"
            style="width: 100%; text-align: center"
            :disabled="options.mode === 'auto'"
            >{{
              (options.time.minute ? options.time.minute : '0') +
              ':' +
              (options.time.second ? options.time.second : '0') +
              ':' +
              (options.time.millisecond ? options.time.millisecond : '0')
            }}</a-radio
          >
        </a-tooltip>
        <a-tooltip :content="t('home.camera.options.frames')">
          <a-radio
            value="3"
            style="width: 100%; text-align: center"
            :disabled="options.mode === 'auto'"
            >{{ (options.frame ? options.frame : '1') + t('home.camera.options.frame') }}</a-radio
          >
        </a-tooltip>
      </a-radio-group>

      <a-card v-if="switchIndex === '0'">
        <div class="card-div">
          <a-radio-group v-model="options.mode" type="button" size="large" style="width: 100%">
            <a-radio value="auto" style="width: 100%; text-align: center">{{
              t('home.camera.options.auto.model')
            }}</a-radio>
            <a-radio value="manual" style="width: 100%; text-align: center">{{
              t('home.camera.options.manual.model')
            }}</a-radio>
          </a-radio-group>
        </div>
      </a-card>
      <a-card v-if="switchIndex === '1'">
        <div class="card-div">
          <a-radio-group
            v-model="options.quality"
            type="button"
            size="large"
            style="width: 100%"
            @change="handleQualityChange"
          >
            <a-tooltip :content="t('home.camera.options.3000.title')">
              <a-radio value="0" style="width: 100%; text-align: center">{{
                t('home.camera.options.3000.pixel')
              }}</a-radio>
            </a-tooltip>
            <a-tooltip :content="t('home.camera.options.1500.title')">
              <a-radio value="1" style="width: 100%; text-align: center">{{
                t('home.camera.options.1500.pixel')
              }}</a-radio>
            </a-tooltip>
            <a-tooltip :content="t('home.camera.options.1000.title')">
              <a-radio value="2" style="width: 100%; text-align: center">{{
                t('home.camera.options.1000.pixel')
              }}</a-radio>
            </a-tooltip>
          </a-radio-group>
        </div>
      </a-card>
      <a-card v-if="switchIndex === '2'">
        <div class="card-div">
          <a-input-group>
            <a-input-number
              v-model="options.time.minute"
              size="large"
              :placeholder="t('home.camera.options.minute')"
              mode="button"
              :min="0"
              :max="59"
              :step="1"
              :input-attrs="{ style: { textAlign: 'center' } }"
              model-event="input"
            />
            <a-input-number
              v-model="options.time.second"
              size="large"
              :placeholder="t('home.camera.options.second')"
              mode="button"
              :min="0"
              :max="59"
              :step="1"
              :input-attrs="{ style: { textAlign: 'center' } }"
              model-event="input"
            />
            <a-input-number
              v-model="options.time.millisecond"
              size="large"
              :placeholder="t('home.camera.options.millisecond')"
              mode="button"
              :min="0"
              :max="999"
              :step="100"
              :input-attrs="{ style: { textAlign: 'center' } }"
              model-event="input"
            />
          </a-input-group>
        </div>
      </a-card>
      <a-card v-if="switchIndex === '3'">
        <div class="card-div">
          <a-input-number
            v-model="options.frame"
            size="large"
            :placeholder="t('home.camera.options.frames')"
            mode="button"
            :min="1"
            :max="maxFrams"
            :step="1"
            :input-attrs="{ style: { textAlign: 'center' } }"
            model-event="input"
          />
        </div>
      </a-card>

      <div style="display: flex; justify-content: space-between">
        <a-button
          type="primary"
          size="large"
          style="width: 120px"
          :loading="loading.hatch"
          :disabled="disabled.hatch"
          @click="handleHatch"
        >
          <template #icon>
            <icon-expand />
          </template>
          {{
            appStore.hatch ? t('home.camera.options.in') : t('home.camera.options.out')
          }}</a-button
        >
        <a-button
          type="primary"
          size="large"
          style="width: 120px"
          :loading="loading.preview"
          :disabled="disabled.preview"
          @click="handlePreview"
        >
          <template #icon>
            <icon-eye />
          </template>
          {{ t('home.camera.options.preview') }}
        </a-button>
        <a-button
          type="primary"
          size="large"
          style="width: 120px"
          :disabled="disabled.shot"
          @click="handleShoot"
        >
          <template #icon>
            <icon-camera />
          </template>
          {{ t('home.camera.options.shoot') }}
        </a-button>
      </div>
    </a-space>
  </a-card>
  <a-modal
    :visible="progress.visible"
    :ok-button-props="{ type: 'primary', status: 'danger' }"
    :ok-text="t('home.camera.options.cancel')"
    :closable="false"
    :hide-cancel="true"
    :mask-closable="false"
    @ok="handleCancel"
  >
    <template #title> {{ t('home.camera.options.shooting') }} </template>
    <a-space direction="vertical" size="large">
      <a-countdown
        :value="Date.now() + progress.time"
        :now="Date.now()"
        format="mm:ss.SSS"
        :start="progress.visible"
        @finish="handleFinish"
      />
    </a-space>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Message } from '@arco-design/web-vue'
import { preview, pixel, manual, auto, cancel, result } from '@renderer/api/camera'
import { hatch } from '@renderer/api/machine'
import { useAppStore } from '@renderer/store'
import useHomeState from '@renderer/states/home'

const { options } = useHomeState()

// 获取应用信息
const appStore = useAppStore()

const emit = defineEmits(['shoot', 'preview'])

const { t } = useI18n()

// 模式
const switchIndex = ref('0')

// 进度条
const progress = ref({
  visible: false,
  message: '',
  time: 0
})

// loading状态
const loading = ref({
  hatch: false,
  preview: false
})

// 关闭
const disabled = ref({
  hatch: false,
  preview: false,
  shot: false
})

//根据曝光时间计算最大帧数不能超过曝光时间除以5秒
const maxFrams = computed(() => {
  const max = Math.floor((options.value.time.minute * 60 + options.value.time.second) / 5)
  if (max < 1) {
    return 1
  }

  return max
})

watch(
  () => maxFrams.value,
  (value) => {
    if (options.value.frame > value) {
      options.value.frame = value
    }
  },
  {
    immediate: true
  }
)

const handleQualityChange = async (value: unknown) => {
  try {
    await pixel({ index: Number(value) })
  } catch (error) {
    Message.error((error as Error).message)
  }
}

const handleHatch = async () => {
  loading.value.hatch = true
  disabled.value = { hatch: true, preview: true, shot: true }
  try {
    const before = appStore.hatch
    await hatch({ code: appStore.hatch ? 0 : 1 })
    appStore.toggleHatch(!before)
    if (before) {
      disabled.value.preview = false
      disabled.value.shot = false
      await handlePreview()
    }
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.hatch = false
    disabled.value.hatch = false
  }
}

// 拍摄
const handleShoot = async () => {
  progress.value.visible = true
  progress.value.message = t('home.camera.options.shooting')
  try {
    if (options.value.mode === 'auto') {
      progress.value.time = 12000
      const res = await auto()
      progress.value.time = res.data / 1000 + 1500
      const ms = res.data / 1000
      options.value.time.minute = Math.floor(ms / 1000 / 60)
      options.value.time.second = Math.floor((ms / 1000) % 60)
      options.value.time.millisecond = Math.floor(ms % 1000)
    } else {
      progress.value.time = getExposureTime() / 1000 + 1500
      await manual({
        exposure: getExposureTime(),
        frame: options.value.frame
      })
    }
  } catch (error) {
    progress.value.visible = false
    Message.error((error as Error).message)
  }
}

// 预览
const handlePreview = async () => {
  try {
    loading.value.preview = true
    disabled.value = { hatch: true, preview: true, shot: true }
    await preview()
    // 延时500ms
    await delay(500)
    const res = await result()
    if (res.data.length > 0) {
      emit('preview', res.data[0])
    } else {
      Message.error(t('home.camera.options.preview.failed'))
    }
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.preview = false
    disabled.value = { hatch: false, preview: false, shot: false }
  }
}

// 获取曝光时间
const getExposureTime = () => {
  return (
    ((options.value.time.minute ? options.value.time.minute : 0) * 60 * 1000 +
      (options.value.time.second ? options.value.time.second : 0) * 1000 +
      (options.value.time.millisecond ? options.value.time.millisecond : 0)) *
    1000
  )
}

// 取消
const handleCancel = async () => {
  progress.value.visible = false
  try {
    await cancel()
  } catch (error) {
    Message.error((error as Error).message)
  }
}

// 完成
const handleFinish = async () => {
  if (progress.value.visible) {
    progress.value.visible = false
    try {
      const res = await result()
      if (res.data.length > 0) {
        emit('shoot', res.data)
      }
    } catch (error) {
      Message.error((error as Error).message)
    }
  }
}

// 延迟
const delay = (ms: number) => {
  return new Promise((resolve) => setTimeout(resolve, ms))
}
</script>

<style scoped lang="less">
.image-edit {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 12px;
}
.image-info {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 12px;
}

.card-div {
  display: flex;
  direction: column;
  justify-content: center;
}
</style>
