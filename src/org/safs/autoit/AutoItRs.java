package org.safs.autoit;

import org.safs.IndependantLog;
public class AutoItRs {

	public static final String AUTOIT_PREFIX = ":autoit:";
    public static final String AUTOIT_DELIMITER=";";
	
	public final static String TITLE = "title";
	public final static String TEXT = "text";
	public final static String CONTROL = "control";
	
	private String winRs;
	private String compRs;
	
	private String title;
	private String text;
	private String control;
	
	
	public AutoItRs(String winRS, String compRS){
		this.winRs = winRS;
		this.compRs = compRS;
		splitRS();
	}
	
	protected void splitRS() {
		// remove autoit: from string
		String cleanWinRs = winRs.replace(AUTOIT_PREFIX,"");
		String cleanCompRs = compRs.replace(AUTOIT_PREFIX,"");
		
		String[] sWinRs = cleanWinRs.split(AUTOIT_DELIMITER);
		for (String string : sWinRs) {
			if(string.startsWith(TITLE))
				title = string.split("=")[1];		
		}
		String[] sCompRs = cleanCompRs.split(AUTOIT_DELIMITER);
		for (String string : sCompRs) {
			if(string.startsWith(TEXT))
				text = string.split("=")[1];
			if(string.startsWith(CONTROL))
				control = string.split("=")[1];
		}	
	}
	
	/**
	 * Attempt to determine if recognition string is for autoit testing
	 * @param recognition -- recognition, usually from App Map
	 * @return true if it contains elements of autoit testing recognition
	 */
	public static boolean isAutoitBasedRecognition(String recognition){
		IndependantLog.debug("AutoIt Rec " + recognition);
		try{
			String lcrec = recognition.toLowerCase();
			if(lcrec.startsWith(AUTOIT_PREFIX)) return true;			
		}catch(Exception x) {}
		return false;
	}	
	
	public String getTitle() {
		return title;
	}
	
	public String getText() {
		return text;
	}
	
	public String getControl() {
		return control;
	}	
}
