package com.kgplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 中间件配置属性。
 * <p>
 * 默认所有中间件均为 disabled，应用启动时不连接任何外部服务，仅返回 Mock 数据。
 * 本地安装了对应中间件后，将 enabled 设为 true 即可启用真实调用。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "kg-platform.middleware")
public class MiddlewareProperties {

    private PostgresConfig postgresql = new PostgresConfig();
    private Neo4jConfig neo4j = new Neo4jConfig();
    private KafkaConfig kafka = new KafkaConfig();
    private XxlJobConfig xxlJob = new XxlJobConfig();
    private FaissConfig faiss = new FaissConfig();

    @Data
    public static class PostgresConfig {
        private boolean enabled = false;
        private String url = "jdbc:postgresql://localhost:5432/kgplatform";
        private String username = "postgres";
        private String password = "postgres";
    }

    @Data
    public static class Neo4jConfig {
        private boolean enabled = false;
        private String uri = "bolt://localhost:7687";
        private String username = "neo4j";
        private String password = "neo4j";
    }

    @Data
    public static class KafkaConfig {
        private boolean enabled = false;
        private String bootstrapServers = "localhost:9092";
    }

    @Data
    public static class XxlJobConfig {
        private boolean enabled = false;
        private String adminAddresses = "http://localhost:8081/xxl-job-admin";
        private String accessToken = "default_token";
        private String appName = "kg-platform-executor";
    }

    @Data
    public static class FaissConfig {
        private boolean enabled = false;
        private String indexPath = "/tmp/faiss/index";
        private int dimension = 768;
    }
}
