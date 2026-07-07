export interface MenuItem {
  key: string
  label: string
  icon?: string
  path?: string
  children?: MenuItem[]
}

/**
 * 左侧导航菜单结构。
 * icon 字段对应 @ant-design/icons-vue 中的组件名（在 MainLayout 中动态解析）。
 */
export const menuConfig: MenuItem[] = [
  {
    key: 'dashboard',
    label: '系统首页',
    icon: 'DashboardOutlined',
    path: '/dashboard'
  },
  {
    key: 'platform',
    label: '平台管理',
    icon: 'SettingOutlined',
    children: [
      { key: 'platform/users', label: '用户管理', icon: 'TeamOutlined', path: '/platform/users' },
      { key: 'platform/roles', label: '角色权限', icon: 'SafetyOutlined', path: '/platform/roles' },
      { key: 'platform/profile', label: '个人中心', icon: 'UserOutlined', path: '/platform/profile' }
    ]
  },
  {
    key: 'project',
    label: '图谱项目',
    icon: 'FolderOutlined',
    children: [
      { key: 'project/list', label: '项目管理', icon: 'FolderOutlined', path: '/project/list' },
      { key: 'project/schema', label: '图谱模型', icon: 'ApartmentOutlined', path: '/project/schema' }
    ]
  },
  {
    key: 'data',
    label: '数据接入',
    icon: 'DatabaseOutlined',
    children: [
      { key: 'data/source', label: '数据源', icon: 'DatabaseOutlined', path: '/data/source' },
      { key: 'data/corpus', label: '语料管理', icon: 'FileTextOutlined', path: '/data/corpus' }
    ]
  },
  {
    key: 'extraction',
    label: '知识抽取',
    icon: 'RobotOutlined',
    children: [
      { key: 'extraction/structured', label: '结构化映射', icon: 'TableOutlined', path: '/extraction/structured' },
      { key: 'extraction/llm', label: '大模型抽取', icon: 'RobotOutlined', path: '/extraction/llm' },
      { key: 'extraction/dl', label: '深度学习抽取', icon: 'ThunderboltOutlined', path: '/extraction/dl' }
    ]
  },
  {
    key: 'annotation',
    label: '数据标注',
    icon: 'EditOutlined',
    children: [
      { key: 'annotation/task', label: '标注任务', icon: 'EditOutlined', path: '/annotation/task' },
      { key: 'annotation/workspace', label: '标注工作台', icon: 'HighlightOutlined', path: '/annotation/workspace' },
      { key: 'annotation/quality', label: '质量控制', icon: 'AuditOutlined', path: '/annotation/quality' }
    ]
  },
  {
    key: 'model',
    label: '模型训练',
    icon: 'ExperimentOutlined',
    children: [
      { key: 'model/training', label: '训练任务', icon: 'ExperimentOutlined', path: '/model/training' },
      { key: 'model/evaluation', label: '效果评估', icon: 'LineChartOutlined', path: '/model/evaluation' },
      { key: 'model/version', label: '版本管理', icon: 'HistoryOutlined', path: '/model/version' }
    ]
  },
  {
    key: 'graph',
    label: '图谱实例',
    icon: 'NodeIndexOutlined',
    children: [
      { key: 'graph/entity', label: '实体管理', icon: 'NodeIndexOutlined', path: '/graph/entity' },
      { key: 'graph/relation', label: '关系管理', icon: 'ShareAltOutlined', path: '/graph/relation' },
      { key: 'graph/version', label: '图谱版本', icon: 'CameraOutlined', path: '/graph/version' }
    ]
  },
  {
    key: 'explore',
    label: '图谱探索',
    icon: 'DeploymentUnitOutlined',
    children: [
      { key: 'explore/visual', label: '图谱可视化', icon: 'DeploymentUnitOutlined', path: '/explore/visual' },
      { key: 'explore/search', label: '实体检索', icon: 'SearchOutlined', path: '/explore/search' }
    ]
  },
  {
    key: 'analysis',
    label: '智能分析',
    icon: 'BulbOutlined',
    children: [
      { key: 'analysis/qa', label: '智能问答', icon: 'MessageOutlined', path: '/analysis/qa' },
      { key: 'analysis/reasoning', label: '知识推理', icon: 'BulbOutlined', path: '/analysis/reasoning' },
      { key: 'analysis/statistics', label: '统计分析', icon: 'PieChartOutlined', path: '/analysis/statistics' }
    ]
  }
]
