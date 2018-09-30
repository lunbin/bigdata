package kafkaconsumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class ConsumerTopicLbsheng extends ConsumerTopic {
  //  public ConsumerTopicLbsheng(KafkaConsumer consumer, String topic) {
  //    super(consumer, topic);
  //  }

  @Override
  public void run() {
    while (true) {
      ConsumerRecords<String, String> records = consumer.poll(100);
      for (ConsumerRecord<String, String> record : records) {
        System.out.println(
            topic + "-" + Thread.currentThread().getName() + ":  " + record.value().toString());
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
