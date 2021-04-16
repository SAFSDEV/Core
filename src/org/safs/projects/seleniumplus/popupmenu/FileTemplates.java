/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.projects.seleniumplus.popupmenu;
/**
 * MAY 11, 2018	(Lei Wang) Modified testINI(): added configuration for 'SAFS_DATA_SERVICE'.
 *                        Added method springConfig(): get spring configuration file template.
 *                        Added some other general methods.
 * MAY 22, 2018	(Lei Wang) Moved some code from testINI() to testINIContents(): string result is easier to modify.
 * SEP 25, 2018	(Lei Wang) Modified springConfig(): import the springConfig.xml of SAFS core project.
 *                                                remove <aop:aspectj-autoproxy/>, which will be provided by Core's springConfig.xml
 *                                                remove package org.safs from <context:component-scan/>, which will be provided by Core's springConfig.xml
 * MAR 11, 2019	(Lei Wang) Modified testINIContents(): removed the section [SAFS_DIRECTORIES] and [SAFS_DATA_SERVICE], which are commonly defined
 *                                                    in the default configuration file %SELENIUM_PLUS%/extra/automation/safstid.ini
 *
 */
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FileTemplates {

	/** "server.url=" used in INI configuration file to represent SAFS Data Service URL */
	public static final String TOKEN_SERVER_URL_EQUAL = "server.url=";
	/**
	 * @param seleniumloc String, the SeleniumPlus installation path.
	 * @param projectName String, the project's name
	 * @return String, the content of INI file with this working template.
	 */
	public static String testINIContents(String seleniumloc,String projectName){

		String contents =	"\n[STAF]\n" +
  				            "# Comment out the line below (using ; or #) to turn OFF the Debug Log to improve performance slightly.\n" +
				            "EmbedDebug=\"DebugLog.txt\"\n" +
							"\n" +

				            "[SAFS_DRIVER]\n" +
				            //Use the separator in Unix-format, windows separator cannot work on Linux machine
							"DriverRoot=\"%SELENIUM_PLUS%/extra/automation\"\n" +
							"# Uncomment showMonitor below to use the SAFS Monitor during testing.\n" +
							"# showMonitor=True\n" +
							"\n" +

//							"[SAFS_DIRECTORIES]\n" +
//							"DATADIR=Maps\n" +
//							"BENCHDIR=Benchmarks\n" +
//							"DIFFDIR=Diffs\n" +
//							"LOGDIR=Logs\n" +
//							"TESTDIR=Actuals\n" +
//							"\n" +

							"[SAFS_SELENIUM]\n" +
							"# Grid or Remote Selenium Server\n" +
							"#SELENIUMHOST=host.domain.com\n" +
							"#SELENIUMPORT=4444\n" +
							"# BROWSER defines the browser on which to run test.\n" +
							"# BROWSER=explorer | firefox | chrome\n" +
							"BROWSER=chrome\n\n"+

							"[SAFS_TEST]\n" +
							"TestName=\""+ projectName + "\"\n" +
							"TestLevel=\"Cycle\"\n" +
							"CycleSeparator=\"\t\"\n" +
							"# CycleLogName=\""+ projectName + "\"\n" +
							"\n" +

							"# 3 logmodes all enabled below.\n" +
							"# Delete those you will not use to improve performance.\n" +
							"CycleLogMode=\"TEXTLOG CONSOLELOG XMLLOG\"\n" +
							"\n" +

							"# secsWaitForWindow=30\n" +
							"# secsWaitForComponent=30\n" +
							"\n" +

//							"[SAFS_DATA_SERVICE]\n"+
//							"#protocol=http\n"+
//							"#host=localhost\n"+
//							"#port=8080\n"+
//							"#base.name=safsdata\n"+
//							"#server.url will override the 'protocol', 'host', 'port' and 'base.name' settings\n"+
//							TOKEN_SERVER_URL_EQUAL+"http://safsdata.server:8080/safsdata\n" +

                            "";

		return contents;
	}

	/**
	 * @param seleniumloc String, the SeleniumPlus installation path.
	 * @param projectName String, the project's name
	 * @return InputStream, the content of INI file with this working template.
	 */
	public static InputStream testINI(String seleniumloc,String projectName) {
		String contents = testINIContents(seleniumloc, projectName);

		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * Create spring configuration file for a test project.<br/>
	 * Currently this configuration file will do:
	 * <ol>
	 * <li>Automatically scan the test project's base package and package 'org.safs'.
	 * <li>Support spring AOP.
	 * </ol>
	 *
	 * @param testBasePackage String, the base package of a test project.
	 * @return String, the spring configuration file.
	 */
	public static InputStream springConfig(String testBasePackage) {

		String contents =
		"<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n\n"+

		"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n"+
		"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:context=\"http://www.springframework.org/schema/context\"\n"+
		"	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n"+
		"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n"+
		"  http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\n"+
		"  http://www.springframework.org/schema/aop\n"+
		"  http://www.springframework.org/schema/aop/spring-aop-3.0.xsd\n"+
		"  http://www.springframework.org/schema/context\n"+
		"  http://www.springframework.org/schema/context/spring-context-3.0.xsd\">\n\n"+

		"	<import resource=\"classpath:springConfig.xml\"/>\n"+
		"	<context:component-scan base-package=\""+testBasePackage+"\" />\n\n"+

		"</beans>";
		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * Create log4j configuration file for selenium usage to avoid some error console messages.<br/>
	 *
	 * @return String, the log4j configuration file.
	 */
	public static InputStream log4j2Config() {

		String contents =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"+
		"<Configuration status=\"WARN\">\n"+
		"  <Appenders>\n"+
		"    <Console name=\"Console\" target=\"SYSTEM_OUT\">\n"+
		"      <PatternLayout pattern=\"%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n\"/>\n"+
		"    </Console>\n"+
		"  </Appenders>\n"+
		"  <Loggers>\n"+
		"    <Root level=\"error\">\n"+
		"      <AppenderRef ref=\"Console\"/>\n"+
		"    </Root>\n"+
		"  </Loggers>\n"+
		"</Configuration>";

		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 *
	 * @param fullQualifiedTestClassName String, the full test case name including the package name.
	 * @return String, the batch file to run this test case.
	 */
	public static InputStream runAutomationBatch(String fullQualifiedTestClassName) {

		String contents =
		"::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n"+
		"@ECHO OFF\n\n"+

		"setlocal enableDelayedExpansion\n"+
		"REM set max=0\n"+
		"REM for /f \"tokens=1* delims=-.0\" %%A in ('dir /b /a-d %SELENIUM_PLUS%\\libs\\selenium-server-standalone*.jar') do if %%B gtr !max! set max=%%B\n"+
		"REM set SELENIUM_SERVER_JAR_LOC=%SELENIUM_PLUS%\\libs\\selenium-%max%\n\n"+

		"REM Get the latest selenium-server-standalone jar, list the selenium-server-standalone jar in order (by date/time), the last one is the latest.\n"+
		"for /f %%A in ('dir %SELENIUM_PLUS%\\libs\\selenium-server-standalone*.jar /B /O:D') do set SELENIUM_SERVER_JAR_LOC=%%A\n"+
		"set SELENIUM_SERVER_JAR_LOC=%SELENIUM_PLUS%\\libs\\%SELENIUM_SERVER_JAR_LOC%\n\n"+

		"set CMDCLASSPATH=%SELENIUM_PLUS%\\libs\\seleniumplus.jar;%SELENIUM_PLUS%\\libs\\JSTAFEmbedded.jar;%SELENIUM_SERVER_JAR_LOC%\n"+
		"set EXECUTE=%SELENIUM_PLUS%/Java/bin/java\n"+

		":: DON'T MODIFY ABOVE SETTING UNLESS NECESSARY\n"+
		":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::\n\n"+

		":: How to override App Map variable\n"+
		":: EXAMPLE:  %EXECUTE% -cp %CMDCLASSPATH%;bin sample.testruns.TestRun1 -safsvar:GoogleUser=email@gmail.com\n"+

		":: How to load external App Map order or Map file\n"+
		":: EXAMPLE:  %EXECUTE% -cp %CMDCLASSPATH%;bin -Dtestdesigner.appmap.order=AppMap_en.order <package name>.TestRun1\n"+
		":: EXAMPLE:  %EXECUTE% -cp bin;%CMDCLASSPATH% -Dtestdesigner.appmap.files=AppMap.map,AppMap_en.map <package name>.TestRun1\n"+

		":: How to send email result\n"+
		":: EXAMPLE:  %EXECUTE% -cp %CMDCLASSPATH% org.safs.tools.mail.Mailer -host mail.server.host -port 25 -from from@exmaple.com -to to1@exmaple.com;to2@example.com -subject \"Test\" -msg \"Check msg in details\" -attachment c:\\seleniumplus\\sample\\logs\\testcase1.xml;logs\\testcase1.txt\n"+

		"REM %EXECUTE% -cp \"%CMDCLASSPATH%;bin\" <package.name>.TestCase1\n"+
		"%EXECUTE% -cp \"%CMDCLASSPATH%;bin\" "+fullQualifiedTestClassName+"\n";

		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * The type of the test source class.
	 */
	public enum TestFileType{
		TestClass, TestRunClass, TestCycle, TestSuite, TestCase
	}
	public enum MapFileType{
		Map, MapEn, Order
	}
	public enum IniFileType{
		Normal
	}

	/**
	 * @param sourceFolder String, the name of source folder in a project.
	 * @param classnameOrPackageName String, the full qualified classname or package.
	 * @return String, the path (including source folder).
	 */
	public static String toProjectPath(String sourceFolder, String classnameOrPackageName){
		if(classnameOrPackageName.startsWith(sourceFolder)){
			return classnameOrPackageName.replaceAll("\\.", "/");
		}
		return sourceFolder+"/"+classnameOrPackageName.replaceAll("\\.", "/");
	}

	/**
	 * @param sourceFolder String, the name of source folder in a project.
	 * @param classOrPackageInProjectPathFormat String, the path (including source folder) of a class or package.
	 * @return String, the full qualified classname or package.
	 */
	public static String toDotFormat(String sourceFolder, String classOrPackageInProjectPathFormat){
		if(classOrPackageInProjectPathFormat.startsWith(sourceFolder)){
			return classOrPackageInProjectPathFormat.substring(sourceFolder.length()+1).replaceAll("/", ".");
		}
		return classOrPackageInProjectPathFormat.replaceAll("/", ".");
	}

	/**
	 * Split full qualified classname into to "package name" and "simple class name".
	 * @param fullQualifiedClassname String, the full qualified classname.
	 * @return String[], string array contains 'package name' and ''
	 */
	public static String[] splitClassName(String fullQualifiedClassname){
		String[] packageAndName = {"", fullQualifiedClassname};

		if(fullQualifiedClassname.contains(".")){
			int lastDotIndex = fullQualifiedClassname.lastIndexOf(".");
			packageAndName[0] = fullQualifiedClassname.substring(0, lastDotIndex);
			packageAndName[1] = fullQualifiedClassname.substring(lastDotIndex+1);
		}

		return packageAndName;
	}

}
