package entity;

import java.io.Serializable;
import com.beust.jcommander.Parameter;

public class MyParameters implements Serializable{
  public static final long serialVersionUID = 5992600283799081029L;

  @Parameter(
      names = {"-h", "--help"},
      description = "print help message",
      required = false
  )
  public boolean help = false;


  @Parameter(
      names = "--env",
      description = "",
      required = false
  )
  public String env = "test";

  @Parameter(
      names = "--batchInterval",
      description = "which topic to put self metrics to kafka",
      required = false
  )
  public int batchInterval = 3;

  @Parameter(names = "--kafkaBootstrap", description = "kafka bootstrap url", required = false)
  public String kafkaBootstrap = "pre1-kafka1:9092,pre1-kafka2:9092,pre1-kafka3:9092";

//  public String kafkaBootstrap = "10.19.248.200:31297,10.19.248.200:32425,10.19.248.200:31335";

  @Parameter(
      names = "--topics",
      description = "which topic to put self metrics to kafka",
      required = false
  )
  public String topics = "lbsheng";

  @Parameter(
      names = "--targetTopic",
      description = "which topic to put self metrics to kafka",
      required = false
  )
  public String targetTopic = "slbtopic";

  @Parameter(
      names = "--groupid",
      description = "kafka group id",
      required = false
  )
  public String kafkaGroupId = "sparkStream1";

  @Parameter(
      names = "--autoOffsetReset",
      description = "auto offset reset",
      required = false
  )
  public String autoOffsetReset = "earliest";


}
