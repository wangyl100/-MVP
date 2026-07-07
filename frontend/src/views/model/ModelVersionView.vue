<template>
  <div class="page-container">
    <PageHeader title="版本管理" description="模型版本时间线、对比与上下线管理" />

    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="14">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">版本时间线 — BERT-NER-Medical</h3>
          <a-timeline mode="left">
            <a-timeline-item v-for="(v, i) in versions" :key="v.version" :color="i===0?'green':'blue'">
              <div class="version-item" :class="{ active: i===0 }">
                <div class="flex-between">
                  <a-space>
                    <a-tag :color="i===0?'green':'blue'">{{ v.version }}</a-tag>
                    <span v-if="i===0"><a-badge status="success" text="当前版本" /></span>
                    <span v-if="v.tag"><a-tag color="gold">{{ v.tag }}</a-tag></span>
                  </a-space>
                  <span class="text-secondary" style="font-size:12px">{{ v.time }}</span>
                </div>
                <p style="margin:8px 0 4px;color:#1f2937">{{ v.desc }}</p>
                <a-space wrap>
                  <a-tag>F1: {{ v.f1 }}</a-tag>
                  <a-tag>P: {{ v.precision }}</a-tag>
                  <a-tag>R: {{ v.recall }}</a-tag>
                  <a-tag>数据: {{ v.dataset }}</a-tag>
                </a-space>
                <div style="margin-top:8px">
                  <a-button type="link" size="small" v-if="i!==0">上线</a-button>
                  <a-button type="link" size="small" v-if="i!==0">回滚</a-button>
                  <a-button type="link" size="small">详情</a-button>
                  <a-button type="link" size="small">导出</a-button>
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
        </div>
      </a-col>
      <a-col :xs="24" :lg="10">
        <div class="page-card mb-24">
          <h3 style="margin:0 0 16px;font-size:16px">版本对比</h3>
          <a-space direction="vertical" style="width:100%" :size="8">
            <a-select v-model:value="v1" style="width:100%"><a-select-option v-for="v in versions" :key="v.version" :value="v.version">{{ v.version }}</a-select-option></a-select>
            <a-select v-model:value="v2" style="width:100%"><a-select-option v-for="v in versions" :key="v.version" :value="v.version">{{ v.version }}</a-select-option></a-select>
          </a-space>
          <a-divider />
          <EChart :option="compareOption" height="240px" />
        </div>
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">所有模型</h3>
          <a-table :columns="cols" :data-source="modelList" row-key="id" :pagination="false" size="small">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-badge :status="modelStatus[record.status].status" :text="modelStatus[record.status].text" />
              </template>
              <template v-else-if="column.key === 'f1'"><a-tag color="green">{{ (record.f1*100).toFixed(1) }}%</a-tag></template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small">版本</a-button>
                <a-button type="link" size="small">导出</a-button>
              </template>
            </template>
          </a-table>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import EChart from '@/components/EChart.vue'
import { modelList } from '@/utils/mock'

const versions = [
  { version: 'v2.3.1', time: '2026-07-04', desc: '修复医疗术语边界识别问题，提升长实体识别', f1: '89.2%', precision: '90.1%', recall: '88.4%', dataset: '12,580', tag: 'current' },
  { version: 'v2.3.0', time: '2026-07-01', desc: '增加药品实体类型，扩展训练数据', f1: '87.6%', precision: '88.5%', recall: '86.8%', dataset: '10,200', tag: 'stable' },
  { version: 'v2.2.0', time: '2026-06-25', desc: '优化少样本场景的抽取能力', f1: '86.2%', precision: '87.1%', recall: '85.3%', dataset: '8,500', tag: '' },
  { version: 'v2.1.0', time: '2026-06-15', desc: '切换预训练模型底座为 RoBERTa-large', f1: '84.8%', precision: '85.6%', recall: '84.0%', dataset: '6,800', tag: '' },
  { version: 'v2.0.0', time: '2026-06-01', desc: '架构升级，采用 BERT+CRF 结构', f1: '82.5%', precision: '83.2%', recall: '81.8%', dataset: '5,000', tag: '' },
  { version: 'v1.0.0', time: '2026-05-15', desc: '初始版本，BiLSTM-CRF 基线模型', f1: '76.3%', precision: '77.8%', recall: '74.9%', dataset: '2,000', tag: '' }
]

const v1 = ref('v2.3.1')
const v2 = ref('v2.0.0')

const compareOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0, data: [v1.value, v2.value] },
  grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
  xAxis: { type: 'category', data: ['Precision','Recall','F1'] },
  yAxis: { type: 'value', min: 0.7, max: 1, axisLabel: { formatter: (v:number)=>(v*100).toFixed(0)+'%' } },
  series: [
    { name: v1.value, type: 'bar', data: [0.901,0.884,0.892], itemStyle:{color:'#1677ff'} },
    { name: v2.value, type: 'bar', data: [0.832,0.818,0.825], itemStyle:{color:'#fa8c16'} }
  ]
}))

const modelStatus: Record<string,{status:any;text:string}> = {
  online: { status: 'success', text: '在线' },
  offline: { status: 'default', text: '离线' },
  training: { status: 'processing', text: '训练中' }
}
const cols = [
  { title: '模型名称', dataIndex: 'name', key: 'name' },
  { title: '类型', dataIndex: 'type', key: 'type', width: 80 },
  { title: '版本', dataIndex: 'version', key: 'version', width: 90 },
  { title: 'F1', key: 'f1', width: 90 },
  { title: '状态', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 120 }
]
</script>

<style lang="less" scoped>
.version-item {
  padding: 12px; border-radius: 8px; border: 1px solid #f0f0f0;
  &.active { border-color: #52c41a; background: #f6ffed; }
}
</style>
