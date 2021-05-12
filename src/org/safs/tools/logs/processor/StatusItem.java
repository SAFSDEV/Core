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
 * @date 2018-04-27    (Lei Wang) Initial release.
 * @date 2018-04-28    (Lei Wang) Added method count(): convert 'text' into integer. 
 */
package org.safs.tools.logs.processor;

/**
 * @author Lei Wang
 *
 */
public class StatusItem extends Tag{
	protected String type = null;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	private int parseInt(String intNumber){
		try{
			return Integer.parseInt(intNumber);
		}catch(NumberFormatException e){
			return 0;
		}
	}

	/**
	 * Convert the property 'text' into an int value.<br>
	 * In fact the property 'text' represents the count number of the 'test record' finished in this status.<br>
	 */
	public int count(){
		return parseInt(text);
	}
}
