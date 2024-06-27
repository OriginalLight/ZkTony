<template>
  <div class="container">
    <div class="card">
      <a-space size="large" direction="vertical">
        <a-card :title="t('debug.serialport.title')">
          <a-space direction="vertical" :style="{ width: '100%' }">
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
                    shape="round"
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
                  <a-button type="primary" shape="round" @click="hatch({ code: 1 })">
                    {{ t('debug.machine.hatch.open') }}
                  </a-button>
                  <a-button type="primary" shape="round" @click="hatch({ code: 0 })">
                    {{ t('debug.machine.hatch.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('debug.machine.light')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" shape="round" @click="light({ code: 1 })">
                    {{ t('debug.machine.light.open') }}
                  </a-button>
                  <a-button type="primary" shape="round" @click="light({ code: 0 })">
                    {{ t('debug.machine.light.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('debug.machine.camera')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" shape="round" @click="camera({ code: 1 })">
                    {{ t('debug.machine.camera.open') }}
                  </a-button>
                  <a-button type="primary" shape="round" @click="camera({ code: 0 })">
                    {{ t('debug.machine.camera.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('debug.machine.screen')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" shape="round" @click="screen({ code: 1 })">
                    {{ t('debug.machine.screen.open') }}
                  </a-button>
                  <a-button type="primary" shape="round" @click="handleCloseScreen">
                    {{ t('debug.machine.screen.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
        <a-card :title="t('debug.sound.title')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('debug.sound.ring')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button
                    v-for="ring in rings"
                    :key="ring.key"
                    type="primary"
                    shape="round"
                    @click="play({ key: ring.key })"
                  >
                    {{ ring.label }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('debug.sound.voice')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button
                    v-for="ring in voices"
                    :key="ring.key"
                    type="primary"
                    shape="round"
                    @click="play({ key: ring.key })"
                  >
                    {{ ring.label }}
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
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { led, serialPort, hatch, light, camera, screen } from '@renderer/api/machine'
import { Message } from '@arco-design/web-vue'
import { play } from '@renderer/api/audio'

const { t } = useI18n()

const ports = [
  { code: 'Com1', label: t('debug.serialport.com1') },
  { code: 'Com2', label: t('debug.serialport.com2') }
]

const rings = [
  { key: 'Assets\\Ringtones\\Start.wav', label: t('debug.sound.ring1') },
  { key: 'Assets\\Ringtones\\Error.wav', label: t('debug.sound.ring2') },
  { key: 'Assets\\Ringtones\\Shot.wav', label: t('debug.sound.ring3') },
  { key: 'Assets\\Ringtones\\CancelShot.wav', label: t('debug.sound.ring4') },
  { key: 'Assets\\Ringtones\\Save.wav', label: t('debug.sound.ring5') },
  { key: 'Assets\\Ringtones\\Export.wav', label: t('debug.sound.ring6') },
  { key: 'Assets\\Ringtones\\Ringtone.wav', label: t('debug.sound.ring7') }
]

const voices = [
  { key: 'Assets\\Voices\\Start.wav', label: t('debug.sound.voice1') },
  { key: 'Assets\\Voices\\Error.wav', label: t('debug.sound.voice2') },
  { key: 'Assets\\Voices\\Shot.wav', label: t('debug.sound.voice3') },
  { key: 'Assets\\Voices\\CancelShot.wav', label: t('debug.sound.voice4') },
  { key: 'Assets\\Voices\\Save.wav', label: t('debug.sound.voice5') },
  { key: 'Assets\\Voices\\Export.wav', label: t('debug.sound.voice6') },
  { key: 'Assets\\Voices\\Voice.wav', label: t('debug.sound.voice7') }
]

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

const handleCloseScreen = async () => {
  try {
    await screen({ code: 0 })
    // 5s 后自动关闭
    setTimeout(() => {
      screen({ code: 1 })
    }, 5000)
  } catch (error) {
    Message.error((error as Error).message)
  }
}
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 66px - 32px);
  padding: 8px;
  margin: 8px;
  border-radius: 4px;
  background: var(--color-bg-2);
  overflow: hidden;

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
