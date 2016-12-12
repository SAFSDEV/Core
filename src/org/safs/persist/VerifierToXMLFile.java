/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 08, 2016    (SBJLWA) Initial release.
 */
package org.safs.persist;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.safs.SAFSException;
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
 * @author sbjlwa
 *
 */
public class VerifierToXMLFile extends VerifierToFile{
	protected SAXParser saxParser = null;
	protected InputSource inputSource = null;

	/**
	 * @param runtime
	 * @param filename
	 */
	public VerifierToXMLFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	public void beforeCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{
		super.beforeCheck(persistable, conditions);

		actualContents = persistable.getContents(defaultElementValues, ignoredFields, true);

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			saxParser = factory.newSAXParser();
			inputSource = new InputSource(reader);

			//The handler will fill 'expectedContents'.
			saxParser.parse(inputSource, new VerificationHandler());

		} catch (ParserConfigurationException | SAXException e) {
			throw new SAFSException("Failed to creat XML SAX Parser!");
		}
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
	private static final Map<String,String> defaultElementValues = new HashMap<String,String>();
	static{
		defaultElementValues.put(Persistable.CONTAINER_ELEMENT, CONTAINER_ELEMENT_DEFAULT_VALUE);
	}

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

	    	//Put the pair(flat-key, value) into the Map 'expectedContents' for later verification
	    	expectedContents.put(fullPathTag.toString(), value.toString());

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
