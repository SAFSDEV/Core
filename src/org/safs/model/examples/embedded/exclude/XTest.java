/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.examples.embedded.exclude;

import org.safs.model.annotations.JSAFSTest;
import org.safs.model.examples.embedded.MyApplicationTest;
import org.safs.model.tools.EmbeddedHookDriverRunner;
import org.safs.model.tools.EmbeddedHookDriverRunnerAware;

/**
 * @author Carl Nagle
 *
 */
public class XTest implements EmbeddedHookDriverRunnerAware{

	static EmbeddedHookDriverRunner runner;
	public void setEmbeddedHookDriverRunner(EmbeddedHookDriverRunner runner) {
		this.runner = runner;
	}
	
	@JSAFSTest
	public void TestA(){
		runner.logPASSED(getClass().getName() +"#TestA() executed.", null);
	}

	public void TestB(){
		runner.logPASSED(getClass().getName() +"#TestB() executed!", null);
	}

}
