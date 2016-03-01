/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.safs.selenium.webdriver.lib.interpreter.WDTestRunFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.factory.StepTypeFactory;

/**
 * Utility Class used to convert SeRunner HTML FIT Tables into suitable SeInterpreter 
 * Script objects for execution.
 *  
 * @author Carl Nagle
 */
public class SRUtilities {

	/** "org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype" */
	public static String SRSTEPTYPE_PACKAGE = "org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype";
	
	/* SeRunner HTML Table Tags of Interest */
	public static String SR_TABLE_TBODY = "tbody";
	public static String SR_TABLE_TR    = "tr";
	public static String SR_TABLE_TD    = "td";
	
	public static List<Script> parseSeRunnerTable(Document d, File f, StepTypeFactory stepTypeFactory) throws IOException {
		
		NodeList table = d.getElementsByTagName(SR_TABLE_TBODY);
		if(table == null || table.getLength()==0) throw new IOException("Unsupported HTML table format missing 'tbody'!");
		NodeList stepsA = table.item(0).getChildNodes();
		if(stepsA.getLength()==0)throw new IOException("Unsupported HTML table format missing children of 'tbody'!");

		ArrayList<Script> scripts = new ArrayList<Script>();
		Script script = new Script();
		if (f != null) {
			script.name = f.getPath();
		}
		scripts.add(script);
		for(int i=0; i< stepsA.getLength(); i++){
			Node step0 = stepsA.item(i);
			if(SR_TABLE_TR.equalsIgnoreCase(step0.getNodeName())){
				NodeList cells = step0.getChildNodes();				
				if(cells.getLength()==0) continue;
				
				String[] params = new String[cells.getLength()];
				
				for(int c=0;c < cells.getLength();c++){
					params[c] = "";
					Node cell = cells.item(c);
					if(SR_TABLE_TD.equalsIgnoreCase(cell.getNodeName())){
						NodeList text = cell.getChildNodes();
						for(int t=0; t<text.getLength(); t++){
							Node t0 = text.item(t);							
							if(t0.getNodeType()==Node.TEXT_NODE){
								params[c] = params[c].concat(t0.getNodeValue());
							}
						}
						//
						// TODO Check for embedded ${variable} references!
						//
					}
				}
				// params[0] = action;
				// params[1] = locator, other param, or empty; 
				// params[2] = other param, or empty.
				
				stepTypeFactory.setSecondaryPackage(SRSTEPTYPE_PACKAGE);
				
				Step step = new Step(stepTypeFactory.getStepTypeOfName(params[0]));
				
				script.steps.add(step);
				
				if(step.type instanceof SRunnerType){
					((SRunnerType)step.type).processParams(step, params);
				}else{
					// handle default StepTypes here (like Pause)
					if("Pause".equalsIgnoreCase(step.type.getClass().getSimpleName())){
						step.stringParams.put("waitTime", params[1]);
					}
				}
			}
		}		
		return scripts;
	}
	
	public static void setLocatorParam(Step step, String rs){
		String type = null;
		String value = null;
		
		if(rs.startsWith("//")){
			type = "xpath";
			value = rs;
		}else{
			if(rs.contains("=")){
				int i = rs.indexOf("=");
				type = rs.substring(0,i).trim();				
				value = rs.substring(i+1).trim();
				
				if("css".equalsIgnoreCase(type)){
					type="css selector";
				}
			}
		}
		
		if(type==null || value == null) 
			throw new RuntimeException(
					"SRUtilities did not successfully process Locator parameter for StepType "+
			        step.type.getClass().getSimpleName() +" using parameter "+ rs);
		
		step.locatorParams.put("locator", new Locator(type, value));
	}
}
