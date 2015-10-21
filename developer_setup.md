# Environment Setup for Developers

### Step 1: Prepare Build Environment
Before you can successfully run any build, you need to install/verify the following:

1. Prepare Ant
  1. Ant can be downloaded from [Ant Official Website](http://ant.apache.org/). The latest version is recommended and the minimum version required is **1.8.2**.
  2. After installation, we need to set System Environment **ANT_HOME** pointing to the folder, where ant has been installed. For example *c:/apache-ant-1.9.3*.
  3. We also need to add **%ANT_HOME%/bin** to the *PATH* environment variable so that *ant* will be recognized as executable.
  4. Put the following *JARs* into **%ANT_HOME%/lib** folder:
    1. **antcontrib.jar** can be downloaded from [ant-contrib site](http://sourceforge.net/projects/ant-contrib/files/ant-contrib/). The minimum version required is **1.0b3**.
	2. **apache-commons-net.jar** can be downloaded from [commons-net site](https://commons.apache.org/proper/commons-net/). The minimum version required is **3.3**.
	3. **jakarta-oro-2.0.8.jar** can be downloaded from [Apache archive site](https://archive.apache.org/dist/jakarta/oro/).
2. Install the git bash 
  1. **Git Bash** can be downloaded from [git-scm site](https://git-scm.com/downloads). The latest version is recommended. We are using the version **2.5.0** when writing this document.
  2. We need to add its *bin* directory to the *PATH* environment so that git will be recognized as executable.


### Step 2: Prepare Develop Environment
Before you can successfully run any build, you need to install/verify the following:

1. Clone all repositories into your local folder, for example: to *c:\SAFSDev*. You will have all repositories under that folder:
   - build
   - Core
   - keywords
   - libs
   - META-INF
   - SAFS-Android-Engine
   - ......
   - VBDDVariables
2. Create a "SAFS Build folder", such as *c:\SAFSBuild*, which is used to build SAFS/SE+. Create an Environment Variable **%SAFS_BUILD%** pointing to "SAFS Build folder".
3. Go to *c:\SAFSDev\build\safs*, copy the file *bootstrap.build.xml* to **%SAFS_BUILD%**.
4. Provide private FTP server:
  1. In *%SAFS_BUILD%/safs.properties* file, you can provide your own private FTP server, which contains the NOT free testing engines, like: Rational Functional Tester, by setting properties: "private.ftpserver", "private.ftpuserid", and "private.ftppasswd". If you don't have private testing engines, the Ant scripts will just ignore these things.
5. Call ```ant -f bootstrap.build.xml bootstrapbuild``` to get all latest ant build scripts.
6. Call ```ant safs.win.prepare``` to prepare the development environment. When this succeed, you will be able to see the **libs** and **safsjars**, which contain the dependency files
   needed by SAFS projects.


### Step 3: Import SAFS Projects
1. Open Eclipse, create a new Variable **SAFS_BUILD**, the value is the **SAFS Build folder** (e.g. c:\SAFSBuild) which is created in **Step 1**.
2. Import the project **SAFS-Android-Remote-Control** into Eclipse and build it, it should succeed.
3. Import the core project **Core**
  1. If you don't have the folder **%SAFS_BUILD%/libs/rft** or you don't plan to develop for **SAFS_RFT**:
    1. Delete the whole folder *org\safs\rational*.
    2. Delete all items beginning with *SAFS_BUILD/libs/rft* in **Java Build Path->Libraries**.
  2. Finally you can build this project, it should succeed.


### Note
If the Internet connection is slow, the script running may stuck in DOS console, user can press the *Enter* to let it continue. Please do **NOT terminate** the script manually or close the DOS console, which will leave some hidden folders like "Core", and they will cause the failures for next build. More details can be found in [Appendix for developer setup from GitHub](http://safsdev.freeforums.net/thread/20/appendix-developer-setup-github).


### Help Me
If you still have any problems or questions, please consult our forum at [SAFS Forum](http://safsdev.freeforums.net/). If you cannot find answers on this forum, you can post your questions there.

You can write us an email at safsdev@sas.com if you don't get support on [SAFS Forum](http://safsdev.freeforums.net/).
