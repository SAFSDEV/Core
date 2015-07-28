/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;


/**
 * Represents a Breakpoint command (record type of "BP"). 
 */
public class Breakpoint extends Command {
   public Breakpoint() {
      super("Breakpoint"/*I18nOK:EMS*/, BREAKPOINT_RECORD_TYPE);
   }
   
   // Override to only return the test record id.  There are no other values.
   public String exportTestRecord (String fieldSeparator) {
      return getTestRecordID();
   }
}
