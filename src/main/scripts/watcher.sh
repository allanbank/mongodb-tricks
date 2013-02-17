#! /bin/sh

java=java
if [ -n "${JAVA_HOME}" ] ; then
  java="${JAVA_HOME}/bin/java"
fi

scriptdir=$( dirname "$0" )
if [ "${scriptdir}" = "." ] ; then
  scriptdir=$(pwd)
fi
maindir=$( dirname "${scriptdir}" )
srcdir=$( dirname "${maindir}" )
rootdir=$( dirname "${srcdir}" )

latestJar=$( ls ${HOME}/.m2/repository/com/allanbank/mongodb-async-driver/*/mongodb-async-driver*.jar | \
                egrep -v "sources|javadoc" | tail -n 1 )
CLASSPATH=${rootdir}/target/classes
CLASSPATH=${CLASSPATH}:${latestJar}
export CLASSPATH

exec ${java} com.allanbank.mongodb.demo.coordination.watch.demo.WatchIt "$@"
