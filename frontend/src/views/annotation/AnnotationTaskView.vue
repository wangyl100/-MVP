<template>
  <div class="page-container">
    <PageHeader title="标注任务" description="管理数据标注任务的创建、分配与状态">
      <template #extra>
        <a-button type="primary" @click="action.openCreate('创建标注任务', '请填写任务名称、类型、标注人员及截止日期等信息')"><PlusOutlined /> 创建标注任务</a-button>
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

    <div class="page-card">
      <a-table :columns="columns" :data-source="annotationTasks" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'"><a-tag :color="typeColor[record.type]">{{ typeLabel[record.type] }}</a-tag></template>
          <template v-else-if="column.key === 'progress'">
            <a-progress :percent="record.progress" size="small" />
            <div class="text-secondary" style="font-size:11px">{{ record.annotated }}/{{ record.total }}</div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="statusMap[record.status].status" :text="statusMap[record.status].text" />
          </template>
          <template v-else-if="column.key === 'quality'">
            <a-tag :color="record.quality > 0.9 ? 'green' : 'orange'">{{ (record.quality * 100).toFixed(1) }}%</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="$router.push('/annotation/workspace')">标注</a-button>
            <a-button type="link" size="small" @click="action.notify('分配', record.name || '任务')">分配</a-button>
            <a-button type="link" size="small" danger @click="action.notify('关闭', record.name || '任务')">关闭</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <a-row :gutter="16" class="mt-16">
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">标注人员工作量</h3>
          <a-table :columns="workerCols" :data-source="workers" :pagination="false" size="small">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'workload'">
                <a-progress :percent="record.workload" size="small" />
              </template>
            </template>
          </a-table>
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">分配方式</h3>
          <a-radio-group v-model:value="assignMode" direction="vertical" style="display:flex;flex-direction:column;gap:12px">
            <a-radio value="average">
              <div><strong>平均分配</strong></div>
              <div class="text-secondary" style="font-size:12px">将任务平均分配给所有标注人员</div>
            </a-radio>
            <a-radio value="ratio">
              <div><strong>按比例分配</strong></div>
              <div class="text-secondary" style="font-size:12px">根据标注人员的能力按比例分配</div>
            </a-radio>
            <a-radio value="manual">
              <div><strong>手动分配</strong></div>
              <div class="text-secondary" style="font-size:12px">手动指定每个标注人员的任务量</div>
            </a-radio>
          </a-radio-group>
          <a-button type="primary" block style="margin-top:16px" @click="action.notify('执行分配', '标注任务')">执行分配</a-button>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { PlusOutlined, EditOutlined, AuditOutlined, TeamOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'
import { annotationTasks } from '@/utils/mock'
import { useAction } from '@/composables/useAction'

const action = useAction()

const stats = [
  { title: '标注任务', value: 12, color: 'linear-gradient(135deg,#1677ff,#4096ff)', icon: EditOutlined },
  { title: '进行中', value: 5, color: 'linear-gradient(135deg,#fa8c16,#ffa940)', icon: AuditOutlined },
  { title: '标注人员', value: 42, color: 'linear-gradient(135deg,#52c41a,#73d13d)', icon: TeamOutlined },
  { title: '本月完成', value: '4.5万', color: 'linear-gradient(135deg,#722ed1,#9254de)', icon: CheckCircleOutlined }
]

const typeColor: Record<string, string> = { entity: 'blue', relation: 'purple', event: 'red', attribute: 'orange', classification: 'cyan' }
const typeLabel: Record<string, string> = { entity: '实体标注', relation: '关系标注', event: '事件标注', attribute: '属性标注', classification: '分类标注' }
const statusMap: Record<string, { status: any; text: string }> = {
  in_progress: { status: 'processing', text: '进行中' },
  completed: { status: 'success', text: '已完成' },
  paused: { status: 'warning', text: '已暂停' },
  pending: { status: 'default', text: '待开始' }
}

const columns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 110 },
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '类型', key: 'type', width: 100 },
  { title: '进度', key: 'progress', width: 200 },
  { title: '标注人员', dataIndex: 'assignees', key: 'assignees', width: 90 },
  { title: '质量', key: 'quality', width: 90 },
  { title: '截止日期', dataIndex: 'deadline', key: 'deadline', width: 120 },
  { title: '状态', key: 'status', width: 110 },
  { title: '操作', key: 'action', width: 200 }
]

const assignMode = ref('average')

const workerCols = [
  { title: '标注人员', dataIndex: 'name', key: 'name' },
  { title: '已完成', dataIndex: 'done', key: 'done', width: 90 },
  { title: '准确率', dataIndex: 'acc', key: 'acc', width: 90, customRender: ({ text }: any) => (text * 100).toFixed(1) + '%' },
  { title: '工作量', key: 'workload', width: 200 }
]
const workers = [
  { name: '张三', done: 1280, acc: 0.95, workload: 85 },
  { name: '李四', done: 980, acc: 0.92, workload: 65 },
  { name: '王五', done: 1560, acc: 0.94, workload: 100 },
  { name: '赵六', done: 720, acc: 0.88, workload: 48 },
  { name: '钱七', done: 1100, acc: 0.91, workload: 73 }
]
</script>
