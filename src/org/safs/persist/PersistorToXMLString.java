/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * OCT 24, 2017    (Lei Wang) Initial release.
 *                           Modified needEscape(): if the value has already been escaped (wrapped in <![CDATA[...]]>), don't escape again.
 * NOV 03, 2017    (Lei Wang) Modified UnpickleHandler.endElement(): If a tag represents a Persistable object, then set the parent tag (a Persistable object) as its parent.
 * JUN 13, 2018    (Lei Wang) Moved needEscape() to XMLConstants. Modified escape().
 */
package org.safs.persist;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.safs.Constants.XMLConstants;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Write Persistable object to an XML string, such as:
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
 * as &lt;![CDATA[...]]>, as shown in the example above. For other special symbols to be escaped, please
 * refer to {@link XMLConstants#SYMBOL_TO_ESCAPE}.
 *
 * @author Lei Wang
 *
 */
public class PersistorToXMLString extends PersistorToHierarchialString{
	protected SAXParser saxParser = null;
	protected InputSource inputSource = null;

	public PersistorToXMLString(){
		super();
	}

	public PersistorToXMLString(String stringFormat){
		super(stringFormat);
	}

	public PersistorToXMLString(Reader stringFormatReader){
		super(stringFormatReader);
	}

	@Override
	protected String getContainerBegin(String className, boolean needLeadingName){
		//Ignore parameter 'needLeadingName', it is useful for JSON file.
		return "<"+getTagName(className)+" "+XMLConstants.PROPERTY_CLASSNAME+"=\""+className+"\">\n";
	}
	@Override
	protected String getChildBegin(String key, String value, String classname){
		return "<"+key+">"+escape(value)+"</"+key+">";
//		return "<"+key+" "+XMLConstants.PROPERTY_CLASSNAME+"=\""+classname+"\">"+value+"</"+key+">";
	}
	@Override
	protected String getContainerEnd(String className){
		return "</"+getTagName(className)+">";
	}


	/**
	 * Wrap the string in "<![CDATA[]]>" if it contains special symbols {@link XMLConstants#SYMBOL_TO_ESCAPE},
	 * please refer to {@link #needEscape(String)}.
	 *
	 * @param value String, the value to be escaped if it contains special symbols
	 * @return String, the escaped string
	 */
	@Override
	protected String escape(String value){
		String result = XMLConstants.escape(value);
		return super.escape(result);
	}

	@Override
	protected void beforeUnpickle()  throws SAFSException{
		super.beforeUnpickle();

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			saxParser = factory.newSAXParser();
			inputSource = new InputSource(getStringFormatReader());

		} catch (ParserConfigurationException | SAXException e) {
			throw new SAFSException("Failed to creat XML SAX Parser! Met "+e.toString());
		}
	}

	@Override
	protected Persistable doUnpickle()  throws SAFSException{
		//The handler will create Persistable object.
		try {
			UnpickleHandler unpickle = new UnpickleHandler();
			saxParser.parse(inputSource, unpickle);

			return unpickle.getFreshFood();
		} catch (SAXException e) {
			throw new SAFSException("Failed to parse XML document with SAX Parser! Met "+e.toString());
		} catch (IOException e) {
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

		@Override
		public void startDocument () throws SAXException{
			value = new StringBuilder();
			warnings = new StringBuilder();
		}

		@Override
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
							tag.ignored = PersistorToXMLString.this.isIgnoredField(parent.classname, qName);
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

		@Override
		public void characters (char ch[], int start, int length) throws SAXException{
			if(!tag.ignored){
				String chunk = new String(ch, start, length);
				value.append(chunk);
			}
		}

		@Override
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
									String trimmedValue = value.toString().trim();
									IndependantLog.info("XML UnpickleHandler: parsing array Object: "+trimmedValue);
									//If the value is wrapped by "[" and "]", then it is probably an array object in JSON format
									if(trimmedValue.startsWith(JSON_ARRAY_BRACKET_LEFT) && trimmedValue.endsWith(JSON_ARRAY_BRACKET_RIGHT)){
										try{
											JSONArray jsonArray = new JSONArray(trimmedValue);
											if(!parent.container.setField(me.name, jsonArray)){
												warnings.append("\nFailed to set field '"+parent.name+"."+me.name+"' with array value '"+value+"'");
											}

										}catch(JSONException jsonE){
											IndependantLog.warn("XML UnpickleHandler: met exception "+jsonE.toString());
											//If the string cannot be parsed as a json array, then we suppose that each item is represented as XML string
											PersistorToXMLString persistor = new PersistorToXMLString();
											//remove the leading "[" and ending "]"
											trimmedValue = trimmedValue.substring(1, trimmedValue.length()-1);
											String[] items = trimmedValue.split(JSON_ARRAY_COMMA_SEP);
											List<Persistable> persitables = new ArrayList<Persistable>();
											for(String item: items){
												persistor.setStringFormat(item.trim());
												persitables.add(persistor.unpickle(ignoredFieldsForUnpickle));
											}

											if(!parent.container.setField(me.name, persitables)){
												warnings.append("\nFailed to set field '"+parent.name+"."+me.name+"' with array value '"+value+"'");
											}

										}
									}
								}
							}else{
								//If me.container (a Persistable object) is not null, we should set its parent Persistable object.
								me.container.setParent(parent.container);
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
