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
package org.safs.staf.service.logging;

import java.io.File;
import java.util.Enumeration;

import org.safs.logging.LogItemDictionary;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.ServiceDebugLog;
import org.safs.tools.CaseInsensitiveFile;

import com.ibm.staf.STAFResult;
import com.ibm.staf.STAFUtil;

/**
 * <code>LocalSLSLogFacility</code> provides a concrete implementation of
 * <code>SLSLogFacility</code> that is to be used by the SAFS logging service in
 * local mode.
 * <p>
 * This class delefates its logging function to <code>STAFTextLogItem</code> and
 * <code>STAFXmlLogItem</code>.
 *
 * @see AbstractSTAFTextLogItem
 * @see AbstractSTAFXmlLogItem
 *
 * @since 	MAY 19 2009		(LW)	Modify the constructor, add an extra parameter 'ServiceDebugLog'.
 */
public class LocalSLSLogFacility extends SLSLogFacility
{
	private String customService;
	protected boolean overwrite = false;
	/**
	 * Creates a <code>LocalSLSLogFacility</code>.
	 * <p>
	 * This method initilaizes enabled STAF log items and creates and starts the
	 * worker thread.
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
	 * @param csvc		the name of the custom logging service;
	 * 					<code>null</code> if custom logging is to be disabled.
	 *  @param debugLog	is used to write debug message to a file
	 */
	public LocalSLSLogFacility(String name, long mode, int level, String linked,
		HandleInterface h, String dir, LogItemDictionary logs, String csvc,ServiceDebugLog debugLog)
		throws STAFLogException
	{
		super(name, mode, level, linked, h, dir, logs,debugLog);
		customService = csvc;
		initializeFac();
	}

	private void initializeFac() throws STAFLogException{
		// validate the destination of enabled logs
		for (Enumeration e = allLogs.items(); e.hasMoreElements(); )
		{
			STAFFileLogItem li = (STAFFileLogItem)e.nextElement();
			if (li.enabled && li.fileExists()){
				if(!overwrite){
				    throw new STAFLogException("",
					new STAFResult(STAFResult.AlreadyExists,
						li.getAbsolutePath()));
				}else{
					boolean deleted = false;
					try{
						File afile = new CaseInsensitiveFile(li.getAbsolutePath()).toFile();
						deleted = afile.delete();
					}
					catch(Exception iox){
     				    throw new STAFLogException("",
					    new STAFResult(STAFResult.AlreadyExists,
						li.getAbsolutePath()));
					}
					if(!deleted) throw new STAFLogException("",
					    new STAFResult(STAFResult.AlreadyExists,
						li.getAbsolutePath()));
				}
			}
		}

		// initialize enabled logs.
		for ( Enumeration e = allLogs.items(); e.hasMoreElements(); )
		{
			STAFFileLogItem li = (STAFFileLogItem)e.nextElement();
			if (li.enabled) li.init();
		}

		// do custom init if necessary
		if (customService != null)
		{
			String request = "init " + STAFUtil.wrapData(facName) + " mode " +
				logMode;
			if (linkedFac != null && linkedFac.length() > 0)
				request += " linkedfac " + STAFUtil.wrapData(linkedFac);

			STAFResult result = handle.submit2("local", customService, request);
			debugLog.debugPrintln("LocalSLSLogFacility.ctor(): " +
				"custom init", result);
		}

		workerThread = new LocalWorkerThread();
		workerThread.start();
	}

	/**
	 * Creates a <code>LocalSLSLogFacility</code>.
	 * <p>
	 * This method initilaizes enabled STAF log items and creates and starts the
	 * worker thread.  It allows you to enable overwriting any previously existing
	 * log file instead of aborting.  Overwriting is disabled (false) by default.
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
	 * @param csvc		the name of the custom logging service;
	 * 					<code>null</code> if custom logging is to be disabled.
	 * @param overwrite	true to delete/overwrite any previous log.
	 * @param debugLog	is used to write debug message to a file
	 */
	public LocalSLSLogFacility(String name, long mode, int level, String linked,
		HandleInterface h, String dir, LogItemDictionary logs, String csvc, boolean overwrite,ServiceDebugLog debugLog)
		throws STAFLogException
	{
		super(name, mode, level, linked, h, dir, logs,debugLog);
		customService = csvc;
		this.overwrite = overwrite;
		initializeFac();
	}


	/**
	 * Closes all logs immediately.
	 * <p>
	 * @throws	STAFLogException
	 * 			if a log failed to close for any reason.
	 */
	@Override
	public void closeNow() throws STAFLogException
	{
		debugLog.debugPrintln("LocalSLSLogFacility.closeNow()");

		// do custom close if necessary
		if (customService != null)
		{
			STAFResult result = handle.submit2("local", customService,
				"close " + STAFUtil.wrapData(facName));
			debugLog.debugPrintln("LocalSLSLogFacility.closeNow(): " +
				"custom close", result);
		}

		// closes all STAF log items
		for (Enumeration e = allLogs.items(); e.hasMoreElements(); )
		{
			STAFFileLogItem li = (STAFFileLogItem) e.nextElement();
			li.close();
		}
	}

	/**
	 * This worker thread fulfills a log request by invoking the logging
	 * functions of the STAF log items.
	 */
	private class LocalWorkerThread extends WorkerThread
	{
		public LocalWorkerThread() {}

		/**
		 * Performs the log request by invoking the <code>logMessage</code>
		 * method on each STAF log item of this log facility.
		 * <p>
		 * @param r		the LOG request from the queue.
		 */
		@Override
		protected void log(WorkerRequest r)
		{
			// do custom logging if necessary
			if (customService != null)
			{
				String request = "logmessage " + STAFUtil.wrapData(facName) +
					" message " + STAFUtil.wrapData(r.msg) +
					" msgtype " + r.msgType;
				if (r.desc != null && r.desc.length() > 0)
					request += " description " + STAFUtil.wrapData(r.desc);

				STAFResult result = handle.submit2("local", customService,
					request);

				debugLog.debugPrintln(
					"LocalSLSLogFacility$LocalWorkerThread().log: " +
					"custom logging", result);

				// bypass normal logging?
				if (result.rc == STAFResult.Ok &&
					result.result.equalsIgnoreCase("bypass")) return;
			}

			for (Enumeration e = allLogs.items() ; e.hasMoreElements(); )
			{
				STAFFileLogItem li = (STAFFileLogItem) e.nextElement();
				li.logMessage(r.msg, r.desc, r.msgType);
			}
		}
	} // end of class LocalWorkerThread

}

