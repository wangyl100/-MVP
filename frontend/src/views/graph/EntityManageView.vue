<template>
  <div class="page-container">
    <PageHeader title="实体管理" description="管理知识图谱中的实体数据，支持增删改查与批量操作">
      <template #extra>
        <a-space>
          <a-button>
            <template #icon><UploadOutlined /></template>
            批量导入
          </a-button>
          <a-button type="primary">
            <template #icon><PlusOutlined /></template>
            新增实体
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

    <!-- 实体类型分布 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="10">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">实体类型分布</h3>
          <EChart :option="typeDistOption" height="280px" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="14">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">数据来源占比</h3>
          <EChart :option="sourceOption" height="280px" />
        </div>
      </a-col>
    </a-row>

    <!-- 筛选 + 表格 -->
    <div class="page-card">
      <a-form layout="inline" class="mb-16">
        <a-form-item label="关键词">
          <a-input v-model:value="filters.keyword" placeholder="实体名称/ID" style="width:200px" allow-clear>
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="实体类型">
          <a-select v-model:value="filters.type" style="width:140px" allow-clear placeholder="全部类型">
            <a-select-option v-for="t in schemaEntityTypes" :key="t.name" :value="t.name">{{ t.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="数据来源">
          <a-select v-model:value="filters.source" style="width:140px" allow-clear placeholder="全部来源">
            <a-select-option value="LLM抽取">LLM抽取</a-select-option>
            <a-select-option value="结构化导入">结构化导入</a-select-option>
            <a-select-option value="DL抽取">DL抽取</a-select-option>
            <a-select-option value="手动添加">手动添加</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="置信度">
          <a-select v-model:value="filters.confidence" style="width:120px" allow-clear placeholder="全部">
            <a-select-option value="high">≥ 0.9</a-select-option>
            <a-select-option value="mid">0.7 ~ 0.9</a-select-option>
            <a-select-option value="low">&lt; 0.7</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary"><template #icon><SearchOutlined /></template>查询</a-button>
            <a-button>重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <div class="flex-between mb-16">
        <a-space>
          <a-button :disabled="!selectedKeys.length">批量删除</a-button>
          <a-button :disabled="!selectedKeys.length">批量合并</a-button>
          <a-button :disabled="!selectedKeys.length">导出选中</a-button>
          <span v-if="selectedKeys.length" class="text-secondary">已选 {{ selectedKeys.length }} 项</span>
        </a-space>
        <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
          <a-radio-button value="table"><TableOutlined /> 表格</a-radio-button>
          <a-radio-button value="card"><AppstoreOutlined /> 卡片</a-radio-button>
        </a-radio-group>
      </div>

      <a-table
        v-if="viewMode === 'table'"
        :columns="columns"
        :data-source="filteredList"
        row-key="id"
        :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (t:number) => `共 ${t} 条` }"
        :row-selection="{ selectedRowKeys: selectedKeys, onChange: (k:any) => selectedKeys = k }"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a class="entity-link" @click="showDetail(record)">{{ record.name }}</a>
          </template>
          <template v-else-if="column.key === 'type'">
            <a-tag :color="typeColor[record.type]">{{ record.type }}</a-tag>
          </template>
          <template v-else-if="column.key === 'source'">
            <a-tag :color="sourceColor[record.source]">{{ record.source }}</a-tag>
          </template>
          <template v-else-if="column.key === 'confidence'">
            <a-progress :percent="Number((record.confidence * 100).toFixed(1))" size="small"
              :stroke-color="record.confidence >= 0.9 ? '#52c41a' : record.confidence >= 0.7 ? '#faad14' : '#ff4d4f'" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="showDetail(record)">详情</a-button>
            <a-button type="link" size="small">编辑</a-button>
            <a-popconfirm title="确认删除该实体？" ok-text="删除" cancel-text="取消">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>

      <a-row v-else :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="e in filteredList.slice(0, 12)" :key="e.id">
          <a-card size="small" hoverable @click="showDetail(e)">
            <div class="entity-card">
              <a-tag :color="typeColor[e.type]" style="margin-bottom:8px">{{ e.type }}</a-tag>
              <h4 style="margin:0 0 4px;font-size:15px">{{ e.name }}</h4>
              <div class="text-secondary" style="font-size:12px">{{ e.id }}</div>
              <a-divider style="margin:8px 0" />
              <div class="flex-between text-secondary" style="font-size:12px">
                <span>关系 {{ e.relations }}</span>
                <span>属性 {{ e.properties }}</span>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 实体详情抽屉 -->
    <a-drawer v-model:open="detailVisible" :width="560" :title="current?.name || '实体详情'">
      <template v-if="current">
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="实体ID">{{ current.id }}</a-descriptions-item>
          <a-descriptions-item label="实体名称">{{ current.name }}</a-descriptions-item>
          <a-descriptions-item label="实体类型"><a-tag :color="typeColor[current.type]">{{ current.type }}</a-tag></a-descriptions-item>
          <a-descriptions-item label="数据来源"><a-tag :color="sourceColor[current.source]">{{ current.source }}</a-tag></a-descriptions-item>
          <a-descriptions-item label="置信度">
            <a-progress :percent="Number((current.confidence * 100).toFixed(1))" size="small" />
          </a-descriptions-item>
          <a-descriptions-item label="关系数量">{{ current.relations }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ current.updateTime }}</a-descriptions-item>
        </a-descriptions>

        <h4 style="margin:20px 0 12px;font-size:15px">属性列表</h4>
        <a-table :columns="propCols" :data-source="propList" row-key="name" :pagination="false" size="small" />

        <h4 style="margin:20px 0 12px;font-size:15px">关联关系（{{ current.relations }}）</h4>
        <a-list :data-source="relList" size="small" bordered>
          <template #renderItem="{ item }">
            <a-list-item>
              <a-space>
                <a-tag :color="typeColor[item.sType]">{{ item.sName }}</a-tag>
                <span style="color:#1677ff">—{{ item.rName }}→</span>
                <a-tag :color="typeColor[item.tType]">{{ item.tName }}</a-tag>
              </a-space>
            </a-list-item>
          </template>
        </a-list>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { PageHeader } from '@/components/PageHeader.vue'
import EChart from '@/components/EChart.vue'
import { entityList, schemaEntityTypes, statisticsData, dashboardStats } from '@/utils/mock'
import {
  PlusOutlined, SearchOutlined, UploadOutlined, TableOutlined, AppstoreOutlined,
  NodeIndexOutlined, ShareAltOutlined, DatabaseOutlined, ApartmentOutlined
} from '@ant-design/icons-vue'

const stats = [
  { label: '实体总数', value: dashboardStats.entityCount.toLocaleString(), icon: NodeIndexOutlined, color: '#1677ff', bg: '#e6f4ff' },
  { label: '实体类型', value: schemaEntityTypes.length, icon: ApartmentOutlined, color: '#52c41a', bg: '#f6ffed' },
  { label: '本月新增', value: '15,280', icon: DatabaseOutlined, color: '#faad14', bg: '#fffbe6' },
  { label: '待审核', value: 326, icon: ShareAltOutlined, color: '#ff4d4f', bg: '#fff2f0' }
]

const typeColor: Record<string, string> = {
  '人物': 'blue', '机构': 'green', '地点': 'gold', '事件': 'red', '概念': 'purple', '作品': 'cyan'
}
const sourceColor: Record<string, string> = {
  'LLM抽取': 'blue', '结构化导入': 'green', 'DL抽取': 'orange', '手动添加': 'default'
}

const columns = [
  { title: '实体ID', dataIndex: 'id', key: 'id', width: 110 },
  { title: '实体名称', dataIndex: 'name', key: 'name' },
  { title: '类型', dataIndex: 'type', key: 'type', width: 90 },
  { title: '属性数', dataIndex: 'properties', key: 'properties', width: 80 },
  { title: '关系数', dataIndex: 'relations', key: 'relations', width: 80 },
  { title: '数据来源', dataIndex: 'source', key: 'source', width: 110 },
  { title: '置信度', dataIndex: 'confidence', key: 'confidence', width: 160 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 110 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const }
]

const propCols = [
  { title: '属性名', dataIndex: 'name', key: 'name' },
  { title: '数据类型', dataIndex: 'type', key: 'type', width: 110 },
  { title: '属性值', dataIndex: 'value', key: 'value' }
]

const filters = reactive({ keyword: '', type: undefined as string | undefined, source: undefined as string | undefined, confidence: undefined as string | undefined })
const selectedKeys = ref<string[]>([])
const viewMode = ref<'table' | 'card'>('table')

const filteredList = computed(() => {
  return entityList.filter(e => {
    if (filters.keyword && !e.name.includes(filters.keyword) && !e.id.includes(filters.keyword)) return false
    if (filters.type && e.type !== filters.type) return false
    if (filters.source && e.source !== filters.source) return false
    if (filters.confidence === 'high' && Number(e.confidence) < 0.9) return false
    if (filters.confidence === 'mid' && (Number(e.confidence) < 0.7 || Number(e.confidence) >= 0.9)) return false
    if (filters.confidence === 'low' && Number(e.confidence) >= 0.7) return false
    return true
  })
})

const typeDistOption = {
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: statisticsData.entityTypeDist,
    label: { formatter: '{b}: {d}%' }
  }]
}

const sourceOption = {
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 60, right: 20, top: 20, bottom: 30 },
  xAxis: { type: 'category', data: statisticsData.sourceDist.map(s => s.name) },
  yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
  series: [{
    type: 'bar', data: statisticsData.sourceDist.map(s => s.value), barWidth: '45%',
    itemStyle: { color: '#1677ff', borderRadius: [4, 4, 0, 0] },
    label: { show: true, position: 'top', formatter: '{c}%' }
  }]
}

const detailVisible = ref(false)
const current = ref<typeof entityList[0] | null>(null)
const propList = ref<any[]>([])
const relList = ref<any[]>([])

function showDetail(e: typeof entityList[0]) {
  current.value = e
  propList.value = (schemaEntityTypes.find(t => t.name === e.type)?.properties || []).slice(0, 6).map((p, i) => ({
    name: p, type: ['String', 'Date', 'Integer', 'URL', 'List'][i % 5], value: ['示例值', '2026-01-01', '42', 'https://example.com', '标签A,标签B'][i % 5]
  }))
  relList.value = Array.from({ length: Math.min(e.relations, 6) }, (_, i) => ({
    sName: e.name, sType: e.type,
    rName: ['出生于', '就职于', '参与', '合作', '位于'][i % 5],
    tName: ['北京', '阿里巴巴', '事件A', '李四', '上海'][i % 5],
    tType: ['地点', '机构', '事件', '人物', '地点'][i % 5]
  }))
  detailVisible.value = true
}
</script>

<style lang="less" scoped>
.entity-link { color: #1677ff; cursor: pointer; }
.entity-link:hover { text-decoration: underline; }
.entity-card { text-align: center; padding: 8px 0; }
</style>
