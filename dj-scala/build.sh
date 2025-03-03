#!/bin/bash

export JAVA_HOME=/usr/local/jdk1.8.0_101
export SCALA_HOME=/usr/local/scala-2.11.8
export PATH=/usr/local/scala-2.11.8/bin:/usr/local/jdk1.8.0_101/bin:/usr/bin

echo building core...

( cd ../core ; ant core_build )

H=../..

export CLASSPATH=$H/library/taglib.jar:$H/../core/library/junit-4.3.1.jar:$H/../core/library/log4j-1.2.14.jar:$H/../core/library/servlet-api-2.5-6.1.4rc1.jar:$H/../core/library/jetty-6.1.4rc1.jar:$H/../core/library/jetty-util-6.1.4rc1.jar:$H/../core/build/lib/thoughtpatterns_core.jar:$H/library/jsoup-1.13.1.jar:$H/library/jline-1.0.jar:$H/library/beatroot-dj.jar:$H/library/jrtf-0.7.jar:$H/build/classes

bootcp=$SCALA_HOME/lib/scala-compiler.jar:$SCALA_HOME/lib/scala-library.jar:$SCALA_HOME/lib/scala-reflect.jar:$SCALA_HOME/lib/jline.jar:$SCALA_HOME/lib/jline-2.12.jar


echo building djapp...

mkdir -p build/classes

( cd src/scala ; scalac -verbose -cp $CLASSPATH -d ../../build/classes/ $(find . -name '*.scala' -o -name '*.java') )

( cd src/scala ; javac  -cp $bootcp:$CLASSPATH -d ../../build/classes/ $(find . -name '*.java') )


mkdir -p build/lib
rm -f build/lib/dj.jar

( cd build/classes; jar -cf ../lib/dj.jar . )


