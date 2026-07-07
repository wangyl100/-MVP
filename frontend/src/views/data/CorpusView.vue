<template>
  <div class="page-container">
    <PageHeader title="语料管理" description="统一管理知识抽取与模型训练的语料数据">
      <template #extra>
        <a-space>
          <a-button><FolderOpenOutlined /> 新建语料集</a-button>
          <a-upload :show-upload-list="false" multiple>
            <a-button type="primary"><CloudUploadOutlined /> 上传语料</a-button>
          </a-upload>
        </a-space>
      </template>
    </PageHeader>

    <!-- 统计 -->
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

    <!-- 上传区 -->
    <div class="page-card mb-24">
      <a-upload-dragger :show-upload-list="false" multiple>
        <p class="ant-upload-drag-icon"><CloudUploadOutlined style="font-size:48px;color:#1677ff" /></p>
        <p class="ant-upload-text">点击或拖拽文件到此处上传</p>
        <p class="ant-upload-hint">支持 TXT、Word、PDF、HTML、CSV、JSON 格式，单文件最大 100MB，支持批量上传</p>
      </a-upload-dragger>
    </div>

    <!-- 语料列表 -->
    <div class="page-card">
      <div class="flex-between mb-16">
        <a-space>
          <a-input v-model:value="search" placeholder="搜索语料名称" allow-clear style="width:220px"><template #prefix><SearchOutlined /></template></a-input>
          <a-select v-model:value="lang" placeholder="语言" allow-clear style="width:120px">
            <a-select-option value="中文">中文</a-select-option>
            <a-select-option value="英文">英文</a-select-option>
          </a-select>
          <a-select v-model:value="source" placeholder="来源" allow-clear style="width:140px">
            <a-select-option v-for="s in sources" :key="s">{{ s }}</a-select-option>
          </a-select>
        </a-space>
        <a-space>
          <a-button>批量导出</a-button>
          <a-button danger>批量删除</a-button>
        </a-space>
      </div>
      <a-table :columns="columns" :data-source="filtered" row-key="id" :pagination="{pageSize:10, showTotal:(t:number)=>`共 ${t} 条`}" :row-selection="{ selectedRowKeys: selectedKeys, onChange: (k:any)=>selectedKeys=k }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-space>
              <FileTextOutlined :style="{color: fileColor(record.name)}" />
              <a-button type="link" size="small" @click="previewVisible = true; previewFile = record">{{ record.name }}</a-button>
            </a-space>
          </template>
          <template v-else-if="column.key === 'tags'">
            <a-tag v-for="t in record.tags" :key="t" color="blue">{{ t }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="record.status === 'ready' ? 'success' : 'processing'" :text="record.status === 'ready' ? '就绪' : '处理中'" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="previewVisible = true; previewFile = record">预览</a-button>
            <a-button type="link" size="small">抽取</a-button>
            <a-button type="link" size="small" danger>删除</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 预览弹窗 -->
    <a-modal v-model:open="previewVisible" :title="previewFile?.name" width="720px" :footer="null">
      <div class="preview-content">
        <p>知识图谱（Knowledge Graph）是一种结构化的语义知识库，用于以符号形式描述物理世界中的概念及其关系。</p>
        <p>知识图谱的本质是由实体、关系和属性组成的语义网络。其中，实体是客观存在的事物，关系是实体之间的关联，属性是实体的特征描述。</p>
        <p>知识图谱的构建一般包括知识抽取、知识融合、知识加工和知识更新四个阶段。知识抽取是从结构化、半结构化和非结构化数据中提取出实体、关系和属性。</p>
        <p>近年来，随着大语言模型（LLM）的发展，基于 LLM 的知识抽取方法成为研究热点，能够实现零样本或少样本的知识抽取，大幅降低构建成本……</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  CloudUploadOutlined, FolderOpenOutlined, FileTextOutlined, SearchOutlined,
  DatabaseOutlined, ReadOutlined, GlobalOutlined
} from '@ant-design/icons-vue'
import { corpusList, formatNumber } from '@/utils/mock'

const stats = [
  { title: '语料总数', value: formatNumber(8543), color: 'linear-gradient(135deg,#1677ff,#4096ff)', icon: FileTextOutlined },
  { title: '总字数', value: '2.4 亿', color: 'linear-gradient(135deg,#52c41a,#73d13d)', icon: ReadOutlined },
  { title: '语料集', value: 24, color: 'linear-gradient(135deg,#fa8c16,#ffa940)', icon: FolderOpenOutlined },
  { title: '今日新增', value: 128, color: 'linear-gradient(135deg,#722ed1,#9254de)', icon: CloudUploadOutlined }
]

const search = ref('')
const lang = ref(undefined as any)
const source = ref(undefined as any)
const sources = ['手动上传', 'API导入', '爬虫采集', '数据库导入']
const selectedKeys = ref<string[]>([])
const previewVisible = ref(false)
const previewFile = ref<any>(null)

const columns = [
  { title: '语料名称', key: 'name' },
  { title: '大小', dataIndex: 'size', key: 'size', width: 90 },
  { title: '字数', dataIndex: 'words', key: 'words', width: 100, customRender: ({ text }: any) => formatNumber(text) },
  { title: '语言', dataIndex: 'language', key: 'language', width: 80 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 110 },
  { title: '标签', key: 'tags' },
  { title: '上传时间', dataIndex: 'uploadTime', key: 'uploadTime', width: 120 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const filtered = computed(() => {
  return corpusList.filter((c) => {
    if (search.value && !c.name.includes(search.value)) return false
    if (lang.value && c.language !== lang.value) return false
    if (source.value && c.source !== source.value) return false
    return true
  })
})

function fileColor(name: string) {
  if (name.endsWith('.pdf')) return '#ff4d4f'
  if (name.endsWith('.docx')) return '#1677ff'
  if (name.endsWith('.html')) return '#fa8c16'
  return '#52c41a'
}
</script>

<style lang="less" scoped>
.preview-content {
  max-height: 480px; overflow-y: auto; line-height: 1.8; color: #1f2937;
  p { margin: 0 0 12px; }
}
</style>
