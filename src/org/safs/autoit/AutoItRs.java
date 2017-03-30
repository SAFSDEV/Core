/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 23, 2015 SBJLWA Modify value of some constant like 'CLASSNAMENN' to lower-case.
 */
package org.safs.autoit;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;

import autoitx4java.AutoItX;

/**
 * Class to handle recognition string parsing for the AutoIt engine.
 * <p>
 * Currently supported:
 * <pre>
 * Window Recognition:
 *
 *   windowName=":AUTOIT:title=Window Title"
 *   windowName=":autoit:caption=Window Title"
 *   windowName=":AutoIt:text=Window Title"
 *
 * Note: the case-insensitive ":autoit:" prefix MUST appear in Window RS and will be removed as needed.
 *
 * Child Control Recognition:
 *
 *   childName=":AutoIt:text=Some Text"
 *   childName="text=Some Text"
 *   childName="id=ID"
 *   childName="class=class name"
 *   childName="id=some id;class=class name"
 *   childName="class=class name;instance=8"
 *   childName="caption=MDI Caption;id=theControlID"  (future)
 *
 * Note: the case-insensitive ":autoit:" prefix CAN appear in Control RS and will be removed if present.
 *  </pre>
 * @author dharmesh
 */
public class AutoItRs {

	/** ":autoit:" */
	public static final String AUTOIT_PREFIX = ":autoit:";
	/** ";" */
    public static final String AUTOIT_DELIMITER=";";

	/** "caption" */
	public final static String CAPTION = "caption=";
	/** "title" */
	public final static String TITLE = "title=";


	/** "id" */       //Component ID
	public final static String ID = "id=";
	/** "class" */    //Component CLASS
	public final static String CLASS = "class=";
	/** "text" */     //Component TEXT
	public final static String TEXT = "text=";
	/** "instance" */    //Component INSTANCE
	public final static String INSTANCE = "instance=";
	/** "classnamenn" */   //Component CLASSNAMENN
	public final static String CLASSNAMENN = "classnamenn=";
	/** "name" */        //Component NAME
	public final static String NAME = "name=";

	private String winRs;
	private String compRs;

	private String title; // windows title

	private String compTitle; // (future) possible sub/child/mdi Window
	private String text;
	private String id;
	private String instance;
	private String classnamenn;
	private String classname;
	private String name;

	public AutoItRs(String winRS, String compRS){
		this.winRs = winRS;
		this.compRs = compRS;
		splitRS();
	}

	/**
	 * Called internally.
	 * Parse the provided recognition strings into needed Window and Child Control elements.
	 */
	private void splitRS() {
		// remove autoit: from string
		String cleanWinRs = null;
		String cleanCompRs = null;

		try{ cleanWinRs = isAutoitBasedRecognition(winRs) ? winRs.substring(AUTOIT_PREFIX.length()) : winRs; }
		catch(IndexOutOfBoundsException x){
			cleanWinRs = "";
		}
		try{ cleanCompRs = isAutoitBasedRecognition(compRs) ? compRs.substring(AUTOIT_PREFIX.length()) : compRs; }
		catch(IndexOutOfBoundsException x){
			cleanCompRs = "";
		}

		String lowercaseStr = null;
		String value = null;
		// Window Recognition
		String[] sWinRs = cleanWinRs.split(AUTOIT_DELIMITER);
		for (String winrs : sWinRs) {
			lowercaseStr = winrs.toLowerCase();
			if(lowercaseStr.startsWith(TITLE)||
			   lowercaseStr.startsWith(CAPTION)){
				try { title = winrs.split("=")[1];}
				catch(IndexOutOfBoundsException x){;}
			}
		}

		// Component/Control Recognition
		String[] sCompRs = cleanCompRs.split(AUTOIT_DELIMITER);
		for (String comprs : sCompRs) {
			lowercaseStr = comprs.toLowerCase();
			try { value = comprs.split("=")[1];}
			catch(IndexOutOfBoundsException x){;}

			if(lowercaseStr.startsWith(TITLE)||
			   lowercaseStr.startsWith(CAPTION)){
				compTitle = value;
			}else if(lowercaseStr.startsWith(TEXT)){
				text = value;
			}else if(lowercaseStr.startsWith(ID)){
			   	id = value;
			}else if(lowercaseStr.startsWith(CLASSNAMENN)){
			   	classnamenn = value;
			}else if(lowercaseStr.startsWith(CLASS)){
			   	classname = value;
			}else if(lowercaseStr.startsWith(NAME)){
			   	name = value;
			}else if(lowercaseStr.startsWith(INSTANCE)){
			   	instance = value;
			}else{
				IndependantLog.warn("Unknown recogniztion string '"+comprs+"'");
			}
		}
	}

	/**
	 * Attempt to determine if recognition string is for autoit testing
	 * @param recognition -- recognition, usually from App Map
	 * @return true if it contains elements of autoit testing recognition.
	 * Primarily, that is startsWith the :autoit: prefix.
	 * @see #AUTOIT_PREFIX
	 */
	public static boolean isAutoitBasedRecognition(String recognition){
		IndependantLog.debug("AutoIt Rec " + recognition);
		try{
			String lcrec = recognition.toLowerCase();
			if(lcrec.startsWith(AUTOIT_PREFIX)) return true;
		}catch(Exception x) {}
		return false;
	}

	/**
	 * Get Windows AutoIt format recognition string.
	 * @return Autoit object locator.
	 */
	public String getWindowsRS(){
		IndependantLog.debug("AutoItRS.getWindowsRS(): " + title);
		return title;
	}

	/**
	 * If the window title is not complete, call this method to complete.
	 * @param it The AutoIt instance.
	 */
	public void normalize(AutoItX it){
		if(it!=null && StringUtils.isValid(title)){
			title = it.winGetTitle(title);
		}
	}

	/**
	 * @return boolean true if the recognition string represents a window.
	 * @throws SAFSException if the window's recognition string is not valid.
	 */
	public boolean isWindow() throws SAFSException{
		if(StringUtils.isValid(winRs)){
			return winRs.equals(compRs);
		}
		throw new SAFSException("The window's recognition string is not valid!");
	}

	/**
	 * Get Component AutoIt format recognition string.
	 * @return - AutoIt object locator.
	 */
	public String getComponentRS(){

		String autoitrs="";
		if (text != null) autoitrs = autoitrs + ";TEXT:" + text;
		if (id != null) autoitrs = autoitrs + ";ID:" + id;
		if (instance != null) autoitrs = autoitrs + ";INSTANCE:" + instance;
		if (classnamenn != null) autoitrs = autoitrs + ";CLASSNAMENN:" + classnamenn;
		if (classname != null) autoitrs = autoitrs + ";CLASS:" + classname;
		if (name != null) autoitrs = autoitrs + ";NAME:" + name;

		String a = autoitrs.replaceFirst(";", ""); // remove fist ";" always
		String b = "[" + a + "]"; // add autoit format
		IndependantLog.debug("AutoItRS.getComponentRS(): " + b);
		return b;
	}

}
