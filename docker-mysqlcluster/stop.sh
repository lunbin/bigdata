#!/bin/bash
list=`docker ps | grep mysqlcluster | awk '{print $1}'`
for i in $list; do
  docker kill ${i}
  docker rm ${i}
done
