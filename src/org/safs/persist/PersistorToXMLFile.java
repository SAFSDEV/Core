/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 05, 2016    (SBJLWA) Initial release.
 */
package org.safs.persist;

import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.safs.Constants.XMLConstants;
import org.safs.SAFSException;
import org.safs.tools.RuntimeDataInterface;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Write Persistable object to an XML file, such as:
 * <pre>
 * &lt;Response classname="org.safs.rest.Response"&gt;
 *   &lt;StatusCode&gt;200&lt;/StatusCode&gt;
 *   &lt;Headers>{Date=Tue, 13 Dec 2016 03:29:27 GMT, Content-Length=4574, Connection=keep-alive, Content-Type=application/xml}&lt;/Headers&gt;
 *   &lt;EntityBody&gt;<b>&lt;![CDATA[</b><font color="red">&lt;?xml</font> version="1.0"?&gt;&lt;CUSTOMERList xmlns:xlink="http://www.w3.org/1999/xlink"&gt;
 *     &lt;CUSTOMER xlink:href="http://www.thomas-bayer.com/sqlrest/CUSTOMER/0/"&gt;0&lt;/CUSTOMER&gt;
 *     &lt;CUSTOMER xlink:href="http://www.thomas-bayer.com/sqlrest/CUSTOMER/49/"&gt;49&lt;/CUSTOMER&gt;
 *     &lt;/CUSTOMERList&gt;<b>]]&gt;</b>&lt;/EntityBody&gt;
 *   &lt;Request classname="org.safs.rest.Request"&gt;
 *     &lt;Method&gt;GET&lt;/Method&gt;
 *     &lt;Headers&gt;
 *       Content-Type:application/octet-stream, Accept:application/octet-stream
 *     &lt;/Headers&gt;
 *   &lt;/Request&gt;
 * &lt;/Response&gt;
 * </pre>
 *
 * NOTE: Be careful with the value starting with <font color="red">&lt;?xml</font>, which should be wrapped
 * as &lt;![CDATA[...]]>, as showed in the example above.
 *
 * @author sbjlwa
 *
 */
public class PersistorToXMLFile extends PersistorToHierarchialFile{
	protected SAXParser saxParser = null;
	protected InputSource inputSource = null;

	/**
	 * @param runtime
	 * @param filename
	 */
	public PersistorToXMLFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	@Override
	protected void containerBegin(String className) throws IOException{
		writer.write("<"+getTagName(className)+" "+XMLConstants.PROPERTY_CLASSNAME+"=\""+className+"\">\n");
	}
	@Override
	protected void childBegin(String key, String value) throws IOException{
		writer.write("<"+key+">"+value+"</"+key+">");
	}
	@Override
	protected void containerEnd(String className) throws IOException{
		writer.write("</"+getTagName(className)+">\n");
	}

	/**
	 * Wrap the string in "<![CDATA[]]>" if it starts with "<?XML".
	 *
	 * @param value String, the value to escape
	 * @return String, the escaped string
	 */
	@Override
	protected String escape(String value){
		String result = value;

		if(result.toUpperCase().startsWith(XMLConstants.XML_START)){
			result = XMLConstants.CDATA_START+result+XMLConstants.CDATA_END;
		}

		return result;
	}

	@Override
	protected void beforeUnpickle()  throws SAFSException, IOException{
		super.beforeUnpickle();

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			saxParser = factory.newSAXParser();
			inputSource = new InputSource(reader);

		} catch (ParserConfigurationException | SAXException e) {
			throw new SAFSException("Failed to creat XML SAX Parser! Met "+e.toString());
		}
	}

	@Override
	protected Persistable doUnpickle()  throws SAFSException, IOException{
		//The handler will create Persistable object.
		try {
			UnpickleHandler unpickle = new UnpickleHandler();
			saxParser.parse(inputSource, unpickle);

			return unpickle.getFreshFood();
		} catch (SAXException e) {
			throw new SAFSException("Failed to parse XML document with SAX Parser! Met "+e.toString());
		}
	}

	private static class Tag{
		/** The XML tag name */
		String name = null;
		/** The property 'classname' of this tag, not all tag has this property, it is ONLY for container tag. */
		String classname = null;
		/** If this Tag represents a container, this field will hold a Persistable object. */
		Persistable container = null;
		/** If this tag should be ignored during unpickle.*/
		boolean ignored = false;
	}

	protected class UnpickleHandler extends DefaultHandler{
		protected Persistable freshFood = null;
		/** It will contain the string value of each document. */
		protected StringBuilder value = null;
		protected StringBuilder warnings = null;

		protected Tag tag = null;
		protected Stack<Tag> tagStack = new Stack<Tag>();

		public Persistable getFreshFood(){
			return freshFood;
		}
		public String getWarnings(){
			return warnings.toString();
		}

		public void startDocument () throws SAXException{
			value = new StringBuilder();
			warnings = new StringBuilder();
		}

		public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException{
			cleanElementValue();
			tag = new Tag();
			tag.name = qName;
			tag.classname = attributes.getValue(XMLConstants.PROPERTY_CLASSNAME);
			if(tag.classname==null){
				String packageName = attributes.getValue(XMLConstants.PROPERTY_PACKAGE);
				if(packageName!=null){
					tag.classname = packageName.isEmpty()? tag.name : packageName+"."+tag.name;
				}
			}

			try{
				Tag parent = null;
				if(!tagStack.isEmpty()){
					parent = tagStack.peek();
				}

				if(parent!=null && parent.ignored){
					tag.ignored = true;
				}

				if(!tag.ignored){
					if(tag.classname!=null){
						try {
							Object object = Class.forName(tag.classname).newInstance();
							if(object instanceof Persistable){
								//The first Persistable will be returned as freshFood.
								if(freshFood==null) freshFood = (Persistable) object;
								tag.container = (Persistable) object;
								tag.ignored = false;
							}else{
								warnings.append("\nThis object '"+tag.classname+"' is not a Persistable, it and its children will be ignored.");
								tag.ignored = true;
							}
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
							warnings.append("\nMet "+e.toString());
							tag.ignored = true;
						}
					}else{
						//we consider it as a field of it parent tag.
						if(parent==null || parent.container==null){
							warnings.append("\nThe parent is null for '"+tag.name+"', this tag will be ignored.");
							tag.ignored = true;
						}else{
							tag.ignored = PersistorToXMLFile.this.isIgnoredFiled(parent.classname, qName);
						}
					}
				}
			}catch(Exception e){
				tag.ignored = true;
				throw new SAXException(e.toString());
			}finally{
				tagStack.push(tag);
			}
		}

		public void characters (char ch[], int start, int length) throws SAXException{
			if(!tag.ignored){
				String chunk = new String(ch, start, length);
				value.append(chunk);
			}
		}

		public void endElement (String uri, String localName, String qName) throws SAXException{
			//Finally the buffer 'value' holds the element's value, we can check it :-)
			//pop the tag from the stack

			try{
				Tag me = tagStack.pop();

				warnings.append("\nFailed match tag name: '"+me.name+"'!='"+qName+"'");
				if(!me.ignored){
					if(!tagStack.isEmpty()){
						Tag parent = tagStack.peek();
						if(!parent.ignored && parent.container!=null){
							if(me.container==null){
								//I am a simple field
								if(!parent.container.setField(me.name, value.toString())){
									warnings.append("\nFailed to set field '"+parent.name+"."+me.name+"' with value '"+value+"'");
								}
							}else{
								//I am a container field, value doesn't hold any value, set my container to parent.
								if(!parent.container.setField(me.name, me.container)){
									warnings.append("\nFailed to set field "+parent.name+"."+me.name+"' with value "+me.container);
								}
							}
						}
					}else{
						warnings.append("\nMissing parent for tag '"+tag.name+"'");
					}
				}

			}catch(Exception e){
				throw new SAXException(e.toString());
			}finally{
				cleanElementValue();
			}

		}

		/** Clean the buffer holding the element's value. */
		private void cleanElementValue(){
			value.delete(0, value.length());
		}
	}

}
