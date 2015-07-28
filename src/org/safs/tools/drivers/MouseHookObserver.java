/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.drivers;
import java.awt.Point;

/** 
 * MouseHookObserver
 * an interface as Observer used in MouseCheckTimer. The class that needs MouseCheckTimer to fire up, 
 * should implements this interface.
 * 
 * @see org.safs.tools.drivers.MouseCheckTimer
 * @see org.safs.tools.drivers.STAFProcessContainer
 * <p>
 * @author  Junwu Ma
 * @since   OCT 22, 2009
 */
public interface MouseHookObserver{
	public void onHandleMouseCheck(Point point); 
};