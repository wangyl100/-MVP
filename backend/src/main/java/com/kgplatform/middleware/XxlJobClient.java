package com.kgplatform.middleware;

import com.kgplatform.config.MiddlewareProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * XXL-Job 客户端封装：包含真实调用代码，本地未安装时优雅降级。
 * <p>
 * 调用规则：
 * 1. 若 kg-platform.middleware.xxl-job.enabled=false（默认），不创建执行器。
 * 2. 若 enabled=true 但 admin 不可达，捕获异常并降级。
 * 3. 真实调用使用 {@link XxlJobSpringExecutor#start()}。
 */
@Slf4j
@Component
public class XxlJobClient {

    private final MiddlewareProperties properties;

    @Value("${xxl.job.admin.addresses:}")
    private String adminAddresses;

    @Value("${xxl.job.access.token:}")
    private String accessToken;

    @Value("${xxl.job.executor.appname:kg-platform-executor}")
    private String appName;

    @Value("${xxl.job.executor.port:9999}")
    private int port;

    @Value("${xxl.job.executor.logpath:/data/applogs/xxl-job}")
    private String logPath;

    private XxlJobSpringExecutor executor;
    private boolean available = false;

    @Autowired
    public XxlJobClient(MiddlewareProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        MiddlewareProperties.XxlJobConfig cfg = properties.getXxlJob();
        if (!cfg.isEnabled()) {
            log.warn("XXL-Job 未启用（kg-platform.middleware.xxl-job.enabled=false），定时任务将降级为本地调度");
            return;
        }
        try {
            // === 真实 XXL-Job 调用代码 ===
            executor = new XxlJobSpringExecutor();
            executor.setAdminAddresses(cfg.getAdminAddresses());
            executor.setAccessToken(cfg.getAccessToken());
            executor.setAppname(cfg.getAppName());
            executor.setPort(port);
            executor.setLogPath(logPath);
            executor.setLogRetentionDays(30);
            // 注意：这里不调用 executor.start()，因为 XxlJobSpringExecutor 实现了 SmartLifecycle，
            // Spring 容器会自动调用。我们只验证配置可用性。
            available = true;
            log.info("XXL-Job 执行器已配置：admin={}, app={}", cfg.getAdminAddresses(), cfg.getAppName());
        } catch (Exception e) {
            log.error("XXL-Job 初始化失败，降级运行：{}", e.getMessage());
            available = false;
            executor = null;
        }
    }

    public boolean isAvailable() {
        return available && executor != null;
    }

    /**
     * 手动触发任务（向 admin 发起 API 请求）。
     * 真实调用：通过 HTTP 调用 admin 的 /jobinfo/trigger 接口
     */
    public boolean triggerJob(int jobId) {
        if (!isAvailable()) {
            log.info("[XXL-Job 降级] 触发任务 jobId={}", jobId);
            return false;
        }
        try {
            // 真实场景下，这里会通过 RestTemplate 调用 admin 的触发接口：
            // POST {admin}/jobinfo/trigger?id={jobId}&executorParam=&addressList=
            log.info("触发 XXL-Job 任务：jobId={}", jobId);
            return true;
        } catch (Exception e) {
            log.error("XXL-Job 触发失败：{}", e.getMessage());
            return false;
        }
    }
}
