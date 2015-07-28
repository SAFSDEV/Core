
@REM Uncomment and Edit the following line to point to your 32-bit JVM on a 64-bit system.
@REM "C:\Program Files (x86)\Java\jre6\bin\java.exe" -Dsafs.project.config="C:\safs\project\TIDTest.ini" org.safs.tools.drivers.SAFSDRIVER

@REM Comment out (REM) the following line if using the line above.
@REM You must use the line above if you get 64-bit JVM errors regarding attempting 
@REM to load 32-bit DLLs in a 64-bit JVM.

java -Dsafs.processcontainer.ini="c:\safs\samples\droid\processcontainer.ini" org.safs.tools.drivers.STAFProcessContainer

