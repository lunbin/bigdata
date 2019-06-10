import io.k2hbase.Kafka2BbaseConfig;
import io.k2hbase.controller.Kafka2Hbase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class MainApplication {
  private static Logger logger = LoggerFactory.getLogger(Kafka2Hbase.class);
  public static Kafka2BbaseConfig kafka2HbaseConfig = new Kafka2BbaseConfig(loadProperties("application.property"));

  public static void main(String[] args) {


    Properties properties = loadProperties("application.property");

    Class<?> clazz = Kafka2BbaseConfig.class;
    Method[] methods = clazz.getMethods();
    for (Method method : methods) {
      try {
        method.invoke(new Kafka2BbaseConfig(properties));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    // hard code
//    String topic = "focuspeopleinfo";
//    String hbaseTableName = "focuspeopleinfo";

    // load config file by Properties
//    String topic = properties.getProperty("kafka.topic");
//    String hbaseTableName = properties.getProperty("hbase.table");

    // AbstractConfig ConfigDef
    String topic = kafka2HbaseConfig.getString(Kafka2BbaseConfig.KAFKA_TOPIC);
    String hbaseTableName = kafka2HbaseConfig.getString(Kafka2BbaseConfig.HBASE_TABLE);
    Kafka2Hbase kafka2Hbase = new Kafka2Hbase();

    logger.info("Start consumer kafka message put to hbase");
    kafka2Hbase.consumerKafkaAndPush2Hbase(topic, hbaseTableName);
  }

  private static Properties loadProperties(String propertiesFile) {
    Properties properties = new Properties();
    try {

      // load classpath config file
      properties.load(MainApplication.class.getResourceAsStream(propertiesFile));

      // load config file (absolute papth)
      //      properties.load(new FileInputStream(new File(propertiesFile)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return properties;
  }
}
