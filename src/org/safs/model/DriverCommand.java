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
 * Simple extension to handle Driver Commands of record type "C" 
 */
public class DriverCommand extends Command {

   /**
    * Create a Driver Command instance using the provided command name.
    * This is used by the org.safs.model.commands classes and is not typically 
    * used by the user unless they are building a custom Driver Command.
    * 
    * @param commandName -- cannot be null or zero-length
    * @throws IllegalArgumentException for null or zero-length command names
    */
   public DriverCommand(String commandName) {
      super(commandName, DRIVER_COMMAND_RECORD_TYPE);
   }
   
   private boolean _warningOK;
   private boolean _failureOK;
   
   /**
    * Indicates if a warning is acceptable for this DriverCommand.
    * <p>
    * @return <code>true</code> if a warning is acceptable, otherwise <code>false</code>
    * @see #setWarningOK(boolean)
    */
   public final boolean isWarningOK() {
      return _warningOK;
   }
   
   /**
    * Sets whether a warning is acceptable for this DriverCommand.
    * <p>
    * If a warning is set as acceptable, <code>setFailureOK(false)</code>
    * is called to turn off failures.
    * <p>
    * @param newValue <code>true</code> to indicate that warnings are acceptable, otherwise <code>false</code>
    * @see #isWarningOK()
    */
   public final void setWarningOK(boolean newValue) {
      if (isWarningOK() == newValue)
         return;
      _warningOK = newValue;
      if (newValue)
         setFailureOK(false);
   }
   
   /**
    * Indicates if a failure is acceptable for this DriverCommand.
    * <p>
    * @return <code>true</code> if a failure is acceptable, otherwise <code>false</code>
    * @see #setFailureOK(boolean)
    */
   public final boolean isFailureOK() {
      return _failureOK;
   }
   
   /**
    * Sets whether a failure is acceptable for this DriverCommand.
    * <p>
    * If a failure is set as acceptable, <code>setWarningOK(false)</code>
    * is called to turn off warnings.
    * <p>
    * @param newValue <code>true</code> to indicate that failures are acceptable, otherwise <code>false</code>
    * @see #isFailureOK()
    */
   public final void setFailureOK(boolean newValue) {
      if (isFailureOK() == newValue)
         return;
      _failureOK = newValue;
      if (newValue)
         setWarningOK(false);
   }

   /**
    * Returns the string record type for this command.  The set of values for DriverCommand
    * commands are:
    * <ul>
    * <li>"CF" - if <code>failureOK</code> is <code>true</code>
    * <li>"CW" - if <code>warningOK</code> is <code>true</code>
    * <li>"C" - if <code>failureOK</code> and <code>warningOK</code> are <code>false</code>
    * </ul>
    * <p>
    * @return the record type of this command
    */
   public String getTestRecordID() {
      if (isFailureOK())
         return DRIVER_COMMAND_FAILOK_RECORD_TYPE;
      if (isWarningOK())
         return DRIVER_COMMAND_WARNOK_RECORD_TYPE;
      return super.getTestRecordID();
   }
}
