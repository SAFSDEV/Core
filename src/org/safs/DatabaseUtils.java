/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.sql.*;
import java.util.*;
import java.io.*;

import org.safs.text.FileUtilities;

/**
 * <br><em>Purpose:</em> Utilities to find a database connection using java.sql.DriverManager;
 * can also getValues.
 * <p>
 * @author  Doug Bauman
 * @since   Oct 23, 2003
 *
 * <br>     Oct 23, 2003    (DBauman) Original Release
 **/
public class DatabaseUtils {
  /**
   * Holds the default JDBC driver.
   * Value is "sun.jdbc.odbc.JdbcOdbcDriver".
   */
  public static final String DEFAULT_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";
  public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

  /** load the driver, if 'driver' is null then uses DEFAULT_DRIVER
   ** @param driver, String
   ** @return Class, the class loaded with Class.forName
   **/
  public static Class loadDriver(String driver) throws SQLException {
    try {
      if (driver==null) driver = DEFAULT_DRIVER;
      System.out.println("........loading:"+driver);
      Class drCl = Class.forName(driver);
      return drCl;
    } catch (IllegalArgumentException iae) {
      throw new SQLException("Driver class (" + driver + "): possibly wrong format: "+
                             iae.getMessage());
    } catch (ClassNotFoundException e) {
      throw new SQLException("Could not find database driver class:" + driver);
    }
  }
  /**
   * Opens a connection to the JDBC database indicated by the 'dbName'
   * parameter. 
   *
   * @param jdbcUrl The database url
   * @param username String, if null then uses the 1-parameter version of DriverManager.getConnection
   * @param password String
   * @return The connection.
   * @exception SQLException if the connection fails.
   */
  public static Connection getConnection (String jdbcUrl,
                                          String username, String password) throws SQLException {
    try {
      Log.info("Trying getConnection with url: "+jdbcUrl);
      if (username == null) {
        return DriverManager.getConnection(jdbcUrl);
      } else {
        return DriverManager.getConnection(jdbcUrl, username, password);
      }
    } catch (SQLException sqle) {
      jdbcUrl = "jdbc:odbc:" + jdbcUrl;
      Log.info("Trying again with url: "+jdbcUrl);
      if (username == null) {
        return DriverManager.getConnection(jdbcUrl);
      } else {
        return DriverManager.getConnection(jdbcUrl, username, password);
      }
    } catch (IllegalArgumentException iae) {
      throw new SQLException("possibly problem with URL: "+jdbcUrl+", "+iae);
    }
  }

  /** <br><em>Purpose:</em> setup select statement
   * @param                     buf, StringBuffer
   * @param                     table, String name of the table
   * @param                     field, String the select field name
   * @param                     attr, String the attr part for the where, if null then no where
   * @param                     value, if attr!=null, then the value part of where, if null,
   * then the where part will be "attr NOT NULL"
   * @return                    String, the statement
   **/
  protected static String setupSelect (StringBuffer buf,
                                       String table, String field,
                                       String attr, Object value) {
    buf.append("SELECT ");
    buf.append(field==null?"*":field);
    buf.append(" FROM ");
    buf.append(table);
    if (attr != null && !attr.trim().equals("")) {
      buf.append(" ");
      attr = attr.trim();
      if (attr.toUpperCase().indexOf("WHERE") != 0) buf.append("WHERE ");
      buf.append(attr);
Log.info("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBUF: "+buf.toString());
      if (value==null) {
        buf.append(" NOT NULL");
      } else if (value instanceof String && ((String)value).trim().equals("")) {
        // do nothing, it should already have the rest
      } else { // otherwise we will construct the rest
        buf.append("=");
        boolean isStr = value instanceof String;
        if (isStr) buf.append("'");
        buf.append(value);
        if (isStr) buf.append("'");
      }
    }
    String statement = buf.toString();
    Log.info("...statement: "+statement);
    return statement;
  }

  /** <br><em>Purpose:</em> simple query, as in: "SELECT field FROM table WHERE attr = 'value'"
   ** or if attr is null then simply "SELECT field FROM table"
   * <br><em>Assumptions:</em>  Note: this version does not take into account special characters.
   * You need a more sophisticated mechanism to take those into account.
   * @param                     con, Connection
   * @param                     strStatement, String the statement, if null, then construct from the other parameters
   * @param                     table, String name of the table
   * @param                     field, String the select field name
   * @param                     attr, String the attr part for the where, if null then no where
   * @param                     value, if attr!=null, then the value part of where, if null,
   * then the where part will be "attr NOT NULL"
   * @param                     st, Statement[], pointer to a return'ed Statement
   * @return                    ResultSet from the executeQuery on the statment
   * @exception                 SQLException
   **/
  public static ResultSet getResultSet (Connection con,
                                        String strStatement,
                                        String table, String field,
                                        String attr, Object value,
                                        Statement[] st) throws SQLException {
    StringBuffer buf = new StringBuffer(128);
    String statement = strStatement;
    if (statement == null) {
      statement = setupSelect(buf, table, field, attr, value);
    }
    st[0] = con.prepareStatement(statement);
    return ((PreparedStatement)st[0]).executeQuery();
  }
  
  /** <br><em>Purpose:</em> execute statement, can be used to do update or insert
   * <br><em>Assumptions:</em>  Note: this version does not take into account special characters.
   * You need a more sophisticated mechanism to take those into account.
   * @param                     con, Connection
   * @param                     strStatement, String the statement
   * @return                    either the row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing
   * @exception                 SQLException
   **/
  public static int execStatement (Connection con,
                                   String strStatement) throws SQLException {
    ResultSet rs = null;
    PreparedStatement st = null;
    try {
      st = con.prepareStatement(strStatement);
      return st.executeUpdate();
    } finally {
      try {
        closeAll(rs, st, null);
      } catch (SQLException e) {
      }
    }
  }
  
  /** <br><em>Purpose:</em> execute delete statement, can be used to do update or insert
   * <br><em>Assumptions:</em>  Note: this version does not take into account special characters.
   * You need a more sophisticated mechanism to take those into account.
   * @param                     con, Connection
   * @param                     strStatement, String the statement
   * @return                    either the row count for INSERT, UPDATE or DELETE statements; or 0 for SQL statements that return nothing
   * @exception                 SQLException
   **/
  public static int execDeleteStatement (Connection con,
                                         String table,
                                         String whereCl) throws SQLException {
    StringBuffer buf = new StringBuffer();
    buf.append("DELETE FROM ");
    buf.append(table);
    if (whereCl != null && !whereCl.trim().equals("")) {
      buf.append(" ");
      whereCl = whereCl.trim();
      if (whereCl.toUpperCase().indexOf("WHERE") != 0) buf.append("WHERE ");
      buf.append(whereCl);
    }
    Log.info("..statement...."+buf.toString());
    ResultSet rs = null;
    PreparedStatement st = null;
    try {
      st = con.prepareStatement(buf.toString());
      return st.executeUpdate();
    } finally {
      try {
        closeAll(rs, st, null);
      } catch (SQLException e) {
      }
    }
  }
  
  /** <br><em>Purpose:</em> simple get, as in: "SELECT field FROM table WHERE attr = 'value'"
   ** or if attr is null then simply "SELECT field FROM table"
   * <br><em>Assumptions:</em>  Note: this version does not take into account special characters.
   * You need a more sophisticated mechanism to take those into account.
   * @param                     con, Connection
   * @param                     strStatement, String the statement, if null, then construct from the other parameters
   * @param                     table, String name of the table
   * @param                     field, String the select field name
   * @param                     attr, String the attr part for the where, if null then no where
   * @param                     value, if attr!=null, then the value part of where, if null,
   * then the where part will be "attr NOT NULL"
   * @param                     columns, if not null, then we add the columns to this list
   * @return                    a 'List' which is represents the first row; if no rows, then null
   * @exception                 SQLException
   **/
  public static List getValue (Connection con,
                               String strStatement,
                               String table, String field,
                               String attr, Object value,
                               ArrayList columns) throws SQLException {
    ResultSet rs = null;
    Statement[] st = new Statement[1];
    try {
      rs = getResultSet(con, strStatement, table, field, attr, value, st);
      if (columns != null) {
        ResultSetMetaData meta = rs.getMetaData();
        int numberOfColumns = meta.getColumnCount();
        for(int i=1; i<=numberOfColumns; i++) { // get each value in the row
          columns.add(meta.getColumnName(i));
        }
      }
      if (rs.next()) {
        List list = getRow(rs);
        return list;
      }
    } finally {
      try {
        closeAll(rs, st[0], null);
      } catch (SQLException e) {
      }
    }
    return null;
  }
  
  /** <br><em>Purpose:</em> simple get(all), as in: "SELECT field FROM table WHERE attr = 'value'"
   ** or if attr is null then simply "SELECT field FROM table"
   * <br><em>Assumptions:</em>  Note: this version does not take into account special characters.
   * You need a more sophisticated mechanism to take those into account.
   * @param                     con, Connection
   * @param                     strStatement, String the statement, if null, then construct from the other parameters
   * @param                     table, String name of the table
   * @param                     field, String the select field name
   * @param                     attr, String the attr part for the where, if null then no where
   * @param                     value, if attr!=null, then the value part of where, if null,
   * then the where part will be "attr NOT NULL"
   * @param                     columns, if not null, then we add the columns to this list
   * @param                     maxRowCount, if not null, then don't return more rows then specified
   * @param                     maxColCount, if not null, then don't return more cols then specified
   * @return                    List of 'List' which is a list of rows in the database, the rows are items for each column, as they are encountered
   * @exception                 SQLException
   **/
  public static List getValues (Connection con,
                                String strStatement,
                                String table, String field,
                                String attr, Object value,
                                ArrayList columns,
                                Integer maxRowCount,
                                Integer maxColCount) throws SQLException {
    ResultSet rs = null;
    Statement[] st = new Statement[1];
    List list = new ArrayList();
    try {
      rs = getResultSet(con, strStatement, table, field, attr, value, st);
      ResultSetMetaData meta = rs.getMetaData();
      int numberOfColumns = meta.getColumnCount();
      if (columns != null) {
        for(int i=1; i<=numberOfColumns; i++) { // get each value in the row
          if (maxColCount != null && (i-1)==maxColCount.intValue()) break;
          columns.add(meta.getColumnName(i));
        }
      }
      for (int rowi= 0; rs.next(); rowi++) {
        if (maxRowCount != null && rowi==maxRowCount.intValue()) {
          Log.info("Stopping at maxRowCount: "+rowi);
          break;
        }
        List l = getRow(rs, meta, numberOfColumns, maxColCount);
        list.add(l);
      }
      return list;
    } finally {
      try {
        closeAll(rs, st[0], null);
      } catch (SQLException e) {
      }
    }
  }
  
  /** <br><em>Purpose:</em> simple get(all), as in: "SELECT field FROM table WHERE attr = 'value'"
   ** or if attr is null then simply "SELECT field FROM table"; in order to get number of columns, and/or to get the names of the columns in parameter 'columns'
   * <br><em>Assumptions:</em>  Note: this version does not take into account special characters.
   * You need a more sophisticated mechanism to take those into account.
   * @param                     con, Connection
   * @param                     strStatement, String the statement, if null, then construct from the other parameters
   * @param                     table, String name of the table
   * @param                     field, String the select field name
   * @param                     attr, String the attr part for the where, if null then no where
   * @param                     value, if attr!=null, then the value part of where, if null,
   * then the where part will be "attr NOT NULL"
   * @param                     columns, if not null, then we add the columns to this list
   * @return                    int, number of columns
   * @exception                 SQLException
   **/
  public static int getNumColumns (Connection con,
                                   String strStatement,
                                   String table, String field,
                                   String attr, Object value,
                                   ArrayList columns) throws SQLException {
    ResultSet rs = null;
    Statement[] st = new Statement[1];
    List list = new ArrayList();
    try {
      rs = getResultSet(con, strStatement, table, field, attr, value, st);
      ResultSetMetaData meta = rs.getMetaData();
      int numberOfColumns = meta.getColumnCount();
      if (columns != null) {
        for(int i=1; i<=numberOfColumns; i++) { // get each value in the row
          columns.add(meta.getColumnName(i));
        }
      }
      return numberOfColumns;
    } finally {
      try {
        closeAll(rs, st[0], null);
      } catch (SQLException e) {
      }
    }
  }
  
  /** <br><em>Purpose:</em> get number of rows
   * @param                     con, Connection
   * @param                     strStatement, String the statement, if null, then construct from the other parameters
   * @param                     table, String name of the table
   * @param                     wherepart, String the whrere part
   * @return                    int, number of rows
   * @exception                 SQLException
   **/
  public static int getNumRows (Connection con,
                                String table, String wherePart) throws SQLException {
    String countField = "COUNT(*)";
    String statement = setupSelect(new StringBuffer(128), table, countField, wherePart, "");
    List l = getValue(con, statement, null, null, null, null, null);
    if (l != null) {
      Iterator ii = l.iterator();
      if (ii.hasNext()) {
        Object nn = ii.next(); // get just the first element
        Log.info("nn: "+nn);
        String queryResultVal = getDBVal(nn);
        return (new Integer(queryResultVal)).intValue();
      }
    }
    return 0;
  }
  
  /** <br><em>Purpose:</em> get the next row from the ResultSet as a Collection
   * <br><em>Assumptions:</em>  that rs.next() was already called and is true.
   * This version calls the getRow with the metadata and the column count
   * @param                     rs, ResultSet
   * @return                    List of items in the row, if rs.next() is null, then null
   * @exception                 SQLException
   **/
  public static List getRow (ResultSet rs) throws SQLException {
    ResultSetMetaData meta = rs.getMetaData();
    int numberOfColumns = meta.getColumnCount();
    return getRow(rs, meta, numberOfColumns, null);
  }
  /** <br><em>Purpose:</em> get the next row from the ResultSet as a Collection
   * <br><em>Assumptions:</em>  that rs.next() was already called and is true
   * @param                     rs, ResultSet
   * @param                     meta, ResultSetMetaData (obtained externally via rs.getMetaData())
   * @param                     numberOfColumns, int (obtained externally via meta.getColumnCount())
   * @param                     maxColCount, Integer
   * @return                    List of items in the row, as they are encountered
   * @exception                 SQLException
   **/
  public static List getRow (ResultSet rs, ResultSetMetaData meta,
                             int numberOfColumns,
                             Integer maxColCount) throws SQLException {
    List row = new ArrayList(); // can add 'null' to an ArrayList
    String name;
    for(int i=1; i<=numberOfColumns; i++) { // get each value in the row
      if (maxColCount != null && (i-1)==maxColCount.intValue()) break;
      name = meta.getColumnName(i);
      switch(meta.getColumnType(i)) {
        case java.sql.Types.ARRAY:
          row.add(rs.getArray(i));
          break;
        case java.sql.Types.BIGINT:
          row.add(new Long(rs.getLong(i))); //?? not sure if correct
          break;
        case java.sql.Types.BINARY:
          row.add(rs.getBinaryStream(i));
          break;
        case java.sql.Types.BIT:
          row.add(new Byte(rs.getByte(i))); //?? not sure if correct
          break;
        case java.sql.Types.BLOB:
          row.add(rs.getBlob(i));
          break;
        case java.sql.Types.CHAR:
          row.add(rs.getString(i)); //?? not sure if correct
          break;
        case java.sql.Types.CLOB:
          row.add(rs.getClob(i));
          break;
        case java.sql.Types.DATE:
          row.add(rs.getDate(i));
          break;
        case java.sql.Types.DECIMAL:
          row.add(rs.getBigDecimal(i));
          break;
        case java.sql.Types.DISTINCT:
          row.add(null); //?? not sure if correct
          break;
        case java.sql.Types.DOUBLE:
          row.add(new Double(rs.getDouble(i)));
          break;
        case java.sql.Types.FLOAT:
          row.add(new Float(rs.getFloat(i)));
          break;
        case java.sql.Types.INTEGER:
          row.add(new Integer(rs.getInt(i)));
          break;
        case java.sql.Types.JAVA_OBJECT:
          row.add(rs.getObject(i));
          break;
        case java.sql.Types.LONGVARBINARY:
          row.add(new Long(rs.getLong(i))); //?? not sure if correct
          break;
        case java.sql.Types.LONGVARCHAR:
          row.add(rs.getString(i)); //?? not sure if correct
          break;
        case java.sql.Types.NULL:
          row.add(null);
          break;
        case java.sql.Types.NUMERIC:
          row.add(new Double(rs.getDouble(i))); //?? not sure if correct
          break;
        case java.sql.Types.OTHER:
          row.add(null); //?? not sure if correct
          break;
        case java.sql.Types.REAL:
          row.add(new Double(rs.getDouble(i))); //?? not sure if correct
          break;
        case java.sql.Types.REF:
          row.add(rs.getRef(i));
          break;
        case java.sql.Types.SMALLINT:
          row.add(new Short(rs.getShort(i)));
          break;
        case java.sql.Types.STRUCT:
          row.add(null); //?? not sure if correct
          break;
        case java.sql.Types.TIME:
          row.add(rs.getTime(i));
          break;
        case java.sql.Types.TIMESTAMP:
          row.add(rs.getTimestamp(i));
          break;
        case java.sql.Types.TINYINT:
          row.add(new Short(rs.getShort(i))); //?? not sure if correct
          break;
        case java.sql.Types.VARBINARY:
          row.add(rs.getBinaryStream(i)); //?? not sure if correct
          break;
        case java.sql.Types.VARCHAR:
          row.add(rs.getString(i)); //?? not sure if correct
          break;
        default:
          row.add(null); //?? not sure if correct
      }
    }
    return row;
  }

  /** Convenience method for closing sql objects. Closing is tried on
   ** all objects, independent of errors/exceptions in previous closes.
   * @param rs ResultSet to be closed
   * @param st Statement (or subclass) to be closed
   * @param conn Connection to be closed
   * @exception SQLException when any problem occurs while closing objects
   */
  public static void closeAll (ResultSet rs, Statement st, Connection conn) throws SQLException {
    try {
      if (rs != null) {
        rs.close();
      }
    } finally {
      try {
        if (st != null) {
          st.close();
        }
      } finally {
        if (conn != null) {
          conn.close();
        }
      }
    }
  }

  /** test main method.
   ** reads username and password as first two lines in c:\test.dat
   ** the remaining lines in the file are optional and include:
   ** <br>jdbcUrl,
   ** <br>tableName (default is 
   ** <br>col (the select column, default is *)
   ** <br>dbName
   **/
  public static void main(String[] arg) {
    Connection con = null;
    String dbName = DEFAULT_DRIVER;
    String svcName = "TESTBY";//"test_automation.msais.com";
    String host = "dorado";
    String port = "1521";
    String jdbcUrl = "jdbc:oracle:thin:@" + host + ":" + port + ":" + svcName;
    String user= null;
    String pass = null;
    String table = "V$DATABASE";
    String col = "*"; //"NAME";
    try {
      Iterator uspa = StringUtils.readfile("c:\\test.dat").iterator();
      user = (String)uspa.next();
      pass = (String)uspa.next();
      String next = ((String)uspa.next()).trim();
      if (next.length()>0) jdbcUrl = next;
      next = ((String)uspa.next()).trim();
      if (next.length()>0) table = next;
      next = ((String)uspa.next()).trim();
      if (next.length()>0) col = next;
      next = ((String)uspa.next()).trim();
      if (next.length()>0) dbName = next;
    } catch (Exception ex) {
    }
    System.out.println("user:"+user);
    System.out.println("pass:"+(pass==null?"null":"*"));
    System.out.println("jdbcUrl:"+jdbcUrl);
    System.out.println("table:"+table);
    System.out.println("col:"+col);
    System.out.println("dbName:"+dbName);
    String line;
    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
    for(;;) {
      if (arg.length > 0) {
        System.out.print("> ");
        line = "exit";
        try {
          line = r.readLine();
        } catch (IOException io) {
        }
        if (line.equalsIgnoreCase("exit")) break;
        table = line.trim();
        if (table.equals("")) continue;
      }
      try {
        Class drCl= loadDriver(dbName);
        con = getConnection(jdbcUrl, user, pass);
        StringBuffer buf = new StringBuffer(4096);
        ArrayList columns = new ArrayList();
        if (arg.length > 0) {
          List list = getValues(con, null, table, col, null, null, columns, null, null);
          printHeader(buf, table, col);
          System.out.println(buf.toString());
          boolean all = false;
          line = "";
          int lineNum = 1;
          for(Iterator i = list.iterator(); i.hasNext(); lineNum++) {
            List inlist = (List) i.next();
            buf.delete(0, buf.length());
            printValues(buf, inlist, columns);
            System.out.println(" ["+lineNum+"]");
            System.out.println(buf.toString());
            if (!all) {
              System.out.print(">> ");
              line = "exit";
              try {
                line = r.readLine();
              } catch (IOException io) {
              }
              if (line.equalsIgnoreCase("exit")) break;
              if (line.equalsIgnoreCase("all")) all = true;
            }
          }
        } else {
          List list = (List)getValue(con, null, table, col, null, null, columns);
          printHeader(buf, table, col);
          printValues(buf, list, columns);
          System.out.println(buf.toString());
        }
      } catch (SQLException se) {
        se.printStackTrace();
        System.out.println("se:"+se.getMessage()+": "+jdbcUrl);
      } finally {
        try {
          closeAll(null, null, con);
        } catch (SQLException se2) {
        }
      }
      if (arg.length == 0) {
        break;
      }
    }
  }

  /** <br><em>Purpose:</em> print the values of 'list' to 'buf'
   * @param                     buf, StringBuffer 
   * @param                     list, List
   * @param                     columns, ArrayList
   **/
  public static void printValues (StringBuffer buf, List list, ArrayList columns) {
    Iterator col = null;
    if (columns != null) {
      col = columns.iterator();
    }
    for(Iterator i = list.iterator(); i.hasNext(); ) {
      Object val = i.next();
      int offset = 0;
      if (col != null) {
        String key = "";
        if (col.hasNext()) {
          key = (String) col.next();
        }
        buf.append("  ");
        buf.append(key);
        int j = key.length();
        offset = 23;
        if (offset>j) {
          buf.append(StringUtils.getSpaces(new Integer(offset-j)));
          j += offset - j;
        }
        buf.append(": ");
      }
      String vals = getDBVal(val);
      buf.append(vals);
      int j = 2+vals.length();
      offset += 2;
      if (offset>j) {
        buf.append(StringUtils.getSpaces(new Integer(offset-j)));
        j += offset - j;
      }
      buf.append(", ");
      buf.append(val==null?"":val.getClass().getName());
      buf.append("\n");
    }
  }
  /** <br><em>Purpose:</em> print the header
   * @param                     buf, StringBuffer 
   * @param                     table, String
   * @param                     col, String
   **/
  public static void printHeader (StringBuffer buf, String table, String col) {
    buf.append("[");
    buf.append(table);
    buf.append(", ");
    buf.append(col);
    buf.append("]");
    buf.append("\n");
  }
  
  /**
   * write to file 'filename' the toString() values contained in list.  
   * Each item is written as a separate line using '\n' as the line separator 
   * regardless of operating system.  
   * Values are written in the system default character encoding.
   * <p>
   * If an item in the list is another Collection, then special handling is performed 
   * via the getDBVal function to potentially filter the value to be written.  The items 
   * in this secondary Collection are treated as delimited fields and will be separated 
   * in the output by the field delimiter provided.
   * 
   *  
   * @param filename String full absolute path filename of file to write.
   * @param list Collection of lines to write.
   * @param delim String field separator to use on values stored in Collections in the list.
   * @throws FileNotFoundException if file cannot be created for any reason.
   * @throws IOException if an error occurs during write operations.
   * @see FileUtilities#getSystemBufferedFileWriter(String)
   * @see #getDBVal(Object)
   **/
  public static  void writefile(String filename, Collection list, String delim) throws IOException {
  	BufferedWriter writer = null;
    try {
      writer = FileUtilities.getSystemBufferedFileWriter(filename);
      for(Iterator i= list.iterator(); i.hasNext(); ) {
        Object n = i.next();
        if (n instanceof Collection) {
          Iterator j = ((Collection)n).iterator();
          StringBuffer s = new StringBuffer();
          for(; j.hasNext();) {
            Object m = j.next();
            String val = getDBVal(m);
            String d = (j.hasNext() ? delim : "");
            s.append(val + d);
          }
          writer.write(s.toString() + "\n");
        } else {
          String s = (n==null ? "" : n.toString()) + "\n";
          writer.write(s);
        }
      }
    } finally {
      if (writer != null)  writer.close();
    }
  }

  /** <br><em>Purpose:</em> getDBVal as a string
   ** if the type is "java.lang.Double",
   ** "java.sql.Timestamp" or "java.sql.Date" the we
   ** remove trailing .0 if exists.
   * @param                     m, Object
   * @return                    String, if m was null then ""
   **/
  public static String getDBVal (Object m) {
    String val = "";
    if (m != null) {
      Class cl = m.getClass();
      //System.out.println("cl: "+cl);
      if (cl.getName().equals("java.lang.Double") ||
          cl.getName().equals("java.lang.Float") ||
          cl.getName().equals("java.sql.Timestamp") ||
          cl.getName().equals("java.sql.Date")) {
        val = m.toString();
        if (val.length()>=2 && val.substring(val.length()-2,val.length()).equals(".0")) {
          //System.out.println("fixing: "+val);
          val = val.substring(0, val.length()-2);
          //System.out.println("fixed:  "+val);
        }
      } else {
        val = m.toString();
      }
    }
    return val;
  }
   
}
