- ##### build rediscluster image
  ```
  $ cd docker
  $ docker build -t mysqlcluster-7.5:0.1 .
  ```
- ##### start docker container
  ```
  $ ./start.sh
  ```

  #### mysqlcluster  
  [ndbd(NDB)]	4 node(s)  
  id=5	@172.18.0.31  (mysql-5.7.22 ndb-7.5.10, Nodegroup: 0, *)  
  id=6	@172.18.0.32  (mysql-5.7.22 ndb-7.5.10, Nodegroup: 0)  
  id=7	@172.18.0.33  (mysql-5.7.22 ndb-7.5.10, Nodegroup: 1)  
  id=8	@172.18.0.34  (mysql-5.7.22 ndb-7.5.10, Nodegroup: 1)

  [ndb_mgmd(MGM)]	2 node(s)  
  id=1	@172.18.0.21  (mysql-5.7.22 ndb-7.5.10)  
  id=2	@172.18.0.22  (mysql-5.7.22 ndb-7.5.10)

  [mysqld(API)]	2 node(s)  
  id=3	@172.18.0.26  (mysql-5.7.22 ndb-7.5.10)  
  id=4	@172.18.0.27  (mysql-5.7.22 ndb-7.5.10)


- #### NOTE
  ###### execute command before run start.sh
  ```
  $ docker network create --driver=bridge --subnet=172.18.0.0/16 mynetwork
  ```

  ##### specify ip address
  ```
  $ docker run -it --net mynetwork --ip 172.18.0.3 2452fb21c8af bash
```
