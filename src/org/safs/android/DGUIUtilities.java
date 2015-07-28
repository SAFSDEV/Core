/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.android;

import java.util.List;

import org.safs.DDGUIUtilities;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.Tree;

public class DGUIUtilities extends DDGUIUtilities {

	public DGUIUtilities() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int waitForObject(String appMapName, String windowName,
			String compName, long secTimeout)
			throws SAFSObjectNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setActiveWindow(String appMapName, String windowName,
			String compName) throws SAFSObjectNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object findPropertyMatchedChild(Object obj, String property,
			String bench, boolean exactMatch)
			throws SAFSObjectNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List extractListItems(Object obj, String countProp, String itemProp)
			throws SAFSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getListItem(Object obj, int i, String itemProp)
			throws SAFSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tree extractMenuBarItems(Object obj) throws SAFSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tree extractMenuItems(Object obj) throws SAFSException {
		// TODO Auto-generated method stub
		return null;
	}

}
