import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import scala.collection.Iterator;

import java.util.Properties;

public class ZookeeperTest {

    private static ZkUtils getZkUtils(ZkClient zkClient, String zookeeperhost) {
        return new ZkUtils(zkClient,new ZkConnection(zookeeperhost),false);
    }

    private static ZkClient getZkClient(String zookeeperhost, int sessionTimeout, int connectTimeout) {
        return new ZkClient(zookeeperhost,sessionTimeout,connectTimeout, ZKStringSerializer$.MODULE$);
    }

    private static void listKafkaTopics(ZkUtils zkUtils) {
        Iterator<String> it = zkUtils.getAllTopics().iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    private static void createKafkaTopic(ZkUtils zkUtils, String topicName, int numOfPartitions, int numOfReplication, Properties topicConfiguration) {
        if (AdminUtils.topicExists(zkUtils,topicName)) {
            System.out.println("Topic " + topicName +  " is already exist...");
            return;
        }
        AdminUtils.createTopic(zkUtils,topicName,numOfPartitions,numOfReplication,topicConfiguration, RackAwareMode.Enforced$.MODULE$);

        if (AdminUtils.topicExists(zkUtils,topicName)) {
            System.out.println("create kafka topic " + topicName + " successful");
        } else {
            System.out.println("create kafka topic " + topicName + " failed");
        }
        return;
    }

    private static void deleteKafkaTopic(ZkUtils zkUtils,String topicName) {
        if ( !AdminUtils.topicExists(zkUtils,topicName)) {
            System.out.println("Topic " + topicName +  " is not exist...");
            return;
        }
        AdminUtils.deleteTopic(zkUtils,topicName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if ( AdminUtils.topicExists(zkUtils,topicName)) {
            System.out.println("Delete topic " + topicName +  " failed...");
        } else {
            System.out.println("Delete topic " + topicName +  " successful...");
        }
    }
    public static void main(String[] args) {
        ZkClient zkClient = null;
        ZkUtils zkUtils = null;
        try {
            String zookeeperhost = "10.19.248.200:31587,10.19.248.200:29152,10.19.248.200:30463";
            int sessionTimeout = 15 * 1000;
            int connectTimeout = 10 * 1000;
            zkClient = getZkClient(zookeeperhost, sessionTimeout, connectTimeout);
            zkUtils = getZkUtils(zkClient, zookeeperhost);
            String topicName = "lbsheng-test";
            int numOfPartitions = 1;
            int numOfReplication = 1;
            Properties topicConfiguration = new Properties();

            createKafkaTopic(zkUtils, topicName, numOfPartitions, numOfReplication, topicConfiguration);
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
