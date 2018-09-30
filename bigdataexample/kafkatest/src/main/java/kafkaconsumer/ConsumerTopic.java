package kafkaconsumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class ConsumerTopic implements Runnable {
  public KafkaConsumer<String,String> consumer = null;
  public String topic = null;

  public ConsumerTopic() {}
  public ConsumerTopic(KafkaConsumer consumer,String topic) {
    this.consumer = consumer;
    this.topic = topic;
  }
  @Override
  public void run() {
    System.out.println("Parent class");
  }
}
