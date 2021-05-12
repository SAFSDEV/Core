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
