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
package org.safs.staf.service.logging;
/**
 * JUN 05, 2018	(Lei Wang) Removed method prependFinalLog(), appendFinalLog().
 *                        Modified method init() and close(): write init/close messages properly by superclass method.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

	@Override
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
			writeInitMessages();
			debugLog.debugPrintln("EmbeddedTextLogItem.init(): '" + p.getAbsolutePath() + "' created.");
		}catch(Exception x){
			debugLog.debugPrintln("EmbeddedTextLogItem.init(): '" + p.getAbsolutePath() + "' creation failure: "+
		    x.getClass().getSimpleName()+", "+ x.getMessage());
			throw new SAFSRuntimeException("EmbeddedTextLogItem.init(): '" + p.getAbsolutePath() + "' creation failure.");
		}
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
		writeCloseMessages();
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
