@REM ----------------------------------------------------------------------------
@REM  Copyright 2001-2006 The Apache Software Foundation.
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM       http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM ----------------------------------------------------------------------------
@REM
@REM   Copyright (c) 2001-2006 The Apache Software Foundation.  All rights
@REM   reserved.

@echo off

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set CMD_LINE_ARGS=%*
goto WinNTGetScriptDir

@REM The 4NT Shell from jp software
:4NTArgs
set CMD_LINE_ARGS=%$
goto WinNTGetScriptDir

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto Win9xGetScriptDir
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto Win9xApp

:Win9xGetScriptDir
set SAVEDIR=%CD%
%0\
cd %0\..\.. 
set BASEDIR=%CD%
cd %SAVEDIR%
set SAVE_DIR=
goto repoSetup

:WinNTGetScriptDir
set BASEDIR=%~dp0\..

:repoSetup
set REPO=


if "%JAVACMD%"=="" set JAVACMD=java

if "%REPO%"=="" set REPO=%BASEDIR%\repo

set CLASSPATH="%BASEDIR%"\etc;"%REPO%"\org\jaggery2\0\org.jaggeryjs.jaggery.core\1.0-SNAPSHOT\org.jaggeryjs.jaggery.core-1.0-SNAPSHOT.jar;"%REPO%"\org\apache\tomcat\embed\tomcat-embed-core\8.0.5\tomcat-embed-core-8.0.5.jar;"%REPO%"\org\apache\tomcat\embed\tomcat-embed-logging-juli\8.0.5\tomcat-embed-logging-juli-8.0.5.jar;"%REPO%"\org\apache\tomcat\embed\tomcat-embed-jasper\8.0.5\tomcat-embed-jasper-8.0.5.jar;"%REPO%"\org\apache\tomcat\embed\tomcat-embed-el\8.0.5\tomcat-embed-el-8.0.5.jar;"%REPO%"\org\eclipse\jdt\core\compiler\ecj\P20140317-1600\ecj-P20140317-1600.jar;"%REPO%"\nashorn\wso2\js\0.1\js-0.1.jar;"%REPO%"\org\jaggery2\0\org.jaggeryjs.scriptengine\1.0-SNAPSHOT\org.jaggeryjs.scriptengine-1.0-SNAPSHOT.jar;"%REPO%"\commons-io\wso2\commons-io\2.0.0.wso2v1\commons-io-2.0.0.wso2v1.jar;"%REPO%"\commons-logging\commons-logging\1.1.3\commons-logging-1.1.3.jar;"%REPO%"\commons-httpclient\wso2\commons-httpclient\3.1.0.wso2v2\commons-httpclient-3.1.0.wso2v2.jar;"%REPO%"\commons-httpclient\commons-httpclient\3.1\commons-httpclient-3.1.jar;"%REPO%"\commons-codec\commons-codec\1.2\commons-codec-1.2.jar;"%REPO%"\org\apache\commons\ssl\not-yet-commons-ssl\0.3.9\not-yet-commons-ssl-0.3.9.jar;"%REPO%"\org\apache\tomcat\tomcat-catalina\8.0.5\tomcat-catalina-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-servlet-api\8.0.5\tomcat-servlet-api-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-jsp-api\8.0.5\tomcat-jsp-api-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-el-api\8.0.5\tomcat-el-api-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-juli\8.0.5\tomcat-juli-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-annotations-api\8.0.5\tomcat-annotations-api-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-api\8.0.5\tomcat-api-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-jni\8.0.5\tomcat-jni-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-coyote\8.0.5\tomcat-coyote-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-util\8.0.5\tomcat-util-8.0.5.jar;"%REPO%"\org\apache\tomcat\tomcat-util-scan\8.0.5\tomcat-util-scan-8.0.5.jar;"%REPO%"\org\jaggery2\0\distributions\1.0-SNAPSHOT\distributions-1.0-SNAPSHOT.jar

set ENDORSED_DIR=
if NOT "%ENDORSED_DIR%" == "" set CLASSPATH="%BASEDIR%"\%ENDORSED_DIR%\*;%CLASSPATH%

if NOT "%CLASSPATH_PREFIX%" == "" set CLASSPATH=%CLASSPATH_PREFIX%;%CLASSPATH%

@REM Reaching here means variables are defined and arguments have been captured
:endInit

%JAVACMD% %JAVA_OPTS%  -classpath %CLASSPATH% -Dapp.name="app" -Dapp.repo="%REPO%" -Dapp.home="%BASEDIR%" -Dbasedir="%BASEDIR%" launcher.TomcatLauncher %CMD_LINE_ARGS%
if %ERRORLEVEL% NEQ 0 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=%ERRORLEVEL%

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set CMD_LINE_ARGS=
goto postExec

:endNT
@REM If error code is set to 1 then the endlocal was done already in :error.
if %ERROR_CODE% EQU 0 @endlocal


:postExec

if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)

exit /B %ERROR_CODE%
