@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Control script for the OSAF Server Bundle
rem
rem Modified for use with the OSAF Server Bundle by using
rem Catalina's startup.bat as a template
rem ---------------------------------------------------------------------------

rem Guess OSAFSRV_HOME if not defined
set CURRENT_DIR=%cd%
if not "%OSAFSRV_HOME%" == "" goto gotHome
set OSAFSRV_HOME=%CURRENT_DIR%
if exist "%OSAFSRV_HOME%\bin\osafsrvctl.bat" goto okHome
cd ..
set OSAFSRV_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%OSAFSRV_HOME%\bin\osafsrvctl.bat" goto okHome
echo The OSAFSRV_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

if not exist "%OSAFSRV_HOME%\logs" md "%OSAFSRV_HOME%\logs"

rem tomcat is found one level above this directory
set CATALINA_HOME=%OSAFSRV_HOME%\tomcat
set CATALINA_BIN=%CATALINA_HOME%\bin
set TOMCAT_SCRIPT=catalina.bat

set EXECUTABLE=%CATALINA_BIN%\%TOMCAT_SCRIPT%

if not exist "%CATALINA_HOME%\temp" md "%CATALINA_HOME%\temp"

rem Check that target executable exists
if exist "%EXECUTABLE%" goto okExec
echo Cannot find %EXECUTABLE%
echo This file is needed to run this program
goto end

:okExec

rem source the config script if it exists
set CFGSCRIPT=osafsrvcfg.bat
if exist "%CURRENT_DIR%\%CFGSCRIPT" goto loadConfig
goto checkFeatures
:loadConfig
call "%CURRENT_DIR%\%CFGSCRIPT"

:checkFeatures

rem enable/disable features
set OSAFSRV_OPTS=-server

if "%OSAFSRV_JMX_LOCAL%" == "" goto setJMX
echo "Disabling JMX"
goto checkDerby
:setJMX
set OSAFSRV_OPTS=%OSAFSRV_OPTS% -Dcom.sun.management.jmxremote

:checkDerby
if "%OSAFSRV_DERBY_ERROR_LOG%" == "" goto logDerby
set OSAFSRV_OPTS=%OSAFSRV_OPTS% -Dderby.stream.error.file=%OSAFSRV_DERBY_ERROR_LOG%
goto checkAppendDerby
:logDerby
set OSAFSRV_DERBY_ERROR_LOG=%OSAFSRV_HOME%\logs\derby.log
set OSAFSRV_OPTS=%OSAFSRV_OPTS% -Dderby.stream.error.file=%OSAFSRV_DERBY_ERROR_LOG%

:checkAppendDerby
echo "Logging derby output to %OSAFSRV_DERBY_ERROR_LOG%"

if "%OSAFSRV_DERBY_ERROR_LOG_APPEND%" == "" goto appendDerby
set OSAFSRV_OPTS=%OSAFSRV_OPTS% -Dderby.infolog.append=false
echo "Disabling appending for derby log"
goto setJavaOpts
:appendDerby
set OSAFSRV_OPTS=%OSAFSRV_OPTS% -Dderby.infolog.append=true

:setJavaOpts

rem move into the server bundle install dir so that paths relative to
rem the current working directory resolve correctly
cd %OSAFSRV_HOME%

set JAVA_OPTS=%JAVA_OPTS% %OSAFSRV_OPTS%

echo "Using OSAFSRV_HOME:      %OSAFSRV_HOME%"

rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

call "%EXECUTABLE%" %CMD_LINE_ARGS%

:end
