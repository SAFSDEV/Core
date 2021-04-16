#!/bin/bash
#SetupSeleniumPlus.sh
. ./sharedFunctions.sh
. ./sharedVariables.sh

#ACCEPT_DEFAULT means that we don't need user's input, we use the default value to install
#SetupSeleniumPlus.sh --default
ACCEPT_DEFAULT=$1

#This script supply an interactive way to help user install STAF and SeleniumPlus
#User can decide to install SeleniumPlus STAF or not, and their install directory
#
#This script will also create a file SeleniumPlusEnv.sh, which is used to setup SeleniumPlus environment
#
#This script will modify /etc/skel/.bash_profile, the original content will be saved to
#/etc/skel/.bash_profile.safs.bak.
#The added content is to setup STAF environment and setup SeleniumPlus environment, 
#and try to start STAF. This /etc/skel/.bash_profile will be copied
#to the new-added-user's home directory, so that each time user login, this will be executed
#automatically.
#
#Finally, this script will call script modifySeleniumPlusFiles.sh, which will modify some .ini files,
#create some .sh file and delete .bat files under SeleniumPlus Project directory. These created scripts
#and .ini configuration files will be used to run some samples of SeleniumPlus, like TID test, SPC,
#ImageManager etc.

#08 JAN, 2019  Lei Wang Initial
#30 DEC, 2019  Lei Wang Change the STAF install directory if the OS is 64 bits.
#                      Don't call modifySAFSFiles.sh to prepare the SAFS sample test scripts etc.
#                      Call $ModifyDotBashScript to modify .bash_profile for "root" user.
#12 FEB, 2020  Lei Wang Add parameter "--default" or "-d" to accept default settings so that no human interactivity is required.
#13 FEB, 2020  Lei Wang Use `whoami` to replace "$USER", sometimes the variable "$USER" is not set (for example during docker build time).

#==========  Check java version  =============================================
jdk=$(getJavaVersion)
jdkMajor=$(getJavaMajorVersion)
jdkMinor=$(getJavaMinorVersion)
echo "You java version is $jdk, major is $jdkMajor, minor is $jdkMinor."
#=============================================================================

#==========  Prepare to install STAF   =======================================
if [ "$ACCEPT_DEFAULT" == "--default" ] || [ "$ACCEPT_DEFAULT" == "-d" ]
then
  tmp=y
else
  echo ""
  echo "Do you want to install STAF [Y|N], default is Y?"
  read tmp
fi

if ( test ! -z $tmp )  && ( [ "$tmp" == "N" ] || [ "$tmp" == "n" ] )
then
  installSTAF=0
else
  installSTAF=1
  if [ `uname -m` == 'x86_64' ]; then
    echo "Detect the current OS is 64 bit, changed the default STAF installation directory '${STAFDirectory}' to '${STAFDirectory}_64'"
  	STAFDirectory="${STAFDirectory}_64"
  fi
  
  if [ "$ACCEPT_DEFAULT" == "--default" ] || [ "$ACCEPT_DEFAULT" == "-d" ]
  then
  	tmp=y
  else
    echo "The default STAF installation directory is '${STAFDirectory}', do you accept it [Y|N], default is Y?"
    read tmp
  fi

  if ( test ! -z $tmp )  && ( [ "$tmp" == "N" ] || [ "$tmp" == "n" ] )
  then
    if [ `uname -m` == 'x86_64' ]; then
      echo "Detect the current OS is 64 bit, input the directory (MUST end with _64) where you want to install STAF:"
    else
      echo "Input the directory where you want to install STAF:"
    fi
    read tmp
    if ( test ! -z $tmp )
    then
      STAFDirectory=$(removeLastPathSepCharFromString $tmp)
    fi
  fi
  echo "STAF will be installed to directory: $STAFDirectory"
  #make a new $STAFDirectory
  if [ -d "$STAFDirectory" ]; then
  	rm -rf $STAFDirectory
  fi
  mkdir $STAFDirectory
fi
#=============================================================================

#==========  Prepare to install SeleniumPlus         =================================
if [ "$ACCEPT_DEFAULT" == "--default" ] || [ "$ACCEPT_DEFAULT" == "-d" ]
then
  tmp=y
else
  echo ""
  echo "Do you want to install SeleniumPlus [Y|N], default is Y?"
  read tmp
fi
if ( test ! -z $tmp )  && ( [ "$tmp" == "N" ] || [ "$tmp" == "n" ] )
then
  installSeleniumPlus=0
else
  installSeleniumPlus=1
  if [ "$ACCEPT_DEFAULT" == "--default" ] || [ "$ACCEPT_DEFAULT" == "-d" ]
  then
  	tmp=y
  else
    echo "The default SeleniumPlus installation directory is $SeleniumPlusDirectory, do you accept it [Y|N], default is Y?"
    read tmp
  fi

  if ( test ! -z $tmp )  && ( [ "$tmp" == "N" ] || [ "$tmp" == "n" ] )
  then
    echo "Input the directory where you want to install SeleniumPlus:"
    read tmp
    if ( test ! -z $tmp )
    then
      SeleniumPlusDirectory=$(removeLastPathSepCharFromString $tmp)
    fi
  fi
  echo "SeleniumPlus will be installed to directory: $SeleniumPlusDirectory"
fi
#===========================================================================


#===========  Ask user if he wants to see installation details   ===========
if ( test $installSTAF = 1 ) || ( test $installSeleniumPlus = 1 ) ; then
  if [ "$ACCEPT_DEFAULT" == "--default" ] || [ "$ACCEPT_DEFAULT" == "-d" ]
  then
  	tmp=y
  else
    echo ""
    echo "Do you want to see details during installation [Y|N], default is Y?"
    read tmp
  fi
  if ( test ! -z $tmp )  && ( [ "$tmp" == "N" ] || [ "$tmp" == "n" ] )
  then
    VERBOSE=0
  fi
#===========================================================================

#==========  Uncompress the JDK, JRE and Eclipse ===========================
  #uncompress the 32 bit JDK tar file
  cd $SeleniumPlusDirectory/Java/
  tar xvzf JDK*.tar.gz
  #move all files (under folder "jdk1.8.0_191") to current folder 
  mv -f jdk*/* -t .
  
  #uncompress the 64 bit JDK tar file
  cd $SeleniumPlusDirectory/Java64/
  tar xvzf JDK*.tar.gz
  #move all files (under folder "jdk1.8.0_281") to current folder
  mv -f jdk*/* -t .  

  #uncompress the Eclipse .tar.gz file
  cd $SeleniumPlusDirectory/eclipse/
  tar xvzf *.tar.gz
  
  mv -f plugins/* eclipse/plugins/
  rm -rf plugins
  
  mv -f configuration/* eclipse/configuration/
  rm -rf configuration
  
  mv -f eclipse eclipse_tmp
  mv -f eclipse_tmp/* -t .
  rm -rf eclipse_tmp

#===========================================================================

#SeleniumPlus does NOT bundle STAF after 2014.07.29
#===========  Create the installation command  =============================
  cd $SeleniumPlusDirectory 
  if [ `uname -m` == 'x86_64' ]; then
    JAVA_HOME="$SeleniumPlusDirectory/Java64/jre"
  else
    JAVA_HOME="$SeleniumPlusDirectory/Java/jre"
  fi
  PATH=.:$JAVA_HOME/bin:$PATH
  
  cmdline="$JAVA_HOME/bin/java -cp $SeleniumPlusDirectory/libs/seleniumplus.jar org.safs.install.SeleniumPlusInstaller "
  if ( test $installSeleniumPlus = 1 )
  then
    cmdline="$cmdline $SeleniumPlusDirectory"
  #else
  #  cmdline="$cmdline -nosafs"
  fi

  if ( test $installSTAF = 1 )
  then
  	echo "Grant execute-access-right to STAF*.bin for all users."
  	chmod a+x STAF*.bin
    cmdline="$cmdline -staf $STAFDirectory"
  else
    cmdline="$cmdline -nostaf"
  fi

  #why $HOME, cd $HOME?
  #$HOME
  if ( test $VERBOSE = 1 )
  then
    cmdline="$cmdline -v"
  fi

  echo "Executing: $cmdline"
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
  if [ `whoami` == "root" ]; then	
    echo "Create directory $stafDataDirectory and grant write-access-right to all users."
    chmod -R a+w $stafDataDirectory
  else
    echo "Create directory $stafDataDirectory"
  fi
fi
#=================================================================================

#==========  Create SeleniumPlus Setup environment script  ===============================
if ( test $installSeleniumPlus = 1 )
then
	if [ ! -d "$SeleniumPlusDirectory/bin" ]; then
	   mkdir $SeleniumPlusDirectory/bin
	fi
	
cat > $SeleniumPlusDirectory/bin/$SeleniumPlusEnvScript <<EOF
	
#====  Define SeleniumPlus' environment   ==============================================
SELENIUM_PLUS=$SeleniumPlusDirectory
STAFDIR=$STAFDirectory

if [ `uname -m` == 'x86_64' ]; then
  JAVA_HOME=\$SELENIUM_PLUS/Java64/jre
else
  JAVA_HOME=\$SELENIUM_PLUS/Java
fi

PATH=.:\$JAVA_HOME/bin:\$SELENIUM_PLUS/bin:\$PATH

CLASSPATH=.:\$JAVA_HOME/lib:\$SELENIUM_PLUS/libs:\$SELENIUM_PLUS/libs/seleniumplus.jar:\$CLASSPATH

TESSDATA_PREFIX=\$SELENIUM_PLUS/extra/automation/ocr

GOCRDATA_DIR=\$SELENIUM_PLUS/extra/automation/ocr/gocrdata/

export SELENIUM_PLUS STAFDIR JAVA_HOME PATH CLASSPATH TESSDATA_PREFIX GOCRDATA_DIR

echo "SeleniumPlus environment is ready."
EOF
	
	#Grant execute-permission to script
	chmod a+x $SeleniumPlusDirectory/bin/$SeleniumPlusEnvScript
	chmod a+x $SeleniumPlusDirectory/extra/*.sh $SeleniumPlusDirectory/extra/geckodriver* $SeleniumPlusDirectory/extra/chromedriver
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

#==== 2 ====   Set SeleniumPlus's environment
. $SeleniumPlusDirectory/bin/$SeleniumPlusEnvScript


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
    echo "   If yes, you can go to $SeleniumPlusDirectory/Project/, you can try to run runTIDTest.sh"
fi
###################################################################################################################
##  ATTENTION: THE CONTENTS ABOVE SHOULD BE REMOVED, IF USER root HAS UNINSTALLED STAF AND SeleniumPlus!!!       ##
###################################################################################################################
EOF
	
	#== 3.Add content of file $PROFILE_SKEL_ADDED_FILE to $PROFILE_SKEL_FILE
	echo "Modifying file $PROFILE_SKEL_FILE"
	cat $PROFILE_SKEL_ADDED_FILE >> $PROFILE_SKEL_FILE
	#====================================   END create .bash_profile   ====================================================
	
	#====================================   BEGIN create .bash_logout ====================================================
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

#====================================   BEGIN create ModifyDotBash.sh ====================================================
if [ `whoami` == "root" ]; then	
	echo "ATTENTION!!!===============================================================================ATTENTION!!!" 
	echo "Creating file $SeleniumPlusDirectory/bin/$ModifyDotBashScript"
cat > $SeleniumPlusDirectory/bin/$ModifyDotBashScript <<EOF
echo "Modifing file $PROFILE_SCRIPT"
cat $PROFILE_SKEL_ADDED_FILE >> ~/$PROFILE_SCRIPT
echo "Modifing file $PROFILE_OUT_SCRIPT"
cat $PROFILE_SKEL_OUT_ADDED_FILE >> ~/$PROFILE_OUT_SCRIPT
EOF
	#make the script file $ModifyDotBashScript executable
	chmod a+x $SeleniumPlusDirectory/bin/$ModifyDotBashScript
	echo "calling script ${ModifyDotBashScript} so that the .bash_profile will be modified for 'root' user"
	$SeleniumPlusDirectory/bin/$ModifyDotBashScript
	echo "Please tell the existing users to call script $SeleniumPlusDirectory/bin/$ModifyDotBashScript, It is important."
	echo "This script will modify .bash_profile and .bash_logout scripts under users' home directroy."
	echo "ATTENTION!!!===============================================================================ATTENTION!!!" 
fi
#====================================   END create ModifyDotBash.sh   ====================================================
if [ `whoami` != "root" ]; then	
	echo "Please re-login to start using of SeleniumPlus."
fi