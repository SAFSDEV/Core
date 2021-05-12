#25 JAN, 2019  Lei Wang

Tested OS:     Red Hat Enterprise Linux Server 7.1 (Maipo)

###########################  INSTALLATION ASSETS ###################################
Linux_Install_Readme.txt    	This file itself

modifySAFSFiles.sh          	Create .ini and .sh file for SAFS, Remove un-useful
                            	files of Windows OS

SAFSInstall.jar             	The Java Silent Installer, who does the real install
                            	and un-install work.

SAFSInstall.ZIP             	Contains all assets for SAFS Installation.

SetupSAFS.sh                	The script to start install SAFS (including STAF)

sharedFunctions.sh          	Contains common functions used during SAFS install

sharedVariables.sh          	Contains common variables used during SAFS install

STAF341-setup-linux-NoJVM.bin (32 bits)   		STAF install executable, which will be invoked by
STAF341-setup-linux-amd64-NoJVM.bin (64 bits)   our Java Silent Installer (SAFSInstall.jar). It supposed
                            					that the correct JRE has been set in your environment.
                            			

UninstallSAFS.sh            	The script to start uninstall SAFS (including STAF)
###################################################################################

1. How to install STAF and SAFS?
   Get the installation file SAFSLnxReleaseCandidate.tar.gz from our build system http://safsbuild:81/jenkins/view/GITLAB_JOBS/job/SAFS_Linux/
   Open a shell
   1.1 create a folder /usr/local/safs
       $mkdir /usr/local/safs
   1.2 un-compress the installation asset to folder /usr/local/safs by following command
       $tar xvzf SAFSLnxReleaseCandidate.tar.gz -C /usr/local/safs
       Then go to directory /usr/local/safs/install where you put 
       the installation asset. There, you can find a file named SetupSAFS.sh, 
       which is an interactive script to setup STAF and SAFS.
       $cd /usr/local/safs/install
   1.3 Make the sript file executable
       1.3.1 grant execute-permission to STAF installer.
             $chmod u+x STAF*.bin
       1.3.2 grant execute-permission to installation scripts.
             $chmod u+x *.sh
   1.4 Install SAFS by following command
       $./SetupSAFS.sh

	   Then, just follow the script's steps:
	   The script will ask user 
	   1. If he wants to install STAF, where he wants to install STAF;
	   2. If he wants to install SAFS, where he wants to install SAFS; 
	   3. If he wants to see detail informations during installation.
	   The simple way is to hit Enter always, script will install SAFS and STAF to the
	   default location:
	    1.For super user root:
	      STAF  --> /usr/local/staf (for 32 bits OS)
	                /usr/local/staf_64 (for 64 bits OS)
	      SAFS  --> /usr/local/safs
	    2.For normal user:
	      STAF  --> $HOME/staf (for 32 bits OS)
	                $HOME/staf_64 (for 64 bits OS)
	      SAFS  --> $HOME/safs
	        
   1.5 Verify the installation
       1.5.1 set environment and start the STAF by calling the following script
             $. ~/.bash_profile
             you should be able to see something like
             SAFS environment is ready.
             Check if STAF is running...
             PONG
                 staf already launched.
                 
       1.5.2 run the TID test
             $cd /usr/local/safs/Project
             $./runTIDTest.sh

2. What is needed to take care?
   2.1 If the installation was done by super user "root", the existing users need to add 
       some contents to his starting script $HOME/.bash_profile, the contents to 
       be added is in file /etc/skel/.bash_profile.safs.add
       The existing users can just call script ModifyDotBash.sh under bin directory of SAFS home

   2.2 If user login with "X Window", the .bash_profile will not run automcatically.
       See https://unix.stackexchange.com/questions/88106/why-doesnt-my-bash-profile-work
       There are 2 ways out:
       2.2.1. We can ". ~/.bash_profile" directly in a terminal so that everything is set to 
              run SAFS test in that terminal. If we open a new terminal, we have to run that script again.
       2.2.2. We can copy the file ~/.bash_profile.safs.add or /etc/skel/.bash_profile.safs.add into
              ~/.profile so that the test environment will be ready when user logins.   

   2.3 User may need to modify the port number in file .bash_profile.
       Find SSL_PORT=6551 and TCP_PORT=6501 in that file, and modify 6551 and 6501
       to a different number, these port numbers must be different for different users.

   2.4 If you want to modify something not related to STAF and SASF in .bash_profile,
       do NOT forget to copy this modification to .bash_profile.safs.bak, because when
       uninstall SAFS, we will use .bash_profile.safs.bak to replace .bash_profile, if
       you don't copy modification to .bash_profile.safs.bak, the modification will be
       lost!!!
       The same attention for file .bash_logout!!! .bash_logout.safs.bak is the backup file

   2.5 All the samples can't work for instant, their initialization file need to be modified
       manually. 
       The samples are located in the samples directory of SAFS home, e.g. /usr/local/safs/samples

   2.6 Only TID and IBT keywords are supported; For RJ engine related keywords, it needs to install
       RFT on Linux


3. What this script does? --> The detail of Installation

   The installation script will detect the java version on your system, STAF require
   the JRE should be 1.8 or higher. We provide embedded 32 bits JRE(1.8) at 64 bit JRE(1.8).

   The script will do:
   a. Extract JRE to folder 'jre' and 'Java64/jre' under SAFS installation home.
      Such as /usr/local/safs/jre/, /usr/local/safs/Java64/jre/
      Set JAVA_HOME to 32 bit JRE directory or 64 bit JRE directory according to the SO architecture.
      
   b. Install SAFS and STAF to the indicated location on your machine.
   
   c. Create a data directory under STAF installation directory for STAF.
      For example, if you install STAF at /usr/local/staf, the data directory will be created as
      /usr/local/staf/data
      If you are super user, script will grant write-access-right to all users by applying 'chmod a+w'
      on the data directory.(When other users run STAF, they need write access right on this directory.)
      If you are normal user, the step to grant write-access-right will not be needed.
   
   d. Create a SAFS-environment-setting script (SAFSEnv.sh):
      If you install SAFS at /usr/local/safs, script will create a script /usr/local/safs/bin/SAFSEnv.sh
      This script will be called by user's auto-starting-script .bash_profile to setup users' environment
      variables automatically.
 
   e. Modify file /etc/skel/.bash_profile ( or ~/.bahs_profile if you are normal user)
      Firstly, we backup .bash_profile to .bash_profile.safs.bak (We should replace .bash_profile 
      with .bash_profile.safs.bak when we uninstall SAFS!!!)
      Then, we create a new file .bash_profile.safs.add (This file contains the script for setting SAFS,
      STAF environment, and starting STAF.)
      Finally, append the contents of .bash_profile.safs.add to .bash_profile

      This script contents of /etc/skel/.bash_profile will be automatically copied to user's home directory 
      when a new user is created.

      What are the contents that we append to this automatic script?
      e.1. Call STAF environment script to setup the STAF environment variables. Please pay attention to
           the variable STAF_INSTANCE, this should be different for each user.
      e.2  Call SAFSEnv.sh to setup the SAFS environment variables.
      e.3  Create a new STAF configuration file: STAF.cfg, you may need to modify the 'ssl port' and 'tcp port'
      e.4  Use command "staf/bin/STAFProc STAF.cfg" to start STAF

   f. Modify file /etc/skel/.bash_logout ( or ~/.bahs_logout if you are normal user)
      Firstly, we backup .bash_logout to .bash_logout.safs.bak
      Then, we create a new file .bash_logout.safs.add (This file contains the script for stopping STAF.)
      Finally, append the contents of .bash_logout.safs.add to .bash_logout

   g. Call script modifySAFSFiles.sh to create .ini and .sh files, which are used to start the TID sample or SPC etc.
      It will also remove un-necessary files:
      .bat files under directory Project of SAFS home, for example /usr/local/safs/Project;
      .wsf and .vbs files under SAFS home, for example /usr/local/safs/;
      .bat, .wsf, .vbs and .exe files under bin of SAFS home, for example /usr/local/safs/bin;

   h. If you are super user root, script will create a file ModifyDotBash.sh under the bin directory of your
      safs installation location, for example, /usr/local/safs/bin. This script will be used to modify the
      existing users' starting-script, e.g. .bash_profile and .bash_logout