import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Producer extends Thread {
  private final KafkaProducer<Integer, String> producer;
  private final String topic;
  private final boolean isAsync;

  public Producer(String topic, boolean isAsync) {
    Properties props = new Properties();
    props.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        KafkaProperties.KAFKA_SERVER_URL);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "DemoProducer");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producer = new KafkaProducer(props);
    this.topic = topic;
    this.isAsync = isAsync;
  }

  public void run() {
    int messageNo = 1;
    while (true) {
      String messageStr = "Message_" + messageNo;
      Long startTime = System.currentTimeMillis();
      if (isAsync) {
        producer.send(
            new ProducerRecord<Integer, String>(topic, messageNo, messageStr),
            new DemoCallBack(startTime, messageNo, messageStr));
      } else {
        try {
          producer.send(new ProducerRecord<Integer, String>(topic, messageNo, messageStr)).get();
          System.out.println("Sent message: (\" + messageNo + \", \" + messageStr + \")");
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      }
      ++messageNo;
    }
  }
}

class DemoCallBack implements Callback {

  private final long startTime;
  private final int key;
  private final String message;

  public DemoCallBack(long startTime, int key, String message) {
    this.startTime = startTime;
    this.key = key;
    this.message = message;
  }

  /**
   * A callback method the user can implement to provide asynchronous handling of request
   * completion. This method will be called when the record sent to the server has been
   * acknowledged. Exactly one of the arguments will be non-null.
   *
   * @param metadata The metadata for the record that was sent (i.e. the partition and offset). Null
   *     if an error occurred.
   * @param exception The exception thrown during processing of this record. Null if no error
   *     occurred.
   */
  public void onCompletion(RecordMetadata metadata, Exception exception) {
    long elapsedTime = System.currentTimeMillis() - startTime;
    if (metadata != null) {
      System.out.println(
          "message("
              + key
              + ", "
              + message
              + ") sent to partition("
              + metadata.partition()
              + "), "
              + "offset("
              + metadata.offset()
              + ") in "
              + elapsedTime
              + " ms");
    } else {
      exception.printStackTrace();
    }
  }
}
