::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::

setlocal enableDelayedExpansion
set max=0
for /f "tokens=1* delims=-.0" %%A in ('dir /b /a-d %SELENIUM_PLUS%\libs\selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B
set SELENIUM_SERVER_JAR_LOC=%SELENIUM_PLUS%\libs\selenium-%max%

set CMDCLASSPATH="%SELENIUM_PLUS%\libs\seleniumplus.jar;%SELENIUM_PLUS%\libs\JSTAFEmbedded.jar;%SELENIUM_SERVER_JAR_LOC%"
set EXECUTE=%SELENIUM_PLUS%/Java/bin/java

:: DON'T MODIFY ABOVE SETTING UNLESS NECESSARY
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:: How to override App Map variable
:: EXAMPLE:  %EXECUTE% -cp %CMDCLASSPATH%;bin sample.testruns.TestRun1 -safsvar:GoogleUser=email@gmail.com

:: How to load external App Map order or Map file
:: EXAMPLE:  %EXECUTE% -cp %CMDCLASSPATH%;bin -Dtestdesigner.appmap.order=AppMap_en.order <package name>.TestRun1
:: EXAMPLE:  %EXECUTE% -cp bin;%CMDCLASSPATH% -Dtestdesigner.appmap.files=AppMap.map,AppMap_en.map <package name>.TestRun1

:: How to send email result
:: EXAMPLE:  %EXECUTE% -cp %CMDCLASSPATH% org.safs.tools.mail.Mailer -host mail.server.host -port 25 -from from@exmaple.com -to to1@exmaple.com;to2@example.com -subject "Test" -msg "Check msg in details" -attachment c:\seleniumplus\sample\logs\testcase1.xml;logs\testcase1.txt

%EXECUTE% -cp %CMDCLASSPATH%;bin <package.name>.TestCase1
