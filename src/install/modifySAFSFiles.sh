#!/bin/sh
#modifySAFSFiles.sh
. ./sharedFunctions.sh

#After installation of SAFS, this script will be called in SetupSAFS.sh
#This script needs to be called with a parameter, the SAFS home directory
#
#This script will modify the .ini configuration files under Project directory of SAFS installation root
#It also creates some scripts to start the TID sample or SPC etc.
#It will remove some un-necessary .bat files, which is used in Windows OS.

#27 AUG, 2010  Lei Wang

INI_SAFS_TID_FILE=safstid.ini
INI_TID_TEST_FILE=tidtest.ini
INI_RFT_TEST_FILE=rfttest.ini
INI_SPC_FILE=processcontainer.ini

RUN_SPC_SCRIPT=runSTAFProcessContainer.sh
RUN_IMM_SCRIPT=runImageManager.sh
RUN_TID_TEST_SCRIPT=runTIDTest.sh
RUN_RFT_TEST_SCRIPT=runRFTTest.sh
RUN_TEST_LOG_SCRIPT=SAFSTESTLOG_Startup.sh
STOP_TEST_LOG_SCRIPT=SAFSTESTLOGShutdown.sh

if ( test $# = 0 ) ; then
  echo "Usage: modifyFiles.sh SAFSHomeDirectory"
else
  SAFSHomeDirectory=$1
  lastChar=$(getLastCharFromString $SAFSHomeDirectory)
  if ( test $? -eq 0 ); then
    if ( test $lastChar != "/" );then
      SAFSHomeDirectory=$SAFSHomeDirectory"/"
    fi
  fi
  echo ""
  echo "Creating .ini files and .sh scripts under SAFS project direcotry: $SAFSHomeDirectory""Project/"

SAFSProjectRoot=$SAFSHomeDirectory"Project/"
SAFSDatastorej=$SAFSHomeDirectory"datastorej/"
SAFSBinDir=$SAFSHomeDirectory"bin/"

#==================================================
#====   Modify .ini configuration files  ==========
#==================================================

#==== safstid.ini
cat > $SAFSProjectRoot$INI_SAFS_TID_FILE <<EOF

[SAFS_DRIVER]
DriverRoot="$SAFSHomeDirectory"
EOF

#==== tidtest.ini
cat > $SAFSProjectRoot$INI_TID_TEST_FILE <<EOF

[SAFS_PROJECT]
ProjectRoot="$SAFSProjectRoot"

[SAFS_TEST]
TestName="TIDTest"
TestLevel="Cycle"
CycleSeparator=","
CycleLogName="TIDTest.SAFS"
CycleLogMode="TEXTLOG XMLLOG"

;millisBetweenRecords= "20"
EOF

#==== rfttest.ini
cat > $SAFSProjectRoot$INI_RFT_TEST_FILE <<EOF

[SAFS_PROJECT]
ProjectRoot="$SAFSProjectRoot"

[SAFS_TEST]
TestName="RFTTest"
TestLevel="Cycle"

;Separator is a TAB character
CycleSeparator="        "
CycleLogName="RFTTest"
CycleLogMode="TEXTLOG XMLLOG"

[SAFS_ENGINES]
First=org.safs.tools.engines.SAFSROBOTJ

[SAFS_ROBOTJ]
AUTOLAUNCH=True
DATASTORE="$SAFSDatastorej"

JVMARGS="-Xms512m -Xmx512m"
EOF

#==== processcontainer.ini
cat > $SAFSProjectRoot$INI_SPC_FILE <<EOF

[SAFS_PROJECT]
ProjectRoot="$SAFSProjectRoot"

[SAFS_ROBOTJ]
TESTDOMAINS=HTML JAVA WIN Flex
AUTOLAUNCH=TRUE
DATASTORE="$SAFSDatastorej"

[SAFS_DRIVER]
DriverRoot="$SAFSHomeDirectory"

[SAFS_ENGINES]
First=org.safs.tools.engines.SAFSROBOTJ
EOF

#==================================================
#====   Create script .sh files          ==========
#==================================================

#==== runSTAFProcessContainer.sh
cat > $SAFSProjectRoot$RUN_SPC_SCRIPT <<EOF
java -Dsafs.processcontainer.ini="$INI_SPC_FILE" org.safs.tools.drivers.STAFProcessContainer
EOF

#==== runImageManager.sh
cat > $SAFSProjectRoot$RUN_IMM_SCRIPT <<EOF
java org.safs.image.ImageManager
EOF

#==== runTIDTest.sh
cat > $SAFSProjectRoot$RUN_TID_TEST_SCRIPT <<EOF
java -Dsafs.project.config="$INI_TID_TEST_FILE" org.safs.tools.drivers.SAFSDRIVER
EOF

#==== runRFTTest.sh
cat > $SAFSProjectRoot$RUN_RFT_TEST_SCRIPT <<EOF
java -Dsafs.project.config="$INI_RFT_TEST_FILE" org.safs.tools.drivers.SAFSDRIVER
EOF


#==== SAFSTESTLOG_Startup.sh
cat > $SAFSProjectRoot$RUN_TEST_LOG_SCRIPT <<EOF
java org.safs.Log debug -file:$SAFSProjectRoot"safsdebug.log"
EOF

#==== SAFSTESTLOGShutdown.sh
cat > $SAFSProjectRoot$STOP_TEST_LOG_SCRIPT <<EOF
staf local queue queue name SAFS/TESTLOG message SHUTDOWN
EOF

chmod a+x $SAFSProjectRoot$RUN_SPC_SCRIPT
chmod a+x $SAFSProjectRoot$RUN_IMM_SCRIPT
chmod a+x $SAFSProjectRoot$RUN_TID_TEST_SCRIPT
chmod a+x $SAFSProjectRoot$RUN_RFT_TEST_SCRIPT
chmod a+x $SAFSProjectRoot$RUN_TEST_LOG_SCRIPT
chmod a+x $SAFSProjectRoot$STOP_TEST_LOG_SCRIPT

ls $SAFSProjectRoot | grep ".ini" 
ls $SAFSProjectRoot | grep ".sh"
echo "These files will be used to run TIDTest, SPC, ImageManager etc."

echo ""
#Only for root user, we need to grant write access to all users;
#For normal user, not necessary as no other users will access SAFS directory.
if ( test $USER = "root" ); then
  echo "Grant write access right recursively to all users for directory $SAFSHomeDirectory"
  chmod -R a+w $SAFSHomeDirectory
fi

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

#End if ( test $# = 0 )
fi