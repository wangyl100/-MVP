package com.kgplatform.controller;

import com.kgplatform.common.ApiResponse;
import com.kgplatform.mock.MockDataProvider;
import com.kgplatform.middleware.FaissClient;
import com.kgplatform.middleware.KafkaClient;
import com.kgplatform.middleware.XxlJobClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 模型训练 Controller。
 * <p>
 * 真实场景下：
 * - 训练任务通过 XxlJob 调度到 GPU 节点执行
 * - 训练数据从标注数据集加载
 * - 模型评估指标实时回传
 * - 模型版本存对象存储，向量索引同步到 Faiss
 */
@RestController
@RequestMapping("/api/v1")
public class ModelController {

    @Autowired private MockDataProvider mock;
    @Autowired private XxlJobClient xxlJobClient;
    @Autowired private KafkaClient kafkaClient;
    @Autowired private FaissClient faissClient;

    // ===== 模型训练 =====
    @GetMapping("/model/training/tasks")
    public ApiResponse<List<Map<String, Object>>> trainingTasks(
        @RequestParam(required = false) String status) {
        List<Map<String, Object>> list = mock.trainingTasks();
        if (status != null && !status.isEmpty()) {
            list = list.stream().filter(t -> status.equals(t.get("status"))).toList();
        }
        return ApiResponse.success(list);
    }

    @PostMapping("/model/training/start")
    public ApiResponse<Map<String, Object>> startTraining(@RequestBody Map<String, Object> body) {
        // 真实调用：通过 XxlJob 触发训练任务
        //   xxlJobClient.triggerJob(trainingJobId)
        //   kafkaClient.sendTrainingTask(taskId, config)
        String taskId = "TR" + System.currentTimeMillis() % 10000;
        int jobId = 200 + (int) (System.currentTimeMillis() % 100);
        xxlJobClient.triggerJob(jobId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", taskId);
        payload.put("modelName", body.getOrDefault("modelName", ""));
        payload.put("baseModel", body.getOrDefault("baseModel", "BERT-base"));
        payload.put("datasetId", body.get("datasetId"));
        payload.put("hyperparams", body.get("hyperparams"));
        kafkaClient.sendTrainingTask(taskId, payload.toString());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("taskId", taskId);
        m.put("xxlJobId", jobId);
        m.put("status", "queued");
        m.put("message", "训练任务已提交" + (xxlJobClient.isAvailable() ? "至 XxlJob 调度器" : "（XxlJob 不可用，降级同步处理）"));
        return ApiResponse.success(m);
    }

    @GetMapping("/model/training/tasks/{id}/logs")
    public ApiResponse<List<Map<String, Object>>> trainingLogs(@PathVariable String id) {
        // 真实调用：从日志服务获取训练日志
        List<Map<String, Object>> logs = new ArrayList<>();
        String[] levels = {"INFO", "INFO", "INFO", "WARN", "INFO"};
        String[] messages = {
            "Epoch 1/30: loss=1.2345, accuracy=0.6521",
            "Epoch 5/30: loss=0.8765, accuracy=0.7823",
            "Epoch 10/30: loss=0.5432, accuracy=0.8512",
            "GPU 显存使用率达到 92%，建议减小 batch_size",
            "Epoch 15/30: loss=0.3214, accuracy=0.8867"
        };
        for (int i = 0; i < messages.length; i++) {
            Map<String, Object> log = new LinkedHashMap<>();
            log.put("id", id);
            log.put("epoch", (i + 1) * 5);
            log.put("level", levels[i]);
            log.put("message", messages[i]);
            log.put("time", "2026-07-06 " + (10 + i) + ":23:4" + i);
            logs.add(log);
        }
        return ApiResponse.success(logs);
    }

    @PostMapping("/model/training/tasks/{id}/stop")
    public ApiResponse<Map<String, Object>> stopTraining(@PathVariable String id) {
        // 真实调用：xxlJobClient.stopJob(jobId)
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("taskId", id);
        m.put("status", "stopped");
        m.put("message", "训练任务已停止");
        return ApiResponse.success(m);
    }

    // ===== 效果评估 =====
    @GetMapping("/model/evaluation/list")
    public ApiResponse<List<Map<String, Object>>> evaluationList() {
        return ApiResponse.success(mock.modelList());
    }

    @GetMapping("/model/evaluation/{modelId}")
    public ApiResponse<Map<String, Object>> evaluationDetail(@PathVariable String modelId) {
        // 真实调用：SELECT * FROM model_evaluations WHERE model_id=?
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("modelId", modelId);
        m.put("modelName", "BERT-NER-Medical");
        m.put("version", "v2.3.1");
        m.put("dataset", "医疗实体标注集 v3.0");

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("precision", 0.901);
        metrics.put("recall", 0.884);
        metrics.put("f1", 0.892);
        metrics.put("accuracy", 0.915);
        m.put("metrics", metrics);

        // 按实体类型的评估结果
        m.put("byType", Arrays.asList(
            typeEval("疾病", 0.932, 0.918, 0.925),
            typeEval("症状", 0.895, 0.872, 0.883),
            typeEval("药物", 0.912, 0.901, 0.906),
            typeEval("检查", 0.876, 0.854, 0.865),
            typeEval("治疗", 0.889, 0.871, 0.880)
        ));

        // 训练曲线
        m.put("trainingCurve", Arrays.asList(
            curvePoint(1, 1.2345, 0.6521, 0.7012),
            curvePoint(5, 0.8765, 0.7823, 0.8123),
            curvePoint(10, 0.5432, 0.8512, 0.8654),
            curvePoint(15, 0.3214, 0.8867, 0.8789),
            curvePoint(20, 0.2134, 0.9012, 0.8912),
            curvePoint(30, 0.1876, 0.9045, 0.8923)
        ));

        m.put("confusionMatrix", Arrays.asList(
            confusionRow("疾病", 482, 8, 5, 3, 2),
            confusionRow("症状", 12, 425, 8, 15, 6),
            confusionRow("药物", 4, 6, 398, 9, 3),
            confusionRow("检查", 2, 12, 7, 356, 8),
            confusionRow("治疗", 1, 4, 3, 6, 312)
        ));
        return ApiResponse.success(m);
    }

    @PostMapping("/model/evaluation/run")
    public ApiResponse<Map<String, Object>> runEvaluation(@RequestBody Map<String, Object> body) {
        // 真实调用：触发离线评估任务
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("modelId", body.get("modelId"));
        m.put("datasetId", body.get("datasetId"));
        m.put("status", "running");
        m.put("message", "评估任务已启动");
        return ApiResponse.success(m);
    }

    // ===== 版本管理 =====
    @GetMapping("/model/version/list")
    public ApiResponse<List<Map<String, Object>>> versionList(
        @RequestParam(required = false) String modelId) {
        // 真实调用：SELECT * FROM model_versions WHERE model_id=? ORDER BY create_time DESC
        List<Map<String, Object>> list = new ArrayList<>();
        String[] versions = {"v2.3.1", "v2.3.0", "v2.2.0", "v2.1.0", "v2.0.0", "v1.0.0"};
        String[] descs = {
            "修复医疗术语边界识别问题，提升长实体识别",
            "增加药品实体类型，扩展训练数据",
            "优化少样本场景的抽取能力",
            "切换预训练模型底座为 RoBERTa-large",
            "架构升级，采用 BERT+CRF 结构",
            "初始版本，BiLSTM-CRF 基线模型"
        };
        double[] f1s = {0.892, 0.876, 0.862, 0.848, 0.825, 0.763};
        String[] tags = {"current", "stable", "", "", "", ""};
        String[] times = {"2026-07-04", "2026-07-01", "2026-06-25", "2026-06-15", "2026-06-01", "2026-05-15"};

        for (int i = 0; i < versions.length; i++) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("version", versions[i]);
            v.put("desc", descs[i]);
            v.put("f1", f1s[i]);
            v.put("precision", f1s[i] + 0.009);
            v.put("recall", f1s[i] - 0.008);
            v.put("dataset", (12580 - i * 1500) + "");
            v.put("tag", tags[i]);
            v.put("time", times[i]);
            v.put("creator", i % 2 == 0 ? "admin" : "zhangsan");
            list.add(v);
        }
        return ApiResponse.success(list);
    }

    @GetMapping("/model/version/compare")
    public ApiResponse<Map<String, Object>> compareVersions(
        @RequestParam String v1, @RequestParam String v2) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("v1", v1);
        m.put("v2", v2);
        m.put("metrics", Arrays.asList(
            metricDiff("Precision", 0.901, 0.832, 0.069),
            metricDiff("Recall", 0.884, 0.818, 0.066),
            metricDiff("F1", 0.892, 0.825, 0.067)
        ));
        m.put("improvements", Arrays.asList(
            "新增医疗术语词表 1,200 条",
            "优化长实体识别逻辑",
            "扩展训练数据 2,580 条",
            "增加药品实体类型"
        ));
        return ApiResponse.success(m);
    }

    @PostMapping("/model/version/{version}/rollback")
    public ApiResponse<Map<String, Object>> rollback(@PathVariable String version) {
        // 真实调用：UPDATE model_versions SET is_current=false; UPDATE ... SET is_current=true WHERE version=?
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("version", version);
        m.put("status", "rollback_success");
        m.put("message", "已回滚到 " + version);
        return ApiResponse.success(m);
    }

    @PostMapping("/model/version/{version}/online")
    public ApiResponse<Map<String, Object>> online(@PathVariable String version) {
        // 真实调用：将模型加载到推理服务，更新路由
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("version", version);
        m.put("status", "online");
        m.put("message", version + " 已上线");
        return ApiResponse.success(m);
    }

    // ===== 辅助方法 =====
    private Map<String, Object> typeEval(String type, double p, double r, double f1) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type); m.put("precision", p); m.put("recall", r); m.put("f1", f1);
        return m;
    }

    private Map<String, Object> curvePoint(int epoch, double loss, double acc, double f1) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("epoch", epoch); m.put("loss", loss); m.put("accuracy", acc); m.put("f1", f1);
        return m;
    }

    private List<Object> confusionRow(String label, int... counts) {
        List<Object> row = new ArrayList<>();
        row.add(label);
        for (int c : counts) row.add(c);
        return row;
    }

    private Map<String, Object> metricDiff(String name, double v1, double v2, double diff) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name); m.put("v1", v1); m.put("v2", v2); m.put("diff", diff);
        return m;
    }
}
