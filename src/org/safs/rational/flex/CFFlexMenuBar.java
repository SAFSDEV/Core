/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.rational.flex;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.Tree;
import org.safs.rational.CFMenuBar;
import org.safs.rational.FlexUtil;
import org.safs.rational.MenuTree;
import org.safs.rational.MenuTreeNode;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.flex.FlexMenuBarTestObject;
import com.rational.test.ft.object.interfaces.flex.FlexMenuTestObject;

/**
 * <br><em>Purpose:</em> 	Process a Flex-MenuBar component. 
 * @author  JunwuMa
 * @since   JAN 12, 2009
 *          JAN 14, 2009    Add Flex support for SelectMenuItem, SelectMenuItemContains, VerifyMenuItem and VerifyMenuItemContains.
 *                          The verification of Menu status (Checked...) not supported so far. 
 *          JAN 20, 2009	Refactor method doSelectMenubar.                  
 *  
 **/

public class CFFlexMenuBar extends CFMenuBar {

	/**
	 * <em>Note:</em> This method is used by the RDDGUIUtilities.java
	 */
	public static Tree staticExtractMenuItems (Object obj, int level) throws SAFSException {
		return new CFFlexMenuBar().extractMenuItems(obj, level);
	}
	
	/**
	 *  <em>Note:<em> Called by selectMenuBar(), perform selecting menu item on Flex menu bar. 
	 * 	@param menuObj, a FlexMenuBarTestObject
	 *  @param menuItemPath, menu path like "File->Open"
	 */
	protected void doSelectMenubar(TestObject menuObj, String menuItemPath) {
		String debugMsg = getClass().getName() + ".doSelecetMenubar() ";
		try {
			FlexUtil.doSelectMenubar(menuObj, menuItemPath);	
	    	testRecordData.setStatusCode(StatusCodes.OK);
		    String altText = "MenuItem \""+ menuItemPath + "\" clicked, " + windowName + ":" + compName + " " + action;
		    log.logMessage(testRecordData.getFac(),
		                   passedText.convert(PRE_TXT_SUCCESS_4, altText, menuItemPath, windowName, compName, action),
		                   PASSED_MESSAGE);			
		} catch (SAFSException se) {
	    	Log.debug(debugMsg + se.getMessage() + "item not found: " + menuItemPath);
	    	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		    log.logMessage(testRecordData.getFac(),"item not found: " + menuItemPath, FAILED_MESSAGE);			
		}
	}
	
	/**
	 * <em>Note:</em> Overrides the superclass
	 */
	protected void selectMenuBar(TestObject menuObj,String menuItemPath,boolean fuzzy,boolean verify) throws SAFSException {
	    String debugMsg = getClass().getName()+".selectMenuBar(): ";
	    String path = menuItemPath;
	    MenuTree atree = null;	      

	    if (verify) {
	    	try {
	    		atree = (MenuTree) extractMenuItems(menuObj, 0);
	    		log.logMessage(testRecordData.getFac(),"atree: "+atree, DEBUG_MESSAGE);
	    		//Do the work of matching..., verify the path
		    	//If fuzzy is true, do the partial match; otherwise do the exact match
		    	path = atree.matchPath(menuItemPath, fuzzy, null);	    		  
	    	} catch (SAFSException se) {
	  	        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		        log.logMessage(testRecordData.getFac(),debugMsg + se.getMessage(),FAILED_MESSAGE);
		        throw new SAFSException(debugMsg + se.getMessage());
		    }	 
	    	if (path != null) {
		        log.logMessage(testRecordData.getFac(),"match: " + path, DEBUG_MESSAGE);	    		  
	    	} else {
	    		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		        log.logMessage(testRecordData.getFac(),getClass().getName()+": no match on: " + path, FAILED_MESSAGE);
		        return;
		    }
	    }
	    
	    doSelectMenubar(menuObj, path);
	}
	 
	protected Integer getSubMenuItemCount(TestObject aMenuObj) {
		String debugMsg = CFFlexMenuBar.class.getName()+".getSubMenuItemCount() ";
		Integer val = new Integer(0);

		try {
			String sval = (String) aMenuObj.getProperty(FlexUtil.PROPERTY_TYPE_NUMAUTOMATIONCHILDREN);
			val = Integer.valueOf(sval);
		} catch (Exception e1) {
			try{
				TestObject subitems[] = (TestObject []) aMenuObj.getChildren();
				val = new Integer(subitems.length);
			}catch(MethodNotFoundException e2) {
				Log.debug(debugMsg + e2.getMessage());
			}
		}
		return val;
	}
		
	protected String getPropertyText(TestObject testObject){
		return (String) testObject.getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME);
	}
	
	protected String getPropertyTextName(){
		return FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME;
	}
	
	protected MenuTreeNode getNewTreeNode(Object userObject,int siblingCounter,int childrenCounter){
		return new FlexMenuTreeNode(userObject,siblingCounter,childrenCounter);
	}
	
	/**
	 * <br><em>Purpose:</em> Called by extractMenuItems() to get the children of a FlexOjectTestObject.
	 *                 Among the children of a FlexMenuBarTestOjbect, only FlexMenuTestObject will be returned and others unnecessary are ignored.     
	 * @param menuObj, a FlexObjectTestObject
	 * @return An array of TestObject found from menuObj
	 */
	protected TestObject[] getFlexMenuChildren(TestObject menuObj) {
		TestObject[] rtlChildren = null;
		TestObject[] children = menuObj.getChildren();;
		if (menuObj instanceof FlexMenuBarTestObject){
			children = menuObj.getChildren();
			int count = 0;
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof FlexMenuTestObject)
					count++;
			}
			rtlChildren = new FlexMenuTestObject[count];
			count = 0;
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof FlexMenuTestObject) 
					rtlChildren[count++] = children[i]; 
			}		
		} else
			rtlChildren = children;
	    	
		return rtlChildren;
	}
	
	/** <br><em>Note:</em> Overrides the superclass to extract a menu hierarchy from a FlexMenuBarTestObject.
	 */
	protected Tree extractMenuItems (Object obj, int level) throws SAFSException {
		    
		String debugmsg = getClass().getName()+".extractMenuItems() ";
		Tree tree = null;
		TestObject[] subitems = null;
		    
		try {
			TestObject tobj = (TestObject) obj;
			Integer itemCount = null;

			subitems = getFlexMenuChildren(tobj);
			itemCount = new Integer(subitems.length);
		    Tree lastjTree = null;
		      
		    for (int j=0; j<itemCount.intValue(); j++) {
		        TestObject gto2 = null;
		        try {
		          gto2 = subitems[j];
		        } catch (ArrayIndexOutOfBoundsException aie) {
		          Log.debug("ArrayIndexOutOfBoundsException for level: "+level+", menuitem: "+j+", probably your menu has a separator or some other unknown object, continuing...");
		          continue;
		        }
		        
		        String text2 = getPropertyText(gto2);
		        
		        // do NOT increment level for what appears to be a SubMenu placeholder
				int inc = (text2==null)? 0:1;
				
		        Integer itemCount2 = getSubMenuItemCount(gto2);
		        if (itemCount2.intValue()==0){
		        	TestObject[] subkids = getFlexMenuChildren(gto2); //gto2.getMappableChildren();
		        	if (subkids != null) itemCount2=new Integer(subkids.length);
		        }
		        
		        Log.debug("level "+ level +": item "+j+": "+ getPropertyTextName() +" \""+text2+"\" "+" children: "+itemCount2);
		        
		        //Use test object to form a tree node
		        Tree jtree = new MenuTree();
		        MenuTreeNode treeNode = getNewTreeNode(gto2,itemCount.intValue(),itemCount2.intValue());
		        jtree.setUserObject(treeNode);

		        if (j==0) tree = jtree;
		        else { lastjTree.setNextSibling(jtree); }

		        jtree.setLevel(new Integer(level));
		        jtree.setSiblingCount(itemCount);
		        jtree.setChildCount(itemCount2);
		        if (itemCount2.intValue() > 0){
		          // inc only when a valid new level exists
		          Tree subtree = extractMenuItems(gto2, level+inc);
		          jtree.setFirstChild(subtree);
		        }
		        lastjTree = jtree;
		    }  
		} catch (Exception ee) {
			throw new SAFSException(debugmsg +": "+ ee.getMessage());
		}
		return tree;
	}

	 /** <br><em>Note:</em> Overrides the superclass to handle a FlexMenuBarTestObject.
	  */
	protected void verifyMenuBar(Object anObj,String menuItemPath,String status,boolean fuzzy) throws SAFSException {
	    String debugMsg = getClass().getName()+".verifyMenuBar() ";
	    
	    FlexMenuBarTestObject menuBarObj = null;
		try{
			menuBarObj = (FlexMenuBarTestObject) anObj;
		} catch (ClassCastException  e){
			String msg = debugMsg + "Cast exceptioin. The Object passed in should be FlexMenuBarTestObject.";
			Log.debug(msg);
			throw new SAFSException(msg);
		}
			
	    MenuTree atree = null;
	    try {
	        atree = (MenuTree) extractMenuItems(menuBarObj, 0);
	        log.logMessage(testRecordData.getFac(),"atree: "+atree, DEBUG_MESSAGE);
	        // do the work of matching...        
	        String match = null;
	        match = atree.matchPath(menuItemPath, fuzzy, status);
	        
	        if (match != null) {
	          log.logMessage(testRecordData.getFac(),"match: "+match, 
	              DEBUG_MESSAGE);
	          // set status to ok
	          testRecordData.setStatusCode(StatusCodes.OK);
	          String altText = "MenuItem \""+ match +"\", "+windowName+":"+compName+" "+action;
	          log.logMessage(testRecordData.getFac(),
	                         passedText.convert(PRE_TXT_SUCCESS_4, altText, match,
	                                            windowName, compName, action),
	                         PASSED_MESSAGE);
	        } else {
	          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	          log.logMessage(testRecordData.getFac(),
	        		  		 debugMsg + ": no match on: " + menuItemPath,
	                         FAILED_MESSAGE);
	        }
	    } catch (SAFSException se) {
	        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	        log.logMessage(testRecordData.getFac(),
	        			   debugMsg+": " + se.getMessage(),
	                       FAILED_MESSAGE);
	    }
	}

}
