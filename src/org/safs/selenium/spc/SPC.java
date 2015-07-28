package org.safs.selenium.spc;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

import org.openqa.selenium.server.RemoteControlConfiguration;
import org.safs.Log;
import org.safs.selenium.SGuiObject;
import org.safs.selenium.SeleniumGUIUtilities;
import org.safs.selenium.SeleniumJavaHook;
import org.safs.selenium.util.HtmlFrameComp;
import org.safs.selenium.util.JavaScriptFunctions;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

/**
 * 
 * AUG 10, 2012		(SBJLWA) Update method highlight(): try to highlight element by id or name.
 */
public class SPC extends SeleniumGUIUtilities{
	
	public static File temp_user_extensions = null;
	private SPCGUI spcGUI;
	private Selenium selenium;
	private BufferedImage ii;
	private boolean runningWindowChecker;
	private boolean runningGetAllElements;
	private String xpathBoundsSeparator = "#";
	private SeleniumJavaHook hook = null;

	public SPC(){
		runningWindowChecker = false;
		runningGetAllElements = false;
	}
	
	public void setGUI(SPCGUI spcgui){
		spcGUI = spcgui;
	}
	
	public void initializeSelenium(String browser, String url){

		try{
			selenium = new DefaultSelenium("localhost", RemoteControlConfiguration.DEFAULT_PORT, browser, url);
			selenium.start();
			selenium.open(url);
			spcGUI.updateWindows(getWindows());
		} catch (Exception e){
			//maybe we just use "*iexplore" as the default browser
			if(browser.equalsIgnoreCase("*iexplore")){
				browser = "*custom C:/Program Files/Internet Explorer/IEXPLORE.EXE";//This can't work in Win7				
			}else{
				browser = "*iexplore";				
			}
			selenium = new DefaultSelenium("localhost", RemoteControlConfiguration.DEFAULT_PORT, browser, url);
			selenium.start();
			selenium.open(url);
			spcGUI.updateWindows(getWindows());
		}
		//selenium.useXpathLibrary("javascript-xpath");

		domParser = initDocumentParser(selenium);
		clearFramesCache();
		
//		String xpathBoundsSeparator = selenium.getEval("SAFSGetBoundsSeparator();");
		String xpathBoundsSeparator = domParser.getBoundsSeparator();
		
		if(xpathBoundsSeparator!=null && !xpathBoundsSeparator.trim().equals("")){
			Log.debug("Selenium SPC get xpathBoundsSeparator: "+xpathBoundsSeparator);
			setXpathBoundsSeparator(xpathBoundsSeparator.trim());
		}
		Log.debug("Using browser "+browser);

	}
	
	public SeleniumJavaHook getHook() {
		return hook;
	}
	public void setHook(SeleniumJavaHook hook) {
		this.hook = hook;
	}
	
	public String getXpathBoundsSeparator() {
		return xpathBoundsSeparator;
	}

	public void setXpathBoundsSeparator(String xpathBoundsSeparator) {
		this.xpathBoundsSeparator = xpathBoundsSeparator;
	}

	public void getAllElements(String title) {
		Log.info("Selenium SPC getAllElements blocked by runningWindowChecker.");
		while(runningWindowChecker);
		Log.info("Selenium SPC getAllElements proceeding...");
		final String t2;
		if(title.equals("")){
			t2 = selenium.getTitle();
		} else {
			t2 = title;
		}
		
		(new Thread(){
			public void run(){
				runningGetAllElements = true;
				Log.info("Selenium SPC getAllElements set runningGetAllElements TRUE...");
				SGuiObject sgo = getWindowIdFromTitle(selenium, t2);
				selectWindow(selenium, sgo.getWindowId(), 1);
//				setWindowFocus(".*"+selenium.getTitle()+".*");
				
				// (CANAGL) which to use?
//				String eval = selenium.getEval("SPCgetSSBounds();");
				//TODO getSSBounds() NOT implemented yet.
				String eval = domParser.getSSBounds();
				Log.info("SPCgetSSBounds="+eval);
				//Rectangle htmlbounds = getComponentBounds("//HTML[1]", selenium);
				//Log.info("SPC.getComponentBounds="+ htmlbounds);
				
				String [] bounds = null;
				try{ 
					bounds = eval.split(SPC.this.getXpathBoundsSeparator());
					int x = Integer.parseInt(bounds[0]);
					int y = Integer.parseInt(bounds[1]);
					int w = Integer.parseInt(bounds[2]);
					int h = Integer.parseInt(bounds[3]);
					try {
						Robot robot = new Robot();
						ii = robot.createScreenCapture(new Rectangle(x,y,w,h));						
					} catch (AWTException e) {
						Log.debug("Selenium SPC Robot error,", e);
					}
				}catch(Exception x){
					Log.debug("Selenium SPC error calculating bounds,",x);
				}
				String [] data;
				try{
					Log.info("Selenium SPC calling SPCgetAllElements()...");
//					data = selenium.getEval("SPCgetAllElements();").split(";");
					data = domParser.getAllElements();
				}catch(Exception e){
					Log.debug("SPCgetAllElements Exception:", e);
					runningGetAllElements = false;
					return;
				}
				runningGetAllElements = false;
				Log.info("Selenium SPC getAllElements set runningGetAllElements FALSE...");
//				if(!data[0].equalsIgnoreCase("INTERRUPTED")){
				if(data.length>0 && !data[data.length-1].equalsIgnoreCase("INTERRUPTED")){
					// CANAGL JAN 27, 2015, not supporting FRAMES here at this time?
					spcGUI.updateData(null, data);
				}else{
					Log.debug("SPC has been interrupted.");
				}
				
				selenium.windowFocus();
				selenium.windowMaximize();
			}
		}).start();
	}
	
	public String getXpath(String title, String rec){
		String id = getWindowIdFromTitle(selenium, title).getWindowId();
		selectWindow(selenium, id, 1);
		SGuiObject guiObj = getGuiObject(rec,selenium,id);
		String xpath = "";
		if(guiObj!=null)
			xpath = guiObj.getLocator();
		return xpath;
	}
	
	public BufferedImage getCurrentPreview(){
		return ii;
	}

	public void stopSelenium() {
		if(selenium != null){
			selenium.stop();
			selenium = null;
		}
	}
	public void stopJavaHOOK(){
		if(hook!=null){
			hook.stopJavaHOOK();
			hook = null;
		}
	}
	
	public String [] getWindows(){
		Log.info("Selenium SPC getWindows blocked by runningGetAllElements.");
		while(runningGetAllElements);
		Log.info("Selenium SPC getWindows now proceeding...");
		runningWindowChecker=true;
		String [] titles = new String[0];
		try{
			if(selenium!=null){
				String [] windowNames = null;
				selectWindow(selenium, null, 1);
				windowNames = getAllWindowNames(selenium);
				Log.info("Selenium SPC windowNames="+ windowNames);
				titles = new String[windowNames.length];
				if(windowNames != null && windowNames.length > 0){
					boolean windowLoaded = false;
					String window = null;
					int timeout = 0;
					for(int i = 0; i < windowNames.length; i++){
						window = windowNames[i].trim();
						windowLoaded = false;
						timeout = 0;
						// try every second for up to 90 seconds
						while((!windowLoaded)&&(timeout < 90)){
							timeout++;
							windowLoaded = selectWindow(selenium, window, 1);						
						}
						if(windowLoaded){
							titles[i] = selenium.getTitle();
						}
					}
				}
			}
		}catch(Exception x){
			Log.debug("SPC getWindows Exception:", x);
		}
		runningWindowChecker=false;
		return titles;
	}
	
	/**
	 * Retrieve the Element Info, which is made up of:
	 * <p><ol>
	 * <li>compBounds (relative to 0,0 of client area?), or ""
	 * <li>calculated SAFS recognition string (not xpath), or ""
	 * <li>the HTML making up the element (parent.innerHTML), or "None"
	 * <li>the innerHTML (if any) of the element itself, or "None"
	 * </ol>
	 * <p>
	 * @param xpath
	 * @return
	 */
	public String [] getElementInfo(String xpath){
		HtmlFrameComp precedingFrame = null;
		String xpathEnd = "";
		String frameURL = null;
		
		try{
			precedingFrame = navigateFrames(domParser.getUrl(),xpath,selenium);
			xpathEnd = precedingFrame.getChildXpath();
			frameURL = precedingFrame.getSrc();
			
			if(frameURL==null || frameURL.equals("")){
				//if no url is returned, we assign the root url to it.
				frameURL = domParser.getUrl();
			}
		} catch(SeleniumException e){
			return null;
		}
		//TODO need to convert the related javascript to JAVA
		return selenium.getEval("SPCgetElementInfo('"+xpathEnd+"');").split(";;;;");
	}
	
	//This method will return Recognition String without the component name
	public String getRobotRecognition(String xpath){
		return domParser.getRobotRecognition(xpath,false);
	}
	//This method will return a SPCTreeNode contain "Recognition String", "Node's id", "Node's name"
	public SPCTreeNode getRobotRecognitionNode(String xpath){
		return domParser.getRobotRecognitionNode(xpath,false);
	}

	//This method will return Recognition String with the component name
	public String getRobotRecognitionWithName(String xpath){
		Log.debug("SELENIUM__SPC.getRobotRecognitionWithName(): "+xpath);
		return domParser.getRobotRecognition(xpath,true);
	}
	
	public void cancelSearch() {
		try{
//			selenium.getEval("interrupt = true;");
			domParser.setInterrupt(true);
		}catch(Exception e){
			Log.warn("Exception when interrupting search.");
		}
	}

	public boolean highlight(SPCTreeNode node){
		String xpath = SeleniumGUIUtilities.normalizeXPath(node.xpath);
		String scriptCommand = null;
		boolean highlighted = true;
		
		selenium.windowFocus();
		try{
			//Use selenium API to highlight a component, the highlight just flash for a moment
			//Then the highlight disappear, NOT good.
//			selenium.focus(xpath);
//			selenium.highlight(xpath);
			
			//We can change the hmtl page style to draw rectangle for highlight, BUT
			//It seems that there is a problem, in the injected javascript,
			//the document is the selenium playback web page, not the web page to be tested.
			//Found some words on selenium reference: "Remember to use window object in case of DOM expressions as by default selenium window is referred to, not the test window."
			//So I added the window before document as window.document
			//if I pass javascript code through method getEval() of selenium like following
//			selenium.getEval("window.document.body.style.border='5px solid red';"); //it really DO work on the test web page;
			//While if call injected javascript, it still fail, not know why ????
			//selenium.getEval(" highlight('"+xpath+"');");
			
			//So I decide to get the function definition as a string, and pass it directly to selenium through API getEval()
			scriptCommand = JavaScriptFunctions.getSAFSgetElementFromXpathFunction();
			scriptCommand += JavaScriptFunctions.getHighlightFunction();
			
			//JSAFSBefore we highlight the component via JAVASCRIPT, we need to navigate to the right frame;
			//and we need get the XPATH without frames
			HtmlFrameComp precedingFrame = navigateFrames(domParser.getUrl(), xpath, selenium);
			String lastPartXpath = precedingFrame.getChildXpath();
			
//			selenium.getEval(JavaScriptFunctions.setJavaScriptLogLevel(JavaScriptFunctions.LOG_LEVEL_INFO));
			try{			
				Log.debug("Highlight by xpath: '"+lastPartXpath+"'");
				selenium.getEval(scriptCommand+" highlight('"+lastPartXpath+"');");
			}catch(Exception e){
				try{
					Log.debug("Highlight by id: '"+node.getId()+"'");
					selenium.getEval(scriptCommand+" highlight('"+node.getId()+"');");
				}catch(Exception e1){
					Log.debug("Highlight by name: '"+node.getName()+"'");
					selenium.getEval(scriptCommand+" highlight('"+node.getName()+"');");					
				}
			}
		}catch(Exception e){
			Log.warn(e.getMessage());
			highlighted=false;
		}
		
		return highlighted;
	}

}
