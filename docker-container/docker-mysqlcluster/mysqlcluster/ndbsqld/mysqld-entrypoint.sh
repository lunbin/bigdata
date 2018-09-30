#!/bin/bash

export MYSQL_ROOT_PASSWORD=root

cp /opt/mysqlcluster/ndbsqld/ndbsql_my.cnf /etc/my.cnf
sed -i "s/%NDB_CONNECT_STRING%/${NDB_CONNECT_STRING}/g" /etc/my.cnf
#cp /mysql/my.cnf /etc/my.cnf
touch /var/log/mysqlcluster.log && chown mysql /var/log/mysqlcluster.log && chgrp mysql /var/log/mysqlcluster.log

#yum install -y libaio
#yum install -y libnuma.so.1
#yum install -y numactl.x86_64
#安装net-tools用于检测1186端口的服务是否开启了
#yum install -y net-tools

curl 172.18.0.21:1186&
netstat -apn | grep curl | grep 1186
flag=$?

while (($flag != 0))
do
sleep 5
curl 172.18.0.21:1186&
netstat -apn | grep curl | grep 1186
flag=$?
done;

#设置无密码，需进入数据库进行更改密码
#修改密码  mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
if [ ! -d /usr/local/mysql/data ]; then
  mkdir /usr/local/mysql/data
fi
mysqld --initialize-insecure 
/etc/init.d/mysql.server start

if [ ! -z "$MYSQL_ROOT_PASSWORD" ]; then
	mysql -uroot --skip-password <<EOF
	ALTER USER 'root'@'localhost' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}';
	GRANT ALL ON *.* TO 'root'@'%' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}';
EOF
	if [ ! -z "$MYSQL_DATABASE" ]; then
		mysql -uroot -proot <<EOF
		CREATE DATABASE IF NOT EXISTS $MYSQL_DATABASE;
EOF
	fi
fi

ps -A |grep mysqld$
flag=$?
while(($flag == 0))
do
sleep 5
ps -A |grep mysqld$
flag=$?
done;
