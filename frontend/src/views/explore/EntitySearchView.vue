<template>
  <div class="page-container">
    <PageHeader title="实体检索" description="多维度实体搜索、属性过滤与搜索结果可视化" />

    <!-- 搜索框 -->
    <div class="page-card mb-24">
      <a-input-search
        v-model:value="keyword"
        placeholder="输入实体名称、ID、属性值进行搜索..."
        enter-button="搜索"
        size="large"
        @search="onSearch"
      />
      <div class="search-suggestions">
        <span class="text-secondary" style="font-size:13px">热门搜索：</span>
        <a-tag v-for="s in hotSearch" :key="s" color="blue" style="cursor:pointer" @click="keyword = s; onSearch()">{{ s }}</a-tag>
      </div>
    </div>

    <a-row :gutter="16" class="mb-24">
      <!-- 左侧筛选 -->
      <a-col :xs="24" :lg="6">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">高级筛选</h3>
          <a-form layout="vertical">
            <a-form-item label="实体类型">
              <a-checkbox-group v-model:value="filters.types" style="display:flex;flex-direction:column">
                <a-checkbox v-for="t in schemaEntityTypes" :key="t.name" :value="t.name">
                  <a-tag :color="typeColor[t.name]" style="margin:0">{{ t.name }}</a-tag>
                  <span class="text-secondary" style="font-size:12px;margin-left:4px">{{ t.instanceCount.toLocaleString() }}</span>
                </a-checkbox>
              </a-checkbox-group>
            </a-form-item>
            <a-form-item label="数据来源">
              <a-checkbox-group v-model:value="filters.sources" style="display:flex;flex-direction:column">
                <a-checkbox v-for="s in sourceOptions" :key="s" :value="s">{{ s }}</a-checkbox>
              </a-checkbox-group>
            </a-form-item>
            <a-form-item label="置信度">
              <a-slider v-model:value="filters.confidence" range :min="0" :max="1" :step="0.1" />
              <div class="flex-between text-secondary" style="font-size:12px">
                <span>{{ filters.confidence[0].toFixed(1) }}</span>
                <span>{{ filters.confidence[1].toFixed(1) }}</span>
              </div>
            </a-form-item>
            <a-form-item label="更新时间">
              <a-range-picker v-model:value="filters.dateRange" style="width:100%" />
            </a-form-item>
            <a-form-item>
              <a-button block @click="resetFilters">重置筛选</a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-col>

      <!-- 右侧搜索结果 -->
      <a-col :xs="24" :lg="18">
        <div class="page-card">
          <div class="flex-between mb-16">
            <span>
              <b>{{ resultList.length }}</b> 条搜索结果
              <span class="text-secondary" style="margin-left:12px">用时 0.0{{ Math.floor(Math.random() * 9) + 1 }}s</span>
            </span>
            <a-space>
              <a-radio-group v-model:value="sortType" button-style="solid" size="small">
                <a-radio-button value="relevance">相关度</a-radio-button>
                <a-radio-button value="time">更新时间</a-radio-button>
                <a-radio-button value="relation">关系数</a-radio-button>
              </a-radio-group>
              <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
                <a-radio-button value="list"><UnorderedListOutlined /></a-radio-button>
                <a-radio-button value="card"><AppstoreOutlined /></a-radio-button>
              </a-radio-group>
            </a-space>
          </div>

          <!-- 列表视图 -->
          <a-list v-if="viewMode === 'list'" :data-source="resultList" item-layout="vertical">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a-space>
                      <a class="entity-link" @click="showDetail(item)">{{ item.name }}</a>
                      <a-tag :color="typeColor[item.type]">{{ item.type }}</a-tag>
                      <a-tag>{{ item.id }}</a-tag>
                      <a-tag color="purple">置信度 {{ item.confidence }}</a-tag>
                    </a-space>
                  </template>
                  <template #avatar>
                    <a-avatar :style="{ background: typeColor[item.type] }" size="large">{{ item.name.charAt(0) }}</a-avatar>
                  </template>
                  <template #description>
                    <div class="text-secondary">
                      来源：<a-tag :color="sourceColor[item.source]" style="margin:0">{{ item.source }}</a-tag>
                      · 属性 {{ item.properties }} 项 · 关系 {{ item.relations }} 条 · 更新于 {{ item.updateTime }}
                    </div>
                  </template>
                </a-list-item-meta>
                <div class="snippet">
                  <span v-for="(seg, i) in highlightSnippet(item.name, keyword)" :key="i"
                    :style="{ color: seg.highlight ? '#1677ff' : '#4b5563', fontWeight: seg.highlight ? 600 : 400 }">
                    {{ seg.text }}
                  </span>
                </div>
                <template #actions>
                  <span @click="action.openDetail('实体', item)"><NodeIndexOutlined /> 详情</span>
                  <span @click="action.info('关系图（演示）')"><ShareAltOutlined /> 关系图</span>
                  <span @click="action.notify('导出', item.name)"><ExportOutlined /> 导出</span>
                </template>
              </a-list-item>
            </template>
          </a-list>

          <!-- 卡片视图 -->
          <a-row v-else :gutter="[12, 12]">
            <a-col :xs="24" :sm="12" :md="8" v-for="item in resultList" :key="item.id">
              <a-card size="small" hoverable @click="showDetail(item)">
                <div class="result-card">
                  <div class="flex-between">
                    <a-avatar :style="{ background: typeColor[item.type] }">{{ item.name.charAt(0) }}</a-avatar>
                    <a-tag :color="typeColor[item.type]">{{ item.type }}</a-tag>
                  </div>
                  <h4 style="margin:10px 0 4px">{{ item.name }}</h4>
                  <div class="text-secondary" style="font-size:12px">{{ item.id }}</div>
                  <a-divider style="margin:8px 0" />
                  <div class="flex-between text-secondary" style="font-size:12px">
                    <span>关系 {{ item.relations }}</span>
                    <span>{{ item.confidence }}</span>
                  </div>
                </div>
              </a-card>
            </a-col>
          </a-row>

          <div style="margin-top:24px;text-align:right">
            <a-pagination :total="resultList.length * 8" :current="1" :page-size="10" show-size-changer />
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 搜索历史与推荐 -->
    <a-row :gutter="16">
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 12px;font-size:16px">最近搜索</h3>
          <a-space wrap>
            <a-tag v-for="h in searchHistory" :key="h" color="default" style="cursor:pointer">{{ h }}</a-tag>
          </a-space>
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="page-card">
          <h3 style="margin:0 0 12px;font-size:16px">相关推荐实体</h3>
          <a-list :data-source="recommendList" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.name" :description="item.desc">
                  <template #avatar>
                    <a-avatar :style="{ background: item.color }" size="small">{{ item.name.charAt(0) }}</a-avatar>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </a-col>
    </a-row>

    <!-- 实体详情抽屉 -->
    <a-drawer v-model:open="detailVisible" :width="520" :title="current?.name || '实体详情'">
      <template v-if="current">
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="实体ID">{{ current.id }}</a-descriptions-item>
          <a-descriptions-item label="名称">{{ current.name }}</a-descriptions-item>
          <a-descriptions-item label="类型"><a-tag :color="typeColor[current.type]">{{ current.type }}</a-tag></a-descriptions-item>
          <a-descriptions-item label="数据来源"><a-tag :color="sourceColor[current.source]">{{ current.source }}</a-tag></a-descriptions-item>
          <a-descriptions-item label="置信度">{{ current.confidence }}</a-descriptions-item>
          <a-descriptions-item label="关系数">{{ current.relations }}</a-descriptions-item>
        </a-descriptions>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { PageHeader } from '@/components/PageHeader.vue'
import { useAction } from '@/composables/useAction'
import { entityList, schemaEntityTypes } from '@/utils/mock'
import {
  UnorderedListOutlined, AppstoreOutlined, NodeIndexOutlined,
  ShareAltOutlined, ExportOutlined
} from '@ant-design/icons-vue'

const action = useAction()

const typeColor: Record<string, string> = {
  '人物': 'blue', '机构': 'green', '地点': 'gold', '事件': 'red', '概念': 'purple', '作品': 'cyan'
}
const sourceColor: Record<string, string> = {
  'LLM抽取': 'blue', '结构化导入': 'green', 'DL抽取': 'orange', '手动添加': 'default'
}

const keyword = ref('')
const hotSearch = ['马云', '阿里巴巴', '人工智能', 'ChatGPT', '北京']
const searchHistory = ['马云', '腾讯', '深度学习', '杭州', 'OpenAI']
const sourceOptions = ['LLM抽取', '结构化导入', 'DL抽取', '手动添加']

const filters = reactive({
  types: [] as string[],
  sources: [] as string[],
  confidence: [0, 1] as [number, number],
  dateRange: undefined as any
})

const sortType = ref<'relevance' | 'time' | 'relation'>('relevance')
const viewMode = ref<'list' | 'card'>('list')

const resultList = computed(() => {
  let list = entityList.filter(e => {
    if (keyword.value && !e.name.includes(keyword.value) && !e.id.includes(keyword.value)) return false
    if (filters.types.length && !filters.types.includes(e.type)) return false
    if (filters.sources.length && !filters.sources.includes(e.source)) return false
    if (Number(e.confidence) < filters.confidence[0] || Number(e.confidence) > filters.confidence[1]) return false
    return true
  })
  if (sortType.value === 'relation') list = [...list].sort((a, b) => b.relations - a.relations)
  else if (sortType.value === 'time') list = [...list].sort((a, b) => b.updateTime.localeCompare(a.updateTime))
  return list
})

const recommendList = [
  { name: '蔡崇信', desc: '阿里巴巴联合创始人', color: '#1677ff' },
  { name: '蚂蚁集团', desc: '阿里巴巴旗下金融科技', color: '#52c41a' },
  { name: '云计算', desc: '阿里云相关概念', color: '#722ed1' },
  { name: '淘宝', desc: '阿里巴巴电商平台', color: '#faad14' },
  { name: '钉钉', desc: '阿里巴巴办公软件', color: '#13c2c2' }
]

function onSearch() {
  // 触发 computed 重算
}

function resetFilters() {
  filters.types = []
  filters.sources = []
  filters.confidence = [0, 1]
  filters.dateRange = undefined
  keyword.value = ''
}

function highlightSnippet(name: string, kw: string) {
  if (!kw) return [{ text: '该实体共参与 ' + Math.floor(Math.random() * 50) + ' 条关系，属性信息完整，置信度较高，是知识图谱中的重要节点。', highlight: false }]
  const idx = name.indexOf(kw)
  if (idx < 0) return [{ text: '该实体匹配了您的搜索关键词，是知识图谱中的相关节点。', highlight: false }]
  return [
    { text: '匹配到实体 "', highlight: false },
    { text: kw, highlight: true },
    { text: `"，类型已识别，关系数 ${Math.floor(Math.random() * 50)} 条。`, highlight: false }
  ]
}

const detailVisible = ref(false)
const current = ref<typeof entityList[0] | null>(null)
function showDetail(e: typeof entityList[0]) {
  current.value = e
  detailVisible.value = true
}
</script>

<style lang="less" scoped>
.search-suggestions { margin-top: 12px; }
.entity-link { color: #1677ff; font-size: 15px; font-weight: 500; cursor: pointer; }
.entity-link:hover { text-decoration: underline; }
.snippet { color: #4b5563; font-size: 13px; line-height: 1.6; margin-top: 8px; }
.result-card { padding: 4px 0; }
</style>
