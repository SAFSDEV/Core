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

import org.safs.model.commands.DDDriverFlowCommands;

/**
 * The concrete implementation to represent test tables for the Cycle test level.
 * Users can create instances here, or create concrete subclasses.
 * <p>
 * Once the table is created the user can commence adding Commands and SuiteTestTables 
 * to build up the test records that make up this test table.
 * <p>
 * Example:
 * <ul><code><pre>
 * CycleTestTable regression = new CycleTestTable("Regression");
 * regression.add( DDDriverCommands.setApplicationMap("MyApp.MAP"));
 * regression.add( new LoginWinTests() );
 * regression.add( new MainWinTests() );
 * ...
 * </pre></code>
 * </ul>
 * There are many different ways to make this more sophisticated and intuitive when 
 * using your own test table classes and helper classes.  See some of the examples 
 * in org.safs.model.examples
 */
public class CycleTestTable extends AbstractTestTable {
   
   /**
    * Create a new instance of a CycleTestTable with the provided name.
    * 
    * @param testTableName -- the name for this CycleTestTable
    * @throws IllegalArgumentException if the provided name is null or zero-length
    */
   public CycleTestTable(String testTableName) {
      super(testTableName, CYCLE_TABLE_FILE_EXTENSION);
   }
   
   /**
    * Called by internal routines to validate commands as they are added.
    * This routine currently prevents CallSuite and CallStep Driver Commands from 
    * being added to the test table since they are not valid at the Cycle level.
    * 
    * @param command -- the Command to be added to the table
    * @throws IllegalArgumentException if a disallowed command is attempted
    */
   protected void validateCommand(Command command) {
      // Check for call suite and step driver commands...
      if (DRIVER_COMMAND_RECORD_TYPE.equalsIgnoreCase(command.getTestRecordID())) {
         if (DDDriverFlowCommands.CALLSUITE_KEYWORD.equalsIgnoreCase(command.getCommandName()))
            throw new IllegalArgumentException("The \"" + DDDriverFlowCommands.CALLSUITE_KEYWORD + "\" driver command cannot be called from a cycle test table.");
         if (DDDriverFlowCommands.CALLSTEP_KEYWORD.equalsIgnoreCase(command.getCommandName()))
            throw new IllegalArgumentException("The \"" + DDDriverFlowCommands.CALLSTEP_KEYWORD + "\" driver command cannot be called from a cycle test table.");
      }
   }
   
    /**
     * Add a SuiteTestTable to the CycleTestTable just like any other valid command.
     * 
     * @param table SuiteTestTable to be added\invoked just like any other command.
     * @throws IllegalArgumentException if the table is null
     */
    public void add (SuiteTestTable table){
        if (table == null)
            throw new IllegalArgumentException("Cannot add a null table.");
        _commands.add(table);
    }
}
