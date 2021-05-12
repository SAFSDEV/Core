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
package org.safs.tools.input;

import org.safs.tools.UniqueStringID;

public class UniqueStringItemInfo
	extends UniqueStringID
	implements UniqueItemInterface {

	protected String sectionname = null;
	protected String itemname    = null;
	
	/**
	 * Constructor for UniqueStringItemInfo
	 */
	public UniqueStringItemInfo() {
		super();
	}

	/**
	 * Constructor for UniqueStringItemInfo
	 */
	public UniqueStringItemInfo(String id) {
		super(id);
	}

	/**
	 * PREFERRED Constructor for UniqueStringItemInfo
	 */
	public UniqueStringItemInfo(String id, String sectionname, String itemname) {
		this(id);
		setSectionName(sectionname);
		setItemName(itemname);
	}

	/**
	 * Set itemname to that provided.
	 */
	public void setItemName(String itemname) {
		this.itemname = itemname;
	}

	/**
	 * @see UniqueItemInterface#getItemName()
	 */
	public String getItemName() {
		return itemname;
	}

	/**
	 * Set sectionname to that provided.
	 */
	public void setSectionName(String sectionname) {
		this.sectionname = sectionname;
	}

	/**
	 * @see UniqueSectionInterface#getSectionName()
	 */
	public String getSectionName() {
		return sectionname;
	}

}

