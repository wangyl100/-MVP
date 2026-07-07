package com.kgplatform.mock;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Mock 数据提供器：当中间件不可用时，所有 API 降级返回这些数据。
 * <p>
 * 数据内容与前端 utils/mock.ts 保持一致，确保页面可见性。
 */
@Component
public class MockDataProvider {

    // ===== Dashboard =====
    public Map<String, Object> dashboardStats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("projectCount", 24);
        m.put("entityCount", 1284503);
        m.put("relationCount", 3892105);
        m.put("taskCount", 156);
        m.put("corpusCount", 8543);
        m.put("modelCount", 18);
        m.put("annotationCount", 45621);
        m.put("userCount", 86);
        return m;
    }

    public Map<String, Object> taskStatus() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("running", 8);
        m.put("pending", 14);
        m.put("failed", 3);
        m.put("completed", 131);
        return m;
    }

    public Map<String, Object> growthTrend() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dates", Arrays.asList("07-01", "07-02", "07-03", "07-04", "07-05", "07-06", "今天"));
        m.put("entity", Arrays.asList(8200, 9320, 9010, 12930, 13300, 15200, 16800));
        m.put("relation", Arrays.asList(18900, 20300, 21400, 28900, 31200, 35800, 42100));
        return m;
    }

    public List<Map<String, Object>> entityTypeDist() {
        return Arrays.asList(
            entityMap("人物", 325400), entityMap("机构", 218900), entityMap("地点", 187200),
            entityMap("事件", 156800), entityMap("概念", 248300), entityMap("其他", 147903)
        );
    }

    private Map<String, Object> entityMap(String name, int value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("value", value);
        return m;
    }

    public List<Map<String, Object>> recentActivities() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(activity("extraction", "医疗知识图谱抽取任务完成", "处理语料 1,250 篇，抽取实体 8,642 个", "5 分钟前", "success"));
        list.add(activity("annotation", "标注任务审核通过", "\"金融实体识别\" 任务已完成 86% 标注", "32 分钟前", "success"));
        list.add(activity("training", "BERT-NER 模型训练完成", "F1 值达到 0.892，比上一版本提升 2.3%", "1 小时前", "success"));
        list.add(activity("data", "数据源连接失败", "MySQL 数据源 prod-db 连接超时，请检查", "2 小时前", "error"));
        list.add(activity("project", "新项目创建", "\"智能制造知识图谱\" 项目已创建", "3 小时前", "info"));
        list.add(activity("extraction", "批量抽取任务启动", "对 5,000 篇新闻语料启动 GPT-4 抽取", "5 小时前", "info"));
        return list;
    }

    private Map<String, Object> activity(String type, String title, String desc, String time, String status) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type); m.put("title", title); m.put("desc", desc);
        m.put("time", time); m.put("status", status);
        return m;
    }

    // ===== 用户列表 =====
    public List<Map<String, Object>> userList() {
        String[] names = {"admin", "zhangsan", "lisi", "wangwu", "zhaoliu", "qianqi", "sunba", "zhoujiu"};
        String[] nicks = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十", "郑十一", "王十二"};
        String[] roles = {"超级管理员", "项目管理员", "算法工程师", "标注人员", "普通用户"};
        List<Map<String, Object>> list = new ArrayList<>();
        Random r = new Random(42);
        for (int i = 0; i < 28; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "U" + String.format("%04d", i + 1));
            m.put("username", names[i % 8] + (i > 7 ? i : ""));
            m.put("nickname", nicks[i % 10]);
            m.put("email", "user" + (i + 1) + "@kg-platform.com");
            m.put("phone", "138" + (10_000_000 + r.nextInt(89_999_999)));
            m.put("role", roles[i % 5]);
            m.put("status", i % 7 == 0 ? "disabled" : i % 11 == 0 ? "pending" : "active");
            m.put("lastLogin", "2026-07-0" + (i % 6 + 1) + " " + (8 + r.nextInt(14)) + ":" + String.format("%02d", 10 + r.nextInt(50)));
            m.put("createTime", "2025-" + String.format("%02d", 1 + r.nextInt(12)) + "-" + String.format("%02d", 1 + r.nextInt(28)));
            list.add(m);
        }
        return list;
    }

    public List<Map<String, Object>> roleList() {
        String[][] data = {
            {"R001", "超级管理员", "super_admin", "2", "系统最高权限，可管理所有用户和资源", "156", "active"},
            {"R002", "项目管理员", "project_admin", "8", "可管理所属项目的所有资源和人员", "98", "active"},
            {"R003", "算法工程师", "algorithm_engineer", "15", "负责模型训练、调优和知识抽取任务", "76", "active"},
            {"R004", "标注人员", "annotator", "42", "仅可进行数据标注相关操作", "24", "active"},
            {"R005", "普通用户", "user", "19", "可浏览和查询知识图谱，无编辑权限", "32", "active"}
        };
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] d : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d[0]); m.put("name", d[1]); m.put("code", d[2]);
            m.put("userCount", Integer.parseInt(d[3])); m.put("desc", d[4]);
            m.put("permissions", Integer.parseInt(d[5])); m.put("status", d[6]);
            list.add(m);
        }
        return list;
    }

    // ===== 项目列表 =====
    public List<Map<String, Object>> projectList() {
        String[][] data = {
            {"P001", "医疗健康知识图谱", "医疗", "medical", "覆盖疾病、症状、药物、检查项目等医疗实体", "326540", "892300", "12", "active", "2026-07-06"},
            {"P002", "金融风控知识图谱", "金融", "finance", "企业、人物、股权、担保、交易关系网络", "528900", "1620000", "18", "active", "2026-07-05"},
            {"P003", "智能制造知识图谱", "制造", "industry", "设备、工艺、物料、故障、质量等制造领域知识", "184300", "425000", "8", "active", "2026-07-04"},
            {"P004", "法律知识图谱", "法律", "law", "法条、案例、罪名、法律主体等法律知识", "95600", "218000", "6", "active", "2026-07-03"},
            {"P005", "教育知识图谱", "教育", "education", "学科、知识点、题目、教学资源关联", "67800", "154000", "5", "archived", "2026-06-28"},
            {"P006", "电商商品知识图谱", "电商", "ecommerce", "商品、品牌、品类、属性、用户画像关联", "84360", "580000", "7", "active", "2026-07-02"}
        };
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] d : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d[0]); m.put("name", d[1]); m.put("domain", d[2]);
            m.put("cover", d[3]); m.put("desc", d[4]);
            m.put("entityCount", Long.parseLong(d[5])); m.put("relationCount", Long.parseLong(d[6]));
            m.put("memberCount", Integer.parseInt(d[7])); m.put("status", d[8]); m.put("updateTime", d[9]);
            list.add(m);
        }
        return list;
    }

    // ===== Schema 实体类型 =====
    public List<Map<String, Object>> schemaEntityTypes() {
        String[][] data = {
            {"ET01", "人物", "#1677ff", "UserOutlined", "325400", "姓名|别名|出生日期|国籍|职业|简介"},
            {"ET02", "机构", "#52c41a", "HomeOutlined", "218900", "名称|成立时间|总部|行业|规模|简介"},
            {"ET03", "地点", "#faad14", "EnvironmentOutlined", "187200", "名称|别名|经纬度|所属区域|类型"},
            {"ET04", "事件", "#ff4d4f", "ThunderboltOutlined", "156800", "名称|时间|地点|参与者|描述"},
            {"ET05", "概念", "#722ed1", "BulbOutlined", "248300", "名称|定义|所属领域|上位概念"},
            {"ET06", "作品", "#13c2c2", "BookOutlined", "62300", "名称|类型|作者|发布时间|简介"}
        };
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] d : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d[0]); m.put("name", d[1]); m.put("color", d[2]);
            m.put("icon", d[3]); m.put("instanceCount", Long.parseLong(d[4]));
            m.put("properties", Arrays.asList(d[5].split("\\|")));
            list.add(m);
        }
        return list;
    }

    public List<Map<String, Object>> schemaRelationTypes() {
        String[][] data = {
            {"RT01", "出生于", "人物", "地点", "人物的出生地点", "285400"},
            {"RT02", "就职于", "人物", "机构", "人物所在的机构", "412300"},
            {"RT03", "位于", "机构", "地点", "机构所在地理位置", "198600"},
            {"RT04", "参与", "人物", "事件", "人物参与的事件", "156800"},
            {"RT05", "创作者", "人物", "作品", "作品的创作者", "58900"},
            {"RT06", "子概念", "概念", "概念", "概念之间的上下位关系", "124300"},
            {"RT07", "发生地", "事件", "地点", "事件发生的地点", "156800"},
            {"RT08", "合作", "人物", "人物", "人物之间的合作关系", "89500"}
        };
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] d : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d[0]); m.put("name", d[1]); m.put("source", d[2]);
            m.put("target", d[3]); m.put("desc", d[4]); m.put("instanceCount", Long.parseLong(d[5]));
            list.add(m);
        }
        return list;
    }

    // ===== 数据源 =====
    public List<Map<String, Object>> dataSourceList() {
        String[][] data = {
            {"DS001", "生产库 MySQL", "mysql", "192.168.1.100:3306", "production", "connected", "48", "2026-07-06 10:23"},
            {"DS002", "业务数据 PostgreSQL", "postgresql", "192.168.1.101:5432", "business", "connected", "32", "2026-07-06 09:15"},
            {"DS003", "新闻语料 API", "api", "https://api.news.example.com", "-", "error", "0", "2026-07-05 18:42"},
            {"DS004", "Excel 数据上传", "excel", "-", "knowledge_data.xlsx", "connected", "5", "2026-07-06 11:30"},
            {"DS005", "网页爬虫源", "web", "https://wiki.example.com", "-", "connected", "0", "2026-07-06 08:00"},
            {"DS006", "Oracle 历史数据", "oracle", "10.0.1.50:1521", "history", "disconnected", "26", "2026-06-30 14:20"}
        };
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] d : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d[0]); m.put("name", d[1]); m.put("type", d[2]);
            m.put("host", d[3]); m.put("database", d[4]); m.put("status", d[5]);
            m.put("tableCount", Integer.parseInt(d[6])); m.put("lastSync", d[7]);
            list.add(m);
        }
        return list;
    }

    // ===== 语料列表 =====
    public List<Map<String, Object>> corpusList() {
        String[] names = {"医疗文献-糖尿病研究.txt", "财经新闻-上市公司分析.docx", "法律案例-合同纠纷.pdf",
            "科技论文-NLP技术综述.txt", "百科词条-历史人物.html", "新闻报道-产业动态.txt"};
        String[] langs = {"英文", "中文", "中文", "中文", "中文", "中文"};
        String[] sources = {"手动上传", "API导入", "爬虫采集", "数据库导入"};
        String[][] tags = {{"医疗"}, {"金融", "上市公司"}, {"法律"}, {"科技", "NLP"}, {"百科"}};
        List<Map<String, Object>> list = new ArrayList<>();
        Random r = new Random(7);
        for (int i = 0; i < 32; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "C" + String.format("%04d", i + 1));
            m.put("name", names[i % 6]);
            m.put("size", (50 + r.nextInt(4950)) + " KB");
            m.put("words", 1000 + r.nextInt(79000));
            m.put("language", i % 4 == 0 ? "英文" : "中文");
            m.put("source", sources[i % 4]);
            m.put("tags", Arrays.asList(tags[i % 5]));
            m.put("uploadTime", "2026-07-" + String.format("%02d", (i % 6) + 1));
            m.put("status", i % 5 == 0 ? "processing" : "ready");
            list.add(m);
        }
        return list;
    }

    // ===== 抽取任务 =====
    public List<Map<String, Object>> extractionTasks() {
        String[] names = {"医疗文献实体抽取", "金融新闻关系抽取", "法律案件事件抽取", "百科词条属性抽取", "科技论文联合抽取"};
        String[] types = {"llm", "dl", "structured"};
        String[] models = {"GPT-4", "Claude-3.5", "BERT-NER-v2", "TPLinker-v1", "结构化映射"};
        String[] statuses = {"running", "success", "success", "failed", "pending"};
        List<Map<String, Object>> list = new ArrayList<>();
        Random r = new Random(11);
        for (int i = 0; i < 15; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "EXT" + String.format("%04d", i + 1));
            m.put("name", names[i % 5]);
            m.put("type", types[i % 3]);
            m.put("model", models[i % 5]);
            m.put("corpusCount", 50 + r.nextInt(4950));
            m.put("processed", r.nextInt(5000));
            m.put("status", statuses[i % 5]);
            m.put("startTime", "2026-07-0" + (i % 6 + 1) + " " + (8 + r.nextInt(14)) + ":" + String.format("%02d", 10 + r.nextInt(50)));
            m.put("duration", (1 + r.nextInt(60)) + "m " + r.nextInt(60) + "s");
            m.put("entityCount", 100 + r.nextInt(9900));
            m.put("relationCount", 50 + r.nextInt(4950));
            list.add(m);
        }
        return list;
    }

    // ===== 标注任务 =====
    public List<Map<String, Object>> annotationTasks() {
        String[] names = {"医疗实体标注", "金融关系标注", "法律事件标注", "科技属性标注", "百科分类标注"};
        String[] types = {"entity", "relation", "event", "attribute", "classification"};
        String[] statuses = {"in_progress", "completed", "in_progress", "paused", "pending"};
        List<Map<String, Object>> list = new ArrayList<>();
        Random r = new Random(23);
        for (int i = 0; i < 12; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "ANN" + String.format("%04d", i + 1));
            m.put("name", names[i % 5]);
            m.put("type", types[i % 5]);
            m.put("total", 500 + r.nextInt(4500));
            m.put("annotated", r.nextInt(5000));
            m.put("assignees", 2 + r.nextInt(6));
            m.put("progress", r.nextInt(100));
            m.put("status", statuses[i % 5]);
            m.put("deadline", "2026-07-" + String.format("%02d", (i % 10) + 10));
            m.put("quality", String.format("%.3f", 0.85 + r.nextDouble() * 0.13));
            list.add(m);
        }
        return list;
    }

    // ===== 模型列表 =====
    public List<Map<String, Object>> modelList() {
        Object[][] data = {
            {"M001", "BERT-NER-Medical", "NER", "v2.3.1", "PyTorch", "online", 0.892, 0.901, 0.884, "2026-07-04", "420MB"},
            {"M002", "RoBERTa-RE-Finance", "RE", "v1.8.0", "PyTorch", "online", 0.856, 0.871, 0.842, "2026-07-02", "480MB"},
            {"M003", "TPLinker-Joint", "Joint", "v1.2.0", "PyTorch", "offline", 0.834, 0.845, 0.824, "2026-06-28", "510MB"},
            {"M004", "BiLSTM-CRF-NER", "NER", "v1.5.2", "TensorFlow", "online", 0.812, 0.823, 0.802, "2026-06-25", "180MB"},
            {"M005", "GCN-RE-Law", "RE", "v2.0.1", "PyTorch", "training", 0.0, 0.0, 0.0, "2026-07-06", "-"}
        };
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] d : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d[0]); m.put("name", d[1]); m.put("type", d[2]);
            m.put("version", d[3]); m.put("framework", d[4]); m.put("status", d[5]);
            m.put("f1", d[6]); m.put("precision", d[7]); m.put("recall", d[8]);
            m.put("trainTime", d[9]); m.put("size", d[10]);
            list.add(m);
        }
        return list;
    }

    // ===== 训练任务 =====
    public List<Map<String, Object>> trainingTasks() {
        String[] names = {"BERT-NER-Medical-v2.4", "RoBERTa-RE-Finance-v1.9", "TPLinker-Joint-v1.3",
            "GCN-RE-Law-v2.1", "BERT-Attr-Edu"};
        String[] models = {"BERT-base", "RoBERTa-large", "TPLinker", "GCN", "BERT-base"};
        String[] statuses = {"running", "success", "success", "failed", "running"};
        List<Map<String, Object>> list = new ArrayList<>();
        Random r = new Random(31);
        for (int i = 0; i < 8; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "TR" + String.format("%04d", i + 1));
            m.put("name", names[i % 5]);
            m.put("model", models[i % 5]);
            m.put("status", statuses[i % 5]);
            m.put("epoch", (5 + r.nextInt(25)) + "/" + (20 + r.nextInt(30)));
            m.put("loss", String.format("%.4f", 0.1 + r.nextDouble() * 1.5));
            m.put("accuracy", String.format("%.4f", 0.7 + r.nextDouble() * 0.25));
            m.put("gpu", "RTX 4090");
            m.put("startTime", "2026-07-0" + (i % 6 + 1) + " " + (8 + r.nextInt(14)) + ":" + String.format("%02d", 10 + r.nextInt(50)));
            m.put("duration", (1 + r.nextInt(12)) + "h " + r.nextInt(60) + "m");
            list.add(m);
        }
        return list;
    }

    // ===== 实体列表 =====
    public List<Map<String, Object>> entityList() {
        String[] names = {"张三", "李四", "阿里巴巴", "腾讯", "北京", "上海", "2024奥运会", "ChatGPT", "人工智能", "机器学习"};
        String[] types = {"人物", "机构", "地点", "事件", "概念", "作品"};
        String[] sources = {"LLM抽取", "结构化导入", "DL抽取", "手动添加"};
        List<Map<String, Object>> list = new ArrayList<>();
        Random r = new Random(53);
        for (int i = 0; i < 50; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "E" + String.format("%06d", i + 1));
            m.put("name", names[i % 10] + (i > 9 ? "-" + i : ""));
            m.put("type", types[i % 6]);
            m.put("properties", 3 + r.nextInt(10));
            m.put("relations", r.nextInt(50));
            m.put("source", sources[i % 4]);
            m.put("confidence", String.format("%.3f", 0.6 + r.nextDouble() * 0.4));
            m.put("updateTime", "2026-07-0" + (i % 6 + 1));
            list.add(m);
        }
        return list;
    }

    // ===== 关系列表 =====
    public List<Map<String, Object>> relationList() {
        String[] sources = {"张三", "李四", "阿里巴巴", "腾讯", "北京", "上海"};
        String[] relations = {"出生于", "就职于", "位于", "参与", "合作", "投资"};
        String[] targets = {"北京", "上海", "阿里巴巴", "腾讯", "张三", "李四"};
        String[] origin = {"LLM抽取", "结构化导入", "DL抽取", "手动添加"};
        List<Map<String, Object>> list = new ArrayList<>();
        Random r = new Random(67);
        for (int i = 0; i < 50; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "R" + String.format("%06d", i + 1));
            m.put("source", sources[i % 6]);
            m.put("relation", relations[i % 6]);
            m.put("target", targets[(i + 2) % 6]);
            m.put("properties", r.nextInt(5));
            m.put("source2", origin[i % 4]);
            m.put("confidence", String.format("%.3f", 0.6 + r.nextDouble() * 0.4));
            m.put("updateTime", "2026-07-0" + (i % 6 + 1));
            list.add(m);
        }
        return list;
    }

    // ===== 图谱版本 =====
    public List<Map<String, Object>> graphVersions() {
        String[] descs = {"新增医疗实体 8,642 个", "修复关系抽取错误", "合并重复实体 320 个", "增量更新金融数据", "初始化版本", "AI 辅助补全属性"};
        String[] creators = {"admin", "zhangsan", "lisi"};
        String[] tags = {"current", "stable", "-", "-", "-", "-", "-", "-"};
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "V" + String.format("%03d", i + 1));
            m.put("version", "v1." + (7 - i) + ".0");
            m.put("desc", descs[i % 6]);
            m.put("entityCount", 1284503 - i * 15200);
            m.put("relationCount", 3892105 - i * 42800);
            m.put("creator", creators[i % 3]);
            m.put("createTime", "2026-07-0" + (i % 6 + 1));
            m.put("tag", tags[i]);
            list.add(m);
        }
        return list;
    }

    // ===== 智能问答示例 =====
    public List<String> qaExamples() {
        return Arrays.asList(
            "阿里巴巴的创始人是谁？",
            "张三和李四是什么关系？",
            "北京有哪些知名互联网公司？",
            "人工智能领域的重要事件有哪些？",
            "ChatGPT 的开发者是谁？"
        );
    }

    public List<Map<String, Object>> qaHistory() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> m1 = new LinkedHashMap<>();
        m1.put("role", "user"); m1.put("content", "阿里巴巴的创始人是谁？");
        list.add(m1);
        Map<String, Object> m2 = new LinkedHashMap<>();
        m2.put("role", "assistant");
        m2.put("content", "阿里巴巴的主要创始人是马云。1999 年，马云带领 17 位创始人在杭州创立了阿里巴巴集团。除马云外，联合创始人还包括蔡崇信等人。");
        m2.put("sources", Arrays.asList("E000123 张三→马云", "R000456 创始人→阿里巴巴"));
        list.add(m2);
        return list;
    }

    // ===== 统计分析 =====
    public Map<String, Object> statistics() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("entityTypeDist", entityTypeDist());
        m.put("relationTypeDist", Arrays.asList(
            entityMap("出生于", 285400), entityMap("就职于", 412300), entityMap("位于", 198600),
            entityMap("参与", 156800), entityMap("创作者", 58900), entityMap("子概念", 124300),
            entityMap("其他", 585805)
        ));
        m.put("sourceDist", Arrays.asList(
            entityMap("LLM抽取", 45), entityMap("结构化导入", 28),
            entityMap("DL抽取", 18), entityMap("手动添加", 9)
        ));
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("density", 0.0024);
        metrics.put("avgDegree", 6.06);
        metrics.put("diameter", 8);
        metrics.put("avgPathLength", 3.42);
        metrics.put("clusteringCoeff", 0.218);
        m.put("graphMetrics", metrics);
        return m;
    }

    // ===== 中间件状态 =====
    public Map<String, Object> middlewareStatus(boolean neo4j, boolean kafka, boolean pg, boolean xxl, boolean faiss) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("neo4j", neo4j ? "connected" : "degraded");
        m.put("kafka", kafka ? "connected" : "degraded");
        m.put("postgresql", pg ? "connected" : "degraded");
        m.put("xxlJob", xxl ? "connected" : "degraded");
        m.put("faiss", faiss ? "connected" : "degraded");
        m.put("h2", "connected"); // H2 始终可用
        return m;
    }
}
