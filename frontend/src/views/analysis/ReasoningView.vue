<template>
  <div class="page-container">
    <PageHeader title="知识推理" description="基于图谱的知识推理引擎，支持规则推理与神经网络推理" />

    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="8">
        <!-- 推理配置 -->
        <div class="page-card mb-24">
          <h3 style="margin:0 0 16px;font-size:16px">推理配置</h3>
          <a-form layout="vertical">
            <a-form-item label="推理引擎">
              <a-radio-group v-model:value="engine" button-style="solid" style="width:100%">
                <a-radio-button value="rule" style="width:33.3%;text-align:center">规则推理</a-radio-button>
                <a-radio-button value="transE" style="width:33.3%;text-align:center">TransE</a-radio-button>
                <a-radio-button value="gnn" style="width:33.3%;text-align:center">图神经网络</a-radio-button>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="推理深度">
              <a-slider v-model:value="depth" :min="1" :max="5" :marks="{ 1: '1跳', 2: '2跳', 3: '3跳', 4: '4跳', 5: '5跳' }" />
            </a-form-item>
            <a-form-item label="置信度阈值">
              <a-slider v-model:value="threshold" :min="0" :max="1" :step="0.05" />
              <div class="text-secondary" style="font-size:12px">当前阈值：{{ threshold.toFixed(2) }}</div>
            </a-form-item>
            <a-form-item label="推理规则集">
              <a-select v-model:value="ruleSet" mode="multiple" style="width:100%" placeholder="选择规则集">
                <a-select-option value="symmetry">对称关系规则</a-select-option>
                <a-select-option value="inverse">逆关系规则</a-select-option>
                <a-select-option value="transitive">传递关系规则</a-select-option>
                <a-select-option value="hierarchy">层级关系规则</a-select-option>
                <a-select-option value="domain">领域规则</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" block @click="runReasoning" :loading="reasoning">
                <template #icon><ThunderboltOutlined /></template>
                启动推理
              </a-button>
            </a-form-item>
          </a-form>
        </div>

        <!-- 规则模板 -->
        <div class="page-card">
          <h3 style="margin:0 0 12px;font-size:16px">规则模板</h3>
          <a-list :data-source="ruleTemplates" size="small">
            <template #renderItem="{ item }">
              <a-list-item style="padding:8px 0">
                <a-tooltip :title="item.rule">
                  <a-space>
                    <a-tag :color="item.enabled ? 'green' : 'default'">{{ item.enabled ? '启用' : '禁用' }}</a-tag>
                    <span style="font-size:13px">{{ item.name }}</span>
                  </a-space>
                </a-tooltip>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </a-col>

      <a-col :xs="24" :lg="16">
        <!-- 推理结果 -->
        <div class="page-card mb-24">
          <div class="flex-between mb-16">
            <h3 style="margin:0;font-size:16px">推理结果</h3>
            <a-space>
              <a-tag color="blue">发现 {{ resultList.length }} 条新关系</a-tag>
              <a-button size="small"><DownloadOutlined /> 导出</a-button>
            </a-space>
          </div>
          <a-table
            :columns="resultCols"
            :data-source="resultList"
            row-key="id"
            :pagination="{ pageSize: 6 }"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'confidence'">
                <a-progress :percent="Number((record.confidence * 100).toFixed(1))" size="small"
                  :stroke-color="record.confidence >= 0.9 ? '#52c41a' : record.confidence >= 0.7 ? '#faad14' : '#ff4d4f'" />
              </template>
              <template v-else-if="column.key === 'rule'">
                <a-tag color="purple">{{ record.rule }}</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small">采纳</a-button>
                <a-button type="link" size="small" danger>拒绝</a-button>
              </template>
            </template>
          </a-table>
        </div>

        <!-- 推理路径可视化 -->
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">推理路径示例</h3>
          <div v-for="(path, i) in reasoningPaths" :key="i" class="path-block">
            <div class="path-meta">
              <a-tag color="blue">路径 {{ i + 1 }}</a-tag>
              <span class="text-secondary" style="font-size:12px">置信度 {{ path.confidence }} · 用时 {{ path.duration }}ms</span>
            </div>
            <div class="path-chain">
              <template v-for="(n, j) in path.nodes" :key="j">
                <span class="path-node" :style="{ background: path.colors[j] + '22', color: path.colors[j], border: `1px solid ${path.colors[j]}` }">{{ n }}</span>
                <span v-if="j < path.edges.length" class="path-edge">—{{ path.edges[j] }}→</span>
              </template>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 推理统计 -->
    <a-row :gutter="16">
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">推理引擎对比</h3>
          <EChart :option="engineCompareOption" height="280px" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">推理性能趋势</h3>
          <EChart :option="performanceOption" height="280px" />
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { PageHeader } from '@/components/PageHeader.vue'
import EChart from '@/components/EChart.vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined, DownloadOutlined } from '@ant-design/icons-vue'

const engine = ref<'rule' | 'transE' | 'gnn'>('rule')
const depth = ref(3)
const threshold = ref(0.7)
const ruleSet = ref<string[]>(['symmetry', 'inverse', 'transitive'])
const reasoning = ref(false)

const ruleTemplates = [
  { name: '对称关系规则', rule: 'IF (A, R, B) THEN (B, R, A)', enabled: true },
  { name: '逆关系规则', rule: 'IF (A, R1, B) AND R1.inverseOf=R2 THEN (B, R2, A)', enabled: true },
  { name: '传递关系规则', rule: 'IF (A, R, B) AND (B, R, C) THEN (A, R, C)', enabled: true },
  { name: '层级关系规则', rule: 'IF A subClassOf B AND (B, R, C) THEN (A, R, C)', enabled: true },
  { name: '领域规则', rule: '自定义业务领域规则集', enabled: false }
]

const resultCols = [
  { title: '头实体', dataIndex: 'source', key: 'source' },
  { title: '关系', dataIndex: 'relation', key: 'relation', width: 100 },
  { title: '尾实体', dataIndex: 'target', key: 'target' },
  { title: '应用规则', dataIndex: 'rule', key: 'rule', width: 120 },
  { title: '置信度', dataIndex: 'confidence', key: 'confidence', width: 160 },
  { title: '操作', key: 'action', width: 130 }
]

const resultList = ref([
  { id: 1, source: '马云', relation: '投资', target: '蚂蚁集团', rule: '传递关系', confidence: 0.92 },
  { id: 2, source: '蔡崇信', relation: '合作', target: '马云', rule: '对称关系', confidence: 0.95 },
  { id: 3, source: '阿里巴巴', relation: '总部位于', target: '杭州', rule: '逆关系', confidence: 0.88 },
  { id: 4, source: '百度', relation: '竞争', target: '阿里巴巴', rule: '对称关系', confidence: 0.81 },
  { id: 5, source: '李彦宏', relation: '创立', target: '百度', rule: '层级关系', confidence: 0.94 },
  { id: 6, source: '腾讯', relation: '投资', target: '美团', rule: '传递关系', confidence: 0.79 },
  { id: 7, source: '马化腾', relation: '总部位于', target: '深圳', rule: '逆关系', confidence: 0.86 }
])

const reasoningPaths = [
  {
    nodes: ['马云', '阿里巴巴', '杭州', '浙江'],
    edges: ['创立', '位于', '属于'],
    colors: ['#1677ff', '#52c41a', '#faad14', '#722ed1'],
    confidence: 0.92, duration: 38
  },
  {
    nodes: ['马化腾', '腾讯', '深圳', '广东'],
    edges: ['创立', '位于', '属于'],
    colors: ['#1677ff', '#52c41a', '#faad14', '#722ed1'],
    confidence: 0.88, duration: 42
  },
  {
    nodes: ['李彦宏', '百度', '北京', '中国'],
    edges: ['创立', '位于', '属于'],
    colors: ['#1677ff', '#52c41a', '#faad14', '#722ed1'],
    confidence: 0.91, duration: 35
  }
]

const engineCompareOption = {
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0, data: ['准确率', '召回率', 'F1'] },
  grid: { left: 50, right: 20, top: 20, bottom: 50 },
  xAxis: { type: 'category', data: ['规则推理', 'TransE', 'RotatE', 'ComplEx', '图神经网络'] },
  yAxis: { type: 'value', max: 1, axisLabel: { formatter: '{value}%' } },
  series: [
    { name: '准确率', type: 'bar', data: [88, 82, 84, 83, 91], itemStyle: { color: '#1677ff' } },
    { name: '召回率', type: 'bar', data: [76, 79, 81, 80, 87], itemStyle: { color: '#52c41a' } },
    { name: 'F1', type: 'bar', data: [81, 80, 82, 81, 89], itemStyle: { color: '#faad14' } }
  ]
}

const performanceOption = {
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0 },
  grid: { left: 50, right: 30, top: 20, bottom: 50 },
  xAxis: { type: 'category', data: ['06-30', '07-01', '07-02', '07-03', '07-04', '07-05', '07-06'] },
  yAxis: [
    { type: 'value', name: '推理数量', position: 'left' },
    { type: 'value', name: '耗时(ms)', position: 'right' }
  ],
  series: [
    { name: '推理数量', type: 'line', smooth: true, data: [820, 932, 901, 1290, 1330, 1520, 1680], itemStyle: { color: '#1677ff' } },
    { name: '耗时(ms)', type: 'line', smooth: true, yAxisIndex: 1, data: [42, 38, 45, 41, 39, 36, 35], itemStyle: { color: '#52c41a' } }
  ]
}

function runReasoning() {
  reasoning.value = true
  message.info(`启动 ${engine.value} 推理引擎，推理深度 ${depth.value} 跳`)
  setTimeout(() => {
    reasoning.value = false
    message.success(`推理完成，发现 ${resultList.value.length} 条新关系`)
  }, 1500)
}
</script>

<style lang="less" scoped>
.path-block {
  padding: 12px; background: #fafafa; border-radius: 8px; margin-bottom: 12px;
  &:last-child { margin-bottom: 0; }
}
.path-meta { margin-bottom: 10px; }
.path-chain { display: flex; flex-wrap: wrap; align-items: center; }
.path-node {
  padding: 4px 12px; border-radius: 16px; font-size: 13px; font-weight: 500; margin: 4px 0;
}
.path-edge { margin: 0 8px; color: #9ca3af; font-size: 12px; }
</style>
