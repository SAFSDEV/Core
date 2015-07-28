/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model.examples.tables.suite;

import org.safs.model.SuiteTestTable;
import org.safs.model.examples.tables.step.*;

/**
 * @author Carl Nagle
 */
public class AlternateLoginTests extends SuiteTestTable{
	
	public AlternateLoginTests(){
		super("AlternateLoginTests");
		
		// no test record parameters
		
		// the Suite table
		add( Step.Logon("Carlos", "Santana"));
		add( Step.Logon("Bogus", "Password"));
		add( Step.Logon("Invalid", "User"));
	}
}
