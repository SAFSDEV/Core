	package org.safs.rational.logging;

    import org.safs.STAFHelper;
    import org.safs.rational.Script;
	import org.safs.logging.*;
	
	import com.rational.test.ft.script.*;
	import com.ibm.staf.*;
	

	/************************************************************************
	 * RLogUtilities extends org.safs.logging.LogUtilities and provides functionality
	 * to log to RobotJ logs and console.
	 ************************************************************************/
	public class RLogUtilities extends LogUtilities {

		//Executing test script from RobotJ	
		Script script;

		public RLogUtilities(){
			super();
		}

		/**
		 * RLogUtilities currently accepts a Script object as its parameter
		 * which requires the running script or hook script to extend the
		 * org.safs.rational.Script class in order to be able to log using
		 * RLogUtilities
		 **/
		public RLogUtilities(STAFHelper helper, Script script) {
			super(helper);
			this.script = script;
		}
		
		/**
		 * logs to robotJ Log via the our Script
		 *
		 * @param messageType int type of to be logged
		 * @param formattedMessage String the message to be logged
		 * @param description String optional description about the message
		 *         being logged
		 **/
		public void toolLog(
			int messageType,
			String formattedMessage,
			String description) {

			/*to determine what type of log it will be (logInfo, logError, logTestResult)*/
			switch (messageType) {

	            case AbstractLogFacility.PASSED_MESSAGE:

					if (description == null || description.equals(""))
							script.localLogTestResult(formattedMessage, true);
					else
							script.localLogTestResult(formattedMessage, true, description);
					break;

	            case AbstractLogFacility.WARNING_MESSAGE:

					//script.logWarning(formattedMessage);
					if (description == null || description.equals(""))
							script.localLogWarning(formattedMessage);
					else
							script.localLogWarning(formattedMessage +"\n"+ 
							                  MessageTypeInfo.GENERIC_MESSAGE_PREFIX +
							                  description);
					break;

	            case AbstractLogFacility.FAILED_MESSAGE:

					//script.logError(formattedMessage);
					if (description == null || description.equals(""))
							script.localLogTestResult(formattedMessage, false);
					else
							script.localLogTestResult(formattedMessage, false, description);
					break;
				
				default:

					//script.logInfo(formattedMessage);
					if (description == null || description.equals(""))
							script.localLogInfo(formattedMessage);
					else
							script.localLogInfo(formattedMessage +"\n"+ 
							                  MessageTypeInfo.GENERIC_MESSAGE_PREFIX +
							                  description);
					break;

			}
		}
		
		/**
		 * logs to robotJ Console 
		 *
		 * @param formattedMessage String the message to be logged
		 * @param description String optional description about the message
		 *         being logged
		 **/
		public void consoleLog(
			String formattedMessage,
			String description) {

			if (description == null || description.equals(""))
				System.out.println(formattedMessage);
			else {
				System.out.println(formattedMessage);
				System.out.println(MessageTypeInfo.GENERIC_MESSAGE_PREFIX +
					               description);
			}
		}
	}