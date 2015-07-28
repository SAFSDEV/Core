/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.drivers;

import org.safs.JavaHook;
import org.safs.SAFSException;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.STAFHelper;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;

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
}
