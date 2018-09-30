import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KafkaProperties {
    public static final String TOPIC = "lbsheng";
    public static final String CONSUMER_GROUP_ID = "cg2";
    public static final String KAFKA_SERVER_URL = "10.19.248.200:31561,10.19.248.200:31923,10.19.248.200:31824";
    public static final String KAFKA_BROKER_HOST = "10.19.248.200";
    public static final int KAFKA_SERVER_PORT = 30443;
    public static final int KAFKA_PRODUCER_BUFFER_SIZE = 64 * 1024;
    public static final int CONNECTION_TIMEOUT = 100000;
    public static final String TOPIC2 = "topic2";
    public static final String TOPIC3 = "topic3";
    public static final String CLIENT_ID = "SimpleConsumerDemoClient";

    public KafkaProperties() {
//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("kafka.properties");
//        Properties p = new Properties();
//        try {
//            p.load(inputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(p.getProperty("topic"));
    }
}
