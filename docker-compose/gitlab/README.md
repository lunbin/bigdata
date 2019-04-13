#### how to deploy gitlab in localhost

1 edit docker-compose.yml  (Can also not be modified)  
Modify the http port corresponding to 80（defaul is 8929)  
Modify the ssh port corresponding to 22（defaul is 2289)
Modify the volumes if you want(config,log, data)

2 exec command  
```
./gitlab.sh up

```

3 modify config(gitlab.rb)

```
vim ~/gitlab/config/gitlab.rb
```
```
add "nginx['listen_port'] = 80" into gitlab.rb

```

4 restart gitlab docker container

```
docker restart [container id]
```
5 wait gitlab container status is healthy(about 4m)

```
docker ps
```
6 access gitlab webui by brower

```
http:localhost:8929
```
