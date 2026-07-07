package com.kgplatform.controller;

import com.kgplatform.common.ApiResponse;
import com.kgplatform.mock.MockDataProvider;
import com.kgplatform.middleware.Neo4jClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 图谱实例管理 Controller。
 * <p>
 * 真实场景下：
 * - 实体和关系存 Neo4j（Cypher 查询）
 * - 属性数据可存 PostgreSQL（属性图模式）
 * - 版本快照存对象存储
 */
@RestController
@RequestMapping("/api/v1")
public class GraphController {

    @Autowired private MockDataProvider mock;
    @Autowired private Neo4jClient neo4jClient;

    // ===== 实体管理 =====
    @GetMapping("/graph/entities")
    public ApiResponse<Map<String, Object>> entityList(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String source,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize) {
        // 真实调用：MATCH (n) WHERE n.name CONTAINS $keyword RETURN n SKIP $skip LIMIT $limit
        List<Map<String, Object>> all;
        if (neo4jClient.isAvailable()) {
            // 真实查询 Neo4j
            String cypher = "MATCH (n) RETURN id(n) AS id, labels(n) AS type, properties(n) AS props " +
                "SKIP " + (page - 1) * pageSize + " LIMIT " + pageSize;
            all = neo4jClient.query(cypher, null);
            if (all.isEmpty()) {
                all = mock.entityList(); // Neo4j 没数据时降级到 mock
            }
        } else {
            all = mock.entityList();
        }

        // 过滤
        if (keyword != null && !keyword.isEmpty()) {
            all = all.stream().filter(e -> String.valueOf(e.get("name")).contains(keyword)).toList();
        }
        if (type != null && !type.isEmpty()) {
            all = all.stream().filter(e -> type.equals(e.get("type"))).toList();
        }
        if (source != null && !source.isEmpty()) {
            all = all.stream().filter(e -> source.equals(e.get("source"))).toList();
        }

        int total = all.size();
        int from = Math.min((page - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<Map<String, Object>> pageList = all.subList(from, to);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("source", neo4jClient.isAvailable() ? "neo4j" : "mock");
        return ApiResponse.success(result);
    }

    @GetMapping("/graph/entities/{id}")
    public ApiResponse<Map<String, Object>> entityDetail(@PathVariable String id) {
        // 真实调用：MATCH (n) WHERE id(n)=$id RETURN n
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", "阿里巴巴");
        m.put("type", "机构");
        m.put("properties", Arrays.asList(
            prop("名称", "阿里巴巴集团控股有限公司"),
            prop("成立时间", "1999-06-28"),
            prop("总部", "杭州"),
            prop("行业", "互联网/电子商务"),
            prop("创始人", "马云"),
            prop("CEO", "张勇"),
            prop("员工数", "235,000+"),
            prop("简介", "全球领先的互联网科技公司")
        ));
        m.put("relations", Arrays.asList(
            relationItem("R001", "马云", "创始人", "阿里巴巴", "outgoing"),
            relationItem("R002", "阿里巴巴", "总部位于", "杭州", "outgoing"),
            relationItem("R003", "阿里巴巴", "投资", "蚂蚁集团", "outgoing"),
            relationItem("R004", "蔡崇信", "联合创始人", "阿里巴巴", "incoming")
        ));
        m.put("source", "LLM抽取");
        m.put("confidence", 0.95);
        m.put("updateTime", "2026-07-06");
        m.put("neo4jAvailable", neo4jClient.isAvailable());
        return ApiResponse.success(m);
    }

    @PostMapping("/graph/entities")
    public ApiResponse<Map<String, Object>> createEntity(@RequestBody Map<String, Object> body) {
        // 真实调用：CREATE (n:Entity {id:$id, name:$name, type:$type})
        Map<String, Object> params = new HashMap<>();
        params.put("id", "E" + System.currentTimeMillis() % 1000000);
        params.put("name", body.getOrDefault("name", ""));
        params.put("type", body.getOrDefault("type", ""));
        neo4jClient.execute(
            "CREATE (n:`" + body.getOrDefault("type", "Entity") + "` {id:$id, name:$name, type:$type})", params);

        Map<String, Object> m = new LinkedHashMap<>();
        m.putAll(params);
        m.put("message", "实体已创建" + (neo4jClient.isAvailable() ? "（已写入 Neo4j）" : "（Neo4j 不可用，仅内存）"));
        return ApiResponse.success(m);
    }

    @PutMapping("/graph/entities/{id}")
    public ApiResponse<Map<String, Object>> updateEntity(@PathVariable String id, @RequestBody Map<String, Object> body) {
        // 真实调用：MATCH (n) WHERE id(n)=$id SET n += $props
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.putAll(body);
        m.put("message", "实体已更新");
        return ApiResponse.success(m);
    }

    @DeleteMapping("/graph/entities/{id}")
    public ApiResponse<Map<String, Object>> deleteEntity(@PathVariable String id) {
        // 真实调用：MATCH (n) WHERE id(n)=$id DETACH DELETE n
        neo4jClient.execute("MATCH (n) WHERE id(n)=$id DETACH DELETE n",
            Map.of("id", Long.parseLong(id.replaceAll("\\D", ""))));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("message", "实体已删除");
        return ApiResponse.success(m);
    }

    @GetMapping("/graph/entities/stats")
    public ApiResponse<Map<String, Object>> entityStats() {
        // 真实调用：MATCH (n) RETURN labels(n), count(n)
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", neo4jClient.isAvailable() ? neo4jClient.countNodes(null) : 1284503);
        m.put("byType", mock.entityTypeDist());
        m.put("bySource", Arrays.asList(
            sourceStat("LLM抽取", 578026, 45),
            sourceStat("结构化导入", 359661, 28),
            sourceStat("DL抽取", 231211, 18),
            sourceStat("手动添加", 115605, 9)
        ));
        return ApiResponse.success(m);
    }

    // ===== 关系管理 =====
    @GetMapping("/graph/relations")
    public ApiResponse<Map<String, Object>> relationList(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize) {
        // 真实调用：MATCH (a)-[r]->(b) RETURN a.name, type(r), b.name SKIP $skip LIMIT $limit
        List<Map<String, Object>> all = mock.relationList();
        if (keyword != null && !keyword.isEmpty()) {
            all = all.stream().filter(r ->
                String.valueOf(r.get("source")).contains(keyword) ||
                String.valueOf(r.get("target")).contains(keyword) ||
                String.valueOf(r.get("relation")).contains(keyword)).toList();
        }
        if (type != null && !type.isEmpty()) {
            all = all.stream().filter(r -> type.equals(r.get("relation"))).toList();
        }
        int total = all.size();
        int from = Math.min((page - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", all.subList(from, to));
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/graph/relations/{id}")
    public ApiResponse<Map<String, Object>> relationDetail(@PathVariable String id) {
        // 真实调用：MATCH (a)-[r]->(b) WHERE id(r)=$id RETURN a, r, b
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("source", "马云");
        m.put("relation", "创始人");
        m.put("target", "阿里巴巴");
        m.put("properties", Arrays.asList(
            prop("任职时间", "1999-06-28"),
            prop("持股比例", "4.8%"),
            prop("角色", "董事局主席")
        ));
        m.put("sourceTracing", Arrays.asList(
            sourceTrace("EXT0042", "LLM抽取", "GPT-4", 0.92, "2026-07-05 14:23"),
            sourceTrace("ANN0028", "人工标注", "张三", 1.0, "2026-07-05 16:08"),
            sourceTrace("C000156", "语料来源", "财经新闻-上市公司分析.docx", null, "2026-07-04 10:15")
        ));
        return ApiResponse.success(m);
    }

    @PostMapping("/graph/relations")
    public ApiResponse<Map<String, Object>> createRelation(@RequestBody Map<String, Object> body) {
        // 真实调用：MATCH (a),(b) WHERE id(a)=$sid AND id(b)=$tid CREATE (a)-[r:`type`]->(b)
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "R" + System.currentTimeMillis() % 1000000);
        m.putAll(body);
        m.put("message", "关系已创建");
        return ApiResponse.success(m);
    }

    @DeleteMapping("/graph/relations/{id}")
    public ApiResponse<Map<String, Object>> deleteRelation(@PathVariable String id) {
        // 真实调用：MATCH ()-[r]->() WHERE id(r)=$id DELETE r
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("message", "关系已删除");
        return ApiResponse.success(m);
    }

    // ===== 图谱版本 =====
    @GetMapping("/graph/versions")
    public ApiResponse<List<Map<String, Object>>> versionList() {
        // 真实调用：SELECT * FROM graph_versions ORDER BY create_time DESC
        return ApiResponse.success(mock.graphVersions());
    }

    @GetMapping("/graph/versions/{id}")
    public ApiResponse<Map<String, Object>> versionDetail(@PathVariable String id) {
        return ApiResponse.success(mock.graphVersions().stream()
            .filter(v -> id.equals(v.get("id"))).findFirst().orElseGet(LinkedHashMap::new));
    }

    @PostMapping("/graph/versions")
    public ApiResponse<Map<String, Object>> createVersion(@RequestBody Map<String, Object> body) {
        // 真实调用：生成图谱快照，存对象存储
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "V" + System.currentTimeMillis() % 1000);
        m.put("version", "v1." + (System.currentTimeMillis() % 100) + ".0");
        m.putAll(body);
        m.put("creator", "admin");
        m.put("createTime", "2026-07-06");
        m.put("tag", "current");
        m.put("message", "新版本已创建");
        return ApiResponse.success(m);
    }

    @PostMapping("/graph/versions/{id}/rollback")
    public ApiResponse<Map<String, Object>> rollbackVersion(@PathVariable String id) {
        // 真实调用：从快照恢复图谱数据
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("status", "rollback_success");
        m.put("message", "已回滚到指定版本");
        return ApiResponse.success(m);
    }

    @GetMapping("/graph/versions/compare")
    public ApiResponse<Map<String, Object>> compareVersions(
        @RequestParam String v1, @RequestParam String v2) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("v1", v1);
        m.put("v2", v2);
        m.put("entityDiff", Map.of("added", 8642, "removed", 320, "modified", 1580));
        m.put("relationDiff", Map.of("added", 24500, "removed", 1850, "modified", 4280));
        m.put("topChanges", Arrays.asList(
            change("新增", "实体", "疾病", 2350),
            change("新增", "实体", "药物", 1820),
            change("合并", "实体", "重复机构", 320),
            change("新增", "关系", "治疗", 8500),
            change("修复", "关系", "错误抽取", 1850)
        ));
        return ApiResponse.success(m);
    }

    // ===== 辅助方法 =====
    private Map<String, Object> prop(String name, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("value", value);
        return m;
    }

    private Map<String, Object> relationItem(String id, String source, String relation, String target, String direction) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("source", source); m.put("relation", relation);
        m.put("target", target); m.put("direction", direction);
        return m;
    }

    private Map<String, Object> sourceStat(String source, long count, double percent) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("source", source); m.put("count", count); m.put("percent", percent);
        return m;
    }

    private Map<String, Object> sourceTrace(String id, String type, String source, Double confidence, String time) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("type", type); m.put("source", source);
        if (confidence != null) m.put("confidence", confidence);
        m.put("time", time);
        return m;
    }

    private Map<String, Object> change(String op, String type, String target, int count) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("op", op); m.put("type", type); m.put("target", target); m.put("count", count);
        return m;
    }
}
