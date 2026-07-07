import * as echarts from 'echarts'
import { palette } from './theme'

/**
 * 工业运营型 ToB 图表主题（对应《UI风格.md》§8）。
 * 注册一次即可，registerTheme 为幂等覆盖。
 * 页面传入的 option 中显式声明的 color / itemStyle.color 仍会覆盖主题，未声明处继承主题。
 */
export const ECHART_THEME_NAME = 'kg-industrial'
export const ECHART_THEME_NAME_DARK = 'kg-industrial-dark'

let registered = false

function buildTheme(isDark: boolean) {
  const labelColor = isDark ? palette.textSecondaryDark : palette.textSecondary
  const gridColor = isDark ? palette.borderDark : palette.gridLineLight
  const axisLineColor = isDark ? palette.borderStrongDark : palette.borderStrong
  const textColor = isDark ? palette.textPrimaryDark : palette.textPrimary
  return {
    color: [
      palette.primary,
      palette.microTrend,
      palette.cyanAccent,
      palette.primaryLight,
      palette.statusNormal,
      palette.statusWarning,
      palette.statusDanger
    ],
    backgroundColor: 'transparent',
    textStyle: {
      fontFamily: "'PingFang SC','DingTalk Latin','Inter','Roboto',Arial,sans-serif",
      color: labelColor
    },
    title: {
      textStyle: { color: textColor, fontWeight: 500, fontSize: 14 },
      subtextStyle: { color: labelColor }
    },
    legend: {
      textStyle: { color: labelColor },
      icon: 'roundRect',
      itemWidth: 12,
      itemHeight: 8,
      itemGap: 16
    },
    tooltip: {
      backgroundColor: isDark ? 'rgba(23,33,51,0.95)' : 'rgba(31,42,55,0.92)',
      borderColor: isDark ? palette.borderDark : palette.borderWeak,
      borderWidth: 1,
      textStyle: { color: palette.textPrimaryDark },
      extraCssText: 'box-shadow:0 2px 8px rgba(15,23,42,0.12);border-radius:8px;'
    },
    categoryAxis: {
      axisLine: { show: true, lineStyle: { color: axisLineColor } },
      axisTick: { show: false },
      axisLabel: { color: labelColor },
      splitLine: { show: false }
    },
    valueAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: labelColor },
      splitLine: { show: true, lineStyle: { color: gridColor, type: 'dashed' } }
    },
    logAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: labelColor },
      splitLine: { show: true, lineStyle: { color: gridColor, type: 'dashed' } }
    },
    timeAxis: {
      axisLine: { show: true, lineStyle: { color: axisLineColor } },
      axisTick: { show: false },
      axisLabel: { color: labelColor },
      splitLine: { show: false }
    },
    radar: {
      axisName: { color: labelColor },
      splitLine: { lineStyle: { color: gridColor } },
      splitArea: { show: false },
      axisLine: { lineStyle: { color: gridColor } }
    },
    line: {
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 2 },
      itemStyle: { borderWidth: 2 }
    },
    bar: { itemStyle: { borderRadius: [4, 4, 0, 0] } },
    pie: { itemStyle: { borderColor: isDark ? palette.cardBgDark : '#fff', borderWidth: 2 } }
  }
}

export function registerChartTheme() {
  if (registered) return
  echarts.registerTheme(ECHART_THEME_NAME, buildTheme(false))
  echarts.registerTheme(ECHART_THEME_NAME_DARK, buildTheme(true))
  registered = true
}

export function chartThemeName(isDark: boolean): string {
  return isDark ? ECHART_THEME_NAME_DARK : ECHART_THEME_NAME
}
