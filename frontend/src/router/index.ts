import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const MainLayout = () => import('@/layouts/MainLayout.vue')

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '系统首页', icon: 'DashboardOutlined' }
      },
      // 平台管理
      {
        path: 'platform/users',
        name: 'Users',
        component: () => import('@/views/platform/UsersView.vue'),
        meta: { title: '用户管理', icon: 'TeamOutlined' }
      },
      {
        path: 'platform/roles',
        name: 'Roles',
        component: () => import('@/views/platform/RolesView.vue'),
        meta: { title: '角色权限', icon: 'SafetyOutlined' }
      },
      {
        path: 'platform/profile',
        name: 'Profile',
        component: () => import('@/views/platform/ProfileView.vue'),
        meta: { title: '个人中心', icon: 'UserOutlined' }
      },
      // 图谱项目
      {
        path: 'project/list',
        name: 'ProjectList',
        component: () => import('@/views/project/ProjectListView.vue'),
        meta: { title: '项目管理', icon: 'FolderOutlined' }
      },
      {
        path: 'project/schema',
        name: 'SchemaDesign',
        component: () => import('@/views/project/SchemaDesignView.vue'),
        meta: { title: '图谱模型', icon: 'ApartmentOutlined' }
      },
      // 数据接入
      {
        path: 'data/source',
        name: 'DataSource',
        component: () => import('@/views/data/DataSourceView.vue'),
        meta: { title: '数据接入', icon: 'DatabaseOutlined' }
      },
      {
        path: 'data/corpus',
        name: 'Corpus',
        component: () => import('@/views/data/CorpusView.vue'),
        meta: { title: '语料管理', icon: 'FileTextOutlined' }
      },
      // 知识抽取
      {
        path: 'extraction/structured',
        name: 'StructuredExtraction',
        component: () => import('@/views/extraction/StructuredExtractionView.vue'),
        meta: { title: '结构化映射', icon: 'TableOutlined' }
      },
      {
        path: 'extraction/llm',
        name: 'LlmExtraction',
        component: () => import('@/views/extraction/LlmExtractionView.vue'),
        meta: { title: '大模型抽取', icon: 'RobotOutlined' }
      },
      {
        path: 'extraction/dl',
        name: 'DlExtraction',
        component: () => import('@/views/extraction/DlExtractionView.vue'),
        meta: { title: '深度学习抽取', icon: 'ThunderboltOutlined' }
      },
      // 数据标注
      {
        path: 'annotation/task',
        name: 'AnnotationTask',
        component: () => import('@/views/annotation/AnnotationTaskView.vue'),
        meta: { title: '标注任务', icon: 'EditOutlined' }
      },
      {
        path: 'annotation/workspace',
        name: 'AnnotationWorkspace',
        component: () => import('@/views/annotation/AnnotationWorkspaceView.vue'),
        meta: { title: '标注工作台', icon: 'HighlightOutlined' }
      },
      {
        path: 'annotation/quality',
        name: 'AnnotationQuality',
        component: () => import('@/views/annotation/AnnotationQualityView.vue'),
        meta: { title: '质量控制', icon: 'AuditOutlined' }
      },
      // 模型训练
      {
        path: 'model/training',
        name: 'ModelTraining',
        component: () => import('@/views/model/ModelTrainingView.vue'),
        meta: { title: '模型训练', icon: 'ExperimentOutlined' }
      },
      {
        path: 'model/evaluation',
        name: 'ModelEvaluation',
        component: () => import('@/views/model/ModelEvaluationView.vue'),
        meta: { title: '效果评估', icon: 'LineChartOutlined' }
      },
      {
        path: 'model/version',
        name: 'ModelVersion',
        component: () => import('@/views/model/ModelVersionView.vue'),
        meta: { title: '版本管理', icon: 'HistoryOutlined' }
      },
      // 图谱实例
      {
        path: 'graph/entity',
        name: 'EntityManage',
        component: () => import('@/views/graph/EntityManageView.vue'),
        meta: { title: '实体管理', icon: 'NodeIndexOutlined' }
      },
      {
        path: 'graph/relation',
        name: 'RelationManage',
        component: () => import('@/views/graph/RelationManageView.vue'),
        meta: { title: '关系管理', icon: 'ShareAltOutlined' }
      },
      {
        path: 'graph/version',
        name: 'GraphVersion',
        component: () => import('@/views/graph/GraphVersionView.vue'),
        meta: { title: '图谱版本', icon: 'CameraOutlined' }
      },
      // 图谱探索
      {
        path: 'explore/visual',
        name: 'GraphVisual',
        component: () => import('@/views/explore/GraphVisualView.vue'),
        meta: { title: '图谱可视化', icon: 'DeploymentUnitOutlined' }
      },
      {
        path: 'explore/search',
        name: 'EntitySearch',
        component: () => import('@/views/explore/EntitySearchView.vue'),
        meta: { title: '实体检索', icon: 'SearchOutlined' }
      },
      // 智能分析
      {
        path: 'analysis/qa',
        name: 'SmartQA',
        component: () => import('@/views/analysis/SmartQAView.vue'),
        meta: { title: '智能问答', icon: 'MessageOutlined' }
      },
      {
        path: 'analysis/reasoning',
        name: 'Reasoning',
        component: () => import('@/views/analysis/ReasoningView.vue'),
        meta: { title: '知识推理', icon: 'BulbOutlined' }
      },
      {
        path: 'analysis/statistics',
        name: 'Statistics',
        component: () => import('@/views/analysis/StatisticsView.vue'),
        meta: { title: '统计分析', icon: 'PieChartOutlined' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, _from, next) => {
  const title = (to.meta?.title as string) || ''
  document.title = title ? `${title} - 智能知识图谱构建平台` : '智能知识图谱构建平台'
  next()
})

export default router
