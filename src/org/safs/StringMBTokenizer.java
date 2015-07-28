/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.util.*;

/**
 * <br>All Implemented Interfaces: 
 * Enumeration 
 * <p>
 * --------------------------------------------------------------------------------
 * <p>
 * The String Multi-Byte Tokenizer allows an application to break a String into tokens. 
 * The tokenization method is such that the provided delimiter string, typically a string 
 * of 2 or more characters, is used as a single multi-byte delimiter.  This differs 
 * from the java.util.StringTokenizer which uses each separate character in the delimiter 
 * string as a possible token delimiter.
 * <p>
 * The StringMBTokenizer methods do not distinguish among identifiers, numbers, and quoted
 * strings, nor do they recognize and skip comments.  Both the String to tokenize and the 
 * delimiter String are treated simply as literal text strings.
 * <p>
 * The multi-byte delimiter (the String that separates tokens) may be specified either at
 * creation time, or on a per-token basis. 
 * <p>
 * StringMBTokenizer will only return tokens.  You cannot optionally request to also 
 * return delimiters.  There is little need for that feature since there is only one
 * active multi-byte delimiter at any given time.
 * <p>
 * A token is returned by taking a substring of the string that was used to create the
 * StringMBTokenizer object. 
 * <p>
 * Two multi-byte delimiters back-to-back delimit an inner empty string token.<br>
 * A leading multi-byte delimiter delimits a leading empty string token.<br>
 * A trailing multi-byte delimiter only terminates the preceding token.  There is no 
 * trailing empty string token following a trailing delimiter.
 * <p>
 * The following is one example of the use of the tokenizer. The code: 
 ** <p><code>
 **    StringMBTokenizer st = new StringMBTokenizer("File->Menu->Menuitem","->");
 **    while (st.hasMoreTokens()) {
 **        println(st.nextToken());
 **    }
 ** </code><p>
 ** prints the following output: 
 ** <p><code><br>
 **  File<br>
 **  Menu<br>
 **  Menuitem<br>
 ** </code>
 **/

public class StringMBTokenizer implements Enumeration {

  private String source    = "";
  private int sourceLen    =  0;
  private String delimiter = "";
  private int tindex = 0;


  /** <br><em>Purpose:</em> Constructs a string tokenizer for the specified string.
   * The delimiter argument is the mult-byte String separating tokens.
   * @param                     source string to be parsed
   * @param                     delimiter string separating tokens
   **/
  public StringMBTokenizer (String source, String delimiter) {
    if (source != null) {
    	this.source = source;
    	sourceLen = source.length();
    }
    if (delimiter != null) this.delimiter = delimiter;
  }


  /** 
   * <br><em>Purpose:</em>  Calculates the number of times that this tokenizer's
   * nextToken method can be called before it generates an exception.  This assumes 
   * the continued use of the delimiter provided to the constructor.  If a different 
   * delimiter is used with nextToken then this count may be inaccurate.
   * @return                    int
   **/
  public int countTokens() { return countTokens(null); }
  
  
  /** 
   * <br><em>Purpose:</em>  Calculates the number of times that this tokenizer's
   * nextToken(delimiter) method can be called before it generates an exception.  This assumes 
   * the continued use of the delimiter parameter provided this routine.  If a different 
   * delimiter is used with nextToken then this count may be inaccurate.
   * @param                     delimiter, String alternate delimiter
   * @return                    int
   **/
  public int countTokens(String delimiter) {
  	if ((sourceLen == 0)||(tindex >= sourceLen)) return 0;

	//sourceLen now guaranteed > 0
  	if (delimiter == null) delimiter = this.delimiter;
  	int delimitLen = delimiter.length();  	
  	if (delimitLen == 0) return 1;
  	
  	int tokens  = 0;
  	int sindex  = tindex;
  	int eindex  = tindex;  	
  	while ((sindex < sourceLen)&&(eindex != -1)){
  		eindex = source.indexOf(delimiter, sindex);
  		tokens++;
  		if(eindex != -1){ sindex = eindex + delimitLen;	}
  	}
    return tokens;
  }
  
  
  /** <br><em>Purpose:</em> Returns the same value as the hasMoreTokens method. 
   * @return                    boolean, true if more, false if not
   **/
  public boolean hasMoreElements() {
    return hasMoreTokens();
  }
  
  
  /** <br><em>Purpose:</em> Returns the same value as the nextToken method,
   ** except that its declared return value is Object rather than String. 
   ** It exists so that this class can implement the Enumeration interface.
   * @return                    Object
   * @exception NoSuchElementException - if there are no more tokens in this tokenizer's string.
   **/
  public Object nextElement() throws NoSuchElementException {
    return nextToken();
  }


  /** <br><em>Purpose:</em> Tests if there are more tokens available from this tokenizer's string. 
   * @return                    boolean, true if more, false if not
   **/
  public boolean hasMoreTokens() {
  	return (tindex < sourceLen);
  }


  /** <br><em>Purpose:</em> Returns the next token from this string tokenizer. 
   ** except that its declared return value is String rather than Object. 
   * @return                    Object
   * @exception NoSuchElementException - if there are no more tokens in this tokenizer's string.
   **/
  public String nextToken() throws NoSuchElementException {
  	return nextToken(null);
  }


  /** 
   * <br><em>Purpose:</em> Returns the next token in this string tokenizer's string.
   * @param                     delimiter, String alternate delimiter
   * @return                    the next token in this string tokenizer's string.
   * @exception NoSuchElementException - if there are no more tokens in this tokenizer's string.
   **/
  public String nextToken(String delimiter) throws NoSuchElementException {
  	
  	if (tindex >= sourceLen) throw new NoSuchElementException();

	//sourceLen now guaranteed > 0
  	if (delimiter == null) delimiter = this.delimiter;
  	int delimitLen = delimiter.length();
  	
  	String value = "";
  	if (delimitLen == 0) {
  		value = source.substring(tindex);
  		tindex = sourceLen;
  		return value;
  	}
  	
  	int index = source.indexOf(delimiter, tindex);
  	if (index == tindex) {
  		tindex = tindex + delimitLen;
  		//return the preset empty string
  	}
  	else if (index > tindex) {
  		value = source.substring(tindex, index);
  		tindex = index + delimitLen;
  	}
  	else {
  		value = source.substring(tindex);
  		tindex = sourceLen;
  	}
  	return value;
  }
  
  private static StringMBTokenizer sysoutSetup(String source, String delimiter){
	System.out.println("");
  	System.out.println(source);
  	return new StringMBTokenizer(source, delimiter);  	
  }
  private static void sysoutTokens(StringMBTokenizer toker){
  	while(toker.hasMoreTokens()){
  		System.out.println(toker.nextToken());
  	}
  }
  public static void main(String[] args){

  	StringMBTokenizer toker = sysoutSetup("File->Menu->Menuitem", "->");
  	System.out.println(toker.countTokens()  +" countTokens 3");
  	sysoutTokens(toker);

  	toker = sysoutSetup("File->->Menuitem", "->");
  	System.out.println(toker.countTokens()  +" countTokens 3");
  	sysoutTokens(toker);

  	toker = sysoutSetup("File->Menu->", "->");
  	System.out.println(toker.countTokens()  +" countTokens 2");
  	sysoutTokens(toker);

  	toker = sysoutSetup("->->Menuitem", "->");
  	System.out.println(toker.countTokens()  +" countTokens 3");
  	sysoutTokens(toker);

  	toker = sysoutSetup("File->Menu->Menuitem", null);
  	System.out.println(toker.countTokens()  +" countTokens 1");
  	sysoutTokens(toker);

  	toker = sysoutSetup(null, "->");
  	System.out.println(toker.countTokens()  +" countTokens 0");
  	sysoutTokens(toker);

  	toker = sysoutSetup("", "->");
  	System.out.println(toker.countTokens()  +" countTokens 0");
  	sysoutTokens(toker);

  	toker = sysoutSetup(null, null);
  	System.out.println(toker.countTokens()  +" countTokens 0");
  	sysoutTokens(toker);
  	
	System.out.println("");
	System.out.println("nextElement throwing NoSuchElementException...");
	try{ toker.nextElement() ;}
	catch(NoSuchElementException e){System.out.println("thrown successfully");}
  	
  }
}

