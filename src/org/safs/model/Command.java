/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;

/**
 * An extra layer to differentiate between ComponentFunction commands and all other 
 * types of commands.  All commands except ComponentFunction commands will subclass 
 * from this Command class.  ComponentFunction commands subclass AbstractCommand 
 * directly.
 */
public class Command extends AbstractCommand {

   /**
    * Simply provide the pass thru constructor to AbstractCommand.
    */
   protected Command(String commandName, String testRecordID) {
      super(commandName, testRecordID);
   }
   
}
