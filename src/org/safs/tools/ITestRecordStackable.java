/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * MAY 12, 2016    (Lei Wang) Initial release.
 */
package org.safs.tools;

import org.safs.TestRecordData;

/**
 * @author Lei Wang
 *
 */
public interface ITestRecordStackable {
	/** */
	public void pushTestRecord(TestRecordData trd);
	/** */
	public TestRecordData popTestRecord();
}