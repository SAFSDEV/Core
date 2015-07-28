/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;

/**
 * The concrete implementation to represent test tables for the Step test level.
 * Users can create instances here, or create concrete subclasses.
 * <p>
 * Once the table is created the user can commence adding Commands and ComponentFunctions  
 * to build up the test records that make up this test table.
 * <p>
 * Example:
 * <ul><code><pre>
 * StepTestTable badUserTest = new StepTestTable("BadUserIDTest");
 * 
 * // using org.safs.model.commands
 * badUserTest.add( DDDriverCommands.setApplicationMap("MyApp.MAP"));
 * badUserTest.add( EditBoxFunctions.setTextValue( "LoginWin", "UserField", "BogusName" ));
 * 
 * // alternatives using org.safs.model.components
 * badUserTest.add( LoginWin.PasswordField.setTextValue( "BogusPassword" ));
 * badUserTest.add( LoginWin.OKButton.click( ));
 * ...
 * </pre></code>
 * </ul>
 * There are many different ways to make this more sophisticated and intuitive when 
 * using your own test table classes and helper classes.  See some of the examples 
 * in org.safs.model.examples
 */
public class StepTestTable extends AbstractTestTable {
   
    /**
     * Create a new instance of a StepTestTable with the provided name.
     * 
     * @param testTableName -- the name for this StepTestTable
     * @throws IllegalArgumentException if the provided name is null or zero-length
     */
    public StepTestTable(String testTableName) {
        super(testTableName, STEP_TABLE_FILE_EXTENSION);
    }

    /**
     * Add a ComponentFunction to the StepTestTable just like any other valid command.
     * Note that ComponentFunctions are ONLY valid at the Step test level.
     * 
     * @param table ComponentFunction to be added\invoked just like any other command.
     * @throws IllegalArgumentException if the command is null
     */
    public void add(ComponentFunction componentFunction) {
        if (componentFunction == null)
            throw new IllegalArgumentException("Cannot add a null Component Function command.");
        _commands.add(componentFunction);
    }
}