<template>
  <div ref="chartRef" :style="{ width: '100%', height: height }"></div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch, shallowRef } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { useAppStore } from '@/stores/app'
import { registerChartTheme, chartThemeName } from '@/styles/echarts-theme'

// 模块加载时注册一次工业风图表主题（幂等）
registerChartTheme()

const props = withDefaults(
  defineProps<{
    option: EChartsOption
    height?: string
  }>(),
  {
    height: '320px'
  }
)

const appStore = useAppStore()
const chartRef = ref<HTMLElement>()
const chart = shallowRef<echarts.ECharts>()

function render() {
  if (!chartRef.value) return
  if (!chart.value) {
    chart.value = echarts.init(chartRef.value, chartThemeName(appStore.darkMode))
  }
  chart.value.setOption(props.option, true)
}

function resize() {
  chart.value?.resize()
}

onMounted(() => {
  render()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart.value?.dispose()
})

watch(
  () => props.option,
  () => render(),
  { deep: true }
)
</script>
