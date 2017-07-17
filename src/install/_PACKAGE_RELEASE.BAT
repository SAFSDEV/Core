del /Q SAFSInstall.ZIP
del /Q SAFSWinReleaseCandidate.ZIP

del /Q .\install\SAFSInstall.ZIP
del /Q .\install\STAF3*.exe

jar cfM SAFSInstall.ZIP *.htm *.txt *.ini
REM Make install/uninstall a COPY operation during install like the JRE: 
jar ufM SAFSInstall.ZIP .\bin\ .\data\ .\datastorej\ .\datastorej_logs\ .\doc\
jar ufM SAFSInstall.ZIP .\keywords\ .\lib\ .\ocr\ .\Project\ .\samples\
jar ufM SAFSInstall.ZIP .\source\ .\TCAFS\

copy SAFSInstall.ZIP .\install\
copy STAF3*.exe .\install\

jar cfM SAFSWinReleaseCandidate.ZIP *.htm *.txt Setup.bat
jar ufM SAFSWinReleaseCandidate.ZIP .\install\ .\uninstall\
jar ufM SAFSWinReleaseCandidate.ZIP .\jre\

REM Install Under Development
REM DO NOT copy /Y SAFSWinReleaseCandidate.ZIP \Public\SAFS_Release\
REM 
REM pause CTRL-C to abort pscp push to "SAFS Win/ReleaseCandidates", any other key to continue...
REM pscp SAFSWinReleaseCandidate.ZIP "username@frs.sourceforge.net:/home/frs/project/safsdev/SAFS Win/ReleaseCandidates/"
