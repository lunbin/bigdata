package entity;

import java.io.Serializable;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaSender implements Serializable{
  private static KafkaProducer producer;
  private static volatile KafkaSender kafkaSender;

  private KafkaSender(KafkaProducer producer) {
    this.producer = producer;
  }

  public static KafkaSender getInstance(MyParameters parameters) {
    if (kafkaSender == null) {
      synchronized (KafkaSender.class) {
        if (kafkaSender == null) {
          producer = getProducer(parameters);
          return new KafkaSender(producer);
        }
      }
    }
    return kafkaSender;
  }

  private static KafkaProducer getProducer(MyParameters parameters) {
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, parameters.kafkaBootstrap);
    properties.put(ProducerConfig.ACKS_CONFIG, "all");
    properties.put(ProducerConfig.RETRIES_CONFIG, 0);
    properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
    properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
    properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
    properties.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    properties.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");

    KafkaProducer producer = new KafkaProducer(properties);
    return producer;
  }

  public void sendData(String topic,String message) {
    producer.send(new ProducerRecord<String,String>(topic, message));
  }
}
