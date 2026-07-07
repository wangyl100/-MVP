package com.kgplatform.middleware;

import com.kgplatform.config.MiddlewareProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

/**
 * PostgreSQL 客户端封装：包含真实 JDBC 调用代码，本地未安装时优雅降级。
 * <p>
 * 注意：JPA 默认使用 H2 内存数据库，保证应用启动。PostgreSQL 作为可选的"业务数据库"，
 * 通过此 Client 进行额外读写操作（如批量导入、报表查询等）。
 */
@Slf4j
@Component
public class PostgresClient {

    private final MiddlewareProperties properties;
    private Connection connection;
    private boolean available = false;

    @Autowired
    public PostgresClient(MiddlewareProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        MiddlewareProperties.PostgresConfig cfg = properties.getPostgresql();
        if (!cfg.isEnabled()) {
            log.warn("PostgreSQL 未启用（kg-platform.middleware.postgresql.enabled=false），使用默认 H2 数据库");
            return;
        }
        try {
            // === 真实 PostgreSQL 调用代码 ===
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword());
            available = true;
            log.info("PostgreSQL 已连接：{}", cfg.getUrl());
        } catch (Exception e) {
            log.error("PostgreSQL 连接失败，降级使用 H2：{}", e.getMessage());
            available = false;
        }
    }

    public boolean isAvailable() {
        if (!available || connection == null) return false;
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 执行查询 SQL。
     * 真实调用：connection.createStatement().executeQuery(sql)
     */
    public List<Map<String, Object>> query(String sql) {
        if (!isAvailable()) {
            log.debug("PostgreSQL 不可用，query 降级返回空列表");
            return Collections.emptyList();
        }
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<Map<String, Object>> list = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                list.add(row);
            }
            return list;
        } catch (SQLException e) {
            log.error("PostgreSQL 查询失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 执行更新/插入 SQL。
     */
    public int execute(String sql) {
        if (!isAvailable()) return 0;
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("PostgreSQL 执行失败：{}", e.getMessage());
            return 0;
        }
    }

    public void close() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) {}
        }
    }
}
