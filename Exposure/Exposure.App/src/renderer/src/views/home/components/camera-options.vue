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
            <a-radio value="0" style="width: 100%; text-align: center">{{
              t('home.camera.options.3000.pixel')
            }}</a-radio>
            <a-radio value="1" style="width: 100%; text-align: center">{{
              t('home.camera.options.1500.pixel')
            }}</a-radio>
            <a-radio value="2" style="width: 100%; text-align: center">{{
              t('home.camera.options.1000.pixel')
            }}</a-radio>
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
            :max="100"
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
          :disabled="loading.hatch"
          @click="handleHatch"
        >
          <template #icon>
            <icon-expand />
          </template>
          {{
            hatchStatus === 0 ? t('home.camera.options.out') : t('home.camera.options.in')
          }}</a-button
        >
        <a-button
          type="primary"
          size="large"
          style="width: 120px"
          :loading="loading.preview"
          :disabled="loading.preview"
          @click="handlePreview"
        >
          <template #icon>
            <icon-eye />
          </template>
          {{ t('home.camera.options.preview') }}
        </a-button>
        <a-button type="primary" size="large" style="width: 120px" @click="handleShoot">
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
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Message } from '@arco-design/web-vue'
import { init, preview, pixel, manual, auto, cancel, result } from '@renderer/api/camera'
import { hatch } from '@renderer/api/machine'
import useHomeState from '@renderer/states/home'

const { options, isInit } = useHomeState()

const hatchStatus = ref(0)

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

const handleQualityChange = async (value: unknown) => {
  try {
    await pixel({ index: Number(value) })
  } catch (error) {
    Message.error((error as Error).message)
  }
}

const handleHatch = async () => {
  loading.value.hatch = true
  try {
    await hatch({ code: hatchStatus.value === 0 ? 1 : 0 })
    hatchStatus.value = hatchStatus.value === 0 ? 1 : 0
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.hatch = false
  }
}

// 拍摄
const handleShoot = async () => {
  progress.value.visible = true
  progress.value.message = t('home.camera.options.shooting')
  try {
    if (options.value.mode === 'auto') {
      progress.value.time = 5000
      const res = await auto()
      progress.value.time = res.data.exposure / 1000 + 1500
      const ms = res.data.exposure / 1000
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
    await preview()
    // 延时500ms
    await delay(500)
    const res = await result()
    emit('preview', res.data[0])
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.preview = false
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

// 初始化
onMounted(async () => {
  try {
    if (!isInit.value) {
      await init()
      isInit.value = true
    }
  } catch (error) {
    console.log(error)
  }
})
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
