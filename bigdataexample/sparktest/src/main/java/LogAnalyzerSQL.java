import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.*;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import scala.Tuple2;


public class LogAnalyzerSQL {

    public static void main(String[] args) {
        //Initialize the Spark context.
        SparkConf sparkConf = new SparkConf().setJars(new String[] {"target/spark-1.0-SNAPSHOT.jar"});
        SparkSession spark = SparkSession
                .builder().config(sparkConf)
                .master("spark://10.19.248.200:30661")
                .appName("Java Spark SQL basic example")
                .getOrCreate();

        JavaRDD<ApacheAccessLog> accesslogs = spark.read().textFile("hdfs://10.19.248.200:32425/apache.accesslog").javaRDD().map(ApacheAccessLog::parseFromLogLine);

        // Apply a schema to an RDD of JavaBeans to get a DataFrame
        Dataset<Row> accesslogDF = spark.createDataFrame(accesslogs,ApacheAccessLog.class);

        // Register the DataFrame as a temporary view
        accesslogDF.createOrReplaceTempView("logs");

        Dataset<Row> contentSizestate = spark.sql("SELECT SUM(contentSize), COUNT(*), MIN(contentSize), MAX(contentSize) FROM logs");
        contentSizestate.show();

        //spark.sql("select * from logs").show();

        // Compute Response Code to Count.
        spark.sql("SELECT responseCode, COUNT(*) FROM logs GROUP BY responseCode LIMIT 100").show();

        //Any IPAddress that has accessed the server more than 10 times.
        spark.sql("SELECT ipAddress, COUNT(*) AS total FROM logs GROUP BY ipAddress HAVING total > 10 LIMIT 100")
                .map((MapFunction<Row, String>) row -> row.getString(0), Encoders.STRING()).show();


        // Top Endpoints.
        spark.sql("SELECT endpoint, COUNT(*) AS total FROM logs GROUP BY endpoint ORDER BY total DESC LIMIT 10")
                .map((MapFunction<Row, String>) row -> row.getString(0), Encoders.STRING()).show();
    }
}
