package com.kgplatform.middleware;

import com.kgplatform.config.MiddlewareProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Kafka 客户端封装：包含真实调用代码，本地未安装时优雅降级。
 * <p>
 * 调用规则：
 * 1. 若 kg-platform.middleware.kafka.enabled=false（默认），跳过 Producer 初始化。
 * 2. 若 enabled=true 但连接失败，捕获异常并降级。
 * 3. 真实调用使用 {@link KafkaProducer#send(ProducerRecord)}。
 */
@Slf4j
@Component
public class KafkaClient {

    private final MiddlewareProperties properties;
    private KafkaProducer<String, String> producer;
    private boolean available = false;

    @Autowired
    public KafkaClient(MiddlewareProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        MiddlewareProperties.KafkaConfig cfg = properties.getKafka();
        if (!cfg.isEnabled()) {
            log.warn("Kafka 未启用（kg-platform.middleware.kafka.enabled=false），消息将降级为日志输出");
            return;
        }
        try {
            // === 真实 Kafka 调用代码 ===
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.getBootstrapServers());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            props.put(ProducerConfig.RETRIES_CONFIG, 3);
            producer = new KafkaProducer<>(props);
            available = true;
            log.info("Kafka Producer 已就绪：{}", cfg.getBootstrapServers());
        } catch (Exception e) {
            log.error("Kafka 初始化失败，降级运行：{}", e.getMessage());
            available = false;
        }
    }

    public boolean isAvailable() {
        return available && producer != null;
    }

    /**
     * 发送消息到指定 topic。
     * 真实调用：producer.send(record)
     * 降级：打印日志
     */
    public Future<org.apache.kafka.clients.producer.RecordMetadata> send(String topic, String key, String value) {
        if (!isAvailable()) {
            log.info("[Kafka 降级] topic={}, key={}, value={}", topic, key, value);
            return null;
        }
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            return producer.send(record, (metadata, e) -> {
                if (e != null) {
                    log.error("Kafka 发送失败：{}", e.getMessage());
                } else {
                    log.debug("Kafka 发送成功：topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Kafka 发送异常：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 发送抽取任务消息。
     */
    public void sendExtractionTask(String taskId, String payload) {
        send("kg-extraction-tasks", taskId, payload);
    }

    /**
     * 发送训练任务消息。
     */
    public void sendTrainingTask(String taskId, String payload) {
        send("kg-training-tasks", taskId, payload);
    }

    /**
     * 发送数据导入消息。
     */
    public void sendImportTask(String taskId, String payload) {
        send("kg-import-tasks", taskId, payload);
    }

    public void close() {
        if (producer != null) {
            try { producer.close(); } catch (Exception ignored) {}
        }
    }
}
