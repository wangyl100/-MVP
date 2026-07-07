<template>
  <div class="page-container">
    <PageHeader title="系统首页" description="全局数据概览与快捷操作入口">
      <template #extra>
        <a-radio-group v-model:value="timeRange" button-style="solid" size="small">
          <a-radio-button value="today">今日</a-radio-button>
          <a-radio-button value="week">本周</a-radio-button>
          <a-radio-button value="month">本月</a-radio-button>
          <a-radio-button value="all">全部</a-radio-button>
        </a-radio-group>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="12" :sm="12" :md="6" :lg="6" v-for="card in statCards" :key="card.title">
        <div class="stat-card">
          <div class="flex-between">
            <div>
              <div class="stat-title">{{ card.title }}</div>
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-trend" :class="card.trend > 0 ? 'up' : 'down'">
                <ArrowUpOutlined v-if="card.trend > 0" />
                <ArrowDownOutlined v-else />
                {{ Math.abs(card.trend) }}% 较上期
              </div>
            </div>
            <div class="stat-icon" :style="{ background: card.color }">
              <component :is="card.icon" />
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 图表区 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="16">
        <div class="page-card">
          <div class="flex-between mb-16">
            <h3 style="margin: 0; font-size: 16px">知识增长趋势</h3>
            <a-space>
              <a-tag color="processing">实体</a-tag>
            <a-tag color="success">关系</a-tag>
            </a-space>
          </div>
          <EChart :option="trendOption" height="320px" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="8">
        <div class="page-card">
          <h3 style="margin: 0 0 16px; font-size: 16px">实体类型分布</h3>
          <EChart :option="pieOption" height="320px" />
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="mb-24">
      <!-- 任务状态 -->
      <a-col :xs="24" :lg="8">
        <div class="page-card">
          <h3 style="margin: 0 0 16px; font-size: 16px">任务状态监控</h3>
          <div class="task-status-list">
            <div class="task-status-item" v-for="t in taskStatusList" :key="t.label">
              <div class="ts-icon" :style="{ background: t.color }">
                <component :is="t.icon" />
              </div>
              <div class="ts-info">
                <div class="ts-label">{{ t.label }}</div>
                <div class="ts-value">{{ t.value }}</div>
              </div>
            </div>
          </div>
        </div>
      </a-col>

      <!-- 快捷操作 -->
      <a-col :xs="24" :lg="8">
        <div class="page-card">
          <h3 style="margin: 0 0 16px; font-size: 16px">快捷操作</h3>
          <div class="quick-actions">
            <div class="quick-action" v-for="action in quickActions" :key="action.label" @click="router.push(action.path)">
              <div class="qa-icon" :style="{ background: action.color }">
                <component :is="action.icon" />
              </div>
              <div class="qa-label">{{ action.label }}</div>
            </div>
          </div>
        </div>
      </a-col>

      <!-- 系统公告 -->
      <a-col :xs="24" :lg="8">
        <div class="page-card">
          <h3 style="margin: 0 0 16px; font-size: 16px">系统公告</h3>
          <a-list :data-source="announcements" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :description="item.time">
                  <template #title>
                    <a-badge :status="item.type" :text="item.title" />
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </a-col>
    </a-row>

    <!-- 最近活动 -->
    <div class="page-card">
      <div class="flex-between mb-16">
        <h3 style="margin: 0; font-size: 16px">最近活动</h3>
        <a-button type="link" size="small">查看全部</a-button>
      </div>
      <a-timeline>
        <a-timeline-item v-for="(act, idx) in recentActivities" :key="idx" :color="statusColor[act.status]">
          <div class="activity-item">
            <div class="activity-title">{{ act.title }}</div>
            <div class="activity-desc">{{ act.desc }}</div>
            <div class="activity-time">{{ act.time }}</div>
          </div>
        </a-timeline-item>
      </a-timeline>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EChart from '@/components/EChart.vue'
import {
  DashboardOutlined,
  NodeIndexOutlined,
  ShareAltOutlined,
  RobotOutlined,
  FileTextOutlined,
  ExperimentOutlined,
  EditOutlined,
  TeamOutlined,
  PlayCircleOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  FolderAddOutlined,
  CloudUploadOutlined,
  PlusCircleOutlined,
  ThunderboltOutlined
} from '@ant-design/icons-vue'
import {
  dashboardStats,
  taskStatusStats,
  growthTrend,
  entityTypeDistribution,
  recentActivities
} from '@/utils/mock'
import { formatNumber } from '@/utils/mock'

const router = useRouter()
const timeRange = ref('week')

const statCards = computed(() => [
  { title: '知识图谱项目', value: dashboardStats.projectCount, trend: 12.5, color: 'linear-gradient(135deg, #2F6BFF, #4C84FF)', icon: DashboardOutlined },
  { title: '实体总数', value: formatNumber(dashboardStats.entityCount), trend: 8.3, color: 'linear-gradient(135deg, #6AC7BA, #53C7E8)', icon: NodeIndexOutlined },
  { title: '关系总数', value: formatNumber(dashboardStats.relationCount), trend: 15.6, color: 'linear-gradient(135deg, #4C84FF, #53C7E8)', icon: ShareAltOutlined },
  { title: '抽取任务', value: dashboardStats.taskCount, trend: -3.2, color: 'linear-gradient(135deg, #2F6BFF, #6AC7BA)', icon: RobotOutlined }
])

const taskStatusList = computed(() => [
  { label: '运行中', value: taskStatusStats.running, color: '#2F6BFF', icon: PlayCircleOutlined },
  { label: '待处理', value: taskStatusStats.pending, color: '#F08A5D', icon: ClockCircleOutlined },
  { label: '已完成', value: taskStatusStats.completed, color: '#33B26D', icon: CheckCircleOutlined },
  { label: '失败', value: taskStatusStats.failed, color: '#E96A5F', icon: ExclamationCircleOutlined }
])

const quickActions = [
  { label: '新建图谱', color: 'linear-gradient(135deg, #2F6BFF, #4C84FF)', icon: FolderAddOutlined, path: '/project/list' },
  { label: '导入数据', color: 'linear-gradient(135deg, #6AC7BA, #53C7E8)', icon: CloudUploadOutlined, path: '/data/source' },
  { label: '大模型抽取', color: 'linear-gradient(135deg, #4C84FF, #2F6BFF)', icon: RobotOutlined, path: '/extraction/llm' },
  { label: '开始标注', color: 'linear-gradient(135deg, #53C7E8, #6AC7BA)', icon: EditOutlined, path: '/annotation/task' },
  { label: '模型训练', color: 'linear-gradient(135deg, #2F6BFF, #6AC7BA)', icon: ExperimentOutlined, path: '/model/training' },
  { label: '图谱探索', color: 'linear-gradient(135deg, #4C84FF, #53C7E8)', icon: ThunderboltOutlined, path: '/explore/visual' }
]

const announcements = [
  { title: 'GPT-4o 模型已上线，抽取效率提升 40%', time: '2 小时前', type: 'success' as const },
  { title: '系统将于今晚 23:00 进行维护升级', time: '5 小时前', type: 'warning' as const },
  { title: '新版 Schema 编辑器支持 AI 自动生成', time: '1 天前', type: 'info' as const },
  { title: '图谱可视化支持 10000+ 节点流畅展示', time: '2 天前', type: 'info' as const }
]

const statusColor: Record<string, string> = {
  success: '#33B26D',
  error: '#E96A5F',
  info: '#2F6BFF'
}

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['新增实体', '新增关系'], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: growthTrend.dates },
  yAxis: { type: 'value' },
  series: [
    {
      name: '新增实体',
      type: 'line',
      smooth: true,
      data: growthTrend.entity,
      itemStyle: { color: '#2F6BFF' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(47,107,255,0.35)' },
            { offset: 1, color: 'rgba(47,107,255,0.02)' }
          ]
        }
      }
    },
    {
      name: '新增关系',
      type: 'line',
      smooth: true,
      data: growthTrend.relation,
      itemStyle: { color: '#6AC7BA' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(106,199,186,0.35)' },
            { offset: 1, color: 'rgba(106,199,186,0.02)' }
          ]
        }
      }
    }
  ]
}))

const pieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, type: 'scroll' },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: entityTypeDistribution,
      color: ['#2F6BFF', '#6AC7BA', '#53C7E8', '#4C84FF', '#33B26D', '#F08A5D']
    }
  ]
}))
</script>

<style lang="less" scoped>
.stat-trend {
  font-size: 12px;
  margin-top: 6px;
  &.up { color: #33B26D; }
  &.down { color: #E96A5F; }
}
.task-status-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.task-status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #F8FAFD;
  border: 1px solid #E8EDF5;
  border-radius: 12px;
  .ts-icon {
    width: 36px; height: 36px;
    border-radius: 10px;
    color: #fff;
    display: flex; align-items: center; justify-content: center;
    font-size: 16px;
  }
  .ts-label { font-size: 12px; color: #7B8794; }
  .ts-value { font-size: 20px; font-weight: 600; color: #1F2A37; }
}
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.quick-action {
  text-align: center;
  padding: 16px 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    background: #EAF2FF;
    transform: translateY(-2px);
  }
  .qa-icon {
    width: 40px; height: 40px;
    border-radius: 10px;
    color: #fff;
    display: flex; align-items: center; justify-content: center;
    font-size: 20px;
    margin: 0 auto 8px;
  }
  .qa-label { font-size: 12px; color: #1F2A37; }
}
.activity-item {
  .activity-title { font-size: 14px; font-weight: 500; color: #1F2A37; }
  .activity-desc { font-size: 12px; color: #7B8794; margin-top: 4px; }
  .activity-time { font-size: 12px; color: #9BA7B4; margin-top: 4px; }
}
</style>
