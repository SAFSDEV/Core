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
package org.safs

import static org.junit.Assert.*

public class LogFileUtil {

	/**
	 * Handles error conditions when a log file is being used.   If the
	 * input closure throws a Throwable, the log file will be read and
	 * the text will be returned as ret.logText.   The throwable will
	 * be returned as ret.throwable.
	 *
	 * If there is no Throwable, null is passed back for throwable and
	 * logText.
	 */
	public withLogFile(logFile, closure) {
		def ret = [throwable:null, logText:null]
		try {
			/*
			 * Delete the logFile if it exists.   This is so that if
			 * something catastrophic happens when the closure is
			 * called, there won't be a stale log lying around that
			 * may confuse things.
			 */
			if (logFile.exists()) {
				logFile.delete()
			}

			closure.call()
		} catch (Throwable t) {
			ret.throwable = t
			if (logFile.exists()) {
				ret.logText = logFile.text
			}
		}
		return ret
	}
}
