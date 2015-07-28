/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.io.*;
import java.util.*;

import org.safs.*;

/**
 * <br><em>Purpose:</em> DCDriverDatabaseCommands, process a database driver commands
 * <br><em>Lifetime:</em> instantiated by DCDriverCommand
 * im
 * <p>
 * @author Doug Bauman
 * @since   Oct 27, 2003
 *
 *   <br>   Oct 27, 2003    (DBauman) Original Release
 **/
public class DCDriverDatabaseCommands extends DriverCommand {

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public DCDriverDatabaseCommands () {
    super();
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
  public void process() {
    try {
      if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.SETJDBCDRIVER)) {
        setJdbcDriver();
      } else if (testRecordData.getCommand().equalsIgnoreCase(DatabaseCommandsHelper.COPYDBTABLECOLUMNTOFILE)) {
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
      } else {
        setRecordProcessed(false);
      }
    } catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> setJdbcDriver
   **/
  private void setJdbcDriver () throws SAFSException {
    DatabaseCommandsHelper.setJdbcDriver(this);
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

