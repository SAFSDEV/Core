TITLE Start Selenium (RC) SPC
@echo off

setlocal enableDelayedExpansion
rem Non User-defined variables, don't modify following variables
set LIBRARY_DIR=%SAFSDIR%\lib
set LAST_SUPPORT_2_X_VERSION=2.52.0.jar
set RETAINED_LEGACY_RC_JAR=%LIBRARY_DIR%\selenium-sever-safs-2.52.jar
set max=0

for /f "tokens=1,2,3* delims=-" %%A in ('dir /b /a-d %LIBRARY_DIR%\selenium-server-standalone-*.jar') do (
 if %%D gtr !max! (
   set max=%%D
 )
)

set SELENIUM_SERVER_JAR_LOC=%LIBRARY_DIR%\selenium-server-standalone-!max!
rem echo %SELENIUM_SERVER_JAR_LOC%

set CLASSPATH=%SELENIUM_SERVER_JAR_LOC%;%CLASSPATH%

rem We add the legacy RC jar ONLY if the selenium jar is later than 2.52.0
if !max! gtr %LAST_SUPPORT_2_X_VERSION% (
	set CLASSPATH=%RETAINED_LEGACY_RC_JAR%;%CLASSPATH%
)

%SAFSDIR%\jre\bin\java -cp %CLASSPATH% -Dsafs.config.paths=SafsDevTest.ini org.safs.selenium.SeleniumJavaHook SPC