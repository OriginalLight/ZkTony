<template>
  <div class="container">
    <div class="card">
      <a-space size="large" direction="vertical">
        <a-card :title="t('option.machine')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('option.machine.id.title')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input v-model="option.machineId" style="width: 350px" />
                  <a-button type="primary" @click="handleOption('MachineId', option.machineId)">{{
                    t('option.set')
                  }}</a-button>
                </a-input-group>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
        <a-card :title="t('option.serialport')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('option.serialport.led')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-select v-model="option.com1" :style="{ width: '350px' }">
                    <a-option v-for="port in option.ports" :key="port" :value="port">
                      {{ port }}
                    </a-option>
                  </a-select>
                  <a-button type="primary" @click="handleOption('Com1', option.com1, false)">{{
                    t('option.set')
                  }}</a-button>
                </a-input-group>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('option.serialport.computer')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-select v-model="option.com2" :style="{ width: '350px' }">
                    <a-option v-for="port in option.ports" :key="port" :value="port">
                      {{ port }}
                    </a-option>
                  </a-select>
                  <a-button type="primary" @click="handleOption('Com2', option.com2, false)">{{
                    t('option.set')
                  }}</a-button>
                </a-input-group>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
        <a-card :title="t('option.camera')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('option.camera.calibrate')"></a-list-item-meta>
              <template #actions>
                <a-button type="primary" :loading="loading" @click="handleCalibrate">{{
                  t('option.camera.calibrate')
                }}</a-button>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('option.camera.threshold')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input-number
                    v-model="option.threshold"
                    style="width: 350px"
                    :min="0"
                    :max="1"
                    mode="button"
                    :step="0.001"
                  >
                  </a-input-number>
                  <a-button
                    type="primary"
                    @click="handleOption('Threshold', String(option.threshold))"
                    >{{ t('option.set') }}</a-button
                  >
                </a-input-group>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('option.camera.targetThreshold')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input-number
                    v-model="option.targetThreshold"
                    style="width: 350px"
                    :min="1000"
                    :max="65535"
                    mode="button"
                    :step="1000"
                  >
                  </a-input-number>
                  <a-button
                    type="primary"
                    @click="handleOption('TargetThreshold', String(option.targetThreshold))"
                    >{{ t('option.set') }}</a-button
                  >
                </a-input-group>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('option.camera.expoTime')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input-number
                    v-model="option.expoTime"
                    style="width: 350px"
                    :min="1000"
                    :max="1000000"
                    mode="button"
                    :step="500"
                  >
                    <template #suffix>μs</template>
                  </a-input-number>
                  <a-button
                    type="primary"
                    @click="handleOption('ExpoTime', String(option.expoTime))"
                    >{{ t('option.set') }}</a-button
                  >
                </a-input-group>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('option.camera.gain')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input-number
                    v-model="option.gain"
                    style="width: 350px"
                    :min="1"
                    :max="10000"
                    mode="button"
                    :step="100"
                  >
                    <template #suffix>%</template>
                  </a-input-number>
                  <a-button type="primary" @click="handleOption('Gain', String(option.gain))">{{
                    t('option.set')
                  }}</a-button>
                </a-input-group>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('option.camera.temperature')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input-number
                    v-model="option.temperature"
                    style="width: 350px"
                    :min="-50"
                    :max="10"
                    mode="button"
                    :step="1"
                  >
                    <template #suffix>°C</template>
                  </a-input-number>
                  <a-button
                    type="primary"
                    @click="handleOption('Temperature', String(option.temperature * 10))"
                    >{{ t('option.set') }}</a-button
                  >
                </a-input-group>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('option.camera.rotate')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input-number
                    v-model="option.rotate"
                    style="width: 350px"
                    :min="-360"
                    :max="360"
                    mode="button"
                    :step="1"
                  >
                    <template #suffix>°</template>
                  </a-input-number>
                  <a-button type="primary" @click="handleOption('Rotate', String(option.rotate))">{{
                    t('option.set')
                  }}</a-button>
                </a-input-group>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('option.camera.roi')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input v-model="option.roi" style="width: 350px" />
                  <a-button type="primary" @click="handleOption('Roi', String(option.roi))">{{
                    t('option.set')
                  }}</a-button>
                </a-input-group>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
        <a-card :title="t('option.lower')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('option.lower.hatch.step')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input-number
                    v-model="option.hatchStep"
                    style="width: 350px"
                    :min="0"
                    mode="button"
                    :max="256000"
                    :step="12800"
                  >
                    <template #suffix>Pulse</template>
                  </a-input-number>
                  <a-button
                    type="primary"
                    @click="handleOption('HatchStep', String(option.hatchStep))"
                    >{{ t('option.set') }}</a-button
                  >
                </a-input-group>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('option.lower.hatch.offset')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input-number
                    v-model="option.hatchOffset"
                    style="width: 350px"
                    mode="button"
                    :min="-12800"
                    :max="12800"
                    :step="200"
                  >
                    <template #suffix>Pulse</template>
                  </a-input-number>
                  <a-button
                    type="primary"
                    @click="handleOption('HatchOffset', String(option.hatchOffset))"
                    >{{ t('option.set') }}</a-button
                  >
                </a-input-group>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :title="t('debug.machine.hatch')"></a-list-item-meta>
              <template #actions>
                <a-space>
                  <a-button type="primary" @click="hatch({ code: 1 })">
                    {{ t('option.lower.hatch.open') }}
                  </a-button>
                  <a-button type="primary" @click="hatch({ code: 0 })">
                    {{ t('option.lower.hatch.close') }}
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
import { setOption, getAllOptions } from '@renderer/api/option'
import { hatch } from '@renderer/api/machine'
import { getPorts } from '@renderer/api/machine'
import { calibrate } from '@renderer/api/camera'
import { Message } from '@arco-design/web-vue'

const { t } = useI18n()

const option = ref({
  machineId: '',
  com1: 'None',
  com2: 'None',
  ports: ['COM1', 'COM2', 'COM3', 'COM4'],
  expoTime: 30000,
  gain: 500,
  temperature: -15,
  rotate: 0,
  roi: '0,1,0,1',
  hatchStep: 256000,
  hatchOffset: 0,
  threshold: 0.001,
  targetThreshold: 10000
})

const loading = ref(false)

const handleOption = async (key: string, value: string, flag: boolean = true) => {
  try {
    await setOption({
      key: key,
      value: value
    })
    if (flag) {
      Message.success(t('option.set.success'))
    } else {
      Message.success(t('option.set.restart'))
    }
  } catch (error) {
    Message.error(t('option.set.fail'))
  }
}

const handleCalibrate = async () => {
  try {
    loading.value = true
    await calibrate()
    loading.value = false
    Message.info(t('option.camera.calibrate.success'))
  } catch (error) {
    loading.value = false
    Message.error((error as Error).message)
  }
}

onMounted(async () => {
  try {
    const all = await getAllOptions()
    const ops = all.data
    if (ops.MachineId) option.value.machineId = ops.MachineId
    if (ops.Com1) option.value.com1 = ops.Com1
    if (ops.Com2) option.value.com2 = ops.Com2
    if (ops.ExpoTime) option.value.expoTime = Number(ops.ExpoTime)
    if (ops.Gain) option.value.gain = Number(ops.Gain)
    if (ops.Temperature) option.value.temperature = Number(ops.Temperature) / 10
    if (ops.HatchStep) option.value.hatchStep = Number(ops.HatchStep)
    if (ops.HatchOffset) option.value.hatchOffset = Number(ops.HatchOffset)
    if (ops.Rotate) option.value.rotate = Number(ops.Rotate)
    if (ops.Roi) option.value.roi = ops.Roi
    if (ops.Threshold) option.value.threshold = Number(ops.Threshold)
    if (ops.TargetThreshold) option.value.targetThreshold = Number(ops.TargetThreshold)
    const res = await getPorts()
    option.value.ports = res.data
  } catch (error) {
    console.error(error)
  }
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
