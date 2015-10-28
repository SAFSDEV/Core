package org.safs.tools.drivers;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Vector;
import org.safs.Log;
import org.safs.text.FAILStrings;
import org.safs.tools.GenericToolsInterface;
import org.safs.tools.ConfigurableToolsInterface;
import org.safs.tools.SimpleToolsInterface;
import org.safs.tools.UniqueStringID;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.engines.TIDDriverCommands;
import org.safs.tools.engines.TIDComponent;
import org.safs.tools.engines.SAFSDRIVERCOMMANDS;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.input.UniqueStringFileInfo;
import org.safs.tools.vars.VarsInterface;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.logs.UniqueStringLogInfo;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.status.SAFSSTATUS;
import org.safs.tools.status.StatusInfo;
import org.safs.tools.status.StatusInterface;
import org.safs.tools.stacks.StacksInterface;
import org.safs.tools.CaseInsensitiveFile;

/**
 * An implementation of AbstractDriver for use by STAFProcessContainer.
 * <p>
 * This class is intended to be used by STAFProcessContainer, but may be useful as a 
 * generic "driver" that can initialize and then shutdown tools needed by a 
 * SAFS-like Driver that is NOT necessarily running tests.
 * <p>
 * Sample Usage:<br>
 * <ul>
 * STAFProcessContainerDriver driver = new STAFProcessContainerDriver();<br>
 * ...<br>
 * driver.initializeDriver();<br>
 * ...do stuff...<br>
 * driver.shutdownDriver();<br>
 * </ul>
 * <p>
 * Command-line Options and Configuration File Options are linked below.
 * <p>
 * The default name of configuration files is "SAFSTID.INI".  There is a hierarchy of
 * configuration files that will be sought based on command-line parameters provided.  
 * This hierarchy is summarized in the Configuration File Options doc linked below and 
 * detailed below:
 * <p>
 * <ol>
 * <li><b>-Dsafs.project.config=c:\SAFSProject\ProcessContainer.INI</b><br>
 * <b>command-line specified PROJECT config file</b><br>
 * a uniquely named config file, other than "SAFSTID.INI", containing config 
 * information intended to override settings in the default PROJECT config file located in the 
 * PROJECT ROOT directory.
 * <p>
 * <li><b>default PROJECT config file</b><br>
 * "SAFSTID.INI" located in the PROJECT ROOT directory.  The PROJECT ROOT would normally 
 * be provided as a command-line parameter.  It may instead be provided in the previous 
 * specified UNIQUE config file.  This file will normally contain settings specific 
 * to an entire project and these override any similar settings in the DRIVER config 
 * files.
 * <p>
 * <li><b>command-line specified DRIVER config file</b><br>
 * Rarely used. A uniquely named config file, other than "SAFSTID.INI", intended to override settings 
 * in any SAFSTID.INI file located in the DRIVER ROOT directory.
 * <p>
 * <li><b>default DRIVER config file</b><br>
 * SAFSTID.INI located in the DRIVER ROOT directory.  The DRIVER ROOT would normally 
 * be provided in a previous config file or as a command-line parameter.  This config 
 * file will contain the bulk of the configuration information that is specific to 
 * the driver and independent of any project or test being executed.
 * </ol>
 * <p>
 * In general, you want to provide the bare minimum of command-line parameters and 
 * place all remaining info in one or more configuration files.  The total of all 
 * command-line parameters and config file information must enable the driver to 
 * locate valid driver and project root directories, project subdirectories, and all 
 * other items necessary to find and configure tools.  See the #initializeDriver() link 
 * below for all the neat things the driver will do during initialization!
 * <p>
 * An example invocation, providing the bare minimum command-line parameters:
 * <p>
 * <ul>
 *     java -Dsafs.project.config=c:\SAFSProject\ProcessContainer.INI SomeProcessContainerImplementation
 * </ul>
 * This then expects ProcessContainer.INI to contain the information concerning which engines 
 * to run, and where the PROJECT ROOT and maybe the DRIVER ROOT directories are located.  
 * The remaining configuration information can reside in SAFSTID.INI files located in 
 * these directories.
 * <p>
 * Sample ProcessContainer.INI in c:\SAFSProject:
 * <p>
 * <ul><pre>
 * [SAFS_PROJECT]
 * ProjectRoot="C:\safsproject"
 *
 * [SAFS_ENGINES]
 * First=org.safs.tools.engines.SAFSSELENIUM
 * 
 * [SAFS_SELENIUM]
 * AUTOLAUNCH=TRUE;
 * </ul>
 * <p>
 * Sample SAFSTID.INI in c:\SAFSProject used by 2default:
 * <p>
 * <ul><pre>
 * [SAFS_DRIVER]
 * DriverRoot="C:\safs"
 * 
 * [SAFS_MAPS]
 * AUTOLAUNCH=TRUE
 * 
 * [SAFS_INPUT]
 * AUTOLAUNCH=TRUE
 * 
 * [SAFS_VARS]
 * AUTOLAUNCH=TRUE
 * 
 * [SAFS_LOGS]
 * AUTOLAUNCH=TRUE
 * </pre>
 * </ul>
 * <p>
 * And that is enough for the driver to configure the environment.
 * <p>
 * Of course, more of the configuration parameters necessary for desired engines 
 * will have to be in those configuration files once the engines actually become 
 * available.
 * <p>
 * @see #initializeDriver()
 * @see <A HREF="http://safsdev.sf.net/doc/JSAFSFrameworkContent.htm#driveroptions">Command-Line Options</A>
 * @see <A HREF="http://safsdev.sf.net/doc/JSAFSFrameworkContent.htm#configfile">Configuration File Options</A>
 */
public class STAFProcessContainerDriver extends AbstractDriver {

	/** "STAFProcessContainerDriver" **/
	public final static String DEFAULT_STAF_PROCESS_CONTAINER_DRIVER = "STAFProcessContainerDriver";
	
	// Configuration Information	
	protected ConfigureLocatorInterface locator = null;
	
	/** Stores ALL instanced EngineInterface objects in instanced order. */
	protected Vector                    engines = new Vector(5,1);
	/** Stores only active 'preferred' engine class names in preferred order. */
	protected Vector                    enginePreference = new Vector(5,1);
	/** Stores ALL instanced EngineInterface objects in classname=engine format. */
	protected Hashtable                 engineObjects    = new Hashtable(5);

	// Directory Information	
	protected String driverConfigPath  = DriverConstant.DEFAULT_CONFIGURE_FILENAME;
	protected String projectConfigPath = DriverConstant.DEFAULT_CONFIGURE_FILENAME;

	/**
	 * Default Constructor used by STAFProcessContainer.
	 * <p>
	 * Once instantiated, the user must still call initializeDriver() to commence complete
	 * initialization of the driver and configured tools for use. 
	 * <p>
	 * Once the user is finished with all driver functionality and tools the user must call 
	 * shutdownDriver() to shutdown all configured tools and the driver itself.
	 * <p>
	 * Sample Usage:<br>
	 * <ul>
	 * STAFProcessContainerDriver driver = new STAFProcessContainerDriver();<br>
	 * ...<br>
	 * driver.initializeDriver();<br>
	 * ...do stuff...<br>
	 * driver.shutdownDriver();<br>
	 * </ul>
	 */
	public STAFProcessContainerDriver(){
		super();
		this.driverName = DEFAULT_STAF_PROCESS_CONTAINER_DRIVER;
	}

	public STAFProcessContainerDriver(String drivername){
		super();
		this.driverName = drivername;
	}

	/**
	 * @see DriverInterface#getEngines()
	 */
	public ListIterator getEngines() { 
			return engines.listIterator();
	}

	/**
	 * @see DriverInterface#getEnginePreferences()
	 */
	public ListIterator getEnginePreferences() { return enginePreference.listIterator();}

	/**
	 * used internally to find the index of an engine marked as "preferred"
	 */
	protected int getPreferredEngineIndex(String key){
		try{
			String uckey = key.toUpperCase();
			int engindex = 0;
			int subindex = -1;
			String eng = null;
			ListIterator list = enginePreference.listIterator();
			while (list.hasNext()){
				eng = (String)list.next();
				subindex = eng.indexOf(uckey);
				if (subindex >= 0) return engindex;
				engindex++;
			}
		}catch(NullPointerException npx){;}
		return -1;				
	}

	/**
	 * used internally to find the validity of an engine name
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	protected String getFullEngineClass(String key) throws IllegalArgumentException{
		try{
			String uckey = key.toUpperCase();
			if (engineObjects.containsKey(uckey)) return uckey;
			int subindex = -1;
			String eng = null;
			Enumeration list = engineObjects.keys();
			while (list.hasMoreElements()){
				eng = (String)list.nextElement();				
				subindex = eng.indexOf(uckey);
				if (subindex >= 0) return eng;
			}
		}catch(NullPointerException npx){;}
		String text = "DefaultDriver.startEnginePreference(KEY)";
		String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
		throw new IllegalArgumentException(err);
	}
	
	/**
	 * @see DriverInterface#getPreferredEngine(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public EngineInterface getPreferredEngine(String key) throws IllegalArgumentException{
		String uckey = getFullEngineClass(key);
		return (EngineInterface)engineObjects.get(uckey);
	}	
	
	/**
	 * @see DriverInterface#hasEnginePreferences()
	 */
	public boolean hasEnginePreferences() { return !(enginePreference.isEmpty());}
	
	/**
	 * @see DriverInterface#startEnginePreference(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public void startEnginePreference(String key) throws IllegalArgumentException{
		String uckey = null;
		try{
			// check if preference already exists.  move to first if so.
			uckey = key.toUpperCase();
			int index = getPreferredEngineIndex(key);
			if (index >= 0){
				Object eng = enginePreference.elementAt(index);
				enginePreference.removeElementAt(index);
				enginePreference.insertElementAt(eng, 0);			
				return;
			}
			// see if the key matches known engine classes
			String fulleng = getFullEngineClass(uckey);
			enginePreference.insertElementAt(fulleng, 0);
		}catch(NullPointerException npx){
			String text = "Driver.startEnginePreference(KEY)";
			String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
			throw new IllegalArgumentException(err);
	    }
	}

	/**
	 * @see DriverInterface#endEnginePreference(String)
	 * @throw IllegalArgumentException if key value is null, invalid, or unknown
	 */
	public void endEnginePreference(String key) throws IllegalArgumentException{
		int index = getPreferredEngineIndex(key);
		if (index < 0) {
			String text = "Driver.endEnginePreference(KEY)";
			String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
			throw new IllegalArgumentException(err);
		}
		enginePreference.removeElementAt(index);
	}

	/**
	 * @see DriverInterface#clearEnginePreferences()
	 */
	public void clearEnginePreferences(){ enginePreference.clear();}

	/**
	 * @see DriverInterface#isPreferredEngine(String)
	 * @throw IllegalArgumentException if key value is invalid or unknown
	 */
	public boolean isPreferredEngine(String key) throws IllegalArgumentException{
		if (enginePreference.isEmpty()) return false;
		try{
			String uckey = key.toUpperCase();
			// try exact match
			if (enginePreference.contains(uckey)) return true;
			// try partial match
			return(getPreferredEngineIndex(uckey) > -1);			
		}catch(NullPointerException npx){
			String text = "Driver.isPreferredEngine(KEY)";
			String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
			throw new IllegalArgumentException(err);
		}
	}
	
	/**
	 * @see DriverInterface#isPreferredEngine(EngineInterface)
	 * @throw IllegalArgumentException if engine is null
	 */
	public boolean isPreferredEngine(EngineInterface engine) throws IllegalArgumentException{
		if (enginePreference.isEmpty()) return false;
		try{
			ListIterator list = enginePreference.listIterator();
			while(list.hasNext()){
				EngineInterface peng = getPreferredEngine((String)list.next());
				if (peng == engine) return true;
			}
			return false;
		}catch(NullPointerException npx){
			String text = "Driver.isPreferredEngine(ENGINE)";
			String err = FAILStrings.convert("bad_param", "Invalid parameter value for "+text, text);
			throw new IllegalArgumentException(err);
		}
	}
	

	/** 
	 * Locate an EngineInterface given the engine priority string as defined in the 
	 * documented Configuration File standard for SAFS_ENGINES. (Ex: "First", "Second", etc.)
	 * <p>
	 * @return an EngineInterface or null if not found or instantiated.**/
	protected EngineInterface getEngineInterface(String itemName){
		
		EngineInterface engine = null;
		Class engineClass = null;
		String engineClassname = configInfo.getNamedValue(
    		                     DriverConstant.SECTION_SAFS_ENGINES, itemName);

		if ((engineClassname==null)||(engineClassname.length()==0)) return null;
		
		try{		
			engineClass = Class.forName(engineClassname);
			engine = (EngineInterface) engineClass.newInstance();
			engineObjects.put(engineClassname.toUpperCase(), engine);
		}
		catch(ClassNotFoundException cnfe){;}
		catch(InstantiationException ie){;}
		catch(IllegalAccessException iae){;}
		return engine;
	}


	/** 
	 * Locate a ConfigureLocatorInterface given the locatorInfo, presumably 
	 * provided from command-line options.
	 * <p>
	 * @exception IllegalArgumentException if appropriate locator class cannot be 
	 * instantiated.**/
	protected ConfigureLocatorInterface getConfigureLocator(String locatorInfo){
		
		ConfigureLocatorInterface locator = null;
		Class locatorClass = null;
		try{		
			locatorClass = Class.forName(locatorInfo);
			locator = (ConfigureLocatorInterface) locatorClass.newInstance();
			return locator;
		}
		catch(ClassNotFoundException cnfe){
			throw new IllegalArgumentException(
			"ClassNotFoundException:Invalid or Missing Configuration Locator class: "+ locatorInfo);
		}
		catch(InstantiationException ie){
			throw new IllegalArgumentException(
			"InstantiationException:Invalid or Missing Configuration Locator class: "+ locatorInfo);
		}
		catch(IllegalAccessException iae){
			throw new IllegalArgumentException(
			"IllegalAccessException:Invalid or Missing Configuration Locator class: "+ locatorInfo);
		}
	}


	/** Initialize or append a ConfigureInterface to existing ones in the search order.**/
	protected void addConfigureInterfaceSource(ConfigureInterface source){
		if (configInfo==null) { configInfo = source; }
		else { if (source!=null) configInfo.addConfigureInterface(source);}		
	}

	
	/** Initialize or insert a ConfigureInterface at the start of the search order.**/
	protected void insertConfigureInterfaceSource(ConfigureInterface source){
		if (configInfo==null) { configInfo = source; }
		else { if (source!=null) configInfo.insertConfigureInterface(source);}		
	}	
	
	/*****************************************************************************  
	 * Attempts to dynamically create a newInstance of a GenericToolsInterface object.
	 * The process first tries to locate the fully qualified name of the Class through 
	 * the ConfigureInterface using the provided configSection name and the value of 
	 * "Item" in that config section.  Example below shows a config file containing 
	 * a section called "ToolHelper":
	 * <p>
	 * <ul>
	 * [ToolHelper]<br>
	 * Item="My.Generic.Tools.Interface.Helper"<br>
	 * </ul>
	 * <p>
	 * If no such section exists in config, or no "Item" value is found, then the 
	 * routine will attempt to instantiate the defaultInterface provided.
	 *  
	 * @param String configSection -- Named section of configuration file(s) potentially  
	 * containing the Name=Value pair of Item=My.Full.Classname.
	 * 
	 * @param String defaultInterface -- fully qualified classname of class to 
	 * instantiate if no config file information is found.
	 * 
	 * @throws java.lang.ClassNotFoundException, java.lang.IllegalAccessException, java.lang.InstantiationException
	 * @see java.lang.Class#forName(String)
	 */
	protected GenericToolsInterface getGenericInterface (String configSection, String defaultInterface)
	                                                    throws ClassNotFoundException, IllegalAccessException,
	                                                           InstantiationException
	{
		String iName = configInfo.getNamedValue(configSection, "Item");
		if (iName==null) iName = defaultInterface;
		return ((GenericToolsInterface) (Class.forName(iName).newInstance()));
	}
	

	/** Initialize all the Driver/Engine interfaces as specified by the config files or 
	 * driver defaults.
	 * This driver passes itself (a DriverInterface) to all Configurable tools it 
	 * instances.
	 * <p>
	 * <ul>Calls:
	 * <li>input.launchInterface(this)
	 * <li>maps.launchInterface(this)
	 * <li>vars.launchInterface(this)
	 * <li>logs.launchInterface(this)
	 * <li>counts.launchInterface(this)
	 * </ul>
	 * @throws IllegalArgumentException if problems arise during initialization.
	 **/
	protected void initializeRuntimeInterface(){

		Log.info("Instantiating Driver Interface...");
		try{
			input = (InputInterface) getGenericInterface(DriverConstant.SECTION_SAFS_INPUT,
			                                             DriverConstant.DEFAULT_INPUT_INTERFACE);

			maps = (MapsInterface) getGenericInterface(DriverConstant.SECTION_SAFS_MAPS,
			                                             DriverConstant.DEFAULT_MAPS_INTERFACE);

			vars = (VarsInterface) getGenericInterface(DriverConstant.SECTION_SAFS_VARS,
			                                             DriverConstant.DEFAULT_VARS_INTERFACE);

			logs = (LogsInterface) getGenericInterface(DriverConstant.SECTION_SAFS_LOGS,
			                                             DriverConstant.DEFAULT_LOGS_INTERFACE);

			counts = (CountersInterface) getGenericInterface(DriverConstant.SECTION_SAFS_COUNTERS,
			                                             DriverConstant.DEFAULT_COUNTERS_INTERFACE);

			Log.info("Driver Interface initializing...");
			
			((ConfigurableToolsInterface)input).launchInterface(this);			
			((ConfigurableToolsInterface)maps).launchInterface(this);			
			((ConfigurableToolsInterface)vars).launchInterface(this);			
			((ConfigurableToolsInterface)logs).launchInterface(this);			
			((ConfigurableToolsInterface)counts).launchInterface(this);						
		}
		catch(ClassNotFoundException cnfe){
		    throw new IllegalArgumentException("\n"+
		    "Unable to locate one or more runtime interface specifications.\n"+
		    cnfe.getMessage() +"\n");}		

		catch(IllegalAccessException iae){
		    throw new IllegalArgumentException("\n"+
		    "Unable to use one or more runtime interface specifications.\n"+
		    iae.getMessage() +"\n");}		

		catch(InstantiationException ie){
		    throw new IllegalArgumentException("\n"+
		    "Unable to create one or more runtime interface specifications.\n"+
		    ie.getMessage() +"\n");}		

		catch(ClassCastException cce){
		    throw new IllegalArgumentException("\n"+
		    "Unable to create one or more runtime interface specifications.\n"+
		    cce.getMessage() +"\n");}		
	}
	
	/** 
	 * Instantiate and initialize any EngineInterface classes (up to 10)listed in the 
	 * SAFS_ENGINE section of the Configuration Source.
	 * <p>
	 * Also instantiates internal tools.engines.TIDDriverCommands in-process support.
	 * <p>
	 * This driver passes itself--a DriverInterface object--to each instanced engine.
	 * @see org.safs.tools.engines.TIDDriverCommands(DriverInterface)
	 ***/
	protected void initializeRuntimeEngines(){
		
		if (tidcommands == null) tidcommands = new TIDDriverCommands(this);
		
		String[] items = {"First", "Second", "Third", "Fourth", "Fifth", 
			              "Sixth", "Seventh", "Eighth", "Ninth", "Tenth"};
		for(int i=0;i<items.length;i++){
			EngineInterface engine = getEngineInterface(items[i]);
			if(! (engine==null)){
				engine.launchInterface(this);
				engines.addElement(engine);
			}
		}
	}

	/** 
	 * Initialize any preset variables such as known project directories, etc.
	 * <p>
	 * Known preset variables are:<br/>
	 * <ul>
	 *   <li>"SAFSPROJECTDIRECTORY" -- Root project directory: "C:\MyProject"
	 *   <li>"SAFSDATAPOOLDIRECTORY" -- Project Datapool directory: "C:\MyProject\Datapool"
	 *   <li>"SAFSBENCHDIRECTORY" -- Project Bench directory: "C:\MyProject\Datapool\Bench"
	 *   <li>"SAFSTESTDIRECTORY" -- Project Test\Actual directory: "C:\MyProject\Datapool\Test"
	 *   <li>"SAFSDIFDIRECTORY" -- Project Dif directory: "C:\MyProject\Datapool\Dif"
	 *   <li>"SAFSLOGSDIRECTORY" -- Project Logs directory: "C:\MyProject\Datapool\Logs"
	 *   <li>"SAFSSYSTEMUSERID" -- UserID of the person\account logged onto the system.
	 * </ul>
	 ***/
	protected void initializePresetVariables(){
		if (vars == null) return;
		try{
			vars.setValue("safsprojectdirectory", projectRootDir + File.separatorChar);
			vars.setValue("safsdatapooldirectory", datapoolSource + File.separatorChar);
			vars.setValue("safsbenchdirectory", benchSource + File.separatorChar);
			vars.setValue("safstestdirectory", testSource + File.separatorChar);
			vars.setValue("safsdifdirectory", difSource + File.separatorChar);
			vars.setValue("safslogsdirectory", logsSource + File.separatorChar);	
			vars.setValue("safssystemuserid", System.getProperty("user.name"));
		}
		catch(Exception x){;}
	}

	/** 
	 * shutdown any engines started with initializeRuntimeEngines() 
	 */
	protected void shutdownRuntimeEngines(){
		
		Enumeration enumerator = engines.elements();
		while(enumerator.hasMoreElements()) {
			EngineInterface engine = (EngineInterface) enumerator.nextElement();
			engine.shutdown();
		}
		engines.removeAllElements();
		enginePreference.clear();
		engineObjects.clear();

		if(tidcommands !=null) tidcommands.shutdown();
	}

	/** 
	 * shutdown GenericToolsInterfaces started with initializeRuntimeInterfaces() 
	 * <p>
	 * <ul>Calls:
	 * <li>input.shutdown()
	 * <li>vars.shutdown()
	 * <li>maps.shutdown()
	 * <li>counts.shutdown()
	 * <li>logs.shutdown()
	 * </ul>
	 */	
	protected void shutdownRuntimeInterface(){

		try{((GenericToolsInterface)input).shutdown();}catch(Exception x){}
		try{((GenericToolsInterface)vars).shutdown();}catch(Exception x){}
		try{((GenericToolsInterface)maps).shutdown();}catch(Exception x){}
		try{((GenericToolsInterface)counts).shutdown();}catch(Exception x){}
		try{((GenericToolsInterface)logs).shutdown();}catch(Exception x){}

		input      = null;
		maps       = null;
		vars       = null;
		logs       = null;
		counts     = null;
	}
	
	/**********************************************************************************
	 * We do nothing here but return a newly initialized (empty) StatusInfo object.
	 * Required to extend AbstractDriver.
	 * @see AbstractDriver#processTest()
	 * @see org.safs.tools.status.StatusInfo()
	 */
	protected StatusInterface processTest(){
		
		return new StatusInfo();
	}

	
	/** 
	 * Bootstrap a newly instanced driver.
	 * The routine calls all the other routines to prepare the driver and configure tools.
	 * <p>  
	 * This routine must be overridden by subclasses if 
	 * they wish to change default start-to-finish execution flow.
	 * <p>
	 * The model for overall driver operation is that any command-line arguments or 
	 * configuration file arguments that prevent normal execution will generate an 
	 * IllegalArgumentException.  Those IllegalArgumentExceptions are caught here and 
	 * sent to stderr output and then rethrown.  Any Exception will invoke shutdownDriver().  
	 * We immediately return from this function.
	 * <p>
	 * <ul>Calls:
	 * <li>{@link #validateRootConfigureParameters()}
	 * <li>{@link #initializeRuntimeInterface()}
	 * <li>{@link #initializeRuntimeEngines()}
	 * </ul>
	 * @throws java.lang.IllegalArgumentException
	 * @see #shutdownDriver()
	 **/
	public void initializeDriver(){
		try{
		    validateRootConfigureParameters(true);	
		    initializeRuntimeInterface();
		    initializePresetVariables();
		    initializeRuntimeEngines();
		}
		catch(IllegalArgumentException iae){ 
			System.err.println( iae.getMessage());
			shutdownDriver();
			throw iae;
		}
		    
		catch(Exception catchall){
			System.err.println("\n****  Unexpected CatchAll Exception handler  ****");
			System.err.println(catchall.getMessage());
			catchall.printStackTrace();
			shutdownDriver();
			throw new IllegalArgumentException("Driver initialization error: "+ catchall.getMessage());
		}		    
	}

	/**
	 * <ul>Calls:
	 * <li>{@link #shutdownRuntimeEngines()}
	 * <li>{@link #shutdownRuntimeInterface()}
	 * </ul>
	 */
	public void shutdownDriver(){
		try{
		    shutdownRuntimeEngines();			// maybe, maybe not.
		    shutdownRuntimeInterface(); 		// maybe, maybe not.
		}    
		catch(Exception any){ 
			System.err.println("\n****  Unexpected Shutdown Exception handler  ****");
			System.err.println( any.getMessage());
			any.printStackTrace();
		}		    
	}
}

