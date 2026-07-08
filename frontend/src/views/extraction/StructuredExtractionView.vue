<template>
  <div class="page-container">
    <PageHeader title="结构化映射" description="将结构化/半结构化数据通过映射规则转化为知识图谱实体与关系">
      <template #extra>
        <a-steps :current="step" size="small" style="width:480px">
          <a-step title="选数据源" />
          <a-step title="配置映射" />
          <a-step title="预览" />
          <a-step title="执行" />
        </a-steps>
      </template>
    </PageHeader>

    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="12">
        <div class="page-card mapping-panel">
          <div class="flex-between mb-16">
            <h3 style="margin:0;font-size:16px">数据表字段（users）</h3>
            <a-select v-model:value="selectedTable" style="width:200px">
              <a-select-option value="users">users</a-select-option>
              <a-select-option value="companies">companies</a-select-option>
              <a-select-option value="relations">relations</a-select-option>
            </a-select>
          </div>
          <div class="field-list">
            <div class="field-item" v-for="f in tableFields" :key="f.name" draggable>
              <DatabaseOutlined :style="{color:'#1677ff'}" />
              <div class="field-info">
                <div class="field-name">{{ f.name }}</div>
                <div class="field-type">{{ f.type }}</div>
              </div>
            </div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="page-card mapping-panel">
          <h3 style="margin:0 0 16px;font-size:16px">图谱 Schema 映射</h3>
          <div class="mapping-target" v-for="m in mappings" :key="m.target">
            <div class="target-head">
              <a-tag :color="m.color">{{ m.entity }}</a-tag>
              <span class="target-name">{{ m.target }}</span>
            </div>
            <div class="target-mapped">
              <a-space wrap>
                <a-tag v-for="s in m.sources" :key="s" closable color="blue">{{ s }}</a-tag>
                <a-button type="dashed" size="small" @click="action.openCreate('添加字段', '为「' + m.target + '」添加来源字段')"><PlusOutlined /> 添加字段</a-button>
              </a-space>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="mb-24">
      <a-col :xs="24" :lg="16">
        <div class="page-card">
          <div class="flex-between mb-16">
            <h3 style="margin:0;font-size:16px">转化预览（抽样 100 条）</h3>
            <a-space>
              <a-button @click="step = 2">上一步</a-button>
              <a-button type="primary" @click="step = 3; runTask()">执行转化</a-button>
            </a-space>
          </div>
          <a-table :columns="previewColumns" :data-source="previewData" :pagination="false" size="small" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="8">
        <div class="page-card">
          <h3 style="margin:0 0 16px;font-size:16px">转化规则配置</h3>
          <a-form layout="vertical">
            <a-form-item label="目标项目">
              <a-select default-value="医疗健康知识图谱">
                <a-select-option v-for="p in projectList" :key="p.id">{{ p.name }}</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="实体对齐规则">
              <a-select mode="tags" default-value="名称完全匹配">
                <a-select-option value="名称完全匹配">名称完全匹配</a-select-option>
                <a-select-option value="模糊匹配">模糊匹配</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="数据转换规则">
              <a-textarea :rows="3" value="date_format(birth, 'YYYY-MM-DD')" />
            </a-form-item>
            <a-form-item label="是否覆盖已存在">
              <a-switch /> <span class="text-secondary" style="margin-left:8px">开启后将覆盖同名实体</span>
            </a-form-item>
          </a-form>
        </div>
      </a-col>
    </a-row>

    <div class="page-card">
      <h3 style="margin:0 0 16px;font-size:16px">映射模板</h3>
      <a-list :grid="{ gutter: 16, xs: 1, sm: 2, md: 4 }" :data-source="templates">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card size="small" hoverable>
              <a-card-meta :title="item.name" :description="item.desc">
                <template #avatar><a-avatar :style="{background:item.color}"><TableOutlined /></a-avatar></template>
              </a-card-meta>
              <template #actions>
                <a-button type="link" size="small" @click="action.info('已载入示例（演示）')">使用</a-button>
                <a-button type="link" size="small" @click="action.openEdit('映射模板', item.name)">编辑</a-button>
              </template>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { PlusOutlined, DatabaseOutlined, TableOutlined } from '@ant-design/icons-vue'
import { projectList } from '@/utils/mock'
import { useAction } from '@/composables/useAction'

const action = useAction()
const step = ref(1)
const selectedTable = ref('users')

const tableFields = [
  { name: 'id', type: 'BIGINT' },
  { name: 'name', type: 'VARCHAR(64)' },
  { name: 'birth_date', type: 'DATE' },
  { name: 'gender', type: 'TINYINT' },
  { name: 'company_id', type: 'BIGINT' },
  { name: 'position', type: 'VARCHAR(32)' },
  { name: 'city', type: 'VARCHAR(32)' },
  { name: 'email', type: 'VARCHAR(128)' },
  { name: 'created_at', type: 'DATETIME' }
]

const mappings = [
  { entity: '人物', target: '姓名', color: '#1677ff', sources: ['name'] },
  { entity: '人物', target: '出生日期', color: '#1677ff', sources: ['birth_date'] },
  { entity: '人物', target: '性别', color: '#1677ff', sources: ['gender'] },
  { entity: '机构', target: '名称', color: '#52c41a', sources: ['company_id'] },
  { entity: '就职于', target: '职位', color: '#722ed1', sources: ['position'] }
]

const previewColumns = [
  { title: '实体类型', dataIndex: 'entity', key: 'entity' },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '属性', dataIndex: 'props', key: 'props' },
  { title: '关系', dataIndex: 'relation', key: 'relation' },
  { title: '状态', key: 'status' }
]
const previewData = [
  { entity: '人物', name: '张三', props: '出生:1990-05-12 / 男', relation: '就职于→阿里巴巴', status: 'ok' },
  { entity: '人物', name: '李四', props: '出生:1985-08-20 / 女', relation: '就职于→腾讯', status: 'ok' },
  { entity: '机构', name: '阿里巴巴', props: '行业:互联网', relation: '-', status: 'ok' },
  { entity: '机构', name: '腾讯', props: '行业:互联网', relation: '-', status: 'ok' },
  { entity: '人物', name: '王五', props: '出生:1992-11-03 / 男', relation: '就职于→百度', status: 'dup' }
]

const templates = [
  { name: '用户表 → 人物', desc: '标准用户表映射人物实体', color: '#1677ff' },
  { name: '公司表 → 机构', desc: '公司信息表映射机构实体', color: '#52c41a' },
  { name: '关系表 → 关系', desc: '外键关系映射图谱关系', color: '#722ed1' },
  { name: '分类表 → 概念', desc: '分类标签映射概念实体', color: '#fa8c16' }
]

function runTask() {
  // 触发后端转化任务
}
</script>

<style lang="less" scoped>
.mapping-panel { min-height: 320px; }
.field-list { display: flex; flex-direction: column; gap: 8px; }
.field-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 12px; background: #fafbfc; border-radius: 8px;
  cursor: grab; border: 1px dashed transparent;
  &:hover { border-color: #1677ff; background: #f0f7ff; }
  .field-name { font-size: 13px; font-weight: 500; }
  .field-type { font-size: 11px; color: #9ca3af; }
}
.mapping-target {
  padding: 12px; border: 1px solid #f0f0f0; border-radius: 8px; margin-bottom: 12px;
  .target-head { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
  .target-name { font-weight: 500; }
}
</style>
