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
package org.safs.rational.flex.custom;

import java.util.List;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.Tree;
import org.safs.rational.MenuTree;

import com.rational.test.ft.object.interfaces.TestObject;

public class CFFlexMenuBar extends org.safs.rational.flex.CFFlexMenuBar {
	private static final String MENU_AUTONAME_CONNECT = ">index:";
	
	protected void selectMenuBar(TestObject menuObj,String menuItemPath,boolean fuzzy,boolean verify) throws SAFSException {
		
		//If the standard Flex-Component comes here, we should let the super class to handle it, NOT here.
		if(!SASUtil.isSASFlexComponent(obj1)){
			super.selectMenuBar(menuObj, menuItemPath, fuzzy, verify);
			return;
		}
		
	    String debugMsg = getClass().getName()+".selectMenuBar(): ";
	    String path = menuItemPath;

	    if (verify) {
	    	MenuTree atree = null;
	    	Log.error(debugMsg+"Command with verification is not supported for SAS Flex Menu.");
	    	throw new SAFSException("Command with verification is not supported for SAS Flex Menu.");
	    }else{
	    	//Generate the path, from "Help->1->2" to "Help->Help>index:1->Help>index:1>index:2"
	    	Log.debug(debugMsg+" menuItemPath="+menuItemPath);
	    	List pathList = StringUtils.getTokenList(menuItemPath, Tree.PATH_SEPARATOR);
	    	Log.debug(debugMsg+" pathList="+pathList);

	    	StringBuffer menuPath = new StringBuffer();
	    	menuPath.append(pathList.get(0));
	    	String levelPath = menuPath.toString();
	    	
	    	for(int i=1;i<pathList.size();i++){
	    		levelPath = levelPath+MENU_AUTONAME_CONNECT+pathList.get(i);
	    		menuPath.append(Tree.PATH_SEPARATOR);
	    		menuPath.append(levelPath);
	    	}
	    	path = menuPath.toString();
	    	Log.debug(debugMsg+" path="+path);
	    }
	    
	    doSelectMenubar(menuObj, path);
	}
}
