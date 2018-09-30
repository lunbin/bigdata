import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditEvent;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditsSource;

public class WikipediaAnalysis {
  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
    DataStream<WikipediaEditEvent> edits = see.addSource(new WikipediaEditsSource());
    see.setParallelism(2);

    KeyedStream<WikipediaEditEvent, String> keyedEdits =
        edits.keyBy(
            new KeySelector<WikipediaEditEvent, String>() {
              public String getKey(WikipediaEditEvent event) {
                return event.getUser();
              }
            });
    DataStream<Tuple2<String, Long>> result =
        keyedEdits
            .timeWindow(Time.seconds(5))
            .fold(
                new Tuple2<String, Long>("", 0L),
                new FoldFunction<WikipediaEditEvent, Tuple2<String, Long>>() {
                  public Tuple2<String, Long> fold(
                      Tuple2<String, Long> acc, WikipediaEditEvent event) {
                    acc.f0 = event.getUser();
                    acc.f1 += event.getByteDiff();
                    return acc;
                  }
                });
    result
        .map(
            new MapFunction<Tuple2<String, Long>, String>() {
              public String map(Tuple2<String,  Long> tuple) {
                return tuple.toString();
              }
            })
        .addSink(
            new FlinkKafkaProducer011("10.19.248.200:31556", "wiki-result", new SimpleStringSchema()));

    see.execute();
  }
}