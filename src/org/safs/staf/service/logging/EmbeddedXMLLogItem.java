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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * APR 28, 2018	(Lei Wang) Used constant instead 'hard-coded string' to generate XML Log file.
 * JUN 05, 2018 (Lei Wang) Removed method prependFinalLog(), appendFinalLog().
 *                        Modified method init() and close(): write init/close messages properly by superclass method.
 * 
 */
package org.safs.staf.service.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
			writeInitMessages();
			debugLog.debugPrintln("EmbeddedXMLLogItem.init(): '" + p.getAbsolutePath() + "' created.");
		}catch(Exception x){
			debugLog.debugPrintln("EmbeddedXMLLogItem.init(): '" + p.getAbsolutePath() + "' creation failure: "+
		    x.getClass().getSimpleName()+", "+ x.getMessage());
			throw new SAFSRuntimeException("EmbeddedXMLLogItem.init(): '" + p.getAbsolutePath() + "' creation failure.");
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
		debugLog.debugPrintln("EmbeddedXMLLogItem.close()");
	// if not enabled or already closed, do nothing
		if (!enabled || out == null || closed) return;
		writeCloseMessages();
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
