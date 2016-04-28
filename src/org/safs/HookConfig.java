/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * MAR 25, 2016    (Lei Wang) Initial release.
 */
package org.safs;

/**
 * In SAFS, the engine and hook will normally run in different JVM.
 * At hook side, we cannot see any settings done at engine side, and we need to
 * get the settings from the ConfigureInterface again so that the hook will know
 * about them.
 *
 */
public interface HookConfig {
	/**Check the ConfigureInterface to get the settings required by Hook.*/
	public void checkConfiguration();
}
