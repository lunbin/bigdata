package io.k2hbase.controller;

import io.k2hbase.utils.HbaseUtils;
import io.k2hbase.utils.KafkaUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Kafka2Hbase {
  private KafkaConsumer consumer;
  private static Connection connection;
  private static HBaseAdmin admin;
  private static Configuration configuration;
  private static Logger logger = LoggerFactory.getLogger(Kafka2Hbase.class);
  private static HbaseUtils hbaseUtils;
  private static KafkaUtils kafkaUtils;
  public static final String toHbaseTableDefaultFamily = "defaultFamily";

  static {
    hbaseUtils = new HbaseUtils();
    kafkaUtils = new KafkaUtils();
    configuration = HbaseUtils.initHbaseConfig();
    try {
      connection = ConnectionFactory.createConnection(configuration);
    } catch (IOException e) {
      logger.error("Get hbase connection error", e);
    }
    try {
      admin = (HBaseAdmin) connection.getAdmin();
    } catch (IOException e) {
      logger.error("Connection get HbaseAdmin error.", e);
    }
  }

  private void preOperation(String topic, String hbseTableName) {
    if (consumer == null) {
      consumer = kafkaUtils.getKafkaConsumer();
    }
    consumer.subscribe(Collections.singletonList(topic));
    try {
      if (!admin.tableExists(TableName.valueOf(hbseTableName))) {
        HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(hbseTableName));
        HColumnDescriptor columnDescriptor = new HColumnDescriptor(toHbaseTableDefaultFamily);
        tableDescriptor.addFamily(columnDescriptor);
        logger.info("Creating Table...");
        admin.createTable(tableDescriptor);
        logger.info("Create table {} success", hbseTableName);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void consumerKafkaAndPush2Hbase(String topic, String hbseTableName) {
    logger.info("========= preOperation ==========");
    preOperation(topic, hbseTableName);
    int tag = 0;

    while (true) {
      ArrayList<Put> puts = new ArrayList<>();
      ConsumerRecords<String, String> records = consumer.poll(1000);
      if (records.isEmpty()) {
        if (tag == 0) {
          logger.info("NO message polled from topic: {}", topic);
          tag = 1;
        }
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        continue;
      } else {
        tag = 0;
      }
      logger.info("polled {} message from topic {}", records.count(), topic);
      for (ConsumerRecord<String, String> record : records) {
        Put put = hbaseUtils.generaterPut(record);
        if (put == null) {
          continue;
        }
        puts.add(put);
      }
      logger.info("Put {} messages to hbase", puts.size());
      hbaseUtils.pushMessages2Hbase(puts, topic, connection);
    }
  }
}
