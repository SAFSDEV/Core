
MKDIR "%SELENIUM_PLUS%\libs\update_bak"
COPY /Y "%SELENIUM_PLUS%\libs\safsupdate.jar" "%SELENIUM_PLUS%\libs\update_bak\safsupdate.jar"
"%SELENIUM_PLUS%\Java\bin\java.exe" -jar "%SELENIUM_PLUS%\libs\update_bak\safsupdate.jar" "-prompt:SeleniumPlus Libs Update" "-s:http://sourceforge.net/projects/safsdev/files/SeleniumPlus Updates/LibraryUpdates/SELENIUMPLUS.LIB.UPDATE.LATEST.ZIP" "-t:%SELENIUM_PLUS%\libs" "-b:%SELENIUM_PLUS%\libs\update_bak" 
@REM "%SELENIUM_PLUS%\Java\bin\java.exe" -jar "%SELENIUM_PLUS%\libs\update_bak\safsupdate.jar" "-prompt:SeleniumPlus PlugIn Update" "-s:http://sourceforge.net/projects/safsdev/files/SeleniumPlus Updates/PlugInUpdates/SeleniumPlusPlugInUpdate.zip" "-t:%SELENIUM_PLUS%\eclipse\plugins" "-b:%SELENIUM_PLUS%\eclipse\plugins\update_bak"
