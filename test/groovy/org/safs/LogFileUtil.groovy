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
