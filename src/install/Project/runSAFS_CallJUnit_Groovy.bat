@ECHO OFF

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: This SAFS_CallJUnit_Groovy test is used to execute 'JUnit Test' by SAFSDriver in traditional keyword
:: way. As the 'spock-groovy test' can be compiled into java class, so it can also be exected as junit test.
:: 
:: [Prerequisite]
:: 1. SAFS Installation.
:: 2. Java/Groovy test files get compiled.
::    We need a java 32 bits compiler, which can be downloaded from 
::    http://download.oracle.com/otn/java/jdk/7u45-b18/jdk-7u45-windows-i586.exe
::    After installation, please set a environment %JAVA_HOME% pointing to the JDK
::    installation directory, for example JAVA_HOME=C:\JDK7_WIN
::    We need a groovy compiler, which can be downloaded from http://groovy-lang.org/download.html
::    After installation, please set a environment %GROOVY_HOME% pointing to the groovy
::    compiler installation directory, for example GROOVY_HOME=C:\groovy-2.4.7
::
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Prepare environment variable for compilation and execution
set max=0
for /f "tokens=1* delims=-.0" %%A in ('dir /b /a-d %SAFSDIR%\lib\selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B
set SELENIUM_SERVER_JAR_LOC=%SAFSDIR%\lib\selenium-%max%
set COMPILE_CLASSPATH=%SELENIUM_SERVER_JAR_LOC%;%SAFSDIR%\lib\safs.jar;%CLASSPATH%
set OUTPUT_DIR=bin

set GROOVYC=%GROOVY_HOME%\bin\groovyc.bat
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
call "%GROOVYC%" -cp "%COMPILE_CLASSPATH%" -d %OUTPUT_DIR% src/com/sas/spock/safs/runner/tests/SpockExperimentWithRunner.groovy
javac -cp "%COMPILE_CLASSPATH%" -encoding UTF-8 -d %OUTPUT_DIR% -nowarn src/com/sas/spock/safs/runner/tests/*.java
Echo "Project has been compiled." 

:: 2. Execute SAFS test
"%EXECUTE%" -cp "%RUNTIME_CLASSPATH%" -Dsafs.project.config="calljunit_groovy_test.ini" org.safs.tools.drivers.SAFSDRIVER

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::