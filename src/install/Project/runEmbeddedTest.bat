
start "Debug Log - Start" "%SAFSDIR%\Project\SAFSTESTLOG_Startup.bat"

"%SAFSDIR%\jre\bin\java.exe" -Dsafs.project.config="%SAFSDIR%\project\EmbeddedTest.ini" org.safs.EmbeddedDCHookDriver

start "Debug Log - Stop" "%SAFSDIR%\Project\SAFSTESTLOGShutdown.bat"
