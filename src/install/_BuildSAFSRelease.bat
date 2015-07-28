c:
cd\safs

SET JRE=C:\j2sdk1.4.2_05\bin
SET RELEASE=2007.04.13

del -Q SAFSInstall.ZIP
del -Q SAFSRelease*.ZIP

:Build_SAFSInstallZIP

%JRE%\jar cMf SAFSInstall.ZIP bin\*.*
%JRE%\jar uMf SAFSInstall.ZIP data\*.*
%JRE%\jar uMf SAFSInstall.ZIP datastorej\*.*
%JRE%\jar uMf SAFSInstall.ZIP doc\*.*
%JRE%\jar uMf SAFSInstall.ZIP include\*.*
%JRE%\jar uMf SAFSInstall.ZIP keywords\*.*
%JRE%\jar uMf SAFSInstall.ZIP lib\*.*
%JRE%\jar uMf SAFSInstall.ZIP Project\*.*
%JRE%\jar uMf SAFSInstall.ZIP samples\*.*
%JRE%\jar uMf SAFSInstall.ZIP source\*.*
%JRE%\jar uMf SAFSInstall.ZIP *.vbs
%JRE%\jar uMf SAFSInstall.ZIP *.wsf
%JRE%\jar uMf SAFSInstall.ZIP *.txt
%JRE%\jar uMf SAFSInstall.ZIP *.htm
%JRE%\jar uMf SAFSInstall.ZIP *.ini

:Build_SAFSReleaseZIP

%JRE%\jar cMf SAFSRelease%RELEASE%.ZIP SAFSInstall.jar
%JRE%\jar uMf SAFSRelease%RELEASE%.ZIP SAFSInstall.ZIP
%JRE%\jar uMf SAFSRelease%RELEASE%.ZIP GNU*.*
%JRE%\jar uMf SAFSRelease%RELEASE%.ZIP A_README.txt
%JRE%\jar uMf SAFSRelease%RELEASE%.ZIP *.vbs
%JRE%\jar uMf SAFSRelease%RELEASE%.ZIP *.wsf
%JRE%\jar uMf SAFSRelease%RELEASE%.ZIP *.htm
%JRE%\jar uMf SAFSRelease%RELEASE%.ZIP *.htm
%JRE%\jar uMf SAFSRelease%RELEASE%.ZIP STAF*.jar

