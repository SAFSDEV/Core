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
package org.safs.tools.data;

import java.awt.Point;

/** <br><em>Purpose:</em> Class NodeInfo contains information about a particular node (in a 2D Array)
 *
 * @author Bob Lawler	-Added 09.02.2005 (RJL)
 **/
public class NodeInfo {
	
	private Point nodepoint;	//x, y coordinates of node
	private String nodepath;	//fully-qualified path of node

	/** Constructors
	 */
	public NodeInfo() {
		nodepoint = new Point();
		nodepath = new String("");
	}
	public NodeInfo(Point p, String path) {
		nodepoint = new Point(p);
		nodepath = new String(path);

	}
	/** Methods
	 */
	public Point getPoint() {
		return nodepoint;
	}
	
	public String getPath() {
		return nodepath;
	}
	
	public void setPoint(Point p) {
		nodepoint.x = p.x;
		nodepoint.y = p.y;
	}
	
	public void setPath(String path) {
		nodepath = path;
	}
	
}
