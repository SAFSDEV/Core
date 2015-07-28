/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.util.*;

import org.safs.*;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;
import com.rational.test.ft.object.map.*;

/**
 * <br><em>Purpose:</em> CFButton, process a BUTTON component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 *   <br>   JUN 04, 2003    (DBauman) Original Release
 **/
public class CFButton extends CFComponent {

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFButton () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is our specific version. We subclass the generic CFComponent.
   ** We first call super.process() [which handles actions like 'click']
   ** The actions handled here are:
   ** <br><ul>
   ** <li>none
   ** </ul><br>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  protected void localProcess() {
    //?? none for now
  }
}
