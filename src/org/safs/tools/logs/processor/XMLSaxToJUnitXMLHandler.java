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
 * @date 2018-06-13    (Lei Wang) Handled count unit (testcase, teststep).
 */
package org.safs.tools.logs.processor;

import java.util.List;

import org.safs.Constants;
import org.safs.Constants.SAFS_XML_LogConstants;
import org.safs.SAFSTestLevelError;
import org.safs.SAFSTestLevelException.TestLevelMessage;
import org.safs.SAFSTestLevelFailure;
import org.safs.Utils;
import org.safs.logging.MessageTypeInfo;

/**
 * Parse SAFS XML Log and convert it to JUnit XML report, such as:
 * <pre>
 * &lt;?xml version='1.0' encoding='utf-8'?&gt;
 * &lt;testsuites name="a.testcycle.Cycle.runTest" timestamp="06-19-2018 10:44:44" time="16"&gt;
 *   &lt;testsuite name="a.testsuite.Suite.runTest" tests="5" errors="1" failures="2" skipped="1" timestamp="06-19-2018 10:44:44" time="16" &gt;
 *     &lt;testcase name="case1" classname="a.testcase.Cases1" time="0"&gt;
 *     &lt;/testcase&gt;
 *     &lt;testcase name="case2" classname="a.testcase.Cases1" time="0"&gt;
 *       &lt;skipped message="This test case will be skipped for some reason" details="a.testcase.Cases1.case2" /&gt;
 *     &lt;/testcase&gt;
 *     &lt;testcase name="case3" classname="a.testcase.Cases1" time="0"&gt;
 *       &lt;failure message="benchFile.txt doesn't match actualFile.txt." details="detailed failure message." type="DIFF" line="59"/&gt;
 *     &lt;/testcase&gt;
 *     &lt;testcase name="case4" classname="a.testcase.Cases1" time="16"&gt;
 *       &lt;failure message="IsComponentExists failed." details="FANTACY_GUI doesn't exist!" type="NON_EXIST" line="72"/&gt;
 *       &lt;failure message="WaitForGUI failed." details="FANTACY_GUI doesn't exist!" type="TIMEOUT" line="76"/&gt;
 *     &lt;/testcase&gt;
 *     &lt;testcase name="case5" classname="a.testcase.Cases1" time="0"&gt;
 *       &lt;failed message="null" type="NullPointerException" line="88"/&gt;
 *     &lt;/testcase&gt;
 *   &lt;/testsuite&gt;
 * &lt;/testsuites>
 * </pre>
 *
 * @author Lei Wang
 *
 */
public class XMLSaxToJUnitXMLHandler extends XMLSaxToJUnitHandler{

	public XMLSaxToJUnitXMLHandler(){}

	/**
	 * @param counterUnit String, the counter unit. It can be one of {@link Constants#VALID_COUTNER_UNITS}.
	 */
	public XMLSaxToJUnitXMLHandler(String counterUnit){
		super(counterUnit);
	}

	/**
	 * Convert the JUnit result to XML string.
	 */
	@Override
	public String getResult(){
    	StringBuilder sb = new StringBuilder();
    	TestCase testcase = null;
    	int level = 0;

    	sb.append("<?xml version='1.0' encoding='utf-8'?>\n");

    	for(TestLevel testLevel:testLevelList){
    		if(testLevel.start){
    			if(testLevel instanceof TestSuites){
    				sb.append("<"+testLevel.tagName+" "+SAFS_XML_LogConstants.PROPERTY_NAME+"=\""+testLevel.name+"\" "+
    			                  SAFS_XML_LogConstants.PROPERTY_TIMESTAMP+"=\""+testLevel.dateTime+"\" "+SAFS_XML_LogConstants.PROPERTY_TIME+"=\""+testLevel.time+"\">\n");

    			}else if(testLevel instanceof TestSuite){
    				sb.append(getIndent(1)+"<"+testLevel.tagName+" "+SAFS_XML_LogConstants.PROPERTY_NAME+"=\""+testLevel.name+"\" "+
    			                               SAFS_XML_LogConstants.PROPERTY_TESTS+"=\""+testLevel.tests+"\" "+
    			                               SAFS_XML_LogConstants.PROPERTY_ERRORS+"=\""+testLevel.errors+"\" "+
    						                   SAFS_XML_LogConstants.PROPERTY_FAILURES+"=\""+testLevel.failures+"\" "+
    						                   SAFS_XML_LogConstants.PROPERTY_SKIPPED+"=\""+testLevel.skipped+"\" "+
    						                   SAFS_XML_LogConstants.PROPERTY_TIMESTAMP+"=\""+testLevel.dateTime+"\" "+
    			                               SAFS_XML_LogConstants.PROPERTY_TIME+"=\""+testLevel.time+"\" >\n");

    			}else if(testLevel instanceof TestCase){
    				testcase = (TestCase) testLevel;
    				String classname = testcase.classname==null? "":" "+SAFS_XML_LogConstants.PROPERTY_CLASSNAME+"=\""+testcase.classname+"\"";
    				sb.append(getIndent(2)+"<"+testcase.tagName+" "+SAFS_XML_LogConstants.PROPERTY_NAME+"=\""+testcase.name+"\""+
    				                                                classname+" "+
    						                                        SAFS_XML_LogConstants.PROPERTY_TIME+"=\""+testcase.time+"\">\n");

    				for(LogMessage msg:testcase.getMessages()){
    					if(MessageTypeInfo.XML_PREFIX_TESTLEVEL_FAILED.equals(msg.type)){
    						//The SAFS Log message of type 'TESTLEVEL_FAILED' which presents when the counter-unit is 'testcase'
    						//User should put his failure messages into SAFSTestLevelFailure which can be got from the msg.text
    						SAFSTestLevelFailure exception = Utils.fromJsonString(msg.text, SAFSTestLevelFailure.class);
    						List<TestLevelMessage> failures = exception.getFailures();

    						failures.forEach(failure->{
    							StackTraceElement trace = failure.getErrorTrace();
    							String details = failure.getDetails()==null? "":SAFS_XML_LogConstants.PROPERTY_DETAILS+"=\""+failure.getDetails()+"\" ";
    							sb.append(getIndent(3)+"<"+SAFS_XML_LogConstants.TAG_FAILURE+" "+
    							                           SAFS_XML_LogConstants.PROPERTY_MESSAGE+"=\""+failure.getMessage()+"\" "+
    							                           details +
    									                   SAFS_XML_LogConstants.PROPERTY_TYPE+"=\""+failure.getType()+"\" "+
    							                           SAFS_XML_LogConstants.PROPERTY_LINE+"=\""+trace.getLineNumber()+"\"/>\n");
    						});
    					}else if(MessageTypeInfo.XML_PREFIX_TESTLEVEL_ERRORED.equals(msg.type)){
    						//The SAFS Log message of type 'TESTLEVEL_ERRORED' which presents when the counter-unit is 'testcase'
    						//User should put his error messages into SAFSTestLevelError which can be got from the msg.text
    						SAFSTestLevelError exception = Utils.fromJsonString(msg.text, SAFSTestLevelError.class);
    						List<TestLevelMessage> errors = exception.getErrors();

    						errors.forEach(error->{
    							StackTraceElement trace = error.getErrorTrace();
    							String details = error.getDetails()==null? "":SAFS_XML_LogConstants.PROPERTY_DETAILS+"=\""+error.getDetails()+"\" ";
    							sb.append(getIndent(3)+"<"+SAFS_XML_LogConstants.TAG_ERROR+" "+
    							                           SAFS_XML_LogConstants.PROPERTY_MESSAGE+"=\""+error.getMessage()+"\" "+
    							                           details +
    									                   SAFS_XML_LogConstants.PROPERTY_TYPE+"=\""+error.getType()+"\" "+
    							                           SAFS_XML_LogConstants.PROPERTY_LINE+"=\""+trace.getLineNumber()+"\"/>\n");
    						});
    					}else if(MessageTypeInfo.XML_PREFIX_TESTLEVEL_SKIPPED.equals(msg.type)){
    						//The SAFS Log message of type 'TESTLEVEL_SKIPPED'
    						String details = msg.getDetails()==null? "":SAFS_XML_LogConstants.PROPERTY_DETAILS+"=\""+msg.getDetails()+"\" ";
							sb.append(getIndent(3)+"<"+SAFS_XML_LogConstants.TAG_SKIPPED+" "+SAFS_XML_LogConstants.PROPERTY_MESSAGE+"=\""+msg.getText()+"\" "+details+"/>\n");

    					}else{
    						//The SAFS Log message of type 'FAILED', 'WARNING' etc. which presents when the counter-unit is 'teststep'
    						String details = msg.getDetails()==null? "":SAFS_XML_LogConstants.PROPERTY_DETAILS+"=\""+msg.getDetails()+"\" ";
    						sb.append(getIndent(3)+"<"+msg.type+" "+SAFS_XML_LogConstants.PROPERTY_MESSAGE+"=\""+msg.getText()+"\" "+details+">\n");

    					}
    				}
    			}
    		}else{
    			if(testLevel instanceof TestSuites){
    				level = 0;
    			}else if(testLevel instanceof TestSuite){
    				level = 1;
    			}else if(testLevel instanceof TestCase){
    				level = 2;
    			}

    			sb.append(getIndent(level)+"</"+testLevel.tagName+">\n");
    		}
    	}

    	return sb.toString();
	}

	private String getIndent(int level){
    	StringBuilder sb = new StringBuilder();
    	for(int i=0;i<level;i++){
    		sb.append("  ");
    	}
    	return sb.toString();
    }

}
