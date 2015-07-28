/**
 * 
 */
package org.safs.selenium.webdriver;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.TestStepProcessor;

/**
 * @author canagl
 *
 */
public class WDTestStepProcessor extends TestStepProcessor {

	/**
	 * 
	 */
	public WDTestStepProcessor() {
		super();
	}

	  /**
	   * Called from superclass to find the windowObject and compObjects WebElements for GUI commands.
	   * <p>
	   * This routine is called only after all MixedUse recognition strings have separately been handled. 
	   * This routine is called only after all non-GUI commands have separately been handled.
	   * This routine is only called if it has been determined we have a "normal" GUI command in which 
	   * OBT window and component objects need to be found.
	   * <p> 
	   * setActiveWindow is NOT invoked since that is not implemented in WebDriverGUIUtilities.
	   * <p>
	   * The primary reason for this override is to prevent duplicate Window searches and JavaScript 
	   * execution that are negatively impacting performance unnecessarily.
	   * @return
	   * @throws SAFSException 
	   */
	  @Override
	  protected boolean getWinAndCompGUIObjects() throws SAFSException{
	      Log.debug("WDTestStepProcessor.getWindAndCompGUIObjects calling waitForObject and verifying status...");
	      //get both window and component (if any) in a single call.
	      return waitForObjectAndCheck(false);
	  }
	  
	
}
