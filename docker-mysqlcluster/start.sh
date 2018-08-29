#!/bin/bash
set -e
ndbsqld_nums=2
ndbmanager_nums=2
ndbdata_nums=4
NDB_CONNECT_STRING=""
for i in `seq 1 1 ${ndbmanager_nums}`; do 
  ip=`expr 20 + ${i}`
  NDB_CONNECT_STRING=${NDB_CONNECT_STRING}"172.18.0.${ip}"
  if [ ${i} -ne ${ndbmanager_nums} ]; then
    NDB_CONNECT_STRING=${NDB_CONNECT_STRING}","
  fi
done

if [ -d /tmp/mysqlcluster ]; then
  rm -rf /tmp/mysqlcluster
fi
find ./mysqlcluster -type f | grep sh$ | xargs -I {} chmod +x {}
cp -r mysqlcluster /tmp
DOCKER=`which docker`
if [ "x${DOCKER}" = "x" ]; then
  echo "ERROR: please install docker "
  exit 1
fi

${DOCKER} run -d --name ndbddata-0 -p 2202:2202 --net mynetwork --entrypoint="" --ip 172.18.0.31 --env NDB_CONNECT_STRING=${NDB_CONNECT_STRING} -v /tmp/mysqlcluster:/opt/mysqlcluster mysqlcluster-7.5:0.1 /opt/mysqlcluster/ndbdata/ndbd-entrypoint.sh
${DOCKER} run -d --name ndbddata-1 -p 2203:2202 --net mynetwork --entrypoint="" --ip 172.18.0.32 --env NDB_CONNECT_STRING=${NDB_CONNECT_STRING} -v /tmp/mysqlcluster:/opt/mysqlcluster mysqlcluster-7.5:0.1 /opt/mysqlcluster/ndbdata/ndbd-entrypoint.sh
${DOCKER} run -d --name ndbddata-2 -p 2204:2202 --net mynetwork --entrypoint="" --ip 172.18.0.33 --env NDB_CONNECT_STRING=${NDB_CONNECT_STRING} -v /tmp/mysqlcluster:/opt/mysqlcluster mysqlcluster-7.5:0.1 /opt/mysqlcluster/ndbdata/ndbd-entrypoint.sh
${DOCKER} run -d --name ndbddata-3 -p 2205:2202 --net mynetwork --entrypoint="" --ip 172.18.0.34 --env NDB_CONNECT_STRING=${NDB_CONNECT_STRING} -v /tmp/mysqlcluster:/opt/mysqlcluster mysqlcluster-7.5:0.1 /opt/mysqlcluster/ndbdata/ndbd-entrypoint.sh

${DOCKER} run -d --name ndbsqld-0 -p 3308:3306 --net mynetwork --entrypoint="" --ip 172.18.0.26 --env NDB_CONNECT_STRING=${NDB_CONNECT_STRING} -v /tmp/mysqlcluster:/opt/mysqlcluster mysqlcluster-7.5:0.1 /opt/mysqlcluster/ndbsqld/mysqld-entrypoint.sh
${DOCKER} run -d --name ndbsqld-1 -p 3309:3306 --net mynetwork --entrypoint="" --ip 172.18.0.27 --env NDB_CONNECT_STRING=${NDB_CONNECT_STRING} -v /tmp/mysqlcluster:/opt/mysqlcluster mysqlcluster-7.5:0.1 /opt/mysqlcluster/ndbsqld/mysqld-entrypoint.sh

${DOCKER} run -d --name ndbmanager-0 -p 1186:1186 --net mynetwork --entrypoint="" --ip 172.18.0.21 --env NDBMANAGER_NUMS=${ndbmanager_nums} --env NDBDATA_NUMS=${ndbdata_nums} --env NDBSQL_NUMS=${ndbsqld_nums} -v /tmp/mysqlcluster:/opt/mysqlcluster mysqlcluster-7.5:0.1 /opt/mysqlcluster/ndbmanager/mgmd-entrypoint.sh
${DOCKER} run -d --name ndbmanager-1 -p 1187:1186 --net mynetwork --entrypoint="" --ip 172.18.0.22 --env NDBMANAGER_NUMS=${ndbmanager_nums} --env NDBDATA_NUMS=${ndbdata_nums} --env NDBSQL_NUMS=${ndbsqld_nums} -v /tmp/mysqlcluster:/opt/mysqlcluster mysqlcluster-7.5:0.1 /opt/mysqlcluster/ndbmanager/mgmd-entrypoint.sh
