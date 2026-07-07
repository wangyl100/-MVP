package com.kgplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 智能知识图谱构建平台 - 后端启动入口
 * <p>
 * 设计原则：本地未安装 Neo4j / PostgreSQL / Kafka / XXL-Job / Faiss 等中间件时，
 * 应用仍可正常启动并对外提供 Mock 数据，保证前端页面可见。
 * 中间件真实调用代码完整保留，通过 {@code kg-platform.middleware.*.enabled} 开关控制。
 * <p>
 * 排除 Spring Data Neo4j 自动配置：我们通过自定义 {@link com.kgplatform.middleware.Neo4jClient}
 * 直接管理 Driver 生命周期，避免 bean 名称冲突，并支持 enabled=false 时的优雅降级。
 */
@SpringBootApplication(exclude = {
    Neo4jAutoConfiguration.class,
    Neo4jDataAutoConfiguration.class,
    Neo4jReactiveDataAutoConfiguration.class
})
@EntityScan(basePackages = "com.kgplatform")
@EnableJpaRepositories(basePackages = "com.kgplatform")
public class KgPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(KgPlatformApplication.class, args);
    }
}
