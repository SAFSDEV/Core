/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 * 
 *  SEP 10, 2014    (sbjlwa) Initial release.
 *  OCT 16, 2015    (sbjlwa) Refector to create IOperable object properly.
 */
package org.safs.selenium.webdriver.lib;

import java.util.Arrays;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.util.JavaScriptFunctions.SAP;
import org.safs.selenium.webdriver.lib.model.EmbeddedObject;
import org.safs.selenium.webdriver.lib.model.IOperable;

/** 
 * A library class to handle different specific ScrollBar.
 */
public class ScrollBar extends Component{
	
	public static int TYPE_SCROLLBAR_HORIZONTAL = 0;
	public static int TYPE_SCROLLBAR_VERTICAL = 1;
	/** Click the up/down/right/left button a number of times to simulate a page movement. */
	public static final int STEPS_OF_A_PAGE = 7;
	
	IScrollable scrollable = null;
	
	/**
	 * @param scrollbar	WebElement combo box object, for example an HTML tag &lt;select&gt;.
	 */
	public ScrollBar(WebElement scrollbar) throws SeleniumPlusException{
		initialize(scrollbar);
	}

	protected void castOperable(){
		super.castOperable();
		scrollable = (IScrollable) anOperableObject;
	}
	
	protected IOperable createSAPOperable(){
		String debugmsg = StringUtils.debugmsg(false);
		IScrollable operable = null;
		try{ operable = new SapScrollable_ScrollBar(this);}catch(SeleniumPlusException se0){
			IndependantLog.debug(debugmsg+" Cannot create IScrollable of "+Arrays.toString(SapScrollable_ScrollBar.supportedClazzes));				
		}
		return operable;
	}
	
	public void page(int scrollbarType, int steps) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "page");
		
		try{
			scrollable.page(scrollbarType, steps);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "page", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}
	
	public void scroll(int scrollbarType, int steps) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "scroll");
		
		try{
			scrollable.scroll(scrollbarType, steps);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "scroll", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}
	
	public static interface IScrollable extends IOperable{
		/**
		 * Try to scroll a page.<br>
		 * If the scrollbar is vertial, then scroll up or down.<br>
		 * If the scrollbar is horizontal, then scroll left or right.<br>
		 * @param scrollbarType int, type of the scrollbar, horizontal or verical.
		 * @param pages int, number of pages to scroll.
		 * @throws SeleniumPlusException
		 */
		public void page(int scrollbarType, int pages) throws SeleniumPlusException;

		/**
		 * Try to scroll number of steps.<br>
		 * If the scrollbar is vertial, then scroll up or down.<br>
		 * If the scrollbar is horizontal, then scroll left or right.<br>
		 * @param scrollbarType int, type of the scrollbar, horizontal or verical.
		 * @param steps int, number of stpes to scroll.
		 * @throws SeleniumPlusException
		 */
		public void scroll(int scrollbarType, int steps) throws SeleniumPlusException;
	}
	
	static abstract class AbstractScrollable extends EmbeddedObject implements IScrollable{
		
		public void clearCache(){
			String methodName = StringUtils.getCurrentMethodName(true);
			IndependantLog.debug(methodName+" has not been implemented.");
		}
		
		public AbstractScrollable(Component component)throws SeleniumPlusException {
			super(component);
		}

	}
	
	static abstract class SapScrollbale extends AbstractScrollable{

		public SapScrollbale(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.Component.Supportable#isSupported(org.openqa.selenium.WebElement)
		 */
		public boolean isSupported(WebElement element){
			return WDLibrary.SAP.isSupported(element, getSupportedClassNames());
		}

	}
	
	static abstract class DojoScrollbale extends AbstractScrollable{

		public DojoScrollbale(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.Component.Supportable#isSupported(org.openqa.selenium.WebElement)
		 */
		public boolean isSupported(WebElement element){
			return WDLibrary.DOJO.isSupported(element, getSupportedClassNames());
		}
	}
	
	static abstract class HtmlScrollable extends AbstractScrollable{

		public HtmlScrollable(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.Component.Supportable#isSupported(org.openqa.selenium.WebElement)
		 */
		public boolean isSupported(WebElement element){
			return WDLibrary.HTML.isSupported(element, getSupportedClassNames());
		}
		
	}
	
	protected static class SapScrollable_ScrollBar extends SapScrollbale{
		public static final String CLASS_NAME_SCROLLBAR = "sap.ui.core.ScrollBar";
		public static final String[] supportedClazzes = {CLASS_NAME_SCROLLBAR};
		
		public SapScrollable_ScrollBar(Component component) throws SeleniumPlusException {
			super(component);
		}
		
		public String[] getSupportedClassNames(){
			return supportedClazzes;
		}
		
		public void scroll(int scrollbarType, int steps)  throws SeleniumPlusException{
			String debugmsg = StringUtils.debugmsg(getClass(), "scroll");
			
			StringBuffer jsScript = new StringBuffer();
			jsScript.append(SAP.sap_ui_core_ScrollBar_scroll(true));
			jsScript.append("sap_ui_core_ScrollBar_scroll(arguments[0],arguments[1]);");

			try {
				WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), steps);
				return;
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			String msg = (scrollbarType==TYPE_SCROLLBAR_HORIZONTAL? "horizontally":"vertically");
			throw new SeleniumPlusException("Fail to scroll "+msg+" '"+steps+"' steps.");
		}

		public void page(int scrollbarType, int pages) throws SeleniumPlusException{
			String debugmsg = StringUtils.debugmsg(getClass(), "page");
			
			StringBuffer jsScript = new StringBuffer();
			jsScript.append(SAP.sap_ui_core_ScrollBar_page(true));
			jsScript.append("sap_ui_core_ScrollBar_page(arguments[0], arguments[1]);");

			try {
				WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), pages);
				return;
			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
			}
			
			String msg = (scrollbarType==TYPE_SCROLLBAR_HORIZONTAL? "horizontally":"vertically");
			throw new SeleniumPlusException("Fail to scroll "+msg+" '"+pages+"' pages.");
		}

	}

}

