package com.kgplatform.controller;

import com.kgplatform.common.ApiResponse;
import com.kgplatform.mock.MockDataProvider;
import com.kgplatform.middleware.Neo4jClient;
import com.kgplatform.middleware.PostgresClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 图谱项目模块 Controller：项目管理、Schema 设计。
 */
@RestController
@RequestMapping("/api/v1")
public class ProjectController {

    @Autowired private MockDataProvider mock;
    @Autowired private PostgresClient postgresClient;
    @Autowired private Neo4jClient neo4jClient;

    // ===== 项目管理 =====
    @GetMapping("/projects")
    public ApiResponse<List<Map<String, Object>>> projectList(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String domain) {
        // 真实调用：SELECT * FROM projects WHERE ...
        List<Map<String, Object>> list = mock.projectList();
        if (keyword != null && !keyword.isEmpty()) {
            list = list.stream().filter(p ->
                String.valueOf(p.get("name")).contains(keyword) ||
                String.valueOf(p.get("desc")).contains(keyword)
            ).toList();
        }
        if (domain != null && !domain.isEmpty()) {
            list = list.stream().filter(p -> domain.equals(p.get("domain"))).toList();
        }
        return ApiResponse.success(list);
    }

    @GetMapping("/projects/{id}")
    public ApiResponse<Map<String, Object>> projectDetail(@PathVariable String id) {
        // 真实调用：SELECT * FROM projects WHERE id=?
        return mock.projectList().stream()
            .filter(p -> id.equals(p.get("id")))
            .findFirst()
            .map(ApiResponse::success)
            .orElseGet(() -> ApiResponse.error(404, "项目不存在"));
    }

    @PostMapping("/projects")
    public ApiResponse<Map<String, Object>> createProject(@RequestBody Map<String, Object> body) {
        // 真实调用：INSERT INTO projects ...
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "P" + System.currentTimeMillis() % 1000);
        m.putAll(body);
        m.put("entityCount", 0);
        m.put("relationCount", 0);
        m.put("memberCount", 1);
        m.put("status", "active");
        m.put("updateTime", "2026-07-06");
        return ApiResponse.success(m);
    }

    @PutMapping("/projects/{id}")
    public ApiResponse<Map<String, Object>> updateProject(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.putAll(body);
        return ApiResponse.success(m);
    }

    @DeleteMapping("/projects/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable String id) {
        return ApiResponse.success();
    }

    // ===== Schema 管理 =====
    @GetMapping("/schema/entity-types")
    public ApiResponse<List<Map<String, Object>>> schemaEntityTypes() {
        // 真实调用：SELECT * FROM schema_entity_types WHERE project_id=?
        // 或从 Neo4j 调用：CALL db.labels()
        if (neo4jClient.isAvailable()) {
            try {
                List<Map<String, Object>> result = neo4jClient.query(
                    "CALL db.labels() YIELD label RETURN label", null);
                if (!result.isEmpty()) {
                    return ApiResponse.success(mock.schemaEntityTypes());
                }
            } catch (Exception ignored) {}
        }
        return ApiResponse.success(mock.schemaEntityTypes());
    }

    @GetMapping("/schema/relation-types")
    public ApiResponse<List<Map<String, Object>>> schemaRelationTypes() {
        // 真实调用：SELECT * FROM schema_relation_types WHERE project_id=?
        if (neo4jClient.isAvailable()) {
            try {
                List<Map<String, Object>> result = neo4jClient.query(
                    "CALL db.relationshipTypes() YIELD relationshipType RETURN relationshipType", null);
                if (!result.isEmpty()) {
                    return ApiResponse.success(mock.schemaRelationTypes());
                }
            } catch (Exception ignored) {}
        }
        return ApiResponse.success(mock.schemaRelationTypes());
    }

    @PostMapping("/schema/entity-types")
    public ApiResponse<Map<String, Object>> createEntityType(@RequestBody Map<String, Object> body) {
        // 真实调用：INSERT INTO schema_entity_types ...
        // 真实调用：CREATE CONSTRAINT FOR (n:Label) REQUIRE n.id IS UNIQUE
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "ET" + System.currentTimeMillis() % 100);
        m.putAll(body);
        m.put("instanceCount", 0);
        return ApiResponse.success(m);
    }

    @PostMapping("/schema/relation-types")
    public ApiResponse<Map<String, Object>> createRelationType(@RequestBody Map<String, Object> body) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "RT" + System.currentTimeMillis() % 100);
        m.putAll(body);
        m.put("instanceCount", 0);
        return ApiResponse.success(m);
    }

    @PostMapping("/schema/ai-generate")
    public ApiResponse<Map<String, Object>> aiGenerateSchema(@RequestBody Map<String, Object> body) {
        // 真实调用：调用大模型 API 生成 Schema
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("entityTypes", Arrays.asList("人物", "机构", "地点", "事件"));
        m.put("relationTypes", Arrays.asList("出生于", "就职于", "位于", "参与"));
        m.put("desc", "基于您的领域描述，AI 生成了推荐 Schema");
        return ApiResponse.success(m);
    }
}
