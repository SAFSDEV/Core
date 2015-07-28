
package org.safs.tools.data;

import java.awt.Point;

/** <br><em>Purpose:</em> Class NodeInfo contains information about a particular node (in a 2D Array)
 *
 * @author bolawl	-Added 09.02.2005 (RJL)
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
