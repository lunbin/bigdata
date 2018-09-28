##### docker pull zendesk/maxwell

##### //create user maxwell, grant all of the table in database maxwell to user maxwell 
```
mysql> GRANT ALL on maxwell.* to 'maxwell'@'%' identified by '123456';
mysql> GRANT SELECT, REPLICATION CLIENT, REPLICATION SLAVE on *.* to 'maxwell'@'%';
```

```
docker run -it zendesk/maxwell bash
bin/maxwell --user='maxwell' --password='123456' --host='10.19.138.130' --producer=stdout  --schema_database=maxwell(auto create database maxwell)
```

##### or
```
bin/maxwell --user='maxwell' --password='123456' --host='10.19.138.130' --schema_database=maxwells --producer=kafka --kafka.bootstrap.servers=10.19.248.200:31561,10.19.248.200:31923,10.19.248.200:31824
```
