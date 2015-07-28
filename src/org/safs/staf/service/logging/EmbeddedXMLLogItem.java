package org.safs.staf.service.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.safs.SAFSRuntimeException;
import org.safs.logging.MessageTypeInfo;
import org.safs.tools.CaseInsensitiveFile;


public class EmbeddedXMLLogItem extends AbstractSTAFXmlLogItem {

	protected OutputStreamWriter out;
	
	public EmbeddedXMLLogItem(String name, String parent, String file) {
		super(name, parent, file);
	}

	public EmbeddedXMLLogItem(String parent, String file) {
		super(parent, file);
	}

	public EmbeddedXMLLogItem(String file) {
		super(file);
	}

	@Override
	protected String getSTAFLogDirectory() {		
		return null;
	}
	
	protected void write(String msg){
		try{ 
			out.append(msg);
			out.write(LINEFEED);
			out.flush();
		}
		catch(IOException x){
			debugLog.debugPrintln("EmbeddedXMLLogItem.write(): IOException, "+ x.getMessage());
		}
	}
	
	@Override
	public void logMessage(String msg, String desc, int msgType){
		if (!enabled || closed || out == null || msg == null || msg.length() <= 0) return;
		MessageTypeInfo info = MessageTypeInfo.get(msgType);
		if (info == null) info = MessageTypeInfo.get(LocalSLSLogFacility.GENERIC_MESSAGE);
		write(formatMessage(msg, desc, info));
	}

	@Override
	public void init(){
		debugLog.debugPrintln("EmbeddedXMLLogItem.init()");
		
		File f = new CaseInsensitiveFile(getAbsolutePath()).toFile();
		File p = new CaseInsensitiveFile(f.getParent()).toFile();
		if (!p.isDirectory()){
			debugLog.debugPrintln("EmbeddedXMLLogItem.init(): '" + p.getAbsolutePath() + "' is not directory.");
			if (!p.mkdirs()){
				debugLog.debugPrintln("EmbeddedXMLLogItem.init(): Failed to create logging directory " + 
			                           p.getAbsolutePath());
				throw new SAFSRuntimeException("EmbeddedXMLLogItem.init(): Failed to create logging directory " + 
			                                    p.getAbsolutePath());
			}
		}
		if(f.exists()) f.delete();
		try{ 
			out = new OutputStreamWriter(new FileOutputStream(f.getAbsolutePath()), Charset.forName("UTF-8")); 
			prependFinalLog(out);
			debugLog.debugPrintln("EmbeddedXMLLogItem.init(): '" + p.getAbsolutePath() + "' created.");
		}catch(Exception x){
			debugLog.debugPrintln("EmbeddedXMLLogItem.init(): '" + p.getAbsolutePath() + "' creation failure: "+
		    x.getClass().getSimpleName()+", "+ x.getMessage());
			throw new SAFSRuntimeException("EmbeddedXMLLogItem.init(): '" + p.getAbsolutePath() + "' creation failure.");
		}		
	}
	
	@Override
	protected void prependFinalLog(Writer out){
		super.prependFinalLog(out);
		write("<LOG_OPENED date='"+ dateTime(0)+"' time='"+ dateTime(1)+"' />");
		write("<LOG_VERSION major='1' minor='1' />");
	}
	
	@Override
	protected void appendFinalLog(Writer out){
		write("<LOG_CLOSED date='" + dateTime(0) + "' time='" + dateTime(1) + "'/>");
		super.appendFinalLog(out);
	}
	
	/**
	 * Close this log if enabled and open.
	 * <p>
	 * @throws	STAFLogException
	 * 			if this method failed for any reason.
	 */
	@Override
	public void close() throws STAFLogException
	{
		debugLog.debugPrintln("EmbeddedXMLLogItem.close()");
	// if not enabled or already closed, do nothing
		if (!enabled || out == null || closed) return;
		finalizeLogFile();
	}

	@Override
	protected void finalizeLogFile()throws STAFLogException{
		debugLog.debugPrintln("EmbeddedXMLLogItem.finalizeLogFile()");
		appendFinalLog(out);
		try{ out.flush();}catch(Exception x){
			debugLog.debugPrintln("EmbeddedXMLLogItem.finalizeLogFile() "+ x.getClass().getSimpleName()+", "+ x.getMessage());
		}
		try{ out.close();}catch(Exception x){
			debugLog.debugPrintln("EmbeddedXMLLogItem.finalizeLogFile() "+ x.getClass().getSimpleName()+", "+ x.getMessage());
		}		
		out = null;
		closed = true;
	}
}
