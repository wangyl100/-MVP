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

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  function setCollapsed(v: boolean) {
    collapsed.value = v
  }

  return { collapsed, userInfo, toggleCollapsed, setCollapsed }
})
