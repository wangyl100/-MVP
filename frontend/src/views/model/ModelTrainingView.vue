<template>
  <div class="page-container">
    <PageHeader title="模型训练" description="深度学习模型训练全流程管理与监控">
      <template #extra>
        <a-button type="primary"><PlusOutlined /> 创建训练任务</a-button>
      </template>
    </PageHeader>

    <a-row :gutter="16" class="mb-24">
      <a-col :xs="12" :md="6" v-for="s in stats" :key="s.title">
        <div class="stat-card">
          <div class="flex-between">
            <div><div class="stat-title">{{ s.title }}</div><div class="stat-value">{{ s.value }}</div></div>
            <div class="stat-icon" :style="{background:s.color}"><component :is="s.icon" /></div>
          </div>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="16">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">训练任务监控</h3>
          <a-table :columns="cols" :data-source="trainingTasks" row-key="id" :pagination="{pageSize:6}">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-badge :status="statusMap[record.status].status" :text="statusMap[record.status].text" />
              </template>
              <template v-else-if="column.key === 'epoch'">
                <a-progress :percent="parseInt(record.epoch.split('/')[0])/parseInt(record.epoch.split('/')[1])*100" size="small" />
                <span style="font-size:11px;color:#6b7280">{{ record.epoch }}</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" v-if="record.status==='running'">暂停</a-button>
                <a-button type="link" size="small">详情</a-button>
                <a-button type="link" size="small" danger v-if="record.status==='running'">停止</a-button>
              </template>
            </template>
          </a-table>
        </div>
      </a-col>
      <a-col :xs="24" :lg="8">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">GPU 资源</h3>
          <div class="gpu-list">
            <div class="gpu-item" v-for="g in gpus" :key="g.name">
              <div class="flex-between">
                <span><DashboardOutlined /> {{ g.name }}</span>
                <a-tag :color="g.util > 80 ? 'red' : 'green'">{{ g.util }}%</a-tag>
              </div>
              <a-progress :percent="g.util" :stroke-color="g.util > 80 ? '#ff4d4f' : '#52c41a'" size="small" />
              <div class="text-secondary" style="font-size:11px;margin-top:4px">显存: {{ g.mem }} / 温度: {{ g.temp }}°C</div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 训练曲线 -->
    <div class="page-card">
      <div class="flex-between mb-16">
        <h3 style="margin:0;font-size:16px">训练曲线 — BERT-NER-Medical-v2.4</h3>
        <a-radio-group size="small" v-model:value="curveMetric">
          <a-radio-button value="loss">Loss</a-radio-button>
          <a-radio-button value="acc">Accuracy</a-radio-button>
          <a-radio-button value="f1">F1</a-radio-button>
        </a-radio-group>
      </div>
      <EChart :option="curveOption" height="320px" />
    </div>

    <!-- 训练配置 -->
    <div class="page-card mt-16">
      <h3 style="margin:0 0 16px;font-size:16px">训练配置模板</h3>
      <a-row :gutter="16">
        <a-col :xs="24" :md="12" :lg="6" v-for="t in templates" :key="t.name">
          <a-card size="small" hoverable>
            <h4 style="margin:0;font-size:14px">{{ t.name }}</h4>
            <p class="text-secondary" style="font-size:12px;margin:6px 0">{{ t.desc }}</p>
            <a-descriptions :column="1" size="small">
              <a-descriptions-item label="模型">{{ t.model }}</a-descriptions-item>
              <a-descriptions-item label="LR">{{ t.lr }}</a-descriptions-item>
              <a-descriptions-item label="Batch">{{ t.batch }}</a-descriptions-item>
              <a-descriptions-item label="Epoch">{{ t.epoch }}</a-descriptions-item>
            </a-descriptions>
            <a-button type="primary" size="small" block style="margin-top:8px">使用模板</a-button>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import EChart from '@/components/EChart.vue'
import { PlusOutlined, ExperimentOutlined, DashboardOutlined, ThunderboltOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'
import { trainingTasks } from '@/utils/mock'

const stats = [
  { title: '训练任务', value: 8, color: 'linear-gradient(135deg,#1677ff,#4096ff)', icon: ExperimentOutlined },
  { title: '运行中', value: 2, color: 'linear-gradient(135deg,#fa8c16,#ffa940)', icon: ThunderboltOutlined },
  { title: '已完成', value: 5, color: 'linear-gradient(135deg,#52c41a,#73d13d)', icon: CheckCircleOutlined },
  { title: 'GPU 使用', value: '6/8', color: 'linear-gradient(135deg,#722ed1,#9254de)', icon: DashboardOutlined }
]

const statusMap: Record<string, { status: any; text: string }> = {
  running: { status: 'processing', text: '运行中' },
  success: { status: 'success', text: '已完成' },
  failed: { status: 'error', text: '失败' }
}

const cols = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '基础模型', dataIndex: 'model', key: 'model', width: 140 },
  { title: 'Epoch', key: 'epoch', width: 160 },
  { title: 'Loss', dataIndex: 'loss', key: 'loss', width: 90 },
  { title: 'Acc', dataIndex: 'accuracy', key: 'accuracy', width: 90 },
  { title: 'GPU', dataIndex: 'gpu', key: 'gpu', width: 100 },
  { title: '状态', key: 'status', width: 110 },
  { title: '操作', key: 'action', width: 160 }
]

const gpus = [
  { name: 'GPU 0 (RTX 4090)', util: 92, mem: '22GB/24GB', temp: 72 },
  { name: 'GPU 1 (RTX 4090)', util: 88, mem: '20GB/24GB', temp: 68 },
  { name: 'GPU 2 (A100)', util: 76, mem: '64GB/80GB', temp: 65 },
  { name: 'GPU 3 (A100)', util: 45, mem: '32GB/80GB', temp: 58 }
]

const curveMetric = ref('loss')
const curveOption = computed(() => {
  const data = curveMetric.value === 'loss'
    ? [1.82, 1.45, 1.12, 0.85, 0.64, 0.48, 0.36, 0.28, 0.22, 0.18, 0.15, 0.13]
    : curveMetric.value === 'acc'
      ? [0.42, 0.58, 0.68, 0.75, 0.81, 0.85, 0.88, 0.90, 0.91, 0.92, 0.93, 0.94]
      : [0.35, 0.52, 0.65, 0.73, 0.79, 0.83, 0.86, 0.88, 0.89, 0.90, 0.91, 0.92]
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['训练集', '验证集'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: { type: 'category', name: 'Epoch', data: Array.from({length:12},(_,i)=>i+1) },
    yAxis: { type: 'value', name: curveMetric.value.toUpperCase() },
    series: [
      { name: '训练集', type: 'line', smooth: true, data, itemStyle: { color: '#1677ff' } },
      { name: '验证集', type: 'line', smooth: true, data: data.map((v:number)=>v*(curveMetric.value==='loss'?1.08:0.96)), itemStyle: { color: '#fa8c16' } }
    ]
  }
})

const templates = [
  { name: 'BERT-NER 标准模板', desc: '基于 BERT 的命名实体识别', model: 'BERT-base', lr: '2e-5', batch: 32, epoch: 30 },
  { name: 'RoBERTa-RE 模板', desc: '关系抽取训练配置', model: 'RoBERTa-large', lr: '1e-5', batch: 16, epoch: 50 },
  { name: 'TPLinker 联合模板', desc: '实体关系联合抽取', model: 'TPLinker', lr: '5e-5', batch: 24, epoch: 40 },
  { name: 'BiLSTM-CRF 模板', desc: '轻量级 NER 方案', model: 'BiLSTM-CRF', lr: '1e-3', batch: 64, epoch: 100 }
]
</script>

<style lang="less" scoped>
.gpu-list { display: flex; flex-direction: column; gap: 12px; }
.gpu-item { padding: 12px; background: #fafbfc; border-radius: 8px; }
</style>
