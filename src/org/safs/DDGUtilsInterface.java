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

import java.util.*;

/**
 * <br><em>Purpose:</em>static DDG utilities, concept taken from SQABasic version
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 *   <br>   JUN 04, 2003    (DBauman) Original Release
 **/
public interface DDGUtilsInterface {

  public Object getGuiObject (Object script, String windowName, String compName, String appMapName,
                              String appMapFilename) throws SAFSException;

  public Object getGuiObject (Object script, String windowName, String compName,
                              String appMapName) throws SAFSException;

  public String getGuiType (Object script, String windowName, String compName, String appMapName,
                            String appMapFilename) throws SAFSException;

  public String getGuiType (Object script, String windowName, String compName, String appMapName)
   throws SAFSException;

  public int waitForSQAObject (Object testObject, int waittime);

  public String getGUIPropertyString (String str, String comp);
}
