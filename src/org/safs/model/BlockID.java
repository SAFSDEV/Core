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
