/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 08, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSPersistableNotEnableException;
import org.safs.SAFSVerificationException;
import org.safs.StringUtils;
import org.safs.tools.RuntimeDataInterface;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Verify a persistable object to an XML file, such as:
 * <pre>
 * &lt;Response&gt;
 *   &lt;StatusCode&gt;200&lt;/StatusCode&gt;
 *   &lt;Headers&gt;
 *     &lt;ContentType&gt;text/xml&lt;/ContentType&gt;
 *   &lt;/Headers&gt;
 *   &lt;Request&gt;
 *     &lt;Method&gt;GET&lt;/Method&gt;
 *     &lt;Headers&gt;
 *       Content-Type:application/octet-stream, Accept:application/octet-stream
 *     &lt;/Headers&gt;
 *   &lt;/Request&gt;
 * &lt;/Response&gt;
 * </pre>
 *
 * This class uses the Java SAX XML Reader to do the work.
 * @author Lei Wang
 *
 */
public class VerifierToXMLFile extends VerifierToFile{
	protected SAXParser saxParser = null;
	protected InputSource inputSource = null;

	/**
	 * Holding the actual contents to verify, they are pairs of (flatKey, content) such as
	 * (Response.ID, "FFE3543545JLFS")
	 * (Response.Headers, "{Date=Tue, 06 DEC 2016 03:08:12 GMT}")
	 * (Response.Request.Headers, "{Content-Length=4574, Via=1.1 inetgw38 (squid)}")
	 */
	protected Map<String, Object> actualContents = null;

	protected Set<String> checkedFields = null;

	/**
	 * Holding the fields which are ignored. No need to verify.<br/>
	 * The field is expressed as a flat key.<br/>
	 * <pre>
	 * If this Set contains a field 'Response.Request', then all its children will
	 * be ignored, such as:
	 * Response.Request.Headers
	 * Response.Request.MessageBody
	 * ...
	 * </pre>
	 * @see #isIgnoredFiled(String)
	 */
	protected Set<String> ignoredFields = null;

	/** If the all the fields of actual object need to match with those in the persistent benchmark.<br/>
	 * If this is true, all the fields of actual object need to be verified.<br/>
	 * Otherwise, only the fields specified in the persistent benchmark need to be verified.<br/>
	 */
	protected boolean matchAllFields 	= false;
	/** If the field's value needs to be matched wholly or partially  */
	protected boolean valueContains 		= false;
	/** If the field's value needs to be matched case-sensitively */
	protected boolean valueCaseSensitive 	= false;

	/**
	 * @param runtime
	 * @param filename
	 */
	public VerifierToXMLFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
		actualContents = new HashMap<String, Object>();
		ignoredFields = new HashSet<String>();
		checkedFields = new HashSet<String>();
		nonMatchedMessages = new StringBuilder();
	}

	public void beforeCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{
		try {
			actualContents.clear();
			ignoredFields.clear();
			checkedFields.clear();
			nonMatchedMessages.delete(0, nonMatchedMessages.length());

			SAXParserFactory factory = SAXParserFactory.newInstance();
			saxParser = factory.newSAXParser();
			inputSource = new InputSource(reader);

			if(conditions.length>0){
				matchAllFields = conditions[0];
			}
			if(conditions.length>1){
				valueContains = conditions[1];
			}
			if(conditions.length>2){
				valueCaseSensitive = conditions[2];
			}

		} catch (ParserConfigurationException | SAXException e) {
			throw new SAFSException("Failed to creat XML SAX Parser!");
		}

		initFlatContents(persistable, null);

	}

	/**
	 * Turn the Persistable hierarchical contents Map into a flat key Map.
	 *
	 * @param persistable Persistable, from which the contents will be retrieved.
	 * @param ancestorsKey String, the ancestors of current Persistable object.
	 * @throws SAFSException if the Persistable object is null.
	 */
	private void initFlatContents(Persistable persistable, String ancestorsKey) throws SAFSException{
		String flatKey = null;

		if(StringUtils.isValid(ancestorsKey)){
			flatKey = ancestorsKey+"."+persistable.getClass().getSimpleName();
		}else{
			flatKey = persistable.getClass().getSimpleName();
		}

		try {
			validate(persistable);
		} catch (SAFSPersistableNotEnableException e) {
			ignoredFields.add(flatKey);
			return;
		}

		//The Persistable object itself doesn't have a value, and it contains children
		//while the SAX XML parser will treat it as Element and assign it a default string "\n" as value
		//So we add the default string "\n" for Persistable object itself in the actualContents Map to
		//get the verification pass.
		actualContents.put(flatKey, CONTAINER_ELEMENT_DEFAULT_VALUE);

		Map<String, Object> contents = persistable.getContents();

		Object value = null;
		for(String key:contents.keySet()){
			value = contents.get(key);

			if(value instanceof Persistable){
				initFlatContents((Persistable)value, flatKey);
			}else{
				actualContents.put(flatKey+"."+key, value);
			}
		}
	}

	@Override
	public void check(Persistable persistable, boolean... conditions) throws SAFSException, IOException {
		try {
			saxParser.parse(inputSource, new VerificationHandler());

			if(!matched){
				throw new SAFSVerificationException(nonMatchedMessages.toString());
			}

			//If we are here, which means all the fields in the persistence benchmark have been satisfied,
			//that is to say "the actual object" contains all the fields in benchmark and matched.
			if(matchAllFields){
				//We still need to check if all the fields of actual object have been satisfied, if fieldContains is not true.
				//all the fields of actual object should be matched.
				Set<String> actualFields = actualContents.keySet();
				actualFields.removeAll(checkedFields);
				if(!actualFields.isEmpty()){
					matched = false;
					String errorMsg = "Missing fields in benchmark file:\n";
					errorMsg += Arrays.toString(actualFields.toArray());
					throw new SAFSVerificationException(errorMsg);
				}
			}

		} catch (SAXException e) {
			throw new SAFSException(e.toString());
		}
	}

	/** This field will contain true if the verification succeed. */
	protected boolean matched = true;
	/**
	 * It contains the messages indicating the non matched fields.
	 */
	protected StringBuilder nonMatchedMessages = null;

	/**
	 * This method will check the filed's value against the {@link #actualContents}<br/>
	 * Here this class is called within {@link VerificationHandler#endElement(String, String, String)}.
	 *
	 * @param field	String, the field to check
	 * @param expectedText String, the field's expected text.
	 */
	protected void match(String field, String expectedText){

		if(isIgnoredFiled(field)){
			IndependantLog.debug("Ignoring checking '"+field+"'.");
		}else{
			IndependantLog.debug("Checking field '"+field+"' ... ");

			Object actual = actualContents.get(field);
			checkedFields.add(field);
			if(actual==null){
				nonMatchedMessages.append("Cannot find the actual value for field '"+field+"'!\n");
				matched = false;

			}else{
				String actualText = actual.toString();

				if(!StringUtils.matchText(actualText, expectedText, valueContains, !valueCaseSensitive)){
					nonMatchedMessages.append("'"+field+"' did not match!\n"
							+ "actual: "+actualText+"\n"
							+ "expected: "+expectedText+"\n");
					matched = false;
				}
			}
		}

	}

	/**
	 * Check if the filed is being ignored at the moment.<br/>
	 * <pre>
	 * If the Set {@link #ignoredFields} contains a field 'Response.Request',
	 * then all its children (starting with 'Response.Request') will be ignored, such as:
	 * Response.Request.Headers
	 * Response.Request.MessageBody
	 * ...
	 * </pre>
	 * @param field String, the field to check
	 * @return boolean if this field is being ignored.
	 * @see #ignoredFields
	 */
	protected boolean isIgnoredFiled(String field){
		for(String ignoredField:ignoredFields){
			if(field.startsWith(ignoredField)) return true;
		}
		return false;
	}

	/**
	 * '<b>\n</b>'<br/>
	 * For container Element, such as <b>Response</b> in XML
	 * <pre>
	 * &lt;Response&gt;
	 *   &lt;StatusCode&gt;200&lt;/StatusCode&gt;
	 * &lt;/Response&gt;
	 * </pre>
	 * actually it doesn't have any string value,
	 * but the XML SAX parser will assign a "<b>\n</b>" to it.
	 */
	private static final String CONTAINER_ELEMENT_DEFAULT_VALUE = "\n";

	protected class VerificationHandler extends DefaultHandler{
		/** It will contain the string value of each document. */
		StringBuilder value = null;
		/** It holds the full path for the Element being processed, such as Response.StatusCode, Response.Headers etc. */
		protected StringBuilder fullPathTag = null;

	    public void startDocument () throws SAXException{
	    	value = new StringBuilder();
	    	fullPathTag = new StringBuilder();
	    }

		//http://www.anddev.org/parsing_xml_from_the_net_-_using_the_saxparser-t353.html
		//http://stackoverflow.com/questions/9434860/java-saxparser-different-between-localname-and-qname
		public void startElement (String uri, String localName, String qName, Attributes atts) throws SAXException{
			cleanElementValue();
			appendTagToPath(qName);

//			debug("startElement: "+qName+"; fullkey="+fullPathTag.toString());

		}

		public void characters (char ch[], int start, int length) throws SAXException{
			String chunk = new String(ch, start, length);
			value.append(chunk);
		}

	    public void endElement (String uri, String localName, String qName) throws SAXException{
	    	//Finally the buffer 'value' holds the element's value, we can check it :-)
//	    	debug(fullPathTag.toString()+" = "+value.toString());

	    	//Call the method match() in the outer class VerifierToXMLFile
	    	match(fullPathTag.toString(), value.toString());

	    	cleanElementValue();
	    	removeTagFromPath(qName);

//	    	debug("endElement: "+qName+"; fullkey="+fullPathTag.toString());

	    }

	    /**
	     * Add the tag name to {@link #fullPathTag}. It is normally called when starting Element.
	     * @param tagName String, the tag name to append to the full-path-tag
	     * @see #startElement(String, String, String, Attributes)
	     */
	    private void appendTagToPath(String tagName){
			if(fullPathTag.length()>0){
				fullPathTag.append("."+tagName);
			}else{
				fullPathTag.append(tagName);
			}
	    }

	    /**
	     * Remove the tag name from {@link #fullPathTag}. It is normally called when ending Element.
	     * @param tagName String, the tag name to remove from the full-path-tag
	     * @see #endElement(String, String, String)
	     */
	    private void removeTagFromPath(String tagName){
	    	int index = fullPathTag.lastIndexOf(tagName);
	    	//remove the tag name
	    	fullPathTag.delete(index, index+tagName.length());

	    	//remove the possible dot before the tag name.
	    	if(index>0 && fullPathTag.charAt(index-1)=='.'){
	    		fullPathTag.delete(index-1, index);
	    	}
	    }

	    /** Clean the buffer holding the element's value. */
	    private void cleanElementValue(){
	    	value.delete(0, value.length());
	    }
	}

	void debug(String msg){
		System.out.println(msg);
	}

	public static void main(String[] args) throws Exception{

		VerifierToXMLFile verifier = new VerifierToXMLFile(null, null);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		Reader reader = new FileReader(new File("D:\\TMP\\response.xml"));
		InputSource inputSource = new InputSource(reader);

		VerificationHandler handler = verifier.new VerificationHandler();

		saxParser.parse(inputSource, handler);
	}
}
