package org.safs.staf.service.logging;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.safs.STAFHelper;
import org.safs.logging.FileLogItem;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.ServiceDebugLog;
import org.safs.tools.CaseInsensitiveFile;

import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;
import com.ibm.staf.STAFUtil;

/**
 * This class represents a file log implemented using STAF LOG service. It
 * contains a STAF handle to interact with STAF.
 * <p>
 * The initialization of this log item clears existing STAF log with the same 
 * name.
 * @author Carl Nagle 02/24/05 changed use of machine to effectiveMachine to avoid "localhost" 
 *          when finding STAF logs to export to SAFS logs.
 * @author Carl Nagle 11/03/2006 modified to process mixed-mode UTF-8 strings. 
 * 
 * @since	MAY 19 2009		(LW)	Modify the method getSTAFLogDirectory() to abstract.
 * 									getSTAFLogDirectory() contains the STAF-version related code,
 * 									realize them in subclass can make the code more independant of STAF-version.
*/
public abstract class STAFFileLogItem extends FileLogItem
{
	protected static final int SAFSLOG_MAJOR_VER = 1;
	protected static final int SAFSLOG_MINOR_VER = 1;
	protected static final String LINEFEED = System.getProperty("line.separator");
	protected String STAFVersion = "2.0";
	
	protected HandleInterface handle = null;
	
	//EffectiveMachine (v2) or MachineNickName (v3)
	private String MACHINE_NAME;
	private String LOGNAME_OPTION;
	private String MACHINE_OPTION;
	protected ServiceDebugLog debugLog;

	/**
	 * Creates a <code>STAFFileLogItem</code> with the default log level
	 * (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * @param name		the name of this log. If <code>null</code>, file name is
	 * 					used as the name.
	 * @param mode		the type of this log (<code>LOGMODE</code> constant
	 * 					defined by <code>AbstractLogFacility</code>).
	 * @param enabled	<code>true</code> to enable this log; <code>false</code>
	 * 					to disable.
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public STAFFileLogItem(String name, long mode, boolean enabled, 
		String parent, String file)
	{
		super(name, mode, enabled, parent, file);
	}

	/**
	 * Creates a disabled <code>STAFFileLogItem</code> with default name (file
	 * name) and log level (<code>LOGLEVEL_INFO</code>).
	 * <p>
	 * @param mode		the type of this log (<code>LOGMODE</code> constant
	 * 					defined by <code>AbstractLogFacility</code>).
	 * @param parent	the parent directory for this log.
	 * @param file		the file spec of this log.
	 */
	public STAFFileLogItem(long mode, String parent, String file)
	{
		this(null, mode, false, parent, file);
	}

	/**
	 * Set the STAF handle of this log item.
	 * <p>
	 * @param h		the new STAF handle.
	 */
	public void setHandle(HandleInterface h)
	{
		handle = h;
	}

	/**
	 * Initializes this log by clearing the STAF log with the same name, if any.
	 */
	public void init()
	{
		debugLog.debugPrintln("STAFFileLogItem.init()");

		STAFVersion =STAFHelper.getSTAFVersionString(handle);
		MACHINE_NAME = this.getMachineName();
		debugLog.debugPrintln("STAF version: "+STAFVersion+" ; STAF machine name: "+MACHINE_NAME);
		
		LOGNAME_OPTION = " logname " + STAFUtil.wrapData(this.name) + " ";
		MACHINE_OPTION = " machine "+MACHINE_NAME+" ";
		
		stafLogRequest("delete" + MACHINE_OPTION + LOGNAME_OPTION + "confirm");
	}
	
	/**
	 * Close this log.
	 * <p>
	 * @throws	STAFLogException
	 * 			if this method failed for any reason.
	 */
	public abstract void close() throws STAFLogException;

	/**
	 * Submits a request to the instance of STAF LOG service loaded by the SAFS 
	 * logging service.
	 * <p>
	 * @param request	the service request to submit.
	 * @return			the result of the request.
	 */
	protected STAFResult stafLogRequest(String request)
	{
		debugLog.debugPrintln("STAFFileLogItem.stafLogRequest()");
		debugLog.debugPrintln("request: " + request);


		STAFResult result = handle.submit2("local",AbstractSAFSLoggingService.SLS_STAF_LOG_SERVICE_NAME, request);
		debugLog.debugPrintln("return: ", result);
		return result;
	}

	/**
	 * Logs a message to the STAF log associated with this log item.
	 * <p>
	 * @param message	the MESSAGE option value of the LOG request.
	 * @param level		the LEVEL option value of the LOG request.
	 */
	protected STAFResult stafLogLog(String message, String level)
	{
		return stafLogRequest("log machine" + LOGNAME_OPTION + 
			levelOption(level) + messageOption(message));
	}

	/**
	 * Logs a message of Info level to the associated STAF log.
	 * <p>
	 * @param message	the MESSAGE option value of the LOG request.
	 */
	protected STAFResult stafLogLog(String message)
	{
		return stafLogLog(message, "Info");
	}

	/**
	 * Builds STAF-formatted text for LEVEL option.
	 * <p>
	 * @param	level	the raw LEVEL option value.
	 * @return	the formatted LEVEL option, including both the option name and 
	 * 			value (e.g. " level :4:info ").
	 */
	private String levelOption(String level)
	{
		return " level " + STAFUtil.wrapData(level) + " ";
	}

	/**
	 * Builds STAF-formatted text for MESSAGE option.
	 * <p>
	 * @param	message	the raw message.
	 * @return	the formatted MESSAGE option, including both the option name and 
	 * 			value (e.g. " message :11:how are you ").
	 */
	private String messageOption(String message)
	{
		return " message " + STAFUtil.wrapData(message) + " ";
	}

	/**
	 * Finalizes the detination log file.
	 * <p>
	 * @throws	SAFSLogException
	 * 			if failed to finalize the log file.
	 * @author Carl Nagle 02/24/05 changed use of machine to effectiveMachine to avoid "localhost" when 
	 *          finding STAF logs to export as SAFS logs.
	 */
	protected void finalizeLogFile() throws STAFLogException
	{
		// create parent directory if not exist
		File p = new CaseInsensitiveFile((new CaseInsensitiveFile(getAbsolutePath()).toFile()).getParent()).toFile();
		try
		{
			if (!p.isDirectory())
			{
				debugLog.debugPrintln(
					"STAFFileLogItem.finalizeLogFile(): '" + 
					p.getAbsolutePath() + "' is not directory.");
				if (!p.mkdirs())
				{
					throw new STAFLogException(
						"Failed to create directory " + p.getAbsolutePath(),
						new STAFResult(STAFResult.DoesNotExist, 
							p.getAbsolutePath()));
				}
			}
		}
		catch (Exception e)
		{
			debugLog.debugPrintln(
				"STAFFileLogItem.finalizeLogFile():", e);
			throw new STAFLogException(e.getMessage(),
				new STAFResult(STAFResult.DoesNotExist, p.getAbsolutePath()));
		}

		try
		{
			// export the STAF log content to the destination file.
			String dir = getSTAFLogDirectory();
			String src = "MACHINE"+ File.separator + MACHINE_NAME + File.separator + "GLOBAL" + File.separator + name + ".log";
			debugLog.debugPrintln("STAF export src: " + dir+File.separator+src) ;
			exportSTAFLog(new CaseInsensitiveFile(dir, src).toFile());
		}
		catch(Exception e)
		{
			debugLog.debugPrintln(
				"STAFFileLogItem.finalizeLogFile():", e);
			throw new STAFLogException(e.getMessage(), 
				new STAFResult(STAFResult.FileWriteError, getAbsolutePath()));
		}
	}

	/**
	 * Returns the DIRECTORY setting of the STAF LOG service.
	 * This method is defined as abstract. Because STAF 2 and STAF 3 return
	 * unmarshalled and marshalled result. While to unmarshall the result, we
	 * must import the STAF 3 library; To separate STAF 2 and STAF 3, this
	 * method is defined as abstract, and it will be implemented in STAFTextLogItem,
	 * STAFTextLogItem3, STAFXmlLogItem and STAFXmlLogItem3.
	 * <P>
	 * @return	the Directory setting as returned by LIST SETTINGS request to
	 * 			STAF LOG service.
	 */
	abstract protected String getSTAFLogDirectory();

	/**
	 * Allow subclasses to prepend information to the final destination log.
	 * This implementation does nothing.
	 * @param out
	 */
	//protected void prependFinalLog(PrintWriter out){ ; }
	protected void prependFinalLog(Writer out){ ; }

	/**
	 * Allow subclasses to append information to the final destination log.
	 * This implementation does nothing.
	 * @param out
	 */
	//protected void appendFinalLog(PrintWriter out){ ; }
	protected void appendFinalLog(Writer out){ ; }

	/**
	 * Exports the message fields of all log records of a STAF log to this log
	 * item's destination file.
	 * <p>
	 * This method parsed the STAF log file directly for the log messages and
	 * writes them to the destination file. In terms of performance and memory
	 * consumption, it is better than indirectly querying the log content via
	 * STAF LOG service.
	 * <p>
	 * @param src	contains full path to the source STAF log file.
	 * @throws		IOException
	 * 				if file related error occured.
	 */
	protected void exportSTAFLog(File src) throws IOException
	{
		debugLog.debugPrintln("STAFFileLogItem.exportSTAFLog(): " +
			"source=" + src.getAbsolutePath());

		DataInputStream in = new DataInputStream(new BufferedInputStream(
			new FileInputStream(src)));
		//PrintWriter out = new PrintWriter(
		//	new FileWriter(getAbsolutePath(), false));
		//PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(getAbsolutePath()), Charset.forName("UTF-8")), false);
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(getAbsolutePath()), Charset.forName("UTF-8"));
		prependFinalLog(out);
		
		try 
		{
			do
			{
				// skip the first 16 bytes, including log format id, time stamp,
				// handle and level.
				in.skipBytes(16);
				// skip machine name
				String s = readStringFromSTAFLog(in);
				// for some reason, skipBytes() does not throw EOFException
				// consistently when end of file is reached, so this additional
				// check is for machine name is added.
				if (s == null || s.length() <= 0) break;
				// skip process name
				s = readStringFromSTAFLog(in);
				//If the STAF version is 3, we should skip also 'user' and 'trust machine'
				if(STAFVersion.startsWith("3")){
					//skip 'user', something like "none://anonymous"
					s = readStringFromSTAFLog(in);
					//skip 'trust machine', something like "localhost://localhost"
					s = readStringFromSTAFLog(in);
				}
				// this is the actual log message
				s = readStringFromSTAFLog(in);
				
				//out.println(s);
				out.write(s);
				out.write(LINEFEED);
				
			} while (true);
		}
		catch (EOFException e) {}
		finally 
		{
			debugLog.debugPrintln(
				"STAFFileLogItem.exportSTAFLog(): close io streams");
			in.close();			
			appendFinalLog(out);			
			out.close();
		}
	}

	/**
	 * Read the next string from the STAF log.
	 * <p>
	 * Strings are saved in the STAF log as byte data, with the number of bytes 
	 * (saved as 4-byte long unsigned integer) preceeding the actual data.
	 * <p>
	 * @param in	the STAF log input stream.
	 * @return		the string value read from the input stream.
	 */
	protected String readStringFromSTAFLog(DataInputStream in)
		throws IOException
	{
		long len = readUIntFromSTAFLog(in);
		if (len < 0) return null;
		if (len == 0) return "";

		byte[] b = new byte[(int)len];
		in.read(b);
		String retval = new String(b, "UTF-8");
		//String retval = new String(b);
		debugLog.debugPrintln(
			"STAFFileLogItem.readStringFromSTAFLog(): return=" + retval);
		return retval;
	}

	/**
	 * Reads the next "unsigned integer" from the input stream.
	 * <p>
	 * This method reads the next 4 bytes from the input stream, and convert 
	 * them to a long (because java does not support unsigned data types), using
	 * the first byte as the highest byte and treating each byte as unsigned.
	 * For example:
	 * <pre>
	 * 00 00 01 01 -> 257
	 * 00 00 00 FF -> 255
	 * </pre>
	 * <p>
	 * @param in	the STAF log input stream.
	 * @return		a <code>long</code> equal to the unsigned integer converted 
	 * 				from the bytes.
	 */
	protected long readUIntFromSTAFLog(DataInputStream in) throws IOException
	{
		long[] l = new long[4];
		for (int i = 0; i < 4; i++) 
		{
			l[i] = in.readUnsignedByte();
		}
		long retval = (l[0]<<24) + (l[1]<<16) + (l[2]<<8) + l[3];
		debugLog.debugPrintln(
			"STAFFileLogItem.readUIntFromSTAFLog(): " +
			"read=" + l[0] + " " + l[1] + " " + l[2] + " " + l[3] + "; " +
			"return=" + retval);
		return retval;
	}

	protected void setDebugLog(ServiceDebugLog debugLog){
		this.debugLog = debugLog;
	}
	
	/**
	 * STAF version 2 will use {STAF/Config/EffectiveMachine} to make log. 
	 * While STAF version 3 use (STAF/Config/MachineNickName)
	 * @return		EffectiveMachine (for version 2) of MachineNickName (for version 3)
	 */
	protected String getMachineName(){
		String machineName = "";
		String request = "GLOBAL GET STAF/Config/EffectiveMachine";
		if(STAFVersion.startsWith("3")){
			debugLog.debugPrintln("Try to get MachineNickname.");
			request = "GET SYSTEM VAR STAF/Config/MachineNickname";
		}else{
			debugLog.debugPrintln("Try to get EffectiveMachine.");
		}
		
		STAFResult result = handle.submit2("local", "VAR", request);
		if(result.rc==STAFResult.Ok){
			machineName = result.result ;
		}
		
		return machineName;
	}
}