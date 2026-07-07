package com.kgplatform.controller;

import com.kgplatform.common.ApiResponse;
import com.kgplatform.mock.MockDataProvider;
import com.kgplatform.middleware.KafkaClient;
import com.kgplatform.middleware.PostgresClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 数据接入与语料管理 Controller。
 * <p>
 * 真实场景下，数据导入任务通过 Kafka 异步触发；语料元数据存 PostgreSQL。
 */
@RestController
@RequestMapping("/api/v1")
public class DataController {

    @Autowired private MockDataProvider mock;
    @Autowired private KafkaClient kafkaClient;
    @Autowired private PostgresClient postgresClient;

    // ===== 数据源 =====
    @GetMapping("/data-sources")
    public ApiResponse<List<Map<String, Object>>> dataSourceList() {
        // 真实调用：SELECT * FROM data_sources
        return ApiResponse.success(mock.dataSourceList());
    }

    @PostMapping("/data-sources")
    public ApiResponse<Map<String, Object>> createDataSource(@RequestBody Map<String, Object> body) {
        // 真实调用：INSERT INTO data_sources ...
        // 真实调用：尝试 JDBC 连接测试
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "DS" + System.currentTimeMillis() % 1000);
        m.putAll(body);
        m.put("status", "connected");
        m.put("tableCount", 0);
        m.put("lastSync", "2026-07-06");
        return ApiResponse.success(m);
    }

    @PostMapping("/data-sources/{id}/test")
    public ApiResponse<Map<String, Object>> testDataSource(@PathVariable String id) {
        // 真实调用：尝试 JDBC 连接测试
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("status", "connected");
        m.put("latency", "32ms");
        return ApiResponse.success(m);
    }

    @PostMapping("/data-sources/{id}/import")
    public ApiResponse<Map<String, Object>> importData(@PathVariable String id, @RequestBody Map<String, Object> body) {
        // 真实调用：通过 Kafka 发送异步导入任务
        String taskId = "IMP" + System.currentTimeMillis() % 10000;
        kafkaClient.sendImportTask(taskId, "{\"dataSourceId\":\"" + id + "\",\"config\":" + body + "}");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("taskId", taskId);
        m.put("status", "queued");
        m.put("message", "导入任务已提交" + (kafkaClient.isAvailable() ? "到 Kafka 队列" : "（Kafka 不可用，降级为本地处理）"));
        return ApiResponse.success(m);
    }

    // ===== 语料管理 =====
    @GetMapping("/corpus")
    public ApiResponse<List<Map<String, Object>>> corpusList(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String language) {
        List<Map<String, Object>> list = mock.corpusList();
        if (keyword != null && !keyword.isEmpty()) {
            list = list.stream().filter(c -> String.valueOf(c.get("name")).contains(keyword)).toList();
        }
        if (language != null && !language.isEmpty()) {
            list = list.stream().filter(c -> language.equals(c.get("language"))).toList();
        }
        return ApiResponse.success(list);
    }

    @PostMapping("/corpus/upload")
    public ApiResponse<Map<String, Object>> uploadCorpus(@RequestBody Map<String, Object> body) {
        // 真实调用：保存文件到对象存储，写入 corpus 表
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "C" + System.currentTimeMillis() % 10000);
        m.putAll(body);
        m.put("status", "ready");
        m.put("uploadTime", "2026-07-06");
        return ApiResponse.success(m);
    }

    @GetMapping("/corpus/{id}/preview")
    public ApiResponse<Map<String, Object>> corpusPreview(@PathVariable String id) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("content", "这是一段示例语料内容，用于预览。实际场景下从对象存储或文件系统读取。");
        m.put("page", 1);
        m.put("totalPages", 12);
        return ApiResponse.success(m);
    }

    @GetMapping("/corpus/stats")
    public ApiResponse<Map<String, Object>> corpusStats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", 8543);
        m.put("totalWords", 12845032);
        m.put("chineseCount", 6421);
        m.put("englishCount", 2122);
        m.put("avgWords", 1503);
        return ApiResponse.success(m);
    }
}
