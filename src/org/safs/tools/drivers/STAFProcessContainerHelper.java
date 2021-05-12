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
/*
 * Created on Jun 29, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.safs.tools.drivers;

import java.util.Vector;

import org.safs.TestRecordHelper;
import org.safs.tools.engines.EngineInterface;

/**
 * @author Carl Nagle
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class STAFProcessContainerHelper extends TestRecordHelper {

	Vector engineResults = new Vector();
	
	/**
	 * 
	 */
	public STAFProcessContainerHelper() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * "org.safs.staf.", but not really used.
	 * @see org.safs.TestRecordHelper#getCompInstancePath()
	 */
	public String getCompInstancePath(){return "org.safs.staf.";}
	
	public void clearResults(){
		engineResults.clear();
	}
	
	public void addResults(Object item){
		engineResults.add(item);
	}
	
	public Vector getResults(){ return engineResults; }
}
