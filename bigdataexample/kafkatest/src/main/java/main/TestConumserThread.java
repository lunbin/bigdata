package main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import kafka.utils.ZkUtils;
import kafkaconsumer.ConsumerConfig;
import kafkaconsumer.ConsumerTopic;
import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import utils.KafkaTopicUtils;

public class TestConumserThread {
  private static final String  consumerTopicPackage = "kafkaconsumer";
  private static ZkClient zkClient = null;
  private static ZkUtils zkUtils = null;

  private static KafkaConsumer getKafkaConsumer() {
    Properties props = new Properties();
    InputStream consumerProperties = TestConumserThread.class.getClassLoader().getResourceAsStream("consumer.properties");
    try {
      props.load(consumerProperties);
    } catch (IOException e) {
      e.printStackTrace();
    }
    props.put(ConsumerConfig.BOOTSTRAP_SERVER, props.getProperty(ConsumerConfig.BOOTSTRAP_SERVER));
    props.put(ConsumerConfig.GROUP_ID, props.getProperty(ConsumerConfig.GROUP_ID));
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT, props.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT));
    props.put(ConsumerConfig.COMMIT_INTERVAL_MS, props.getProperty(ConsumerConfig.COMMIT_INTERVAL_MS));
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS, props.getProperty(ConsumerConfig.SESSION_TIMEOUT_MS));
    props.put(ConsumerConfig.KEY_DESERIALIZER, props.getProperty(ConsumerConfig.KEY_DESERIALIZER));
    props.put(ConsumerConfig.VALUE_DESERIALIZER, props.getProperty(ConsumerConfig.VALUE_DESERIALIZER));
    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
    return consumer;
  }

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
    String topicList = "maxwell,lbsheng";
    HashMap<String,Integer> topicPartitionMap = new HashMap<>();
    int totalPartitionCount = 0;

    for (String topic : topicList.split(",")) {
      int partitionNum = getPartitionConuntForTopic(zkhost,topic);
      totalPartitionCount += partitionNum;
      topicPartitionMap.put(topic,partitionNum);
    }
    shutdown();
    if (totalPartitionCount == 0) {
      System.out.println("Error: no topic partition...");
      return;
    }

    /**
     * create thread pool
     */
    ThreadPoolExecutor threadPoolExecutor =
        new ThreadPoolExecutor(
            totalPartitionCount,
            totalPartitionCount + 4,
            200,
            TimeUnit.MICROSECONDS,
            new ArrayBlockingQueue<Runnable>(10));

    for (int i = 0; i < topicList.split(",").length; i++) {
      String topic = topicList.split(",")[i];
      String className =
          consumerTopicPackage
              + ".ConsumerTopic"
              + String.valueOf(topic.charAt(0)).toUpperCase()
              + topic.substring(1);
      for (int j = 0; j < topicPartitionMap.get(topic); j++) {
        try {

          /**
           * according reflect to create ConsumerTopic object
           */
          ConsumerTopic consumerTopic = (ConsumerTopic) Class.forName(className).newInstance();
          consumerTopic.consumer = getKafkaConsumer();
          consumerTopic.topic = topic;

          /**
           * assign topic partition to consumer
           */
          TopicPartition p1 = new TopicPartition(topic, j);
          consumerTopic.consumer.assign(Collections.singletonList(p1));

          /**
           * consumer seek to partition beginning
           */
          consumerTopic.consumer.seekToBeginning(Collections.singletonList(p1));

          /**
           * thread pool exec consumserRunner
           */
          threadPoolExecutor.execute(consumerTopic);

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
    }
    //    ConsumerTopicMaxwell consumerMaxwell = new
    //     ConsumerTopicMaxwell(getKafkaConsumer(),"maxwell");
    //        threadPoolExecutor.execute(consumerMaxwell);
    threadPoolExecutor.shutdown();
  }
}
