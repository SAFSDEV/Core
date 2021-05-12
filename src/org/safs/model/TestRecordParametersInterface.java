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

import java.util.List;

/**
 * Define the minimal interface needed to identify an item that can export a newline-terminated 
 * String that is the test record needed to invoke its functionality.  
 * That is, it returns the line used to invoke it.
 * <p>
 * Example: There may be a pre-defined "Logon" StepTestTable that takes two parameters; userid 
 * and password.  To invoke this StepTestTable will require a test record line like:
 * <p>
 *   T, Logon, , ^userid="userid", ^password="password"
 * <p>
 * This interface defines the method needed to get that line from the StepTestTable instance.  
 * The same interface is used for commands, as well.
 *     
 * @author Carl Nagle
 */
public interface TestRecordParametersInterface {

	/**
	 * Export a properly formatted test record.
	 * Use the provided separator to delimit fields in the record.
	 * 
	 * @param fieldSeparator -- separates fields in the test record
	 * @return String test record
	 */
    public String exportTestRecord(String fieldSeparator);
    
    /**
     * Return the list of parameters for this test record.
     * 
     * @return List of parameters for the test record (if any).
     * Should return a zero-length array if there are no parameters.
     */
    public List getParameters();

    /**
     * Add a parameter to be used with this test record.
     * 
     * @param parameter -- might be null or zero-length
     */
    public void addParameter(String parameter);

    /**
     * Add an array of parameters to be used with this test record.
     * 
     * @param parameters -- non-null array of parameters to be added
     */
    public void addParameters(String[] parameters);

    /**
     * Append the stored test record parameters during an export operation.
     * This is called by the export mechanism and is not normally called 
     * by the user.
     * 
     * @param buffer -- StringBuffer holding the test record being exported
     * @param fieldSeparator -- separator used to delimit parameters
     * @return buffer with our test record parameters appended
     */
    public StringBuffer appendParametersToTestRecord(StringBuffer buffer, String fieldSeparator);
}
