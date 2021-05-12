/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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
package org.safs.rational;

import java.util.*;

import org.safs.*;

/**
 * <br><em>Purpose:</em> CFDatabase, process a database driver commands
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author Doug Bauman
 * @since   Oct 27, 2003
 *
 *   <br>   Oct 27, 2003    (DBauman) Original Release
 **/
public class CFDatabase extends CFComponent {

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFDatabase () {
    super();
  }

  /** <br><em>Purpose:</em> overrides parent: in our local version we do not get 'obj1',
   **/
  protected void getHelpers() throws SAFSException{
    getHelpersWorker();
    script = ((RTestRecordData)testRecordData).getScript();
    // DON'T do this unless the action will go back to the parent
    if (!DatabaseCommandsHelper.equalsDatabaseCommand(action)) {
      obj1 = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName);
    }
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is the driver command processor for database commands.
   ** <br> 
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing; processedCommand is set to false
   * if we do not recognize this command
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
    try {
      if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.COPYDBTABLECOLUMNTOFILE)) {
        copyDBTableColumnToFile();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.COPYDBTABLETOFILE)) {
        copyDBTableToFile();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.DELETEDBTABLERECORDS)) {
        deleteDBTableRecords();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.EXECSQLQUERY)) {
        execSQLQuery();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.EXECSQLCOMMIT)) {
        execSQLCommit();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.GETDBTABLECOLUMNCOUNT)) {
        getDBTableColumnCount();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.GETDBTABLEROWCOUNT)) {
        getDBTableRowCount();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.GETDBVALUE)) {
        getDBValue();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.VERIFYDATABASENULLVALUE)) {
        verifyDatabaseNullValue();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.VERIFYDATABASEVALUE)) {
        verifyDatabaseValue();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.VERIFYDBNULLVALUE)) {
        verifyDBNullValue();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.VERIFYDBVALUE)) {
        verifyDBValue();
      }
    } catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> copyDBTableColumnToFile
   **/
  private void copyDBTableColumnToFile () throws SAFSException {
    DatabaseCommandsHelper.copyDBTableColumnToFile(this);
  }

  /** <br><em>Purpose:</em> copyDBTableToFile
   **/
  private void copyDBTableToFile () throws SAFSException {
    DatabaseCommandsHelper.copyDBTableToFile(this);
  }

  /** <br><em>Purpose:</em> deleteDBTableRecords
   **/
  private void deleteDBTableRecords () throws SAFSException {
    DatabaseCommandsHelper.deleteDBTableRecords(this);
  }

  /** <br><em>Purpose:</em> execSQLQuery
   **/
  private void execSQLQuery () throws SAFSException {
    DatabaseCommandsHelper.execSQLQuery(this);
  }
  
  /** <br><em>Purpose:</em> execSQLQuery
   **/
  private void execSQLCommit () throws SAFSException {
    DatabaseCommandsHelper.execSQLCommit(this);
  }

  /** <br><em>Purpose:</em> getDBTableColumnCount
   **/
  private void getDBTableColumnCount () throws SAFSException {
    DatabaseCommandsHelper.getDBTableColumnCount(this);
  }

  /** <br><em>Purpose:</em> getDBTableRowCount
   **/
  private void getDBTableRowCount () throws SAFSException {
    DatabaseCommandsHelper.getDBTableRowCount(this);
  }

  /** <br><em>Purpose:</em> getDBValue
   **/
  private void getDBValue () throws SAFSException {
    DatabaseCommandsHelper.getDBValue(this);
  }

  /** <br><em>Purpose:</em> verifyDatabaseNullValue
   **/
  private void verifyDatabaseNullValue () throws SAFSException {
    DatabaseCommandsHelper.verifyDatabaseNullValue(this);
  }

  /** <br><em>Purpose:</em> verifyDatabaseValue
   **/
  private void verifyDatabaseValue () throws SAFSException {
    DatabaseCommandsHelper.verifyDatabaseValue(this);
  }

  /** <br><em>Purpose:</em> verifyDBNullValue
   **/
  private void verifyDBNullValue () throws SAFSException {
    DatabaseCommandsHelper.verifyDBNullValue(this);
  }

  /** <br><em>Purpose:</em> verifyDBValue
   **/
  private void verifyDBValue () throws SAFSException {
    DatabaseCommandsHelper.verifyDBValue(this);
  }
}
