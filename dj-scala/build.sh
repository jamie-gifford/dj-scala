#!/bin/bash

export JAVA_HOME=/usr/local/jdk1.8.0_101/
export SCALA_HOME=usr/local/scala-2.11.8/
export PATH=/usr/local/scala-2.11.8/bin:/usr/local/jdk1.8.0_101/bin:/usr/bin

echo building core...

( cd ../core ; ant core_build )

export CLASSPATH=library/taglib.jar:../core/library/junit-4.3.1.jar:../core/library/log4j-1.2.14.jar:../core/library/servlet-api-2.5-6.1.4rc1.jar:../core/library/jetty-6.1.4rc1.jar:../core/library/jetty-util-6.1.4rc1.jar:../core/build/lib/thoughtpatterns_core.jar:library/jsoup-1.13.1.jar:library/jline-1.0.jar:library/beatroot-dj.jar:library/jrtf-0.7.jar

echo building djapp...

scalac -verbose -cp $CLASSPATH -d build/classes/ $(find src/scala/ -name '*.scala' -o -name '*.java')

mkdir -p build/lib
rm -f build/lib/dj.jar

( cd build/classes; jar -cf ../lib/dj.jar . )


