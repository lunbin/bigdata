import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import java.util.*;
import scala.Tuple2;


public class JavaDirectKafkaWordCount {
//    private static final Pattern SPACE = Pattern.compile(" ");
    public static void main(String[] args) throws Exception {
        String brokers = "10.19.248.200:31792,10.19.248.200:31376,10.19.248.200:31761";
        String topics = "sparkstreamkafka";

        System.out.println(System.getProperty("user.dir"));
        SparkConf sparkConf = new SparkConf().setJars(new String[] {"target/sparktest-1.0-SNAPSHOT.jar"}).setAppName("JavaDirectKafkaWordCount").setMaster("spark://10.19.248.200:30428");
        JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, Durations.seconds(2));

        Set<String> topicsSet = new HashSet<>(Arrays.asList(topics.split(",")));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("metadata.broker.list",brokers);
        kafkaParams.put("bootstrap.servers",brokers);
        kafkaParams.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("key.deserializer","org.apache.kafka.common.serialization.IntegerDeserializer");
        kafkaParams.put("group.id","spstreamkafka");

        // Create direct kafka stream with brokers and topics
        JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(
                jsc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topicsSet, kafkaParams));

        // Get the lines, split them into words, count the words and print
        JavaDStream<String> lines = messages.map(ConsumerRecord::value);
        JavaDStream<String> words = lines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());
        JavaPairDStream<String, Integer> wordCounts = words.mapToPair(s -> new Tuple2<>(s, 1))
                .reduceByKey((i1, i2) -> i1 + i2);
        wordCounts.print();

        // Start the computation
        jsc.start();
        jsc.awaitTermination();
    }
}
