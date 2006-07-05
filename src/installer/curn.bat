@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Front end Windows script for the curn RSS reader
rem
rem $Id$
rem ---------------------------------------------------------------------------

set JAVA_VM_ARGS=-Dcurn.home=$INSTALL_PATH

rem Make sure Java user.home property accurately reflects home directory
if "%HOME%"=="" goto nohome
set JAVA_VM_ARGS=%JAVA_VM_ARGS% -Duser.home=%HOME%

:nohome

"$JAVA_HOME\bin\java" ^
%JAVA_VM_ARGS% ^
-classpath $INSTALL_PATH\lib\curnboot.jar ^
-ea ^
-client ^
org.clapper.curn.Bootstrap ^
$INSTALL_PATH\lib ^
$INSTALL_PATH\plugins ^
@user.home\curn\plugins ^
@user.home\.curn\plugins ^
@user.home\curn\lib ^
@user.home\.curn\lib ^
-- ^
org.clapper.curn.Tool %1 %2 %3 %4 %5 %6 %7 %8 %9

:end

