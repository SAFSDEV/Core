package org.safs.tools.vars;

public interface SimpleVarsInterface {

	/** Set the value of a named variable. 
	 * @return the value as retrieved from the variable after it was set. **/
	String setValue (String var, String value);
	
	/** Get the value of a named variable. 
	 * @return the current value of the variable, or an empty string. **/
	String getValue (String var);
}

