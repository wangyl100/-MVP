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
    font-size: 22px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
  }
  .description {
    font-size: 13px;
    color: #6b7280;
    margin: 6px 0 0;
  }
}
</style>
