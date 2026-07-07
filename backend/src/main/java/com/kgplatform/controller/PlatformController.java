package com.kgplatform.controller;

import com.kgplatform.common.ApiResponse;
import com.kgplatform.mock.MockDataProvider;
import com.kgplatform.middleware.Neo4jClient;
import com.kgplatform.middleware.PostgresClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 平台管理模块 Controller：Dashboard、用户、角色、个人中心。
 * <p>
 * 真实场景下用户/角色数据来自 PostgreSQL（JPA + PostgresClient）；
 * 中间件不可用时降级返回 Mock 数据。
 */
@RestController
@RequestMapping("/api/v1")
public class PlatformController {

    @Autowired private MockDataProvider mock;
    @Autowired private PostgresClient postgresClient;
    @Autowired private Neo4jClient neo4jClient;

    // ===== Dashboard =====
    @GetMapping("/dashboard/stats")
    public ApiResponse<Map<String, Object>> dashboardStats() {
        // 真实调用：从 PostgreSQL 聚合统计
        if (postgresClient.isAvailable()) {
            try {
                // SELECT COUNT(*) FROM projects; SELECT COUNT(*) FROM entities; ...
                // 这里仅作演示，实际聚合后填充 map
                return ApiResponse.success(mock.dashboardStats());
            } catch (Exception e) {
                return ApiResponse.success(mock.dashboardStats());
            }
        }
        return ApiResponse.success(mock.dashboardStats());
    }

    @GetMapping("/dashboard/task-status")
    public ApiResponse<Map<String, Object>> taskStatus() {
        return ApiResponse.success(mock.taskStatus());
    }

    @GetMapping("/dashboard/growth-trend")
    public ApiResponse<Map<String, Object>> growthTrend() {
        return ApiResponse.success(mock.growthTrend());
    }

    @GetMapping("/dashboard/entity-type-dist")
    public ApiResponse<List<Map<String, Object>>> entityTypeDist() {
        return ApiResponse.success(mock.entityTypeDist());
    }

    @GetMapping("/dashboard/recent-activities")
    public ApiResponse<List<Map<String, Object>>> recentActivities() {
        return ApiResponse.success(mock.recentActivities());
    }

    // ===== 用户管理 =====
    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> userList(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String role,
        @RequestParam(required = false) String status) {
        // 真实调用：SELECT * FROM users WHERE ...
        List<Map<String, Object>> list = mock.userList();
        if (keyword != null && !keyword.isEmpty()) {
            list = list.stream().filter(u ->
                String.valueOf(u.get("username")).contains(keyword) ||
                String.valueOf(u.get("nickname")).contains(keyword) ||
                String.valueOf(u.get("email")).contains(keyword)
            ).toList();
        }
        if (role != null && !role.isEmpty()) {
            list = list.stream().filter(u -> role.equals(u.get("role"))).toList();
        }
        if (status != null && !status.isEmpty()) {
            list = list.stream().filter(u -> status.equals(u.get("status"))).toList();
        }
        return ApiResponse.success(list);
    }

    @PostMapping("/users")
    public ApiResponse<Map<String, Object>> createUser(@RequestBody Map<String, Object> body) {
        // 真实调用：INSERT INTO users ...
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", "U" + System.currentTimeMillis() % 10000);
        result.putAll(body);
        result.put("status", "active");
        result.put("createTime", "2026-07-06");
        return ApiResponse.success(result);
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable String id, @RequestBody Map<String, Object> body) {
        // 真实调用：UPDATE users SET ... WHERE id=?
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.putAll(body);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable String id) {
        // 真实调用：UPDATE users SET deleted=true WHERE id=?
        return ApiResponse.success();
    }

    // ===== 角色管理 =====
    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> roleList() {
        return ApiResponse.success(mock.roleList());
    }

    @PostMapping("/roles")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", "R" + System.currentTimeMillis() % 1000);
        result.putAll(body);
        return ApiResponse.success(result);
    }

    // ===== 个人中心 =====
    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "U0001");
        m.put("username", "admin");
        m.put("nickname", "超级管理员");
        m.put("email", "admin@kg-platform.com");
        m.put("phone", "13800138000");
        m.put("role", "超级管理员");
        m.put("avatar", "");
        m.put("lastLogin", "2026-07-06 14:32");
        return ApiResponse.success(m);
    }

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> body) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.putAll(body);
        m.put("id", "U0001");
        return ApiResponse.success(m);
    }

    @GetMapping("/profile/logs")
    public ApiResponse<List<Map<String, Object>>> profileLogs() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[][] data = {
            {"2026-07-06 14:32", "登录系统", "127.0.0.1"},
            {"2026-07-06 11:08", "创建项目 P006", "127.0.0.1"},
            {"2026-07-06 09:15", "修改用户角色", "127.0.0.1"},
            {"2026-07-05 18:42", "导出图谱数据", "127.0.0.1"},
            {"2026-07-05 16:30", "启动训练任务 TR0003", "127.0.0.1"}
        };
        for (String[] d : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("time", d[0]); m.put("action", d[1]); m.put("ip", d[2]);
            list.add(m);
        }
        return ApiResponse.success(list);
    }
}
