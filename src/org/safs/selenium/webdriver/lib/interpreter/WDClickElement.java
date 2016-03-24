/**
 * 
 */
package org.safs.selenium.webdriver.lib.interpreter;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.WDLibrary;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.ClickElement;

/**
 * @author canagl
 *
 */
public class WDClickElement extends ClickElement {

	public WDClickElement() {
		super();
	}

	@Override
	public boolean run(TestRun ctx){
		ctx.getLog().debug("WDClick executing custom Find and Click via WDLibrary.");
		try{
			WebElement e = ctx.currentStep().locatorParams.get("locator").find(ctx);
			if(e == null){
				ctx.getLog().info("WDClick locator did NOT find the expected WebElement!");
				return false;
			}else{
				ctx.getLog().info("WDClick locator found the expected WebElement.");
			}
			WDLibrary.windowSetFocus(e); 
			WDLibrary.click(e);			
			ctx.getLog().info("WDClick finished without known errors.");
			return true;
		}
		catch(Throwable t){
			ctx.getLog().debug("WDClick error: "+ t.getMessage(), t);
		}
		return false;
	}
}
