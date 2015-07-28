package org.safs.rational.flex;

import org.safs.rational.CFComponent;
import org.safs.*;
import com.rational.test.ft.object.interfaces.flex.FlexNumericStepperTestObject;
import com.rational.test.ft.script.SubitemFactory;



/**
 * <br><em>Purpose:</em> CFFlexSlider, process a FLEX NumericStepper component 
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  JunwuMa
 * @since   APR 24, 2009
 *   
 **/
public class CFFlexNumericStepper extends CFComponent {
	public static final String SETVALUE = "SetValue";	

	public CFFlexNumericStepper() {
	    super();
	}
	protected void localProcess() {
		// then we have to process for specific items not covered by our super
		Log.info(getClass().getName()+".process, searching specific tests...");
		if (action != null) {
			Log.info("....."+getClass().getName()+".process; ACTION: "+action+"; win: "+ windowName +"; comp: "+compName);
		    if (action.equalsIgnoreCase(SETVALUE)){
		        String param = (String) params.iterator().next();
				Log.info("...param: " + param);
		    	try {
		    		FlexNumericStepperTestObject stepperObj = new FlexNumericStepperTestObject(obj1.getObjectReference());
		    		stepperObj.selectText(SubitemFactory.atPoint(0,1));
		    		stepperObj.input(param);
				    // set status to ok
				    String altText = windowName+":"+compName+" "+action +" successful using "+ param;
				    log.logMessage(testRecordData.getFac(),
				                     passedText.convert("success3a", altText, 
				                                        windowName, compName, action, param),
				                     PASSED_MESSAGE);
				    testRecordData.setStatusCode(StatusCodes.OK);		    		
		    	} catch (Exception ex) {
		              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		              String detail = failedText.convert("failure2", "Unable to perform "+ action +" on "+ compName, action, compName);
		              componentFailureMessage(detail+": "+ex.getMessage());		    		
		    	}
		    }
		} 
	}
}