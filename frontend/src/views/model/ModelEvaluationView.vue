<template>
  <div class="page-container">
    <PageHeader title="效果评估" description="模型性能评估、错误分析与 Badcase 管理" />

    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="8">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">评估模型选择</h3>
          <a-select v-model:value="modelId" style="width:100%;margin-bottom:12px">
            <a-select-option v-for="m in evalModels" :key="m.id" :value="m.id">{{ m.name }} ({{ m.version }})</a-select-option>
          </a-select>
          <a-button type="primary" block :loading="evaluating" @click="runEval"><ThunderboltOutlined /> 运行评估</a-button>
          <a-divider />
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="评估数据集">测试集 (2,580 条)</a-descriptions-item>
            <a-descriptions-item label="评估时间">2026-07-06 11:23</a-descriptions-item>
            <a-descriptions-item label="评估耗时">5 分 23 秒</a-descriptions-item>
          </a-descriptions>
        </div>
      </a-col>
      <a-col :xs="24" :lg="16">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">核心指标</h3>
          <a-row :gutter="16">
            <a-col :span="8">
              <div class="metric-card">
                <div class="metric-title">Precision</div>
                <div class="metric-value" style="color:#1677ff">90.1%</div>
                <a-progress :percent="90.1" :stroke-color="'#1677ff'" />
              </div>
            </a-col>
            <a-col :span="8">
              <div class="metric-card">
                <div class="metric-title">Recall</div>
                <div class="metric-value" style="color:#52c41a">88.4%</div>
                <a-progress :percent="88.4" :stroke-color="'#52c41a'" />
              </div>
            </a-col>
            <a-col :span="8">
              <div class="metric-card">
                <div class="metric-title">F1 Score</div>
                <div class="metric-value" style="color:#722ed1">89.2%</div>
                <a-progress :percent="89.2" :stroke-color="'#722ed1'" />
              </div>
            </a-col>
          </a-row>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">分类型性能</h3>
          <EChart :option="typePerfOption" height="320px" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">混淆矩阵</h3>
          <EChart :option="confusionOption" height="320px" />
        </div>
      </a-col>
    </a-row>

    <div class="page-card mb-24">
      <h3 style="margin:0 0 16px;font-size:16px">错误类型分析</h3>
      <a-table :columns="errCols" :data-source="errors" :pagination="false" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'rate'">
            <a-progress :percent="record.rate*100" size="small" />
          </template>
        </template>
      </a-table>
    </div>

    <div class="page-card">
      <div class="flex-between mb-16">
        <h3 style="margin:0;font-size:16px">Badcase 管理</h3>
        <a-button type="primary" size="small" @click="action.openCreate('添加 Badcase', '请填写 Badcase 文本、预测结果、真实标注及错误类型')"><PlusOutlined /> 添加 Badcase</a-button>
      </div>
      <a-table :columns="badCols" :data-source="badcases" row-key="id" :pagination="{pageSize:6}">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'"><a-tag :color="badTypeColor[record.type]">{{ record.type }}</a-tag></template>
          <template v-if="column.key === 'status'">
            <a-badge :status="record.status==='fixed'?'success':'warning'" :text="record.status==='fixed'?'已修复':'待处理'" />
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import EChart from '@/components/EChart.vue'
import { PlusOutlined, ThunderboltOutlined } from '@ant-design/icons-vue'
import { modelList } from '@/utils/mock'
import { useAction } from '@/composables/useAction'

const action = useAction()

const evalModels = modelList.filter((m) => m.f1 > 0)
const modelId = ref(evalModels[0]?.id)
const evaluating = ref(false)
function runEval() {
  evaluating.value = true
  setTimeout(() => { evaluating.value = false }, 1500)
}

const typePerfOption = {
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0, data: ['Precision','Recall','F1'] },
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
  xAxis: { type: 'category', data: ['人物','机构','地点','事件','概念','作品'] },
  yAxis: { type: 'value', max: 1, axisLabel: { formatter: (v:number)=>(v*100).toFixed(0)+'%' } },
  series: [
    { name:'Precision', type:'bar', data:[0.92,0.88,0.91,0.85,0.89,0.87], itemStyle:{color:'#1677ff'} },
    { name:'Recall', type:'bar', data:[0.90,0.86,0.89,0.82,0.87,0.85], itemStyle:{color:'#52c41a'} },
    { name:'F1', type:'bar', data:[0.91,0.87,0.90,0.83,0.88,0.86], itemStyle:{color:'#722ed1'} }
  ]
}

const confusionOption = {
  tooltip: {},
  grid: { left: '15%', right: '8%', bottom: '15%' },
  xAxis: { type: 'category', name: '预测', data: ['人物','机构','地点','其他'], splitArea: { show: true } },
  yAxis: { type: 'category', name: '真实', data: ['人物','机构','地点','其他'], splitArea: { show: true } },
  visualMap: { min: 0, max: 500, calculable: true, orient: 'horizontal', left: 'center', bottom: 0, inRange: { color: ['#f0f7ff','#1677ff'] } },
  series: [{
    type: 'heatmap',
    data: [
      [0,0,482],[0,1,12],[0,2,8],[0,3,5],
      [1,0,15],[1,1,398],[1,2,22],[1,3,8],
      [2,0,6],[2,1,18],[2,2,356],[2,3,4],
      [3,0,9],[3,1,11],[3,2,7],[3,3,289]
    ],
    label: { show: true }
  }]
}

const errCols = [
  { title: '错误类型', dataIndex: 'type', key: 'type' },
  { title: '数量', dataIndex: 'count', key: 'count', width: 100 },
  { title: '占比', key: 'rate', width: 200 },
  { title: '示例', dataIndex: 'example', key: 'example' }
]
const errors = [
  { type: '边界错误', count: 48, rate: 0.38, example: '"阿里巴巴集团" 被截断为 "阿里巴巴"' },
  { type: '类型错误', count: 32, rate: 0.25, example: '"苹果" 被识别为 ORG 实际是 PRODUCT' },
  { type: '漏标', count: 28, rate: 0.22, example: '"ChatGPT" 未被识别' },
  { type: '误标', count: 19, rate: 0.15, example: '"红色" 被误识别为实体' }
]

const badCols = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '文本', dataIndex: 'text', key: 'text' },
  { title: '预测', dataIndex: 'pred', key: 'pred', width: 140 },
  { title: '真实', dataIndex: 'gold', key: 'gold', width: 140 },
  { title: '类型', key: 'type', width: 100 },
  { title: '状态', key: 'status', width: 100 }
]
const badTypeColor: Record<string,string> = { '边界错误':'orange','类型错误':'red','漏标':'purple','误标':'blue' }
const badcases = [
  { id:'BC001', text:'阿里巴巴集团发布了财报', pred:'阿里巴巴', gold:'阿里巴巴集团', type:'边界错误', status:'pending' },
  { id:'BC002', text:'苹果手机销量增长', pred:'ORG', gold:'PRODUCT', type:'类型错误', status:'fixed' },
  { id:'BC003', text:'ChatGPT 由 OpenAI 开发', pred:'-', gold:'ChatGPT', type:'漏标', status:'pending' },
  { id:'BC004', text:'红色是暖色调', pred:'红色 → 实体', gold:'非实体', type:'误标', status:'fixed' }
]
</script>

<style lang="less" scoped>
.metric-card {
  text-align: center; padding: 16px; background: #fafbfc; border-radius: 8px;
  .metric-title { color: #6b7280; font-size: 13px; }
  .metric-value { font-size: 32px; font-weight: 700; margin: 8px 0; }
}
</style>
