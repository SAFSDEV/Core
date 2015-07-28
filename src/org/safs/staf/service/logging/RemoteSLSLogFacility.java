package org.safs.staf.service.logging;

import org.safs.logging.FileLogItem;
import org.safs.logging.LogItemDictionary;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.ServiceDebugLog;

import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;
import com.ibm.staf.STAFUtil;

/**
 * <code>RemoteSLSLogFacility</code> provides a concrete implementation of
 * <code>SLSLogFacility</code> that is to be used by the SAFS logging service in
 * remote mode.
 * <p>
 * This log facility serves as a proxy to the remote logging service running on
 * the remote machine. Log facility creation, logging, and closing are all
 * delegated to the remote logging service. Attributes of the remote log
 * facility are stored and retrieved locally, however, to maximize the 
 * performance of requests that query the state of a log facility. For the same 
 * reason, the <code>STAFTextLogItem</code> and <code>STAFXmlLogItem</code> that
 * this log facility holds are only place holders for their attributes. Their 
 * logging functions are not used by this log facility because they are only for
 * local logging mode.
 * 
 * @since 	MAY 19 2009		(LW)	Modify the constructor, add an extra parameter 'ServiceDebugLog'.
 */
public class RemoteSLSLogFacility extends SLSLogFacility
{
	private String remoteMachine;
	private String remoteService;
	protected boolean overwrite = false;
	
	/**
	 * Creates a <code>RemoteSLSLogFacility</code>.
	 * <p>
	 * This method creates a log facility on the remote machine by submitting
	 * the INIT request to the remote logging service.
	 * <p>
	 * @param name		the name of this log facility.
	 * @param mode		the log mode.
	 * @param level		the log level for this log facility.
	 * @param linked	the name of another log facility linked to this one.
	 * @param handle			a STAF handle to interact with STAF.
	 * @param dir		the default log directory. Overwrites the parent 
	 * 					directory attribute of all log items of this facility.
	 * @param logs		the spec of a <code>STAFTextLogItem</code> and
	 * 					<code>STAFXmlLogItem</code>. If a log item is omitted,
	 *                  default settings (file name, parent directory etc) will
	 *                  be used.
	 * @param machine	the name of the remote machine.
	 * @param service	the name of the remote logging service.
	 * @param debugLog	is used to write debug message to a file
	 */
	public RemoteSLSLogFacility(String name, long mode, int level, 
		String linked, HandleInterface handle, String dir, LogItemDictionary logs,
		String machine, String service,ServiceDebugLog debugLog)	throws STAFLogException
	{
		super(name, mode, level, linked, handle, dir, logs,debugLog);

		remoteMachine = machine;
		remoteService = service;
		initializeFac();
	}
	
	private void initializeFac() throws STAFLogException{
	
		// submit the init command to the remote logging service. if failed 
		// throw a STAFLogException
		STAFResult result = init();
		if (result.rc != STAFResult.Ok) 
			throw new STAFLogException("Remote INIT failed", result);

		workerThread = new RemoteWorkerThread();
		workerThread.start();
	}

	/**
	 * Creates a <code>RemoteSLSLogFacility</code>.
	 * <p>
	 * This method creates a log facility on the remote machine by submitting
	 * the INIT request to the remote logging service.  It allows you to enable 
	 * overwriting any previously existing log file instead of aborting.  
	 * Overwriting is disabled (false) by default.
	 * <p>
	 * @param name		the name of this log facility.
	 * @param mode		the log mode.
	 * @param level		the log level for this log facility.
	 * @param linked	the name of another log facility linked to this one.
	 * @param h			a STAF handle to interact with STAF.
	 * @param dir		the default log directory. Overwrites the parent 
	 * 					directory attribute of all log items of this facility.
	 * @param logs		the spec of a <code>STAFTextLogItem</code> and
	 * 					<code>STAFXmlLogItem</code>. If a log item is omitted,
	 *                  default settings (file name, parent directory etc) will
	 *                  be used.
	 * @param machine	the name of the remote machine.
	 * @param service	the name of the remote logging service.
	 * @param overwrite	true to delete/overwrite any previous log.
	 * @param debugLog	is used to write debug message to a file
	 */
	public RemoteSLSLogFacility(String name, long mode, int level, 
		String linked, HandleInterface h, String dir, LogItemDictionary logs,
		String machine, String service, boolean overwrite,ServiceDebugLog debugLog)	throws STAFLogException
	{
		super(name, mode, level, linked, h, dir, logs,debugLog);
		remoteMachine = machine;
		remoteService = service;
		this.overwrite = overwrite;
		initializeFac();
	}
	
	/**
	 * Creates the log facility on the remote machine by submitting a INIT
	 * request to the remote logging service.
	 * <p>
	 * @return	the <code>STAFResult</code> of the INIT request.
	 */
	private STAFResult init()
	{
		String option = "";
		if (isModeEnabled(LOGMODE_SAFS_TEXT))
		{
			FileLogItem li = (FileLogItem) allLogs.get(LOGMODE_SAFS_TEXT);
			option += " TEXTLOG " + STAFUtil.wrapData(li.getFileSpec());
		}
		if (isModeEnabled(LOGMODE_SAFS_XML))
		{
			FileLogItem li = (FileLogItem) allLogs.get(LOGMODE_SAFS_XML);
			option += " XMLLOG " + STAFUtil.wrapData(li.getFileSpec());
		}
		if (isModeEnabled(LOGMODE_TOOL)) option += " TOOLLOG";
		if (isModeEnabled(LOGMODE_CONSOLE)) option += " CONSOLELOG";

		if(overwrite) option += " OVERWRITE";
		
		STAFResult result = submit("init " + STAFUtil.wrapData(facName) + option);
		if (result.rc != STAFResult.Ok) return result;

		// set up the remote log to receive all messages. all settings including
		// log level are controlled by this proxy anyway.
		return submit("loglevel " + STAFUtil.wrapData(facName) + " debug");
	}

	/**
	 * Submits a request to the remote logging service running on the remote
	 * machine.
	 * <p>
	 * @param request	the SAFS logging service request to submit.
	 * @return			the <code>STAFResult</code> of the request.
	 */
	private STAFResult submit(String request)
	{
		debugLog.debugPrintln(
			"RemoteSLSLogFacility.submitRemote(): request=" + request);

		return handle.submit2(remoteMachine, remoteService, request);
	}

	/**
	 * Closes this log facility immediately by submitting a CLOSE request to
	 * the remote logging service.
	 * <p>
	 * @throws	STAFLogException
	 * 			if the remote CLOSE request failed.
	 */
	public void closeNow() throws STAFLogException
	{
		debugLog.debugPrintln("RemoteSLSLogFacility.closeNow()");

		// submit CLOSE request to the remote service.
		STAFResult result = submit("close " + STAFUtil.wrapData(facName));
		if (result.rc != STAFResult.Ok) 
			throw new STAFLogException("Remote CLOSE failed.", result);
	}

	/**
	 * This worker thread fulfills a log request by submitting a LOGMESSAGE
	 * request to the remote logging service.
	 */
	private class RemoteWorkerThread extends WorkerThread
	{
		public RemoteWorkerThread() {}

		/**
		 * Fulfills the log request by submitting a LOGMESSAGE request to the
		 * remote logging service.
		 * <p>
		 * @param r		the LOG request from the queue.
		 */
		protected void log(WorkerRequest r)
		{
			String option = " message " + STAFUtil.wrapData(r.msg);
			if (r.desc != null && r.desc.length() > 0)
				option += " description " + STAFUtil.wrapData(r.desc);
			option += " msgtype " + r.msgType;
			submit("logmessage " + STAFUtil.wrapData(facName) + option);
		}
	} // end of class RemoteWorkerThread

}