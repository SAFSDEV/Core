/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent;

import org.safs.*;
import org.safs.jvmagent.LocalServer;
import java.util.*;


/**
 * Part of the required implementation to support SAFS "standard" recognition strings.
 * External users would normally not use this class directly.<br>
 * Consequently, the API and associated data is subject to change without notice.
 * <p>
 * Extends org.safs.GuiClassData to use org.safs.jvmagent.LocalServer specific mechanisms for 
 * identifying class hierarchy in Objects.
 * 
 * @author  Carl Nagle
 * @since   JUN 28, 2007
 * @see org.safs.jvmagent.LocalServer 
 *
 * <br> FEB 13, 2009    (JunwuMa)   Modify getMappedClassType(), fix S0561499 about how to judge if a Html.SELECT is ComboBox. 
 *                                  See the same behavior about Html.SELECT in org.safs.rational.RGuiClassData.getMappedClassType().

 **/
public class LocalServerGuiClassData extends GuiClassData{

	LocalServer server = null;
	
	/**
	 * Constructor providing LocalServer access without DDGUIUtilities.
	 * This might be used for things like ProcessContainer.
	 * @see org.safs.abbot.ProcessContainer
	 */
	public LocalServerGuiClassData(LocalServer localserver){
		super();
		server = localserver;
	}
	
	/**
	 * Retrieves the class Type we have stored for the provided class name (if any).
	 * We will cycle through all possible superclasses (except Object) looking for 
	 * a match to a known superclass.  For example, we want to find out that the 
	 * input swingapp.SwingAppFrame--which we don't know about--is actually a 
	 * subclass of javax.swing.JFrame--something we know how to handle.
	 * <p>
	 * Overrides the superclass to handle local object proxies.
	 * 
	 * 
	 * @param classname the actual classname sought as a known class type.  This is 
	 *                  really just the name of the proxied class as provided in 
	 *                  theObject.
	 * 
	 * @param theObject the TestObject proxy to evaluate for class hierarchy.
	 * 
	 * @return the class Type (i.e. JavaPanel) for the provided class name (i.e. javax.swing.JPanel).
	 *          <CODE>null</CODE> if no mapped type is found.
	 */
	public String getMappedClassType(String classname, Object anObject){
		
		if(classname == null){
			Log.info("LSGCD classname: null, returning null mapped class type.");
			return null;
		}
		String typeclass = classmap().getProperty(classname, null);		
		if (server == null) return null;				
		if (typeclass == null) {
			
			try{ 
                Log.info("LSGCD: resolving classname: "+classname+", typeclass: "+typeclass);
                if (classname.toLowerCase().indexOf("html.")>=0) {
                    return classname;
                }
				String[] classes = server.getSuperClassNames(anObject);
				for(int sindex = classes.length-1; ((sindex >= 0)&&(typeclass == null)); sindex--){
					typeclass = classmap().getProperty(classes[sindex], null);
				}
			}
			catch(Exception ex) { 				
				Log.info("LSGCD.getMappedClassType Exception: "+ ex.getClass().getSimpleName(), ex);
			}
		}
		// special processing for various classnames
		// *** not sure if Abbot implementation needs this yet ***
		else if (classname.equalsIgnoreCase("HTML.SELECT")){
            String size = "";
            String multiple = "";
			Log.info("LSGCD.getMappedClassType checking ComboBox/ListBox flags..."+ classname);
			try{multiple = server.getProperty(anObject, "multiple");}
			catch(Exception x){;}finally{if (multiple==null) multiple="";}
			try{size = server.getProperty(anObject, "size");}
			catch(Exception x){;}finally{if (size==null) size="";}
			if (multiple.equalsIgnoreCase("true")) {
				typeclass = "ListBox";}
			else if ((size.trim().equalsIgnoreCase("1"))||(size.equals(""))||(size.trim().equals("0"))) {
				typeclass = "ComboBox";}
		}		
        Log.info("LSGCD:classname: "+classname+", typeclass: "+typeclass);
		return typeclass;
	}		
}

