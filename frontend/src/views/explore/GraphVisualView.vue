<template>
  <div class="page-container">
    <PageHeader title="图谱可视化" description="交互式知识图谱探索，支持节点展开、路径分析与多布局切换">
      <template #extra>
        <a-space>
          <a-select v-model:value="layout" style="width:120px" @change="renderGraph">
            <a-select-option value="force">力导向布局</a-select-option>
            <a-select-option value="circle">环形布局</a-select-option>
            <a-select-option value="grid">网格布局</a-select-option>
            <a-select-option value="radial">辐射布局</a-select-option>
          </a-select>
          <a-button @click="resetZoom">
            <template #icon><ReloadOutlined /></template>
            重置视图
          </a-button>
          <a-button type="primary" @click="renderGraph">
            <template #icon><ReloadOutlined /></template>
            重新布局
          </a-button>
        </a-space>
      </template>
    </PageHeader>

    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="18">
        <div class="page-card" style="padding:0;overflow:hidden">
          <!-- 工具条 -->
          <div class="graph-toolbar">
            <a-space>
              <a-input-search v-model:value="searchKeyword" placeholder="搜索节点" style="width:200px" @search="onSearch" />
              <a-tooltip title="放大"><a-button @click="zoomIn"><ZoomInOutlined /></a-button></a-tooltip>
              <a-tooltip title="缩小"><a-button @click="zoomOut"><ZoomOutOutlined /></a-button></a-tooltip>
              <a-tooltip title="全屏"><a-button><FullscreenOutlined /></a-button></a-tooltip>
              <a-divider type="vertical" />
              <a-checkbox v-model:checked="showLabel">显示标签</a-checkbox>
              <a-checkbox v-model:checked="showArrow">显示箭头</a-checkbox>
            </a-space>
            <a-space>
              <a-tag color="blue">节点 {{ graphData.nodes.length }}</a-tag>
              <a-tag color="green">边 {{ graphData.edges.length }}</a-tag>
            </a-space>
          </div>
          <!-- 图谱画布 -->
          <div ref="containerRef" class="graph-canvas" />
          <!-- 缩略图 -->
          <div class="minimap-hint">
            <InfoCircleOutlined /> 滚轮缩放 · 拖拽平移 · 点击节点展开邻居
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :lg="6">
        <!-- 节点详情面板 -->
        <div class="page-card mb-24">
          <h3 style="margin:0 0 16px;font-size:16px">节点详情</h3>
          <template v-if="selectedNode">
            <a-descriptions :column="1" size="small">
              <a-descriptions-item label="名称"><b>{{ selectedNode.label }}</b></a-descriptions-item>
              <a-descriptions-item label="类型"><a-tag :color="selectedNode.color">{{ selectedNode.type }}</a-tag></a-descriptions-item>
              <a-descriptions-item label="ID">{{ selectedNode.id }}</a-descriptions-item>
              <a-descriptions-item label="度数">{{ selectedNode.degree }}</a-descriptions-item>
              <a-descriptions-item label="属性数">{{ selectedNode.properties }}</a-descriptions-item>
            </a-descriptions>
            <a-divider style="margin:12px 0" />
            <a-space direction="vertical" style="width:100%">
              <a-button block type="primary" size="small">展开邻居节点</a-button>
              <a-button block size="small">查找最短路径</a-button>
              <a-button block size="small">隐藏此节点</a-button>
              <a-button block size="small" danger>从此节点删除</a-button>
            </a-space>
          </template>
          <a-empty v-else description="点击节点查看详情" :image="null" style="padding:24px 0">
            <template #description><span class="text-secondary">点击节点查看详情</span></template>
          </a-empty>
        </div>
        <!-- 图例 -->
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">图例</h3>
          <a-list :data-source="legendData" size="small">
            <template #renderItem="{ item }">
              <a-list-item style="padding:6px 0">
                <a-space>
                  <span class="legend-dot" :style="{ background: item.color }" />
                  <span>{{ item.name }}</span>
                  <span class="text-secondary" style="font-size:12px">({{ item.count }})</span>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </a-col>
    </a-row>

    <!-- 路径分析 -->
    <div class="page-card">
      <h3 style="margin:0 0 16px;font-size:16px">最短路径分析</h3>
      <a-row :gutter="16">
        <a-col :xs="24" :md="8">
          <a-form layout="vertical">
            <a-form-item label="起始节点">
              <a-input v-model:value="pathStart" placeholder="输入实体名称" />
            </a-form-item>
            <a-form-item label="目标节点">
              <a-input v-model:value="pathEnd" placeholder="输入实体名称" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" block @click="findPath">
                <template #icon><NodeIndexOutlined /></template>
                查找路径
              </a-button>
            </a-form-item>
          </a-form>
        </a-col>
        <a-col :xs="24" :md="16">
          <div v-if="pathResult.length" class="path-result">
            <div class="text-secondary" style="margin-bottom:8px">找到 {{ pathResult.length - 1 }} 跳路径：</div>
            <div class="path-chain">
              <template v-for="(n, i) in pathResult" :key="n">
                <span class="path-node">{{ n }}</span>
                <span v-if="i < pathResult.length - 1" class="path-arrow">→</span>
              </template>
            </div>
            <a-alert type="success" show-icon style="margin-top:12px"
              message="路径长度 3 跳，途经 4 个节点，平均关系强度 0.87" />
          </div>
          <a-empty v-else description="输入起始与目标节点查询最短路径" style="padding:32px 0" />
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { PageHeader } from '@/components/PageHeader.vue'
import G6 from '@antv/g6'
import {
  ReloadOutlined, ZoomInOutlined, ZoomOutOutlined, FullscreenOutlined,
  NodeIndexOutlined, InfoCircleOutlined
} from '@ant-design/icons-vue'

const containerRef = ref<HTMLElement | null>(null)
let graph: any = null
const layout = ref<'force' | 'circle' | 'grid' | 'radial'>('force')
const searchKeyword = ref('')
const showLabel = ref(true)
const showArrow = ref(true)
const selectedNode = ref<any>(null)

const legendData = [
  { name: '人物', color: '#1677ff', count: 12 },
  { name: '机构', color: '#52c41a', count: 8 },
  { name: '地点', color: '#faad14', count: 6 },
  { name: '事件', color: '#ff4d4f', count: 4 },
  { name: '概念', color: '#722ed1', count: 5 }
]

const typeColorMap: Record<string, string> = {
  '人物': '#1677ff', '机构': '#52c41a', '地点': '#faad14',
  '事件': '#ff4d4f', '概念': '#722ed1', '作品': '#13c2c2'
}

const graphData = reactive<{ nodes: any[]; edges: any[] }>({
  nodes: [],
  edges: []
})

function buildMockGraph() {
  const nodeNames = ['马云', '阿里巴巴', '杭州', '蔡崇信', '腾讯', '深圳', '马化腾', 'ChatGPT', 'OpenAI', '旧金山', '人工智能', '深度学习', '北京', '李彦宏', '百度']
  const nodeTypes = ['人物', '机构', '地点', '人物', '机构', '地点', '人物', '作品', '机构', '地点', '概念', '概念', '地点', '人物', '机构']
  const nodes = nodeNames.map((name, i) => ({
    id: 'n' + i, label: name, type: nodeTypes[i],
    color: typeColorMap[nodeTypes[i]],
    degree: Math.floor(Math.random() * 6) + 1,
    properties: Math.floor(Math.random() * 8) + 2,
    x: Math.random() * 600, y: Math.random() * 400
  }))
  const edgeDefs = [
    [0, 1, '创立'], [0, 2, '出生于'], [0, 3, '合作'], [3, 1, '就职于'], [1, 2, '位于'],
    [4, 5, '位于'], [6, 4, '创立'], [6, 5, '出生于'], [0, 6, '竞争'], [1, 4, '竞争'],
    [7, 8, '开发者'], [8, 9, '位于'], [7, 10, '属于'], [10, 11, '相关'], [12, 13, '出生于'],
    [13, 14, '创立'], [14, 12, '位于'], [1, 14, '竞争'], [10, 7, '应用']
  ]
  const edges = edgeDefs.map((e, i) => ({ id: 'e' + i, source: 'n' + e[0], target: 'n' + e[1], label: String(e[2]) }))
  graphData.nodes = nodes
  graphData.edges = edges
}

function getLayoutConfig() {
  switch (layout.value) {
    case 'force': return { type: 'force', preventOverlap: true, nodeStrength: -50, linkDistance: 140 }
    case 'circle': return { type: 'circular' }
    case 'grid': return { type: 'grid' }
    case 'radial': return { type: 'radial' }
    default: return { type: 'force' }
  }
}

function renderGraph() {
  if (!containerRef.value) return
  if (graph) { graph.destroy(); graph = null }
  const width = containerRef.value.offsetWidth
  const height = 500
  graph = new G6.Graph({
    container: containerRef.value,
    width, height,
    layout: getLayoutConfig() as any,
    modes: {
      default: ['drag-canvas', 'zoom-canvas', 'drag-node']
    },
    defaultNode: {
      size: 36, style: { cursor: 'pointer' },
      labelCfg: { position: 'bottom', style: { fill: '#555', fontSize: 11 } }
    },
    defaultEdge: {
      style: { stroke: '#bcc4d0', lineWidth: 1.5, endArrow: showArrow.value },
      labelCfg: { style: { fill: '#888', fontSize: 10 }, refY: 4 }
    },
    nodeStateStyles: { selected: { shadowBlur: 12, shadowColor: '#1677ff', lineWidth: 3 } }
  })
  graph.data({
    nodes: graphData.nodes.map(n => ({
      ...n, style: { fill: n.color, stroke: '#fff', lineWidth: 2 },
      label: showLabel.value ? n.label : ''
    })),
    edges: graphData.edges.map(e => ({ ...e, label: showLabel.value ? e.label : '' }))
  })
  graph.render()
  graph.on('node:click', (evt: any) => {
    const node = evt.item
    const model = node.getModel()
    graph.setItemState(node, 'selected', true)
    selectedNode.value = model
  })
  graph.on('canvas:click', () => {
    selectedNode.value = null
  })
}

function onSearch(value: string) {
  if (!value || !graph) return
  const found = graphData.nodes.find(n => n.label.includes(value))
  if (found && graph.findById(found.id)) {
    graph.focusItem(found.id, true, { easing: 'easeCubic', duration: 600 })
    const item = graph.findById(found.id)
    graph.setItemState(item, 'selected', true)
    selectedNode.value = found
  }
}

function zoomIn() { graph && graph.zoom(1.2) }
function zoomOut() { graph && graph.zoom(0.8) }
function resetZoom() { graph && graph.fitView(20) }

watch([showLabel, showArrow], () => renderGraph())

onMounted(() => {
  buildMockGraph()
  renderGraph()
  window.addEventListener('resize', onResize)
})

function onResize() {
  if (graph && containerRef.value) {
    graph.changeSize(containerRef.value.offsetWidth, 500)
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  if (graph) { graph.destroy(); graph = null }
})

// 路径分析
const pathStart = ref('马云')
const pathEnd = ref('深圳')
const pathResult = ref<string[]>([])
function findPath() {
  if (!pathStart.value || !pathEnd.value) return
  // 模拟查找路径
  pathResult.value = [pathStart.value, '阿里巴巴', '腾讯', pathEnd.value]
}
</script>

<style lang="less" scoped>
.graph-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; border-bottom: 1px solid #f0f0f0;
}
.graph-canvas { width: 100%; height: 500px; background: #fafbfc; }
.minimap-hint {
  padding: 8px 16px; font-size: 12px; color: #9ca3af;
  border-top: 1px solid #f0f0f0; background: #fff;
}
.legend-dot {
  display: inline-block; width: 12px; height: 12px;
  border-radius: 50%; margin-right: 6px;
}
.path-result {
  background: #fafafa; border-radius: 8px; padding: 16px;
}
.path-chain { display: flex; flex-wrap: wrap; align-items: center; }
.path-node {
  padding: 6px 14px; background: #e6f4ff; color: #1677ff;
  border-radius: 16px; font-size: 13px; margin: 4px 0;
}
.path-arrow { margin: 0 8px; color: #9ca3af; }
</style>
