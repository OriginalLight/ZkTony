<template>
  <div class="container">
    <div class="card">
      <a-space size="large" direction="vertical">
        <a-card :title="t('fqc.led.title')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('fqc.led.cycle')"></a-list-item-meta>
              <template #actions>
                <a-button type="primary" shape="round" @click="handleLedFQC">
                  {{ t('fqc.led.start') }}
                </a-button>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
        <a-card :title="t('fqc.machine.title')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('fqc.machine.hatch')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" shape="round" @click="hatch({ code: 1 })">
                    {{ t('fqc.machine.hatch.open') }}
                  </a-button>
                  <a-button type="primary" shape="round" @click="hatch({ code: 0 })">
                    {{ t('fqc.machine.hatch.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('fqc.machine.light')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" shape="round" @click="light({ code: 1 })">
                    {{ t('fqc.machine.light.open') }}
                  </a-button>
                  <a-button type="primary" shape="round" @click="light({ code: 0 })">
                    {{ t('fqc.machine.light.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
        <a-card :title="t('fqc.sound.title')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('fqc.sound.ring')"></a-list-item-meta>
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
              <a-list-item-meta :title="t('fqc.sound.voice')"></a-list-item-meta>
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
import { useI18n } from 'vue-i18n'
import { led, hatch, light } from '@renderer/api/machine'
import { play } from '@renderer/api/audio'

const { t } = useI18n()

const rings = [
  { key: 'Assets\\Ringtones\\Start.wav', label: t('fqc.sound.ring1') },
  { key: 'Assets\\Ringtones\\Error.wav', label: t('fqc.sound.ring2') },
  { key: 'Assets\\Ringtones\\Shot.wav', label: t('fqc.sound.ring3') },
  { key: 'Assets\\Ringtones\\CancelShot.wav', label: t('fqc.sound.ring4') },
  { key: 'Assets\\Ringtones\\Save.wav', label: t('fqc.sound.ring5') },
  { key: 'Assets\\Ringtones\\Export.wav', label: t('fqc.sound.ring6') },
  { key: 'Assets\\Ringtones\\Ringtone.wav', label: t('fqc.sound.ring7') }
]

const voices = [
  { key: 'Assets\\Voices\\Start.wav', label: t('fqc.sound.voice1') },
  { key: 'Assets\\Voices\\Error.wav', label: t('fqc.sound.voice2') },
  { key: 'Assets\\Voices\\Shot.wav', label: t('fqc.sound.voice3') },
  { key: 'Assets\\Voices\\CancelShot.wav', label: t('fqc.sound.voice4') },
  { key: 'Assets\\Voices\\Save.wav', label: t('fqc.sound.voice5') },
  { key: 'Assets\\Voices\\Export.wav', label: t('fqc.sound.voice6') },
  { key: 'Assets\\Voices\\Voice.wav', label: t('fqc.sound.voice7') }
]

const handleLedFQC = async () => {
  try {
    await led({ code: 1 })
    await delay(5000)
    await led({ code: 0 })
    await delay(5000)
    await led({ code: 3 })
    await delay(5000)
    await led({ code: 9 })
    await delay(5000)
    await led({ code: 15 })
    await delay(5000)
    await led({ code: 255 })
  } catch (error) {
    console.error(error)
  }
}

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))
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
