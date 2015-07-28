package org.safs.tools.stacks;

import org.safs.tools.status.StatusInfoInterface;

/** 
 * Intended as simple Stack storage.  
 * Normally wouldn't actually communicate with tools or services directly. **/
public interface StacksInterface {

	public long pushStack (StatusInfoInterface statusInfo);
	public StatusInfoInterface peekStack ();
	public StatusInfoInterface popStack ();
	public boolean isEmpty ();
	public long count();
}

