<template>
  <div class="page-container">
    <PageHeader title="大模型抽取" description="利用 LLM 从非结构化文本中零样本/少样本抽取实体、关系、属性">
      <template #extra>
        <a-space>
          <a-button><SettingOutlined /> Prompt 管理</a-button>
          <a-button type="primary"><PlusOutlined /> 新建抽取任务</a-button>
        </a-space>
      </template>
    </PageHeader>

    <!-- 即时抽取 -->
    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="14">
        <div class="page-card">
          <div class="flex-between mb-16">
            <h3 style="margin:0;font-size:16px"><RobotOutlined style="color:#722ed1" /> 即时文本抽取</h3>
            <a-select v-model:value="model" style="width:180px">
              <a-select-option value="gpt-4">GPT-4o</a-select-option>
              <a-select-option value="claude">Claude-3.5 Sonnet</a-select-option>
              <a-select-option value="ernie">文心一言 4.0</a-select-option>
              <a-select-option value="qwen">通义千问 Max</a-select-option>
            </a-select>
          </div>
          <a-textarea v-model:value="inputText" :rows="6" placeholder="输入或粘贴待抽取文本，点击「开始抽取」由大模型自动识别实体、关系和属性" />
          <div class="flex-between mt-16">
            <a-space>
              <span class="text-secondary">置信度阈值：</span>
              <a-slider v-model:value="threshold" :min="0" :max="1" :step="0.05" style="width:120px" />
              <span>{{ threshold.toFixed(2) }}</span>
            </a-space>
            <a-space>
              <a-button @click="inputText = sampleText">载入示例</a-button>
              <a-button type="primary" :loading="extracting" @click="extract">
                <ThunderboltOutlined /> 开始抽取
              </a-button>
            </a-space>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :lg="10">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">模型参数</h3>
          <a-form layout="vertical" size="small">
            <a-form-item label="Temperature"><a-slider v-model:value="params.temperature" :min="0" :max="2" :step="0.1" /></a-form-item>
            <a-row :gutter="8">
              <a-col :span="12"><a-form-item label="Top P"><a-slider v-model:value="params.topP" :min="0" :max="1" :step="0.05" /></a-form-item></a-col>
              <a-col :span="12"><a-form-item label="Max Tokens"><a-input-number v-model:value="params.maxTokens" :min="256" :max="8192" style="width:100%" /></a-form-item></a-col>
            </a-row>
            <a-form-item label="Prompt 模板">
              <a-select v-model:value="promptTemplate" style="width:100%">
                <a-select-option value="default">默认抽取模板</a-select-option>
                <a-select-option value="medical">医疗领域专用</a-select-option>
                <a-select-option value="finance">金融领域专用</a-select-option>
                <a-select-option value="fewshot">少样本学习模板</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="AI 增强选项">
              <a-checkbox-group v-model:value="enhancements" :options="enhOptions" />
            </a-form-item>
          </a-form>
        </div>
      </a-col>
    </a-row>

    <!-- 抽取结果 -->
    <div class="page-card mb-24" v-if="extracted">
      <div class="flex-between mb-16">
        <h3 style="margin:0;font-size:16px">抽取结果（高亮展示）</h3>
        <a-space>
          <a-button size="small"><CheckOutlined /> 全部确认</a-button>
          <a-button size="small"><DownloadOutlined /> 导出</a-button>
        </a-space>
      </div>
      <div class="highlight-text" v-html="highlightedText"></div>
      <a-divider />
      <a-tabs>
        <a-tab-pane key="entity" :tab="`实体 (${entities.length})`">
          <a-table :columns="entityColumns" :data-source="entities" :pagination="false" size="small" row-key="id">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'"><a-tag :color="entityColor[record.type]">{{ record.type }}</a-tag></template>
              <template v-else-if="column.key === 'confidence'">
                <a-progress :percent="record.confidence*100" :stroke-color="record.confidence>0.85?'#52c41a':'#faad14'" size="small" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small">确认</a-button>
                <a-button type="link" size="small" danger>删除</a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="relation" :tab="`关系 (${relations.length})`">
          <a-table :columns="relColumns" :data-source="relations" :pagination="false" size="small" row-key="id">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'relation'"><a-tag color="purple">{{ record.name }}</a-tag></template>
              <template v-else-if="column.key === 'confidence'">
                <a-progress :percent="record.confidence*100" size="small" />
              </template>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="attr" :tab="`属性 (${attrs.length})`">
          <a-table :columns="attrColumns" :data-source="attrs" :pagination="false" size="small" row-key="id" />
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 批量抽取任务 -->
    <div class="page-card">
      <div class="flex-between mb-16">
        <h3 style="margin:0;font-size:16px">批量抽取任务</h3>
        <a-button type="primary" size="small"><PlusOutlined /> 创建批量任务</a-button>
      </div>
      <a-table :columns="taskColumns" :data-source="extractionTasks" row-key="id" :pagination="{pageSize:8}">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'"><a-tag :color="typeColor[record.type]">{{ typeLabel[record.type] }}</a-tag></template>
          <template v-else-if="column.key === 'progress'">
            <a-progress :percent="Math.round(record.processed/record.corpusCount*100)" :status="record.status==='running'?'active':record.status==='failed'?'exception':'normal'" size="small" />
            <div class="text-secondary" style="font-size:11px">{{ record.processed }}/{{ record.corpusCount }}</div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="taskStatus[record.status].status" :text="taskStatus[record.status].text" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small">详情</a-button>
            <a-button type="link" size="small" v-if="record.status==='failed'">重试</a-button>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  PlusOutlined, RobotOutlined, ThunderboltOutlined, SettingOutlined,
  CheckOutlined, DownloadOutlined
} from '@ant-design/icons-vue'
import { extractionTasks } from '@/utils/mock'

const model = ref('gpt-4')
const threshold = ref(0.7)
const extracting = ref(false)
const extracted = ref(false)
const inputText = ref('')
const promptTemplate = ref('default')
const params = ref({ temperature: 0.2, topP: 0.9, maxTokens: 4096 })
const enhancements = ref(['schema', 'validate'])
const enhOptions = [
  { label: 'Schema 自适应', value: 'schema' },
  { label: '结果校验', value: 'validate' },
  { label: '实体消歧', value: 'disambig' },
  { label: '知识补全', value: 'complete' }
]

const sampleText = '马云，1964年9月10日出生于浙江省杭州市，阿里巴巴集团主要创始人。1999年，马云带领17位创始人在杭州创立阿里巴巴。蔡崇信也是阿里巴巴联合创始人之一，现任阿里巴巴集团董事会执行副主席。'

const entityColor: Record<string, string> = {
  人物: '#1677ff', 机构: '#52c41a', 地点: '#faad14', 事件: '#ff4d4f', 时间: '#13c2c2'
}

const entities = ref<any[]>([])
const relations = ref<any[]>([])
const attrs = ref<any[]>([])

const highlightedText = ref('')

function extract() {
  if (!inputText.value) return
  extracting.value = true
  setTimeout(() => {
    extracting.value = false
    extracted.value = true
    entities.value = [
      { id: 1, name: '马云', type: '人物', confidence: 0.98 },
      { id: 2, name: '杭州市', type: '地点', confidence: 0.95 },
      { id: 3, name: '阿里巴巴集团', type: '机构', confidence: 0.97 },
      { id: 4, name: '蔡崇信', type: '人物', confidence: 0.93 }
    ]
    relations.value = [
      { id: 1, source: '马云', name: '出生于', target: '杭州市', confidence: 0.96 },
      { id: 2, source: '马云', name: '创立', target: '阿里巴巴集团', confidence: 0.94 },
      { id: 3, source: '蔡崇信', name: '就职于', target: '阿里巴巴集团', confidence: 0.91 }
    ]
    attrs.value = [
      { id: 1, entity: '马云', attr: '出生日期', value: '1964年9月10日' },
      { id: 2, entity: '马云', attr: '国籍', value: '中国' },
      { id: 3, entity: '阿里巴巴集团', attr: '成立时间', value: '1999年' }
    ]
    // 高亮文本
    let html = inputText.value
    entities.value.forEach((e) => {
      const color = entityColor[e.type] || '#1677ff'
      html = html.replaceAll(e.name, `<span class="hl" style="background:${color}33;border-bottom:2px solid ${color};padding:1px 3px;border-radius:3px">${e.name}<sup style="font-size:10px;color:${color}">${e.type}</sup></span>`)
    })
    highlightedText.value = html
  }, 1200)
}

const entityColumns = [
  { title: '实体', dataIndex: 'name', key: 'name' },
  { title: '类型', key: 'type', width: 100 },
  { title: '置信度', key: 'confidence', width: 200 },
  { title: '操作', key: 'action', width: 140 }
]
const relColumns = [
  { title: '头实体', dataIndex: 'source', key: 'source' },
  { title: '关系', key: 'relation', width: 120 },
  { title: '尾实体', dataIndex: 'target', key: 'target' },
  { title: '置信度', key: 'confidence', width: 180 }
]
const attrColumns = [
  { title: '实体', dataIndex: 'entity', key: 'entity' },
  { title: '属性', dataIndex: 'attr', key: 'attr' },
  { title: '值', dataIndex: 'value', key: 'value' }
]

const typeColor: Record<string, string> = { llm: 'purple', dl: 'blue', structured: 'green' }
const typeLabel: Record<string, string> = { llm: '大模型', dl: '深度学习', structured: '结构化' }
const taskStatus: Record<string, { status: any; text: string }> = {
  running: { status: 'processing', text: '运行中' },
  success: { status: 'success', text: '成功' },
  failed: { status: 'error', text: '失败' },
  pending: { status: 'warning', text: '待处理' }
}
const taskColumns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 110 },
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '类型', key: 'type', width: 100 },
  { title: '模型', dataIndex: 'model', key: 'model', width: 130 },
  { title: '进度', key: 'progress', width: 180 },
  { title: '实体/关系', key: 'count', customRender: ({ record }: any) => `${record.entityCount}/${record.relationCount}`, width: 110 },
  { title: '状态', key: 'status', width: 100 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 150 },
  { title: '操作', key: 'action', width: 120 }
]
</script>

<style lang="less" scoped>
.highlight-text {
  padding: 16px; background: #fafbfc; border-radius: 8px;
  line-height: 2; font-size: 14px;
}
</style>
