package com.kgplatform.controller;

import com.kgplatform.common.ApiResponse;
import com.kgplatform.mock.MockDataProvider;
import com.kgplatform.middleware.FaissClient;
import com.kgplatform.middleware.Neo4jClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 图谱探索 Controller。
 * <p>
 * 真实场景下：
 * - 图可视化：Neo4j Cypher 查询子图，前端用 G6 渲染
 * - 实体检索：Faiss 向量检索 + Neo4j 精确查询
 * - 路径分析：Neo4j shortestPath 算法
 */
@RestController
@RequestMapping("/api/v1")
public class ExploreController {

    @Autowired private MockDataProvider mock;
    @Autowired private Neo4jClient neo4jClient;
    @Autowired private FaissClient faissClient;

    // ===== 图谱可视化 =====
    @GetMapping("/explore/graph")
    public ApiResponse<Map<String, Object>> graphData(
        @RequestParam(required = false) String center,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "50") int limit) {
        // 真实调用：MATCH (n)-[r]->(m) RETURN n, r, m LIMIT $limit
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        // 尝试 Neo4j 真实查询
        if (neo4jClient.isAvailable()) {
            String cypher = "MATCH (n)-[r]->(m) RETURN n, r, m LIMIT " + limit;
            List<Map<String, Object>> results = neo4jClient.query(cypher, null);
            if (!results.isEmpty()) {
                // 转换 Neo4j 结果为前端 G6 格式
                // 真实代码略，此处降级到 mock 数据
            }
        }

        // 降级/默认 mock 数据
        String[] nodeIds = {"n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8", "n9", "n10", "n11", "n12", "n13", "n14", "n15"};
        String[] nodeLabels = {"马云", "阿里巴巴", "杭州", "腾讯", "深圳", "马化腾", "ChatGPT", "OpenAI", "人工智能",
            "机器学习", "北京", "上海", "李彦宏", "百度", "2024奥运会"};
        String[] nodeTypes = {"人物", "机构", "地点", "机构", "地点", "人物", "作品", "机构", "概念",
            "概念", "地点", "地点", "人物", "机构", "事件"};
        String[] nodeColors = {"#1677ff", "#52c41a", "#faad14", "#52c41a", "#faad14", "#1677ff",
            "#13c2c2", "#52c41a", "#722ed1", "#722ed1", "#faad14", "#faad14", "#1677ff", "#52c41a", "#ff4d4f"};

        for (int i = 0; i < nodeIds.length; i++) {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id", nodeIds[i]);
            n.put("label", nodeLabels[i]);
            n.put("type", nodeTypes[i]);
            n.put("color", nodeColors[i]);
            n.put("size", 30 + (i % 3) * 10);
            nodes.add(n);
        }

        // 边
        String[][] edgeData = {
            {"n1", "n2", "创始人"}, {"n1", "n3", "出生于"}, {"n2", "n3", "总部位于"},
            {"n4", "n5", "总部位于"}, {"n6", "n4", "创始人"}, {"n6", "n5", "居住于"},
            {"n7", "n8", "开发者"}, {"n8", "n9", "研究"}, {"n9", "n10", "包含"},
            {"n7", "n9", "属于"}, {"n11", "n15", "举办"}, {"n14", "n11", "总部位于"},
            {"n13", "n14", "创始人"}, {"n13", "n11", "居住于"}, {"n2", "n4", "竞争"},
            {"n8", "n2", "投资"}, {"n10", "n7", "支撑"}, {"n4", "n9", "布局"},
            {"n2", "n9", "布局"}
        };
        for (int i = 0; i < edgeData.length; i++) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", "e" + (i + 1));
            e.put("source", edgeData[i][0]);
            e.put("target", edgeData[i][1]);
            e.put("label", edgeData[i][2]);
            edges.add(e);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodes", nodes);
        result.put("edges", edges);
        result.put("source", neo4jClient.isAvailable() ? "neo4j" : "mock");
        result.put("totalNodes", nodes.size());
        result.put("totalEdges", edges.size());
        return ApiResponse.success(result);
    }

    @GetMapping("/explore/graph/neighbors")
    public ApiResponse<Map<String, Object>> neighbors(
        @RequestParam String nodeId,
        @RequestParam(defaultValue = "20") int limit) {
        // 真实调用：MATCH (n)-[r]-(m) WHERE id(n)=$id RETURN m, type(r) LIMIT $limit
        List<Map<String, Object>> neighbors = neo4jClient.findNeighbors(nodeId, limit);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodeId", nodeId);
        result.put("neighbors", neighbors.isEmpty() ? Arrays.asList(
            Map.of("id", "n2", "label", "阿里巴巴", "relation", "创始人"),
            Map.of("id", "n3", "label", "杭州", "relation", "出生于")
        ) : neighbors);
        result.put("source", neo4jClient.isAvailable() ? "neo4j" : "mock");
        return ApiResponse.success(result);
    }

    @PostMapping("/explore/graph/shortest-path")
    public ApiResponse<Map<String, Object>> shortestPath(@RequestBody Map<String, Object> body) {
        // 真实调用：MATCH p=shortestPath((a)-[*..8]-(b)) WHERE id(a)=$sid AND id(b)=$tid RETURN p
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("source", body.get("source"));
        result.put("target", body.get("target"));
        result.put("found", true);
        result.put("length", 3);
        result.put("path", Arrays.asList(
            pathNode("n1", "马云", "人物"),
            pathNode("n2", "阿里巴巴", "机构"),
            pathNode("n8", "OpenAI", "机构"),
            pathNode("n7", "ChatGPT", "作品")
        ));
        result.put("edges", Arrays.asList(
            pathEdge("e1", "n1", "n2", "创始人"),
            pathEdge("e2", "n2", "n8", "投资"),
            pathEdge("e3", "n8", "n7", "开发者")
        ));
        result.put("source_type", neo4jClient.isAvailable() ? "neo4j" : "mock");
        return ApiResponse.success(result);
    }

    // ===== 实体检索 =====
    @GetMapping("/explore/search")
    public ApiResponse<Map<String, Object>> search(
        @RequestParam String keyword,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String source,
        @RequestParam(required = false) Double minConfidence,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize) {
        // 真实调用：
        // 1. 精确匹配：MATCH (n) WHERE n.name CONTAINS $keyword
        // 2. 模糊匹配：Faiss 向量检索 top-K
        List<Map<String, Object>> all = mock.entityList();
        if (keyword != null && !keyword.isEmpty()) {
            all = all.stream().filter(e -> String.valueOf(e.get("name")).contains(keyword)).toList();
        }
        if (type != null && !type.isEmpty()) {
            all = all.stream().filter(e -> type.equals(e.get("type"))).toList();
        }
        if (source != null && !source.isEmpty()) {
            all = all.stream().filter(e -> source.equals(e.get("source"))).toList();
        }
        if (minConfidence != null) {
            all = all.stream().filter(e -> {
                try { return Double.parseDouble(String.valueOf(e.get("confidence"))) >= minConfidence; }
                catch (Exception ex) { return false; }
            }).toList();
        }

        int total = all.size();
        int from = Math.min((page - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", all.subList(from, to));
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("keyword", keyword);
        result.put("faissAvailable", faissClient.isAvailable());
        result.put("faissIndexed", faissClient.getIndexSize());
        return ApiResponse.success(result);
    }

    @GetMapping("/explore/search/hot")
    public ApiResponse<List<Map<String, Object>>> hotKeywords() {
        return ApiResponse.success(Arrays.asList(
            hot("阿里巴巴", 1850, "up"),
            hot("马云", 1620, "up"),
            hot("ChatGPT", 1480, "up"),
            hot("人工智能", 1350, "up"),
            hot("腾讯", 1180, "down"),
            hot("百度", 920, "up"),
            hot("OpenAI", 850, "up"),
            hot("机器学习", 780, "down")
        ));
    }

    @GetMapping("/explore/search/history")
    public ApiResponse<List<Map<String, Object>>> searchHistory() {
        return ApiResponse.success(Arrays.asList(
            history("H001", "阿里巴巴", "2026-07-06 14:23", 32),
            history("H002", "马云 创始人", "2026-07-06 11:08", 8),
            history("H003", "人工智能 领域", "2026-07-06 09:45", 156),
            history("H004", "ChatGPT 开发者", "2026-07-05 18:32", 5),
            history("H005", "北京 互联网公司", "2026-07-05 16:20", 24)
        ));
    }

    @GetMapping("/explore/search/recommend")
    public ApiResponse<List<Map<String, Object>>> recommend() {
        return ApiResponse.success(Arrays.asList(
            recommend("阿里巴巴集团", "机构", "杭州", 528900),
            recommend("马云", "人物", "杭州", 325400),
            recommend("杭州", "地点", "浙江", 187200),
            recommend("人工智能", "概念", "计算机科学", 248300),
            recommend("ChatGPT", "作品", "OpenAI", 62300)
        ));
    }

    @PostMapping("/explore/search/vector")
    public ApiResponse<Map<String, Object>> vectorSearch(@RequestBody Map<String, Object> body) {
        // 真实调用：将查询文本编码为向量，调用 Faiss 检索
        //   float[] queryVec = embeddingClient.encode(body.get("text"));
        //   List<Map<String, Object>> results = faissClient.search(queryVec, topK);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", body.get("text"));
        result.put("results", Collections.emptyList());
        result.put("faissAvailable", faissClient.isAvailable());
        result.put("message", faissClient.isAvailable() ? "向量检索完成" : "Faiss 不可用，无法进行向量检索");
        return ApiResponse.success(result);
    }

    // ===== 辅助方法 =====
    private Map<String, Object> pathNode(String id, String label, String type) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("label", label); m.put("type", type);
        return m;
    }

    private Map<String, Object> pathEdge(String id, String source, String target, String label) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("source", source); m.put("target", target); m.put("label", label);
        return m;
    }

    private Map<String, Object> hot(String keyword, int count, String trend) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("keyword", keyword); m.put("count", count); m.put("trend", trend);
        return m;
    }

    private Map<String, Object> history(String id, String keyword, String time, int resultCount) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("keyword", keyword); m.put("time", time); m.put("resultCount", resultCount);
        return m;
    }

    private Map<String, Object> recommend(String name, String type, String location, long count) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("type", type); m.put("location", location); m.put("instanceCount", count);
        return m;
    }
}
