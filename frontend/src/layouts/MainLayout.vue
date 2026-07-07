<template>
  <a-layout class="main-layout">
    <!-- 侧边导航 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="240"
      :collapsed-width="64"
      class="sider"
      :class="{ 'sider--dark': appStore.darkMode }"
    >
      <div class="logo" @click="router.push('/dashboard')">
        <div class="logo-icon">
          <ShareAltOutlined />
        </div>
        <transition name="fade">
          <span v-if="!collapsed" class="logo-text">知识图谱平台</span>
        </transition>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        theme="dark"
        :items="menuItems"
        @click="onMenuClick"
      />
    </a-layout-sider>

    <a-layout>
      <!-- 顶部栏 -->
      <a-layout-header class="header">
        <div class="header-left">
          <a-button type="text" class="collapse-btn" @click="toggleCollapsed">
            <MenuFoldOutlined v-if="!collapsed" />
            <MenuUnfoldOutlined v-else />
          </a-button>
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="(item, idx) in breadcrumbList" :key="idx">
              {{ item }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <a-tooltip :title="appStore.darkMode ? '切换为浅色' : '切换为暗色'">
            <a-button type="text" shape="circle" class="icon-btn" @click="appStore.toggleDarkMode">
              <BulbFilled v-if="!appStore.darkMode" />
              <BulbOutlined v-else />
            </a-button>
          </a-tooltip>
          <a-tooltip title="搜索">
            <a-button type="text" shape="circle" class="icon-btn">
              <SearchOutlined />
            </a-button>
          </a-tooltip>
          <a-tooltip title="通知">
            <a-badge :count="5" size="small">
              <a-button type="text" shape="circle" class="icon-btn">
                <BellOutlined />
              </a-button>
            </a-badge>
          </a-tooltip>
          <a-dropdown>
            <div class="user-info">
              <a-avatar style="background-color: #2f6bff">
                {{ userInfo.nickname.charAt(0) }}
              </a-avatar>
              <span class="username">{{ userInfo.nickname }}</span>
              <DownOutlined />
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile" @click="router.push('/platform/profile')">
                  <UserOutlined /> 个人中心
                </a-menu-item>
                <a-menu-item key="settings">
                  <SettingOutlined /> 系统设置
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 主内容区 -->
      <a-layout-content class="content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, ref, watch, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { menuConfig, type MenuItem } from '@/config/menu'
import * as Icons from '@ant-design/icons-vue'
import { BulbOutlined, BulbFilled } from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const collapsed = computed({
  get: () => appStore.collapsed,
  set: (v) => appStore.setCollapsed(v)
})
const { toggleCollapsed } = appStore
const userInfo = computed(() => appStore.userInfo)

// 将菜单配置转换为 a-menu items，并动态解析图标
function resolveIcon(name?: string) {
  if (!name) return null
  const Comp = (Icons as any)[name]
  return Comp ? () => h(Comp) : null
}
function buildMenuItems(list: MenuItem[]): any[] {
  return list.map((item) => ({
    key: item.key,
    label: item.label,
    icon: resolveIcon(item.icon),
    children: item.children ? buildMenuItems(item.children) : undefined
  }))
}
const menuItems = computed(() => buildMenuItems(menuConfig))

const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])

function findPath(keys: string[]): string[] {
  // 根据当前 route.path 找到对应的菜单 key
  for (const item of menuConfig) {
    if (item.path === route.path) return [item.key]
    if (item.children) {
      for (const c of item.children) {
        if (c.path === route.path) return [c.key]
      }
    }
  }
  return keys
}

watch(
  () => route.path,
  (p) => {
    selectedKeys.value = findPath([])
    // 自动展开父级
    for (const item of menuConfig) {
      if (item.children?.some((c) => c.path === p)) {
        if (!openKeys.value.includes(item.key)) {
          openKeys.value = [...openKeys.value, item.key]
        }
      }
    }
  },
  { immediate: true }
)

function onMenuClick({ key }: { key: string }) {
  const find = (list: MenuItem[]): { path?: string; hasChildren: boolean } | undefined => {
    for (const item of list) {
      if (item.key === key) {
        return { path: item.path, hasChildren: !!item.children }
      }
      if (item.children) {
        const r = find(item.children)
        if (r) return r
      }
    }
  }
  const result = find(menuConfig)
  if (result && !result.hasChildren && result.path) {
    router.push(result.path)
  }
}

// 面包屑
const breadcrumbList = computed(() => {
  const list: string[] = []
  for (const item of menuConfig) {
    if (item.path === route.path) {
      list.push(item.label)
      return list
    }
    if (item.children) {
      for (const c of item.children) {
        if (c.path === route.path) {
          list.push(item.label)
          list.push(c.label)
          return list
        }
      }
    }
  }
  list.push(route.meta?.title as string || '页面')
  return list
})
</script>

<style lang="less" scoped>
.main-layout {
  min-height: 100vh;
}
.sider {
  // 浅色：背景由 Layout.siderBg token (#0050B3) 提供
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
  overflow: auto;
}
.sider--dark {
  background: linear-gradient(180deg, #132642 0%, #0d1a30 100%);
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  cursor: pointer;
  color: #fff;
  overflow: hidden;
  .logo-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: linear-gradient(135deg, #2f6bff, #4c84ff);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;
  }
  .logo-text {
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;
  }
}
.header {
  // 背景由 Layout.headerBg token 提供（浅色 #fff / 暗色 #101B2D），随主题切换
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.06);
  height: 64px;
  z-index: 9;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 18px;
}
.breadcrumb {
  font-size: 14px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 8px;
  border-radius: 6px;
  &:hover {
    background: #eaf2ff;
  }
  .username {
    font-size: 14px;
    color: #1f2a37;
  }
}
.content {
  background: #f5f7fb;
  overflow-y: auto;
}
// 顶栏图标按钮：浅色 Header 上给一个明显可见的浅灰底 + 细描边，
// 避免按钮与白色 Header 融为一体、看不出这里是个可点击按钮。
.icon-btn {
  background: #f0f2f5 !important;
  color: #4b5563 !important;
  border: 1px solid #e8edf5 !important;
  &:hover {
    background: #eaf2ff !important;
    color: #2f6bff !important;
    border-color: #2f6bff !important;
  }
  &:focus,
  &:focus-visible {
    outline: none !important;
    box-shadow: none !important;
  }
}

// 暗色模式局部覆盖
html[data-theme='dark'] {
  .content {
    background: #0f1726;
  }
  .header {
    background: #101b2d;
  }
  .sider {
    background: linear-gradient(180deg, #132642 0%, #0d1a30 100%);
  }
  .username {
    color: #e8edf8;
  }
  .user-info:hover {
    background: rgba(255, 255, 255, 0.06);
  }
  .icon-btn {
    background: rgba(255, 255, 255, 0.08) !important;
    color: #c0ccdd !important;
    border-color: rgba(255, 255, 255, 0.12) !important;
    &:hover {
      background: rgba(47, 107, 255, 0.18) !important;
      color: #fff !important;
      border-color: #2f6bff !important;
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.page-enter-active {
  transition: all 0.25s ease-out;
}
.page-leave-active {
  transition: all 0.2s ease-in;
}
.page-enter-from {
  opacity: 0;
  transform: translateX(12px);
}
.page-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}
</style>
