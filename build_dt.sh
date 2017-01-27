#!/bin/sh
#executes decision tree learner and output accuracy results
./compile.sh
/opt/jdk1.8.0_60/bin/java DecisionTreeUtil02 $1 $2 $3 $4 $5 $6
#end
