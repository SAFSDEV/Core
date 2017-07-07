/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.safs.Domains;
import org.safs.StringUtils;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.webdriver.lib.CheckBox;
import org.safs.selenium.webdriver.lib.ComboBox;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

/**
 *
 * History:<br>
 *
 *  <br>   Dec 24, 2013    (Lei Wang) Initial release.
 *  <br>   Aug 27, 2014    (dharmesh4) test eclipse checkin
 */
public class TestSeleniumPlusLibrary {

	public static void main(String[] args){
		try{
			Domains.enableDomain(Domains.HTML_DOMAIN);
			Domains.enableDomain(Domains.HTML_DOJO_DOMAIN);
			Domains.enableDomain(Domains.HTML_SAP_DOMAIN);

			testComboBox();
			testCheckBox();
			testJavaScript();

		}catch(Throwable th){
			th.printStackTrace();
		}finally{
			try { WDLibrary.shutdown();} catch (Throwable e) {}
		}
	}

	/**
	 * Will start a firefox browser and return a generated id.<br>
	 * @param url
	 * @return
	 * @throws SeleniumPlusException
	 */
	static String startFireFoxBroswer(String url) throws SeleniumPlusException{
		String browser = "firefox";
		String id = String.valueOf((new Date()).getTime());
		int timeout = 15;

		HashMap<String,Object> extraParameters = new HashMap<String, Object>();
		extraParameters.put(SelectBrowser.KEY_PROXY_SETTING, "your.proxy.host:80");
		extraParameters.put(SelectBrowser.KEY_PROXY_BYPASS_ADDRESS, "localhost,127.0.0.1,***REMOVED***,***REMOVED***,***REMOVED***");

		try{
			WDLibrary.startBrowser(browser, url, id, timeout, true, extraParameters);
		}catch(Throwable th){
			debug(StringUtils.debugmsg(TestSeleniumPlusLibrary.class, "startFireBroswer", th));
			try {
				WDLibrary.startBrowser(browser, url, id, timeout, false, extraParameters);
			} catch (Exception e) {
				throw new SeleniumPlusException(e);
			}
		}

		return id;
	}

	static void testCheckBox() throws SeleniumPlusException{
		if(Domains.isDojoEnabled()){

		}
		if(Domains.isSapEnabled()){
			testSapCheckBox();
		}
		if(Domains.isHtmlEnabled()){
			testHtmlCheckBox();
		}
	}

	static void testSapCheckBox() throws SeleniumPlusException{
		String id = startFireFoxBroswer("http://***REMOVED***/openui5home/1.16.8/checkbox.htm");
		WebElement check = WDLibrary.getObject("id=state_check");

		CheckBox checkbox = new CheckBox(check);

		testLibCheckBox(checkbox);

		WDLibrary.stopBrowser(id);
	}

	static void testHtmlCheckBox() throws SeleniumPlusException{
		String id = startFireFoxBroswer("http://***REMOVED***/openui5home/1.16.8/checkbox.htm");
		WebElement check = WDLibrary.getObject("id=normalCheckBox");

		CheckBox checkbox = new CheckBox(check);

		testLibCheckBox(checkbox);

		WDLibrary.stopBrowser(id);
	}

	static void testLibCheckBox(CheckBox checkbox) throws SeleniumPlusException{
		try{
			checkbox.check();
			debug("Successfully check check-box.");
		}catch(SeleniumPlusException e){
			debug("Fail to check check-box.");
		}
		try{
			checkbox.uncheck();
			debug("Successfully uncheck check-box.");
		}catch(SeleniumPlusException e){
			debug("Fail to uncheck check-box.");
		}
	}

	static void testComboBox() throws SeleniumPlusException{
		if(Domains.isDojoEnabled()){
			testDojoSelect();
			testDojoComboBox();
			testDojoFilteringSelect();
		}
		if(Domains.isSapEnabled()){
			testSapComboBox();
		}
		if(Domains.isHtmlEnabled()){
			testNormalComboBox();
		}
	}

	static void testNormalComboBox() throws SeleniumPlusException{
		String id = startFireFoxBroswer("http://***REMOVED***.apac.sas.com/TestDojo/demo/combobox.php");
		WebElement combo = WDLibrary.getObject("id=fruit");

		ComboBox combobox = new ComboBox(combo);
		// TODO combobox.testSelect();

		String item = "Pears";
		try{
			combobox.select(item, true, false, true);
			debug("Successfully select '"+item+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select '"+item+"'");
		}

		item = "App";
		try{
			combobox.select(item, true, true, true);
			debug("Successfully select '"+item+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select '"+item+"'");
		}

		item = "Oranges";
		try{
			combobox.select(item, false, true, true);
			debug("Successfully select '"+item+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select '"+item+"'");
		}

		int index = 0;
		try{
			combobox.selectIndex(index, true, true);
			debug("Successfully select index '"+index+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select index '"+index+"'");
		}

		try{
			combobox.select(item, true, true, true);
			debug("Successfully select '"+item+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select '"+item+"'");
		}


		index = 3;
		try{
			combobox.selectIndex(index, true, true);
			debug("Successfully select index '"+index+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select index '"+index+"'");
		}

		WDLibrary.stopBrowser(id);
	}


	static void testDojoSelect() throws SeleniumPlusException{
		String id = startFireFoxBroswer("http://***REMOVED***/dojohome/demo/Select.php");

		String RS = "id=stateSelect";
		WebElement combo = WDLibrary.getObject(RS);

		String clazz = WDLibrary.DOJO.getDojoClassName(combo);
		debug("class name="+clazz);

		ComboBox combobox = new ComboBox(combo);
		testLibComboBox(combobox);

		WDLibrary.stopBrowser(id);
	}

	static void testDojoComboBox() throws SeleniumPlusException{
		String id = startFireFoxBroswer("http://***REMOVED***/dojohome/demo/ComboBox.php");
		WebElement combo = WDLibrary.getObject("id=widget_stateSelect");

		ComboBox combobox = new ComboBox(combo);
		testLibComboBox(combobox);

		WDLibrary.stopBrowser(id);
	}
	static void testDojoFilteringSelect() throws SeleniumPlusException{
		String id = startFireFoxBroswer("http://***REMOVED***/dojohome/demo/FilteringSelect.php");
		WebElement combo = WDLibrary.getObject("id=widget_stateSelect");

		ComboBox combobox = new ComboBox(combo);
		testLibComboBox(combobox);

		WDLibrary.stopBrowser(id);
	}

	static void testSapComboBox() throws SeleniumPlusException{
		String id = startFireFoxBroswer("http://***REMOVED***/openui5home/1.16.8/combobox.htm");
		WebElement combo = WDLibrary.getObject( "id=state_cb");

		String clazz = WDLibrary.SAP.getSAPClassName(combo);
		debug("class name="+clazz);

		ComboBox combobox = new ComboBox(combo);
		testLibComboBox(combobox);

		WDLibrary.stopBrowser(id);
	}

	private static void testLibComboBox(ComboBox combobox){
		// TODO combobox.testSelect();

		String item = "Arizona";
		try{
			combobox.select(item, true, false, true);
			debug("Successfully select '"+item+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select '"+item+"'");
		}

		item = "Calif";
		try{
			combobox.select(item, true, true, true);
			debug("Successfully select '"+item+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select '"+item+"'");
		}

		item = "laska";
		try{
			combobox.select(item, false, true, true);
			debug("Successfully select '"+item+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select '"+item+"'");
		}

		int index = 0;
		try{
			combobox.selectIndex(index, true, true);

			debug("Successfully select index '"+index+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select index '"+index+"'");
		}

		try{
			combobox.select(item, true, true, true);
			debug("Successfully select '"+item+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select '"+item+"'");
		}


		index = 3;
		try{
			combobox.selectIndex(index, true, true);
			debug("Successfully select index '"+index+"'");
		}catch(SeleniumPlusException e){
			debug("Fail to select index '"+index+"'");
		}
	}

	static void testJavaScript() throws SeleniumPlusException{
		if(Domains.isDojoEnabled()) testJavaScriptOnDOJOApplication();
		if(Domains.isSapEnabled())  testJavaScriptOnSAPApplication();
	}

	static void testJavaScriptOnDOJOApplication() throws SeleniumPlusException{
		String id = startFireFoxBroswer("http://***REMOVED***/TestDojo/demo/combobox.php");

		WDLibrary.js_initError();

		int errorCode = JavaScriptFunctions.ERROR_CODE_NOT_SET;

		try {
			errorCode = WDLibrary.js_getErrorCode();
			if(JavaScriptFunctions.ERROR_CODE_NOT_SET==errorCode){
				debug("Succeed to initialize global error code.");
			}else{
				debug("Failed to initialize global error code.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			WDLibrary.js_setErrorCode(3);
			errorCode = WDLibrary.js_getErrorCode();
			if(3==errorCode){
				debug("Succeed to set global error code to 3.");
			}else{
				debug("Succeed to set global error code to 3.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			WDLibrary.js_cleanError();
			errorCode = WDLibrary.js_getErrorCode();
			debug("Global error = "+errorCode);
		} catch (Exception e) {
			e.printStackTrace();
		}


		WDLibrary.stopBrowser(id);
	}

	static void testJavaScriptOnSAPApplication() throws SeleniumPlusException{
		String id = startFireFoxBroswer("http://***REMOVED***/openui5home/1.16.8/combobox.htm");

		String RS = "id=state_cb";
		WebElement combo = WDLibrary.getObject(RS);

		List<String> classes = WDLibrary.SAP.getSAPClassNames(combo);
		for(String clazz:classes)
			debug("class name="+clazz);

		WDLibrary.stopBrowser(id);
	}

	public static void debug(String message){
		System.out.println(message);
	}

}