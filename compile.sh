#!/bin/sh

#compile java 8 source code

export JAVA_HOME=/opt/jdk1.8.0_60
export PATH=/opt/jdk1.8.0_60/bin:$PATH
javac *.java

#end
