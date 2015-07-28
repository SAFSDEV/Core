package org.safs.tools.vars;

public interface VarsInterface extends SimpleVarsInterface {

	/** 
	 * Process the input record for supported numeric and string expressions.
	 * The input record fields are delimited by the provided separator, and each 
	 * field is separately processed for expressions. 
	 * @return Copy of input record with all expressions processed. **/
	String resolveExpressions (String record, String sep);
	
	/** delete a variable from storage. **/
	void deleteVariable (String var);
}

