@CLS

start "STAF" /B "%STAFDIR%\startSTAFProc.bat"
start "Debug" /B "%SELENIUM_PLUS%\Java\jre\bin\java.exe" org.safs.Log debug -file:"%SELENIUM_PLUS%\debuglog.txt"


