#!/bin/csh

setenv BDSIGNER_LIB ../build
setenv BC_LIB ../resource

echo "Using JDK:" ${JAVA_HOME}
echo "Signing the Applet using BDSigner tool.....please wait"
${JAVA_HOME}/bin/java -cp ${BDSIGNER_LIB}/bdsigner.jar:${JAVA_HOME}/lib/tools.jar:${BC_LIB}/bcprov-jdk15-137.jar \
 net.java.bd.tools.bdsigner.BDSigner -debug ffff0001 writefile.jar

echo "Running the signed applet....."
$JAVA_HOME/bin/appletviewer -J-Djava.security.policy=writefile.policy writefile.html

/bin/rm -rf appcert.cer
/bin/rm -rf appcert.csr
/bin/rm -rf writetest
