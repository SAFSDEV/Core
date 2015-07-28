package org.safs.text;

import org.safs.*;
import org.apache.regexp.*;

//needed for the Java 1.4 implementation
//import java.util.regex.Pattern;

/*******************************************************************************************
 *
 * Class: StringProcessor
 *
 * Methods to do special String Processing.
 *<p>
 * At this time the class uses Regular Expression support provided by the Jakarta/Apache 
 * regexp project to avoid a requirement for Java V1.4.
 * <p>
 * The JAR file containing the required files should be provided by a standard SAFS install.<br>
 * <p>
 * Software Automation Framework Support (SAFS) http://safsdev.sourceforge.net<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 * Apache Jakarta Regexp Project (Regexp) http://jakarta.apache.org/regexp<br>
 * <p>
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 *********************************************************************************************/
public class StringProcessor
{
    protected static final String JAKARTA_REGEX = "jakarta-regexp-X.X.jar";
    int status = 0;

	/*********************************************************************************
	* This method takes three string arguments: a string, and two Strings for
	* the start and stop regular expressions.  It attempts to find the substring in
	* string that begins with the text immediately following the first regular
    * expression and continuing until it finds the second regular expression.
    *
    * The function returns substring, or empty string if not found.
    * <p>
    * The following status codes can occur:<br>
    * 0  - Success<br>
    * 38 - JavaError - Regular Expression support not found<br>
    * 47 - Invalid Value(s)<br>
	**********************************************************************************/
	public String getEmbeddedSubstring(String string, String regexStart, String regexStop)
	{
		if ((string     == null) ||
		    (regexStart == null) ||
		    (regexStop  == null)){
			status = 47; // Invalid Values
			return "";
		}
/*Changed: April 31, 2004 by Ben Tomlinson
 *save for future conversion to JV1.4
 *					try{
 *					  if (Pattern.matches(".*" + regexStart + ".*", string)) {
 *						Pattern p1 = Pattern.compile(regexStart);
 *						String[] s1 = p1.split(string, 2);
 *						Pattern p2 = Pattern.compile(regexStop);
 *						String[] s2 = p2.split(s1[1], 2);
 *						return s2[0];
 *					  }
 *      			}catch(NoClassDefFoundError no4){}						
 */

		status = 0;		
	    try{
			/* Changed: April 31, 2004 by Ben Tomlinson
			 * this line was removed and replaced with the following line to increase performance
			 * the .* pieces are not needed when using the Apache RE object but are needed for the Java 1.4 Pattern object
			 *
			 * RE matcher = new RE(".*" + regexStart + ".*");
			 */
			RE matcher = new RE(regexStart);

			if(matcher.match(string)){
			    RE p1 = new RE(regexStart);
			    p1.match(string);
				int start = p1.getParenEnd(0);
				String clipped = string.substring(start);
				RE p2 = new RE(regexStop);
				boolean hasEnd = p2.match(clipped);
				if(hasEnd){					
					int end = p2.getParenStart(0);							
				    clipped = clipped.substring(0, end);				    
			        return clipped;
				}
		    }
		}
		catch(NoClassDefFoundError no3){
			status = 38; // Java Error
			String regex  = GENStrings.text(GENStrings.REGULAR_EXPRESSIONS, 
			                "Regular Expressions");
			String nosupport = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
			                "Support for "+regex+" not found!", 
			                regex);
			String install = FAILStrings.convert(FAILStrings.ADD_TO_CLASSPATH, 
			                "Install '"+JAKARTA_REGEX+"' in CLASSPATH", 
			                JAKARTA_REGEX);						                
			String message = "StringProcessor.getEmbeddedSubstring:" +
			                 nosupport+"  "+install;
			                 
			throw new SAFSRegExNotFoundException(message);
		}catch(Exception ex){
			ex.printStackTrace();
			String message = "StringProcessor.getEmbeddedSubstring:" +
			                 "  "+ex.getMessage();
			throw new SAFSRegExNotFoundException(message);
	    }
		return ""; // no match found
	} /* function */

    /** 
     * returns the status code of the last function called.
     * The status codes are STAF return codes. 
     * <p>
     * The following codes are implemented:<br>
     * 0  - Success<br>
     * 38 - JavaError - Regular Expression support not found<br>
     * 47 - Invalid Value(s)<br>
     */
	public int returnStatus() {
		return status;
	} /* function */

} /* class */
