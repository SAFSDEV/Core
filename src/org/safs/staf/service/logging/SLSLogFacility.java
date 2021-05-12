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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;

import org.safs.STAFHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.FileLogItem;
import org.safs.logging.LogItem;
import org.safs.logging.LogItemDictionary;
import org.safs.logging.MessageTypeInfo;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.ServiceDebugLog;

/**
 * <code>SLSLogFacility</code> is the abstract log facility solely used by
 * <code>SAFSLoggingService</code>.
 * <p>
 * In addition to common attributes defined in its super class,
 * <code>AbstractLogFacility</code>, this log facility contains a STAF
 * handle, a default log directory, and a pair of
 * <code>{@link AbstractSTAFTextLogItem}</code> and <code>{@link AbstractSTAFXmlLogItem}</code>
 * for standard SAFS text/xml logs. The STAF handle is used to interact with
 * STAF. The default log directory corresponds to the <code>DIR</code> setting
 * of the SAFS logging service.
 * <p>
 * Internally, this class performs logging functions asynchronously to maximize
 * its performance. The main thread accepts log message requests (i.e. calls to
 * the {@link #logMessage logMessage} method) and puts them on a request queue.
 * A worker thread is expected to check the queue for any request and performs
 * actual logging actions.
 * <p>
 * Depending on the mode (local or remote) of the logging service, the actual
 * logging and closing implementations would be different. Therefore the
 * {@link #closeNow} method and the {@link WorkerThread} inner class, which does
 * the actual logging, are abstract and expected to be implemented by
 * subclasses.
 *
 * @since 	MAY 19 2009		(LW)	Modify the constructor, add an extra parameter 'ServiceDebugLog'.
 *									Modify method validateLogFiles(): use java reflection to instantiate the STAF-version LogItem
 * 									Add method getSTAFTextLogItem(),getSTAFTextLogItem and getNewInstance() to help instantiate a LogItem.
 */
public abstract class SLSLogFacility extends AbstractLogFacility
{
	public static String STAF_TEXT_LOG_ITEM_V2 = "org.safs.staf.service.logging.v2.STAFTextLogItem";
	public static String STAF_XML_LOG_ITEM_V2 = "org.safs.staf.service.logging.v2.STAFXmlLogItem";
	public static String STAF_TEXT_LOG_ITEM_V3 = "org.safs.staf.service.logging.v3.STAFTextLogItem3";
	public static String STAF_XML_LOG_ITEM_V3 = "org.safs.staf.service.logging.v3.STAFXmlLogItem3";

	protected HandleInterface handle;
	protected String defaultDir;
	protected LogItemDictionary allLogs;

	protected RequestQueue rQueue;
	protected WorkerThread workerThread;
	protected ServiceDebugLog debugLog;

	/**
	 * Creates a <code>SLSLogFacility</code>.
	 * <p>
	 * @param name		the name of this log facility.
	 * @param mode      the log mode.
	 * @param level  	the log level for this log facility.
	 * @param linked	the name of another log facility linked to this one.
	 * @param h			a STAF handle to interact with STAF.
	 * @param dir		the default log directory.
	 * @param logs		the spec of a <code>STAFTextLogItem</code> and
	 * 					<code>STAFXmlLogItem</code>. If a log item is omitted,
	 *                  default settings (file name, parent directory etc) will
	 *                  be used.
	 * @param debugLog	is used to write debug message to a file
	 */
	public SLSLogFacility(String name, long mode, int level, String linked,
		HandleInterface h, String dir, LogItemDictionary logs, ServiceDebugLog debugLog)
	{
		super(name, mode, level, linked);
		handle = h;
		defaultDir = dir;
		allLogs = new LogItemDictionary();
		this.debugLog = debugLog;

		// populate the internal collection of log items.
		validateLogFiles(logs);

		rQueue = new RequestQueue();
	}

	/**
	 * Sets user-specified STAF log items on this log facility.
	 * <p>
	 * Log item not provided is populated with default values.
	 * <p>
	 * @param logs	the spec of a <code>STAFTextLogItem</code> and
	 * 				<code>STAFXmlLogItem</code>. If a log item is omitted,
	 *				default settings (file name, parent directory etc) will be
	 *              used.
	 */
	private void validateLogFiles(LogItemDictionary logs)
	{
		if (logs.contains(LOGMODE_SAFS_TEXT))
			allLogs.put(logs.get(LOGMODE_SAFS_TEXT));
		else{
			AbstractSTAFTextLogItem item = getSTAFTextLogItem(facName + ".txt", defaultDir,DEFAULT_SAFS_TEXT_FILE);
			if(item != null){
				item.setDebugLog(this.debugLog);
				allLogs.put(item);
			}
		}

		if (logs.contains(LOGMODE_SAFS_XML))
			allLogs.put(logs.get(LOGMODE_SAFS_XML));
		else{
			AbstractSTAFXmlLogItem item = getSTAFXmlLogItem(facName + ".xml", defaultDir,DEFAULT_SAFS_XML_FILE);
			if(item!=null){
				item.setDebugLog(this.debugLog);
				allLogs.put(item);
			}
		}

		// update the state of all logs
		setLogMode(logMode);
		setLogLevel(logLevel);
		setDefaultDir(defaultDir);
		setHandle(handle);
	}

	/*
	 * According to the version of STAF, get a new instance of STAFTextLogItem or STAFTextLogItem3
	 */
	@SuppressWarnings("unchecked")
	private AbstractSTAFTextLogItem getSTAFTextLogItem(String name,String directory,String filename){
		Class itemClass = null;
		String debugmsg = getClass().getName()+".getSTAFTextLogItem(): ";
		String stafVersion = STAFHelper.getSTAFVersionString(handle);
		debugLog.debugPrintln(debugmsg+" STAF version is "+stafVersion);

		try {
			if (stafVersion.startsWith("2")) {
				itemClass = Class.forName(STAF_TEXT_LOG_ITEM_V2);
			} else if (stafVersion.startsWith("3")) {
				itemClass = Class.forName(STAF_TEXT_LOG_ITEM_V3);
			} else {
				debugLog.debugPrintln(debugmsg+ " staf version is neither 2 nor 3. Can not instanciate a STAFTextLogItem.");
				return null;
			}
		} catch (ClassNotFoundException e) {
			debugLog.debugPrintln(debugmsg+ e.getMessage());
			return null;
		}

		if(itemClass==null){
			debugLog.debugPrintln(debugmsg+" itemClass is null.");
			return null;
		}
		Object logItem = this.getNewInstance(itemClass, name, directory, filename);
		if(logItem instanceof AbstractSTAFTextLogItem){
			return (AbstractSTAFTextLogItem) logItem;
		}else{
			debugLog.debugPrintln(debugmsg+" logItem is not a AbstractSTAFTextLogItem.");
			return null;
		}
	}

	/*
	 * According to the version of STAF, get a new instance of STAFXmlLogItem or STAFXmlLogItem3
	 */
	@SuppressWarnings("unchecked")
	private AbstractSTAFXmlLogItem getSTAFXmlLogItem(String name,String directory,String filename){
		Class itemClass = null;
		String debugmsg = getClass().getName()+".getSTAFXmlLogItem(): ";
		String stafVersion = STAFHelper.getSTAFVersionString(handle);
		debugLog.debugPrintln(debugmsg+" STAF version is "+stafVersion);

		try {
			if (stafVersion.startsWith("2")) {
				itemClass = Class.forName(STAF_XML_LOG_ITEM_V2);
			} else if (stafVersion.startsWith("3")) {
				itemClass = Class.forName(STAF_XML_LOG_ITEM_V3);
			} else {
				debugLog.debugPrintln(debugmsg+ " staf version is neither 2 nor 3. Can not instanciate a STAFXmlLogItem.");
				return null;
			}
		} catch (ClassNotFoundException e) {
			debugLog.debugPrintln(debugmsg+ e.getMessage());
			return null;
		}

		if(itemClass==null){
			debugLog.debugPrintln(debugmsg+" itemClass is null.");
			return null;
		}
		Object logItem = this.getNewInstance(itemClass, name, directory, filename);
		if(logItem instanceof AbstractSTAFXmlLogItem){
			return (AbstractSTAFXmlLogItem) logItem;
		}else{
			debugLog.debugPrintln(debugmsg+" logItem is not a AbstractSTAFXmlLogItem.");
			return null;
		}
	}

	/**
	 * @param clazz			the Class of STAFTextLogItem or STAFTextLogItem3 or STAFXmlLogItem or STAFXmlLogItem3
	 * @param name
	 * @param directory
	 * @param filename
	 * @return				an new instance of clazz by its constructor clazz(String,String,String)
	 */
	@SuppressWarnings("unchecked")
	private Object getNewInstance(Class clazz,String name,String directory,String filename){
		String debugmsg = getClass().getName()+".getNewInstance(): ";
		Class parameterTypes[] = new Class[3];
		parameterTypes[0] = String.class;//name
		parameterTypes[1] = String.class;//directory
		parameterTypes[2] = String.class;//filename

		Constructor constructor = null;
		try {
			constructor = clazz.getConstructor(parameterTypes);
		} catch (Exception e) {
			debugLog.debugPrintln(debugmsg+e.getMessage());
			return null;
		}

		Object initargs[] = new Object[3];
		initargs[0] = name;
		initargs[1] = directory;
		initargs[2] = filename;
		Object aNewInstance = null;

		try {
			aNewInstance = constructor.newInstance(initargs);
		} catch (Exception e) {
			debugLog.debugPrintln(debugmsg+e.getMessage());
			return null;
		}

		return aNewInstance;
	}

	/**
	 * Sets the log mode of this log facility.
	 * <p>
	 * This method also sets the enabled state of each <code>LogItem</code>
	 * accordingly.
	 * <p>
	 * @param mode	the new log mode. Bitwise-OR of one or more
	 * 				<code>LOGMODE</code> constants.
	 */
	@Override
	public void setLogMode(long mode)
	{
		super.setLogMode(mode);
		for (Enumeration e = allLogs.items() ; e.hasMoreElements(); )
		{
			LogItem li = (LogItem) e.nextElement();
			if (isModeEnabled(li.mode)) li.enabled = true;
		}
	}

	/**
	 * Sets the log level of this log facility.
	 * <p>
	 * This method also sets the log level of each <code>LogItem</code>.
	 * <p>
	 * @param level	the new log level. Must be one of the <code>LOGLEVEL</code>
	 * 				constants.
	 */
	@Override
	public void setLogLevel(int level)
	{
		super.setLogLevel(level);
		for (Enumeration e = allLogs.items() ; e.hasMoreElements(); )
		{
			((LogItem) e.nextElement()).level = logLevel;
		}
	}

	/**
	 * Sets the default log directory of this log facility.
	 * <p>
	 * Parent directory of each <code>FileLogItem</code> is set to the new value
	 * as well.
	 * <p>
	 * @param dir	the new default directory.
	 */
	public void setDefaultDir(String dir)
	{
		defaultDir = dir;
		for (Enumeration e = allLogs.items() ; e.hasMoreElements(); )
		{
			Object li = e.nextElement();
			if (li instanceof FileLogItem) ((FileLogItem)li).setParentDir(dir);
		}
	}

	/**
	 * Sets the STAF handle for this log facility.
	 * <p>
	 * STAF handle of each <code>STAFFileLogItem</code> is set to the new value
	 * as well.
	 * <p>
	 * @param h		the new STAF handle.
	 */
	public void setHandle(HandleInterface h)
	{
		handle = h;
		for (Enumeration e = allLogs.items() ; e.hasMoreElements(); )
		{
			Object li = e.nextElement();
			if (li instanceof STAFFileLogItem)
				((STAFFileLogItem)li).setHandle(h);
		}
	}

	/**
	 * Returns the string representation of the current state of
	 * <code>STAFTextLogItem</code>.
	 * <p>
	 * @return the name, path, and enabled flag of the log item.
	 */
	public String getSAFSTextLogStateString()
	{
		AbstractSTAFTextLogItem log = (AbstractSTAFTextLogItem) allLogs.get(LOGMODE_SAFS_TEXT);
		return
			"  TEXTLOG Name:       " + log.name + "\n" +
			"  TEXTLOG Path:       " + log.getAbsolutePath() + "\n" +
			"  TEXTLOG Enabled:    " + isModeEnabled(LOGMODE_SAFS_TEXT) + "\n";
	}

	/**
	 * Returns the string representation of the current state of
	 * <code>STAFXmlLogItem</code>.
	 * <p>
	 * @return the name, path, and enabled flag of the log item.
	 */
	public String getSAFSXmlLogStateString()
	{
		AbstractSTAFXmlLogItem log = (AbstractSTAFXmlLogItem) allLogs.get(LOGMODE_SAFS_XML);
		return
			"  XMLLOG Name:        " + log.name + "\n" +
			"  XMLLOG Path:        " + log.getAbsolutePath() + "\n" +
			"  XMLLOG Enabled:     " + isModeEnabled(LOGMODE_SAFS_XML) + "\n";
	}

	/**
	 * Set the capXML setting for the XML log.
	 * @param capbool true to force XML capping on closure.
	 */
	public void setSAFSXmlLogCapXML(boolean capbool){
		AbstractSTAFXmlLogItem log = (AbstractSTAFXmlLogItem) allLogs.get(LOGMODE_SAFS_XML);
		if (log != null){
			log.setCapXML(capbool);
		}
	}


	/**
	 * Returns the string representation of the current state of the tool log.
	 * <p>
	 * @return the enabled state of the tool log.
	 */
	public String getToolLogStateString()
	{
		return "  TOOLLOG Enabled:    " + isModeEnabled(LOGMODE_TOOL) + "\n";
	}

	/**
	 * Returns the string representation of the current state of the console
	 * log.
	 * <p>
	 * @return the enabled state of the console log.
	 */
	public String getConsoleLogStateString()
	{
		return "  CONSOLELOG Enabled: " + isModeEnabled(LOGMODE_CONSOLE) + "\n";
	}

	/**
	 * Returns the string representation of the states of this log facility.
	 * <p>
	 * This string is included in the result buffer of every LOGMESSAGE request
	 * to let the user of the logging service, such as a SAFS-aware testing
	 * tool, know the current state of this log facility, so that it could
	 * perform tool-specific logging actions accordingly.
	 * <p>
	 * @return	the states of this log facility. Use
	 * 			<code>SLSLogFacilityStates</code> to parse the string.
	 */
	public String getStatesString()
	{
		boolean toollog = isModeEnabled(LOGMODE_TOOL) && !suspended;
		boolean consolelog = isModeEnabled(LOGMODE_CONSOLE) && !suspended;
		return
			AbstractSAFSLoggingService.SLS_STATES_TOOLLOG_PREFIX + toollog + "\n"+
			AbstractSAFSLoggingService.SLS_STATES_CONSOLELOG_PREFIX + consolelog + "\n"+
			AbstractSAFSLoggingService.SLS_STATES_LOGLEVEL_PREFIX + logLevel;
	}

	/**
	 * Logs a message.
	 * <p>
	 * If this log facility is not suspended, and the message type is not
	 * filtered out by the current log level, this method creates a log request
	 * and puts it on the request queue for the worker thread to process.
	 * <p>
	 * @param msg		the message to log.
	 * @param desc		additional description to log.
	 * @param msgType	the int identifier of the type of message being logged.
	 */
	@Override
	public void logMessage(String msg,	String desc, int msgType)
	{
		debugLog.debugPrintln(
			"SLSLogFacility.logMessage(): msg=" + msg + ";desc=" + desc +
			";msgType=" + msgType);

		if (suspended) return;
		if (!MessageTypeInfo.typeBelongsToLevel(msgType, logLevel)) return;

		rQueue.queue(new WorkerRequest(WorkerRequest.CMD_LOG, msg, desc,
			msgType));
		if (workerThread.isAlive()) workerThread.interrupt(); // wake up!
	}

	/**
	 * Closes this log facility.
	 * <p>
	 * This method waits for the request queue to clear before actually shuts
	 * down the log facility. It also signals the worker thread to exit (and
	 * wait until it acutally dies) before return.
	 * <p>
	 * @throws	STAFLogException
	 * 			if this method failed for any reason.
	 */
	@Override
	public void close() throws STAFLogException
	{
		debugLog.debugPrintln("SLSLogFacility.close()");

		// wait for the worker thread to clear the queue (i.e. to complete all
		// the logging requests) before proceeding.
		while (workerThread.isAlive() && !rQueue.isEmpty())
		{
			try { Thread.sleep(100); }
			catch (InterruptedException e) {}
		}

		// closes associated log(s) immediately
		closeNow();

		// if closed successfully, queue an EXIT command to signal the worker
		// thread to exit.
		rQueue.queue(new WorkerRequest(WorkerRequest.CMD_EXIT));
		if (workerThread.isAlive()) workerThread.interrupt(); // wake up!

		// wait util the thread is actually dead.
		while (workerThread.isAlive())
		{
			try { Thread.sleep(100); }
			catch (InterruptedException e) {}
		}
	}

	/**
	 * Closes this log facility immediately.
	 * <p>
	 * This method is called by the <code>close</code> method to actually shut
	 * down this log facility.
	 * <p>
	 * @throws	STAFLogException
	 * 			if failed to close this log facility for any reason.
	 */
	protected abstract void closeNow() throws STAFLogException;

	/**
	 * This class is the queued request for the worker thread of this log
	 * facility.
	 * <p>
	 * A request contains a command identifier, which tells the worker thread
	 * what action to perform, and message specifications if the command is to
	 * log a message.
	 */
	protected class WorkerRequest
	{
		/**
		 * Instructs the worker thread to exit its <code>run</code> method.
		 */
		public static final int CMD_EXIT = 0;
		/**
		 * Instructs the worker thread to log a message.
		 */
		public static final int CMD_LOG = 1;

		/**
		 * Command identifier of this request. One of the <code>CMD</code>
		 * constants.
		 */
		public int cmd;
		/**
		 * The message to log.
		 */
		public String msg;
		/**
		 * Additional description to log.
		 */
		public String desc;
		/**
		 * The message type.
		 */
		public int msgType;

		/**
		 * Creates a <code>WorkerRequest</code>
		 * <p>
		 * @param c		the command identifier. One of the <code>CMD</code>
		 * 				constants.
		 * @param m		the message to log.
		 * @param d		additional description to log.
		 * @param t		the message type.
		 */
		public WorkerRequest(int c, String m, String d, int mt)
		{
			cmd = c;
			msg = (m == null)? "" : m;
			desc = d;
			msgType = mt;
		}

		/**
		 * Creates a <code>WorkerRequest</code> without message spec.
		 * <p>
		 * This constructor is used to create non-LOG request.
		 * <p>
		 * @param c		the command identifier. One of the <code>CMD</code>
		 * 				constants.
		 */
		public WorkerRequest(int c)
		{
			this(c, "", null, 0);
		}
	} // end of class WorkerRequest

	/**
	 * This class implements the (FIFO) request queue used internally by this
	 * log facility. This class is thread-safe.
	 */
	protected class RequestQueue
	{
		/**
		 * Stores all requests for the worker thread.
		 */
		private ArrayList requests;

		/**
		 * Creates an empty request queue.
		 */
		public RequestQueue()
		{
			requests = new ArrayList();
		}

		/**
		 * Adds a new request on this queue.
		 * <p>
		 * @param r		the <code>WorkerRequest</code> to queue.
		 */
		public synchronized void queue(SLSLogFacility.WorkerRequest r)
		{
			requests.add(r);
		}

		/**
		 * Removes and returns the first request in this queue.
		 * <p>
		 * @return	the first <code>WorkerRequest</code> in this queue;
		 * 			<code>null</code> if queue is empty.
		 */
		public synchronized SLSLogFacility.WorkerRequest get()
		{
			if (requests.size() <= 0) return null;
			return (WorkerRequest)requests.remove(0);
		}

		/**
		 * Returns the first request in this queue without removing it.
		 * <p>
		 * @return	the first <code>WorkerRequest</code> in this queue;
		 * 			<code>null</code> if queue is empty.
		 */
		public synchronized SLSLogFacility.WorkerRequest peek()
		{
			if (requests.size() <= 0) return null;
			return (WorkerRequest)requests.get(0);
		}

		/**
		 * Returns the size of this queue.
		 * <p>
		 * @return	the number of requests in this queue.
		 */
		public synchronized int size()
		{
			return requests.size();
		}

		/**
		 * Tests if this queue is empty.
		 * <p>
		 * @return	<code>true</code> if queue is empty; <code>false</code>
		 * 			otherwise.
		 */
		public synchronized boolean isEmpty()
		{
			return (requests.size() <= 0);
		}

		/**
		 * Clears this queue.
		 */
		public synchronized void clear()
		{
			requests.clear();
		}
	} // end of class RequestQueue

	/**
	 * This is the worker thread that fulfills the log message request received
	 * by this log facility. This thread constantly checks the request queue for
	 * incoming log message requests and carries them out. It only exits the
	 * <code>run</code> method if an EXIT request is found on the queue.
	 * <p>
	 * Subclass of <code>SLSLogFacility</code> should derive its worker thread
	 * class from this class and implement the {@link #log log} method.
	 */
	protected abstract class WorkerThread extends Thread
	{
		public WorkerThread() {}

		/**
		 * Runs in a indefinite loop checking for queued request. The interval
		 * of the loop is acutally quite long. It assumes that the log facility
		 * will interrupt this thread when a new request is queued. This method
		 * exits when an EXIT request is found.
		 */
		@Override
		public void run()
		{
			boolean exit = false;
			while (!exit)
			{
				while (!rQueue.isEmpty())
				{
					// do not yet remove the request from the queue. we only
					// remove the request after processing it. this is because
					// the close method, when called in the main thread,
					// proceeds when the queue is empty. so if the request is
					// removed before it is fulfilled, it is possible that the
					// close method ends up coming through first, resulting in
					// out-of-order log messages at the end of the log file.
					WorkerRequest r = rQueue.peek();
					switch (r.cmd)
					{
						case WorkerRequest.CMD_LOG:
							debugLog.debugPrintln(
								"SLSLogFacility$WorkerThread.run(): CMD_LOG");
							log(r);
							break;
						case WorkerRequest.CMD_EXIT:
							debugLog.debugPrintln(
								"SLSLogFacility$WorkerThread.run(): CMD_EXIT");

							//should log to the STAFJVM log in STAF\data\lang\java\jvm
							System.out.println("LogFacility EXIT command has been received...");

							exit = true;
							break;
					}
					// now we remove the request from the queue.
					rQueue.get();
					if (exit) break;
				}
				if (exit) break;
				// clear the interrupted state before going to sleep.
				// is this really necessary?
				// interrupted();
				// Carl Nagle 20130508 - removed interrupted() clearing for fear it is
				// causing service shutdown problems.

				// Carl Nagle -- too long of sleep may cause STAF/Service shutdown problems
				try { sleep(3000); }
				catch (InterruptedException e)
				{
					debugLog.debugPrintln(
						"SLSLogFacility$WorkerThread.run(): interrupted");
				}
			}
			debugLog.debugPrintln(
				"SLSLogFacility$WorkerThread.run(): return");
		}

		/**
		 * Fulfills the logging request.
		 * <p>
		 * @param r		the LOG request from the queue.
		 */
		protected abstract void log(WorkerRequest r);

	} // end of class WorkerThread

	protected void setDebugLog(ServiceDebugLog debugLog){
		this.debugLog = debugLog;
	}
}

