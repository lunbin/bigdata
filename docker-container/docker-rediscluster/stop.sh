#!/bin/bash

list=`docker ps | grep redistest | awk '{print $1}'`

for c in $list; do 
  docker kill $c
  docker rm $c
done
