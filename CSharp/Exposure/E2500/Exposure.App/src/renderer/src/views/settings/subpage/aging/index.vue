<template>
  <div class="container">
    <div class="card">
      <a-space size="large" direction="vertical">
        <a-card :title="t('aging.test')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('aging.cycle')"></a-list-item-meta>
              <template #actions>
                <a-input-number
                  v-model="agingOptions.cycle"
                  :min="1"
                  :max="10000"
                  :step="1"
                  mode="button"
                />
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('aging.interval')"></a-list-item-meta>
              <template #actions>
                <a-input-number
                  v-model="agingOptions.interval"
                  :min="5"
                  :max="1000"
                  :step="1"
                  mode="button"
                />
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('aging.camera')"></a-list-item-meta>
              <template #actions>
                <a-switch v-model="agingOptions.camera" />
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('aging.hatch')"></a-list-item-meta>
              <template #actions>
                <a-switch v-model="agingOptions.hatch" />
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('aging.led')"></a-list-item-meta>
              <template #actions>
                <a-switch v-model="agingOptions.led" />
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('aging.light')"></a-list-item-meta>
              <template #actions>
                <a-switch v-model="agingOptions.light" />
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('aging.switch')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button
                    type="primary"
                    shape="round"
                    :disabled="
                      !agingOptions.hatch &&
                      !agingOptions.led &&
                      !agingOptions.light &&
                      !agingOptions.camera
                    "
                    @click="agingTest(agingOptions)"
                  >
                    {{ t('aging.start') }}
                  </a-button>
                  <a-button
                    type="primary"
                    shape="round"
                    @click="
                      agingTest({
                        hatch: false,
                        led: false,
                        light: false,
                        camera: false,
                        cycle: 0,
                        interval: 0
                      })
                    "
                  >
                    {{ t('aging.stop') }}
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
import { agingTest } from '@renderer/api/test'

const { t } = useI18n()

const agingOptions = ref({
  hatch: false,
  led: false,
  light: false,
  camera: false,
  cycle: 10,
  interval: 10
})
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
