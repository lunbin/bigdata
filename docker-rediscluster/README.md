- ##### build rediscluster image
docker build -t redistest:1.0 .

- ##### start docker container
./start.sh

- ##### into anyone container exec command
ruby --version
gem -version
gem install redis
/opt/redis/src/redis-trib.rb create --replicas 1 172.18.0.11:6379 172.18.0.12:6379 172.18.0.13:6379 172.18.0.14:6379 172.18.0.15:6379 172.18.0.16:6379




- specify ip address  
docker run -it --net mynetwork --ip 172.18.0.3 2452fb21c8af bash
docker network create --driver=bridge --subnet=172.19.0.0/16 mynetwork2
