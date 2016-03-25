/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.text;

import java.util.regex.Pattern;

/**
 * Provide different types of text comparisons:
 * <p> 
 * <ul>
 *     <li>glob  : supports &quot;*&quot; and &quot;?&quot; wildcarding.
 *     <li>regexp: case-sensitive matches based on a regular expression.
 *     <li>regexpi: matches based on a regular expression, ignoring case.
 *     <li>exact: an exact verbatim match.
 * </ul>
 *
 * @author Carl Nagle
 */
public class Comparator {

	/**
	 * Attempt a 'glob' pattern match on a given text string.
	 * The pattern supports "*" and "?" wildcarding of a case-insensitive compare.
	 * <p>
	 * The 'glob:' pattern prefix (Selenium 1.0) is assumed to already be removed.
	 * 
	 * @param text String to compare against the 'glob' pattern.
	 * @param glob String pattern to use for comparison.
	 * @return true if the text matches the provided 'glob' pattern. false otherwise.
	 * if both parameters are null this is considered a match.
	 */
	public static boolean isGlobMatch(String text, String glob){
		if(text == null && glob == null) return true;
		if(text == null || glob == null) return false;
	    String rest = null;
	    final char q = '?';
	    final char s = '*';
	    
	    int pos = glob.indexOf(s);
	    if (pos != -1) {
	        try{ rest = glob.substring(pos + 1);}
	        catch(IndexOutOfBoundsException x){ rest = null; }
	        glob = glob.substring(0, pos);
	    }
	    if (glob.length() > text.length())
	        return false;

	    // handle the part up to the first *
	    for (int i = 0; i < glob.length(); i++)
	        if (glob.charAt(i) != q && !glob.substring(i, i + 1).equalsIgnoreCase(text.substring(i, i + 1)))
	            return false;

	    // recurse for the part after the first *, if any
	    if (rest == null) {
	        return glob.length() == text.length();
	    } else {
	        for (int i = glob.length(); i <= text.length(); i++) {
	            if (isGlobMatch(text.substring(i), rest))
	                return true;
	        }
	        return false;
	    }
	}

	/**
	 * Attempt an 'exact' pattern match on a given text string.<br>
	 * The pattern is assumed to be an exact text match.
	 * <p>
	 * The 'exact:' pattern prefix (Selenium 1.0) is assumed to already be removed.
	 * 
	 * @param text String to compare against the bench pattern.
	 * @param bench String to match exactly.
	 * @return true if the text matches the provided bench pattern exactly. false otherwise.
	 * if both parameters are null this is considered a match.
	 */
	public static boolean isExactMatch(String text, String bench){		
		if(text == null && bench == null) return true;
		return ((text == null)||(bench == null)) ? false : text.equals(bench);
	}

	/**
	 * Attempt a 'regexp' pattern match on a given text string.<br>
	 * The pattern is assumed to be a regular expression.
	 * <p>
	 * The 'regexp:' pattern prefix (Selenium 1.0) is assumed to already be removed.
	 * 
	 * @param text String to compare against the 'regexp' pattern.
	 * @param regexp String pattern.
	 * @return true if the text matches the 'regexp' pattern. false otherwise.
	 * if both parameters are null this is considered a match.
	 */
	public static boolean isRegexpMatch(String text, String regexp){
		if(text == null && regexp == null) return true;
		return ((text == null)||(regexp == null)) ? false : text.matches(regexp);
	}

	/**
	 * Attempt a 'regexp' pattern match on a given text string, ignoring case.<br>
	 * <p>
	 * The 'regexpi:' pattern prefix (Selenium 1.0) is assumed to already be removed.
	 * 
	 * @param text String to compare against the 'regexp' pattern.
	 * @param regexp String pattern.
	 * @return true if the text matches the 'regexp' pattern, ignoring case. false otherwise.
	 * if both parameters are null this is considered a match.
	 */
	public static boolean isRegexpiMatch(String text, String regexp){
		if(text == null && regexp == null) return true;
		if ((text == null)||(regexp == null)) return false;
		Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
		return pattern.matcher(text).matches();
	}
}
