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

echo ${MYSQL_ROOT_PASSWORD} > /pwd
echo ${IP} > /ip

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS maplestory_079"
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "use maplestory_079;source /MapleStory-Server-079/ms_20210813_234816.sql;"


sed -i "s/afauria/${MYSQL_ROOT_PASSWORD}/g"  /MapleStory-Server-079/config/db.properties
sed -i "s/127.0.0.1/${IP}/g" /MapleStory-Server-079/config/server.properties

nohup java -cp /MapleStory-Server-079/bin/maple.jar -server -DhomePath=/MapleStory-Server-079/config/ -DscriptsPath=/MapleStory-Server-079/scripts/ -DwzPath=/MapleStory-Server-079/scripts/wz -Xms512m -Xmx2048m -XX:PermSize=256m -XX:MaxPermSize=512m -XX:MaxNewSize=512m server.Start  > /MapleStory-Server-079/maple.log &
