/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;


/**
 * Superclass of all command types of classes (versus test table types of classes).
 * Keeps track of the command name and record type for the command. 
 */
public abstract class AbstractCommand extends AbstractTestRecord {
      
   private String _commandName;
   private String _testRecordID;

   /**
    * Invoked from subclasses when instancing new commands.
    * Neither specified parameter can be null or zero-length.
    * 
    * @param commandName -- the name of the command ("SetApplicationMap", etc.)
    * @param testRecordID -- the record type of the command ("T", "C", etc.)
    * @throws IllegalArgumentException if specified parameters are null or zero-length
    */
   protected AbstractCommand(String commandName, String testRecordID) {
      super();
      if (commandName == null || testRecordID == null)
         throw new IllegalArgumentException("Command names or record types cannot be null.");
      if ((commandName.length()==0) || (testRecordID.length() == 0))
        throw new IllegalArgumentException("Command names or record types cannot be zero-length.");
      
      _commandName = commandName;
      _testRecordID = testRecordID;
   }

   /**
    * Returns the string name of the command.
    * 
    * @return the name of this command
    */
   public String getCommandName() {
      return _commandName;
   }
   
   /**
    * Returns the string record type for this command.
    * 
    * @return the record type of this command
    */
   public String getTestRecordID () {
      return _testRecordID;
   }

   /**
    * TestRecordExporter interface implementation.  
    * This routine will call appendCommandToTestRecord and appendParametersToTestRecord.
    * Subclasses only need to override those as necessary, if at all.
    * The routine does add the newline to complete the record just prior to returning 
    * the String to the caller.
    * 
    * @param fieldSeparator used to delimit fields in test record
    * @return String test record ready for export
    */
   public String exportTestRecord (String fieldSeparator) {
      StringBuffer sb = new StringBuffer();
      sb.append(getTestRecordID());
      sb.append(fieldSeparator);
      sb = appendCommandToTestRecord(sb, fieldSeparator);
      sb = appendParametersToTestRecord(sb, fieldSeparator);
      return sb.toString();
   }

   /**
    * Called by exportTestRecord() after the testRecordID() and a field
    * separator have been appended to the StringBuffer.  The default
    * implementation appends getCommandName() only with no additional separator.  
    * ComponentFunction, for example, needs to override
    * in order to prepend the window name and component name in front of the
    * command name.
    * 
    * @param sb StringBuffer to append fields to
    * @param fieldSeparator character to append in between fields
    * @return sb StringBuffer appended as appropriate.
    */
   protected StringBuffer appendCommandToTestRecord(StringBuffer sb, String fieldSeparator) {
      sb.append(getCommandName());
      return sb;
   }   
}