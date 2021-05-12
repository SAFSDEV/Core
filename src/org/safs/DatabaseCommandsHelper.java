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
package org.safs;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.safs.tools.CaseInsensitiveFile;

/**
 * <br><em>Purpose:</em> DatabaseCommandsHelper, used by DCDriverCommands.
 * <br><em>Lifetime:</em> static methods
 * <p>
 * @author Doug Bauman
 * @since   Oct 27, 2003
 *
 *   <br>   Nov 05, 2003    (DBauman) Original Release
 **/
public class DatabaseCommandsHelper {

  public static final String SETJDBCDRIVER                 = "SetJdbcDriver";
  public static final String COPYDBTABLECOLUMNTOFILE       = "CopyDBTableColumnToFile";
  public static final String COPYDBTABLETOFILE             = "CopyDBTableToFile";
  public static final String DELETEDBTABLERECORDS          = "DeleteDBTableRecords";
  public static final String EXECSQLQUERY                  = "ExecSQLQuery";
  public static final String EXECSQLCOMMIT                 = "ExecSQLCommit";
  public static final String GETDBTABLECOLUMNCOUNT         = "GetDBTableColumnCount";
  public static final String GETDBTABLEROWCOUNT            = "GetDBTableRowCount";
  public static final String GETDBVALUE                    = "GetDBValue";
  public static final String VERIFYDATABASENULLVALUE       = "VerifyDatabaseNullValue";
  public static final String VERIFYDATABASEVALUE           = "VerifyDatabaseValue";
  public static final String VERIFYDBNULLVALUE             = "VerifyDBNullValue";
  public static final String VERIFYDBVALUE                 = "VerifyDBValue";

  // terrible hack for performance and explicit commit sake
  protected static String lastConnUrl = null;
  protected static String lastConnUser = null;
  protected static String lastConnPass = null;
  protected static Connection lastConn = null;

  public static boolean equalsDatabaseCommand (String command) {
    Log.debug(".....equalsDatabaseCommand...........: "+command);
    boolean result =
      command.equalsIgnoreCase(DatabaseCommandsHelper.SETJDBCDRIVER) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.COPYDBTABLECOLUMNTOFILE) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.COPYDBTABLETOFILE) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.DELETEDBTABLERECORDS) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.EXECSQLQUERY) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.EXECSQLCOMMIT) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.GETDBTABLECOLUMNCOUNT) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.GETDBTABLEROWCOUNT) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.GETDBVALUE) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.VERIFYDATABASENULLVALUE) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.VERIFYDATABASEVALUE) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.VERIFYDBNULLVALUE) ||
      command.equalsIgnoreCase(DatabaseCommandsHelper.VERIFYDBVALUE);
    Log.debug(".....equalsDatabaseCommand.............: "+result);
    return result;
  }

  /** <br><em>Purpose:</em>      NOT USED: static map wich cross references
   ** to actual connections
   ** <br><em>Initialized:</em>  here
   **/
  //private static Map dbMap = new HashMap(); //NOT USED

  /** <br><em>Purpose:</em>      loaded river
   ** <br><em>Initialized:</em>  'loadDriver' method.
   ** <br> this field is lazily used by 'getConnection' to determine if *ANY* driver
   ** has yet been loaded, if none, then it calls 'loadDriver(null)' to load the
   ** defalult driver (jdbc:odbc)
   **/
  private static Class loadedDriver = null;

  /** <br><em>Purpose:</em> getConnection;
   * <br> does this first:  if (loadedDriver == null) loadDriver(null);
   * <br> then opens the connection
   * @param                     url, String
   * @param                     user, String
   * @param                     pass, String
   * @return                    Connection
   * @throws                 SQLException
   **/
  protected static Connection getConnection (String url,
                                             String user, String pass) throws SQLException {
    Connection con;

    if (loadedDriver == null) loadDriver(null);

    // was this our last connection?
    if ((url.equals(lastConnUrl) &&
         user.equals(lastConnUser) &&
         pass.equals(lastConnPass)) &&
         (lastConn != null) && (!lastConn.isClosed()))
    {
    	Log.info("returning existing con for url: " + url);
    	con = lastConn;
    } else {
    	if ((lastConn != null) && (!lastConn.isClosed())) {
    		lastConn.close();
        }
        Log.info("getting con for url: "+url+", user:"+user+", pass:"+pass);
    	con = DatabaseUtils.getConnection(url, user, pass);
    	lastConnUrl = url;
        lastConnUser = user;
        lastConnPass = pass;
        lastConn = con;
    }
    Log.info("got con:"+con);
    return con;
  }

  /** <br><em>Purpose:</em> load driver
   * @param driver, String, if null will load the DEFAULT_DRIVER
   * @throws                 SQLException
   **/
  protected static Class loadDriver(String driver) throws SQLException {
    Log.info("loading driver: "+driver);
    loadedDriver = DatabaseUtils.loadDriver(driver);
    Log.info("loaded: "+loadedDriver);
    return loadedDriver;
  }

  /** <br><em>Purpose:</em> setJdbcDriver
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void setJdbcDriver (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 0) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: DriverClassName",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    String driver = (String)iterator.next();
    try {
      loadDriver(driver);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok ",
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (SQLException se) {
      //se.printStackTrace();
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams +
                                      ": "+se.getMessage()+": "+driver,
                                      Processor.FAILED_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> copyDBTableColumnToFile
   ** <p>
   ** This routine will export the contents of a DBTable to a delimeted file.
   ** <p>
   ** A data source name and Query String is required.
   ** <br>
   ** <br> 0. ADBAlias - ignored
   ** <br> 1. ATableAlias - ignored
   ** <br> 2. DBSourceName
   ** <br>     Name of the Datasource containing the DBTable
   ** <br> 3. FileOut
   ** <br>     Name of the output file.
   ** <br> 4. SQLQuery
   ** <br>     The file will contain the results of the executed Query. The query is NOT validated by this function.
   ** <br> 5. [ DirectoryOut ]
   ** <br>     If not specified the file will be stored in the default test directory.
   ** <br> 6. [ SQLStatus ]
   ** <br>      A variable which holds the current SQL run status.
   ** <br> 7. [ delimiter ]
   ** <br>     If not specified the default ',' seperator will be used.
   ** <br> 8. [ UserID ]
   ** <br>      UserID for accessing the Datasource (if required).
   ** <br> 9. [ Password ]
   ** <br>      Password for accessing the Datasource (if required).
   ** <br>Examples:
   ** <br>    C, CopyDBTableColumnToFile, ADBAlias, ATableAlias, Data Source Name (dsn), OutputFileName, [SQLQuery], [OutputDirectory], [Delimiter], [sqlStatus], "[UID]", "[PWD]"
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void copyDBTableColumnToFile (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 4) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBSourceName, OutputFileName, "+
                                      "SQLQuery, [OutputDirectory], [sqlStatus], [Delimiter], "+
                                      "[UID], [PWD]",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String dbSourceName = (String) iterator.next();
    String fileOut = (String) iterator.next();
    String sqlQuery = (String) iterator.next();
    String directoryOut = null;
    String sqlStatusCode = null;
    String delimiter = "\t";
    String userID = null;
    String password = null;
    try { // optional params
      directoryOut = (String) iterator.next();
      sqlStatusCode = (String) iterator.next();
      delimiter = (String) iterator.next();
      if (delimiter.trim().equals("")) delimiter = "\t";
      userID = (String) iterator.next();
      password = (String) iterator.next();
    } catch (NoSuchElementException nsee) { // ignore
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);
      List list = DatabaseUtils.getValues(con, sqlQuery, null, null, null, null, null,
                                          null, null);
      if (list == null) {
        sqlStatusCodeVal = "NO ROWS";
      } else {
        if (directoryOut!=null && !directoryOut.equals("")) {
          File file = new CaseInsensitiveFile(directoryOut, fileOut).toFile();
          fileOut = file.getAbsolutePath();
        }
        File file = new CaseInsensitiveFile(fileOut).toFile();
        if (!file.isAbsolute()) {
          String testdir = pr.getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
          if (testdir != null) {
            file = new CaseInsensitiveFile(testdir, fileOut).toFile();
            fileOut = file.getAbsolutePath();
          }
        }
        Log.info("..fileOut: "+fileOut);
        DatabaseUtils.writefile(fileOut, list, delimiter);
      }
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok ",
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (IOException io) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": " + io.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": " + se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } finally {
      try {
        DatabaseUtils.closeAll(null, null, con);
      } catch (SQLException se2) {
      }
    }
  }

  /** <br><em>Purpose:</em> copyDBTableToFile
   ** <p>
   ** This routine will export the contents of a DBTable to a delimeted file.
   ** <p>
   ** A data source name, file out name, and table name are required.
   ** <br>
   ** <br> 0. ADBAlias - ignored
   ** <br> 1. ATableAlias - ignored
   ** <br> 2. DBSourceName
   ** <br>     Name of the Datasource containing the DBTable
   ** <br> 3. FileOut
   ** <br>     Name of the output file.
   ** <br> 4. DBTableName
   ** <br>     Name of the TABLE to access within the DBSourceName
   ** <br> 5. [ ColumnCount ]
   ** <br>     If specified the output file will contain only the number of columns specified.
   ** <br> 6. [ RowCount ]
   ** <br>     If specified the output file will contain only the number of columns specified.
   ** <br> 7. [ SQLQuery ]
   ** <br>     If specified, the file will contain the results of the executed Query. The query is NOT validated by this function.
   ** <br> 8. [ delimiter ]
   ** <br>     If not specified the default ',' seperator will be used.
   ** <br> 9. [ DirectoryOut ]
   ** <br>     If not specified the file will be stored in the default test directory.
   ** <br> 10. [ SQLStatus ]
   ** <br>      A variable which holds the current SQL run status.
   ** <br> 11. [ UserID ]
   ** <br>      UserID for accessing the Datasource (if required).
   ** <br> 12. [ Password ]
   ** <br>      Password for accessing the Datasource (if required).
   ** <br>Examples:
   ** <br>    C, CopyDBTableToFile, ADBAlias, ATableAlias, Data Source Name (dsn), OutputFileName, tableName, [ColumnCount],[rowCount], [SQLQuery], [Delimiter], [OutputDirectory], [sqlStatus], "[UID]", "[PWD]"
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void copyDBTableToFile (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 4) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBSourceName, "+
                                      "OutputFileName, tableName, [ColumnCount], [rowCount], "+
                                      "[SQLQuery], [Delimiter], [OutputDirectory], [sqlStatus], "+
                                      "[UID], [PWD]",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String dbSourceName = (String) iterator.next();
    String fileOut = (String) iterator.next();
    String tableName = (String) iterator.next();
    Integer columnCount = null;
    Integer rowCount = null;
    String sqlQuery = null;
    String delimiter = ",";
    String directoryOut = null;
    String sqlStatusCode = null;
    String userID = null;
    String password = null;
    try { // optional params
      String columnCountStr = (String)iterator.next();
      String rowCountStr = (String)iterator.next();
      if (!columnCountStr.equals("")) columnCount = new Integer(columnCountStr);
      if (!rowCountStr.equals(""))    rowCount = new Integer(rowCountStr);
      sqlQuery = (String) iterator.next();
      if (sqlQuery.trim().equals("")) sqlQuery = null;
      delimiter = (String) iterator.next();
      if (delimiter.trim().equals("")) delimiter = ",";
      directoryOut = (String) iterator.next();
      sqlStatusCode = (String) iterator.next();
      userID = (String) iterator.next();
      password = (String) iterator.next();
    } catch (NoSuchElementException nsee) { // ignore
    } catch (NumberFormatException ex) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      pr.getTestRecordData().getCommand() +
                                      ": row or column number failure: "+ex.getMessage(),
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);
      List list = DatabaseUtils.getValues(con, sqlQuery, tableName, null, null, null, null,
                                          rowCount, columnCount);
      //for(Iterator i = list.iterator()
      if (list == null) {
        sqlStatusCodeVal = "NO ROWS";
      } else {
        if (directoryOut!=null && !directoryOut.equals("")) {
          File file = new CaseInsensitiveFile(directoryOut, fileOut).toFile();
          fileOut = file.getAbsolutePath();
        }
        File file = new CaseInsensitiveFile(fileOut).toFile();
        if (!file.isAbsolute()) {
          String testdir = pr.getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
          if (testdir != null) {
            file = new CaseInsensitiveFile(testdir, fileOut).toFile();
            fileOut = file.getAbsolutePath();
          }
        }
        Log.info("..fileOut: "+fileOut);
        DatabaseUtils.writefile(fileOut, list, delimiter);
      }
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok ",
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (IOException io) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": " + io.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": " + se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } finally {
      try {
        DatabaseUtils.closeAll(null, null, con);
      } catch (SQLException se2) {
      }
    }
  }

  /** <br><em>Purpose:</em> deleteDBTableRecords
   ** <br> Deletes records in a database table
   ** <br>
   ** <br> 0. ADBAlias - ignored
   ** <br> 1. ATableAlias - ignored
   ** <br> 2. DBTableName
   ** <br>     Name of the TABLE to access within the DBSourceName
   ** <br> 3. DBQueryCond
   ** <br>     Expression allowed by the WHERE clause of the delete sentence of the SQL driver
   ** <br>     A query condition can be included. It can be any valid expression
   **          allowed by the WHERE clause of the delete sentence of the SQL driver you are
   **          using. For example: "CliBal &gt; 100000 and CliCat = 'C'". "where" will be added
   **          to any expression if it is not provided ("")
   ** <br> 4. DBSourceName
   ** <br>     Name of the Datasource containing the DBTable
   ** <br> 5. [ UserID]
   ** <br>     UserID for accessing the Datasource (if required).
   ** <br> 6. [ Password]
   ** <br>     Password for accessing the Datasource (if required).
   ** <br> 7. [ SQLStatus ]
   ** <br>     SQL status code as a result of executing the query gets stored in this variable
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void deleteDBTableRecords (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 4) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBtableName, "+
                                      "DBQueryCond, DBSourceName, [UserId], [Password],"+
                                      " [sqlStatus]",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String tableName = (String) iterator.next();
    String queryCond = (String) iterator.next();
    String dbSourceName = (String) iterator.next();
    String userID = null;
    String password = null;
    String sqlStatusCode = null;
    try { // optional params
      userID = (String) iterator.next();
      password = (String) iterator.next();
      sqlStatusCode = (String) iterator.next();
    } catch (NoSuchElementException nsee) { // ignore
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);
      int rowCount = DatabaseUtils.execDeleteStatement(con, tableName, queryCond);
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok; rowCount: "+rowCount,
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": " + se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } finally {
      try {
        DatabaseUtils.closeAll(null, null, con);
      } catch (SQLException se2) {
      }
    }
  }

  /** <br><em>Purpose:</em> execSQLQuery
   ** <br>  Executes the query provided in a queryStr on the database table.
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void execSQLQuery (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 4) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBSourceName, queryStr,"+
                                      " SQLStatusCode, [ UserID ], [ Password ]\n",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String dbSourceName = (String) iterator.next();
    String queryStr = (String) iterator.next();
    String sqlStatusCode = (String) iterator.next();
    String userID = null;
    String password = null;
    String autoCommit = "true";
    try { // optional params
      userID = (String) iterator.next();
      password = (String) iterator.next();
      autoCommit = (String) iterator.next();
    } catch (Exception ex) { // ignore
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);

      if (autoCommit.equalsIgnoreCase("false")) {
        con.setAutoCommit(false);
      } else {
        con.setAutoCommit(true);
      }

      int rowCount = DatabaseUtils.execStatement(con, queryStr);
      if (!pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok; rowCount: "+rowCount,
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (!pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": "+se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    }
  }

  /** <br><em>Purpose:</em> execSqlCommit
   ** <br>  Commits the pending queries on the database.
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void execSQLCommit (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 4) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBSourceName, queryStr,"+
                                      " SQLStatusCode, [ UserID ], [ Password ]\n",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String dbSourceName = (String) iterator.next();
    String sqlStatusCode = (String) iterator.next();
    String userID = null;
    String password = null;

    try { // optional params
      userID = (String) iterator.next();
      password = (String) iterator.next();
    } catch (Exception ex) { // ignore
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);
      con.commit();

      if (!pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok;," +
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (!pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": "+se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } finally {
      try {
        DatabaseUtils.closeAll(null, null, con);
      } catch (SQLException se2) {
      }
    }
  }

  /** <br><em>Purpose:</em> getDBTableColumnCount
   ** <br>  Executes 'select * from table' and returns the total column count.
   ** <br>
   ** <br> 0. ADBAlias - ignored
   ** <br> 1. ATableAlias - ignored
   ** <br> 2. DBSourceName
   ** <br>     Name of the Datasource containing the DBTable
   ** <br> 3. DBTableName
   ** <br>     Name of the TABLE to access within the DBSourceName
   ** <br> 4. ColumnCount
   ** <br>     total column count gets stored in this variable
   ** <br> 5. SQLStatus
   ** <br>     SQL status code as a result of executing the query gets stored in this variable
   ** <br> 6. [ UserID ]
   ** <br>      UserID for accessing the Datasource (if required).
   ** <br> 7. [ Password ]
   ** <br>      Password for accessing the Datasource (if required).
   ** <br>Examples:
   ** <br>    C, getDBTableRowCount, ADBAlias, ATableAlias, Data Source Name (dsn), tableName, RowCount, sqlStatus, "[UID]", "[PWD]"
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void getDBTableColumnCount (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 5) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBSourceName, "+
                                      "tableName, ColumnCount, sqlStatus, "+
                                      "[UID], [PWD]",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String dbSourceName = (String) iterator.next();
    String tableName = (String) iterator.next();
    String columnCountVar = (String)iterator.next();
    String sqlStatusCode = (String)iterator.next();
    String userID = null;
    String password = null;
    try { // optional params
      userID = (String) iterator.next();
      password = (String) iterator.next();
    } catch (NoSuchElementException nsee) { // ignore
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);
      int numCols = DatabaseUtils.getNumColumns(con, null, tableName, null, null, null, null);
      if (columnCountVar != null && !pr.setVariable(columnCountVar, Integer.toString(numCols))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+columnCountVar,
                                        Processor.FAILED_MESSAGE);
        return;
      }
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok ",
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": " + se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } finally {
      try {
        DatabaseUtils.closeAll(null, null, con);
      } catch (SQLException se2) {
      }
    }
  }

  /** <br><em>Purpose:</em> getDBTableRowCount
   ** <br>  Executes 'select * from table' and returns the total row count.
   ** <br>
   ** <br> 0. ADBAlias - ignored
   ** <br> 1. ATableAlias - ignored
   ** <br> 2. DBSourceName
   ** <br>     Name of the Datasource containing the DBTable
   ** <br> 3. DBTableName
   ** <br>     Name of the TABLE to access within the DBSourceName
   ** <br> 4. RowCount
   ** <br>     total row count gets stored in this variable
   ** <br> 5. SQLStatus
   ** <br>     SQL status code as a result of executing the query gets stored in this variable
   ** <br> 6. [ UserID ]
   ** <br>      UserID for accessing the Datasource (if required).
   ** <br> 7. [ Password ]
   ** <br>      Password for accessing the Datasource (if required).
   ** <br>Examples:
   ** <br>    C, getDBTableRowCount, ADBAlias, ATableAlias, Data Source Name (dsn), tableName, RowCount, sqlStatus, "[UID]", "[PWD]"
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void getDBTableRowCount (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 5) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBSourceName, "+
                                      "tableName, RowCount, sqlStatus, "+
                                      "[UID], [PWD]",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String dbSourceName = (String) iterator.next();
    String tableName = (String) iterator.next();
    String rowCountVar = (String)iterator.next();
    String sqlStatusCode = (String)iterator.next();
    String userID = null;
    String password = null;
    try { // optional params
      userID = (String) iterator.next();
      password = (String) iterator.next();
    } catch (NoSuchElementException nsee) { // ignore
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);
      int numCols = DatabaseUtils.getNumRows(con, tableName, null);
      if (rowCountVar != null && !pr.setVariable(rowCountVar, Integer.toString(numCols))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+rowCountVar,
                                        Processor.FAILED_MESSAGE);
        return;
      }
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok ",
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": " + se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } finally {
      try {
        DatabaseUtils.closeAll(null, null, con);
      } catch (SQLException se2) {
      }
    }
  }

  /** <br><em>Purpose:</em> getDBValue
   ** getAlternateParams:<br>
   ** <br> 0. ADBAlias - ignored
   ** <br> 1. ATableAlias - ignored
   ** <br> 2. DBSourceName - Name of the Datasource containing the DBTable
   ** <br> 3. queryStr - Actual query stored in a queryStr.
   ** <br> 4. queryResult - variable to place query result
   ** <br> 5. SQLStatusCode - SQL status code as a result of executing the query.
   ** <br> 6. [ UserID = ] - UserID for accessing the Datasource (if required).
   ** <br> 7. [ Password = ] - Password for accessing the Datasource (if required).
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void getDBValue (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 5) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBSourceName, queryStr, queryResult,"+
                                      " SQLStatusCode, [ UserID ], [ Password ]\n",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String dbSourceName = (String) iterator.next();
    String queryStr = (String) iterator.next();
    String queryResult = (String) iterator.next();
    String sqlStatusCode = (String) iterator.next();
    String userID = null;
    String password = null;
    try { // optional params
      userID = (String) iterator.next();
      password = (String) iterator.next();
    } catch (Exception ex) { // ignore
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);
      List list = DatabaseUtils.getValue(con, queryStr, null, null, null, null, null);
      String queryResultVal = "NULL";
      if (list == null) {
        sqlStatusCodeVal = "NO ROWS";
      } else {
        Iterator ii = list.iterator();
        if (ii.hasNext()) {
          Object nn = ii.next(); // get just the first element
          queryResultVal = DatabaseUtils.getDBVal(nn);
        }
      }
      if (!pr.setVariable(queryResult, queryResultVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+queryResult,
                                        Processor.FAILED_MESSAGE);
        return;
      }
      if (!pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok ("+sqlStatusCodeVal+") :"+queryResultVal,
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (!pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": "+se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } finally {
      try {
        DatabaseUtils.closeAll(null, null, con);
      } catch (SQLException se2) {
      }
    }
  }

  /** <br><em>Purpose:</em> verifyDatabaseNullValue
   ** <br> Verifies that the value of a field in a database table is NULL.
   ** <br>
   ** <br> 0. ADBAlias - ignored
   ** <br> 1. ATableAlias - ignored
   ** <br> 2. DBFieldName
   ** <br>     Name of the FIELD within DBTableName used in the verification
   ** <br> 3. DBTableName
   ** <br>     Name of the TABLE to access within the DBSourceName
   ** <br> 4. DBQueryCond
   ** <br>     Expression allowed by the WHERE clause of the SELECT sentence of the SQL driver
   ** <br>     A query condition can be included. It can be any valid expression
   **          allowed by the WHERE clause of the SELECT sentence of the SQL driver you are
   **          using. For example: "CliBal &gt; 100000 and CliCat = 'C'". "where" will be added
   **          to any expression if it is not provided ("")
   ** <br> 5. DBSourceName
   ** <br>     Name of the Datasource containing the DBTable
   ** <br> 6. [ UserID]
   ** <br>     UserID for accessing the Datasource (if required).
   ** <br> 7. [ Password]
   ** <br>     Password for accessing the Datasource (if required).
   ** <br> 8. [ SQLStatus ]
   ** <br>     SQL status code as a result of executing the query gets stored in this variable
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void verifyDatabaseNullValue (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 5) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBFieldName, DBtableName, "+
                                      "DBQueryCond, DBSourceName, [UserId], [Password],"+
                                      " [sqlStatus]",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String fieldName = (String) iterator.next();
    String tableName = (String) iterator.next();
    String queryCond = (String) iterator.next();
    String dbSourceName = (String) iterator.next();
    String userID = null;
    String password = null;
    String sqlStatusCode = null;
    try { // optional params
      userID = (String) iterator.next();
      password = (String) iterator.next();
      sqlStatusCode = (String) iterator.next();
    } catch (NoSuchElementException nsee) { // ignore
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);
      String wherePart = queryCond + " AND " + fieldName + " IS NULL ";
      int numCols = DatabaseUtils.getNumRows(con, tableName, wherePart);
      if (numCols == 0) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ", Column is not NULL as expected",
                                        Processor.FAILED_MESSAGE);
        return;
      }
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok ",
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": " + se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } finally {
      try {
        DatabaseUtils.closeAll(null, null, con);
      } catch (SQLException se2) {
      }
    }
  }

  /** <br><em>Purpose:</em> verifyDatabaseValue:
   ** <br> Verifies the case-sensitive value of a field in a database table.
   ** <br>
   ** <br> 0. ADBAlias - ignored
   ** <br> 1. ATableAlias - ignored
   ** <br> 2. DBFieldName
   ** <br>     Name of the FIELD within DBTableName used in the verification
   ** <br> 3. DBTableName
   ** <br>     Name of the TABLE to access within the DBSourceName
   ** <br> 4. DBQueryCond
   ** <br>     Expression allowed by the WHERE clause of the SELECT sentence of the SQL driver
   ** <br>     A query condition can be included. It can be any valid expression
   **          allowed by the WHERE clause of the SELECT sentence of the SQL driver you are
   **          using. For example: "CliBal &gt; 100000 and CliCat = 'C'". "where" will be added
   **          to any expression if it is not provided ("")
   ** <br> 5. DBSourceName
   ** <br>     Name of the Datasource containing the DBTable
   ** <br> 6. UserID
   ** <br>     UserID for accessing the Datasource (if required).
   ** <br> 7. Password
   ** <br>     Password for accessing the Datasource (if required).
   ** <br> 8. ExpectedValue
   ** <br>     Benchmark value to compare against retrieved DBFieldName value
   ** <br> 9. [ SQLStatus ]
   ** <br>     SQL status code as a result of executing the query gets stored in this variable
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void verifyDatabaseValue (Processor pr) throws SAFSException {
    Collection altParams = pr.getAlternateParams();
    if (altParams.size() <= 8) {
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() +
                                      ": wrong params, should be: \n  " +
                                      "ADBAlias, ATableAlias, DBFieldName, DBtableName, "+
                                      "DBQueryCond, DBSourceName, UserId, Password, ExpectedValue,"+
                                      " [sqlStatus]",
                                      Processor.FAILED_MESSAGE);
      return;
    }
    Iterator iterator = altParams.iterator();
    // get the params
    iterator.next(); // ignore
    iterator.next(); // ignore
    String fieldName = (String) iterator.next();
    String tableName = (String) iterator.next();
    String queryCond = (String) iterator.next();
    String dbSourceName = (String) iterator.next();
    String userID = (String) iterator.next();
    String password = (String) iterator.next();
    String expectedValue = (String)iterator.next();
    String sqlStatusCode = null;
    try { // optional params
      sqlStatusCode = (String) iterator.next();
    } catch (NoSuchElementException nsee) { // ignore
    }
    Log.info(".............alternate params: "+altParams);
    Connection con = null;
    try {
      String sqlStatusCodeVal = "OK";
      con = getConnection(dbSourceName, userID, password);
      List list = DatabaseUtils.getValue(con, null, tableName, fieldName, queryCond, "", null);
      if (list == null) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": NO ROWS",
                                        Processor.FAILED_MESSAGE);
        return;
      } else {
        Iterator ii = list.iterator();
        if (ii.hasNext()) {
          Object nn = ii.next(); // get just the first element
          String queryResultVal = DatabaseUtils.getDBVal(nn);
          if (!queryResultVal.equals(expectedValue)) {
            pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                            pr.getTestRecordData().getCommand() +
                                            ": Value:"+queryResultVal+
                                            ", does not equal expected value: "+expectedValue,
                                            Processor.FAILED_MESSAGE);
            return;
          }
        }
      }
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, sqlStatusCodeVal)) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
        return;
      }

      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                      " "+pr.getTestRecordData().getCommand()+" "+
                                      altParams+" ok ",
                                      Processor.GENERIC_MESSAGE);
      pr.getTestRecordData().setStatusCode(StatusCodes.OK);
    } catch (SQLException se) {
      //se.printStackTrace();
      Integer status = getSqlStatus(se);
      if (sqlStatusCode != null && !pr.setVariable(sqlStatusCode, (status == null ? se.getMessage() : status.toString()))) {
        pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(),
                                        pr.getTestRecordData().getCommand() +
                                        ": setVariable failure: "+sqlStatusCode,
                                        Processor.FAILED_MESSAGE);
      }
      pr.getTestRecordData().setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      pr.getLogUtilities().logMessage(pr.getTestRecordData().getFac(), ", "+
                                      pr.getTestRecordData().getCommand() + " " +
                                      altParams + ": " + se.getMessage(),
                                      Processor.FAILED_MESSAGE);
    } finally {
      try {
        DatabaseUtils.closeAll(null, null, con);
      } catch (SQLException se2) {
      }
    }
  }

  /** <br><em>Purpose:</em> verifyDBNullValue: same as verifyDatabaseNullValue
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void verifyDBNullValue (Processor pr) throws SAFSException {
    verifyDatabaseNullValue(pr);
  }

  /** <br><em>Purpose:</em> verifyDBValue: same as verifyDatabaseValue
   * @param                     pr, Processor
   * @throws                 SAFSException
   **/
  public static void verifyDBValue (Processor pr) throws SAFSException {
    verifyDatabaseValue(pr);
  }

  /** <br><em>Purpose:</em> get sql status code by scanning the se.getMessage() string
   * @param                     se, SQLException
   * @return                    Integer, null if not found
   **/
  private static Integer getSqlStatus (SQLException se) {
    String tokens = "!@#$%^&*()_-+={}[]|\\:;'<>,.?/`~";
    StringTokenizer st = new SAFSStringTokenizer(se.getMessage(), tokens);
    boolean inside = false;
    while (st.hasMoreTokens()) {
      String next = st.nextToken();
      System.out.println("next: "+next);
      if (next.equals("[")) {inside = true; continue;}
      if (next.equals("]")) {inside = false; continue;}
      if (tokens.indexOf(next) >= 0) continue;
      if (!inside) {
        try {
          Integer i = new Integer(next);
          return i;
        } catch (NumberFormatException nfe) { //ignore
        }
      }
    }
    return null;
  }
}

