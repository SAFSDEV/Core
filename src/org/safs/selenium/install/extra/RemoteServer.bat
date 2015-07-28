TITLE Remote Server
@echo off

cd %SELENIUM_PLUS%\libs
setlocal enableDelayedExpansion
set max=0
for /f "tokens=1* delims=-.0" %%A in ('dir /b /a-d selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B
set SELENIUM_SERVER_JAR_LOC=%SELENIUM_PLUS%\libs\selenium-%max%
cd %SELENIUM_PLUS%\extra


"%SELENIUM_PLUS%\Java\jre\bin\java.exe" -Xms512m -Xmx1g -jar "%SELENIUM_SERVER_JAR_LOC%" -Dwebdriver.chrome.driver="%SELENIUM_PLUS%\extra\chromedriver.exe" -Dwebdriver.ie.driver="%SELENIUM_PLUS%\extra\IEDriverServer.exe" -timeout=20 -browserTimeout=60

