;
;REM Start the SafsDriver to run the test
;
setlocal enableDelayedExpansion
set max=0
for /f "tokens=1* delims=-.0" %%A in ('dir /b /a-d %SAFSDIR%\lib\selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B
set SELENIUM_SERVER_JAR_LOC=%SAFSDIR%\lib\selenium-%max%

set CLASSPATH=%STAFDIR%\bin\JSTAF.jar;%SAFSDIR%\lib\safsselenium.jar;%SELENIUM_SERVER_JAR_LOC%
set EXECUTE=%SAFSDIR%/jre/bin/java.exe

"%EXECUTE%" -Dsafs.project.config="se2processcontainer.ini" -cp "%CLASSPATH%" org.safs.selenium.spc.WDSPC


