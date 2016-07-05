package com.sas.spock.safs.runner.tests;

import org.junit.Assert;
import org.junit.Test;
import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.model.tools.Runner;

/**
 * The class uses the new org.safs.model.tool.Runner for access to the running framework.<br>
 * 
 * @author Carl Nagle
 */
public class CallJunitTest {
	private TestRecordHelper trd;
	
	private boolean trdPassed(){
		return trd.getStatusCode() == StatusCodes.OK;
	}
	
	@Test
	public void runner_callJunit() {
		try {
			//User Runner to invoke CallJUnit to execute a spock groovy test.
			trd = Runner.command("CallJunit", "com.sas.spock.safs.runner.tests.SpockExperimentWithRunner");
			if(trdPassed()){
				System.out.println(trd.getCommand()+" Succeeded, with result: "+trd.getStatusInfo());				
			}else{
				System.out.println(trd.getCommand()+" Failed, with result: "+trd.getStatusInfo());				
			}
		} catch (Throwable sx) {
			Assert.fail(sx.getClass()+", "+sx.getMessage()+", retrieving and comparing local variables ^a, ^b.");
		}
	}
	

}
