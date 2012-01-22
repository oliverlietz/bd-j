@echo off


rem %~dp0 is expanded pathname of the current script under NT
set BDSIGNER_HOME=%~dp0..

echo %BDSIGNER_HOME%

if "%JAVA_HOME%" == "" goto noJavaHome
  %JAVA_HOME%/bin/java -cp %BDSIGNER_HOME%/build/bdsigner.jar;%JAVA_HOME%/lib/tools.jar;%BDSIGNER_HOME%/resource/bcprov-jdk15-137.jar net.java.bd.tools.bdsigner.BDSigner -debug ffff0001 writefile.jar
  echo "Running the signed applet....."

  %JAVA_HOME%/bin/appletviewer -J-Djava.security.policy=writefile.policy writefile.html

  rm -rf appcert.cer
  rm -rf appcert.csr
  rm -rf writetest
  rm -rf app.discroot.crt
  rm -rf keystore.store
  goto end
:noJavaHome
  goto end
:end
