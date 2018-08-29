#!/bin/bash

cp /opt/mysqlcluster/ndbdata/ndbdata_my.cnf /etc/my.cnf
sed -i "s/%NDB_CONNECT_STRING%/${NDB_CONNECT_STRING}/g" /etc/my.cnf
#cp /mysql/my.cnf /etc/my.cnf
ndbmtd
flag=$?

while (($flag != 0))
do
sleep 5
ndbd
flag=$?
done;

tail -f /etc/hosts

ps -A |grep ndbd$
flag=$?
while(($flag == 0))
do
sleep 5
ps -A |grep ndbd$
flag=$?
done;
#echo "lbsheng" > /opt/mysql.log
#tail -f /etc/hosts

