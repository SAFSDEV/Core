package org.safs.selenium.webdriver.lib.test;
import org.safs.TestRecordHelper;
import org.safs.model.Component;
import org.openqa.selenium.WebDriver;
import org.safs.selenium.webdriver.SeleniumPlus;

/**
 * This class is not expected to execute.  
 * It is primarily provided as a build/API verification means.
 * <p>
 * If any of the underlying SeleniumPlus superclass API changes unexpectedly this
 * class should not successfully compile.
 * 
 * @author Carl Nagle
 */
public class SeleniumPlusAPITest extends SeleniumPlus {

	org.safs.model.Component win = new org.safs.model.Component("win");
	org.safs.model.Component comp = new org.safs.model.Component(win, "comp");
	String aparam = new String("anything");
	String[] allparams = new String[]{"1", "2"};

	/* (non-Javadoc)
	 * 
	 */
	public SeleniumPlusAPITest() {
		super();
	}

	void staticAPIInvocations(){
		
		WebDriver wd = SeleniumPlus.WebDriver();
		TestRecordHelper trh = SeleniumPlus.prevResults;
		
		try{ SeleniumPlus.AbortTest("no reason");}catch(Throwable ignore){}
		
		boolean bool = SeleniumPlus.Click(comp);
		bool = SeleniumPlus.Click(comp, aparam);
		bool = SeleniumPlus.Click(comp, allparams);
		bool = SeleniumPlus.GetGUIImage(comp, aparam);
		bool = SeleniumPlus.GetGUIImage(comp, aparam, allparams);
		
		String str = SeleniumPlus.GetVariableValue(aparam);
		bool = SeleniumPlus.SetVariableValue(aparam, aparam);
		
		bool = SeleniumPlus.Pause(1);
		bool = SeleniumPlus.PrintTestCaseSummary(aparam);
		bool = SeleniumPlus.StartTestCase(aparam);
		bool = SeleniumPlus.StartWebBrowser(aparam, aparam);
    	bool = SeleniumPlus.StartWebBrowser(aparam, aparam, aparam);
    	bool = SeleniumPlus.StartWebBrowser(aparam, aparam, allparams);
    	bool = SeleniumPlus.StopTestCase(aparam);
    	bool = SeleniumPlus.StopWebBrowser(aparam);
    	bool = SeleniumPlus.SwitchWebBrowser(aparam);
    
    	bool = SeleniumPlus.ComboBox.CaptureItemsToFile(comp, aparam);
    	bool = SeleniumPlus.ComboBox.CaptureItemsToFile(comp, aparam, allparams);
    	bool = SeleniumPlus.ComboBox.HideList(comp);
    	bool = SeleniumPlus.ComboBox.Select(comp, aparam);
    	bool = SeleniumPlus.ComboBox.SelectIndex(comp, 1);
    	bool = SeleniumPlus.ComboBox.SelectPartialMatch(comp, aparam);
    	bool = SeleniumPlus.ComboBox.SelectUnverified(comp, aparam);
    	bool = SeleniumPlus.ComboBox.ShowList(comp);
    	bool = SeleniumPlus.ComboBox.VerifySelected(comp, aparam);

    	bool = SeleniumPlus.Counters.LogCounterInfo(aparam);
    	bool = SeleniumPlus.Counters.ResumeCounts();
    	bool = SeleniumPlus.Counters.ResumeCounts(aparam);
    	bool = SeleniumPlus.Counters.ResumeCounts(allparams);
    	bool = SeleniumPlus.Counters.StartCounter(aparam);
    	bool = SeleniumPlus.Counters.StartCounter(aparam, allparams);
    	bool = SeleniumPlus.Counters.StopCounter(aparam);
    	bool = SeleniumPlus.Counters.StopCounter(aparam, allparams);
    	bool = SeleniumPlus.Counters.StoreCounterInfo(aparam, aparam);
    	bool = SeleniumPlus.Counters.SuspendCounts();
    	bool = SeleniumPlus.Counters.SuspendCounts(aparam);
    	bool = SeleniumPlus.Counters.SuspendCounts(allparams);
    	
    	bool = SeleniumPlus.EditBox.SetTextValue(comp, aparam);
    	
    	// bool = SeleniumPlus.Files
    	
    	bool = SeleniumPlus.Logging.LogMessage(aparam);
    	bool = SeleniumPlus.Logging.LogMessage(aparam, allparams);
    	bool = SeleniumPlus.Logging.LogTestFailure(aparam);
    	bool = SeleniumPlus.Logging.LogTestFailure(aparam, allparams);
    	bool = SeleniumPlus.Logging.LogTestSuccess(aparam);
    	bool = SeleniumPlus.Logging.LogTestSuccess(aparam, allparams);
    	bool = SeleniumPlus.Logging.LogTestWarning(aparam);
    	bool = SeleniumPlus.Logging.LogTestWarning(aparam, allparams);
    	bool = SeleniumPlus.Logging.ResumeLogging();
    	bool = SeleniumPlus.Logging.SuspendLogging();
    	
    	bool = SeleniumPlus.Misc.AssignClipboardVariable(aparam);
    	bool = SeleniumPlus.Misc.CallRemote(aparam, aparam, aparam, aparam, aparam);
    	bool = SeleniumPlus.Misc.CallRemote(aparam, aparam, aparam, aparam, aparam, allparams);
    	
    	// bool = SeleniumPlus.Strings
    	
    	bool = SeleniumPlus.Tree.ExpandTextNode(comp, aparam);
    	bool = SeleniumPlus.Tree.SelectTextNode(comp, aparam);
	}
	
	void instanceAPIInvocations(){
		
		WebDriver wd = WebDriver();
		TestRecordHelper trh = prevResults;
		
		try{ AbortTest("no reason");}catch(Throwable ignore){}
		
		boolean bool = Click(comp);
		bool = Click(comp, aparam);
		bool = Click(comp, allparams);
		bool = GetGUIImage(comp, aparam);
		bool = GetGUIImage(comp, aparam, allparams);
		
		String str = GetVariableValue(aparam);
		bool = SetVariableValue(aparam, aparam);
		
		bool = Pause(1);
		bool = PrintTestCaseSummary(aparam);
		bool = StartTestCase(aparam);
		bool = StartWebBrowser(aparam, aparam);
    	bool = StartWebBrowser(aparam, aparam, aparam);
    	bool = StartWebBrowser(aparam, aparam, allparams);
    	bool = StopTestCase(aparam);
    	bool = StopWebBrowser(aparam);
    	bool = SwitchWebBrowser(aparam);
    
    	bool = ComboBox.CaptureItemsToFile(comp, aparam);
    	bool = ComboBox.CaptureItemsToFile(comp, aparam, allparams);
    	bool = ComboBox.HideList(comp);
    	bool = ComboBox.Select(comp, aparam);
    	bool = ComboBox.SelectIndex(comp, 1);
    	bool = ComboBox.SelectPartialMatch(comp, aparam);
    	bool = ComboBox.SelectUnverified(comp, aparam);
    	bool = ComboBox.ShowList(comp);
    	bool = ComboBox.VerifySelected(comp, aparam);

    	bool = Counters.LogCounterInfo(aparam);
    	bool = Counters.ResumeCounts();
    	bool = Counters.ResumeCounts(aparam);
    	bool = Counters.ResumeCounts(allparams);
    	bool = Counters.StartCounter(aparam);
    	bool = Counters.StartCounter(aparam, allparams);
    	bool = Counters.StopCounter(aparam);
    	bool = Counters.StopCounter(aparam, allparams);
    	bool = Counters.StoreCounterInfo(aparam, aparam);
    	bool = Counters.SuspendCounts();
    	bool = Counters.SuspendCounts(aparam);
    	bool = Counters.SuspendCounts(allparams);
    	
    	bool = EditBox.SetTextValue(comp, aparam);
    	
    	// bool = Files
    	
    	bool = Logging.LogMessage(aparam);
    	bool = Logging.LogMessage(aparam, allparams);
    	bool = Logging.LogTestFailure(aparam);
    	bool = Logging.LogTestFailure(aparam, allparams);
    	bool = Logging.LogTestSuccess(aparam);
    	bool = Logging.LogTestSuccess(aparam, allparams);
    	bool = Logging.LogTestWarning(aparam);
    	bool = Logging.LogTestWarning(aparam, allparams);
    	bool = Logging.ResumeLogging();
    	bool = Logging.SuspendLogging();
    	
    	bool = Misc.AssignClipboardVariable(aparam);
    	bool = Misc.CallRemote(aparam, aparam, aparam, aparam, aparam);
    	bool = Misc.CallRemote(aparam, aparam, aparam, aparam, aparam, allparams);
    	
    	// bool = Strings
    	
    	bool = Tree.ExpandTextNode(comp, aparam);
    	bool = Tree.SelectTextNode(comp, aparam);
	}
	
	/* (non-Javadoc)
	 * @see org.safs.selenium.webdriver.SeleniumPlus#runTest()
	 */
	@Override
	public void runTest() throws Throwable {
		
	}

}
