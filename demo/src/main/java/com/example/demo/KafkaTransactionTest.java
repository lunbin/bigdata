package com.example.demo;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaTransactionTest {

  public static Logger logger = LoggerFactory.getLogger(DemoApplication.class);

  public void transactionTest() {
    KafkaProducer producer = getKafkaProducer();
    KafkaConsumer consumer = getKafkaConsumer();
    System.out.println("start consumer kafka record per 5s");
    while (true) {
      // 初始化事务（需要设置transaction.id）
//			producer.initTransactions();

      Map<TopicPartition, OffsetAndMetadata> commits = new HashMap<>();
      ConsumerRecords<String,String> records = consumer.poll(Duration.ofSeconds(10));


      logger.info("time is: " + new Date());

      try {
        Thread.sleep(20000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      if (records.isEmpty()) {
        continue;
      }
      consumer.commitSync();

// kafka 0.11 以上使用

//			try{
//				for (ConsumerRecord record : records) {
//					// 记录事务<topicpartition, offset>
//					commits.put(new TopicPartition(record.topic(),record.partition()),new OffsetAndMetadata(record.offset()));
//
//					// producer send message
//					producer.send(new ProducerRecord("topic", "message"));
//				}
//				// 提交偏移量
//				producer.sendOffsetsToTransaction(commits, "comsumergroup");
//
//				// 事务提交
//				producer.commitTransaction();
//
//			} catch (Exception e) {
//				// 放弃事务
//				producer.abortTransaction();
//			}


    }
  }

  public KafkaProducer<String, String> getKafkaProducer() {
    Properties producerProperties = new Properties();
    producerProperties.put("bootstrap.servers", "10.19.248.200:31297");
    producerProperties.put(ProducerConfig.ACKS_CONFIG, "all");
    producerProperties.put(ProducerConfig.RETRIES_CONFIG, 0);
    producerProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
    producerProperties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
    producerProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

    // for kafka transaction
//		producerProperties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,"first-transactiona");
//		producerProperties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,"true");
//		producerProperties.put(ProducerConfig.ACKS_CONFIG, "all");
//		producerProperties.put(ProducerConfig.RETRIES_CONFIG,"1");



    producerProperties.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    producerProperties.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    KafkaProducer<String, String> producer = new KafkaProducer<String, String>(producerProperties);
    return producer;
  }

  public static KafkaConsumer getKafkaConsumer() {
    Properties consumerProperties = new Properties();
    consumerProperties.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.19.248.200:31297");
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "slb");
    consumerProperties.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//		consumerProperties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    consumerProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
    consumerProperties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProperties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringDeserializer");

    // for kafka transactions
//    consumerProperties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG,"read_committed");
    consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");


    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(consumerProperties);
    String topic = "lbsheng";
    consumer.subscribe(Arrays.asList(topic));
    return consumer;
  }


}
