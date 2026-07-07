/**
 * 本地 Mock 数据工具：当后端不可用时，前端直接使用这些数据展示页面内容。
 * 后端启动后会通过 /api/v1/* 返回真实数据。
 */

export function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

export function formatNumber(n: number): string {
  if (n >= 10000) {
    return (n / 10000).toFixed(1) + '万'
  }
  return n.toString()
}

// ===== Dashboard 统计卡片 =====
export const dashboardStats = {
  projectCount: 24,
  entityCount: 1284503,
  relationCount: 3892105,
  taskCount: 156,
  corpusCount: 8543,
  modelCount: 18,
  annotationCount: 45621,
  userCount: 86
}

// ===== 任务状态 =====
export const taskStatusStats = {
  running: 8,
  pending: 14,
  failed: 3,
  completed: 131
}

// 增长趋势（最近7天）
export const growthTrend = {
  dates: ['07-01', '07-02', '07-03', '07-04', '07-05', '07-06', '今天'],
  entity: [8200, 9320, 9010, 12930, 13300, 15200, 16800],
  relation: [18900, 20300, 21400, 28900, 31200, 35800, 42100]
}

// 实体类型分布
export const entityTypeDistribution = [
  { name: '人物', value: 325400 },
  { name: '机构', value: 218900 },
  { name: '地点', value: 187200 },
  { name: '事件', value: 156800 },
  { name: '概念', value: 248300 },
  { name: '其他', value: 147903 }
]

// ===== 最近活动 =====
export const recentActivities = [
  { type: 'extraction', title: '医疗知识图谱抽取任务完成', desc: '处理语料 1,250 篇，抽取实体 8,642 个', time: '5 分钟前', status: 'success' },
  { type: 'annotation', title: '标注任务审核通过', desc: '“金融实体识别” 任务已完成 86% 标注', time: '32 分钟前', status: 'success' },
  { type: 'training', title: 'BERT-NER 模型训练完成', desc: 'F1 值达到 0.892，比上一版本提升 2.3%', time: '1 小时前', status: 'success' },
  { type: 'data', title: '数据源连接失败', desc: 'MySQL 数据源 prod-db 连接超时，请检查', time: '2 小时前', status: 'error' },
  { type: 'project', title: '新项目创建', desc: '“智能制造知识图谱” 项目已创建', time: '3 小时前', status: 'info' },
  { type: 'extraction', title: '批量抽取任务启动', desc: '对 5,000 篇新闻语料启动 GPT-4 抽取', time: '5 小时前', status: 'info' }
]

// ===== 用户列表 =====
export const userList = Array.from({ length: 28 }, (_, i) => ({
  id: `U${String(i + 1).padStart(4, '0')}`,
  username: ['admin', 'zhangsan', 'lisi', 'wangwu', 'zhaoliu', 'qianqi', 'sunba', 'zhoujiu'][i % 8] + (i > 7 ? i : ''),
  nickname: ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十', '郑十一', '王十二'][i % 10],
  email: `user${i + 1}@kg-platform.com`,
  phone: `138${String(randomInt(10000000, 99999999))}`,
  role: ['超级管理员', '项目管理员', '算法工程师', '标注人员', '普通用户'][i % 5],
  status: i % 7 === 0 ? 'disabled' : i % 11 === 0 ? 'pending' : 'active',
  lastLogin: `2026-07-0${(i % 6) + 1} ${String(randomInt(8, 22))}:${String(randomInt(10, 59))}`,
  createTime: `2025-${String(randomInt(1, 12)).padStart(2, '0')}-${String(randomInt(1, 28)).padStart(2, '0')}`
}))

// ===== 角色列表 =====
export const roleList = [
  { id: 'R001', name: '超级管理员', code: 'super_admin', userCount: 2, desc: '系统最高权限，可管理所有用户和资源', permissions: 156, status: 'active' },
  { id: 'R002', name: '项目管理员', code: 'project_admin', userCount: 8, desc: '可管理所属项目的所有资源和人员', permissions: 98, status: 'active' },
  { id: 'R003', name: '算法工程师', code: 'algorithm_engineer', userCount: 15, desc: '负责模型训练、调优和知识抽取任务', permissions: 76, status: 'active' },
  { id: 'R004', name: '标注人员', code: 'annotator', userCount: 42, desc: '仅可进行数据标注相关操作', permissions: 24, status: 'active' },
  { id: 'R005', name: '普通用户', code: 'user', userCount: 19, desc: '可浏览和查询知识图谱，无编辑权限', permissions: 32, status: 'active' }
]

// ===== 项目列表 =====
export const projectList = [
  { id: 'P001', name: '医疗健康知识图谱', domain: '医疗', cover: 'medical', desc: '覆盖疾病、症状、药物、检查项目等医疗实体', entityCount: 326540, relationCount: 892300, memberCount: 12, status: 'active', updateTime: '2026-07-06' },
  { id: 'P002', name: '金融风控知识图谱', domain: '金融', cover: 'finance', desc: '企业、人物、股权、担保、交易关系网络', entityCount: 528900, relationCount: 1620000, memberCount: 18, status: 'active', updateTime: '2026-07-05' },
  { id: 'P003', name: '智能制造知识图谱', domain: '制造', cover: 'industry', desc: '设备、工艺、物料、故障、质量等制造领域知识', entityCount: 184300, relationCount: 425000, memberCount: 8, status: 'active', updateTime: '2026-07-04' },
  { id: 'P004', name: '法律知识图谱', domain: '法律', cover: 'law', desc: '法条、案例、罪名、法律主体等法律知识', entityCount: 95600, relationCount: 218000, memberCount: 6, status: 'active', updateTime: '2026-07-03' },
  { id: 'P005', name: '教育知识图谱', domain: '教育', cover: 'education', desc: '学科、知识点、题目、教学资源关联', entityCount: 67800, relationCount: 154000, memberCount: 5, status: 'archived', updateTime: '2026-06-28' },
  { id: 'P006', name: '电商商品知识图谱', domain: '电商', cover: 'ecommerce', desc: '商品、品牌、品类、属性、用户画像关联', entityCount: 84360, relationCount: 580000, memberCount: 7, status: 'active', updateTime: '2026-07-02' }
]

// ===== Schema 实体类型 =====
export const schemaEntityTypes = [
  { id: 'ET01', name: '人物', color: '#1677ff', icon: 'UserOutlined', properties: ['姓名', '别名', '出生日期', '国籍', '职业', '简介'], instanceCount: 325400 },
  { id: 'ET02', name: '机构', color: '#52c41a', icon: 'HomeOutlined', properties: ['名称', '成立时间', '总部', '行业', '规模', '简介'], instanceCount: 218900 },
  { id: 'ET03', name: '地点', color: '#faad14', icon: 'EnvironmentOutlined', properties: ['名称', '别名', '经纬度', '所属区域', '类型'], instanceCount: 187200 },
  { id: 'ET04', name: '事件', color: '#ff4d4f', icon: 'ThunderboltOutlined', properties: ['名称', '时间', '地点', '参与者', '描述'], instanceCount: 156800 },
  { id: 'ET05', name: '概念', color: '#722ed1', icon: 'BulbOutlined', properties: ['名称', '定义', '所属领域', '上位概念'], instanceCount: 248300 },
  { id: 'ET06', name: '作品', color: '#13c2c2', icon: 'BookOutlined', properties: ['名称', '类型', '作者', '发布时间', '简介'], instanceCount: 62300 }
]

// ===== Schema 关系类型 =====
export const schemaRelationTypes = [
  { id: 'RT01', name: '出生于', source: '人物', target: '地点', desc: '人物的出生地点', instanceCount: 285400 },
  { id: 'RT02', name: '就职于', source: '人物', target: '机构', desc: '人物所在的机构', instanceCount: 412300 },
  { id: 'RT03', name: '位于', source: '机构', target: '地点', desc: '机构所在地理位置', instanceCount: 198600 },
  { id: 'RT04', name: '参与', source: '人物', target: '事件', desc: '人物参与的事件', instanceCount: 156800 },
  { id: 'RT05', name: '创作者', source: '人物', target: '作品', desc: '作品的创作者', instanceCount: 58900 },
  { id: 'RT06', name: '子概念', source: '概念', target: '概念', desc: '概念之间的上下位关系', instanceCount: 124300 },
  { id: 'RT07', name: '发生地', source: '事件', target: '地点', desc: '事件发生的地点', instanceCount: 156800 },
  { id: 'RT08', name: '合作', source: '人物', target: '人物', desc: '人物之间的合作关系', instanceCount: 89500 }
]

// ===== 数据源 =====
export const dataSourceList = [
  { id: 'DS001', name: '生产库 MySQL', type: 'mysql', host: '192.168.1.100:3306', database: 'production', status: 'connected', tableCount: 48, lastSync: '2026-07-06 10:23' },
  { id: 'DS002', name: '业务数据 PostgreSQL', type: 'postgresql', host: '192.168.1.101:5432', database: 'business', status: 'connected', tableCount: 32, lastSync: '2026-07-06 09:15' },
  { id: 'DS003', name: '新闻语料 API', type: 'api', host: 'https://api.news.example.com', database: '-', status: 'error', tableCount: 0, lastSync: '2026-07-05 18:42' },
  { id: 'DS004', name: 'Excel 数据上传', type: 'excel', host: '-', database: 'knowledge_data.xlsx', status: 'connected', tableCount: 5, lastSync: '2026-07-06 11:30' },
  { id: 'DS005', name: '网页爬虫源', type: 'web', host: 'https://wiki.example.com', database: '-', status: 'connected', tableCount: 0, lastSync: '2026-07-06 08:00' },
  { id: 'DS006', name: 'Oracle 历史数据', type: 'oracle', host: '10.0.1.50:1521', database: 'history', status: 'disconnected', tableCount: 26, lastSync: '2026-06-30 14:20' }
]

// ===== 语料列表 =====
export const corpusList = Array.from({ length: 32 }, (_, i) => ({
  id: `C${String(i + 1).padStart(4, '0')}`,
  name: ['医疗文献-糖尿病研究.txt', '财经新闻-上市公司分析.docx', '法律案例-合同纠纷.pdf', '科技论文-NLP技术综述.txt', '百科词条-历史人物.html', '新闻报道-产业动态.txt'][i % 6],
  size: randomInt(50, 5000) + ' KB',
  words: randomInt(1000, 80000),
  language: i % 4 === 0 ? '英文' : '中文',
  source: ['手动上传', 'API导入', '爬虫采集', '数据库导入'][i % 4],
  tags: [['医疗'], ['金融', '上市公司'], ['法律'], ['科技', 'NLP'], ['百科']][i % 5],
  uploadTime: `2026-07-${String((i % 6) + 1).padStart(2, '0')}`,
  status: i % 5 === 0 ? 'processing' : 'ready'
}))

// ===== 抽取任务 =====
export const extractionTasks = Array.from({ length: 15 }, (_, i) => ({
  id: `EXT${String(i + 1).padStart(4, '0')}`,
  name: ['医疗文献实体抽取', '金融新闻关系抽取', '法律案件事件抽取', '百科词条属性抽取', '科技论文联合抽取'][i % 5],
  type: ['llm', 'dl', 'structured'][i % 3],
  model: ['GPT-4', 'Claude-3.5', 'BERT-NER-v2', 'TPLinker-v1', '结构化映射'][i % 5],
  corpusCount: randomInt(50, 5000),
  processed: randomInt(0, 5000),
  status: ['running', 'success', 'success', 'failed', 'pending'][i % 5],
  startTime: `2026-07-0${(i % 6) + 1} ${String(randomInt(8, 22))}:${String(randomInt(10, 59))}`,
  duration: `${randomInt(1, 60)}m ${randomInt(0, 59)}s`,
  entityCount: randomInt(100, 10000),
  relationCount: randomInt(50, 5000)
}))

// ===== 标注任务 =====
export const annotationTasks = Array.from({ length: 12 }, (_, i) => ({
  id: `ANN${String(i + 1).padStart(4, '0')}`,
  name: ['医疗实体标注', '金融关系标注', '法律事件标注', '科技属性标注', '百科分类标注'][i % 5],
  type: ['entity', 'relation', 'event', 'attribute', 'classification'][i % 5],
  total: randomInt(500, 5000),
  annotated: randomInt(0, 5000),
  assignees: randomInt(2, 8),
  progress: randomInt(0, 100),
  status: ['in_progress', 'completed', 'in_progress', 'paused', 'pending'][i % 5],
  deadline: `2026-07-${String((i % 10) + 10).padStart(2, '0')}`,
  quality: (0.85 + Math.random() * 0.13).toFixed(3)
}))

// ===== 模型列表 =====
export const modelList = [
  { id: 'M001', name: 'BERT-NER-Medical', type: 'NER', version: 'v2.3.1', framework: 'PyTorch', status: 'online', f1: 0.892, precision: 0.901, recall: 0.884, trainTime: '2026-07-04', size: '420MB' },
  { id: 'M002', name: 'RoBERTa-RE-Finance', type: 'RE', version: 'v1.8.0', framework: 'PyTorch', status: 'online', f1: 0.856, precision: 0.871, recall: 0.842, trainTime: '2026-07-02', size: '480MB' },
  { id: 'M003', name: 'TPLinker-Joint', type: 'Joint', version: 'v1.2.0', framework: 'PyTorch', status: 'offline', f1: 0.834, precision: 0.845, recall: 0.824, trainTime: '2026-06-28', size: '510MB' },
  { id: 'M004', name: 'BiLSTM-CRF-NER', type: 'NER', version: 'v1.5.2', framework: 'TensorFlow', status: 'online', f1: 0.812, precision: 0.823, recall: 0.802, trainTime: '2026-06-25', size: '180MB' },
  { id: 'M005', name: 'GCN-RE-Law', type: 'RE', version: 'v2.0.1', framework: 'PyTorch', status: 'training', f1: 0.0, precision: 0.0, recall: 0.0, trainTime: '2026-07-06', size: '-' }
]

// ===== 训练任务 =====
export const trainingTasks = Array.from({ length: 8 }, (_, i) => ({
  id: `TR${String(i + 1).padStart(4, '0')}`,
  name: ['BERT-NER-Medical-v2.4', 'RoBERTa-RE-Finance-v1.9', 'TPLinker-Joint-v1.3', 'GCN-RE-Law-v2.1', 'BERT-Attr-Edu'][i % 5],
  model: ['BERT-base', 'RoBERTa-large', 'TPLinker', 'GCN', 'BERT-base'][i % 5],
  status: ['running', 'success', 'success', 'failed', 'running'][i % 5],
  epoch: `${randomInt(5, 30)}/${randomInt(20, 50)}`,
  loss: (0.1 + Math.random() * 1.5).toFixed(4),
  accuracy: (0.7 + Math.random() * 0.25).toFixed(4),
  gpu: 'RTX 4090',
  startTime: `2026-07-0${(i % 6) + 1} ${String(randomInt(8, 22))}:${String(randomInt(10, 59))}`,
  duration: `${randomInt(1, 12)}h ${randomInt(0, 59)}m`
}))

// ===== 图谱实体 =====
export const entityList = Array.from({ length: 50 }, (_, i) => ({
  id: `E${String(i + 1).padStart(6, '0')}`,
  name: ['张三', '李四', '阿里巴巴', '腾讯', '北京', '上海', '2024奥运会', 'ChatGPT', '人工智能', '机器学习'][i % 10] + (i > 9 ? `-${i}` : ''),
  type: ['人物', '机构', '地点', '事件', '概念', '作品'][i % 6],
  properties: randomInt(3, 12),
  relations: randomInt(0, 50),
  source: ['LLM抽取', '结构化导入', 'DL抽取', '手动添加'][i % 4],
  confidence: (0.6 + Math.random() * 0.4).toFixed(3),
  updateTime: `2026-07-0${(i % 6) + 1}`
}))

// ===== 关系列表 =====
export const relationList = Array.from({ length: 50 }, (_, i) => ({
  id: `R${String(i + 1).padStart(6, '0')}`,
  source: ['张三', '李四', '阿里巴巴', '腾讯', '北京', '上海'][i % 6],
  relation: ['出生于', '就职于', '位于', '参与', '合作', '投资'][i % 6],
  target: ['北京', '上海', '阿里巴巴', '腾讯', '张三', '李四'][(i + 2) % 6],
  properties: randomInt(0, 5),
  source2: ['LLM抽取', '结构化导入', 'DL抽取', '手动添加'][i % 4],
  confidence: (0.6 + Math.random() * 0.4).toFixed(3),
  updateTime: `2026-07-0${(i % 6) + 1}`
}))

// ===== 图谱版本 =====
export const graphVersions = Array.from({ length: 8 }, (_, i) => ({
  id: `V${String(i + 1).padStart(3, '0')}`,
  version: `v1.${7 - i}.0`,
  desc: ['新增医疗实体 8,642 个', '修复关系抽取错误', '合并重复实体 320 个', '增量更新金融数据', '初始化版本', 'AI 辅助补全属性'][i % 6],
  entityCount: 1284503 - i * 15200,
  relationCount: 3892105 - i * 42800,
  creator: ['admin', 'zhangsan', 'lisi'][i % 3],
  createTime: `2026-07-0${(i % 6) + 1}`,
  tag: i === 0 ? 'current' : i === 1 ? 'stable' : '-'
}))

// ===== 智能问答示例 =====
export const qaExamples = [
  '阿里巴巴的创始人是谁？',
  '张三和李四是什么关系？',
  '北京有哪些知名互联网公司？',
  '人工智能领域的重要事件有哪些？',
  'ChatGPT 的开发者是谁？'
]

export const qaHistory = [
  { role: 'user', content: '阿里巴巴的创始人是谁？' },
  { role: 'assistant', content: '阿里巴巴的主要创始人是马云。1999 年，马云带领 17 位创始人在杭州创立了阿里巴巴集团。除马云外，联合创始人还包括蔡崇信等人。', sources: ['E000123 张三→马云', 'R000456 创始人→阿里巴巴'] },
  { role: 'user', content: '他出生于哪里？' },
  { role: 'assistant', content: '马云出生于浙江省杭州市。', sources: ['E000123 马云', 'R000789 出生于→杭州'] }
]

// ===== 统计分析 =====
export const statisticsData = {
  entityTypeDist: entityTypeDistribution,
  relationTypeDist: [
    { name: '出生于', value: 285400 },
    { name: '就职于', value: 412300 },
    { name: '位于', value: 198600 },
    { name: '参与', value: 156800 },
    { name: '创作者', value: 58900 },
    { name: '子概念', value: 124300 },
    { name: '其他', value: 585805 }
  ],
  sourceDist: [
    { name: 'LLM抽取', value: 45 },
    { name: '结构化导入', value: 28 },
    { name: 'DL抽取', value: 18 },
    { name: '手动添加', value: 9 }
  ],
  graphMetrics: {
    density: 0.0024,
    avgDegree: 6.06,
    diameter: 8,
    avgPathLength: 3.42,
    clusteringCoeff: 0.218
  }
}
