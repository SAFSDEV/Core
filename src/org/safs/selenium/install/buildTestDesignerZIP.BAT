
DEL "%CD%\TestDesigner.ZIP"
COPY /Y "%CD%\eclipse.ini" "%CD%\eclipse\"
COPY /Y /B "\\D77601\public\plug-ins\SeleniumPlus\plugins\Seleniumplus_plugin_*.jar" "%CD%\eclipse\plugins\Seleniumplus_plugin_1.0.0.201312102336.jar"

SET JAR="%CD%\Java\bin\jar.exe"

%JAR% cMf TestDesigner.ZIP Setup.bat InstallInstructions.txt libs\ install\ uninstall\ Java\ extra\ eclipse\ doc\ .metadata\.lock .metadata\version.ini 

%JAR% uf TestDesigner.ZIP .metadata\.plugins\org.eclipse.core.runtime\ 
%JAR% uf TestDesigner.ZIP .metadata\.plugins\org.eclipse.core.resources\
%JAR% uf TestDesigner.ZIP .metadata\.plugins\org.eclipse.ui.workbench\ 
%JAR% uf TestDesigner.ZIP .metadata\.plugins\org.eclipse.e4.workbench\ 
%JAR% uf TestDesigner.ZIP .metadata\.plugins\org.eclipse.debug.core\.launches\ 

