import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

public class JavaNetworkWordCount {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("NetworkWordCount").setMaster("local[2]");
//        SparkSession sss = SparkSession.builder().master("").appName("").config(sparkConf).getOrCreate();
//        sss.sparkContext();
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(5));
        JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost",9999);
        JavaDStream<String> words = lines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());
        JavaPairDStream<String,Integer> pairs = words.mapToPair(word -> new Tuple2<String,Integer>(word,1));
        JavaPairDStream<String,Integer> wordcounts = pairs.reduceByKey((p1,p2) -> p1 + p2);

        // print the ten elements of echo RDD generated in this DStream to the console
        wordcounts.print();

        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jssc.stop(false);

    }
}
