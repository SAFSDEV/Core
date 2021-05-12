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
 * Simple extension to handle "Project Commands" of record type "T".
 * Project Commands are those records that invoke user-defined test tables.
 * For example, a Suite table may invoke a "Login" step table by 
 * creating a Project Command that uses "Login" as the name of the 
 * command.
 * <p>
 * Example:
 * <ul><code><pre>
 * // Login table uses default TAB separator
 * ProjectCommand login = new ProjectCommand("Login", null);
 * login.addParameter( "^userid=Carlos" );
 * login.addParameter( "^password=Santana" );
 * ...
 * suiteTable.add( login );
 * </pre></code>
 * </ul>
 * <p>
 * This is generally only useful if you are trying to invoke a test table 
 * that has not been defined within this API but will exist at runtime. 
 */
public class ProjectCommand extends Command {

   /**
    * Create an instance of a Project Command to invoke a runtime test table.
    * Once created, any additional parameters needed by the command can be added 
    * as usual with addParameter.
    * 
    * @param tableName -- the name of the test table to invoke
    * @param separator -- the separator that table uses to delimit fields.
    * This separator can be null or zero-length if the specified table uses 
    * the default separator or the one used by the calling table.
    * @throws IllegalArgumentException if the tableName is null or zero-length
    */
   public ProjectCommand(String tableName, String separator) {
      super(tableName, PROJECT_COMMAND_RECORD_TYPE);
      addParameter(separator);
   }
}
