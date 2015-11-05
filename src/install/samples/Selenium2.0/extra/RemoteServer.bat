TITLE Remote Server
set max=0
for /f "tokens=1* delims=-.0" %%A in ('dir /b /a-d %SAFSDIR%\lib\selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B
set SELENIUM_SERVER_JAR_LOC=%SAFSDIR%\lib\selenium-%max%
set EXECUTE=%SAFSDIR%/jre/Java64/jre/bin/java
set SE2BIN=%SAFSDIR%/samples/Selenium2.0/extra
set CLASSPATH=%SAFSDIR%\lib\safsselenium.jar;%SELENIUM_SERVER_JAR_LOC%

"%EXECUTE%" -Xms512m -Xmx2g -cp %CLASSPATH% org.safs.selenium.util.SeleniumServerRunner -jar "%SELENIUM_SERVER_JAR_LOC%" -Dwebdriver.chrome.driver="%SE2BIN%/chromedriver.exe" -Dwebdriver.ie.driver="%SE2BIN%/IEDriverServer.exe" -timeout=20 -browserTimeout=60
