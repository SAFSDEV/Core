
set CLASSPATH=%STAFDIR%\bin\JSTAF.jar;%SAFSDIR%\lib\safsselenium.jar;%SAFSDIR%\lib\selenium-server-standalone-2.45.0.jar
"%SAFSDIR%\jre\bin\java.exe" -Dsafs.project.config="se2processcontainer.ini" -cp "%CLASSPATH%" org.safs.selenium.spc.WDSPC


