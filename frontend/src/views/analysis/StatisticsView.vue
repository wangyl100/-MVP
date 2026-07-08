<template>
  <div class="page-container">
    <PageHeader title="图谱统计分析" description="知识图谱的整体统计、分布特征与图谱指标分析">
      <template #extra>
        <a-space>
          <a-select v-model:value="project" style="width:200px">
            <a-select-option value="all">全部项目</a-select-option>
            <a-select-option v-for="p in projectList" :key="p.id" :value="p.id">{{ p.name }}</a-select-option>
          </a-select>
          <a-range-picker />
          <a-button type="primary" @click="action.notify('导出', '报告')"><ExportOutlined /> 导出报告</a-button>
        </a-space>
      </template>
    </PageHeader>

    <!-- 核心指标卡片 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="12" :md="6" v-for="s in stats" :key="s.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: s.bg, color: s.color }">
            <component :is="s.icon" />
          </div>
          <div>
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
            <div class="stat-trend" :class="s.trend >= 0 ? 'up' : 'down'">
              <ArrowUpOutlined v-if="s.trend >= 0" /> <ArrowDownOutlined v-else />
              {{ Math.abs(s.trend) }}% 较上周
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 实体/关系类型分布 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">实体类型分布</h3>
          <EChart :option="entityDistOption" height="300px" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">关系类型分布</h3>
          <EChart :option="relationDistOption" height="300px" />
        </div>
      </a-col>
    </a-row>

    <!-- 增长趋势 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="16">
        <div class="page-card">
          <div class="flex-between mb-16">
            <h3 style="margin:0;font-size:16px">知识增长趋势</h3>
            <a-radio-group v-model:value="trendRange" button-style="solid" size="small">
              <a-radio-button value="7">近7天</a-radio-button>
              <a-radio-button value="30">近30天</a-radio-button>
              <a-radio-button value="90">近90天</a-radio-button>
            </a-radio-group>
          </div>
          <EChart :option="growthOption" height="320px" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="8">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">数据来源占比</h3>
          <EChart :option="sourcePieOption" height="320px" />
        </div>
      </a-col>
    </a-row>

    <!-- 图谱指标 -->
    <div class="page-card mb-24">
      <h3 style="margin:0 0 16px;font-size:16px">图谱结构指标</h3>
      <a-row :gutter="16">
        <a-col :xs="12" :md="8" :lg="4" v-for="m in graphMetricsList" :key="m.label">
          <div class="metric-card">
            <div class="metric-value" :style="{ color: m.color }">{{ m.value }}</div>
            <div class="metric-label">{{ m.label }}</div>
            <div class="metric-desc text-secondary">{{ m.desc }}</div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 项目维度统计 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="14">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">各项目知识规模</h3>
          <a-table :columns="projectCols" :data-source="projectStats" row-key="id" :pagination="false" size="middle">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name'">
                <a-tag :color="record.color">{{ record.name }}</a-tag>
              </template>
              <template v-else-if="column.key === 'entity'">
                <a-progress :percent="record.entityPercent" size="small" :stroke-color="record.color" />
              </template>
              <template v-else-if="column.key === 'relation'">
                <a-progress :percent="record.relationPercent" size="small" :stroke-color="record.color" />
              </template>
            </template>
          </a-table>
        </div>
      </a-col>
      <a-col :xs="24" :lg="10">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">活跃度热力图</h3>
          <EChart :option="heatmapOption" height="320px" />
        </div>
      </a-col>
    </a-row>

    <!-- Top 实体与关系 -->
    <a-row :gutter="16">
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">Top 10 高关联实体</h3>
          <EChart :option="topEntitiesOption" height="320px" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">关系强度分布</h3>
          <EChart :option="relationStrengthOption" height="320px" />
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { PageHeader } from '@/components/PageHeader.vue'
import { useAction } from '@/composables/useAction'
import EChart from '@/components/EChart.vue'
import { statisticsData, dashboardStats, projectList, growthTrend } from '@/utils/mock'
import {
  ExportOutlined, ArrowUpOutlined, ArrowDownOutlined,
  ApartmentOutlined, BranchesOutlined, NodeIndexOutlined, ShareAltOutlined, DatabaseOutlined
} from '@ant-design/icons-vue'

const action = useAction()

const project = ref('all')
const trendRange = ref<'7' | '30' | '90'>('7')

const stats = [
  { label: '实体总数', value: dashboardStats.entityCount.toLocaleString(), icon: ApartmentOutlined, color: '#1677ff', bg: '#e6f4ff', trend: 8.2 },
  { label: '关系总数', value: dashboardStats.relationCount.toLocaleString(), icon: BranchesOutlined, color: '#52c41a', bg: '#f6ffed', trend: 12.5 },
  { label: '本月新增', value: '15,280', icon: NodeIndexOutlined, color: '#faad14', bg: '#fffbe6', trend: 5.6 },
  { label: '平均度数', value: '6.06', icon: ShareAltOutlined, color: '#722ed1', bg: '#f9f0ff', trend: -2.3 }
]

const graphMetricsList = [
  { label: '图谱密度', value: statisticsData.graphMetrics.density, desc: '实际边数/可能边数', color: '#1677ff' },
  { label: '平均度数', value: statisticsData.graphMetrics.avgDegree, desc: '节点平均连接数', color: '#52c41a' },
  { label: '图谱直径', value: statisticsData.graphMetrics.diameter, desc: '最长最短路径', color: '#faad14' },
  { label: '平均路径长度', value: statisticsData.graphMetrics.avgPathLength, desc: '节点间平均跳数', color: '#ff4d4f' },
  { label: '聚类系数', value: statisticsData.graphMetrics.clusteringCoeff, desc: '节点聚集程度', color: '#722ed1' },
  { label: '连通分量', value: 12, desc: '子图数量', color: '#13c2c2' }
]

const entityDistOption = {
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, type: 'scroll' },
  series: [{
    type: 'pie', radius: ['35%', '65%'],
    data: statisticsData.entityTypeDist,
    label: { formatter: '{b}\n{d}%', fontSize: 11 },
    itemStyle: { borderColor: '#fff', borderWidth: 2 }
  }]
}

const relationDistOption = {
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 60, right: 30, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: statisticsData.relationTypeDist.map(s => s.name), axisLabel: { rotate: 30 } },
  yAxis: { type: 'value' },
  series: [{
    type: 'bar', data: statisticsData.relationTypeDist.map(s => s.value), barWidth: '50%',
    itemStyle: {
      color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#52c41a' }, { offset: 1, color: '#95de64' }] },
      borderRadius: [4, 4, 0, 0]
    }
  }]
}

const growthOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['实体数', '关系数'], bottom: 0 },
  grid: { left: 60, right: 60, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: growthTrend.dates },
  yAxis: [
    { type: 'value', name: '实体' },
    { type: 'value', name: '关系' }
  ],
  series: [
    {
      name: '实体数', type: 'line', smooth: true, data: growthTrend.entity,
      itemStyle: { color: '#1677ff' }, areaStyle: { opacity: 0.15 }
    },
    {
      name: '关系数', type: 'line', smooth: true, yAxisIndex: 1, data: growthTrend.relation,
      itemStyle: { color: '#52c41a' }, areaStyle: { opacity: 0.15 }
    }
  ]
}))

const sourcePieOption = {
  tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: '60%',
    data: statisticsData.sourceDist.map(s => ({ ...s, value: s.value })),
    label: { formatter: '{b}\n{c}%' }
  }]
}

const projectCols = [
  { title: '项目', dataIndex: 'name', key: 'name', width: 140 },
  { title: '实体数', dataIndex: 'entityCount', key: 'entityCount', width: 120 },
  { title: '实体占比', key: 'entity', width: 160 },
  { title: '关系数', dataIndex: 'relationCount', key: 'relationCount', width: 120 },
  { title: '关系占比', key: 'relation', width: 160 }
]

const colors = ['#1677ff', '#52c41a', '#faad14', '#ff4d4f', '#722ed1', '#13c2c2']
const maxEntity = Math.max(...projectList.map(p => p.entityCount))
const maxRelation = Math.max(...projectList.map(p => p.relationCount))
const projectStats = projectList.map((p, i) => ({
  id: p.id, name: p.domain, color: colors[i % colors.length],
  entityCount: p.entityCount.toLocaleString(),
  relationCount: p.relationCount.toLocaleString(),
  entityPercent: Math.round((p.entityCount / maxEntity) * 100),
  relationPercent: Math.round((p.relationCount / maxRelation) * 100)
}))

const heatmapOption = {
  tooltip: { position: 'top' },
  grid: { left: 60, right: 20, top: 20, bottom: 60 },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'], splitArea: { show: true } },
  yAxis: { type: 'category', data: ['0-4', '4-8', '8-12', '12-16', '16-20', '20-24'], splitArea: { show: true } },
  visualMap: {
    min: 0, max: 100, calculable: true, orient: 'horizontal', left: 'center', bottom: 0,
    inRange: { color: ['#e6f4ff', '#1677ff'] }
  },
  series: [{
    type: 'heatmap', data: Array.from({ length: 7 * 6 }, (_, i) => [i % 7, Math.floor(i / 7), Math.floor(Math.random() * 100)]),
    label: { show: false }
  }]
}

const topEntitiesOption = {
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 80, right: 30, top: 20, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: ['马云', '阿里巴巴', '腾讯', '北京', '人工智能', 'ChatGPT', '百度', '杭州', '马化腾', '深圳'].reverse() },
  series: [{
    type: 'bar', data: [42, 56, 48, 62, 38, 35, 45, 52, 40, 47].reverse(),
    barWidth: '60%',
    itemStyle: { color: '#1677ff', borderRadius: [0, 4, 4, 0] },
    label: { show: true, position: 'right' }
  }]
}

const relationStrengthOption = {
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: [
      { name: '强关系 (>0.9)', value: 32, itemStyle: { color: '#52c41a' } },
      { name: '中关系 (0.7-0.9)', value: 45, itemStyle: { color: '#faad14' } },
      { name: '弱关系 (<0.7)', value: 23, itemStyle: { color: '#ff4d4f' } }
    ],
    label: { formatter: '{b}\n{d}%' }
  }]
}
</script>

<style lang="less" scoped>
.stat-trend {
  font-size: 11px; margin-top: 4px;
  &.up { color: #52c41a; }
  &.down { color: #ff4d4f; }
}
.metric-card {
  text-align: center; padding: 16px 8px;
  background: #fafbfc; border-radius: 8px;
  .metric-value { font-size: 26px; font-weight: 600; }
  .metric-label { font-size: 13px; color: #1f2937; margin: 4px 0; }
  .metric-desc { font-size: 11px; }
}
</style>
