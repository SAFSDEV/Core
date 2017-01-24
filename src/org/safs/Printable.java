/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2016年12月14日    (SBJLWA) Initial release.
 */
package org.safs;

/**
 * @author sbjlwa
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
