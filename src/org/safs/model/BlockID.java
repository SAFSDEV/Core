/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;


/**
 * Represents a BlockID command (record type of "B"). 
 */
public class BlockID extends Command {

   /**
    * Create a BlockID command instance with the specified block ID.
    * 
    * @param blockID the id for this BlockID
    * @throws IllegalArgumentException for null or zero-length block IDs
    */
   public BlockID(String blockID) {
      super(blockID, BLOCKID_RECORD_TYPE);
   }

   /**
    * Returns the block ID for this BlockID command.
    * This is a convenience for <code>getCommandName()</code>.
    * <p>
    * @return the block ID for this BlockID command
    */
   public final String getBlockID() {
      return getCommandName();
   }
}
