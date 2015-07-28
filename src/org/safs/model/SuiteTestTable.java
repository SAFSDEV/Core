/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;

import org.safs.model.commands.DDDriverFlowCommands;


/**
 * The concrete implementation to represent test tables for the Suite test level.
 * Users can create instances here, or create concrete subclasses.
 * <p>
 * Once the table is created the user can commence adding Commands and StepTestTables 
 * to build up the test records that make up this test table.
 * <p>
 * Example:
 * <ul><code><pre>
 * SuiteTestTable loginTests = new SuiteTestTable("LoginTests");
 * loginTests.add( DDDriverCommands.setApplicationMap("MyApp.MAP"));
 * loginTests.add( new BadUserIDTest() );
 * loginTests.add( new BadPasswordTest() );
 * loginTests.add( new GoodLoginTest() );
 * ...
 * </pre></code>
 * </ul>
 * There are many different ways to make this more sophisticated and intuitive when 
 * using your own test table classes and helper classes.  See some of the examples 
 * in org.safs.model.examples
 */
public class SuiteTestTable extends AbstractTestTable {
   
    /**
     * Create a new instance of a SuiteTestTable with the provided name.
     * 
     * @param testTableName -- the name for this SuiteTestTable
     * @throws IllegalArgumentException if the provided name is null or zero-length
     */
    public SuiteTestTable(String testTableName) {
       super(testTableName, SUITE_TABLE_FILE_EXTENSION);
    }
   
    /**
     * Called by internal routines to validate commands as they are added.
     * This routine currently prevents CallStep Driver Commands from 
     * being added to the test table since they are not valid at the Suite level.
     * 
     * @param command -- the Command to be added to the table
     * @throws IllegalArgumentException if a disallowed command is attempted
     */
    protected void validateCommand(Command command) {
        // Check for call step driver commands...
        if (command.getTestRecordID().equalsIgnoreCase(DRIVER_COMMAND_RECORD_TYPE)) {
            if (DDDriverFlowCommands.CALLSTEP_KEYWORD.equalsIgnoreCase(command.getCommandName()))
                throw new IllegalArgumentException("The \"" + DDDriverFlowCommands.CALLSTEP_KEYWORD + "\" driver command cannot be called from a suite test table.");
        }
    }
   
    /**
     * Add a StepTestTable to the SuiteTestTable just like any other valid command.
     * 
     * @param table StepTestTable to be added\invoked just like any other command.
     * @throws IllegalArgumentException if the table is null
     */
    public void add (StepTestTable table){
        if (table == null)
            throw new IllegalArgumentException("Cannot add a null table.");
        _commands.add(table);
    }
}