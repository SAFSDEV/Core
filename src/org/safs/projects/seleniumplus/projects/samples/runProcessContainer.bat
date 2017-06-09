::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::

set CMDCLASSPATH="%SELENIUM_PLUS%/libs/seleniumplus.jar;%SELENIUM_PLUS%/libs/JSTAFEmbedded.jar"
set EXECUTE=%SELENIUM_PLUS%/Java/bin/java

:: DON'T MODIFY ABOVE SETTING UNLESS NECESSARY
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

%EXECUTE% -cp %CMDCLASSPATH% org.safs.selenium.spc.WDSPC
