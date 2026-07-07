<template>
  <div class="page-header">
    <div class="page-header-left">
      <h2 class="title">{{ title }}</h2>
      <p v-if="description" class="description">{{ description }}</p>
    </div>
    <div class="page-header-right">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style lang="less" scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  .title {
    // §5.2 页面区块标题 16px
    font-size: 16px;
    font-weight: 600;
    color: #1f2a37;
    margin: 0;
    line-height: 24px;
  }
  .description {
    // §5.2 胶囊/标签/说明 12px
    font-size: 12px;
    color: #7b8794;
    margin: 6px 0 0;
  }
}
</style>
