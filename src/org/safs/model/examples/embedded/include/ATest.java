/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.examples.embedded.include;

import org.safs.model.annotations.JSAFSTest;
import org.safs.model.examples.embedded.MyApplicationTest;
import org.safs.model.tools.EmbeddedHookDriverRunner;
import org.safs.model.tools.EmbeddedHookDriverRunnerAware;

/**
 * @author canagl
 *
 */
public class ATest implements EmbeddedHookDriverRunnerAware{

	static EmbeddedHookDriverRunner runner;
	/**
	 * Should automatically get called during autoconfiguration.
	 */
	public void setEmbeddedHookDriverRunner(EmbeddedHookDriverRunner runner) {
		this.runner = runner;		
	}
	
	@JSAFSTest
	public void TestA(){
		runner.logPASSED(getClass().getName() +"#TestA() executed.", null);
	}

	/**
	 * This class should NOT run during an autorun, but it can be called 
	 * independently.
	 * @see org.safs.model.examples.embedded.MyApplicationTest#TestB()
	 */
	public void TestB(){
		runner.logPASSED(getClass().getName() +"#TestB() executed.", null);
	}

}
