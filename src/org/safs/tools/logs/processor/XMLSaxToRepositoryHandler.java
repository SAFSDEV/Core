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
 * @date 2018-05-04    (Lei Wang) Modified method getResult(): Treat the 'push to repository' as a transaction.
 *                                                             If one of data (cycle, suite, testcase) fails to push, the whole transaction will be cancelled.
 * @date 2018-05-17    (Lei Wang) Modified method getResult(): Moved the "code of deleting model" to Utils.
 *                                                             Append message of "fail deleted models" during roll-back of transaction.
 * @date 2018-05-17    (Lei Wang) Modified method getResult(): Throw SAFSRuntimeException in the catch block instead of returning error message as result.
 * @date 2018-06-05    (Lei Wang) Modified method collectTestcycle(): Push cycle with more properties 'user', 'machine', 'ip' and 'testName'.
 * @date 2018-06-07    (Lei Wang) Modified collectTestcycle(): Push the Orderable data and set the orderableId to Cycle.
 * @date 2018-06-13    (Lei Wang) Added constructor to accept parameter 'counterUnit'.
 */
package org.safs.tools.logs.processor;

import java.util.Stack;

import org.safs.Constants;
import org.safs.IndependantLog;
import org.safs.SAFSDatabaseException;
import org.safs.SAFSModelCreationException;
import org.safs.SAFSRuntimeException;
import org.safs.StringUtils;
import org.safs.Utils;
import org.safs.data.model.Orderable;
import org.safs.data.model.RestModel;
import org.safs.data.model.Testcase;
import org.safs.data.model.Testcycle;
import org.safs.data.model.Testsuite;
import org.safs.rest.REST;

/**
 * Parse SAFS XML Log and push the test data into 'safs data repository'.
 *
 * @author Lei Wang
 *
 */
public class XMLSaxToRepositoryHandler extends XMLSaxToJUnitHandler{

	/**
	 * The safsdata-service's base URL.<br>
	 * If this is not null, then this handler will try to push safs test data (cycle, suite, case) into safs data repository.<br>
	 * @see #getResult()
	 */
	private String safsdataServiceURL = null;
	/**
	 * The session ID for safsdata-service.
	 */
	private String safsdataServiceID = null;

	public XMLSaxToRepositoryHandler(){}

	/**
	 * Construct a handler with safsdata-service URL.
	 * If the service's URL is not null, then this handler will try to push safs test data (cycle, suite, case) into safs data repository<br>
	 * @param safsdataServiceURL String, The safsdata-service's base URL.
	 */
	public XMLSaxToRepositoryHandler(String safsdataServiceURL){
		this.safsdataServiceURL = safsdataServiceURL;
	}

	/**
	 * Construct a handler with safsdata-service URL.
	 * If the service's URL is not null, then this handler will try to push safs test data (cycle, suite, case) into safs data repository<br>
	 * @param safsdataServiceURL String, The safsdata-service's base URL.
	 * @param counterUnit String, the counter unit. It can be one of {@link Constants#VALID_COUTNER_UNITS}.
	 */
	public XMLSaxToRepositoryHandler(String safsdataServiceURL, String counterUnit){
		super(counterUnit);
		this.safsdataServiceURL = safsdataServiceURL;
	}

	/**
	 * 1. Start the session to connect to safsdata service.<br>
	 * 2. Push the safs test data to repository.<br>
	 * 3. Close the session to safsdata service.<br>
	 *
	 * @return String, a string of all the pushed safs data models.
	 * @throws SAFSRuntimeException if 'push of data to safs repository' fails.
	 */
	@Override
	public String getResult(){

		Stack<RestModel> pushedModelStack = new Stack<RestModel>();
		StringBuilder resultOfPushedModels = new StringBuilder();

		try{
			safsdataServiceID = XMLSaxToRepositoryHandler.class.getSimpleName()+System.currentTimeMillis();
			REST.StartServiceSession(safsdataServiceID, safsdataServiceURL);

			resultOfPushedModels.append("Test data have been pushed to reposiotry as below:\n");

			Testcycle modelTestCycle = null;
			Testsuite modelTestSuite = null;
			Testcase modelTestCase = null;

			for(TestLevel testLevel:testLevelList){
				if(testLevel.start){//The begin of this test level
					if(testLevel instanceof TestSuites){
						modelTestCycle = collectTestcycle((TestSuites) testLevel);
						pushedModelStack.push(modelTestCycle);

						resultOfPushedModels.append(modelTestCycle.getClass().getSimpleName()+": "+modelTestCycle.toString()+"\n");

					}else if(testLevel instanceof TestSuite){
						modelTestSuite = collectTestsuite((TestSuite) testLevel, modelTestCycle.getId());
						pushedModelStack.push(modelTestSuite);
						resultOfPushedModels.append(modelTestSuite.getClass().getSimpleName()+": "+modelTestSuite.toString()+"\n");

					}else if(testLevel instanceof TestCase){
						TestCase testcase = (TestCase) testLevel;

//						for(LogMessage msg:testcase.getMessages()){
//							//Teststep: testcaseId, statusId, logMessage;
//							System.out.println("type: "+msg.getType()+"\nmessage: "+msg.getText());
//						}

						modelTestCase = collectTestcase(testcase, modelTestSuite.getId());
						pushedModelStack.push(modelTestCase);
						resultOfPushedModels.append(modelTestCase.getClass().getSimpleName()+": "+modelTestCase.toString()+"\n");
					}
				}else{//The end of this test level, we do nothing for now.

				}
			}

		} catch (SAFSDatabaseException | SAFSModelCreationException e) {
			String eMsg = StringUtils.debugmsg(e)+"\n"+e.getCause()==null?"":StringUtils.debugmsg(e.getCause());
			IndependantLog.error("Failed to push safs test data (cycle, suite, testcase), due to "+eMsg);
			StringBuilder throwMsg = new StringBuilder();

			if(pushedModelStack.isEmpty()){
				throwMsg.append("No test data was pushed to repository, due to "+eMsg);
			}else{
				RestModel model = null;
				StringBuilder failDeleteModels = new StringBuilder();
				while(!pushedModelStack.empty()){
					model = pushedModelStack.pop();
					try{
						Utils.deleteFromRepository(safsdataServiceID, model);
					}catch(SAFSDatabaseException sde){
						IndependantLog.warn("Failed to delete "+model+", due to "+StringUtils.debugmsg(sde)+", "+sde.getCause().getMessage());
						failDeleteModels.append(model);
					}
				}

				throwMsg.append("!!! BUT WE CANCEL THE TRANSACTION (ROLLBACK THE PUSHED MODEL), DUE TO ERROR\n"+ eMsg);
				if(failDeleteModels.length()!=0){
					throwMsg.append("\nBut we failed to delete some pushed model\n").append(failDeleteModels.toString());
				}
			}

			throw new SAFSRuntimeException(throwMsg.toString());

		}finally{
			if(safsdataServiceID!=null) REST.EndServiceSession(safsdataServiceID);
		}

		return resultOfPushedModels.toString();
	}

	protected Testcycle collectTestcycle(TestSuites testlevel) throws SAFSDatabaseException, SAFSModelCreationException{
		Testcycle cycle = new Testcycle(/*testlevel.getOrderableID() ,*/testlevel.getName(), testlevel.getErrors(), testlevel.getTests(), testlevel.getFailures(), testlevel.getSkipped(), testlevel.getTime(), testlevel.getTimestamp(),
				testlevel.getTestName(), testlevel.getUser(), testlevel.getMachine(), testlevel.getIp());

		if(orderable!=null){
			orderable = (Orderable) phoneHome(orderable);
			cycle.setOrderableId(orderable.getId());
		}

		return (Testcycle) phoneHome(cycle);
	}
	protected Testsuite collectTestsuite(TestSuite testlevel, Long id) throws SAFSDatabaseException, SAFSModelCreationException{
		Testsuite suite = new Testsuite(id, testlevel.getName(), testlevel.getTests(), testlevel.getErrors(), testlevel.getFailures(), testlevel.getSkipped(), testlevel.getTime(), testlevel.getTimestamp());
		return (Testsuite) phoneHome(suite);
	}
	protected Testcase collectTestcase(TestCase testlevel, Long id) throws SAFSDatabaseException, SAFSModelCreationException{
		String classname = testlevel.classname==null? "":testlevel.classname;
		Testcase testcase = new Testcase(id, testlevel.getName(), classname, testlevel.getTime());
		return (Testcase) phoneHome(testcase);
	}
	protected RestModel phoneHome(RestModel restModel) throws SAFSDatabaseException, SAFSModelCreationException{
		return Utils.pushToRepository(safsdataServiceID, restModel);
	}
}
