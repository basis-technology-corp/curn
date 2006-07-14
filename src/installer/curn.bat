@echo off
if "%OS%" == "Windows_NT" setlocal

rem ---------------------------------------------------------------------------
rem Front end Windows script for the curn RSS reader
rem
rem $Id$
rem ---------------------------------------------------------------------------
rem This software is released under a BSD-style license:
rem
rem Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.
rem
rem Redistribution and use in source and binary forms, with or without
rem modification, are permitted provided that the following conditions are
rem met:
rem
rem 1.  Redistributions of source code must retain the above copyright notice,
rem     this list of conditions and the following disclaimer.
rem
rem 2.  The end-user documentation included with the redistribution, if any,
rem     must include the following acknowlegement:
rem
rem       "This product includes software developed by Brian M. Clapper
rem       (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
rem       copyright (c) 2004-2006 Brian M. Clapper."
rem
rem     Alternately, this acknowlegement may appear in the software itself,
rem     if wherever such third-party acknowlegements normally appear.
rem
rem 3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
rem     nor any of the names of the project contributors may be used to
rem     endorse or promote products derived from this software without prior
rem     written permission. For written permission, please contact
rem     bmc@clapper.org.
rem
rem 4.  Products derived from this software may not be called "clapper.org
rem     Java Utility Library", nor may "clapper.org" appear in their names
rem     without prior written permission of Brian M.a Clapper.
rem
rem THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
rem WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
rem MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
rem NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
rem INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
rem NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
rem DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
rem THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
rem (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
rem THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
rem ---------------------------------------------------------------------------

set JAVA_VM_ARGS=-Dcurn.home=$INSTALL_PATH

rem Make sure Java user.home property accurately reflects home directory
if NOT "%HOME%"=="" set JAVA_VM_ARGS=%JAVA_VM_ARGS% -Duser.home=%HOME%

if NOT "%CURN_JAVA_VM_ARGS%"=="" set JAVA_VM_ARGS=%JAVA_VM_ARGS% %CURN_JAVA_VM_ARGS%

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

