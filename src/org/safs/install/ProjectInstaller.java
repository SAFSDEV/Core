/** Copyright (C) SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.install;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.safs.text.FileUtilities;
import org.safs.text.INIFileReader;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;

/**
 * Prepare and install a new SAFS Project based on a config.ini, safstid.ini, environment variables, 
 * command-line options, and internal defaults.
 * <p>
 * Virtually everything is optional.  
 * <p>
 * If no parameters or options are provided then a new project will be 
 * created with the current "Working Directory" as the root project directory.
 * <p>
 * If only a -project or ProjectName is provided, then a new project will be created as a subdirectory 
 * of the current "Working Directory".
 * <p>
 * The class not only creates the required directory structure, but will also attempt to populate the 
 * project with common project-specific utility scripts and assets.
 * <p>
 * Refer to <a href="http://safsdev.sourceforge.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile" 
 * alt="Config File Options" title="Config File Options">Config File Options</a> for more detailed 
 * information on available options.
 * <p>
 * @author canagl
 * @see #processINIFile(File)
 */
public class ProjectInstaller {

	public static final String ARG_CONFIG = "-config";
	public static final String ARG_PATH = "-path";
	public static final String ARG_PROJECT = "-project";
	public static final String ARG_TYPE = "-type";
	public static final String ARG_DATA = "-data";
	public static final String ARG_BENCH = "-bench";
	public static final String ARG_TEST = "-test";
	public static final String ARG_DIF = "-dif";
	public static final String ARG_LOGS = "-logs";
	public static final String ARG_STAF = "-staf";
	public static final String ARG_SAFS = "-safs";
	public static final String ARG_SELENIUM = "-seleniumplus";
	public static final String ARG_OUTPUT = "-output";
	
	protected String LOG_NAME = "projectsetup.log";
	
	protected File safstidFile = null;
	protected File configFile = null;
	
	protected File workDir = new File(System.getProperty(DriverConstant.PROPERTY_USER_DIR));
	protected File stafDir = null;
	
	protected File safsHome = null;
	protected File seleniumHome = null;
	protected File safsDir = null; // safs or seleniumplus?
	
	protected File projectDir = null;
	protected File projectParent = null;
	protected File driverDir = null; 
	protected File dataDir = null;
	protected File benchDir = null;
	protected File difDir = null;
	protected File testDir = null;
	protected File logsDir = null;
	
	protected File outputDir = null;
	
	protected StringBuffer progress = null;
	
	protected boolean isSAFS = false;
	protected boolean isSelenium = false;
	
	/** newline */
    protected static final String nl = System.getProperty(DriverConstant.PROPERTY_LINE_SEPARATOR); 
    
	
	/**
	 * Default constructor.<br>
	 * Functionality invoked with createProject(String[]).
	 * @see #createProject(String[]) 
	 */
	public ProjectInstaller() {
		super();
	}
	
	/** write progress/debug info to one or more sinks */
	protected void progress(String message){
		if(progress == null) progress = new StringBuffer();
		progress.append(message + nl);
		System.out.println(message); // might comment out this System.out.
	}

	/** 
	 * first-order settings
	 * <p><ul>Check Environment Settings:
	 * <p>
	 * <li>STAFDIR -- STAF Install Directory
	 * <li>SAFSDIR -- SAFS Install Directory
	 * <li>SELENIUM_PLUS -- SeleniumPlus Install Directory
	 * </ul><p>
	 * Anything found and set here can be overridden by second- and third-order settings.
	 * <p>
	 * @see #processINIFile(File)
	 * @see #processCMDLine(String[])
	 */
	protected void processEnvironment(){
		String v = null;
		try{ 
			v = System.getenv(STAFInstaller.STAFDIREnv);
			if(v != null){
				File tempfile = new CaseInsensitiveFile(v).toFile();
				if(tempfile.isDirectory()){
					stafDir = tempfile;
					progress("Environment setting for STAF: "+ stafDir.getAbsolutePath());
				}
			}
		}
		catch(Throwable ignore){}
		if(stafDir == null) progress("Environment setting for STAF not found.");
		
		v = null;
		try{ 
			v = System.getenv(SAFSInstaller.SAFSDIREnv);
			if(v != null){
				File tempfile = new CaseInsensitiveFile(v).toFile();
				if(tempfile.isDirectory()){
					safsHome = tempfile;
					isSAFS = true;
					progress("Environment setting for SAFS: "+ safsHome.getAbsolutePath());
				}
			}else{
				progress("Environment setting for SAFS was not found.");
			}
		}
		catch(Throwable ignore){}

		v = null;
		try{ 
			v = System.getenv(SeleniumPlusInstaller.SELENIUMDIREnv);
			if(v != null){
				File tempfile = new CaseInsensitiveFile(v).toFile();
				if(tempfile.isDirectory()){
					seleniumHome = tempfile;
					if(safsHome == null) {
						isSAFS = false;
						isSelenium = true;
					}
					progress("Environment setting for SeleniumPlus: "+ seleniumHome.getAbsolutePath());
				}
			}else{
				progress("Environment setting for SeleniumPlus was not found.");
			}
		}
		catch(Throwable ignore){}

		safsDir = isSAFS ? safsHome : seleniumHome;
		if(safsDir == null) {
			progress("Environment setting for DriverRoot not found.");
		}else{
			if(isSAFS)
				progress("Environment setting for DriverRoot using SAFS setting "+safsDir.getAbsolutePath());
			else if(isSelenium)
				progress("Environment setting for DriverRoot using SeleniumPlus setting "+safsDir.getAbsolutePath());
			else{
				progress("Environment setting for DriverRoot using UNKNOWN setting "+safsDir.getAbsolutePath());
			}
		}
	}
	
	/** second-order settings
	 * <p>
	 * override first-order settings. 
	 * <p>
	 * Anything found and set here can be overridden by third-order settings.
	 * <p>
	 * INI File Settings Sought:
	 * <pre><ol><li>[STAF]
	 * PATH=pathTo/STAFProc (parsed for STAF Install Directory)
	 * <p><li>[SAFS_DRIVER]
	 * DriverName=SAFS | SeleniumPlus
	 * DriverRoot=pathTo/SAFS or SeleniumPlus Install Directory
	 * <p><li>[SAFS_PROJECT]
	 * ProjectRoot=pathTo/ProjectParent directory (or ProjectDirectory if ProjectName is not provided)
	 * ProjectName=NameOfProject -- appended to project parent directory if provided
	 * <p><li>[SAFS_DIRECTORIES]
	 * DATADIR =Data directory -- absolute path, or project-relative path
	 * BENCHDIR=Benchmark directory -- absolute path, or project-relative path
	 * TESTDIR =Test directory -- absolute path, or project-relative path
	 * DIFFDIR =Differences directory -- absolute path, or project-relative path
	 * LOGDIR =Logs directory -- absolute path, or project-relative path
	 * </ol></pre>
	 * @see #processEnvironment()
	 * @see #processCMDLine(String[])
	 */
	protected void processINIFile(File inifile){
		String encoding = FileUtilities.detectFileEncoding(inifile.getAbsolutePath());
		INIFileReader reader = encoding != null ? 
				               new INIFileReader(inifile,INIFileReader.IFR_MEMORY_MODE_STORED, encoding):
				               new INIFileReader(inifile, INIFileReader.IFR_MEMORY_MODE_STORED);
        
		progress("Processing INI file: "+ inifile.getAbsolutePath());
		
        // **********************
	    // seek STAFProc executable in INI				               
        // **********************
        String tempvalue = reader.getAppMapItem(DriverConstant.SECTION_STAF, "PATH"); // executable
        File tempfile = null;
        if(tempvalue != null){
        	tempfile = new CaseInsensitiveFile(tempvalue).toFile();
        	if(tempfile.isFile()){
        	    if(tempfile.getParentFile().getParentFile().isDirectory()){
                	stafDir = tempfile.getParentFile().getParentFile(); // STAFDIR
					progress("INI setting for STAF Root: "+ stafDir.getAbsolutePath());
        	    }else{
					progress("INI setting for STAF Root is NOT a directory!");
        	    }
        	}else{
				progress("INI setting for STAF PATH is NOT valid!");
        	}
        }else{
			progress("INI setting for STAF not found.");
        }

        // **********************
	    // seek DriverName in INI ( DriverName=SAFS | SeleniumPlus )
        // Must be checked before DriverRoot
        // **********************
        tempvalue = reader.getAppMapItem(DriverConstant.SECTION_SAFS_DRIVER, "DriverName"); 
        if(tempvalue != null){
			if(tempvalue.equalsIgnoreCase(ARG_SELENIUM.substring(1))){
				isSAFS = false;
				isSelenium = true;
				if(seleniumHome != null) {
					safsDir = seleniumHome;
				}
			}else if(tempvalue.equalsIgnoreCase(ARG_SAFS.substring(1))){
				isSAFS = true;
				isSelenium = false;
				if(safsHome != null) {
					safsDir = safsHome;
				}
			}else{
				
			}
			progress("INI setting for DriverName processed as: "+ tempvalue);

			if(safsDir != null){
				progress("INI setting for DriverName making DriverRoot: "+ safsDir.getAbsolutePath());        	
			}else{
				progress("INI setting for DriverName does NOT yet point to a valid DriverRoot.");        	
			}
        }

        // **********************
	    // seek DriverRoot in INI
        // should be checked AFTER DriverName
        // **********************
        tempvalue = reader.getAppMapItem(DriverConstant.SECTION_SAFS_DRIVER, DriverConstant.ITEM_DRIVER_ROOT); // root dir
        if(tempvalue != null){
        	tempfile = new CaseInsensitiveFile(tempvalue).toFile();
        	if(tempfile.isFile()){
        	    if(tempfile.getParentFile().isDirectory()){
                	safsDir = tempfile.getParentFile(); // SAFSDIR, SELENIUMPLUS, etc..
					progress("INI setting for DriverRoot: "+ safsDir.getAbsolutePath());
        	    }else{
					progress("INI setting for DriverRoot is NOT a directory!");
        	    }
        	}else{
				progress("INI setting for DriverRoot is NOT valid!");        		
        	}
        }else{
			progress("INI setting for Driver Root not found.");        	
        }

        // **********************
	    // seek Project Info in INI
        // **********************        
        // project PATH 
        tempvalue = reader.getAppMapItem(DriverConstant.SECTION_SAFS_PROJECT, DriverConstant.ITEM_PROJECT_ROOT); // root dir
        if(tempvalue != null){
        	// doesn't have to exist, but does need to be an absolute path
        	tempfile = new File(tempvalue);
        	if(tempfile.isAbsolute()){
        		projectParent = tempfile;
    			progress("INI setting for ProjectRoot: "+ tempvalue);        	
        	}else{
    			progress("INI setting for ProjectRoot is NOT an absolute file path and will be ignored.");        	
        	}
        }else{
			progress("INI setting for Project Path not found.");        	
        }
        // project NAME
        // appends to ProjectPath if different than last in PATH
        // or appends to Working Directory if different from working directory
        tempvalue = reader.getAppMapItem(DriverConstant.SECTION_SAFS_PROJECT, "ProjectName"); // root dir
        if(tempvalue != null){
        	if(projectParent != null){
        		if( ! projectParent.getName().toUpperCase().equalsIgnoreCase(tempvalue.toUpperCase())){
        			projectDir = new File(tempvalue);
        			progress("INI setting for ProjectRoot relative ProjectName: "+ tempvalue);        	
        		}else{
        			progress("INI setting for ProjectName matches ProjectRoot and may be ignored.");        	
        		}
        	}else{
    			projectDir = new File(tempvalue);
    			progress("INI setting for ProjectName with no ProjectRoot: "+ tempvalue);        	
        	}
        }else{
			progress("INI setting for ProjectName not found.");        	
        }
        
        // **********************
	    // seek Directories Info in INI
        // **********************
        tempvalue = reader.getAppMapItem(DriverConstant.SECTION_SAFS_DIRECTORIES, "DataDir"); // root dir
        if(tempvalue != null){
        	dataDir = new File(tempvalue);
			progress("INI setting for Data Directory: "+ tempvalue);        	
        }else{
			progress("INI setting for Data Directory was not found.");        	
        }
        tempvalue = reader.getAppMapItem(DriverConstant.SECTION_SAFS_DIRECTORIES, "BenchDir");
        if(tempvalue != null){
        	benchDir = new File(tempvalue);
			progress("INI setting for Benchmark Directory: "+ tempvalue);        	
        }else{
			progress("INI setting for Benchmark Directory was not found.");        	
        }
        tempvalue = reader.getAppMapItem(DriverConstant.SECTION_SAFS_DIRECTORIES, "TestDir");
        if(tempvalue != null){
        	testDir = new File(tempvalue);
			progress("INI setting for Test/Actuals Directory: "+ tempvalue);        	
        }else{
			progress("INI setting for Test/Actuals Directory was not found.");        	
        }
        tempvalue = reader.getAppMapItem(DriverConstant.SECTION_SAFS_DIRECTORIES, "DiffDir");
        if(tempvalue != null){
        	difDir = new File(tempvalue);
			progress("INI setting for Differences Directory: "+ tempvalue);        	
        }else{
			progress("INI setting for Differences Directory was not found.");        	
        }
        tempvalue = reader.getAppMapItem(DriverConstant.SECTION_SAFS_DIRECTORIES, "LogDir");
        if(tempvalue != null){
        	logsDir = new File(tempvalue);
			progress("INI setting for Logs Directory: "+ tempvalue);        	
        }else{
			progress("INI setting for Logs Directory was not found.");        	
        }        
	}
	
	/** third-order settings
	 * <p>
	 * override first- and second-order settings.
	 * <p>
	 *  @param args -- {@link #createProject(String[])}
	 * @see #processEnvironment()
	 * @see #seekConfigFiles(String[])
	 * @see #processINIFile(File)
	 * @see #deduceMissingParameters()
	 */
	protected void processCMDLine(String[] args){
		/* -config pathToTest.ini already handled by seekConfigFiles */
		String arg = null;
		String temp = null;
		File tempfile = null;
		for(int i=0;i < args.length;i++){
			arg= args[i];
			try{
				if(arg.equalsIgnoreCase(ARG_PROJECT)){
					projectDir = new File(args[++i]);
					progress(ARG_PROJECT+" processed as: "+ projectDir.getName());        	
				}else if(arg.equalsIgnoreCase(ARG_PATH)){
					projectParent = new File(args[++i]);
					progress(ARG_PATH+" processed as: "+ projectParent.getPath());        	
				}else if(arg.equalsIgnoreCase(ARG_TYPE)){
					temp = args[++i];
					if(temp.equalsIgnoreCase(ARG_SELENIUM.substring(1))){
						if(seleniumHome != null) {
							isSAFS = false;
							safsDir = seleniumHome;
							progress(ARG_TYPE+" altering DriverRoot to: "+ safsDir.getAbsolutePath());        	
						}
					}
					progress(ARG_TYPE+" processed as: "+ temp);        	
				}else if(arg.equalsIgnoreCase(ARG_DATA)){
					dataDir = new File(args[++i]);
					progress(ARG_DATA+" processed as: "+ dataDir.getPath());        	
				}else if(arg.equalsIgnoreCase(ARG_BENCH)){
					benchDir = new File(args[++i]);
					progress(ARG_BENCH+" processed as: "+ benchDir.getPath());        	
				}else if(arg.equalsIgnoreCase(ARG_TEST)){
					testDir = new File(args[++i]);
					progress(ARG_TEST+" processed as: "+ testDir.getPath());        	
				}else if(arg.equalsIgnoreCase(ARG_DIF)){
					difDir = new File(args[++i]);
					progress(ARG_DIF+" processed as: "+ difDir.getPath());        	
				}else if(arg.equalsIgnoreCase(ARG_LOGS)){
					logsDir = new File(args[++i]);
					progress(ARG_LOGS+" processed as: "+ logsDir.getPath());        	
				}else if(arg.equalsIgnoreCase(ARG_STAF)){
					tempfile = new CaseInsensitiveFile(args[++i]).toFile();
					if(tempfile.isAbsolute()&& tempfile.isDirectory()){
						stafDir = tempfile;
						progress(ARG_STAF+" processed as: "+ stafDir.getPath());
					}else{
						progress(ARG_STAF+" argument "+ tempfile.getPath()+" does not appear to be valid.");
					}
				}else if(arg.equalsIgnoreCase(ARG_SAFS)){
					tempfile = new CaseInsensitiveFile(args[++i]).toFile();
					if(tempfile.isAbsolute()&& tempfile.isDirectory()){
						safsHome = tempfile;
						safsDir = tempfile;
						isSAFS = true;
						isSelenium = false;
						progress(ARG_SAFS+" altering DriverRoot to: "+ safsDir.getPath());
					}else{
						progress(ARG_SAFS+" argument "+ tempfile.getPath()+" does not appear to be valid.");
					}
				}else if(arg.equalsIgnoreCase(ARG_SELENIUM)){
					tempfile = new CaseInsensitiveFile(args[++i]).toFile();
					if(tempfile.isAbsolute()&& tempfile.isDirectory()){
						seleniumHome = tempfile;
						safsDir = tempfile;
						isSAFS = false;
						isSelenium = true;
						progress(ARG_SELENIUM+" altering DriverRoot to: "+ safsDir.getPath());
					}else{
						progress(ARG_SELENIUM+" argument "+ tempfile.getPath()+" does not appear to be valid.");
					}
				}else if(arg.equalsIgnoreCase(ARG_OUTPUT)){
					tempfile = new CaseInsensitiveFile(args[++i]).toFile();
					if(tempfile.isAbsolute()&& tempfile.isDirectory()){
						outputDir = tempfile;
						progress(ARG_OUTPUT+" using Directory: "+ outputDir.getPath());
					}else{
						progress(ARG_OUTPUT+" argument "+ tempfile.getPath()+" does not appear to be valid.");
					}
				}else{
					progress(arg +" is not recognized as a valid command-line argument.");
				}
			}catch(Exception x){
				progress(x.getClass().getSimpleName()+": "+x.getMessage()+" while processing arg: "+ arg);
			}
		}
	}
	
	/**
	 * check for -config argument from command-line.<br>
	 * -config can be absolute or relative to the working directory.<br>
	 * Attempt to locate safstid.ini relative to -config, if not then<br> 
	 * Attempt to locate safstid.ini relative to working directory.<br>
	 * sets configFile and safstidFile variables as appropriate.
	 * @param args
	 */
	protected void seekConfigFiles(String[] args){
		String configPath = null;
		String safstidPath = null;
		String arg = null;
		File tempfile = null;
		
		// see if -config file is absolute or relative		
		for(int i=0;i < args.length;i++){
			arg = args[i];
			if(arg.equalsIgnoreCase(ARG_CONFIG)){
				try{ configPath = args[i+1]; }
				catch(Exception ignore){
					progress(ARG_CONFIG +" incomplete <file> argument.");        	
				}
				break;
			}
		}
		// seek config INI
		if(configPath != null){
			tempfile = new CaseInsensitiveFile(configPath).toFile();
			if(! tempfile.isAbsolute()) tempfile = new CaseInsensitiveFile(workDir, configPath).toFile();
			if (tempfile.isAbsolute()){
				if(tempfile.isFile()){
					configFile = tempfile;
					progress(ARG_CONFIG +" processed as: "+ configFile.getAbsolutePath());        	
				}else{
					progress(ARG_CONFIG +" '"+ tempfile.getAbsolutePath() +"' is NOT a valid absolute file path!");
				}
			}else{
				progress(ARG_CONFIG +" '"+ tempfile.getPath() +"' is NOT a valid relative file path!");
			}
		}else{
			progress(ARG_CONFIG +" was not found.");        	
		}
		
	    // seek safstid INI same place 
		tempfile = null;
		if(configFile != null) tempfile = new CaseInsensitiveFile(configFile.getParent(), DriverConstant.DEFAULT_CONFIGURE_FILENAME).toFile();
		else tempfile = new CaseInsensitiveFile(workDir, DriverConstant.DEFAULT_CONFIGURE_FILENAME).toFile();
		if (tempfile.isAbsolute()){
			if(tempfile.isFile()){
				safstidFile = tempfile;
				progress(DriverConstant.DEFAULT_CONFIGURE_FILENAME +" processed as: "+ safstidFile.getAbsolutePath());        	
			}else{
				progress(DriverConstant.DEFAULT_CONFIGURE_FILENAME +" '"+ tempfile.getAbsolutePath() +"' was not found.");
			}
		}else{
			progress(DriverConstant.DEFAULT_CONFIGURE_FILENAME +" '"+ tempfile.getPath() +"' was not found.");
		}
	}
	
	/** fourth-order (last) processing to attempt to fill-in missing settings relative to other known info. */
	protected void deduceMissingParameters(){
		if(projectParent == null){
			if(projectDir == null){
				projectParent = workDir.getParentFile();
				if(projectParent == null)
					throw new IllegalArgumentException("Cannot use the Platform Root directory as a Project root directory!");
				projectDir = workDir;
			}else{
				if(projectDir.isAbsolute()){
					projectParent = projectDir.getParentFile();
					if(projectParent == null)
						throw new IllegalArgumentException("Cannot use the Platform Root directory as a Project root directory!");
				}else{
					projectParent = workDir;
					projectDir = new File(workDir, projectDir.getPath());
				}
			}
		}else{
			if(projectParent.isAbsolute()){
				if(projectDir == null){
					projectDir = projectParent;
					projectParent = projectParent.getParentFile();
					if(projectParent == null)
						throw new IllegalArgumentException("Cannot use the Platform Root directory as a Project root directory!");
				}else{
					if(projectDir.isAbsolute()){  //overrides
						projectParent = projectDir.getParentFile();
						if(projectParent == null)
							throw new IllegalArgumentException("Cannot use the Platform Root directory as a Project root directory!");
					}else{
						projectDir = new File(projectParent, projectDir.getPath());
					}
				}
			}else{
				if(projectDir == null){
					
				}else{
					if(projectDir.isAbsolute()){  //overriding
						projectParent = projectDir.getParentFile();
						if(projectParent == null)
							throw new IllegalArgumentException("Cannot use the Platform Root directory as a Project root directory!");
					}else{
						projectParent = new File(workDir, projectParent.getPath());
						projectDir = new File(projectParent, projectDir.getPath());
					}
				}
			}
		}
		progress("Using Parent  directory: "+ projectParent.getPath());
		progress("Using Project directory: "+ projectDir.getPath());
		
		// DATAPOOL
		if(dataDir == null){
			dataDir = new File(projectDir, DriverConstant.DEFAULT_PROJECT_DATAPOOL);
		}else{
			if(! dataDir.isAbsolute())
				dataDir = new File(projectDir, dataDir.getPath());
		}
		progress("Using Data    directory: "+ dataDir.getPath());

		// BENCH
		if(benchDir == null){
			benchDir = new File(dataDir, DriverConstant.DEFAULT_PROJECT_BENCH);
		}else{
			if(! benchDir.isAbsolute())
				benchDir = new File(projectDir, benchDir.getPath());
		}
		progress("Using Bench   directory: "+ benchDir.getPath());

		// TEST
		if(testDir == null){
			testDir = new File(dataDir, DriverConstant.DEFAULT_PROJECT_TEST);
		}else{
			if(! testDir.isAbsolute())
				testDir = new File(projectDir, testDir.getPath());
		}
		progress("Using Test    directory: "+ testDir.getPath());

		// DIF
		if(difDir == null){
			difDir = new File(dataDir, DriverConstant.DEFAULT_PROJECT_DIF);
		}else{
			if(! difDir.isAbsolute())
				difDir = new File(projectDir, difDir.getPath());
		}
		progress("Using Diff    directory: "+ difDir.getPath());

		// LOGS
		if(logsDir == null){
			logsDir = new File(dataDir, DriverConstant.DEFAULT_PROJECT_LOGS);
		}else{
			if(! logsDir.isAbsolute())
				logsDir = new File(projectDir, logsDir.getPath());
		}
		progress("Using Logs    directory: "+ logsDir.getPath());
	}
	
	/**
	 * @param target -- can be relative or absolute
	 * @param relativeParent -- must be absolute if target is relative. can be null if target is absolute.
	 * @param role - for logging/debug purposes (Project, Data, Test, Logs, Bench, Diff) 
	 * @throws IllegalArgumentException if we are unable to create the directory.
	 */
	protected void createDirectory(File target, File relativeParent, String role) throws IllegalArgumentException{
		if(!target.isAbsolute()){
			target = new File(relativeParent, target.getPath());
			progress(role+" directory processed as: "+ target.getAbsolutePath());
		}
		if (! target.isDirectory()) {
			target.mkdirs();				
			if(target.isDirectory()) 
				progress(target.getAbsolutePath()+" has been created.");
			else{
				progress("Failed to create "+ role +" directory: "+target.getAbsolutePath());
				throw new IllegalArgumentException("Failed to create "+role+" directory: "+target.getAbsolutePath());
			}
		}else{
			progress(role+" directory '"+ target.getAbsolutePath()+"' already exists.");
		}
	}
	
	/**
	 * Create the project directory structure using the required preset values.
	 * @throws IllegalArgumentException -- if required parameters are invalid or null.
	 * @throws SecurityException 
	 */
	protected void createProjectStructure() throws IllegalArgumentException, SecurityException{
		if(projectDir == null) throw new IllegalArgumentException(ARG_PROJECT +" was not provided or deduced.");
		if(dataDir == null) throw new IllegalArgumentException(ARG_DATA +" was not provided or deduced.");
		if(benchDir == null) throw new IllegalArgumentException(ARG_BENCH +" was not provided or deduced.");
		if(testDir == null) throw new IllegalArgumentException(ARG_TEST +" was not provided or deduced.");
		if(difDir == null) throw new IllegalArgumentException(ARG_DIF +" was not provided or deduced.");
		if(logsDir == null) throw new IllegalArgumentException(ARG_LOGS +" was not provided or deduced.");
		if(!projectDir.isAbsolute()){
			if(projectParent == null) throw new IllegalArgumentException(ARG_PATH +" was not provided or deduced.");
			if(!projectParent.isAbsolute()) throw new IllegalArgumentException("Full absolute path to project was not provided or deduced.");
		}
		createDirectory(projectDir, projectParent, "Project");
		if(! projectDir.isAbsolute()) {
			projectDir = new File(projectParent, projectDir.getPath());
		}
		createDirectory(dataDir, projectDir, "Data");
		createDirectory(benchDir, projectDir, "Bench");
		createDirectory(testDir, projectDir, "Test");
		createDirectory(difDir, projectDir, "Diff");
		createDirectory(logsDir, projectDir, "Logs");
	}

	/**
	 * If no arguments are provided then a default Project will be created in the "working directory".
	 * @param args 
	 * <ul>
	 * -config pathToTest.ini
	 * <ul>if not present will attempt to locate a "safstid.ini" in the current working directory. 
	 * If safstid.ini is not present it will attempt to use other command-line parameters and internal 
	 * default values.</ul>
	 * -project ProjectName
	 * <ul>Optional Name for the Project.  This equates to the name of the root directory of the Project. 
	 * If not provided, it is assumed the provided or derived path for the Project constitutes the name of 
	 * the Project.</ul>
	 * -path pathToProject
	 * <ul>full or ini-relative path to the new Project. Would override any ProjectRoot INI entry.
	 * if the -project arg is also provided then the ProjectName is appended to -path to determine the full 
	 * root directory for the new Project.</ul>
	 * -type SAFS|SeleniumPlus
	 * <ul>Defaults to SAFS project type, unless SeleniumPlus is specified.<br>
	 * Only matters if BOTH SAFS and SeleniumPlus are installed on the machine and neither -safs nor 
	 * -seleniumplus are provided.</ul>
	 * -data pathToDataDir
	 * <ul>Defaults to "Datapool" if not provided. Would override any DataDir INI entry.</ul>
	 * -bench pathToBenchDir
	 * <ul>Defaults to "Bench" if not provided. Would override any BenchDir INI entry.</ul>
	 * -test pathToTestDir
	 * <ul>Defaults to "Test" if not provided. Would override any TestDir INI entry.</ul>
	 * -dif pathToDifDir<br>
	 * <ul>Defaults to "Dif" if not provided. Would override any DiffDir INI entry.</ul>
	 * -logs pathToLogsDir<br>
	 * <ul>Defaults to "Logs" if not provided. Would override any LogDir INI entry.</ul>
	 * -staf pathToSTAFInstallDir
	 * <ul>Otherwise seeks STAFDIR environment variable.</ul>
	 * -safs pathToSAFSInstallDir
	 * <ul>Otherwise seeks SAFSDIR environment variable.</ul>
	 * -seleniumplus pathToSeleniumPlusInstallDir
	 * <ul>Otherwise seeks SELENIUM_PLUS environment variable.</ul>
	 * -output pathToOutputDir to contain our projectsetup.log
	 * <ul>Otherwise seeks SELENIUM_PLUS environment variable.</ul>
	 * </ul>
	 * @see #processEnvironment()
	 * @see #seekConfigFiles(String[])
	 * @see #processINIFile(File)
	 * @see #processCMDLine(String[])
	 * @see #deduceMissingParameters()
	 */
	public void createProject(String[] args){
		progress("ProjectInstaller runtime working directory: "+ workDir.getAbsolutePath());
		
		try{
			// the order of execution below is intentional and should NOT be changed
			// each process method below is intended to override the previous one.
			processEnvironment();
			seekConfigFiles(args);
			if(safstidFile != null && safstidFile.isFile()) processINIFile(safstidFile);
			if(configFile != null && configFile.isFile()) processINIFile(configFile);
			processCMDLine(args);
			deduceMissingParameters();
			createProjectStructure();
			progress("ProjectInstall has completed.");
		}catch(Throwable error){
			progress("ProjectInstaller failed with "+ error.getClass().getName()+":\n"+ error.getMessage());
		}
		
		// output our project setup log
		File log = null;
		if(outputDir != null && outputDir.isDirectory())
			log = new File(outputDir, LOG_NAME);
		else if(projectDir != null && projectDir.isDirectory())
			log = new File(projectDir, LOG_NAME);
		else
			log = new File(workDir, LOG_NAME);		
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(log));
			writer.write(progress.toString());
			writer.flush();
			writer.close();
			writer = null;
		}catch(IOException x){
			System.err.println("Failed to write project creation log to "+ log.getPath());
		}
	}
	
	/** @see #createProject(String[])  */
	public static void main(String[] args) {
		new ProjectInstaller().createProject(args);
	}

}
