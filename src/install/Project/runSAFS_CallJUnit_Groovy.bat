@ECHO OFF

REM :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
REM This SAFS_CallJUnit_Groovy test is used to execute 'JUnit Test' by SAFSDriver in traditional keyword
REM way. As the 'spock-groovy test' can be compiled into java class, so it can also be executed as junit test.
REM 
REM [Prerequisite]
REM 1. SAFS Installation.
REM 2. The java/groovy source code MUST reside in the "src" sub-folder of current project.
REM 3. To pre-compile Java/Groovy test files before testing, this step is OPTIONAL.
REM    We need a java 32 bits compiler, which can be downloaded from 
REM    http://download.oracle.com/otn/java/jdk/7u45-b18/jdk-7u45-windows-i586.exe
REM    After installation, please set a environment %JAVA_HOME% pointing to the JDK
REM    installation directory, for example JAVA_HOME=C:\JDK7_WIN
REM    For groovy compiler, we by default use the FileSystemCompiler to compile 
REM    groovy source code, it could work normally. If any problem, we could also 
REM    download a groovy compiler from http://groovy-lang.org/download.html
REM    After installation, please set a environment %GROOVY_HOME% pointing to the groovy
REM    compiler installation directory, for example GROOVY_HOME=C:\groovy-2.4.7
REM
REM    Run script as "runSAFS_CallJUnit_Groovy.bat pre" to invoke pre-compilation;
REM    Otherwise, Run script as "runSAFS_CallJUnit_Groovy.bat", the compilation will happen at runtime.
REM
REM :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

REM Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" SETLOCAL
set OUTPUT_DIR=bin

REM Get input parameters
SET PRE_COMPILE=%1

REM :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
REM Prepare environment variable for compilation and execution
set max=0
for /f "tokens=1* delims=-.0" %%A in ('dir /b /a-d %SAFSDIR%\lib\selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B
set SELENIUM_SERVER_JAR_LOC=%SAFSDIR%\lib\selenium-%max%
set COMPILE_CLASSPATH=%SELENIUM_SERVER_JAR_LOC%;%SAFSDIR%\lib\safs.jar;%SAFSDIR%\lib\safsselenium.jar;%CLASSPATH%

IF DEFINED PRE_COMPILE (
    ECHO We are going to pre-compile java/groovy code before testing ...
    if "%GROOVY_HOME%"=="" (
      set GROOVYC=org.codehaus.groovy.tools.FileSystemCompiler
    ) else (
      set GROOVYC=%GROOVY_HOME%\bin\groovyc.bat
    )
    set JAVAC=%JAVA_HOME%\bin\javac
)

set EXECUTE=%SAFSDIR%\jre\bin\java.exe
set RUNTIME_CLASSPATH=%OUTPUT_DIR%;%COMPILE_CLASSPATH%

REM DON'T MODIFY ABOVE SETTING UNLESS NECESSARY
REM :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

REM :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
REM 1. Compile the project. This step can be skipped if we have compiled classes already, for example,
REM    this project has been imported into Eclipse IDE and got compiled.
rmdir /s /q %OUTPUT_DIR%
mkdir %OUTPUT_DIR%

IF DEFINED PRE_COMPILE (
    REM 1.1 compile groovy source file. If we have groovyc installed, then we can use it to compile; 
    REM     otherwise, we use org.codehaus.groovy.tools.FileSystemCompiler to compile.
    echo "Using %GROOVYC% to compile groovy source codes."
    if "%GROOVY_HOME%"=="" (
      %EXECUTE% -cp "%COMPILE_CLASSPATH%" "%GROOVYC%" -d %OUTPUT_DIR% src/com/sas/spock/safs/runner/tests/SpockExperimentWithRunner.groovy
    ) else (
      call "%GROOVYC%" -cp "%COMPILE_CLASSPATH%" -d %OUTPUT_DIR% src/com/sas/spock/safs/runner/tests/SpockExperimentWithRunner.groovy
    )
    REM 1.2 compile java source file
    javac -cp "%COMPILE_CLASSPATH%" -encoding UTF-8 -d %OUTPUT_DIR% -nowarn src/com/sas/spock/safs/runner/tests/*.java
    Echo "Project has been compiled." 

)

REM 2. Execute SAFS test
"%EXECUTE%" -cp "%RUNTIME_CLASSPATH%" -Dsafs.project.config="calljunit_groovy_test.ini" org.safs.tools.drivers.SAFSDRIVER

REM :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

REM End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" ENDLOCAL