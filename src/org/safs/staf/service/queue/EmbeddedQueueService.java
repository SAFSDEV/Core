/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.staf.service.queue;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.STAFHelper;
import org.safs.SingletonSTAFHelper;
import org.safs.StringUtils;
import org.safs.staf.embedded.EmbeddedHandle;
import org.safs.staf.embedded.EmbeddedHandles;
import org.safs.staf.embedded.EmbeddedServiceHandle;
import org.safs.staf.embedded.ServiceInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.InfoInterface.RequestInfo;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;

/**
 * 
 * <br>
 * History:<br>
 * 
 *  <br>   Jul 17, 2014    (sbjlwa) Initial release.
 */
public class EmbeddedQueueService implements ServiceInterface {

	public static final String DEFAULT_HANDLE_ID    = "STAF/Service/QUEUE";
	public static final String DEFAULT_SERVICE_NAME = "QUEUE";

	public static final String COMMAND_QUEUE 	= "QUEUE";
	public static final String COMMAND_GET 		= "GET";

	public static final String OPTION_NAME   	= "NAME";
	public static final String OPTION_WAIT   	= "WAIT";
	public static final String OPTION_MESSAGE   = "MESSAGE";

	protected EmbeddedServiceHandle fHandle;
	protected String servicename = DEFAULT_SERVICE_NAME;

	protected String handleID = DEFAULT_HANDLE_ID;

	protected STAFCommandParser parser = new STAFCommandParser();
	
	public STAFResult init(InfoInterface.InitInfo initInfo)
	{
		this.servicename = initInfo.name;
		
		parser.addOption(COMMAND_QUEUE, 1, STAFCommandParser.VALUEALLOWED);
		parser.addOption(COMMAND_GET, 1, STAFCommandParser.VALUEALLOWED);
		
		parser.addOption( OPTION_NAME, 1, STAFCommandParser.VALUEALLOWED );
		parser.addOption( OPTION_WAIT, 1, STAFCommandParser.VALUEALLOWED );
		parser.addOption( OPTION_MESSAGE, 1, STAFCommandParser.VALUEREQUIRED );
		
		parser.addOptionGroup(COMMAND_QUEUE+" "+COMMAND_GET, 1, 1);
		
		parser.addOptionNeed(OPTION_MESSAGE, COMMAND_QUEUE);
		
		try{
			debug("EmbeddedQueueService.init() registering handle '"+ handleID +"' for service '"+ servicename +"'");
			fHandle = new EmbeddedServiceHandle(handleID, servicename, this);
			fHandle.register();
			
		}catch(STAFException ignore){ 
			debug("EmbeddedQueueService.init() "+StringUtils.debugmsg(ignore));
		}
		return new STAFResult(STAFResult.Ok);
	}
	
	@Override
	public STAFResult acceptRequest(RequestInfo info) {
		debug("EmbeddedQueueService.acceptRequest() from "+ info.handleName +", "+ info.request);
		STAFCommandParseResult parsedData = parser.parse(info.request);
		if (parsedData.rc != STAFResult.Ok) {
			return new STAFResult(STAFResult.InvalidRequestString, parsedData.errorBuffer);
		}
		
		if (parsedData.optionTimes(COMMAND_QUEUE) > 0) {
			return queue(parsedData);
		}else if (parsedData.optionTimes(COMMAND_GET) > 0) {
			return get(parsedData);
		}else{
    		debug("EmbeddedQueueService().acceptRequest unsupported request: "+ info.request);
			return new STAFResult(STAFResult.InvalidRequestString, info.request);
		}
	}
	
	public STAFResult queue(STAFCommandParseResult param){
		String handleName = handleID;
		EmbeddedHandle handle = null;
		String message = null;
		if(param.optionTimes(OPTION_MESSAGE)<0){
			return new STAFResult(STAFResult.InvalidParm);
		}else{
			message = param.optionValue(OPTION_MESSAGE);
		}
		
		try{
			if(param.optionTimes(OPTION_NAME)>0){
				handleName = param.optionValue(OPTION_NAME);
				if(handleName==null || handleName.trim().isEmpty()) handleName=handleID;
			}
			handle = EmbeddedHandles.getHandle(handleName);
			if(handle==null) throw new SAFSException("Can not get handle for name '"+handleName+"'");
		}catch(Exception e){
			return new STAFResult(STAFResult.InvalidParm, StringUtils.debugmsg(e));
		}
		
		return handle.acceptQueueMessage(message);
	}
	
	public STAFResult get(STAFCommandParseResult param){
		String handleName = handleID;
		EmbeddedHandle handle = null;
		String timeoutStr = null;
		int timeout = Integer.MIN_VALUE;
		try{
			if(param.optionTimes(OPTION_WAIT)>0){
				timeoutStr = param.optionValue(OPTION_WAIT);
				if(timeoutStr==null || timeoutStr.trim().isEmpty()) timeout = Fifo.TIMEOUT_WAIT_FOREVER;
				else timeout = Integer.parseInt(timeoutStr);
			}else{
				timeout = Fifo.TIMEOUT_NO_WAIT;
			}
		}catch(NumberFormatException nfe){
			return new STAFResult(STAFResult.InvalidParm);
		}
		
		try{
			if(param.optionTimes(OPTION_NAME)>0){
				handleName = param.optionValue(OPTION_NAME);
				if(handleName==null || handleName.trim().isEmpty()) handleName=handleID;
			}
			handle = EmbeddedHandles.getHandle(handleName);
			if(handle==null) throw new SAFSException("Can not get handle for name '"+handleName+"'");
		}catch(Exception e){
			return new STAFResult(STAFResult.InvalidParm, StringUtils.debugmsg(e));
		}
		
		
		return handle.getQueueMessage(timeout);
	}
	
	public STAFResult terminate(){
		debug("EmbeddedQueueService().terminate() unregistering service handle.");
		EmbeddedHandles.cleanQueues();
		fHandle.unRegister();
		return new STAFResult(STAFResult.Ok);
	}
	
	private static boolean DEBUG_ENABLE = false;
	private static void debug(String message){
		if(DEBUG_ENABLE) System.out.println(message);
	}
	
	public String getHandleID(){
		return handleID;
	}
	
	/**
	 * This is used to test the queue service. Send 10 messages to Default_Queue, 10 messages to<br>
	 * queue 'MyQueue1' and 5 messages to queue 'MyQueue2'. Finally get these queued messages one by one.<br>
	 */
	public static void main(String[] args){
		//only no_staf_handles is true helper.initialize() will succeed without STAF
		STAFHelper.no_staf_handles = true;
		STAFHelper.startEmbeddedQueueService();
		
		final STAFHelper helper =  SingletonSTAFHelper.getHelper();
		try {
			helper.initialize(Log.SAFS_TESTLOG_PROCESS);
		} catch (SAFSSTAFRegistrationException e) {
			e.printStackTrace();
		}
		
		Fifo.DEBUG_ENABLE = true;
		String handle0 = null;
		String handle1 = DEFAULT_HANDLE_ID;
		String handle2 = DEFAULT_HANDLE_ID;
		@SuppressWarnings("unused")
		String queueMsg = null;
		int timeout = 1000;
//		int timeout = Fifo.TIMEOUT_WAIT_FOREVER;
		
		for(int i=0;i<10;i++){
			new Thread(new SendMessageThread(helper, handle0, i)).start();
		}
		for(int i=0;i<10;i++){
			new Thread(new SendMessageThread(helper, handle1, i)).start();
		}
		for(int i=0;i<5;i++){
			new Thread(new SendMessageThread(helper, handle2, i)).start();
		}
		
		while(true){
			try {
				queueMsg = helper.getQueueMessage(handle0, timeout);
			} catch (SAFSException e) {
				e.printStackTrace();
				break;
			}
		}
		while(true){
			try {
				queueMsg = helper.getQueueMessage(handle1, timeout);
			} catch (SAFSException e) {
				e.printStackTrace();
				break;
			}
		}
		while(true){
			try {
				queueMsg = helper.getQueueMessage(handle2, timeout);
			} catch (SAFSException e) {
				e.printStackTrace();
				break;
			}
		}

		STAFHelper.shutdownEmbeddedServices();
	}
	
	public static class SendMessageThread implements Runnable{
		private int i = 0;
		private STAFHelper helper = null;
		private String handleName = null;
		
		public SendMessageThread(STAFHelper helper, String handleName, int i){
			this.helper = helper;
			this.i = i;
			if(handleName==null && STAFHelper.queue!=null) this.handleName = STAFHelper.queue.getHandleID();
			else this.handleName = handleName;
		}

		@Override
		public void run() {
			String queueMsg = "messag "+i+" "+System.currentTimeMillis()+" from handle "+handleName;
//			debug("push queue message '"+queueMsg+"'");
			helper.sendQueueMessage(handleName, queueMsg);
		}
		
	}

	@Override
	public STAFResult terminateService() {
		return terminate();
	}
}
