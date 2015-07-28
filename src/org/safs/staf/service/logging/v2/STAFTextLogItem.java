package org.safs.staf.service.logging.v2;

import java.util.StringTokenizer;

import org.safs.staf.service.logging.AbstractSTAFTextLogItem;

import com.ibm.staf.STAFResult;

/**
 * This class is used to realize STAF-version related method.
 * It will be instantiated in class SAFSLoggingService and class SLSLogFacility
 * 
 * @since	MAY 19 2009		(LW)	Realize the method getSTAFLogDirectory()
 *
 * @see SAFSLoggingService
 * @see org.safs.staf.service.logging.SLSLogFacility
 */
public class STAFTextLogItem extends AbstractSTAFTextLogItem {

	/**
	 * Creates a disabled <code>STAFTextLogItem</code> with default name (file
	 * name) and log level (<code>LOGLEVEL_INFO</code>), and empty parent
	 * directory.
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_TEXT</code>.
	 * <p>
	 * @param file		the file spec of this log.
	 */
	public STAFTextLogItem(String file) {
		super(file);
	}

	/**
	 * Creates a disabled <code>STAFTextLogItem</code> with default name (file
	 * name) and log level (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_TEXT</code>.
	 * <p>
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public STAFTextLogItem(String parent, String file) {
		super(parent, file);
	}
	
	/**
	 * Creates a disabled <code>STAFTextLogItem</code> with default log level 
	 * (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * The type of this log item is always <code>LOGMODE_SAFS_XML</code>.
	 * <p>
	 * @param name		the name of this log.
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public STAFTextLogItem(String name, String parent, String file) {
		super(name, parent, file);
	}

	/**
	 * Returns the DIRECTORY setting of the STAF LOG service.
	 * <P>
	 * @return	the Directory setting as returned by LIST SETTINGS request to
	 * 			STAF LOG service.
	 */
	protected String getSTAFLogDirectory() {
		// submit LIST SETTINGS request to STAF LOG service to retrieve its
		// settings. the second line of the result buffer contains directory.
		STAFResult result = stafLogRequest("list settings");

		// If stas version is 2, the result will be String
		StringTokenizer st = new StringTokenizer(result.result, "\n\r\f");
		debugLog.debugPrintln("getSTAFLogDirectory(), list result is: "
				+ st.toString());
		st.nextToken();
		if (st.hasMoreElements()) {
			String s = st.nextToken();
			return s.substring(s.indexOf(":") + 2);
		} else {
			debugLog
					.debugPrintln("getSTAFLogDirectory(): Need modify code to get the log directory.");
			return "";
		}
	}

}
