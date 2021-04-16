@ECHO OFF
@CLS

Set JRE=%SAFSDIR%\jre\bin\java

IF EXIST "%STAFDIR%\install.properties" (
  ECHO According to the STAF's architecture, we use either 32 or 64 bit Java.
  FOR /f "usebackq eol=; tokens=1,2* delims==" %%i IN ("%STAFDIR%\install.properties") DO (
    rem ECHO %%i = %%j
    IF "%%i" == "architecture" (
      echo The installed STAF is %%j
      IF "%%j" == "64-bit" (
        set JRE=%SAFSDIR%\jre\Java64\jre\bin\java
      )
    )
  )
)

"%JRE%" org.safs.Log debug -file:"%SAFSDIR%\project\safsdebug.log"