/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.model;

/**
 * Represents a ComponentFunction command.
 * These are only allowed in Step tables. 
 */
public class ComponentFunction extends AbstractCommand {
   
   private String _windowName;
   private String _componentName;

   /**
    * Create an instance of a ComponentFunction command to act on a specific component.
    * This is used by our org.safs.model.commands and is not normally used by the user 
    * unless they are trying to build a custom component function.
    * 
    * @param functionName -- the action keyword ("Click", "Select", etc.)
    * @param windowName -- the name of the Window on which to act
    * @param componentName -- the name of the Component within the window to act upon
    * @throws IllegalArgumentException if these parameters are null or zero-length
    */
   public ComponentFunction(String functionName, String windowName, String componentName) {
      super(functionName, COMPONENT_FUNCTION_RECORD_TYPE);
      if (windowName == null || componentName == null)
         throw new IllegalArgumentException("Window and Component names cannot be null.");
      if (windowName.length() == 0 || componentName.length() == 0)
        throw new IllegalArgumentException("Window and Component names cannot be zero-length.");

      _windowName = windowName;
      _componentName = componentName;
   }

   /**
    * Retrieves the name of the window to act upon.
    * @return the name of the window to act upon.
    */
   public String getWindowName() {
      return _windowName;
   }

   /**
    * Retrieve the name of the component to act upon.
    * @return the name of the component to act upon.
    */
   public String getComponentName() {
      return _componentName;
   }
   
   /**
    * Overrides superclass to append the windowname and componentname before
    * invoking super.appendCommandToTestRecord to append the actual command.
    * 
    * @param sb StringBuffer to append fields to
    * @param fieldSeparator character to append in between fields
    * @return sb StringBuffer appended as appropriate.
    */
   protected StringBuffer appendCommandToTestRecord(StringBuffer sb, String fieldSeparator) {
      sb.append(getWindowName());
      sb.append(fieldSeparator);
      sb.append(getComponentName());
      sb.append(fieldSeparator);
      return super.appendCommandToTestRecord(sb, fieldSeparator);
   }   
   
   private boolean _warningOK;
   private boolean _failureOK;
   
   /**
    * Indicates if a warning is acceptable for this ComponentFunction.
    * <p>
    * @return <code>true</code> if a warning is acceptable, otherwise <code>false</code>
    * @see #setWarningOK(boolean)
    */
   public final boolean isWarningOK() {
      return _warningOK;
   }
   
   /**
    * Sets whether a warning is acceptable for this ComponentFunction.
    * <p>
    * If a warning is set as acceptable, <code>setFailureOK(false)</code>
    * is called to turn off failures.
    * <p>
    * @param newValue <code>true</code> to indicate that warnings are acceptable, otherwise <code>false</code>
    * @see #isWarningOK()
    */
   public final void setWarningOK(boolean newValue) {
      if (isWarningOK() == newValue)
         return;
      _warningOK = newValue;
      if (newValue)
         setFailureOK(false);
   }
   
   /**
    * Indicates if a failure is acceptable for this ComponentFunction.
    * <p>
    * @return <code>true</code> if a failure is acceptable, otherwise <code>false</code>
    * @see #setFailureOK(boolean)
    */
   public final boolean isFailureOK() {
      return _failureOK;
   }
   
   /**
    * Sets whether a failure is acceptable for this ComponentFunction.
    * <p>
    * If a failure is set as acceptable, <code>setWarningOK(false)</code>
    * is called to turn off warnings.
    * <p>
    * @param newValue <code>true</code> to indicate that failures are acceptable, otherwise <code>false</code>
    * @see #isFailureOK()
    */
   public final void setFailureOK(boolean newValue) {
      if (isFailureOK() == newValue)
         return;
      _failureOK = newValue;
      if (newValue)
         setWarningOK(false);
   }

   /**
    * Returns the string record type for this command.  The set of values for ComponentFunction
    * commands are:
    * <ul>
    * <li>"TF" - if <code>failureOK</code> is <code>true</code>
    * <li>"TW" - if <code>warningOK</code> is <code>true</code>
    * <li>"T" - if <code>failureOK</code> and <code>warningOK</code> are <code>false</code>
    * </ul>
    * <p>
    * @return the record type of this command
    */
   public String getTestRecordID() {
      if (isFailureOK())
         return COMPONENT_FUNCTION_FAILOK_RECORD_TYPE;
      if (isWarningOK())
         return COMPONENT_FUNCTION_WARNOK_RECORD_TYPE;
      return super.getTestRecordID();
   }
}