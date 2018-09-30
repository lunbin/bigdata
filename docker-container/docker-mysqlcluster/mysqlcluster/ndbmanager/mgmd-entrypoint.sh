#!/bin/bash
if [ "x${NDBMANAGER_NUMS}" = "x" ]; then
  echo "env \${NDBMANAGER_NUMS} not set"
  exit 1
fi
if [ "x${NDBDATA_NUMS}" = "x" ]; then
  echo "env \${NDBDATA_NUMS} not set"
  exit 1
fi

if [ "x${NDBSQL_NUMS}" = "x" ]; then
  echo "env \${NDBSQL_NUMS} not set"
  exit 1
fi
#tail -f /etc/hosts
if [ ! -d /mysql ]; then
  mkdir /mysql
fi
cp /opt/mysqlcluster/ndbmanager/ndbmanager-config.ini /mysql/config.ini
for i in $(seq 1 1 ${NDBMANAGER_NUMS}); do
  startip=`expr 20 + ${i}`
  echo "[ndb_mgmd]" >> /mysql/config.ini
  echo "NodeId = ${i}" >> /mysql/config.ini
  echo "HostName = 172.18.0.${startip}" >> /mysql/config.ini
  echo >> /mysql/config.ini 
done

for i in $(seq 1 1 ${NDBSQL_NUMS}); do
  startip=`expr 25 + ${i}`
  id=`expr ${NDBMANAGER_NUMS} + ${i}`
  echo "[mysqld]" >> /mysql/config.ini
  echo "NodeId = ${id}" >> /mysql/config.ini
  echo "HostName = 172.18.0.${startip}" >> /mysql/config.ini
  echo >> /mysql/config.ini
done

for i in $(seq 1 1 ${NDBDATA_NUMS}); do
  startip=`expr 30 + ${i}`
  id=`expr ${NDBMANAGER_NUMS} + ${NDBSQL_NUMS} + ${i}`
  echo "[ndbd]" >> /mysql/config.ini
  echo "NodeId = ${id}" >> /mysql/config.ini
  echo "HostName = 172.18.0.${startip}" >> /mysql/config.ini
  echo >> /mysql/config.ini
done

#sed -i "s/%NDBMANAGER_NAME%/${NDBMANAGER_NAME}/g" /mysql/config.ini
#sed -i "s/%NDBDATA_NAME%/${NDBDATA_NAME}/g" /mysql/config.ini
#sed -i "s/%NDBSQL_NAME%/${NDBSQL_NAME}/g" /mysql/config.ini
ndb_mgmd -f /mysql/config.ini
flag=$?

#因为使用的是hostname而非ip地址，所以必须等所有pod运行起来以后才能对config.ini正确解析。
#因此不断循环执行ndb_mgmd来启动节点
while (($flag != 0))
do
sleep 5
ndb_mgmd -f /mysql/config.ini
flag=$?
done;

#守护进程，后面的文件是随便找的一个
tail -f /var/log/yum.log
