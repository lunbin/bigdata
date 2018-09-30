import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueStore;
import java.util.Properties;

public class kafkastream {
    public static void main(String[] args) {
        Properties kafkasteamconfig = new Properties();
        kafkasteamconfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"10.19.248.200:31111");
        kafkasteamconfig.put(StreamsConfig.APPLICATION_ID_CONFIG,"wordcount");
        kafkasteamconfig.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        kafkasteamconfig.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String,String> slbKstream = builder.stream("slb");
//        slbKstream.print();
//
//        KTable<String,Long> slbKtable = slbKstream
//                .flatMapValues(lines -> Arrays.asList(lines.toLowerCase().split("\\W+")))
//                .groupBy((key,word) -> word).count("count");

        slbKstream.mapValues(value -> String.valueOf(value.length())).to("wordscounttopic");
//        slbKtable.toStream().to("wordscounttopic");

        KafkaStreams streams = new KafkaStreams(builder,kafkasteamconfig);
        streams.start();
    }
}
