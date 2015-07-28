package org.safs.selenium.util;

import org.safs.selenium.SeleniumGUIUtilities;


/**
 * This class is used to store the Frame's information.<br>
 * In SeleniumGUIUtilities, during the navigation of a xpath, if we meet a prefix frame xpath,<br>
 * we will create a HtmlFrameComp's instance and store it in a map,<br>
 * next time we meet navigate an other xpath starting<br>
 * by the same frame xpath, we don't need to navigate again, just reuse this one,<br>
 * to save time.<br><br>
 * 
 * If Frame's xpath is /HTML/FRAMESET/FRAMESET/FRAME, and its html source is <br>
 * &lt;FRAME NAME="toc1" SRC="somelink.html" TARGET="toc2"&gt; <br>
 * Assume that its parent's url is http://some.site.com <br>
 * This HtmlFrameComp will have following value for its fields:<br>
 * src = "http://some.site.com/somelink.html"<br>
 * locator = "name=toc1"<br>
 * fullXpath = "/HTML/FRAMESET/FRAMESET/FRAME"<br>
 *
 * When navigate a xpath /HTML/FRAMESET/FRAMESET/FRAME/HTML/BODY/TABLE/TR/TD
 * The field childXpath will contain /HTML/BODY/TABLE/TR/TD as value
 * @see SeleniumGUIUtilities#getPrefixingFrame(String xpath)
 * @see SeleniumGUIUtilities#navigateFramesR
 */
public class HtmlFrameComp{
	/**
	 * The frame's attribute src, it is an absolute url
	 */
	private String src = "";
	/**
	 * The locator used by selenium to find tag on html page.
	 */
	private String locator = "";
	/**
	 * The whole xpath representing this frame.
	 */
	private String fullXpath = "";
	/**
	 * The SAFS-robot recoginition string representing this frame
	 */
	private String recognitionString = "";
	/**
	 * This a temporary field, representing this frame's child's xpath<br>
	 * This will be set value during the navigation of a xpath. <br>
	 * see navigateFramesR() and getPrefixingFrame()
	 */
	private String childXpath = "";
	
	public HtmlFrameComp(){}
	
	public HtmlFrameComp(String src, String childXpath){
		this.src = src;
		this.childXpath = childXpath;
	}
	
	public HtmlFrameComp(String src, String locator, String fullXpath, String recognitionString){
		this.src = src;
		this.locator = locator;
		this.fullXpath = fullXpath;
		this.recognitionString = recognitionString;
	}
	
	public HtmlFrameComp(String src, String locator, String fullXpath, String recognitionString, String childXpath){
		this(src,locator,fullXpath,recognitionString);
		this.childXpath = childXpath;
	}
	
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getLocator() {
		return locator;
	}
	public void setLocator(String locator) {
		this.locator = locator;
	}
	public String getFullXpath() {
		return fullXpath;
	}
	public void setFullXpath(String fullXpath) {
		this.fullXpath = fullXpath;
	}
	public String getRecognitionString() {
		return recognitionString;
	}
	public void setRecognitionString(String recognitionString) {
		this.recognitionString = recognitionString;
	}
	public String getChildXpath() {
		return childXpath;
	}
	public void setChildXpath(String childXpath) {
		this.childXpath = childXpath;
	}
}
