/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.tools.drivers;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.STAFHelper;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.consoles.ProcessCapture;

import com.ibm.staf.STAFResult;

public class EmbeddedHookSTAFHelper extends STAFHelper {
	
	TestRecordData _data;
	boolean _engineRun = false;

	public EmbeddedHookSTAFHelper() {
		super();
	}

	public EmbeddedHookSTAFHelper(String processName)
			throws SAFSSTAFRegistrationException {
		super(processName);
	}

	protected TestRecordData _getData(){
		if (_data == null) _data = new TestRecordHelper();
		return _data;
	}
	
	/**
	 * The "engine" waits until the _engineRun flag is true and then begins execution.
	 * @see org.safs.STAFHelper#getNextHookTestEvent(java.lang.String, java.lang.String)
	 */
	@Override
	public String getNextHookTestEvent(String process_name, String trd_name)
			throws SAFSException {		
		while(!_engineRun){// && !JavaHook.SHUTDOWN_RECORD.equalsIgnoreCase(_data.getInputRecord()));
			try{Thread.sleep(50);}
			catch(Exception ignore){}
		}
		return _getData().getInputRecord();
	}

	/**
	 * Sets _engineRun = true to kickoff the "engine" execution.
	 * <p>
	 * The "driver" then waits for _engineRun flag to change to indicate the "engine" is finished.
	 * @see org.safs.STAFHelper#postNextHookTestEvent(java.lang.String, java.lang.String, org.safs.TestRecordData)
	 */
	@Override
	public int postNextHookTestEvent(String process_name, String trd_root,
			TestRecordData trd) throws SAFSException {
		setSAFSTestRecordData(trd_root, trd);		
		_engineRun = true;
		while(_engineRun){
			try{Thread.sleep(50);}
			catch(Exception x){}
		}
		return _getData().getStatusCode();
	}

	/**
	 * Sets the local driver/engine shared _data object and sets the TRD_HOOK data per the superclass.  
	 * @see org.safs.STAFHelper#setSAFSTestRecordData(java.lang.String, org.safs.TestRecordData)
	 */
	@Override
	public void setSAFSTestRecordData(String trd_root, TestRecordData trd)
			throws SAFSException {
		_data = trd.copyData(_getData());
		super.setSAFSTestRecordData(trd_root, trd);
	}

	@Override
	public void getSAFSTestRecordData(String trd_root, TestRecordData trd){
		trd = _getData().copyData(trd);
	}
	/**
	 * Prevents the posting of STAF EVENTS (RESULTS and DONE) events since everything 
	 * is local in the same JVM process.
	 * <p>
	 * Sets _engineRun = false to indicate the "engine" has finished processing the 
	 * record and allow the "driver" to evaluate the results.
	 * <p>
	 * @see org.safs.STAFHelper#setHookTestResultsWTimeout(java.lang.String, long)
	 */
	@Override
	public void setHookTestResultsWTimeout(String process_name,
			long timeoutseconds) throws SAFSException {
		_engineRun = false;
	}
	
	@Override
	public String getSTAFEnv(String env) {
	    String result = super.getSTAFEnv(env);
	    if(result == null){
	    	try{ result = System.getenv(env); }catch(SecurityException ignore){}
	    }
	    return result;
	}

	/**
	 * Cannot use STAF to run a process when STAF is not running!
	 * @param machine String, the name of the ("local") machine where the application will run
	 * @param appname String, the name of application to run
	 * @param workdir String, the directory serves as working directory for application
	 * @return STAFResult the result. STAFResult.OK, STAFResult.STAFNotRunning, STAFResult.InvalidParam, 
	 * STAFResult.UnknownError, STAFResult.ProcessNotComplete
	 * @throws IOException
	 */
	@Override
	public STAFResult localStartProcess(String appname, String workdir) throws IOException{
		String debugmsg = getClass().getName()+".localStartProcess(): ";
        Runtime rt = Runtime.getRuntime();
		Process p = null;
		File thedir = null;
		STAFResult rc = new STAFResult(STAFResult.STAFNotRunning);
        if (workdir != null){
        	if (workdir.length()>0){
        		thedir = new CaseInsensitiveFile(workdir).toFile();
        		if( (!(thedir.isDirectory())) || thedir == null){
        			thedir = null;
        			workdir= null;
        		}
        	}else{
        	    thedir = null;
        	    workdir= null;
        	}
        }
        try{
        	if (workdir==null) {
        		try{
        			p = rt.exec(appname.trim());
        		}
        		catch(SecurityException sx){
        	    	Log.debug(debugmsg+"Runtime.exec(appname) SecurityException: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
        		}
        	} else {
        		try{
        			p = rt.exec(appname.trim(), null, thedir);
        		}
        		catch(SecurityException sx){
        	    	Log.debug(debugmsg+"Runtime.exec(appname, null, thedir) SecurityException: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
        		}
        	}
        }catch(IOException io){
        	//Try to use "cmd /c commandString" to run
        	//This fixes problems like missing ".bat" extension, but does not fix other 
        	//issues that can generate an IOException.  We will need to put code here to 
        	//better deduce WHAT kind of problem we really have that generated the IOException.
        	if( ! appname.toUpperCase().startsWith("CMD /C")){
            	Log.debug(debugmsg +"IOException occured. We will try 'cmd /c commandString' to execute.");
        		appname = "cmd /c " + appname;
	        	if (workdir==null) {
	        		try{
	        			p = rt.exec(appname);
	        		}
	        		catch(SecurityException sx){
	        	    	Log.debug(debugmsg+"Runtime.exec(cmd /c appname) SecurityException: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
	        		}
	        		catch(Exception sx){
	        	    	Log.debug(debugmsg+"Runtime.exec(cmd /c appname) Exception: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
	        		}
	        	} else {
	        		try{
	        			p = rt.exec(appname, null, thedir);
	        		}
	        		catch(SecurityException sx){
	        	    	Log.debug(debugmsg+"Runtime.exec(cmd /c appname, null, thedir) SecurityException: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
	        		}
	        		catch(Exception sx){
	        	    	Log.debug(debugmsg+"Runtime.exec(cmd /c appname, null, thedir) Exception: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
	        		}
	        	}
        	}
        	else{
            	Log.debug(debugmsg+"CMD /C IOException occurred and unrecoverable: "+ io.getClass().getSimpleName()+" "+ io.getMessage());
            	throw io;//caught below
        	}
        }
		Log.info(debugmsg+"checking success for "+ appname);
		ProcessCapture console = null;
		Thread thread = null;
		try{
			console = new ProcessCapture(p);
			thread = new Thread(console);
		}catch(Exception io){
	    	Log.debug(debugmsg+"Runtime.exec(cmd /c appname, null, thedir) preparing console thread Exception: "+ io.getClass().getSimpleName()+":"+ io.getMessage());
			throw new IllegalArgumentException("Specified application process may not be valid: "+ appname);
		}
		thread.setDaemon(true);
		thread.start();
		int  loop = 0;
		long loopsleep = 750; //milliseconds
		int  loopmax = 3;
		int exitValue = 99;
		while(loop++ < loopmax){
	    	try {
				exitValue = p.exitValue();//throws IllegalStateException if still running
    		    p.destroy();
				Log.info(debugmsg+"Process exited with code '"+exitValue+"' for "+ appname);
				console.shutdown();
				Vector data = console.getData();
				String lf = "\n";
				StringBuffer message = new StringBuffer(lf);
				for(int line=0;line < data.size();line++) 
					message.append(data.get(line) +lf);
    		    rc.result = message.toString();
				if(exitValue!=0){
	    			Log.debug(debugmsg+"Process may not have terminated within timeout: "+ message.toString());
	    		    rc.rc = STAFResult.UnknownError;
	    		    return rc;
				}else{
	    		    rc.rc = STAFResult.Ok;
	    			Log.info(debugmsg+"Process seems to have exited normally for "+ appname);
	    			return rc;
				}
			} catch (IllegalThreadStateException e) {
				//if we got here, then the process is still running
				//we will check it up to loopmax to be sure all is well
    			Log.debug(debugmsg+"Process is still running...loop "+ loop);
			} catch(Exception e2){
    			Log.debug(debugmsg+"IGNORING Process or Thread "+ 
    					  e2.getClass().getSimpleName()+"; "+e2.getMessage());
    			break;
			}
			try{Thread.sleep(loopsleep);}catch(InterruptedException x){}
		}
		// might not want to shutdown a process.
		// the intention may be to run indefinitely?
		// for such threads, if we shutdown the console, it will likely deadlock on a full output buffer.
		console.shutdown();	
		Log.debug(debugmsg+"Process may not have terminated within timeout period!");
	    rc.rc = STAFResult.ProcessNotComplete;
	    rc.result = "Process may not have terminated within timeout period!";
	    return rc;
	}
}
