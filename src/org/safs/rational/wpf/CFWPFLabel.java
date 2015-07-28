package org.safs.rational.wpf;

import org.safs.rational.CFComponent;

/**
 * <br><em>Purpose:</em> CFWPFLabel, process a WPFLabel component
 * <br><em>Lifetime:</em> instantiated by TestStepProcessor
 * <br><em>The Code is a copy of CFLabel. It is just for taking care of mapping class WPFLabel. 
 * @author  Junwu Ma
 * @since   JUN 11, 2010 
 * <p>
 **/
public class CFWPFLabel extends CFComponent {

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public CFWPFLabel () {
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
    //?? none special for WPFLabel now.
  }
}
