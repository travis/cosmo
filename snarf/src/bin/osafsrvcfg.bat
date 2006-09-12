rem -----------------------------------------------------------------------------
rem Copyright 2006 Open Source Applications Foundation
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.


rem
rem Environment configuration file for the osafctl.bat script.
rem
rem The environment variables set by this file control features of
rem various components within the server. These features are
rem specified by command line options passed to the JVM.
rem
rem Each of the commented out lines below sets an environment variable
rem to its default value. You may leave these commented out, as the
rem server will use a variable's default value if the variable is
rem unset. If you uncomment one of these lines or set the variable
rem yourself, the server will use that value instead and will log a
rem message to that end.
rem
rem $JAVA_OPTS is unaffected by this file. Administrators may use this
rem environment variable to specify additional options to the JVM as
rem usual.
rem


rem
rem This feature enables the JVM's JMX agent for local monitoring and
rem management. See
rem <http://java.sun.com/j2se/1.5.0/docs/guide/management/agent.html>
rem for more details.
rem
remset OSAFSRV_JMX_LOCAL=true

rem
rem This feature allows iCalendar objects with both LF and CRLF line
rem endings (RFC 2445 mandates CRLF, but some implementations only use
rem LF). To disable this feature and require strict line endings, set
rem the variable to false or 0.
rem
remset OSAFSRV_ICAL4J_UNFOLDING_RELAXED=true

rem
rem This feature allows you to redirect Derby's logging output to
rem another location on the filesystem. Derby unfortunately uses a
rem proprietary logging mechanism rather than commons-logging or log4j,
rem so we have to treat its logfile specially.
rem
rem The filesystem path is interpreted relative to $OSAFSRV_HOME, the top
rem level directory of the server bundle distribution (the directory
rem above this one).
rem
remset OSAFSRV_DERBY_ERROR_LOG=logs/derby.log

rem
rem This feature controls whether Derby will overwrite its log file
rem when it starts up or whether it will append to an existing log
rem file. The default is to append to an existing file. To have the file
rem overwritten, set the variable to false or 0.
rem
remset OSAFSRV_DERBY_ERROR_LOG_APPEND=true
