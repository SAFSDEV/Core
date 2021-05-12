#!/bin/sh
#UninstallSeleniumPlus.sh
. ./sharedFunctions.sh
. ./sharedVariables.sh

#This script supply an interactive way to help user uninstall STAF and SeleniumPlus
#User can decide to uninstall SeleniumPlus/STAF or not
#
#Finally, for root user, this script will replace /etc/skel/.bash_profile 
#with content of /etc/skel/.bash_profile.safs.bak, this .bak file is the 
#script before we install SeleniumPlus. For normal user, all mentioned files will
#be under his home directory
#Take attention, if root user modify /etc/skel/.bash_profile or .bash_logout
#after he installs STAF, the uninstall STAF will erase what he has modified!!!

#!!!!! To be fixed, If the root user uninstall SeleniumPlus, how about the other users
#who are sharing the SeleniumPlus and STAF installed by root, in their .bash_profile
#and .bash_logout, the scripts of STAF and SeleniumPlus will be still called, which 
#will cause errors!!!

#09 JAN, 2019  Lei Wang Initial
#30 DEC, 2019  Lei Wang Change the STAF install directory if the OS is 64 bits.

#==========  Prepare to uninstall STAF         =======================================
echo "Do you want to uninstall STAF [Y|N], default is Y?"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  uninstallSTAF=0
else
  uninstallSTAF=1
  if [ `uname -m` == 'x86_64' ]; then
    echo "Detect the current OS is 64 bit, changed the default STAF installation directory '${STAFDirectory}' to '${STAFDirectory}_64'"
  	STAFDirectory="${STAFDirectory}_64"
  fi
  echo "Is your STAF installation directory $STAFDirectory? If NOT, Input your staf installation directory. If YES, just type Enter."
  read tmp
  if ( test ! -z $tmp ) && ( test -d $tmp )
  then
      STAFDirectory=$tmp
  fi
  #echo "Your STAF installation directory: $STAFDirectory"
fi
#========================================================================================

#==========  Prepare to uninstall SeleniumPlus         =======================================
echo "Do you want to uninstall SeleniumPlus [Y|N], default is Y?"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  uninstallSeleniumPlus=0
else
  uninstallSeleniumPlus=1
  echo "Is your SeleniumPlus installation directory $SeleniumPlusDirectory? If NOT, Input your SeleniumPlus installation directory. If YES, just type Enter."
  read tmp

  if ( test ! -z $tmp ) && ( test -d $tmp )
  then
      SeleniumPlusDirectory=$tmp
  fi
  #echo "Your SeleniumPlus installation directory: $SeleniumPlusDirectory"
fi
#========================================================================================

#===========  Ask user if he wants to see installation details   ========
if ( test $uninstallSeleniumPlus = 1 ) || ( test $uninstallSTAF = 1 ); then
  echo "Do you want to see details during Uninstallation [Y|N], default is Y?"
  read tmp
  if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
  then
    VERBOSE=0
  fi

#===========  Create the uninstallation command  ==================================
  if [ `uname -m` == 'x86_64' ]; then
    JAVA_HOME="$SeleniumPlusDirectory/Java64/jre"
  else
    JAVA_HOME="$SeleniumPlusDirectory/Java/jre"
  fi
  
  cmdline="$JAVA_HOME/bin/java -cp $SeleniumPlusDirectory/libs/seleniumplus.jar org.safs.install.SeleniumPlusInstaller "
  if ( test $uninstallSeleniumPlus = 1 )
  then
    cmdline="$cmdline -u $SeleniumPlusDirectory"
  fi

  if ( test $uninstallSTAF = 1 )
  then
    cmdline="$cmdline -removestaf $STAFDirectory 3"
  fi

  if ( test $VERBOSE = 1 )
  then
    cmdline="$cmdline -v"
  fi

  echo "Executing: $cmdline"

#==========  run uninstallation command  ==================================
  $cmdline
fi
#========================================================================

#=====  After uninstallation:
#=====  For power user root, we need to modify /etc/skel/.bash_profile and /etc/skel/.bash_logout to original content  ==========
#=====  But for other existing users who share the Selenium and STAF installed by root, how to modify their .bahs_profile ????
#
#=====  For normal user, we need to modify $HOME/.bash_profile $HOME/.bash_logout to original content  ==========
if ( test $uninstallSTAF = 1 ); then
  if ( test -e $PROFILE_SKEL_BAK_FILE ); then
#if $PROFILE_SKEL_FILE has been modified, if ( test `$(compareFileLastModify $PROFILE_SKEL_BAK_FILE $PROFILE_SKEL_FILE)` eq "older" ); then
#we should not simply replace the $PROFILE_SKEL_FILE with $PROFILE_SKEL_BAK_FILE
    echo "Replacing file $PROFILE_SKEL_FILE with content of $PROFILE_SKEL_BAK_FILE"
    cat > $PROFILE_SKEL_FILE < $PROFILE_SKEL_BAK_FILE
    echo "Deleting file $PROFILE_SKEL_BAK_FILE"
    rm -f $PROFILE_SKEL_BAK_FILE
  fi
  if ( test -e $PROFILE_SKEL_ADDED_FILE ); then
    echo "Deleting file $PROFILE_SKEL_ADDED_FILE"
    rm -f $PROFILE_SKEL_ADDED_FILE
  fi

  if ( test -e $PROFILE_SKEL_OUT_BAK_FILE ); then
    echo "Replacing file $PROFILE_SKEL_OUT_FILE with content of $PROFILE_SKEL_OUT_BAK_FILE"
    cat > $PROFILE_SKEL_OUT_FILE < $PROFILE_SKEL_OUT_BAK_FILE
    echo "Deleting file $PROFILE_SKEL_OUT_BAK_FILE"
    rm -f $PROFILE_SKEL_OUT_BAK_FILE
  fi
  if ( test -e $PROFILE_SKEL_OUT_ADDED_FILE ); then
    echo "Deleting file $PROFILE_SKEL_OUT_ADDED_FILE"
    rm -f $PROFILE_SKEL_OUT_ADDED_FILE
  fi
fi
#========================================================================
