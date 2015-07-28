
29 AUG, 2011  SAFS Development Team

Tested OS: Mac OS X 10.6.8 Snow Leapard

###########################  INSTALLATION ASSETS ##############################

Mac_Install_Readme.txt      This file itself

GNU General Public License.txt   The GPL license text.

modify-IOS-SAFSFiles.sh     Create .ini and .sh file for SAFS, Remove un-useful
                            files of Windows OS

SAFSInstall.jar             The Java Silent Installer, who does the real install
                            and un-install work.

SAFSInstall.ZIP             Contains all assets for SAFS Installation.

Setup-IOS-SAFS.sh           The script to start install SAFS (including STAF)

sharedFunctions.sh          Contains common functions used during SAFS install

sharedVariablesIos.sh       Contains common variables used during SAFS install

STAF346-setup-macosx0i386.bin     STAF install executable, which will be invoked by
                            our Java Silent Installer (SAFSInstall.jar), This
                            Executable has a JRE1.6 inside, In future we may 
                            use STAF-install-executable without JRE inside.

Uninstall-IOS-SAFS.sh       The script to uninstall SAFS (including STAF)

###############################################################################

P1. Packaging: The install package should initially be a ZIP file 
    containing all the above.  Ex: SAFSIOSRelease2011.08.29.ZIP
    
P2. Packaging: Inside SAFSInstall.ZIP should be the remaining SAFS assets to install 
    including all Java libraries and dependencies, IBT dependencies, etc.  These 
    are generally laid out in the ZIP in the folder structure required for SAFS.
    This should include:
    
    SAFS/*.sh
    SAFS/*.ini
    SAFS/*.txt
    SAFS/data/ (empty)
    SAFS/doc/*.*
    SAFS/doc/info/SAFSImageBasedRecognition.htm
    SAFS/doc/static/*.*
    SAFS/IOS/*.* (all subdirectories)
    SAFS/keywords/*.*
    SAFS/lib/:
    (?) clibwrapper_jiio.jar
        CustomJavaObjectsMap.dat (sample)
    (?) dom4j-2.0.0-ALPHA-2.jar
        FilterImageGUI.jar
        jaccess.jar     (2000 version?)
        jai_codec.jar   (2004 version?)
        jai_core.jar    (2004 version?)
        jai_imageio.jar (2010 version?)
    (?) jakarta-regexp-1.3.jar
    (?) jakarta-regexp-1.3.zip (redist)
        java.policy        (RMI)
        JavaObjectsMap.dat (until GuiClassData refactored)
    (?) jaxen-1.1.1.jar
        jna.zip
    (?) nekohtml.jar
        ObjectTypesMap.dat
        safs.jar
        safsabbot.jar
        safscust.jar
        safsdebug.jar
        safsinput.jar
        SAFSInstall.jar
        safsios.jar
        safsjvmagent.jar
        safsjvmagent.properties (with Mac paths)
        SAFSKeycodeMap.dat      (Mac specific ?)
        safslogs.jar
        safsmaps.jar
        safsmodel.jar
        safsselenium.jar
        safsvars.jar
        selenium-server-standalone-2.0b1.jar
    SAFS/Project:
        *.ini
        *.sh
    SAFS/Project/Datapool/*.* (all subdirectories)
    SAFS/samples/Log Transforms: (as appropriate)
        *.xsl
        *.xml
        *.htm
    SAFS/Perl/*.*
    

1. How to install STAF and SAFS?
   =============================
   1.1 After you login, go to directory where you put the SAFSIOSRelease ZIP file.

   1.2 Double-Click it or otherwise extract the contents of the ZIP file into a directory.

   1.3 Find the Setup-IOS-SAFS.sh, which is an interactive script to setup STAF and SAFS.

   1.4 Run ./Setup-IOS-SAFS.sh in your shell, if it can't be run:
   
       a. make sure the script is executable: 'chmod u+x Setup-IOS-SAFS.sh'
       b. prefix the shell: '/bin/sh Setup-IOS-SAFS.sh'

   1.5 Follow the script's instructions which will prompt for things like:
   
       a. Confirm to install STAF and where to install STAF.
       b. Confirm to install SAFS and where to install SAFS.
       c. Confirm to see verbose information during installation.
       d. Use {Enter} at each prompt to install SAFS and STAF to the default locations:
    
          STAF  --> /Library/staf
          SAFS  --> /Library/safs  

2. What the install script does--The Details
   =========================================
   
   2.1 The installation script will detect the java version on your system. 
   
   2.2 SAFS require the JRE should be 1.5 or higher. 
   
   2.3 The STAF installation uses its own internal JRE.

   2.4 The script will:
   
       a. Install SAFS and STAF to the indicated location on your machine.
   
       b. Create a data directory under STAF installation directory for STAF.
          For example, with the default /Library/staf, the data directory 
          will be created as /Library/staf/data
          
          The script will attempt to grant write-access-right to all users 
          by applying 'chmod a+w' on the data directory.(When other users run STAF, 
          they need write access right on this directory.)
          
       c. Create a SAFS-environment-setting script (Ex:/Library/staf/STAFEnv.sh):
      
       d. Create/Modify several SAFS Runtime assets:
       
          /Library/safs/Project/safstid.ini
          /Library/safs/Project/iostest.ini
          /Library/safs/Project/iosprocesscontainer.ini
          
          /Library/safs/Project/runIOSTest.sh
          /Library/safs/Project/runIOSProcessContainer.sh
          /Library/safs/Project/runImageManager.sh
          /Library/safs/Project/SAFSTESTLOG_Startup.sh
          /Library/safs/Project/SAFSTESTLOGShutdown.sh
          
          /Library/safs/lib/safsjvmagent.properties
          
       e. Attempt to grant write-access to all users for the SAFS install directories.   
       
       f. Remove any remnant MS Windows assets that might be present in the 
          SAFS install directories.