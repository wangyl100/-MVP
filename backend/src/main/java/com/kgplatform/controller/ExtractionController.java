package com.kgplatform.controller;

import com.kgplatform.common.ApiResponse;
import com.kgplatform.mock.MockDataProvider;
import com.kgplatform.middleware.FaissClient;
import com.kgplatform.middleware.KafkaClient;
import com.kgplatform.middleware.Neo4jClient;
import com.kgplatform.middleware.XxlJobClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 知识抽取 Controller。
 * <p>
 * 真实场景下，抽取任务通过 Kafka 异步分发：
 * - 结构化映射任务 → JDBC 直读 → ETL → 写 Neo4j
 * - LLM 抽取任务 → 调用 OpenAI/Claude API → 解析结果 → 写 Neo4j
 * - DL 抽取任务 → 调用 BERT-NER/TPLinker 模型服务 → 写 Neo4j
 * <p>
 * 抽取出的实体向量同步写入 Faiss，用于检索增强。
 */
@RestController
@RequestMapping("/api/v1")
public class ExtractionController {

    @Autowired private MockDataProvider mock;
    @Autowired private KafkaClient kafkaClient;
    @Autowired private Neo4jClient neo4jClient;
    @Autowired private FaissClient faissClient;
    @Autowired private XxlJobClient xxlJobClient;

    // ===== 结构化映射 =====
    @GetMapping("/extraction/structured/tasks")
    public ApiResponse<List<Map<String, Object>>> structuredTasks() {
        // 真实调用：SELECT * FROM extraction_tasks WHERE type='structured'
        List<Map<String, Object>> list = mock.extractionTasks();
        return ApiResponse.success(list.stream().filter(t -> "structured".equals(t.get("type"))).toList());
    }

    @PostMapping("/extraction/structured/run")
    public ApiResponse<Map<String, Object>> runStructuredExtraction(@RequestBody Map<String, Object> body) {
        // 真实调用：通过 Kafka 发送结构化映射任务
        String taskId = "EXT-S-" + System.currentTimeMillis() % 10000;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", taskId);
        payload.put("type", "structured");
        payload.put("dataSourceId", body.get("dataSourceId"));
        payload.put("mapping", body.get("mapping"));
        kafkaClient.sendExtractionTask(taskId, payload.toString());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("taskId", taskId);
        m.put("status", "queued");
        m.put("message", "结构化映射任务已提交" + (kafkaClient.isAvailable() ? "至 Kafka" : "（Kafka 不可用，降级同步处理）"));
        return ApiResponse.success(m);
    }

    @GetMapping("/extraction/structured/mapping-templates")
    public ApiResponse<List<Map<String, Object>>> mappingTemplates() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(template("TPL001", "通用实体映射", "将关系数据库的实体表映射为图谱节点", "MySQL/PostgreSQL"));
        list.add(template("TPL002", "关系映射模板", "将外键关系映射为图谱边", "MySQL/PostgreSQL"));
        list.add(template("TPL003", "层级映射模板", "将树形结构映射为上下位关系", "Oracle"));
        list.add(template("TPL004", "多源融合模板", "融合多个数据源的同名实体", "混合数据源"));
        return ApiResponse.success(list);
    }

    // ===== LLM 抽取 =====
    @GetMapping("/extraction/llm/tasks")
    public ApiResponse<List<Map<String, Object>>> llmTasks() {
        return ApiResponse.success(mock.extractionTasks().stream()
            .filter(t -> "llm".equals(t.get("type"))).toList());
    }

    @PostMapping("/extraction/llm/run")
    public ApiResponse<Map<String, Object>> runLlmExtraction(@RequestBody Map<String, Object> body) {
        // 真实调用：调用大模型 API（OpenAI/Claude/通义千问）进行抽取
        // 真实代码示例：
        //   String prompt = buildPrompt(body.get("text"), schema);
        //   String response = openAiClient.chatCompletion(prompt);
        //   List<Entity> entities = parseLlmResponse(response);
        //   neo4jClient.execute("CREATE (n:Entity {id:$id, name:$name})", params);
        //   faissClient.addVector(entityId, embedding);
        String taskId = "EXT-LLM-" + System.currentTimeMillis() % 10000;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", taskId);
        payload.put("type", "llm");
        payload.put("model", body.getOrDefault("model", "GPT-4"));
        payload.put("corpusIds", body.get("corpusIds"));
        kafkaClient.sendExtractionTask(taskId, payload.toString());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("taskId", taskId);
        m.put("model", body.getOrDefault("model", "GPT-4"));
        m.put("status", "queued");
        m.put("message", "LLM 抽取任务已提交");
        return ApiResponse.success(m);
    }

    @PostMapping("/extraction/llm/preview")
    public ApiResponse<Map<String, Object>> llmPreview(@RequestBody Map<String, Object> body) {
        // 真实调用：同步调用大模型 API 进行单条抽取预览
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("text", body.getOrDefault("text", ""));
        m.put("entities", Arrays.asList(
            entity("阿里巴巴", "机构", 0.95),
            entity("马云", "人物", 0.93),
            entity("杭州", "地点", 0.91)
        ));
        m.put("relations", Arrays.asList(
            relation("马云", "创始人", "阿里巴巴", 0.89),
            relation("阿里巴巴", "总部位于", "杭州", 0.92)
        ));
        m.put("model", body.getOrDefault("model", "GPT-4"));
        m.put("latency", "1.2s");
        return ApiResponse.success(m);
    }

    // ===== 深度学习抽取 =====
    @GetMapping("/extraction/dl/tasks")
    public ApiResponse<List<Map<String, Object>>> dlTasks() {
        return ApiResponse.success(mock.extractionTasks().stream()
            .filter(t -> "dl".equals(t.get("type"))).toList());
    }

    @PostMapping("/extraction/dl/run")
    public ApiResponse<Map<String, Object>> runDlExtraction(@RequestBody Map<String, Object> body) {
        // 真实调用：通过 XxlJob 触发离线 DL 抽取任务
        String taskId = "EXT-DL-" + System.currentTimeMillis() % 10000;
        int jobId = 100 + (int) (System.currentTimeMillis() % 100);
        xxlJobClient.triggerJob(jobId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", taskId);
        payload.put("type", "dl");
        payload.put("model", body.getOrDefault("model", "BERT-NER-v2"));
        payload.put("corpusIds", body.get("corpusIds"));
        kafkaClient.sendExtractionTask(taskId, payload.toString());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("taskId", taskId);
        m.put("model", body.getOrDefault("model", "BERT-NER-v2"));
        m.put("status", "queued");
        m.put("xxlJobId", jobId);
        m.put("message", "DL 抽取任务已提交" + (xxlJobClient.isAvailable() ? "至 XxlJob" : "（XxlJob 不可用，降级处理）"));
        return ApiResponse.success(m);
    }

    @GetMapping("/extraction/dl/models")
    public ApiResponse<List<Map<String, Object>>> dlModels() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(modelInfo("BERT-NER-v2", "NER", "PyTorch", "v2.3.1", 0.892, "online"));
        list.add(modelInfo("RoBERTa-RE-Finance", "RE", "PyTorch", "v1.8.0", 0.856, "online"));
        list.add(modelInfo("TPLinker-Joint", "Joint", "PyTorch", "v1.2.0", 0.834, "offline"));
        list.add(modelInfo("BiLSTM-CRF-NER", "NER", "TensorFlow", "v1.5.2", 0.812, "online"));
        return ApiResponse.success(list);
    }

    // ===== 通用：抽取任务列表与详情 =====
    @GetMapping("/extraction/tasks")
    public ApiResponse<List<Map<String, Object>>> allTasks(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String status) {
        List<Map<String, Object>> list = mock.extractionTasks();
        if (type != null && !type.isEmpty()) {
            list = list.stream().filter(t -> type.equals(t.get("type"))).toList();
        }
        if (status != null && !status.isEmpty()) {
            list = list.stream().filter(t -> status.equals(t.get("status"))).toList();
        }
        return ApiResponse.success(list);
    }

    @GetMapping("/extraction/tasks/{id}")
    public ApiResponse<Map<String, Object>> taskDetail(@PathVariable String id) {
        // 真实调用：SELECT * FROM extraction_tasks WHERE id=?
        return ApiResponse.success(mock.extractionTasks().stream()
            .filter(t -> id.equals(t.get("id"))).findFirst().orElseGet(LinkedHashMap::new));
    }

    @GetMapping("/extraction/stats")
    public ApiResponse<Map<String, Object>> extractionStats() {
        // 真实调用：聚合统计
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalTasks", 156);
        m.put("todayTasks", 12);
        m.put("successRate", 0.923);
        m.put("avgDuration", "8m 42s");
        m.put("totalEntities", 1284503);
        m.put("totalRelations", 3892105);
        m.put("faissIndexed", faissClient.getIndexSize());
        m.put("faissAvailable", faissClient.isAvailable());
        return ApiResponse.success(m);
    }

    // ===== 辅助方法 =====
    private Map<String, Object> template(String id, String name, String desc, String dataSource) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("name", name); m.put("desc", desc); m.put("dataSource", dataSource);
        return m;
    }

    private Map<String, Object> entity(String name, String type, double confidence) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("type", type); m.put("confidence", confidence);
        return m;
    }

    private Map<String, Object> relation(String source, String relation, String target, double confidence) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("source", source); m.put("relation", relation);
        m.put("target", target); m.put("confidence", confidence);
        return m;
    }

    private Map<String, Object> modelInfo(String name, String type, String framework, String version, double f1, String status) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("type", type); m.put("framework", framework);
        m.put("version", version); m.put("f1", f1); m.put("status", status);
        return m;
    }
}
