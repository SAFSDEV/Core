#!/bin/sh
#UninstallSAFS.sh
. ./sharedFunctions.sh
. ./sharedVariablesIos.sh

#This script supply an inter-active way to help user uninstall STAF and SAFS
#User can decide to uninstall SAFS STAF or not
#
#Finally, for root user, this script will replace /etc/skel/.bash_profile 
#with content of /etc/skel/.bash_profile.safs.bak, this .bak file is the 
#script before we install SAFS. For normal user, all mentioned files will
#be under his home directory
#Take attention, if root user modify /etc/skel/.bash_profile or .bash_logout
#after he installs STAF, the uninstall STAF will earse what he has modified!!!

#!!!!! To be fixed, If the root user uninstall SAFS, how about the other users
#who are sharing the SAFS and STAF installed by root, in their .bash_profile
#and .bash_logout, the scripts of STAF and SAFS will be still called, which 
#will cause errors!!!

#17 AUG, 2011  DharmeshPatel

#==========  Prepare to uninstall STAF         =======================================
echo "Do you want to uninstall STAF [Y|N]?  (Default is Y)"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  uninstallSTAF=0
else
  uninstallSTAF=1
  echo "Is your STAF installation directory $STAFDirectory? If NOT, Input your staf installation directory. If YES, just type Enter."
  read tmp
  if ( test ! -z $tmp ) && ( test -d $tmp )
  then
      STAFDirectory=$tmp
  fi
  #echo "Your STAF installation directory: $STAFDirectory"
fi
#========================================================================================

#==========  Prepare to uninstall SAFS         =======================================
echo "Do you want to uninstall SAFS [Y|N]?  (Default is Y)"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  uninstallSAFS=0
else
  uninstallSAFS=1
  echo "Is your SAFS installation directory $SAFSDirectory? If NOT, Input your safs installation directory. If YES, just type Enter."
  read tmp

  if ( test ! -z $tmp ) && ( test -d $tmp )
  then
      SAFSDirectory=$tmp
  fi
  #echo "Your SAFS installation directory: $SAFSDirectory"
fi
#========================================================================================

#===========  Ask user if he wants to see installation details   ========
if ( test $uninstallSAFS = 1 ) || ( test $uninstallSTAF = 1 ); then
  echo "Do you want to see details during Uninstallation [Y|N]?  (Default is Y)"
  read tmp
  if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
  then
    VERBOSE=0
  fi

#===========  Create the uninstallation command  ==================================
  cmdline="java -jar SAFSInstall.jar "
  if ( test $uninstallSAFS = 1 )
  then
    cmdline="$cmdline -removesafs $SAFSDirectory"
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

