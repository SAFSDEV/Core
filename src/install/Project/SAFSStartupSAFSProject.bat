staf local service add service safsmaps  library jstaf execute "%SAFSDIR%/lib/safsmaps.jar" OPTION JVM="%SAFSDIR%\jre\bin\java" PARMS dir "%SAFSDIR%\project\datapool"
staf local service add service safsvars  library jstaf execute "%SAFSDIR%/lib/safsvars.jar"
staf local service add service safsinput library jstaf execute c:/safs/lib/safsinput.jar PARMS dir "%SAFSDIR%\project\datapool"
staf local service add service safslogs  library jstaf execute "%SAFSDIR%/lib/safslogs.jar" PARMS dir "%SAFSDIR%\project\datapool\logs"
