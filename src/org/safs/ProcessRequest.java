/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import org.safs.logging.*;
import java.util.*;
/**
 * <br><em>Purpose:</em> ProcessRequest does a request when intantiated with a TestRecordData;
 * it acts like a factory which first grabs the request (recordType), then routes it
 * by instantiating the appropriate processor
 * <br><em>Lifetime:</em> instantiated by a hook script or some kind of controller.
 * Does not stay around for long as it only processes one request
 * <p>
 * @author  Doug Bauman
 * @since   JUN 03, 2003
 *
 *   <br>   JUN 03, 2003    (DBauman) Original Release
 *   <br>   SEP 16, 2003    (Carl Nagle) Implemented use of new SAFSLOGS logging.
 *   <br>   NOV 10, 2003    (Carl Nagle) Allowing for alternate processors.
 *   <br>   JUL 10, 2007    (Carl Nagle) Added engine command processor support.
 **/
public class ProcessRequest {

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

  /** 
   * Primary Engine Command Processor
   * Defaults to an instance of 'org.safs.EngineCommandProcessor'
   **/
  private Processor engineCommandProcessor = new EngineCommandProcessor();

  public Processor getEngineCommandProcessor() { return engineCommandProcessor; }
  public void      setEngineCommandProcessor(Processor aprocessor) {
  	engineCommandProcessor = aprocessor;
  }

  /** 
   * Primary Test Step Processor
   * Defaults to an instance of 'org.safs.TestStepProcessor'
   **/
  private Processor testStepProcessor = new TestStepProcessor();

  public Processor getTestStepProcessor() { return testStepProcessor; }
  public void      setTestStepProcessor(Processor aprocessor) {
  	testStepProcessor = aprocessor;
  }

  /** 
   * Primary Driver Command Processor
   * Defaults to an instance of 'org.safs.DriverCommandProcessor'
   **/
  private Processor driverCommandProcessor = new DriverCommandProcessor();

  public Processor getDriverCommandProcessor() { return driverCommandProcessor; }
  public void      setDriverCommandProcessor(Processor aprocessor) {
  	driverCommandProcessor = aprocessor;
  }

  /** 
   * Custom Engine Command Processor -- for non-core custom processing.
   * Defaults to 'null'.  A null value instructs the processor to dynamically seek 
   * an appropriate custom class if all other processors have been exhausted.
   **/
  private Processor  custom_ec_processor = null;

  public Processor getCustomEngineCommandProcessor() { return custom_ec_processor; }
  public void      setCustomEngineCommandProcessor(Processor aprocessor) {
  	custom_ec_processor = aprocessor;
  }

  /** 
   * Custom Test Step Processor -- for non-core custom processing.
   * Defaults to 'null'.  A null value instructs the processor to dynamically seek 
   * an appropriate custom class if all other processors have been exhausted.
   **/
  private Processor  custom_ts_processor = null;

  public Processor getCustomTestStepProcessor() { return custom_ts_processor; }
  public void      setCustomTestStepProcessor(Processor aprocessor) {
  	custom_ts_processor = aprocessor;
  }

  /** 
   * Custom Driver Command Processor -- for non-core custom processing.
   * Defaults to 'null'.  A null value instructs the processor to dynamically seek 
   * an appropriate custom class if all other processors have been exhausted.
   **/
  private Processor  custom_dc_processor = null;
  
  public Processor getCustomDriverCommandProcessor() { return custom_dc_processor; }
  public void      setCustomDriverCommandProcessor(Processor aprocessor) {
  	custom_dc_processor = aprocessor;
  }

  /** 
   * TestRecordData Helper subclass to be used by this processor.
   * This is generally provided via a class constructor.
   **/
  protected TestRecordHelper testRecordData = null;

  public TestRecordHelper getTestRecordData () { return testRecordData; }
  public void             setTestRecordData (TestRecordHelper testRecordData) {
    this.testRecordData = testRecordData; 
  }
  
  /** 
   * LogUtilities to be used by this processor.
   * This is usually provide via the constructor.
   **/
  protected LogUtilities log = null;

  public LogUtilities getLogUtilities () { return log; }
  public void         setLogUtilities (LogUtilities log) {
    this.log = log;
  }

  /** true if we have non-null objects where required **/
  protected boolean requesterReady = false;
  
  
  /**
   * Store of any additional processors used instead of, or in addition 
   * to, the normal core and custom processors. 
   */
  private java.util.Vector processors = null;
  
  /**
   * Add a processor that is used instead of, or in addition to, the 
   * normal core and custom processors. 
   */
  public void addProcessor(Processor aprocessor){
  	  if (aprocessor==null) return;
  	  if (processors==null) processors = new Vector(3,3);
  	  processors.addElement(aprocessor);
  }

  /**
   * Set (or clear) any list of processors.
   * <p>
   * processorList is a Vector list of Processors.  It is the user's responsibility 
   * to ensure any non-null Vector contains elements that are valid subclasses of 
   * org.safs.Processor.  As always, additional processors can be added to the list 
   * with the addProcessor method.
   */
  public void setProcessorList(Vector processorList){
  	if (processors != null) processors.clear();
  	processors = processorList;
  }
   
    
  /** 
   * Make sure the processor has a valid log.  If it does not, set the processor to 
   * use our log. (Hopefully our log is not also null!)
   * @param aprocessor -- Processor to initialize
   **/
  protected void initializeProcessor (Processor aprocessor){
  	if (aprocessor == null) return;
  	LogUtilities alog = aprocessor.getLogUtilities();
  	if (alog == null) aprocessor.setLogUtilities(getLogUtilities());
  }
  
  /** true if initializeProcessor has been called at least once. **/
  protected boolean initialized = false;

  /**
   * Force all necessary cross-initialization to occur prior to first use.
   * This can be invoked once all required assets have been 'set' from the constructor,
   * or after all subsequent 'set' commands.  Primarily, this makes sure all 'set' 
   * processors and other items are initialized to a valid log, etc..
   * <p>
   * This method will be called at the first execution of doRequest if it has not
   * already been called.
   * 
   * @see initializeProcessor
   **/
  public void initializeRequester(){
  	initializeProcessor(engineCommandProcessor);
  	initializeProcessor(testStepProcessor);
  	initializeProcessor(driverCommandProcessor);
  	initializeProcessor(custom_ec_processor);
  	initializeProcessor(custom_ts_processor);
  	initializeProcessor(custom_dc_processor);
    if ((processors != null)&&(! processors.isEmpty())){
      Enumeration procs = processors.elements();
      do{
          Processor proc = (Processor) procs.nextElement();
          initializeProcessor(proc);
      }while(procs.hasMoreElements());
    }
  	initialized = true;
  }

    
  /** 
   * Noop constructor.
   * Processor initialization must be completed by subsequent method calls.
   **/
  public ProcessRequest () {
  }

  /** 
   * Standard constructor allowing for default Driver Command and Test Step Processors.
   * Processor initialization must be completed by subsequent method calls if different 
   * processors are to be used.
   **/
  public ProcessRequest (TestRecordHelper testRecordData, LogUtilities log) {
  	this.setLogUtilities(log); // must be first for any logging
	this.setTestRecordData(testRecordData);
  }


  /** 
   * Advanced constructor allowing for Driver Command and Test Step Processors different
   * from the those provided by default.  
   * <p>
   * Note: null values can be used for any of the Processors to effectively disable 
   * the handling of that type of test record.  For example, a null value passed in 
   * for 'altTestStepProcessor' effectively disables the processing of 'standard'
   * Test Step Records.  Other non-null Processors will still be called, however.
   **/
  public ProcessRequest (TestRecordHelper testRecordData, LogUtilities log,
                         Processor altDriverCommandsProcessor, 
                         Processor altTestStepProcessor,
                         Processor altCustomDriverCommandsProcessor, 
                         Processor altCustomTestStepProcessor) {
                         	
  	this(testRecordData, log);
  	setDriverCommandProcessor(altDriverCommandsProcessor);
  	setTestStepProcessor(altTestStepProcessor);
  	setCustomDriverCommandProcessor(altCustomDriverCommandsProcessor);
  	setCustomTestStepProcessor(altCustomTestStepProcessor);
  }

  /** 
   * Advanced constructor allowing for Engine, Driver, and Test Processors different
   * from the those provided by default.  
   * <p>
   * Note: null values can be used for any of the Processors to effectively disable 
   * the handling of that type of test record.  For example, a null value passed in 
   * for 'altTestStepProcessor' effectively disables the processing of 'standard'
   * Test Step Records.  Other non-null Processors will still be called, however.
   **/
  public ProcessRequest (TestRecordHelper testRecordData, LogUtilities log,
  		                 Processor altEngineCommandsProcessor,
                         Processor altDriverCommandsProcessor, 
                         Processor altTestStepProcessor,
						 Processor altCustomEngineCommandsProcessor,
                         Processor altCustomDriverCommandsProcessor, 
                         Processor altCustomTestStepProcessor) {
  	this(testRecordData, log, altDriverCommandsProcessor, altTestStepProcessor, 
  		 altCustomDriverCommandsProcessor, altCustomTestStepProcessor);
  	setEngineCommandProcessor(altEngineCommandsProcessor);
  	setCustomEngineCommandProcessor(altCustomEngineCommandsProcessor);  	
  }
  
  /** 
   * Advanced constructor allowing for an alternate list of processors.  
   * <p>
   * Note: null values will be set for standard processors to effectively disable 
   * their handling.  Only those Processors in the provided processorList will 
   * be used.
   * <p>
   * processorList is a Vector list of Processors.  It is the user's responsibility 
   * to ensure any non-null Vector contains elements that are valid subclasses of 
   * org.safs.Processor.  As always, additional processors can be added to the list 
   * with the addProcessor method.
   **/
  public ProcessRequest (TestRecordHelper testRecordData, 
                         LogUtilities log,
                         Vector processorList) {
                         	
  	this(testRecordData, log);
  	setEngineCommandProcessor(null);
  	setDriverCommandProcessor(null);
  	setTestStepProcessor(null);
  	setCustomEngineCommandProcessor(null);
  	setCustomDriverCommandProcessor(null);
  	setCustomTestStepProcessor(null);
  	setProcessorList(processorList);
  }


  /**
   * Process a dispatch event for handling an input record from a hook/driver. 
   * All object instance preparation ('setX') must be complete prior to a call to this routine.
   * This routine will force a one-time, final initialization of all object assets prior to 
   * processing the first test record.  That generally means that all non-standard 'setX' 
   * calls and preparations must be complete prior test execution by the owning hook/driver.  
   * Of course, some hook/driver objects will handle this preparation automatically.
   * <p>
   * .doRequest first grabs the request (recordType), then routes it.
   * What we are processing here is a input record which comes from test tables in the form 
   * of Driver Commands, Test Steps, or other input record typs.
   * <p>
   * Sample Test Table Input Records<br>
   * ==============================<br>
   * <br>C , Expressions, ON
   * <br>T , Login , Password ,  SetTextValue  , ^password
   * <br>T , Login ,  Submit  ,  Click
   * <br>T , Error , Message  , VerifyProperty , ^prop="Text", ^val="Invalid UserID or Password!"
   * <br>T , Error ,   OK     ,  Click
   **/
  public void doRequest () {

	if(! initialized) initializeRequester();
	if( (! requesterReady)                   && 
	     ((( testRecordData == null )||( log == null))||
	       (( driverCommandProcessor == null) &&
	        ( engineCommandProcessor == null) &&
	        ( testStepProcessor      == null) &&
	        ( custom_ec_processor    == null) &&
	        ( custom_ts_processor    == null) &&
	        ( custom_dc_processor    == null) &&
	        ( processors             == null)))) { 
	   String message = getClass().getName() +":No Processors available for request!";
	   throw new SAFSProcessorInitializationException ( message );	   
	}else { requesterReady = true; }

	// DEFAULT RETURN STATUS
    testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	
    // first grab the recordType
    String recordType=""; //..get the numbered recordtype, always the first token
    try {
      recordType = testRecordData.getTrimmedUnquotedInputRecordToken(0).toUpperCase();
    } 
    catch (IndexOutOfBoundsException ioobe) {   	  
      Log.setDoLogMsg(true);
      Log.debug(getClass().getSimpleName()+
               ".doRequest: unsupported record format (field indexing) for this engine:["+
               testRecordData.getInputRecord()+"], or possibly you are using the wrong delimiter?");
      Log.setDoLogMsg(false);
      return;
    } 
    catch (SAFSException e) {    	
      Log.setDoLogMsg(true);
      Log.info(getClass().getSimpleName()+
               ".doRequest: unsupported record format for this engine: ["+
               testRecordData.getInputRecord()+"], or possibly you are using the wrong delimiter? "
               + e.getMessage());
      Log.setDoLogMsg(false);
      return;
    }
    
    testRecordData.setRecordType(recordType);
    
    // try driverCommandProcessor
	tryProcessor(driverCommandProcessor);

    // try testStepProcessor
	if(testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)
	   tryProcessor(testStepProcessor);

    // try engineCommandProcessor
	if(testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)
	   tryProcessor(engineCommandProcessor);

    // try the list of processors if we have any
	if((testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)&&
      (processors != null)&&(! processors.isEmpty())){
      Enumeration procs = processors.elements();
      do{
          Processor proc = (Processor) procs.nextElement();
          tryProcessor(proc);
      }while((testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)&&(procs.hasMoreElements()));
    }

    // try custom_dc_processor
	if(testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)
	   tryProcessor(custom_dc_processor);

    // try custom_ts_processor
	if(testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)
	   tryProcessor(custom_ts_processor);

    // try custom_ec_processor
	if(testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED)
	   tryProcessor(custom_ec_processor);

    // RECORD TYPE OR COMMAND IS NOT SUPPORTED
	if(testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED){
      if (testRecordData.getRecordType().length() > 1) Log.setDoLogMsg(true);
      Log.info(getClass().getSimpleName()+
               ".doRequest: Unknown Command or Unsupported RecordType ["+ 
               testRecordData.getRecordType() +"] for this engine.");
      if (testRecordData.getRecordType().length() > 1) Log.setDoLogMsg(false);
    }
  }
  

  /** 
   * Attempt execution of the current testRecordData.inputRecord.
   * This method is normally called by .doRequest() and is not normally called
   * directly.  
   * <p>
   * The method expects the testRecordData to be filled in as much 
   * as is normal when routing by record type.  Thus, the getRecordType() method 
   * of testRecordData is expected to return the current record type to be executed.
   * <p>
   * @param aprocessor -- the Processor to attempt execution of the current record.
   **/
  protected void tryProcessor(Processor aprocessor){
  	if (aprocessor == null) return;
  	if (aprocessor.isSupportedRecordType(testRecordData.getRecordType())){
  		aprocessor.setTestRecordData(testRecordData);
  		aprocessor.process();
  	}
  }
}


