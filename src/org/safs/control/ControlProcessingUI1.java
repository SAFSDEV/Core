
package org.safs.control;

public interface ControlProcessingUI1 extends ControlProcessing {
  /** set the text of the 'statusCode' text field **/
  public void setStatusCode(String status);
  /** set the text of the 'testLogMsg' text field **/
  public void setTestLogMsg(String testLogMsg);
  /** set the text of the 'appMapItem' text field **/
  public void setAppMapItem(String val);
  /** set the text of the 'inputRecord' text field **/
  public void setInputRecord(String rec);
  /** set the text of the 'delim' text field **/
  public void setDelim(String d);
}
