/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.input.ReaderInputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.safs.IndependantLog;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.factory.DataSourceFactory;
import com.sebuilder.interpreter.factory.ScriptFactory;
import com.sebuilder.interpreter.factory.StepTypeFactory;

/**
 * Support the creation of Scripts from both JSON and HTML Fit Tables.
 * @author canagl
 */
public class WDScriptFactory extends ScriptFactory {

	protected StepTypeFactory stepTypeFactory = new StepTypeFactory();
	protected WDTestRunFactory testRunFactory = new WDTestRunFactory();
	protected DataSourceFactory dataSourceFactory = new DataSourceFactory();

	/** "org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype" */
	public static final String SRSTEPTYPE_PACKAGE = "org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype";
	
	/* SeRunner HTML Table Tags of Interest */
	public static final String SR_TABLE_TBODY = "tbody";
	public static final String SR_TABLE_TR    = "tr";
	public static final String SR_TABLE_TD    = "td";

	public static final String LOCATOR_PARAM  = "locator";
	public static final String TEXT_PARAM     = "text";
	public static final String ITEM_PARAM     = "item";
	public static final String VARIABLE_PARAM = "variable";
	public static final String WAITTIME_PARAM = "waitTime";

	public static final String XPATH_LOCATORTYPE = "xpath";
	public static final String MAP_LOCATORTYPE = "map";
	public static final String LINK_LOCATORTYPE = "link";
	public static final String LINKTEXT_LOCATORTYPE = "link text";
	public static final String NAME_LOCATORTYPE = "name";
	public static final String ID_LOCATORTYPE = "id";
	public static final String VALUE_LOCATORTYPE = "value";
	public static final String CSS_LOCATORTYPE = "css";
	public static final String CSSSELECTOR_LOCATORTYPE = "css selector";
	
	public static final String ANDWAIT_CLASS  = "AndWait";
	public static final String PAUSE_CLASS    = "Pause";
	public static final String STORE_CLASS    = "Store";
	
	/**
	 * @param script -- One of two possible formats:<br>
	 * A JSON string describing a script or suite, or an HTML Fit Table format.
	 * @param sourceFile Optionally. the file the script was loaded from.
	 * @return A Script object, ready to run.
	 * @throws IOException If anything goes wrong with interpreting the script, or
	 * with any Readers.
	 */
	@Override
	public List<Script> parse(String script, File sourceFile) throws IOException {
		String message = "WDScriptFactory.parse(String)";
		try{
			return parse(new JSONObject(new JSONTokener(script)), sourceFile);
		}catch(Exception x){
			message += ", "+ x.getClass().getSimpleName()+" "+ x.getMessage();
			try{
				return parse(getDocumentBuilder().parse(new ByteArrayInputStream(script.getBytes("UTF-8"))), sourceFile, stepTypeFactory);
			}catch(Exception x2){
				message += ", " + x2.getClass().getSimpleName()+" "+ x2.getMessage();
				throw new IOException(message);
			}
		}
	}

	/**
	 * Parse a XML/HTML Fit Table Document into a Script object. 
	 * @param d the Document object parsed from the HTML source File.
	 * @param f the File the HTML file was sourced from.
	 * @param stepTypeFactory
	 * @return Script to run
	 * @throws IOException
	 */
	public List<Script> parse(Document d, File f, StepTypeFactory stepTypeFactory) throws IOException {
		String debugmsg = "WDScriptFactory.parse(Document) ";
		NodeList table = d.getElementsByTagName(SR_TABLE_TBODY);
		if(table == null || table.getLength()==0) throw new IOException("Unsupported HTML table format missing 'tbody'!");
		
		NodeList stepsA = table.item(0).getChildNodes();
		ArrayList<Node> rows = new ArrayList<Node>();
		for(int i=0;i<stepsA.getLength();i++){
			Node n = stepsA.item(i);
			if(n.getNodeName().equalsIgnoreCase(SR_TABLE_TR)) {
				rows.add(n);
			}
		}
		
		if(rows.isEmpty())throw new IOException("Unsupported HTML table format missing children of 'tbody'!");
		
		IndependantLog.info(debugmsg +"found "+ rows.size()+" ROWS of FIT data to process...");
		
		ArrayList<Script> scripts = new ArrayList<Script>();
		Script script = new Script();
		if (f != null) {
			script.name = f.getPath();
		}
		scripts.add(script);
		for(int i=0; i< rows.size(); i++){
			Node step0 = rows.get(i);
			IndependantLog.info(debugmsg +"processing nodeName "+ step0.getNodeName()+"...");
			NodeList cells = step0.getChildNodes();				
			ArrayList<Node> cols = new ArrayList<Node>();
			for(int c = 0;c< cells.getLength();c++){
				Node cn = cells.item(c);
				if(cn.getNodeName().equalsIgnoreCase(SR_TABLE_TD)) cols.add(cn);
			}
			IndependantLog.info(debugmsg +"found "+ cols.size()+" COLS of FIT data to process...");
			
			if(cols.size()==0) continue;
			
			String[] params = new String[cols.size()];
			
			for(int c=0;c < cols.size();c++){
				params[c] = "";
				Node cell = cols.get(c);
				IndependantLog.info(debugmsg +"processing nodeName "+ cell.getNodeName()+"...");
				NodeList text = cell.getChildNodes();
				IndependantLog.info(debugmsg +"found "+ text.getLength()+" nodes of data in cell...");
				for(int t=0; t<text.getLength(); t++){
					Node t0 = text.item(t);							
					IndependantLog.info(debugmsg +"processing nodeName:"+ t0.getNodeName()+", nodeType:"+ t0.getNodeType()+", nodeValue:"+ t0.getNodeValue()+"...");
					if(t0.getNodeType()==Node.TEXT_NODE){
						params[c] = params[c].concat(t0.getNodeValue());
					}
				}
			}
			// params[0] = action;
			// params[1] = locator, other param, or empty; 
			// params[2] = other param, or empty.
			
			stepTypeFactory.setSecondaryPackage(SRSTEPTYPE_PACKAGE);				
			Step step = null;
			try{
				step = new Step(stepTypeFactory.getStepTypeOfName(params[0]));			
			}catch(Throwable x){
				if(params[0].endsWith(ANDWAIT_CLASS)){
					IndependantLog.info(debugmsg +"stripping 'unecessary' AndWait class suffix fro "+ params[0] +"...");
					//AndWait support should be implicit in WebDriver?
					step = new Step(stepTypeFactory.getStepTypeOfName(params[0].substring(0, params[0].length() - ANDWAIT_CLASS.length())));
				}else{
					IndependantLog.debug(debugmsg +"rethrowing "+ x.getClass().getSimpleName()+"...");
					throw x;
				}
			}
			script.steps.add(step);
			if(step.type instanceof SRunnerType){
				IndependantLog.info(debugmsg +"Step Type "+ step.type.getClass().getName()+" processing params as a SRunnerType...");
				((SRunnerType)step.type).processParams(step, params);
			}else{
				IndependantLog.info(debugmsg +"Step Type "+ step.type.getClass().getName()+" is NOT a SRunnerType.  Seeking Getter...");
				// check for Steps that are implied (WaitFor, Verify, Assert, Store, etc..)and contain us as Getters
				boolean hadGetter = false;
				try{					
					Field[] fields = step.type.getClass().getFields();
					for(int g=0;g<fields.length;g++){
						Field field = fields[g];
						if(field.getType().isAssignableFrom(Getter.class)){
							Getter getter = (Getter) field.get(step.type);
							if(getter instanceof SRunnerType){
								IndependantLog.info(debugmsg +"Getter "+ getter.getClass().getName()+" processing params as a SRunnerType...");
								((SRunnerType)getter).processParams(step, params);
								hadGetter = true;
								IndependantLog.info(debugmsg +"Getter "+ getter.getClass().getSimpleName() +" has successfully processed params as a SRunnerType...");
							}else{
								IndependantLog.info(debugmsg +"Getter "+ getter.getClass().getName()+" is NOT a SRunnerType. Seeking alternative...");
								String newClass = stepTypeFactory.getSecondaryPackage() + "." + getter.getClass().getSimpleName();
								try {
									Class c = Class.forName(newClass);
									getter = (Getter) c.newInstance();
									//make sure we are "fixing" the Step with the right new Getter instance.
									// this persists for all future calls for this Step name (like "verifyText")
									field.setAccessible(true);
									field.set(step.type, getter);
									((SRunnerType)getter).processParams(step, params);
									hadGetter = true;
									IndependantLog.info(debugmsg +"Getter "+ newClass+" has successfully processed params as a SRunnerType...");
								} catch (ClassNotFoundException cnfe) {
										throw new RuntimeException("No SelRunner Getter '" + newClass + "' implementation found!");
								} catch (InstantiationException ix){
									throw new RuntimeException("SelRunner Getter '" + newClass + "' could not be instantiated!");
								} catch (ClassCastException ix){
									throw new RuntimeException("SelRunner Getter '"+ newClass +"' must implement the SRunnerType Interface.");
								} catch (IllegalArgumentException ix){
									throw new RuntimeException("SelRunner Getter '" + newClass + "' could not be set into "+step.type.getClass().getName()+"!");
								}								
							}
						}
					}
					if(! hadGetter) {
						IndependantLog.info(debugmsg +"Step Type "+ step.type.getClass().getName()+" processing as a simple, native (non-SRunnerType)...");
						processNativeStep(step, params);
					}else{
						
					}
				}catch(IllegalAccessException ignore){
					IndependantLog.info("SelRunner SRunnerType for Step or Getter cannot be acquired for "+ step.type.getClass().getName()+"!");
					throw new RuntimeException("SelRunner SRunnerType for Step or Getter cannot be acquired for "+ step.type.getClass().getName()+"!");
				}								
			}
		}		
		return scripts;
	}

    protected void processNativeStep(Step step, String[] params){
    	String stepName = step.type.getClass().getSimpleName();
    	
//    	Locator loc = step.locatorParams.get("locator");
//    	if(loc != null && !(loc instanceof WDLocator)){
//    		step.locatorParams.put(LOCATOR_PARAM, new WDLocator(loc));
//    	}
//    	
		//              handle default SeInterpreter StepTypes here (like Pause)
		if( PAUSE_CLASS.equalsIgnoreCase(stepName)){
			step.stringParams.put(WAITTIME_PARAM, params[1]);
		}
		else 
		if( STORE_CLASS.equalsIgnoreCase(stepName)){
			step.stringParams.put(TEXT_PARAM, params[1]);
			step.stringParams.put(VARIABLE_PARAM, params[2]);
		}else{
			IndependantLog.debug("WDScriptFactory.processNativeStep "+ step.type.getClass().getName()+" is not yet supported.");
			throw new RuntimeException("Native SeInterpreter StepType '"+ stepName +" is not yet supported!");
		}
    }
		
	/**
	 * @param reader A Reader pointing to one of 2 possible script formats:<br>
	 * a JSON stream describing a script or suite, or an HTML Fit Table.
	 * @param sourceFile Optionally. the file the script was loaded from.
	 * @return A list of scripts, ready to run.
	 * @throws IOException If anything goes wrong with interpreting the script, or
	 * with the Reader.
	 */
	public List<Script> parse(Reader reader, File sourceFile) throws IOException{
		String message = "WDScriptFactory.parse(Reader)";
		try{ 
			return parse(new JSONObject(new JSONTokener(reader)), sourceFile);
		}catch(Exception x){
			message += ", "+ x.getClass().getSimpleName()+" "+ x.getMessage();
			try{reader.close();}catch(Exception rc){}
			try{
				reader = getUTF8Reader(sourceFile);
				return parse(getDocumentBuilder().parse(new ReaderInputStream(reader)), sourceFile, stepTypeFactory);
			}catch(Exception px){
				message += ", "+ px.getClass().getSimpleName()+" "+ px.getMessage();				
				throw new IOException(message);
			}finally{
				try{ reader.close();}catch(Exception fx){}
			}
		}
	}
	
	/**
	 * @return DocumentBuilder if one can successfully be created.
	 * @throws ParserConfigurationException
	 */
	protected DocumentBuilder getDocumentBuilder() throws ParserConfigurationException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		return dbf.newDocumentBuilder();
	}
	
	/**
	 * @param sourceFile
	 * @return BufferedReader opened with UTF-8 encoding.
	 * @throws IOException
	 */
	protected BufferedReader getUTF8Reader(File sourceFile) throws IOException{
		return new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));		
	}
	
	/**
	 * @param f A File pointing to a JSON file describing a script or suite.
	 * @return A list of scripts, ready to run.
	 * @throws IOException If anything goes wrong with interpreting the JSON, or
	 * with the Reader.
	 * @throws JSONException If the JSON can't be parsed.
	 */
	public List<Script> parse(File f) throws IOException{
		BufferedReader r = null;
		try {
			return parse(r = getUTF8Reader(f), f);
		} finally {
			try { r.close(); } catch (Exception e) {}
		}
	}	
}
