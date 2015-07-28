/**
 * 
 */
package org.safs.selenium.util;

import org.openqa.selenium.WebElement;

/**
 * @author Carl Nagle
 *
 */
public class MouseEvent {

	/** String */
	public String listenerID = null;
	/** int / Long  1=CAPTURING, 2=AT TARGET, 3=BUBBLING */
	public int EVENT_PHASE = 0;
	/** String ex: click */
	public String EVENT_TYPE = null;
	/** WebElement / EventTarget / Selenium RemoteWebElement */
	public WebElement EVENT_TARGET = null;
	/** WebElement / EventTarget / Selenium RemoteWebElement */
	public WebElement EVENT_CURRENTTARGET = null;
	/** long / Long */
	public long EVENT_TIMESTAMP = 0;
	/** Object / null */
	public Object EVENT_VIEW = null;
	/** long / Long */
	public long EVENT_DETAIL = -1;
	/** long / Long */
	public long EVENT_SCREENX = 0;
	/** long / Long */
	public long EVENT_SCREENY = 0;
	/** long / Long */
	public long EVENT_CLIENTX = 0;
	/** long / Long */
	public long EVENT_CLIENTY = 0;
	/** boolean / Boolean */
	public boolean EVENT_CTRLKEY = false;
	/** boolean / Boolean */
	public boolean EVENT_SHIFTKEY = false;
	/** boolean / Boolean */
	public boolean EVENT_ALTKEY = false;
	/** boolean / Boolean */
	public boolean EVENT_METAKEY = false;
	/** int / Long 0=left, 1=middle, 2=right (right-handed) */
	public int EVENT_BUTTON = -1;
	/** WebElement / EventTarget / Selenium RemoteWebElement */
	public WebElement EVENT_RELATEDTARGET = null;
	
	private MouseEvent(){	super(); /* hide */  }
	public MouseEvent(String listenerID){
		this();
		this.listenerID = listenerID;
	}
	public String toString(){
		String n = "\n";
		return "listener : "+ listenerID          +n+
	           "timeStamp: "+ EVENT_TIMESTAMP     +n+
			   "Target   : "+ EVENT_TARGET        +n+
			   "Current  : "+ EVENT_CURRENTTARGET +n+
			   "Related  : "+ EVENT_RELATEDTARGET +n+
	           "eventPhase : "+ EVENT_PHASE       +n+
	           "eventType  : "+ EVENT_TYPE        +n+
	           "eventView  : "+ EVENT_VIEW        +n+
	           "eventDetail: "+ EVENT_DETAIL      +n+
	           "eventScrn_X: "+ EVENT_SCREENX     +n+
	           "eventScrn_Y: "+ EVENT_SCREENY     +n+
	           "eventClnt_X: "+ EVENT_CLIENTX     +n+
	           "eventClnt_Y: "+ EVENT_CLIENTY     +n+
	           "eventCtrlKY: "+ EVENT_CTRLKEY     +n+
	           "eventShftKY: "+ EVENT_SHIFTKEY    +n+
	           "eventAltKEY: "+ EVENT_ALTKEY      +n+
	           "eventMetaKY: "+ EVENT_METAKEY     +n+
	           "eventButton: "+ EVENT_BUTTON      +n;
	}
}
