<template>
  <div class="page-container">
    <PageHeader title="标注工作台" description="对文本进行实体、关系、属性的标注操作">
      <template #extra>
        <a-space>
          <a-button-group>
            <a-button :type="mode==='entity'?'primary':'default'" @click="mode='entity'">实体标注</a-button>
            <a-button :type="mode==='relation'?'primary':'default'" @click="mode='relation'">关系标注</a-button>
            <a-button :type="mode==='attribute'?'primary':'default'" @click="mode='attribute'">属性标注</a-button>
          </a-button-group>
          <a-button type="primary" @click="action.notify('提交标注', '当前任务')"><SaveOutlined /> 提交标注</a-button>
        </a-space>
      </template>
    </PageHeader>

    <a-row :gutter="16">
      <!-- 文本标注区 -->
      <a-col :xs="24" :lg="18">
        <div class="page-card">
          <div class="flex-between mb-16">
            <a-space>
              <a-tag color="blue">任务：医疗实体标注</a-tag>
              <a-tag color="green">进度：35/100</a-tag>
              <a-tag color="purple">准确率：95.2%</a-tag>
            </a-space>
            <a-space>
              <a-tooltip title="撤销 (Ctrl+Z)"><a-button size="small" @click="action.notify('撤销', '标注')"><UndoOutlined /></a-button></a-tooltip>
              <a-tooltip title="重做 (Ctrl+Y)"><a-button size="small" @click="action.notify('重做', '标注')"><RedoOutlined /></a-button></a-tooltip>
              <a-tooltip title="快捷键"><a-button size="small" @click="action.info('快捷键：Ctrl+Z 撤销，Ctrl+Y 重做')"><QuestionOutlined /></a-button></a-tooltip>
            </a-space>
          </div>

          <!-- AI 预标注提示 -->
          <a-alert type="info" show-icon style="margin-bottom:12px">
            <template #message>
              <a-space>
                <RobotOutlined style="color:#722ed1" />
                <span>AI 已预标注 <b>18</b> 个实体，请审核确认</span>
                <a-button type="link" size="small" @click="action.notify('采纳', '全部 AI 预标注')">全部接受</a-button>
                <a-button type="link" size="small" danger @click="action.notify('拒绝', '全部 AI 预标注')">全部拒绝</a-button>
              </a-space>
            </template>
          </a-alert>

          <!-- 标注文本 -->
          <div class="annot-text">
            <span class="text-plain">2024年9月，</span>
            <span class="ent" style="--c:#1677ff">OpenAI<sup class="ent-tag">ORG</sup></span>
            <span class="text-plain">发布了新一代大模型</span>
            <span class="ent" style="--c:#13c2c2">o1<sup class="ent-tag">PRODUCT</sup></span>
            <span class="text-plain">，该模型在数学和编程能力上有显著提升。CEO </span>
            <span class="ent" style="--c:#1677ff">Sam Altman<sup class="ent-tag">PER</sup></span>
            <span class="text-plain">表示，</span>
            <span class="ent" style="--c:#13c2c2">o1<sup class="ent-tag">PRODUCT</sup></span>
            <span class="text-plain">是迈向 AGI 的重要一步。该模型发布于 </span>
            <span class="ent" style="--c:#faad14">旧金山<sup class="ent-tag">LOC</sup></span>
            <span class="text-plain">的发布会。</span>
          </div>

          <a-divider />
          <div class="legend">
            <span class="text-secondary">图例：</span>
            <a-tag color="blue">PER 人物</a-tag>
            <a-tag color="green">ORG 机构</a-tag>
            <a-tag color="orange">LOC 地点</a-tag>
            <a-tag color="cyan">PRODUCT 产品</a-tag>
            <a-tag color="red">EVT 事件</a-tag>
            <a-tag color="purple">TIME 时间</a-tag>
          </div>
        </div>

        <!-- 已标注列表 -->
        <div class="page-card mt-16">
          <h3 style="margin:0 0 12px;font-size:16px">已标注实体</h3>
          <a-table :columns="cols" :data-source="annotated" :pagination="false" size="small">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'"><a-tag :color="typeColor[record.type]">{{ record.type }}</a-tag></template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" @click="action.openEdit('标注实体', record.text)">编辑</a-button>
                <a-button type="link" size="small" danger @click="action.confirmDelete(record.text)">删除</a-button>
              </template>
            </template>
          </a-table>
        </div>
      </a-col>

      <!-- 右侧面板 -->
      <a-col :xs="24" :lg="6">
        <div class="page-card mb-24">
          <h3 style="margin:0 0 12px;font-size:16px">标注进度</h3>
          <a-progress type="circle" :percent="35" />
          <a-divider style="margin:12px 0" />
          <a-statistic title="今日完成" :value="35" />
          <a-statistic title="剩余" :value="65" style="margin-top:8px" />
          <a-statistic title="预计完成" value="2.5 小时" style="margin-top:8px" />
        </div>
        <div class="page-card">
          <h3 style="margin:0 0 12px;font-size:16px">标注规范</h3>
          <a-list :data-source="guidelines" size="small">
            <template #renderItem="{ item, index }">
              <a-list-item>{{ index + 1 }}. {{ item }}</a-list-item>
            </template>
          </a-list>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { SaveOutlined, UndoOutlined, RedoOutlined, QuestionOutlined, RobotOutlined } from '@ant-design/icons-vue'
import { useAction } from '@/composables/useAction'

const action = useAction()

const mode = ref('entity')

const typeColor: Record<string, string> = { PER: 'blue', ORG: 'green', LOC: 'orange', PRODUCT: 'cyan', EVT: 'red', TIME: 'purple' }

const cols = [
  { title: '文本', dataIndex: 'text', key: 'text' },
  { title: '类型', key: 'type', width: 100 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]
const annotated = [
  { text: 'OpenAI', type: 'ORG', source: 'AI预标注' },
  { text: 'o1', type: 'PRODUCT', source: 'AI预标注' },
  { text: 'Sam Altman', type: 'PER', source: '人工标注' },
  { text: '旧金山', type: 'LOC', source: '人工标注' }
]

const guidelines = [
  '人名标注为 PER 类型',
  '公司、机构标注为 ORG',
  '地理位置标注为 LOC',
  '产品名标注为 PRODUCT',
  '边界要包含完整实体',
  '同名实体保持类型一致'
]
</script>

<style lang="less" scoped>
.annot-text {
  padding: 24px; background: #fafbfc; border-radius: 8px;
  font-size: 16px; line-height: 2.4;
  .ent {
    background: var(--c, #1677ff);
    background: color-mix(in srgb, var(--c, #1677ff) 18%, transparent);
    border-bottom: 2px solid var(--c, #1677ff);
    padding: 1px 4px; border-radius: 3px; cursor: pointer;
    .ent-tag {
      font-size: 10px; color: var(--c, #1677ff);
      margin-left: 2px; font-weight: 600;
    }
  }
}
.legend { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }
</style>
