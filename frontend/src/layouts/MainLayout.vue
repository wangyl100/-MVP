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
          <a-tooltip title="搜索">
            <a-button type="text" shape="circle">
              <SearchOutlined />
            </a-button>
          </a-tooltip>
          <a-tooltip title="通知">
            <a-badge :count="5" size="small">
              <a-button type="text" shape="circle">
                <BellOutlined />
              </a-button>
            </a-badge>
          </a-tooltip>
          <a-dropdown>
            <div class="user-info">
              <a-avatar style="background-color: #1677ff">
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
  const find = (list: MenuItem[]): string | undefined => {
    for (const item of list) {
      if (item.key === key) return item.path
      if (item.children) {
        const r = find(item.children)
        if (r) return r
      }
    }
  }
  const path = find(menuConfig)
  if (path) router.push(path)
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
  background: #001529;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
  overflow: auto;
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
    background: linear-gradient(135deg, #1677ff, #4096ff);
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
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
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
    background: #f5f7fa;
  }
  .username {
    font-size: 14px;
    color: #1f2937;
  }
}
.content {
  background: #f5f7fa;
  overflow-y: auto;
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
