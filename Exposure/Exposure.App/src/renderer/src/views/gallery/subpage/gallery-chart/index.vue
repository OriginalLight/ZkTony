<template>
  <div class="box">
    <v-chart class="chart" :option="option" />
    <a-spin v-show="loading" class="loading" dot />
    <a-button v-show="!loading" class="back" @click="router.back()">
      <template #icon>
        <icon-arrow-left />
      </template>
      <template #default>{{ t('gallery.chart.back') }}</template>
    </a-button>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import * as echarts from 'echarts'
import { TooltipComponent, DataZoomComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { SurfaceChart } from 'echarts-gl/charts'
import { Grid3DComponent } from 'echarts-gl/components'

import useGalleryState from '@renderer/states/gallery'

const { t } = useI18n()
const router = useRouter()
const { subpage } = useGalleryState()

echarts.use([TooltipComponent, DataZoomComponent, Grid3DComponent, SurfaceChart, CanvasRenderer])
echarts.env.touchEventsSupported = true
echarts.env.pointerEventsSupported = false
const option = ref({})
const loading = ref(true)

const img = new Image()
const canvas = document.createElement('canvas')
const ctx = canvas.getContext('2d')
img.onload = () => {
  loading.value = true
  const width = img.width
  const height = img.height
  canvas.width = width
  canvas.height = height
  ctx!.drawImage(img, 0, 0, width, height)
  const imgData = ctx!.getImageData(0, 0, width, height)
  const data: Array<number[]> = []

  for (let i = 0; i < imgData.data.length; i += 4) {
    const r = imgData.data[i * 4]
    const g = imgData.data[i * 4 + 1]
    const b = imgData.data[i * 4 + 2]
    const lum = 0.2125 * r + 0.7154 * g + 0.0721 * b
    data.push([i % width, height - Math.floor(i / width), lum])
  }

  option.value = {
    tooltip: {},
    xAxis3D: {
      type: 'value',
      min: 0,
      max: width
    },
    yAxis3D: {
      type: 'value',
      min: 0,
      max: height
    },
    zAxis3D: {
      type: 'value',
      min: 0,
      max: 300
    },
    grid3D: {
      axisPointer: {
        show: false
      },
      viewControl: {
        distance: 200,
        animation: true
      },
      ambient: {
        intensity: 0.3,
        color: '#fff'
      },
      ambientCubemap: {
        texture: 'pisa.hdr',
        exposure: 2.0
      },
      postEffect: {
        enable: true
      },
      light: {
        main: {
          shadow: true,
          intensity: 2
        }
      }
    },
    series: [
      {
        type: 'surface',
        silent: true,
        wireframe: {
          show: false
        },
        itemStyle: {
          color: function (params) {
            const i = params.dataIndex
            const r = imgData.data[i * 4]
            const g = imgData.data[i * 4 + 1]
            const b = imgData.data[i * 4 + 2]
            if (data[i][2] >= 255) return 'rgb(255, 0, 0)'
            else return 'rgb(' + [r, g, b].join(',') + ')'
          }
        },
        data: data
      }
    ]
  }
  loading.value = false
}

img.src = subpage.value.item?.thumbnail || ''
</script>

<style scoped>
.box {
  display: flex;
  position: relative;
  height: calc(100vh - 66px - 32px);
  padding: 8px;
  margin: 8px;
  border-radius: 4px;
  background: var(--color-bg-2);
  overflow: hidden;

  .chart {
    height: 100%;
    width: 100%;
  }

  .loading {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  .back {
    position: absolute;
    top: 16px;
    left: 16px;
  }
}
</style>
