#!/bin/bash
#rvm use 2.3.3
#gem install redis
gem install redis
sed -i "s/%bindip%/${bindip}/g" /opt/redis/redis.conf
/usr/local/redis/bin/redis-server /opt/redis/redis.conf

