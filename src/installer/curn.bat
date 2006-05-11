@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Front end Windows script for the curn RSS reader
rem
rem $Id$
rem ---------------------------------------------------------------------------

"$JAVA_HOME\bin\java" -Dorg.clapper.curn.home=$INSTALL_PATH -ea -client -classpath "$INSTALL_PATH\lib\ocutil.jar;$INSTALL_PATH\lib\xerces.jar;$INSTALL_PATH\lib\activation.jar;$INSTALL_PATH\lib\mail.jar;$INSTALL_PATH\lib\freemarker.jar;$INSTALL_PATH\lib\commons-logging.jar;$INSTALL_PATH\lib\curn.jar;%CLASSPATH%" org.clapper.curn.Tool %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
