<template>
  <div class="page-container">
    <PageHeader title="用户管理" description="管理平台用户账号、角色分配与状态">
      <template #extra>
        <a-space>
          <a-button>
            <UploadOutlined /> 批量导入
          </a-button>
          <a-button type="primary" @click="modalVisible = true">
            <PlusOutlined /> 新增用户
          </a-button>
        </a-space>
      </template>
    </PageHeader>

    <div class="page-card">
      <!-- 筛选 -->
      <a-form layout="inline" class="mb-16">
        <a-form-item label="用户名">
          <a-input v-model:value="filters.keyword" placeholder="搜索用户名/昵称" allow-clear style="width: 200px" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="filters.role" placeholder="全部角色" allow-clear style="width: 160px">
            <a-select-option v-for="r in roleList" :key="r.code" :value="r.name">{{ r.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="filters.status" placeholder="全部状态" allow-clear style="width: 120px">
            <a-select-option value="active">正常</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
            <a-select-option value="pending">待激活</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary"><SearchOutlined /> 查询</a-button>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="filteredUsers"
        row-key="id"
        :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (t:number) => `共 ${t} 条` }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'nickname'">
            <a-avatar style="background-color: #1677ff">{{ record.nickname.charAt(0) }}</a-avatar>
            <span style="margin-left: 8px">{{ record.nickname }}</span>
          </template>
          <template v-else-if="column.key === 'role'">
            <a-tag :color="roleColor[record.role]">{{ record.role }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="statusMap[record.status].status" :text="statusMap[record.status].text" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small">编辑</a-button>
            <a-divider type="vertical" />
            <a-button type="link" size="small">详情</a-button>
            <a-divider type="vertical" />
            <a-popconfirm title="确定删除该用户吗？" ok-text="确定" cancel-text="取消">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增用户弹窗 -->
    <a-modal v-model:open="modalVisible" title="新增用户" width="560px" ok-text="确定" cancel-text="取消">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="用户名"><a-input placeholder="请输入" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="昵称"><a-input placeholder="请输入" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="邮箱"><a-input placeholder="请输入" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="手机号"><a-input placeholder="请输入" /></a-form-item></a-col>
          <a-col :span="12">
            <a-form-item label="角色">
              <a-select placeholder="请选择">
                <a-select-option v-for="r in roleList" :key="r.code" :value="r.name">{{ r.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12"><a-form-item label="初始密码"><a-input-password placeholder="请输入" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { PlusOutlined, UploadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { userList, roleList } from '@/utils/mock'

const filters = ref({ keyword: '', role: undefined as any, status: undefined as any })
const modalVisible = ref(false)

const columns = [
  { title: '用户ID', dataIndex: 'id', key: 'id', width: 90 },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '角色', dataIndex: 'role', key: 'role' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '最后登录', dataIndex: 'lastLogin', key: 'lastLogin' },
  { title: '操作', key: 'action', width: 200 }
]

const statusMap: Record<string, { status: any; text: string }> = {
  active: { status: 'success', text: '正常' },
  disabled: { status: 'error', text: '禁用' },
  pending: { status: 'warning', text: '待激活' }
}

const roleColor: Record<string, string> = {
  超级管理员: 'magenta',
  项目管理员: 'purple',
  算法工程师: 'blue',
  标注人员: 'orange',
  普通用户: 'default'
}

const filteredUsers = computed(() => {
  return userList.filter((u) => {
    if (filters.value.keyword && !u.nickname.includes(filters.value.keyword) && !u.username.includes(filters.value.keyword)) return false
    if (filters.value.role && u.role !== filters.value.role) return false
    if (filters.value.status && u.status !== filters.value.status) return false
    return true
  })
})
</script>
