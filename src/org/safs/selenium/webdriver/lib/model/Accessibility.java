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
/**
 * History:
 *
 *  SEP 09, 2019    (Lei Wang) Initial release.
 */
package org.safs.selenium.webdriver.lib.model;

/**
 * This provides a uniformed Accessibility object to represent the accessibility item within a SAS Canvas.<br>
 *
 * Below are 2 kinds of Canvas view:<br>
 *  ["sas.van.views.graph.DataGridUIView", "sas.hc.graph.UIView", "sas.ltjs.commons.views.Visualization", "sas.ltjs.commons.views.AbstractVisualization", "sas.ltjs.commons.views.UI", "sap.ui.core.Control", "sap.ui.core.Element", "sap.ui.base.ManagedObject", "sap.ui.base.EventProvider", "sap.ui.base.Object"]<br>
 *  ["sas.van.views.graph.GraphUIView", "sas.hc.graph.GraphView", "sas.ltjs.commons.views.Visualization", "sas.ltjs.commons.views.AbstractVisualization", "sas.ltjs.commons.views.UI", "sap.ui.core.Control", "sap.ui.core.Element", "sap.ui.base.ManagedObject", "sap.ui.base.EventProvider", "sap.ui.base.Object"]<br>
 *
 * <p>
 * We use the below fields to match the property of item within Canvas (javascript object).<br>
 * <ul>
 * <li>{@link #frame} contains the value of property '<b>accessibilityFrame</b>' of item within Canvas.
 * <li>{@link #hint} contains the value of property '<b>accessibilityHint</b>' of item within Canvas.
 * <li>{@link #terseLabel} contains the value of property '<b>terseAccessibilityLabel</b>' of item within Canvas.
 * <li>{@link Element#label} contains the value of property '<b>accessibilityLabel</b>' of item within Canvas.
 * <li>{@link Element#selected} contains the value of property '<b>isSelected</b>' of item within Canvas.
 * </ul>
 */
public class Accessibility extends Element{

	/** The property <b>accessibilityFrame</b> of item within Canvas (javascript object). */
	public static final String PROPERTY_ACCESSIBILITY_FRAME 		= "accessibilityFrame";
	/** The property <b>accessibilityHint</b> of item within Canvas (javascript object). */
	public static final String PROPERTY_ACCESSIBILITY_HINT	 		= "accessibilityHint";
	/** The property <b>accessibilityLabel</b> of item within Canvas (javascript object). */
	public static final String PROPERTY_ACCESSIBILITY_LABEL 		= "accessibilityLabel";
	/** The property <b>terseAccessibilityLabel</b> of item within Canvas (javascript object). */
	public static final String PROPERTY_TERSE_ACCESSIBILITY_LABEL	= "terseAccessibilityLabel";

	/** contains the value of property {@link #PROPERTY_ACCESSIBILITY_FRAME} of item within Canvas (javascript object). */
	protected String frame = null;
	/** contains the value of property {@link #PROPERTY_ACCESSIBILITY_HINT} of item within Canvas (javascript object). */
	protected String hint = null;
	/** contains the value of property {@link #PROPERTY_TERSE_ACCESSIBILITY_LABEL} of item within Canvas (javascript object). */
	protected String terseLabel = null;

	protected Accessibility(){}

	/**
	 * Constructor used to create an uniformed Item object. User may override this one to parse their own object.
	 * @param object Object, the item object. It may be a Map returned from javascript function; It maybe a WebElement.
	 */
	public Accessibility(Object object){
		initialize(object);
	}

	/**
	 * set/update the class's fields through the underlying WebElement or AbstractMap.
	 */
	@Override
	public void updateFields(){
//		super.updateFields();

		if(map!=null){
			frame = getAttribute(PROPERTY_ACCESSIBILITY_FRAME);
			hint = getAttribute(PROPERTY_ACCESSIBILITY_HINT);
			label = getAttribute(PROPERTY_ACCESSIBILITY_LABEL);
			terseLabel = getAttribute(PROPERTY_TERSE_ACCESSIBILITY_LABEL);
			try{ selected = Boolean.parseBoolean(getAttribute(PROPERTY_SELECTED));} catch(Exception e){}

		}else if(webelement!=null){
			//TODO NOT implemented yet, do we have such kind of WebElement?
		}
	}

	public String getFrame() {
		return frame;
	}

	public String getHint() {
		return hint;
	}

	public String getTerseLabel() {
		return terseLabel;
	}

	/**
	 * @return String, the content of this Item (label, or terseLabel).
	 */
	@Override
	public String contentValue(){
		if(super.contentValue()==null) return terseLabel;
		return super.contentValue();
	}

	@Override
	public String toString(){
		return "frame="+frame+"\n hint="+hint+"\n label="+label+"\n terseLabel="+terseLabel+"\n selected="+selected+"\n";
	}
}
