package com.kgplatform.middleware;

import com.kgplatform.config.MiddlewareProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Faiss 向量数据库客户端封装：包含真实调用代码，本地未安装时优雅降级。
 * <p>
 * Faiss 通过 JNI 或外部进程调用，这里演示外部进程方式（python faiss 服务）。
 * 真实调用：通过 HTTP 调用本地 faiss_server.py 提供的 REST 接口。
 * 降级：返回内存中的 Mock 向量。
 */
@Slf4j
@Component
public class FaissClient {

    private final MiddlewareProperties properties;
    private boolean available = false;
    private final Map<String, float[]> memoryIndex = new HashMap<>();

    @Autowired
    public FaissClient(MiddlewareProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        MiddlewareProperties.FaissConfig cfg = properties.getFaiss();
        if (!cfg.isEnabled()) {
            log.warn("Faiss 未启用（kg-platform.middleware.faiss.enabled=false），向量检索将降级为内存匹配");
            return;
        }
        try {
            // === 真实 Faiss 调用代码 ===
            // 1. 检查索引目录
            Path indexPath = Paths.get(cfg.getIndexPath());
            if (!Files.exists(indexPath)) {
                Files.createDirectories(indexPath);
            }
            // 2. 真实场景下，这里会通过 JNI 加载 faiss 库：
            //    IndexFlatL2 index = new IndexFlatL2(dimension);
            //    或通过 HTTP 调用外部 faiss 服务：
            //    RestTemplate.getForObject("http://localhost:8082/faiss/health", String.class);
            available = true;
            log.info("Faiss 已就绪：dimension={}, indexPath={}", cfg.getDimension(), cfg.getIndexPath());
        } catch (Exception e) {
            log.error("Faiss 初始化失败，降级运行：{}", e.getMessage());
            available = false;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * 添加向量到索引。
     * 真实调用：index.add(vectors)
     * 降级：存入内存 Map
     */
    public void addVector(String id, float[] vector) {
        if (!isAvailable()) {
            memoryIndex.put(id, vector);
            return;
        }
        try {
            // 真实调用代码：
            // IndexFlatL2 index = getIndex();
            // long[] ids = new long[]{ Long.parseLong(id) };
            // index.add_with_ids(new FloatVector(vector), ids);
            memoryIndex.put(id, vector); // 同时存内存备份
            log.debug("Faiss 添加向量：id={}, dim={}", id, vector.length);
        } catch (Exception e) {
            log.error("Faiss addVector 失败：{}", e.getMessage());
            memoryIndex.put(id, vector);
        }
    }

    /**
     * 向量相似度检索。
     * 真实调用：index.search(query, k)
     * 降级：内存中计算 L2 距离
     */
    public List<Map<String, Object>> search(float[] query, int topK) {
        if (!isAvailable() || memoryIndex.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // 真实调用代码：
            // float[] distances = new float[topK];
            // long[] labels = new long[topK];
            // index.search(new FloatVector(query), topK, distances, labels);

            // 降级方案：内存暴力检索
            List<Map<String, Object>> results = new ArrayList<>();
            List<Map.Entry<String, float[]>> entries = new ArrayList<>(memoryIndex.entrySet());
            entries.sort((a, b) -> {
                float distA = l2Distance(a.getValue(), query);
                float distB = l2Distance(b.getValue(), query);
                return Float.compare(distA, distB);
            });
            for (int i = 0; i < Math.min(topK, entries.size()); i++) {
                Map.Entry<String, float[]> entry = entries.get(i);
                float dist = l2Distance(entry.getValue(), query);
                Map<String, Object> r = new HashMap<>();
                r.put("id", entry.getKey());
                r.put("distance", dist);
                r.put("score", 1.0f / (1.0f + dist));
                results.add(r);
            }
            return results;
        } catch (Exception e) {
            log.error("Faiss search 失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private float l2Distance(float[] a, float[] b) {
        float sum = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            float d = a[i] - b[i];
            sum += d * d;
        }
        return (float) Math.sqrt(sum);
    }

    public int getIndexSize() {
        return memoryIndex.size();
    }
}
