@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem NT Service Install/Uninstall script modified for the OSAF Server Bundle
rem by using Catalina's service.bat as a template
rem
rem Options
rem install                Install the service using chandler as service name.
rem                        Service is installed using default settings.
rem remove                 Remove the service from the System.
rem
rem name        (optional) If the second argument is present it is considered
rem                        to be new service name                                           
rem
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

set CATALINA_HOME=%OSAFSRV_HOME%\tomcat

if exist "%CATALINA_HOME%\bin\tomcat5.exe" goto okExec
echo The tomcat.exe was not found...
echo The CATALINA_HOME environment variable is not defined correctly.
echo This environment variable is needed to run this program
goto end
:okExec

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto okJava
echo The JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end 
:okJava
if not "%CATALINA_BASE%" == "" goto gotBase
set CATALINA_BASE=%CATALINA_HOME%
:gotBase
 
set EXECUTABLE=%CATALINA_HOME%\bin\tomcat5.exe

rem Set default Service name
set SERVICE_NAME=chandler
set PR_DISPLAYNAME=Chandler Server

if "%1" == "" goto displayUsage
if "%2" == "" goto setServiceName
set SERVICE_NAME=%2
set PR_DISPLAYNAME=Chandler Server
:setServiceName
if %1 == install goto doInstall
if %1 == remove goto doRemove
if %1 == uninstall goto doRemove
echo Unknown parameter "%1"
:displayUsage
echo.
echo Usage: service.bat install/remove [service_name]
goto end

:doRemove
rem Remove the service
"%EXECUTABLE%" //DS//%SERVICE_NAME%
echo The service '%SERVICE_NAME%' has been removed
goto end

:doInstall
rem Install the service
echo Installing the service '%SERVICE_NAME%' ...
echo Using CATALINA_HOME:    %CATALINA_HOME%
echo Using CATALINA_BASE:    %CATALINA_BASE%
echo Using JAVA_HOME:        %JAVA_HOME%

rem Use the environment variables as an example
rem Each command line option is prefixed with PR_

set PR_DESCRIPTION=Chandler Server - http://www.chandlerproject.org
set PR_INSTALL=%EXECUTABLE%
set PR_LOGPATH=%CATALINA_BASE%\logs
set PR_CLASSPATH=%CATALINA_HOME%\bin\bootstrap.jar
rem Set the server jvm from JAVA_HOME
set PR_JVM=%JAVA_HOME%\jre\bin\server\jvm.dll
if exist "%PR_JVM%" goto foundJvm
rem Set the client jvm from JAVA_HOME
set PR_JVM=%JAVA_HOME%\jre\bin\client\jvm.dll
if exist "%PR_JVM%" goto foundJvm
set PR_JVM=auto
:foundJvm
echo Using JVM:              %PR_JVM%
"%EXECUTABLE%" //IS//%SERVICE_NAME% --StartClass org.apache.catalina.startup.Bootstrap --StopClass org.apache.catalina.startup.Bootstrap --StartParams start --StopParams stop
if not errorlevel 1 goto installed
echo Failed installing '%SERVICE_NAME%' service
goto end
:installed
rem Clear the environment variables. They are not needed any more.
set PR_DISPLAYNAME=
set PR_DESCRIPTION=
set PR_INSTALL=
set PR_LOGPATH=
set PR_CLASSPATH=
set PR_JVM=
rem Set extra parameters
"%EXECUTABLE%" //US//%SERVICE_NAME% --JvmOptions "-Dcatalina.base=%CATALINA_BASE%;-Dcatalina.home=%CATALINA_HOME%;-Djava.endorsed.dirs=%CATALINA_HOME%\common\endorsed" --StartMode jvm --StopMode jvm
rem More extra parameters
set PR_LOGPATH=%CATALINA_BASE%\logs
set PR_STDOUTPUT=auto
set PR_STDERROR=auto
"%EXECUTABLE%" //US//%SERVICE_NAME% ++JvmOptions "-Djava.io.tmpdir=%CATALINA_BASE%\temp" --JvmMs 128 --JvmMx 256
rem Cosmo parameters
"%EXECUTABLE%" //US//%SERVICE_NAME% ++JvmOptions "-Dical4j.unfolding.relaxed=true -Dical4j.parsing.relaxed=true -Dical4j.validation.relaxed=true -Dnet.fortuna.ical4j.timezone.registry=org.osaf.cosmo.calendar.CosmoTimeZoneRegistryFactory" --StartPath "%OSAFSRV_HOME%"
echo The service '%SERVICE_NAME%' has been installed.

:end
cd %CURRENT_DIR%
