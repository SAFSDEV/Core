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
