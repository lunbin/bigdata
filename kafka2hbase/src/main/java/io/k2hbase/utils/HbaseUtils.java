package io.k2hbase.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HbaseUtils {
  private static ObjectMapper mapper = new ObjectMapper();
  private static Logger logger = LoggerFactory.getLogger(HbaseUtils.class);

  public static Configuration initHbaseConfig() {
    Configuration configuration =  HBaseConfiguration.create();
    configuration.set("hbase.zookeeper.quorum", "10.19.248.200:31127,10.19.248.200:30761,10.19.248.200:29776");
    configuration.set("zookeeper.znode.parent", "/pre1-hbase");
    return configuration;
  }

  public Put generaterPut(ConsumerRecord<String, String> record) {
    Map recordMap = new HashMap();
    Put put = null;
    try {
      recordMap = mapper.readValue(record.value(), Map.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (recordMap.containsKey("data")) {
      HashMap<String,Object> dataMap = null;
      try {
        dataMap = mapper.readValue(recordMap.get("data").toString(), HashMap.class);
      } catch (IOException e) {
        logger.error(" format data struct to Map error", e);
      }

      if (dataMap.get("uuid") == null) {
        return null;
      }
      String rowKey = dataMap.get("uuid").toString();
      put = new Put(Bytes.toBytes(rowKey));
      for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
        String qualified = entry.getKey();
        Object value = entry.getValue();
        if (value instanceof String) {
          put.addColumn(
              Bytes.toBytes("defaultFamily"), Bytes.toBytes(qualified), Bytes.toBytes(String.valueOf(value)));
        } else if (value instanceof Double) {
          put.addColumn(
              Bytes.toBytes("defaultFamily"), Bytes.toBytes(qualified), Bytes.toBytes((Double) value));
        } else if (value instanceof Integer) {
          put.addColumn(
              Bytes.toBytes("defaultFamily"), Bytes.toBytes(qualified), Bytes.toBytes((Integer) value));
        }
      }
    } else {
      return null;
    }
    return put;
  }

  public void pushMessages2Hbase(
      ArrayList<Put> puts, String tableName, Connection connection) {
    BufferedMutatorParams mutatorParams = new BufferedMutatorParams(TableName.valueOf(tableName));
    BufferedMutator mutator = null;
    try {
      mutator = connection.getBufferedMutator(mutatorParams);
      mutator.mutate(puts);
      mutator.flush();
      mutator.close();
    } catch (IOException e) {
      logger.error("push message to hbase error", e);
    }
  }
}
