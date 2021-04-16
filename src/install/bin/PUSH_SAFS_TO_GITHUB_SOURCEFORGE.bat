@ECHO off

REM ================================================================================
REM Purpose:
REM   This script is supposed to push/delete "safs reference files", "static
REM   web documents", and "API documents" to/from github.
REM Parameter:
REM   Workspace                 the full-path representing the workspace.
REM   RepoPath                  the git-repository sub-folder (relative to workspace), where git meta-data resides
REM   RepoWorkPath              the repository work sub-folder (relative to workspace), where safs-reference/static-document/API files reside.
REM   FileTypes                 the file holding the file-types to push.
REM   GitHubKnownHost           the full-path holding the public key for github
REM   GitHubPrivateKey          the full-path holding the private key for github
REM   Debug                     whatever if provided then show the debug message
REM Prerequisite:
REM 1. The GIT should have been installed and configured
REM 2. PUTTY should have been installed 
REM 3. The OS should be configured to be able to push/delete
REM    from github automatically without asking user/password.
REM History:
REM   JUL 12, 2017	(Lei Wang) Do NOT update/upload to SourceForge anymore.
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
SET GIT_KNOWNHOSTS=%1
SHIFT
SET GIT_PRIVATE_KEY=%1
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

ECHO We push SAFS files to destination GITHUB

REM WE PUSH SAFS FILES to GitHub
REM 1. To GitHub, we can do it by git commands. This needs ssh connection.

ECHO Firstly we are going to prepare the SSH connection settings for GITHUB.

ECHO Prepare SSH configuration files for user "%USERNAME%" to push to GIT automatically
IF NOT EXIST "%USERPROFILE%\.ssh\" (
	IF DEFINED DEBUG (
    	ECHO MKDIR "%USERPROFILE%\.ssh\"
    )
    REM It is very strange that I cannot see the folder "C:\Windows\system32\config\systemprofile\.ssh\" after the script makes directory.
	MKDIR "%USERPROFILE%\.ssh\"
)
IF NOT EXIST "%USERPROFILE%\.ssh\known_hosts" (
	IF DEFINED DEBUG (
    	ECHO COPY "%GIT_KNOWNHOSTS%" "%USERPROFILE%\.ssh\"
    )
	COPY "%GIT_KNOWNHOSTS%" "%USERPROFILE%\.ssh\"
)
IF NOT EXIST "%USERPROFILE%\.ssh\id_rsa" (
	IF DEFINED DEBUG (
    	ECHO COPY "%GIT_PRIVATE_KEY%" "%USERPROFILE%\.ssh\"
    )
	COPY "%GIT_PRIVATE_KEY%" "%USERPROFILE%\.ssh\"
)
ECHO :REMINDER: "%USERPROFILE%\.ssh\known_hosts" should trust host github.com
ECHO :REMINDER: "%USERPROFILE%\.ssh\id_rsa" should be trusted by host github.com

REM The git command will work in the repository folder.
ECHO Change directory to the GIT repository folder "%FULL_PATH_REPO%", which contains GITHUB meta data.
PUSHD %FULL_PATH_REPO%
IF DEFINED DEBUG ECHO The current working directory is %cd%

REM Firstly, we use 'git add' to add files to git stage area
IF DEFINED DEBUG ECHO Git-Adding files defined in "%cd%\%FILE_TYPES%", to git stage area.
FOR /f "tokens=1* " %%i IN (%FILE_TYPES%) DO (
    IF DEFINED DEBUG ECHO git add %%i
    git add %%i
)


REM Finally, we use 'git commit' and 'git push' to upload modified files to github
REM The OS should be configured correctly so that files can be pushed to remote automatically
ECHO === Pushing files to github ...
git config --global user.name "safsdev"
git config --global user.email safsdev@sas.com
git commit -m "Updated by script automatically."
REM The git repository remote url should be set the ssh url format
REM git remote set-url origin git@github.com:SAFSDEV/safsdev.github.io.git
git remote set-url origin %GIT_REPO_PREFIX%%GITHUB_REPO%.git
ECHO Push committed files to git remote repository ...
git push origin master


POPD

ENDLOCAL