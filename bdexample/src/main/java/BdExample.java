import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import scala.Tuple2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class BdExample {
  private Properties properties;

  /**
   * init config Properties.
   *
   * @param configfile
   */
  public void initProperties(String configfile) {
    properties = new Properties();
    try (InputStream inputStream = new FileInputStream(new File(configfile))) {
      properties.load(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void executeExample(String target) {
    switch (target) {
      case "zookeeper":
        zk_example();
        break;
      case "kafka":
        kafka_example();
        break;
      case "hdfs":
        hdfs_example();
        break;
      case "hbase":
        hbase_example();
        break;
      case "spark":
        spark_example();
        break;
      case "redis":
        redis_example();
        break;
      default:
        System.out.println("Error: not support!");
        break;
    }
  }

  private void redis_example() {
    String rediscluster_url = properties.getProperty("rediscluster_url");
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxIdle(1);
    config.setMaxTotal(1);
    config.setMaxWaitMillis(1000);
    HashSet<HostAndPort> hostAndPorts = new HashSet<HostAndPort>();

    for (int i = 0; i < rediscluster_url.split(",").length; i++) {
      String url = rediscluster_url.split(",")[i];
      String host = url.split(":")[0];
      int port = Integer.parseInt(url.split(":")[1]);
      hostAndPorts.add(new HostAndPort(host, port));
    }
    JedisCluster jedisCluster = new JedisCluster(hostAndPorts, config);
    System.out.println("set name:helium");
    jedisCluster.set("name", "helium");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("get name: ");
    System.out.println(jedisCluster.get("name"));
  }

  private void spark_example() {
    Pattern SPACE = Pattern.compile(" ");
    String spark_master = properties.getProperty("spark_master");
    SparkConf sparkConf = new SparkConf()
        .setMaster(spark_master)
        .setJars(new String[]{"/opt/bdexample.jar"})
        .setAppName("JavaWordCount");
    SparkSession spark =
        SparkSession.builder().config(sparkConf).getOrCreate();

    JavaRDD<String> lines = spark.read().textFile("example.config").javaRDD();
    JavaRDD<String> words = lines.flatMap(s -> Arrays.asList(SPACE.split(s)).iterator());
    JavaPairRDD<String, Integer> ones = words.mapToPair(s -> new Tuple2<>(s, 1));
    JavaPairRDD<String, Integer> counts = ones.reduceByKey((i1, i2) -> i1 + i2);

    List<Tuple2<String, Integer>> output = counts.collect();
    for (Tuple2<?,?> tuple : output) {
      System.out.println(tuple._1() + ": " + tuple._2());
    }
    spark.stop();
  }

  private void hbase_example() {
    TableName hbaseTable = TableName.valueOf("helium1");
    String cloumnStr = "cf1,cf2,cf3";

    Configuration configuration = null;
    Admin admin = null;
    configuration = HBaseConfiguration.create();
    String zkservers = properties.getProperty("hbase_zookeeper_quorum");
    String zknode = properties.getProperty("zookeeper_znode_parent");
    configuration.set("hbase.zookeeper.quorum", zkservers);
    configuration.set("zookeeper.znode.parent", zknode);
    List<String> columnFamilies = Arrays.asList(cloumnStr.split(","));
    try {
      Connection connection = ConnectionFactory.createConnection(configuration);
      admin = connection.getAdmin();
      if (admin.tableExists(hbaseTable)) {
        System.out.println("Table " + hbaseTable + " already exist......");
        System.out.println("delete first");
        admin.disableTable(hbaseTable);
        admin.deleteTable(hbaseTable);
      }
      HTableDescriptor hTableDescriptor = new HTableDescriptor(hbaseTable);
      for (String columnFamilie : columnFamilies) {
        hTableDescriptor.addFamily(new HColumnDescriptor(columnFamilie));
      }
      admin.createTable(hTableDescriptor);
      System.out.println("create table:" + hbaseTable.getName() + "success!");
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (admin != null) {
        try {
          admin.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return;
  }

  private void hdfs_example() {
    /** get FileSystem */
    FileSystem fs = null;
    String src = "config/example.config";
    String dst = "/user/root";
    Configuration configuration = new Configuration();
    configuration.addResource(new Path("config/core-site.xml"));
    configuration.addResource(new Path("config/hdfs-site.xml"));
    try {
      fs = FileSystem.get(configuration);
      if (!fs.exists(new Path(dst))) {
        fs.mkdirs(new Path(dst));
      }
      fs.copyFromLocalFile(new Path(src), new Path(dst));
      RemoteIterator<LocatedFileStatus> list = fs.listFiles(new Path("/"), false);
      while (list.hasNext()) {
        System.out.println(list.next().toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return;
  }

  private void kafka_example() {
    /** get KafkaConsumer */
    Properties consumerProperties = new Properties();
    consumerProperties.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap_servers"));
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getProperty("group_id"));
    consumerProperties.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getProperty("auto_offset_reset"));
    consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    consumerProperties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    consumerProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
    consumerProperties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProperties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(consumerProperties);
    String topic = properties.getProperty("kafka_topic");
    consumer.subscribe(Arrays.asList(topic));

    /** get KafkaProducer */
    Properties producerProperties = new Properties();
    producerProperties.put("bootstrap.servers", properties.getProperty("bootstrap_servers"));
    producerProperties.put(ProducerConfig.ACKS_CONFIG, "all");
    producerProperties.put(ProducerConfig.RETRIES_CONFIG, 0);
    producerProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
    producerProperties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
    producerProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
    producerProperties.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    producerProperties.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    Producer<String, String> producer = new KafkaProducer<String, String>(producerProperties);

    /** start a thread to produce message */
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                for (int i = 0; i < 100; i++) {
                  producer.send(new ProducerRecord<String, String>(topic, "message-" + i));
                  System.out.println("send message -------- message-" + i);
                }
              }
            })
        .start();

    /** start a thread to consumer topic */
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                while (true) {
                  ConsumerRecords<String, String> records = consumer.poll(1000);
                  if (!records.isEmpty()) {
                    for (ConsumerRecord record : records) {
                      System.out.println(
                          "comsumer message ------- group_id: "
                              + properties.getProperty("group_id")
                              + " topic: "
                              + record.topic()
                              + " partition: "
                              + record.partition()
                              + " message: "
                              + record.value()
                              + " offset: "
                              + record.offset());
                    }
                  }
                }
              }
            })
        .start();
  }


  /**
   * create znode in zookeeper
   * @param zooKeeper
   * @param absolutePath
   */
  private void createZnode(ZooKeeper zooKeeper, String absolutePath) {
    try {
      if (zooKeeper.exists(absolutePath, null) == null) {
        zooKeeper.create(absolutePath,"".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
      } else {
        System.out.println("path: " + absolutePath + " already exist...");
      }
    } catch (KeeperException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }
  private void zk_example() {
    String host = properties.getProperty("zookeeper_host");
    List<String> list = new ArrayList<>();
    ZooKeeper zooKeeper = null;
    try {
      zooKeeper = new ZooKeeper(host, 2000, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
//   String envHome =  System.getenv("HOME");
//   System.out.println(envHome);
//   System.out.println("===============");
//   createZnode(zooKeeper,"/xxx-kafkapreprocess3");
//   createZnode(zooKeeper,"/xxx-kafkapreprocess3/planName1");
//   createZnode(zooKeeper,"/xxx-kafkapreprocess3/planName1/topic1");
//   CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(host,new RetryNTimes(10,5000));
//   curatorFramework.start();
//   try {
//     curatorFramework.createContainers("/xxx1/xxx2");
//   } catch (Exception e) {
//     e.printStackTrace();
//   }

    try {
      list = zooKeeper.getChildren("/", null);
    } catch (KeeperException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    for (String str : list) {
      System.out.print(str + " ");
    }
    System.out.println();
  }
}
