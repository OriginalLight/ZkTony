<template>
  <div class="container">
    <div class="card">
      <a-space size="large" direction="vertical">
        <a-card :title="t('fqc.led.title')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('fqc.led.cycle')"></a-list-item-meta>
              <template #actions>
                <a-button type="primary" @click="handleLedFQC">
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
                  <a-button type="primary" @click="hatch({ code: 1 })">
                    {{ t('fqc.machine.hatch.open') }}
                  </a-button>
                  <a-button type="primary" @click="hatch({ code: 0 })">
                    {{ t('fqc.machine.hatch.close') }}
                  </a-button>
                </a-space>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('fqc.machine.light')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" @click="light({ code: 1 })">
                    {{ t('fqc.machine.light.open') }}
                  </a-button>
                  <a-button type="primary" @click="light({ code: 0 })">
                    {{ t('fqc.machine.light.close') }}
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

const { t } = useI18n()

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
