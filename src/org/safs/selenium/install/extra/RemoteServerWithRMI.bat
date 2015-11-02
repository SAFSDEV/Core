TITLE Remote Server With RMI
@echo off

REM Start remote sever as selenium hub on default port 4444, also start the SAFS RMI server.
"%SELENIUM_PLUS%\Java\jre\bin\java.exe" org.safs.selenium.webdriver.lib.RemoteDriver -safs.rmi.server "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx1g"

REM Start remote sever as selenium hub on port 4567, also start the SAFS RMI server.
REM "%SELENIUM_PLUS%\Java\jre\bin\java.exe" org.safs.selenium.webdriver.lib.RemoteDriver -safs.rmi.server "-port 4567" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx1g"

REM Start remote sever as selenium hub on default port 4567, also start the SAFS RMI server.
REM "%SELENIUM_PLUS%\Java\jre\bin\java.exe" org.safs.selenium.webdriver.lib.RemoteDriver -safs.rmi.server "-role hub" "-port 4567" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx2g"

REM Start remote sever as selenium node on port 5678 (the selenium hub should already be running on hub.machine on port 4567), also start the SAFS RMI server.
REM "%SELENIUM_PLUS%\Java\jre\bin\java.exe" org.safs.selenium.webdriver.lib.RemoteDriver -safs.rmi.server "-role node -hub http://hub.machine:4567/grid/register" "-port 5678" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx2g"