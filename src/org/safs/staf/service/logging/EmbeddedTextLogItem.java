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

public class EmbeddedTextLogItem extends AbstractSTAFTextLogItem {

	protected OutputStreamWriter out;
		
	public EmbeddedTextLogItem(String name, String parent, String file) {
		super(name, parent, file);
	}

	public EmbeddedTextLogItem(String parent, String file) {
		super(parent, file);
	}

	public EmbeddedTextLogItem(String file) {
		super(file);
	}

	@Override
	protected String getSTAFLogDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	protected void write(String msg){
		try{ 
			out.append(msg);
			out.write(LINEFEED);
			out.flush();
		}catch(IOException x){
			debugLog.debugPrintln("EmbeddedTextLogItem.write(): IOException, "+ x.getMessage());
		}
	}
	
	@Override
	public void logMessage(String msg, String desc, int msgType){
		debugLog.debugPrintln("EmbeddedTextLogItem.logMessage(): "+ msg);
		if (!enabled || closed || out == null || msg == null || msg.length() <= 0) return;
		// format and log the message.
		MessageTypeInfo info = MessageTypeInfo.get(msgType);
		if (info == null) info = MessageTypeInfo.get(LocalSLSLogFacility.GENERIC_MESSAGE);
		write(info.textPrefix + msg);
		if (desc != null && desc.length() > 0){
			info = MessageTypeInfo.get(LocalSLSLogFacility.GENERIC_MESSAGE);
			write(info.textPrefix + desc);
		}
	}

	@Override
	public void init(){
		debugLog.debugPrintln("EmbeddedTextLogItem.init()");
		
		File f = new CaseInsensitiveFile(getAbsolutePath()).toFile();
		File p = new CaseInsensitiveFile(f.getParent()).toFile();
		if (!p.isDirectory()){
			debugLog.debugPrintln("EmbeddedTextLogItem.init(): '" + p.getAbsolutePath() + "' is not directory.");
			if (!p.mkdirs()){
				debugLog.debugPrintln("EmbeddedTextLogItem.init(): Failed to create logging directory " + 
			                           p.getAbsolutePath());
				throw new SAFSRuntimeException("EmbeddedTextLogItem.init(): Failed to create logging directory " + 
			                                    p.getAbsolutePath());
			}
		}
		if(f.exists()) f.delete();
		try{ 
			out = new OutputStreamWriter(new FileOutputStream(f.getAbsolutePath()), Charset.forName("UTF-8")); 
			prependFinalLog(out);
			debugLog.debugPrintln("EmbeddedTextLogItem.init(): '" + p.getAbsolutePath() + "' created.");
		}catch(Exception x){
			debugLog.debugPrintln("EmbeddedTextLogItem.init(): '" + p.getAbsolutePath() + "' creation failure: "+
		    x.getClass().getSimpleName()+", "+ x.getMessage());
			throw new SAFSRuntimeException("EmbeddedTextLogItem.init(): '" + p.getAbsolutePath() + "' creation failure.");
		}		
	}
	
	protected void prependFinalLog(Writer out){
		write("Version 1.1");
		write("Log OPENED " + dateTime(0) + " " + dateTime(1) );
	}
	
	protected void appendFinalLog(Writer out){
		write("Log CLOSED " + dateTime(0) + " " + dateTime(1) );
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
		debugLog.debugPrintln("EmbeddedTextLogItem.close()");
	// if not enabled or already closed, do nothing
		if (!enabled || out == null || closed) return;
		finalizeLogFile();
	}

	@Override
	protected void finalizeLogFile()throws STAFLogException{
		debugLog.debugPrintln("EmbeddedTextLogItem.finalizeLogFile()");
		appendFinalLog(out);
		try{ out.flush();}catch(Exception x){
			debugLog.debugPrintln("EmbeddedTextLogItem.finalizeLogFile() "+ x.getClass().getSimpleName()+", "+ x.getMessage());
		}
		try{ out.close();}catch(Exception x){
			debugLog.debugPrintln("EmbeddedTextLogItem.finalizeLogFile() "+ x.getClass().getSimpleName()+", "+ x.getMessage());
		}		
		out = null;
		closed = true;
	}
	
}
