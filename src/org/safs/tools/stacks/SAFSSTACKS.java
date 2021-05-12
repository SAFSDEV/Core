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
package org.safs.tools.stacks;

import org.safs.tools.CoreInterface;
import org.safs.tools.SimpleToolsInterface;
import org.safs.tools.status.StatusInfoInterface;

public class SAFSSTACKS
	implements SimpleToolsInterface, StacksInterface {

	/**
	 * Constructor for SAFSSTACKS
	 */
	public SAFSSTACKS() {
		super();
	}

	/**
	 * @see SimpleToolsInterface#launchInterface()
	 */
	public void launchInterface() {
	}

	/**
	 * @see StacksInterface#pushStack(StatusInfoInterface)
	 */
	public long pushStack(StatusInfoInterface statusInfo) {
		return 0;
	}

	/**
	 * @see StacksInterface#peekStack()
	 */
	public StatusInfoInterface peekStack() {
		return null;
	}

	/**
	 * @see StacksInterface#popStack()
	 */
	public StatusInfoInterface popStack() {
		return null;
	}

	/**
	 * @see StacksInterface#isEmpty()
	 */
	public boolean isEmpty() {
		return false;
	}

	/**
	 * @see StacksInterface#count()
	 */
	public long count() {
		return 0;
	}

	/**
	 * @see GenericToolsInterface#getCoreInterface()
	 * @throws IllegalStateException ALWAYS since there is no CoreInterface in this class.
	 */
	public CoreInterface getCoreInterface() throws IllegalStateException {
		throw new IllegalStateException("SAFSSTACKS does not interface with the core framework directly.");
	}
	
    /** 
     * TRUE if the underlying tool is running.
     * Typically called AFTER initialization attempts have been made.
     */
	public boolean isToolRunning(){
		return false;
	}
	
	/**
	 * @see GenericToolsInterface#reset()
	 */
	public void reset() {
	}

	/**
	 * @see GenericToolsInterface#shutdown()
	 */
	public void shutdown() {
	}

}

