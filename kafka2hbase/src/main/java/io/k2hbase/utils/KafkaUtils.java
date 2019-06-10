package io.k2hbase.utils;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

public class KafkaUtils {

  public KafkaConsumer getKafkaConsumer() {
    Properties consumerProperties = new Properties();

    consumerProperties.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.19.248.200:31297,10.19.248.200:32425,10.19.248.200:31335");
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "k2hbase"+ System.currentTimeMillis());
    consumerProperties.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    consumerProperties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    consumerProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
    consumerProperties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProperties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(consumerProperties);
    return consumer;
  }
}
