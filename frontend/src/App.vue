<template>
  <a-config-provider :theme="themeConfig" :locale="zhCN">
    <router-view />
  </a-config-provider>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { useAppStore } from '@/stores/app'
import { buildThemeConfig } from '@/styles/theme'

const appStore = useAppStore()
const themeConfig = computed(() => buildThemeConfig(appStore.darkMode))

// 同步 data-theme 到 <html>，供 global.less 的暗色规则使用
watch(
  () => appStore.darkMode,
  (v) => {
    document.documentElement.setAttribute('data-theme', v ? 'dark' : 'light')
  },
  { immediate: true }
)
</script>
