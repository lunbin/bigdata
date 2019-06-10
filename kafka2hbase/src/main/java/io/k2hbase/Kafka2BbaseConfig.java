package io.k2hbase;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;
import java.util.Properties;

public class Kafka2BbaseConfig extends AbstractConfig{

  public static final String KAFKA_TOPIC = "kafka.topic";
  public static final String HBASE_TABLE = "hbase.table";

  public static final ConfigDef CONFIG_DEF = baseConfigDef();
  public static ConfigDef baseConfigDef() {
    ConfigDef configDef = new ConfigDef();
    configDef
        .define(KAFKA_TOPIC,
            ConfigDef.Type.STRING,
            "topic",
            null,
            ConfigDef.Importance.HIGH,
            "kafka topic",
            "kafka2hbase",
            4,
            ConfigDef.Width.MEDIUM,
            "kafka topic")
        .define(HBASE_TABLE,
            ConfigDef.Type.STRING,
            "table",
            null,
            ConfigDef.Importance.HIGH,
            "hbase table",
            "kafka2hbase",
            4,
            ConfigDef.Width.MEDIUM,
            "hbase table");
    return configDef;
  }

  public Kafka2BbaseConfig(Properties props) {
    super(CONFIG_DEF, props);
  }
}
