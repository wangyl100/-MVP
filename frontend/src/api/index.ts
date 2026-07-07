/**
 * API 服务层：统一封装所有后端接口调用。
 * - 后端可用时：调用 /api/v1/* 真实接口
 * - 后端不可用时：自动降级到 @/utils/mock 中的 Mock 数据，保证页面可见
 *
 * 使用方式：
 *   import { api } from '@/api'
 *   const stats = await api.dashboard.stats()
 */
import { request } from './request'
import * as mock from '@/utils/mock'

export const api = {
  // ===== Dashboard =====
  dashboard: {
    stats: () => request({ url: '/dashboard/stats' }, mock.dashboardStats),
    taskStatus: () => request({ url: '/dashboard/task-status' }, mock.taskStatusStats),
    growthTrend: () => request({ url: '/dashboard/growth-trend' }, mock.growthTrend),
    entityTypeDist: () => request({ url: '/dashboard/entity-type-dist' }, mock.entityTypeDistribution),
    recentActivities: () => request({ url: '/dashboard/recent-activities' }, mock.recentActivities)
  },

  // ===== 平台管理 =====
  platform: {
    users: () => request({ url: '/users' }, mock.userList),
    roles: () => request({ url: '/roles' }, mock.roleList)
  },

  // ===== 图谱项目 =====
  project: {
    list: () => request({ url: '/projects' }, mock.projectList),
    schemaEntityTypes: () => request({ url: '/schema/entity-types' }, mock.schemaEntityTypes),
    schemaRelationTypes: () => request({ url: '/schema/relation-types' }, mock.schemaRelationTypes)
  },

  // ===== 数据接入 =====
  data: {
    sources: () => request({ url: '/data-sources' }, mock.dataSourceList),
    corpus: (params?: { keyword?: string; language?: string }) =>
      request({ url: '/corpus', params }, mock.corpusList),
    corpusStats: () => request({ url: '/corpus/stats' }, {
      total: 8543, totalWords: 12845032, chineseCount: 6421, englishCount: 2122, avgWords: 1503
    })
  },

  // ===== 知识抽取 =====
  extraction: {
    tasks: (params?: { type?: string; status?: string }) =>
      request({ url: '/extraction/tasks', params }, mock.extractionTasks),
    structuredTasks: () => request({ url: '/extraction/structured/tasks' }, mock.extractionTasks),
    llmTasks: () => request({ url: '/extraction/llm/tasks' }, mock.extractionTasks),
    dlTasks: () => request({ url: '/extraction/dl/tasks' }, mock.extractionTasks),
    stats: () => request({ url: '/extraction/stats' }, {
      totalTasks: 156, todayTasks: 12, successRate: 0.923, avgDuration: '8m 42s',
      totalEntities: 1284503, totalRelations: 3892105
    })
  },

  // ===== 数据标注 =====
  annotation: {
    tasks: (params?: { type?: string; status?: string }) =>
      request({ url: '/annotation/tasks', params }, mock.annotationTasks),
    qualityOverview: () => request({ url: '/annotation/quality/overview' }, {
      totalAnnotated: 45621, avgQuality: 0.918, passRate: 0.892, reviewRate: 0.645, consistency: 0.876
    })
  },

  // ===== 模型训练 =====
  model: {
    trainingTasks: (params?: { status?: string }) =>
      request({ url: '/model/training/tasks', params }, mock.trainingTasks),
    evaluationList: () => request({ url: '/model/evaluation/list' }, mock.modelList),
    versionList: (params?: { modelId?: string }) =>
      request({ url: '/model/version/list', params }, [])
  },

  // ===== 图谱实例 =====
  graph: {
    entities: (params?: { keyword?: string; type?: string; source?: string; page?: number; pageSize?: number }) =>
      request({ url: '/graph/entities', params }, { list: mock.entityList, total: mock.entityList.length, page: 1, pageSize: 20 }),
    entityStats: () => request({ url: '/graph/entities/stats' }, {
      total: 1284503, byType: mock.entityTypeDistribution
    }),
    relations: (params?: { keyword?: string; type?: string; page?: number; pageSize?: number }) =>
      request({ url: '/graph/relations', params }, { list: mock.relationList, total: mock.relationList.length, page: 1, pageSize: 20 }),
    versions: () => request({ url: '/graph/versions' }, mock.graphVersions)
  },

  // ===== 图谱探索 =====
  explore: {
    graph: (params?: { center?: string; type?: string; limit?: number }) =>
      request({ url: '/explore/graph', params }, { nodes: [], edges: [] }),
    search: (params: { keyword: string; type?: string; source?: string; page?: number; pageSize?: number }) =>
      request({ url: '/explore/search', params }, { list: mock.entityList, total: mock.entityList.length, page: 1, pageSize: 20 }),
    hotKeywords: () => request({ url: '/explore/search/hot' }, []),
    searchHistory: () => request({ url: '/explore/search/history' }, []),
    recommend: () => request({ url: '/explore/search/recommend' }, [])
  },

  // ===== 智能分析 =====
  analysis: {
    qaExamples: () => request({ url: '/analysis/qa/examples' }, mock.qaExamples),
    qaHistory: () => request({ url: '/analysis/qa/history' }, mock.qaHistory),
    ask: (question: string, model?: string) =>
      request({ url: '/analysis/qa/ask', method: 'post', data: { question, model: model || 'GPT-4' } }, {
        question, answer: '后端不可用，这是 Mock 回答。', model: model || 'GPT-4', sources: [], confidence: 0
      }),
    reasoningEngines: () => request({ url: '/analysis/reasoning/engines' }, []),
    statisticsOverview: () => request({ url: '/analysis/statistics/overview' }, {
      coreMetrics: [], entityTypeDist: mock.entityTypeDistribution, graphMetrics: mock.statisticsData.graphMetrics
    }),
    graphMetrics: () => request({ url: '/analysis/statistics/graph-metrics' }, mock.statisticsData.graphMetrics),
    topEntities: (limit?: number) => request({ url: '/analysis/statistics/top-entities', params: { limit } }, []),
    projectStats: () => request({ url: '/analysis/statistics/projects' }, [])
  }
}

export default api
