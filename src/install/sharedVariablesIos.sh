#sharedVariablesIos.sh

#This script contains some global variables shared by
#Uninstall-IOS-SAFS.sh and Setup-IOS-SAFS.sh

#Since Mac prefer not to use root user, the software will be 
#installed into default /Library dir.

#17 AUG, 2011  DharmeshPatel

#=============================================================
#=======  Define global variables                    =========
#=============================================================

installSAFS=1
installSTAF=1
uninstallSAFS=1
uninstallSTAF=1
STAFDirectory=/Library/staf
SAFSDirectory=/Library/safs
STAFEnvScript=STAFEnv.sh
SAFSEnvScript=SAFSEnv.sh
ModifyDotBashScript=ModifyDotBash.sh

#=============================================================
#===  For other version of Mac OS X, we may need to modify ===
#===        the content of the following variables         ===
#=============================================================

PROFILE_SCRIPT=.profile
PROFILE_SCRIPT_BAK=.profile.safs.bak
PROFILE_SCRIPT_ADD=.profile.safs.add
PROFILE_OUT_SCRIPT=.bash_logout
PROFILE_OUT_SCRIPT_BAK=.bash_logout.safs.bak
PROFILE_OUT_SCRIPT_ADD=.bash_logout.safs.add
#=============================================================

USER_PROFILE=$HOME/$PROFILE_SCRIPT
USER_BAK_PROFILE=$HOME/$PROFILE_SCRIPT_BAK
USER_PROFILE_OUT=$HOME/$PROFILE_OUT_SCRIPT
USER_BAK_PROFILE_OUT=$HOME/$PROFILE_OUT_SCRIPT_BAK
VERBOSE=1

#================================================================================================
