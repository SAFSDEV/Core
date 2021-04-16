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
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2019-07-11    (Lei Wang) Initial release.
 * @date 2019-08-02    (Lei Wang) Used window's and component's SAFS RS as the search conditions. Remove the 'label', 'xpath' and 'css'.
 * @date 2019-08-23    (Lei Wang) Added field 'type', it helps to locate the exact web-element, when the 'componentRS' is a simple string.
 *
 */
package org.safs.cukes.ai.selenium;

/**
 * This class holds the search-conditions to find the target component.
 *
 * @author Lei Wang
 */
public class Criteria{
	/**
	 * The parent recognition string.<br>
	 * This might be null or empty.<br>
	 */
	private String parentRS = null;
	/**
	 * The component recognition string.<br>
	 * This might be a simple string served as a label to find label component in "Selenium AI",
	 * and the field 'type' will help to locate the exact web-element.<br>
	 */
	private String componentRS = null;

	/**
	 * The component's type (such as Button, ComboBox, EditBox etc.),
	 * and it helps to locate the exact web-element, when the {@link #componentRS} is a simple string.<br>
	 */
	private String type = null;

	/**
	 * If the text will be matched partially (sub-string of the target string).<br>
	 */
	private boolean partialMatch = false;

	public Criteria() {
		super();
	}

	/**
	 * @param parentRS String, the window's recognition string
	 * @param componentRS String, the component's recognition string
	 * @param partialMatch boolean, if we should match the text partially (sub-string).
	 */
	public Criteria(String parentRS, String componentRS, boolean partialMatch) {
		super();
		this.parentRS = parentRS;
		this.componentRS = componentRS;
		this.partialMatch = partialMatch;
	}

	/**
	 * @param parentRS String, the window's recognition string
	 * @param componentRS String, the component's recognition string
	 * @param partialMatch boolean, if we should match the text partially (sub-string).
	 * @param type String, The component's type (such as Button, ComboBox, EditBox etc.)
	 */
	public Criteria(String parentRS, String componentRS, boolean partialMatch, String type) {
		super();
		this.parentRS = parentRS;
		this.componentRS = componentRS;
		this.partialMatch = partialMatch;
		this.type = type;
	}

	/**
	 * @param parentRS String, the window's recognition string
	 * @param componentRS String, the component's recognition string
	 */
	public Criteria(String parentRS, String componentRS) {
		super();
		this.parentRS = parentRS;
		this.componentRS = componentRS;
	}

	/**
	 * The parent recognition string.<br>
	 * This might be null or empty.<br>
	 */
	public String getParentRS() {
		return parentRS;
	}

	public void setParentRS(String parentRS) {
		this.parentRS = parentRS;
	}

	/**
	 * The component recognition string.<br>
	 * This might be a simple string served as a label to find component in "Selenium AI".<br>
	 */
	public String getComponentRS() {
		return componentRS;
	}

	public void setComponentRS(String componentRS) {
		this.componentRS = componentRS;
	}

	/**
	 * @return String, The component's type (such as Button, ComboBox, EditBox etc.),
	 *                 and it helps to locate the exact web-element, when the {@link #componentRS} is a simple string.<br>
	 */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * If the text will be matched partially (sub-string of the target string).<br>
	 * It is going to be used with the field {@link #parentRS}.<br>
	 */
	public boolean isPartialMatch() {
		return partialMatch;
	}
	public void setPartialMatch(boolean partialMatch) {
		this.partialMatch = partialMatch;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("partialMatch="+partialMatch+" ");
		if(parentRS!=null) sb.append("parentRS="+parentRS+" ");
		if(componentRS!=null) sb.append("componentRS="+componentRS+" ");
		if(type!=null) sb.append("type="+type+" ");
		String result = sb.toString();
		if(result.endsWith(" ")){
			result = result.substring(0, result.length()-1);
		}
		return result;
	}

}
