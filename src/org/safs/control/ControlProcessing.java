/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.control;

import java.util.*;

/**
 * Description   : ControlProcessing interface, can indicate when processing is complete
 * @author dbauman
 * @since   DEC 17, 2003
 *
 *   <br>   DEC 17, 2003    (DBauman) Original Release
 * <p>
 */

public interface ControlProcessing {

  public void processingComplete (boolean pc);
  /** pass along to 'controlObservable'
   ** with a line like this: <br> <pre>
   ** controlObservable.addObserver(observer); </pre>
   **/
  public void addObserver(Observer observer);
  public void start();
}
