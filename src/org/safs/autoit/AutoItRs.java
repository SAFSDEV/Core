/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.autoit;

import org.safs.IndependantLog;

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
 *   childName="control=theControlID"
 *   childName="id=theControlID"
 *   childName="title=MDI Caption;id=theControlID"    (future)
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
	public final static String CAPTION = "caption";
	/** "title" */
	public final static String TITLE = "title";
	
	/** "text" */
	public final static String TEXT = "text";
	
	/** "control" */  //used same as id
	public final static String CONTROL = "control";
	/** "id" */       //used same as control
	public final static String ID = "id";
	
	private String winRs;
	private String compRs;
	
	private String title;
	private String compTitle; // (future) possible sub/child/mdi Window
	private String text;
	private String control;
	
	
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
		
		// Window Recognition
		String[] sWinRs = cleanWinRs.split(AUTOIT_DELIMITER);
		for (String string : sWinRs) {
			if((string.toLowerCase().startsWith(TITLE))||
			   (string.toLowerCase().startsWith(CAPTION))||
			   (string.toLowerCase().startsWith(TEXT))){
				try { title = string.split("=")[1];}		
				catch(IndexOutOfBoundsException x){;}
			}
		}
		
		// Component/Control Recognition
		String[] sCompRs = cleanCompRs.split(AUTOIT_DELIMITER);
		for (String string : sCompRs) {
			if((string.toLowerCase().startsWith(TITLE))||
			    (string.toLowerCase().startsWith(CAPTION))){
				try { compTitle = string.split("=")[1];}		
				catch(IndexOutOfBoundsException x){;}
			}
			if(string.toLowerCase().startsWith(TEXT)){
				try{ text = string.split("=")[1];}
 			    catch(IndexOutOfBoundsException x){;}
			}
			if((string.toLowerCase().startsWith(CONTROL))||
			   (string.toLowerCase().startsWith(ID))){
				try{ control = string.split("=")[1]; }
 			    catch(IndexOutOfBoundsException x){;}
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
	 * @return specified Window title, or null if never specified.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * (future)
	 * @return specified Child Control title (mid window, etc..), or null if never specified.
	 */
	public String getCompTitle() {
		return compTitle;
	}
	
	/**
	 * @return specified Child Control text, or null if never specified.
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * @return specified Child Control ID, or null if never specified.
	 */
	public String getControl() {
		return control;
	}	
}
