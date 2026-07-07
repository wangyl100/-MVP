<template>
  <div class="not-found">
    <div class="content">
      <div class="code">404</div>
      <h2>页面未找到</h2>
      <p class="desc">抱歉，您访问的页面不存在或已被移除</p>
      <a-button type="primary" size="large" @click="router.push('/dashboard')">返回首页</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style lang="less" scoped>
.not-found {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  .content {
    text-align: center;
    .code {
      font-size: 120px;
      font-weight: 700;
      background: linear-gradient(135deg, #1677ff, #722ed1);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      line-height: 1;
    }
    h2 { font-size: 24px; color: #1f2937; margin: 16px 0 8px; }
    .desc { color: #6b7280; margin-bottom: 24px; }
  }
}
</style>
