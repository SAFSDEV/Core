/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/**
 * <br><em>Purpose:</em> string utilities
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 * JUN 04, 2003	(DBauman) 	Original Release
 * JUN 26, 2003	(CANAGL) 	Added convertWildcardsToRegularExpression
 * DEC 19, 2006	(CANAGL) 	Added readUTF8File support
 * OCT 29, 2008	(LeiWang)	Add method getTokenList().
 * NOV 25, 2008	(LeiWang)	Add method containsSepcialKeys(): See defect S0546329
 * JAN 26, 2009	(CANAGL) 	Fixed getTrimmedUnquotedString to trim appropriate whitespace
 * MAR 27, 2014	(SBJLWA) 	Add method getTokenList() getTokenArray(): get delimited tokens, token can contain delimiter.
 * FEB 17, 2015	(SBJLWA) 	Add method breakXpath(): break a slash-separated-xpath into an array.
 * APR 08, 2015	(SBJLWA) 	Add method getSystemProperty(): get/set property value according to configuration file.
 *                          Add method replaceJVMOptionValue().
 * May 20, 2015	(SBJLWA) 	Add method getTrimmedTokenList() and arrayToList().
 * JUN 23, 2015	(SBJLWA) 	Add getCallerClassName(), isLocalHost(), isMacAddress() and getHostIP().
 *                          Modify deduceUnusedSeparatorString(): avoid NullPointerException.
 * JUL 08, 2015 (SBJLWA) 	Add some constant. Add method urlEncode().
 * SEP 07, 2015 (SBJLWA) 	Move some content from method convertLine() to a new method convertCoordsToArray().
 * NOV 24, 2015 (SBJLWA) 	Remove the dependency of WebElement.
 *                          Deprecate method extractCoordStringPair(), use convertCoordsToArray() instead.
 *                          Delete some methods, rename some methods to make this class clearer.
 * OCT 19, 2016 (SBJLWA) 	Moved convertWindowPosition() to ComponentFunction class.
 *                          Use IndependantLog instead of Log.
 *
 **/
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safs.ComponentFunction.Window;
import org.safs.sockets.DebugListener;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.ConfigureInterface;
import org.w3c.tools.codec.Base64Decoder;
import org.w3c.tools.codec.Base64Encoder;

/**
 * This class provides different kinds of utilities to handle string.
 **/
public class StringUtils{

	/**"."*/
	public static final String REGEX_CHARACTER_ANY			= ".";
	/**"*"*/
	public static final String REGEX_CHARACTER_MATCH_MANY	= "*";
	/**"?"*/
	public static final String REGEX_CHARACTER_MATCH_ONE	= "?";
	/**character slash / */
	public static final char CHAR_SLASH = '/';
	/**character single quote ' */
	public static final char CHAR_QUOTE = '\'';
	/**character double quote " */
	public static final char CHAR_DOUBLE_QUOTE = '"';
	/**character back slash \ */
	public static final char CHAR_BACK_SLASH = '\\';
	/**character number # */
	public static final char CHAR_NUMBER = '#';
	/**character colon : */
	public static final char CHAR_COLON = ':';
	/**character equal = */
	public static final char CHAR_EQUAL = '=';
	/**character and & */
	public static final char CHAR_AND = '&';
	/**character interrogation ? */
	public static final char CHAR_INTERROGATION = '?';
	/**string slash / */
	public static final String SLASH = String.valueOf(StringUtils.CHAR_SLASH);
	/**string single quote ' */
	public static final String QUOTE = String.valueOf(StringUtils.CHAR_QUOTE);
	/**string double quote " */
	public static final String DOUBLE_QUOTE = String.valueOf(StringUtils.CHAR_DOUBLE_QUOTE);
	/**string back slash \ */
	public static final String BACK_SLASH = String.valueOf(StringUtils.CHAR_BACK_SLASH);
	/**string number # */
	public static final String NUMBER = String.valueOf(StringUtils.CHAR_NUMBER);
	/**string colon :*/
	public static final String COLON = String.valueOf(StringUtils.CHAR_COLON);
	/**string equal = */
	public static final String EQUAL = String.valueOf(StringUtils.CHAR_EQUAL);
	/**string and & */
	public static final String AND = String.valueOf(StringUtils.CHAR_AND);
	/**string interrogation ? */
	public static final String INTERROGATION = String.valueOf(StringUtils.CHAR_INTERROGATION);
	/**10000*/
    public static final int maxBytesPerRead = 10000;
    /**1024*10*/
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 10;
    public static final String[] specialKeys = {"~","+","^","%","(",")","{","}"};
    /**'\\'*/
    public static final Character REGEX_ESCAPE_CHARACTER		= new Character('\\');
    /**"+"*/
	public static final String PLUS 			= "+";
	/**"-"*/
	public static final String MINUS 		= "-";
	/**","*/
	public static final String COMMA = ",";
	/**";"*/
    public static final String SEMI_COLON = ";";
    /**"%"*/
    public static final String PERCENTAGE = "%";
    /**" "*/
    public static final String SPACE = " ";

	/**'localhost'*/
	public static final String LOCAL_HOST = Constants.LOCAL_HOST;
	/** '127.0.0.1'*/
	public static final String LOCAL_HOST_IP = Constants.LOCAL_HOST_IP;

    /** "CASE-INSENSITIVE" **/
    public static final String CASE_INSENSITIVE = "CASE-INSENSITIVE";
    /** "CASEINSENSITIVE" **/
    public static final String CASEINSENSITIVE  = "CASEINSENSITIVE";
    /** "\n" new line string **/
    public static final String NEW_LINE = "\n";
    /** "\r\n" PC new line string **/
    public static final String CRLF = "\r\n";
    /** "\r" carriage return **/
    public static final String CARRIAGE_RETURN = "\r";

    /** "UTF-8" charset **/
    public static final String CHARSET_UTF8 = "UTF-8";
	/** The charset used to translate the base64 encoded bytes to string,
	 *  and translate that string back to base64 encoded bytes.*/
	public static final String KEY_UTF8_CHARSET = "UTF-8";

	/**'http.proxyHost'*/
	public static final String SYSTEM_PROPERTY_PROXY_HOST = "http.proxyHost";
	/**'http.proxyPort'*/
	public static final String SYSTEM_PROPERTY_PROXY_PORT = "http.proxyPort";
	/**'http.proxyBypass'*/
	public static final String SYSTEM_PROPERTY_PROXY_BYPASS = "http.proxyBypass";
	/**'http.nonProxyHosts'*/
	public static final String SYSTEM_PROPERTY_NON_PROXY_HOSTS = "http.nonProxyHosts";

  /** <br><em>Purpose:</em> find a match based on the parameter by walking the list,
   ** finding a string which contains the matching substring, and returning the index
   ** into the list if it matched.
   * <br><em>Side Effects:</em> The iterator will be moved to the appropriate item (one past),
   * therefore, the value of the item can also be fetched with iter.previous()
   * <br><em>Assumptions:</em>  match is not null; All elements on the supporting List
   * are of type String; first match will be considered the match, subsequent matches would
   * therefore be ignored.
   * @param                     iter, ListIterator
   * @param                     match, String
   * @return                    index if match, -1 if no match
   **/
  public static int findMatchIndex (ListIterator iter, String match) {
    int result = -1;
    for(int i=0; iter.hasNext(); i++) {
      String next = (String) iter.next();
      if (next.indexOf(match) >= 0) {
        result = i;
        break;
      }
    }
    return result;
  }

  /** <br><em>Purpose:</em> find an exact match based on the parameter by walking the list,
   ** finding a string which equals the matching string, and returning the index
   ** into the list if it matched.
   * <br><em>Side Effects:</em> The iterator will be moved to the appropriate item (one past),
   * therefore, the value of the item can also be fetched with iter.previous()
   * <br><em>Assumptions:</em>  match is not null; All elements on the supporting List
   * are of type String; first match will be considered the match, subsequent matches would
   * therefore be ignored.
   * @param                     iter, ListIterator
   * @param                     match, String
   * @return                    index if match, -1 if no match
   **/
  public static int findExactMatchIndex (ListIterator iter, String match) {
    int result = -1;
    for(int i=0; iter.hasNext(); i++) {
      String next = (String) iter.next();
      if (next.equals(match)) {
        result = i;
        break;
      }
    }
    return result;
  }

  /**parse a string to tell if it represnet case insensitive.*/
  public static boolean isCaseSensitive(String caseSensitiveStr){
	  return !((caseSensitiveStr.equalsIgnoreCase(CASE_INSENSITIVE))||
			  (caseSensitiveStr.equalsIgnoreCase(CASEINSENSITIVE))||
			  (caseSensitiveStr.equalsIgnoreCase("FALSE")));
  }

  /**
   * <em>Purpose:</em> evaluate 2 string values to see if they match.
   * If exactMatch is true, then the 2 strings must match exactly.
   * If exactMatch is false, then we will not only ignore case, but the
   * source string can merely be a substring appearing anywhere in the target.
   * @param    source, String to match against target.  Can be a case-insensitive
   *           substring of target if exactMatch is false.
   * <p>
   * @param    target, String to compare with source.
   * <p>
   * @param    exactMatch, false allows a case-insensitive comparison for a source
   *           substring in target.  Otherwise, the source and target must match exactly.
   * <p>
   * @return   true for a match as described above or if *both* source and target are null.
   *           false if no match as described above or if either source or target are null.
   **/
  public static boolean isCaseContainsMatch (String source, String target, boolean exactMatch) {

	if ((source==null)&&(target==null)) return true;
	if ((source==null)||(target==null)) return false;
	if (exactMatch) return source.equals(target);
	return (target.toLowerCase().indexOf(source.toLowerCase()) > -1);
  }


  /** <br><em>Purpose:</em> get a string with the number of specified spaces
   * @param                     num Integer
   * @return                    String with spaces
   **/
  public static String getSpaces (Integer num) {
    if (num == null) return "";
    StringBuffer b = new StringBuffer(20);
    for(int i=0; i<num.intValue(); i++) b.append(" ");
    return b.toString();
  }

  /** <br><em>Purpose:</em> get a string with the number of specified sequenced 'chars'
   * @param                     num Integer
   * @param                     chars String
   * @return                    String with spaces
   **/
  public static String getChars (Integer num, String chars) {
    if (chars==null || chars.length()==0) return getSpaces(num);
    if (num == null) return "";
    StringBuffer b = new StringBuffer(20);
    for(int i=0; i<num.intValue(); ) {
      if (i+chars.length()-1 < num.intValue()) {
        b.append(chars);
        i+= chars.length();
      } else {
        b.append(" ");
        i+= chars.length();
      }
    }
    return b.toString();
  }

  /** <br><em>Purpose:</em> read all contents of BufferedReader, returns collection of lines with no CR or
   ** LF
   * @param                     filename, String
   * @return                    Collection (instance of type ArrayList)
   **/
  private static Collection readBuffer(BufferedReader reader)throws IOException{
	Collection result = new ArrayList();
	String line = null;
	do {
	  line = reader.readLine();
	  if (line != null) {
		result.add(line);
	  }
	} while (line != null);
	reader.close();
	return result;
  }

  /** <br><em>Purpose:</em> read all contents of BufferedReader.<br>
   * Does NOT strip out any content or line-endings.
   * @param                     filename, String
   * @return                    StringBuffer
   **/
  private static StringBuffer readRawBuffer(BufferedReader reader)throws IOException{
	StringBuffer data = new StringBuffer();
	char[] charbuff = new char[1024];
	int read = 0;
	do {
		read = reader.read(charbuff);
		if(read > 0){
			data.append(charbuff, 0, read);
		}
	} while (read != -1);
	reader.close();
	return data;
  }

  public static String PROPERTY_START = ":PROPERTY:";

  /** <br><em>Purpose:</em> read all contents of BufferedReader assuming the contents are in
   * a SAFS Properties file format:
   * <pre>
   * :PROPERTY:propertyName
   * property value possibly spanning
   * multiple lines.
   * :PROPERTY:propertyName
   * etc...
   * </pre>
   * @param                     reader
   * @return                    Properties
   **/
  private static Map<String,String> readPropertiesMap(BufferedReader reader)throws IOException{
	    Map<String,String> props = new HashMap<String,String>();
		String name = "";
		String natural = "";
		String logical = "";
		String test = null;
		do {
		  natural = reader.readLine();
		  if (natural != null) {
			  test = natural.trim();
			  try{
				  if(test.startsWith(PROPERTY_START)){
					  // save last property value
					  if(name.length() > 0)
						  props.put(name, logical);
					  // begin new property
					  name = test.substring(PROPERTY_START.length()).trim();
					  logical = "";
				  }else{
					  logical = logical.length() > 0   ?
							    logical +"\n"+ natural :
							    natural;
				  }
			  }catch(Exception ignore){}
		  }else{
			  // gotta save that last property!
			  if(name.length() > 0)
				  props.put(name, logical);
		  }
		} while (natural != null);
		reader.close();
		return props;
  }

  /**
   * read file based on 'filename', returns collection of lines with no CR or
   * LF.  The file is assumed to be in the default System character encoding.
   * @param                     filename, String
   * @return                    Collection (instance of type ArrayList)
   * @see FileUtilities#getSystemBufferedFileReader(String)
   * @see #readBuffer(BufferedReader)
   **/
  public static  Collection readfile(String filename) throws IOException {
    return readBuffer(FileUtilities.getSystemBufferedFileReader(filename));
  }

  /**
   * read file based on 'filename', returns StringBuffer of file contents.<br>
   * The file is assumed to be in the default System character encoding.
   * @param                     filename, String
   * @return                    StringBuffer
   * @see FileUtilities#getSystemBufferedFileReader(String)
   * @see #readRawBuffer(BufferedReader)
   **/
  public static  StringBuffer readRawFile(String filename) throws IOException {
    return readRawBuffer(FileUtilities.getSystemBufferedFileReader(filename));
  }

  /**
   * read file based on 'filename', returns collection of lines with no CR or
   * LF.  The file is assumed to be using UTF-8 character encoding.
   * @param                     filename, String
   * @return                    Collection (instance of type ArrayList)
   * @see FileUtilities#getUTF8BufferedFileReader(String)
   * @see #readBuffer(BufferedReader)
   **/
  public static  Collection readUTF8file(String filename) throws IOException {
	return readBuffer(FileUtilities.getUTF8BufferedFileReader(filename));
  }

  /**
   * read file based on 'filename', returns StringBuffer of file contents.<br>
   * The file is assumed to be using UTF-8 character encoding.
   * @param                     filename, String
   * @return                    StringBuffer
   * @see FileUtilities#getUTF8BufferedFileReader(String)
   * @see #readRawBuffer(BufferedReader)
   **/
  public static  StringBuffer readRawUTF8File(String filename) throws IOException {
	return readRawBuffer(FileUtilities.getUTF8BufferedFileReader(filename));
  }

  /**
   * read file based on 'filename', returns collection of lines with no CR or
   * LF.  The file is assumed to be using character encoding indicated by parameter.
   * @param                     filename, String
   * @param                     encoding, the character encoding used to read a file.
   * @return                    Collection (instance of type ArrayList)
   * @see FileUtilities#getUTF8BufferedFileReader(String)
   * @see #readBuffer(BufferedReader)
   **/
  public static  Collection readEncodingfile(String filename, String encoding) throws IOException {
	return readBuffer(FileUtilities.getBufferedFileReader(filename, encoding));
  }

  /**
   * read file based on 'filename', returns collection of lines with no CR or
   * LF.  The file is assumed to be using character encoding indicated by parameter.
   * @param                     filename, String
   * @param                     encoding, the character encoding used to read a file.
   * @return                    Collection (instance of type ArrayList)
   * @see FileUtilities#getUTF8BufferedFileReader(String)
   * @see #readBuffer(BufferedReader)
   **/
  public static Map<String,String> readEncodingMap(String filename, String encoding) throws IOException {
	return readPropertiesMap(FileUtilities.getBufferedFileReader(filename, encoding));
  }

  /**
   * Encode the object to a string with charset {@value #KEY_UTF8_CHARSET}.<br>
   *
   * @param object, Object an object must implements interface Serializable.
   * @return	String, the base64 encoded string with charset {@value #KEY_UTF8_CHARSET}
   * @throws IllegalThreadStateException
   * @see {@link #decodeBase64Object(String)}
   */
  public static String encodeBase64Object(Object object) throws IllegalThreadStateException{
	  return Base64Encoder.encodeBase64Object(object, KEY_UTF8_CHARSET);
  }

  /**
   * Decode a string to object with charset {@value #KEY_UTF8_CHARSET}.<br>
   *
   * @param base64String, String, the base64 encoded string with charset {@value #KEY_UTF8_CHARSET}
   * @return Object, an object who implements interface Serializable.
   * @throws IllegalThreadStateException
   * @see {@link #encodeBase64Object(Object)}
   */
  public static Object decodeBase64Object(String base64String) throws IllegalThreadStateException{
	  return Base64Decoder.decodeBase64Object(base64String, KEY_UTF8_CHARSET);
  }

  /**
   * @param source
   * @param target
   * @return true if both Maps have the same number of keys and matching key values.
   */
  public static boolean isMatchingMaps(Map<String,String> source, Map<String,String> target){
		Set<String> propkeys = source.keySet();
		Set<String> benchkeys = target.keySet();
		boolean matched = false;
		boolean mismatched = false;
		if(propkeys.size()==benchkeys.size()){
			String propval = null;
			String benchval = null;
			for(String key:propkeys){
				propval = source.get(key);
				benchval = target.get(key);
				if(benchval == null || propval == null || !propval.equals(benchval)){
					IndependantLog.debug("SU.isMatchingMaps property '"+ key +"' did not match: \n"+
	                         propval +"\n did not match\n"+ benchval);
					mismatched = true;
					break;
				}
			}
			matched = !mismatched;
		}else{
			IndependantLog.debug("SU.isMatchingMaps do NOT have the same number of properties: "+
		                         propkeys.size()+" to "+ benchkeys.size());
		}
		return matched;
  }

  /**
   * Compares 2 Maps, but only cares about matching those key/value items that exist
   * in the target/benchmark.  The source can have additional key/value pairs that are ignored.
   * @param source -- runtime Map to be compared against the target/benchmark.
   * @param target -- benchmark to be compared against.
   * @return true if source map values match target map values for those keys in the target/benchmark.
   */
  public static boolean isMatchingTargetMapValues(Map<String,String> source, Map<String,String> target){
		Set<String> benchkeys = target.keySet();
		boolean matched = false;
		boolean mismatched = false;
		String propval = null;
		String benchval = null;
		for(String key:benchkeys){
			propval = source.get(key);
			benchval = target.get(key);
			if(benchval == null || propval == null || !propval.equals(benchval)){
				IndependantLog.debug("SU.isMatchingTargetMapValues property '"+ key +"' did not match: \n'"+
                         propval +"'\n did not match\n'"+ benchval+"'");
				mismatched = true;
				break;
			}
		}
		matched = !mismatched;
		return matched;
  }

  /**
   * read string, returns collection of lines with no CR or LF.  This essentially
   * splits the String at line separators into an array of Strings.
   * @param                     str, String,
   * @return                    Collection (instance of type ArrayList)
   * @see StringReader
   * @see BufferedReader#readLine()
   **/
  public static  Collection readstring(String str) throws IOException {
    BufferedReader reader;
    Collection result = new ArrayList();
    reader = new BufferedReader(new StringReader(str));
    String line = null;
    do {
      line = reader.readLine();
      if (line != null) {
        result.add(line);
      }
    } while (line != null);
    reader.close();
    return result;
  }

  /** <br><em>Purpose:</em> read Binary file based on 'filename', returns String
   * @param                     filename, String
   * @return                    String
   * NOTE: I believe that there is a bug in the java class(es) BufferedReader/FileReader
   * when reading certain characters from a binary file.  Therefore, the implementation
   * here is with the older BufferedInputStream/FileInputStream classes, which, through
   * exhaustive testing, I find to do a better job of reading in binary characters.
   **/
  public static StringBuffer readBinaryFile(String filename) throws IOException {
    InputStream reader = new BufferedInputStream(new FileInputStream(new CaseInsensitiveFile(filename).toFile()));
    StringBuffer buf = new StringBuffer(maxBytesPerRead);
    String line = null;
    for(; ; ) {
      int i= reader.read();
      if (i == -1){
        break;
      }
      buf.append((char)i);
    }
    reader.close();
    return buf;
  }

  /**
   * write to file 'filename' the toString() values contained in list.
   * Each item is written as a separate line using '\n' as the line separator
   * regardless of operating system.
   * Values are written in the system default character encoding.
   *
   * @param filename String full absolute path filename of file to write.
   * @param list Collection of lines to write.
   * @throws FileNotFoundException if file cannot be created for any reason.
   * @throws IOException if an error occurs during write operations.
   * @see FileUtilities#getSystemBufferedFileWriter(String)
   * @see FileUtilities#writeCollectionToFile(BufferedWriter, Collection, String)
   **/
  public static  void writefile(String filename, Collection list) throws IOException {
      BufferedWriter writer = FileUtilities.getSystemBufferedFileWriter(filename);
      FileUtilities.writeCollectionToFile(writer, list, NEW_LINE);
  }

  /**
   * Note: The file will be opened as UTF-8 OutputStreamWriter
   * write to file 'filename' the toString() values contained in list.
   * Each item is written as a separate line using '\n' as the line separator
   * regardless of operating system.
   * Values are written in the UTF-8 encoding.
   *
   * @param filename String full absolute path filename of file to write.
   * @param list Collection of lines to write.
   * @throws FileNotFoundException if file cannot be created for any reason.
   * @throws IOException if an error occurs during write operations.
   * @see FileUtilities#getSystemBufferedFileWriter(String)
   * @see FileUtilities#writeCollectionToFile(BufferedWriter, Collection, String)
   **/
  public static  void writeUTF8file(String filename, Collection list) throws IOException {
      BufferedWriter writer = FileUtilities.getUTF8BufferedFileWriter(filename);
      FileUtilities.writeCollectionToFile(writer, list, NEW_LINE);
  }

  /**
   * Note: The file will be opened as OutputStreamWriter with encoding
   * write to file 'filename' the toString() values contained in list.
   * Each item is written as a separate line using '\n' as the line separator
   * regardless of operating system.
   * Values are written in the encoding indicated by parameter
   *
   * @param filename   String full absolute path filename of file to write.
   * @param list       Collection of lines to write.
   * @param encoding   Encoding is used to write a file.
   * @throws FileNotFoundException if file cannot be created for any reason.
   * @throws IOException if an error occurs during write operations.
   * @see FileUtilities#getSystemBufferedFileWriter(String)
   * @see FileUtilities#writeCollectionToFile(BufferedWriter, Collection, String)
   **/
  public static  void writeEncodingfile(String filename, Collection list, String encoding) throws IOException {
      BufferedWriter writer = FileUtilities.getBufferedFileWriter(filename, encoding);
      FileUtilities.writeCollectionToFile(writer, list, NEW_LINE);
  }

  /**
   * Convert a collection of text entries formatted as name=value into a Map file.
   * Each item in the collection has potential linebreaks "normalized".
   * @param contents
   * @param sep the separator used to delimit the name from the value
   * @return Map<String,String>
   * @see #normalizeLineBreaks(String)
   */
  public static Map<String,String> convertCollectionToMap(Collection<String> contents, String sep){
	 Map<String,String> props = new HashMap<String,String>();
	 if((contents != null) && (!contents.isEmpty())){
		 int s = 0;
		 String name = null;
		 String value = null;
		 for(String item:contents){
			 s = item.indexOf(sep);
			 if(s > 0){
				 name = item.substring(0, s).trim();
				 try{
					 value = item.substring(s+1);
				 }catch(Exception x){
					 value = "";
				 }
				 value = normalizeLineBreaks(value);
				 props.put(name, value);
			 }
		 }
	 }
	 return props;
  }

  /**
   * Convert window's position-size-status string of the formats:
   * <ul>
   * <li>"x;y;width;height;status"
   * <li>"x,y,width,height,status"
   * <li>"x y width height status"
   * <li>"Coords=x;y;width;height;Status=status"
   * <li>"Coords=x,y,width,height,Status=status"
   * <li>"Coords=x y width height Status=status"
   * </ul>
   * into a org.safs.ComponentFunction.Window object.
   *
   * @param   windowPosition String, window's position-size-status string
   * @return  org.safs.ComponentFunction.Window if successful, null otherwise
   * @deprecated Please use {@link ComponentFunction#ConvertWindowPosition(String)} instead. <b>This method will be removed in the future.</b>
   **/
  @Deprecated
  public static Window convertWindowPosition(String windowPosition) {
	  return ComponentFunction.ConvertWindowPosition(windowPosition);
  }

  /**
   * Note: The file will be opened as OutputStreamWriter with encoding
   * write to file 'filename' the toString() values contained in list.
   * Each property is written as in a special format allowing for multi-line property values.
   * Values are written in the encoding indicated by parameter
   *
   * @param filename   String full absolute path filename of file to write.
   * @param list       Properties to write.
   * @param encoding   Encoding is used to write a file.
   * @throws FileNotFoundException if file cannot be created for any reason.
   * @throws IOException if an error occurs during write operations.
   * @see FileUtilities#getSystemBufferedFileWriter(String)
   * @see FileUtilities#writePropertiesFile(BufferedWriter, Properties)
   **/
  public static  void writeEncodingProperties(String filename, Map<String,String> list, String encoding) throws IOException {
      BufferedWriter writer = FileUtilities.getBufferedFileWriter(filename, encoding);
      FileUtilities.writePropertiesFile(writer, list);
  }

  /**
   * @deprecated for DatabaseUtils.writefile(String, Collection, String)
   * @see DatabaseUtils#writefile(String, Collection, String)
   **/
  public static  void writefile(String filename, Collection list, String delim) throws IOException {
  	DatabaseUtils.writefile(filename, list, delim);
  }

  /**
   * @deprecated for DatabaseUtils.getDBVal(Object)
   * @see DatabaseUtils#getDBVal(Object)
   **/
  public static String getDBVal (Object m) {
  	return DatabaseUtils.getDBVal(m);
  }


	/**
	 * Trims all chars below char (int) 32 EXCEPT for TAB char (int) 9
	 * from the left side of the provided text string.  Does not modify the input
	 * text string.
	 * @return a new trimmed string if changes were made, or the text string passed
	 * as input if no changes were made, or null if the provided text was null.
	 */
	public static String leftTrimSpace(String text){
		String lttext = text;
		try{
			if (text.length() > 0){
				int i = 0;
			    for(;i<text.length();i++){
			        char c = text.charAt(i);
			        if (c == (int) 9) break;
			        if (c > (int)32) break;
			    }
			    if( i < text.length()) lttext = text.substring(i);
			}
			return lttext;
		}catch(NullPointerException npx){ return null;}
	}

	/**
	 * Trims all chars below char (int) 32 EXCEPT for TAB characters (int) 9
	 * from the right side of the provided text string.  Does not modify the input
	 * text string.
	 * @return a new trimmed string if changes were made, or the text string passed
	 * as input if no changes were made, or null if the provided text was null.
	 */
	public static String rightTrimSpace(String text){
		String rttext = text;
		try{
			if (text.length() > 0){
				int i = text.length();
			    for(;i>0;i--){
			        char c = text.charAt(i-1);
			        if (c == (int) 9) break;
			        if (c > (int)32) break;
			    }
			    if( i > 0) rttext = text.substring(0,i);
			}
			return rttext;
		}catch(NullPointerException npx){ return null;}
	}


  /** <br><em>Purpose:</em> This method takes a string trims it of leading and trailing
   * spaces, non-breaking spaces, and tabs,
   * and then strips one leading and/or trailing quotation mark(#34)--if they exist.
   * @param                     str, String to unquote
   * @return String result, null if str was null
   **/
  public static String getTrimmedUnquotedStr (String str)  {
    String quote = "\"";
    String result = str;
    char cspace = (char)32;
    char cnbspace = (char)160;
    char ctab = (char)9;
    if (result == null) return result;
    if (result.length() == 0) return result;

    //problem: trims encoded quotes during expression processing
    //result = result.trim();
    int smark = 0;
    int emark = result.length();
    char check = 0;
    int i = smark;
    boolean mod = false;
    //only stays in the loop while leading whitespace is detected
    for( ; i < emark; i++){
    	check = result.charAt(i);
    	if((check != cspace)&&
    	  (check != cnbspace)&&
    	  (check != ctab))
    	  break;
    	mod = true; //at least one whitespace char was detected
    }
    smark = i;
    i = emark;
    //only stays in the loop while trailing whitespace is detected
    for( ; i > smark; i--){
    	check = result.charAt(i-1);
    	if((check != cspace)&&
    	  (check != cnbspace)&&
    	  (check != ctab))
    	  break;
    	mod = true; //at least one whitespace char was detected
    }
    emark = i;
    if (smark == emark) return ""; // all whitespace

    // trim leading and\or trailing whitespace
    if(mod) result = result.substring(smark, emark);

    if (result.substring(0, 1).equals(quote)) {
      result = result.substring(1, result.length());
    }
    if (result.length() == 0) return result;
    if (result.substring(result.length()-1, result.length()).equals(quote)) {
      result = result.substring(0, result.length()-1);
    }
    return result;
  }

  /** <br><em>Purpose:</em> concatenate strings
   * @param                     s1, String
   * @param                     s2, String
   * @param                     s3, String
   * @return                    s1+s2+s3
   **/
  public static String concat(String s1, String s2, String s3) {
    return s1+s2+s3;
  }

	/** convert ? and * wildcarding to regular expression wildcarding .? and .*
	 **/
	public static String convertWildcardsToRegularExpression(String input){

		int len = -1;

		// don't play with bad input
		try{ len = input.length();}
		catch(NullPointerException np){ return null;}

		StringBuffer buffer = new StringBuffer(input);
		String value = null;

		// loop backwards through the string and insert . as necessary
		for (int i = len -1; i >= 0; i--){

			value = input.substring(i, i+1);
			if ((value.equals(REGEX_CHARACTER_MATCH_MANY)) ||
			    (value.equals(REGEX_CHARACTER_MATCH_ONE))){

		 	   buffer.insert(i, REGEX_CHARACTER_ANY);
			}
		}

		return buffer.toString();
	}

  private static Object patternInstance  = null;
  private static Method patternMethod  = null;

  private static Class charSequenceClass = null;
  static{
  	try{ charSequenceClass = Class.forName("java.lang.CharSequence");}
  	catch(Exception e){;}
  }

  private static final Class [] patternType = {String.class, charSequenceClass};
  private static Object []patternParams = new Object[2];
  private static Object reInstance  = null;
  private static Constructor reConst = null;
  private static Method reMethod  = null;
  private static final Class [] reType = {String.class};
  private static final Class [] reConstParameterTypes = {String.class};
  private static final Object [] reInitargs = new Object[1];
  private static Object []reParams = new Object[1];
  /** <br><em>Purpose:</em> match a regular expression to a value.
   * <br><em>Assumptions:</em>  tries first jdk 1.4 built in java.util.regex.Pattern class, then
   * the jakarta org.apache.regexp.RE class.  If either are found, it tries to keep
   * static methods and/or instances around so that reflection is minimized the next
   * time around.
   * @param                     expression, String
   * @param                     value, String
   * @return                    boolean, true if match, false otherwise
   * @exception                 SAFSRegExException if an error occurs.
   * @exception                 SAFSRegExNotFoundException if no RegEx support class can be found.
   **/
  public static boolean matchRegex(String expression, String value) throws SAFSException {
    String patternClassName = "java.util.regex.Pattern";
    String patternMethodName = "matches";
    if (patternMethod != null) {

      patternParams[0] = expression;
      patternParams[1] = value;
      try { // now invoke
        Object obj = patternMethod.invoke(null, patternParams);

        Boolean result = (Boolean) obj;
        return result.booleanValue();
      }
      catch (Exception re) {}
    } else {
      try {
        Class patternClass = Class.forName(patternClassName);

        // now get the method, as in: java.util.regex.Pattern.matches(expression, value);
        try {
          patternMethod = patternClass.getMethod(patternMethodName, patternType);
          patternParams[0] = expression;
          patternParams[1] = value;

          // now invoke
          Object obj = patternMethod.invoke(null, patternParams);

          Boolean result = (Boolean) obj;
          return result.booleanValue();
        } catch (java.lang.NoSuchMethodException nsme) {
          patternMethod = null;
        } catch (java.lang.IllegalArgumentException iae) {
          patternMethod = null;
        } catch (java.lang.IllegalAccessException iae2) {
          patternMethod = null;
        } catch (java.lang.reflect.InvocationTargetException ite) {
          patternMethod = null;
        } catch (java.lang.ClassCastException cce) {
          patternMethod = null;
        }
      } catch (NoClassDefFoundError nc) {
      } catch (ClassCastException cc) {
      } catch (ClassNotFoundException ex) {
      }
    }
    // falls here if java 1.4 not installed or working properly
    String reClassName = "org.apache.regexp.RE";
    String reMethodName = "match";
    reMethod=null;
    if (reMethod != null) {

      reInitargs[0] = expression;
      reParams[0] = value;
      try { // now invoke
        reInstance = reConst.newInstance(reInitargs);
        Object obj = reMethod.invoke(reInstance, reParams);
        Boolean result = (Boolean) obj;
        return result.booleanValue();
      } catch (InstantiationException ie) {
        IndependantLog.error("matchRegex, using classname: "+reClassName, ie);
      } catch (java.lang.IllegalAccessException iae) {
        IndependantLog.error("matchRegex, using classname: "+reClassName, iae);
      } catch (java.lang.reflect.InvocationTargetException ite) {
        //throw new SAFSException("expression: "+expression+", "+ite.toString());
        throw new SAFSRegExException(
              "StringUtilities.matchRegex: "+ expression +" :"+ ite.toString());
      } catch (Exception reex) {
        //throw new SAFSException("expression: "+expression+", "+reex.toString());
        throw new SAFSRegExException(
              "StringUtilities.matchRegex: "+ expression +" :"+ reex.toString());
      }
    } else {
      try {
        Class reClass = Class.forName(reClassName);
        //System.out.println("reClass: "+reClass.getName());
        reConst = reClass.getConstructor(reConstParameterTypes);
        // this is the dynamic part...
        reInitargs[0] = expression;
        reInstance = reConst.newInstance(reInitargs);
        //System.out.println("reInstance: "+reInstance);
        // now get the method, as in: java.util.regex.Re.matches(expression, value);
        try {
          reMethod = reClass.getMethod(reMethodName, reType);

          reParams[0] = value;
          // now invoke
          Object obj = reMethod.invoke(reInstance, reParams);

          Boolean result = (Boolean) obj;
          return result.booleanValue();
        } catch (java.lang.NoSuchMethodException nsme) {
          IndependantLog.error("matchRegex, using classname: "+reClassName, nsme);
          reMethod = null;
        } catch (java.lang.IllegalArgumentException iae) {
          IndependantLog.error("matchRegex, using classname: "+reClassName, iae);
          reMethod = null;
        } catch (java.lang.IllegalAccessException iae2) {
          IndependantLog.error("matchRegex, using classname: "+reClassName, iae2);
          reMethod = null;
        } catch (java.lang.reflect.InvocationTargetException ite) {
              throw new SAFSRegExException("expression: "+expression+", "+ite.toString());
        } catch (java.lang.ClassCastException cce) {
          IndependantLog.error("matchRegex, using classname: "+reClassName, cce);
          reMethod = null;
        }
      } catch (NoSuchMethodException nsme) {
        IndependantLog.error("matchRegex, using classname: "+reClassName, nsme);
      } catch (java.lang.reflect.InvocationTargetException ite) {
        IndependantLog.error("matchRegex, using classname: "+reClassName, ite);
      } catch (NoClassDefFoundError nc) {
        IndependantLog.error("matchRegex, using classname: "+reClassName+ nc);
      } catch (ClassCastException cc) {
        IndependantLog.error("matchRegex, using classname: "+reClassName, cc);
      } catch (InstantiationException ie) {
        IndependantLog.error("matchRegex, using classname: "+reClassName, ie);
      } catch (ClassNotFoundException ex) {
        IndependantLog.error("matchRegex, using classname: "+reClassName, ex);
      } catch (IllegalAccessException iae) {
        IndependantLog.error("matchRegex, using classname: "+reClassName, iae);
      } catch (Exception reex) {
        throw new SAFSRegExException("expression: "+expression+", "+reex.toString());
      }
    }
    throw new SAFSRegExNotFoundException(
              "StringUtilities.matchRegex: No Regular Expression support found!  "+
              "The engine needs to be run with a 1.4 JVM or later; or, the Jakarta "+
              "class 'org.apache.regexp.RE' must be available in the CLASSPATH.");
  }

	/**
	 * match a text(provided as fullstring, substing, regex) against<br>
	 * the actual text got from application component(list, menu, tree, tab etc.)<br>
	 * @param actualText String, the actual text of the component's label
	 * @param expectedText String, the expected text, it can be full-string, sub-string or regex.
	 * @param partialMatch boolean, if the expectedText is provided as sub-string
	 * @param ignoreCase boolean, if the texts are case in-sensitive to compare
	 * @return boolean, true if matched.
	 */
	public static boolean matchText(String actualText, String expectedText, boolean partialMatch, boolean ignoreCase){
		String debugmsg = debugmsg(StringUtils.class, "matchText");
		boolean matched = false;

		try{
			if(ignoreCase){
				actualText = actualText.toLowerCase();
				expectedText = expectedText.toLowerCase();
			}

			if(partialMatch){
				matched = actualText.contains(expectedText);
			}else{
				matched = actualText.equals(expectedText);
			}
			if(!matched) matched = matchRegex(expectedText, actualText);

		}catch(Exception e){
			IndependantLog.debug(debugmsg+"fail to match '"+expectedText+"' against '"+actualText+"'",e);
		}

		return matched;
	}

  public static String[] getSortArray(String[] inp) {
    String[] res = new String[inp.length];
    TreeMap map = new TreeMap();
    for(int i=0; i<inp.length; i++) {
      map.put(inp[i]+(new Integer(i+100000)).toString(), inp[i]);
    }
    int k=0;
    for(Iterator j=map.keySet().iterator(); j.hasNext(); ) {
      String l = (String) j.next();
      String n = (String) map.get(l);
      res[k++] = n;
    }
    return res;
  }

  /** <br><em>Purpose:</em> get 0-based field N of input by tokenizing with sep.
   * We use a SAFSStringTokenizer.  The first field is field #0.
   * @param                     input, String
   * @param                     n, int 0-based index
   * @param                     sep, String
   * @return                    token
   * @exception                 SAFSNullPointerException if input or sep are null
   * @exception                 java.lang.StringIndexOutOfBoundsException if not found for index n
   **/
  public static String getInputToken (String input, int n,
                                      String sep) throws SAFSNullPointerException {
    if (input != null && sep != null) {
      int j=0;
      for (StringTokenizer st = new SAFSStringTokenizer(input, sep); st.hasMoreTokens(); j++) {
        String next = st.nextToken();
        if (j==n) return next;
      }
      throw new java.lang.StringIndexOutOfBoundsException("cannot find for index: "+n);
    } else {
      throw new SAFSNullPointerException("either 'input' or 'sep' are null");
    }
  }

  /** <br><em>Purpose:</em> Finds the count of all fields within the inputRecord found from
   **        startindex to the end of the inputRecord.
   * @param                     input, String
   * @param                     startindex, int
   * @param                     seps, String
   * @return                    count
   * @exception                 SAFSNullPointerException if input or sep are null
   **/
  public static int getFieldCount (String input, int startindex,
                                   String seps) throws SAFSNullPointerException {
    if (input != null && seps != null) {
      int j=0;
      for (StringTokenizer st = new SAFSStringTokenizer(input.substring(startindex, input.length()), seps); st.hasMoreTokens(); j++) {
        String next = st.nextToken();
      }
      return j;
    } else {
      throw new SAFSNullPointerException("either 'input' or 'seps' are null");
    }
  }

  /**
   * As the List returned by Arrays.asList() does not support remove operation.<br>
   * So we provide this method ourselves.
   * @param array T[], an array containing some value
   * @return List<T>, the converted list
   */
  public static <T> List<T> arrayToList(T[] array){
	  List<T> list = new ArrayList<T>();
	  if(array!=null && array.length>0){
		  for(T value: array) list.add(value);
	  }
	  return list;
  }

  /**
   * <em>Purpose:</em> Get tokens of text delimited by delimiter, delimiter will be considered as a whole string.
   *                   If the tokens contain any leading/ending spaces, they will be removed.
   *
   * @param text		String, the delimited string
   * @param delimiter	String, the delimiter
   * @return	List, a list of tokens
   * @see #getTrimmedTokenList(String, String, Character)
   */
  public static List<String> getTrimmedTokenList(String text, String delimiter){
	  return getTrimmedTokenList(text, delimiter, null);
  }

  /**
   * <em>Purpose:</em> Get tokens of text separated by delimiter, delimiter will be considered as a whole string,
   * 				   if some token contains the delimiter, user needs to escape the delimiter. If the tokens
   *                   contain any leading/ending spaces, they will be removed.
   *
   * @param text		String, the delimited string
   * @param delimiter	String, the delimiter
   * @param escapeChar	Character, the char to escape the delimiter
   * @return	List, a list of tokens
   * @see #getTokenList(String, String, Character)
   */
  public static List<String> getTrimmedTokenList(String text, String delimiter, Character escapeChar){
	  List<String> tokens = getTokenList(text, delimiter, escapeChar);

	  String temp = null;
	  for(int i=0;i<tokens.size();i++){
		  temp = tokens.get(i);
		  if(temp!=null) tokens.set(i, temp.trim());
	  }

	  return tokens;
  }

  /**
   * <em>Purpose:</em> Get tokens of text delimited by delimiter, delimiter will be considered as a whole string
   *
   * <br>Example: If delimiter is "->", text is "parent->child->grandChild", then the returned list will contains
   * <br>		  3 items: "parent", "child" and "grandChild".
   * @param text		String, the delimited string
   * @param delimiter	String, the delimiter
   * @return	List, a list of tokens
   * @see #getTokenList(String, String, Character)
   */
  public static List<String> getTokenList(String text, String delimiter){
	  return getTokenList(text, delimiter, null);
  }

  /**
   * <em>Purpose:</em> Get tokens of text delimited by delimiter, delimiter will be considered as a whole string,
   * 				   if some token contains the delimiter, user needs to escape the delimiter.
   *
   * <br>Example: If delimiter is "=", text is "css=input[name*='sf.c']", then the returned list will contains
   * <br>		  3 items: "css", "input[name*" and "'sf.c']";
   * <br>		  But if user wants "input[name*='sf.c']" as one whole token, then he needs to escape the delimiter,
   * <br>		  specify the text as css=input[name*\='sf.c'], and call this method with '\' for parameter escapeChar,
   * <br>		  then the result will be 2 items: "css" and "input[name*='sf.c']".
   * <br>		  <b>Note: Nothing MUST NOT appear between 'escape char' and 'delimiter'.</b>
   *
   * @param text		String, the delimited string
   * @param delimiter	String, the delimiter
   * @param escapeChar	Character, the char to escape the delimiter
   * @return	List, a list of tokens
   */
  public static List<String> getTokenList(String text, String delimiter, Character escapeChar){
	  List<String> tokenList = new ArrayList<String>();
	  if(text==null || text.equals("")) return tokenList;
	  if(delimiter==null || delimiter.equals("")){
		  tokenList.add(text);
		  return tokenList;
	  }
	  int delimiterIndex = -1;
	  int delimiterLength = delimiter.length();
	  int startPosition = 0;
	  while(true){
		  delimiterIndex = text.indexOf(delimiter, startPosition);
		  if(delimiterIndex==-1){
			  tokenList.add(removeEscapeChar(text, delimiter, escapeChar));
			  break;
		  }
		  //Check if the escape character exists
		  if(escapeChar!=null && delimiterIndex>0){
			  //Get the char before the delimiter, and comare it with the escapeChar
			  String charBeforeDelimiter = text.substring(delimiterIndex-1, delimiterIndex);
			  if(charBeforeDelimiter.equals(escapeChar.toString())){
				  startPosition = delimiterIndex+delimiterLength;
				  continue;
			  }else{
				  startPosition = 0;
			  }
		  }

		  tokenList.add(removeEscapeChar(text.substring(0,delimiterIndex), delimiter, escapeChar));
		  if(delimiterIndex+delimiterLength==text.length()){
			  //Reach the end
			  break;
		  }
		  text = text.substring(delimiterIndex+delimiterLength);
	  }

	  return tokenList;
  }

  /**
   * @param text		String, the delimited string
   * @param delimiter	String, the delimiter
   * @return	an array of string tokens
   * @see #getTokenList(String, String, Character)
   */
  public static String[] getTokenArray(String text, String delimiter){
	  return getTokenList(text, delimiter).toArray(new String[0]);
  }

  /**
   * @param text		String, the delimited string
   * @param delimiter	String, the delimiter
   * @param escapeChar	Character, the char to escape the delimiter
   * @return	an array of string tokens
   * @see #getTokenList(String, String, Character)
   */
  public static String[] getTokenArray(String text, String delimiter, Character escapeChar){
	  return getTokenList(text, delimiter, escapeChar).toArray(new String[0]);
  }

  /**
   * If the token contains escapeChar+delimiter, then replace it by delimiter.
   */
  private static String removeEscapeChar(String token, String delimiter, Character escapeChar){

	  if(token==null || token.isEmpty()) return token;
	  if(delimiter==null || escapeChar==null) return token;

	  String result = token;
	  String toReplace = escapeChar.toString()+delimiter;

	  if(token.contains(toReplace)){
		  //If the escapeChar equals to the 'Regex escape char', we need to escape it again
		  //by 'Regex escape char', so that the escapeChar will be considered as normal literal and replaced
		  if(escapeChar.equals(REGEX_ESCAPE_CHARACTER)){
			  toReplace = REGEX_ESCAPE_CHARACTER.toString()+toReplace;
		  }
		  result = token.replaceAll(toReplace, delimiter);
	  }

	  return result;
  }

  /**
   * <em>Purpose:</em> Get text strung delimited by delimiter from a list, delimiter will be considered as a whole string
   * <br>Example: If delimiter is "->", if the list contains 3 items, "parent" "child" "grandChild"
   * <br>		  the returned text is "parent->child->grandChild"
   * @param list
   * @param delimiter
   * @return
   */
  public static String getDelimitedString(List<String> list, String delimiter){
	  String delimitedString = "";

	  if(list!=null && delimiter!=null){
		  for(int i=0;i<list.size();i++){
			  delimitedString +=list.get(i)+delimiter;
		  }
	  }
	  //Remove the last delimiter if exists
	  if(delimitedString.endsWith(delimiter)){
		  delimitedString = delimitedString.substring(0, delimitedString.lastIndexOf(delimiter));
	  }

	  return delimitedString;
  }

  public static boolean containsSepcialKeys(String value){
	  boolean contain = false;

	  if(value==null || value.equals("")){
		  return false;
	  }

	  for(int i=0;i<specialKeys.length;i++){
		  if(value.indexOf(specialKeys[i])!=-1){
			  contain = true;
			  break;
		  }
	  }

	  return contain;
  }

  /** An array defines the characters should not be encoded. */
  public static final char[] URL_ENCODING_IGNORE_CHARACTERS = {CHAR_EQUAL, CHAR_AND, CHAR_COLON, CHAR_SLASH, CHAR_INTERROGATION};

  /**
   * Encode URL or form-content. The encoding is {@link #CHARSET_UTF8}, and the escape characters are {@link #URL_ENCODING_IGNORE_CHARACTERS}.
   * @param content String, the URL or form-content to encode
   * @return String, the encoded string
   * @see #urlEncode(String, String, char[])
   */
  public static String urlEncode(String content){
	  return urlEncode(content, CHARSET_UTF8, URL_ENCODING_IGNORE_CHARACTERS);
  }
  /**
   * Encode URL or form-content. The escape characters are {@link #URL_ENCODING_IGNORE_CHARACTERS}.
   * @param content String, the URL or form-content to encode
   * @param encoding String, the encoding to use
   * @return String, the encoded string
   * @see #urlEncode(String, String, char[])
   */
  public static String urlEncode(String content, String encoding){
	  return urlEncode(content, encoding, URL_ENCODING_IGNORE_CHARACTERS);
  }
  /**
   * Encode URL or form-content following the rule defined at <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1">application/x-www-form-urlencoded </a>
   * @param content String, the URL or form-content to encode
   * @param encoding String, the encoding to use
   * @param escapeChars char[], an array of characters to escape, which means these characters will not be encoded.
   * @return String, the encoded string
   */
  public static String urlEncode(String content, String encoding, char[] escapeChars){
	  if(content==null) return null;

	  String debugmsg = debugmsg(false);
	  StringBuffer result = new StringBuffer();
	  try {
		  if(contains(content, escapeChars)){
			  int index = 0;
			  int previousIndex = index;
			  String token = null;
			  String character = null;
			  while(index<content.length()){
				  //take one char from the content
				  character = content.substring(index, index+1);
				  //check if this character is one of escape characters
				  if(contains(character, escapeChars)){
					  //get the token before this escape char
					  token = content.substring(previousIndex, index);
					  //encode the token, and append it to result
					  if(!token.isEmpty()) result.append(URLEncoder.encode(token, encoding));
					  //append the escape char
					  result.append(character);
					  //move the previousIndex to the position after this escape char
					  previousIndex = index+1;
				  }else{
					  if(index+1==content.length()){
						  token = content.substring(previousIndex);
						  if(!token.isEmpty()) result.append(URLEncoder.encode(token, encoding));
					  }
				  }
				  index++;
			  }
		  }else{
			  result.append(URLEncoder.encode(content, encoding));
		  }
	  } catch (UnsupportedEncodingException e) {
		  IndependantLog.error(debugmsg+debugmsg(e));
		  result.append(content);
	  }

	  return result.toString();
  }
  /**
   * check if a string contains one of characters given by an array.<br>
   * @param content String, the string to check
   * @param characters char[], an array of characters
   * @return boolean, if the content contains one of character in parameter characters.
   */
  public static boolean contains(String content, char[] characters){
	  if(content==null || characters==null) return false;
	  for(char c:characters){
		  if(content.contains(String.valueOf(c))) return true;
	  }
	  return false;
  }

  /**
   * Convert an array to a string, which contains the array's value separated by a space.<br>
   * @param values Object[], the array to convert
   * @return String, the array's value separated by a space.
   * @see Arrays#toString(Object[])
   */
  public static String arrayToString(Object[] values){
	  return arrayToString(values, null);
  }

  /**
   * Convert an array to a string, which contains the array's value separated by parameter delimiter.<br>
   * <pre>
   * For example:
   * String[] numbers = {"one", "two", "three"};
   * System.out.println(arrayToString(numbers, "$"));//one$two$three
   * System.out.println(arrayToString(numbers, "^"));//one^two^three
   * </pre>
   * @param values Object[], the array to convert
   * @param delimiter String, used to separate each value from the array in the final string.
   * @return String, the array's value separated by parameter delimiter
   */
  public static String arrayToString(Object[] values, String delimiter){
	  if (values == null) return null;
	  StringBuffer buffer = new StringBuffer();
	  String del = (delimiter != null) ? delimiter : " ";
	  for(int i=0; i < values.length; i++){
		  if(i > 0) buffer.append(del);
		  buffer.append(String.valueOf(values[i]));
	  }
	  return buffer.toString();
  }


  /** <br><em>Purpose:</em> convertNum: convert into a number
   * <br><em>Assumptions:</em>  all exceptions are handled.
   * @param                     numStr, String
   * (indexed from 1, 1 will be subtracted from the number before returned)
   * @return                    Integer if successful, null otherwise (if alpha chars instead
   * of digits are encountered; or if number is less than one)
   **/
  public static Integer convertNum (String num) {
    try {
      num = num.trim();
      if (num.equals("")) num = "1"; // assumption
      IndependantLog.debug("num: "+num);
      Integer ii = new Integer(num);
      int n = ii.intValue();
      if (--n < 0) {
        IndependantLog.debug("bad number, illegal index, they begin from 1: "+num);
        return null;
      }
      IndependantLog.debug("convertNum: "+n);
      return new Integer(n);
    } catch (NumberFormatException ee) {
      IndependantLog.debug("got bad num: "+num+", ee: "+ee);
      return null;
    }
  }

  /**
   * Convert s String float number (such as 35%, 0.68, 23, 5.4 etc.) into float.
   * @param floatNumber String, the String float number need to be converted
   * @return float, the converted float number.
   * <br>
   * NumberFormatException will be thrown out if the parameter is not valid.
   */
  public static float parseFloat(String floatNumber) {
	  if(!isValid(floatNumber)){
		  throw new NumberFormatException("The parameter '"+floatNumber+"' is not valid!");
	  }
	  try {
		  floatNumber = floatNumber.trim();
		  // If it is number format, convert it directly.
		  return Float.parseFloat(floatNumber);
	  } catch(NumberFormatException nfe) {
		  int index = floatNumber.indexOf(StringUtils.PERCENTAGE);

		  //Check that the percentage symbol % is the last character
		  if(index==(floatNumber.length()-1)) {
			  return 0.01F * Float.parseFloat(floatNumber.substring(0, index));
		  } else {
			  IndependantLog.warn("Cannot parse '" + floatNumber+"', whose format might be invalid.");
			  throw nfe;
		  }
	  }
  }

  /**
   * Extract coordinate String pair, String[], from coordinate string formats:
   * <ul>
   * <li>"x;y"
   * <li>"x,y"
   * <li>"x y"
   * <li>"Coords=x;y"
   * <li>"Coords=x,y"
   * <li>"Coords=x y"
   * </ul>
   *
   * @param   coords String, x;y or x,y or Coords=x;y  or Coords=x,y
   * @return  String[], String pair if successful, null otherwise
   * @deprecated call {@link #convertCoordsToArray(String, int)}, which is more generic.
   */
  public static String[] extractCoordStringPair(String coords) {
	// CANAGL OCT 21, 2005 This function previously did NOT support the
  	// "Coords=" prefix and used to decrement 1 for all provided values.
  	// It also did not accept coords of x or y < 0.  And it allowed the
  	// y value to be left off.
  	// The routine has been modified to leave the provided values "as-is"
  	// and to support the "Coords=" prefix.  The y value

	//SCNTAX NOV 12, 2015 Moved from the original 'convertCoords(String)' method.
	// 					  Keep the comments here for reference.
	try {
		String ncoords = new String(coords);

		// Strip string from beginning to '=' mark.
		int coordsindex = coords.indexOf(EQUAL);
		if(coordsindex > 0) ncoords = ncoords.substring(coordsindex+1);
		ncoords=ncoords.trim();
		IndependantLog.info("working with coords: "+ coords +" prefix stripped to: "+ncoords);

		// Extract parameter by delimiter ';' or ','.
		int sindex = ncoords.indexOf(";");
		if (sindex < 0) sindex = ncoords.indexOf(",");
		boolean isspace = false;
		if(sindex < 0){
			sindex = ncoords.indexOf(" ");
			isspace = (sindex > 0);
		}
		if (sindex < 0){
    		IndependantLog.debug("invalid coords: "+ ncoords +"; no separator detected.");
			return null;
		}

		// properly handles case where coordsindex = -1 (not found)
	    String xS = null;
		String yS = null;
		if(isspace){
    		IndependantLog.info("converting space-delimited coords: "+ ncoords);
			StringTokenizer toker = new StringTokenizer(ncoords, " ");
			if(toker.countTokens() < 2) {
        		IndependantLog.debug("invalid space-delimited coords: "+ ncoords);
				return null;
			}
			xS = toker.nextToken();
			yS = toker.nextToken();
		}else{
			xS = ncoords.substring(0, sindex).trim();
			yS = ncoords.substring(sindex+1).trim();
		}
		if ((xS.length()==0)||(yS.length()==0)){
    		IndependantLog.debug("invalid coordinate substrings  "+ xS +","+ yS);
			return null; // assumption
		}

		IndependantLog.info("x: "+xS);
		IndependantLog.info("y: "+yS);

		return new String[]{xS, yS};
	} catch(Exception ee) {
  		IndependantLog.debug( "bad coords format: "+ coords, ee);
  		return null;
	}
  }

  /** '5', the default x coordinate in pixel.*/
  public static final int DEFAULT_X_COORDINATE = 5;
  /** '5', the default y coordinate in pixel.*/
  public static final int DEFAULT_Y_COORDINATE = 5;

  /**
   * Convert the coordinate (given by array of String) into java.awt.Point format.<br>
   * <p>
   * The 'coordinate' could be provided in 2 formats:<br>
   * It could be <b>number, such as [12, 15], [5, 10]</b>. With this format
   * the second parameter 'compRect' is not needed (could be provided as null).<br>
   * Or it could be in <b>percentage format, such as [30%, 45%], [0.25, 0.8]</b>. With this
   * format, the second parameter 'compRect' SHOULD be provided, and the width and
   * height of component rectangle are necessary.<br>
   *
   * @param coordsPair String[], the 2 dimension array representing the [x,y] coordinate.<br>
   * @param compRect Rectangle, it represents the component relative to which the coordinate will be calculated.<br>
   *                            Only the width and height will be counted, the rectangle x,y position will not be used.<br>
   * @return java.awt.Point, the converted coordinate relative to the component
   */
  public static java.awt.Point convertCoords(String[] coordsPair, Rectangle compRect) {
	  String debugmsg = StringUtils.debugmsg(false);

	  if(coordsPair == null || coordsPair.length != 2) {
		  IndependantLog.error(debugmsg + "bad coords format: "+ Arrays.toString(coordsPair));
		  return null;
	  }

	  try {
		  IndependantLog.debug(debugmsg+" converting coordinate "+Arrays.toString(coordsPair)+" to Point, component rectangle is "+compRect);

		  int x = DEFAULT_X_COORDINATE;
		  int y = DEFAULT_Y_COORDINATE;

		  // Convert the coordinate parameter into float
		  float xF = parseFloat(coordsPair[0]);
		  float yF = parseFloat(coordsPair[1]);
		  //If the compRect is not null, we might provide a percentage for the coordinate
		  if(compRect!=null){
			  // If the coordinate value is between zero to one, it will be treated as percentage format;
			  // otherwise, the casting int format of coordinate value will be used directly.
			  x = (0<xF && xF<1) ? (int) (xF * compRect.getSize().width) : (int) xF;
			  y = (0<yF && yF<1) ? (int) (yF * compRect.getSize().height) : (int) yF;
		  }else{
			  x = (int) xF;
			  y = (int) yF;
		  }

		  IndependantLog.debug(debugmsg+"converted coords: x: " + x + ", y: " + y);
		  if(x < 0 || y < 0) IndependantLog.warn(debugmsg+"Coordinate contains negative value!");
		  return new java.awt.Point(x, y);
	  } catch (Exception e) {
		  IndependantLog.error(debugmsg+ "bad coords format: "+ Arrays.toString(coordsPair) + ", " + e);
		  return null;
	  }
  }

  /**
   * Convert the coordinate (given by array of String) into java.awt.Point format.<br>
   * @param coordsPair String[], a 2 dimension array containing [x,y] to be converted.
   * @return java.awt.Point coordinate if successful, null otherwise
   */
  public static java.awt.Point convertCoords(String[] coordsPair) {
	  return convertCoords(coordsPair, null);
  }

    /**
     * Convert coordinates string formats:
     * <ul>
     * <li>"x;y"
	 * <li>"x,y"
	 * <li>"x y"
	 * <li>"Coords=x;y"
	 * <li>"Coords=x,y"
	 * <li>"Coords=x y"
	 * </ul>
	 * into a java.awt.Point object.
	 * <p>
     * Subclasses may override to convert alternative values, such
     * as Row and Column values as is done in org.safs.rational.CFTable
     *
     * @param   coords String, x;y or x,y or Coords=x;y  or Coords=x,y
     * @return  java.awt.Point if successful, null otherwise
     * @author CANAGL OCT 21, 2005 modified to work as required for keywords as documented.
     * @author CANAGL MAR 23, 2010 added space delimiter support
     * @author SCNTAX NOV 12, 2015 This method has been split into {@link #extractCoordStringPair(String)} and {@link #convertCoords(String[], Rectangle)} methods.
     * @author SBJLWA NOV 24, 2015 Call {@link #convertCoordsToArray(String, int)} instead of {@link #extractCoordStringPair(String)}.
     *
     * @see #extractCoordStringPair(String)
     * @see #convertCoords(String[])
     * @see #convertCoordsToArray(String, int)
     **/
    public static java.awt.Point convertCoords(String coords) {
    	return convertCoords(convertCoordsToArray(coords, 2));
    }

    /**
     * Convert 2-point Line coordinates string of the formats:
     * <ul>
     * <li>"x1;y1;x2;y2"
	 * <li>"x1,y1,x2,y2"
	 * <li>"x1 y1 x2 y2"
	 * <li>"Coords=x1;y1;x2;y2"
	 * <li>"Coords=x1,y1,x2,y2"
	 * <li>"Coords=x1 y1 x2 y2"
	 * </ul>
	 * into a java.awt.Polygon object.
     *
     * @param   coords String, x1;y1;x2;y2 or x1,y1,x2,y2 or Coords=x1;y1;x2;y2  or Coords=x1,y1,x2,y2
     * @return  Polygon if successful, null otherwise
     **/
    public static java.awt.Polygon convertLine(String coords) {
	    try {
	    	String[] coordsArray = convertCoordsToArray(coords, 4);

	    	int x1 = (int) Float.parseFloat(coordsArray[0]);
      		int y1 = (int) Float.parseFloat(coordsArray[1]);
      		int x2 = (int) Float.parseFloat(coordsArray[2]);
      		int y2 = (int) Float.parseFloat(coordsArray[3]);

      		IndependantLog.debug("converted points: x1: "+x1+", y1: "+y1 +", x2:"+ x2 +", y2:"+ y2);
      		Polygon poly = new Polygon();
      		poly.addPoint(x1, y1);
      		poly.addPoint(x2, y2);

        	return poly;
	    } catch (Exception ee) {
      		IndependantLog.debug( "bad points format: "+ coords, ee);
      		return null;
    	}
    }

    /**
     * Convert coordinates string of the formats:
     * <ul>
     * <li>"x1;y1;x2;y2"
	 * <li>"x1,y1,x2,y2"
	 * <li>"x1 y1 x2 y2"
	 * <li>"x1;y1;x2;y2;x3;y3;x4;y4"
	 * <li>"Coords=x1;y1;x2;y2"
	 * <li>"Coords=x1,y1,x2,y2"
	 * <li>"Coords=x1 y1 x2 y2"
	 * <li>"Coords=x1 y1 x2 y2 x3 y3 x4 y4"
	 * </ul>
	 * into an array object.
     *
     * @param coords String, x1;y1;x2;y2 or x1,y1,x2,y2 or Coords=x1;y1;x2;y2  or Coords=x1,y1,x2,y2
     * @param length int, the number of token contained in the first parameter coords.
     * @return  String[] an array of coordinates if successful, null otherwise.
     **/
    public static String[] convertCoordsToArray(String coords, int length) {
    	String debugmsg = debugmsg(false);

    	try {
    		String[] coordsArray = new String[length];

    		String ncoords = new String(coords.trim());
    		int coordsindex = coords.indexOf(EQUAL);
    		if(coordsindex > 0) ncoords = ncoords.substring(coordsindex+1);
    		ncoords=ncoords.trim();
    		IndependantLog.info(debugmsg+"working with coods: "+ coords +" prefix stripped to: "+ncoords);

    		String sep = parseSeparator(ncoords);
    		if (sep == null){
    			IndependantLog.error(debugmsg+"invalid coods: "+ ncoords +".");
    			return null;
    		}

    		IndependantLog.info(debugmsg+"converting coods: "+ ncoords);
    		StringTokenizer toker = new StringTokenizer(ncoords, sep);
    		if(toker.countTokens() < length) {
    			IndependantLog.error(debugmsg+"invalid coods present: "+ ncoords);
    			return null;
    		}
    		//Put the token into the array
    		for(int i=0;i<length;i++) coordsArray[i] = toker.nextToken().trim();

    		String coord = null;
    		for(int i=0; i<coordsArray.length; i++){
    			coord = coordsArray[i];
    			if(coord==null || coord.isEmpty()){
    				IndependantLog.error(debugmsg+"invalid coods substrings  "+ Arrays.toString(coordsArray));
    				return null;
    			}
    			IndependantLog.info(debugmsg+"coord "+i+": "+coord);
    		}

    		return coordsArray;
    	} catch (Exception ee) {
    		IndependantLog.debug(debugmsg+"bad coods format: "+ coords, ee);
    		return null;
    	}
    }

    /**
     * If the params contain the originalSeparator, then replace it by a different separator.<br>
     *
     * @param originalSeparator String, the original separator to search in parameters
     * @param params String..., an array of parameter
     * @return String[], an array of parameter with new separator
     */
	public static String[] replaceSeparator(String originalSeparator, String... params){
		if(params!=null && params.length>0){
			String separator = StringUtils.generatePositionSepeartor(originalSeparator);
			String[] result = new String[params.length];
			String param = null;
			for(int i=0;i<params.length;i++){
				param = params[i];
				if(param!=null && param.contains(originalSeparator)){
					result[i] = param.replaceAll(originalSeparator, separator);
				}else{
					result[i] = param;
				}
			}
			return result;
		}
		return params;
	}

    /**
     * Generate a separator different from 'step separator', and it can be used to separate<br>
     * the coordinates for the position parameter in a test record.<br>
     * @param stepSeparator String, the separator in the step test record.
     * @return String, a separator different from 'step separator'
     */
    public static String generatePositionSepeartor(String stepSeparator){
    	if(stepSeparator==null) return ",";
    	if(!stepSeparator.equals(",")) return ",";
    	else return ";";//stepSeparator is comma, then we return semi-colon
    }

    /**
     * Parse a string to get the separator.
     * <ul>
     * <li>"x;y;width;height;status"
     * <li>"x,y,width,height,status"
     * <li>"x y width height status"
     * <li>"x1;y1;x2;y2"
     * <li>"x1,y1,x2,y2"
     * <li>"x1 y1 x2 y2"
     * </ul>
     * @param input String, the string to parse
     * @return String, the separator
     */
    public static String parseSeparator(String input){
		String sep = null;

		if(input==null){
			IndependantLog.error("input string is null");
			return null;
		}

		int sindex = input.indexOf(";");
		if (sindex < 0) {
			sindex = input.indexOf(",");
			if(sindex < 0){
				sindex = input.indexOf(" ");
				if(sindex >= 0) sep = " ";
			}else{
				sep = ",";
			}
		}else{
			sep = ";";
		}

		if (sep==null){
			IndependantLog.error("no separator detected for input string '"+input+"'");
		}

		return sep;
    }

    public static String debugmsg(Throwable e){
    	return (e==null? "":e.getClass().getSimpleName()+":"+e.getMessage());
    }

    /**
     * Generate debug message with class name and method name.<br>
     * @param clazz	Class, the class object of the instance, which is caller of the method.
     * @param methodName String, the string name of the method.
     * @return	String, debug massage, for example "org.safs.IndependantLog.debug(): "
     */
    public static String debugmsg(Class<?> clazz, String methodName){
    	return (clazz==null?"":clazz.getSimpleName())+
    		   (methodName==null? ": ":"."+methodName+"(): ");
    }

    /**
     * Generate debug message with class name, method name and message.<br>
     * @param clazz	Class, the class object of the instance, which is caller of the method.
     * @param methodName String, the string name of the method.
     * @param message String, the detail message for the debug IndependantLog.
     * @return	String, debug massage, for example "org.safs.IndependantLog.debug(): Something is wrong."
     */
    public static String debugmsg(Class<?> clazz, String methodName, String message){
    	return (clazz==null?"":clazz.getSimpleName())+
    		   (methodName==null? ": ":"."+methodName+"(): ")+
    		   (message==null? "":message);
    }

    /**
     * Generate debug message with class name, method name and Throwable object.<br>
     * @param clazz	Class, the class object of the instance, which is caller of the method.
     * @param methodName String, the string name of the method.
     * @param e	Throwable, the Throwable object rose when calling method.
     * @return	String, debug massage, for example "org.safs.IndependantLog.debug(): SomeException:Exception message."
     */
    public static String debugmsg(Class<?> clazz, String methodName, Throwable e){
    	return (clazz==null?"":clazz.getSimpleName())+
    		   (methodName==null? ": ":"."+methodName+"(): ")+
    		   (e==null? "":e.getClass().getSimpleName()+":"+e.getMessage());
    }
    /**
     * Generate debug message with class name, method name, detail message and Throwable object.<br>
     * @param clazz	Class, the class object of the instance, which is caller of the method.
     * @param methodName String, the string name of the method.
     * @param message String, the detail message for the debug IndependantLog.
     * @param e	Throwable, the Throwable object rose when calling method.
     * @return	String, debug massage, for example "org.safs.IndependantLog.debug(): Something is wrong:SomeException:Exception message."
     */
    public static String debugmsg(Class<?> clazz, String methodName, String message, Throwable e){
    	return (clazz==null?"":clazz.getSimpleName())+
    			(methodName==null? ": ":"."+methodName+"(): ")+
    			(message==null? "":message)+
    			(e==null? "":":"+e.getClass().getSimpleName()+":"+e.getMessage());
    }

    /**
     * @param fullQualified boolean, true if the returned method name should be full-qualified.
     * @return String, "my method name"+"(): "
     * @see #getMethodName(int, boolean)
     */
    public static String debugmsg(boolean fullQualified){
    	return getMethodName(1, fullQualified)+"(): ";
    }
    /**
     * @param fullQualified boolean, true if the returned method name should be full qualified.
     * @return String, my method name
     * @see #getMethodName(int, boolean)
     */
    public static String getCurrentMethodName(boolean fullQualified){
    	return getMethodName(1, fullQualified);
    }
    /**
     * @param fullQualified boolean, true if the returned method name should be full qualified.
     * @return String, my caller's method name
     * @see #getMethodName(int, boolean)
     */
    public static String getCallerName(boolean fullQualified){
    	return getMethodName(2, fullQualified);
    }
    /**
     * @param fullQualified boolean, true if the returned class name should be full qualified.
     * @return String, my caller's class name
     * @see #getClassName(int, boolean)
     */
    public static String getCallerClassName(boolean fullQualified){
    	return getClassName(2, fullQualified);
    }
    /**
     * Get my caller's id, which is a string containing 'full-qualified-callername'+'current-thread-id',
     * @param fullQualified boolean, true if the returned method name should be full qualified.
     * @return String, my caller's id
     * @see #getMethodName(int, boolean)
     */
    public static String getCallerID(boolean fullQualified){
    	StringBuffer id = new StringBuffer();
    	id.append(getMethodName(2, fullQualified));
    	id.append(Thread.currentThread().getId());
    	return id.toString();
    }

    /**
     * Get the method name, or its caller's name, or its caller's caller's name etc.<br>
     * this depends on the parameter 'level'.
     * level=0, the method calling 'org.safs.StringUtils.getMethodName'
     * level=1, the method calling the method at level 0
     * level=2, the method calling the method at level 1
     * ...
     *
     * @param level	int, the level in the StackTrace.
     * @param fullQualified boolean, true if the returned name should be full qualified.
     * @return String, the method name.
     * @see #getStackTraceElement(int)
     */
    public static String getMethodName(int level, boolean fullQualified){
    	StringBuffer callerName = new StringBuffer();
    	try{
    		String fullClassName = null;
    		int lastPointIndex = -1;
    		StackTraceElement trace = getStackTraceElement(level+1);//+1 to skip current method 'getMethodName' in stack
    		fullClassName = trace.getClassName();
    		if(fullQualified){
    			callerName.append(fullClassName).append(".").append(trace.getMethodName());
    		}else{
    			lastPointIndex = fullClassName.lastIndexOf(".")+1;
    			callerName.append(fullClassName.substring(lastPointIndex)).append(".").append(trace.getMethodName());
    		}
    	}catch(Exception e){
    		IndependantLog.error("Fail to get MethodName infomation, Met Exception ", e);
    	}
		return callerName.toString();
    }

    /**
     * Get the class name, or class-name of its caller, or class-name of its caller's caller etc.<br>
     * this depends on the parameter 'level'.
     * level=0, the class name in which a method calling the method 'org.safs.StringUtils.getClassName'
     * level=1, the class name in which a method calling the method at level 0
     * level=2, the class name in which a method calling the method at level 1
     * ...
     *
     * @param level	int, the level in the StackTrace.
     * @param fullQualified boolean, true if the returned name should be full qualified.
     * @return String, the class name.
     * @see #getStackTraceElement(int)
     */
    public static String getClassName(int level, boolean fullQualified){
    	StringBuffer callerName = new StringBuffer();
    	try{
    		String fullClassName = null;
    		int lastPointIndex = -1;
    		StackTraceElement trace = getStackTraceElement(level+1);//+1 to skip current method 'getClassName' in stack
    		fullClassName = trace.getClassName();
    		if(fullQualified){
    			callerName.append(fullClassName);
    		}else{
    			lastPointIndex = fullClassName.lastIndexOf(".")+1;
    			callerName.append(fullClassName.substring(lastPointIndex));
    		}
    	}catch(Exception e){
    		IndependantLog.error("Fail to get ClassName infomation, Met Exception ", e);
    	}
    	return callerName.toString();
    }

    /**
     * <pre>
     * To get the current method information or its caller or its ancester dynamically,
     * we can refer to the StackTrace, which is a stack recording the methods' invocation trace.
     * The current thread trace of this method is as following:
     * ...
     * java.lang.Thread.getStackTrace
     * org.safs.StringUtils.getStackTraceElement
     * level=0, Here is the method, who calls StringUtils.getStackTraceElement()
     * level=1, Here is the direct caller of method at level 0
     * level=2, Here is the direct caller of method at level 1
     * ...
     *
     * We can supply the parameter 'level' to this method, and we can get the appropriate StackTraceElement.
     *
     * </pre>
     *
     * @param level	int, the level in the StackTrace under 'org.safs.StringUtils.getStackTraceElement'
     * @return StackTraceElement, it conatins the information of method, class, file etc.
     */
    public static StackTraceElement getStackTraceElement(int level){

    	try{
    		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
    		StackTraceElement trace = null;
    		String getStackTraceMethodName = "getStackTrace";
    		//Remember change the value of field 'myMethodName', if this method name changes
    		String myMethodName = "getStackTraceElement";


    		for(int i=0;i<traces.length;i++){
    			trace = traces[i];
    			//java.lang.Thread.getStackTrace
    			if(trace.getClassName().equals(Thread.class.getName())
    					&& trace.getMethodName().equals(getStackTraceMethodName)){
    				if(++i<traces.length){
    					trace = traces[i];
    					//org.safs.StringUtils.getStackTraceElement
    					if(trace.getClassName().equals(StringUtils.class.getName())
    							&& trace.getMethodName().equals(myMethodName)){
    						//under 'org.safs.StringUtils.getStackTraceElement', the StackTraceElement is what we want
    						//according to the level, we will get the StackTraceElement
    						i = i+1+level;
    						if(i<traces.length){
    							trace = traces[i];
    							return trace;
    						}else{
    							IndependantLog.warn("Stack Trace doesn't contain enough infomation.");
    						}
    					}else{
    						IndependantLog.warn("StackTraceElement at '"+i+"' is not '"+StringUtils.class.getName()+"."+myMethodName+"'");
    					}
    				}else{
    					IndependantLog.warn("Stack Trace doesn't contain enough infomation.");
    				}
    				break;
    			}
    		}

    	}catch(Exception e){
    		IndependantLog.error("Fail to get StackTraceElement infomation, Met Exception ", e);
    	}

    	return null;
    }

    /**
     * @param content String, multiple lines, delimited by "\n"
     * @return String, the first line of content
     */
    public static String getFirstLine(String content){
    	try{
    		int indexofNewLine = content.indexOf(NEW_LINE);
    		if(indexofNewLine!=-1)	return content.substring(0, indexofNewLine);
    	}catch(Exception e){}
    	return content;
    }

    /**
     * Reverse an array.
     * @param array T[], the array to reverse.
     * @return boolean, true if the reverse has been done successfully.
     */
	public static <T> boolean reverseArray(T[] array){
		String debugmsg = debugmsg(true);
		try{
			if(array!=null){
				int mid = array.length>>1;
    			T temp = null;
    			for(int i=0, j=array.length-1; i<mid; i++, j--){
    				temp = array[i];
    				array[i] = array[j];
    				array[j] = temp;
    			}
				return true;
			}else{
				IndependantLog.warn(debugmsg+" the array is null, cannot reverse.");
				return false;
			}

		}catch(Exception e){
			IndependantLog.error(debugmsg+" Met Exception ", e);
			return false;
		}
	}

	/**
	 * Decide what is the best separator to use for a record from a possible separator list:
	 * {@link TestRecordData#POSSIBLE_SEPARATOR}.<br>
	 * @param expression String, to examine to find which of the separators does NOT exist inside
	 * the expression.
	 * @return a single String sep, or null if the expression contains a character of every
	 * possible separator.
	 */
	public static String deduceUnusedSeparatorString(String expression){
		if(expression!=null){
			for(String sep: TestRecordData.POSSIBLE_SEPARATOR){
				if(expression.indexOf(sep)< 0) return sep;
			}
		}
		IndependantLog.warn(StringUtils.debugmsg(false)+" cannot deduce a valid separator for expression: "+expression);
		return null;
	}

	/**
	 * Get a separator NOT appear in command neither in params; if no such separator<br>
	 * can be found, then the default separator will be return.<br>
	 * @param sep String, the default separator
	 * @param command String, the command
	 * @param params String[], the parameters
	 * @return String, an appropriate separator
	 */
	public static String getUniqueSep(String sep, String command, String... params){
		StringBuffer sb = new StringBuffer();
		if(params instanceof String[]&& params.length > 0) {
			for(String ap:params) sb.append(ap+" ");
		}
		String check = command.concat(sb.toString());
		return check.contains(sep) ? deduceUnusedSeparatorString(check): sep;
	}

	/**
	 * Remove the leading and ending double quote.<br>
	 * @param expression
	 * @return String
	 * @see #processExpression(String)
	 * @see #processExpression(String, String)
	 */
	public static String removeWrappingDoubleQuotes(String expression){
		if(expression==null) return null;
//		if(expression.length()>1
//		   && expression.charAt(0)==StringUtils.CHAR_DOUBLE_QUOTE
//		   && expression.charAt(expression.length()-1)==StringUtils.CHAR_DOUBLE_QUOTE ){
//			expression = expression.substring(1);
//			expression = expression.substring(0,expression.length()-1);
//		}
		if(expression.charAt(0)==StringUtils.CHAR_DOUBLE_QUOTE) expression = expression.substring(1);
		if(expression.length()>0 && expression.charAt(expression.length()-1)==StringUtils.CHAR_DOUBLE_QUOTE ){
			expression = expression.substring(0,expression.length()-1);
		}
		return expression;
	}
	/**
	 * Add double-quote around a string value. For "combine-word", the result is "\"combine-word\"";<br>
	 * @param parameter String, the string to be double-quoted.
	 * @return String
	 */
	public static String quote(String parameter){
		if(parameter==null) return null;
		return "\"" + parameter +"\"";
	}

	/**
	 * Test if the value is double-quoted or not.
	 * @param value String, the string value to be tested.
	 * @return boolean, true if the value is double-quoted.
	 */
	public static boolean isQuoted(String value){
		if(value==null || value.trim().isEmpty()) return false;
		String trimmedValue = value.trim();
		if(trimmedValue.startsWith(DOUBLE_QUOTE) && trimmedValue.endsWith(DOUBLE_QUOTE)) return true;
		return false;
	}

	/**
	 * Normalize potential line breaks:
	 * <pre>
	 *   {@link StringUtils#CRLF},
	 *   {@link StringUtils#CARRIAGE_RETURN},
	 * </pre>
	 * into a single uniform {@link StringUtils#NEW_LINE} linebreak.
	 * @param source
	 * @return String with all linebreaks normalized to {@link StringUtils#NEW_LINE}.
	 */
	public static String normalizeLineBreaks(String source){
		if(source != null){
			if(source.contains(CRLF)) source = source.replace(CRLF, NEW_LINE);
			if(source.contains(CARRIAGE_RETURN)) source = source.replace(CARRIAGE_RETURN, NEW_LINE);
		}
		return source;
	}

	/**
	 * This class is used to parse the test error message.
	 */
	public static class ErrorLineParser{
		private static final String PATTERN_LINENUMBER = "[0-9]+";//a digital number
		private static final String PATTERN_CLASS_NAME = "[a-zA-Z_0-9\\.]+";//a.class.Name
		private static final String PATTERN_METHOD_NAME = "\\w+\\(\\)";//a_methodName()
		/**
		 * This pattern contains 2 groups, the first is the class-name, the second is the method-name.
		 */
		private static final String PATTERN_FILE = "("+PATTERN_CLASS_NAME+")"+StringUtils.CHAR_NUMBER+"?("+PATTERN_METHOD_NAME+")?";//sample.testcases.TestCase1#main()

		//TODO
		//1. For other Locale, we need to modify the pattern
		//2. Maybe we need other pattern to match more
		/**
		 * "Error at line "+ {@link #PATTERN_LINENUMBER} +" in file "+ {@link #PATTERN_FILE} : error reason.<br>
		 *  It contains 4 groups, 1->linenumber, 2->filename, 3->methodname, and 4->error<br>
		 */
		private static final Pattern TEST_ERROR_PATTERN1 = Pattern.compile("Error at line ("+PATTERN_LINENUMBER+") in file "+PATTERN_FILE+" ?:? ?(.*)");
		/**
		 * "in filename "+ {@link #PATTERN_FILE} +" at line "+ {@link #PATTERN_LINENUMBER}
		 *  It contains 3 groups, 1->filename, 2->methodname, and 3->linenumber<br>
		 */
		private static final Pattern TEST_ERROR_PATTERN2 = Pattern.compile("in filename "+PATTERN_FILE+" at line ("+PATTERN_LINENUMBER+")");
		/**
		 * "in table " + {@link #PATTERN_FILE} + " at line "+ {@link #PATTERN_LINENUMBER}
		 *  It contains 3 groups, 1->filename, 2->methodname, and 3->linenumber<br>
		 */
		private static final Pattern TEST_ERROR_PATTERN3 = Pattern.compile("in table "+PATTERN_FILE+" at line ("+PATTERN_LINENUMBER+")");
		/**
		 * "Unable to perform "+ saction +" on "+ scomp +" in "+ {@link #PATTERN_FILE} +" line "+ {@link #PATTERN_LINENUMBER}
		 *  It contains 3 groups, 1->filename, 2->methodname, and 3->linenumber<br>
		 */
		private static final Pattern TEST_ERROR_PATTERN4 = Pattern.compile("Unable to perform \\w+ on \\w+ in "+PATTERN_FILE+" line ("+PATTERN_LINENUMBER+")");

		/**
		 * According to a line of console string, generate the ErrorTrace.<br>
		 * If the "console string" matches some patterns, a ErrorTrace will be generated.<br>
		 * @param offset int, the start position of the parameter aLine on the console.
		 * @param aLine String, a line of console string to be parsed.
		 * @return ErrorTrace, it the parameter aLine doesn't match any regex pattern, then null is returned.
		 */
		public static ErrorLinkTrace parse(int offset, String aLine){
			if(aLine==null || aLine.isEmpty()) return null;
			ErrorLinkTrace errorTrace = null;
			String fileName = null;
			String methodName = null;
			String lineNumber = null;
			String error = null;
			boolean matched = false;

			try{
				Matcher m = TEST_ERROR_PATTERN1.matcher(aLine);
				//if we call find() on the same matcher for several times, only the first time it will give the correct;
				//the rest call will always give false, it is better to store it in a local variable
				matched = m.find();
				if(matched) {
					lineNumber = m.group(1);
					fileName = m.group(2);
					methodName = m.group(3);
					error = m.group(4);
					for(int i=0;i<=m.groupCount();i++) IndependantLog.debug(i+"-->  "+m.group(i));
					offset += m.start(2);//the link start position is the beginning of the fileName
				}
				if(!matched){
					m = TEST_ERROR_PATTERN2.matcher(aLine);
					matched = m.find();
					if(!matched) {
						m = TEST_ERROR_PATTERN3.matcher(aLine);
						matched = m.find();
					}
					if(!matched) {
						m = TEST_ERROR_PATTERN4.matcher(aLine);
						matched = m.find();
					}
					if(matched) {
						fileName = m.group(1);
						methodName = m.group(2);
						lineNumber = m.group(3);
						for(int i=0;i<=m.groupCount();i++) IndependantLog.debug(i+"-->  "+m.group(i));
						offset += m.start(1);//the link start position is the beginning of the fileName
					}
				}

				//The link will be shown on fileName
				if(matched) errorTrace = new ErrorLinkTrace(fileName, methodName, lineNumber, error, offset, fileName.length());
			}catch(Exception ex) {
				IndependantLog.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(ex));
			}

			return errorTrace;
		}

	}
	/**
	 * This class encapsulate the test error message.
	 */
	public static class ErrorLinkTrace{
		/** the offset of the link on the console, meaning the start position*/
		private int linkOffset = 0;
		/** the length of the link*/
		private int linkLength = 0;

		/** filename, in which the error occur, such as regression.testcases.GenericMasterTests */
		private String fileName = null;
		/** methodName, in which the error occur, such as testLogin() */
		private String methodName = null;
		/** line, the linenumber where the error occur, such as 45 */
		private String line = null;
		/** error, the detail error message */
		private String error = null;

		public ErrorLinkTrace(String fileName, String methodName, String line, String error, int linkOffset, int linkLength) {
			this.fileName = fileName;
			this.methodName = methodName;
			this.line = line;
			this.error = error;
			this.linkOffset = linkOffset;
			this.linkLength = linkLength;
		}

		/** the offset of the link on the console, meaning the start position*/
		public int getLinkOffset() {
			return linkOffset;
		}
		/** the length of the link*/
		public int getLinkLength() {
			return linkLength;
		}
		/** filename, in which the error occur, such as regression.testcases.GenericMasterTests */
		public String getFileName() {
			return fileName;
		}
		/** methodName, in which the error occur, such as testLogin() */
		public String getMethodName() {
			return methodName;
		}
		/** line, the linenumber where the error occur, such as 45 */
		public String getLine() {
			return line;
		}
		/** error, the detail error message */
		public String getError() {
			return error;
		}
	}

	/**
	 * check if the next character in the string is the a certain character or not.
	 * @param i int, the current index
	 * @param string String, the string to check
	 * @param c Character, the expected character
	 * @param previous boolean, if true then the next index is (i-1); otherwise is (i+1)
	 * @return boolean, true if the next char is equal to the parameter c.
	 */
	private static boolean checkNextChar(int i, String string, Character c, boolean previous){
		if(previous){
			if((--i)>=0 && string.charAt(i)==c) return true;
		}else{
			if((++i)<string.length() && string.charAt(i)==c) return true;
		}

		return false;
	}

	/**
	 * get the xpart-string from stack and add it to list.
	 * @param xparts List<String>, the list of xpart
	 * @param xpartStack Stack<Character>, the stack of characters representing an xpart
	 */
	private static void popXpart(List<String> xparts, Stack<Character> xpartStack){
		if(xparts!=null && xpartStack!=null){
			StringBuffer sb = new StringBuffer();
			while(!xpartStack.isEmpty()){
				sb.append(xpartStack.pop());
			}
			if(sb.length()>0) xparts.add(sb.toString());
		}
	}

	/**
	 * Break the xpath into multiple xparts by slash and return these xparts as an array.<br>
	 * The slash between quote or double-quote will not be considered as separator of xpart.<br>
	 * @param xpath String, the xpath
	 * @param onlyLeaf boolean, if we only need to get the leaf
	 * @param rootToLeaf boolean, true if we want the list begins with root, ends with leaf; false otherwise.
	 * @return String[] the array containing xparts of an xpath
	 */
	public static String[] breakXpath(String xpath, boolean onlyLeaf, boolean rootToLeaf){
		List<String> xpartsList = new ArrayList<String>();
		if(xpath==null) return xpartsList.toArray(new String[0]);

		try{
			Stack<Character> quoteStack = new Stack<Character>();
			Stack<Character> xpartStack = new Stack<Character>();

			Character token = null;

			for(int i=xpath.length()-1;i>=0;i--){
				token = xpath.charAt(i);
				xpartStack.push(token);

				if(quoteStack.isEmpty()){
					//if meet slash /, then get the xpart
					if(token.charValue()==CHAR_SLASH){
						if(!checkNextChar(i, xpath, CHAR_BACK_SLASH, true)){
							xpartStack.pop();//remove the char slash '/'
							popXpart(xpartsList, xpartStack);
							//if we want only the leaf, the first match is leaf, we stop parsing
							if(onlyLeaf) break;
						}
						//else if there is a back-slash in front, then / will be not considered as xpath separator
						//continue;
					}
					//if meet quote ' or double quote ", then push to quoteStack
					else if(token.charValue()==CHAR_QUOTE || token.charValue()==CHAR_DOUBLE_QUOTE){
						if(!checkNextChar(i, xpath, CHAR_BACK_SLASH, true)) quoteStack.push(token);
						//else if there is a back-slash in front, then ' " will be not considered as a valid quote/double-quote
						//continue;
					}

				}else{
					//Try to find the paired quote/double-quote to empty the quoteStack
					if(quoteStack.peek().charValue()==token.charValue()){
						if(!checkNextChar(i, xpath, CHAR_BACK_SLASH, true)) quoteStack.pop();
					}else{
						continue;
					}
				}
			}
			//Finally flush out all left in the xpartStack
			if(!xpartStack.isEmpty()) popXpart(xpartsList, xpartStack);
		}catch(Exception e){
			IndependantLog.warn(debugmsg(false)+debugmsg(e));
		}

		//The array xparts contains xpart from leaf to root
		String[] xparts = xpartsList.toArray(new String[0]);
		if(rootToLeaf && !onlyLeaf && xparts.length>1){
			//If we want xpart from root to leaf, we need to reverse the array
			reverseArray(xparts);
		}

		return xparts;
	}

	/**
	 * Return the parent part of an xpath. If this xpath doesn't have a parent then "" or "//" etc. will be returned.
	 * @param xpath String, the xpath to parse
	 * @return String, the parent part of an xpath
	 */
	public static String getParentXpath(String xpath){
		String parent = xpath;
		String leaf = null;
		try{
			String[] xparts = breakXpath(xpath, true, false);
			if(xparts!=null && xparts.length>0){
				leaf = xparts[0];
				int length = leaf.length();
				//remove the leaf
				parent = parent.substring(0, (parent.length()-length));
				//remove the slash /
				if(parent.endsWith(SLASH)){
					parent = parent.substring(0, (parent.length()-1));
				}
			}
		}catch(Exception e){
			IndependantLog.error(debugmsg(false)+debugmsg(e));
		}

		return parent;
	}

	/**
	 * <pre>
	 * Get the value of a property from system properties and Set a value from the configuration to System-Properties.
	 * If the System-Properties contains the 'property',
	 *   keep the value in the System-Properties (don't override by the value from ConfigureInterface).
	 * Otherwise,
	 *   if the System-Properties does NOT contain the 'property', then get the value from the ConfigureInterface,
	 *   if the value is null or empty, assign the 'default value' if it is provided. Finally set this value to System-Properties.
	 * </pre>
	 *
	 * @param property String, The property name in System-Properties.
	 * @param config ConfigureInterface, containing the configuration initial parameters
	 * @param section String, The section name in the ConfigureInterface
	 * @param key String, The key name in the ConfigureInterface
	 * @param defaultValue String[], String[0] is the default value if no value can be got from system-properties and config.
	 * @return String, the value of the property.
	 */
	public static String getSystemProperty(String property, ConfigureInterface config, String section, String key, String... defaultValue){
		String debugmsg = StringUtils.debugmsg(false);
		String value = null;

		try{
			//First, try to get the value from the system properties
			IndependantLog.info("Checking for Command-Line setting '-D"+property+"'");
			value = System.getProperty(property);
			if(isValid(value)) return value;
		}catch(Exception e){
			IndependantLog.error(debugmsg+" fail to get value for property '"+property+"'. "+StringUtils.debugmsg(e));
		}

		//If not found, try to get the value from the configuration. And set it to system properties.
		try{
			if(!isValid(value)){
				IndependantLog.info("Checking for alternative configuration setting '"+section+"':'"+key+"'...");
				value = config.getNamedValue(section, key);
			}
		}catch(Exception e){
			IndependantLog.error(debugmsg+" fail to get configuration setting '"+section+"':'"+key+"'. "+StringUtils.debugmsg(e));
		}

		//If not found, assign the default value if the default value is provided.
		try{
			if(!isValid(value) && defaultValue!=null){
				if(defaultValue.length>0){
					IndependantLog.info("Use the default value '"+defaultValue[0]+"'. ");
					value = defaultValue[0];
				}
			}
		}catch(Exception e){
			IndependantLog.error(debugmsg+" fail to get defaul value. "+StringUtils.debugmsg(e));
		}
		//Finally if the value is valid, set it to the system properties
		if(isValid(value)){
			IndependantLog.debug(debugmsg+" Set value '"+value+"' to system property '"+property+"'.");
			System.setProperty(property, value);
		}

		return value;
	}

	/**
	 * @param value String, the string value to test
	 * @return boolean, true if the value is not null and is not empty.
	 */
	public static boolean isValid(String value){
		return (value!=null && !value.trim().isEmpty());
	}

	/**
	 * @param booleanString, the boolean string to parse.
	 * @return boolean, the boolean value, parsed result of the parameter 'booleanString'.
	 * @throws SAFSParamException if the parameter 'booleanString' is not a valid boolean string.
	 */
	public static boolean parseBoolean(String booleanString) throws SAFSParamException{
		boolean valid = (booleanString!=null && !booleanString.trim().isEmpty() &&
				 (
				   booleanString.equalsIgnoreCase(Boolean.TRUE.toString()) ||
				   booleanString.equalsIgnoreCase(Boolean.FALSE.toString())
				  )
				);
		if(valid){
			return Boolean.parseBoolean(booleanString);
		}else{
			throw new SAFSParamException("The parameter '"+booleanString+"' is not a valid boolean string.");
		}
	}

	/**
	 * Generate a unique name. For example, it can be used as a javascript variable name<br>
	 * when calling functions in JavaScriptFunctions to avoid the conflict with variables defined<br>
	 * in the Application Under Test.<br>
	 *
	 * @param prefix String, the prefix of the unique name
	 * @return String, the unique name
	 */
	public static String generateUniqueName(String prefix){
		StringBuffer uniqueName = new StringBuffer();
		if(prefix!=null && !prefix.trim().isEmpty()){
			uniqueName.append(prefix);
		}else{
			uniqueName.append("temp");
		}

		uniqueName.append(new Date().getTime());

		return uniqueName.toString();
	}

	/**
	 * Replace the value of an option in the whole JVM options.<br>
	 * @param jvmOptions String, the JVM options to modify
	 * @param option String, the JVM option to replace
	 * @param value String, the new value to use
	 * @return String, the options replaced by new value
	 */
	public static String replaceJVMOptionValue(String jvmOptions, String option, String value){
		StringBuffer sb = new StringBuffer();
		String debugmsg = StringUtils.debugmsg(false);

		try{
			int optionIndex = jvmOptions.indexOf(option);
			String optionSepearator = " ";
			if(optionIndex>-1){
				sb.append(jvmOptions.substring(0, optionIndex));
				int optionSepearatorIndex = jvmOptions.indexOf(optionSepearator, optionIndex);
				if(optionSepearatorIndex<0){
					optionSepearator = "\t";//try other separator
					optionSepearatorIndex = jvmOptions.indexOf(optionSepearator, optionIndex);
				}

				String oldvalue = null;
				if(optionSepearatorIndex<0){
					//we may need to try other separators
					IndependantLog.warn(debugmsg+" not found option separtor '"+optionSepearator+"' in jvmOptions '"+jvmOptions+"' from option '"+option+"'");
					//for now, we consider the rest as the old option value
					oldvalue = jvmOptions.substring(optionIndex+option.length());
					sb.append(" "+option+value);
				}else{
					//retrive the option value and replace it
					oldvalue = jvmOptions.substring(optionIndex+option.length(), optionSepearatorIndex);
					sb.append(" "+option+value);
					sb.append(" "+jvmOptions.substring(optionSepearatorIndex+optionSepearator.length()));
				}
				IndependantLog.debug(debugmsg+" replace the old value '"+oldvalue+"' by '"+value+"' for option '"+option+"'");

			}else{
				//Cannot find the option to replace, return the original one
				return jvmOptions;
			}

		}catch(Exception e){
			IndependantLog.error(debugmsg(false)+" Fail to replace due to "+debugmsg(e));
			return jvmOptions;
		}

		return sb.toString();
	}

	/**
	 * Get the object's memory address according to the method hashCode().<br>
	 * <b>Note:</b> The method hashCode() should not be overridden.
	 *
	 * @param object Object, the object to get memory address for.
	 * @return String, the memory address
	 */
	public static String getMemoryAddress(Object object){
		if(object==null) return null;
		return "@"+Integer.toHexString(object.hashCode());
	}

	/**
	 * @param object Object, the object to get string
	 * @return String, the object's string format with memory address.
	 */
	public static String toStringWithAddress(Object object){
		if(object==null) return null;
		return getMemoryAddress(object) +" : "+object;
	}

	/**
	 *
	 * @param stack Stack, a stack holding some objects.
	 * @return String, the string format of the stack,
	 * @see #toStringWithAddress(Object)
	 */
	public static String getStackInfo(Stack<?> stack){
		int size = stack.size();
		Object object = null;
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<size;i++){
			object = stack.elementAt(i);
			sb.append(toStringWithAddress(object)+"\n");
		}

		return sb.toString();
	}

	public static void test_replaceJVMOptionValue(){
		String jvmOptions = JavaConstant.JVM_Xms+"128m   "+JavaConstant.JVM_Xmx+"1g";

		debug(jvmOptions);
		String newoptions = replaceJVMOptionValue(jvmOptions, JavaConstant.JVM_Xms, "512m");
		debug("newoptions="+newoptions);
		newoptions = replaceJVMOptionValue(newoptions, JavaConstant.JVM_Xmx, "2g");
		debug("newoptions="+newoptions);
		debug("newoptions="+newoptions);
		debug("");

		jvmOptions = JavaConstant.JVM_Xms+"128m   "+JavaConstant.JVM_Xmx+"1g  ";
		debug(jvmOptions);
		newoptions = replaceJVMOptionValue(jvmOptions, JavaConstant.JVM_Xms, "512m");
		debug("newoptions="+newoptions);
		newoptions = replaceJVMOptionValue(newoptions, JavaConstant.JVM_Xmx, "2g");
		debug("newoptions="+newoptions);
		debug("");

		jvmOptions = JavaConstant.JVM_Xms+"128m\t"+JavaConstant.JVM_Xmx+"1g\t";
		debug(jvmOptions);
		newoptions = replaceJVMOptionValue(jvmOptions, JavaConstant.JVM_Xms, "512m");
		debug("newoptions="+newoptions);
		newoptions = replaceJVMOptionValue(newoptions, JavaConstant.JVM_Xmx, "2g");
		debug("newoptions="+newoptions);
		debug("");

		jvmOptions = JavaConstant.JVM_Xms+"128m\t"+JavaConstant.JVM_Xmx+"1g\t";
		debug(jvmOptions);
		newoptions = replaceJVMOptionValue(jvmOptions, JavaConstant.JVM_Xms, "512m");
		debug("newoptions="+newoptions);
		newoptions = replaceJVMOptionValue(newoptions, "-Xgc:", "singlecon");
		debug("newoptions="+newoptions);
		newoptions = replaceJVMOptionValue(newoptions, JavaConstant.JVM_Xmx, "2g");
		debug("newoptions="+newoptions);
		debug("");

		jvmOptions = "-Xgc:gencon" + JavaConstant.JVM_Xms+"128m\t"+JavaConstant.JVM_Xmx+"1g\t" +"-Xdebug";
		debug(jvmOptions);
		newoptions = replaceJVMOptionValue(jvmOptions, JavaConstant.JVM_Xms, "512m");
		debug("newoptions="+newoptions);
		newoptions = replaceJVMOptionValue(newoptions, JavaConstant.JVM_Xmx, "2g");
		debug("newoptions="+newoptions);
		debug("");

		jvmOptions = "-Xgc:gencon" + " -Xdebug";
		debug(jvmOptions);
		newoptions = replaceJVMOptionValue(jvmOptions, JavaConstant.JVM_Xms, "512m");
		debug("newoptions="+newoptions);
		newoptions = replaceJVMOptionValue(newoptions, JavaConstant.JVM_Xmx, "2g");
		debug("newoptions="+newoptions);
		newoptions = replaceJVMOptionValue(newoptions, "-Xgc:", "singlecon");
		debug("newoptions="+newoptions);
		debug("");

	}

	public static void test_breakXpath(){
		debug("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+debugmsg(false)+"$$$$$$$$$$$$$$$$$$$");
		String[] xpaths = {
				"/html/table/tr[3]/td[0]/a[@name='hello wang/sa']/",
				"/html/table/tr[3]/td[0]/a[@name='hello wang/sa']/../",
				"/html/table/tr[3]/td[0]/a[@name='hello wang/sa']",
				"/html/table/tr[3]/td[0]/a[@id=my\\/_heel]",
				"/html/table/tr[3]/td[0]/a[@name='hello \" wang/sa']",
				"//html/table/tr[3]/td[@id='td /1 \" \\' ']/a[@name='hello \" wang\\'s a']",
				".//html/table/tr[3]/td[@name='td 0']/a[@name='hello']",
				"",
				"//html",
				"html",
				null
		};

		String[] xparts = null;
		for(String xpath:xpaths){
			debug("");
			debug("XPATH="+xpath);
			xparts = breakXpath(xpath, false, false);
			debug("XPARTS ARRAY="+Arrays.toString(xparts));
		}
	}

	public static void test_getTokenList(){
		debug("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+debugmsg(false)+"$$$$$$$$$$$$$$$$$$$");
		String delimiter = "=";
		Character escape = CHAR_BACK_SLASH;
		String text = "=a=b=c=d=e=f=k=";

		debug("Delimiter='"+delimiter+"'; Escape='"+escape+"'");

		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "=a=b\\=c=d=e=f=k=";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "b\\=c=d=e=f=k=";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "\\=a=b=c=d=e=f=k=";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "=a=b=c=d=e=f=k\\=";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "=\\a=b=c=d=e=f=k\\=";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "=a=b=c=d=e=f=k=\\";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "=a=b=c=d=e=f=k\\";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "\\a=b=c=d=e=f=k=";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "=";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "css=input[jsaction*='sf.c']";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "css=input[jsaction*\\='sf.c']=aeee";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "css=input[jsaction*\\='sf.c']";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "css=input[jsaction*X='sf.c']=aeee";
		debug(text+ "		"+getTokenList(text, delimiter, escape));

		text = "css=input[jsaction*X='sf.c']";
		debug(text+ "		"+getTokenList(text, delimiter, escape));
	}

	public static void test_reverseArray(){
		debug("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+debugmsg(false)+"$$$$$$$$$$$$$$$$$$$");
		Integer[] arr = {1,2,3,4,5,6,7,8,9,0};
		System.out.println("Original "+Arrays.toString(arr));
		reverseArray(arr);
		System.out.println("Reversed "+Arrays.toString(arr));

		Float[] a = {3.0f, 3.6f, 8.9f};
		System.out.println("Original "+Arrays.toString(a));
		reverseArray(a);
		System.out.println("Reversed "+Arrays.toString(a));

		Character[] strArr = {'S', 't', 'r', 'i', 'n', 'g'};
		System.out.println("Original "+Arrays.toString(strArr));
		reverseArray(strArr);
		System.out.println("Reversed "+Arrays.toString(strArr));

		reverseArray(null);
	}

	public static void test_convertLine(){
		Point pointA = new Point(13, 45);
		Point pointB = new Point(18, 27);
		String coords = pointA.x+", "+pointA.y+", "+pointB.x+", "+pointB.y;
		java.awt.Polygon poly = convertLine(coords);

		if(pointA.equals(new Point(poly.xpoints[0], poly.ypoints[0])) && pointB.equals(new Point(poly.xpoints[1], poly.ypoints[1]))){
			System.out.println("Succeed convert "+coords+" to polygon "+poly);
		}else{
			System.err.println("Fail to convert "+coords+" to polygon. The polygon is "+poly);
		}
	}

	private static void testErrorLineParser(){
		debug("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+debugmsg(false)+"$$$$$$$$$$$$$$$$$$$");
		String[] testStrings = {"Error at line 50 in file sample.testcases.TestCase1#main(): Some error happened.",
				"Error at line 25 in file sample.testcases.TestCase1#: Nullpointer Exception.",
				"Error at line 15 in file sample.testcases.TestCase1: Missing parameters.",
				"Error at line 39 in file sample.testcases.TestCase1 Keyword not supported.",
				"Error at line 4 in file sample.testcases.TestCase1.",
				"Met Error in filename sample.testcases.TestCase1#main() at line 13",
				"in filename sample.testcases.TestCase1#main() at line 13",
				"Met Problem in table sample.testcases.TestCase1#main() at line 28",
				"in table sample.testcases.TestCase1#main() at line 28",
				"Unable to perform Click on Button_as in sample.testcases.TestCase1#main() line 36"
		};

		for(String s:testStrings){
			ErrorLineParser.parse(0, s);
		}
	}

	private static void test_urlEncode(){
		debug(urlEncode("http://no.exist.com/hello?name=Mickey Mouse&pass=!he*$&email=mickey.mouse@disney.com"));
		debug(urlEncode("http://no.exist.com/hello?name=Mickey Mouse &pass=!he*$&email=mickey.mouse@disney.com="));
		debug(urlEncode("http://no.exist.com/hello?name=Mickey Mouse&pass=!he*$&email=mickey.mouse@disney.com&"));
		debug(urlEncode("http://no.exist.com/hello?name=Mickey Mouse&pass=!he*$&email=mickey.mouse@disney.com\\/"));
		debug(urlEncode("name=Mickey Mouse&pass=!he*@$&email=mickey.mouse@disney.com\\/"));
	}

	/**
	 * Test {@link #convertCoordsToArray(String, int)} and {@link #extractCoordStringPair(String)}. It seems that
	 * {@link #convertCoordsToArray(String, int)} is more tolerable.
	 * @param showDetail
	 */
	private static void test_convertCoords(boolean showDetail){
		String[] badCoordinatesArray = {null, "", " ", "23," , ",23, , 56", ";;23"};
		String[] goodCoordinatesArray = { "23, 56", "23;56", "23 56", "  23   56  ", ";23 ; 56; ", ",23, 56, , ,"};
		String[] resultArray = null;

		System.out.println("\n========================== "+debugmsg(false)+" ============================");
		int unexpected = 0;

		System.out.println("\n----------------------------------------------> Convert badCoordinatesArray: ");
		for(String coordinates: badCoordinatesArray){
			resultArray = extractCoordStringPair(coordinates);
			if(showDetail) System.out.println("extractCoordStringPair(): '"+coordinates+"' has been converted to array "+Arrays.toString(resultArray));
			if(resultArray!=null){
				//we don't care too much about the error of deprecated method
				System.out.println("extractCoordStringPair(): Conversion for '"+coordinates+"' is wrong!");
			}

			resultArray = convertCoordsToArray(coordinates, 2);
			if(showDetail) System.out.println("convertCoordsToArray(): '"+coordinates+"' has been converted to array "+Arrays.toString(resultArray));
			if(resultArray!=null){
				System.err.println("convertCoordsToArray(): Conversion for '"+coordinates+"' is wrong!");
				unexpected++;
			}
		}

		System.out.println("\n----------------------------------------------> Convert goodCoordinatesArray: ");
		for(String coordinates: goodCoordinatesArray){
			resultArray = extractCoordStringPair(coordinates);
			if(showDetail)  System.out.println("extractCoordStringPair(): '"+coordinates+"' has been converted to array "+Arrays.toString(resultArray));
			if(resultArray==null){
				//we don't care too much about the error of deprecated method
				System.out.println("extractCoordStringPair(): conversion for '"+coordinates+"' is wrong!");
			}

			if(showDetail)  resultArray = convertCoordsToArray(coordinates, 2);
			System.out.println("convertCoordsToArray(): '"+coordinates+"' has been converted to array "+Arrays.toString(resultArray));
			if(resultArray==null){
				System.err.println("convertCoordsToArray(): conversion for '"+coordinates+"' is wrong!");
				unexpected++;
			}
		}

		if(unexpected>0){
			System.err.println(debugmsg(false)+"XXXXXXXXXXXXXXXXXXXXXXX We met "+unexpected+" UNEXPECTED errors.");
		}
	}

	private static void test_parseFloat(){
		String[] goodFloatNumber = {"23.5", " 23% ", " 0.36 ", "12.3", " 45 ", "  12.5  % ", "12.3 %"};
		String[] badFloatNumber = {"%%", " % ", " ", " 12. 3", " %5 ", "%", null};

		System.out.println("\n========================== "+debugmsg(false)+" ============================");
		int unexpected = 0;

		for(String number:goodFloatNumber){
			try{
				System.out.println("Converting '"+number+"' to float "+parseFloat(number));
			}catch(Exception e){
				System.err.println("Fail to convert '"+number+"' to float, met "+debugmsg(e));
				unexpected++;
			}
		}
		for(String number:badFloatNumber){
			try{
				System.err.println("Converting '"+number+"' to float "+parseFloat(number));
				unexpected++;
			}catch(Exception e){
				System.out.println("Fail to convert '"+number+"' to float, met "+debugmsg(e));
			}
		}

		if(unexpected>0){
			System.err.println(debugmsg(false)+"XXXXXXXXXXXXXXXXXXXXXXX We met "+unexpected+" UNEXPECTED errors.");
		}
	}

	private static void debug(String message){
		System.out.println(message);
	}

	public static void initIndependantLogByConsole(){
		IndependantLog.setDebugListener(new DebugListener() {
			public String getListenerName() {
				return null;
			}
			public void onReceiveDebug(String arg0) {
				System.out.println(arg0);
			}
		});
	}

	public static void main(String[] args){
		initIndependantLogByConsole();

		test_getTokenList();
		test_reverseArray();
		testErrorLineParser();
		test_breakXpath();
		test_replaceJVMOptionValue();
		test_urlEncode();
		test_convertLine();
		test_convertCoords(true);
		test_parseFloat();

	}

}
