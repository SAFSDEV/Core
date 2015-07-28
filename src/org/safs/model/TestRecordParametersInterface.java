/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
 * @author canagl
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
