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
package org.safs.staf.embedded;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;

public interface HandleInterface {

	public void setHandleId(String handleId);
	public void register() throws STAFException;
	public String submit(String where, String handler, String request) throws STAFException;
	public STAFResult submit2(String where, String handler, String request);
	
	/**
	 * @param target -- destination handle name for the message.
	 * @param message -- the message to send.
	 */
	public STAFResult sendQueueMessage(String target, String message) throws STAFException;
	public STAFResult acceptQueueMessage(String message);
	public STAFResult getQueueMessage(int timeout);
	public int getHandle();
	public void unRegister()throws STAFException;
}
