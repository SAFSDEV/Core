
/*
 * Created on Feb 21, 2004 by Jack
 * OCT 04, 2004 Carl Nagle 	Added old-style substitute/extract variables processing for
 *                     	when EXPRESSIONS are not enabled.
 * OCT 27, 2004 Carl Nagle 	Fixed "0" conversion at end of evalPrimative.  Also the handling
 *                     	of whitespace for numeric and non-numeric processing.
 * SEP 08, 2008 WangLei	Modify method evalPrimative(), call method removeInternalDoubleQuotes() to
 * 						remove the double quote after concatenating two strings, see defect S0532438.
 * 						For example, s1="aaa", s2="bbb", after the concatenation s1 and s2 the result
 * 						string will be "aaa""bbb", the double quote should be removed.
 * JAN 08, 2009	WangLei Modify method removeInternalDoubleQuotes(): remove also single quote, see defect S0532438.
 * 						For example, s2 is a variable whose value is "bbb", if concatenate "aaa" and s2 ("aaa" & ^s2)
 * 						string will be "aaa"bbb", the single quote should be removed.
 *                      Modify removeInternalDoubleQuotes(): enclose the returned string with " in case its trailing
 *                  	blanks are missing while doing & operation again.
 * MAY 26, 2017	WangLei Added method main(), _simple_test(), _verifyExpresion() for unit testing.
 */
package org.safs.tools.expression;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.safs.Log;
import org.safs.SAFSStringTokenizer;
import org.safs.StringUtils;
import org.safs.tools.stringutils.StringUtilities;
import org.safs.tools.vars.SimpleVarsInterface;
/**
 * <pre> (Copied from RRAFS StringUtilities -- the original implementation.)
 *
      Given an expression attempt to locate variable assignment references
      and other operators and process the expressions into a result string.

      Valid operators (default values):

              ^                    '(Caret) Variable Prefix
              =                    'Assignment operator
              "                    'A single Double-Quote mark
              &                    'String concatenate operator
              +                    'Addition operator
              -                    'Subtraction operator
              &#42;                    'Multiplication operator
              /                    'Division operator
              %                    'Modulus/Remainder operator
              (                    'Open Group operator
              )                    'Close Group operator

      Double-quote marks are used to enclose literal text strings or values.
      To include a double-quote inside a literal text string use two
      double-quote marks with no intervening characters.


      Precedence:

      Groupings ( ) are processed first.  Within groupings, and after all groups
      have been processed, operators are processed in the following order:

          &#42; / % + - &

      The expression will be trimmed of leading and trailing whitespace on
      entry.  Use double-quote marks (literal text) to retain significant
      leading and trailing whitespace.

      Variable assignment references are identified by a leading caret (^)
      immediately followed by the name of the variable, an equal sign (=),
      and the value to assign to that variable. Whitespace can exist on either
      side of the equal sign.

      Variable names must conform to the SAFS standard (need link).

      Variable lookups(GET) and assignments(SET) will be done via the current
      SAFSVARS service or its equivalent.

      If the proposed value is NOT another variable reference then the proposed
      value will be trimmed of leading and trailing whitespace (spaces and tabs).
      And, yes, a variable can be assigned the value of another variable.

      Additionally, if there is text or operators that are not to be considered as
      variables they should be enclosed in double-quote marks.

      NOTE:
      Expressions containing groupings but no other legitimate operators may
      produce unexpected results that may not be the same on later versions of
      these routines.
 * </pre>
 * @author Jack
 */
public class SafsExpression {

	public boolean debugPrint = false;

	protected String sExpression ;
	protected int iExpressionlength ;
	protected SimpleVarsInterface varinterface ;
	protected Vector vQuoteLocs ;
	protected Vector vSubStrings ;
	protected Vector vVariableNames ;
	protected String strErrors = "" ;
	protected boolean booErrors = false ;
	protected boolean stripLeadZero = true ;
	protected boolean stripTrailingZeros = true ;
	protected boolean stripTrailingDecimalPoint = true ;
	/** 14 **/
	protected int numDecimalPlaces = 14 ;  // emperically set based upon tests on wxp

	/** "\"" **/
	protected final String qt = "\"";

	/** (char) 11 **/
	protected char quoteEncoded = (char) 11;
	/** ((char) 11).toString() **/
	protected String expEncodedQuote = new Character(quoteEncoded).toString();
	/** (char) 1 **/
	protected char varQuoteEncoded = (char) 1;
	/** ((char) 1).toString() **/
	protected String expVarEncodedQuote = new Character(varQuoteEncoded).toString();
	/** + - * / %  **/
	public static final String NUMERIC_OPERATORS = "+-*/%" ;
	/** + - * / % & =  **/
	public static final String ALL_OPERATORS = "+-*/%&=" ;

	/**
	 * Method SafsExpression.  Basic contstructor with no arguments.  Defaults sExpression to empty string.
	 */
	public SafsExpression( ) {
		sExpression = "" ;
	}

	/**
	 * Method SafsExpression.
	 * @param sText  The SAFS string expression
	 * @throws Exception  Error messages such as unmatched parentheses or illegal variable names
	 */
	public SafsExpression( String sText ) throws Exception {
		/* constructor, takes the expression and sets the data members (fields) */
		try {
			initData(sText) ;
		} catch ( Exception e ) {
			throw e ;
		}
	}

	/**
	 * Method SafsExpression.
	 * @param vi  Any object that implements org.safs.tools.vars.SimpleVarsInterface used to get and set SAFS variables.
	 */
	public SafsExpression( SimpleVarsInterface vi ) {
		this();
		setVarInterface(vi) ;
	}

	/**
	 * Method SafsExpression.
	 * @param sText  The SAFS string expression
	 * @param vi  Any object that implements org.safs.tools.vars.SimpleVarsInterface used to get and set SAFS variables.
	 * @throws Exception  Error messages such as unmatched parentheses or illegal variable names
	 */
	public SafsExpression( String sText, SimpleVarsInterface vi ) throws Exception {
		/* constructor, takes the expression and sets the data members (fields) */
		varinterface = vi ;
		try {
			initData(sText) ;
		} catch ( Exception e ) {
			throw e ;
		}
	}

	/**
	 * Method setVarInterface.  Sets the SAFS variable interface object.
	 * @param vi  Any object that implements org.safs.tools.vars.SimpleVarsInterface used to get and set SAFS variables.
	 */
	public void setVarInterface(SimpleVarsInterface vi) {
		varinterface = vi ;
	}

	/**
	 * Method getVarInterface.  Gets the SAFS variable interface object.
	 * @return SimpleVarsInterface  The SAFS variable interface used to get and set variables.
	 */
	public SimpleVarsInterface getVarInterface() {
		return varinterface ;
	}

	/**
	 * Method setExpression.  Sets the SAFS expression to a new value and initializes internals
	 * @param sText  The SAFS string expression
	 * @throws Exception  Error messages such as unmatched parentheses or illegal variable names
	 */
	public void setExpression(String sText) throws Exception {
		/* setter for the sExpression string */
		try {
			initData(sText) ;
		} catch ( Exception e ) {
			throw e ;
		}

	}

	/**
	 * Method getExpression.  Accessor for the protected field sExpression
	 * @return String  The SAFS expression
	 */
	public String getExpression() {
		return decodeLiteralDoubleQuotes(sExpression) ;
	}

	/**
	 * Method getQuoteLocs.  Accessor for the protected field vQuoteLocs
	 * @return Vector  {@link Integer} objects containing locations of the quotes (if any)
	 */
	public Vector getQuoteLocs() {
		return vQuoteLocs ;
	}

	/**
	 * Method getSubStrings.  Accessor for the protected field vSubStrings
	 * @return Vector  {@link String} objects containing quoted substrings of the expression (if any)
	 */
	public Vector getSubStrings() {
		return vSubStrings ;
	}

	/**
	 * Method getVariableNames.  Accessor for the protected field vVariableNames
	 * @return Vector  {@link String} objects that are the variables found within the expression sExpression
	 */
	public Vector getVariableNames() {
		return vVariableNames ;
	}

	/**
	 * Method stripLeadZero.  Setter for the protected field stripLeadZero
	 * <p>
	 * sets option to strip the leading zero from expression results.  For example,
	 * if the expression evaluates to 0.1 and stripLeadZero is true, the result will
	 * be formatted such that the leading zero is not shown, which would be .1
	 * @param b  true or false specifying wheather or not leading zeros should be stripped
	 */
	public void stripLeadZero( boolean b ) {
		stripLeadZero = b ;
	}

	/**
	 * Method stripLeadZero.  Accessor for the protected field stripLeadZero
	 * @return boolean  The current specified option to strip leading zeros
	 */
	public boolean stripLeadZero( ) {
		return stripLeadZero ;
	}

	/**
	 * Method stripTrailingZeros.  Setter for the protected field stripTrailingZeros
	 * <p>
	 * sets option to strip the trailing zeros from expression results.  For example,
	 * if the expression evaluates to 1.100 and stripTrailingZeros is true, the result will
	 * be formatted such that the trailing zeros are not shown, which would be 1.1
	 * @param b  true or false specifying wheather or not trailing zeros should be stripped
	 */
	public void stripTrailingZeros( boolean b ) {
		stripTrailingZeros = b ;
	}

	/**
	 * Method stripTrailingZeros.  Accessor for the protected field stripTrailingZeros
	 * @return boolean  The current specified option to strip trailing zeros
	 */
	public boolean stripTrailingZeros( ) {
		return stripTrailingZeros ;
	}

	/**
	 * Method stripTrailingDecimalPoint.  Setter for the protected field stripTrailingDecimalPoint
	 * <p>
	 * sets option to strip the trailing decimal points from expression results.  For example,
	 * if the expression evaluates to 1. and stripTrailingDecimalPoint is true, the result will
	 * be formatted such that the trailing zeros are not shown, which would be 1
	 * @param b  true or false specifying wheather or not trailing decimal points should be stripped
	 */
	public void stripTrailingDecimalPoint( boolean b ) {
		stripTrailingDecimalPoint = b ;
	}

	/**
	 * Method stripTrailingDecimalPoint.  Accessor for the protected field stripTrailingDecimalPoint
	 * @return boolean  The current specified option to strip trailing decimal point
	 */
	public boolean stripTrailingDecimalPoint( ) {
		return stripTrailingDecimalPoint ;
	}

	/**
	 * Method numDecimalPlaces.  Setter for the protected field numDecimalPlaces
	 * @param i  int specifying the number of decimal places to round half up in division operations
	 */
	public void numDecimalPlaces( int i ) {
		numDecimalPlaces = i ;
	}

	/**
	 * Method numDecimalPlaces.  Accessor for the protected field numDecimalPlaces
	 * @return int  The number of decimal places to round half up in division operations
	 */
	public int numDecimalPlaces( ) {
		return numDecimalPlaces ;
	}

	/**
	 * The main entry point to process the overall expression.
	 * @return String  The result of evaluating the expression
	 * @see #evalExpression(String)
	 */
	public String evalExpression() {
		/* simply a public interface that uses the member variable sExpression to evaluate */
		String strResult = evalExpression(sExpression) ;

		// double quotes have special meaning and may be present in the result
		// two consecutive double quotes represent a single literal double quote
		// a double quote that is not adjacent to another double quote, should be removed
		// special case:  two consecutive double quotes that appear due to concatonation
		// i.e. not in the original expression, should not be treated as a single literal
		// double quote.
		strResult = interpretInternalDoubleQuotes(strResult) ;
		if((strResult.startsWith(qt)) &&
		   (strResult.endsWith(qt))) strResult = qt + strResult + qt;
		return strResult ;
	}

	/**
	 * Method evalVariables.  This is the method called to evaluate the expression and return the result.
	 * This is in support of old-style variable handling before expressions were allowed.
	 * @return String  The result of evaluating the variables
	 */
	public String evalVariables() {
		/* simply a public interface that uses the member variable sExpression to evaluate */
		String strResult = evalVariables(sExpression) ;

		// double quotes have special meaning and may be present in the result
		// two consecutive double quotes represent a single literal double quote
		// a double quote that is not adjacent to another double quote, should be removed
		// special case:  two consecutive double quotes that appear due to concatonation
		// i.e. not in the original expression, should not be treated as a single literal
		// double quote, they should be removed since they were not adjacent in the original expression
		strResult = interpretInternalDoubleQuotes(strResult) ;
		return strResult ;
	}

	/**
	 * Method initData.  Initialize internal data fields and validate the expression
	 * @param sText  The SAFS string expression
	 * @throws Exception  Error messages such as unmatched parentheses or illegal variable names
	 */
	protected void initData( String sText ) throws Exception {
		/* initialize the instance variables required when setting an expression */
		if(debugPrint) Log.info("SAFSExpression initializing for: __"+ sText +"__");
		booErrors = false ;
		strErrors = "" ;
		sExpression = encodeLiteralDoubleQuotes(sText) ;
		iExpressionlength = sExpression.length() ;
		vQuoteLocs = StringUtilities.locateQuotedSubStrings(sExpression) ;
		validateQuoteMatches() ;
		if( booErrors ) {
			// throw exception here because unmatched double quotes break the other validation checks
			throw new Exception(strErrors) ;
		}
		vSubStrings = StringUtilities.getSubStrings(sExpression,vQuoteLocs) ;
		vVariableNames = new Vector() ;

		setVariableNames() ;
		if( ! validateParens() ) {
			strErrors += "Expression: [" + sExpression + "] does not have correctly matched parentheses\n" ;
			booErrors = true ;
		}
		validateVariableNames() ;

		validateVariableAssignments() ;

		if( booErrors ) {
			throw new Exception(strErrors) ;
		}
	}

	/**
	 * @param sOperator  The first char will be tested against known numeric operators
	 * @return true if the first char in sOperator is a known numeric operator.
	 */
	protected boolean isNumericOperator(String sOperator){
		try{
			return (NUMERIC_OPERATORS.indexOf(sOperator.substring(0,1))> -1);
		}catch(Exception x){ return false; }
	}

	private String encodeLiteralDoubleQuotes(String sText) {
		/* literal double quotes are represented by "".
		 * there are problems with determining if adjacent double quotes
		 * are meant to be a single literal ", or are just two double
		 * quotes who are adjacent to each other. for this reason we
		 * will encode literal double quotes as an unsupported and non-printing
		 * ASCII character (vertical tab [char 11])
		 */

		// return sText with literal double quotes replaced (encoded) with vtab

		String strEncoded = "" ;
		boolean quoted = false;
		for( int str_idx = 0 ; str_idx < sText.length() ; str_idx++ ) {

			// if not inside a quote just keep the first quote
			if( sText.charAt(str_idx) != '"'){
				strEncoded += sText.substring(str_idx,str_idx+1) ;
				continue;
			}

			// if we are not in quotes then we begin a quoted substring
			if(!quoted){
				quoted=true;
				strEncoded += sText.substring(str_idx,str_idx+1) ;
				continue;
			}

			// we are in now a quoted substring

			// if the next char is also a DQ then we stay quoted
			if( StringUtilities.nextCharIsDQ(sText,str_idx) ) {
				strEncoded += expVarEncodedQuote;
				// skip over next character since it is a double quote
				str_idx++ ;
			}
			// otherwise, we have exited our quoted substring. just keep the quote
			else{
				quoted=false;
				strEncoded += sText.substring(str_idx,str_idx+1) ;
			}
		}

		if(debugPrint) Log.info("SAFSExpression encodeLiteralDoubleQuotes out: __"+ strEncoded +"__");
		return strEncoded ;

	}

	private String decodeLiteralDoubleQuotes(String sText) {
		/* literal double quotes are represented by "".
		 * there are problems with determining if adjacent double quotes
		 * are meant to be a single literal ", or are just two double
		 * quotes who are adjacent to each other. for this reason we
		 * will encode literal double quotes as an unsupported and non-printing
		 * ASCII characters.
		 */

		// return sText with the encoding characters replaced with two
		// consecutive double quotes

		String strDecoded = "" ;

		for( int str_idx = 0 ; str_idx < sText.length() ; str_idx++ ) {
			if( sText.charAt(str_idx) == quoteEncoded ) {
				/* strDecoded += "\"\"" ; */
				strDecoded += "\"" ;
			} else if( sText.charAt(str_idx) == varQuoteEncoded ) {
				strDecoded += "\"" ;
			} else {
				strDecoded += sText.substring(str_idx,str_idx+1) ;
			}
		}

		return strDecoded ;

	}

	/**
	 * Method setVariableNames.  Used to populate the protected field vVariableNames
	 */
	protected void setVariableNames() {
		/* create the vector containing the variable names within sExpression as strings
		 * within the vector keeping in mind that there may be quoted substrings
		 * a variable in safs is any text between ^ and one of the following:
		 * =, +, -, *, /, %, &, ), that is not quoted
		 * set the vVariableNames data member (field) vector to contain the vars
		 */

		int spos = StringUtilities.locateNextUnquotedSubstring(sExpression,"^",0,vQuoteLocs) ;
		while ( spos >= 0 && spos < iExpressionlength ) {
			/* locate the end of the variable by searching for one of the termination chars */

			int epos = StringUtilities.locateNextUnquotedSingleChar(sExpression,"=+-*/%& \t)",spos,vQuoteLocs) ;
			/* use end of string if epos not found */
			if( epos == -1 ) epos = iExpressionlength ;
			/* add the variable if it is not already present in the vector */
			if(! vVariableNames.contains(sExpression.substring(spos+1,epos))) {
				vVariableNames.addElement(sExpression.substring(spos+1,epos)) ;
			}
			spos = StringUtilities.locateNextUnquotedSubstring(sExpression,"^", epos + 1,vQuoteLocs) ;
		}

	}

	/**
	 * Method validateParens.  Returns true or false specifying wheather or not the expression has correctly matched parentheses
	 * @return boolean
	 */
	protected boolean validateParens() {
		/* make sure the expression has parentheses that are matched including nested correctly */
		/* return true of false if string s has matched parens or not */
		/* not very efficient code, but it is only run once for the expression */
		int sLen = sExpression.length() ;
		int inParen = 0 ;

		for( int i=0 ; i < sLen ; i++ ) {
			if( ! StringUtilities.isQuoted(sExpression,i,vQuoteLocs) ) {
				// only increment inParen if non-negative (which means invalid expression)
				if( sExpression.charAt(i) == '(' && inParen >= 0 ) {
					inParen++ ;
				} else if( sExpression.charAt(i) == ')' ) {
					inParen-- ;
				}
			}
		}

		if( inParen == 0 ) {
			return true ;
		} else {
			return false ;
		}

	}

	protected void validateVariableAssignments() {
		/* locate substrings between unquoted = and unquoted ^ to left
		 * using beginning of string if no ^ found, and validate those
		 * substrings as legal variable names (do not include ^ or =)
		 */
		int iend   = StringUtilities.locateNextUnquotedSubstring(sExpression,"=",0) ;
		while( iend != -1 ) {
			/* since we know how to search to the right, we reverse the substring
			 * of interest when we need to search to the left and search to the right
			 */
			String strLeftOfEq = sExpression.substring(0,iend) ;
			String strLeftRev = StringUtilities.reverse(strLeftOfEq) ;
			int irevbegin = StringUtilities.locateNextUnquotedSubstring(strLeftRev,"^",0) ;
			if( irevbegin == -1 ) irevbegin = strLeftRev.length() -1 ;
			int ibegin = iend - irevbegin ;
			String strAssignmentVariable = sExpression.substring(ibegin,iend) ;
			strAssignmentVariable = StringUtilities.TWhitespace(strAssignmentVariable) ;
			if( ! StringUtilities.isLegalVarName(strAssignmentVariable) ) {
				strErrors += "Variable: [" + strAssignmentVariable + "] is not a legal variable name\n" ;
				booErrors = true ;
			}
			iend   = StringUtilities.locateNextUnquotedSubstring(sExpression,"=",iend+1) ;
		}
	}

	/**
	 * Method validateVariableNames.  Make sure the variables within the expression are legally named.
	 * Set error string and flag for illegally named variables.
	 */
	protected void validateVariableNames() {
		/* make sure the variables are legally named */
		/* loop the variables and flag an error on the first illegal varname */
		for(int vec_idx = 0 ; vec_idx < vVariableNames.size() ; vec_idx++ ) {
			String varName = (String)vVariableNames.elementAt(vec_idx) ;
			if( ! StringUtilities.isLegalVarName(varName) ) {
				strErrors += "Variable: [" + varName + "] is not a legal variable name\n" ;
				booErrors = true ;
			}
		}

	}

	protected void validateQuoteMatches() {
		/* a basic validation that does not guarantee a well
		 * quoted expression, but does catch some impropperly
		 * quoted substrings is to check the vQuoteLocs vector
		 * which must contain an even number of items otherwise
		 * the expression is not well quoted
		 */
		 int size = vQuoteLocs.size() ;
		 if( size % 2 != 0 ) {
			strErrors += "Expression [" + sExpression + "] does not have matched double quotes" ;
			strErrors += "There must be an even number of non-literal double quotes and [" + size +"] were found" ;
		 	booErrors = true ;
		 }
	}

	/**
	 * Method getVarValue.  Retrieve the SAFS variable value
	 * @param strVarName  The name of the variable to retrieve
	 * @return String  The value of the variable
	 */
	protected String getVarValue( String strVarName ) {
		String strVarValue = varinterface.getValue(StringUtilities.TWhitespace(strVarName)) ;
		if(debugPrint) System.out.println("Retrieved "+ StringUtilities.TWhitespace(strVarName) +"="+ strVarValue);
		return strVarValue ;
	}

	/**
	 * Method setVarValue.  Set the specified variable to the specified value
	 * @param strVarName  The name of the variable to set
	 * @param strVarValue The value to set the variable to
	 */
	protected String setVarValue( String strVarName, String strVarValue ) {
		//return varinterface.setValue(StringUtilities.TWhitespace(strVarName),StringUtilities.LTWhitespace(strVarValue)) ;
		if(debugPrint) System.out.println("Setting "+ StringUtilities.TWhitespace(strVarName) +"="+ strVarValue);
		return varinterface.setValue(StringUtilities.TWhitespace(strVarName),strVarValue) ;
	}

	/**
	 * Recursively called with each nested parenthetical expression, if any.
	 * @param strExpr  The expression to evaluate
	 * @return String  The result of the evaluation
	 * @see #evalExpression()
	 */
	protected String evalExpression( String strExpr ) {
		/* recursively called with each nested parenthetical expression */

		/* need to locate text between left and right parens matching the
		 * parens so that nesting is allowed
		 */

		 // ( a + b ) + ( c * ( e - 3 ) ) - 1 for example

		 /* for efficiency, determine the locations of quotes whenever strExpr is changed
		  * so we can pass the vector to the string utilities that needs to deal with quotes
		  */
		 Vector vLocalQuoteLocs = StringUtilities.locateQuotedSubStrings(strExpr) ;

		 int iLeftParenLoc = StringUtilities.locateNextUnquotedSubstring(strExpr,"(",0,vLocalQuoteLocs) ;

		/* loop all left parens in expression so ( ) ( ) ( ) types are handled by
		 * while loop iteration rather than recursion
		 */
		while( iLeftParenLoc > -1 ) {

			 /* two cases, either another left paren will come next or a right paren.
			  * if left comes next, there is a nested grouping and we have to recurse
			  * if right comes next, the right matches the left and this subgroup can be
			  * evaluated as a simple expression
			  */
			 int iNextLeftParenLoc = StringUtilities.locateNextUnquotedSubstring(strExpr,"(",iLeftParenLoc+1,vLocalQuoteLocs) ;
			 int iRightParenLoc = StringUtilities.locateNextUnquotedSubstring(strExpr,")",iLeftParenLoc+1,vLocalQuoteLocs) ;

			 /* check and handle case that a negative sign is found vs a minus sign */
			 if(iNextLeftParenLoc < iRightParenLoc && iNextLeftParenLoc != -1 ) {
				/* find the matching right parenthesis */
			 	while( iNextLeftParenLoc < iRightParenLoc && iNextLeftParenLoc != -1 ) {
					  iLeftParenLoc = iNextLeftParenLoc ;
					  iNextLeftParenLoc = StringUtilities.locateNextUnquotedSubstring(strExpr,"(",iLeftParenLoc+1,vLocalQuoteLocs) ;
			 	}
				/* nested grouping found, recurse to evaluate */
				String strRecurseExpr = strExpr.substring(iLeftParenLoc+1,iRightParenLoc) ;
				if( debugPrint ) System.out.println("_____________Expr: " + strExpr + " RecurseExpr: " + strRecurseExpr) ;
				String strResult = evalExpression(strRecurseExpr) ;
			 	strExpr = StringUtilities.replaceString(strExpr,strResult,iLeftParenLoc,iRightParenLoc) ;
				vLocalQuoteLocs = StringUtilities.locateQuotedSubStrings(strExpr) ;
			 } else {
				/* the grouping is found, drop the parens and pass the simple expr
				 * to evalSimple, substitue the results, and continue checking parens
				 */
				 String strSimple = strExpr.substring(iLeftParenLoc+1,iRightParenLoc) ;
				 if( debugPrint ) System.out.println("_____________Expr: " + strExpr + " SimpleExpr: " + strSimple) ;
				 String strResult = evalSimple(strSimple) ;
				 strExpr = StringUtilities.replaceString(strExpr,strResult,iLeftParenLoc,iRightParenLoc) ;
				 vLocalQuoteLocs = StringUtilities.locateQuotedSubStrings(strExpr) ;
			 }

			 iLeftParenLoc = StringUtilities.locateNextUnquotedSubstring(strExpr,"(",0,vLocalQuoteLocs) ;
		}

		// this means we have a simple expression
		strExpr = evalSimple(strExpr) ;

		return strExpr ;

	}

	/** called internally by evalVariables **/
	protected String substituteVariables( String strExpr )
	{
		try{
			String strResult = new String(strExpr);
			if (! strResult.startsWith("^")) return strResult;
			if (strResult.indexOf("=") > 0)  return strResult;
			SAFSStringTokenizer toker = new SAFSStringTokenizer(strResult.substring(1), " ^\"\t");
			String varname  = "";
			try{ varname = toker.nextToken();}catch(Exception iex){ return strResult;}
			if (varname.length() > 0){
				strResult = '\"'+ getVarValue(varname) +'\"';
				try{ strResult += strExpr.substring(varname.length()+1);}
				catch(Exception iobx){;}
			}
			return strResult;
		}catch(Exception npx){;}
		return strExpr;
	}

	/** called internally by evalVariables **/
	protected String extractVariables( String strExpr )
	{
		try{
			String strResult = new String(strExpr);
			if (! strResult.startsWith("^")) return strResult;
			int eq = strResult.indexOf("=");
			if (eq < 2 ) return strResult;
			String varname  = StringUtilities.RTWhitespace(strResult.substring(1,eq));
			String varvalue = "";
			String infield  = "";
			String outfield = "";

			if (varname.length() > 0){

				try{ infield  = strResult.substring(eq+1);}
				catch(Exception iobx){;}

			    outfield = substituteVariables(infield);
			    varvalue = StringUtils.getTrimmedUnquotedStr(outfield);

			    setVarValue(varname, varvalue);
			    strResult = '\"'+ varvalue +'\"';
			}

			return strResult;
		}catch(Exception npx){;}
		return strExpr;
	}

	/**
	 * Method evalVariables.  Supports old-style variables processing when expressions are not enabled.
	 * This essentially requires that the first char of strExpr is a CARET (^).  If not, the
	 * input string is returned unaltered.
	 * @param strExpr  The expression to evaluate
	 * @return String  The result of the evaluation
	 */
	protected String evalVariables( String strExpr ) {

		String strResult = substituteVariables(strExpr);
		return extractVariables(strResult);
	}


	/**
	 * Method evalSimple.  A simple expression is one without parentheses
	 * @param strExpr  The simple expression to evaluate
	 * @return String  The simple expression evaluated
	 */
	protected String evalSimple(String strExpr) {
		/* Simple means an expression with no parentheses */

		/* result will be input expression of no operations to perform */
		String strResult = strExpr ;

		/* remove unquoted whitespace */
		String trimExpr = StringUtilities.TWhitespace(strExpr) ;

    	if(debugPrint) Log.info("SAFSExpression evalSimple processing: __"+ trimExpr +"__");

		/* strAssignments will be created to contain variable assignment items */
		String strAssignments = "" ;

		/* the left side of the expression may contain variable assignments
		 * we will handle this by grabbing the text to the left
		 * of an equal sign (if present), using the cleaned result
		 * in the evaluation, and then looping the assignment variables
		 * obtained and performing the assignments
		 */

		if( StringUtilities.locateNextUnquotedSubstring(trimExpr,"=",0) != -1 ) {
			int iCleanLeftLoc = trimExpr.length() - StringUtilities.locateNextUnquotedSubstring(StringUtilities.reverse(trimExpr), "=",0) ;
			strAssignments = trimExpr.substring(0,iCleanLeftLoc) ;
			trimExpr = trimExpr.substring(iCleanLeftLoc) ;
			trimExpr = StringUtilities.LTWhitespace(trimExpr) ;
		}

		/* the order of operations is: *, /, and % at same level from L to R
		 * then +, -, and & at the next level from L to R
		 * handleOperators() does the grunt work for operators at the same precedence
		 */

		strResult = handleOperators(trimExpr,"*/%") ;
		strResult = handleOperators(strResult,"+-&") ;

    	if(debugPrint) Log.info("SAFSExpression evalSimple operators processed: __"+ strResult +"__");
		/* there is a chance that the expression is only a variable name
		 * if so, we just dereference the variable and return the result */
		try{
			if( StringUtilities.TWhitespace(strResult).charAt(0) == '^' ) {
				strResult = encodeDereferencedVariable(StringUtilities.TWhitespace(strResult)) ;
			}
		// TWhitespace may give us an EMPTY string and charAt(0) will throw this
		}catch(StringIndexOutOfBoundsException sob){;}

    	strResult = StringUtils.getTrimmedUnquotedStr(strResult);

		/* do the variable assignments if any are present */
		if( ! strAssignments.equals("") ) {
			String strVars [] = strAssignments.split("=") ;
	        for (int i = 0; i < strVars.length; i++) {
	        	String strVName = StringUtilities.TWhitespace(strVars[i]) ;
	        	// use .substring(1) to skip the ^ preceding the varName
	        	strVName = strVName.substring(1);

	        	String strVValue = decodeLiteralDoubleQuotes(strResult);

	        	if(debugPrint) Log.info("SAFSExpression setting assignment var: "+ strVName +"="+strVValue);
				setVarValue(strVName,strVValue) ;
			}
		}
    	if(debugPrint) Log.info("SAFSExpression evalSimple returns: __"+ strResult +"__");
		return strResult ;
	}


	/**
	 * Method handleOperators.  The grunt work of handling operators at the same precedence.
	 * @param strExpr  The expression to evaluate
	 * @param strOps  The operators to handle at the same precedence.  For example "+-&" are
	 * evaluated at the same precedence.
	 * @return String  The result of the expression evaluation
	 */
	protected String handleOperators(String strExpr, String strOps) {

		int iOpStartidx = StringUtilities.locateNextUnquotedNonWhiteSpace(strExpr,0) ;
		int iOperatorLoc = StringUtilities.locateNextUnquotedSingleChar(strExpr,strOps,iOpStartidx) ;
		if( iOperatorLoc != -1 ) {
		 	if ( strExpr.charAt(iOperatorLoc) == '-' && iOperatorLoc == iOpStartidx ) { // negative sign if true, get next "-"
				iOperatorLoc = StringUtilities.locateNextUnquotedSingleChar(strExpr,strOps,iOperatorLoc+1) ;
			}
		}

		/* process while there are operators at the current precedence level */
		while(iOperatorLoc > 0 ) {

		 	String strOperator = strExpr.substring(iOperatorLoc,iOperatorLoc+1) ;
			String strRightOperand = getRightOperand(strExpr,iOperatorLoc) ;
			String strLeftOperand = getLeftOperand(strExpr,iOperatorLoc) ;

			int ibegin = getLeftOperandBegin(strExpr,strLeftOperand,iOperatorLoc) ;
			int iend   = getRightOperandEnd(strExpr,strRightOperand,iOperatorLoc) ;

			if ( debugPrint ) {
				System.out.println("") ;
				System.out.println("pre eval strExpr: " + strExpr ) ;
				System.out.println("ibegin: " + ibegin + "  iend: " + iend + "  iOperatorLoc: " + iOperatorLoc) ;
				System.out.println(strExpr) ;
				System.out.println(StringUtilities.spacePad(ibegin) + "^" + StringUtilities.spacePad(iOperatorLoc-ibegin-1) + "^" + StringUtilities.spacePad(iend-iOperatorLoc-1) + "^") ;
				System.out.println("Left:" + strLeftOperand + " Operator:" + strOperator + " Right:" + strRightOperand) ;
			}
			// exit if no valid operation remains
			if( iOperatorLoc > iend ){
				if(debugPrint) {
					System.out.println ("iOperatorLoc out of bounds for operation.");
			        System.out.println ("return strExpr: " + strExpr ) ;
				}
				break;
			}

			/* the operands may be variables.  replace variables with their values */
			strLeftOperand = encodeDereferencedVariable(StringUtilities.TWhitespace(strLeftOperand));
			strRightOperand = encodeDereferencedVariable(StringUtilities.TWhitespace(strRightOperand));

			if ( debugPrint ) {
				Log.info("SAFSExpression handleOperation __" + strLeftOperand + "___" + strOperator + "___" + strRightOperand +"__") ;
				System.out.println("Left:" + strLeftOperand + " Operator:" + strOperator + " Right:" + strRightOperand) ;
			}

			String strResult = evalPrimative(strLeftOperand,strOperator,strRightOperand) ;

			if ( debugPrint ) System.out.println("Result Equals: " + strResult ) ;

			strExpr = StringUtilities.replaceString(strExpr,strResult,ibegin,iend) ;

			if ( debugPrint ) System.out.println("pst eval strExpr: " + strExpr ) ;

			/* tricky part is beginning the operator search from ibegin+strResult.length()-1
			 * because we are parsing L-R at the operator precedence and we DO NOT want to
			 * get tricked by string concats that result in text that looks like an expression
			 * e.g if ^x=3 and ^y=-2, ^x&^y&"hithere" may resolve to 3-2&"hithere" -> 1hithere
			 * if the concat gets performed and further expression evauations on its result
			 */
			iOpStartidx = StringUtilities.locateNextUnquotedNonWhiteSpace(strExpr,ibegin+strResult.length()-1) ;
			iOperatorLoc = StringUtilities.locateNextUnquotedSingleChar(strExpr,strOps,iOpStartidx) ;

			if ( iOperatorLoc > -1 ) { // negative sign if true, get next "-"
			 	strOperator = strExpr.substring(iOperatorLoc,iOperatorLoc+1) ;
			 	if ( strOperator.equals("-") && iOperatorLoc == 0 ) { // negative sign if true, get next "-"
					iOperatorLoc = StringUtilities.locateNextUnquotedSingleChar(strExpr,strOps,iOperatorLoc+1) ;
	 			}
			}

		}

		return strExpr ;

	}

	/**
	 * Method dereferenceVariable.  If strText is a variable, return its value
	 * @param strText  The string to check wich may or may not be a variable
	 * @return String  The string strText or the value of the variable if strText is a variable
	 */
	protected String dereferenceVariable(String strText) {
		/* may or may not be a variable.  deref if var else return strText */
		if((strText==null)||(strText.length()==0)) return "";
		if( strText.charAt(0) == '^' ) {
			strText = getVarValue(strText.substring(1)) ;
		}
		return strText ;
	}

	/**
	 * Method encodeDereferencedVariable.  If strText is a variable, return its value.
	 * Encode any embedded quotes with the non-printable encoding character.
	 * @param strText  The string to check wich may or may not be a variable
	 * @return String  The string strText or the value of the variable if strText is a variable
	 */
	protected String encodeDereferencedVariable(String strText) {
		// may or may not be a variable.  deref if var else return strText
		if((strText==null)||(strText.length()==0)) return "";
		if( strText.charAt(0) == '^' ) {
			strText = getVarValue(strText.substring(1)) ;
			strText = "\""+ StringUtilities.findAndReplace(strText, "\"", expVarEncodedQuote)+"\"";
		}
		return strText ;
	}

	/**
	 * Method getRightOperand.  Using infix notation, the right operand is the string to the right of the
	 * operator at iOperatorLoc up to a termination character.
	 * @param strExpr  The expression
	 * @param iOperatorLoc  The int location of the operator within strExpr
	 * @return String  The right operand
	 */
	protected String getRightOperand(String strExpr, int iOperatorLoc) {
		/* return the operand to the right of iOperatorLoc
		 * the operator will begin with the string one place
		 * to the right of iOperatorLoc and will end with either
		 * the end of strExpr, or the presence of another non quoted
		 * operator
		 */
		 Vector localQuoteLocs = StringUtilities.locateQuotedSubStrings(strExpr) ;

		 int begin = StringUtilities.locateNextNonWhiteSpace(strExpr,iOperatorLoc + 1) ;
		 int end = strExpr.length() - 1 ;
		 if ( begin == end) begin = iOperatorLoc + 1 ;
		 int nextOp = StringUtilities.locateNextUnquotedSingleChar(strExpr, "*/%+-&", begin, localQuoteLocs) ;
		 /* check and handle case that a negative sign is found vs a minus sign */
		 if ( nextOp != -1 && strExpr.charAt(nextOp) == '-' && nextOp == begin ) {
			nextOp = StringUtilities.locateNextUnquotedSingleChar(strExpr, "*/%+-&", begin+1, localQuoteLocs) ;
		 }
		 if( nextOp > -1 ) end = nextOp - 1 ;

		 /* we now know the location of the right operand as a substring within strExpr
		  * it lies between begin and end inclusive
		  */

		 String strRightOperand = strExpr.substring(begin,end+1) ;

		 return StringUtilities.LTWhitespace(strRightOperand) ;

	}

	private int getRightOperandEnd(String strExpr,String strRightOperand,int iOperatorLoc) {
		/* return the string position of the end of the right operand within the expression */
		int iStart = StringUtilities.locateNextSubstring(strExpr,strRightOperand,iOperatorLoc) ;
		int iEnd = iStart + strRightOperand.length() -1 ;
		return iEnd ;
	}

	/**
	 * Method getLeftOperand.  Using infix notation, the left operand is the string to the left of the
	 * operator at iOperatorLoc up to a termination character.
	 * @param strExpr  The expression
	 * @param iOperatorLoc  The int location of the operator within strExpr
	 * @return String  The left operand
	 */
	protected String getLeftOperand(String strExpr, int iOperatorLoc) {
		/* return the operand to the left of iOperatorLoc
		 * to do this, we'll just reverse the string, translate the operator loc
		 * and get a RigthOperand, then reverse it get make the LeftOperand
		 */
		String strReversed = StringUtilities.reverse(strExpr) ;
		int iOpLocRev = strExpr.length() - iOperatorLoc - 1 ;
		String strLeftOpRev = getRightOperand(strReversed,iOpLocRev) ;
		String strLeftOperand = StringUtilities.reverse(strLeftOpRev) ;
		/* check to see if left operand is negative
		 * we look at the iprefix (location to left of operator minus the operand length)
		 * make sure it is within the expression string, and is a '-' char, and the char to
		 * the left of it is any operator (* / % + - &).  this identifies a negative sign
		 */
		int numWhiteSpaces = 0 ;
		while( strExpr.charAt(iOperatorLoc-1-numWhiteSpaces) == ' ' || strExpr.charAt(iOperatorLoc-1-numWhiteSpaces) == '\t' ) {
	       	numWhiteSpaces++ ;
		}
		int iprefix = iOperatorLoc - 1 -numWhiteSpaces - strLeftOperand.length() ;  // location of char to left of LeftOperator
		/* iprefix must be within the string and be a - character */
		if ( iprefix > -1 && strExpr.charAt(iprefix) == '-' ) {
			/* need to check position to left of iprefix if there is another position to the left */
			if( iprefix == 0 ) {
				/* no position to left, so this - is a negative sign */
		    	strLeftOperand = "-" + strLeftOperand ;
			} else if( StringUtilities.locateNextUnquotedSingleChar(
		     			strExpr.substring(iprefix-1,iprefix), "*/%+-& \t", 0) > -1) {
							/* position to left is whitespace or another operator, so this - is a negative sign */
							strLeftOperand = "-" + strLeftOperand ;
		    }
		}

		return strLeftOperand ;

	}

	private int getLeftOperandBegin(String strExpr,String strLeftOperand,int iOperatorLoc) {
		/* return the string position of the beginning of the left operand within the expression */

		/* work with reversed strings so can search forward using StringUtilities functions */
		String strRevExpr = StringUtilities.reverse(strExpr) ;
		String strRevLeftOperand = StringUtilities.reverse(strLeftOperand) ;
		int iOpLocRev = strExpr.length() - iOperatorLoc - 1 ;

		/* look forward in the reversed expression for the reversed left operand */
		int iStart = StringUtilities.locateNextSubstring(strRevExpr,strRevLeftOperand,iOpLocRev) ;
		int iEnd = iStart + strRevLeftOperand.length() -1 ;

		/* since found in reversed strings need to translate the end location to forward equivalent */
		iEnd = strExpr.length() - 1 - iEnd ;

		return iEnd ;

	}

	/**
	 * Capture NumberFormatExceptions when creating a BigDecimal from a String.
	 * Return 0 when the exception is encountered.
	 * @param decimal -- String representation of a (Big)Decimal number, presumably.
	 * @return BigDecimal object
	 */
	private BigDecimal bigDecimal(String decimal){
		try{
			return new BigDecimal(decimal);
		}
		catch(NumberFormatException nfx){
			Log.debug("SAFSExpression BigDecimal NumberFormatException on '"+ decimal +"'", nfx);
			return new BigDecimal(0);
		}
	}

	/**
	 * Retrieve plain value with no scientific notation.  This was necessitated by a change in
	 * Java 1.5.0 where scientific notation is now used in the standard toString method.  Thus,
	 * this method allows us to still handle both 1.4 and 1.5 versions of the BigDecimal class.
	 * @param decimal BigDecimal to retrieve "plain" toString output without scientific notation.
	 * @return
	 */
	private String toPlainString(BigDecimal decimal){
		Method toPlainString = null;
		try{
			Class bd = decimal.getClass();
			toPlainString = bd.getMethod("toPlainString", new Class[0]);
			return (String) toPlainString.invoke(decimal, new Object[0]);
		}
		catch(Exception x){
			return decimal.toString();
		}
	}

	/**
	 * Method evalPrimative.  Primative means an expression in infix notation as "LeftOperand Operator RightOperand" only
	 * @param strLeftOperand  The left operand
	 * @param strOperator  The operator
	 * @param strRightOperand  The right operand
	 * @return String  The result of evaluating "LeftOperand Operator RightOperand"
	 */
	protected String evalPrimative(String strLeftOperand, String strOperator, String strRightOperand) {

		/* Primative means an infix expression as "LeftOperand Operator RightOperand" */

		String strResult = "" ;

		BigDecimal bdLeft ;
		BigDecimal bdRight ;
		BigDecimal dbResult ;

		/* round the result to numDecimalPlaces which should likely ~O machine epsilon
		 * on wxp.  there should be a better solution but this is the best
		 * found at this time.  this rounding is only applicable to non-concatonation
		 * and non-modulus operations
		 */

		char cOperator = strOperator.charAt(0) ;
		strLeftOperand = StringUtils.getTrimmedUnquotedStr(strLeftOperand) ;
		strRightOperand = StringUtils.getTrimmedUnquotedStr(strRightOperand) ;
		Log.info("SAFSEXpression evaluating: "+ strLeftOperand + cOperator + strRightOperand );

		switch  (cOperator) {
			case '&':
				strResult = "\""+ strLeftOperand + strRightOperand +"\"";
				break ;
			case '+':
				bdLeft = bigDecimal(strLeftOperand);
				bdRight = bigDecimal(strRightOperand) ;
				dbResult = bdLeft.add(bdRight) ;
				strResult = toPlainString(dbResult) ;
				break ;
			case '-':
				bdLeft = bigDecimal(strLeftOperand) ;
				bdRight = bigDecimal(strRightOperand) ;
				dbResult = bdLeft.subtract(bdRight) ;
				strResult = toPlainString(dbResult) ;
				break ;
			case '*':
				bdLeft = bigDecimal(strLeftOperand) ;
				bdRight = bigDecimal(strRightOperand) ;
				dbResult = bdLeft.multiply(bdRight) ;
				strResult = toPlainString(dbResult) ;
				break ;
			case '/':
				bdLeft = bigDecimal(strLeftOperand) ;
				bdRight = bigDecimal(strRightOperand) ;
				dbResult = bdLeft.divide(bdRight,numDecimalPlaces,BigDecimal.ROUND_HALF_UP) ;
				strResult = toPlainString(dbResult) ;
				break ;
			case '%':
				int ileft  = 0;
				int iright = 0;
				try{ ileft = Integer.valueOf(strLeftOperand).intValue();}
				catch(NumberFormatException nfx){;}
				try{ iright = Integer.valueOf(strRightOperand).intValue();}
				catch(NumberFormatException nfx){;}
				strResult = Integer.toString(ileft % iright) ;
				break ;
		}

		if(debugPrint) Log.debug("SAFSEXpression raw result: "+ strResult );

		/* if the operation is not concatonation, strip lead/trailing zeros
		 * according to the boolean options stripLeadZero and stripTrailingZeros
		 * and likewise strip trailing decimal points if desired
		 * Note, we don't do this for concatonation because these items are important
		 * for concatonation evaluation
		 */
		if( stripLeadZero && cOperator != '&' ) {
			strResult = strResult.replaceAll("^0", "") ;
		}
		if( stripTrailingZeros && cOperator != '&' ) {
			strResult = strResult.replaceAll("\\.0*$", ".") ;
		}
		if( stripTrailingDecimalPoint && cOperator != '&' && strResult.indexOf('.') > -1 ) {
			// if the result contains a decimal place, we reverse and replace lead zeros, and reverse the result
			// because it is difficult to catch 100 -> 100, 100.0 -> 100, and 1.6500 -> 1.65
			strResult = StringUtilities.reverse(StringUtilities.reverse(strResult).replaceAll("^0*", "")) ;
		}
		/* (Carl Nagle)if NOT a concatenation result:
		 * special case if the result is a period (dot, decimal point) or empty
		 * it means the result is zero, but has been stripped in the above
		 * so set the result to a string zero before returning
		 */
		if ((cOperator != '&')&&(( strResult.equals("."))||(strResult.equals("")))) {
			strResult = "0" ;
		}

		if(debugPrint) Log.debug("SAFSEXpression strResult: "+ strResult );
		return strResult ;
	}

	/**
	 * Method interpretInternalDoubleQuotes.  Return strText with the double quotes interpreted
	 * two consecutive double quotes are interpreted as a single literal double quote
	 * double quotes not adjacent to other double quotes are meant for quoting only
	 * the interpretation replaces "" with " and replaces " with nothing and leaves other
	 * characters unchanged
	 * @param strText  The string to interpret its double quotes
	 */
	private String interpretInternalDoubleQuotes( String strText ) {
		/* private method due to how specific it is to this class
		 * two consecutive double quotes are interpreted as a single literal double quote
		 * double quotes not adjacent to other double quotes are meant for quoting only
		 * the interpretation replaces "" with " and replaces " with nothing and leaves other
		 * characters unchanged.  special case:  two consecutive double quotes that appear due to concatonation
		 * i.e. not in the original expression, should not be treated as a single literal
		 * double quote, they should be removed since they were not adjacent in the original expression
		 */

		// since literal double quotes have been encoded, replace all " with empty

		// decode the literal double quotes so can replace "" with " and do so
		String strResult = decodeLiteralDoubleQuotes(strText) ;

		return strResult ;

	}

	private static class VariableServ implements SimpleVarsInterface{
		private Map<String,String> varStore = new HashMap<>();

		@Override
		public String setValue(String var, String value) {
			return varStore.put(var, value);
		}

		@Override
		public String getValue(String var) {
			return varStore.get(var);
		}

	}

	private static void _verifyExpresion(String expression, String expectedResult, SafsExpression se){
		try {
			String result = null;
			se.setExpression(expression);
			result = se.evalExpression();
			assert result.equals(expectedResult);
		} catch (Exception e) {
			System.err.println("Failed to eval '"+expression+"', due to "+e);
		}
	}

	private static void _simple_test(SafsExpression se){
		SimpleVarsInterface varServ = se.getVarInterface();
		String expression = null;
		String expectedResult = null;

		expression = "6+9";//"6+9"
		expectedResult = "15";//"15"
		_verifyExpresion(expression, expectedResult, se);

		expression = "\"6+9\"";//""6+9""
		expectedResult = "6+9";//"6+9"
		_verifyExpresion(expression, expectedResult, se);

		String var = "var";
		String operand1 = "5";
		String operand2 = "6";

		//Set "5" to variable "var"
		varServ.setValue(var, operand1);

		expression = "\"^"+var+"\"";//""^var""
		expectedResult = "^"+var;//"^var"
		_verifyExpresion(expression, expectedResult, se);

		expression = "^"+var;//"^var"
		expectedResult = operand1;//"5"
		_verifyExpresion(expression, expectedResult, se);

		expression = operand2+"+^"+var+"";//"6+^var"
		expectedResult = String.valueOf(Integer.parseInt(operand1)+Integer.parseInt(operand2));
		_verifyExpresion(expression, expectedResult, se);

		expression = "\""+operand2+"+^"+var+"\"";//""6+^var""
		expectedResult = operand2+"+^"+var+"";//"6+^var"
		_verifyExpresion(expression, expectedResult, se);

		expression = operand2+"+\"^"+var+"\"";//"6+"^var""
		expectedResult = operand2;//"6"
		_verifyExpresion(expression, expectedResult, se);

	}

	public static void main(String[] args){
		VariableServ varServ = new VariableServ();
		SafsExpression se = new SafsExpression(varServ);

		_simple_test(se);
	}
}
