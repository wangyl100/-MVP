<template>
  <div class="page-container">
    <PageHeader title="项目管理" description="管理知识图谱项目，所有图谱数据、模型、任务都归属特定项目">
      <template #extra>
        <a-space>
          <a-radio-group v-model:value="viewMode" button-style="solid">
            <a-radio-button value="grid"><AppstoreOutlined /></a-radio-button>
            <a-radio-button value="list"><UnorderedListOutlined /></a-radio-button>
          </a-radio-group>
          <a-button type="primary"><PlusOutlined /> 创建项目</a-button>
        </a-space>
      </template>
    </PageHeader>

    <div class="page-card mb-24">
      <a-row :gutter="16">
        <a-col :span="8"><a-input v-model:value="search" placeholder="搜索项目名称" allow-clear><template #prefix><SearchOutlined /></template></a-input></a-col>
        <a-col :span="6">
          <a-select v-model:value="domain" placeholder="所属领域" allow-clear style="width:100%">
            <a-select-option v-for="d in domains" :key="d">{{ d }}</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-select v-model:value="status" placeholder="项目状态" allow-clear style="width:100%">
            <a-select-option value="active">活跃</a-select-option>
            <a-select-option value="archived">已归档</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="4"><a-button type="primary" block>查询</a-button></a-col>
      </a-row>
    </div>

    <!-- 网格视图 -->
    <a-row v-if="viewMode === 'grid'" :gutter="16">
      <a-col :xs="24" :sm="12" :md="12" :lg="8" v-for="p in filteredProjects" :key="p.id" style="margin-bottom: 16px">
        <a-card hoverable class="project-card">
          <div class="project-cover" :style="{ background: coverColor[p.cover] }">
            <component :is="coverIcon[p.cover]" />
            <a-tag v-if="p.status === 'archived'" class="status-tag">已归档</a-tag>
          </div>
          <div class="project-body">
            <div class="flex-between">
              <h3 class="project-name">{{ p.name }}</h3>
              <a-tag color="blue">{{ p.domain }}</a-tag>
            </div>
            <p class="project-desc">{{ p.desc }}</p>
            <a-divider style="margin: 12px 0" />
            <a-row :gutter="8">
              <a-col :span="8"><a-statistic title="实体" :value="p.entityCount" :value-style="{fontSize:'16px'}" /></a-col>
              <a-col :span="8"><a-statistic title="关系" :value="p.relationCount" :value-style="{fontSize:'16px'}" /></a-col>
              <a-col :span="8"><a-statistic title="成员" :value="p.memberCount" :value-style="{fontSize:'16px'}" /></a-col>
            </a-row>
            <a-divider style="margin: 12px 0" />
            <div class="flex-between">
              <span class="text-secondary">更新于 {{ p.updateTime }}</span>
              <a-space>
                <a-button type="primary" size="small" ghost>进入</a-button>
                <a-button size="small">设置</a-button>
              </a-space>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 列表视图 -->
    <div v-else class="page-card">
      <a-table :columns="columns" :data-source="filteredProjects" row-key="id" :pagination="{pageSize:10}">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-space>
              <div class="mini-cover" :style="{background:coverColor[record.cover]}"><component :is="coverIcon[record.cover]" /></div>
              <div>
                <div style="font-weight:500">{{ record.name }}</div>
                <div class="text-secondary" style="font-size:12px">{{ record.desc }}</div>
              </div>
            </a-space>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="record.status === 'active' ? 'success' : 'default'" :text="record.status === 'active' ? '活跃' : '已归档'" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small">进入</a-button>
            <a-button type="link" size="small">设置</a-button>
            <a-button type="link" size="small" danger>归档</a-button>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  PlusOutlined, SearchOutlined, AppstoreOutlined, UnorderedListOutlined,
  HeartOutlined, DollarOutlined, ToolOutlined, ScaleOutlined, ReadOutlined, ShoppingOutlined
} from '@ant-design/icons-vue'
import { projectList } from '@/utils/mock'
import { formatNumber } from '@/utils/mock'

const viewMode = ref('grid')
const search = ref('')
const domain = ref(undefined as any)
const status = ref(undefined as any)
const domains = ['医疗', '金融', '制造', '法律', '教育', '电商']

const coverColor: Record<string, string> = {
  medical: 'linear-gradient(135deg,#ff4d4f,#ff7875)',
  finance: 'linear-gradient(135deg,#1677ff,#4096ff)',
  industry: 'linear-gradient(135deg,#fa8c16,#ffa940)',
  law: 'linear-gradient(135deg,#722ed1,#9254de)',
  education: 'linear-gradient(135deg,#52c41a,#73d13d)',
  ecommerce: 'linear-gradient(135deg,#13c2c2,#36cfc9)'
}
const coverIcon: Record<string, any> = {
  medical: HeartOutlined,
  finance: DollarOutlined,
  industry: ToolOutlined,
  law: ScaleOutlined,
  education: ReadOutlined,
  ecommerce: ShoppingOutlined
}

const columns = [
  { title: '项目名称', key: 'name' },
  { title: '领域', dataIndex: 'domain', key: 'domain', width: 80 },
  { title: '实体数', dataIndex: 'entityCount', key: 'entityCount', width: 100, customRender: ({ text }: any) => formatNumber(text) },
  { title: '关系数', dataIndex: 'relationCount', key: 'relationCount', width: 100, customRender: ({ text }: any) => formatNumber(text) },
  { title: '成员', dataIndex: 'memberCount', key: 'memberCount', width: 70 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 120 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 200 }
]

const filteredProjects = computed(() => {
  return projectList.filter((p) => {
    if (search.value && !p.name.includes(search.value)) return false
    if (domain.value && p.domain !== domain.value) return false
    if (status.value && p.status !== status.value) return false
    return true
  })
})
</script>

<style lang="less" scoped>
.project-card {
  :deep(.ant-card-body) { padding: 0; }
}
.project-cover {
  height: 120px;
  display: flex; align-items: center; justify-content: center;
  font-size: 48px; color: #fff;
  position: relative;
  .status-tag { position: absolute; top: 12px; right: 12px; }
}
.project-body { padding: 16px; }
.project-name { margin: 0; font-size: 16px; font-weight: 600; }
.project-desc { color: #6b7280; font-size: 13px; margin: 8px 0 0; min-height: 40px; }
.mini-cover {
  width: 40px; height: 40px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 18px;
}
</style>
