#!/bin/bash

#StartRMIServer.sh
#This script is used to start the "SeleniumPlus RMI server"
#It will change the environment SELENIUM_PLUS and CLASSPATH in the sub-shell
#If we want to get those environments modification persistent in the current shell,we need to call this script as "source ./StartRMIServer.sh" 

#to avoid the problem "java.awt.AWTError: Assistive Technology not found: org.GNOME.Accessibility.AtkWrapper"
#we need to comment out the line "assistive_technologies=" in java accessibility.properties
sed -i -e '/^assistive_technologies=/s/^/#/' /etc/java-*-openjdk/accessibility.properties

#set the environment SELENIUM_PLUS to a default value "/dev/seleniumplus" if it doesn't exist
if ( test -z $SELENIUM_PLUS ) then
  echo "We set the environment SELENIUM_PLUS to '/dev/seleniumplus'."	
  SELENIUM_PLUS=/dev/seleniumplus
fi

#==========  Allow user to sepcify the SELENIUM_PLUS variable =============
echo "Currently, the environment SELENIUM_PLUS is $SELENIUM_PLUS."
echo "If you want to change it, please enter below:"
read tmp
if ( test ! -z $tmp ) then
  SELENIUM_PLUS=$tmp 
fi

echo "We suppose the SeleniumPlus resides at directory: $SELENIUM_PLUS"
echo ""
#===========================================================================

#set the classpath for seleniumplus
CLASSPATH=$SELENIUM_PLUS/libs/seleniumplus.jar:$SELENIUM_PLUS/libs/JSTAFEmbedded.jar:$SELENIUM_PLUS/libs/selenium-server-standalone-3.14.0.jar:$CLASSPATH

export SELENIUM_PLUS CLASSPATH

#It is very important to specify the property "java.rmi.server.hostname" as "localhost", and give an explicit port to the "server.port"
#when we start the "selenium docker container", we need to map the "server port" to a port on the localhost from where the "selenium container" is launched.
java -Djava.rmi.server.hostname=localhost -Dserver.port=6890 -Dregistry.port=1099 org.safs.selenium.rmi.server.SeleniumServer
