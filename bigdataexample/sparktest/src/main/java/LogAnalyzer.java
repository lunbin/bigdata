import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;
import java.io.Serializable;

import java.util.Comparator;
import java.util.List;

/**
 * The LogAnalyzer takes in an apache access log file and
 * computes some statistics on them.
 *
 * Example command to run:
 * %  ${YOUR_SPARK_HOME}/bin/spark-submit
 *     --class "com.databricks.apps.logs.chapter1.LogsAnalyzer"
 *     --master local[4]
 *     target/log-analyzer-1.0.jar
 *     ../../data/apache.accesslog
 */

public class LogAnalyzer {
    private static Function2<Long, Long, Long> SUM_REDUCER = (a, b) -> a + b;

    private static class ValueComparator<K, V> implements Comparator<Tuple2<K, V>>, Serializable {
        private Comparator<V> comparator;

        public ValueComparator(Comparator<V> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Tuple2<K, V> o1, Tuple2<K, V> o2) {
            return comparator.compare(o1._2(), o2._2());
        }
    }
    //spark://10.19.248.200:30661
    public static void main(String[] args) {
        //creat spark context
        SparkConf sparkConf = new SparkConf().setJars(new String[] {"target/spark-1.0-SNAPSHOT.jar"})
                .setMaster("spark://10.19.248.200:30661")
                .setAppName("Log Analyzer");

        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        //load text file into spark
//        if (args.length == 0) {
//            System.out.println("Must specify an access logs file.");
//            System.exit(-1);
//        }
//        String logfile = args[0];
        JavaRDD<String> logLines =  sc.textFile("hdfs://10.19.248.200:32425/apache.accesslog");
        // Convert the text log lines to ApacheAccessLog objects and cache them
        //   since multiple transformations and actions will be called on that data.

        JavaRDD<ApacheAccessLog> accessLogs = logLines.map(ApacheAccessLog::parseFromLogLine).repartition(3);
        accessLogs.saveAsTextFile("hdfs://10.19.248.200:32425/tmp/rdd");

        // Calculate statistics based on the content size.
        // Note how the contentSizes are cached as well since multiple actions
        //   are called on that RDD.
        JavaRDD<String> contentSize = accessLogs.map(ApacheAccessLog::getContentSize).cache();
        System.out.println(String.format("Content Size  Min: %s, Max: %s",
                contentSize.min(Comparator.naturalOrder()),
                contentSize.max(Comparator.naturalOrder())));

        // Compute Response Code to Count.
        List<Tuple2<String, Long>> responseCodeToCount =
                accessLogs.mapToPair(log -> new Tuple2<>(log.getResponseCode(), 1L))
                        .reduceByKey(new Function2<Long, Long, Long>() {
                            @Override
                            public Long call(Long v1, Long v2) throws Exception {
                                return v1 + v2;
                            }
                        })
                        .take(100);
        System.out.println(String.format("Response code counts: %s", responseCodeToCount));

        // Any IPAddress that has accessed the server more than 10 times.
        List<String> ipAddresses = accessLogs.mapToPair(log -> new Tuple2<>(log.getIpAddress(), 1L))
                .reduceByKey(SUM_REDUCER)
                .filter(tuple -> tuple._2() > 10)
                .map(Tuple2::_1)
                .take(100);
        System.out.println(String.format("IPAddresses > 10 times: %s", ipAddresses));

        // Top Endpoints.
        List<Tuple2<String, Long>> topEndpoints = accessLogs
                .mapToPair(log -> new Tuple2<>(log.getEndpoint(), 1L))
                .reduceByKey(SUM_REDUCER)
                .top(10, new ValueComparator<>(Comparator.<Long>naturalOrder()));
        System.out.println(String.format("Top Endpoints: %s", topEndpoints));


        // Stop the Spark Context before exiting.
        sc.stop();
    }
}
