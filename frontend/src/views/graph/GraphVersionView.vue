<template>
  <div class="page-container">
    <PageHeader title="图谱版本管理" description="图谱版本快照、版本对比与回滚">
      <template #extra>
        <a-space>
          <a-button>
            <template #icon><HistoryOutlined /></template>
            创建快照
          </a-button>
          <a-button type="primary">
            <template #icon><CameraOutlined /></template>
            版本对比
          </a-button>
        </a-space>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="12" :md="6" v-for="s in stats" :key="s.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: s.bg, color: s.color }">
            <component :is="s.icon" />
          </div>
          <div>
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="mb-24">
      <!-- 版本时间线 -->
      <a-col :xs="24" :lg="14">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">版本历史</h3>
          <a-timeline mode="left">
            <a-timeline-item v-for="(v, i) in graphVersions" :key="v.id" :color="i===0?'green':(v.tag==='stable'?'blue':'gray')">
              <div class="version-item" :class="{ active: i===0 }">
                <div class="flex-between">
                  <a-space>
                    <a-tag :color="i===0?'green':(v.tag==='stable'?'blue':'default')">{{ v.version }}</a-tag>
                    <span v-if="i===0"><a-badge status="success" text="当前版本" /></span>
                    <a-tag v-if="v.tag && v.tag !== '-'" color="gold">{{ v.tag }}</a-tag>
                  </a-space>
                  <span class="text-secondary" style="font-size:12px">{{ v.createTime }} · {{ v.creator }}</span>
                </div>
                <p style="margin:8px 0 4px;color:#1f2937;font-size:13px">{{ v.desc }}</p>
                <a-space wrap>
                  <a-tag>实体 {{ v.entityCount.toLocaleString() }}</a-tag>
                  <a-tag>关系 {{ v.relationCount.toLocaleString() }}</a-tag>
                </a-space>
                <div style="margin-top:8px">
                  <a-button type="link" size="small" v-if="i!==0" @click="rollback(v)">回滚到此版本</a-button>
                  <a-button type="link" size="small" @click="selectForCompare(v)" :disabled="compareV.includes(v.id)">加入对比</a-button>
                  <a-button type="link" size="small">详情</a-button>
                  <a-button type="link" size="small">导出</a-button>
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
        </div>
      </a-col>

      <!-- 版本对比 -->
      <a-col :xs="24" :lg="10">
        <div class="page-card mb-24">
          <h3 style="margin:0 0 16px;font-size:16px">版本对比</h3>
          <a-space direction="vertical" style="width:100%" :size="8">
            <a-select v-model:value="v1" style="width:100%" placeholder="选择版本A">
              <a-select-option v-for="v in graphVersions" :key="v.id" :value="v.id">{{ v.version }} - {{ v.desc }}</a-select-option>
            </a-select>
            <a-select v-model:value="v2" style="width:100%" placeholder="选择版本B">
              <a-select-option v-for="v in graphVersions" :key="v.id" :value="v.id">{{ v.version }} - {{ v.desc }}</a-select-option>
            </a-select>
          </a-space>
          <a-divider />
          <div v-if="v1 && v2 && v1 !== v2">
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="实体数变化">
                <span :style="{ color: entityDiff >= 0 ? '#52c41a' : '#ff4d4f' }">
                  {{ entityDiff >= 0 ? '+' : '' }}{{ entityDiff.toLocaleString() }}
                </span>
              </a-descriptions-item>
              <a-descriptions-item label="关系数变化">
                <span :style="{ color: relationDiff >= 0 ? '#52c41a' : '#ff4d4f' }">
                  {{ relationDiff >= 0 ? '+' : '' }}{{ relationDiff.toLocaleString() }}
                </span>
              </a-descriptions-item>
            </a-descriptions>
            <EChart :option="compareOption" height="220px" />
          </div>
          <a-empty v-else description="请选择两个不同版本进行对比" style="margin-top:32px" />
        </div>

        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">版本演进趋势</h3>
          <EChart :option="trendOption" height="240px" />
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { PageHeader } from '@/components/PageHeader.vue'
import EChart from '@/components/EChart.vue'
import { graphVersions, dashboardStats } from '@/utils/mock'
import { message, Modal } from 'ant-design-vue'
import {
  CameraOutlined, HistoryOutlined, BranchesOutlined, DatabaseOutlined,
  DeploymentUnitOutlined, ApartmentOutlined
} from '@ant-design/icons-vue'

const stats = [
  { label: '版本总数', value: graphVersions.length, icon: HistoryOutlined, color: '#1677ff', bg: '#e6f4ff' },
  { label: '当前实体', value: dashboardStats.entityCount.toLocaleString(), icon: ApartmentOutlined, color: '#52c41a', bg: '#f6ffed' },
  { label: '当前关系', value: dashboardStats.relationCount.toLocaleString(), icon: BranchesOutlined, color: '#faad14', bg: '#fffbe6' },
  { label: '本月快照', value: 3, icon: CameraOutlined, color: '#722ed1', bg: '#f9f0ff' }
]

const compareV = ref<string[]>([])
const v1 = ref<string>('')
const v2 = ref<string>('')

const va = computed(() => graphVersions.find(v => v.id === v1.value))
const vb = computed(() => graphVersions.find(v => v.id === v2.value))
const entityDiff = computed(() => (va.value?.entityCount || 0) - (vb.value?.entityCount || 0))
const relationDiff = computed(() => (va.value?.relationCount || 0) - (vb.value?.relationCount || 0))

const compareOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['实体数', '关系数'], bottom: 0 },
  grid: { left: 50, right: 20, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: [va.value?.version || 'A', vb.value?.version || 'B'] },
  yAxis: [
    { type: 'value', name: '实体' },
    { type: 'value', name: '关系' }
  ],
  series: [
    { name: '实体数', type: 'bar', data: [va.value?.entityCount || 0, vb.value?.entityCount || 0], itemStyle: { color: '#1677ff' } },
    { name: '关系数', type: 'bar', yAxisIndex: 1, data: [va.value?.relationCount || 0, vb.value?.relationCount || 0], itemStyle: { color: '#52c41a' } }
  ]
}))

const trendOption = {
  tooltip: { trigger: 'axis' },
  legend: { data: ['实体数', '关系数'], bottom: 0 },
  grid: { left: 50, right: 50, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: [...graphVersions].reverse().map(v => v.version) },
  yAxis: [
    { type: 'value', name: '实体' },
    { type: 'value', name: '关系' }
  ],
  series: [
    {
      name: '实体数', type: 'line', smooth: true, data: [...graphVersions].reverse().map(v => v.entityCount),
      itemStyle: { color: '#1677ff' }, areaStyle: { opacity: 0.1 }
    },
    {
      name: '关系数', type: 'line', smooth: true, yAxisIndex: 1, data: [...graphVersions].reverse().map(v => v.relationCount),
      itemStyle: { color: '#52c41a' }, areaStyle: { opacity: 0.1 }
    }
  ]
}

function selectForCompare(v: typeof graphVersions[0]) {
  if (compareV.value.length >= 2) {
    compareV.value = [compareV.value[1]]
  }
  compareV.value.push(v.id)
  if (compareV.value.length === 1) v1.value = v.id
  else if (compareV.value.length === 2) {
    v2.value = v.id
  }
  message.info(`已将 ${v.version} 加入对比`)
}

function rollback(v: typeof graphVersions[0]) {
  Modal.confirm({
    title: '版本回滚确认',
    content: `确认将图谱回滚到 ${v.version}（${v.createTime}）？该操作不可撤销。`,
    okText: '确认回滚',
    okType: 'danger',
    cancelText: '取消',
    onOk: () => {
      message.success(`已回滚到 ${v.version}`)
    }
  })
}
</script>

<style lang="less" scoped>
.version-item {
  padding: 8px 12px;
  border-radius: 6px;
  background: #fafafa;
  margin-bottom: 4px;
  &.active { background: #f6ffed; }
}
</style>
