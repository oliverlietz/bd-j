#!/bin/sh
rm -rf classes
mkdir classes
javac -d classes -sourcepath .:../../src Test.java
if [ $? -ne 0 ] ; then
    exit 1;
fi
java -cp classes Test 
rm -rf classes
