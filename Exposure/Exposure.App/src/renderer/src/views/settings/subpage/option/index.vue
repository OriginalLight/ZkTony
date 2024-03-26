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
                  <a-button type="primary" @click="handleMachineId">{{ t('option.set') }}</a-button>
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
                  <a-button type="primary" @click="handleCom1">{{ t('option.set') }}</a-button>
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
                  <a-button type="primary" @click="handleCom2">{{ t('option.set') }}</a-button>
                </a-input-group>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
        <a-card :title="t('option.camera')">
          <a-list>
            <a-list-item>
              <a-list-item-meta :title="t('option.camera.expoTime')"></a-list-item-meta>
              <template #actions>
                <a-input-group>
                  <a-input-number
                    v-model="option.expoTime"
                    style="width: 350px"
                    :min="1000"
                    :max="10000000"
                    mode="button"
                    :step="500"
                  >
                    <template #suffix>NS</template>
                  </a-input-number>
                  <a-button type="primary" @click="handleExpoTime">{{ t('option.set') }}</a-button>
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
                  <a-button type="primary" @click="handleGain">{{ t('option.set') }}</a-button>
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
                  <a-button type="primary" @click="handleTemperature">{{
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
                    :step="12800"
                  >
                    <template #suffix>Pulse</template>
                  </a-input-number>
                  <a-button type="primary" @click="handleHatchStep">{{ t('option.set') }}</a-button>
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
                    :step="12800"
                  >
                    <template #suffix>Pulse</template>
                  </a-input-number>
                  <a-button type="primary" @click="handleHatchOffset">{{
                    t('option.set')
                  }}</a-button>
                </a-input-group>
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
import { setOption, getOption } from '@renderer/api/option'
import { getPorts } from '@renderer/api/machine'
import { Message } from '@arco-design/web-vue'

const { t } = useI18n()

const option = ref({
  machineId: '',
  com1: 'COM4',
  com2: 'COM3',
  ports: ['COM1', 'COM2', 'COM3', 'COM4'],
  expoTime: 1500,
  gain: 3000,
  temperature: -15,
  hatchStep: 0,
  hatchOffset: 0
})

const handleMachineId = async () => {
  try {
    await setOption({
      key: 'MachineId',
      value: option.value.machineId
    })
    Message.success(t('option.set.success'))
  } catch (error) {
    Message.error(t('option.set.fail'))
  }
}

const handleCom1 = async () => {
  try {
    await setOption({
      key: 'Com1',
      value: option.value.com1
    })
    Message.success(t('option.set.restart'))
  } catch (error) {
    Message.error(t('option.set.fail'))
  }
}

const handleCom2 = async () => {
  try {
    await setOption({
      key: 'Com2',
      value: option.value.com2
    })
    Message.success(t('option.set.restart'))
  } catch (error) {
    Message.error(t('option.set.fail'))
  }
}

const handleExpoTime = async () => {
  try {
    await setOption({
      key: 'ExpoTime',
      value: String(option.value.expoTime)
    })
    Message.success(t('option.set.restart'))
  } catch (error) {
    Message.error(t('option.set.fail'))
  }
}

const handleGain = async () => {
  try {
    await setOption({
      key: 'Gain',
      value: String(option.value.gain)
    })
    Message.success(t('option.set.restart'))
  } catch (error) {
    Message.error(t('option.set.fail'))
  }
}

const handleTemperature = async () => {
  try {
    await setOption({
      key: 'Temperature',
      value: String(option.value.temperature * 10)
    })
    Message.success(t('option.set.restart'))
  } catch (error) {
    Message.error(t('option.set.fail'))
  }
}

const handleHatchStep = async () => {
  try {
    await setOption({
      key: 'HatchStep',
      value: String(option.value.hatchStep)
    })
    Message.success(t('option.set.success'))
  } catch (error) {
    Message.error(t('option.set.fail'))
  }
}

const handleHatchOffset = async () => {
  try {
    await setOption({
      key: 'HatchOffset',
      value: String(option.value.hatchOffset)
    })
    Message.success(t('option.set.success'))
  } catch (error) {
    Message.error(t('option.set.fail'))
  }
}

onMounted(async () => {
  try {
    const res = await getOption({ key: 'MachineId' })
    option.value.machineId = res.data
    const res1 = await getOption({ key: 'Com1' })
    option.value.com1 = res1.data
    const res2 = await getOption({ key: 'Com2' })
    option.value.com2 = res2.data
    const res3 = await getPorts()
    option.value.ports = res3.data
    const res4 = await getOption({ key: 'ExpoTime' })
    if (res4.data != 'None') {
      option.value.expoTime = Number(res4.data)
    }
    const res5 = await getOption({ key: 'Gain' })
    if (res5.data != 'None') {
      option.value.gain = Number(res5.data)
    }
    const res6 = await getOption({ key: 'Temperature' })
    if (res6.data != 'None') {
      option.value.temperature = Number(res6.data) / 10
    }
    const res7 = await getOption({ key: 'HatchStep' })
    if (res7.data != 'None') {
      option.value.hatchStep = Number(res7.data)
    }
    const res8 = await getOption({ key: 'HatchOffset' })
    if (res8.data != 'None') {
      option.value.hatchOffset = Number(res8.data)
    }
  } catch (error) {
    console.error(error)
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
