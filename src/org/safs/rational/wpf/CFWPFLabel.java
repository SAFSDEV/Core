/**
 * Copyright (C) SAS Institute, All rights reserved.
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
