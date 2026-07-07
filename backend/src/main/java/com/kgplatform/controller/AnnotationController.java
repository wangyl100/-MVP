package com.kgplatform.controller;

import com.kgplatform.common.ApiResponse;
import com.kgplatform.mock.MockDataProvider;
import com.kgplatform.middleware.PostgresClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 数据标注 Controller。
 * <p>
 * 真实场景下，标注任务和标注数据存 PostgreSQL，标注工作台支持实体/关系/事件/属性四类标注。
 */
@RestController
@RequestMapping("/api/v1")
public class AnnotationController {

    @Autowired private MockDataProvider mock;
    @Autowired private PostgresClient postgresClient;

    // ===== 标注任务 =====
    @GetMapping("/annotation/tasks")
    public ApiResponse<List<Map<String, Object>>> taskList(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String status) {
        // 真实调用：SELECT * FROM annotation_tasks
        List<Map<String, Object>> list = mock.annotationTasks();
        if (type != null && !type.isEmpty()) {
            list = list.stream().filter(t -> type.equals(t.get("type"))).toList();
        }
        if (status != null && !status.isEmpty()) {
            list = list.stream().filter(t -> status.equals(t.get("status"))).toList();
        }
        return ApiResponse.success(list);
    }

    @PostMapping("/annotation/tasks")
    public ApiResponse<Map<String, Object>> createTask(@RequestBody Map<String, Object> body) {
        // 真实调用：INSERT INTO annotation_tasks ...
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "ANN" + System.currentTimeMillis() % 10000);
        m.putAll(body);
        m.put("progress", 0);
        m.put("annotated", 0);
        m.put("status", "pending");
        return ApiResponse.success(m);
    }

    @GetMapping("/annotation/tasks/{id}")
    public ApiResponse<Map<String, Object>> taskDetail(@PathVariable String id) {
        return ApiResponse.success(mock.annotationTasks().stream()
            .filter(t -> id.equals(t.get("id"))).findFirst().orElseGet(LinkedHashMap::new));
    }

    // ===== 标注工作台 =====
    @GetMapping("/annotation/workspace/{taskId}/items")
    public ApiResponse<Map<String, Object>> workspaceItems(
        @PathVariable String taskId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize) {
        // 真实调用：SELECT * FROM annotation_items WHERE task_id=? LIMIT ?,?
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", 500);

        List<Map<String, Object>> items = new ArrayList<>();
        String[] texts = {
            "阿里巴巴集团由马云等18人于1999年在杭州创立。",
            "腾讯控股有限公司成立于1998年，总部位于深圳。",
            "北京是中华人民共和国的首都，是全国政治文化中心。",
            "ChatGPT 由 OpenAI 于 2022 年 11 月发布，引发人工智能热潮。",
            "人工智能是研究、开发用于模拟、延伸和扩展人的智能的理论、方法、技术及应用系统的一门新的技术科学。"
        };
        String[] types = {"entity", "relation", "event", "attribute", "classification"};
        for (int i = 0; i < pageSize; i++) {
            int idx = (page - 1) * pageSize + i;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "ITEM" + String.format("%04d", idx + 1));
            m.put("text", texts[idx % texts.length]);
            m.put("type", types[idx % types.length]);
            m.put("status", idx % 3 == 0 ? "annotated" : "pending");
            m.put("assignee", idx % 3 == 0 ? "标注员" + (idx % 5 + 1) : null);
            if (idx % 3 == 0) {
                m.put("annotations", Arrays.asList(
                    ann("阿里巴巴", "机构", 0, 5),
                    ann("马云", "人物", 7, 2),
                    ann("杭州", "地点", 14, 2)
                ));
            }
            items.add(m);
        }
        result.put("items", items);
        return ApiResponse.success(result);
    }

    @PostMapping("/annotation/workspace/{taskId}/submit")
    public ApiResponse<Map<String, Object>> submitAnnotation(
        @PathVariable String taskId, @RequestBody Map<String, Object> body) {
        // 真实调用：UPDATE annotation_items SET annotations=?, status='annotated' WHERE id=?
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("taskId", taskId);
        m.put("itemId", body.get("itemId"));
        m.put("status", "annotated");
        m.put("message", "标注已提交");
        return ApiResponse.success(m);
    }

    @PostMapping("/annotation/workspace/{taskId}/skip")
    public ApiResponse<Map<String, Object>> skipItem(
        @PathVariable String taskId, @RequestBody Map<String, Object> body) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("taskId", taskId);
        m.put("itemId", body.get("itemId"));
        m.put("status", "skipped");
        return ApiResponse.success(m);
    }

    // ===== 质量控制 =====
    @GetMapping("/annotation/quality/overview")
    public ApiResponse<Map<String, Object>> qualityOverview() {
        // 真实调用：聚合统计标注质量
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalAnnotated", 45621);
        m.put("avgQuality", 0.918);
        m.put("passRate", 0.892);
        m.put("reviewRate", 0.645);
        m.put("consistency", 0.876);

        Map<String, Object> byType = new LinkedHashMap<>();
        byType.put("entity", typeStat(18500, 0.932, 0.915));
        byType.put("relation", typeStat(12800, 0.902, 0.887));
        byType.put("event", typeStat(7600, 0.891, 0.865));
        byType.put("attribute", typeStat(5200, 0.938, 0.921));
        byType.put("classification", typeStat(1521, 0.952, 0.943));
        m.put("byType", byType);

        m.put("topAnnotators", Arrays.asList(
            annotator("张三", 1850, 0.945, 0.962),
            annotator("李四", 1620, 0.932, 0.951),
            annotator("王五", 1380, 0.928, 0.943),
            annotator("赵六", 1240, 0.921, 0.935),
            annotator("钱七", 1080, 0.915, 0.928)
        ));
        return ApiResponse.success(m);
    }

    @GetMapping("/annotation/quality/conflicts")
    public ApiResponse<List<Map<String, Object>>> conflicts() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(conflict("C001", "ITEM0042", "阿里巴巴", "机构 / 概念", "张三 / 李四", "高"));
        list.add(conflict("C002", "ITEM0078", "杭州", "地点 / 机构", "王五 / 赵六", "中"));
        list.add(conflict("C003", "ITEM0156", "ChatGPT", "作品 / 概念", "钱七 / 孙八", "高"));
        list.add(conflict("C004", "ITEM0203", "参与", "关系类型", "张三 / 王五", "低"));
        return ApiResponse.success(list);
    }

    @PostMapping("/annotation/quality/review")
    public ApiResponse<Map<String, Object>> review(@RequestBody Map<String, Object> body) {
        // 真实调用：UPDATE annotation_items SET reviewed=true, final_label=? WHERE id=?
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("conflictId", body.get("conflictId"));
        m.put("status", "resolved");
        m.put("finalLabel", body.getOrDefault("finalLabel", ""));
        m.put("message", "冲突已解决");
        return ApiResponse.success(m);
    }

    // ===== 辅助方法 =====
    private Map<String, Object> ann(String text, String label, int start, int len) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("text", text); m.put("label", label);
        m.put("start", start); m.put("length", len);
        return m;
    }

    private Map<String, Object> typeStat(int count, double quality, double passRate) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("count", count); m.put("quality", quality); m.put("passRate", passRate);
        return m;
    }

    private Map<String, Object> annotator(String name, int count, double quality, double speed) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("count", count);
        m.put("quality", quality); m.put("speed", speed);
        return m;
    }

    private Map<String, Object> conflict(String id, String itemId, String content, String conflictType, String annotators, String level) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("itemId", itemId); m.put("content", content);
        m.put("conflictType", conflictType); m.put("annotators", annotators); m.put("level", level);
        return m;
    }
}
