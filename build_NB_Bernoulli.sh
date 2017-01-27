#!/bin/sh
# implementation of NB classifier - Bernoulli model
export JAVA_HOME=/opt/jdk1.8.0_60
export PATH=/opt/jdk1.8.0_60/bin:$PATH
javac *.java
java NaiveBayesClassifier_Bernoulli $@
# end
