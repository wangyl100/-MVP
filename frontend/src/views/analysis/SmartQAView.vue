<template>
  <div class="page-container">
    <PageHeader title="智能问答" description="基于知识图谱的智能问答系统，支持自然语言提问与多跳推理" />

    <a-row :gutter="16" class="mb-24">
      <!-- 统计卡片 -->
      <a-col :xs="12" :md="6" v-for="s in stats" :key="s.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: s.bg, color: s.color }">
            <component :is="s.icon" />
          </div>
          <div>
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <!-- 问答主区 -->
      <a-col :xs="24" :lg="17">
        <div class="page-card qa-panel">
          <div class="qa-header">
            <a-space>
              <a-avatar :style="{ background: '#1677ff' }" size="large">
                <RobotOutlined />
              </a-avatar>
              <div>
                <h3 style="margin:0;font-size:16px">知识图谱智能助手</h3>
                <div class="text-secondary" style="font-size:12px">基于图谱推理 · 引用实体与关系</div>
              </div>
            </a-space>
            <a-space>
              <a-select v-model:value="qaModel" style="width:160px" size="small">
                <a-select-option value="gpt4">GPT-4 Turbo</a-select-option>
                <a-select-option value="claude">Claude 3.5 Sonnet</a-select-option>
                <a-select-option value="qwen">通义千问 Max</a-select-option>
                <a-select-option value="graph">图谱推理引擎</a-select-option>
              </a-select>
              <a-button size="small" @click="clearHistory">
                <template #icon><ClearOutlined /></template>
                清空
              </a-button>
            </a-space>
          </div>

          <!-- 对话区 -->
          <div class="qa-conversation" ref="convRef">
            <div v-if="!messages.length" class="qa-empty">
              <RobotOutlined style="font-size:48px;color:#1677ff" />
              <h3 style="margin:16px 0 8px">您好！我是知识图谱智能助手</h3>
              <p class="text-secondary">您可以向我提问关于实体、关系、事件等任何问题</p>
              <div class="qa-suggestions">
                <a-button v-for="ex in qaExamples" :key="ex" size="small" @click="askExample(ex)">
                  {{ ex }}
                </a-button>
              </div>
            </div>

            <div v-for="(m, i) in messages" :key="i" class="qa-msg" :class="m.role">
              <a-avatar v-if="m.role === 'assistant'" :style="{ background: '#1677ff' }" size="small">
                <RobotOutlined />
              </a-avatar>
              <a-avatar v-else :style="{ background: '#52c41a' }" size="small">
                <UserOutlined />
              </a-avatar>
              <div class="qa-bubble">
                <div class="qa-content">{{ m.content }}</div>
                <div v-if="m.sources && m.sources.length" class="qa-sources">
                  <div class="text-secondary" style="font-size:11px;margin-bottom:4px">
                    <LinkOutlined /> 引用知识（{{ m.sources.length }}）
                  </div>
                  <a-tag v-for="(s, si) in m.sources" :key="si" color="blue" style="margin:2px">{{ s }}</a-tag>
                </div>
              </div>
            </div>

            <div v-if="loading" class="qa-msg assistant">
              <a-avatar :style="{ background: '#1677ff' }" size="small"><RobotOutlined /></a-avatar>
              <div class="qa-bubble">
                <a-spin size="small" /> <span class="text-secondary" style="margin-left:8px">正在基于知识图谱推理...</span>
              </div>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="qa-input-area">
            <a-input
              v-model:value="input"
              placeholder="输入您的问题，例如：阿里巴巴的创始人是谁？"
              @press-enter="send"
              size="large"
            >
              <template #prefix><MessageOutlined style="color:#9ca3af" /></template>
            </a-input>
            <a-button type="primary" size="large" @click="send" :loading="loading">
              <template #icon><SendOutlined /></template>
              发送
            </a-button>
          </div>
        </div>
      </a-col>

      <!-- 侧边栏 -->
      <a-col :xs="24" :lg="7">
        <div class="page-card mb-24">
          <h3 style="margin:0 0 12px;font-size:15px">推荐问题</h3>
          <a-list :data-source="qaExamples" size="small">
            <template #renderItem="{ item }">
              <a-list-item style="cursor:pointer;padding:8px 0" @click="askExample(item)">
                <a-space>
                  <QuestionCircleOutlined style="color:#1677ff" />
                  <span style="font-size:13px">{{ item }}</span>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
        </div>

        <div class="page-card mb-24">
          <h3 style="margin:0 0 12px;font-size:15px">对话历史</h3>
          <a-list :data-source="historyList" size="small">
            <template #renderItem="{ item }">
              <a-list-item style="padding:8px 0;cursor:pointer" @click="action.info('加载对话：' + item.title + '（演示）')">
                <a-list-item-meta :title="item.title" :description="item.time">
                  <template #avatar><MessageOutlined style="color:#9ca3af" /></template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </div>

        <div class="page-card">
          <h3 style="margin:0 0 12px;font-size:15px">能力说明</h3>
          <a-space direction="vertical" style="width:100%" :size="6">
            <div class="capability"><CheckCircleFilled style="color:#52c41a" /> 实体属性查询</div>
            <div class="capability"><CheckCircleFilled style="color:#52c41a" /> 关系推理查询</div>
            <div class="capability"><CheckCircleFilled style="color:#52c41a" /> 多跳路径推理</div>
            <div class="capability"><CheckCircleFilled style="color:#52c41a" /> 实体聚合统计</div>
            <div class="capability"><CheckCircleFilled style="color:#52c41a" /> 时序事件查询</div>
          </a-space>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { PageHeader } from '@/components/PageHeader.vue'
import { useAction } from '@/composables/useAction'
import { qaExamples, qaHistory } from '@/utils/mock'
import {
  RobotOutlined, UserOutlined, MessageOutlined,
  SendOutlined, ClearOutlined, LinkOutlined, QuestionCircleOutlined, CheckCircleFilled,
  ThunderboltOutlined, TeamOutlined, DatabaseOutlined
} from '@ant-design/icons-vue'

const action = useAction()

const stats = [
  { label: '今日提问', value: 326, icon: MessageOutlined, color: '#1677ff', bg: '#e6f4ff' },
  { label: '回答准确率', value: '94.2%', icon: ThunderboltOutlined, color: '#52c41a', bg: '#f6ffed' },
  { label: '活跃用户', value: 58, icon: TeamOutlined, color: '#faad14', bg: '#fffbe6' },
  { label: '知识引用', value: '1.2k', icon: DatabaseOutlined, color: '#722ed1', bg: '#f9f0ff' }
]

interface Message {
  role: 'user' | 'assistant'
  content: string
  sources?: string[]
}

const messages = ref<Message[]>([...qaHistory as Message[]])
const input = ref('')
const loading = ref(false)
const qaModel = ref('gpt4')
const convRef = ref<HTMLElement | null>(null)

const historyList = [
  { title: '阿里巴巴创始人相关信息', time: '今天 14:32' },
  { title: '北京知名互联网公司', time: '今天 11:08' },
  { title: '人工智能领域重要事件', time: '昨天 16:45' },
  { title: 'ChatGPT 与 OpenAI 关系', time: '昨天 09:20' },
  { title: '张三与李四的合作关系', time: '07-04 18:12' }
]

function scrollToBottom() {
  nextTick(() => {
    if (convRef.value) convRef.value.scrollTop = convRef.value.scrollHeight
  })
}

function askExample(q: string) {
  input.value = q
  send()
}

function send() {
  if (!input.value.trim() || loading.value) return
  const q = input.value.trim()
  messages.value.push({ role: 'user', content: q })
  input.value = ''
  loading.value = true
  scrollToBottom()

  setTimeout(() => {
    const answer = generateAnswer(q)
    messages.value.push({ role: 'assistant', content: answer.content, sources: answer.sources })
    loading.value = false
    scrollToBottom()
  }, 900)
}

function generateAnswer(q: string): { content: string; sources: string[] } {
  if (q.includes('阿里巴巴') && q.includes('创始人')) {
    return {
      content: '阿里巴巴的主要创始人是马云。1999 年，马云带领 17 位创始人在杭州创立了阿里巴巴集团。除马云外，联合创始人还包括蔡崇信等人。',
      sources: ['E000123 马云', 'E000124 蔡崇信', 'R000456 创始人→阿里巴巴', 'R000789 联合创始人→阿里巴巴']
    }
  }
  if (q.includes('北京') && q.includes('公司')) {
    return {
      content: '北京有众多知名互联网公司，包括百度（由李彦宏创立）、字节跳动（旗下有抖音、今日头条）、京东、美团、新浪、滴滴出行、快手等。这些公司覆盖搜索、电商、社交、内容分发等多个领域。',
      sources: ['E000234 百度', 'E000235 字节跳动', 'E000236 京东', 'R000567 总部位于→北京']
    }
  }
  if (q.includes('ChatGPT') || q.includes('OpenAI')) {
    return {
      content: 'ChatGPT 是由 OpenAI 开发的大型语言模型产品。OpenAI 成立于 2015 年，总部位于美国旧金山，由 Sam Altman 担任 CEO，联合创始人包括 Elon Musk（后退出）、Greg Brockman、Ilya Sutskever 等人。',
      sources: ['E000345 ChatGPT', 'E000346 OpenAI', 'R000678 开发者→OpenAI']
    }
  }
  if (q.includes('关系')) {
    return {
      content: '根据知识图谱的推理结果，相关实体之间存在多种关系，包括合作关系、雇佣关系、投资关系等。具体关系链路已通过图谱多跳推理得出，可信度 0.92。',
      sources: ['R000890 合作→', 'R000891 投资→', 'R000892 雇佣→']
    }
  }
  return {
    content: `已基于知识图谱对「${q}」进行推理分析。本次推理共涉及 12 个实体、28 条关系，通过 2 跳路径推理得出结论。请查看引用的知识条目以了解推理依据。`,
    sources: ['E000001 实体A', 'E000002 实体B', 'R000001 关系链路']
  }
}

function clearHistory() {
  messages.value = []
}
</script>

<style lang="less" scoped>
.qa-panel { display: flex; flex-direction: column; height: 640px; }
.qa-header {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 12px; border-bottom: 1px solid #f0f0f0;
}
.qa-conversation {
  flex: 1; overflow-y: auto; padding: 16px 4px;
  display: flex; flex-direction: column; gap: 12px;
}
.qa-empty {
  text-align: center; padding: 40px 0;
  .qa-suggestions { margin-top: 20px; display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
}
.qa-msg {
  display: flex; gap: 8px;
  &.user { flex-direction: row-reverse; .qa-bubble { background: #e6f4ff; color: #1677ff; } }
  &.assistant { .qa-bubble { background: #f5f5f5; color: #1f2937; } }
}
.qa-bubble {
  max-width: 75%; padding: 10px 14px; border-radius: 12px;
  font-size: 14px; line-height: 1.6;
}
.qa-content { word-break: break-word; }
.qa-sources { margin-top: 8px; padding-top: 8px; border-top: 1px dashed #d9d9d9; }
.qa-input-area {
  display: flex; gap: 8px; padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.capability { font-size: 13px; color: #4b5563; }
</style>
