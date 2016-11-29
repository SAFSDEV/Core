/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 *   <br>   Sep 23, 2003    (DBauman) Original Release
 *   <br>   Oct 02, 2003    (DBauman) moved to package org.safs because no dependency on rational
 *   <br>   Nov 17, 2003    (javadoug) testing writing to cvs from home<br>
 *   <br>   Jan 13, 2004    (CANAGL) Fixed findSubstring to accept caret in varname strings.<br>
 *                                   Fixed Keyword GetSubstringsInString to be properly recognized.<br>
 *   <br>   Oct 18, 2004    (RDucharme) Fixed subString to handle arguments as specified in document.<br>
 *   <br>   Nov 16, 2004    (RDucharme) Fixed GetTrimmedField to trim the returned field.<br>
 *   <br>   Jan 10, 2005    (RDucharme) Added GetREDelimitedFieldCount and GetREDelimitedField. Removed GetMultiDelimitedFieldCount<br>
 *   <br>				and GetMultiDelimitedField references.<br>
 *   <br>   Mar 16, 2005    (RDucharme) Improved Error checking for GetREDelimitedFieldCount and GetREDelimitedField.<br>
 *   <br>   APR 14, 2005    (CANAGL) GetSystemEnviron fixed to return SCRIPT_NOT_EXECUTED if unfulfilled.<br>
 *   <br>   APR 20, 2005    (RLawler) Updated log messages for Index() and findSubstring() (RJL).<br>
 *   <br>   MAY 02, 2005    (CANAGL) Revampled GetSystemEnviron to use STAF/Env variables.
 * 									 Added GetSystemUser command.
 * 	 <br>	SEP 24, 2008	(LeiWang)	Modified method getNextDelimiterIndex(), treate the parameter startindex.
 * 										See defect S0537064
 * 	 <br>	OCT 29, 2008	(LeiWang)	Add keyword GetMultiDelimitedFieldCount and GetMultiDelimitedField.
 * 										See defect S0544477
 *   <br>	FEB 10, 2009	(JunwuMa)   Modify replace() to keep the source string unchanged if a substring for replacement 
 *   <br>                               is not found in it. Fix defect S0560893.
 *   <br>	APR 18, 2013	(sbjlwa)    Modify replace() to set source string to variable if no replacement occur. See S0963164.
 *   <br>	NOV 29, 2016	(sbjlwa)    Modified compare(): support regex comparison.
 **/
package org.safs;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.text.StringProcessor;


/**
 * Process a string driver commands.
 * Instantiated by DCDriverCommand
 *   
 * @author Doug Bauman
 * @since   Sep 23, 2003
 **/
public class DCDriverStringCommands extends DriverCommand {

  public static final String LENGTH                      = "Length";
  public static final String COMPARE                     = "Compare";
  public static final String CONCATENATE                 = "Concatenate";
  public static final String TOUPPERCASE                 = "ToUpperCase";
  public static final String TOLOWERCASE                 = "ToLowerCase";
  public static final String LEFTTRIM                    = "LeftTrim";
  public static final String RIGHTTRIM                   = "RightTrim";
  public static final String TRIM                        = "Trim";
  public static final String LEFT                        = "Left";
  public static final String RIGHT                       = "Right";
  public static final String SUBSTRING                   = "SubString";
  public static final String FINDSUBSTRINGINSTRING       = "FindSubStringInString";
  public static final String GETSUBSTRINGINSTRING        = "GetSubStringsInString";
  public static final String INDEX                       = "Index";
  public static final String REPLACE                     = "Replace";
  public static final String CLEANSTRING                 = "CleanString";
  public static final String GETFIELD                    = "GetField";
  public static final String GETTRIMMEDFIELD             = "GetTrimmedField";
  public static final String GETFIXEDWIDTHFIELD          = "GetFixedWidthField";
  public static final String GETNEXTDELIMITERINDEX       = "GetNextDelimiterIndex";
  public static final String GETFIELDCOUNT               = "GetFieldCount";
  public static final String GETFIELDARRAY               = "GetFieldArray";
  public static final String GETREDELIMITEDFIELDCOUNT    = "GetREDelimitedFieldCount";
  public static final String GETREDELIMITEDFIELD         = "GetREDelimitedField";
  public static final String GETSYSTEMENVIRON            = "GetSystemEnviron";
  public static final String GETSYSTEMUSER               = "GetSystemUser";
  public static final String GETMULTIDELIMITEDFIELDCOUNT = "GetMultiDelimitedFieldCount";
  public static final String GETMULTIDELIMITEDFIELD      = "GetMultiDelimitedField";
  
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public DCDriverStringCommands () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is the driver command processor for string commands.
   ** Current commands :<br>
   ** <br>
   ** <br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing; processedCommand is set to false
   * if we do not recognize this command
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  public void process() {
    try {
      if (testRecordData.getCommand().equalsIgnoreCase(LENGTH)) {
        length();
      } else if (testRecordData.getCommand().equalsIgnoreCase(COMPARE)) {
        compare();
      } else if (testRecordData.getCommand().equalsIgnoreCase(CONCATENATE)) {
        concatenate();
      } else if (testRecordData.getCommand().equalsIgnoreCase(TOUPPERCASE)) {
        toUpperCase();
      } else if (testRecordData.getCommand().equalsIgnoreCase(TOLOWERCASE)) {
        toLowerCase();
      } else if (testRecordData.getCommand().equalsIgnoreCase(LEFTTRIM)) {
        leftTrim();
      } else if (testRecordData.getCommand().equalsIgnoreCase(RIGHTTRIM)) {
        rightTrim();
      } else if (testRecordData.getCommand().equalsIgnoreCase(TRIM)) {
        trim();
      } else if (testRecordData.getCommand().equalsIgnoreCase(LEFT)) {
        left();
      } else if (testRecordData.getCommand().equalsIgnoreCase(RIGHT)) {
        right();
      } else if (testRecordData.getCommand().equalsIgnoreCase(SUBSTRING)) {
        subString();
      } else if ((testRecordData.getCommand().equalsIgnoreCase(FINDSUBSTRINGINSTRING))||
                 (testRecordData.getCommand().equalsIgnoreCase(GETSUBSTRINGINSTRING))) {
        findSubstring();
      } else if (testRecordData.getCommand().equalsIgnoreCase(INDEX)) {
        index();
      } else if (testRecordData.getCommand().equalsIgnoreCase(REPLACE)) {
        replace();
      } else if (testRecordData.getCommand().equalsIgnoreCase(CLEANSTRING)) {
        cleanString();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETFIELD)) {
        getField();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETTRIMMEDFIELD)) {
        getTrimmedField();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETFIXEDWIDTHFIELD)) {
        getFixedWidthField();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETNEXTDELIMITERINDEX)) {
        getNextDelimiterIndex();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETFIELDCOUNT)) {
        getFieldCount();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETFIELDARRAY)) {
        getFieldArray();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETREDELIMITEDFIELDCOUNT)) {
        getREDelimitedFieldCount();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETREDELIMITEDFIELD)) {
        getREDelimitedField();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETMULTIDELIMITEDFIELDCOUNT)) {
        getMultiDelimitedFieldCount();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETMULTIDELIMITEDFIELD)) {
    	getMultiDelimitedField();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETSYSTEMENVIRON)) {
        getSystemEnviron();
      } else if (testRecordData.getCommand().equalsIgnoreCase(GETSYSTEMUSER)) {
        getSystemUser();
      } else {
        setRecordProcessed(false);
      }
    } catch (SAFSException ex) {
      //ex.printStackTrace();
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> length
   **/
  private void length () throws SAFSException {
    if (!checkParams(2)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
    String val = (new Integer(src.length())).toString();
    String comment;
	if (!setVar(varName, val)) return;
    comment = genericText.convert("equals", 
                                   varName +" equals "+ val,
                                   varName, val);
    issueGenericSuccess(comment);
  }

  /** <br><em>Purpose:</em> compare
   **/
  private void compare () throws SAFSException {
    if (!checkParams(3)) return;
    Iterator<?> iterator = params.iterator();
    String src = (String) iterator.next();
    String dest = (String) iterator.next();
    String varName = (String) iterator.next();
    
    boolean regexMatch = false;
    if(iterator.hasNext()){
    	try{
    		regexMatch = Boolean.parseBoolean(iterator.next().toString());
    	}catch(Exception e){
    		Log.warn("DCDSC.compare(): the parameter regexMatch is wrong, met exception "+e.getMessage());
    	}
    }
    

    Log.info(".............................params: "+params);
    boolean matched = false;
    String val = null;
    
    if(regexMatch){
    	//the destination string is a regular expression
    	Pattern pattern = Pattern.compile(dest);
    	Matcher matcher = pattern.matcher(src);
    	matched = matcher.find();
    }else{
    	matched = src.equals(dest);
    }
    
    val = String.valueOf(matched);
	if (!setVar(varName, val))return;
    String comment = genericText.convert("equals", 
                                   varName +" equals "+ val,
                                   varName, val);
    String detail =  StringUtils.quote(src)+ (matched? " matched ":" did not match ")+ StringUtils.quote(dest);
    		
    issueGenericSuccess(comment, detail);
  }

  /** <br><em>Purpose:</em> concatenate
   **/
  private void concatenate () throws SAFSException {
    if (!checkParams(3)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    String str2 = (String) iterator.next();
    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
    String val = src+str2;
    String comment;
	if (!setVar(varName, val))return;
    comment = genericText.convert("equals", 
                                   varName +" equals "+ val,
                                   varName, val);
    issueGenericSuccess(comment);
  }

  /** <br><em>Purpose:</em> toUpperCase
   **/
  private void toUpperCase () throws SAFSException {
    if (!checkParams(2)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
    String val = src.toUpperCase();
    String comment;
	if (!setVar(varName, val)) return;
    comment = genericText.convert("equals", 
                                   varName +" equals "+ val,
                                   varName, val);
    issueGenericSuccess(comment);
  }

  /** <br><em>Purpose:</em> toLowerCase
   **/
  private void toLowerCase () throws SAFSException {
    if (!checkParams(2)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    String varName = (String) iterator.next();
    Log.info(".............................params: "+params);
    String val = src.toLowerCase();
    String comment;
	if (!setVar(varName, val))return;
    comment = genericText.convert("equals", 
                                   varName +" equals "+ val,
                                   varName, val);
    issueGenericSuccess(comment);
  }

  /** <br><em>Purpose:</em> leftTrim
   **/
  private void leftTrim () throws SAFSException {
    if (!checkParams(2)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
    String comment;
    String val;
    try {
        val = src.trim();
        int j = src.indexOf(val);
        if (j >= 0) val = src.substring(j, src.length());
        else val = src;
		if (!setVar(varName, val)) return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure( src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> rightTrim
   **/
  private void rightTrim () throws SAFSException {
    if (!checkParams(2)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
    String comment;
    String val;
    try {
      val = src.trim();
      int j = src.indexOf(val);
      if (j >= 0) val = src.substring(0, j) + val;
      else val = src;
		if (!setVar(varName, val))return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> trim
   **/
  private void trim () throws SAFSException {
    if (!checkParams(2)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    String varName = (String) iterator.next();
    Log.info(".............................params: "+params);
    String val = src.trim();
    String comment;
	if (!setVar(varName, val))return;
    comment = genericText.convert("equals", 
                                   varName +" equals "+ val,
                                   varName, val);
    issueGenericSuccess(comment);    
  }

  /** <br><em>Purpose:</em> left
   **/
  private void left () throws SAFSException {
    if (!checkParams(3)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int num = getInt(iterator);
    String varName = (String) iterator.next();
    Log.info(".............................params: "+params);
    try {
        String val = src.substring(0, num);
        String comment;
		if (!setVar(varName, val))return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> right
   **/
  private void right () throws SAFSException {
    if (!checkParams(3)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int num = getInt(iterator);
    String varName = (String) iterator.next();
    Log.info(".............................params: "+params);
    try {
        String val = src.substring(src.length()-num, src.length());
        String comment;
		if (!setVar(varName, val)) return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> FindSubstringInString & GetSubstringInString
   * @author (CANAGL) Jan 13, 2004 Fixed to ignore leading caret on varname parameter.
   **/
  private void findSubstring () throws SAFSException {
    if (params.size() <= 3) {
      issueParameterCountFailure();
      return;
    }
    Iterator iterator = params.iterator();
    String string     = (String) iterator.next();  // need to fail on 0 length
    if (string.length()<1) {
    	issueParameterValueFailure("STRING");
    	return;
    }
    String regexStart = (String) iterator.next();
    if (regexStart.length()<1) {
    	issueParameterValueFailure("REGEXStart");
    	return;
    }
    String regexStop  = (String) iterator.next();
    if (regexStop.length()<1) {
    	issueParameterValueFailure("REGEXStop");
    	return;
    }
    String varname    = (String) iterator.next();  // need to fail on 0 length
    if (varname.length()<1) {
    	issueParameterValueFailure("VARNAME");
    	return;
    }
    if (varname.charAt(0)=='^'){ varname = varname.substring(1);}
    Log.info(".............................params: "+params);
    StringProcessor fp = new StringProcessor();
    String substring = fp.getEmbeddedSubstring(string, regexStart, regexStop);
    int status = fp.returnStatus();
    Log.info(".................substring: "+substring);
    Log.info("....................status: "+status);
    switch(status){

    	// continue
    	case 0:
    	    break;

    	// exit with unknown error
    	case 38:
    	    String regex = GENStrings.text(GENStrings.REGULAR_EXPRESSIONS,
    	                   "Regular Expressions");
    	    String error = FAILStrings.convert(FAILStrings.SUPPORT_NOT_INSTALLED,
    	                   regex+" support may not be installed!",
    	                   regex);
    	    issueUnknownErrorFailure(error);
    	    return;
    }
    // get substring succeeded
    String comment;
	if (! setVar(varname, substring))return;
    comment = genericText.convert("equals", 
                                   varname +" equals "+ substring,
                                   varname, substring);
    issueGenericSuccess(comment);   
  }

  /** <br><em>Purpose:</em> subString
   **/
  private void subString () throws SAFSException {
    //rld: added logic to handle length specification correctly.
    if (!checkParams(4)) return;    

    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int num = getInt(iterator);

    // handle the "optional" length parameter
    int len;
    String nums = (String) iterator.next();
    if (nums.equals("")) {
	len = 0;
    } else {
        // create an int from the results
        try {
          Integer n = new Integer(nums);
          len = n.intValue();
        } catch (NumberFormatException nfe) {
          issueParameterValueFailure("LENGTH");
          return;
        }
    }

    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
    try {
	    //rld: Several things possible with the len value. It could either
	    //     not be here or else it is less than 1. If either is true, get
	    //     all the remaining characters. Note that if it was not passed in,
	    //     it is defaulted to 0.
	    String val;
	    String comment;
	    if (len < 1) {
		    val = src.substring(num);
	    } else {
	        val = src.substring(num, num+len);
	    }
		if (!setVar(varName, val))return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> index
   **/
  private void index () throws SAFSException {
    if (!checkParams(4)) return;
    
    Iterator iterator = params.iterator();
    int start = getInt(iterator);
    String src = (String) iterator.next();
    String find = (String) iterator.next();
    String varName = (String) iterator.next();
    Log.info(".............................params: "+params);
    String comment;
    try {
      String val = src.substring(start, src.length());
      int j = val.indexOf(find);
      if (j>=0) {
        val = (new Integer(j+start)).toString();
		if (!setVar(varName, val))return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
      } else {
        val = (new Integer(j)).toString();
        if (!setVar(varName, val)) return;
        comment = genericText.convert("equals", 
	                                      varName +" equals "+ val,
	                                      varName, val);
	                                      
        comment += " "+ failedText.convert("substring_not_found_2",
                                     "Substring '"+ find +"' not found in '"+ src +"'.",
                                     find, src);
        issueGenericSuccess(comment);
        return;
      }
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> replace
   **/
  private void replace () throws SAFSException {
    if (!checkParams(4))return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    String find = (String) iterator.next();
    String replace = (String) iterator.next();
    String varName = (String) iterator.next();
    Log.info(".............................params: "+params);
    String comment;
    try {
      String val = src;
      boolean didReplace = false;
      int j = src.indexOf(find);
      while (j>=0) {
      	Log.debug("Replacing found substring at index "+ String.valueOf(j));
        val = val.substring(0, j) + replace + val.substring(j+find.length(), val.length());
        didReplace = true;
        j = val.indexOf(find,j+replace.length());
      }
      
      val = didReplace? val:src;
      Log.debug("Replacing substrings completed!");
	  if (! setVar(varName, val)) return;
	  Log.debug("The result "+val+" is set to varialbe "+varName);
	  
	  comment = genericText.convert("equals", 
			                        varName +" equals "+ val,
			                        varName, val);
	  
	  //If nothing was replaced, add some extra comment to tell user.
      if (!didReplace) {
    	  comment += " "+ failedText.convert("substring_not_found_2",
    			  "Substring '"+ find +"' not found in '"+ src +"'.",
    			  find, src);
      }
      
      issueGenericSuccess(comment);
      return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
    }
  }

  /** <br><em>Purpose:</em> cleanString;<br>
   ** for each char in string: if ((char > 31) && (char < 127)) keep it,
   ** otherwise turn it into a space
   **/
  private void cleanString () throws SAFSException {
    if (!checkParams(2))return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
    try {
        String val = src.replaceAll ("[^ -~]", " ");
	    String comment;
		if (! setVar(varName, val))return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.util.regex.PatternSyntaxException pse) {
      issueActionOnXFailure(src, pse.getMessage());
      setVar(varName, "");
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    } catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
      issueActionOnXFailure(src, aioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> getTrimmedField
   **/
  private void getTrimmedField () throws SAFSException {
    if (!checkParams(4))return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int num = getInt(iterator);
    String str2 = (String) iterator.next();
    String varName = (String) iterator.next();
    Log.info(".............................params: "+params);
    try {
        String sTmp = StringUtils.getInputToken(src, num, str2);
        String val = sTmp.trim();
	    String comment;
		if (!setVar(varName, val)) return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> getField
   **/
  private void getField () throws SAFSException {
    if (!checkParams(4)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int num = getInt(iterator);
    String str2 = (String) iterator.next();
    String varName = (String) iterator.next();
    Log.info(".............................params: "+params);
    try {
        String val = StringUtils.getInputToken(src, num, str2);
	    String comment;
		if (!setVar(varName, val)) return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> getFixedWidthField
   **/
  private void getFixedWidthField () throws SAFSException {
    if (!checkParams(2)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int num = getInt(iterator);
    int fixedwidth = getInt(iterator);
    int sindex = num * fixedwidth;
    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
    try {
        String val = src.substring(sindex, sindex+fixedwidth);
	    String comment;
		if (! setVar(varName, val)) return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> getNextDelimiterIndex
   **/
  private void getNextDelimiterIndex () throws SAFSException {
	String debugmsg = getClass().getName()+".getNextDelimiterIndex() ";
	
    if (!checkParams(4)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int index = getInt(iterator);
    String delims = (String) iterator.next();
    String varName = (String) iterator.next();
    String subsrc = null;

    try{
    	subsrc = src.substring(index);
    }catch(IndexOutOfBoundsException e){
    	String msg = FAILStrings.convert(FAILStrings.BAD_PARAM,
    									 "Invalid parameter value for "+index,
    									 Integer.toString(index));
    	Log.debug(debugmsg+msg+" "+e.getMessage());
    	throw new SAFSException(msg);
    }
    Log.info(".............................params: "+params);
    int min = -1;
    for(int j=0; j<delims.length(); j++) {
      String d = delims.substring(j, j+1);
      int k= subsrc.indexOf(d);
      if (min < 0 || min > k) min = k;
    }
    
    if(min>-1){
    	min += index;
    	Log.info(debugmsg+" Delimiter '"+delims+"' found at "+min);
    }else{
    	Log.info(debugmsg+" Delimiter '"+delims+"' not found.");
    	String detail = FAILStrings.convert(FAILStrings.DELIMITER_NOT_FOUND,
    									 "Delimiter '"+delims+"' was not found",
    									 delims);
    	issueExecutionNegativeMessage(detail);
    }
    String val = Integer.toString(min);
    String comment;
	if (! setVar(varName, val)) return;
    comment = genericText.convert(GENStrings.EQUALS, 
                                   varName +" equals "+ val,
                                   varName, val);
    issueGenericSuccess(comment);
  }

  /** <br><em>Purpose:</em> getFieldCount
   **/
  private void getFieldCount () throws SAFSException {
    if (!checkParams(4)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int start = getInt(iterator);
    String delims = (String) iterator.next();
    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
    String comment;
    try {
      String val = Integer.toString(StringUtils.getFieldCount(src, start, delims));
		if (! setVar(varName, val))return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
      issueActionOnXFailure(src, sioobe.getMessage());
      setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> getFieldArray: not implemented yet
   **/
  private void getFieldArray () throws SAFSException {
  	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
  	setRecordProcessed(false);
  }

  /** <br><em>Purpose:</em> getREDelimitedFieldCount:
   **/
  private void getREDelimitedFieldCount () throws SAFSException {
    if (!checkParams(4)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int start = getInt(iterator);
    String delims = (String) iterator.next();
    String varName = (String) iterator.next();
    String sTmp;

    Log.info(".............................params: "+params);
	String comment;
    try {
		sTmp = src.substring(start);
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
        issueActionOnXFailure(src, sioobe.getMessage());
	    setVar(varName, "");		// just ignore failure
	    return;
    }
    try {
    	/* check the entry parameters for validity */
    	if (start < 0) {
			/* in this case, the index is invalid - return error message */
	        issueParameterValueFailure("INDEX");
			setVar(varName, "");
			return;
    	}
    	if (delims.length() == 0) {
			/* in this case, the length is zero so no data exists - return error message */
	        issueParameterValueFailure("REGEXP");
			setVar(varName, "");
			return;
    	}
    	if (src.length() == 0) {
		/* in this case, the length is zero so no data exists - return error message */
	        issueParameterValueFailure("INPUT");
			setVar(varName, "");
			return;
    	}

		/* rld: we are going to use the split command from String to create an array of elements
		 * then use the count of array elements to figure out the number of fields. Note that we
		 * specify the number as "-1" so that null fields are also counted.
		 */
		String[] sArray = sTmp.split(delims, -1);
		String val = Integer.toString(Array.getLength(sArray));
		if (! setVar(varName, val))return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.util.regex.PatternSyntaxException pse) {
        issueActionOnXFailure(src, pse.getMessage());
		setVar(varName, "");
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
        issueActionOnXFailure(src, sioobe.getMessage());
		setVar(varName, "");
    } catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
        issueActionOnXFailure(src, aioobe.getMessage());
        setVar(varName, "");
    }
  }

  /** <br><em>Purpose:</em> getREDelimitedField
   **/
  private void getREDelimitedField () throws SAFSException {
    if (!checkParams(4)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int index = getInt(iterator);
    String delim = (String) iterator.next();
    String varName = (String) iterator.next();

    Log.info(".............................params: "+params);
	String comment;
    try {
        if (index < 1) {
	    /* in this case, the index is invalid - return error message */
	        issueParameterValueFailure("INDEX");
		    setVar(varName, "");
		    return;
        }
        if (delim.length() == 0) {
	    /* in this case, the length is zero so no data exists - return error message */
	        issueParameterValueFailure("REGEXP");
		    setVar(varName, "");
		    return;
        }
        if (src.length() == 0) {
	        issueParameterValueFailure("INPUT");
		    setVar(varName, "");
		    return;
        }

		/* rld: we are going to use the split command from String to create an array of elements
		 * then pass the appropriate array element back. Note that we
		 * specify the number as "-1" so that null fields are also counted.
		 */
		String[] sArray = src.split(delim, -1);
		String val = sArray[index-1];
		if (! setVar(varName, val)) return;
        comment = genericText.convert("equals", 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
        return;
    } catch (java.util.regex.PatternSyntaxException pse) {
  	  	 testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		 log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
	                     ", "+pse.getMessage(), FAILED_MESSAGE);
	     setVar(varName, "");
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
	      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
	                     ", "+sioobe.getMessage(), FAILED_MESSAGE);
	     setVar(varName, "");
    } catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
	      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
	                     ", "+aioobe.getMessage(), FAILED_MESSAGE);
	     setVar(varName, "");
    }
  }

  private void getMultiDelimitedFieldCount() throws SAFSException {
	  String debugmsg = getClass().getName()+".getMultiDelimitedFieldCount() ";
		
	    if (!checkParams(4)) return;
	    Iterator iterator = params.iterator();
	    String src = (String) iterator.next();
	    int startIndex = getInt(iterator);
	    String delim = (String) iterator.next();
	    String varName = (String) iterator.next();
	    
	    Log.info(".............................params: "+params);
		String comment;
	    try {
	        if (startIndex < 1) {
	        	issueParameterValueFailure("STARTINDEX");
	        	setVar(varName, "");
	        	return;
	        }
	        if (delim.length() == 0) {
		        issueParameterValueFailure("REGEXP");
			    setVar(varName, "");
			    return;
	        }
	        if (src.length() == 0) {
		        issueParameterValueFailure("INPUT");
			    setVar(varName, "");
			    return;
	        }

	        String subString = src.substring(startIndex-1);
	        List tokens = StringUtils.getTokenList(subString, delim);
			String val = String.valueOf(tokens.size());
			Log.debug(debugmsg+" Set field count: "+val);
			
			if (! setVar(varName, val)) return;
	        comment = genericText.convert(GENStrings.EQUALS, 
		                                   varName +" equals "+ val,
		                                   varName, val);
	        issueGenericSuccess(comment);
	    } catch (java.util.regex.PatternSyntaxException pse) {
	  	  	 testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			 log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
		                     ", "+pse.getMessage(), FAILED_MESSAGE);
		     setVar(varName, "");
	    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
		      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
		                     ", "+sioobe.getMessage(), FAILED_MESSAGE);
		     setVar(varName, "");
	    } catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
		      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
		                     ", "+aioobe.getMessage(), FAILED_MESSAGE);
		     setVar(varName, "");
	    }
  }

  private void getMultiDelimitedField() throws SAFSException {
	String debugmsg = getClass().getName()+".getMultiDelimitedField() ";
	
    if (!checkParams(5)) return;
    Iterator iterator = params.iterator();
    String src = (String) iterator.next();
    int index = getInt(iterator);
    int startIndex = getInt(iterator);
    String delim = (String) iterator.next();
    String varName = (String) iterator.next();
    
    Log.info(".............................params: "+params);
	String comment;
    try {
        if (index < 1) {
	        issueParameterValueFailure("INDEX");
		    setVar(varName, "");
		    return;
        }
        if (startIndex < 1) {
        	issueParameterValueFailure("STARTINDEX");
        	setVar(varName, "");
        	return;
        }
        if (delim.length() == 0) {
	        issueParameterValueFailure("REGEXP");
		    setVar(varName, "");
		    return;
        }
        if (src.length() == 0) {
	        issueParameterValueFailure("INPUT");
		    setVar(varName, "");
		    return;
        }


        String subString = src.substring(startIndex-1);
        List tokens = StringUtils.getTokenList(subString, delim);
		String val = "";
		if(index<=tokens.size()){
			val = tokens.get(index-1).toString();
		}else{
			Log.debug(debugmsg+" index "+index+" should not be bigger than number of tokens "+tokens.size());
		}
		Log.debug(debugmsg+" Set field value: "+val);			
		
		if (! setVar(varName, val)) return;
        comment = genericText.convert(GENStrings.EQUALS, 
	                                   varName +" equals "+ val,
	                                   varName, val);
        issueGenericSuccess(comment);
    } catch (java.util.regex.PatternSyntaxException pse) {
  	  	 testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		 log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
	                     ", "+pse.getMessage(), FAILED_MESSAGE);
	     setVar(varName, "");
    } catch (java.lang.StringIndexOutOfBoundsException sioobe) {
	      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
	                     ", "+sioobe.getMessage(), FAILED_MESSAGE);
	     setVar(varName, "");
    } catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
	      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
	                     ", "+aioobe.getMessage(), FAILED_MESSAGE);
	     setVar(varName, "");
    }
  }
  
	/** 
	 * getSTAFEnv
	 * returns string value or an empty string
	 */
	private String getSTAFEnv(String env) {
		STAFHelper stafHelper = testRecordData.getSTAFHelper();
		String result = stafHelper.getSTAFEnv(env);
		
	    if (result == null) issueParameterValueFailure(env);
    	return result;
	}
	
	/** 
	 * getSystemEnviron
	 * Extract STAF/Env/VARIABLENAME from the STAF VAR service.
	 * <p>
	 * (CANAGL) APR 14, 2005 return SCRIPT_NOT_EXECUTED if unable to satisfy.
	 * (CANAGL) MAY 02, 2005 use STAF/Env in STAF VAR service to read Environment variables.
	**/
	private void getSystemEnviron () throws SAFSException {
	    if (!checkParams(2)) return;
	    Iterator iterator = params.iterator();
	    String src = (String) iterator.next();
	    String varName = (String) iterator.next();
	    if (varName.substring(0,1).equals("^")) {
	    	varName = varName.substring(1);
	    }
		String comment;
    	String result = getSTAFEnv(src);
	  	if (! setVar(varName, result)) return;
      	comment = genericText.convert("equals", 
	                                   varName +" equals "+ result,
	                                   varName, result);
      	issueGenericSuccess(comment);
    }

	/** 
	 * getSystemUser
	 * Extract STAF/Env/USERNAME from the STAF VAR service.
	 * <p>
	 * (CANAGL) APR 14, 2005 return SCRIPT_NOT_EXECUTED if unable to satisfy.
	 * (CANAGL) MAY 02, 2005 use STAF/Env in STAF VAR service to read Environment variables.
	**/
	private void getSystemUser () throws SAFSException {
	    if (!checkParams(1)) return;
	    Iterator iterator = params.iterator();
	    String src = "USERNAME";
	    String varName = (String) iterator.next();
	    if (varName.substring(0,1).equals("^")) {
	    	varName = varName.substring(1);
	    }
		String comment;
    	String result = getSTAFEnv(src);
	  	if (! setVar(varName, result)) return;
      	comment = genericText.convert("equals", 
	                                   varName +" equals "+ result,
	                                   varName, result);
      	issueGenericSuccess(comment);
    }

  /** <br><em>Purpose:</em> calls setVariable from a super class, if unsuccessful
   ** then sets statusCode to GENERAL_SCRIPT_FAILURE and logs warning
   * @param                     varName, String
   * @param                     val, String
   * @return                    boolean if successful
   **/
  public boolean setVar (String varName, String val) throws SAFSException {
    if (!setVariable(varName, val)) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     " failure, could not assign variable: "+varName+" a value of: "+val,
                     FAILED_MESSAGE);
      return false;
    }
    return true;
  }

  /** <br><em>Purpose:</em> check if params are < expected, if so then unsuccessful, ok otherwise.
   ** if unsuccessful then sets statusCode to GENERAL_SCRIPT_FAILURE and logs warning
   * @param                     expected, int
   * @return                    boolean, false if params.size() < expected, true otherwise
   **/
  public boolean checkParams (int expected) throws SAFSException {
    if (params.size() < expected) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      paramsFailedMsg();
      return false;
    }
    return true;
  }

  /** <br><em>Purpose:</em> get iterator.next() as a number, if unsuccessful,
   ** then sets statusCode to GENERAL_SCRIPT_FAILURE and logs warning
   * @param                     iterator, Iterator
   * @return                    int, the int value
   * @exception SAFSException if not a number
   **/
  public int getInt (Iterator iterator) throws SAFSException {
    if (!iterator.hasNext()) {
      paramsFailedMsg();
      throw new SAFSException("wrong number of params");
    }
    String nums = (String) iterator.next();
    try {
      Integer n = new Integer(nums);
      return n.intValue();
    } catch (NumberFormatException nfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+testRecordData.getCommand() +
                     ": not an integer: "+nums+ ", msg: "+nfe.getMessage(),
                     FAILED_MESSAGE);
      throw new SAFSException("not an integer: "+nums);
    }
  }

}
