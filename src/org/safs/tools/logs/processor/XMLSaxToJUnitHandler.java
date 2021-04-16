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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2018-04-27    (Lei Wang) Initial release.
 * @date 2018-04-28    (Lei Wang) Used constants from SAFS_XML_LogConstants.
 *                                Used 'tagStack' instead of 'currentTag' to trace the tag being processed.
 * @date 2018-06-05    (Lei Wang) Handle property 'user', 'machine', 'ip' and 'testName' of tag LOG_OPENED.
 * @date 2018-06-07    (Lei Wang) Convert log message of type 'Orderable' to object Orderable.
 * @date 2018-06-13    (Lei Wang) Handled count unit (testcase, teststep).
 *                                We have 2 units, one is 'teststep' and the other is 'testcase'. The default is 'testcase'.
 *                                When the unit is 'teststep', the SAFS counter system will provide the count of 'failures', 'skipped', 'warning' etc.
 *                                When the unit is 'testcase', XMLSaxToJUnitHandler will count according to the message of type TESTLEVEL_FAILED, TESTLEVEL_ERRORED or TESTLEVEL_SKIPPED.
 * @date 2018-08-21    (Lei Wang) Modified handleLogMessage(): Fixed the junit counter, the 'tests' is the total tests including the 'failures', 'errors', 'skipped' and 'success'.
 */
package org.safs.tools.logs.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.safs.Constants;
import org.safs.Constants.SAFS_LogConstants;
import org.safs.Constants.SAFS_XML_LogConstants;
import org.safs.IndependantLog;
import org.safs.Utils;
import org.safs.data.model.Orderable;
import org.safs.logging.MessageTypeInfo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This is a XML SAX handler to process SAFS XML Log and convert it into a JUnit report.
 *
 * @author Lei Wang
 *
 */
public abstract class XMLSaxToJUnitHandler extends XMLSaxAbstractHandler{

	/** It will contain the string value of each Element. */
	protected StringBuilder value = null;

	/**
	 * The stack contains the tag currently being processed.<br>
	 * The tag will be pushed into the stack at the end of {@link #startElement(String, String, String, Attributes)}, and it
	 * will be popped out at the begin of {@link #endElement(String, String, String)}.<br>
	 * The pushed/popped Tag can very probable be null if we don't map a XML tag into Java class.<br>
	 */
	protected Stack<Tag> tagStack = null;

	/**
	 * The current status report for a certain 'test level'.<br>
	 * It will be reset to null at the begin of a certain 'test level'.<br>
	 * It could be null if a 'test level' doesn't contain a 'status report'.<br>
	 */
	protected StatusReport statusReport = null;
	/**
	 * It will contains the whole JUnit report generated from the SAFS XML Log.<br>
	 * It will be used in method {@link #getResult()} of the sub class.<br>
	 */
	protected List<TestLevel> testLevelList = null;

	/**
	 * It contains the Orderable object parsed from the LogMessage of type {@link MessageTypeInfo#ORDERABLE_XML_PREFIX}.
	 * @see #endElement(String, String, String)
	 */
	protected Orderable orderable = null;

	/**
	 * The unit to count the test's failures, skipped, warnings etc.
	 */
	private String counterUnit = Constants.COUNTER_UNIT_TESTCASE;

	public XMLSaxToJUnitHandler(){}

	/**
	 * @param counterUnit String, the counter unit. It can be one of {@link Constants#VALID_COUTNER_UNITS}.
	 */
	public XMLSaxToJUnitHandler(String counterUnit){
		this.counterUnit = counterUnit;
	}

	/** Clean the buffer holding the element's value, it should be called
	 * at the begin of {@link #startElement(String, String, String, Attributes)} or
	 * at the end of {@link #endElement(String, String, String)}.
	 */
	private void cleanElementValue(){
		value.delete(0, value.length());
	}

	private String getDateTime(Attributes attributes){
		return SAFS_LogConstants.getDateTime(attributes.getValue(SAFS_XML_LogConstants.PROPERTY_DATE),attributes.getValue(SAFS_XML_LogConstants.PROPERTY_TIME));
	}

	/**
	 * Handle 'LOG_MESSAGE' tag according to the log message's type.<br>
	 * If the type is 'START CYCLE/SUITE/TESTCASE', we add a "start {@link TestLevel}" into {@link #testLevelList}.<br>
	 * Else if the type is 'END CYCLE/SUITE/TESTCASE', we find the matching "start {@link TestLevel}" from {@link #testLevelList}
	 *    and we set its properties such as 'time', 'tests', 'failures', 'skipped' etc.
	 *    and we add a "end {@link TestLevel}" into {@link #testLevelList}.<br>
	 * Else, it is a normal log message, the type is 'failed', 'warning', 'skipped', 'debug' 'generic' etc.,
	 *       we only add 'failed', 'warning' and 'skipped' message into the "start {@link TestLevel}" (if it is {@link TestCase}).<br>
	 */
	private LogMessage handleLogMessage(String qName, Attributes attributes){
		String type = attributes.getValue(SAFS_XML_LogConstants.PROPERTY_TYPE);
		String dateTime = getDateTime(attributes);
		LogMessage message = null;
		TestLevel testLevel = null;

		if(MessageTypeInfo.START_CYCLE_XML_PREFIX.equals(type)){
			testLevel = new TestSuites();
			((TestSuites)testLevel).setTestName(testName);
			((TestSuites)testLevel).setUser(user);
			((TestSuites)testLevel).setMachine(machine);
			((TestSuites)testLevel).setIp(ip);
			testLevel.setParent(null);
		}else if(MessageTypeInfo.START_SUITE_XML_PREFIX.equals(type)){
			testLevel = new TestSuite();
			//Find the last 'TestSuites', which should be my parent
			for(int i=testLevelList.size()-1;i>-1;i--){
				if(testLevelList.get(i).getClass().getName().equals(TestSuites.class.getName())){
					testLevel.setParent(testLevelList.get(i));
					break;
				}
			}
		}else if(MessageTypeInfo.START_TESTCASE_XML_PREFIX.equals(type)){
			testLevel = new TestCase();
			//Find the last 'TestSuite', which should be my parent
			for(int i=testLevelList.size()-1;i>-1;i--){
				if(testLevelList.get(i).getClass().getName().equals(TestSuite.class.getName())){
					testLevel.setParent(testLevelList.get(i));
					break;
				}
			}
		}

		if(testLevel!=null){
			//It is 'Start Cycle/Suite/TestCase', the begin of a certain test level
			testLevel.setTimestamp(SAFS_LogConstants.getDate(dateTime));
			testLevelList.add(testLevel);
			message = testLevel;
//			testLevel.name = node.text;//will be assigned in endElement()
//			testLevel.classname = node.text;//will be assigned in endElement()
//			testLevel.time=null; //will be calculated at 'Stop cycle/suite/testcase'

			//Reset the status report at the begin of a certain test level
			statusReport = null;
		}else{

			TestLevel startTestLevel = null;
			TestLevel endTestLevel = null;
			if(MessageTypeInfo.END_CYCLE_XML_PREFIX.equals(type)){
				endTestLevel = new TestSuites();
			}else if(MessageTypeInfo.END_SUITE_XML_PREFIX.equals(type)){
				endTestLevel = new TestSuite();
			}else if(MessageTypeInfo.END_TESTCASE_XML_PREFIX.equals(type)){
				endTestLevel = new TestCase();
			}

			//At the end of a test level:
			//1. calculate the consumed time
			//2. count test's failures, errors, skipped etc.
			//   If the unit is 'teststep', the SAFS counters will be just ok
			//   If the unit is 'testcase', then we need to calculate it ourselves.
			if(endTestLevel!=null){
				endTestLevel.setTimestamp(SAFS_LogConstants.getDate(dateTime));
				endTestLevel.setStart(false);

				message = endTestLevel;

				//It is 'Stop Cycle/Suite/TestCase', the end of a certain test level.
				//Find the last Cycle/Suite/TestCase from the 'testLevelList', it should be the matching 'start Cycle/Suite/Case'
				for(int i=testLevelList.size()-1;i>-1;i--){
					if(testLevelList.get(i).getClass().getName().equals(endTestLevel.getClass().getName())){
						startTestLevel = testLevelList.get(i);
						break;
					}
				}

				//If we find the matched 'start Cycle/Suite/Case'
				if(startTestLevel!=null){
					//endTestLevel has the same parent as startTestLevel
					endTestLevel.setParent(startTestLevel.getParent());
					//calculate the time and mark it as stopped.
					startTestLevel.setTime((endTestLevel.getTimestamp().getTime()- startTestLevel.getTimestamp().getTime())/1000);

					if(Constants.COUNTER_UNIT_TESTCASE.equals(counterUnit)){
						//The count unit is 'testcase'
						//parentTestLevel is used to keep the 'failures', 'skipped' etc. of this test level
						TestLevel parentTestLevel = startTestLevel.getParent();

						if(parentTestLevel!=null){
							//We count according the type of messages stored in the test level (currently, TestCase contains this kind of message).
							for(LogMessage logmessage : startTestLevel.getMessages()){
								if(MessageTypeInfo.XML_PREFIX_TESTLEVEL_ERRORED.equals(logmessage.getType())){
									parentTestLevel.incrementErrors();
									break;
								}else if(MessageTypeInfo.XML_PREFIX_TESTLEVEL_FAILED.equals(logmessage.getType())){
									parentTestLevel.incrementFailures();
									break;
								}else if(MessageTypeInfo.XML_PREFIX_TESTLEVEL_SKIPPED.equals(logmessage.getType())){
									parentTestLevel.incrementSkipped();
									break;
								}
							}
							//TODO If we don't throw SAFSTestLevelFailure, SAFSTestLevelError from the TestSuite, how to count the tests, failures and errors for TestCycle?
							//TestCycle's 'tests' is how many TestSuites this TestCycle contains
							//TestCycle's 'errors' is how many TestSuites fail with error this TestCycle contains
							//TestCycle's 'failures' is how many TestSuites fail with failure this TestCycle contains

							parentTestLevel.incrementTests();
							System.out.println("Test: "+startTestLevel.text+" tests:"+parentTestLevel.tests+" errors:"+parentTestLevel.errors+" failures:"+parentTestLevel.failures);
						}
					}else{//SAFS_LogConstants.COUNTER_LEVEL_STEP
						//Get the information from the StatusReport, which counts based on unit 'teststep'
						if(statusReport!=null){
							startTestLevel.setTests(statusReport.countTests());
							startTestLevel.setFailures(statusReport.countTestFailures());
							startTestLevel.setSkipped(statusReport.countSkipped());
						}
					}
					testLevelList.add(endTestLevel);
				}else{
					IndependantLog.warn("Cannot find the matched 'Start "+endTestLevel.getClass().getSimpleName()+"'");
				}

			}else{
				//It is a normal test message, such as below:
				//<LOG_MESSAGE type='FAILED' date='06-12-2018' time='09:45:44' >
				//<LOG_MESSAGE type='TESTLEVEL_FAILED' date='06-12-2018' time='09:45:44' >
				//<LOG_MESSAGE type='GENERIC' date='06-12-2018' time='09:45:44' >
				//<LOG_MESSAGE type='TESTLEVEL_SKIPPED' date='06-12-2018' time='09:45:44' >
				message = new LogMessage();

				//if testLevelList is empty, then the message doesn't belong to any test level, we ignore it.
				if(!testLevelList.isEmpty()){
					//Get the last item in the 'testLevelList', that item should be a certain 'Cycle/Suite/Case'
					startTestLevel = testLevelList.get(testLevelList.size()-1);
					//if startTestLevel.start is true, that means this test level is still open
					//The test message should belong to this test level, we will try to add it to this test level.
					if(startTestLevel.start){

						if(Constants.COUNTER_UNIT_TESTCASE.equals(counterUnit)){
							//For 'testcase count unit', only keep message of type 'TESTLEVEL_FAILED' or 'TESTLEVEL_SKIPPED'
							if(MessageTypeInfo.XML_PREFIX_TESTLEVEL_ERRORED.equals(type) ||
							   MessageTypeInfo.XML_PREFIX_TESTLEVEL_FAILED.equals(type) ||
							   MessageTypeInfo.XML_PREFIX_TESTLEVEL_SKIPPED.equals(type)){
								startTestLevel.addMessage(message);
							}

						}else{//SAFS_LogConstants.COUNTER_UNIT_STEP
							//'FAILED','WARNING' and 'SKIPPED' are message types for each test step, save them into TestCase
							if( MessageTypeInfo.FAILED_MESSAGE_XML_PREFIX.equals(type) ||
								MessageTypeInfo.WARNING_MESSAGE_XML_PREFIX.equals(type) ||
								MessageTypeInfo.SKIPPED_TEST_XML_PREFIX.equals(type)){
								if(startTestLevel instanceof TestCase){
									startTestLevel.addMessage(message);
								}
							}
						}

					}else{
						//startTestLevel.start is false, this test level has closed.
						//So this message doesn't belong to a certain test level; it is a generic message, and and we ignore it.
					}
				}
			}
		}

		message.setType(type);
		message.setDateTime(dateTime);
//		message.text will be assigned in endElement() method

		return message;
	}

	@Override
	public void startDocument () throws SAXException{
		value = new StringBuilder();
		testLevelList = new ArrayList<TestLevel>();
		tagStack = new Stack<Tag>();
	}

	/** keep the testName got from tag LOG_OPENED's property */
	private String testName = null;
	/** keep the user got from tag LOG_OPENED's property */
	private String user = null;
	/** keep the machine got from tag LOG_OPENED's property */
	private String machine = null;
	/** keep the ip got from tag LOG_OPENED's property */
	private String ip = null;

	@Override
	public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException{
		cleanElementValue();

		Tag currentTag = null;
		try{
			if(SAFS_XML_LogConstants.TAG_LOG_OPENED.equals(qName)){
				currentTag = new Tag();
				testName = attributes.getValue(SAFS_XML_LogConstants.PROPERTY_NAME);
				user = attributes.getValue(SAFS_XML_LogConstants.PROPERTY_USER);
				machine = attributes.getValue(SAFS_XML_LogConstants.PROPERTY_MACHINE);
				ip = attributes.getValue(SAFS_XML_LogConstants.PROPERTY_IP);

			}else if(SAFS_XML_LogConstants.TAG_LOG_MESSAGE.equals(qName)){
				currentTag = handleLogMessage(qName, attributes);
			}else if(SAFS_XML_LogConstants.TAG_STATUS_REPORT.equals(qName)){
				statusReport = new StatusReport();
				statusReport.setName(attributes.getValue(SAFS_XML_LogConstants.PROPERTY_NAME));
				statusReport.setDateTime(getDateTime(attributes));

				currentTag = statusReport;
			}else if(SAFS_XML_LogConstants.TAG_STATUS_ITEM.equals(qName)){
				StatusItem item = new StatusItem();
				item.setType(attributes.getValue(SAFS_XML_LogConstants.PROPERTY_TYPE));
				//We know 'STATUS_ITEM' is inside 'STATUS_REPORT', so 'statusReport' has already been created and it is not null
				statusReport.addItem(item);

				currentTag = item;
			}
		}catch(Exception e){
			throw new SAXException(e.toString());
		}finally{
			tagStack.push(currentTag);
		}
	}

	@Override
	public void characters (char ch[], int start, int length) throws SAXException{
		String chunk = new String(ch, start, length);
		value.append(chunk);
	}

	/** Keep the text of tag &lt;MESSAGE_TEXT>, it is set at the end of tag &lt;MESSAGE_TEXT> and reset at the end of tag &lt;LOG_MESSAGE> */
	private String logMessageText = null;
	/** Keep the text of tag &lt;MESSAGE_DETAILS>, it is set at the end of tag &lt;MESSAGE_DETAILS> and reset at the end of tag &lt;LOG_MESSAGE> */
	private String logMessageDetails = null;
	/** Keep the text of tag &lt;STATUS_ITEM_TEXT>, it is set at the end of tag &lt;STATUS_ITEM_TEXT> and reset at the end of tag &lt;STATUS_ITEM> */
	private String statusItemText = null;
	/** Keep the text of tag &lt;STATUS_ITEM_TEXT>, it is set at the end of tag &lt;STATUS_ITEM_TEXT> and reset at the end of tag &lt;STATUS_ITEM> */
	private String statusItemDetails = null;

	@Override
	public void endElement (String uri, String localName, String qName) throws SAXException{

		try{
			Tag currentTag = tagStack.pop();

			if(SAFS_XML_LogConstants.TAG_LOG_MESSAGE.equals(qName)){
				if(currentTag instanceof LogMessage){
					currentTag.setText(logMessageText);
					currentTag.setDetails(logMessageDetails);
					if(currentTag instanceof TestLevel){
						((TestLevel) currentTag).setName(currentTag.getText());
						if(currentTag instanceof TestCase){
							//If we parse a traditional SAFS XML log, we don't present the property 'classname'. The property 'name' is the test table name, such as xxx.CDD, xxx.STD or xxx.SDD, so it is enough.
							//Otherwise, we are going to split the 'name' into 'method name' and 'class name'.
							String name = ((TestLevel) currentTag).getName();
							int lastDotIndex = -1;
							if(!name.toLowerCase().endsWith(".sdd")){
								lastDotIndex = name.lastIndexOf(".");
							}
							if(lastDotIndex >0){
								((TestCase) currentTag).setClassname(name.substring(0, lastDotIndex));
								((TestCase) currentTag).setName(name.substring(lastDotIndex+1));
							}
						}
					}else{
						//currentTag contains the 'orderable' information
						if(MessageTypeInfo.ORDERABLE_XML_PREFIX.equals(((LogMessage) currentTag).getType())){
							orderable = Utils.fromJsonString(currentTag.getText(), Orderable.class);
						}
					}
				}else{
					IndependantLog.warn("The current tag '"+currentTag+"' is not '"+LogMessage.class.getSimpleName()+"'");
				}
				logMessageText = null;
				logMessageDetails = null;

			}else if(SAFS_XML_LogConstants.TAG_STATUS_REPORT.equals(qName)){
				if(statusReport!=null){
					statusReport.setStopped(true);
				}
			}else if(SAFS_XML_LogConstants.TAG_STATUS_ITEM.equals(qName)){
				if(currentTag instanceof StatusItem){
					currentTag.setText(statusItemText);
					currentTag.setDetails(statusItemDetails);
				}else{
					IndependantLog.warn("The current tag '"+currentTag+"' is not '"+StatusItem.class.getSimpleName()+"'");
				}
				statusItemText = null;
				statusItemDetails = null;

			}else if(SAFS_XML_LogConstants.TAG_STATUS_ITEM_TEXT.equals(qName)){
				statusItemText = value.toString();
			}else if(SAFS_XML_LogConstants.TAG_STATUS_ITEM_DETAILS.equals(qName)){
				statusItemDetails = value.toString();
			}else if(SAFS_XML_LogConstants.TAG_MESSAGE_TEXT.equals(qName)){
				logMessageText = value.toString();
			}else if(SAFS_XML_LogConstants.TAG_MESSAGE_DETAILS.equals(qName)){
				logMessageDetails = value.toString();
			}

		}catch(Exception e){
			throw new SAXException(e.toString());
		}finally{
			cleanElementValue();
		}
	}
}
