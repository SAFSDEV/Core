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
package org.safs.tools.drivers;
public interface FlowControlInterface {
	
	/****************************************************** 
	 * Is a ScriptNotExecuted BlockID assigned?
	 **/
	public boolean isScriptNotExecutedBlockValid ();

	/** BlockID for status=ScriptNotExecuted response. 
	 **/
	public String getScriptNotExecutedBlock ();

	/** BlockID for status=ScriptNotExecuted response. 
	 **/
	public void setScriptNotExecutedBlock (String blockID);



	/****************************************************** 
	 * Is an ExitTable BlockID assigned?
	 **/
	public boolean isExitTableBlockValid ();

	/** BlockID for status=ExitTable response. 
	 **/
	public String getExitTableBlock ();

	/** BlockID for status=ExitTable response. 
	 **/
	public void setExitTableBlock (String blockID);



	/****************************************************** 
	 * Is a NoScriptFailure BlockID assigned?
	 **/
	public boolean isNoScriptFailureBlockValid ();

	/** BlockID for status=NoScriptFailure response. 
	 **/
	public String getNoScriptFailureBlock ();

	/** BlockID for status=NoScriptFailure response. 
	 **/
	public void setNoScriptFailureBlock (String blockID);



	/****************************************************** 
	 * Is a ScriptFailure BlockID assigned?
	 **/
	public boolean isScriptFailureBlockValid ();

	/** BlockID for status=ScriptFailure response. 
	 **/
	public String getScriptFailureBlock ();

	/** BlockID for status=ScriptFailure response. 
	 **/
	public void setScriptFailureBlock (String blockID);



	/***************************************************** 
	 * Is a ScriptWarning BlockID assigned?
	 **/
	public boolean isScriptWarningBlockValid ();

	/** BlockID for status=ScriptWarning response. 
	 **/
	public String getScriptWarningBlock ();

	/** BlockID for status=ScriptWarning response. 
	 **/
	public void setScriptWarningBlock (String blockID);



	/***************************************************** 
	 * Is an IOFailrue BlockID assigned?
	 **/
	public boolean isIOFailureBlockValid ();

	/** BlockID for status=IOFailure response. 
	 **/
	public String getIOFailureBlock ();

	/** BlockID for status=IOFailure response. 
	 **/
	public void setIOFailureBlock (String blockID);
	
}

