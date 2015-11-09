TITLE Remote Server With RMI
@echo off

setlocal enableDelayedExpansion
set max=0
for /f "tokens=1* delims=-.0" %%A in ('dir /b /a-d %SELENIUM_PLUS%\libs\selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B
set SELENIUM_SERVER_JAR_LOC=%SELENIUM_PLUS%\libs\selenium-%max%
set CMDCLASSPATH="%SELENIUM_PLUS%\libs\seleniumplus.jar;%SELENIUM_PLUS%\libs\JSTAFEmbedded.jar;%SELENIUM_SERVER_JAR_LOC%"

REM ====  Running as stand-alone server ================================================================
REM Start remote sever as "SELENIUM stand-alone" on default port 4444, also start the SAFS RMI server.
"%SELENIUM_PLUS%\Java\jre\bin\java.exe" -cp %CMDCLASSPATH% org.safs.selenium.webdriver.lib.RemoteDriver -safs.rmi.server "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx1g"

REM Start remote sever as "SELENIUM stand-alone" on port 4567, also start the SAFS RMI server.
REM "%SELENIUM_PLUS%\Java\jre\bin\java.exe" -cp %CMDCLASSPATH% org.safs.selenium.webdriver.lib.RemoteDriver -safs.rmi.server "-port 4567" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx1g"
REM =====================================================================================================

REM ====  Running as "HUB server" + "NODE" ====================================================================
REM Start remote sever as "SELENIUM hub" on port 4567, we can then start "SELENIUM node" to connect to this hub.
REM "%SELENIUM_PLUS%\Java\jre\bin\java.exe" -cp %CMDCLASSPATH% org.safs.selenium.webdriver.lib.RemoteDriver "-role hub" "-port 4567" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx1g"
REM Start remote sever as "SELENIUM node" on port 5678 (the "SELENIUM hub" should already be running at hub.machine on port 4567), also start the SAFS RMI server.
REM "%SELENIUM_PLUS%\Java\jre\bin\java.exe" -cp %CMDCLASSPATH% org.safs.selenium.webdriver.lib.RemoteDriver -safs.rmi.server "-role node -hub http://hub.machine:4567/grid/register" "-port 5678" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx1g"
REM ===========================================================================================================