TITLE Remote Server
@echo off
set SAFSLIB=%SAFSDIR%\lib

pushd %SAFSLIB%
setlocal enableDelayedExpansion
set max=0
for /f "tokens=1* delims=-.0" %%A in ('dir /b /a-d selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B
set SELENIUM_SERVER_JAR_LOC=%SAFSDIR%\lib\selenium-%max%
popd

"%SAFSDIR%\jre\bin\java.exe" -Xms512m -Xmx1g -jar "%SELENIUM_SERVER_JAR_LOC%" -Dwebdriver.chrome.driver="%SAFSLIB%\chromedriver.exe" -Dwebdriver.ie.driver="%SAFSLIB%\IEDriverServer.exe" -timeout=20 -browserTimeout=60