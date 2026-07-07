<template>
  <div class="page-container">
    <PageHeader title="个人中心" description="管理个人信息、安全设置与大模型配置" />

    <a-row :gutter="16">
      <a-col :xs="24" :lg="8">
        <div class="page-card" style="text-align: center">
          <a-avatar :size="100" style="background-color: #1677ff; font-size: 36px">
            {{ userInfo.nickname.charAt(0) }}
          </a-avatar>
          <h3 style="margin: 16px 0 4px">{{ userInfo.nickname }}</h3>
          <a-tag color="magenta">{{ userInfo.role }}</a-tag>
          <a-divider />
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="用户名">{{ userInfo.username }}</a-descriptions-item>
            <a-descriptions-item label="邮箱">{{ userInfo.email }}</a-descriptions-item>
            <a-descriptions-item label="手机号">138****8888</a-descriptions-item>
            <a-descriptions-item label="所属部门">算法研发部</a-descriptions-item>
            <a-descriptions-item label="注册时间">2025-01-15</a-descriptions-item>
          </a-descriptions>
        </div>
      </a-col>

      <a-col :xs="24" :lg="16">
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="info" tab="基本信息">
            <a-form layout="vertical" style="max-width: 480px">
              <a-form-item label="头像">
                <a-upload :show-upload-list="false"><a-button><UploadOutlined /> 更换头像</a-button></a-upload>
              </a-form-item>
              <a-form-item label="昵称"><a-input :value="userInfo.nickname" /></a-form-item>
              <a-form-item label="邮箱"><a-input :value="userInfo.email" /></a-form-item>
              <a-form-item label="手机号"><a-input value="13888888888" /></a-form-item>
              <a-form-item label="个人简介">
                <a-textarea :rows="3" value="从事知识图谱与大模型应用研发工作。" />
              </a-form-item>
              <a-form-item><a-button type="primary">保存修改</a-button></a-form-item>
            </a-form>
          </a-tab-pane>

          <a-tab-pane key="password" tab="修改密码">
            <a-form layout="vertical" style="max-width: 400px">
              <a-form-item label="原密码"><a-input-password placeholder="请输入原密码" /></a-form-item>
              <a-form-item label="新密码"><a-input-password placeholder="请输入新密码" /></a-form-item>
              <a-form-item label="确认密码"><a-input-password placeholder="请再次输入" /></a-form-item>
              <a-form-item><a-button type="primary">更新密码</a-button></a-form-item>
            </a-form>
          </a-tab-pane>

          <a-tab-pane key="llm" tab="大模型配置">
            <a-alert message="在此配置个人使用的大模型 API Key，调用时优先使用个人配置" type="info" show-icon style="margin-bottom: 16px" />
            <a-list :data-source="llmConfigs" bordered>
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #title>
                      <a-space>
                        <a-tag :color="item.color">{{ item.name }}</a-tag>
                        <span v-if="item.active"><a-badge status="success" text="启用中" /></span>
                      </a-space>
                    </template>
                    <template #description>API Key: {{ item.keyPreview }}</template>
                    <template #avatar>
                      <a-avatar :style="{ background: item.color }">{{ item.name.charAt(0) }}</a-avatar>
                    </template>
                  </a-list-item-meta>
                  <template #actions>
                    <a-button type="link" size="small">编辑</a-button>
                    <a-switch :checked="item.active" size="small" />
                  </template>
                </a-list-item>
              </template>
            </a-list>
            <a-button type="dashed" block style="margin-top: 16px"><PlusOutlined /> 添加模型配置</a-button>
          </a-tab-pane>

          <a-tab-pane key="apikey" tab="API 密钥">
            <a-table :columns="apiKeyColumns" :data-source="apiKeys" :pagination="false" row-key="id">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'key'">
                  <a-typography-text copy :content="record.key">{{ record.key }}</a-typography-text>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-badge :status="record.status === 'active' ? 'success' : 'error'" :text="record.status === 'active' ? '启用' : '已禁用'" />
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-button type="link" size="small" danger>禁用</a-button>
                </template>
              </template>
            </a-table>
            <a-button type="primary" style="margin-top: 16px"><PlusOutlined /> 生成新密钥</a-button>
          </a-tab-pane>

          <a-tab-pane key="log" tab="操作日志">
            <a-timeline>
              <a-timeline-item v-for="(log, idx) in logs" :key="idx" :color="log.color">
                <p style="margin: 0">{{ log.action }}</p>
                <p style="margin: 4px 0 0; color: #6b7280; font-size: 12px">{{ log.time }} · {{ log.ip }}</p>
              </a-timeline-item>
            </a-timeline>
          </a-tab-pane>
        </a-tabs>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { useAppStore } from '@/stores/app'
import { PlusOutlined, UploadOutlined } from '@ant-design/icons-vue'

const appStore = useAppStore()
const userInfo = computed(() => appStore.userInfo)
const activeTab = ref('info')

const llmConfigs = [
  { name: 'GPT-4', color: '#10a37f', keyPreview: 'sk-****...****a1b2', active: true },
  { name: 'Claude-3.5', color: '#d97757', keyPreview: 'sk-ant-****...****c3d4', active: false },
  { name: '文心一言', color: '#2932e1', keyPreview: '****...****e5f6', active: false },
  { name: '通义千问', color: '#615ced', keyPreview: '****...****g7h8', active: false }
]

const apiKeyColumns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: 'API Key', dataIndex: 'key', key: 'key' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action' }
]
const apiKeys = [
  { id: 1, name: '生产环境调用', key: 'kg_a1b2c3d4e5f6g7h8', createTime: '2026-06-15', status: 'active' },
  { id: 2, name: '测试环境', key: 'kg_x1y2z3w4v5u6t7s8', createTime: '2026-05-20', status: 'disabled' }
]

const logs = [
  { action: '登录系统', time: '2026-07-06 09:15:23', ip: '192.168.1.100', color: 'green' },
  { action: '创建知识抽取任务 EXT0012', time: '2026-07-06 10:23:11', ip: '192.168.1.100', color: 'blue' },
  { action: '修改 Schema 定义', time: '2026-07-05 16:42:08', ip: '192.168.1.100', color: 'blue' },
  { action: '上传语料文件 32 个', time: '2026-07-05 14:08:55', ip: '192.168.1.100', color: 'blue' },
  { action: '修改个人密码', time: '2026-07-04 11:23:42', ip: '192.168.1.100', color: 'orange' }
]
</script>
