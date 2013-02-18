#! /bin/sh

mkdir -p /tmp/mongodb-0 /tmp/mongodb-1 /tmp/mongodb-2

host=$(hostname)

mongod=mongod
mongos=mongos
mongo=mongo
if [ -n "${MONGODB_HOME}" ] ; then
  mongod="${MONGODB_HOME}/bin/mongod"
  mongos="${MONGODB_HOME}/bin/mongos"
  mongo="${MONGODB_HOME}/bin/mongo"
fi

${mongod} --port 27017 --dbpath /tmp/mongodb-0 --replSet rs0        \
          --oplogSize 1000 --smallfiles --nojournal --nounixsocket  \
          --fork --logpath /tmp/mongodb-0/mongodb.log
${mongod} --port 27018 --dbpath /tmp/mongodb-1 --replSet rs0        \
          --oplogSize 1000 --smallfiles --nojournal --nounixsocket  \
          --fork --logpath /tmp/mongodb-1/mongodb.log
${mongod} --port 27019 --dbpath /tmp/mongodb-2 --replSet rs0        \
          --oplogSize 1000 --smallfiles --nojournal --nounixsocket  \
          --fork --logpath /tmp/mongodb-2/mongodb.log

sleep 10
${mongo} --quiet ${host}:27017/admin --eval \
   "printjson(rs.initiate({_id:\"rs0\",members:[{_id:0,host:\"${host}:27017\"},{_id:1,host:\"${host}:27018\"},{_id:2,host:\"${host}:27019\"}]}))" 

tail -f /tmp/mongo*/mongo*.log
