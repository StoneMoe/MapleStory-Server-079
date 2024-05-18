#! /bin/bash

TABLE_COUNT=$(mysql -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -h"${MYSQL_HOST}" -se"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${MYSQL_DATABASE}';")

if [ "$TABLE_COUNT" -eq 0 ]; then
    echo "No tables found in '${MYSQL_DATABASE}' database. Starting data import..."
    mysql -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -e "use ${MYSQL_DATABASE};source /app/ms_20210813_234816.sql;"

else
    echo "Database contains tables. No need for data import."
fi

sed -i "s/mysql_user/${MYSQL_USER}/g"  /app/config/db.properties
sed -i "s/mysql_host/${MYSQL_HOST}/g"  /app/config/db.properties
sed -i "s/mysql_password/${MYSQL_PASSWORD}/g"  /app/config/db.properties
sed -i "s/mysql_database/${MYSQL_DATABASE}/g"  /app/config/db.properties
sed -i "s/public_ip/${PUBLIC_IP}/g" /app/config/server.properties

java -cp /MapleStory_Server.jar -server -DhomePath=/app/config/ -DscriptsPath=/app/scripts/ -DwzPath=/app/scripts/wz/ -Xms512m -Xmx2048m -XX:PermSize=256m -XX:MaxPermSize=512m -XX:MaxNewSize=512m server.Start
