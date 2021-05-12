@ECHO OFF
REM Push seleniumplus dependency jar files into the maven "local repository" (specified by "localRepository" in maven setttings.xml file).
REM so that we can build/run seleniumplus test in "maven project" or "gradle project".
REM This script should run in the folder 'libs' of installed SeleniumPlus or the 'libs' of the SeleniumPlus distribution during the build.

ECHO "Pushing seleniumplus dependency jar files into maven local repository."

call mvn install:install-file -Dfile=JSTAFEmbedded.jar -DgroupId=com.ibm.staf -DartifactId=JSTAFEmbedded -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=seleniumplus.jar -DgroupId=org.safs.selenium -DartifactId=seleniumplus -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=selenium-server-standalone-3.14.0.jar -DgroupId=org.openqa.selenium -DartifactId=selenium-server-standalone -Dversion=3.14.0 -Dpackaging=jar
REM call mvn install:install-file -Dfile=safscust.jar -DgroupId=org.safs.selenium.manifest -DartifactId=safscust -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=safsdatamodel.jar -DgroupId=org.safs.selenium.manifest -DartifactId=safsdatamodel -Dversion=1.0 -Dpackaging=jar
REM call mvn install:install-file -Dfile=safsmodel.jar -DgroupId=org.safs.selenium.manifest -DartifactId=safsmodel -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=jai_core.jar -DgroupId=org.safs.selenium.manifest -DartifactId=jai_core -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=jai_codec.jar -DgroupId=org.safs.selenium.manifest -DartifactId=jai_codec -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=jna-4.2.2.jar -DgroupId=org.safs.selenium.manifest -DartifactId=jna -Dversion=4.2.2 -Dpackaging=jar
call mvn install:install-file -Dfile=jna-platform-4.2.2.jar -DgroupId=org.safs.selenium.manifest -DartifactId=jna-platform -Dversion=4.2.2 -Dpackaging=jar
call mvn install:install-file -Dfile=win32-x86.zip -DgroupId=org.safs.selenium.manifest -DartifactId=win32-x86 -Dversion=1.0 -Dpackaging=zip
call mvn install:install-file -Dfile=juniversalchardet-1.0.3.jar -DgroupId=org.safs.selenium.manifest -DartifactId=juniversalchardet -Dversion=1.0.3 -Dpackaging=jar
call mvn install:install-file -Dfile=jai_imageio.jar -DgroupId=org.safs.selenium.manifest -DartifactId=jai_imageio -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=SeInterpreter.jar -DgroupId=org.safs.selenium.manifest -DartifactId=SeInterpreter -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=jakarta-regexp-1.3.jar -DgroupId=org.safs.selenium.manifest -DartifactId=jakarta-regexp -Dversion=1.3 -Dpackaging=jar
call mvn install:install-file -Dfile=org.json.jar -DgroupId=org.safs.selenium.manifest -DartifactId=org.json -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=commons-logging-1.1.1.jar -DgroupId=org.safs.selenium.manifest -DartifactId=commons-logging -Dversion=1.1.1 -Dpackaging=jar
call mvn install:install-file -Dfile=commons-lang3-3.5.jar -DgroupId=org.safs.selenium.manifest -DartifactId=commons-lang3 -Dversion=3.5 -Dpackaging=jar
call mvn install:install-file -Dfile=commons-beanutils-1.9.3.jar -DgroupId=org.safs.selenium.manifest -DartifactId=commons-beanutils -Dversion=1.9.3 -Dpackaging=jar
call mvn install:install-file -Dfile=javax.mail.jar -DgroupId=org.safs.selenium.manifest -DartifactId=javax.mail -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=AutoItX4Java.jar -DgroupId=org.safs.selenium.manifest -DartifactId=AutoItX4Java -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=jacob.jar -DgroupId=org.safs.selenium.manifest -DartifactId=jacob -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=groovy-all-2.4.7.jar -DgroupId=org.safs.selenium.manifest -DartifactId=groovy-all -Dversion=2.4.7 -Dpackaging=jar
call mvn install:install-file -Dfile=spock-core-1.0-groovy-2.4.jar -DgroupId=org.safs.selenium.manifest -DartifactId=spock-core-1.0-groovy -Dversion=2.4 -Dpackaging=jar
call mvn install:install-file -Dfile=Saxon-HE-9.7.0-8.jar -DgroupId=org.safs.selenium.manifest -DartifactId=Saxon-HE -Dversion=9.7.0-8 -Dpackaging=jar
call mvn install:install-file -Dfile=httpclient5-5.0-alpha2-SNAPSHOT.jar -DgroupId=org.safs.selenium.manifest -DartifactId=httpclient5 -Dversion=5.0-alpha2-SNAPSHOT -Dpackaging=jar
call mvn install:install-file -Dfile=httpcore5-5.0-alpha2.jar -DgroupId=org.safs.selenium.manifest -DartifactId=httpcore5 -Dversion=5.0-alpha2 -Dpackaging=jar
call mvn install:install-file -Dfile=httpcore5-testing-5.0-alpha2.jar -DgroupId=org.safs.selenium.manifest -DartifactId=httpcore5-testing -Dversion=5.0-alpha2 -Dpackaging=jar
call mvn install:install-file -Dfile=log4j-api-2.8.1.jar -DgroupId=org.safs.selenium.manifest -DartifactId=log4j-api -Dversion=2.8.1 -Dpackaging=jar
call mvn install:install-file -Dfile=log4j-core-2.8.1.jar -DgroupId=org.safs.selenium.manifest -DartifactId=log4j-core -Dversion=2.8.1 -Dpackaging=jar
call mvn install:install-file -Dfile=slf4j-api-1.7.21.jar -DgroupId=org.safs.selenium.manifest -DartifactId=slf4j-api -Dversion=1.7.21 -Dpackaging=jar
call mvn install:install-file -Dfile=spring-core-4.3.4.RELEASE.jar -DgroupId=org.safs.selenium.manifest -DartifactId=spring-core -Dversion=4.3.4.RELEASE -Dpackaging=jar
call mvn install:install-file -Dfile=spring-web-4.3.4.RELEASE.jar -DgroupId=org.safs.selenium.manifest -DartifactId=spring-web -Dversion=4.3.4.RELEASE -Dpackaging=jar
call mvn install:install-file -Dfile=aspectjweaver.jar -DgroupId=org.safs.selenium.manifest -DartifactId=aspectjweaver -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=spring-aop.jar -DgroupId=org.safs.selenium.manifest -DartifactId=spring-aop -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=spring-aspects.jar -DgroupId=org.safs.selenium.manifest -DartifactId=spring-aspects -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=spring-beans.jar -DgroupId=org.safs.selenium.manifest -DartifactId=spring-beans -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=spring-context.jar -DgroupId=org.safs.selenium.manifest -DartifactId=spring-context -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=spring-expression.jar -DgroupId=org.safs.selenium.manifest -DartifactId=spring-expression -Dversion=1.0 -Dpackaging=jar
call mvn install:install-file -Dfile=ghost4j-1.0.1.jar -DgroupId=org.safs.selenium.manifest -DartifactId=ghost4j -Dversion=1.0.1 -Dpackaging=jar
call mvn install:install-file -Dfile=itext-2.1.7.jar -DgroupId=org.safs.selenium.manifest -DartifactId=itext -Dversion=2.1.7 -Dpackaging=jar
call mvn install:install-file -Dfile=log4j-over-slf4j-1.7.25.jar -DgroupId=org.safs.selenium.manifest -DartifactId=log4j-over-slf4j -Dversion=1.7.25 -Dpackaging=jar
call mvn install:install-file -Dfile=cucumber-core-4.2.0.jar -DgroupId=org.safs.selenium.manifest -DartifactId=cucumber-core -Dversion=4.2.0 -Dpackaging=jar
call mvn install:install-file -Dfile=cucumber-expressions-6.2.0.jar -DgroupId=org.safs.selenium.manifest -DartifactId=cucumber-expressions -Dversion=6.2.0 -Dpackaging=jar
call mvn install:install-file -Dfile=cucumber-html-0.2.7.jar -DgroupId=org.safs.selenium.manifest -DartifactId=cucumber-html -Dversion=0.2.7 -Dpackaging=jar
call mvn install:install-file -Dfile=cucumber-java-4.2.0.jar -DgroupId=org.safs.selenium.manifest -DartifactId=cucumber-java -Dversion=4.2.0 -Dpackaging=jar
call mvn install:install-file -Dfile=cucumber-junit-4.2.0.jar -DgroupId=org.safs.selenium.manifest -DartifactId=cucumber-junit -Dversion=4.2.0 -Dpackaging=jar
call mvn install:install-file -Dfile=cucumber-jvm-deps-1.0.6.jar -DgroupId=org.safs.selenium.manifest -DartifactId=cucumber-jvm-deps -Dversion=1.0.6 -Dpackaging=jar
call mvn install:install-file -Dfile=datatable-1.1.7.jar -DgroupId=org.safs.selenium.manifest -DartifactId=datatable -Dversion=1.1.7 -Dpackaging=jar
call mvn install:install-file -Dfile=datatable-dependencies-1.1.7.jar -DgroupId=org.safs.selenium.manifest -DartifactId=datatable-dependencies -Dversion=1.1.7 -Dpackaging=jar
call mvn install:install-file -Dfile=gherkin-5.0.0.jar -DgroupId=org.safs.selenium.manifest -DartifactId=gherkin -Dversion=5.0.0 -Dpackaging=jar
call mvn install:install-file -Dfile=gherkin-jvm-deps-1.0.4.jar -DgroupId=org.safs.selenium.manifest -DartifactId=gherkin-jvm-deps -Dversion=1.0.4 -Dpackaging=jar
call mvn install:install-file -Dfile=hamcrest-all-1.3.jar -DgroupId=org.safs.selenium.manifest -DartifactId=hamcrest-all -Dversion=1.3 -Dpackaging=jar
call mvn install:install-file -Dfile=junit-4.12.jar -DgroupId=org.safs.selenium.manifest -DartifactId=junit -Dversion=4.12 -Dpackaging=jar
call mvn install:install-file -Dfile=cglib-nodep-2.2.jar -DgroupId=org.safs.selenium.manifest -DartifactId=cglib-nodep -Dversion=2.2 -Dpackaging=jar
call mvn install:install-file -Dfile=ekspreso-event-creator-0.4.24-fat.jar -DgroupId=org.safs.selenium.manifest -DartifactId=ekspreso-event-creator -Dversion=0.4.24-fat -Dpackaging=jar
