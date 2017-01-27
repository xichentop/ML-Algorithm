#!/bin/sh
#executes decision tree learner and output accuracy results
export JAVA_HOME=/opt/jdk1.8.0_60
export PATH=/opt/jdk1.8.0_60/bin:$PATH
javac *.java
java DecisionTreeUtil02 $@
#end
