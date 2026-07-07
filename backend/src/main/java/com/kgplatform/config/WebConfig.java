package com.kgplatform.config;

import com.kgplatform.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：CORS + 全局异常处理。
 * <p>
 * 允许前端跨域访问（开发期前后端分离）。
 * 任何未被捕获的异常都包装为统一 ApiResponse，避免前端拿到 500 错误。
 */
@Slf4j
@org.springframework.context.annotation.Configuration
@org.springframework.web.bind.annotation.RestControllerAdvice
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("未捕获异常：", e);
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.error(500, "服务器内部错误：" + e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArg(IllegalArgumentException e) {
        log.warn("参数错误：{}", e.getMessage());
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.error(400, "参数错误：" + e.getMessage()));
    }
}
