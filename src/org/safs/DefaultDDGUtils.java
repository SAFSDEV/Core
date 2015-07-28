/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
