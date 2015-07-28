/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.custom;

import java.util.*;

import org.safs.*;

/**
 * <br><em>Purpose:</em> DCDriverCommand, custom driver commands
 * <br><em>Lifetime:</em> instantiated by DriverCommandProcessor
 * im
 * <p>
 * @author  Doug Bauman
 * @since   JUN 14, 2003
 *
 *   <br>   Oct 02, 2003    (DBauman) Original Release
 **/
public class DCDriverCommand extends DriverCommand {

  //public static final String ABC_123_YOU_AND_ME_1       = "ABC_123_YOU_AND_ME_1";
  //public static final String ABC_123_YOU_AND_ME_2       = "ABC_123_YOU_AND_ME_2";

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public DCDriverCommand () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is a driver command processor.
   ** Current commands :<br>
   ** <br> none yet...
   ** <br> 
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing; processedCommand is set to false
   * if we do not recognize this command
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  public void process() {
    //if (testRecordData.getCommand().equalsIgnoreCase(ABC_123_YOU_AND_ME_1)) {
    //} else if (testRecordData.getCommand().equalsIgnoreCase(ABC_123_YOU_AND_ME_2)) {
    //} else {
    Log.info("Custom driver commands, FOR NOW< THERE ARE NONE >");
    setRecordProcessed(false);
    //}
  }

}
