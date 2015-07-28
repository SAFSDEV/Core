/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.rational.wpf;

import org.safs.Log;
import org.safs.rational.CFText;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.IText;

/**
 * <br><em>Note:</em>	 	extends from org.safs.rational.CFText
 * <br><em>Purpose:</em> 	process a Text component of domain .NET_WPF
 * <br><em>Lifetime:</em> 	instantiated by TestStepProcessor
 * <p>
 * @author  Lei	Wang
 * @since   SEP 27, 2009
 **/

public class CFWPFText extends CFText {

	/**
	 * Note: Override its superclass's method getText()
	 * 
	 * @return The text of the component
	 */
	protected String getText() {
		String debugMsg = getClass().getName() + ".getText(): ";
		String textValue = null;
		String className = obj1.getObjectClassName();
		Log.debug("*************  object's className: " + className);

		if (obj1 instanceof IText) {
			IText wpfText = (IText) obj1;
			textValue = wpfText.getText();
			//For System.Windows.Controls.RichTextBox, getText() will return a string ends with "\r\n"
			//we should remove them so that the verification will pass.
			if(textValue.endsWith("\r\n")){
				textValue = textValue.substring(0, textValue.length()-2);
			}
		} else {
			Log.debug(debugMsg+ " This is not a IText, Please find other way to get Text value.");
		}

		return textValue;
	}

	/**
	 * Note: override its superclass's method setPropertyText()
	 */
	protected boolean setPropertyText(TestObject testObject, String text) {
		String debugMsg = getClass().getName() + ".setPropertyText(): ";
		boolean setTextOK = true;

		String className = testObject.getObjectClassName();
		Log.debug(debugMsg + "set value " + text + " to object whose className is " + className);
		
		if (obj1 instanceof IText) {
			IText wpfText = (IText) obj1;
			wpfText.setText(text);
		} else {
			Log.debug(debugMsg+ " This is not a IText, Please find other way to set Text value.");
			setTextOK = false;
		}

		return setTextOK;
	}
}
