package utils;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import scala.collection.Iterator;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.Properties;

public class KafkaTopicUtils {
  public static ZkUtils getZkUtils(ZkClient zkClient, String zookeeperhost) {
    return new ZkUtils(zkClient, new ZkConnection(zookeeperhost), false);
  }

  public static ZkClient getZkClient(
      String zookeeperhost, int sessionTimeout, int connectTimeout) {
    return new ZkClient(zookeeperhost, sessionTimeout, connectTimeout, ZKStringSerializer$.MODULE$);
  }

  public static void listKafkaTopics(ZkUtils zkUtils) {
    Iterator<String> it = zkUtils.getAllTopics().iterator();
    while (it.hasNext()) {
      System.out.println(it.next());
    }
  }

  public static void createKafkaTopic(
      ZkUtils zkUtils,
      String topicName,
      int numOfPartitions,
      int numOfReplication,
      Properties topicConfiguration) {
    if (AdminUtils.topicExists(zkUtils, topicName)) {
      System.out.println("Topic " + topicName + " is already exist...");
      return;
    }
    AdminUtils.createTopic(
        zkUtils,
        topicName,
        numOfPartitions,
        numOfReplication,
        topicConfiguration,
        RackAwareMode.Enforced$.MODULE$);

    if (AdminUtils.topicExists(zkUtils, topicName)) {
      System.out.println("create kafka topic " + topicName + " successful");
    } else {
      System.out.println("create kafka topic " + topicName + " failed");
    }
    return;
  }

  public static void deleteKafkaTopic(ZkUtils zkUtils, String topicName) {
    if (!AdminUtils.topicExists(zkUtils, topicName)) {
      System.out.println("Topic " + topicName + " is not exist...");
      return;
    }
    AdminUtils.deleteTopic(zkUtils, topicName);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (AdminUtils.topicExists(zkUtils, topicName)) {
      System.out.println("Delete topic " + topicName + " failed...");
    } else {
      System.out.println("Delete topic " + topicName + " successful...");
    }
  }

  public static boolean isHaveTopic(ZkUtils zkUtils, String topicName) {
    if (!AdminUtils.topicExists(zkUtils, topicName)) {
      System.out.println("Topic " + topicName + " is not exist...");
      return false;
    }
    return true;
  }

  public static int getPartitionCountForTopic(ZkUtils zkUtils, String topicName) {
    if (!AdminUtils.topicExists(zkUtils, topicName)) {
      System.out.println("Topic " + topicName + " is not exist...");
      return 0;
    }
    ArrayList topicList = new ArrayList();
    topicList.add(topicName);
    Seq<String> topics = JavaConverters.asScalaBuffer(topicList).toSeq();
    return JavaConverters.mapAsJavaMap(zkUtils.getPartitionAssignmentForTopics(topics))
        .get(topicName)
        .size();
  }

  public static void main(String[] args) {
    ZkClient zkClient = null;
    ZkUtils zkUtils = null;
    try {
      //      String zookeeperhost = "10.19.248.200:31587,10.19.248.200:29152,10.19.248.200:30463";
      String zookeeperhost = "10.19.248.200:32577";

      int sessionTimeout = 15 * 1000;
      int connectTimeout = 10 * 1000;
      zkClient = getZkClient(zookeeperhost, sessionTimeout, connectTimeout);
      zkUtils = getZkUtils(zkClient, zookeeperhost);
      System.out.println(getPartitionCountForTopic(zkUtils, "lbsheng"));

      //      String topicName = "lbsheng-test";
      //      int numOfPartitions = 1;
      //      int numOfReplication = 1;
      //      Properties topicConfiguration = new Properties();
      //
      //      createKafkaTopic(zkUtils, topicName, numOfPartitions, numOfReplication,
      // topicConfiguration);
      //            deleteKafkaTopic(zkUtils,topicName);
    } catch (Exception ex) {
      ex.printStackTrace();

    } finally {
      if (zkClient != null) {
        zkClient.close();
      }
    }
  }
}
