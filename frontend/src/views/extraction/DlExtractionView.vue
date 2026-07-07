<template>
  <div class="page-container">
    <PageHeader title="深度学习抽取" description="基于训练好的深度学习模型进行高准确率知识抽取">
      <template #extra>
        <a-button type="primary"><PlusOutlined /> 新建抽取任务</a-button>
      </template>
    </PageHeader>

    <!-- 在线模型 -->
    <div class="page-card mb-24">
      <h3 style="margin:0 0 16px;font-size:16px">已部署模型</h3>
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="8" v-for="m in onlineModels" :key="m.id">
          <a-card hoverable size="small" class="model-card">
            <div class="flex-between">
              <div>
                <h4 style="margin:0;font-size:14px">{{ m.name }}</h4>
                <div class="text-secondary" style="font-size:12px">{{ m.type }} · {{ m.version }}</div>
              </div>
              <a-badge status="success" text="在线" />
            </div>
            <a-divider style="margin:8px 0" />
            <a-space direction="vertical" style="width:100%" :size="4">
              <div class="text-secondary" style="font-size:12px">F1: <b style="color:#52c41a">{{ m.f1 }}</b> · 延迟: {{ m.latency }}ms</div>
              <div class="text-secondary" style="font-size:12px">QPS: {{ m.qps }} · 调用: {{ m.calls }}</div>
            </a-space>
            <a-button type="primary" size="small" block style="margin-top:8px">使用此模型</a-button>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <a-row :gutter="16" class="mb-24">
      <!-- 实时抽取测试 -->
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px"><ThunderboltOutlined style="color:#1677ff" /> 实时抽取测试</h3>
          <a-select v-model:value="selectedModel" style="width:100%;margin-bottom:12px">
            <a-select-option v-for="m in onlineModels" :key="m.id" :value="m.id">{{ m.name }} ({{ m.type }})</a-select-option>
          </a-select>
          <a-textarea v-model:value="testText" :rows="4" placeholder="输入待抽取文本" />
          <a-button type="primary" block style="margin-top:12px" :loading="testing" @click="runTest">
            <ThunderboltOutlined /> 实时抽取
          </a-button>
          <div v-if="testResult" class="test-result">
            <a-divider style="margin:12px 0" />
            <a-tag v-for="(e, i) in testResult.entities" :key="i" :color="entityColor[e.type]" style="margin:2px">{{ e.text }} ({{ e.type }} {{ (e.score*100).toFixed(0) }}%)</a-tag>
            <div style="margin-top:8px;font-size:12px;color:#6b7280">耗时: {{ testResult.time }}ms</div>
          </div>
        </div>
      </a-col>

      <!-- 模型对比 -->
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">模型效果对比</h3>
          <a-table :columns="compareColumns" :data-source="compareData" :pagination="false" size="small">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name'"><strong>{{ record.name }}</strong></template>
              <template v-else-if="column.key === 'f1'">
                <a-progress :percent="record.f1*100" :stroke-color="record.f1>0.85?'#52c41a':'#faad14'" size="small" />
              </template>
            </template>
          </a-table>
        </div>
      </a-col>
    </a-row>

    <!-- 批量抽取任务 -->
    <div class="page-card">
      <div class="flex-between mb-16">
        <h3 style="margin:0;font-size:16px">批量抽取任务</h3>
        <a-button type="primary" size="small"><PlusOutlined /> 新建批量任务</a-button>
      </div>
      <a-table :columns="taskColumns" :data-source="dlTasks" row-key="id" :pagination="{pageSize:8}">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge :status="statusMap[record.status].status" :text="statusMap[record.status].text" />
          </template>
          <template v-else-if="column.key === 'progress'">
            <a-progress :percent="record.progress" size="small" />
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { PlusOutlined, ThunderboltOutlined } from '@ant-design/icons-vue'
import { modelList } from '@/utils/mock'

const onlineModels = modelList.filter((m) => m.status === 'online').map((m) => ({
  ...m, latency: [45, 62, 38][Math.floor(Math.random() * 3)], qps: [120, 85, 200][Math.floor(Math.random() * 3)], calls: Math.floor(Math.random() * 10000)
}))

const selectedModel = ref(onlineModels[0]?.id)
const testText = ref('')
const testing = ref(false)
const testResult = ref<any>(null)

const entityColor: Record<string, string> = {
  PER: '#1677ff', ORG: '#52c41a', LOC: '#faad14', TIME: '#13c2c2', EVT: '#ff4d4f'
}

function runTest() {
  if (!testText.value) return
  testing.value = true
  setTimeout(() => {
    testing.value = false
    testResult.value = {
      entities: [
        { text: '张三', type: 'PER', score: 0.98 },
        { text: '阿里巴巴', type: 'ORG', score: 0.95 },
        { text: '杭州', type: 'LOC', score: 0.92 }
      ],
      time: 52
    }
  }, 800)
}

const compareColumns = [
  { title: '模型', key: 'name' },
  { title: 'Precision', dataIndex: 'precision', key: 'precision', customRender: ({ text }: any) => (text * 100).toFixed(1) + '%' },
  { title: 'Recall', dataIndex: 'recall', key: 'recall', customRender: ({ text }: any) => (text * 100).toFixed(1) + '%' },
  { title: 'F1', key: 'f1' }
]
const compareData = modelList.filter((m) => m.f1 > 0)

const statusMap: Record<string, { status: any; text: string }> = {
  running: { status: 'processing', text: '运行中' },
  success: { status: 'success', text: '成功' },
  failed: { status: 'error', text: '失败' },
  pending: { status: 'warning', text: '排队中' }
}

const taskColumns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 110 },
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '使用模型', dataIndex: 'model', key: 'model', width: 160 },
  { title: '语料数', dataIndex: 'count', key: 'count', width: 90 },
  { title: '进度', key: 'progress', width: 200 },
  { title: '状态', key: 'status', width: 110 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 160 }
]

const dlTasks = Array.from({ length: 8 }, (_, i) => ({
  id: `DL${String(i + 1).padStart(4, '0')}`,
  name: ['医疗 NER 批量抽取', '金融 RE 批量抽取', '法律事件抽取', '百科联合抽取'][i % 4],
  model: onlineModels[i % onlineModels.length]?.name || 'BERT-NER',
  count: [500, 1200, 800, 320][i % 4],
  progress: [100, 65, 30, 0][i % 4],
  status: ['success', 'running', 'running', 'pending'][i % 4],
  startTime: `2026-07-0${(i % 6) + 1} ${10 + i}:23`
}))
</script>

<style lang="less" scoped>
.model-card { margin-bottom: 16px; }
.test-result {
  margin-top: 12px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
}
</style>
