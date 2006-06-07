@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Front end Windows script for the curn RSS reader
rem
rem $Id$
rem ---------------------------------------------------------------------------

"$JAVA_HOME\bin\java" -classpath $INSTALL_PATH\lib\curnboot.jar -ea -client -Dcurn.home=$INSTALL_PATH org.clapper.curn.Bootstrap $INSTALL_PATH\lib $INSTALL_PATH\plugins @user.home\curn\plugins @user.home\.curn\plugins -- org.clapper.curn.Tool %1 %2 %3 %4 %5 %6 %7 %8 %9

:end 
