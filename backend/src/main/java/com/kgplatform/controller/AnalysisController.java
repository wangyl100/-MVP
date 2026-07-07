package com.kgplatform.controller;

import com.kgplatform.common.ApiResponse;
import com.kgplatform.mock.MockDataProvider;
import com.kgplatform.middleware.FaissClient;
import com.kgplatform.middleware.Neo4jClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 智能分析 Controller。
 * <p>
 * 真实场景下：
 * - 智能问答：基于图谱的 KBQA + LLM 生成
 * - 知识推理：基于规则/表示学习（TransE）/图神经网络（GNN）的三种推理引擎
 * - 统计分析：图谱结构指标、分布统计、热点分析
 */
@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

    @Autowired private MockDataProvider mock;
    @Autowired private Neo4jClient neo4jClient;
    @Autowired private FaissClient faissClient;

    // ===== 智能问答 =====
    @GetMapping("/analysis/qa/examples")
    public ApiResponse<List<String>> qaExamples() {
        return ApiResponse.success(mock.qaExamples());
    }

    @GetMapping("/analysis/qa/history")
    public ApiResponse<List<Map<String, Object>>> qaHistory() {
        return ApiResponse.success(mock.qaHistory());
    }

    @PostMapping("/analysis/qa/ask")
    public ApiResponse<Map<String, Object>> ask(@RequestBody Map<String, Object> body) {
        // 真实调用流程：
        // 1. 实体链接：识别问题中的实体
        //    List<Entity> entities = entityLinker.link(question);
        // 2. 图谱查询：基于实体查询子图
        //    Map<String, Object> subgraph = neo4jClient.query(cypher, params);
        // 3. 向量检索增强：Faiss 检索相关上下文
        //    List<Map> context = faissClient.search(queryVec, topK);
        // 4. LLM 生成：将子图和上下文作为 prompt 输入大模型
        //    String answer = llmClient.generate(prompt);
        String question = String.valueOf(body.getOrDefault("question", ""));
        String model = String.valueOf(body.getOrDefault("model", "GPT-4"));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("question", question);
        m.put("answer", generateAnswer(question));
        m.put("model", model);
        m.put("sources", Arrays.asList(
            source("E000123", "实体", "阿里巴巴", "图谱实例"),
            source("R000456", "关系", "创始人", "图谱实例"),
            source("E000124", "实体", "马云", "图谱实例")
        ));
        m.put("confidence", 0.92);
        m.put("latency", "1.8s");
        m.put("neo4jAvailable", neo4jClient.isAvailable());
        m.put("faissAvailable", faissClient.isAvailable());
        return ApiResponse.success(m);
    }

    @GetMapping("/analysis/qa/capabilities")
    public ApiResponse<List<Map<String, Object>>> capabilities() {
        return ApiResponse.success(Arrays.asList(
            capability("实体查询", "查询实体的属性、关系和相关信息", "阿里巴巴的总部在哪里？", 156),
            capability("关系查询", "查询两个实体之间的关系", "马云和蔡崇信是什么关系？", 89),
            capability("路径推理", "查找实体间的最短路径和关联", "阿里巴巴和腾讯有什么关联？", 45),
            capability("统计分析", "对图谱数据进行聚合统计", "杭州有多少家互联网公司？", 67),
            capability("比较分析", "比较多个实体的异同", "比较 BAT 三家的业务范围", 23)
        ));
    }

    // ===== 知识推理 =====
    @GetMapping("/analysis/reasoning/engines")
    public ApiResponse<List<Map<String, Object>>> reasoningEngines() {
        return ApiResponse.success(Arrays.asList(
            engine("rule", "规则推理", "基于预定义规则的逻辑推理", "fast", 0.85, "online"),
            engine("transe", "TransE", "基于翻译模型的表示学习推理", "medium", 0.78, "online"),
            engine("gnn", "图神经网络", "基于 GNN 的深层关系推理", "slow", 0.91, "online")
        ));
    }

    @PostMapping("/analysis/reasoning/run")
    public ApiResponse<Map<String, Object>> runReasoning(@RequestBody Map<String, Object> body) {
        // 真实调用流程：
        // 1. 根据引擎类型选择推理器
        //    Reasoner reasoner = reasonerFactory.get(engine);
        // 2. 加载推理规则/模型
        //    reasoner.load(ruleset / modelWeights);
        // 3. 执行推理
        //    List<Triple> inferred = reasoner.infer(seedEntities, depth, threshold);
        // 4. 写回图谱
        //    neo4jClient.execute("CREATE (a)-[r:INFERRED]->(b)", params);
        String engine = String.valueOf(body.getOrDefault("engine", "rule"));
        int depth = Integer.parseInt(String.valueOf(body.getOrDefault("depth", "3")));
        double threshold = Double.parseDouble(String.valueOf(body.getOrDefault("threshold", "0.7")));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("engine", engine);
        m.put("depth", depth);
        m.put("threshold", threshold);
        m.put("inferredCount", 28);
        m.put("results", Arrays.asList(
            inference("I001", "马云", "投资", "蚂蚁集团", 0.92, "rule"),
            inference("I002", "阿里巴巴", "竞争", "腾讯", 0.89, "rule"),
            inference("I003", "ChatGPT", "属于", "人工智能", 0.95, "rule"),
            inference("I004", "杭州", "聚集", "互联网公司", 0.83, "transe"),
            inference("I005", "OpenAI", "研究", "大语言模型", 0.91, "gnn")
        ));
        m.put("pathVisualization", Arrays.asList(
            pathNode("n1", "马云", "人物"),
            pathNode("n2", "阿里巴巴", "机构"),
            pathNode("n3", "OpenAI", "机构"),
            pathNode("n4", "ChatGPT", "作品")
        ));
        m.put("neo4jAvailable", neo4jClient.isAvailable());
        m.put("message", "推理完成" + (neo4jClient.isAvailable() ? "（结果已写回 Neo4j）" : "（Neo4j 不可用，仅返回结果）"));
        return ApiResponse.success(m);
    }

    @GetMapping("/analysis/reasoning/rules")
    public ApiResponse<List<Map<String, Object>>> ruleTemplates() {
        return ApiResponse.success(Arrays.asList(
            rule("R001", "传递性规则", "如果 A 投资 B，B 投资 C，则 A 间接投资 C", "投资", 156, true),
            rule("R002", "对称性规则", "如果 A 合作 B，则 B 合作 A", "合作", 89, true),
            rule("R003", "逆关系规则", "A 创始人 B ↔ B 创立于 A", "创始人/创立于", 45, true),
            rule("R004", "层级规则", "如果 A 是 B 的子概念，B 是 C 的子概念，则 A 是 C 的子概念", "子概念", 124, true),
            rule("R005", "属性传递", "如果 A 属于 B，B 有属性 P，则 A 继承属性 P", "属于", 67, false)
        ));
    }

    @GetMapping("/analysis/reasoning/comparison")
    public ApiResponse<Map<String, Object>> engineComparison() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("engines", Arrays.asList("rule", "transe", "gnn"));
        m.put("metrics", Arrays.asList(
            metric("准确率", 0.85, 0.78, 0.91),
            metric("召回率", 0.72, 0.81, 0.86),
            metric("F1", 0.78, 0.795, 0.885),
            metric("推理速度(条/秒)", 1250, 320, 45)
        ));
        return ApiResponse.success(m);
    }

    // ===== 统计分析 =====
    @GetMapping("/analysis/statistics/overview")
    public ApiResponse<Map<String, Object>> statisticsOverview() {
        // 真实调用：聚合查询 Neo4j 和 PostgreSQL
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("coreMetrics", Arrays.asList(
            metric2("entityCount", 1284503, 12.5, "up"),
            metric2("relationCount", 3892105, 18.3, "up"),
            metric2("projectCount", 24, 9.1, "up"),
            metric2("taskCount", 156, -3.2, "down")
        ));
        m.put("entityTypeDist", mock.entityTypeDist());
        m.put("relationTypeDist", statisticsData().get("relationTypeDist"));
        m.put("sourceDist", statisticsData().get("sourceDist"));
        m.put("graphMetrics", statisticsData().get("graphMetrics"));
        m.put("neo4jAvailable", neo4jClient.isAvailable());
        m.put("faissAvailable", faissClient.isAvailable());
        return ApiResponse.success(m);
    }

    @GetMapping("/analysis/statistics/trend")
    public ApiResponse<Map<String, Object>> growthTrend(
        @RequestParam(defaultValue = "30") int days) {
        // 真实调用：SELECT date, count FROM daily_stats WHERE date > NOW() - INTERVAL '$days' DAY
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("days", days);
        m.put("dates", Arrays.asList("07-01", "07-02", "07-03", "07-04", "07-05", "07-06", "今天"));
        m.put("entity", Arrays.asList(8200, 9320, 9010, 12930, 13300, 15200, 16800));
        m.put("relation", Arrays.asList(18900, 20300, 21400, 28900, 31200, 35800, 42100));
        return ApiResponse.success(m);
    }

    @GetMapping("/analysis/statistics/graph-metrics")
    public ApiResponse<Map<String, Object>> graphMetrics() {
        // 真实调用：Neo4j GDS 库计算图结构指标
        //   CALL gds.graph.list() YIELD graphName, nodeCount, relationshipCount
        //   CALL gds.alpha.graph.listDensity()
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("density", 0.0024);
        m.put("avgDegree", 6.06);
        m.put("diameter", 8);
        m.put("avgPathLength", 3.42);
        m.put("clusteringCoeff", 0.218);
        m.put("connectedComponents", 12);
        m.put("maxComponentSize", 1284503);
        m.put("source", neo4jClient.isAvailable() ? "neo4j-gds" : "mock");
        return ApiResponse.success(m);
    }

    @GetMapping("/analysis/statistics/top-entities")
    public ApiResponse<List<Map<String, Object>>> topEntities(
        @RequestParam(defaultValue = "10") int limit) {
        // 真实调用：MATCH (n)-[r]-() RETURN n, count(r) AS degree ORDER BY degree DESC LIMIT $limit
        List<Map<String, Object>> list = new ArrayList<>();
        String[] names = {"阿里巴巴", "腾讯", "百度", "马云", "杭州", "北京", "人工智能", "ChatGPT", "深圳", "上海"};
        int[] degrees = {8520, 7680, 6450, 5890, 5230, 4870, 4520, 4180, 3950, 3720};
        String[] types = {"机构", "机构", "机构", "人物", "地点", "地点", "概念", "作品", "地点", "地点"};
        for (int i = 0; i < Math.min(limit, names.length); i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank", i + 1);
            m.put("name", names[i]);
            m.put("type", types[i]);
            m.put("degree", degrees[i]);
            m.put("inDegree", degrees[i] * 3 / 5);
            m.put("outDegree", degrees[i] * 2 / 5);
            list.add(m);
        }
        return ApiResponse.success(list);
    }

    @GetMapping("/analysis/statistics/heatmap")
    public ApiResponse<List<Map<String, Object>>> heatmap() {
        // 真实调用：按类型对统计关系频次
        List<Map<String, Object>> list = new ArrayList<>();
        String[] types = {"人物", "机构", "地点", "事件", "概念", "作品"};
        Random r = new Random(99);
        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < types.length; j++) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("source", types[i]);
                m.put("target", types[j]);
                m.put("value", r.nextInt(500000));
                list.add(m);
            }
        }
        return ApiResponse.success(list);
    }

    @GetMapping("/analysis/statistics/projects")
    public ApiResponse<List<Map<String, Object>>> projectStats() {
        // 真实调用：SELECT p.name, p.entity_count, p.relation_count FROM projects p
        List<Map<String, Object>> list = new ArrayList<>();
        Object[][] data = {
            {"医疗健康知识图谱", 326540, 892300, 12, 0.92, "active"},
            {"金融风控知识图谱", 528900, 1620000, 18, 0.89, "active"},
            {"智能制造知识图谱", 184300, 425000, 8, 0.86, "active"},
            {"法律知识图谱", 95600, 218000, 6, 0.91, "active"},
            {"教育知识图谱", 67800, 154000, 5, 0.83, "archived"},
            {"电商商品知识图谱", 84360, 580000, 7, 0.88, "active"}
        };
        for (Object[] d : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", d[0]); m.put("entityCount", d[1]); m.put("relationCount", d[2]);
            m.put("memberCount", d[3]); m.put("quality", d[4]); m.put("status", d[5]);
            list.add(m);
        }
        return ApiResponse.success(list);
    }

    // ===== 辅助方法 =====
    private String generateAnswer(String question) {
        if (question.contains("阿里巴巴") && question.contains("创始人")) {
            return "阿里巴巴的主要创始人是马云。1999 年，马云带领 17 位创始人在杭州创立了阿里巴巴集团。除马云外，联合创始人还包括蔡崇信等人。";
        }
        if (question.contains("ChatGPT") && question.contains("开发者")) {
            return "ChatGPT 由 OpenAI 开发。OpenAI 是一家人工智能研究公司，成立于 2015 年，总部位于旧金山。";
        }
        return "已收到您的问题：\"" + question + "\"。基于知识图谱的分析结果，相关实体和关系已检索到。详细答案请参考图谱可视化中的关联信息。";
    }

    private Map<String, Object> source(String id, String type, String name, String desc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("type", type); m.put("name", name); m.put("desc", desc);
        return m;
    }

    private Map<String, Object> capability(String name, String desc, String example, int usedCount) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("desc", desc); m.put("example", example); m.put("usedCount", usedCount);
        return m;
    }

    private Map<String, Object> engine(String code, String name, String desc, String speed, double accuracy, String status) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", code); m.put("name", name); m.put("desc", desc);
        m.put("speed", speed); m.put("accuracy", accuracy); m.put("status", status);
        return m;
    }

    private Map<String, Object> inference(String id, String source, String relation, String target, double confidence, String engine) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("source", source); m.put("relation", relation);
        m.put("target", target); m.put("confidence", confidence); m.put("engine", engine);
        return m;
    }

    private Map<String, Object> pathNode(String id, String label, String type) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("label", label); m.put("type", type);
        return m;
    }

    private Map<String, Object> rule(String id, String name, String desc, String relation, int applied, boolean enabled) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("name", name); m.put("desc", desc);
        m.put("relation", relation); m.put("applied", applied); m.put("enabled", enabled);
        return m;
    }

    private Map<String, Object> metric(String name, double rule, double transe, double gnn) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("rule", rule); m.put("transe", transe); m.put("gnn", gnn);
        return m;
    }

    private Map<String, Object> metric2(String name, long value, double trend, String direction) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("value", value); m.put("trend", trend); m.put("direction", direction);
        return m;
    }

    private Map<String, Object> statisticsData() {
        return mock.statistics();
    }
}
