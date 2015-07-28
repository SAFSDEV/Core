package org.safs.selenium.spc;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.selenium.DocumentParser;
import org.safs.selenium.SGuiObject;
import org.safs.selenium.SeleniumGUIUtilities;
import org.safs.selenium.SeleniumJavaHook;
import org.safs.selenium.util.HtmlFrameComp;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.webdriver.EmbeddedSeleniumHookDriver;
import org.safs.selenium.webdriver.SeleniumHook;
import org.safs.selenium.webdriver.SeleniumPlus;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.RemoteDriver;
import org.safs.selenium.webdriver.lib.RemoteDriver.SessionInfo;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.text.GENKEYS;
import org.safs.tools.drivers.EmbeddedHookDriver;

import com.gargoylesoftware.htmlunit.util.StringUtils;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

/**
 * 
 * APR 03, 2014		(Carl Nagle) Initial Release
 */
public class WDSPC extends SeleniumPlus{
	
	public static File temp_user_extensions = null;
	private WDSPCGUI spcGUI;
	private WebDriverGUIUtilities utils;
	private WebDriver selenium;
	private BufferedImage ii;
	private boolean runningWindowChecker;
	private boolean runningGetAllElements;
	public String xpathBoundsSeparator = "#";
	private SeleniumHook standardhook = null;
	private EmbeddedSeleniumHookDriver embeddedhook = null;
	
	public String BROWSER_ID_ROOT = "WDSPC";
	public int BROWSER_TIMEOUT = 30;
	public boolean USE_REMOTE = true;
	public HashMap BROWSER_PARMS = new HashMap();

	public WDSPC(){
		super();
		_isSPC = true;		
	}
	
	public WDSPC(WebDriverGUIUtilities utils){
		this();
		runningWindowChecker = false;
		runningGetAllElements = false;
		this.utils = utils;
	}
	
	public void setGUI(WDSPCGUI spcgui){
		this.spcGUI = spcgui;
	}
	
	public String getFrameRS(){ return spcGUI.getFrameRS(); }
	
	public String appendFrameRS(String rs){ return spcGUI.appendFrameRS(rs); }
	
	public WebDriverGUIUtilities getUtils(){ return utils; }
	
	private String getUniqueDriverID(){
		String id = BROWSER_ID_ROOT;
		int index=0;
		try{
			List<SessionInfo> list = RemoteDriver.getSessionsFromFile();
			Log.info("WDSPC getUniqueDriverID found "+list.size()+" existing remote sessions to process.");
			boolean matched = true;
			while(matched){
				matched = false;
				for(SessionInfo info: list){
					if(id.equals(info.id)) {
						matched = true;
						break;
					}
				}
				if(matched) id = BROWSER_ID_ROOT+String.valueOf(++index);
			}
		}catch(Exception x){
			Log.error("WDSPC getUniqueDriverID "+ x.getClass().getSimpleName()+": "+x.getMessage(), x);
		}
		return id;
	}
	
	/**
	 * 
	 * @param browser
	 * @param url
	 */
	public void initializeSelenium(String browser, String url) throws SAFSException {
		String id = getUniqueDriverID();
		try {			
			WDLibrary.startBrowser(browser, url, id, BROWSER_TIMEOUT, USE_REMOTE, BROWSER_PARMS);
		}
		catch(Throwable th){
			String thmsg = 	"WDSPC initial session start() error: "+ th.getMessage();
			if(USE_REMOTE){
				Log.info("WDSPC attempting to (re)start RemoteServer.");
				//if(WebDriverGUIUtilities.startRemoteServer()){
				if(WebDriverGUIUtilities.startRemoteServer(Runner.jsafs().getProjectRootDir())){
					try{
						WDLibrary.startBrowser(browser, url, id, BROWSER_TIMEOUT, USE_REMOTE, BROWSER_PARMS);
					}catch(Throwable th2){
						thmsg = "WDSPC second session start() error:"+ th2.getMessage();
						System.err.println(thmsg);
						Log.error(thmsg);
						throw new SAFSException(thmsg);
					}
				}
			}else{
				System.err.println(thmsg);
				Log.error(thmsg);
				throw new SAFSException(thmsg);
			}
		}
		Log.debug("Initialized browser: "+browser+" with ID: "+ id);
		selenium = WDLibrary.getBrowserWithID(id);
		spcGUI.updateWindows(getWindows());
	}
	
	/**
	 * Will be a WebDriver-based SeleniumHook, or an EmbeddedSeleniumHookDriver, or null.
	 * @return a WebDriver-based SeleniumHook, or an EmbeddedSeleniumHookDriver, or null.
	 */
	public Object getHook() {
		return standardhook != null ? standardhook: embeddedhook;
	}
	public void setHook(SeleniumHook hook) {
		standardhook = hook;
	}
	public void setHook(EmbeddedSeleniumHookDriver hook) {
		embeddedhook = hook;
	}
	
	public String getXpathBoundsSeparator() {
		return xpathBoundsSeparator;
	}

	public void setXpathBoundsSeparator(String xpathBoundsSeparator) {
		this.xpathBoundsSeparator = xpathBoundsSeparator;
	}

	public void getAllElements(String title, final String frameRS) {
		Log.info("WDSPC getAllElements blocked by runningWindowChecker...");
		while(runningWindowChecker);
		Log.info("WDSPC getAllElements proceeding...");
		final String t2;
		if(title.equals("")){
			t2 = selenium.getTitle();// what if selenium is null? can it be?
		} else {
			t2 = title;
		}
		stopthread = false;
		(new Thread(){
			public void run(){
				runningGetAllElements = true;
				Log.info("WDSPC getAllElements set runningGetAllElements TRUE...");
				try{ selenium = WDLibrary.getBrowserWithTitle(t2);}
				catch(SeleniumPlusException ignore){
					Log.info("WDSPC getAllElements proceeding with existing WebDriver due to: "+ignore.getMessage());
				}
				SGuiObject sgo = new SGuiObject("","", WDLibrary.getIDForWebDriver(selenium), false);
				
				List<WebElement> data = null;
				try{
					TargetLocator locator = selenium.switchTo();
					locator.defaultContent();
					if(frameRS==null || frameRS.trim().isEmpty()){
						//clear the 'last frame', so that the component will be searched on default html document
						SearchObject.setLastFrame(null);
					}else{
						//String[] st = StringUtils.getTokenArray(frameRS, SearchObject.childSeparator, SearchObject.escapeChar);
						SearchObject.switchFrame(selenium, frameRS);
					}
					
					Log.info("WDSPC calling WebDriver findElements()...");
					data = selenium.findElements(By.xpath("//*"));
				}catch(Exception e){
					Log.debug("WDSPC findElements() Exception:", e);
					runningGetAllElements = false;
					return;
				}
				
				if(!data.isEmpty()){
					Log.info("WDSPC getAllElements found "+ data.size()+" elements. Attempting XPaths...");
					// get the xpaths to each element
					try{
						utils.setWDTimeoutLock();
						utils.setWDTimeout(0);
						List<String> paths = new ArrayList();
						List<WebElement> retained = new ArrayList();
						WebElement e = null;
						String t = null;
						String info = null;
						SearchObject.resetXPathObjectCache();
						for(int i=0;i<data.size() && !stopthread ;i++){
							try{
								e = data.get(i);
								t = e.getTagName();
								if(!SearchObject.isIgnoredNonUITag(t)){									
									if(spcGUI.useVisibleOnly()&& !e.isDisplayed()) continue;									
									info = SearchObject.generateGenericXPath(e);
									if(t.equalsIgnoreCase(SearchObject.TAG_FRAME) ||
									   t.equalsIgnoreCase(SearchObject.TAG_IFRAME)){
										String frameRS = SearchObject.generateSAFSFrameRecognition(info);
										if(spcGUI.addFrameRS(frameRS)){
											spcGUI.addFrameRSToCache(e, info, (String)spcGUI.jcb_curwindows.getSelectedItem());
										}
									}
									paths.add(SearchObject.generateFullGenericXPath(e));
									spcGUI.setStatus(info, null);									
									retained.add(e);
								}
							}catch(Exception x){Log.debug("WDSPC getAllElements ignoring "+ x.getClass().getSimpleName()+": "+x.getMessage(), x);}
						}
						SearchObject.resetXPathObjectCache();
						
						// TODO handle actual Frames
						spcGUI.updateData(null, retained, paths);
						
						runningGetAllElements = false;
						Log.info("WDSPC getAllElements set runningGetAllElements FALSE...");
					}catch(Throwable t){
						Log.info("WDSPC getAllElements "+ t.getClass().getSimpleName()+": "+t.getMessage(), t);
					}finally{
						utils.resetWDTimeout();
						utils.resetWDTimeoutLock();
					}
				}else{
					runningGetAllElements = false;
					Log.info("WDSPC getAllElements set runningGetAllElements FALSE...");
					Log.debug("WDSPC getAllElements was interrupted, or otherwise found 0 Elements.");
				}
			}
		}).start();
	}
	
	public void getAllChildElements(final WebElement parentElement, final String parentNode) {
		Log.info("WDSPC.getAllChildElements blocked by runningWindowChecker...");
		while(runningWindowChecker);
		Log.info("WDSPC.getAllChildElements proceeding...");
		stopthread = false;
		(new Thread(){
			public void run(){
				runningGetAllElements = true;
				Log.info("WDSPC.getAllChildElements set runningGetAllElements TRUE...");
				selenium = WDLibrary.getWebDriver();
				SGuiObject sgo = new SGuiObject("","", WDLibrary.getIDForWebDriver(selenium), false);
				List<WebElement> data = null;
				try{
					Log.info("WDSPC.getAllChildElements calling WebDriver findElements()...");
					data = parentElement.findElements(By.xpath(".//*"));
				}catch(Exception e){
					Log.debug("WDSPC findElements() Exception:", e);
					runningGetAllElements = false;
					return;
				}
				
				if(!data.isEmpty()){
					Log.info("WDSPC.getAllChildElements found "+ data.size()+" elements. Attempting XPaths...");
					// get the xpaths to each element
					try{
						utils.setWDTimeoutLock();
						utils.setWDTimeout(0);
						//List<String> paths = new ArrayList();
						//List<WebElement> retained = new ArrayList();
						WebElement e = null;
						String t = null;
						String rec = null;						
						//SearchObject.resetXPathObjectCache();
						for(int i=0;i<data.size() && !stopthread ;i++){
							try{
								e = data.get(i);
								t = e.getTagName();
								if(!SearchObject.isIgnoredNonUITag(t)){									
									rec = SearchObject.generateFullGenericXPath(e);
									spcGUI.insertComponentInTree(e, rec, parentNode);
									//retained.add(e);
									//paths.add(SearchObject.generateFullGenericXPath(e));
								}
							}catch(Exception x){Log.debug("WDSPC.getAllChildElements ignoring "+ x.getClass().getSimpleName()+": "+x.getMessage());}
						}
						//SearchObject.resetXPathObjectCache();
						//spcGUI.updateData(retained, paths);
						runningGetAllElements = false;
						Log.info("WDSPC getAllChildElements set runningGetAllElements FALSE...");
					}catch(Throwable t){
						Log.info("WDSPC getAllChildElements "+ t.getClass().getSimpleName()+": "+t.getMessage());
					}finally{
						utils.resetWDTimeout();
						utils.resetWDTimeoutLock();
					}
				}else{
					runningGetAllElements = false;
					Log.info("WDSPC getAllChildElements set runningGetAllElements FALSE...");
					Log.debug("WDSPC getAllChildElements was interrupted, or otherwise found 0 Elements.");
				}
				spcGUI.setGUIForReady();
			}
		}).start();
	}
	
	public BufferedImage getCurrentPreview(){
		String imagepath = null;
		// let UI settle
		try{Thread.sleep(700);}catch(Exception x){}
		try{ 					
			imagepath = File.createTempFile("WDSPCBrowser", "png").getAbsolutePath();					
			WDLibrary.captureScreen(imagepath);
			ii = ImageIO.read(new File(imagepath));
		}catch(Exception x){
			Log.debug("WDSPC.getAllElements getScreenshot "+ x.getClass().getSimpleName()+", "+x.getMessage());
		}		
		return ii;
	}

	/**
	 * Currently issues the WebDriver quit() command.  Do not call if you intend to leave the browser running.
	 */
	public void stopSelenium() {
		if(selenium != null){
			selenium.quit();
			selenium = null;
		}
	}
	public void stopJavaHOOK(){
		if(standardhook !=null){
			standardhook.stopJavaHOOK();
			standardhook = null;
		}
		if(embeddedhook !=null){
			embeddedhook.shutdown();
			embeddedhook = null;
		}
	}
	
	/**
	 * Retrieve the "title" of each available browser "window" controlled by a WebDriver 
	 * we have launched.
	 * @return
	 */
	public String [] getWindows(){
		Log.info("WDSPC getWindows blocked by runningGetAllElements.");
		while(runningGetAllElements);
		Log.info("WDSPC getWindows now proceeding...");
		runningWindowChecker=true;
		String [] titles = new String[0];
		try{
			titles = WDLibrary.getAllWindowTitles();
		}catch(Exception x){
			Log.debug("WDSPC getWindows Exception:", x);
		}
		runningWindowChecker=false;
		return titles;
	}
	
	private boolean stopthread = false;
	
	/**
	 * sets the stopthread flag polled by the getAllElements Thread.
	 */
	public void cancelSearch() { 
		stopthread = true;
		while( runningGetAllElements ){
			try{Thread.sleep(1000);}catch(Exception x){}
		}
	}
	
	/**
	 * clears the stopthread flag polled by the getAllElements Thread.
	 */
	public void enableSearch() {
		stopthread = false;
	}

	public boolean highlight(SPCTreeNode node){
		String dbgmsg = "WDSPC.highlight "; 
		String xpath = node.xpath;
		String scriptCommand = null;
		boolean highlighted = true;
		
		// Carl Nagle attempt at setFocus()?
		try{ selenium.manage().window().setPosition(selenium.manage().window().getPosition()); }
		catch(NullPointerException ignore){
			if(selenium == null){
				Log.warn(dbgmsg+"did not find a non-null selenium WebDriver to focus!");
			}else{
				Log.warn(dbgmsg+"did not find a non-null WebDriver window() to manage() for get/set Position!");
			}
		}
		
		try{ 
			String rs = node.getRecognitionString();
			if(rs ==null||rs.length()==0) {
				Log.debug(dbgmsg+"did not find stored recognition string. Switching to XPATH.");
				rs="XPATH="+xpath;
			}
			utils.setWDTimeoutLock();
			utils.setWDTimeout(0);
			
			WebElement item = SearchObject.getObject(rs);
			if(item==null){
				Log.debug(dbgmsg+"did not find the WebElement using recognition string: "+rs);
				return false;
			}
			highlighted = SearchObject.highlight(item);
			
		}catch(Exception e){
			Log.warn(dbgmsg+e+getClass().getSimpleName()+": "+e.getMessage());
			highlighted=false;
		}finally{
			utils.resetWDTimeout();
			utils.resetWDTimeoutLock();
		}
		
		return highlighted;
	}

	@Override
	public void runTest() throws Throwable {
		runningWindowChecker = false;
		runningGetAllElements = false;
		utils = (WebDriverGUIUtilities) Runner.hookDriver().getGUIUtilities(); 
		spcGUI = new WDSPCGUI(this);
		try{Thread.sleep(2000);}catch(Exception x){}
		while(spcGUI.isDisplayable()){
			try{Thread.sleep(1000);}catch(Exception x){}
		}
	}

}
