#sharedVariables.sh

#This script contains some global variables shared by
#UninstallSAFS.sh and SetupSAFS.sh

#27 AUG, 2010  Lei Wang
#10 JAN, 2019  Lei Wang Replace the Windows-newline by Unix-newline.

#=============================================================
#=======  Define global variables                    =========
#=============================================================
installSAFS=1
installSeleniumPlus=1
installSTAF=1
uninstallSAFS=1
uninstallSeleniumPlus=1
uninstallSTAF=1
STAFDirectory=/usr/local/staf
SAFSDirectory=/usr/local/safs
SeleniumPlusDirectory=/usr/local/seleniumplus
STAFEnvScript=STAFEnv.sh
SAFSEnvScript=SAFSEnv.sh
SeleniumPlusEnvScript=SeleniumPlusEnv.sh
ModifyDotBashScript=ModifyDotBash.sh

#===  For other version of Linux or Unix, we may need to modify
#===  the content of the following variables
PROFILE_SCRIPT=.bash_profile
PROFILE_SCRIPT_BAK=.bash_profile.safs.bak
PROFILE_SCRIPT_ADD=.bash_profile.safs.add
PROFILE_OUT_SCRIPT=.bash_logout
PROFILE_OUT_SCRIPT_BAK=.bash_logout.safs.bak
PROFILE_OUT_SCRIPT_ADD=.bash_logout.safs.add
#=============================================================


PROFILE_SKEL_FILE=/etc/skel/$PROFILE_SCRIPT
PROFILE_SKEL_BAK_FILE=/etc/skel/$PROFILE_SCRIPT_BAK
PROFILE_SKEL_ADDED_FILE=/etc/skel/$PROFILE_SCRIPT_ADD
PROFILE_SKEL_OUT_FILE=/etc/skel/$PROFILE_OUT_SCRIPT
PROFILE_SKEL_OUT_BAK_FILE=/etc/skel/$PROFILE_OUT_SCRIPT_BAK
PROFILE_SKEL_OUT_ADDED_FILE=/etc/skel/$PROFILE_OUT_SCRIPT_ADD

USER_PROFILE=$HOME/$PROFILE_SCRIPT
USER_BAK_PROFILE=$HOME/$PROFILE_SCRIPT_BAK
USER_SKEL_ADDED_FILE=$HOME/$PROFILE_SCRIPT_ADD
USER_PROFILE_OUT=$HOME/$PROFILE_OUT_SCRIPT
USER_BAK_PROFILE_OUT=$HOME/$PROFILE_OUT_SCRIPT_BAK
USER_SKEL_ADDED_FILE_OUT=$HOME/$PROFILE_OUT_SCRIPT_ADD
VERBOSE=1


#==========  Change the default SAFS, SeleniumPlus and STAF installation directory if user is not root =======
if ( test ! $USER = "root" ); then
STAFDirectory=$HOME"/staf"
SAFSDirectory=$HOME"/safs"
SeleniumPlusDirectory=$HOME"/seleniumplus"
PROFILE_SKEL_FILE=$USER_PROFILE
PROFILE_SKEL_BAK_FILE=$USER_BAK_PROFILE
PROFILE_SKEL_ADDED_FILE=$USER_SKEL_ADDED_FILE
PROFILE_SKEL_OUT_FILE=$USER_PROFILE_OUT
PROFILE_SKEL_OUT_BAK_FILE=$USER_BAK_PROFILE_OUT
PROFILE_SKEL_OUT_ADDED_FILE=$USER_SKEL_ADDED_FILE_OUT
fi
#================================================================================================