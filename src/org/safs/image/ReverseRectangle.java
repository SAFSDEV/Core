/**
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.image;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * ReverseRectangle functions as a standard Rectangle object.
 * However, where a normal rectangle assumes 0,0 is at the top-left corner, 
 * this class assumes 0,0 is at the bottom-left corner.
 * <p>
 * Consequently, rectangles with higher Y values are actually higher up on a plane 
 * than rectangles with lower Y values.
 * <p>
 * The class primarily serves as a marker. Users must treat the Rectangle 
 * information accordingly when comparing or merging with other Rectangles.
 * 
 * @author Carl Nagle
 * <br>OCT 21, 2010 Carl Nagle Initial Release
 */
public class ReverseRectangle extends Rectangle {

	public ReverseRectangle() {
		super();
	}

	public ReverseRectangle(Rectangle r) {
		super(r);
	}

	public ReverseRectangle(Point p) {
		super(p);
	}

	public ReverseRectangle(Dimension d) {
		super(d);
	}

	public ReverseRectangle(int width, int height) {
		super(width, height);
	}

	public ReverseRectangle(Point p, Dimension d) {
		super(p, d);
	}

	public ReverseRectangle(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
}
