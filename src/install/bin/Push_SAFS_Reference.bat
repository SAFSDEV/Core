@ECHO off

REM ================================================================================
REM Purpose:
REM   This script is supposed to push/delete safs reference files
REM   to/from sourceforge and github.
REM Parameter:
REM   RepoFullPath              the folder of git repository, where safs reference files reside.
REM   SourceForgeUser           the user name of sourceforge.
REM   SourceFogetPrivateKey     the fullpath holding the private key for sourceforge
REM   FTP put script            the ftp script to upload files
REM   FTP del script            the ftp script to delete files
REM   Debug                     whatever if provided then show the debug message
REM Prerequisite:
REM 1. The GIT should have been installed and configured
REM 2. PUTTY should have been installed 
REM 3. The OS should be configured to be able to push/delete
REM    from github/sourceforge automatically without asking user/password.
REM ================================================================================

SETLOCAL ENABLEDELAYEDEXPANSION
SET GITHUB_IO_FOLDER=%1
SET SF_USER=%2
SET SF_PRIVATE_KEY=%3
SET FTP_PUT_SCRIPT=%4
SET FTP_DEL_SCRIPT=%5
SET DEBUG=%6

SET FOLDER_COPY=doc.copy
SET FOLDER_DEL=doc.del

ECHO Push SAFS Document files under folder (github repository) at %GITHUB_IO_FOLDER%
IF DEFINED DEBUG (
    ECHO The current environments are as below:
    SET
    ECHO GITHUB_IO_FOLDER=%GITHUB_IO_FOLDER% FTP_PUT_SCRIPT=%FTP_PUT_SCRIPT% FTP_DEL_SCRIPT=%FTP_DEL_SCRIPT% DEBUG=%DEBUG%
)
ECHO Register the sourceforge host key so that sourceforge host will be trusted through the ssh protocol.
REM Add blindly, it is not secure.
REM echo y | psftp -i %SF_PRIVATE_KEY% %SF_USER%@web.sourceforge.net
REM reg query HKEY_CURRENT_USER\Software\SimonTatham\PuTTY\SshHostKeys /v rsa2@22:web.sourceforge.net > C:/reg.key1.txt
REM Add explicitly, it is secure.
REG ADD HKEY_CURRENT_USER\Software\SimonTatham\PuTTY\SshHostKeys /v rsa2@22:web.sourceforge.net /t REG_SZ /f /d 0x23,0xdae89f1d96cd7b1c3a7176f2835267cc38ad2f956162cd04eb91e4fed2c03e67269b91ae886794a08fc1d1e512345b1bab3c20c2bb6d8e7cca30a8862cde425959a537521718601f2d8b10c0aeb002eee4605ce4a8fe2373073926bf543d1a4975820aef7ded4849b369be2a7393aaf22cca40a21ac5735eec58262f5e478d2a5ab45b7dc3fa2dfc21d2efc15402bde3dd1c1e9070a6fe384f7aa865300802fa35203496c5b770e584369131ad0d61a995ce65fc0f8eebb6605b10353795de807b35813d44792e1ece0cec257d5239f0d01ffda60d966be5d936626ae0424daece3d84b25c99c83db41783a610943927342af42bbebd1237889407d8f3b678b7

PUSHD %GITHUB_IO_FOLDER%
IF DEFINED DEBUG ECHO current working directory is %cd%

MKDIR %FOLDER_COPY%
MKDIR %FOLDER_DEL%

REM WE NEED TO PUSH THE MODIFIED HTM/HTML FILES to Github and SourceForge
REM 1. To Github, we can do it by git commands
REM 2. To SourceForge, we use command pscp, we need to open the 'pageant' adding the private key

REM Firstly, we use 'git add' to add files to git stage area
git add *.htm
git add *.html
git add *.js
git add *.css

REM Then, we use the 'git status' to get the modified files, which will be uploaded to sourceforge
FOR /f /F "usebackq tokens=1,2* " %%i IN (`git status --short`) DO (
    SET OPERATION=NONE
    IF [%%i]==[M] SET OPERATION=ADD
    IF [%%i]==[A] SET OPERATION=ADD
    IF [%%i]==[D] SET OPERATION=DELETE
    IF DEFINED DEBUG ECHO Git status %%i -- !OPERATION! %%j
    
    IF [!OPERATION!]==[ADD] (
        ECHO ... Copying file %%j to folder '%FOLDER_COPY%'.
        COPY %%j %FOLDER_COPY%
        rem pscp -i %SF_PRIVATE_KEY% %%j %SF_USER%,safsdev@web.sourceforge.net:/home/groups/s/sa/safsdev/htdocs/sqabasic2000/
    )
    IF [!OPERATION!]==[DELETE] (
        ECHO ... Copying file %%j to folder '%FOLDER_COPY%'.
        COPY %%j %FOLDER_DEL%
        rem TODO Find a way to delete file from sourceforge pscp %%j %SF_USER%,safsdev@web.sourceforge.net:/home/groups/s/sa/safsdev/htdocs/sqabasic2000/
    )
)

ECHO Pushing files to sourceforge ...
psftp -i %SF_PRIVATE_KEY% -b %FTP_PUT_SCRIPT% %SF_USER%,safsdev@web.sourceforge.net
REM ECHO Deleting files from sourceforge ...
REM psftp -i %SF_PRIVATE_KEY% -b %FTP_DEL_SCRIPT% %SF_USER%,safsdev@web.sourceforge.net

RMDIR /S /Q %FOLDER_COPY%
RMDIR /S /Q %FOLDER_DEL%

REM Finally, we use 'git commit' and 'git push' to upload modified files to github
REM The OS should be configured correctly so that files can be pushed to remote automatically
git commit -m "Updated by script automatically."
git remote set-url origin git@github.com:SAFSDEV/safsdev.github.io.git
git remote -v
ECHO Push git commit to remote GIT repository ...
git push origin master

POPD

ENDLOCAL