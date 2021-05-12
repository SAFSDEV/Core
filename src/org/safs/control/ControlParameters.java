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
