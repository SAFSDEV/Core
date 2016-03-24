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

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.interpreter.WDLocator;
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
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
 * @author canagl
 */
public class SRUtilities {

	public static void setLocatorParam(Step step, String rs){
		String debugmsg = StringUtils.debugmsg(false);
		String type = null;
		String value = null;
		
		if(rs.startsWith("//")){
			type = WDScriptFactory.XPATH_LOCATORTYPE;
			value = rs;
		}else{
			if(rs.contains("=")){
				int i = rs.indexOf("=");
				type = rs.substring(0,i).trim();				
				value = rs.substring(i+1).trim();
				
				if(WDScriptFactory.CSS_LOCATORTYPE.equalsIgnoreCase(type)){
					type=WDScriptFactory.CSSSELECTOR_LOCATORTYPE;
				}
			}
		}
		
		if(type==null || value == null) 
			throw new RuntimeException(
					"SRUtilities did not successfully process Locator parameter for StepType "+
			        step.type.getClass().getSimpleName() +" using parameter "+ rs);
		
		try{
			IndependantLog.info(debugmsg+ step.type.getClass().getName()+" to receive WDLocator Type '"+type+"' with value '"+value+"'.");
			step.locatorParams.put(WDScriptFactory.LOCATOR_PARAM, new WDLocator(type, value));
		}catch(Throwable t){
			IndependantLog.debug(debugmsg+ step.type.getClass().getName()+" "+ t.getClass().getSimpleName()+", "+ t.getMessage());
			throw t;
		}
	}
}
