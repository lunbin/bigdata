#!/bin/bash
# ./build_image.sh [1 | 2]
prefix=10.19.248.200:29006
version=2.2.3
image_name=${prefix}/tools/bigdata-example:${version}
type=$1
if [ ${type} -eq 1 ]; then
  docker build -t ${image_name} -f Dockerfile1 .

elif [ ${type} -eq 2 ]; then
  mvn package
  docker build -t ${image_name} -f Dockerfile2 .
fi
docker push ${image_name}
