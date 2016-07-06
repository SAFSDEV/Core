@ECHO OFF

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: This SAFS_CallJUnit_Groovy test is used to execute 'JUnit Test' by SAFSDriver in traditional keyword
:: way. As the 'spock-groovy test' can be compiled into java class, so it can also be exected as junit test.
:: 
:: [Prerequisite]
:: 1. SAFS Installation.
:: 2. To compile Java/Groovy test files at runtime,
::    We need a java 32 bits compiler, which can be downloaded from 
::    http://download.oracle.com/otn/java/jdk/7u45-b18/jdk-7u45-windows-i586.exe
::    After installation, please set a environment %JAVA_HOME% pointing to the JDK
::    installation directory, for example JAVA_HOME=C:\JDK7_WIN
::    For groovy compiler, we by default use the FileSystemCompiler to compile 
::    groovy source code, it could work normally. If any problem, we could also 
::    download a groovy compiler from http://groovy-lang.org/download.html
::    After installation, please set a environment %GROOVY_HOME% pointing to the groovy
::    compiler installation directory, for example GROOVY_HOME=C:\groovy-2.4.7
::
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

@REM Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" SETLOCAL

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Prepare environment variable for compilation and execution
set max=0
for /f "tokens=1* delims=-.0" %%A in ('dir /b /a-d %SAFSDIR%\lib\selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B
set SELENIUM_SERVER_JAR_LOC=%SAFSDIR%\lib\selenium-%max%
set COMPILE_CLASSPATH=%SELENIUM_SERVER_JAR_LOC%;%SAFSDIR%\lib\safs.jar;%SAFSDIR%\lib\safsselenium.jar;%CLASSPATH%
set OUTPUT_DIR=bin

if "%GROOVY_HOME%"=="" (
  set GROOVYC=org.codehaus.groovy.tools.FileSystemCompiler
) else (
  set GROOVYC=%GROOVY_HOME%\bin\groovyc.bat
)
set JAVAC=%JAVA_HOME%\bin\javac

set EXECUTE=%SAFSDIR%\jre\bin\java.exe
set RUNTIME_CLASSPATH=%OUTPUT_DIR%;%COMPILE_CLASSPATH%

:: DON'T MODIFY ABOVE SETTING UNLESS NECESSARY
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: 1. Compile the project. This step can be skipped if we have compiled classes already, for example,
::    this project has been imported into Eclipse IDE and got compiled.
rmdir /s /q %OUTPUT_DIR%
mkdir %OUTPUT_DIR%
:: 1.1 compile groovy source file. If we have groovyc installed, then we can use it to compile; 
::     otherwise, we use org.codehaus.groovy.tools.FileSystemCompiler to compile.
echo "Using %GROOVYC% to compile groovy source codes."
if "%GROOVY_HOME%"=="" (
  %EXECUTE% -cp "%COMPILE_CLASSPATH%" "%GROOVYC%" -d %OUTPUT_DIR% src/com/sas/spock/safs/runner/tests/SpockExperimentWithRunner.groovy
) else (
  call "%GROOVYC%" -cp "%COMPILE_CLASSPATH%" -d %OUTPUT_DIR% src/com/sas/spock/safs/runner/tests/SpockExperimentWithRunner.groovy
)
:: 1.2 compile java source file
javac -cp "%COMPILE_CLASSPATH%" -encoding UTF-8 -d %OUTPUT_DIR% -nowarn src/com/sas/spock/safs/runner/tests/*.java
Echo "Project has been compiled." 

:: 2. Execute SAFS test
"%EXECUTE%" -cp "%RUNTIME_CLASSPATH%" -Dsafs.project.config="calljunit_groovy_test.ini" org.safs.tools.drivers.SAFSDRIVER

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

@REM End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" SETLOCAL