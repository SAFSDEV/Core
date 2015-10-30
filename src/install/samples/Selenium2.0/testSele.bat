;REM Start the SafsDriver to run the test
set CLASSPATH=%STAFDIR%\bin\JSTAF.jar;%SAFSDIR%\lib\safsselenium.jar;%SAFSDIR%\lib\selenium-server-standalone-2.47.1.jar
"%SAFSDIR%\jre\bin\java.exe" -Dsafs.project.config=SafsDevTest.ini org.safs.tools.drivers.SAFSDRIVER
