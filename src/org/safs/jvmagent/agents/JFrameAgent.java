/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent.agents;

import java.awt.*;
import java.util.*;
import javax.swing.JFrame;

import org.safs.jvmagent.NoSuchPropertyException;


/**
 * @author canagl
 */
public class JFrameAgent extends FrameAgent {

	/** 
	 * "JFrame"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "JFrame";
	
	/**
	 * Constructor for JFrameAgent.
	 */
	public JFrameAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.FrameAgent.class.getName());
	}

	/**
	 * @see org.safs.jvmagent.LocalAgent#getChildCount(Object)
	 */
	public int getChildCount(Object object) {
		return getChildren(object).length;
	}

	/**
	 * Some returned children might be null.
	 * @see org.safs.jvmagent.LocalAgent#getChildren(Object)
	 */
	public Object[] getChildren(Object object) {
		if(!(object instanceof JFrame)) return super.getChildren(object);
		JFrame frame = (JFrame) object;
		
		//Object[] children = super.getChildren(object);
		Object[] children = new Object[0];
		
		Object[] morechildren = new Object[]{
			frame.getRootPane()
		};
		ArrayList allkids = new ArrayList(children.length + morechildren.length);
		for(int i=0;i<children.length;i++) allkids.add(children[i]);
		Object child = null;
		for(int i=0;i<morechildren.length;i++) {
			child = morechildren[i];
			//if(child instanceof Component) System.out.println("FrameChild("+i+"):"+child.getClass().getName());
			if(child instanceof Component) allkids.add(child);
		}
		allkids.trimToSize();
		return (Object[]) allkids.toArray(new Object[0]);
	}

}
