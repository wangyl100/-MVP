<template>
  <div class="page-container">
    <PageHeader title="图谱模型" description="定义知识图谱的本体结构：实体类型、关系类型、属性定义">
      <template #extra>
        <a-space>
          <a-button><RobotOutlined /> AI 生成 Schema</a-button>
          <a-button><DownloadOutlined /> 导入</a-button>
          <a-button type="primary"><SaveOutlined /> 保存</a-button>
        </a-space>
      </template>
    </PageHeader>

    <a-row :gutter="16">
      <!-- 实体类型 -->
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <div class="flex-between mb-16">
            <h3 style="margin:0;font-size:16px">实体类型（{{ schemaEntityTypes.length }}）</h3>
            <a-button type="primary" size="small"><PlusOutlined /> 新增</a-button>
          </div>
          <a-list :data-source="schemaEntityTypes" item-layout="horizontal">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <div class="entity-icon" :style="{background:item.color}">{{ item.name.charAt(0) }}</div>
                  </template>
                  <template #title>
                    <a-space>
                      <span style="font-weight:500">{{ item.name }}</span>
                      <a-tag>{{ item.id }}</a-tag>
                      <a-tag color="blue">{{ formatNumber(item.instanceCount) }} 实例</a-tag>
                    </a-space>
                  </template>
                  <template #description>
                    <a-space wrap size="[4,8]">
                      <a-tag v-for="p in item.properties" :key="p" :color="item.color" style="opacity:0.85">{{ p }}</a-tag>
                    </a-space>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="link" size="small">编辑</a-button>
                  <a-button type="link" size="small" danger>删除</a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </a-col>

      <!-- 关系类型 -->
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <div class="flex-between mb-16">
            <h3 style="margin:0;font-size:16px">关系类型（{{ schemaRelationTypes.length }}）</h3>
            <a-button type="primary" size="small"><PlusOutlined /> 新增</a-button>
          </div>
          <a-table :columns="relColumns" :data-source="schemaRelationTypes" :pagination="false" size="small" row-key="id">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'relation'">
                <a-tag color="purple">{{ record.name }}</a-tag>
              </template>
              <template v-else-if="column.key === 'chain'">
                <a-tag color="blue">{{ record.source }}</a-tag>
                <ArrowRightOutlined style="margin: 0 4px; color:#9ca3af" />
                <a-tag color="blue">{{ record.target }}</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small">编辑</a-button>
                <a-button type="link" size="small" danger>删除</a-button>
              </template>
            </template>
          </a-table>
        </div>
      </a-col>
    </a-row>

    <!-- 可视化 Schema 图 + AI 辅助 -->
    <a-row :gutter="16" class="mt-16">
      <a-col :xs="24" :lg="16">
        <div class="page-card">
          <div class="flex-between mb-16">
            <h3 style="margin:0;font-size:16px">Schema 可视化</h3>
            <a-radio-group size="small" v-model:value="layout">
              <a-radio-button value="force">力导向</a-radio-button>
              <a-radio-button value="circle">环形</a-radio-button>
              <a-radio-button value="grid">网格</a-radio-button>
            </a-radio-group>
          </div>
          <div ref="schemaGraphRef" class="schema-graph"></div>
        </div>
      </a-col>
      <a-col :xs="24" :lg="8">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px"><BulbOutlined style="color:#faad14" /> AI 辅助 Schema 设计</h3>
          <a-textarea v-model:value="aiInput" :rows="4" placeholder="输入领域描述或上传样本文本，AI 自动生成 Schema。例如：构建医疗领域的知识图谱，包含疾病、症状、药物等" />
          <a-button type="primary" block style="margin-top:12px" :loading="aiLoading" @click="runAi">
            <RobotOutlined /> AI 生成
          </a-button>
          <a-divider />
          <h4 style="margin:0 0 12px;font-size:14px">AI 优化建议</h4>
          <a-list :data-source="aiSuggestions" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.title" :description="item.desc">
                  <template #avatar>
                    <a-avatar :style="{background:item.color}">{{ item.icon }}</a-avatar>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { PlusOutlined, SaveOutlined, DownloadOutlined, RobotOutlined, BulbOutlined, ArrowRightOutlined } from '@ant-design/icons-vue'
import { schemaEntityTypes, schemaRelationTypes, formatNumber } from '@/utils/mock'
import { Graph } from '@antv/g6'

const aiInput = ref('')
const aiLoading = ref(false)
const layout = ref('force')

const relColumns = [
  { title: '关系', key: 'relation', width: 100 },
  { title: '头实体 → 尾实体', key: 'chain' },
  { title: '实例数', dataIndex: 'instanceCount', key: 'instanceCount', width: 100, customRender: ({ text }: any) => formatNumber(text) },
  { title: '操作', key: 'action', width: 120 }
]

const aiSuggestions = [
  { title: '合并相似实体类型', desc: '建议将「人物」与「作者」合并，作者可作为人物的角色属性', icon: '🔁', color: '#1677ff' },
  { title: '补充缺失属性', desc: '「机构」类型缺少「联系方式」属性，建议添加', icon: '➕', color: '#52c41a' },
  { title: '关系约束优化', desc: '「参与」关系可放宽为多对多，提升抽取覆盖率', icon: '⚡', color: '#faad14' }
]

function runAi() {
  if (!aiInput.value) return
  aiLoading.value = true
  setTimeout(() => { aiLoading.value = false }, 1500)
}

// Schema 可视化图
const schemaGraphRef = ref<HTMLElement>()
let graph: Graph | null = null

function buildData() {
  const nodes = schemaEntityTypes.map((e) => ({
    id: e.name,
    data: { label: e.name, color: e.color }
  }))
  const edges = schemaRelationTypes.map((r) => ({
    source: r.source,
    target: r.target,
    data: { label: r.name }
  }))
  return { nodes, edges }
}

async function renderGraph() {
  if (!schemaGraphRef.value) return
  if (graph) {
    graph.destroy()
    graph = null
  }
  const data = buildData()
  graph = new Graph({
    container: schemaGraphRef.value,
    width: schemaGraphRef.value.clientWidth,
    height: 400,
    data,
    node: {
      type: 'circle',
      style: {
        size: 48,
        fill: (d: any) => d.data?.color || '#1677ff',
        labelText: (d: any) => d.data?.label || d.id,
        labelFill: '#fff',
        labelFontSize: 12,
        labelFontWeight: 600
      }
    },
    edge: {
      type: 'line',
      style: {
        stroke: '#9ca3af',
        labelText: (d: any) => d.data?.label || '',
        labelFontSize: 11,
        labelFill: '#6b7280',
        labelBackground: true,
        labelBackgroundFill: '#fff',
        endArrow: true as any
      }
    },
    layout: {
      type: layout.value === 'circle' ? 'circular' : layout.value === 'grid' ? 'grid' : 'force',
      preventOverlap: true,
      nodeSize: 60
    } as any,
    behaviors: ['drag-canvas', 'zoom-canvas', 'drag-element']
  })
  await graph.render()
}

onMounted(() => {
  renderGraph()
})
watch(layout, () => renderGraph())
onBeforeUnmount(() => {
  graph?.destroy()
})
</script>

<style lang="less" scoped>
.entity-icon {
  width: 40px; height: 40px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: 600;
}
.schema-graph {
  width: 100%; height: 400px;
  background: #fafbfc;
  border-radius: 8px;
}
</style>
