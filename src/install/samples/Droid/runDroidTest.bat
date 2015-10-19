;REM Start the SafsDriver to run the test
%SAFSDIR%\jre\bin\java -Dsafs.project.config="droidTest.ini" org.safs.tools.drivers.SAFSDRIVER
call SAFSTESTLOGShutdown.bat
