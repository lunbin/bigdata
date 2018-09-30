import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public class LogAnalyzerStreamingSQL {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[2]").setAppName("Log Analyzer Streaming SQL");
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(javaSparkContext, Durations.seconds(5));

        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        JavaReceiverInputDStream<String> logDataDStream = javaStreamingContext.socketTextStream("localhost",9999);
        JavaDStream<ApacheAccessLog> accessLogDStream = logDataDStream.map(ApacheAccessLog::parseFromLogLine).cache();
        JavaDStream<ApacheAccessLog>  windowlogDStream = accessLogDStream.window(Durations.seconds(30),Durations.seconds(10));

        windowlogDStream.foreachRDD(accesslog -> {
            if (accesslog.count() == 0) {
                System.out.println("No access logs in this time interval");
                return;
            }
            Dataset<Row> accesslogDF = spark.createDataFrame(accesslog,ApacheAccessLog.class);
            accesslogDF.createOrReplaceTempView("logs");
            spark.sql("SELECT SUM(contentSize), COUNT(*), MIN(contentSize), MAX(contentSize) FROM logs").show();
            spark.sql("SELECT ipAddress, COUNT(*) AS total FROM logs GROUP BY ipAddress HAVING total > 10 LIMIT 100")
                    .map((MapFunction<Row, String>) row -> row.getString(0), Encoders.STRING()).show();
            spark.sql("SELECT endpoint, COUNT(*) AS total FROM logs GROUP BY endpoint ORDER BY total DESC LIMIT 10")
                    .map((MapFunction<Row, String>) row -> row.getString(0), Encoders.STRING()).show();
        });

        javaStreamingContext.start();
        try {
            javaStreamingContext.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
