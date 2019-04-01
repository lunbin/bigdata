#!/bin/bash

version=${1:default}
prefix=10.19.248.200:29006/tools/sparkstreaming

mvn package

docker build -t ${prefix}:${version} -f Dockerfile .

docker push ${prefix}:${version}

mvn clean
