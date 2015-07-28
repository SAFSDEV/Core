/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

/**
 * This interface is generally intended to be a flag used by LocalAgents to indicate 
 * they request a reference to the instance of the LocalAgentFactory that instantiated 
 * them.  This allows these classes to retrieve other related LocalAgents without creating 
 * additional instances of them.  Why, do you ask?
 * <p>
 * Use a JFrameAgent as an example.  There is a JFrameAgent in the org.safs.jvmagent 
 * package which is a subclass of the FrameAgent in the same jvmagent package.  The 
 * org.safs.abbot.JFrameAgent is a subclass of this org.safs.jvmagent.JFrameAgent class.
 * However, when the abbot.JFrameAgent wishes to invoke certain functions for component 
 * processing on a "superclass" like JComponentAgent it does not want to go up its 
 * true jvmagent superclass hierarchy, it wants to go up the abbot superclass hierarchy 
 * of which it is not a true descendant.  
 * <p>
 * Consequently, this class would implement the LocalAgentFactoryUser interface and 
 * the LocalAgentFactory will provide a reference so that the class can issue callbacks 
 * for the abbot superclass instance it desires.
 * 
 * @author canagl
 * @since Mar 31, 2005
 */
public interface LocalAgentFactoryUser {
	
	/** 
	 * Set the LocalAgentFactory used by the class instance.
	 * This is normally set by the LocalAgentFactory itself when the User is instanced.
	 */
	public void setLocalAgentFactory (LocalAgentFactory factory);

	/** 
	 * Retrieve any LocalAgentFactory set for this object.
	 * May be null if not set.
	 */
	public LocalAgentFactory getLocalAgentFactory ();
}
