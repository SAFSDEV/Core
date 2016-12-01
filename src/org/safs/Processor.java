/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 *
 * JUN 03, 2003    (DBauman) Original Release
 * SEP 16, 2003    (CANAGL) Implemented use of new SAFSLOGS logging.
 * NOV 10, 2003    (CANAGL) Added constants for known record types.
 *                          Added new abstract isSupportedRecordType()
 * NOV 13, 2003    (CANAGL) Added support for stored processors or
 *                          full or partial class names for processors.
 *                          The latter is moving the functionality out of
 *                          the TestRecordData class into the individual
 *                          Processors for more flexible differentiation of
 *                          processors.
 * NOV 13, 2003    (DBauman) Added support for breakpoints.
 * NOV 19, 2003    (CANAGL) Additional refactoring.
 * AUG 08, 2006    (PHSABO) Added helper functions getSubAreaRectangle and getClippedSubAreaRectangle used by GetGUIImage and FilterImage.
 * JAN 29, 2007    (CANAGL) Added chainedProcessor support for all Processors.
 * JAN 25, 2008    (CANAGL) Removed Java 1.5 String.contains dependencies.
 * JUL 31, 2008    (CANAGL) Added CASEINSENSITIVE static strings for subclasses.
 * AUG 25, 2008    (CANAGL) Added try/catch in distributeConfigInfo routine.
 * MAR 26, 2009    (CANAGL) Added safsparams support for clearProxiesAlways.
 * MAR 08, 2011    (DharmeshPatel) Added RFSMOnly support for RFSM search mode.
 * JUL 22, 2013    (SBJLWA) Added methods deducexxxFile().
 * DEC 01, 2016    (SBJLWA) Added methods setAtEndOfProcess() etc.
 **/
package org.safs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Vector;

import org.safs.jvmagent.AgentClassLoader;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.DefaultTestRecordStackable;
import org.safs.tools.ITestRecordStackable;
import org.safs.tools.RuntimeDataInterface;
import org.safs.tools.drivers.DriverConstant;

/**
 * Abstract Processor, enforces that the 'process' and 'isSupportedRecordType'
 * methods be implemented.
 * <p>
 * @author  Doug Bauman
 * @since   JUN 03, 2003
 **/
public abstract class Processor implements RuntimeDataInterface, ITestRecordStackable{

  /** "org.safs"
   * Default package for Processor subclasses used to execute test data.
   **/
  public static final String DEFAULT_PROCESSOR_PACKAGE           = "org.safs";
  public static final String DEFAULT_CUSTOM_PROCESSOR_PACKAGE    = "org.safs.custom";
  public static final String DEFAULT_CUSTOM_PROCESSOR_SUBPACKAGE = "custom";
  public static final String CASEINSENSITIVE_FLAG = "CASEINSENSITIVE";
  public static final String CASE_INSENSITIVE_FLAG = "CASE-INSENSITIVE";
  
  /** The ITestRecordStackable used to store 'Test Record' in a FILO. */
  protected ITestRecordStackable testrecordStackable = new DefaultTestRecordStackable();

  /**
   * Instance a given Processor from the provided classname.
   * @param classname should not be null or 0 length
   * @return Processor if the instanced object can be cast to org.safs.Processor;
   *         returns null if the object cannot be instanced or cast.
   */
  protected Processor getClassInstance(String classname){
  	  Processor proc = null;
  	  String method ="PROC.GCI:";
      try {
          Log.debug(method+"trying processor:"+classname);
       	  Class aclass = Class.forName(classname);
          Log.debug(method+"processorClass: "+aclass.getName());
          proc = (Processor) aclass.newInstance();
      }catch (Exception ex) { Log.info(method+classname +":"+ ex.getClass().getName());}
  	  return proc;
  }

  /** Validate that a given classname is valid and assignable to org.safs.Processor
   */
  protected boolean validProcessorClassName(String classname){
  	  boolean status = true;
  	  String method ="PROC.VPCN:";
  	  try{
        Log.debug(method+"trying processor:"+classname);
  	  	Class aclass = Class.forName(classname);
        Log.debug(method+"processorClass: "+aclass.getName());
  	  	if(! org.safs.Processor.class.isAssignableFrom(aclass)) status = false;
  	  }catch(Exception ex) {
  	  	status = false;
        Log.info(method+classname +":"+ ex.getClass().getName());
  	  }
  	  return status;
  }

  /**
   * Attempts to locate a class for the current procInstancePath.
   * For this superclass implementation, no additional information is appended
   * to the procInstancePath data to attempt to locate a matching Processor class.
   * The user can store a complete alternate classname with setProcInstancePath.
   * <p>
   * Subclasses should override this method to return additional classnames
   * that are based off a root package name stored in procInstancePath.
   * <p>
   * Use validProcessorClassName before adding a classname to the list.
   * <p>
   * @return a list of potential processor classnames to try.
   **/
  protected ArrayList getProcClassNames(){
  	  ArrayList classlist = new ArrayList(10);
  	  String classname = getProcInstancePath();
  	  if (validProcessorClassName(classname)) classlist.add(classname);
  	  return classlist;
  }

  /**
   * Attempts to locate a class for the current customProcInstancePath.
   * For this superclass implementation, no additional information is appended
   * to the customProcInstancePath data to attempt to locate a matching Processor class.
   * The user can store a complete alternate classname with setCustomProcInstancePath.
   * <p>
   * Subclasses, however, can also override this method to return specific classnames
   * that are based off a root package name stored in customProcInstancePath.
   * <p>
   * Use validProcessorClassName before adding a classname to the list.
   * <p>
   * @return valid String name for processor class or null if the stored path is
   *         not a valid class name.  This usually means the stored path is to the
   *         package only.
   **/
  protected  ArrayList getCustomProcClassNames(){
  	  ArrayList classlist = new ArrayList(10);
  	  String classname = getCustomProcInstancePath();
  	  if (validProcessorClassName(classname)) classlist.add(classname);
  	  return classlist;
  }



  /** The instance of the processor to interpret test data.
   * For example, we may store an org.safs.DriverCommandProcessor object to
   * provide default handling of Driver Command records.
   **/
  private Processor procInstance     = null;
  
  /**
   * Return/Create an instance of the actual processor used to interpret test data.
   * If we already have stored a procInstance object; that that will be returned.
   * If all we have is a procInstancePath, then the routine will attempt to
   * instance an object from that Path information.  At this level, if no Processor
   * can be instanced, then the subclass will need to override the getProcInstance
   * method to provide the full functionality of instancing an appropriate test
   * record processor.  This will likely be different for each processor type.
   * <p>
   * @returns Processor object needed to process the test record, or null.
   **/
  public    Processor getProcInstance() {
      if(procInstance != null) return procInstance;
      String classname = getProcInstancePath();
      procInstance = getClassInstance(classname);
      return procInstance;
  }

  /** Provide a Processor object to interpret test data.
   **/
  public    void      setProcInstance(Processor aprocessor){
      procInstance = aprocessor;
  }

  /** 
   * The instance of the chained processor to interpret test data if this 
   * one does not succeed in processing the data.
   * <p>
   * For example, we may store an org.safs.DriverCommandProcessor object to
   * provide default handling of Driver Command records not overridden by 
   * some DriverCommand subclass.
   * @since Jan 29, 2007 CANAGL
   **/
  private Processor chainedProcessor   = null;

  /** 
   * True if this processor has an instance of a chained processor to 
   * interpret test data if this one does not succeed in processing the data.
   * @since Jan 29, 2007 CANAGL
   **/
  public boolean hasChainedProcessor(){return chainedProcessor != null;}
  
  /** 
   * Returns chained processor to interpret test data, if any.
   * Can be null if no chainedProcessor has been set.
   * @since Jan 29, 2007 CANAGL
   **/
  public Processor getChainedProcessor(){
  	return chainedProcessor;
  }
  
  /** 
   * Set the instance of the chained processor to interpret test data if this 
   * one does not succeed in processing the data.
   * <p>
   * For example, we may store an org.safs.DriverCommandProcessor object to
   * provide default handling of Driver Command records not overridden by 
   * some subclass.
   * <p>
   * Can be set to null to remove any existing chainedProcessor.
   * @since Jan 29, 2007 CANAGL
   **/
  public void setChainedProcessor(Processor aprocessor){
  	chainedProcessor = aprocessor;
  	try{
  		if (aprocessor.getLogUtilities()==null) aprocessor.setLogUtilities(this.getLogUtilities());
		if (aprocessor.getTestRecordData()==null) aprocessor.setTestRecordData(this.getTestRecordData());
  	}catch(Exception np){;}  	
  }

  /**
   * The package name or complete classname of the processor to interpret test data.
   * By default, this holds the default "org.safs" package name that may be used
   * by some subclasses.
   * <p>
   * If a package name is provided, then the subclass must implement a means to create
   * the full class name if the processor object is to be instantiated by this string
   * information.  This is not necessary if a viable instance of a Processor is
   * already stored and available via getProcInstance.
   **/
  private String    procInstancePath = DEFAULT_PROCESSOR_PACKAGE;

  /** Return the current procInstancePath setting.
   * It is possible for the procInstancePath setting to be overridden by a concrete
   * subclass of TestRecordHelper.getCompInstancePath.  Thus, the TestRecordHelper
   * subclass must not provide a non-null value from that method if the user intends
   * to specify alternative paths here.
   **/
  public   String     getProcInstancePath () {
  	String compInstance = testRecordData.getCompInstancePath();
  	if (compInstance != null) return compInstance;
  	return procInstancePath;
  }

  /** Change the procInstancePath setting.
   * This may be called by some subclasses to provide a full class name or
   * a different package name for instancing a processor to interpret test data.
   **/
  public    void      setProcInstancePath (String pkgname){
      procInstancePath = pkgname;
  }


  /**
   * The package name or complete classname of the custom processor to interpret test data.
   * By default, this holds the default "org.safs.custom" package name that may be used
   * by some subclasses.
   * <p>
   * If a package name is provided, then the subclass must implement a means to create
   * the full class name if the processor object is to be instantiated by this string
   * information.  This is not necessary if a viable instance of a Processor is
   * already stored and available via getProcInstance.
   * <p>
   **/
  private String    customProcInstancePath = DEFAULT_CUSTOM_PROCESSOR_PACKAGE;

  /** Return the current customProcInstancePath setting.
   * It is possible for the this setting to be overridden by a concrete
   * subclass of TestRecordHelper.getCompInstancePath.  Thus, the TestRecordHelper
   * subclass must not provide a non-null value from that method if the user intends
   * to specify alternative paths here.
   **/
  public   String     getCustomProcInstancePath () {
  	String compInstance = testRecordData.getCompInstancePath();
  	if (compInstance != null) return compInstance + DEFAULT_CUSTOM_PROCESSOR_SUBPACKAGE;
  	return customProcInstancePath;
  }

  /** Change the procInstancePath setting.
   * This may be called by some subclasses to provide a full class name or
   * a different package name for instancing a processor to interpret test data.
   **/
  public    void      setCustomProcInstancePath (String pkgname){
      customProcInstancePath = pkgname;
  }


  /**
   * All processor instances may enable/disable and force breakpoints where they
   * deem this appropriate.  The static setting is shared by all processors.
   * By default, breakpoints at all the coded locations are true.
   **/
  protected static boolean breakpointsOn = false;

  /** This may be set by any means.  In a runtime debugging environment this
   * will likely be set by a Driver Command.
   * By default, breakpoints are enabled.
   **/
  public static void setBreakpointsOn(boolean enabled){ breakpointsOn = enabled;}

  /** test if breakpoints is enabled.
   **/
  public static boolean isBreakpointsOn() {return breakpointsOn;}

  /** test for enabled breakpoints and active a breakpoint if enabled. **/
  protected static void checkBreakpoints(String breakpoint_message){
  	if (isBreakpointsOn()) { activateBreakpoint(breakpoint_message);}
  }

  /** Activate the SAFSBreakpointException breakpoint.
   **/
  protected static void activateBreakpoint(String breakpoint_message){
    System.out.println("Activate Breakpoint: "+breakpoint_message);
    java.util.Date t1 = new java.util.Date();
    long t1t = t1.getTime();

    // first try this, if the duration is long enough,
    // then the GUI handled it
    try{ throw new SAFSBreakpointException(breakpoint_message);}
    catch(SAFSBreakpointException bp){}

    java.util.Date t2 = new java.util.Date();
    long t2t = t2.getTime();

    // otherwise, if this is enabled, then let the command line handle it
    if (commandLineBreakpoint && (t2t < t1t + 40)) {
      commandLineBreakpoint();
    }
  }

  private static BufferedReader systemInReader = null;
  private static void commandLineBreakpoint() {
    if (systemInReader == null) {
      systemInReader = new BufferedReader(new InputStreamReader(System.in));
    }
    System.out.println(" Hit <Enter> to continue...");
    try {
      if (systemInReader != null) {
        systemInReader.readLine();
      }
    } catch (IOException io) {} // ignore
  }


  /**
   * Individual processor instances may enable/disable and force breakpoints where they
   * deem this appropriate.  The setting is specific to individual processors.
   * By default, breakpoints on individual processors is disabled.  However, a check is
   * done for the static Processor.isBreakpoints enabled which overrides this setting.
   * Processor breakpoints can be enabled separately when all other global breakpoints are
   * disabled.
   **/
  protected boolean myBreakpointsOn = false;

  /** This may be set by any means.  In a runtime debugging environment this
   * will likely be set by a Driver Command or during construction.
   * By default, these breakpoints are disabled.
   **/
  public void setMyBreakpointsOn(boolean enabled){ myBreakpointsOn = enabled;}

  /** test if breakpoints is enabled.
   * @return true if myBreakpoints on is true.
   **/
  public boolean isMyBreakpointsOn() {return (myBreakpointsOn);}

  /** test for enabled breakpoints and active a breakpoint if enabled.
   * Subclasses should override this method if they wish to add additional checks
   * for enabled breakpoints.
   **/
  protected void checkMyBreakpoints(String breakpoint_message){
  	if (isMyBreakpointsOn()) { activateBreakpoint(breakpoint_message);}
  }



  /** GetText.text() keyword. **/
  protected static final String TXT_SUCCESS_1 = "success1";
  /** GetText.text() keyword. **/
  protected static final String TXT_SUCCESS_2 = "success2";
  /** GetText.text() keyword. **/
  protected static final String TXT_SUCCESS_2a = "success2a";
  /** GetText.text() keyword. **/
  protected static final String TXT_SUCCESS_3 = "success3";
  /** GetText.text() keyword. **/
  protected static final String TXT_SUCCESS_3a = "success3a";
  /** GetText.text() keyword. **/
  protected static final String TXT_SUCCESS_4 = "success4";
  /** GetText.text() keyword. **/
  protected static final String PRE_TXT_SUCCESS_4 = "presuccess4";
  /** GetText.text() keyword. **/
  protected static final String TXT_SUCCESS_5 = "success5";
  /** GetText.text() keyword. **/
  protected static final String PRE_TXT_SUCCESS_5 = "presuccess5";
  /** GetText.text() keyword. **/
  protected static final String SENT_MSG_3 = "sentmsg3";


  /** GetText.text() keyword. **/
  protected static final String TXT_FAILURE_1    = "failure1";

  /** GetText.text() keyword. **/
  protected static final String TXT_FAILURE_2    = "failure2";

  /** GetText.text() keyword. **/
  protected static final String TXT_FAILURE_3    = "failure3";

  /** GetText.text() keyword. **/
  protected static final String TXT_FAILURE_4    = "failure4";

  /** GetText.text() keyword. **/
  protected static final String LINE_FAILURE_4   = "linefail4";

  /** GetText.text() keyword. **/
  protected static final String TXT_FAILURE_5    = "failure5";

  /** GetText.text() keyword. **/
  protected static final String FAILURE_DETAIL  = "failureDetail";

  /** GetText.text() keyword. **/
  protected static final String PARAM_SIZE_1    = "paramsize1";

  /** GetText.text() keyword. **/
  protected static final String PARAM_SIZE_2    = "paramsize2";

  /** GetText.text() keyword. **/
  protected static final String PARAM_SIZE_3    = "paramsize3";

  /** GetText.text() keyword. **/
  protected static final String PARAM_SIZE_4    = "paramsize4";


  /** "C" Record Type Constant **/
  public final static String RECTYPE_DRIVER_COMMAND   = "C";
  /** "CW" Record Type Constant **/
  public final static String RECTYPE_DRIVER_COMMAND_W = "CW";
  /** "CF" Record Type Constant **/
  public final static String RECTYPE_DRIVER_COMMAND_F = "CF";
  /** "T" Record Type Constant **/
  public final static String RECTYPE_TEST_STEP        = "T";
  /** "TW" Record Type Constant **/
  public final static String RECTYPE_TEST_STEP_W      = "TW";
  /** "TF" Record Type Constant **/
  public final static String RECTYPE_TEST_STEP_F      = "TF";


  /** Convenience for local referencing instead of referencing AbstractLogFacility. **/
  protected static final int DEBUG_MESSAGE      = AbstractLogFacility.DEBUG_MESSAGE;

  /** Convenience for local referencing instead of referencing AbstractLogFacility. **/
  protected static final int GENERIC_MESSAGE    = AbstractLogFacility.GENERIC_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int FAILED_MESSAGE     = AbstractLogFacility.FAILED_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int FAILED_OK_MESSAGE  = AbstractLogFacility.FAILED_OK_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int PASSED_MESSAGE     = AbstractLogFacility.PASSED_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int WARNING_MESSAGE    = AbstractLogFacility.WARNING_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int WARNING_OK_MESSAGE = AbstractLogFacility.WARNING_OK_MESSAGE;


  /** <br><em>Purpose:</em>      holds the testRecordData info
   ** <br><em>Initialized:</em>  constructor
   **/
  protected TestRecordHelper testRecordData;

  public TestRecordHelper getTestRecordData() {return testRecordData;}
  public void             setTestRecordData(TestRecordHelper testRecordData) {
    this.testRecordData = testRecordData;
  }

  protected LogUtilities     log;

  public LogUtilities getLogUtilities() { return this.log;}
  public void setLogUtilities(LogUtilities log) {
    this.log = log;
  }


  public static final String SAFS_RESBUN_NAME = "SAFSTextResourceBundle";
  protected static final String genericStr = "";//"generic";
  protected static final String passedStr  = "";//"passed";
  protected static final String failedStr  = "failed";
  protected static final String warningStr = "failed";//"warning";
  protected static final String otherStr   = "";//"other";
  protected static final String customStr  = "";//"custom";
  protected static final String debugStr   = "";//"debug";
  protected static GetText genericText = new GetText(genericStr+SAFS_RESBUN_NAME, Locale.getDefault());
  protected static GetText passedText  = new GetText(passedStr+SAFS_RESBUN_NAME, Locale.getDefault());
  protected static GetText failedText  = new GetText(failedStr+SAFS_RESBUN_NAME, Locale.getDefault());
  protected static GetText warningText = new GetText(warningStr+SAFS_RESBUN_NAME, Locale.getDefault());
  protected static GetText otherText   = new GetText(otherStr+SAFS_RESBUN_NAME, Locale.getDefault());
  protected static GetText customText  = new GetText(customStr+SAFS_RESBUN_NAME, Locale.getDefault());
  protected static GetText debugText   = new GetText(debugStr+SAFS_RESBUN_NAME, Locale.getDefault());


  /** <br><em>Purpose:</em>      the collection of params
   ** <br><em>Initialized:</em>  constructor
   **/
  protected Collection params;
  public Collection getParams () { return params; }
  public void       setParams (Collection params) {this.params = params;}
  public Collection getAlternateParams () { return params; }


  /** keeps instantiated processors for reuse
   **/
  protected Map processorMap = new HashMap();


  /** Status for whether the processor processed the record, if the processor
   * fails to support the record it has been given, then the processor should
   * set this to false, so that any processor chain can continue to the next
   * processor, if one is available.
   **/
  protected boolean recordProcessed;
  public boolean isRecordProcessed () {return recordProcessed;}
  public void setRecordProcessed (boolean processed) {
    recordProcessed = processed;
  }


  /** Tries to instance a processor and then process a record.
   * Given a processor class name, the routine attempts to first locate a processor
   * that already may exist in the processorMap.  If none is found, then the
   * routine attempts to make an instance of the class.  If successful, the instance
   * is stored in the processorMap and forwarded to the initProcessorAndProcess
   * .method for record processing.
   * <p>
   * @param                     aprocessor, Processor
   * @param                     params, Collection
   * @return true if it processed the record, false otherwise
   **/
  protected boolean instanceProcessorAndProcess(String instanceName, Collection params){
  	if (instanceName == null) return false;
  	String method = "PROC.IPAP:";
    Processor dc = (Processor) processorMap.get(instanceName);
    if (dc == null) {
      try { // next try using Class.forName...
        Log.debug(method+"trying processor:"+instanceName);
        Class dcClass = Class.forName(instanceName);
        Log.debug(method+"processorClass: "+dcClass.getName());
        dc = (Processor) dcClass.newInstance();
        processorMap.put(instanceName, dc);
      } catch (NoClassDefFoundError nc) {
        Log.info(method+"no class definition found: "+instanceName);
      } catch (ClassCastException cc) {
        Log.info(method+"can't Cast class: "+instanceName);
      } catch (InstantiationException ie) {
        Log.info(method+"can't instantiate class: "+instanceName);
      } catch (ClassNotFoundException ex) {
        Log.info(method+"can't find class: "+instanceName);
      } catch (IllegalAccessException iae) {
        Log.info(method+iae.getMessage());
      }
    }
    return initProcessorAndProcess(dc, params);
  }


  public void distributeConfigInformation(){
	  try{
		  //TODO: This is a problem. This makes an assumption that ALL running engines will have the same settings!
		  //TODO: Engines should not rely on these variable settings since any Processor in any Engine can change them!
		  Log.info("Saving config settings for Processor: "+ this.getClass().getSimpleName());
		  try{ setVariable(SAFS_SECSWAITFORWINDOW_VARIABLE, Integer.toString(secsWaitForWindow).trim());}
		  catch(Exception x){Log.info("Ignoring SECSWAITFORWINDOW setVariable Exception: "+ getClass().getSimpleName());}
		  try{ System.setProperty(SAFS_SECSWAITFORWINDOW_VARIABLE, Integer.toString(secsWaitForWindow).trim());}
		  catch(Exception x){Log.info("Ignoring SECSWAITFORWINDOW System.setProperty Exception: "+ getClass().getSimpleName());}
		  try{ setVariable(SAFS_SECSWAITFORCOMPONENT_VARIABLE, Integer.toString(secsWaitForComponent).trim());}
		  catch(Exception x){Log.info("Ignoring SECSWAITFORCOMPONENT setVariable Exception: "+ getClass().getSimpleName());}
		  try{ System.setProperty(SAFS_SECSWAITFORCOMPONENT_VARIABLE, Integer.toString(secsWaitForComponent).trim());}
		  catch(Exception x){Log.info("Ignoring SECSWAITFORCOMPONENT System.setProperty Exception: "+ getClass().getSimpleName());}
		  try{ setVariable(SAFS_COMMANDLINEBREAKPOINT_VARIABLE, Boolean.toString(commandLineBreakpoint).trim());}
		  catch(Exception x){Log.info("Ignoring COMMANDLINEBREAKPOINT setVariable Exception: "+ getClass().getSimpleName());}
		  try{ System.setProperty(SAFS_COMMANDLINEBREAKPOINT_VARIABLE, Boolean.toString(commandLineBreakpoint).trim());}
		  catch(Exception x){Log.info("Ignoring COMMANDLINEBREAKPOINT System.setProperty Exception: "+ getClass().getSimpleName());}
    	  processorConfigSet = true;
    	  Log.info("Processor config settings saved.");
	  }
	  catch(Exception x){
		  Log.error("Ignoring exception when saving Processor Config Settings:"+ x.getClass().getSimpleName());
	  }
  }
  
  /** Init the processor and process a record
   * Initializes the processor by passing it the currently active LogUtilities,
   * TestRecordHelper, and extracted record parameters.  It then invokes the
   * .process method of the Processor.  This routine is normally called by other
   * routines that first instance or retrieve the processor to init (like
   * instanceProcessorAndProcess).
   * <p>
   * @param                     aprocessor, Processor
   * @param                     params, Collection
   * @return true if it processed the record, false otherwise
   **/
  protected boolean initProcessorAndProcess (Processor aprocessor, Collection params) {
    if (aprocessor != null) {
      Log.info("PROC.IPAP2:Trying processor : "+aprocessor);
      aprocessor.setRecordProcessed(true); // assume true, prove otherwise
      aprocessor.setLogUtilities(log);
      aprocessor.setTestRecordData(testRecordData);
      if(!processorConfigSet) distributeConfigInformation();
      aprocessor.setParams(params);
      aprocessor.process();
      boolean success = aprocessor.isRecordProcessed();      
      // handle the case where aprocessor does not properly set recordProcessed status
      return (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED) ?
             false:success;
    }
    return false;
  }


  /** <br><em>Purpose:</em> constructor
   **/
  public Processor () {
    Log.debug("new Processor: "+this);
  }

  protected boolean delay (int millisec) {
    try {
      Thread.sleep(millisec);
      return true;
    } catch (InterruptedException e) {
      STAFHelper helper = getTestRecordData().getSTAFHelper();
      return helper.delay(millisec);
    }
  }

  public boolean setVariable (String var, String val) throws SAFSException {
  	STAFHelper helper = getTestRecordData().getSTAFHelper();
  	return helper.setVariable(var, val);
  }

  public String getVariable (String var) throws SAFSException {
  	STAFHelper helper = getTestRecordData().getSTAFHelper();
  	return helper.getVariable(var);
  }

  public String getAppMapItem(String appMapID, String section, String item) {
  	STAFHelper helper = getTestRecordData().getSTAFHelper();
    String lookup = helper.getAppMapItem(appMapID, section, item);
    lookup = StringUtils.getTrimmedUnquotedStr(lookup);
    return lookup;
  }

  /** <br><em>Purpose:</em> if leading ^ found, then substitute variable.
   ** It will log a warning message, set statusCode to StatusCodes.GENERAL_SCRIPT_FAILURE
   ** and return null if variable not found.<br>
   ** *NOTE* According to the chief architect for the SAFS framework, an engine should
   ** not use this method in a driver command or a component function; therefore be
   ** careful about it's use.  Maybe this method should be moved to StringUtils or some
   ** other place so that when and if a java 'controller' is developed, it can be
   ** utilized.
   * @param                     str, String string to search for ^ at beginning
   * @return                    str if no ^, or variable value, or null if not found
   **/
  public String substituteVariable (String str) {
    if (str.length() > 0 && str.charAt(0)=='^') { // then it is a variable
      Object rval = null;
      try {
        rval = getVariable(str.substring(1, str.length()));
      } catch (SAFSException se) {} // ignore, rval will be null
      if (rval == null) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(),
                       testRecordData.getCommand()+
                       " failure, read variable is null for : "+str,
                       WARNING_MESSAGE);
        return null;
      }
      return rval.toString();
    }
    return str;
  }

  /** used by processSubclassProcessor and processCustomProcessor **/
  private Vector tempmap = null;

  protected boolean processSubclassProcessor(Collection params){
    // now try to instantiate using other mechanisms (based on a subpackage)
    String instanceName = null;
    boolean success = false;
    if (tempmap == null) tempmap = new Vector(10);
    tempmap.clear();
    ListIterator list = getProcClassNames().listIterator();
    while ((list.hasNext())&&(! success)){
        instanceName = (String)list.next();
    	if ((instanceName != null)&&(tempmap.indexOf(instanceName)==-1)){
    		tempmap.addElement(instanceName);
            success = instanceProcessorAndProcess(instanceName, params);
    	}
    }
    if (success) tempmap.clear();
    return success;
  }

  protected boolean processCustomProcessor(Collection params){
    // now try to instantiate using other mechanisms (based on a custom subpackage)
    String instanceName = null;
    boolean success = false;
    if (tempmap == null) tempmap = new Vector(10);
    tempmap.clear();
    ListIterator list = getCustomProcClassNames().listIterator();
    while ((list.hasNext())&&(! success)){
        instanceName = (String)list.next();
    	if ((instanceName != null)&&(tempmap.indexOf(instanceName)==-1)){
    		tempmap.addElement(instanceName);
            success = instanceProcessorAndProcess(instanceName, params);
    	}
    }
    if (success) tempmap.clear();
    return success;
  }

  /**
   * The primary method that all subclasses must implement.
   * The implementation here simply checks for any chainedProcessor,<br>
   * invokes chainedProcessor.setParams(this.getParams()),<br>
   * invokes chainedProcessor.process(),<br>
   * invokes setRecordProcessed(chainedProcessor.isRecordProcessed()).
   * <p>
   * Thus, any subclassing Processor should attempt to process it's 
   * own commands and then invoke super.process() to try any chained 
   * Processor(s). 
   * <p>
   * Otherwise, by default executes setRecordProcessed(false) if no 
   * chainedProcessor is present.
   * @since Jan27, 2007 CANAGL
   */
  public void process(){
  	if (hasChainedProcessor()){
			org.safs.Processor aprocessor = getChainedProcessor();
			aprocessor.setParams(getParams());
			aprocessor.process();
			setRecordProcessed(aprocessor.isRecordProcessed());
	}else{
		setRecordProcessed(false);
	}
  }

  /**
   * Determine support for a particular record type.
   * @return 'true' if the provided recordType is one this Processor can execute.
   * Note, that SAFS record types are normally not case-sensitive.  So the
   * comparison to match the provided recordType should ignore case.
   * <p>
   * @param recordType -- String text of record type to match (ignoring case).
   **/
  public abstract boolean isSupportedRecordType(String recordType);

  /** Convenience routine for isSupportedRecordType to return true if
   * the Processor supports standard Driver Command records.
   **/
  public static boolean isDriverCommandRecord(String recordType){
  	if (recordType == null) return false;
  	String rt = recordType.toUpperCase();
  	if ((rt.equals(RECTYPE_DRIVER_COMMAND))  ||
  	    (rt.equals(RECTYPE_DRIVER_COMMAND_W))||
  	    (rt.equals(RECTYPE_DRIVER_COMMAND_F)))  return true;
  	return false;
  }

  /** Convenience routine for isSupportedRecordType to return true if
   * the Processor supports standard Component Function records.
   **/
  public static boolean isComponentFunctionRecord(String recordType){
  	if (recordType == null) return false;
  	String rt = recordType.toUpperCase();
  	if ((rt.equals(RECTYPE_TEST_STEP))  ||
  	    (rt.equals(RECTYPE_TEST_STEP_W))||
  	    (rt.equals(RECTYPE_TEST_STEP_F)))  return true;
  	return false;
  }

  /** Convenience routine for isSupportedRecordType to return true if
   * the Processor supports standard Engine Command records.
   **/
  public boolean isEngineCommandRecord(String recordType){
  	if (recordType == null) return false;
  	return DriverConstant.RECTYPE_E.equalsIgnoreCase(recordType);
  }



  /** <br><em>Purpose:</em> log a FAILED_MESSAGE about the wrong number of parameters;
   ** this version is used by DriverCommands
   **/
  protected void paramsFailedMsg() throws SAFSException {
    paramsFailedMsg(null, null);
  }

  /** <br><em>Purpose:</em> log a FAILED_MESSAGE about the wrong number of parameters
   * @param                     windowName, String
   * @param                     compName, String
   **/
  protected void paramsFailedMsg(String windowName, String compName) throws SAFSException {
    String tag = PARAM_SIZE_4;
    List list = new LinkedList();
    list.add(testRecordData.getCommand());
    list.add(Integer.toString(params.size()));
    if (windowName !=null && compName != null) {
      list.add(windowName);
      list.add(compName);
    } else {
      list.add(getClass().getName());
      tag = PARAM_SIZE_3;
    }
    String message =
      failedText.convert(tag,
                         testRecordData.getCommand()+": wrong num params:"+params.size(),
                         list);
    String detail =
      failedText.convert(FAILURE_DETAIL,
                         "Line: "+testRecordData.getLineNumber()+": "+testRecordData.getInputRecord(),
                         Long.toString(testRecordData.getLineNumber()),
                         testRecordData.getFilename(),
                         testRecordData.getInputRecord());
    log.logMessage(testRecordData.getFac(), message,  FAILED_MESSAGE, detail);
  }

  /**
   * check if params.size() < minparams
   * If params.size() is insufficient we set testRecordData.setStatusCode to GENERAL_SCRIPT_FAILURE 
   * and log a paramsFailedMsg and return 'false'.  
   * Otherwise, we simply return 'true'.
   * @param minparams - minimum number of parameters required for processing
   * @return true if minimum number of parameters exist
   */
  protected boolean validateParamSize(int minparams){
	  try {
		  if (params.size()< minparams){
			  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			  paramsFailedMsg();
			  return false;
		  }
	  } catch (SAFSException e) {
		  String emsg = e.getMessage();
		  if (emsg.length()==0) emsg = e.getClass().getName();
		  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		  standardFailureMessage(failedText.convert("failure1", 
								 "Unable to perform "+ testRecordData.getCommand(), testRecordData.getCommand()), 
								 "SAFSException:"+ emsg);
		  return false;
	  }
	  return true;
  }
  /**
   * @param filename		A file name
   * @param directory		This is a variable assigned in STAF system. It can be one of following:
   * 						1. STAFHelper.SAFS_VAR_BENCHDIRECTORY
   * 						2. STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY
   * 						3. STAFHelper.SAFS_VAR_DIFDIRECTORY
   * 						4. STAFHelper.SAFS_VAR_LOGSDIRECTORY
   * 						5. STAFHelper.SAFS_VAR_PROJECTDIRECTORY
   * 						6. STAFHelper.SAFS_VAR_TESTDIRECTORY
   * @return				An absolute filename
   *                        if the parameter filename is absolute, return the parameter filename itself.
   *                        if the parameter filename is relative,
   *                           return the file under parameter directory if that file exist; 
   *                           otherwise if that file does not exist, return file under directory STAFHelper.SAFS_VAR_PROJECTDIRECTORY
   */
  protected String getAbsolutFileName(String filename, String directory){
	  File fn = new CaseInsensitiveFile(filename).toFile();

	  if (!fn.isAbsolute()) {
		  String pdir = null;
		  try {
			  pdir = getVariable(directory);
		  } catch (SAFSException e) {
			  Log.error("Fail to get directory value by variable '"+directory+"'");
		  }
		  if (pdir == null) pdir="";
		  fn = new CaseInsensitiveFile(pdir, filename).toFile();
		  
		  try{
			  //Check the parent directory, if it is not valid, we try to combine filename with the project directory
			  File parent = fn.getParentFile();
			  if( (!parent.isDirectory() || !parent.exists()) || !fn.isAbsolute()){
				  try {
					  //relative to project directory
					  pdir = getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
				  } catch (SAFSException e) {
					  Log.error("Fail to get directory value by variable '"+STAFHelper.SAFS_VAR_PROJECTDIRECTORY+"'");
				  }
				  if (pdir!=null) fn = new CaseInsensitiveFile(pdir, filename).toFile();
			  }
		  }catch(Exception e){
			  Log.error("Fail to check parent directory '"+fn.getParentFile().getAbsolutePath()+"', due to "+StringUtils.debugmsg(e));
		  }
	  }
	  
	  //It is possible that the file does NOT exist when user capture data for the first time
	  if(!fn.exists() || !fn.isFile()) Log.warn("The file '"+ fn.getAbsolutePath() +"' does NOT exist.");
	  
	  return fn.getAbsolutePath();
  }
  
  /**
   * Return an absolute file name. If the parameter 'filename' is relative, the returned absolute<br>
   * filename will use value of variable {@link STAFHelper#SAFS_VAR_TESTDIRECTORY} as parent folder.<br>
   * @param filename String, a file name. Can be relative or absolute.
   * @return String, an absolute file name.
   * @deprecated use {@link #deduceTestFile(String)} instead
   */
  protected String normalizeTestFileName(String filename){
	  return getAbsolutFileName(filename, STAFHelper.SAFS_VAR_TESTDIRECTORY);
  }
  
  /**
   * Retrieve a non-empty field value from the testRecordData inputrecord.
   * If successful, fvalue will exit with the retrieved value.  On failure, the routine 
   * will set testRecordData.setStatusCode to GENERAL_SCRIPT_FAILURE and log a 'bad_param' 
   * standardFailureMessage.
   * 
   * @param findex - 0-based index of field to extract from input record.
   * @param fname - field name to use in issuing any Invalid Parameter error messages.
   * @param fvalue - String to receive the desired field value on success.
   * @return true if fvalue was successfully set to a non-empty value.  false if it 
   * was not and standard failure logging was performed.
   */  
  protected boolean getRequiredField(int findex, String fname, String fvalue){
	  try {
		  fvalue = StringUtils.getTrimmedUnquotedStr(testRecordData.getInputRecordToken(findex));
		  if(fvalue.length()==0){
			  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			  String errmsg = failedText.convert("bad_param", "Invalid parameter value for "+ fname, fname);
			  standardFailureMessage(errmsg, testRecordData.getInputRecord());
			  return false;
		  }
	  } catch (SAFSException e) {
		  String emsg = e.getMessage();
		  if (emsg.length()==0) emsg = e.getClass().getName();
		  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		  standardFailureMessage(failedText.convert("failure1", 
							   "Unable to perform "+ testRecordData.getCommand(), testRecordData.getCommand()), 
							   "SAFSException:"+ emsg);
		  return false;
	  }
	  return true;
  }

  /**
   * Retrieve the standard "SOMETHING failure in filename FILENAME at line LINENUMBER" message.
   * Expects testRecordData to already have filename and lineNumber.
   * @param failure -- text to appear before the filename and line number info.
   **/
  protected String getStandardErrorMessage(String failure){
  	return failedText.convert("standard_err", failure+" failure in filename "+
  			           testRecordData.getFilename()+" at line "+testRecordData.getLineNumber(),
					   failure, testRecordData.getFilename(),String.valueOf(testRecordData.getLineNumber()));
  }

  /**
   * log the more standard FAILED_MESSAGE with detail.
   * "SOMETHING failure in filename FILENAME at line LINENUMBER"<br/>
   * "DETAIL"<br/>
   * Expects testRecordData to already have filename and lineNumber.
   * @param failure -- the failure text that precedes the 'failure in filename' message.
   * @param detail -- the detail that will be used unmodified.
   **/
  protected void standardFailureMessage(String failure, String detail) {
    String message = getStandardErrorMessage(failure);
    log.logMessage(testRecordData.getFac(), message,  FAILED_MESSAGE, detail);
  }

  protected static int secsWaitForWindow    = 30;
  public static void setSecsWaitForWindow(int secs){ secsWaitForWindow = secs;}
  public static int getSecsWaitForWindow(){ return secsWaitForWindow;}
  
  protected static int secsWaitForComponent = 30;
  public static void setSecsWaitForComponent(int secs){ secsWaitForComponent = secs;}
  public static int getSecsWaitForComponent(){ return secsWaitForComponent;}
  
  protected static boolean commandLineBreakpoint = false;
  public static void setCommandLineBreakpoint(boolean enabled){ commandLineBreakpoint = enabled;}
  
  // should be some semi-colon delimited combination of Java;Html;Swt;Net;Win;Flex
  protected static String testDomains = "Html;Java;Swt;Net;Win;Flex";
  public static void setTestDomains(String domains){ if(domains != null) testDomains = domains;}

  protected static boolean clearProxiesAlways = false;
  public static void setClearProxiesAlways(boolean enabled){ clearProxiesAlways = enabled;}
  public static boolean getClearProxiesAlways(){ return clearProxiesAlways;}
  
  protected static boolean RFSMOnly;
  public static void setRFSMOnly(boolean enable){RFSMOnly = enable;}
  public static boolean isRFSMOnly(){return RFSMOnly;}
  
  protected static boolean RFSMCache;
  public static void setRFSMCache(boolean enable){RFSMCache = enable;}
  public static boolean isRFSMCache(){return RFSMCache;}

  /** How to handle the 'alert dialog' if it is visible unexpectedly, it could be set as 'accept' or 'dismiss' or 'ignore' */
  protected static String unexpectedAlertBehaviour = DriverConstant.DEFAULT_UNEXPECTED_ALERT_BEHAVIOUR;
  /** Get how the 'alert dialog' will be handled if it is visible unexpectedly. 
   * @return String, 'accept' or 'dismiss' or 'ignore'*/
  public static String getUnexpectedAlertBehaviour() {
	  return unexpectedAlertBehaviour;
  }
  /** Set how to handle the 'alert dialog' if it is visible unexpectedly, it could be set as 'accept' or 'dismiss' or 'ignore' */
  public static void setUnexpectedAlertBehaviour(String behaviour) {
	  unexpectedAlertBehaviour = behaviour;
  }
  
  /**
   * Deduce the absolute full path test-relative file.
   * @param filename, String, the test/actual file name.  If there are any File.separators in the 
   * relative path then the path is actually considered relative to the Datapool 
   * directory unless it does not exist, or is already an absolute file path.
   * <p>
   * If a relative directory path does not exist relative to the Datapool directory then 
   * the final path will be relative to the Project directory.
   * <p>
   * If it is an absolute path, and contains a root path that includes the Bench directory, then the 
   * file will be converted to a comparable relative path off the Test directory.
   * <p>
   * @return File, the absolute full path test file.
   * @throws SAFSException
   * @see {@link #deduceFile(String, int)}
   */
  protected File deduceTestFile(String filename) throws SAFSException{
	  return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_TEST, this);
  }
  
  /**
   * Deduce the absolute full path Diff-relative file.
   * @param filename, String, the diff file name.  If there are any File.separators in the 
   * relative path then the path is actually considered relative to the Datapool 
   * directory unless it does not exist, or is already an absolute file path.
   * <p>
   * If a relative directory path does not exist relative to the Datapool directory then 
   * the final path will be relative to the Project directory.
   * <p>
   * If it is an absolute path, and contains a root path that includes the Bench directory, then the 
   * file will be converted to a comparable relative path off the Diff directory.
   * <p>
   * @return File, the absolute full path diff file.
   * @throws SAFSException
   * @see {@link #deduceFile(String, int)}
   */
  protected File deduceDiffFile(String filename) throws SAFSException{
	  return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_DIFF, this);
  }
  
  /**
   * Deduce the absolute full path bench-relative file.
   * @param filename, String, the test file name.  If there are any File.separators in the 
   * relative path then the path is actually considered relative to the Datapool 
   * directory unless it does not exist, or is already an absolute file path.
   * If a relative directory path does not exist relative to the Datapool directory then 
   * the final path will be relative to the Project directory.
   * @return File, the absolute full path bench file.
   * @throws SAFSException
   * @see {@link #deduceFile(String, int)}
   */
  protected File deduceBenchFile(String filename) throws SAFSException{
	  return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_BENCH, this);
  }
  
  /**
   * Deduce the absolute full path to a project-relative file.
   * @param filename, String, the test file name.  The path is ALWAYS considered relative 
   * to the project root directory regardless of the absence or presence of File.separators 
   * unless the file is already an absolute path.
   * @return File, the absolute full path bench file.
   * @throws SAFSException 
   * @see {@link #deduceFile(String, int)}
   */
  protected File deduceProjectFile(String filename) throws SAFSException{
	  return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_PROJECT, this);
  }

  /**
   * Write failure to log.<br>
   * Write failure to debug log.<br>
   * @param aborttext String, the abort message
   * @param details String, the detail failure message
   * @param error String, the error message
   */
  protected void logFailureMessage(String aborttext, String details, String error){
	  log.logMessage(testRecordData.getFac(), aborttext , details, FAILED_MESSAGE);
	  IndependantLog.error("Error at line "+testRecordData.getLineNumber()+ " in file "+testRecordData.getFilename()+" : "+ error);
	  IndependantLog.error("Inputrecord "+testRecordData.getInputRecord());  
  }
  
  /** 
   * Unable to perform [action]. <br>
   * "Error at line [number] in file [file]: [error]"
   * <p>
   * Sets status to FAILURE and issues a FAILED message.
   * called by other issueFailure routines 
   ***/
  protected void issueInputRecordFailure(String error){
	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  String aborttext = FAILStrings.convert(FAILStrings.FAILURE_1, 
			  "Unable to perform '"+testRecordData.getCommand()+"'.",
			  testRecordData.getCommand());
	  String details = FAILStrings.convert(FAILStrings.FAILURE_DETAIL,
			  "Error at line "+testRecordData.getLineNumber()+
			  " in file "+testRecordData.getFilename()+
			  " : "+ error,
			  String.valueOf(testRecordData.getLineNumber()),
			  testRecordData.getFilename(),
			  error);
	  logFailureMessage(aborttext, details, error);
  }
  
  /**
   * If [message]==null<br>
   * Unable to perform [action]. <br>
   * "Error at line [number] in file [file]: [error]"<br>
   * 
   * If [message]!=null<br>
   * [message] <br>
   * "Error at line [number] in file [file]: [error]"<br>
   * <p>
   * Sets status to FAILURE and issues a FAILED message.
   * called by other issueFailure routines 
   ***/
  protected void issueInputRecordFailure(String message, String error){
	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  String aborttext = message==null?
			  FAILStrings.convert(FAILStrings.FAILURE_1, 
			  "Unable to perform '"+testRecordData.getCommand()+"'.",
			  testRecordData.getCommand()) : message;
	  String details = FAILStrings.convert(FAILStrings.FAILURE_DETAIL,
			  "Error at line "+testRecordData.getLineNumber()+
			  " in file "+testRecordData.getFilename()+
			  " : "+ error,
			  String.valueOf(testRecordData.getLineNumber()),
			  testRecordData.getFilename(),
			  error);
	  logFailureMessage(aborttext, details, error);
  }
  
  /** 
   * issue OK status and PASSED message.<br>
   * If [message]==null<br>
   * [action] successful.<br>
   * [detail]<br>
   * 
   * If [message]!=null<br>
   * [message]<br>
   * [detail]<br>
   * string comment is expected to already be localized, but can be null.
   **/
  protected void issuePassedSuccess(String message, String detail){
      testRecordData.setStatusCode(StatusCodes.OK);
      String success = message==null?
    		  GENStrings.convert(GENStrings.SUCCESS_1, 
                       testRecordData.getCommand() +" successful.",
                       testRecordData.getCommand()) : message;
      log.logMessage(testRecordData.getFac(), success, detail, PASSED_MESSAGE);	  
  }
  
  /**
   * At the end of processing a test-record, this method could be called to set the
   * test-record's status code, and write the message to the Test Log.<br>
   * Currently, for
   * <ul> 
   * <li>{@link StatusCodes#NO_SCRIPT_FAILURE}, it calls {@link #issuePassedSuccess(String, String)}
   * <li>{@link StatusCodes#GENERAL_SCRIPT_FAILURE}, it calls {@link #issueInputRecordFailure(String, String)}
   * <li>Other status code, it simply writes a generic message to the test log.
   * </ul>
   * @param statusCode int, the status code to set to test-record.
   * @param message String, the message to write to the Test Log.
   *                        If it is null, then the default message will be used. 
   * @param detail String, the detail message to write to the Test Log.
   *                       If it is null, then the default detail message will be used.
   *
   * @see #issueInputRecordFailure(String, String)
   * @see #issuePassedSuccess(String, String)
   */
  protected void setAtEndOfProcess(int statusCode, String message, String detail){
	  IndependantLog.debug("After process, the status code is '"+statusCode+"'.\n message:\n "+message+"\n detail:\n"+detail+"\n");

	  if(statusCode==StatusCodes.NO_SCRIPT_FAILURE){
		  issuePassedSuccess(message, detail);
	  }else if(statusCode==StatusCodes.GENERAL_SCRIPT_FAILURE){
		  issueInputRecordFailure(message, detail);
	  }else{
		  log.logMessage(testRecordData.getFac(), message, detail, GENERIC_MESSAGE);	
		  testRecordData.setStatusCode(statusCode);
	  }
  }

  /** 
   * Unable to perform [action]. <br>
   * "Error at line [number] in file [file]: [error]"
   * <p>
   * Sets status to FAILURE and issues a FAILED message.
   * called by other issueFailure routines 
   ***/
  protected void issueActionFailure(String error){
	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  String aborttext = FAILStrings.convert(FAILStrings.FAILURE_1, 
			  "Unable to perform '"+testRecordData.getCommand()+"'.",
			  testRecordData.getCommand());
	  aborttext += "  "+ error;
	  String details = FAILStrings.convert(FAILStrings.FAILURE_DETAIL,
			  "Error at line "+testRecordData.getLineNumber()+
			  " in file "+testRecordData.getFilename()+
			  " : "+ error,
			  String.valueOf(testRecordData.getLineNumber()),
			  testRecordData.getFilename(),
			  error);
	  logFailureMessage(aborttext, details, error);
  }

  /**
   * Unable to perform [action]. <br>
   * "Error at line [number] in file [file]: [error]"
   * <p>
   * Sets status to FAILURE and issues a FAILED message.
   */
  protected void issueActionOnXFailure(String x, String error){
	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  String aborttext = FAILStrings.convert(FAILStrings.FAILURE_1, 
			  "Unable to perform '"+testRecordData.getCommand()+"' on '"+ x +"'.",
			  testRecordData.getCommand(), x);
	  aborttext += "  "+ error;
	  String details = FAILStrings.convert(FAILStrings.FAILURE_DETAIL,
			  "Error at line "+testRecordData.getLineNumber()+
			  " in file "+testRecordData.getFilename()+
			  " : "+ error,
			  String.valueOf(testRecordData.getLineNumber()),
			  testRecordData.getFilename(),
			  error);
	  logFailureMessage(aborttext, details, error);
  }

  /** 
   * "Error performing [action]."<br>
   * "Error at line [number] in file [file]: [error]"
   ***/
  protected void issueErrorPerformingAction(String error){
	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  String aborttext = FAILStrings.convert(FAILStrings.ERROR_PERFORMING_1, 
			  "Error performing '"+testRecordData.getCommand()+"'.",
			  testRecordData.getCommand());
	  aborttext += "  "+ error;
	  String details = FAILStrings.convert(FAILStrings.FAILURE_DETAIL,
			  "Error at line "+testRecordData.getLineNumber()+
			  " in file "+testRecordData.getFilename()+
			  " : "+ error,
			  String.valueOf(testRecordData.getLineNumber()),
			  testRecordData.getFilename(),
			  error);
	  logFailureMessage(aborttext, details, error);
  }

  /**
   * "Error performing [action] on [x]."<br>
   * "Error at line [number] in file [file]: [error]"
   ***/
  protected void issueErrorPerformingActionOnX(String x, String error){
	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  String aborttext = FAILStrings.convert(FAILStrings.ERROR_PERFORMING_2, 
			  "Error performing '"+testRecordData.getCommand()+"' on '"+ x +"'.",
			  testRecordData.getCommand(), x);
	  aborttext += "  "+ error;
	  String details = FAILStrings.convert(FAILStrings.FAILURE_DETAIL,
			  "Error at line "+testRecordData.getLineNumber()+
			  " in file "+testRecordData.getFilename()+
			  " : "+ error,
			  String.valueOf(testRecordData.getLineNumber()),
			  testRecordData.getFilename(),
			  error);
	  logFailureMessage(aborttext, details, error);
  }

  /**
   * "[action] was not successful using [x]."<br>
   * "Error at line [number] in file [file]: [error]"
   ***/
  protected void issueErrorPerformingActionUsing(String x, String error){
	  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  String aborttext = FAILStrings.convert(FAILStrings.NO_SUCCESS_2, 
			  testRecordData.getCommand()+ " was not successful using '"+ x +"'.",
			  testRecordData.getCommand(), x);
	  aborttext += "  "+ error;
	  String details = FAILStrings.convert(FAILStrings.FAILURE_DETAIL,
			  "Error at line "+testRecordData.getLineNumber()+
			  " in file "+testRecordData.getFilename()+
			  " : "+ error,
			  String.valueOf(testRecordData.getLineNumber()),
			  testRecordData.getFilename(),
			  error);
	  logFailureMessage(aborttext, details, error);
  }
  
  /** "safsparams.dat" **/
  public static final String SAFS_PARAMS_FILE = "safsparams.dat";
  
  /** "clearProxiesAlways" 
   **/
   public static final String SAFS_CLEARPROXIESALWAYS_ITEM = "clearProxiesAlways";
   
  /** 
  * "testDomains" 
  * Config setting should contain semi-colon delimited combinations of values:
  * Java;Html;Net;Win
  **/
  public static final String SAFS_TESTDOMAINS_ITEM = "testDomains";
  
  /** "secsWaitForWindow" 
  **/
  public static final String SAFS_SECSWAITFORWINDOW_ITEM = "secsWaitForWindow";
  
  /** "secsWaitForComponent" 
  **/
  public static final String SAFS_SECSWAITFORCOMPONENT_ITEM = "secsWaitForComponent";
  
  /** "commandLineBreakpoint" 
  **/
  public static final String SAFS_COMMANDLINEBREAKPOINT_ITEM = "commandLineBreakpoint";
  
  /** "safs.secsWaitForWindow" 
  **/
  public static final String SAFS_SECSWAITFORWINDOW_VARIABLE = "safs."+ SAFS_SECSWAITFORWINDOW_ITEM;

  /** "safs.secsWaitForComponent" 
  **/
  public static final String SAFS_SECSWAITFORCOMPONENT_VARIABLE = "safs."+ SAFS_SECSWAITFORCOMPONENT_ITEM;
  
  /** "safs.commandLineBreakpoint" 
  **/
  public static final String SAFS_COMMANDLINEBREAKPOINT_VARIABLE = "safs."+ SAFS_COMMANDLINEBREAKPOINT_ITEM;

  /** "safs.testDomains" 
  **/
  public static final String SAFS_TESTDOMAINS_VARIABLE = "safs."+ SAFS_TESTDOMAINS_ITEM;
  
  protected static boolean processorConfigSet = false;
  
  /** "RFSMOnly"
   **/
  public static final String RFT_FIND_SEARCH_MODE_ALGORITHM = "RFSMOnly";
  
  /** "RFSMCache"
   **/
  public static final String RFT_FIND_SEARCH_MODE_CACHE = "RFSMCache";

  static {
    try{      
      Properties safsparams = new Properties();
      Log.info("Processor attempting getResource for "+ SAFS_PARAMS_FILE);

      URL url = null;
      try{ url = GuiClassData.getResourceURL(Processor.class,SAFS_PARAMS_FILE);}
      catch(MissingResourceException mr){throw new NullPointerException("SAFS.Processor could not locate:"+ SAFS_PARAMS_FILE);}
      URL custom = AgentClassLoader.findCustomizedJARResource(url, SAFS_PARAMS_FILE);
      try{ if(!custom.sameFile(url))url = custom; }catch(Exception x){;}
      
      InputStream in = url.openStream();
      System.out.println(SAFS_PARAMS_FILE +" path: '"+ url.getPath()+"'");
      
      if (in != null) {
    	
    	Log.info("Processor loading "+ SAFS_PARAMS_FILE);        
        safsparams.load(in);
        in.close();
        in = null;
        String p = safsparams.getProperty(SAFS_SECSWAITFORWINDOW_ITEM);
        secsWaitForWindow = Integer.parseInt(p);
        System.out.println(SAFS_SECSWAITFORWINDOW_ITEM +": "+secsWaitForWindow);
        p = safsparams.getProperty(SAFS_SECSWAITFORCOMPONENT_ITEM);
        secsWaitForComponent = Integer.parseInt(p);
        System.out.println(SAFS_SECSWAITFORCOMPONENT_ITEM +": "+secsWaitForComponent);
        p = safsparams.getProperty(SAFS_COMMANDLINEBREAKPOINT_ITEM);
        commandLineBreakpoint = Boolean.valueOf(p).booleanValue();
        System.out.println(SAFS_COMMANDLINEBREAKPOINT_ITEM +": "+commandLineBreakpoint);
        p = safsparams.getProperty(SAFS_TESTDOMAINS_ITEM);
        if ((p!=null)&&(p.length()> 0)) {
        	testDomains = p;        	
        }
        Domains.enableDomains(testDomains);
        System.out.println(SAFS_TESTDOMAINS_ITEM +": "+testDomains);
        p = safsparams.getProperty(SAFS_CLEARPROXIESALWAYS_ITEM);
        if ((p!=null)&&(p.length()> 0)) {
            setClearProxiesAlways(Boolean.valueOf(p).booleanValue());
            System.out.println(SAFS_CLEARPROXIESALWAYS_ITEM +": "+p);
        }
      }else{
        Log.info("Processor failed loading "+ SAFS_PARAMS_FILE);
      }
    } catch (FileNotFoundException ex) {
      System.err.println(ex.getMessage());
      Log.error(ex.getMessage(), ex);
    } catch (IOException io) {
      System.err.println(io.getMessage());
      Log.error(io.getMessage(), io);
    } catch (NumberFormatException nf) {
      System.err.println(nf.getMessage());
      Log.error(nf.getMessage(), nf);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      Log.error(e.getMessage(), e);
    }
  }
  
	/**
	 * <p>
	 * Push the current 'test record' into the Stack before the execution of a keyword.
	 * This should be called after the 'test record' is properly set.
	 * </p>
	 * 
	 * @param trd TestRecordData, the test record to push into a stack
	 * @see #popTestRecord()
	 */
	public void pushTestRecord(TestRecordData trd) {
		testrecordStackable.pushTestRecord(trd);		
	}

	/**
	 * Retrieve the Test-Record from the the Stack after the execution of a keyword.<br>
	 * <p>
	 * After execution of a keyword, pop the test record from Stack and return is as the result.
	 * Replace the class field 'Test Record' by that popped from the stack if they are not same.
	 * </p>
	 * 
	 * @see #pushTestRecord()
	 * @return TestRecordData, the 'Test Record' on top of the stack
	 */
	public TestRecordData popTestRecord() {
		String debugmsg = StringUtils.debugmsg(false);
		DefaultTestRecordStackable.debug(debugmsg+"Current test record: "+StringUtils.toStringWithAddress(testRecordData));
		
		TestRecordData history = testrecordStackable.popTestRecord();
		
		if(!testRecordData.equals(history)){
			DefaultTestRecordStackable.debug(debugmsg+"Reset current test record to: "+StringUtils.toStringWithAddress(history));
			//The cast should be safe, as we push TestRecordHelper into the stack.
			testRecordData = (TestRecordHelper) history;
		}
		
		return history;
	}
}
