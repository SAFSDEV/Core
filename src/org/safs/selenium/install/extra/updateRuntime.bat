@ECHO OFF
REM This script is used to update "Selenium Plus".
REM TODO Future This script could be used to update "SAFS".
REM  Prerequisite: SeleniumPlus (SAFS) should have been successfully installed.
 
REM External update resource
REM SET SE_LIB_UPDATE=https://github.com/SAFSDEV/UpdateSite/releases/download/seleniumplus/SEPLUS.LIB.UPDATE.ZIP
REM SET SE_PLUGIN_UPDATE=https://github.com/SAFSDEV/UpdateSite/releases/download/seleniumplus/SEPLUS.PLUGIN.UPDATE.ZIP
REM SET SAFS_LIB_UPDATE=https://github.com/SAFSDEV/UpdateSite/releases/download/safs/SAFS.LIB.UPDATE.ZIP

REM Internal update resource
SET SE_LIB_UPDATE=http://safsbuild.na.sas.com:81/jenkins/job/SeleniumPlus/ws/updatesite/lib/latest/SEPLUS.LIB.UPDATE.ZIP
SET SE_PLUGIN_UPDATE=http://safsbuild.na.sas.com:81/jenkins/job/SeleniumPlus/ws/updatesite/plugin/latest/SEPLUS.PLUGIN.UPDATE.ZIP
SET SAFS_LIB_UPDATE=http://safsbuild.na.sas.com:81/jenkins/job/SAFS/ws/updatesite/lib/latest/SAFS.LIB.UPDATE.ZIP

copy %SELENIUM_PLUS%\libs\safsupdate.jar %SELENIUM_PLUS%\update_bak\libs\safsupdate.jar
java -jar %SELENIUM_PLUS%\update_bak\libs\safsupdate.jar -r -a -q -prompt:"SeleniumPlus Plugin Update" -s:"%SE_PLUGIN_UPDATE%" -t:"%SELENIUM_PLUS%\eclipse\plugins" -b:"%SELENIUM_PLUS%\eclipse\plugins\update_bak"
java -jar %SELENIUM_PLUS%\update_bak\libs\safsupdate.jar -r -a -q -prompt:"SeleniumPlus Libs Update" -s:"%SE_LIB_UPDATE%" -t:"%SELENIUM_PLUS%" -b:"%SELENIUM_PLUS%\update_bak"