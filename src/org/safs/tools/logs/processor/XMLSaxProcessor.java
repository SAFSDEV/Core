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
 * APR 27, 2018    (Lei Wang) Initial release.
 * MAY 02, 2018    (Lei Wang) Added comments to show how to use this class.
 *                           Added method main() to provide an interface in command line console.
 * MAY 14, 2018    (Lei Wang) Modified code to simplify the use of this XMLSaxProcessor.
 *                           Provided constructors to accept source in more types.
 * JUN 13, 2018    (Lei Wang) Provided parameter -counterlevel.
 *
 */
package org.safs.tools.logs.processor;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.safs.Constants;
import org.safs.SAFSException;
import org.safs.text.FileUtilities;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parse SAFS XML Log.<br>
 * The output will depends on the concrete XMLSaxAbstractHandler assigned in the constructor.<br>
 * <br>
 *
 * Examples:<br>
 * <pre>
 * //Create a processor with XMLSaxToJUnitXMLHandler to convert 'SAFS XML Log' into JUnit xml report.
 * String source = "C:\\temp\\safstest\\Datapool\\Logs\\TIDTest.SAFS.xml";
 * XMLSaxProcessor sxp = new XMLSaxProcessor(new FileReader(source), new XMLSaxToJUnitXMLHandler());
 * Object result = sxp.parse();
 * System.out.println(result);
 *
 * //Use the same processor to process another 'SAFS XML Log'.
 * source = "C:\\SeleniumPlusProjects\\AUTOCOUNTABLETEST\\Logs\\VATest.xml";
 * sxp.setSource(new InputSource(new FileReader(source)));
 * result = sxp.parse();
 * System.out.println(result);
 *
 * //Create a new processor with XMLSaxToRepositoryHandler to push 'SAFS XML Log' into 'safs data repository'.
 * String safsdataServiceURL = "http://safsDataService";
 * source = "C:\\temp\\safstest\\Datapool\\Logs\\TIDTest.SAFS.xml";
 * sxp = new XMLSaxProcessor(new FileReader(source), new XMLSaxToRepositoryHandler(safsdataServiceURL));
 * Object result = sxp.parse();
 * System.out.println(result);
 * </pre>
 *
 * @author Lei Wang
 *
 */
public class XMLSaxProcessor {
	protected SAXParser saxParser = null;
	protected InputSource inputSource = null;
	protected XMLSaxAbstractHandler handler = null;

	private XMLSaxProcessor() throws ParserConfigurationException, SAXException{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		saxParser = factory.newSAXParser();
	}

	public XMLSaxProcessor(Reader source, XMLSaxAbstractHandler handler) throws ParserConfigurationException, SAXException{
		this();
		inputSource = new InputSource(source);
		this.handler = handler;
	}

	public XMLSaxProcessor(String source, XMLSaxAbstractHandler handler) throws ParserConfigurationException, SAXException{
		this(new StringReader(source), handler);
	}

	public XMLSaxProcessor(InputStream source, XMLSaxAbstractHandler handler) throws ParserConfigurationException, SAXException{
		this();
		inputSource = new InputSource(source);
		this.handler = handler;
	}

	public XMLSaxProcessor(InputSource inputSource, XMLSaxAbstractHandler handler) throws ParserConfigurationException, SAXException{
		this();
		this.inputSource = inputSource;
		this.handler = handler;
	}

	public void setInputSource(InputSource inputSource){
		this.inputSource = inputSource;
	}

	protected void beforeParse()  throws SAFSException{
		if(saxParser==null || inputSource==null || handler==null){
			throw new SAFSException("The saxParser, inputSource or handler is null, cannot process!");
		}
	}

	protected void afterParse()  throws SAFSException{

	}

	public Object parse()  throws SAFSException{
		beforeParse();

		try {
			saxParser.parse(inputSource, handler);

			return handler.getResult();
		} catch (SAXException e) {
			throw new SAFSException("Failed to parse XML document with SAX Parser! Met "+e.toString());
		} catch (IOException e) {
			throw new SAFSException("Failed to parse XML document with SAX Parser! Met "+e.toString());
		}finally{
			afterParse();
		}
	}

	private static String usage(){
		String usage = "java XMLSaxProcessor -source SAFSXMLLog.xml [-counterunit TESTCASE|TESTSTEP] [-out junitLog.xml] [-safsdata http://safsdataServiceURL]\n"+
				       "java XMLSaxProcessor -s SAFSXMLLog.xml [-cu TESTCASE|TESTSTEP] [-o junitLog.xml] [-d http://safsdataurl]\n";
		return usage;
	}
	/**
	 * Convert the SAFS XML Log into JUnit XML report. The JUnit report will be
	 * printed to the console or be written into a file if the optional parameter '-out/-o' is provided.<br>
	 * Push the SAFS XML Log into SAFS-Data-Repository if the optional parameter '-safsdata/-d' is provided.<br>
	 * Provide the counter unit by the optional parameter '-counterunit/-cu'; It can be TESTCASE or TESTSTEP, the default unit is 'TESTCASE'.<br>
	 * <br>
	 * Usage:<br>
	 * <pre>
	 * java XMLSaxProcessor -source SAFSXMLLog.xml [-counterunit TESTCASE|TESTSTEP] [-out junitLog.xml] [-safsdata http://safsdataurl]
	 * java XMLSaxProcessor -s SAFSXMLLog.xml [-cu TESTCASE|TESTSTEP] [-o junitLog.xml] [-d http://safsdataurl]
	 * </pre>
	 * Examples:<br>
	 * <pre>
	 * java XMLSaxProcessor -source SAFSXMLLog.xml
	 * java XMLSaxProcessor -source SAFSXMLLog.xml -out junitLog.xml
	 * java XMLSaxProcessor -source SAFSXMLLog.xml -counterunit TESTSTEP -out junitLog.xml
	 * java XMLSaxProcessor -source SAFSXMLLog.xml -safsdata http://safsdataurl
	 * java XMLSaxProcessor -source SAFSXMLLog.xml -out junitLog.xml -safsdata http://safsdataurl
	 * </pre>
	 *
	 * @param args
	 */
	public static void main(String[] args){

		if(args.length<1){
			System.out.println(usage());
		}

		String source = null;
		String out = null;
		String safsdata = null;
		String counterunit = null;
		for(int i=0;i<args.length;i++){
			if(args[i].equals("-s") || args[i].equals("-source")){
				if(i+1<args.length){
					source = args[++i];
				}else{
					System.err.println("Missing '-source' parameter!");
					System.out.println(usage());
					return;
				}
			}else if(args[i].equals("-o") || args[i].equals("-out")){
				if(i+1<args.length){
					out = args[++i];
				}else{
					System.err.println("Missing '-out' parameter!");
					System.out.println(usage());
					return;
				}
			}else if(args[i].equals("-d") || args[i].equals("-safsdata")){
				if(i+1<args.length){
					safsdata = args[++i];
				}else{
					System.err.println("Missing '-safsdata' parameter!");
					System.out.println(usage());
					return;
				}
			}else if(args[i].equals("-cu") || args[i].equals("-counterunit")){
				if(i+1<args.length){
					counterunit = args[++i].toUpperCase().trim();
					if(!Constants.isCounterUnitValid(counterunit)){
						System.err.println("-counterunit parameter '"+counterunit+"' is not valid. It can be one of "+ Arrays.asList(Constants.VALID_COUTNER_UNITS));
						return;
					}
				}else{
					System.err.println("Missing '-counterunit' parameter!");
					System.out.println(usage());
					return;
				}
			}
		}

		if(source==null){
			System.err.println("Missing '-source' parameter!");
			System.out.println(usage());
			return;
		}

		XMLSaxAbstractHandler handler = null;
		try{
			if(counterunit!=null){
				handler = new XMLSaxToJUnitXMLHandler(counterunit);
			}else{
				handler = new XMLSaxToJUnitXMLHandler();
			}
			XMLSaxProcessor sxp = new XMLSaxProcessor(new FileReader(source), handler);
			Object result = sxp.parse();

			if(out!=null){
				FileUtilities.writeStringToUTF8File(out, result.toString());
			}else{
				System.out.println(result);
			}
		}catch(Exception e){
			System.out.println(usage());
			e.printStackTrace();
		}

		try{
			if(safsdata!=null){
				if(counterunit!=null){
					handler = new XMLSaxToRepositoryHandler(safsdata, counterunit);
				}else{
					handler = new XMLSaxToRepositoryHandler(safsdata);
				}

				//Create processor with XMLSaxToRepositoryHandler to push 'test data' into safs-data-repository.
				XMLSaxProcessor sxp = new XMLSaxProcessor(new FileReader(source), handler);
				Object result = sxp.parse();
				System.out.println(result);
			}
		}catch(Exception e){
			System.out.println(usage());
			e.printStackTrace();
		}

	}
}
