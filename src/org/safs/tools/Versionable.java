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
 * @date 2018-04-18    (Lei Wang) Initial release.
 */
package org.safs.tools;

/**
 * @author Lei Wang
 *
 */
public interface Versionable {
	/** set the name of the version-able object */
	public void setProductName(String productName);
	/** set the version number */
	public void setVersion(String version);
	/** set the description of the version-able object */
	public void setDescription(String description);
	/** The name of the version-able object */
	public String getProductName();
	/** The version number */
	public String getVersion();
	/** The description of the version-able object */
	public String getDescription();
}
