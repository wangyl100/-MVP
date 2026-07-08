<template>
  <div class="page-container">
    <PageHeader title="数据接入" description="多源异构数据接入、数据源管理与导入任务监控">
      <template #extra>
        <a-button type="primary" @click="action.openCreate('新增数据源', '请选择数据源类型并填写连接配置。')"><PlusOutlined /> 新增数据源</a-button>
      </template>
    </PageHeader>

    <!-- 统计 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="12" :md="6" v-for="s in stats" :key="s.title">
        <div class="stat-card">
          <div class="flex-between">
            <div>
              <div class="stat-title">{{ s.title }}</div>
              <div class="stat-value">{{ s.value }}</div>
            </div>
            <div class="stat-icon" :style="{background:s.color}"><component :is="s.icon" /></div>
          </div>
        </div>
      </a-col>
    </a-row>

    <div class="page-card">
      <a-table :columns="columns" :data-source="dataSourceList" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-space>
              <a-avatar :style="{background: typeColor[record.type]}"><component :is="typeIcon[record.type]" /></a-avatar>
              <div>
                <div style="font-weight:500">{{ record.name }}</div>
                <div class="text-secondary" style="font-size:12px">{{ record.host }}</div>
              </div>
            </a-space>
          </template>
          <template v-else-if="column.key === 'type'">
            <a-tag :color="typeColor[record.type]">{{ typeLabel[record.type] }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="statusMap[record.status].status" :text="statusMap[record.status].text" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="action.testConnection(record.name)">测试</a-button>
            <a-button type="link" size="small" @click="action.notify('导入', record.name)">导入</a-button>
            <a-button type="link" size="small" @click="action.openEdit('数据源', record.name)">编辑</a-button>
            <a-button type="link" size="small" danger @click="action.confirmDelete(record.name)">删除</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 导入任务 -->
    <div class="page-card mt-16">
      <h3 style="margin:0 0 16px;font-size:16px">导入任务监控</h3>
      <a-table :columns="importColumns" :data-source="importTasks" row-key="id" :pagination="false" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'progress'">
            <a-progress :percent="record.progress" :status="record.status === 'failed' ? 'exception' : record.status === 'running' ? 'active' : 'success'" size="small" />
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="impStatusMap[record.status].status" :text="impStatusMap[record.status].text" />
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import {
  PlusOutlined, DatabaseOutlined, ApiOutlined, FileExcelOutlined, GlobalOutlined,
  CloudServerOutlined, TableOutlined, CheckCircleOutlined
} from '@ant-design/icons-vue'
import { dataSourceList } from '@/utils/mock'
import { useAction } from '@/composables/useAction'

const action = useAction()

const stats = [
  { title: '数据源总数', value: 6, color: 'linear-gradient(135deg,#1677ff,#4096ff)', icon: DatabaseOutlined },
  { title: '已连接', value: 4, color: 'linear-gradient(135deg,#52c41a,#73d13d)', icon: CheckCircleOutlined },
  { title: '今日导入任务', value: 18, color: 'linear-gradient(135deg,#fa8c16,#ffa940)', icon: CloudServerOutlined },
  { title: '数据表总数', value: 111, color: 'linear-gradient(135deg,#722ed1,#9254de)', icon: TableOutlined }
]

const typeColor: Record<string, string> = {
  mysql: '#1677ff', postgresql: '#52c41a', oracle: '#ff4d4f', api: '#722ed1', excel: '#13c2c2', web: '#fa8c16'
}
const typeIcon: Record<string, any> = {
  mysql: DatabaseOutlined, postgresql: DatabaseOutlined, oracle: DatabaseOutlined,
  api: ApiOutlined, excel: FileExcelOutlined, web: GlobalOutlined
}
const typeLabel: Record<string, string> = {
  mysql: 'MySQL', postgresql: 'PostgreSQL', oracle: 'Oracle', api: 'API', excel: 'Excel/CSV', web: '网页爬虫'
}
const statusMap: Record<string, { status: any; text: string }> = {
  connected: { status: 'success', text: '已连接' },
  disconnected: { status: 'default', text: '未连接' },
  error: { status: 'error', text: '连接错误' }
}

const columns = [
  { title: '数据源', key: 'name' },
  { title: '类型', key: 'type', width: 120 },
  { title: '数据库', dataIndex: 'database', key: 'database', width: 160 },
  { title: '表数量', dataIndex: 'tableCount', key: 'tableCount', width: 90 },
  { title: '状态', key: 'status', width: 110 },
  { title: '最后同步', dataIndex: 'lastSync', key: 'lastSync', width: 160 },
  { title: '操作', key: 'action', width: 240 }
]

const importColumns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '数据源', dataIndex: 'source', key: 'source', width: 140 },
  { title: '记录数', dataIndex: 'count', key: 'count', width: 100 },
  { title: '进度', key: 'progress', width: 200 },
  { title: '状态', key: 'status', width: 110 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 160 }
]

const importTasks = [
  { id: 'IMP001', name: '生产库全量同步', source: '生产库 MySQL', count: 48200, progress: 78, status: 'running', startTime: '2026-07-06 10:15' },
  { id: 'IMP002', name: '业务数据增量导入', source: 'PostgreSQL', count: 3260, progress: 100, status: 'success', startTime: '2026-07-06 09:30' },
  { id: 'IMP003', name: '新闻语料拉取', source: 'API', count: 1250, progress: 35, status: 'failed', startTime: '2026-07-06 11:00' },
  { id: 'IMP004', name: 'Excel 数据导入', source: 'Excel 上传', count: 860, progress: 100, status: 'success', startTime: '2026-07-06 11:30' }
]

const impStatusMap: Record<string, { status: any; text: string }> = {
  running: { status: 'processing', text: '运行中' },
  success: { status: 'success', text: '成功' },
  failed: { status: 'error', text: '失败' }
}
</script>
