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
