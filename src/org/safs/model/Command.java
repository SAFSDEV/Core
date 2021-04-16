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
