/**
 * 
 */
package org.safs.selenium.webdriver.lib.interpreter;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.text.GENKEYS;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.Get;

/**
 * Override standard SeBuilder Get to launch a new Se+ session, if needed.
 * @author Carl Nagle
 */
public class WDGet extends Get {

	/**
	 * Initialize the superclass.
	 */
	public WDGet() {
		super();
	}

//	@Override
//	public boolean run(TestRun ctx){
//		ctx.getLog().debug("Executing custom Get Step via WDLibrary.");
//		try{
//			int timeout = 60;			
//			WebDriver driver = WDLibrary.getWebDriver();
//			if(driver == null){
//				ctx.getLog().info("WDGet found no existing WebDriver session.  Attempting to start a new session.");
//				try{ WDLibrary.startBrowser(null, null, null, timeout, true);}
//				catch(Throwable th){
//					String thmsg = th.getMessage();
//					ctx.getLog().info("WDGet initially failed to create a new WebDriver session. RemoteServer may not be running.");
//					String seleniumhost = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_HOST, SelectBrowser.DEFAULT_SELENIUM_HOST);
//					String seleniumport = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_PORT, SelectBrowser.DEFAULT_SELENIUM_PORT);
//					if( seleniumhost.equals(SelectBrowser.DEFAULT_SELENIUM_HOST)){			
//						ctx.getLog().info("WDGet attempting to start a local RemoteServer...");
//						if(WebDriverGUIUtilities.startRemoteServer()){
//							try{
//								WDLibrary.startBrowser(null, null, null, timeout, true);
//								ctx.getLog().info("WDGet successful starting browser session. Commencing Get Step...");
//								return super.run(ctx);
//							}catch(Throwable th2){
//								ctx.getLog().debug("WDGet failed to start a new WebDriver session:", th2);
//								return false;
//							}
//						}else{
//							ctx.getLog().debug("WDGet failed to start the local RemoteServer!");
//							return false;
//						}
//					}else{
//						ctx.getLog().info("WDGet detected the expected RemoteServer '"+ seleniumhost+":"+ seleniumport +"' is not running and cannot be started locally.");
//						return false;
//					}
//				}
//			}
//			return super.run(ctx);
//		}
//		catch(Throwable t){
//			ctx.getLog().debug("WDGet Error:", t);
//		}
//		return false;
//	}	
}
