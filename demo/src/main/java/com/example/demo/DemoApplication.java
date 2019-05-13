package com.example.demo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zendesk.maxwell.MaxwellConfig;
import com.zendesk.maxwell.producer.BufferedProducer;
import com.zendesk.maxwell.row.RowMap;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.demo.mapper")
public class DemoApplication implements ApplicationRunner {
  public static Logger logger = LoggerFactory.getLogger(DemoApplication.class);
  public static ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) throws InterruptedException {

//    // springboot test
    SpringApplication.run(DemoApplication.class, args);

////    kafka transaction test
//    KafkaTransactionTest transactionTest = new KafkaTransactionTest();
//    transactionTest.transactionTest();

////    hbase test
//    HbaseTest hbaseTest = new HbaseTest();
//    hbaseTest.createTable("test","personal,professional");
//    hbaseTest.listHbaseTables();
//    hbaseTest.addColumnFamilyForTable("test","age");
//    hbaseTest.deleteColumnFamilyForTable("test","age");
//    hbaseTest.putData2HTable("emp","4");
//    hbaseTest.getDataFromHTableByRowKey("emp","1");
//    hbaseTest.deleteDataFromHTableByRowKey("emp","4");
//    hbaseTest.scanHTable("emp");
//    hbaseTest.countHTable("emp");
//    hbaseTest.exist("emp", "personal data", "city");
//    hbaseTest.putData2HTableByMutator("emp");

//    // maxwell test
//    maxwellTest();

  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    return;
  }

  public static void maxwellTest() {
    logger.info("Start maxwell test...");
    HashMap<String, String> properties = new HashMap<>();
    properties.put("host", "10.19.138.130");
    properties.put("port", "3306");
    properties.put("user", "root");
    properties.put("password", "123456");

    properties.put("client_id", "210905121027");
    properties.put("replica_server_id", "210905121027");
    properties.put("kafka_topic", ""); // no effect for producer is buffer
    properties.put("producer", "buffer");
    properties.put("log_level", "info");
    MaxwellConfig maxwellConfig = MaxWellTest.generateMaxwellConfig(properties);

    try {
      MaxWellTest maxWellTest = new MaxWellTest(maxwellConfig);
      Executors.newSingleThreadExecutor().execute(maxWellTest);
      BufferedProducer producer = (BufferedProducer) maxWellTest.context.getProducer();


      SchemaBuilder builder = SchemaBuilder.struct()
          .field("data_source", Schema.STRING_SCHEMA)
          .field("database", Schema.OPTIONAL_STRING_SCHEMA)
          .field("schema", Schema.OPTIONAL_STRING_SCHEMA)
          .field("table", Schema.OPTIONAL_STRING_SCHEMA)
          .field("data", Schema.OPTIONAL_STRING_SCHEMA)
          .field("directory", Schema.OPTIONAL_STRING_SCHEMA)
          .field("trace", Schema.OPTIONAL_STRING_SCHEMA)
          .field("type", Schema.OPTIONAL_STRING_SCHEMA)
          .field("timestamp", Schema.OPTIONAL_INT64_SCHEMA);

      Schema valueSchema = builder.build();
      Struct valueStruct = new Struct(valueSchema);

      while (true) {
        RowMap row = producer.poll(2000, TimeUnit.MILLISECONDS);
        if (row == null) {
          Thread.sleep(3000);
          logger.info("poll binlog messages is 0");
          continue;
        }
        if ("maxwell".equals(row.getDatabase())) {
          continue;
        }
//        logger.info(mapper.writeValueAsString(row));
//        logger.info("=============================");
        valueStruct.put("data_source", "binlog")
            .put("database", row.getDatabase())
            .put("schema", "null")
            .put("table",row.getTable())
            .put("data",mapper.writeValueAsString(row.getData()))
            .put("directory","null")
            .put("timestamp",row.getTimestampMillis())
            .put("type",row.getRowType());
        logger.info(valueStruct.toString());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
