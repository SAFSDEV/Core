REM
REM CD to where the latest classes were built
REM This CD is not necessary if using prepackaged classes already in the CLASSPATH
REM

cd %SAFSDIR%\samples\Droid\SoloRemoteControl\bin

REM
REM Add those new (modified?) classes to the CLASSPATH
REM This SET is not necessary if using classes already in the CLASSPATH
REM

SET CLASSPATH=.;%CLASSPATH%

REM
REM execute either class: com.jayway.android.robotium.remotecontrol.solo.SoloTest  (superclass)
REM execute either class: com.jayway.android.robotium.remotecontrol.MyTest         (subclass)
REM avd=yourAVD, or remove it to locate a real connected device.
REM aut=path to the APK, or use -noaut if the APK is already installed on the device.
REM 
%SAFSDIR%\jre\bin\java -Dandroid-home="%ANDROID_HOME%" com.jayway.android.robotium.remotecontrol.MyTest avd=SprintEvo aut=C:\\SAFS\samples\\Droid\\SpinnerSample\\bin\\SpinnerActivity-debug.apk messenger=c:\\SAFS\\sample\\Droid\\SAFSTCPMessenger\\bin\\SAFSTCPMessenger-debug.apk runner=c:\\SAFS\\sample\\Droid\\SAFSTestRunner\\bin\\SAFSTestRunner-debug.apk instrument=org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner
