/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
