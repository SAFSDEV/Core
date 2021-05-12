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
