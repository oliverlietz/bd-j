@echo off


rem %~dp0 is expanded pathname of the current script under NT
set SECURITY_HOME=%~dp0..

if "%JAVA_HOME%" == "" goto noJavaHome
  %JAVA_HOME%/bin/java -cp %SECURITY_HOME%/build/security.jar;%JAVA_HOME%/lib/tools.jar%SECURITY_HOME%/resource/bcprov-jdk15-137.jar sun.security.tools.JarSigner %*
  goto end
:noJavaHome
  echo Please set JAVA_HOME before running this script
  goto end
:end
