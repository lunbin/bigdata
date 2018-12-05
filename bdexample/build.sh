#!/bin/bash
# ./build.sh [podname]
namespace=lbsheng
podname=$1
mvn package
ennctl -n ${namespace} cp target/bdexample-1.0-SNAPSHOT.jar ${podname}:/opt/bdexample-1.0-SNAPSHOT.jar
ennctl -n ${namespace} cp target/lib ${podname}:/opt/lib
ennctl -n ${namespace} cp src/main/resources ${podname}:/opt/config
ennctl -n ${namespace} cp cmd.txt ${podname}:/opt/cmd.txt
mvn clean

