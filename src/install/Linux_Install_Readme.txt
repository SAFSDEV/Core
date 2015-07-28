#27 AUG, 2010  LeiWang

Tested OS:     Red Hat Enterprise Linux Server release 5.3 (Tikanga)
Prerequisites: X-Window-Server (Xming is recommended for Windows OS),
               It is required for installation and SAFS IBT to run on Linux.

###########################  INSTALLATION ASSETS ##############################
Linux_Install_Readme.txt    This file itself

modifySAFSFiles.sh          Create .ini and .sh file for SAFS, Remove un-useful
                            files of Windows OS

SAFSInstall.jar             The Java Silent Installer, who does the real install
                            and un-install work.

SAFSInstall.ZIP             Contains all assets for SAFS Installation.

SetupSAFS.sh                The script to start install SAFS (including STAF)

sharedFunctions.sh          Contains common functions used during SAFS install

sharedVariables.sh          Contains common variables used during SAFS install

STAF341-setup-linux.bin     STAF install executable, which will be invoked by
                            our Java Silent Installer (SAFSInstall.jar), This
                            Executable has a JRE1.6 inside, In future we will
                            use STAF-install-executable without JRE inside.

UninstallSAFS.sh            The script to start uninstall SAFS (including STAF)
###############################################################################

1. How to install STAF and SAFS?
   After you login, go to directory where you put the installation asset.
   There, you can find a file named SetupSAFS.sh, which is an interactive script
   to setup STAF and SAFS.
   Run ./SetupSAFS.sh in your shell, if it can't be run, remember to modify it to 
   executable by command 'chmod u+x SetpuSAFS.sh'.

   Then, just follow the script's steps:
   The script will ask user if he wants to install STAF, where he wants to install STAF;
   if he wants to install SAFS, where he wants to install SAFS; if he wants to see detail
   informations during installation.
   The simple way is to hit Enter always, script will install SAFS and STAF to the
   default location:
    1.For super user root:
      STAF  --> /usr/local/staf
      SAFS  --> /usr/local/safs
    2.For normal user:
      STAF  --> $HOME/staf
      SAFS  --> $HOME/safs  


2. What is needed to take care?
   2.1 If the installation is did by super user root, the existing users need to add 
       some contents to his starting script $HOME/.bash_profile, the contents to 
       be added is in file /etc/skel/.bash_profile.safs.add
       The existing users can just call script ModifyDotBash.sh under bin directory of SAFS home

   2.2 User may need to modify the port number in file .bash_profile.
       Find SSL_PORT=6551 and TCP_PORT=6501 in that file, and modify 6551 and 6501
       to a different number, these port numbers must be different for different users.

   2.3 If you want to modify something not related to STAF and SASF in .bash_profile,
       do NOT forget to copy this modification to .bash_profile.safs.bak, because when
       uninstall SAFS, we will use .bash_profile.safs.bak to replace .bash_profile, if
       you don't copy modification to .bash_profile.safs.bak, the modification will be
       lost!!!
       The same attention for file .bash_logout!!! .bash_logout.safs.bak is the backup file

   2.4 All the samples can't work for instant, their initialization file need to be modified
       manually. 
       The samples are located in the samples directory of SAFS home, e.g. /usr/local/safs/samples

   2.5 Only TID and IBT keywords are supported; For RJ engine related keywords, it needs to install
       RFT on Linux


3. What this script do? --> The detail of Installation

   The installation script will detect the java version on your system, STAF require
   the JRE should be 1.5 or higher. Now our STAF installation STAF341-setup-linux.bin
   has a 1.6 JRE inside it, so we just use this JRE for instant. But in future we should
   use a STAF without JRE, and let user decide its own JRE.

   The script will do:
   a. Install SAFS and STAF to the indicated location on your machine.
   
   b. Create a data directory under STAF installation directory for STAF.
      For example, if you install STAF at /usr/local/staf, the data directory will be created as
      /usr/local/staf/data
      If you are super user, script will grant write-access-right to all users by applying 'chmod a+w'
      on the data directory.(When other users run STAF, they need write access right on this directory.)
      If you are normal user, the step to grant write-access-right will not be needed.
   
   c. Create a SAFS-environment-setting script (SAFSEnv.sh):
      If you install SAFS at /usr/local/safs, script will create a script /usr/local/safs/bin/SAFSEnv.sh
      This script will be called by user's auto-starting-script .bash_profile to setup users' environment
      variables automatically.
 
   d. Modify file /etc/skel/.bash_profile ( or ~/.bahs_profile if you are normal user)
      Firstly, we backup .bash_profile to .bash_profile.safs.bak (We should replace .bash_profile 
      with .bash_profile.safs.bak when we uninstall SAFS!!!)
      Then, we create a new file .bash_profile.safs.add (This file contains the script for setting SAFS,
      STAF environment, and starting STAF.)
      Finally, append the contents of .bash_profile.safs.add to .bash_profile

      This script contents of /etc/skel/.bash_profile will be automatically copied to user's home directory 
      when a new user is created.

      What are the contents that we append to this automatic script?
      c.1. Call STAF environment script to setup the STAF environment variables. Please pay attention to
           the variable STAF_INSTANCE, this should be different for each user.
      c.2  Call SAFSEnv.sh to setup the SAFS environment variables.
      c.3  Create a new STAF configuration file: STAF.cfg, you may need to modify the 'ssl port' and 'tcp port'
      c.4  Use command "staf/bin/STAFProc STAF.cfg" to start STAF

   e. Modify file /etc/skel/.bash_logout ( or ~/.bahs_logout if you are normal user)
      Firstly, we backup .bash_logout to .bash_logout.safs.bak
      Then, we create a new file .bash_logout.safs.add (This file contains the script for stopping STAF.)
      Finally, append the contents of .bash_logout.safs.add to .bash_logout

   f. Call script modifySAFSFiles.sh to create .ini and .sh files, which are used to start the TID sample or SPC etc.
      It will also remove un-necessary files:
      .bat files under directory Project of SAFS home, for example /usr/local/safs/Project;
      .wsf and .vbs files under SAFS home, for example /usr/local/safs/;
      .bat, .wsf, .vbs and .exe files under bin of SAFS home, for example /usr/local/safs/bin;

   g. If you are super user root, script will create a file ModifyDotBash.sh under the bin directory of your
      safs installation location, for example, /usr/local/safs/bin. This script will be used to modify the
      existing users' starting-script, e.g. .bash_profile and .bash_logout