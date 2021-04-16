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
package org.safs.model.examples.tables.step;

import org.safs.model.StepTestTable;
import org.safs.model.examples.tables.Variables;
import org.safs.model.examples.tables.appmap.LoginWin;

/**
 * @author Carl Nagle
 */
public class Logon extends StepTestTable {
	
	public Logon (String userid, String password){
		super("Logon");
		
		// the Suite test record
		addParameter(Variables.userid.getAssignment(userid));
		addParameter(Variables.password.getAssignment(password));
		
		//the Step table
	    add( LoginWin.UserName.setTextValue( Variables.userid.getReference() ));
		add( LoginWin.Password.setTextValue( Variables.password.getReference() ));
		add( LoginWin.OK.click() );
	}	
}
