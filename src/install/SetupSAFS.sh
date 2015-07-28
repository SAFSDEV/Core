#!/bin/sh
#SetupSAFS.sh
. ./sharedFunctions.sh
. ./sharedVariables.sh

#This script supply an inter-active way to help user install STAF and SAFS
#User can decide to install SAFS STAF or not, and their install directory
#
#This script will also create a file SAFSEnv.sh, which is used to setup SAFS environment
#
#This script will modify /etc/skel/.bash_profile, the original content will be saved to
#/etc/skel/.bash_profile.safs.bak.
#The added content is to setup STAF environment and setup SAFS environment, 
#and try to start STAF. This /etc/skel/.bash_profile will be copied
#to the new-added-user's home directory, so that each time user login, this will be executed
#automatically.
#
#Finally, this script will call script modifySAFSFiles.sh, which will modify some .ini files,
#create some .sh file and delete .bat files under SAFS Project directory. These created scripts
#and .ini configuration files will be used to run some samples of SAFS, like TID test, SPC,
#ImageManager etc.

#27 AUG, 2010  LeiWang


#==========  Check java version  =============================================
jdk=$(getJavaVersion)
jdkMajor=$(getJavaMajorVersion)
jdkMinor=$(getJavaMinorVersion)
echo "You java version is $jdk, major is $jdkMajor, minor is $jdkMinor."
#=============================================================================

#==========  Prepare to install STAF   =======================================
echo ""
echo "Do you want to install STAF [Y|N], default is Y?"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  installSTAF=0
else
  installSTAF=1
  echo "The default STAF installation directory is $STAFDirectory, do you accept it [Y|N], default is Y?"
  read tmp

  if ( test ! -z $tmp ) && (( test $tmp = "N" ) || ( test $tmp = "n" ))
  then
    echo "Input the directory where you want to install STAF:"
    read tmp
    if ( test ! -z $tmp )
    then
      STAFDirectory=$(removeLastPathSepCharFromString $tmp)
    fi
  fi
  echo "STAF will be installed to directory: $STAFDirectory"
fi
#=============================================================================

#==========  Prepare to install SAFS         =================================
echo ""
echo "Do you want to install SAFS [Y|N], default is Y?"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  installSAFS=0
else
  installSAFS=1
  echo "The default SAFS installation directory is $SAFSDirectory, do you accept it [Y|N], default is Y?"
  read tmp

  if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
  then
    echo "Input the directory where you want to install SAFS:"
    read tmp
    if ( test ! -z $tmp )
    then
      SAFSDirectory=$(removeLastPathSepCharFromString $tmp)
    fi
  fi
  echo "SAFS will be installed to directory: $SAFSDirectory"
fi
#===========================================================================


#===========  Ask user if he wants to see installation details   ===========
if ( test $installSTAF = 1 ) || ( test $installSAFS = 1 ) ; then
  echo ""
  echo "Do you want to see details during installation [Y|N], default is Y?"
  read tmp
  if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
  then
    VERBOSE=0
  fi
#===========================================================================

#===========  Create the installation command  =============================
  cmdline="java -jar SAFSInstall.jar "
  if ( test $installSAFS = 1 )
  then
    cmdline="$cmdline -safs $SAFSDirectory"
  else
    cmdline="$cmdline -nosafs"
  fi

  if ( test $installSTAF = 1 )
  then
    cmdline="$cmdline -staf $STAFDirectory"
  else
    cmdline="$cmdline -nostaf"
  fi
$HOME
  if ( test $VERBOSE = 1 )
  then
    cmdline="$cmdline -v"
  fi

  echo "Executing: $cmdline"
#===========================================================================

#==========  run installation command  =====================================
  $cmdline
#===========================================================================
fi


#==========  Create STAF data directory and grant write access to all users  =====
#==========  so that users can create directories under the data directory   =====
#==========  when starting STAF                                              =====
if ( test $installSTAF = 1 ); then
  stafDataDirectory=$STAFDirectory"/data"
  mkdir $stafDataDirectory

  #Only for root, we need to grant write access to all users; For normal, not necessary.
  if ( test $USER = "root" ); then
    echo "Create directory $stafDataDirectory and grant write-access-right to all users."
    chmod -R a+w $stafDataDirectory
  else
    echo "Create directory $stafDataDirectory"
  fi
fi
#=================================================================================

#==========  Create SAFS Setup environment script  ===============================
if ( test $installSAFS = 1 )
then
cat > $SAFSDirectory/bin/$SAFSEnvScript <<EOF

#====  Define SAFS' environment   ==============================================
SAFSDIR=$SAFSDirectory
STAFDIR=$STAFDirectory

JAVA_HOME=\$STAFDIR/jre/jre

PATH=.:\$JAVA_HOME/bin:\$SAFSDIR/bin:\$PATH

CLASSPATH=.:\$JAVA_HOME/lib:\$SAFSDIR/lib:\$SAFSDIR/lib/safs.jar:\$SAFSDIR/lib/safsjvmagent.jar:\$SAFSDIR/lib/safsrational_ft.jar:\$SAFSDIR/lib/safsrational_ft_enabler.jar:\$CLASSPATH

TESSDATA_PREFIX=\$SAFSDIR/ocr/

GOCRDATA_DIR=\$SAFSDIR/ocr/gocrdata/

export SAFSDIR STAFDIR JAVA_HOME PATH CLASSPATH TESSDATA_PREFIX GOCRDATA_DIR

echo "SAFS environment is ready."
EOF
fi
#==================================================================================


#====   For power user root, we need to modify /etc/skel/.bash_profile and /etc/skel/.bash_logout         =========
#====   So that when a new user is created, these files will be copied to his personal home directory     =========
#====   and served as starting script .bash_profile and .bash_logout                                      =========
#====   For normal user, we need to modify his starting script$HOME/.bash_profile and $HOME/.bash_logout  =========

if ( test $installSTAF = 1 ); then
#====================================   BEGIN create .bash_profile    =============================================
#==  1.Backup the original .bash_profile.safs to .bash_profile.safs.bak
cp $PROFILE_SKEL_FILE $PROFILE_SKEL_BAK_FILE
#==  2.Create a new file .bash_profile.safs.add
cat > $PROFILE_SKEL_ADDED_FILE <<EOF

###################################################################################################################
##  ATTENTION: THE FOLLOWING CONTENTS SHOULD BE REMOVED, IF USER root HAS UNINSTALLED STAF AND SAFS!!!           ##
###################################################################################################################
#==== 1 ====   Set STAF's environment
#For concept of  multiple STAF instnaces, refer to http://staf.sourceforge.net/current/STAFUG.htm#HDRSTPROC
#you must specify you own staf instance name
STAF_INSTANCE="STAF_"\$USER
. $STAFDirectory/$STAFEnvScript \$STAF_INSTANCE

#==== 2 ====   Set SAFS's environment
. $SAFSDirectory/bin/$SAFSEnvScript


#==== 3 ====   Prepare STAF configuration file and Launch STAF
#==== 3.1 ==== Prepare STAF configuration file
#You may need to modify manually the following variables DEFAULT_SSL_PORT, DEFAULT_TCP_PORT, SSL_PORT and TCP_PORT
#DEFAULT_SSL_PORT and DEFAULT_TCP_PORT are the default port numbers, they must be the same as in file $STAFDirectory/bin/STAF.cfg
DEFAULT_SSL_PORT=6550
DEFAULT_TCP_PORT=6500

#SSL_PORT and TCP_PORT are the port numbers that each STAF instance will use, they must be different for each STAF instance
#You can use 'netstat -a | grep 65' to see if the port number you specify is already used by other users; If yes, you need
#change them to a different number
SSL_PORT=6551
TCP_PORT=6501

#If the STAF configuration file doesn't exist in user's home directory, we will create a new one
if ( test ! -e STAF.cfg ); then
  #The following command will replace the default port number with new port number and put the new file STAF.cfg to your home
  sed 's/'\$DEFAULT_SSL_PORT'/'\$SSL_PORT'/g' $STAFDirectory/bin/STAF.cfg | sed 's/'\$DEFAULT_TCP_PORT'/'\$TCP_PORT'/g' > ~/STAF.cfg
  #You should see the following two lines in file STAF.cfg
  #           interface ssl library STAFTCP option Secure=Yes option Port=6551
  #           interface tcp library STAFTCP option Secure=No  option Port=6501
fi

#==== 3.2 ==== Launch STAF if it is NOT running
echo "Check if STAF is running..."
if STAF local ping ping|grep PONG; then
    echo "   STAF already launched!"
else
    echo "   No,starting it..."
    if ( test ! -e STAF.cfg )
    then
        #echo "   Use default STAF configuration file $STAFDirectory/bin/STAF.cfg"
        nohup \$STAFDIR/bin/STAFProc &
    else
        #echo "   Starting STAF with configuration file STAF.cfg"
        nohup \$STAFDIR/bin/STAFProc STAF.cfg &
    fi
    echo "   Please check nohup.out to see if STAF is successfully started."
    echo "   If not, you may need to modify SSL and TCP port number in STAF configuration file \$HOME/STAF.cfg"
    echo "   If yes, you can go to $SAFSDirectory/Project/, you can try to run runTIDTest.sh"
fi
###################################################################################################################
##  ATTENTION: THE CONTENTS ABOVE SHOULD BE REMOVED, IF USER root HAS UNINSTALLED STAF AND SAFS!!!               ##
###################################################################################################################
EOF

#== 3.Add content of file $PROFILE_SKEL_ADDED_FILE to $PROFILE_SKEL_FILE
echo "Modifying file $PROFILE_SKEL_FILE"
cat $PROFILE_SKEL_ADDED_FILE >> $PROFILE_SKEL_FILE
#====================================   END create .bash_profile   ====================================================

#====================================   BEGIN create .bash_logoute ====================================================
cp $PROFILE_SKEL_OUT_FILE $PROFILE_SKEL_OUT_BAK_FILE
cat > $PROFILE_SKEL_OUT_ADDED_FILE <<EOF

###################################################################################################################
##  ATTENTION: THE FOLLOWING CONTENTS SHOULD BE REMOVED, IF USER root HAS UNINSTALLED STAF AND SAFS!!!           ##
###################################################################################################################
#==== Stop STAF if it is running
echo "Check if STAF is running..."
if STAF local ping ping|grep PONG; then
    echo "   Try to stop STAF ..."
    staf local shutdown shutdown
fi
###################################################################################################################
##  ATTENTION: THE CONTENTS ABOVE SHOULD BE REMOVED, IF USER root HAS UNINSTALLED STAF AND SAFS!!!               ##
###################################################################################################################
EOF
echo "Modifying file $PROFILE_SKEL_OUT_FILE"
cat $PROFILE_SKEL_OUT_ADDED_FILE >> $PROFILE_SKEL_OUT_FILE
#====================================   END create .bash_logout   ====================================================
fi
#=====================================================================================================================

#===========  Modify .ini files and create .sh files to run test SPC etc ===========
if ( test $installSAFS = 1 ); then
  ./modifySAFSFiles.sh $SAFSDirectory
fi
#==================================================================================

#====================================   BEGIN create ModifyDotBash.sh ====================================================
if ( test $USER = "root" ); then
echo "ATTENTION!!!===============================================================================ATTENTION!!!" 
echo "Creating file $SAFSDirectory/bin/$ModifyDotBashScript"
cat > $SAFSDirectory/bin/$ModifyDotBashScript <<EOF
echo "Modifing file $PROFILE_SCRIPT"
cat $PROFILE_SKEL_ADDED_FILE >> ~/$PROFILE_SCRIPT
echo "Modifing file $PROFILE_OUT_SCRIPT"
cat $PROFILE_SKEL_OUT_ADDED_FILE >> ~/$PROFILE_OUT_SCRIPT
EOF
chmod a+x $SAFSDirectory/bin/$ModifyDotBashScript
echo "Please tell the existing users to call script $SAFSDirectory/bin/$ModifyDotBashScript, It is important."
echo "This script will modify .bash_profile and .bash_logout scripts under users' home directroy."
echo "ATTENTION!!!===============================================================================ATTENTION!!!" 
fi
#====================================   END create ModifyDotBash.sh   ====================================================

if ( test ! $USER = "root" ); then
echo "Please re-login to start using of SAFS."
fi