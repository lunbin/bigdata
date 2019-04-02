#!/bin/bash

/opt/spark/bin/spark-submit --master spark://pre1-master1.lbsheng:7077 --conf spark.exector.memory=500m --conf spark.executor.cores=1 --conf spark.executor.extraJavaOptions=-XX:+UseG1GC --conf spark.streaming.kafka.maxRatePerPartition=500 --class SparkStreaming --total-executor-cores 3 /opt/sparkstream-1.0-SNAPSHOT-jar-with-dependencies.jar --env online
