@ECHO off

REM ================================================================================
REM Purpose:
REM   This script is supposed to push/delete "safs reference files", "static
REM   web documents", and "API documents" to/from sourceforge and github.
REM Parameter:
REM   Workspace                 the full-path representing the workspace.
REM   RepoPath                  the git-repository sub-folder (relative to workspace), where git meta-data resides
REM   RepoWorkPath              the repository work sub-folder (relative to workspace), where safs-reference/static-document/API files reside.
REM   FileTypes                 the file holding the file-types to push.
REM   SourceForgeUser           the user name of sourceforge.
REM   SourceFogetPrivateKey     the full-path holding the private key for sourceforge
REM   FTP put script            the ftp script to upload files
REM   FTP del script            the ftp script to delete files
REM   GitHubKnownHost           the full-path holding the public key for github
REM   GitHubPrivateKey          the full-path holding the private key for github
REM   Destination               the destination to push code. It could be SF or GIT or SF GIT or NONE
REM   Debug                     whatever if provided then show the debug message
REM Prerequisite:
REM 1. The GIT should have been installed and configured
REM 2. PUTTY should have been installed 
REM 3. The OS should be configured to be able to push/delete
REM    from github/sourceforge automatically without asking user/password.
REM ================================================================================

SETLOCAL ENABLEDELAYEDEXPANSION
SET GIT_REPO_PREFIX=git@github.com:SAFSDEV/
SET WORKSPACE=%1
SHIFT
SET GITHUB_REPO=%1
SHIFT
SET GITHUB_REPO_WORKDIR=%1
SHIFT
SET FILE_TYPES=%1
SHIFT
SET SF_USER=%1
SHIFT
SET SF_PRIVATE_KEY=%1
SHIFT
SET FTP_PUT_SCRIPT=%1
SHIFT
SET FTP_DEL_SCRIPT=%1
SHIFT
SET GIT_KNOWNHOSTS=%1
SHIFT
SET GIT_PRIVATE_KEY=%1
SHIFT
SET DESTINATION=%1
SHIFT
SET DEBUG=%1

SET FOLDER_COPY=doc.copy
SET FOLDER_DEL=doc.del

SET FULL_PATH_REPO=%WORKSPACE%\%GITHUB_REPO%
SET FULL_PATH_REPO_WDIR=%WORKSPACE%\%GITHUB_REPO_WORKDIR%

ECHO Push SAFS Reference/static-document/api files under github repository working folder "%FULL_PATH_REPO_WDIR%"
IF DEFINED DEBUG (
    ECHO The current environments are as below:
    SET
)

ECHO We push SAFS files to destination %DESTINATION%
if not [%DESTINATION:SF=%]==[%DESTINATION%] set PUSH_TO_SF=true
if not [%DESTINATION:GIT=%]==[%DESTINATION%] set PUSH_TO_GIT=true
IF DEFINED PUSH_TO_SF echo Files will be pushed to SOURCEFORGE
IF DEFINED PUSH_TO_GIT echo Files will be pushed to GITHUB

REM WE PUSH SAFS FILES to Github and SourceForge
REM 1. To Github, we can do it by git commands. This needs ssh connection.
REM 2. To SourceForge, we use command psftp to connect server, then copy files. This also needs ssh connection.

ECHO Firstly we are going to prepare the SSH connection settings for GITHUB and SOURCEFORGE.

ECHO Register the sourceforge host key so that sourceforge host will be trusted through the ssh protocol.
REM Add blindly, it is not secure.
REM echo y | psftp -i %SF_PRIVATE_KEY% %SF_USER%@web.sourceforge.net
REM reg query HKEY_CURRENT_USER\Software\SimonTatham\PuTTY\SshHostKeys /v rsa2@22:web.sourceforge.net > C:/reg.key1.txt
REM Add explicitly, it is secure.
REG ADD HKEY_CURRENT_USER\Software\SimonTatham\PuTTY\SshHostKeys /v rsa2@22:web.sourceforge.net /t REG_SZ /f /d 0x23,0xdae89f1d96cd7b1c3a7176f2835267cc38ad2f956162cd04eb91e4fed2c03e67269b91ae886794a08fc1d1e512345b1bab3c20c2bb6d8e7cca30a8862cde425959a537521718601f2d8b10c0aeb002eee4605ce4a8fe2373073926bf543d1a4975820aef7ded4849b369be2a7393aaf22cca40a21ac5735eec58262f5e478d2a5ab45b7dc3fa2dfc21d2efc15402bde3dd1c1e9070a6fe384f7aa865300802fa35203496c5b770e584369131ad0d61a995ce65fc0f8eebb6605b10353795de807b35813d44792e1ece0cec257d5239f0d01ffda60d966be5d936626ae0424daece3d84b25c99c83db41783a610943927342af42bbebd1237889407d8f3b678b7

ECHO Prepare SSH configuration files for user "%USERNAME%" to push to GIT automatically
IF DEFINED DEBUG (
    ECHO MKDIR "%USERPROFILE%\.ssh\"
    ECHO COPY "%GIT_KNOWNHOSTS%" "%USERPROFILE%\.ssh\"
    ECHO COPY "%GIT_PRIVATE_KEY%" "%USERPROFILE%\.ssh\"
)
REM It is very strange that I cannot see the folder "C:\Windows\system32\config\systemprofile\.ssh\" after the script makes directory.
MKDIR "%USERPROFILE%\.ssh\"
COPY "%GIT_KNOWNHOSTS%" "%USERPROFILE%\.ssh\"
COPY "%GIT_PRIVATE_KEY%" "%USERPROFILE%\.ssh\"


REM The git command will work in the repository folder.
ECHO Change directory to the GIT repository folder "%FULL_PATH_REPO%", which contains GITHUB meta data.
PUSHD %FULL_PATH_REPO%
IF DEFINED DEBUG ECHO The current working directory is %cd%

REM Firstly, we use 'git add' to add files to git stage area
IF DEFINED DEBUG ECHO Git-Adding files defined in "%cd%\%FILE_TYPES%"
FOR /f "tokens=1* " %%i IN (%FILE_TYPES%) DO (
    IF DEFINED DEBUG ECHO git add %%i
    git add %%i
)

IF DEFINED PUSH_TO_SF (
    RMDIR /S /Q %FOLDER_COPY%
    RMDIR /S /Q %FOLDER_DEL%
    MKDIR %FOLDER_COPY%
    MKDIR %FOLDER_DEL%
    
    REM Then, we use the 'git status' to get the modified files, which will be uploaded to SOURCEFORGE
    FOR /F "usebackq tokens=1,2* " %%i IN (`git status --short`) DO (
        SET Modified_File=%%j
        SET Modified_File=!Modified_File:/=\!
        
        SET OPERATION=NONE
        IF [%%i]==[M] SET OPERATION=ADD
        IF [%%i]==[A] SET OPERATION=ADD
        IF [%%i]==[D] SET OPERATION=DELETE
        IF DEFINED DEBUG ECHO Git status %%i -- !OPERATION! !Modified_File!
        
        IF [!OPERATION!]==[ADD] (
            IF DEFINED DEBUG ECHO ... %systemroot%\system32\xcopy /c /y /q %FULL_PATH_REPO_WDIR%\!Modified_File! %FOLDER_COPY%\!Modified_File!
            ECHO F | %systemroot%\system32\xcopy /c /y /q %FULL_PATH_REPO_WDIR%\!Modified_File! %FOLDER_COPY%\!Modified_File!
        )
        IF [!OPERATION!]==[DELETE] (
            IF DEFINED DEBUG ECHO ... %systemroot%\system32\xcopy /c /y /q %FULL_PATH_REPO_WDIR%\!Modified_File! %FOLDER_COPY%\!Modified_File!
            ECHO F | %systemroot%\system32\xcopy /c /y /q %FULL_PATH_REPO_WDIR%\!Modified_File! %FOLDER_COPY%\!Modified_File!
        )
    )
    
    ECHO === Pushing files to sourceforge ...
    echo psftp -i %SF_PRIVATE_KEY% -b %FTP_PUT_SCRIPT% %SF_USER%,safsdev@web.sourceforge.net
    REM ECHO === Deleting files from sourceforge ...
    REM psftp -i %SF_PRIVATE_KEY% -b %FTP_DEL_SCRIPT% %SF_USER%,safsdev@web.sourceforge.net
    
    RMDIR /S /Q %FOLDER_COPY%
    RMDIR /S /Q %FOLDER_DEL%
)

IF DEFINED PUSH_TO_GIT (
    REM Finally, we use 'git commit' and 'git push' to upload modified files to github
    REM The OS should be configured correctly so that files can be pushed to remote automatically
    ECHO === Pushing files to github ...
    git config --global user.name "safsdev"
    git config --global user.email safsdev@yourCompany.com
    echo git commit -m "Updated by script automatically."
    REM The git repository remote url should be set the ssh url format
    REM git remote set-url origin git@github.com:SAFSDEV/safsdev.github.io.git
    echo git remote set-url origin %GIT_REPO_PREFIX%%GITHUB_REPO%.git
    ECHO Push committed files to git remote repository ...
    echo git push origin master
)

POPD

ENDLOCAL