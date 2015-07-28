/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;


/**
 * Represents a Skipped command (record type of "S"). 
 */
public class Skipped extends Command {
   
   /**
    * Create a Skipped command instance using the provided message.
    * 
    * @param message the message for the skipped record
    * @throws IllegalArgumentException for null or zero-length messages
    */
   public Skipped(String message) {
      super(message, SKIPPED_RECORD_TYPE);
   }
   
   /**
    * Returns the message for this Skipped command.
    * This is a convenience for <code>getCommandName()</code>.
    * <p>
    * @return the message for this Skipped command
    */
   public final String getMessage() {
      return getCommandName();
   }
}
