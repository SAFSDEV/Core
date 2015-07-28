/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.control;
import java.util.*;
/**
 * Description   : ControlParameters, contains objects passed to the control
 * @author dbauman
 * @since   DEC 17, 2003
 *
 *   <br>   DEC 17, 2003    (DBauman) Original Release
 * <p>
 *
 */

public class ControlParameters {
  private String command;
  public String getCommand() {iterator=params.iterator(); return command;}
  public void setCommand(String command) {this.command=command;}
  private Collection params = new LinkedList();
  private Iterator iterator;
  public void addParam(Object param) {params.add(param);}
  public boolean hasNext () {return iterator.hasNext();}
  public Object next() {return iterator.next();}
  public String nextToken() {return (String)next();}
}
