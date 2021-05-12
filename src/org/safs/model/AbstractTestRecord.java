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
package org.safs.model;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * The first concrete (though still abstract) implementation of the TestRecordParametersInterface.
 * Provides the base functionality for storing command parameters and properly exporting them 
 * when tasked to append them to a test record.  This mechanism is the same for both individual 
 * commands and multi-record test tables.
 */
public abstract class AbstractTestRecord implements TestRecordParametersInterface{

    /** Identifies an empty value that can be specified for optional parameters. */
	public static final String EMPTY_PARAMETER = "";

    /** 'B' */
    public static final String BLOCKID_RECORD_TYPE                   = "B";
    /** 'BP' */
    public static final String BREAKPOINT_RECORD_TYPE                = "BP";
    /** 'T' */
    public static final String COMPONENT_FUNCTION_RECORD_TYPE        = "T";
    /** 'TW' */
    public static final String COMPONENT_FUNCTION_WARNOK_RECORD_TYPE = "TW";
    /** 'TF' */
    public static final String COMPONENT_FUNCTION_FAILOK_RECORD_TYPE = "TF";
    /** 'C' */
    public static final String DRIVER_COMMAND_RECORD_TYPE            = "C";
    /** 'CW' */
    public static final String DRIVER_COMMAND_WARNOK_RECORD_TYPE     = "CW";
    /** 'CF' */
    public static final String DRIVER_COMMAND_FAILOK_RECORD_TYPE     = "CF";
    /** 'E' */
    public static final String ENGINE_COMMAND_RECORD_TYPE            = "E";
    /** 'EW' */
    public static final String ENGINE_COMMAND_WARNOK_RECORD_TYPE     = "EW";
    /** 'EF' */
    public static final String ENGINE_COMMAND_FAILOK_RECORD_TYPE     = "EF";
    /** 'T' */
    public static final String PROJECT_COMMAND_RECORD_TYPE           = "T";
    /** 'S' */
    public static final String SKIPPED_RECORD_TYPE                   = "S";
	   	
	protected List _parameters;
	
	/**
	 * Initializes our parameters list with an empty List.
	 * Invoked from subclasses via the super() mechanism.
	 */
	protected AbstractTestRecord(){
		_parameters = new ArrayList();		
	}
	
	/**
	 * Retrieve the unmodifiableList of parameters associated with the record.
	 * 
	 * @return unmodifiableList of parameters
	 */
	public List getParameters(){ 
		return Collections.unmodifiableList(_parameters);
	}

	/**
	 * Add a parameter to the current list of parameters associated with the record.
	 * Null parameters are converted to zero-length EMPTY_PARAMETERS.
	 * 
	 * @param parameter -- null parameters are converted to EMPTY_PARAMETERs
	 */
	public void addParameter(String parameter) {
	    _parameters.add(parameter == null ? EMPTY_PARAMETER : parameter);
	}
	   
	/**
	 * Add an array of parameters to the current list of parameters associated with the record.
	 * If the specified array is null then no action is taken.
	 * Calls addParameter for each individual element in the array.
	 * 
	 * @param String[] parameters -- if null then no action is taken
	 */
	public void addParameters(String[] parameters) {
		if (parameters == null) return;
	    for (int i=0, cnt=parameters.length; i<cnt; i++)
	        addParameter(parameters[i]);
	}
	
	/**
	 * Appends all record or table invocation parameters to the provided buffer.
	 * This is normally called as part of the export mechanism and most users will not 
	 * normally ever call this directly.
	 * 
	 * @param buffer -- the test record export string being built by the export mechanism
	 * @param fieldSeparator -- the delimiter used between test record fields
	 * @return the buffer with our parameters added onto it
	 */
    public StringBuffer appendParametersToTestRecord(StringBuffer buffer, String fieldSeparator){
        List parameters = getParameters();
        for (int p = 0, pCount = parameters.size(); p < pCount; p++) {
           buffer.append(fieldSeparator);
           buffer.append(parameters.get(p));
        }
        return buffer;
    }
}
