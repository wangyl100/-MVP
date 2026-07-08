<template>
  <div class="page-container">
    <PageHeader title="质量控制" description="标注质量评估、审核与一致性检查" />

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
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">标注准确率趋势</h3>
          <EChart :option="trendOption" height="280px" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">标注人员质量分布</h3>
          <EChart :option="barOption" height="280px" />
        </div>
      </a-col>
    </a-row>

    <div class="page-card mb-24">
      <h3 style="margin:0 0 16px;font-size:16px">待审核标注</h3>
      <a-table :columns="reviewCols" :data-source="reviews" row-key="id" :pagination="{pageSize:6}">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" style="color:#52c41a" @click="action.notify('通过', record.id || '审核项')">通过</a-button>
            <a-button type="link" size="small" danger @click="action.notify('驳回', record.id || '审核项')">驳回</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <div class="page-card">
      <h3 style="margin:0 0 16px;font-size:16px">一致性检查（交叉验证）</h3>
      <a-alert type="warning" show-icon style="margin-bottom:12px">
        <template #message>检测到 <b>23</b> 处标注不一致，建议仲裁处理</template>
      </a-alert>
      <a-table :columns="inconsCols" :data-source="inconsistencies" :pagination="false" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'conflict'">
            <a-tag color="red">{{ record.label1 }} vs {{ record.label2 }}</a-tag>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import EChart from '@/components/EChart.vue'
import { CheckCircleOutlined, WarningOutlined, AuditOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { useAction } from '@/composables/useAction'

const action = useAction()

const stats = [
  { title: '平均准确率', value: '93.5%', color: 'linear-gradient(135deg,#52c41a,#73d13d)', icon: CheckCircleOutlined },
  { title: '待审核', value: 156, color: 'linear-gradient(135deg,#fa8c16,#ffa940)', icon: AuditOutlined },
  { title: '不一致项', value: 23, color: 'linear-gradient(135deg,#ff4d4f,#ff7875)', icon: WarningOutlined },
  { title: '审核人员', value: 8, color: 'linear-gradient(135deg,#1677ff,#4096ff)', icon: TeamOutlined }
]

const trendOption = {
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0, data: ['准确率', '一致性'] },
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
  xAxis: { type: 'category', data: ['周一','周二','周三','周四','周五','周六','周日'] },
  yAxis: { type: 'value', min: 0.7, max: 1, axisLabel: { formatter: (v:number)=>(v*100).toFixed(0)+'%' } },
  series: [
    { name: '准确率', type: 'line', smooth: true, data: [0.89,0.91,0.92,0.93,0.94,0.93,0.94], itemStyle:{color:'#52c41a'} },
    { name: '一致性', type: 'line', smooth: true, data: [0.85,0.87,0.88,0.86,0.89,0.9,0.91], itemStyle:{color:'#1677ff'} }
  ]
}

const barOption = {
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '5%', containLabel: true },
  xAxis: { type: 'category', data: ['张三','李四','王五','赵六','钱七','孙八'] },
  yAxis: { type: 'value', max: 1, axisLabel: { formatter: (v:number)=>(v*100).toFixed(0)+'%' } },
  series: [{
    type: 'bar', data: [0.95,0.92,0.94,0.88,0.91,0.93],
    itemStyle: { color: '#1677ff', borderRadius: [6,6,0,0] },
    barWidth: 30
  }]
}

const reviewCols = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '文本片段', dataIndex: 'text', key: 'text' },
  { title: '标注内容', dataIndex: 'label', key: 'label' },
  { title: '标注人', dataIndex: 'annotator', key: 'annotator', width: 90 },
  { title: '提交时间', dataIndex: 'time', key: 'time', width: 140 },
  { title: '操作', key: 'action', width: 140 }
]
const reviews = Array.from({length:12},(_,i)=>({
  id: `RV${i+1}`, text: '...OpenAI发布了o1模型...', label: 'o1 → PRODUCT', annotator: ['张三','李四','王五'][i%3], time: `2026-07-0${(i%6)+1} ${10+i}:23`
}))

const inconsCols = [
  { title: '文本', dataIndex: 'text', key: 'text' },
  { title: '冲突标注', key: 'conflict' },
  { title: '标注人1', dataIndex: 'a1', key: 'a1', width: 90 },
  { title: '标注人2', dataIndex: 'a2', key: 'a2', width: 90 },
  { title: '操作', key: 'action', width: 120 }
]
const inconsistencies = [
  { text: '苹果', label1: 'ORG', label2: 'PRODUCT', a1: '张三', a2: '李四' },
  { text: 'Amazon', label1: 'ORG', label2: 'LOC', a1: '王五', a2: '赵六' },
  { text: '华为Mate60', label1: 'PRODUCT', label2: 'ORG', a1: '钱七', a2: '孙八' }
]
</script>
