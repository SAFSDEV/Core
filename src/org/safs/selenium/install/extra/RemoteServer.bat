REM 31 JUL 2017 (Lei Wang) Detected the Selenium's version:
REM                        If the version is 2.52.0 or before, we invoke it as before; 
REM                        otherwise (for selenium 3.X) we pass "VM parameter" as "VM parameter" and pass timeout and browserTimeout parameter WITHOUT the equal sign "=".

TITLE Remote Server
@echo off

setlocal enableDelayedExpansion
rem User-defined variables
rem Find explanation of TIMEOUT and BROWER_TIMEOUT from https://seleniumhq.github.io/docs/remote.html
rem Currently, we set them to 0 so that the browser will not go away until we stop it.
set TIMEOUT=0
set BROWER_TIMEOUT=0

rem Non User-defined variables, don't modify following variables
set LAST_SUPPORT_2_X_VERSION=2.52.0.jar
set max=0
rem Set the geckodriver according the OS architecture (32 or 64 bits)
if "%PROCESSOR_ARCHITECTURE%" == "AMD64" (
  set GECKO_DRIVER=geckodriver_64.exe
) else (
  set GECKO_DRIVER=geckodriver.exe
)

for /f "tokens=1,2,3* delims=-" %%A in ('dir /b /a-d %SELENIUM_PLUS%\libs\selenium-server-standalone-*.jar') do (
 if %%D gtr !max! (
   set max=%%D
   rem echo !max!
 )
)

set SELENIUM_SERVER_JAR_LOC=%SELENIUM_PLUS%\libs\selenium-server-standalone-!max!
rem echo %SELENIUM_SERVER_JAR_LOC%

if !max! gtr %LAST_SUPPORT_2_X_VERSION% (
  echo RUNNING Selenium 3.X through %SELENIUM_SERVER_JAR_LOC%
  "%SELENIUM_PLUS%\Java64\jre\bin\java.exe" -Xms512m -Xmx2g -Dwebdriver.chrome.driver="%SELENIUM_PLUS%\extra\chromedriver.exe" -Dwebdriver.ie.driver="%SELENIUM_PLUS%\extra\IEDriverServer.exe" -Dwebdriver.gecko.driver="%SELENIUM_PLUS%\extra\%GECKO_DRIVER%" -jar "%SELENIUM_SERVER_JAR_LOC%" -timeout %TIMEOUT% -browserTimeout %BROWER_TIMEOUT%
) else (
  "%SELENIUM_PLUS%\Java64\jre\bin\java.exe" -Xms512m -Xmx2g -jar "%SELENIUM_SERVER_JAR_LOC%" -Dwebdriver.chrome.driver="%SELENIUM_PLUS%\extra\chromedriver.exe" -Dwebdriver.ie.driver="%SELENIUM_PLUS%\extra\IEDriverServer.exe" -timeout=%TIMEOUT% -browserTimeout=%BROWER_TIMEOUT%
)