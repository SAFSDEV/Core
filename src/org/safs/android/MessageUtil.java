/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android;

import org.safs.JavaSocketsUtils;

/**
 * Utilities shared by 3 different processes--local and remote--associated with testing Android applications 
 * with a SAFS Droid Engine.
 * <p>
 * This class defines the message syntax that will be used by the local and remote SocketProtocol 
 * objects to ultimately communicate with a "remote" Android device or emulator being tested.
 * 
 * @author Carl Nagle, SAS Institute, Inc.
 */
public class MessageUtil extends JavaSocketsUtils{

	/** "spcout"
	 * Message sent by remote Process Container client to route message to 
	 * a local Process Container Object Info/File.
	 * <p>
	 * Example message: "spcout:Text=Text in the Box"
	 */
	
	public static final String MSG_ENGINE_SPCOUT   = "spcout"; 
	
	/** "spcmap"
	 * Message sent by remote Process Container client to route message to 
	 * a local Process Container App Map File output.
	 * <p>
	 * Example message: "spcmap:Comp=Class=EditBox;Index=1"
	 */
	public static final String MSG_ENGINE_SPCMAP   = "spcmap"; 
	
	/** "comment"
	 * Message sent by remote client to route a log comment message to 
	 * a local controller.  This is generally used in conjunction with--or instead 
	 * of--placing the messages in the Properties bundle that would normally be returned 
	 * by a resultsProps message.
	 * <p>
	 * Example message: "comment:Attempt to perform the action failed."
	 */
	public static final String MSG_ENGINE_COMMENT = "comment";
	
	/** "detail"
	 * Message sent by remote client to route a comment detail message to 
	 * a local controller.  This is generally used in conjunction with--or instead 
	 * of--placing the messages in the Properties bundle that would normally be returned 
	 * by a resultsProps message.
	 * <p>
	 * Example message: "detail:The object for the action was not found."
	 */
	public static final String MSG_ENGINE_DETAIL = "detail";
	
}
