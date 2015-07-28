/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
