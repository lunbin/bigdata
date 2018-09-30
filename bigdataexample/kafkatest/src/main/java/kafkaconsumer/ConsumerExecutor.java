package kafkaconsumer;

import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import utils.KafkaTopicUtils;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class KafkaConsumerRunner implements Runnable {
  private AtomicBoolean closed = new AtomicBoolean(false);
  private KafkaConsumer consumer = null;
  private String topic = null;

  public KafkaConsumerRunner(KafkaConsumer consumer, String topic) {
    this.consumer = consumer;
    this.topic = topic;
  }

  public void shutdown() {
    closed.set(true);
    consumer.wakeup();
  }

  @Override
  public void run() {
    try {
      while (!closed.get()) {
        ConsumerRecords<String, String> records = consumer.poll(10000);
        for (ConsumerRecord<String, String> record : records) {
          System.out.println(record.value());
        }
      }

    } catch (WakeupException e) {
      if (!closed.get()) {
        throw e;
      }
    } finally {
      consumer.close();
    }
  }
}

class KafkaTopicConsumer {
  private KafkaConsumer consumer = null;

  public KafkaTopicConsumer(KafkaConsumer consumer, String topic) {
    this.consumer = consumer;
  }

  public static KafkaConsumer getTopicConsumer() {
    Properties props = new Properties();
    props.put("bootstrap.servers", "10.19.248.200:31561,10.19.248.200:31923,10.19.248.200:31824");
    props.put("group.id", "binlog");
    props.put("enable.auto.commit", "true");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
    return consumer;
  }
}

public class ConsumerExecutor {
  private static ZkClient zkClient = null;
  private static ZkUtils zkUtils = null;

  public static int getPartitionConuntForTopic(String zkhost, String topic) {
    int sessionTimeout = 15 * 1000;
    int connectTimeout = 10 * 1000;
    zkClient = KafkaTopicUtils.getZkClient(zkhost, sessionTimeout, connectTimeout);
    zkUtils = KafkaTopicUtils.getZkUtils(zkClient, zkhost);
    return KafkaTopicUtils.getPartitionCountForTopic(zkUtils, topic);
  }

  public static void shutdown() {
    if (zkUtils != null) {
      zkUtils.close();
    }

    if (zkClient != null) {
      zkClient.close();
    }
  }

  public static void main(String[] args) {
    String zkhost = "10.19.248.200:32577";
    String topic = "maxwell";

    int partitionCount = getPartitionConuntForTopic(zkhost, topic);
    shutdown();

    ThreadPoolExecutor threadPoolExecutor =
        new ThreadPoolExecutor(3, 6, 200, TimeUnit.MICROSECONDS, new ArrayBlockingQueue(10));

    for (int i = 0; i < partitionCount; i++) {
      KafkaConsumer<String, String> consumer = KafkaTopicConsumer.getTopicConsumer();
      TopicPartition tp = new TopicPartition(topic, i);
      consumer.assign(Collections.singletonList(tp));
      KafkaConsumerRunner consumerRunner = new KafkaConsumerRunner(consumer, topic);
      threadPoolExecutor.execute(consumerRunner);
    }

    shutdown();
  }
}
