# MS 079 Server
For study only

### Refs

https://github.com/aoaostar/MapleStory

https://github.com/mrzhqiang/ms079

### docker
`docker run -itd -p 6350:6350 -p 8600:8600 -p 9595:9595 -p 2525:2525 -p 2526:2526 -p 2527:2527 -p 2528:2528 -p 2529:2529 -p 2530:2530 -e IP=111.229.0.0  -e MYSQL_ROOT_PASSWORD=mysql-pwd -v /data/mysql/data:/var/lib/mysql morty2333/mxd079:1.0.0`

Fill server public IP in `IP`
