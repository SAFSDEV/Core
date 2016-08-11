@ECHO off

REM ================================================================================
REM Purpose:
REM   This script is supposed to push/delete safs java model files to/from github.
REM Parameter:
REM   Workspace              	the full-path representing the workspace
REM   RepoPath              	the sub folder of git repository, where safs java model files reside.
REM   GitHubKnownHost           the full-path holding the public key for github
REM   GitHubPrivateKey          the full-path holding the private key for github
REM   Debug                     whatever if provided then show the debug message
REM Prerequisite:
REM 1. The GIT should have been installed and configured
REM 3. The OS should be configured to be able to push/delete
REM    from github automatically without asking user/password.
REM ================================================================================

SETLOCAL ENABLEDELAYEDEXPANSION
SET GIT_REPO_PREFIX=git@github.com:SAFSDEV/
SET WORKSPACE_FOLDER=%1
SET GITHUB_REPO=%2
SET GIT_KNOWNHOSTS=%3
SET GIT_PRIVATE_KEY=%4
SET DEBUG=%5

ECHO Pushing SAFS Java Model files ...
IF DEFINED DEBUG (
    ECHO The current environments are as below:
    SET
)

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

ECHO Change directory to the workspace folder %WORKSPACE_FOLDER%
PUSHD %WORKSPACE_FOLDER%
ECHO Change directory to sub folder %GITHUB_REPO%
PUSHD %GITHUB_REPO%
ECHO current working directory is %cd%

REM WE NEED TO PUSH THE MODIFIED FILES to Github

REM Firstly, we use 'git add' to add files to git stage area
git add *.java

REM Finally, we use 'git commit' and 'git push' to upload modified files to github
REM The OS should be configured correctly so that files can be pushed to remote automatically
ECHO === Pushing files to github ...
git config --global user.name "safsdev"
git config --global user.email safsdev@yourCompany.com
git commit -m "Updated by script automatically."
REM The git repository remote url should be set the ssh url format
git remote set-url origin %GIT_REPO_PREFIX%%GITHUB_REPO%.git
ECHO Push committed files to git remote repository ...
git push origin master

POPD
REM After this POPD, we are in the workspace folder
IF DEFINED DEBUG ECHO After pushing codes, we wentc back to directory %cd%

POPD

ENDLOCAL