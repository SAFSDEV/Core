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
package org.safs.model.examples.embedded.include;

import org.safs.model.annotations.JSAFSTest;
import org.safs.model.tools.EmbeddedHookDriverRunner;
import org.safs.model.tools.EmbeddedHookDriverRunnerAware;

/**
 * @author Carl Nagle
 *
 */
public class ATest implements EmbeddedHookDriverRunnerAware{

	static EmbeddedHookDriverRunner runner;
	/**
	 * Should automatically get called during autoconfiguration.
	 */
	@Override
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
