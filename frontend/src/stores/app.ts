import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const collapsed = ref(false)
  const userInfo = ref({
    id: 'u_001',
    username: 'admin',
    nickname: '超级管理员',
    avatar: '',
    role: '超级管理员',
    email: 'admin@kg-platform.com'
  })

  // 主题模式：spec §3.3 主题切换 / §12 检查浅色与暗色主题
  const darkMode = ref<boolean>(
    typeof localStorage !== 'undefined' && localStorage.getItem('kg_theme') === 'dark'
  )

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  function setCollapsed(v: boolean) {
    collapsed.value = v
  }

  function setDarkMode(v: boolean) {
    darkMode.value = v
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('kg_theme', v ? 'dark' : 'light')
    }
  }

  function toggleDarkMode() {
    setDarkMode(!darkMode.value)
  }

  return {
    collapsed,
    userInfo,
    darkMode,
    toggleCollapsed,
    setCollapsed,
    setDarkMode,
    toggleDarkMode
  }
})
