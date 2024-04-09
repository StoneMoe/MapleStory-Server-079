echo "
+----------------------------------------------------------------------
|                   冒险岛079 FOR CentOS/Ubuntu/Debian
+----------------------------------------------------------------------
"

echo "mysql服务是否启动"
while true
do
	port=`netstat -antp | grep "3306"`
  if [ -n "$port" ]; then
		echo "mysql服务已经启动"
		break;
	fi
	sleep 5
done

echo ${MYSQL_ROOT_PASSWORD} > /tmp/pwd
echo ${IP} > /tmp/ip

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS maplestory_079"
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "use maplestory_079;source /usr/local/MapleStory-Server-079/ms_20210813_234816.sql;"


sed -i "s/afauria/${MYSQL_ROOT_PASSWORD}/g"  /usr/local/MapleStory-Server-079/config/db.properties
sed -i "s/127.0.0.1/${IP}/g" /usr/local/MapleStory-Server-079/config/server.properties

nohup java -cp /usr/local/MapleStory-Server-079/bin/maple.jar -server -DhomePath=/usr/local/MapleStory-Server-079/config/ -DscriptsPath=/usr/local/MapleStory-Server-079/scripts/ -DwzPath=/usr/local/MapleStory-Server-079/scripts/wz -Xms512m -Xmx2048m -XX:PermSize=256m -XX:MaxPermSize=512m -XX:MaxNewSize=512m server.Start  > /usr/local/MapleStory-Server-079/logs/maple.log &
