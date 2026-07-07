<template>
  <div class="page-container">
    <PageHeader title="角色权限" description="基于 RBAC 的角色与权限管理">
      <template #extra>
        <a-button type="primary"><PlusOutlined /> 新建角色</a-button>
      </template>
    </PageHeader>

    <a-row :gutter="16">
      <a-col :xs="24" :lg="8" v-for="role in roleList" :key="role.id">
        <a-card class="role-card" hoverable>
          <div class="role-header">
            <div class="role-icon" :style="{ background: roleColor[role.code] }">
              <SafetyOutlined />
            </div>
            <div>
              <h3 class="role-name">{{ role.name }}</h3>
              <a-tag color="default">{{ role.code }}</a-tag>
            </div>
          </div>
          <p class="role-desc">{{ role.desc }}</p>
          <a-divider style="margin: 12px 0" />
          <a-row :gutter="16">
            <a-col :span="8">
              <a-statistic title="用户数" :value="role.userCount" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="权限数" :value="role.permissions" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="状态" value="启用" value-style="color:#52c41a" />
            </a-col>
          </a-row>
          <a-divider style="margin: 12px 0" />
          <a-space>
            <a-button type="primary" size="small" ghost>配置权限</a-button>
            <a-button size="small">编辑</a-button>
            <a-button size="small" danger>删除</a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <div class="page-card mt-16">
      <h3 style="margin: 0 0 16px; font-size: 16px">权限矩阵</h3>
      <a-table
        :columns="permColumns"
        :data-source="permMatrix"
        :pagination="false"
        size="middle"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'module'">
            <strong>{{ record.module }}</strong>
          </template>
          <template v-else>
            <a-checkbox :checked="record[column.key]" />
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import { PlusOutlined, SafetyOutlined } from '@ant-design/icons-vue'
import { roleList } from '@/utils/mock'

const roleColor: Record<string, string> = {
  super_admin: 'linear-gradient(135deg,#ff4d4f,#ff7875)',
  project_admin: 'linear-gradient(135deg,#722ed1,#9254de)',
  algorithm_engineer: 'linear-gradient(135deg,#1677ff,#4096ff)',
  annotator: 'linear-gradient(135deg,#fa8c16,#ffa940)',
  user: 'linear-gradient(135deg,#52c41a,#73d13d)'
}

const permColumns = [
  { title: '功能模块', key: 'module', width: 160 },
  { title: '超级管理员', key: 'super', align: 'center' as const },
  { title: '项目管理员', key: 'project', align: 'center' as const },
  { title: '算法工程师', key: 'algo', align: 'center' as const },
  { title: '标注人员', key: 'annotator', align: 'center' as const },
  { title: '普通用户', key: 'user', align: 'center' as const }
]

const permMatrix = [
  { module: '用户管理', super: true, project: false, algo: false, annotator: false, user: false },
  { module: '项目管理', super: true, project: true, algo: false, annotator: false, user: false },
  { module: 'Schema 设计', super: true, project: true, algo: true, annotator: false, user: false },
  { module: '数据接入', super: true, project: true, algo: true, annotator: false, user: false },
  { module: '知识抽取', super: true, project: true, algo: true, annotator: false, user: false },
  { module: '数据标注', super: true, project: true, algo: false, annotator: true, user: false },
  { module: '模型训练', super: true, project: false, algo: true, annotator: false, user: false },
  { module: '图谱浏览', super: true, project: true, algo: true, annotator: true, user: true },
  { module: '智能问答', super: true, project: true, algo: true, annotator: true, user: true },
  { module: '系统设置', super: true, project: false, algo: false, annotator: false, user: false }
]
</script>

<style lang="less" scoped>
.role-card {
  margin-bottom: 16px;
  .role-header {
    display: flex;
    align-items: center;
    gap: 12px;
    .role-icon {
      width: 48px; height: 48px;
      border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
      color: #fff; font-size: 22px;
    }
    .role-name { margin: 0; font-size: 16px; font-weight: 600; }
  }
  .role-desc { color: #6b7280; font-size: 13px; margin: 12px 0 0; min-height: 40px; }
}
</style>
