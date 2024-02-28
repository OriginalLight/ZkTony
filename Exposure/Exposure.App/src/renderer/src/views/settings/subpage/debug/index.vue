<template>
  <div class="container">
    <div class="card">
      <a-space size="large" direction="vertical">
        <a-card :title="t('debug.serialport.title')">
          <a-space direction="vertical" :style="{ width: '100%' }">
            <a-space v-if="serialStatus.length > 0">
              <a-tag v-for="code in serialStatus" :key="code" size="large" color="arcoblue">
                {{ ports.find((port) => port.code === code)?.label }}
              </a-tag>
            </a-space>
            <a-input-group :style="{ width: '100%' }">
              <a-select v-model="serialOptions.port" :style="{ width: '15%' }">
                <a-option v-for="port in ports" :key="port.code" :value="port.code">
                  {{ port.label }}
                </a-option>
              </a-select>
              <a-input-search
                v-model="serialOptions.hex"
                :style="{ width: '100%' }"
                allow-clear
                search-button
                @search="handleExcute"
              >
                <template #prepend> Hex </template>
                <template #button-icon>
                  <icon-send />
                </template>
                <template #button-default> {{ t('debug.serialport.execute') }} </template>
              </a-input-search>
            </a-input-group></a-space
          >
        </a-card>
        <a-card :title="t('debug.led.title')">
          <a-list>
            <a-list-item v-for="button in ledbtns" :key="button.title">
              <a-list-item-meta :title="button.title"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button
                    v-for="option in button.options"
                    :key="option.code"
                    type="primary"
                    @click="led(option)"
                  >
                    {{ option.label }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
        <a-card :title="t('debug.machine.title')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('debug.machine.hatch')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" @click="hatch({ code: 1 })">
                    {{ t('debug.machine.hatch.open') }}
                  </a-button>
                  <a-button type="primary" @click="hatch({ code: 0 })">
                    {{ t('debug.machine.hatch.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('debug.machine.light')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" @click="light({ code: 1 })">
                    {{ t('debug.machine.light.open') }}
                  </a-button>
                  <a-button type="primary" @click="light({ code: 0 })">
                    {{ t('debug.machine.light.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('debug.machine.camera')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" @click="camera({ code: 1 })">
                    {{ t('debug.machine.camera.open') }}
                  </a-button>
                  <a-button type="primary" @click="camera({ code: 0 })">
                    {{ t('debug.machine.camera.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('debug.machine.screen')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" @click="screen({ code: 1 })">
                    {{ t('debug.machine.screen.open') }}
                  </a-button>
                  <a-button type="primary" @click="screen({ code: 0 })">
                    {{ t('debug.machine.screen.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
      </a-space>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  led,
  serialPort,
  serialPortStatus,
  hatch,
  light,
  camera,
  screen
} from '@renderer/api/machine'
import { Message } from '@arco-design/web-vue'

const { t } = useI18n()

const ports = [
  { code: 'Com1', label: t('debug.serialport.com1') },
  { code: 'Com2', label: t('debug.serialport.com2') }
]

const serialStatus = ref<string[]>([])

const serialOptions = ref({
  port: 'Com1',
  hex: ''
})

const ledbtns = [
  {
    title: t('debug.led.always'),
    options: [
      { code: 0, label: t('debug.led.red') },
      { code: 1, label: t('debug.led.green') },
      { code: 2, label: t('debug.led.blue') },
      { code: 3, label: t('debug.led.yellow') },
      { code: 4, label: t('debug.led.purple') },
      { code: 5, label: t('debug.led.white') }
    ]
  },
  {
    title: t('debug.led.blink.fast'),
    options: [
      { code: 6, label: t('debug.led.red') },
      { code: 7, label: t('debug.led.green') },
      { code: 8, label: t('debug.led.blue') },
      { code: 9, label: t('debug.led.yellow') },
      { code: 10, label: t('debug.led.purple') },
      { code: 11, label: t('debug.led.white') }
    ]
  },
  {
    title: t('debug.led.blink.slow'),
    options: [
      { code: 12, label: t('debug.led.red') },
      { code: 13, label: t('debug.led.green') },
      { code: 14, label: t('debug.led.blue') },
      { code: 15, label: t('debug.led.yellow') },
      { code: 16, label: t('debug.led.purple') },
      { code: 17, label: t('debug.led.white') }
    ]
  },
  {
    title: t('debug.led.switch'),
    options: [{ code: 255, label: t('debug.led.off') }]
  }
]

const handleExcute = async () => {
  try {
    if (
      !serialOptions.value.hex ||
      serialOptions.value.hex.length % 2 !== 0 ||
      serialOptions.value.hex.length === 0
    ) {
      return
    }
    await serialPort(serialOptions.value)
  } catch (error) {
    Message.error((error as Error).message)
  }
}

onMounted(async () => {
  try {
    const res = await serialPortStatus()
    serialStatus.value = res.data
  } catch (error) {
    console.log(error)
  }
})
</script>

<style lang="less" scoped>
.container {
  display: flex;
  padding: 16px;
  height: calc(100vh - 66px - 32px);

  .card {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow-y: scroll;
  }
}
.card::-webkit-scrollbar {
  display: none;
}
</style>
