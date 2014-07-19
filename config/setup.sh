#!/bin/sh

#
# You can configure the local database using environment variables. It is typical
# to add a script in config/local.sh that defines the environment variables similar
# to the following;
#
# export DB_SERVER_HOST=127.0.0.1
# export DB_SERVER_PORT=1500
#

DB_SERVER_HOST=${DB_SERVER_HOST:-127.0.0.1}
DB_SERVER_PORT=${DB_SERVER_PORT:-5432}
DB_PROPS_PREFIX="ServerName=${DB_SERVER_HOST}:User=${USER}:Password=letmein:PortNumber=${DB_SERVER_PORT}:DatabaseName="
DB_DRIVER=org.postgresql.ds.PGSimpleDataSource

CREATED_DOMAIN=false
STOP_DOMAIN=false

R=`(asadmin list-domains | grep -q 'fgis ') && echo yes`
if [ "$R" != 'yes' ]; then
  asadmin create-domain --user admin --nopassword fgis
  CREATED_DOMAIN=true
fi

R=`(asadmin list-domains | grep -q 'fgis running') && echo yes`
if [ "$R" != 'yes' ]; then
  STOP_DOMAIN=true
  asadmin start-domain fgis
  if [ "$CREATED_DOMAIN" == 'true' ]; then
    asadmin delete-jvm-options -XX\\:MaxPermSize=192m
    asadmin delete-jvm-options -Xmx512m
    asadmin create-jvm-options -XX\\:MaxPermSize=400m
    asadmin create-jvm-options -Xmx1500m
    asadmin create-jvm-options -Dcom.sun.enterprise.tools.admingui.NO_NETWORK=true
    asadmin restart-domain fgis
  fi
fi

R=`(asadmin list-libraries | grep -q postgresql-9.1-901.jdbc4.jar) && echo yes`
if [ "$R" != 'yes' ]; then
  asadmin add-library ~/.m2/repository/postgresql/postgresql/9.1-901.jdbc4/postgresql-9.1-901.jdbc4.jar
  asadmin restart-domain fgis
fi

asadmin delete-jdbc-resource jdbc/FGIS
asadmin delete-jdbc-connection-pool FGISConnectionPool

asadmin create-jdbc-connection-pool --datasourceclassname ${DB_DRIVER} --restype javax.sql.DataSource --isconnectvalidatereq=true --validationmethod auto-commit --ping true --description "FGIS Connection Pool" --property "${DB_PROPS_PREFIX}${USER}_CALENDAR_DEV" FGISConnectionPool

asadmin set domain.resources.jdbc-connection-pool.FGISPool.property.JDBC30DataSource=true

asadmin set-log-levels javax.enterprise.resource.resourceadapter.com.sun.gjc.spi=WARNING

if [ "$STOP_DOMAIN" == 'true' ]; then
  asadmin stop-domain fgis
fi
