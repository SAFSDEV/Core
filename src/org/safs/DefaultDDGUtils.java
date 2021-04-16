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

import org.safs.Log;
import org.safs.DDGUtilsInterface;

import java.util.*;

/**
 * <br><em>Purpose:</em>DDG utilities, concept taken from SQABasic version
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 *   <br>   ??? ??, 2003    Original Release
 **/
public class DefaultDDGUtils implements DDGUtilsInterface {

  public Object getGuiObject (Object script, String windowName, String compName, String appMapName,
                              String appMapFilename) throws SAFSException {
    Log.info("............................getGuiObject, compName: "+compName);
    return null;
  }

  public Object getGuiObject (Object script, String windowName, String compName, String appMapName)
    throws SAFSException {
    return getGuiObject(script, windowName, compName, appMapName, null);
  }

  public String getGuiType (Object script, String windowName, String compName, String appMapName,
                            String appMapFilename) throws SAFSException {
    Object obj = (Object) getGuiObject(script, windowName, compName, appMapName, appMapFilename);
    Log.warn(".....................getGuiType, compName:"+compName);
    return "Unknown";
  }

  public String getGuiType (Object script, String windowName, String compName, String appMapName)
    throws SAFSException {
    return getGuiType(script, windowName, compName, appMapName, null);
  }

  public int waitForSQAObject (Object testObject, int waittime) {
    Log.error("DDGUtils.ddgWaitForSQAObject: Not implmented yet, testObject: "+testObject+
              ", waittime: "+waittime);
    return 1;//??
  }

  public String getGUIPropertyString (String str, String comp) {
    Log.error("DDGUtils.ddgGetGUIPropertyString: Not implmented yet, str: "+str+
              ", comp: "+comp);
    return str + "|"+ comp;
  }
}
