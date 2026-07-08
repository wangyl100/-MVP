<template>
  <div class="page-container">
    <PageHeader title="关系管理" description="管理知识图谱中的关系数据，支持关系增删改查与批量操作">
      <template #extra>
        <a-space>
          <a-button @click="action.notify('批量导入', '关系')">
            <template #icon><UploadOutlined /></template>
            批量导入
          </a-button>
          <a-button type="primary" @click="action.openCreate('新增关系', '请填写头实体、关系类型、尾实体。')">
            <template #icon><PlusOutlined /></template>
            新增关系
          </a-button>
        </a-space>
      </template>
    </PageHeader>

    <!-- 关系类型分布 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="14">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">关系类型分布（Top 7）</h3>
          <EChart :option="relDistOption" height="300px" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="10">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">关系类型概览</h3>
          <a-list :data-source="schemaRelationTypes" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a-space>
                      <a-tag color="blue">{{ item.name }}</a-tag>
                      <span style="font-size:12px;color:#6b7280">{{ item.source }} → {{ item.target }}</span>
                    </a-space>
                  </template>
                  <template #description>{{ item.desc }} · 共 {{ item.instanceCount.toLocaleString() }} 条</template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </a-col>
    </a-row>

    <!-- 关系列表 -->
    <div class="page-card">
      <a-form layout="inline" class="mb-16">
        <a-form-item label="关键词">
          <a-input v-model:value="filters.keyword" placeholder="头实体/尾实体/关系名" style="width:220px" allow-clear>
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="关系类型">
          <a-select v-model:value="filters.relation" style="width:140px" allow-clear placeholder="全部类型">
            <a-select-option v-for="r in schemaRelationTypes" :key="r.name" :value="r.name">{{ r.name }}</a-select-option>
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
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="action.notify('查询', '关系列表')"><template #icon><SearchOutlined /></template>查询</a-button>
            <a-button @click="action.info('已重置筛选条件')">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <div class="flex-between mb-16">
        <a-space>
          <a-button :disabled="!selectedKeys.length" @click="action.confirmDelete('选中关系')">批量删除</a-button>
          <a-button :disabled="!selectedKeys.length" @click="action.notify('导出', '选中关系')">导出选中</a-button>
          <span v-if="selectedKeys.length" class="text-secondary">已选 {{ selectedKeys.length }} 项</span>
        </a-space>
        <a-tag color="blue">共 {{ filteredList.length }} 条关系</a-tag>
      </div>

      <a-table
        :columns="columns"
        :data-source="filteredList"
        row-key="id"
        :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (t:number) => `共 ${t} 条` }"
        :row-selection="{ selectedRowKeys: selectedKeys, onChange: (k:any) => selectedKeys = k }"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'source'">
            <a-tag :color="sourceColor[record.source2]">{{ record.source2 }}</a-tag>
          </template>
          <template v-else-if="column.key === 'confidence'">
            <a-progress :percent="Number((record.confidence * 100).toFixed(1))" size="small"
              :stroke-color="record.confidence >= 0.9 ? '#52c41a' : record.confidence >= 0.7 ? '#faad14' : '#ff4d4f'" />
          </template>
          <template v-else-if="column.key === 'relation'">
            <a-tag color="blue">{{ record.relation }}</a-tag>
          </template>
          <template v-else-if="column.key === 'triple'">
            <span class="triple-cell">
              <span class="node s">{{ record.source }}</span>
              <span class="edge">—{{ record.relation }}→</span>
              <span class="node t">{{ record.target }}</span>
            </span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="action.openDetail('关系详情', record)">详情</a-button>
            <a-button type="link" size="small" @click="action.openEdit('关系', record.relation)">编辑</a-button>
            <a-popconfirm title="确认删除该关系？" ok-text="删除" cancel-text="取消" @confirm="action.confirmDelete(record.relation)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 关系详情抽屉 -->
    <a-drawer v-model:open="detailVisible" :width="520" :title="current ? `${current.source} —${current.relation}→ ${current.target}` : '关系详情'">
      <template v-if="current">
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="关系ID">{{ current.id }}</a-descriptions-item>
          <a-descriptions-item label="关系类型"><a-tag color="blue">{{ current.relation }}</a-tag></a-descriptions-item>
          <a-descriptions-item label="头实体">{{ current.source }}</a-descriptions-item>
          <a-descriptions-item label="尾实体">{{ current.target }}</a-descriptions-item>
          <a-descriptions-item label="数据来源"><a-tag :color="sourceColor[current.source2]">{{ current.source2 }}</a-tag></a-descriptions-item>
          <a-descriptions-item label="置信度">
            <a-progress :percent="Number((current.confidence * 100).toFixed(1))" size="small" />
          </a-descriptions-item>
          <a-descriptions-item label="属性数量">{{ current.properties }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ current.updateTime }}</a-descriptions-item>
        </a-descriptions>

        <h4 style="margin:20px 0 12px;font-size:15px">关系属性</h4>
        <a-table :columns="propCols" :data-source="relProps" row-key="name" :pagination="false" size="small" />

        <h4 style="margin:20px 0 12px;font-size:15px">来源追溯</h4>
        <a-alert type="info" show-icon>
          <template #message>该关系由 <b>{{ current.source2 }}</b> 于 {{ current.updateTime }} 生成</template>
          <template #description>
            原始数据：语料 ID #CORPUS_{{ Math.floor(Math.random() * 9999).toString().padStart(4, '0') }}
            <br />
            抽取置信度：{{ current.confidence }} · 可信度评分：{{ (Number(current.confidence) * 100).toFixed(0) }}/100
          </template>
        </a-alert>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { PageHeader } from '@/components/PageHeader.vue'
import EChart from '@/components/EChart.vue'
import { relationList, schemaRelationTypes, statisticsData } from '@/utils/mock'
import { PlusOutlined, SearchOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { useAction } from '@/composables/useAction'

const action = useAction()

const sourceColor: Record<string, string> = {
  'LLM抽取': 'blue', '结构化导入': 'green', 'DL抽取': 'orange', '手动添加': 'default'
}

const columns = [
  { title: '关系ID', dataIndex: 'id', key: 'id', width: 110 },
  { title: '三元组 (头实体 — 关系 → 尾实体)', key: 'triple' },
  { title: '关系类型', dataIndex: 'relation', key: 'relation', width: 100 },
  { title: '属性数', dataIndex: 'properties', key: 'properties', width: 80 },
  { title: '数据来源', dataIndex: 'source2', key: 'source', width: 110 },
  { title: '置信度', dataIndex: 'confidence', key: 'confidence', width: 150 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 110 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const }
]

const propCols = [
  { title: '属性名', dataIndex: 'name', key: 'name' },
  { title: '数据类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '属性值', dataIndex: 'value', key: 'value' }
]

const filters = reactive({ keyword: '', relation: undefined as string | undefined, source: undefined as string | undefined })
const selectedKeys = ref<string[]>([])

const filteredList = computed(() => {
  return relationList.filter(r => {
    if (filters.keyword) {
      const kw = filters.keyword
      if (!r.source.includes(kw) && !r.target.includes(kw) && !r.relation.includes(kw)) return false
    }
    if (filters.relation && r.relation !== filters.relation) return false
    if (filters.source && r.source2 !== filters.source) return false
    return true
  })
})

const relDistOption = {
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 70, right: 30, top: 20, bottom: 30 },
  xAxis: { type: 'category', data: statisticsData.relationTypeDist.map(s => s.name), axisLabel: { rotate: 25 } },
  yAxis: { type: 'value' },
  series: [{
    type: 'bar',
    data: statisticsData.relationTypeDist.map(s => s.value),
    barWidth: '50%',
    itemStyle: {
      color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#1677ff' }, { offset: 1, color: '#69b1ff' }] },
      borderRadius: [4, 4, 0, 0]
    },
    label: { show: true, position: 'top', formatter: (p: any) => p.value > 1000 ? (p.value / 10000).toFixed(1) + '万' : p.value }
  }]
}

const detailVisible = ref(false)
const current = ref<typeof relationList[0] | null>(null)
const relProps = ref<any[]>([])

function showDetail(r: typeof relationList[0]) {
  current.value = r
  relProps.value = [
    { name: '开始时间', type: 'Date', value: '2020-01-01' },
    { name: '结束时间', type: 'Date', value: '至今' },
    { name: '职位', type: 'String', value: '技术专家' },
    { name: '证明来源', type: 'URL', value: 'https://example.com/news/123' }
  ].slice(0, r.properties)
  detailVisible.value = true
}
</script>

<style lang="less" scoped>
.triple-cell {
  .node { padding: 2px 8px; border-radius: 4px; font-size: 13px; }
  .node.s { background: #e6f4ff; color: #1677ff; }
  .node.t { background: #f6ffed; color: #52c41a; }
  .edge { margin: 0 6px; color: #6b7280; font-size: 12px; }
}
</style>
