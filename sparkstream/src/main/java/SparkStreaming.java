import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import com.beust.jcommander.JCommander;
import entity.KafkaSender;
import entity.MyParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

public class SparkStreaming {
  public static void main(String[] args) throws InterruptedException {

    SparkConf sparkConf = new SparkConf();
    MyParameters parameters = new MyParameters();
    JCommander jc = new JCommander(parameters, args);
    if ("test".equals(parameters.env)) {
      sparkConf.setMaster("local[2]").setAppName("sparkstring-kafka");
    } else {
      sparkConf.setAppName("sparkstring-kafka");
    }

    JavaStreamingContext jssc =
        new JavaStreamingContext(sparkConf, Durations.seconds(parameters.batchInterval));
    Broadcast<MyParameters> sparkParameter = jssc.sparkContext().broadcast(parameters);

    JavaInputDStream<ConsumerRecord<String, String>> kafkaDirectDStream =
        getKafkaDStreaming(parameters, jssc);

    //    JavaDStream lines = kafkaDirectDStream.map(v1 -> {return v1.value();});
    Broadcast<MyParameters> sparkParameters = jssc.sparkContext().broadcast(parameters);

    //    kafkaDirectDStream.mapPartitions(new
    // FlatMapFunction<Iterator<ConsumerRecord<String,String>>, Object>() {
    //      @Override
    //      public Iterator<Object> call(Iterator<ConsumerRecord<String, String>>
    // consumerRecordIterator) throws Exception {
    //        sendData(consumerRecordIterator);
    //        return null;
    //      }
    //    });

//    KafkaProducer producer = getProducer(parameters);
//    KafkaSender kafkaSender = KafkaSender.getInstance(parameters);
//    Broadcast<KafkaSender> kafkaSenderBroadcast = jssc.sparkContext().broadcast(kafkaSender);

    kafkaDirectDStream.foreachRDD(new VoidFunction2<JavaRDD<ConsumerRecord<String, String>>, Time>() {
      private static final long serialVersionUID = -2703874190091109593L;
      @Override
      public void call(JavaRDD<ConsumerRecord<String, String>> v1, Time v2) throws Exception {
        v1.foreachPartition((VoidFunction<Iterator<ConsumerRecord<String, String>>>) consumerRecordIterator -> sendData(sparkParameter,consumerRecordIterator));
      }
    });
//    kafkaDirectDStream.foreachRDD(
//        (VoidFunction2<JavaRDD<ConsumerRecord<String, String>>, Time>)
//            (v1, v2) -> {
//              v1.foreachPartition(
//                  (VoidFunction<Iterator<ConsumerRecord<String, String>>>)
//                      iter -> {
//                        sendData(sparkParameter, producer, iter);
//                        return;
//                      });

              //          VoidFunction vf =
              //              (VoidFunction<Iterator<ConsumerRecord<String, String>>>)
              // SparkStreaming::sendData;
              //          v1.foreachPartition(vf); // 这里才会调用sendData方法
//            });
    jssc.start();
    jssc.awaitTermination();
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

    return new KafkaProducer(properties);
  }

  private static void sendData(
      Broadcast<MyParameters> sparkParameter,
      Iterator<ConsumerRecord<String, String>> iter) {
    KafkaSender sender = KafkaSender.getInstance(sparkParameter.getValue());
    while (iter.hasNext()) {
      ConsumerRecord<String, String> record = iter.next();
      System.out.println("=======================");
      System.out.println(
          "topic: "
              + record.topic()
              + " partition: "
              + record.partition()
              + " offset:"
              + record.offset());
      System.out.println(record.value());
      System.out.println("=========================");

      sender.sendData(sparkParameter.value().targetTopic, record.value());
    }
  }

  private static JavaInputDStream<ConsumerRecord<String, String>> getKafkaDStreaming(
      MyParameters parameters, JavaStreamingContext jssc) {
    Map<String, Object> kafkaParams = new HashMap<>();
    kafkaParams.put("bootstrap.servers", parameters.kafkaBootstrap);
    kafkaParams.put("key.deserializer", StringDeserializer.class);
    kafkaParams.put("value.deserializer", StringDeserializer.class);
    kafkaParams.put("group.id", parameters.kafkaGroupId);
    kafkaParams.put("auto.offset.reset", parameters.autoOffsetReset);
    kafkaParams.put("enable.auto.commit", true);

    Collection<String> topics = Collections.singletonList(parameters.topics);

    return KafkaUtils.createDirectStream(
        jssc,
        LocationStrategies.PreferConsistent(),
        ConsumerStrategies.Subscribe(topics, kafkaParams));
  }
}
