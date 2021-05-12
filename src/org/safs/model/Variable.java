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

/**
 * An object representing a DDVariable in the SAFS framework.
 * <p>
 * <a href="/sqabasic2000/DDVariableStore.htm">DDVariableStore</a>
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
