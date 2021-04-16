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
 * 2016年12月14日    (Lei Wang) Initial release.
 */
package org.safs;

/**
 * @author Lei Wang
 *
 */
public interface Printable {
	/**
	 * @return int, The number of tabulation to print before the actual message.
	 */
	public int getTabulation();
	/**
	 * @param tabulation int, The number of tabulation to print before the actual message.
	 */
	public void setTabulation(int tabulation);

	/**
	 * @param threshold int, the size of the data too big to print.
	 */
	public void setThreshold(int threshold);
	/**
	 * @return int, the size of the data too big to print.
	 */
	public int getThreshold();

	/**
	 * Enable/disable the "threshold ability".<br/>
	 * If enabled, then the data bigger than the 'threshold' will not be printed out.
	 * Otherwise, it will be printed out.
	 *
	 * @param enabled boolean, enable or disable
	 */
	public void setThresholdEnabled(boolean thresholdEnabled);
	/**
	 * If enabled, then the data bigger than the 'threshold' will not be printed out.
	 * Otherwise, it will be printed out.
	 * @return boolean, If "threshold ability" is enabled.
	 */
	public boolean isThresholdEnabled();

	/** "** THE DATA IS TOO BIG TO PRINT **" */
	public static final String DATA_BIGGER_THAN_THRESHOLD = "** THE DATA IS TOO BIG TO PRINT **";
}
