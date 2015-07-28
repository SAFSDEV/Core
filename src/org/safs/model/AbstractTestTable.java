/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The intended superclass for all Test Table classes.
 * This abstract class contains most of the workhorse code for building and exporting test tables.
 * The specific subclasses finish up the implementation for each different type of test table 
 * (Cycle, Suite, and Step). 
 */
public abstract class AbstractTestTable extends AbstractTestRecord {
	
    final static private String PATH_SEPARATOR = "/";
    final static public String DEFAULT_FIELD_SEPARATOR = "\t";
    // RationalRobot requires \r\n, otherwise the files are not read
    // correctly when processing the test tables.
    final static private String TEST_RECORD_LINE_SEPARATOR = "\r\n"/*I18nOK:EMS*/;
    
    /** 'SDD' */
    final static public String STEP_TABLE_FILE_EXTENSION  = "SDD";
    /** 'STD' */
    final static public String SUITE_TABLE_FILE_EXTENSION = "STD";
    /** 'CDD' */
    final static public String CYCLE_TABLE_FILE_EXTENSION = "CDD";
   
   private String _testTableName;
   private String _testTableExtension;
   private String _separator = DEFAULT_FIELD_SEPARATOR;
   
   // subclasses might need to update this list.  For example, StepTestTable must be able 
   // to add ComponentFunction records to this list 
   protected List _commands;

   // tracks which table names have already been exported to avoid multiple exports of 
   // the same table and infinite loop recursion.
   private static List _exports = new ArrayList();
   
   /**
    * Create a new instance of a test table with the provided name and export file extension.
    * The name cannot be null or zero-length, but the file extension can be.
    * 
    * @param testTableName -- the name cannot be null or zero-length
    * @param testTableFileExtension
    * @throws IllegalArgumentException if testTableName is null or zero-length
    */
   public AbstractTestTable(String testTableName, String testTableFileExtension) {
      super();
      
      if ((testTableName == null )||(testTableName.length()== 0))
         throw new IllegalArgumentException("Test Table Name cannot be null or zero-length.");
      _testTableName = testTableName;
      setTestTableFileExtension(testTableFileExtension);
      _commands = new ArrayList();
      _separator = DEFAULT_FIELD_SEPARATOR;
   }

   /**
    * Return the name of this test table.
    * The name does NOT include the file extension.
    * 
    * @return the name of this test table
    */
   final public String getTestTableName() {
      return _testTableName;
   }

   /**
    * Return the current file extension setting used during exports for this table.
    * 
    * @return current testTableExtension setting.  Can be null or zero-length.
    */
   final public String getTestTableFileExtension() {
      return _testTableExtension;
   }
   
   /**
    * Set the file extension to be used when exporting this test table.
    * This value can be null or zero-length in which case no file extension 
    * will be used.
    *  
    * @param testTableExtension -- the file extension to use for this table.  
    * Might be null or zero-length.
    */
   final public void setTestTableFileExtension (String testTableExtension) {      
   	  _testTableExtension = testTableExtension;      
   }

   /**
    * Retrieve the single separator String character for this table.
    * 
    * @return the single separator String that will delimit test record fields.
    */
   final public String getTestRecordFieldSeparator() {
      return _separator;
   }

   /**
    * Specify an alternate single separator String (other than TAB) to be used to delimit 
    * the fields in the records of this table.  If using the default TAB character than 
    * this function does not need to be called.
    * 
    * @param separator -- String character used to delimite test record fields
    * @throws IllegalArgumentException if the provided separator is null or zero-length. 
    */
   final public void setTestRecordFieldSeparator(String separator) {
      try{ _separator = separator.substring(0,1);}
      catch(Exception x){ 
      	throw new IllegalArgumentException("Invalid table separator specified for "+ getTestTableName());
      }
   }
   
   /**
    * Retrieve an unmodifiable List of this table's test record objects 
    * @return unmodifiableList
    */
   final public List getCommands() {
      return Collections.unmodifiableList(_commands);
   }

   /**
    * Append the provided command\record to our table's store of test records.
    * Note that ComponentFunction commands do not subclass Command because they are 
    * only valid at the StepTestTable level.  All other command types should be 
    * acceptable though.  They will be validated with validateCommand.
    * 
    * @param command
    * @throws IllegalArgumentException if command is null or if validateCommand 
    *         throws the exception.
    */
   final public void add(Command command) {
      if (command == null)
           throw new IllegalArgumentException("Cannot add a null command.");
      validateCommand(command);
      _commands.add(command);
   }

   /**
    * Returns true if the table has already been exported.
    * @return true if the table has already been exported.
    */
   final public boolean isExported(){
       return (_exports.indexOf(getSAFSFileName())> -1);
   }
   
   /**
    * Throw an IllegalArgumentException if the command is not valid.
    * <p>
    * Example: CallStep is not valid in Cycle and Suite tables.
    * <p>
    * Subclasses should override this method to check whether the specified command is 
    * valid for its test table type.  If the command is not valid, an 
    * IllegalArgumentException with a detailed message should be thrown.  Valid commands 
    * simply do not throw the exception.
    * 
    * @param command -- the command to validate
    * @throws IllegalArgumentException if the command is not valid for the current test table type
    */
   protected void validateCommand(Command command) {
      /* do nothing base class */
   }

   /**
    * Returns the concatenation of the table name, period, and table extension.
    * If the current file extension is null or zero-length then we only return 
    * the test table name.
    * 
    * @return 'filename.ext', or just 'filename' if no extension is set 
    */
   protected String getSAFSFileName() {
      StringBuffer buffer = new StringBuffer(getTestTableName());
      String ext = getTestTableFileExtension();
      try{
      	 if (ext.length() > 0){
            buffer.append('.');
            buffer.append(ext);
      	 }
      }catch(Exception x){ 
      	// ignore null or empty file extensions 
      }
      return buffer.toString();
   }
   
   /**
    * Exports the one record used to call the test table including the ending newline.
    * Remember, while a test table may have multiple records of its own, the calling test table 
    * invokes it with a Project Command test record like below:
    * <p>
    * <ul>T  LoginWinTests  ^UserID=Carlos  ^Password=Santana</ul>
    * <p>  
    * A test table must export this test record when it is asked to do so.
    * 
    * @param fieldSeparator used to delimit fields in test record
    * @return String -- newline-terminated test record ready for export
    */
   public String exportTestRecord (String fieldSeparator) {
      StringBuffer sb = new StringBuffer();
      sb.append(PROJECT_COMMAND_RECORD_TYPE);
      sb.append(fieldSeparator);
      sb.append(getTestTableName());
      sb.append(fieldSeparator);

      if ( ! getTestRecordFieldSeparator().equalsIgnoreCase(fieldSeparator))
          sb.append(getTestRecordFieldSeparator());

      if ( ! getParameters().isEmpty())      	  
          sb = appendParametersToTestRecord(sb, fieldSeparator);
      
      return sb.toString();
   }

   /**
    * Export the table to a field-delimited file in the specified filePath directory.
    * This not only exports this one table, but also exports any tables called by this table.
    * Those other called tables will also export any tables they call, and so on.  So, for most cases, 
    * calling this routine on the topmost table will export all tables needed for the test.
    * 
    * @param filePath -- the full path to the directory in which the table should be exported.
    */
   public void exportToCSV(String filePath) {
	  
   	  System.out.println(getTestTableName()+" is now being exported.");
      String fileName = getSAFSFileName();
      
      // store to ensure this table is exported only once
      _exports.add(fileName);
      
      FileOutputStream outFile = null;
      try {
         String pathSeparator;
         if (filePath.endsWith(PATH_SEPARATOR))
            pathSeparator = "";
         else
            pathSeparator = PATH_SEPARATOR;

         outFile = new FileOutputStream(filePath + pathSeparator + fileName);

         StringBuffer sb = new StringBuffer();
         
         // process each _command making up the table
         for (int i = 0, iCount = _commands.size(); i < iCount; i++) {
         	
         	// commands *and* whole test tables implement TestRecordParametersInterface
            TestRecordParametersInterface cmd = (TestRecordParametersInterface)_commands.get(i);
            
            // get the String test record which is already newline terminated
            sb.append(cmd.exportTestRecord(_separator));
            sb.append(TEST_RECORD_LINE_SEPARATOR);
            
            // if this is a test table, see if it needs to also be exported.
            if (cmd instanceof AbstractTestTable) {
            	AbstractTestTable table = (AbstractTestTable)cmd;
            	if (! table.isExported()){
            		table.exportToCSV(filePath);
            	}else{
            		System.out.println(table.getTestTableName()+" has already been exported.");
            	}
            }
         }
         // output the file
         outFile.write(sb.toString().getBytes());
      
      // handle errors and cleanup
      } catch (Exception ex) {
         ex.printStackTrace();
      } finally {
         if (outFile != null)
            try {
               outFile.close();
            } catch (IOException ex) {
               ex.printStackTrace();
            }
      }
   }
}