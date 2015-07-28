#!/bin/sh

#modify-IOS-SAFSFiles.sh

. ./sharedFunctions.sh
. ./sharedVariablesIos.sh

#After installation of SAFS, this script will be called in Setup-IOS-SAFS.sh
#This script needs to be called with two parameters: 
#
#   SAFS Home directory
#   STAF Home directory
#
#This script will modify the .ini configuration files under Project directory of SAFS installation root
#It also creates some scripts to start the TID sample or IOSPC etc.
#It will remove any unnecessary files intended for the Windows OS.

# 17 AUG, 2011  DharmeshPatel
# 30 AUG, 2011  Carl Nagle

INI_IOS_TEST_FILE=iostest.ini
INI_IOSPC_TEST_FILE=iosprocesscontainer.ini
INI_SAFS_TID_FILE=safstid.ini
INI_TID_TEST_FILE=tidtest.ini

SAFSJVMAGENT_PROPERTIES_FILE=safsjvmagent.properties

RUN_IOS_TEST_SCRIPT=runIOSTest.sh
RUN_IOSPC_SCRIPT=runIOSProcessContainer.sh
RUN_IMAGEMANAGER_SCRIPT=runImageManager.sh
RUN_TEST_LOG_SCRIPT=SAFSTESTLOG_Startup.sh
STOP_TEST_LOG_SCRIPT=SAFSTESTLOGShutdown.sh
RUN_TID_TEST_SCRIPT=runTIDTest.sh
STOP_IOS_ENGINE_SCRIPT=SAFSIOSShutdown.sh
RECENT_PROCESSCONTAINER=/IOS/instruments/recent/ProcessContainer
RECENT_SAFSRUNTIME=/IOS/instruments/recent/SAFSRuntime

if ( test $# = 0 ) ; then
  echo "Usage: modify-IOS-SAFSFiles.sh SAFSHomeDirectory STAFHomeDirectory"
else
  SAFSHomeDirectory=$1
  STAFHomeDirectory=$2
  lastChar=$(getLastCharFromString $SAFSHomeDirectory)
  if ( test $? -eq 0 ); then
    if ( test $lastChar != "/" );then
      SAFSHomeDirectory=$SAFSHomeDirectory"/"
    fi
  fi
  echo ""
  echo "Creating .ini files and .sh scripts under SAFS project direcotry: $SAFSHomeDirectory""Project/"

SAFSProjectRoot=$SAFSHomeDirectory"Project/"
SAFSBinDir=$SAFSHomeDirectory"bin/"

#==================================================
#====   Modify .ini configuration files  ==========
#==================================================

#==== safstid.ini

cat > $SAFSProjectRoot$INI_SAFS_TID_FILE <<EOF

[SAFS_DRIVER]
DriverRoot="$SAFSHomeDirectory"

EOF

#==== iostest.ini
cat > $SAFSProjectRoot$INI_IOS_TEST_FILE <<EOF

[SAFS_PROJECT]
ProjectRoot="$SAFSProjectRoot"

[SAFS_TEST]
TestName="IOSTest"
TestLevel="Cycle"

;Separator is a COMMA character
CycleSeparator=","
CycleLogName="IOSTest.SAFS"
CycleLogMode="TEXTLOG CONSOLE"

[SAFS_ENGINES]
First=org.safs.tools.engines.SAFSIOS

[SAFS_DRIVER]
PreferredEnginesOverride=TRUE

[SAFS_IOS]
AUTOLAUNCH=True
Project="/Library/safs/samples/UICatalog/"

EOF

#==== tidtest.ini
cat > $SAFSProjectRoot$INI_TID_TEST_FILE <<EOF

[SAFS_PROJECT]
ProjectRoot="$SAFSProjectRoot"

[SAFS_TEST]
TestName="TIDTest"
TestLevel="Cycle"

;Separator is a COMMA character
CycleSeparator=","
CycleLogName="TIDTest.SAFS"
CycleLogMode="TEXTLOG CONSOLE"

EOF

#==== iosprocesscontainer.ini 

cat > $SAFSProjectRoot$INI_IOSPC_TEST_FILE <<EOF

;IOSPC will provide initial contents when run

EOF

#==================================================
#====  Create safsjvmagent.properties file  =======
#==================================================

cat > $SAFSHomeDirectory/lib/SAFSJVMAGENT_PROPERTIES_FILE <<EOF

# safsjvmagent.properties (Abbot Sample)
#
# SAFS Bootstrap AgentClassLoader uses this file to know which 
# class(es) to launch and what CLASSPATH to use to find them.
# As an Extensions ClassLoader we do NOT have access to the 
# System CLASSPATH.  That is why we need this info here.

safs.jvmagent.classpath=\$STAFHomeDirectory/lib/JSTAF.jar:\$SAFSHomeDirectory/lib:\$SAFSHomeDirectory/lib/safs.jar:\$SAFSHomeDirectory/lib/safsios.jar:
\$SAFSHomeDirectory/lib/safsabbot.jar:\$SAFSHomeDirectory/lib/jna.zip:\$SAFSHomeDirectory/lib/safsjvmagent.jar:\$SAFSHomeDirectory/lib/safscust.jar:\$SAFSHomeDirectory/lib/safsdebug.jar:\$SAFSHomeDirectory/lib/jai_imageio.jar:\$SAFSHomeDirectory/lib/clibwrapper_jiio.jar:\$SAFSHomeDirectory/lib/jai_codec.jar:\$SAFSHomeDirectory/lib/jai_core.jar

safs.jvmagent.classes=org.safs.abbot.jvmagent.JVMAgent

EOF

#==================================================
#====   Create script .sh files          ==========
#==================================================

#==== recent/ProcessContainer
cat > $SAFSHomeDirectory$RECENT_PROCESSCONTAINER <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<array>
	<string>$SAFSHomeDirectory/IOS/jscript/ProcessContainer.js</string>
</array>
</plist>
EOF

#==== recent/SAFSRuntime
cat > $SAFSHomeDirectory$RECENT_SAFSRUNTIME <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<array>
	<string>$SAFSHomeDirectory/IOS/jscript/SAFSRuntime.js</string>
</array>
</plist>
EOF

#==== SAFSTESTLOG_Startup.sh
cat > $SAFSProjectRoot$RUN_TEST_LOG_SCRIPT <<EOF
. $STAFHomeDirectory/$STAFEnvScript
java org.safs.Log debug -file:$SAFSProjectRoot"safsdebug.log"
EOF

#==== SAFSTESTLOGShutdown.sh
cat > $SAFSProjectRoot$STOP_TEST_LOG_SCRIPT <<EOF
staf local queue queue name SAFS/TESTLOG message SHUTDOWN
EOF

#==== runIOSTest.sh
cat > $SAFSProjectRoot$RUN_IOS_TEST_SCRIPT <<EOF
. $STAFHomeDirectory/$STAFEnvScript
java -Dsafs.project.config="$INI_IOS_TEST_FILE" org.safs.tools.drivers.SAFSDRIVER
EOF

#==== SAFSIOSShutdown.sh
cat > $SAFSProjectRoot$STOP_IOS_ENGINE_SCRIPT <<EOF
. $STAFHomeDirectory/$STAFEnvScript
staf local safsvars set safs/hook/inputrecord value SHUTDOWN_HOOK
staf local sem event safs/iosdispatch pulse

EOF

#==== runTIDTest.sh
cat > $SAFSProjectRoot$RUN_TID_TEST_SCRIPT <<EOF
. $STAFHomeDirectory/$STAFEnvScript
java -Dsafs.project.config="$INI_TID_TEST_FILE" org.safs.tools.drivers.SAFSDRIVER
EOF

#==== runImageManager.sh
cat > $SAFSProjectRoot$RUN_IMAGEMANAGER_SCRIPT <<EOF
. $STAFHomeDirectory/$STAFEnvScript
java org.safs.image.ImageManager
EOF

#==== runIOSProcessContainer.sh
cat > $SAFSProjectRoot$RUN_IOSPC_SCRIPT <<EOF
. $STAFHomeDirectory/$STAFEnvScript
java -Dsafs.processcontainer.ini="$INI_IOSPC_TEST_FILE" org.safs.ios.IOSProcessContainer

EOF

chmod a+x $SAFSProjectRoot$RUN_IOS_TEST_SCRIPT
chmod a+x $SAFSProjectRoot$RUN_TID_TEST_SCRIPT
chmod a+x $SAFSProjectRoot$RUN_IOSPC_SCRIPT
chmod a+x $SAFSProjectRoot$RUN_IMAGEMANAGER_SCRIPT
chmod a+x $SAFSProjectRoot$RUN_TEST_LOG_SCRIPT
chmod a+x $SAFSProjectRoot$STOP_TEST_LOG_SCRIPT
chmod a+x $SAFSProjectRoot$STOP_IOS_ENGINE_SCRIPT

ls $SAFSProjectRoot | grep ".ini" 
ls $SAFSProjectRoot | grep ".sh"
echo "These files will be used to run TIDTest, IOSPC, ImageManager etc."

echo ""
#For normal user, not necessary as no other users will access SAFS directory.
echo "Grant write access right recursively to all users for directory $SAFSHomeDirectory"
chmod -R a+w $SAFSHomeDirectory
chmod -R a+x $SAFSHomeDirectory/*.sh
chmod -R a+x $SAFSHomeDirectory/*.jar

#=================================================================
#====  Remove un-necessary .bat, .wsf and .vbs files    ==========
#=================================================================
echo ""
echo "Deleting .bat files under directory $SAFSProjectRoot"
currentDir=$PWD
cd $SAFSProjectRoot
rm -f *.bat
echo "Deleting .wsf and .vbs files under directory $SAFSHomeDirectory"
cd $SAFSHomeDirectory
rm -f *.vbs *.wsf
echo "Deleting .bat, .wsf, .vbs, .dll and .exe files under directory $SAFSBinDir"
cd $SAFSBinDir
rm -f *.vbs *.wsf *.bat *.exe *.dll
cd $currentDir
echo ""
fi
