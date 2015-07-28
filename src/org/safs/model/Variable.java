/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;

/**
 * An object representing a DDVariable in the SAFS framework.
 * <p>
 * http://safsdev.sourceforge.net/sqabasic2000/DDVariableStore.htm
 * <p>
 * We use references to Variable objects to prevent human error in keying in DDVariable names 
 * and references.
 */
public class Variable {

	/** '^' */
	public static final String DEFAULT_VAR_IDENTIFIER = "^";
	
	private String _name;

	/**
	 * Create a new Variable object with the given varname.
	 * Variable names cannot contain spaces, can contain alpha and numeric characters, 
	 * and can contain a few special characters like '.'(period) and '_'(underscore).
	 * <p>
	 * Note: Special characters should NOT be used where they may conflict with the 
	 * separator used in your table records.
	 * <p>
	 * SAFS DDVariable names are NOT case-sensitive, so variables named 
	 * 'varname' and 'VARNAME' reference the same variable.
	 * <p>
	 * @param varname valid DDVariable name (cannot be null or contain whitespace).
	 * The name provided will be trim()'d prior to use.\
	 * @throws IllegalArgumentException if null or zero-length name is specified
	 */
	public Variable (String varname){
		try{
			String var = varname.trim();
			if (var.length()== 0) throw new IllegalArgumentException();
			_name = var;
		}
		catch(Exception x){
			throw new IllegalArgumentException("Variable name cannot be null or zero-length.");
		}
	}
	
	/**
	 * "varname" -- Returns the name of this variable with no leading variable identifier('^').
	 * This is used at times when only the name of the variable is needed.
	 * <p>
	 * Some SAFS commands want the names of variables.  <br/>  
	 * Attempting to provide the variable name with the variable identifier prefix 
	 * will result in providing the variable's value instead of its name at runtime.
	 * @return varname
	 */
	public String getName() {
		return _name;
	}
		
	/**
	 * "^varname" -- Returns a dereferencing version of this variable.
	 * This is used in SAFS records to retrieve the SAFS runtime value of the variable.
	 * @return ^varname
	 */
	public String getReference(){
	    return DEFAULT_VAR_IDENTIFIER + _name ;	
	}

	/**
	 * "^varname=expression" -- Returns an assignment version of this variable.
	 * This is used in SAFS records to assign a new value to the variable at SAFS runtime.
	 * Remember to wrap the expression in double-quotes prior to this call if it contains 
	 * SAFS expression operators or leading or trailing whitespace that must be treated as 
	 * literal text.
	 * @return ^varname=expression
	 */
	public String getAssignment(String expression){
	    return getReference() +"="+ expression;	
	}
}
