package com.kgplatform.middleware;

import com.kgplatform.config.MiddlewareProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.neo4j.driver.summary.ResultSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Neo4j 客户端封装：包含真实调用代码，本地未安装时优雅降级。
 * <p>
 * 调用规则：
 * 1. 若 kg-platform.middleware.neo4j.enabled=false（默认），跳过 Driver 初始化，所有方法返回空/Mock。
 * 2. 若 enabled=true 但连接失败，捕获异常并降级，应用继续运行。
 * 3. 真实调用使用 {@link Driver#session()} 执行 Cypher。
 */
@Slf4j
@Component
public class Neo4jClient {

    private final MiddlewareProperties properties;
    private Driver driver;
    private boolean available = false;

    @Autowired
    public Neo4jClient(MiddlewareProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        MiddlewareProperties.Neo4jConfig cfg = properties.getNeo4j();
        if (!cfg.isEnabled()) {
            log.warn("Neo4j 未启用（kg-platform.middleware.neo4j.enabled=false），相关操作将降级为内存数据");
            return;
        }
        try {
            // === 真实 Neo4j 调用代码 ===
            driver = GraphDatabase.driver(cfg.getUri(),
                AuthTokens.basic(cfg.getUsername(), cfg.getPassword()));
            driver.verifyConnectivity();
            available = true;
            log.info("Neo4j 已连接：{}", cfg.getUri());
        } catch (Exception e) {
            log.error("Neo4j 连接失败，降级运行：{}", e.getMessage());
            available = false;
        }
    }

    public boolean isAvailable() {
        return available && driver != null;
    }

    /**
     * 执行 Cypher 查询并返回结果列表。
     * 真实调用：driver.session().run(cypher).list()
     * 降级：返回 emptyList
     */
    public List<Map<String, Object>> query(String cypher, Map<String, Object> params) {
        if (!isAvailable()) {
            log.debug("Neo4j 不可用，query 降级返回空列表");
            return Collections.emptyList();
        }
        try (Session session = driver.session()) {
            Result result = params == null ? session.run(cypher) : session.run(cypher, params);
            List<Map<String, Object>> list = new ArrayList<>();
            while (result.hasNext()) {
                org.neo4j.driver.Record r = result.next();
                Map<String, Object> map = new HashMap<>();
                r.keys().forEach(k -> map.put(k, r.get(k).asObject()));
                list.add(map);
            }
            return list;
        } catch (Exception e) {
            log.error("Neo4j 查询失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 执行写入 Cypher（CREATE/MERGE/DELETE）。
     */
    public int execute(String cypher, Map<String, Object> params) {
        if (!isAvailable()) {
            log.debug("Neo4j 不可用，execute 降级返回 0");
            return 0;
        }
        try (Session session = driver.session()) {
            ResultSummary summary = params == null
                ? session.run(cypher).consume()
                : session.run(cypher, params).consume();
            return summary.counters().nodesCreated();
        } catch (Exception e) {
            log.error("Neo4j 执行失败：{}", e.getMessage());
            return 0;
        }
    }

    /**
     * 统计节点数量。
     */
    public long countNodes(String label) {
        if (!isAvailable()) return 0L;
        try (Session session = driver.session()) {
            String cypher = label == null || label.isEmpty()
                ? "MATCH (n) RETURN count(n) AS cnt"
                : "MATCH (n:`" + label + "`) RETURN count(n) AS cnt";
            Result result = session.run(cypher);
            if (result.hasNext()) {
                return result.next().get("cnt").asLong();
            }
            return 0L;
        } catch (Exception e) {
            log.error("Neo4j countNodes 失败：{}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 查询节点的邻居。
     */
    public List<Map<String, Object>> findNeighbors(String nodeId, int limit) {
        if (!isAvailable()) return Collections.emptyList();
        try (Session session = driver.session()) {
            String cypher = "MATCH (n)-[r]-(m) WHERE id(n)=$id RETURN m, type(r) AS rel LIMIT $limit";
            Result result = session.run(cypher,
                Values.parameters("id", Long.parseLong(nodeId), "limit", limit));
            List<Map<String, Object>> list = new ArrayList<>();
            while (result.hasNext()) {
                org.neo4j.driver.Record rec = result.next();
                Map<String, Object> map = new HashMap<>();
                map.put("node", rec.get("m").asMap());
                map.put("relation", rec.get("rel").asString());
                list.add(map);
            }
            return list;
        } catch (Exception e) {
            log.error("Neo4j findNeighbors 失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void close() {
        if (driver != null) {
            try { driver.close(); } catch (Exception ignored) {}
        }
    }
}
