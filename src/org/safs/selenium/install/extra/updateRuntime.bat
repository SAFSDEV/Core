@ECHO OFF
REM This script is used to update "Selenium Plus".
REM This script could be used to update "SAFS".
REM Prerequisite: SeleniumPlus (SAFS) should have been successfully installed.
REM USAGE: updateRuntime.bat [ALL|SE|SAFS]
REM Example:
REM         updateRuntime.bat SAFS
REM         updateRuntime.bat SE
REM         updateRuntime.bat ALL

SETLOCAL
 
REM External update resource
REM SET SE_LIB_UPDATE=https://github.com/SAFSDEV/UpdateSite/releases/download/seleniumplus/SEPLUS.LIB.UPDATE.ZIP
REM SET SE_PLUGIN_UPDATE=https://github.com/SAFSDEV/UpdateSite/releases/download/seleniumplus/SEPLUS.PLUGIN.UPDATE.ZIP
REM SET SAFS_LIB_UPDATE=https://github.com/SAFSDEV/UpdateSite/releases/download/safs/SAFS.LIB.UPDATE.ZIP

REM Internal update resource
SET SE_LIB_UPDATE=http://safsbuild.na.sas.com:81/jenkins/job/SeleniumPlus/ws/updatesite/lib/latest/SEPLUS.LIB.UPDATE.ZIP
SET SE_PLUGIN_UPDATE=http://safsbuild.na.sas.com:81/jenkins/job/SeleniumPlus/ws/updatesite/plugin/latest/SEPLUS.PLUGIN.UPDATE.ZIP
SET SAFS_LIB_UPDATE=http://safsbuild.na.sas.com:81/jenkins/job/SAFS/ws/updatesite/lib/latest/SAFS.LIB.UPDATE.ZIP

REM GET PRODUCT NAME FROM INPUT PARAMETER, IT COULD BE ALL|SE|SAFS
REM The default value is ALL
REM SET PRODUCT=ALL

SET PARAM=%1

IF DEFINED PARAM (
    SET PRODUCT=%PARAM%
)

IF [%PRODUCT%]==[SE] (
    GOTO UPDATE_SE
)
IF [%PRODUCT%]==[SAFS] (
    GOTO UPDATE_SAFS
)

IF NOT [%PRODUCT%]==[ALL] (
    GOTO HELP
)

:UPDATE_SE
ECHO "Updating product SeleniumPlus."
SET BACKUP_LIBDIR=%SELENIUM_PLUS%\update_bak\libs
SET JAVA_EXE=%SELENIUM_PLUS%\Java\bin\java
IF NOT EXIST %BACKUP_LIBDIR% MKDIR %BACKUP_LIBDIR%
copy %SELENIUM_PLUS%\libs\safsupdate.jar %BACKUP_LIBDIR%\safsupdate.jar
%JAVA_EXE% -jar %BACKUP_LIBDIR%\safsupdate.jar -r -a -q -prompt:"SeleniumPlus Plugin Update" -s:"%SE_PLUGIN_UPDATE%" -t:"%SELENIUM_PLUS%\eclipse\plugins" -b:"%SELENIUM_PLUS%\eclipse\plugins\update_bak"
%JAVA_EXE% -jar %BACKUP_LIBDIR%\safsupdate.jar -r -a -q -prompt:"SeleniumPlus Libs Update" -s:"%SE_LIB_UPDATE%" -t:"%SELENIUM_PLUS%" -b:"%SELENIUM_PLUS%\update_bak"
IF NOT [%PRODUCT%]==[ALL] GOTO SHOW_INFO

:UPDATE_SAFS
ECHO "Updating product SAFS."
SET BACKUP_LIBDIR=%SAFSDIR%\update_bak\lib
SET JAVA_EXE=%SAFSDIR%\jre\bin\java
IF NOT EXIST %BACKUP_LIBDIR% MKDIR %BACKUP_LIBDIR%
copy %SAFSDIR%\lib\safsupdate.jar %BACKUP_LIBDIR%\safsupdate.jar
%JAVA_EXE% -jar %BACKUP_LIBDIR%\safsupdate.jar -r -a -q -prompt:"SAFS Libs Update" -s:"%SAFS_LIB_UPDATE%" -t:"%SAFSDIR%" -b:"%SAFSDIR%\update_bak"
IF NOT [%PRODUCT%]==[ALL] GOTO SHOW_INFO

GOTO END
:HELP
ECHO "You provided bad parameters!"
ECHO "USAGE: updateRuntime.bat [ALL|SE|SAFS]"
ECHO "Example:"
ECHO         "updateRuntime.bat SAFS"
ECHO         "updateRuntime.bat SE"
ECHO         "updateRuntime.bat ALL"
GOTO END

:SHOW_INFO
ECHO "Product %PRODUCT% has been updated."

:END

ENDLOCAL