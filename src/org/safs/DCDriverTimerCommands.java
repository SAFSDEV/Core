/** Copyright (C) (MSA, Inc) All rights reserved.
** General Public License: http://www.opensource.org/licenses/gpl-license.php
**/

package org.safs;

import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;

import java.util.Iterator;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Process a timer commands.
 * Instantiated by DCDriverCommand
 *
 *   <br>   Sep 16, 2005    (MCasebeer) Original Release 
 * 
 * @author Matt Casebeer
 * @since   Dec 16, 2005
 **/
public class DCDriverTimerCommands extends DriverCommand{
    
    public static final String COMMAND_START_TIMER = "StartTimer";
    public static final String COMMAND_STOP_TIMER = "StopTimer";
    public static final String COMMAND_RESET_TIMER = "ResetTimer";
    public static final String COMMAND_VERIFY_TIMER = "VerifyTimer";
    public static final String COMMAND_VERIFY_TIMER_LESS = 
                                                    "VerifyElapsedTimeIsLess";
    public static final String COMMAND_VERIFY_TIMER_MORE = 
                                                    "VerifyElapsedTimeIsMore";
    public static final String COMMAND_VERIFY_TIMER_RANGE = 
                                                    "VerifyElapsedTimeInRange";
    public static final String COMMAND_STORE_TIMER = "StoreTimerInfo";
    public static final String COMMAND_STORE_TIMER_FILE = "StoreTimerInfoFile";
    
    public static final int VERIFY_LESS_THEN = 1;
    public static final int VERIFY_GREATER_THEN = 2;
    public static final int VERIFY_RANGE = 3;
    
    public static final String TIMER_START_SUFFIX    = "_startDT";
    public static final String TIMER_END_SUFFIX      = "_endDT";
    public static final String TIMER_FAILURES_SUFFIX = "_failures";
    public static final String TIMER_VERIFY_SUFFIX   = "_verifyValue";
    
    public static final String TIMER_STATUS_ACTIVE   = "active";
    public static final String TIMER_STATUS_STOPPED  = "stopped";
	public static final String TIMER_STATUS_RESET    = "reset";

    public static final String VAR_START_SUFFIX      = ".startTime";
    public static final String VAR_END_SUFFIX        = ".endTime";
    public static final String VAR_FAILURES_SUFFIX   = ".failures";
    public static final String VAR_ELAPSED_SUFFIX    = ".elapsed";
    
    /** <br><em>Purpose:</em> constructor, calls super
    **/
    public DCDriverTimerCommands() {
        super();             
    }
    
    /** <br><em>Purpose:</em> process: process the testRecordData
    ** <br>This is the driver command processor for timer commands.
    ** Current commands :<br>
    ** <br> StartTimer
    ** <br> StopTimer
    ** <br> ResetTimer
    ** <br> VerifyTimer
    ** <br> VerifyElapsedTimeIsLess
    ** <br> VerifyElapsedTimeIsMore
    ** <br> VerifyElapsedTimeInRange
    ** <br> StoreTimerInfo
    ** <br> StoreTimerInfoFile
    ** <br> 
    **/
    public void process() {   
        String cmd = testRecordData.getCommand();
        try{
        	setRecordProcessed(true);
            if(cmd.equalsIgnoreCase(COMMAND_START_TIMER)){
                cmdStartTimer();
            }else if(cmd.equalsIgnoreCase(COMMAND_STOP_TIMER)){
                cmdStopTimer();
            }else if(cmd.equalsIgnoreCase(COMMAND_RESET_TIMER)){
                cmdResetTimer();
            }else if(cmd.equalsIgnoreCase(COMMAND_VERIFY_TIMER) || 
                     cmd.equalsIgnoreCase(COMMAND_VERIFY_TIMER_LESS)){
                cmdVerifyTimer(VERIFY_LESS_THEN);
            }else if(cmd.equalsIgnoreCase(COMMAND_VERIFY_TIMER_MORE)){
                cmdVerifyTimer(VERIFY_GREATER_THEN);
            }else if(cmd.equalsIgnoreCase(COMMAND_VERIFY_TIMER_RANGE)){
                cmdVerifyTimer(VERIFY_RANGE);
            }else if(cmd.equalsIgnoreCase(COMMAND_STORE_TIMER)){
                cmdStoreTimer();
            }else if(cmd.equalsIgnoreCase(COMMAND_STORE_TIMER_FILE)){
                cmdStoreTimerFile();
            }else{
                setRecordProcessed(false);
            }
        }catch(SAFSException se){
        	Log.debug("DCDTC '"+ cmd +"' "+ se.getClass().getSimpleName()+", "+se.getMessage());
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            log.logMessage(testRecordData.getFac(),"SAFSException: " + 
                           se.getMessage(),FAILED_MESSAGE);            
        }
    }
              
    /**
     *<em>Purpose:</em>Handles command StartTimer.&nbsp;Starts a timer using the
     *given unique name. If the unique name has already been activated the 
     *method will fail. However, if a timer with the name is "stopped", it will be 
     *allowed to be restarted with a new start time, but a warning will be logged 
     *that it is being restarted.    
     *@params   none
     *@return   void
     *@throws   SAFSException
     */
    public void cmdStartTimer() throws SAFSException{                
        if(!checkParams(1)){
            return;
        }
        Iterator iterator = params.iterator();
        String uid = (String)iterator.next();
        Log.info("DCDTC evaluating startTime for timer '"+ uid +"'");
        if(getVariable(uid).equals(TIMER_STATUS_ACTIVE)){
            issueActionFailure(GENStrings.convert(GENStrings.EXISTS, 
            		uid+" exists.", uid));
            return;
        }
        if(getVariable(uid).equals(TIMER_STATUS_STOPPED)){
            log.logMessage(testRecordData.getFac(),GENStrings.convert("timer_restarted", 
            		"Timer "+ uid +" will be restarted.", uid), WARNING_MESSAGE);
        }
        String verifyValue = "";
        if(params.size() > 1){            
            verifyValue = (String)iterator.next();
        }else{
            verifyValue = "-1";
        }
        if(verifyValue.length()> 0){
	        try{
	        	int check = Integer.parseInt(verifyValue);
	        }catch(NumberFormatException nf){
	        	issueParameterValueFailure("VerifyValue");
	        	return;
	        }
        }
        startTimer(uid, verifyValue);
    }
    
    /**
     *<em>Purpose:</em>Handles command ResetTimer.&nbsp;Resets a stopped timer of the
     *given unique name. If the timer is not "stopped" the method will fail.    
     *@params   none
     *@return   void
     *@throws   SAFSException
     */
    public void cmdResetTimer() throws SAFSException{                
        if(!checkParams(1)){
            return;
        }
        Iterator iterator = params.iterator();
        String uid = (String)iterator.next();
        Log.info("DCDTC evaluating stopped status for timer '"+ uid +"'");
        if(!getVariable(uid).equals(TIMER_STATUS_STOPPED)){
            issueActionFailure(GENStrings.convert("timer_must_stop", 
            		"Timer "+ uid+ " must be stopped.", uid));
            return;
        }
        resetTimer(uid);
    }
    
    /**
     *<em>Purpose:</em>Handles the command StoreTimerInfoFile. Stores all
     *standard information about a given timer plus any additional information
     *located within TimerStorageVariables.     
     *@params   none
     *@return   void
     *@throws   SAFSException
     */
    public void cmdStoreTimerFile() throws SAFSException{
        if(!checkParams(2)){
            return;
        }
        Iterator iterator = params.iterator();
        String uid = (String)iterator.next();
        String path = (String)iterator.next();
        String cols = getVariable("TimerStorageVariables");
        
        /*
         *TODO: Currently uses SAFS_VAR_PROJECTDIRECTORY should be updated to use
         *it's own directory path. If the script uses an absolute path it will
         *override the default path.
         */
        File file = new File(path);
        if(!file.isAbsolute()){
            String pdir = getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
            if (pdir == null) return;
            file = new File(pdir + path);
        }
        
        String sOut = "";
        if(!file.exists()){
            sOut = "Name,Start,Stop,Duration,Timeout,Pass?";
            if(cols.length() > 0){
                sOut = sOut + "," + cols;
            }
            try{
            	writeFile(file,sOut,false);
            }catch(IOException e){
	            issueActionFailure(FAILStrings.convert(FAILStrings.FILE_WRITE_ERROR, 
	            		"Error writing to file '"+ file.getAbsolutePath()+"'", file.getAbsolutePath()));
	            return;
	        }            
        }
        sOut = uid + ","  + getStartTime(uid).replace(',',' ') + "," + 
               getEndTime(uid).replace(',',' ') + "," + 
               getElapsed(uid) + "," + 
               getVariable(uid + TIMER_VERIFY_SUFFIX) + ",";
        try{
	        if(Integer.parseInt(getFailures(uid)) > 0){
	            sOut = sOut + "-1";
	        }else{
	            sOut = sOut + "0";
	        }
        }catch(NumberFormatException nf){
        	Log.debug("DCDTC IGNORING getFailures "+ nf.getClass().getSimpleName());
            sOut = sOut + "0";
        }
        if(cols.length() > 0){
            String[] aCols = cols.split(",");
            for(int i=0;i < aCols.length;i++){
                sOut = sOut + "," + getVariable(aCols[i]);
            }
        }
        try{
        	writeFile(file,sOut,true);
            issueGenericSuccess(GENStrings.convert(GENStrings.BE_SAVED_TO, 
            		uid +" has been saved to "+ file.getAbsolutePath(), uid, file.getAbsolutePath()));
        }catch(IOException e){
            issueActionFailure(FAILStrings.convert(FAILStrings.FILE_WRITE_ERROR, 
            		"Error writing to file '"+ file.getAbsolutePath()+"'", file.getAbsolutePath()));
        }            
    }
    
    /**<em>Purpose:</em> Handles StoreTimerInfo command by storing the following
     *standard timer information: .elapsed .startTime .endTime .failures under
     *the given variable name.
     *
     *@author Matt Casebeer
     *@params none
     *@return void
     *@throws SAFSException
     */
    public void cmdStoreTimer() throws SAFSException{
        if(!checkParams(2)){
            return;
        }
        boolean setError = false;
        Iterator iterator = params.iterator();
        String uid = (String)iterator.next();
        String var = (String)iterator.next();
        Integer elapsed = new Integer(getElapsed(uid));
        if(!setVariable(var + VAR_ELAPSED_SUFFIX,elapsed.toString())){
            setError = true;
        }
        if(!setVariable(var + VAR_START_SUFFIX,getStartTime(uid))){
            setError = true;
        }
        if(!setVariable(var + VAR_END_SUFFIX,getEndTime(uid))){
            setError = true;
        }
        if(!setVariable(var + VAR_FAILURES_SUFFIX,getFailures(uid))){
            setError = true;
        }
        if(setError){
           issueActionFailure(FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
        		   "Could not set '"+var+"' to '"+ uid+"'.", var, uid));
           return;
        }
        issueGenericSuccess(GENStrings.convert(GENStrings.VARASSIGNED2, 
        		"Multiple values from '"+uid+"' were assigned to root variable '"+var+"'.", uid, var));
    }
    
    /**
     *<em>Purpose:</em>Handles command StopTimer. It will stop the given timer 
     *and set its status to "stopped".  If a verify value was set it will attempt 
     *to verify if the timer was successful.
     *
     *@author: Matt Casebeer
     *@params: none
     *@return: void
     *@throws: SAFSException
     */    
    public void cmdStopTimer() throws SAFSException{
        Date dt = new Date();
        DateFormat dateFormat = 
                DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);
        if(!checkParams(1)){
            return;
        }
        Iterator iterator = params.iterator();
        String uid = (String)iterator.next();
        if(!setVariable(uid,TIMER_STATUS_STOPPED)){        	
            issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS));
            return;
        }
        if(!setVariable(uid + TIMER_END_SUFFIX,dateFormat.format(dt))){        	
            issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS));
            return;
        }
        String verifyValue = getVariable(uid + TIMER_VERIFY_SUFFIX);
        try{
        	int diff = 0;
        	if(Integer.parseInt(verifyValue) > -1){
        		diff = getElapsed(uid);
        		evaluateTimer(diff, uid, verifyValue, null, VERIFY_LESS_THEN);
        		return;
        	}
        }catch(NumberFormatException nf){//not a number to test
        	Log.info("DCDTC.stopTimer IGNORING optional verifyValue '"+ verifyValue +"' is NOT a number...");        
        }
        catch(NullPointerException np){
        	Log.info("DCDTC.stopTimer IGNORING optional verifyValue '"+ verifyValue +"' is null");        
        }        
        issueGenericSuccess(GENStrings.convert(GENStrings.TIMER_STOPPED, 
        		"Timer "+ uid +" has been stopped.", uid));
    }

    /**
     * Issue pass or fail status and log messages for diff and verify values compared 
     * agains VERIFY_LESS_THAN, VERIFY_GREATER_THAN,and VERIFY_RANGE.
     * @param diff
     * @param uid
     * @param verifyValue
     * @param verifyValue2
     * @param actionType
     * @throws SAFSException
     */
    protected void evaluateTimer(int diff, String uid, String verifyValue, String verifyValue2, int actionType)throws SAFSException{
    	int vvalue = 0;
    	String detail = null;
    	try{ vvalue = Integer.parseInt(verifyValue);}
    	catch(NumberFormatException nf){
    		issueParameterValueFailure("VerifyValue");
    		return;
    	}
        switch(actionType){
            case VERIFY_LESS_THEN:
                if(diff > vvalue){
                    if(!setFailures(uid)){
                        issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS));
                        return;
                    }
                    detail = GENStrings.convert(GENStrings.GREATER, 
                    		uid +" was greater than "+ verifyValue, 
                    		uid, verifyValue);
                    issueExecutionNegativeMessage(detail);
                }else{
                    detail = GENStrings.convert(GENStrings.NOT_GREATER, 
                    		uid+" was not greater than "+ verifyValue, 
                    		uid, verifyValue);
                    this.issuePassedSuccess(detail);
                }
                break;
            case VERIFY_GREATER_THEN:
                if(diff < vvalue){
                    if(!setFailures(uid)){
                        issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS));
                        return;
                    }
                    detail = GENStrings.convert(GENStrings.NOT_GREATER, 
                    		uid +" was not greater than "+ verifyValue, 
                    		uid, verifyValue);
                    issueExecutionNegativeMessage(detail);
                }else{
                    detail = GENStrings.convert(GENStrings.GREATER, 
                    		uid +" was greater than "+ verifyValue, 
                    		uid, verifyValue);
                    this.issuePassedSuccess(detail);
                }
                break;
            case VERIFY_RANGE:
            	int vvalue2 = 0;
            	try{ vvalue2 = Integer.parseInt(verifyValue2);}
            	catch(NumberFormatException nf){
            		this.issueParameterValueFailure("VerifyValue2");
            		return;
            	}
                if((diff < vvalue) || (diff > vvalue2)){
                    if(!setFailures(uid)){
                        issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS));
                        return;
                    }
                    detail = GENStrings.convert(GENStrings.NOT_IN_RANGE, 
                    		uid +" was not in range "+ verifyValue +" to "+ verifyValue2, 
                    		uid, verifyValue, verifyValue2);
                    issueExecutionNegativeMessage(detail);
                }else{
                    detail = GENStrings.convert(GENStrings.IN_RANGE, 
                    		uid +" was in range "+ verifyValue +" to "+ verifyValue2, 
                    		uid, verifyValue, verifyValue2);
                    this.issuePassedSuccess(detail);
                }
                break;
        }
    }
    
    /**
     *<em>Purpose:</em>Handles commands VerifyTimer, VerifyElapsedTimeIsLess, 
     * VerifyElapsedTimeIsMore, VerifyElapsedTimeInRange. Uses given values to 
     *run the correct checks to Verify if the timer passed or failed.
     *
     *@author Matt Casebeer
     *@params actionType Which type of action the Verify: 1 greater, 2 less, 3 
     *range.
     *@return void
     *@throws SAFSException
     */
    public void cmdVerifyTimer(int actionType) throws SAFSException{
        if(!checkParams(1)){
            return;
        }
        Iterator iterator = params.iterator();
        String uid = (String)iterator.next();
        String verifyValue ="";
        if(params.size() > 1){
            verifyValue = (String)iterator.next();            
        }else{
            verifyValue =  getVariable(uid + TIMER_VERIFY_SUFFIX);        
        }        
        String verifyValue2 = "";        
        if(params.size() > 2){
            verifyValue2 = (String)iterator.next();
        }
        if(!setVariable(uid + TIMER_VERIFY_SUFFIX,verifyValue)){
            return;
        }
        int diff = getElapsed(uid);
        evaluateTimer(diff, uid, verifyValue, verifyValue2, actionType);
    }
    
    //Private functions
    
    /**
     *<em>Purpose:</em>Gets the stored start time based on timer id.
     *@author Matt Casebeer
     *@params String unique name of the timer.
     *@return String start time stored.
     *@throws SAFSException
     */
    private String getStartTime(String uid) throws SAFSException{
        return getVariable(uid + TIMER_START_SUFFIX);
    }
    
    /**
     *<em>Purpose:</em>Gets the stored end time based on timer id.
     *@author Matt Casebeer
     *@params String unique name of the timer.
     *@return String end time stored
     *@throws SAFSException
     */
    private String getEndTime(String uid) throws SAFSException{
        return getVariable(uid + TIMER_END_SUFFIX);
    }
    
    /**
     *<em>Purpose:</em>Gets the number of failures stored base on timer id.
     *@author Matt Casebeer
     *@params String unique name of the timer.
     *@return String count of failures.
     *@throws SAFSException
     */
    private String getFailures(String uid) throws SAFSException{
        return getVariable(uid + TIMER_FAILURES_SUFFIX);
    }
    
    /**
     *<em>Purpose:</em>Calculates and returns the elapsed time of a timer based
     *on timer id.
     *@params String unique timer id.
     *@returns int Elapsed time in seconds
     *@throws SAFSException
     */
    private int getElapsed(String uid) throws SAFSException{
        String sEndDT = getEndTime(uid);
        String sStartDT = getStartTime(uid);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(
                                                DateFormat.FULL,DateFormat.FULL);
        
        if(sEndDT.length() <= 0){
            Date dt = new Date();
            sEndDT = dateFormat.format(dt);
        }
        Date endDT = null;
        Date startDT = null;
        try{
            endDT = dateFormat.parse(sEndDT);
            startDT = dateFormat.parse(sStartDT);
        }catch(ParseException pe){
        	issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS));
            return 0;
        }
        int diff = (int)((endDT.getTime() - startDT.getTime())/1000);
        return diff;
    }
    
    /**
     *<em>Purpose:</em>Increments the current stored Failures by one and resets
     *the value based on timer id.     
     *@params String unique name of the timer
     *@return true if we successfully set the _failures variable. false otherwise.
     *@throws SAFSException
     */     
    private boolean setFailures(String uid) throws SAFSException{        
        int iFails = 0;
        try{ iFails = Integer.parseInt(getFailures(uid));}
        catch(NumberFormatException nf){
        	Log.info("DCDTC IGNORING unfound failure counter for counter '"+ uid +"'");
        }
        iFails = iFails + 1;
        return setVariable(uid + TIMER_FAILURES_SUFFIX,new Integer(iFails).toString());
    }
    
    /**
     *<em>Purpose:</em>Starts a timer and stores information needed through the
     *timers life cycle.
     *@param String String unique timer id.
     *@param String value to use for verification.
     *@return void
     *@throws SAFSException
     */
    private void startTimer(String uid, String verifyValue) 
            throws SAFSException{   
        boolean setError=false;
        Date dt = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(
                                                DateFormat.FULL,DateFormat.FULL);
        if(!setVariable(uid + TIMER_VERIFY_SUFFIX,verifyValue)){
            setError = true;
        }
        if(!setVariable(uid,TIMER_STATUS_ACTIVE)){
            setError = true;
        }
        if(!setVariable(uid + TIMER_START_SUFFIX,dateFormat.format(dt))){
            setError = true;
        }
        if(!setVariable(uid + TIMER_END_SUFFIX,"")){
            setError = true;
        }
        if(!setVariable(uid + TIMER_FAILURES_SUFFIX,"0")){
            setError = true;
        }
        if(setError){
            issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS));
            return;
        }
        issueGenericSuccess(GENStrings.convert(GENStrings.TIMER_STARTED, 
        		"Timer "+ uid +" has been started.", uid));
    }
    
    /**
     *<em>Purpose:</em>Resets a timer.
     *@param String String unique timer id.
     *@return void
     *@throws SAFSException
     */
    private void resetTimer(String uid) 
            throws SAFSException{   
        boolean setError=false;
        if(!setVariable(uid + TIMER_VERIFY_SUFFIX, "")){
            setError = true;
        }
        if(!setVariable(uid,TIMER_STATUS_RESET)){
            setError = true;
        }
        if(!setVariable(uid + TIMER_START_SUFFIX, "")){
            setError = true;
        }
        if(!setVariable(uid + TIMER_END_SUFFIX,"")){
            setError = true;
        }
        if(!setVariable(uid + TIMER_FAILURES_SUFFIX,"0")){
            setError = true;
        }        
        if(setError){
            issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS));
            return;
        }
        issueGenericSuccess(GENStrings.convert("timer_reset", 
        		"Timer "+ uid +" has been reset.", uid));
    }
    
    /**
     *<em>Purpose:</em>Checks command params. Issues failure status and generic 
     *failure message if insufficient parameters counted.
     *@param int Number of params expected.
     *@return boolean if expected number was recieved.
     *@throws SAFSException
     */
    private boolean checkParams (int expected) throws SAFSException {
        if (params.size() < expected) {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            paramsFailedMsg();
            return false;
        }
        return true;
    }   
    
    /**
     *<em>Purpose:</em>Writes information to file.
     *@param File   file to write information to
     *@param String line of data to write.
     *@param boolean to append data
     *@return void
     */
    private void writeFile(File file, String sLine, boolean append)throws IOException{
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), Charset.forName("UTF-8")), 1024*10);             
        out.write(sLine);                
        out.newLine();
        out.close();
    }
}
