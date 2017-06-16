/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年6月14日    (Lei Wang) Initial release.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

/**
 * This is exact the same as AlertText.<br>
 * In "Selenium IDE", the command list contains 'storeAlert', 'verifyAlert', 'verifyNotAlert' etc.
 * To get compatible with the script generated from the "Selenium IDE", this 'Alert' getter is provided.<br>
 *
 * @author Lei Wang
 */
public class Alert extends AlertText implements SRunnerType{
}
