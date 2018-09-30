import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import utils.KafkaTopicUtils;

import java.util.ArrayList;
import java.util.Properties;

public class ConsumerBinlog {

  private static final String INSERT = "insert";
  private static final String UPDATE = "update";
  private static final String DELETE = "delete";
  private static final String topic = "maxwell";

  /**
   * parse record json to mysql sql
   * @param json
   * @return String not null
   */
  private static String generateSqlByRecord(JSONObject json) {
    String resSql = null;
    String database = null;
    String table = null;
    String type = null;
    String data = null;
    String old = null;
    if (json == null) {
      return null;
    }
    try {
      database = json.get("database").toString();
      table = json.getString("table").toString();
      type = json.get("type").toString();
      data = json.getString("data").toString();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    switch (type) {
      case INSERT:
        String insertData = "";
        String temInsertData = data.replace("{", "").replace("}", "");
        String[] temInsertDataArray = temInsertData.split(",");
        for (int i = 0; i < temInsertDataArray.length; i++) {
          insertData = insertData + temInsertDataArray[i].split(":")[1];
          if (i != temInsertDataArray.length - 1) {
            insertData = insertData + ",";
          }
        }
        resSql = "insert into " + database + "." + table + " values(" + insertData + ")";
        break;
      case DELETE:
        String deleteData = "";
        String temDelData = data.replace("{", "").replace("}", "");
        String[] temDelDataArray = temDelData.split(",");
        for (int i = 0; i < temDelDataArray.length; i++) {
          deleteData =
              deleteData
                  + temDelDataArray[i].split(":")[0].replace("\"", "")
                  + "="
                  + temDelDataArray[i].split(":")[1];
          if (i != temDelDataArray.length - 1) {
            deleteData = deleteData + " and ";
          }
        }
        resSql = "delete from " + database + "." + table + " where " + deleteData;
        break;
      case UPDATE:
        try {
          old = json.getString("old").toString().replace("{", "").replace("}", "");
        } catch (JSONException e) {
          old = null;
          break;
        }
        String temUpdData = data.replace("{", "").replace("}", "");
        String[] temUpdDataArray = temUpdData.split(",");
        String[] temOldArray = old.split(",");
        String updSetData = "";
        String updWhereData = "";
        int i = 0, j = 0;
        while (i < temOldArray.length && j < temUpdDataArray.length) {
          if (temOldArray[i].split(":")[0].equals(temUpdDataArray[j].split(":")[0])) {
            updSetData = updSetData + temUpdDataArray[j];
            updWhereData = updWhereData + temOldArray[i];
            if (i < temOldArray.length - 1) {
              updSetData = updSetData + ",";
            }

            if (j < temUpdDataArray.length - 1) {
              updWhereData = updWhereData + ",";
            }
            i++;
            j++;
          } else {
            updWhereData = updWhereData + temUpdDataArray[j];
            if (j < temUpdDataArray.length - 1) {
              updWhereData = updWhereData + ",";
            }
            j++;
          }
        }
        while (j < temUpdDataArray.length) {
          updWhereData = updWhereData + temUpdDataArray[j];
          if (j < temUpdDataArray.length - 1) {
            updWhereData = updWhereData + ",";
          }
          j++;
        }

        String[] updSetDataArray = updSetData.split(",");
        String[] updWhereDataArray = updWhereData.split(",");
        String resUpdSetData = "";
        String resUpdWhereData = "";
        for (i = 0; i < updSetDataArray.length; i++) {
          resUpdSetData =
              resUpdSetData
                  + updSetDataArray[i].split(":")[0].replace("\"", "")
                  + "="
                  + updSetDataArray[i].split(":")[1];
          if (i < updSetDataArray.length - 1) {
            resUpdSetData = resUpdSetData + ",";
          }
        }
        for (j = 0; j < updWhereDataArray.length; j++) {
          resUpdWhereData =
              resUpdWhereData
                  + updWhereDataArray[j].split(":")[0].replace("\"", "")
                  + "="
                  + updWhereDataArray[j].split(":")[1];
          if (j < updWhereDataArray.length - 1) {
            resUpdWhereData = resUpdWhereData + " and ";
          }
        }
        resSql =
            "update "
                + database
                + "."
                + table
                + " set "
                + resUpdSetData
                + " where "
                + resUpdWhereData;
        break;
      default:
        return null;
    }
    return resSql;
  }

  public static void main(String[] args) {
    String zookeeperhost = "10.19.248.200:32577";
    int sessionTimeout = 15 * 1000;
    int connectTimeout = 10 * 1000;

    /**
     * get kafka config, replace with parse config file (props.load(InputStreaming)).
     */
    Properties props = new Properties();
    props.put("bootstrap.servers", "10.19.248.200:31561,10.19.248.200:31923,10.19.248.200:31824");
    props.put("group.id", "binlog");
    props.put("enable.auto.commit", "true");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

    /**
     * get partition nums for topic.
     */

    ZkClient zkClient = KafkaTopicUtils.getZkClient(zookeeperhost,sessionTimeout,connectTimeout);;
    ZkUtils zkUtils = KafkaTopicUtils.getZkUtils(zkClient,zookeeperhost);
    int partitionCountForTopic = KafkaTopicUtils.getPartitionCountForTopic(zkUtils,topic);

    /**
     * assin topic partition for consumer.
     */
    ArrayList<TopicPartition> partitions = new ArrayList<TopicPartition>();
    for (int i=0; i<partitionCountForTopic;i++) {
      TopicPartition t = new TopicPartition(topic, i);
      partitions.add(t);
    }
    consumer.assign(partitions);
    System.out.println(consumer.assignment());

    /**
     * seek to partition beginning offset.
     */
    consumer.seekToBeginning(partitions);
    JSONObject jsonObject = null;

    /**
     * poll topic record.
     */
    while (true) {
      ConsumerRecords<String, String> records = consumer.poll(100);
      for (ConsumerRecord<String, String> record : records) {
        try {
          jsonObject = new JSONObject(record.value());
          if ("maxwell".equals(jsonObject.get("database").toString())) {
            continue;
          }

          if ("maxwells".equals(jsonObject.get("database").toString())) {
            continue;
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }

        /**
         * parse record to mysql sql
         */
        String sql = generateSqlByRecord(jsonObject);
        System.out.println(sql);
      }
    }
  }
}
