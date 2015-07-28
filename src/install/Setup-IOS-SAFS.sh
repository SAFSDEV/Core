#!/bin/sh

#Setup-IOS-SAFS.sh

. ./sharedFunctions.sh
. ./sharedVariablesIos.sh

#This script supply an inter-active way to help user install STAF and SAFS
#User can decide to install SAFS STAF or not, and their install directory
#
#This script will also create a file SAFSEnv.sh, which is used to setup SAFS environment
#The added content is to setup STAF environment and setup SAFS environment, 
#
#Finally, this script will call script modifySAFSFiles.sh, which will modify some .ini files,
#create some .sh file and delete .bat files under SAFS Project directory. These created scripts
#and .ini configuration files will be used to run some samples of SAFS, like TID test, SPC,
#ImageManager etc.

#17 AUG, 2011  DharmeshPatel

#==========  Check java version  =============================================
jdk=$(getJavaVersion)
jdkMajor=$(getJavaMajorVersion)
jdkMinor=$(getJavaMinorVersion)
echo "You java version is $jdk, major is $jdkMajor, minor is $jdkMinor."
#=============================================================================

#==========  Prepare to install STAF   =======================================
echo ""
echo "STAF is REQUIRED for the SAFS Runtime Environment."
echo "Do you want to install STAF [Y|N]?  (Default is Y)"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  installSTAF=0
else
  installSTAF=1
  echo "The default STAF installation directory is $STAFDirectory, do you accept it [Y|N]?  (Default is Y)"
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
echo "Do you want to install SAFS [Y|N]?  (Default is Y)"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  installSAFS=0
else
  installSAFS=1
  echo "The default SAFS installation directory is $SAFSDirectory, do you accept it [Y|N]?  (Default is Y)"
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
  echo "Do you want to see details during installation [Y|N]?  (Default is Y)"
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
  echo "Create directory $stafDataDirectory and grant write-access-right to all users."
  chmod -R a+w $stafDataDirectory
fi
#=================================================================================

#==========  Create SAFS Setup environment script  ===============================
if ( test $installSTAF = 1 )
then
cat > $STAFDirectory/$STAFEnvScript <<EOF
#!/bin/sh
# STAF/SAFS environment variables

SAFSDIR=$SAFSDirectory
STAFDIR=$STAFDirectory

PATH=\$STAFDIR/bin:\$STAFDIR/lib:\$SAFSDIR/bin:${PATH:-}

DYLD_LIBRARY_PATH=\$STAFDIR/lib:${DYLD_LIBRARY_PATH:-}

CLASSPATH=\$STAFDIR/lib/JSTAF.jar:\$SAFSDIR/lib:\$SAFSDIR/lib/safs.jar:\$SAFSDIR/lib/safsios.jar:\$SAFSDIR/lib/jna.zip:\$SAFSDIR/lib/safsjvmagent.jar:\$SAFSDIR/lib/safscust.jar:\$SAFSDIR/lib/safsdebug.jar:\$SAFSDIR/lib/jai_imageio.jar:\$SAFSDIR/lib/clibwrapper_jiio.jar:\$SAFSDIR/lib/jai_codec.jar:\$SAFSDIR/lib/jai_core.jar:\$STAFDIR/samples/demo/STAFDemo.jar:${CLASSPATH:-}

STAFCONVDIR=\$STAFDIR/codepage

TESSDATA_PREFIX=\$SAFSDIR/ocr/
GOCRDATA_DIR=\$SAFSDIR/ocr/gocrdata/

# JAVA_HOME=/Library/Java

if [ $# = 0 ]
then
    STAF_INSTANCE_NAME=STAF
else
    if [ $1 != "start" ]
    then
        STAF_INSTANCE_NAME=$1
    else
        # Ignore "start" STAF instance name
        STAF_INSTANCE_NAME=STAF
    fi
fi

export PATH DYLD_LIBRARY_PATH CLASSPATH STAFCONVDIR STAF_INSTANCE_NAME SAFSDIR STAFDIR TESSDATA_PREFIX GOCRDATA_DIR

EOF
fi
#==================================================================================

#===========  Modify .ini files and create .sh files to run test IOSPC etc ===========
if ( test $installSAFS = 1 ); then
  ./modify-IOS-SAFSFiles.sh $SAFSDirectory $STAFDirectory 
fi
#==================================================================================

