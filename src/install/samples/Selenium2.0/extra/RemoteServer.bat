TITLE Remote Server
set SE2BIN=%SAFSDIR%\samples\Selenium2.0\extra
"%SAFSDIR%\jre\bin\java.exe" -jar "%SAFSDIR%\lib\selenium-server-standalone-2.41.0.jar" -Dwebdriver.chrome.driver="%SE2BIN%\chromedriver.exe" -Dwebdriver.ie.driver="%SE2BIN%\IEDriverServer.exe" -timeout=20 -browserTimeout=60
