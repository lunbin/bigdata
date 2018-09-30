package kafkaconsumer;

import org.omg.CORBA.PUBLIC_MEMBER;

public class ConsumerConfig {
  public static String BOOTSTRAP_SERVER = "bootstrap.servers";
  public static String GROUP_ID = "group.id";
  public static String ENABLE_AUTO_COMMIT = "enable.auto.commit";
  public static String COMMIT_INTERVAL_MS = "auto.commit.interval.ms";
  public static String SESSION_TIMEOUT_MS = "session.timeout.ms";
  public static String KEY_DESERIALIZER = "key.deserializer";
  public static String VALUE_DESERIALIZER = "value.deserializer";

}
