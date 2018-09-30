#!/bin/bash
VERSION=1.3
docker run -d --name redis1 -p 6379:6379 --env bindip=172.18.0.11 --net mynetwork --entrypoint="" --ip 172.18.0.11 redistest:${VERSION} /opt/entrypoint.sh
docker run -d --name redis2 -p 6380:6379 --env bindip=172.18.0.12 --net mynetwork --entrypoint="" --ip 172.18.0.12 redistest:${VERSION} /opt/entrypoint.sh 
docker run -d --name redis3 -p 6381:6379 --env bindip=172.18.0.13 --net mynetwork --entrypoint="" --ip 172.18.0.13 redistest:${VERSION} /opt/entrypoint.sh 
docker run -d --name redis4 -p 6382:6379 --env bindip=172.18.0.14 --net mynetwork --entrypoint="" --ip 172.18.0.14 redistest:${VERSION} /opt/entrypoint.sh 
docker run -d --name redis5 -p 6383:6379 --env bindip=172.18.0.15 --net mynetwork --entrypoint="" --ip 172.18.0.15 redistest:${VERSION} /opt/entrypoint.sh
docker run -d --name redis6 -p 6384:6379 --env bindip=172.18.0.16 --net mynetwork --entrypoint="" --ip 172.18.0.16 redistest:${VERSION} /opt/entrypoint.sh 
