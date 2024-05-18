#! /bin/bash
WAIT_TIME=10

while true; do
    echo "Try to connect mysql."
    mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" -D "$MYSQL_DATABASE" -e "SELECT 1;" &>/dev/null
    if [ $? -eq 0 ]; then
        echo "Mysql online!"
        break
    else
        echo "Mysql offline."
        sleep $WAIT_TIME
    fi
done


TABLE_COUNT=$(mysql -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -h"${MYSQL_HOST}" -se"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${MYSQL_DATABASE}';")

if [ "$TABLE_COUNT" -eq 0 ]; then
    echo "No tables found in '${MYSQL_DATABASE}' database. Starting data import..."
    mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "use ${MYSQL_DATABASE};source /app/ms_20210813_234816.sql;"

else
    echo "Database contains tables. No need for data import."
fi

sed -i "s/placeholder_mysql_user/${MYSQL_USER}/g"  /app/config/db.properties
sed -i "s/placeholder_mysql_host/${MYSQL_HOST}/g"  /app/config/db.properties
sed -i "s/placeholder_mysql_password/${MYSQL_PASSWORD}/g"  /app/config/db.properties
sed -i "s/placeholder_mysql_database/${MYSQL_DATABASE}/g"  /app/config/db.properties
sed -i "s/placeholder_public_ip/${PUBLIC_IP}/g" /app/config/server.properties

java -jar /app/MapleStory_Server.jar -DhomePath=/app/config/ -DscriptsPath=/app/scripts/ -DwzPath=/app/scripts/wz/ -Xms512m -Xmx2048m -XX:PermSize=256m -XX:MaxPermSize=512m -XX:MaxNewSize=512m server.Start
