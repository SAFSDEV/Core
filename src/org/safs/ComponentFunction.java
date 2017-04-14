/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;
/**
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 * JUN 04, 2003 (DBauman) Original Release
 * SEP 16, 2003 (CANAGL) Implemented use of new SAFSLOGS logging.
 * NOV 10, 2003 (CANAGL) Added isSupportedRecordType() implementation.
 * OCT 28, 2005 (CANAGL) Refactored to allow for override of convertCoords methods.
 * MAR 19, 2008	(LeiWang) Added componentSuccessMessage for common success message use.
 * MAR 25, 2008	(JuwnuMa) Added componentExecutedFailureMessage
 * DEC 03, 2008	(LeiWang) Modify method action_getGuiImage() and action_verifyGuiImageToFile():
 *								  Call ImageUtils.saveImageToFile() to save image, instead of ImageIO.write();
 *								  Because using ImageIO.write() will loss quality of jpg image. Now we use full
 *								  quality to save jpg image, that is when call ImageUtils.saveImageToFile(), we
 *								  set its third parameter to 1.0f
 * MAR 25, 2009 (CANAGL) Added issuePassedSuccessUsing
 * NOV 12, 2009 (CANAGL) Simple JavaDoc update
 * APR 14, 2010 (JunwuMa)Move TIDComponent.setRectVars() to ComponentFunction for sharing.
 * JUL 22, 2013 (sbjlwa) Move some methods convertXXX() to StringUtils.java and make them static.
 *								 Modify methods action_xxxGuiImage() to concentrate the redundant code and
 *							 	 move some concentrated code to org.safs.Processor
 * MAR 05, 2014 (SBJLWA) Move some keyword constants from CFComponent.
 * DEC 12, 2014 (SBJLWA) Add extra parameter "FilteredAreas" for GetGUIImage/VerifyGUIImage.
 * JAN 12, 2015 (SBJLWA) Modify some methods issueXXX(): Make comment and code consistent.
 *                       Modify some methods to give more detail (line number, file name) if keyword fails.
 * SEP 07, 2015 (SBJLWA) Handle DragTo. Correct a typo, change method preformDrag to performDrag.
 * NOV 26, 2015 (SBJLWA) Modify methods checkForCoord() so that percentage coordinate will be accepted.
 * NOV 26, 2015 (SBJLWA) Moved StringUtils.convertWindowPosition() to this class and renamed it to ConvertWindowPosition.
 */
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.safs.android.auto.lib.Console;
import org.safs.image.ImageUtils;
import org.safs.model.commands.DDDriverCommands;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.model.commands.GenericObjectFunctions;
import org.safs.model.commands.WindowFunctions;
import org.safs.robot.Robot;
import org.safs.text.FAILKEYS;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENKEYS;
import org.safs.text.GENStrings;
import org.safs.tools.ocr.OCREngine;
import org.safs.tools.stringutils.StringUtilities;

/**
 * <br><em>Purpose:</em> abstract ComponentFunction, enforces that the 'process' method be implemented
 *  <br>
 * <ul>
 * <li>COMPONENT FUNCTIONS (so far...):
 * <ul>
 * <li>  CFButton
 *
 * <li>  CFCheckBox
 * <ul>
 * <li>    check
 * <ul>
 * <li>      Example: T,Frame,rememberPasswordCheckBox,check
 * </ul>
 * <li>    uncheck
 * <ul>
 * <li>      Example: TF,Frame,rememberPasswordCheckBox,uncheck
 * </ul>
 * </ul>
 *
 * <li>  CFComboBox
 * <ul>
 * <li>    selectIndex
 * <ul>
 * <li>      1st param: index into the combobox of the item to select
 * <li>      Example: T,Frame,nameComboComboBox,selectIndex,2
 * </ul>
 * <li>    select/selectTextItem/selectUnverified/selectUnverifiedTextItem
 * <ul>
 * <li>      1st param: name of the item to select
 * <li>      Example: T,Frame,nameComboComboBox,select,Karen Farrell
 * </ul>
 * <li>    setTextValue (not sure if this one actually works, does guiObj.setProperty("text", val);)
 * <ul>
 * <li>      1st param: name of the item to select
 * <li>      Example: T,Frame,nameComboComboBox,selectPartialMatch,Sammy The Fish
 * </ul>
 * <li>    showlist/hidelist - shows or hides the list
 * <ul>
 * <li>      Example: T,Frame,nameComboComboBox,showlist
 * </ul>
 * </ul>
 *
 * <li>  CFComponent (Available to nearly all components)
 * <ul>
 * <li>    click/rightClick
 * <ul>
 * <li>      Example: T,Frame,radioButtonARadioButton,click
 * </ul>
 * <li>    closeWindow/restore/minimize/maximize
 * <ul>
 * <li>      Example: T,Frame,radioButtonARadioButton,restore
 * </ul>
 * <li>    verifyProperty - verify that a property exists and exactly matches specified value
 * <ul>
 * <li>      1st param: property name
 * <li>      2nd param: property value to verify against
 * <li>      Example: TW,PlaceOrder,totalPriceLabel,VerifyProperty,text,$19.99
 * </ul>
 * <li>    verifyPropertyContains - verify that a property exists and contains specified value
 * <ul>
 * <li>      1st param: property name
 * <li>      2nd param: property value to look for containment (substring of actual)
 * <li>      Example: TW,PlaceOrder,totalPriceLabel,VerifyPropertyContains,text,19
 * </ul>
 * <li>    verifyValue - verify that a value exactly matches specified value
 * <ul>
 * <li>      1st param: property name
 * <li>      2nd param: property value to verify against
 * <li>      Example: TW,PlaceOrder,totalPriceLabel,VerifyValue,$19.99,^variable
 * </ul>
 * <li>    verifyValueContains - verify that a value contains specified value
 * <ul>
 * <li>      1st param: property name
 * <li>      2nd param: property value to look for containment (substring of actual)
 * <li>      Example: TW,PlaceOrder,totalPriceLabel,VerifyPropertyContains,^variable,$19
 * </ul>
 * <li>    clearAppMapCache - clear the cache of testobjects, normally not needed.
 * <ul>
 * <li>      Example: T,PlaceOrder,PlaceOrder,clearAppMapCache
 * </ul>
 * <li>    assignPropertyVariable - Assign the value of a cell to a variable
 * <ul>
 * <li>      1st param: property name
 * <li>      2nd param: variablename
 * <li>      Example: T,Frame,orderTableTable,assignPropertyVariable,text,var1
 * <li>      Example: T,Frame,orderTableTable,assignPropertyVariable,text,var2
 * <li>      Example: T,Frame,orderTableTable,assignPropertyVariable,text,var3
 * </ul>
 * <li>    inputKeys - input keys to the frame for the component
 * <ul>
 * <li>      1st param: keys
 * <li>      Example: T,PlaceOrder,_cardNumberFieldText,inputKeys,1234123412341234
 * <li>      Example: T,PlaceOrder,_expireFieldText,inputKeys,09
 * <li>      Example: T,PlaceOrder,_expireFieldText,inputKeys,/03
 * </ul>
 * <li>    VerifyObjectDataToFile, grab value(s) in "text" property, verify against file
 * <ul>
 * <li>      1st param: file to verify against (in the bench directory)
 * <li>      Example: T,PlaceOrder,PlaceOrder,VerifyObjectDataToFile,file.txt
 * </ul>
 * <li>    VerifyTextFileToFile, verify one file to another
 * <ul>
 * <li>      1st param: file to verify against (in the bench directory)
 * <li>      2nd param: file to verify against (in the bench directory)
 * <li>      Example: T,PlaceOrder,PlaceOrder,VerifyObjectDataToFile,file.txt
 * </ul>
 * </ul>
 * <li>  CFLabel
 *
 * <li>  CFList (similar to combobox)
 * <ul>
 * <li>    activatetextitem
 * <li>    activatepartialmatch
 * <li>    activateunverifiedpartialmatch
 * <li>    activateunverifiedtextitem
 * <li>    select
 * <li>    selectTextItem
 * <li>    selectIndex
 * <li>    selectPartialMatch
 * <li>    verifymenupartialmatch - verify if a menu item exists (partial match)
 * <li>    verifymenuitem - verify if a menu item exists
 * <li>    verifypartialmatch - based on an index
 * <li>    verifyitem - based on an index
 * <li>    selectUnverifiedPartialMatch
 * <li>    selectUnverified
 * <li>    selectUnverifiedTextItem
 * <li>    setTextValue
 * </ul>
 * <li>  CFMenuBar
 * <ul>
 * <li>    selectMenuItem
 * <li>    selectMenuItemContains
 * </ul>
 * <li>  CFPageTabList
 * <ul>
 * <li>    click/clicktab
 * <ul>
 * <li>      1st param: the tab to click
 * <li>      Example: T,Frame,tabbedPanePageTabList,clicktab,Details
 * </ul>
 * <li>    selecttab/makeselection
 * <ul>
 * <li>      1st param: the tab to select
 * <li>      Example: T,Frame,tabbedPanePageTabList,selecttab,Details
 * </ul>
 * </ul>
 * <li>  CFPopupMenu
 * <ul>
 * <li>    selectPopupMenuItem
 * <li>    verifyPopupMenuItem
 * <ul>
 * <li>      1st param: verify a popup menu item exists
 * <li>      Example: T,Frame,popup,verifyPopupMenuItem,"Item"
 * </ul>
 * </ul>
 * <li>  CFTable/CFJCTable (for Swing JTable, Sitraka JCTable respectively)
 * <ul>
 * <li>    clickCellOfColWithRowValues/doubleclickCellOfColWithRowValues - click or doubleclick a cell specified by the params (CFTable only)
 * <ul>
 * <li>      1st param(opt): row;col coordinate within the cell, if omitted then the coords not used
 * <li>      2nd param: Column name of the cell to click on
 * <li>      3rd param: for a row, first column name of the row to click on
 * <li>      4th param: for a row, first cell value of the row to click on for this column
 * <li>      5th param(opt): for a row, second column name of the row to click on
 * <li>      6th param(opt): for a row, second cell value of the row to click on for this column
 * <li>      7th param(opt): for a row, third column name of the row to click on
 * <li>      8th param(opt): for a row, third cell value of the row to click on for this column
 * <li>      Example: T,Frame,orderTableTable,clickCellOfColWithRowValues,,TOTAL,ORDERDATE,3/11/98,EXPDATE,1298,QUANTITY,3
 * <li>      Example: T,Frame,orderTableTable,clickCellOfColWithRowValues,43;12,TOTAL,ORDERDATE,3/11/98,EXPDATE,1298,QUANTITY,3
 * </ul>
 * <li>    click/doubleclick/activatecell - click or doubleclick(activatecell) a cell specified by the params (from 1)
 * <ul>
 * <li>      1st param: row;col row and column of the cell to click
 * <li>      Example: T,Frame,orderTableTable,click,1;2
 * </ul>
 * <li>    selectRowWithRowValues - select a row specified by the params (CFTable only)
 * <ul>
 * <li>      1st param: for a row, first column name of the row to click on
 * <li>      2nd param: for a row, first cell value of the row to click on for this column
 * <li>      3rd param(opt): for a row, second column name of the row to click on
 * <li>      4th param(opt): for a row, second cell value of the row to click on for this column
 * <li>      5th param(opt): for a row, third column name of the row to click on
 * <li>      6th param(opt): for a row, third cell value of the row to click on for this column
 * <li>      Example: T,Frame,orderTableTable,selectRowWithRowValues,ORDERDATE,3/11/98,EXPDATE,1298,QUANTITY,3
 * </ul>
 * <li>    selectcell/selectcelltext - select a cell specified by the params (from 1)
 * <ul>
 * <li>      1st param(opt): row;col row and column of the cell to select
 * <li>      Example: T,Frame,orderTableTable,selectcell,3;4
 * </ul>
 * <li>    selectfuzzycelltext - fuzzy select a cell specified by the params (from 1)
 * <ul>
 * <li>      1st param(opt): row;col row and column of the cell to select
 * <li>      Example: T,Frame,orderTableTable,selectcell,3;4
 * </ul>
 * <li>    verifycelltext/verifyfuzzycelltext - verify the text of a cell
 * <ul>
 * <li>      1st param: basevalue - value to compare against
 * <li>      2nd param(opt): row;col row and column of the cell to verify text
 * <li>      3rd param(opt): if included, the name of the column, in which case the col is ignored.
 * <li>      Example: T,Frame,orderTableTable,verifyCellText,xyz
 * <li>      Example: T,Frame,orderTableTable,verifyCellText,750 Central Expy,2;3
 * <li>      Example: T,Frame,orderTableTable,verifyCellText,750 Central Expy,2,STREET
 * </ul>
 * <li>    verifycelltextcontains/verifyfuzzycelltextcontains - verify the text of a cell contains substring
 * <ul>
 * <li>      1st param: basevalue - value to compare against (substring)
 * <li>      2nd param(opt): row;col row and column of the cell to verify text
 * <li>      3rd param(opt): if included, the name of the column, in which case the col is ignored.
 * <li>      Example: T,Frame,orderTableTable,verifyCellTextContains,xy
 * <li>      Example: T,Frame,orderTableTable,verifyCellTextContains,750 Central,2;3
 * <li>      Example: T,Frame,orderTableTable,verifyCellTextContains,50 Cen,2,STREET
 * </ul>
 * <li>    assignVariableCellText/assignVariableFuzzyCellText - Assign the value of a cell to a variable
 * <ul>
 * <li>      1st param: variablename
 * <li>      2nd param(opt): row;col row and column of the cell get text from
 * <li>      3rd param(opt): if included, the name of the column, in which case the col is ignored.
 * <li>      Example: T,Frame,orderTableTable,assignVariableCellText,var1
 * <li>      Example: T,Frame,orderTableTable,assignVariableCellText,var2,2;3
 * <li>      Example: T,Frame,orderTableTable,assignVariableCellText,var3,2,STREET
 * </ul>
 * <li>    captureRangeToFile/captureFuzzyRangeToFile - capture range to file
 * <ul>
 * <li>      1st param: variablename
 * <li>      2nd param(opt): startRow
 * <li>      3rd param(opt): startCol
 * <li>      4rt param(opt): numRows
 * <li>      5th param(opt): nujCols
 * <li>      Example: T, JavaWin, JTable, CaptureRangeToFile, AFileName.txt
 * <li>      Example: T, JavaWin, JTable, CaptureRangeToFile, AFileName.txt, 4, "Field5", 2, 5
 * </ul>
 * </ul>
 * <li>  CFText
 * <ul>
 * <li>    setTextValue
 * <ul>
 * <li>      1st param: the text to set in the text value
 * <li>      Example: T,Frame,_expireFieldText,SetTextValue,12/03
 * </ul>
 * </ul>
 * <li>  CFTree
 * <ul>
 * <li>    click
 * <ul>
 * <li>      1st param: the node of the tree to click
 * <li>      Example: T,Frame,tree2Tree,click,Composers->Bach->Brandenburg Concertos Nos. 1 & 3
 * </ul>
 * <li>    doubleclick
 * <ul>
 * <li>      1st param: the node of the tree to doubleclick
 * <li>      Example: T,Frame,tree2Tree,doubleclick,Composers->Bach
 * </ul>
 * <li>    select/makeselection/selecttextnode/selectpartialtextnode
 * <ul>
 * <li>      1st param: the node of the tree to select
 * <li>      Example: T,Frame,tree2Tree,select,Composers->Bach->Violin Concertos
 * </ul>
 * <li>    expand/expandtextnode/expandpartialtextnode
 * <ul>
 * <li>      1st param: the node of the tree to expand
 * <li>      Example: T,Frame,tree2Tree,expand,Composers->Schubert
 * </ul>
 * <li>    collapse/collapsetextnode/collapsepartialtextnode
 * <ul>
 * <li>      1st param: the node of the tree to collapse
 * <li>      Example: T,Frame,tree2Tree,collapse,Composers->Haydn
 * </ul>
 * </ul>
 * </ul>
 * </ul>
 **/
public abstract class ComponentFunction extends Processor{
	//GenericObjectFunctions Actions
	public static final String ALTCLICK						= "AltClick";
	public static final String CLICK						= GenericObjectFunctions.CLICK_KEYWORD;//"Click";
	public static final String COMPONENTCLICK				= "ComponentClick";
	public static final String CTRLCLICK					= GenericObjectFunctions.CTRLCLICK_KEYWORD;//"CtrlClick";
	public static final String CTRLRIGHTCLICK				= GenericObjectFunctions.CTRLRIGHTCLICK_KEYWORD;//"CtrlRightClick";
	public static final String DOUBLECLICK					= GenericObjectFunctions.DOUBLECLICK_KEYWORD;//"DoubleClick";
	public static final String MOUSECLICK					= GenericObjectFunctions.MOUSECLICK_KEYWORD;//"MouseClick";
	public static final String RIGHTCLICK					= GenericObjectFunctions.RIGHTCLICK_KEYWORD;//"RightClick";
	public static final String SHIFTCLICK					= GenericObjectFunctions.SHIFTCLICK_KEYWORD;//"ShiftClick";
	public static final String HSCROLLTO					= GenericObjectFunctions.HSCROLLTO_KEYWORD;//"HScrollTo";
	public static final String VSCROLLTO					= GenericObjectFunctions.VSCROLLTO_KEYWORD;//"VScrollTo";
	public static final String ALTLEFTDRAG					= GenericObjectFunctions.ALTLEFTDRAG_KEYWORD;//"AltLeftDrag";
	public static final String CTRLALTLEFTDRAG				= GenericObjectFunctions.CTRLALTLEFTDRAG_KEYWORD;//"CtrlAltLeftDrag";
	public static final String CTRLLEFTDRAG					= GenericObjectFunctions.CTRLLEFTDRAG_KEYWORD;//"CtrlLeftDrag";
	public static final String CTRLSHIFTLEFTDRAG			= GenericObjectFunctions.CTRLSHIFTLEFTDRAG_KEYWORD;//"CtrlShiftLeftDrag";
	public static final String LEFTDRAG						= GenericObjectFunctions.LEFTDRAG_KEYWORD;//"LeftDrag";
	public static final String RIGHTDRAG					= GenericObjectFunctions.RIGHTDRAG_KEYWORD;//"RightDrag";
	public static final String SHIFTLEFTDRAG				= GenericObjectFunctions.SHIFTLEFTDRAG_KEYWORD;//"ShiftLeftDrag";

	//GenericMasterFunctions Actions
	public static final String ASSIGNPROPERTYVARIABLE		= GenericMasterFunctions.ASSIGNPROPERTYVARIABLE_KEYWORD;//"AssignPropertyVariable";
	public static final String CAPTUREOBJECTDATATOFILE		= GenericMasterFunctions.CAPTUREOBJECTDATATOFILE_KEYWORD;//"CaptureObjectDataToFile";
	public static final String CAPTUREPROPERTIESTOFILE  	= GenericMasterFunctions.CAPTUREPROPERTIESTOFILE_KEYWORD;//"CapturePropertiesToFile";
	public static final String CAPTUREPROPERTYTOFILE    	= GenericMasterFunctions.CAPTUREPROPERTYTOFILE_KEYWORD;//"CapturePropertyToFile";
	public static final String CLEARAPPMAPCACHE           	= GenericMasterFunctions.CLEARAPPMAPCACHE_KEYWORD;//"ClearAppMapCache";
	public static final String GUIDOESEXIST					= GenericMasterFunctions.GUIDOESEXIST_KEYWORD;//"GUIDoesExist";
	public static final String GUIDOESNOTEXIST				= GenericMasterFunctions.GUIDOESNOTEXIST_KEYWORD;//"GUIDoesNotExist";
	public static final String GETGUIIMAGE					= GenericMasterFunctions.GETGUIIMAGE_KEYWORD;//"GetGuiImage";
	public static final String HOVERMOUSE					= GenericMasterFunctions.HOVERMOUSE_KEYWORD;//"HoverMouse";
	public static final String INPUTCHARACTERS				= GenericMasterFunctions.INPUTCHARACTERS_KEYWORD;//"InputCharacters";
	public static final String INPUTKEYS					= GenericMasterFunctions.INPUTKEYS_KEYWORD;//"InputKeys";
	public static final String ISPROPERTYEXIST	      		= GenericMasterFunctions.ISPROPERTYEXIST_KEYWORD;//"IsPropertyExist";
	public static final String SENDEVENT				    = GenericMasterFunctions.SENDEVENT_KEYWORD;//"SendEvent";
	public static final String VERIFYARRAYPROPERTYTOFILE  	= GenericMasterFunctions.VERIFYARRAYPROPERTYTOFILE_KEYWORD;//"VerifyArrayPropertyToFile";
	public static final String VERIFYBINARYFILETOFILE    	= GenericMasterFunctions.VERIFYBINARYFILETOFILE_KEYWORD;//"VerifyBinaryFileToFile";
	public static final String VERIFYCLIPBOARDTOFILE    	= GenericMasterFunctions.VERIFYCLIPBOARDTOFILE_KEYWORD;//"VerifyClipboardToFile";
	public static final String VERIFYFILETOFILE           	= GenericMasterFunctions.VERIFYFILETOFILE_KEYWORD;//"VerifyFileToFile";
	public static final String VERIFYGUIIMAGETOFILE			= GenericMasterFunctions.VERIFYGUIIMAGETOFILE_KEYWORD;//"VerifyGUIImageToFile";
	public static final String VERIFYOBJECTDATATOFILE	    = GenericMasterFunctions.VERIFYOBJECTDATATOFILE_KEYWORD;//"VerifyObjectDataToFile";
	public static final String VERIFYPROPERTY				= GenericMasterFunctions.VERIFYPROPERTY_KEYWORD;//"VerifyProperty";
	public static final String VERIFYPROPERTYTOFILE       	= GenericMasterFunctions.VERIFYPROPERTYTOFILE_KEYWORD;//"VerifyPropertyToFile";
	public static final String VERIFYPROPERTYCONTAINS		= GenericMasterFunctions.VERIFYPROPERTYCONTAINS_KEYWORD;//"VerifyPropertyContains";
	public static final String VERIFYTEXTFILETOFILE   		= GenericMasterFunctions.VERIFYTEXTFILETOFILE_KEYWORD;//"VerifyTextFileToFile";
	public static final String VERIFYVALUECONTAINS     	   	= GenericMasterFunctions.VERIFYVALUECONTAINS_KEYWORD;//"VerifyValueContains";
	public static final String VERIFYVALUEEQUALS       	   	= "VerifyValueEquals";
	public static final String VERIFYVALUES            	   	= GenericMasterFunctions.VERIFYVALUES_KEYWORD;//"VerifyValues";
	public static final String VERIFYVALUESIGNORECASE  	   	= GenericMasterFunctions.VERIFYVALUESIGNORECASE_KEYWORD;//"VerifyValuesIgnoreCase";
	public static final String SETPROPERTYVALUE		 	   	= GenericMasterFunctions.SETPROPERTYVALUE_KEYWORD;//"SetPropertyValue";
	// Keywords to detect text on GUI via OCR
	public static final String GETTEXTFROMGUI			   	= GenericMasterFunctions.GETTEXTFROMGUI_KEYWORD;//"GetTextFromGUI";
	public static final String LOCATESCREENIMAGE         	= GenericMasterFunctions.LOCATESCREENIMAGE_KEYWORD;//"LocateScreenImage";
	public static final String SAVETEXTFROMGUI		 	  	= GenericMasterFunctions.SAVETEXTFROMGUI_KEYWORD;//"SaveTextFromGUI";

	//WindowFunctions Actions
	public static final String CLOSEWINDOW					= WindowFunctions.CLOSEWINDOW_KEYWORD;//"CloseWindow";
	public static final String MAXIMIZE						= WindowFunctions.MAXIMIZE_KEYWORD;//"Maximize";
	public static final String MINIMIZE						= WindowFunctions.MINIMIZE_KEYWORD;//"Minimize";
	public static final String RESTORE						= WindowFunctions.RESTORE_KEYWORD;//"Restore";
	public static final String SELECTMENUITEM				= WindowFunctions.SELECTMENUITEM_KEYWORD;//"SelectMenuItem";
	public static final String SELECTMENUITEMCONTAINS		= WindowFunctions.SELECTMENUITEMCONTAINS_KEYWORD;//"SelectMenuItemContains";
	public static final String SETPOSITION					= WindowFunctions.SETPOSITION_KEYWORD;//"SetPosition";
	public static final String VERIFYMENUITEM		   		= WindowFunctions.VERIFYMENUITEM_KEYWORD;//"VerifyMenuItem";
	public static final String VERIFYMENUITEMCONTAINS		= WindowFunctions.VERIFYMENUITEMCONTAINS_KEYWORD;//"VerifyMenuItemContains";

	//Test Step Command version instead of Driver Command because TID cannot access any GUI application objects
	public static final String WAITFORGUI					= DDDriverCommands.WAITFORGUI_KEYWORD;//"WaitForGUI";

	/** "UUID" */
	public static final String PARAM_UUID                   = "UUID";
	/** "FILTER" */
	public static final String PARAM_FILTER                   = "FILTER";

	/** "text" property to grab item's label. **/
	protected static final String PROPERTY_text 				= "text";
	/** ".itemText" property for list and combobox to grab all the items. **/
	protected static final String PROPERTY_DOT_itemText 		= ".itemText";
	/** "Items", property to grab all the items for DotNet Control. **/
	protected static final String PROPERTY_Items 				= "Items";

	/** "innerText" property to html element's content. **/
	protected static final String PROPERTY_innerText			= "innerText";
	/** "textContent" property to html element's content. **/
	protected static final String PROPERTY_textContent 			= "textContent";
	/** "innerHTML", property to grab html element's content. **/
	protected static final String PROPERTY_innerHTML			= "innerHTML";

	/** ".actual.txt", file suffix. **/
	protected static final String TEST_DATA_SUFFIX = ".actual.txt";
	protected static final String OBJECT_DATA = "OBJECT_DATA";

	protected String action;
	protected String windowName;
	protected String compName;
	protected String mapname;

	protected DDGUIUtilities utils;

	protected Iterator<String> iterator;
	protected String altText;

	/**
	 * Updates our internal storage for action, windowName, compName, and mapname from
	 * the current TestRecordData.  Missing items in the TestRecordData will update our
	 * storage with null.
	 */
	protected void updateFromTestRecordData(){
		try{ action = testRecordData.getTrimmedUnquotedInputRecordToken(3);}
		catch(Exception npe){action = null;}
		try{ windowName = testRecordData.getWindowName();}
		catch(Exception npe){windowName = null;}
		try{ compName = testRecordData.getCompName();}
		catch(Exception npe){compName = null;}
		try{ mapname = testRecordData.getAppMapName();}
		catch(Exception npe){mapname = null;}
	}

	/** Supports standard TEST STEP record types (T, TW, TF) **/
	public boolean isSupportedRecordType(String recordType){
		if (recordType == null) return false;
		String rt = recordType.toUpperCase();
		if ((rt.equals(RECTYPE_TEST_STEP))  ||
				(rt.equals(RECTYPE_TEST_STEP_W))||
				(rt.equals(RECTYPE_TEST_STEP_F)))  return true;
		return false;
	}

	/** <br><em>Purpose:</em> zero-based params with: <br>
	 * <ol>
	 * <li>windowname
	 * <li>compname
	 * <li>+N additional parameters
	 * </ol>
	 * @return                    Collection
	 **/
	public Collection getAlternateParams () {
		Collection alt = new ArrayList();
		alt.add(windowName);
		alt.add(compName);
		alt.addAll(getParams());
		return alt;
	}

	/** <br><em>Purpose:</em> constructor
	 **/
	public ComponentFunction () {
		super();
	}


	/**
	 * set instance variables from the current testRecordData:
	 * <ul><li>action<li>windowName<li>compName<li>mapname<li>utils</ul>
	 * Subclasses can override this for any additional setup beyond getHelpersWorker.
	 * @throws SAFSException
	 */
	protected void getHelpersWorker() throws SAFSException{
		action = testRecordData.getCommand();
		windowName = testRecordData.getWindowName();
		compName = testRecordData.getCompName();
		mapname = testRecordData.getAppMapName();
		utils = ((TestRecordHelper)testRecordData).getDDGUtils();
	}

	/**
	 * Simply calls getHelpersWorker.
	 * Subclasses can override this for any additional setup beyond getHelpersWorker.
	 * @throws SAFSException
	 */
	protected void getHelpers() throws SAFSException{
		getHelpersWorker();
	}

	/** <br><em>Purpose:</em> convertNum: convert into a number
	 * <br><em>Assumptions:</em>  all exceptions are handled.
	 * @param                     numStr, String
	 * (indexed from 1, 1 will be subtracted from the number before returned)
	 * @return                    Integer if successful, null otherwise (if alpha chars instead
	 * of digits are encountered; or if number is less than one)
	 **/
	public Integer convertNum (String num) {
		return StringUtils.convertNum(num);
	}

	/**
	 * <br>
	 * <em>Purpose:</em> check 'iterator' for coords, either a map reference, or x;y notation
	 * and convert it into Point.
	 *
	 * The 'coords' could be provided in 2 formats:<br>
	 * It could be <b>number, such as "12, 15", "5, 10"</b>.
	 * Or it could be in <b>percentage format, such as "30%, 45%", "0.25, 0.8"</b>. With this
	 * format, the method {@link #getComponentRectangle()} should be provided in subclass, component's width and
	 * height are necessary to calculate the coordinate.<br>
	 *
	 * @param iterator Iterator, whose next element is the coords parameter or map-reference
	 * @return java.awt.Point, null if iterator doesn't have another element
	 * @see #checkForCoord(String)
	 * @see #getComponentRectangle()
	 **/
	protected java.awt.Point checkForCoord(Iterator<String> iterator) {
		if (iterator!=null && iterator.hasNext()) return checkForCoord((String) iterator.next());

		return null;
	}

	/**
	 * <em>Purpose:</em> check for coords, either a map reference, or x;y notation
	 * and convert it into Point.
	 *
	 * The 'coords' could be provided in 2 formats:<br>
	 * It could be <b>number, such as "12, 15", "5, 10"</b>.
	 * Or it could be in <b>percentage format, such as "30%, 45%", "0.25, 0.8"</b>. With this
	 * format, the method {@link #getComponentRectangle()} should be provided in subclass, component's width and
	 * height are necessary to calculate the coordinate.<br>
	 *
	 * @param coordinate String, either an AppMap reference, or (x;y) notation
	 * @return java.awt.Point, null if coordinate is null or empty
	 *
	 * @see #getComponentRectangle()
	 **/
	protected java.awt.Point checkForCoord(String coordinate) {
		String debugmsg = StringUtils.debugmsg(false);
		String[] coordsPair = null;
		java.awt.Point point = null;

		Log.info(debugmsg+ "checking for coordinate: " + coordinate);
		if(!StringUtils.isValid(coordinate)){
			IndependantLog.warn(StringUtils.debugmsg(false)+"The passed in parameter '"+coordinate+"' is not valid");
			return null;
		}
		//Treat the parameter coordinate as a reference and try to get the coordinate String from the Map file.
		String lookup = lookupAppMapReference(coordinate);
		//If we can not find the value for 'coordinate' from the Map file, we use it directly as coordinate String.
		if( lookup == null) lookup = coordinate;
		//convert the coordinate string "x, y" into an array [x, y]
		coordsPair = StringUtils.convertCoordsToArray(lookup, 2);
		IndependantLog.debug(debugmsg+" the coordinate has been converted to array '"+Arrays.toString(coordsPair)+"'.");
		//convert the coordinate array [x, y] based on the component's rectangle
		point = StringUtils.convertCoords(coordsPair, getComponentRectangle());
		IndependantLog.debug(debugmsg+" the final coordinate is '"+point+"'.");

		return point;
	}

	/**
	 * Convert coordinates string of the formats:
	 * <ul>
	 * <li>"x;y"
	 * <li>"x,y"
	 * <li>"x y"
	 * <li>"Coords=x;y"
	 * <li>"Coords=x,y"
	 * <li>"Coords=x y"
	 * </ul>
	 * into a java.awt.Point object.
	 * <p>
	 * Subclasses may override to convert alternative values, such
	 * as Row and Col values as is done in org.safs.rational.CFTable
	 *
	 * @param   coords, String x;y or x,y or Coords=x;y  or Coords=x,y
	 * @return  Point if successful, null otherwise
	 **/
	public java.awt.Point convertCoords(String coords) {
		return StringUtils.convertCoords(coords);
	}

	/**
	 * Convert 2-point Line coordinates string of the formats:
	 * <ul>
	 * <li>"x1;y1;x2;y2"
	 * <li>"x1,y1,x2,y2"
	 * <li>"x1 y1 x2 y2"
	 * <li>"Coords=x1;y1;x2;y2"
	 * <li>"Coords=x1,y1,x2,y2"
	 * <li>"Coords=x1 y1 x2 y2"
	 * </ul>
	 * into a java.awt.Polygon object.
	 *
	 * @param   coords, String x1;y1;x2;y2 or x1,y1,x2,y2 or Coords=x1;y1;x2;y2  or Coords=x1,y1,x2,y2
	 * @return  Polygon if successful, null otherwise
	 **/
	public java.awt.Polygon convertLine(String coords) {
		return StringUtils.convertLine(coords);
	}

	/**
	 * Convert window's position-size-status string of the formats:
	 * <ul>
	 * <li>"x;y;width;height;status"
	 * <li>"x,y,width,height,status"
	 * <li>"x y width height status"
	 * <li>"Coords=x;y;width;height;Status=status"
	 * <li>"Coords=x,y,width,height,Status=status"
	 * <li>"Coords=x y width height Status=status"
	 * </ul>
	 * into a org.safs.ComponentFunction.Window object.
	 *
	 * @param   windowPosition String, window's position-size-status string; or a map reference for window's status string.
	 * @return  org.safs.ComponentFunction.Window if successful, null otherwise
	 **/
	public Window convertWindowPosition(String windowPosition){
		String position = windowPosition;
		String temp = lookupAppMapReference(windowPosition);
		if (temp != null) position = temp;
		return ConvertWindowPosition(position);
	}

	/**
	 * Looks up an item using the current appmapID=mapname, section=compName, item=referenceName.
	 * mapname and compName are expected to already be set from prior command processing.
	 * @param  referenceName
	 * @return retrieved value or null
	 **/
	protected String lookupAppMapReference(String referenceName) {
		String lookup = getAppMapItem(mapname, compName, referenceName);

		Log.info("***** LOOKUP THE APPMAP REFERENCE: "+referenceName+ ", RETURNED VALUE: "+lookup);
		if (lookup == null) {
			Log.info("lookupAppMapReference not found! ");
		}
		return lookup;
	}

	/**
	 * lookupAppMapCoordReference: uses 'convertCoords'
	 * after the call to 'lookupAppMapReference'.
	 * @param referenceName, String, the reference name passed on to
	 *         method 'lookupAppMapReference' to lookup the coordinate string
	 *         from an AppMap. Then that value is passed on to convertCoords.
	 * @return Point if successful, null otherwise (if alpha chars instead
	 *          of digits are encountered; or if row or col less than one)
	 **/
	protected java.awt.Point lookupAppMapCoordReference(String referenceName) {
		String lookup = lookupAppMapReference(referenceName);
		if( lookup == null) return null;

		Log.debug("calling convertCoords: ("+lookup+")");
		return convertCoords(lookup);
	}

	/**
	 * lookupAppMapLineReference: uses 'convertLine'
	 * after the call to 'lookupAppMapReference'.
	 * @param referenceName, String, the reference name passed on to
	 *         method 'lookupAppMapReference' to lookup the Line string
	 *         from an AppMap. Then that value is passed on to convertLine.
	 * @return Polygon if successful, null otherwise (if alpha chars instead
	 *          of digits are encountered; or if less than 2 points are detected.)
	 **/
	protected java.awt.Polygon lookupAppMapLineReference(String referenceName) {
		String lookup = lookupAppMapReference(referenceName);
		if( lookup == null) return null;

		Log.debug("calling convertLine: ("+lookup+")");
		return convertLine(lookup);
	}

	/** <br><em>Purpose:</em> lookupAppMapNumReference: uses 'convertNum'
	 ** after the call to 'lookupAppMapReference'.
	 * @param                     referenceName, String, the reference name passed on to
	 * method 'lookupAppMapReference' to lookup the number string from an AppMap. Then that
	 * value is passed on to convertNum.
	 * @return                    Integer if successful, null otherwise (if alpha chars instead
	 * of digits are encountered; or if index is less than one)
	 **/
	protected Integer lookupAppMapNumReference(String referenceName) {
		String lookup = lookupAppMapReference(referenceName);
		if( lookup == null) return null;

		log.logMessage(testRecordData.getFac(), "Calling convertNum: ("+lookup+")",
				DEBUG_MESSAGE);
		Integer result = convertNum(lookup);
		if (result == null) {
			Log.info("Couldn't convertNum for lookup: "+lookup);
		}
		return result;
	}

	/**
	 * An inner class to encapsulate a window information for adjusting window size, position etc.<br>
	 * It includes also some static methods to restore, maximize, minimize or close current focused window.<br>
	 * These methods may only work on Windows system, it uses java Robot to trigger shortcut menu.<br>
	 */
	public static class Window{
		//define valid status for window
		/** 'NORMAL'. */
		public static final String NORMAL		= "NORMAL";
		/** 'MINIMIZED'. */
		public static final String MINIMIZED  = "MINIMIZED";
		/** 'MAXIMIZED'. */
		public static final String MAXIMIZED  = "MAXIMIZED";

		/** the left-up corner of the Window. */
		private Point position;
		/** the width and height of the Window. */
		private Dimension size;
		/** the status of the Window, can be one of 'NORMAL', 'MINIMIZED' or 'MAXIMIZED' */
		private String status;

		public Window(int x, int y, int width, int height){
			position = new Point(x, y);
			size = new Dimension(width, height);
		}

		public Window(int x, int y, int width, int height, String status){
			this(x, y, width, height);
			this.status = status;
		}

		public Point getPosition() {
			return position;
		}
		public void setPosition(Point position) {
			this.position = position;
		}
		public Dimension getSize() {
			return size;
		}
		public void setSize(Dimension size) {
			this.size = size;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}

		/**
		 * Restore the current window.
		 */
		public static void restore() throws SAFSException{
			try{
				Robot.restoreFocusedWindow();
			}catch(Exception e){
				String message = "Fail to restore window. Exception="+StringUtils.debugmsg(e);
				Log.error(message);
				throw new SAFSException(message);
			}
		}

		/**
		 * Maximize the current window
		 */
		public static void maximize() throws SAFSException{
			try{
				Robot.maximizeFocusedWindow();
			}catch(Exception e){
				String message = "Fail to maximize window. Exception="+StringUtils.debugmsg(e);
				Log.error(message);
				throw new SAFSException(message);
			}
		}

		/**
		 * Minimize the current window
		 */
		public static void minimize() throws SAFSException{
			try{
				Robot.minimizeFocusedWindow();
			}catch(Exception e){
				String message = "Fail to minimize window. Exception="+StringUtils.debugmsg(e);
				Log.error(message);
				throw new SAFSException(message);
			}
		}

		/**
		 * Close the current window
		 */
		public static void close() throws SAFSException{
			try{
				Robot.closeFocusedWindow();
			}catch(Exception e){
				String message = "Fail to close window. Exception="+StringUtils.debugmsg(e);
				Log.error(message);
				throw new SAFSException(message);
			}
		}

		public String toString(){
			StringBuffer sb = new StringBuffer();
			if(position!=null) sb.append(" position:("+position.x+","+position.y+")");
			if(size!=null) sb.append(" size:("+size.width+","+size.height+")");
			if(status!=null) sb.append("status: "+status);
			return sb.toString();
		}
	}

    /**
     * Convert window's position-size-status string of the formats:
     * <ul>
     * <li>"x;y;width;height;status"
     * <li>"x,y,width,height,status"
     * <li>"x y width height status"
     * <li>"Coords=x;y;width;height;Status=status"
     * <li>"Coords=x,y,width,height,Status=status"
     * <li>"Coords=x y width height Status=status"
     * </ul>
     * into a org.safs.ComponentFunction.Window object.
     *
     * @param   windowPosition String, window's position-size-status string
     * @return  org.safs.ComponentFunction.Window if successful, null otherwise
     **/
    public static Window ConvertWindowPosition(String windowPosition) {
    	try {
    		String position = new String(windowPosition);
            // parsing preset string to get position,size and status
    		position = position.toUpperCase();
    		position = position.replace("COORDS=",""); // remove "Coords="
    		position = position.replace("STATUS=",""); // remove "Status="

    		position=position.trim();
    		Log.info("working with position: "+ windowPosition +" prefix stripped to: "+position);

    		position = position.replace(";",","); //replace ";" by "," in string "0,0,640,480;Status=NORMAL"
    		String sep = StringUtils.parseSeparator(position);
    		if (sep == null){
    			Log.error("invalid position: "+ position +".");
    			return null;
    		}

    		// properly handles case where coordsindex = -1 (not found)
    		Log.info("converting position: "+ position);
    		StringTokenizer toker = new StringTokenizer(position, sep);
    		if(toker.countTokens() < 4) {
    			Log.error("invalid position: "+ position);
    			return null;
    		}
    		String x = toker.nextToken().trim();
    		String y = toker.nextToken().trim();
    		String width = toker.nextToken().trim();
    		String height = toker.nextToken().trim();

    		if ((x.length()==0)||(y.length()==0)||(width.length()==0)||(height.length()==0)){
    			Log.error("invalid position substrings  "+ x +","+ y +", "+ width +","+ height);
    			return null;
    		}

    		Window window = new Window(
    				(int) Float.parseFloat(x),
    				(int) Float.parseFloat(y),
    				(int) Float.parseFloat(width),
    				(int) Float.parseFloat(height));

    		if(toker.hasMoreTokens()) window.setStatus(toker.nextToken().trim());

    		return window;

    	} catch (Exception ee) {
    		Log.debug( "bad window's position-size-status format: "+ windowPosition, ee);
    		return null;
    	}
    }

	/**
	 * Restore the current window.<br>
	 */
	protected void restore() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "restore");
		try {
			_restore();
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert(GENKEYS.SUCCESS_2, action+" "+ windowName+ " successful.", action, windowName);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

		}catch(Throwable th){
			String thmsg = action+" window error.";
			Log.error(debugmsg+thmsg, th);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure(thmsg+StringUtils.debugmsg(th));
		}
	}
	/**
	 * Subclass should give its own implementation, if it is difficult then call this instead, but<br>
	 * this implementation is not guarantee to work<br>
	 */
	protected void _restore() throws SAFSException{
		Window.restore();
	}

	/**
	 * Maximize the current window.
	 */
	protected void maximize() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "maximize");
		try {
			_maximize();
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert(GENKEYS.SUCCESS_2, action+" "+ windowName+ " successful.", action, windowName);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

		}catch(Throwable th){
			String thmsg = action+" window error.";
			Log.error(debugmsg+thmsg, th);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure(thmsg+StringUtils.debugmsg(th));
		}
	}
	/**
	 * Subclass should give its own implementation, if it is difficult then call this instead, but<br>
	 * this implementation is not guarantee to work<br>
	 */
	protected void _maximize() throws SAFSException{
		Window.maximize();
	}

	/**
	 * Minimize the current window.
	 */
	protected void minimize() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "minimize");
		try {
			_minimize();
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert(GENKEYS.SUCCESS_2, action+" "+ windowName+ " successful.", action, windowName);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

		}catch(Throwable th){
			String thmsg = action+" window error.";
			Log.error(debugmsg+thmsg, th);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure(thmsg+StringUtils.debugmsg(th));
		}
	}
	/**
	 * Subclass should give its own implementation, if it is difficult then call this instead, but<br>
	 * this implementation is not guarantee to work<br>
	 */
	protected void _minimize() throws SAFSException{
		Window.minimize();
	}

	/**
	 * Close the current window.
	 */
	protected void close() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "close");
		try {
			_close();
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert(GENKEYS.SUCCESS_2, action+" "+ windowName+ " successful.", action, windowName);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

		}catch(Throwable th){
			String thmsg = action+" window error.";
			Log.error(debugmsg+thmsg, th);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure(thmsg+StringUtils.debugmsg(th));
		}
	}
	/**
	 *This implementation is NOT guarantee to work, if it works, it will happen on current focused window.<br>
	 *Subclass should give its own implementation<br>
	 */
	protected void _close() throws SAFSException{
		Window.close();
	}

	/**
	 * perform SetPosition to move Main Window, resize it and set its status.
	 * Format: "Coords=0,0,640,480;Status=NORMAL". Status can be NORMAL, MINIMAZED and MAXMAZED
	 * Alteratively, semi-colon (;) can be used instead of comma (,) to separate
	 * numeric data.  This would be required when placing the data directly in the
	 * test record and NOT in the App Map when using comma-delimited test tables.
	 * @exception SAFSException
	 */
	protected void setPosition() throws SAFSException {
		String debugInf = StringUtils.debugmsg(false);
		if (params.size()<1) {
			paramsFailedMsg(windowName, compName);
			return;
		}

		try {
			// format of preset: Coords=0,0,640,480;Status=NORMAL
			String preset = (String)params.iterator().next();
			Log.info("...params for SetPosition: "+preset);

			Window window = convertWindowPosition(preset);

			Log.info("...moveto: "+window.getPosition());
			_setPosition(window.getPosition());
			Log.info("...resize window: "+window.getSize());
			_setSize(window.getSize());

			if(Window.NORMAL.equalsIgnoreCase(window.getStatus())){
				_restore();
			}else if(Window.MINIMIZED.equalsIgnoreCase(window.getStatus())){
				_minimize();
			}else if(Window.MAXIMIZED.equalsIgnoreCase(window.getStatus())){
				_maximize();
			}

			testRecordData.setStatusCode(StatusCodes.OK);
			log.logMessage(testRecordData.getFac(),
					genericText.convert(TXT_SUCCESS_3, altText, windowName, compName, action),
					PASSED_MESSAGE);

		} catch (Exception e) {
			Log.debug(debugInf+StringUtils.debugmsg(e));
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			componentFailureMessage(e.getMessage());
		}
	}
	/**
	 *Subclass should give its own implementation<br>
	 */
	protected void _setPosition(Point position) throws SAFSException {
		throw new SAFSException("Un-Supported yet.");
	}
	/**
	 *Subclass should give its own implementation<br>
	 */
	protected void _setSize(Dimension size) throws SAFSException {
		throw new SAFSException("Un-Supported yet.");
	}
	/**
	 * "windowName compName action successful."
	 * "[detail]"
	 * <br><em>Purpose:</em> Log a generic success message.
	 **/
	protected void componentSuccessMessage(String detail) {
		String altMsg = windowName+":"+compName+" "+action+" successful.";
		String message = passedText.convert(TXT_SUCCESS_3, altMsg,this.windowName, this.compName, this.action);
		log.logMessage(testRecordData.getFac(), message,  PASSED_MESSAGE, detail);
	}
	/**
	 * "windowName compName action successful. [comment]"
	 * Sets status to OK and issues a PASSED message.
	 * string comment is expected to already be localized.
	 * The comment can be empty, or null.
	 **/
	protected void issuePassedSuccess(String comment){
		testRecordData.setStatusCode(StatusCodes.OK);
		if (comment == null) comment = "";
		String success = GENStrings.convert(GENStrings.SUCCESS_3,
				windowName+":"+compName+" "+action+" successful.",
				windowName, compName, action);
		log.logMessage(testRecordData.getFac(), success+" "+comment, PASSED_MESSAGE);
	}
	/**
	 * "windowName compName action successful using [using]"
	 * Sets status to OK and issues a PASSED message.
	 * string comment is expected to already be localized, if necessary.
	 **/
	protected void issuePassedSuccessUsing(String using){
		testRecordData.setStatusCode(StatusCodes.OK);
		String success = GENStrings.convert(GENStrings.SUCCESS_3A,
				windowName+":"+compName+" "+action+" successful using "+ using,
				windowName, compName, action, using);
		log.logMessage(testRecordData.getFac(), success, PASSED_MESSAGE);
	}

	/**
	 * "Unable to perform action on compName in file line N."
	 * "[Detail]"
	 * <br><em>Purpose:</em> log an "Unable to perform function" FAILED_MESSAGE with detail.
	 * Expects testRecordData to already have filename, lineNumber, compName, and command.**/
	protected void componentFailureMessage(String detail) {
		String tag = LINE_FAILURE_4;
		String sfile = testRecordData.getFilename();
		String scomp = testRecordData.getCompName();
		String saction = testRecordData.getCommand();
		String sline = String.valueOf(testRecordData.getLineNumber());

		String message = failedText.convert(tag, "Unable to perform "+ saction +" on "+ scomp +" in "+ sfile +" line "+ sline,
				sfile, scomp, saction, sline);
		logFailureMessage(message, detail, "");
	}
	/**
	 * "windowname compName action processed with a negative result."
	 * "Error at line number in file: [detail]"
	 * <br><em>Purpose:</em> log a simple failure message when a test record got processed with negative result. When an
	 *            action intends to make a judgment, a negative result is gotten if the logical result of the judgment
	 *            is false; the test step will be logged as 'FAIL' although it got executed correctly. Called by some comparing actions.
	 *
	 * E.g. When doing verifyCaptureText, the result is negative if the body test captured doesn't match the bench mark file.
	 *
	 * @param error -- a error message
	 */
	protected void componentExecutedFailureMessage(String error) {
		String altMsg = windowName+":"+compName+" "+action+" processed with a negative result.";
		String message = FAILStrings.convert(FAILStrings.EXECUTED_WITH_NEGATIVERESULT, altMsg,this.windowName, this.compName, this.action);
		String details = FAILStrings.convert(FAILStrings.FAILURE_DETAIL,
				"Error at line "+testRecordData.getLineNumber()+
				" in file "+testRecordData.getFilename()+
				" : "+ error,
				String.valueOf(testRecordData.getLineNumber()),
				testRecordData.getFilename(),
				error);
		logFailureMessage(message, details, error);
	}

	/** Issue parameter count error and failure message **/
	protected void issueParameterCountFailure(){
		issueInputRecordFailure(FAILStrings.text(FAILStrings.PARAMSIZE,
				"Insufficient Parameters."));
	}

	/** Issue parameter count error and failure message
	 * @param detail detail about specific missing params or command format
	 ***/
	protected void issueParameterCountFailure(String detail){
		issueInputRecordFailure(FAILStrings.text(FAILStrings.PARAMSIZE, detail));
	}

	/** Issue parameter value error and failure message
	 * @param paramName -- the Name of the action parameter in error. **/
	protected void issueParameterValueFailure(String paramName){
		String error = FAILStrings.convert(FAILStrings.BAD_PARAM,
				"Invalid parameter value for "+ paramName +".",
				paramName);
		issueInputRecordFailure(error);
	}

	/** Issue a file error and failure message
	 * Filename does not need to be localized. **/
	protected void issueFileErrorFailure(String filename){
		issueActionFailure(FAILStrings.convert(FAILStrings.FILE_ERROR,
				"Error opening or using "+filename,
				filename));
	}

	/** Issue a generic error and failure message
	 * The cause parameter is expected to already be localized. **/
	protected void issueUnknownErrorFailure(String cause){
		issueActionFailure(FAILStrings.convert(FAILStrings.GENERIC_ERROR,
				"*** Error *** "+cause,
				cause));
	}

	/**
	 * @param subareaMapKey, String, the reference in Map file, represent the subarea;
	 *                               or the subarea itself. The subares is defined as (x, y, width, height)
	 *                               for example like (0, 0, %50, %90)
	 * @return Rectangle, The subarea rectangle deduced from the 'Component Rectangle' and 'subarea'
	 * @throws SAFSException
	 * @see {@link #lookupAppMapReference(String)}
	 * @see {@link #getComponentRectangle()}
	 */
	protected Rectangle deduceImageRect(String subareaMapKey) throws SAFSException{

		//scroll to make the element show as much as possible
		//showComponentAsMuchPossible(false);//Lei: auto-scroll may cause the difference of captured image for each time.

		Rectangle compRect = getComponentRectangle();
		if (compRect==null) throw new SAFSException("Can't get Rectangle for Componet "+compName);

		String subarea = getPossibleMapItem(subareaMapKey);

		Rectangle imageRect = null;
		if(subarea==null || subarea.equals("")){
			//get Rectangle from component itself
			imageRect = compRect;
		}else{
			//get Rectangle from compRect and SubArea parameter
			imageRect = ImageUtils.getSubAreaRectangle(compRect, subarea);
			if (imageRect == null) {
				throw new SAFSException("Can't get get sub area '"+subarea+"' from Componet Rectangle "+compRect);
			}
		}

		return imageRect;
	}

	protected String getPossibleMapItem(String mapKey){

		String mapValue = null;
		if(mapKey==null || mapKey.equals("")){
			Log.info ("Map Key not provided..., will return null as value.");
		}else{
			mapValue = lookupAppMapReference(mapKey);
			if (mapValue == null) {
				//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
				String error = FAILStrings.convert(FAILStrings.BAD_APP_MAP_ITEM,
						"Item '"+ compName+":"+mapKey +"' was not found in App Map '"+ mapname +"'",
						compName+":"+mapKey, mapname);
				IndependantLog.warn(error);
				//If can't find value from map, maybe itself is the value
				mapValue=mapKey;
			}
		}
		return  mapValue;
	}


	/**
	 * Processes a Component Function Test Record to capture a screen GUI image to a file.
	 * Currently, the available formats for the output file are JPG, BMP, TIF, GIF, PNG and PNM.
	 * Optionally, the user may utilize the SubArea parameter to only capture a portion of the Component.
	 * This routine expects to be called by subclasses only and the subclass must override
	 * the getComponentRectangle method to provide the necessary location of the
	 * desired Component.  Subclasses must also have already set the fields for mapname,
	 * windowName, compName, and action.  This is usually already done in the process() call
	 * from the subclasses.
	 * This routine utilizes Java Advanced Imaging (JAI) to output the screen image to the file.
	 * JAI must be installed at compile time and runtime.
	 **/
	protected void action_getGuiImage () throws SAFSException {
		testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
		if ( params.size( ) < 1 ) {
			this.issueParameterCountFailure("OutputFile");
			return;
		}
		String filename = iterator.next( );
		File fn = null;
		try{
			filename = ImageUtils.normalizeFileNameSuffix(filename);
			fn = deduceTestFile(filename);
		}catch(SAFSException e){
			issueParameterValueFailure("OutputFile "+e.getMessage());
			return;
		}

		//get optional SubArea parameter
		String stemp = iterator.hasNext()? iterator.next() : null;
		Rectangle imageRect = null;
		try{
			imageRect = deduceImageRect(stemp);
		}catch(SAFSException e){
			issueParameterValueFailure("SubArea "+e.getMessage());
			return;
		}

		List<String> warnings = new ArrayList<String>();
		//get optional FilteredAreas parameter
		String filteredAreas = null;
		if(iterator.hasNext()){
			filteredAreas = (String) iterator.next();
			filteredAreas = parseFilteredAreasParam(filteredAreas, warnings);
		}

		//capture component image to file
		//since our call to getSubAreaRectangle() has already confirmed that imageRect is
		//contained in compRect, we can assume that imageRect is also contained in the parent window
		//(compRect was retrieved from parent window)
		try {
			Log.debug("CF GetGuiImage imageRect resolves to: "+ imageRect);
			BufferedImage buffimg = getRectangleImage(imageRect);
			Log.debug("CF GetGuiImage captured image resolves to: "+ buffimg);
			if(filteredAreas!=null){
				buffimg = ImageUtils.filterImage(buffimg, filteredAreas, warnings);
				Log.debug("CF GetGuiImage filtered image resolves to: "+ buffimg);
			}

			if(!warnings.isEmpty()) for(String warning:warnings) log.logMessage(testRecordData.getFac(), warning, WARNING_MESSAGE);

			ImageUtils.saveImageToFile(buffimg, fn, 1.0F);
		}
		catch (java.lang.SecurityException se) {
			//error, security problems accessing output file
			this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.CANT_CREATE_FILE,
					"Can not create file '"+fn.getAbsolutePath()+"': "+
							se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
			return;
		}
		catch (java.lang.IllegalArgumentException se) {
			//error, bad parameters sent to JAI.create call
			//error, security problems accessing output file
			this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.CANT_CREATE_FILE,
					"Can not create file '"+fn.getAbsolutePath()+"': "+
							se.getClass().getSimpleName(), fn.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
			return;
		}
		catch (NoClassDefFoundError ncdfe) {
			//error, JAI not installed
			this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND,
					"Support for Java Advanced Imaging (JAI) not found!",
					"Java Advanced Imaging (JAI)"));
			return;
		}
		catch (Exception e) {
			//error, unable to capture the screen image
			this.issueErrorPerformingAction(StringUtils.debugmsg(e));
			return;
		}

		//success!  set status to ok
		this.issuePassedSuccess(GENStrings.convert(GENStrings.BE_SAVED_TO,
				"Image has been saved to '"+fn.getAbsolutePath()+"'",
				"Image", fn.getAbsolutePath()));
		testRecordData.setStatusCode(StatusCodes.OK);
	}

	/**
	 * Check if the provided parameter is a UUID= flag.<br>
	 * True if UUID=true|1|-1|on|yes. false otherwise. Unless an Exception is thrown.
	 * @param parameter to evaluate
	 * @return true/false value of the validated UUID flag, or throws the Exception.
	 * @throws Exception if the parameter is bad or does NOT start with "UUID=" (case-insensitive).
	 * @see org.safs.tools.stringutils.StringUtilities#convertBool(Object)
	 */
	public static boolean getUUIDBoolean(String parameter)throws Exception{
		String flag = parameter.trim().toUpperCase();
		flag = flag.replace(" ","");
		if(flag.length() > 5){
			if(flag.startsWith(PARAM_UUID+"=")){
				flag = flag.substring(5);
				return StringUtilities.convertBool(flag);
			}
		}
		throw new IllegalArgumentException("Provided parameter is NOT a valid UUID= parameter.");
	}

	/**
	 * Processes a Component Function Test Record to verify a screen GUI image to a file.
	 * Currently, the available formats for the output file are BMP, JPG, TIFF, GIF, PNG, and PNM.
	 * Optionally, the user may utilize the SubArea parameter to only capture a portion of the Component.
	 * This routine expects to be called by subclasses only and the subclass must override
	 * the getComponentRectangle method to provide the necessary location of the
	 * desired Component.  Subclasses must also have already set the fields for mapname,
	 * windowName, compName, and action.  This is usually already done in the process() call
	 * from the subclasses.
	 * This routine utilizes Java Advanced Imaging (JAI) to output the screen image to the file.
	 * JAI must be installed at compile time and runtime.
	 **/
	protected void action_verifyGuiImageToFile () throws SAFSException {
		String debugmsg = StringUtils.debugmsg(false);

		testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
		if ( params.size( ) < 1 ) {
			this.issueParameterCountFailure("BenchmarkFile");
			return;
		}

		String benchname = iterator.next( );

		// make the UUID suffix for Actuals and Diffs optional (default = true)
		boolean doUUID = true;
		File benchFile = null;
		File testout = null;
		String benchsuffix = null;
		boolean missingBench = false;

		try{
			benchname = ImageUtils.normalizeFileNameSuffix(benchname);
			benchsuffix = benchname.substring(benchname.lastIndexOf("."));//.jpg .png .bmp
			benchFile = deduceBenchFile(benchname);
		}catch(SAFSException e){
			issueParameterValueFailure("BenchmarkFile "+e.getMessage());
			return;
		}

		// we still need to capture the ACTUAL even when benchmark is missing!
		// this way we can use the Actual to become the benchmark.
		if(!benchFile.exists()||!benchFile.isFile()||!benchFile.canRead()){
			Log.warn(debugmsg+action +" benchmark file "+ benchFile.getAbsolutePath() +" does not appear to be valid.");
			missingBench = true;
		//	this.issueParameterValueFailure("BenchmarkFile="+fn.getAbsolutePath());
		//	return;
		}

		//get optional SubArea parameter
		String stemp = iterator.hasNext()? (String) iterator.next() : null;
		Rectangle imageRect = null;
		try{
			imageRect = deduceImageRect(stemp);
		}catch(SAFSException e){
			Log.info(debugmsg+action +" image subarea does not appear to be valid: "+ e.getMessage());
			issueParameterValueFailure("SubArea "+e.getMessage());
			return;
		}
		//get optional Tolerance parameter
		int percentBitsTolerance = 100;
		if(iterator.hasNext()){
			try{
				percentBitsTolerance = Integer.decode((String) iterator.next());
			}catch(Exception e){
				Log.info(debugmsg+action +" percentBitsTolerance does not appear to be valid: "+ e.getMessage());
			}
		}
		if(iterator.hasNext()) try{ doUUID = getUUIDBoolean(iterator.next());}catch(Exception ignore){}

		List<String> warnings = new ArrayList<String>();
		//get optional FilteredAreas parameter
		String filteredAreas = null;
		if(iterator.hasNext()){
			filteredAreas = (String) iterator.next();
			filteredAreas = parseFilteredAreasParam(filteredAreas, warnings);
		}

		String warnMsg = null;

		//capture component image to file
		//since our call to getSubAreaRectangle() has already confirmed that imageRect is
		//contained in compRect, we can assume that imageRect is also contained in the parent window
		//(compRect was retrieved from parent window)
		try {
			BufferedImage buffimg = getRectangleImage(imageRect);

			try{
				if(filteredAreas!=null) buffimg = ImageUtils.filterImage(buffimg, filteredAreas, warnings);
			}catch(SAFSException e){
				warnMsg = "Component Image Not Filtered due to "+ e.getMessage();
				Log.warn(warnMsg);
				issueErrorPerformingActionUsing(filteredAreas, warnMsg);
				return;
			}

			//Save the BufferedImage to a temporary file with the same suffix as bench image file
			File tmpFile = File.createTempFile("image",benchsuffix);
            boolean tmpFileDelete = true;

			ImageUtils.saveImageToFile(buffimg, tmpFile, 1.0F);

			//Read these two files
			String bench = null;
			String test  = null;
			try{ bench = StringUtils.readBinaryFile(benchFile.getAbsolutePath()).toString();}
			catch(IOException io){
				Log.warn(debugmsg+action +" failed to load benchmark file "+benchFile.getAbsolutePath());
				bench = null;
				//don't throw Exception, we need to save the current GUI snapshot to test file
			}
			try{ test  = StringUtils.readBinaryFile(tmpFile.getAbsolutePath()).toString();}
			catch(IOException io){
				Log.error(debugmsg+action +" failed to load gui snapshot file "+tmpFile.getAbsolutePath());
				throw new SAFSException(io.getMessage());
			}

			boolean verified = false;
			BufferedImage diffimg = null;
			if(bench != null && test != null){
				//Compare two binary-strings
				Log.info(debugmsg+"comparing the binary content of 2 images ...");
				Log.info(debugmsg+"benchcontents.length: "+bench.length());
				Log.info(debugmsg+"testcontents.length: "+test.length());
				verified = bench.equals(test);
				if(!verified){
					//Compare two buffered-images
					Log.info(debugmsg+"comparing each bits of 2 images ...");
					BufferedImage benchimg = ImageUtils.getStoredImage(benchFile.getAbsolutePath());
					try{
						if(filteredAreas!=null) benchimg = ImageUtils.filterImage(benchimg, filteredAreas, warnings);
					}catch(SAFSException e){
						warnMsg = "Benchmark Image Not Filtered due to "+ e.getMessage();
						Log.warn(warnMsg);
						issueErrorPerformingActionUsing(filteredAreas, warnMsg);
						return;
					}

					verified = ImageUtils.compareImage(buffimg, benchimg, percentBitsTolerance);
					if(!verified){
						try{ diffimg = ImageUtils.createDiffImage(buffimg, benchimg);}
						catch(Exception x){
							Log.info(debugmsg+action +" failed to create Diff Image due to: "+x.getClass().getName()+", "+ x.getMessage());
						}
					}
				}
			}
			// Always write the Actual for manual verification, if needed.
			if(doUUID){
				testout = deduceTestFile(FileUtilities.deduceMatchingUUIDFilename(benchname));
			}else{
				testout = deduceTestFile(benchname);
			}
			FileUtilities.copyFileToFile(tmpFile, testout);

			if(!warnings.isEmpty()) for(String warning:warnings) log.logMessage(testRecordData.getFac(), warning, WARNING_MESSAGE);

			if (verified) {
				//success!  set status to ok
				testRecordData.setStatusCode(StatusCodes.OK);
				issuePassedSuccess(GENStrings.convert(GENStrings.CONTENT_MATCHES_KEY,
						"the content of '"+ testout.getAbsolutePath() +"' matches the content of "+benchFile.getAbsolutePath(),
						testout.getAbsolutePath(),benchFile.getAbsolutePath()));
			} else {
				try{
					StringBuffer message = new StringBuffer();
					//If the verification failure was caused by "bench file missing or bench reading error"
					//we cannot make a diff image,
					if(missingBench || bench==null){
						if(missingBench){
							message.append(GENStrings.convert(GENStrings.NOT_EXIST,
									benchFile.getAbsolutePath() +" does not exist.",
									benchFile.getAbsolutePath()));
						}else{
							message.append(FAILStrings.convert(FAILKEYS.FILE_READ_ERROR,
									"Error reading from file '"+benchFile.getAbsolutePath()+"'",
									benchFile.getAbsolutePath()));
						}
						message.append("; "+GENStrings.convert(GENStrings.BE_SAVED_TO,
								compName+" Image has been saved to '"+testout.getAbsolutePath()+"'",
								compName+" Image", testout.getAbsolutePath()));
					}else{
						File diffout = null;
						if(diffimg != null){
							if(doUUID){
								diffout = deduceDiffFile(FileUtilities.deduceMatchingUUIDFilename(benchname));
							}else{
								diffout = deduceDiffFile(benchname);
							}
							ImageUtils.saveImageToFile(diffimg, diffout);
						}
						message.append(GENStrings.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
								"the content of '"+ testout.getAbsolutePath() +"' does not match the content of "+benchFile.getAbsolutePath(),
								testout.getAbsolutePath(),benchFile.getAbsolutePath()));
						//see_difference_file		: Please see difference in file '%1%'.
						if(diffimg != null){
							message.append(" "+ GENStrings.convert(GENStrings.SEE_DIFFERENCE_FILE,
									"Please see difference in file '"+ diffout.getAbsolutePath() +"'.",
									diffout.getAbsolutePath()));
						}
					}

					issueErrorPerformingActionOnX(compName, message.toString());
				}catch(SAFSException x){
					tmpFileDelete = false;
					issueErrorPerformingActionOnX(compName, GENStrings.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
							"the content of '"+ tmpFile.getAbsolutePath() +"' does not match the content of "+benchFile.getAbsolutePath(),
							tmpFile.getAbsolutePath(),benchFile.getAbsolutePath()));
				}
			}
			if(tmpFileDelete) tmpFile.delete();
			return;
		}
		catch (java.lang.SecurityException se) {
			//error, security problems accessing output file
			issueErrorPerformingActionOnX(compName,FAILStrings.convert(FAILStrings.FILE_ERROR,
					"Error opening or reading or writing file '"+benchFile.getAbsolutePath()+"': "+
							se.getClass().getSimpleName(), benchFile.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
		}
		catch (java.lang.IllegalArgumentException se) {
			//error, bad parameters sent to JAI.create call
			//error, security problems accessing output file
			this.issueErrorPerformingActionOnX(compName, FAILStrings.convert(FAILStrings.FILE_ERROR,
					"Error opening or reading or writing file '"+benchFile.getAbsolutePath()+"': "+
							se.getClass().getSimpleName(), benchFile.getAbsolutePath()+": "+ se.getClass().getSimpleName()));
		}
		catch (NoClassDefFoundError ncdfe) {
			//error, JAI not installed
			this.issueErrorPerformingActionOnX(compName,FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND,
					"Support for Java Advanced Imaging (JAI) not found!",
					"Java Advanced Imaging (JAI)"));
		}
		catch (Exception e) {
			//error, unable to capture the screen image
			this.issueErrorPerformingActionOnX(compName, StringUtils.debugmsg(e));
		}
	}

	/**
	 * Parse FilteredAreas Parameter.<br>
	 * @param filteredAreasParam String, the "filtered areas parameter"
	 * @param warnings List<String>, if the "filtered areas parameter" is not valid, this list will contain a warning message.
	 * @return String, the parsed "filtered areas", or null if not valid.
	 */
	private String parseFilteredAreasParam(String filteredAreasParam, List<String> warnings){
		String debugmsg = StringUtils.debugmsg(false);
		String filteredAreas = filteredAreasParam;
		boolean isFilteredAreas = false;

		//Maybe the parameter is just a key in the map file, mapkey="Filter=0,0,5,5 40,40,30%,50%"
		filteredAreas = getPossibleMapItem(filteredAreas);

		String[] tokens = StringUtils.getTokenArray(filteredAreas, "=");
		if(tokens.length>1){
			boolean hasOnlyFilterPrefix = true;
			//Remove possible extra prefix "Filter"
			for(int i=0;i<tokens.length-1;i++){
				if(!tokens[i].trim().equalsIgnoreCase(PARAM_FILTER)){
					hasOnlyFilterPrefix =false;
					break;
				}
			}
			if(hasOnlyFilterPrefix){
				filteredAreas = tokens[tokens.length-1].trim();

				//Maybe the parameter is just a key in the map file, Filter=mapkeyOfSubAreas
				filteredAreas = getPossibleMapItem(filteredAreas);
				if(filteredAreas!=null && !filteredAreas.trim().isEmpty()){
					//Remove the possible extra "Filter="
					int index = filteredAreas.indexOf("=");
					if(index>=PARAM_FILTER.length() && (index+1<filteredAreas.length()))
						filteredAreas = filteredAreas.substring(index+1);
				}
				if(filteredAreas!=null && !filteredAreas.trim().isEmpty()) isFilteredAreas=true;

				Log.debug(debugmsg+"parameter filteredAreas='"+filteredAreas+"'");
			}
		}

		if(!isFilteredAreas){
			IndependantLog.warn(debugmsg+"'"+filteredAreas+"' is not a valid FilteredAreas parameter.");
			warnings.add("'"+filteredAreas+"' is not a valid FilteredAreas parameter, try add prefix '"+PARAM_FILTER+"='");
			return null;
		}else{
			return filteredAreas;
		}
	}

	/**
	 * Set the varname.x, varname.y, varname.w, and varname.h variables to the
	 * values retrieved from the rectangle.
	 * @param rect Rectangle,
	 * @param varname String,
	 * @return
	 */
	protected boolean setRectVars(Rectangle rect, String varname){
		try{
			setVariable(varname, "[x y w h]="+"["+rect.x+" "+rect.y+" "+rect.width+" "+rect.height+"]");
			setVariable(varname+".x", String.valueOf(rect.x));
			setVariable(varname+".y", String.valueOf(rect.y));
			setVariable(varname+".w", String.valueOf(rect.width));
			setVariable(varname+".h", String.valueOf(rect.height));
			return true;
		}catch(Exception x){
			return false;
		}
	}

	public void setIterator(Iterator<String> iterator){
		this.iterator = iterator;
	}

	/**
	 * Process generic actions on a component.
	 */
	@SuppressWarnings("unchecked")
	protected void componentProcess(){
		String debugmsg = StringUtils.debugmsg(false);

		try {
			if(action==null) updateFromTestRecordData();

			if(action==null){
				componentFailureMessage(FAILStrings.text(FAILKEYS.ACTION_NOT_VALID)+":"+testRecordData.getInputRecord());
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				return;
			}

			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);

			//Log.debug(debugmsg+" processing "+testRecordData);
			if (params != null){
				iterator = params.iterator();
				Log.info(debugmsg+" processing "+action+"; win: "+ windowName +"; comp: "+ compName+"; with params: "+params);
			}else{
				Log.info(debugmsg+" processing "+action+"; win: "+ windowName +"; comp: "+ compName+" without parameters.");
			}

			altText = windowName+":"+compName+" "+action+" Successful ";

			if ( COMPONENTCLICK.equalsIgnoreCase(action)
					|| CLICK.equalsIgnoreCase(action)
					|| CTRLCLICK.equalsIgnoreCase(action)
					|| CTRLRIGHTCLICK.equalsIgnoreCase(action)
					|| DOUBLECLICK.equalsIgnoreCase(action)
					|| RIGHTCLICK.equalsIgnoreCase(action)
					|| SHIFTCLICK.equalsIgnoreCase(action)){
				componentClick();

			} else if(GETGUIIMAGE.equalsIgnoreCase(action)){
				action_getGuiImage();

			}else if (WAITFORGUI.equalsIgnoreCase(action)) {
				waitForGUI( );
			} else if(GenericMasterFunctions.EXECUTESCRIPT_KEYWORD.equalsIgnoreCase(action)){
				executeScript();
			} else if(GenericMasterFunctions.INPUTKEYS_KEYWORD.equalsIgnoreCase(action)
					|| GenericMasterFunctions.INPUTCHARACTERS_KEYWORD.equalsIgnoreCase(action)){
				inputKeystrokes();
			} else if(WindowFunctions.MAXIMIZE_KEYWORD.equalsIgnoreCase(action)){
				maximize();
			} else if(WindowFunctions.MINIMIZE_KEYWORD.equalsIgnoreCase(action)){
				minimize();
			} else if(WindowFunctions.RESTORE_KEYWORD.equalsIgnoreCase(action)){
				restore();
			} else if(WindowFunctions.CLOSEWINDOW_KEYWORD.equalsIgnoreCase(action)){
				close();
			} else if(WindowFunctions.SETPOSITION_KEYWORD.equalsIgnoreCase(action)){
				setPosition();
			} else if(GenericMasterFunctions.VERIFYCOMPUTEDSTYLE_KEYWORD.equalsIgnoreCase(action)){
				action_ComputedStyle(true);
			} else if(GenericMasterFunctions.GETCOMPUTEDSTYLE_KEYWORD.equalsIgnoreCase(action)){
				action_ComputedStyle(false);
			} else if(GenericMasterFunctions.VERIFYPROPERTY_KEYWORD.equalsIgnoreCase(action)){
				verifyProperty();
			} else if(GenericMasterFunctions.CLEARCACHE_KEYWORD.equalsIgnoreCase(action)){
				clearCache();
			}else if(GenericMasterFunctions.ASSIGNPROPERTYVARIABLE_KEYWORD.equalsIgnoreCase(action)){
				assignPropertyVariable();
			} else if (action.equalsIgnoreCase(CLOSEWINDOW)) {
				closeWindow();
			} else if (action.equalsIgnoreCase(VERIFYPROPERTYCONTAINS)) {
				verifyPropertyContains();
			} else if (action.equalsIgnoreCase(VERIFYVALUECONTAINS)) {
				verifyValueContains();
			} else if (action.equalsIgnoreCase(VERIFYVALUES) ||
					action.equalsIgnoreCase(VERIFYVALUEEQUALS) ||
					action.equalsIgnoreCase(VERIFYVALUESIGNORECASE)) {
				verifyValues();
			} else if (action.equalsIgnoreCase(ISPROPERTYEXIST)) {
				isPropertyExist();
			} else if (GenericMasterFunctions.HOVERSCREENLOCATION_KEYWORD.equalsIgnoreCase(action)) {
				hoverScreenLocation();
			} else if (action.equalsIgnoreCase(CLEARAPPMAPCACHE)) {
				clearAppMapCache();
			} else if (action.equalsIgnoreCase(VERIFYOBJECTDATATOFILE)) {
				verifyObjectDataToFile();
			} else if (action.equalsIgnoreCase(CAPTUREOBJECTDATATOFILE)) {
				captureObjectDataToFile();
			} else if (action.equalsIgnoreCase(CAPTUREPROPERTIESTOFILE)) {
				capturePropertiesToFile();
			} else if (action.equalsIgnoreCase(VERIFYTEXTFILETOFILE) ||
					action.equalsIgnoreCase(VERIFYFILETOFILE)) {
				verifyFileToFile(true);
			} else if (action.equalsIgnoreCase(GUIDOESEXIST)) {
				guiDoesExist(true);
			} else if (action.equalsIgnoreCase(GUIDOESNOTEXIST)) {
				guiDoesExist(false);
			} else if (action.equalsIgnoreCase(GenericMasterFunctions.VERIFYPROPERTIESTOFILE_KEYWORD) ||
					  (action.equalsIgnoreCase(GenericMasterFunctions.VERIFYPROPERTIESSUBSETTOFILE_KEYWORD))){
				verifyPropertiesToFile();
			} else if (action.equalsIgnoreCase(VERIFYPROPERTYTOFILE)){
				verifyPropertyToFile(false);
			} else if (action.equalsIgnoreCase(VERIFYARRAYPROPERTYTOFILE)) {
				verifyPropertyToFile(true);
			} else if (action.equalsIgnoreCase(CAPTUREPROPERTYTOFILE)) {
				capturePropertyToFile();
			} else if (action.equalsIgnoreCase(VERIFYBINARYFILETOFILE)) {
				verifyFileToFile(false);
			} else if (action.equalsIgnoreCase(VERIFYCLIPBOARDTOFILE)) {
				verifyClipboardToFile();
			} else if (action.equalsIgnoreCase(HOVERMOUSE)) {
				hoverMouse();
			} else if (action.equalsIgnoreCase(VERIFYGUIIMAGETOFILE)) {
				action_verifyGuiImageToFile();
			} else if (action.equalsIgnoreCase(SETPROPERTYVALUE)){
				setPropertyValue();
			} else if(action.equalsIgnoreCase(VSCROLLTO)||
					action.equalsIgnoreCase(HSCROLLTO)){
				performScorll();
			} else if (action.equalsIgnoreCase(SELECTMENUITEM)) {
				selectMenuItem(false);
			} else if (action.equalsIgnoreCase(SELECTMENUITEMCONTAINS)) {
				selectMenuItem(true);
			} else if (action.equalsIgnoreCase(VERIFYMENUITEM)) {
				verifyMenuItem(false);
			} else if (action.equalsIgnoreCase(VERIFYMENUITEMCONTAINS)) {
				verifyMenuItem(true);
			}else if (action.equalsIgnoreCase(LEFTDRAG) ||
					action.equalsIgnoreCase(RIGHTDRAG) ||
					action.equalsIgnoreCase(SHIFTLEFTDRAG) ||
					action.equalsIgnoreCase(CTRLSHIFTLEFTDRAG ) ||
					action.equalsIgnoreCase(CTRLLEFTDRAG) ||
					action.equalsIgnoreCase(ALTLEFTDRAG) ||
					action.equalsIgnoreCase(CTRLALTLEFTDRAG )) {
				performDrag();
			} else if (action.equalsIgnoreCase(GenericObjectFunctions.DRAGTO_KEYWORD)) {
				dragTo();
			} else if (action.equalsIgnoreCase(SENDEVENT)) {
				sendEvent();
			} else if (action.equalsIgnoreCase(GETTEXTFROMGUI) ||
					action.equalsIgnoreCase(SAVETEXTFROMGUI)) {
				action_GetSaveTextFromGUI();
			} else if (action.equalsIgnoreCase(LOCATESCREENIMAGE)) {
				locateScreenImage();
			} else if (action.equalsIgnoreCase(GenericMasterFunctions.SHOWONPAGE_KEYWORD)) {
				action_showOnPage();
			}

		}catch( SAFSException se ) {
			if(SAFSException.CODE_ACTION_NOT_SUPPORTED.equals(se.getCode())){
				//Let the other engine to process
				Log.info(debugmsg+" '"+action+"' is not supported here.");
				testRecordData.setStatusCode( StatusCodes.SCRIPT_NOT_EXECUTED );
			}else{
				testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
				String message = "Met "+StringUtils.debugmsg(se);
				String detail = FAILStrings.convert(FAILStrings.STANDARD_ERROR,
						action +" failure in table "+ testRecordData.getFilename() + " at Line " + testRecordData.getLineNumber(),
						action, testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));

				log.logMessage(testRecordData.getFac(), message, detail, FAILED_MESSAGE);
			}

		}catch (Throwable e){
			String eName = e.getClass().getName();
			String eMessage = " window: "+ windowName +"; comp: "+compName+ "; msg: "+e.getMessage();

			if(SAFSException.NAME_SeleniumException.equals(eName)
					&& e.getMessage().startsWith("ERROR: There was an unexpected Alert!")){
				log.logMessage( testRecordData.getFac( ), "Met "+StringUtils.debugmsg(e), GENERIC_MESSAGE );
				//Should we do something to close the Alert??
				componentProcess();

			}else{
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				String message = null;
				String detail = FAILStrings.convert(FAILStrings.STANDARD_ERROR,
						action +" failure in table "+ testRecordData.getFilename() + " at Line " + testRecordData.getLineNumber(),
						action, testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));

				if(SAFSException.NAME_FT_ObjectNotFoundException.equals(eName)){
					message = "ObjectNotFound: the window or component are not visible or do not exist;"+eMessage;

				}else if(SAFSException.NAME_FT_PropertyNotFoundException.equals(eName)){
					message = "PropertyNotFound: the window or component does not have the property;"+eMessage;

				}else if(SAFSException.NAME_FT_TargetGoneException.equals(eName)){
					message = "TargetGone: the window or component are unexpectedly gone;"+eMessage;

				}else if(SAFSException.NAME_FT_UnsupportedActionException.equals(eName)){
					message = "UnsupportedAction: possibly the component is not visible, or possibly this action is really not supported;"+eMessage;

				}else if(SAFSException.NAME_FT_WindowActivateFailedException.equals(eName)){
					message = GENStrings.convert(GENStrings.ACTIVATE_WARN,
							compName +" activation warning.  "+ compName +" may be disabled or obstructed.", compName);

				}else{
					message = "Met "+StringUtils.debugmsg(e);

				}

				log.logMessage(testRecordData.getFac(), message, detail, FAILED_MESSAGE);
			}

		}
	}

	protected void componentClick() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void executeScript() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void inputKeystrokes() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void action_ComputedStyle(boolean verification) throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	protected void closeWindow() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	protected void verifyPropertyContains() throws SAFSException {
		String debugmsg = StringUtils.debugmsg(false);

		testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
		if ( params.size()<2 ) {
			paramsFailedMsg( windowName, compName );
		} else {
			String prop = iterator.next();
			String val = iterator.next();
			boolean ignorecase = false;
			try { ignorecase = !StringUtils.isCaseSensitive(iterator.next()); } catch( Exception x ) {;}

			Log.info(debugmsg+" prop : " + prop + " val: " + val );

			String rval = getProperty(prop);
			Log.info( "..... real value is: " + rval );

			// it is possible the property name is not valid for this engine
			// it may be a property valid in a different engine though
			if ( rval==null || rval.equals("null")) {
				testRecordData.setStatusCode( StatusCodes.SCRIPT_NOT_EXECUTED );
				return;
			}

			String testval = ignorecase ? val.toUpperCase() : val;
			String testrval = ignorecase ? rval.toUpperCase() : rval;

			if ( testrval.indexOf(testval) < 0 ) {
				// failed
				testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
				String detail = genericText.convert("bench_not_contains",
						compName+":"+prop +" did not contain expected substring '"+ val +"'",
						compName+":"+prop, val);
				detail += " " + genericText.convert("actual_value", "ActualValue='"+ rval +"'", rval);
				componentExecutedFailureMessage(detail);
			} else {
				// set status to ok
				testRecordData.setStatusCode( StatusCodes.OK );
				String p4 = " Property '"+prop;
				String p5 = "' contains substring '"+val+"' as expected.";
				altText += p4+p5;
				String detail = passedText.convert(TXT_SUCCESS_5, altText, windowName, compName, action, p4, p5);
				log.logMessage(testRecordData.getFac(), detail, PASSED_MESSAGE);
			}
		}
	}

	protected void verifyValueContains() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void verifyValues() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void hoverScreenLocation() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void isPropertyExist () throws SAFSException {
		String debugmsg = StringUtils.debugmsg(false);

		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		if (params.size() < 2) {
			paramsFailedMsg(windowName, compName);
		} else {
			String prop = (String) iterator.next();
			String propExistVar = (String) iterator.next();
			String  exist = "false";

			Log.info(debugmsg+" ready to assign the existence of prop : "+prop+" to propExistVar: "+propExistVar);

			try{
				Object rval = getProperty(prop);
				Log.info(debugmsg+"..... real value is: "+rval);
				if (rval == null) {
					Log.info(debugmsg+" property '"+prop+"' does NOT exist.");
				}else {
					exist = "true";
				}
			}catch(SAFSException e){
				if(!SAFSException.CODE_PropertyNotFoundException.equals(e.getCode())) throw e;
				Log.info(debugmsg+" property '"+prop+"' NOT found.");
			}

			if (!setVariable(propExistVar, exist)) {
				Log.debug(debugmsg+" Fail to set variable " + propExistVar + " to "+exist);
				String message = "Property '"+prop+"' existence is "+exist;
				message = "But "+FAILStrings.convert(FAILStrings.COULD_NOT_SET,
								 "Could not set '"+exist+"' to '"+propExistVar+"'", exist, propExistVar);
				issueErrorPerformingActionOnX(compName, message);
			}else{
				testRecordData.setStatusCode(StatusCodes.OK);
				log.logMessage(testRecordData.getFac(),
						passedText.convert(TXT_SUCCESS_5,
								altText+", Assigned variable \""+ propExistVar +"\" value \""+ exist +"\".",
								windowName, compName, action,
								" Assigned variable '"+propExistVar+"'",
								"value '"+exist+"'."),
								PASSED_MESSAGE);
			}
		}
	}
	protected void clearAppMapCache() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	protected String normalizeFileEncoding(String encoding){
		//If user put a blank string as encoding,
		//we should consider that user does NOT provide a encoding, reset encoding to null.
		if(encoding==null || encoding.trim().isEmpty()) return null;

		return encoding;
	}

	protected void verifyObjectDataToFile () throws SAFSException {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
			return;
		}
		String filename =  iterator.next();
		String benchFile = null;
		String actfilename = null;
		//fileEncoding is used to read bench file, and store the actual data to a file.
		String fileEncoding = null;
		if(iterator.hasNext()) fileEncoding = normalizeFileEncoding(iterator.next());
		Log.info("...filename: "+filename+" ; encoding:"+fileEncoding);
		final String OBJECT_DATA = "OBJECT_DATA";
		try{
			boolean multiline = false;
			boolean isMissing = false;
			String missingMessage = null;
			Collection<?> benchContents = null;
			benchFile = getAbsolutFileName(filename, STAFHelper.SAFS_VAR_BENCHDIRECTORY);
			Collection<String> actualContents = captureObjectData();
			Map<String,String> properties = new HashMap<String,String>();
			Map<String,String>benchmark = new HashMap<String,String>();
			StringBuffer sb = new StringBuffer();
			if(!actualContents.isEmpty()){
				Collection<String> normcontents = new ArrayList<String>();
				for(String line: actualContents){
					line = StringUtils.normalizeLineBreaks(line);
					normcontents.add(line);
					if(line.contains(StringUtils.NEW_LINE)){
						multiline = true;
					}
				}
				if(normcontents.size() > 1) multiline = true;
				if(multiline){
					for(String line: normcontents) sb.append(line + StringUtils.NEW_LINE);
					properties.put(OBJECT_DATA, sb.toString());
				}else{
					properties.put(OBJECT_DATA, normcontents.iterator().next());
				}
				if(multiline){
					try{
						benchmark = StringUtils.readEncodingMap(benchFile, fileEncoding);
					}catch(IOException io){
						Log.info("VerifyObjectDataToFile benchmark file '"+ benchFile +"' IOException: "+io.getMessage());
						isMissing = true;
						missingMessage = io.getMessage();
					}
					benchContents = benchmark.values();
				}else{
					try{
						benchContents = StringUtils.readEncodingfile(benchFile, fileEncoding);
						if(!benchContents.isEmpty()) benchmark.put(OBJECT_DATA, benchContents.iterator().next().toString());
					}catch(IOException io){
						Log.info("VerifyObjectDataToFile benchmark file '"+ benchFile +"' IOException: "+io.getMessage());
						benchContents = benchmark.values();// nothing. Empty Collection.
						isMissing = true;
						missingMessage = io.getMessage();
					}
				}
				if(StringUtils.isMatchingMaps(properties, benchmark)){
					issuePassedSuccessUsing(benchFile);
				}else{
					actfilename = FileUtilities.deduceMatchingUUIDFilename(benchFile);
					File testout = deduceTestFile(actfilename);
					Log.info("VerifyObjectDataToFile property file: "+ testout.getAbsolutePath() +", encoding: "+fileEncoding);
					if(multiline){
						StringUtils.writeEncodingProperties(testout.getAbsolutePath(), properties, fileEncoding);
					}else{
						StringUtils.writeEncodingfile(testout.getAbsolutePath(), normcontents, fileEncoding);
					}
					if(isMissing){
						missingMessage = FAILStrings.convert(FAILKEYS.FILE_READ_ERROR,
								  "Error reading from file '"+ benchFile+"'",
								  benchFile) +
								  ": "+ missingMessage +". " +
								  GENStrings.convert(GENKEYS.BE_SAVED_TO,
										  "'"+ OBJECT_DATA +"' has been save to '"+ testout.getAbsolutePath()+"'",
										  OBJECT_DATA,testout.getAbsolutePath());
						issueErrorPerformingActionOnX(compName, missingMessage);
					}else{

					issueErrorPerformingActionOnX(compName, GENStrings.convert(GENKEYS.CONTENT_NOT_MATCHES_KEY,
				               "the content of '"+ testout.getAbsolutePath() +"' does not match the content of '"+ benchFile+"'",
				               testout.getAbsolutePath(),benchFile));
					}
				}
			}else{
				issueErrorPerformingActionOnX(compName, FAILStrings.convert(FAILKEYS.COULD_NOT_GET,
						  "Could not get '"+ OBJECT_DATA +"'.",
						  OBJECT_DATA));
			}
		}catch (IllegalArgumentException ioe) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(),
					testRecordData.getCommand()+
					" failure, error capturing object data: "+filename+", msg: "+ioe.getMessage(),
					FAILED_MESSAGE);
		} catch (IOException ioe) {
			issueErrorPerformingActionOnX(compName, FAILStrings.convert(FAILKEYS.FILE_ERROR,
					  "Error opening, reading, or writing file '"+ ioe.getMessage()+"'",
					  ioe.getMessage()));
		}
	}

	protected void captureObjectDataToFile() throws SAFSException {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
		} else {
			String debugmsg = StringUtils.debugmsg(false);
			String filename = iterator.next();
			String encoding = iterator.hasNext()? iterator.next(): null;

			filename = getAbsolutFileName(filename, STAFHelper.SAFS_VAR_TESTDIRECTORY);
			IndependantLog.info(debugmsg+" filename='"+filename+"'; encoding='"+encoding+"'.");
			Map<String,String> props = new HashMap<String,String>();

			boolean multiline = false;
			try {
				Collection<String> contents = captureObjectData();
				if(contents==null || contents.isEmpty()){
					String type = testRecordData.getCompType();
					throw new SAFSException("Fail to get object data"+(type==null?"":" for '"+type+"'"),SAFSException.CODE_CONTENT_ISNULL);
				}
				Collection<String> normcontents = new ArrayList<String>();
				for(String line: contents){
					line = StringUtils.normalizeLineBreaks(line);
					normcontents.add(line);
					if(line.contains(StringUtils.NEW_LINE)){
						multiline = true;
					}
				}
				if(normcontents.size() > 1) multiline = true;
				if(multiline){
					StringBuffer sb = new StringBuffer();
					for(String line: normcontents) sb.append(line + StringUtils.NEW_LINE);
					props.put(OBJECT_DATA, sb.toString());
					StringUtils.writeEncodingProperties(filename, props, encoding);
				}else{
					StringUtils.writeEncodingfile(filename, normcontents, encoding);
				}
				// set status to ok
				issuePassedSuccessUsing(filename);

			}catch(IOException ioe) {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				log.logMessage(testRecordData.getFac(),
						testRecordData.getCommand()+
						" failure, file can't be written: "+filename+", msg: "+ioe.getMessage(),
						FAILED_MESSAGE);
			}catch(SAFSException spe){
				if(SAFSException.CODE_CONTENT_ISNULL.equals(spe.getCode())){
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					log.logMessage(testRecordData.getFac(),
							testRecordData.getCommand()+ " failure, " + spe.getMessage(),
							FAILED_MESSAGE);
				}else{
					throw spe;
				}
			}
		}
	}

	/**
	 * @param isTextFile, boolean, if true, then text files, else binary files
	 */
	protected void verifyFileToFile(boolean isTextFile) throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	/**
	 * @param does boolean, true if hope the GUI exist; false hope not exist.
	 * @throws SAFSException
	 */
	protected void guiDoesExist(boolean does) throws SAFSException {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		boolean verified = false;

		try{
			verified = does? exist():!exist();
		}catch (SAFSException se){
			Log.warn("Met Exception "+StringUtils.debugmsg(se));
			if(SAFSException.CODE_ACTION_NOT_SUPPORTED.equals(se.getCode())) throw se;

			//GUI does not exist or is not visible
			verified = does? false:true;
		}

		if(verified){
			testRecordData.setStatusCode(StatusCodes.OK);
			String message = genericText.convert(TXT_SUCCESS_3, altText,windowName, compName, action);
			log.logMessage(testRecordData.getFac(), message, GENERIC_MESSAGE);

		}else{
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			String compStr = windowName +":"+ compName;
			String message = null;
			if(does) message = genericText.convert(GENKEYS.NOT_EXIST, "'"+compStr+"' does not exist", compStr);
			else message = genericText.convert(GENKEYS.EXISTS, "'"+compStr+"' exists", compStr);
			componentFailureMessage(message);
		}
	}

	/**
	 * <br><em>Purpose:</em> verifyArrayPropertyToFile/verifyPropertyToFile/verifyPropertiesToFile
	 * @param array boolean, if true, then array property, else scalar
	 **/
	@SuppressWarnings("unchecked")
	protected void verifyPropertyToFile (boolean array) throws SAFSException {
		String debugmsg = StringUtils.debugmsg(false);

		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		if (params.size() < 2) {
			paramsFailedMsg(windowName, compName);
		} else {
			String prop = iterator.next();
			String benchFile =  iterator.next();
			String testFile = null;
			String fileEncoding = null;
			boolean multiline = false;
			Collection<String> propertyContents = null;
			Collection<String> benchContents = null;
			Map<String,String> benchmark = null;
			Map<String,String> properties = null;
			boolean isMissing = false;
			String missingMessage = null;
			if(iterator.hasNext()) fileEncoding = normalizeFileEncoding(iterator.next());
			//TODO FilterMode
			//TODO FilterOptions

			benchFile = getAbsolutFileName(benchFile, STAFHelper.SAFS_VAR_BENCHDIRECTORY);
			Log.info(debugmsg+" property: "+prop+" filename: "+benchFile+" encoding: "+fileEncoding);
			String rawline = null;
			String normline = null;
			try {
				properties = new HashMap<String,String>();
				benchmark = new HashMap<String,String>();
				propertyContents = getPropertyCollection(prop);
				if(!propertyContents.isEmpty()){
					rawline = propertyContents.iterator().next();
					normline = StringUtils.normalizeLineBreaks(rawline);
					multiline = normline.contains(StringUtils.NEW_LINE);
					properties.put(prop, normline);
					// should be just one property value, not name=value.
					if(multiline){
						try{
							benchmark = StringUtils.readEncodingMap(benchFile, fileEncoding);
						}catch(IOException io){
							Log.info(debugmsg+" benchmark file '"+ benchFile +"' IOException: "+io.getMessage());
							isMissing = true;
							missingMessage = io.getMessage();
						}
						benchContents = benchmark.values();
					}else{
						try{
							benchContents = StringUtils.readEncodingfile(benchFile, fileEncoding);
							if(!benchContents.isEmpty()) benchmark.put(prop, benchContents.iterator().next());
						}catch(IOException io){
							Log.info(debugmsg+" benchmark file '"+ benchFile +"' IOException: "+io.getMessage());
							benchContents = benchmark.values();// nothing. Empty Collection.
							isMissing = true;
							missingMessage = io.getMessage();
						}
					}
					if(StringUtils.isMatchingMaps(properties, benchmark)){
						issuePassedSuccessUsing(benchFile);
					}else{
						testFile = FileUtilities.deduceMatchingUUIDFilename(benchFile);
						File testout = deduceTestFile(testFile);
						Log.info(debugmsg+" property file: "+ testout.getAbsolutePath() +", encoding: "+fileEncoding);
						if(multiline){
							StringUtils.writeEncodingProperties(testout.getAbsolutePath(), properties, fileEncoding);
						}else{
							StringUtils.writeEncodingfile(testout.getAbsolutePath(), propertyContents, fileEncoding);
						}
						if(isMissing){
							missingMessage = FAILStrings.convert(FAILKEYS.FILE_READ_ERROR,
									  "Error reading from file '"+ benchFile+"'",
									  benchFile) +
									  ": "+ missingMessage +". " +
									  GENStrings.convert(GENKEYS.BE_SAVED_TO,
											  "'"+ prop +"' has been save to '"+ testout.getAbsolutePath()+"'",
											  prop,testout.getAbsolutePath());
							issueErrorPerformingActionOnX(compName, missingMessage);
						}else{

						issueErrorPerformingActionOnX(compName, GENStrings.convert(GENKEYS.CONTENT_NOT_MATCHES_KEY,
					               "the content of '"+ testout.getAbsolutePath() +"' does not match the content of '"+ benchFile+"'",
					               testout.getAbsolutePath(),benchFile));
						}
					}
				}else{
					issueErrorPerformingActionOnX(compName, FAILStrings.convert(FAILKEYS.COULD_NOT_GET,
							  "Could not get '"+ prop+"'.",
							  prop));
				}
			} catch (IOException ioe) {
				issueErrorPerformingActionOnX(compName, FAILStrings.convert(FAILKEYS.FILE_ERROR,
						  "Error opening, reading, or writing file '"+ ioe.getMessage()+"'",
						  ioe.getMessage()));
			}
		}
	}

	/**
	 * <br><em>Purpose:</em> verifyPropertiesToFile
	 * @param array boolean, if true, then array property, else scalar
	 **/
	@SuppressWarnings("unchecked")
	protected void verifyPropertiesToFile () throws SAFSException {
		String debugmsg = StringUtils.debugmsg(false);

		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
		} else {
			String benchFile =  iterator.next();
			String testFile = null;
			String fileEncoding = null;
			boolean multiline = false;
			boolean isMissing = false;
			String missingMessage = null;
			if(iterator.hasNext()) fileEncoding = normalizeFileEncoding(iterator.next());
			//FUTURE FilterMode
			//FUTURE FilterOptions

			benchFile = getAbsolutFileName(benchFile, STAFHelper.SAFS_VAR_BENCHDIRECTORY);
			Log.info(debugmsg+" filename: "+benchFile+" encoding: "+fileEncoding);

			try {
				Collection<String> propertyContents = getPropertyCollection((List<String>)null);
				Collection<String> benchContents = null;
				Map<String,String> properties = null;
				Map<String,String> benchmark = new HashMap<String,String>();
				if(!propertyContents.isEmpty()){
					for(String line:propertyContents){
						if(line.contains("\n")||line.contains("\r")){
							multiline = true;
							break;
						}
					}
					properties = StringUtils.convertCollectionToMap(propertyContents, testRecordData.getSeparator());
					if(multiline){
						try{ benchmark = StringUtils.readEncodingMap(benchFile, fileEncoding);}
						catch(IOException io){
							Log.info(debugmsg+" benchmark file '"+ benchFile +"' IOException: "+io.getMessage());
							isMissing = true;
							missingMessage = io.getMessage();
						}
					}else{
						try{
							benchContents = StringUtils.readEncodingfile(benchFile, fileEncoding);
							if(benchContents != null && !benchContents.isEmpty()){
								benchmark = StringUtils.convertCollectionToMap(benchContents, testRecordData.getSeparator());
							}
						}
						catch(IOException io){
							Log.info(debugmsg+" benchmark file '"+ benchFile +"' IOException: "+io.getMessage());
							isMissing = true;
							missingMessage = io.getMessage();
						}
					}
					boolean success = action.equalsIgnoreCase(GenericMasterFunctions.VERIFYPROPERTIESSUBSETTOFILE_KEYWORD) ?
					                  StringUtils.isMatchingTargetMapValues(properties, benchmark):
							          StringUtils.isMatchingMaps(properties, benchmark);
					if(success){
						issuePassedSuccessUsing(benchFile);
					}else{
						testFile = FileUtilities.deduceMatchingUUIDFilename(benchFile);
						File testout = deduceTestFile(testFile);
						Log.info(debugmsg+" properties file: "+ testout.getAbsolutePath() +", encoding: "+fileEncoding);
						if(multiline){
							StringUtils.writeEncodingProperties(testout.getAbsolutePath(), properties, fileEncoding);
						}else{
							StringUtils.writeEncodingfile(testout.getAbsolutePath(), propertyContents, fileEncoding);
						}
						if(isMissing){
							missingMessage = FAILStrings.convert(FAILKEYS.FILE_READ_ERROR,
									  "Error reading from file '"+ benchFile+"'",
									  benchFile) +
									  ": "+ missingMessage +". " +
									  GENStrings.convert(GENKEYS.BE_SAVED_TO,
											  "'Properties' has been save to '"+ testout.getAbsolutePath()+"'",
											  "Properties", testout.getAbsolutePath());
							issueErrorPerformingActionOnX(compName, missingMessage);
						}else{
						    issueErrorPerformingActionOnX(compName, GENStrings.convert(GENKEYS.CONTENT_NOT_MATCHES_KEY,
					               "the content of '"+ testout.getAbsolutePath() +"' does not match the content of '"+ benchFile+"'",
					               testout.getAbsolutePath(),benchFile));
						}
					}
				}else{
					issueErrorPerformingActionOnX(compName, FAILStrings.convert(FAILKEYS.COULD_NOT_GET,
 							  "Could not get 'Properties'.",
 							  "Properties"));
				}
			} catch (IOException ioe) {
				issueErrorPerformingActionOnX(compName, FAILStrings.convert(FAILKEYS.FILE_ERROR,
						  "Error opening, reading, or writing file '"+ ioe.getMessage()+"'",
						  ioe.getMessage()));
			}
		}
	}

	protected void capturePropertyToFile () throws SAFSException {
		String debugmsg = StringUtils.debugmsg(false);
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);

		if (params.size() < 2) {
			paramsFailedMsg(windowName, compName);
		} else {
			String prop = iterator.next();
			String filename = iterator.next();
			String encoding = null;
			if(iterator.hasNext()) normalizeFileEncoding(iterator.next());

			filename = getAbsolutFileName(filename, STAFHelper.SAFS_VAR_TESTDIRECTORY);

			Log.info(debugmsg+" property: "+prop+" filename: "+filename+" encoding: "+encoding);

			try {
				Collection<String> contents = getPropertyCollection(prop);
				if(!contents.isEmpty()){
					String value = contents.iterator().next();
					String normval = StringUtils.normalizeLineBreaks(value);
					if(normval.contains("\n")){
						Map<String,String> props = new HashMap<String,String>();
						props.put(prop, value);
						StringUtils.writeEncodingProperties(filename, props, encoding);
					}else{
						StringUtils.writeEncodingfile(filename, contents, encoding);
					}
					// set status to ok
					testRecordData.setStatusCode(StatusCodes.OK);
					log.logMessage(testRecordData.getFac(),
							passedText.convert(GENKEYS.PERFNODE4A,
									action+" performed on "+ compName +"; output file '"+ filename +"'.",
									action, compName, filename),
							passedText.convert(GENKEYS.BE_SAVED_TO,
									"'"+ prop +"' has been saved to '"+ filename +"'",
									prop,filename),
									PASSED_MESSAGE);
				}else{
					issueActionFailure(failedText.convert(FAILKEYS.COULD_NOT_GET,
							                           "Could not get '"+ prop +"'.",
							                           prop));
				}
			} catch (IOException ioe) {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				log.logMessage(testRecordData.getFac(),
						testRecordData.getCommand()+
						" failure, file can't be written: "+filename+", msg: "+ioe.getMessage(),
						FAILED_MESSAGE);
			}
		}
	}

	protected void capturePropertiesToFile () throws SAFSException {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
		} else {
			String filename = iterator.next();
			String encoding = null;
			if(iterator.hasNext()) encoding = iterator.next();

			filename = getAbsolutFileName(filename, STAFHelper.SAFS_VAR_TESTDIRECTORY);
			Log.debug("capture properties to file "+filename+" with encoding "+encoding);

			boolean multiline =false;

			try {
				Collection<String> contents = getPropertyCollection((List<String>)null);
				if(! contents.isEmpty()){
					for(String line: contents){
						if(line.contains("\n")||line.contains("\r")){
							multiline = true;
							break;
						}
					}
					if(multiline){
						Map<String,String> props = StringUtils.convertCollectionToMap(contents, testRecordData.getSeparator());
						StringUtils.writeEncodingProperties(filename, props, encoding);
					}else{
						StringUtils.writeEncodingfile(filename, contents, encoding);
					}
					Log.debug("finished writing properties to file "+filename);
					// set status to ok
					testRecordData.setStatusCode(StatusCodes.OK);
					log.logMessage(testRecordData.getFac(),
							passedText.convert(GENKEYS.PERFNODE4A,
									action+" performed on "+ compName +"; output file '"+ filename +"'.",
									action, compName, filename),
									PASSED_MESSAGE);
				}else{
					issueActionFailure(failedText.convert(FAILKEYS.COULD_NOT_GET,
	                           							  "Could not get 'Properties'.",
	                           							  "Properties"));
				}
			} catch (IOException ioe) {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				log.logMessage(testRecordData.getFac(),
						testRecordData.getCommand()+
						" failure, file can't be written: "+filename+", msg: "+ioe.getMessage(),
						FAILED_MESSAGE);
			}
		}
	}

	protected void verifyClipboardToFile() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	/**
	 * <em>Purpose:</em> Response for HOVERMOUSE; called by componentProcess() to hover mouse.<br>
	 * <pre>
	 * Parameters:
	 * CoordinationMapKey String, The offset from 'Uper-Left corner' of component,
	 *                            such as "20;40", or a mapKey defined under "ComponentName" or "ApplicationConstants" in map file.
	 * HoverTime int, milliseconds to hover
	 * support different format of test record:
	 *  1. T,	ClassicCD,	JavaTree,	HoverMouse
	 *  2. T,	ClassicCD,	JavaMenu,	HoverMouse,	myCoord
	 *     --  In this case myCoord should be defined in the 'JavaMenu' section of the Application Map File like this:
	 *     --  [JavaMenu]
	 *     --  myCoord="10,10"
	 *  3. T,	ClassicCD,	JavaMenu,	HoverMouse,	"10;10"
	 *  4. T,	ClassicCD,	JavaTree,	HoverMouse,	"10;10", 4000
	 *  5. T,	ClassicCD,	JavaMenu,	HoverMouse,	, 3000
	 *</pre>
	 **/
	protected void hoverMouse() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		Point pointRelativeToComponent = null;//null is default, means the center to hover
		int milliseconds = 2000;//default is 2000 milli seconds to hover

		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		//optional parameter 'coordination'
		if(iterator.hasNext()){
			String param1 = iterator.next();
			pointRelativeToComponent = checkForCoord(param1);
			//if point cannot be got, we just write a warning message to debug log instead of
			//write error to SAFS Log. The reason is that maybe user just want to hover
			//at the center of component and hope adjust the 'hover time'.
			if(pointRelativeToComponent==null) IndependantLog.warn(debugmsg+ " '" + param1 + "' is not valid coordinate format.");
		}

		//optional parameter 'hoverTime'
		if(iterator.hasNext()){
			String param2 = iterator.next();
			try{
				milliseconds = Integer.parseInt(param2);
			}catch(NumberFormatException nfe){
				IndependantLog.warn(" '" + param2 + "' is not valid number. Met "+StringUtils.debugmsg(nfe));
			}
		}

		IndependantLog.info(debugmsg+" performing '"+action+"' at cordinate="+pointRelativeToComponent+"; hoverTime="+milliseconds+" seconds.");

		//ready to do the hoverMouse
		boolean hoverSuccess = false;
		try{
			IndependantLog.warn(debugmsg+" Try hover mouse by specific tools API.");
			hoverSuccess = performHoverMouse(pointRelativeToComponent, milliseconds);
		}catch(SAFSException se){
			IndependantLog.warn("Met "+StringUtils.debugmsg(se));
		}

		if(!hoverSuccess){
			IndependantLog.warn(debugmsg+" Fail to hover with specifc tools API, Try org.safs.robot.Robot to hover.");
			Rectangle screenRect = getComponentRectangleOnScreen();
			if(screenRect==null){
				issueErrorPerformingActionOnX(windowName+":"+compName, "Can NOT get component's screen rectangle.");
				return;
			}

			Point point = new Point(screenRect.x, screenRect.y);
			if(pointRelativeToComponent==null){
				point.translate(screenRect.width/2, screenRect.height/2);
			}else{
				point.translate(pointRelativeToComponent.x, pointRelativeToComponent.y);
			}

			try{
				org.safs.robot.Robot.mouseHover(point, milliseconds);
				hoverSuccess = true;
			}catch(Exception e){
				IndependantLog.error(debugmsg+" Use Robot to hover mouse, met "+StringUtils.debugmsg(e));
				hoverSuccess = false;
			}
		}

		if(hoverSuccess){
			String message = null;
			if(pointRelativeToComponent!=null){
				altText += " using ["+pointRelativeToComponent.x+","+pointRelativeToComponent.y+"]";
				message = genericText.convert(TXT_SUCCESS_3a, altText, windowName, compName, action, pointRelativeToComponent.toString());
			}else{
				message = genericText.convert(TXT_SUCCESS_3, altText, windowName, compName, action);
			}
			testRecordData.setStatusCode(StatusCodes.OK);
			log.logMessage(testRecordData.getFac(), message, PASSED_MESSAGE);
		}else{
			String error = pointRelativeToComponent==null? "":"["+pointRelativeToComponent.x+","+pointRelativeToComponent.y+"]";
			issueErrorPerformingActionOnX(compName, error);
		}
	}

	protected void setPropertyValue() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void performScorll() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	/**
	 * @param fuzzy, false: select the menu item that exactly matches the given path
	 */
	protected void selectMenuItem(boolean fuzzy) throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	/**
	 * @param fuzzy, to do exactly match if false, otherwise do fuzzy match
	 */
	protected void verifyMenuItem(boolean fuzzy) throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void performDrag() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void dragTo() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	protected void sendEvent() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	/**
	 * Use OCR to detect the text from captured screen, executing SaveTextFromGUI or SaveTextFromGUI.
	 * T, Window, Component, SaveTextFromGUI, outputfile [,subArea] [,OCR option] [,LangId] [,scaleRatio]
	 * T, Window, Component,GetTextFromGUI, variable [,subArea][,OCR] [,LangId] [,scaleRatio]
	 *
	 * @throws SAFSException
	 */
	protected void action_GetSaveTextFromGUI() throws SAFSException {
		String debugmsg = StringUtils.debugmsg(false);
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);

		STAFHelper staf = testRecordData.getSTAFHelper();
		if (params.size() < 1) {
			Log.error(debugmsg+"require at least one paramater, exit!");
			issueParameterCountFailure("output");
			return;
		}

		String output  = iterator.next();		     				// 1st param, a variable name (for GetTextFromImage) or a file name (for SaveTextFromImage)
		String subareaKey = "";                        	     		// 2nd optional param, setting default. A subkey in appmap
		String ocrId      = OCREngine.OCR_DEFAULT_ENGINE_KEY;		// 3th optional param, setting default
		String langId     = OCREngine.getOCRLanguageCode(staf);  	// 4th optional param, setting the language defined in STAF as default
		float  scaleRatio  = -1; 							     	// 5th optional param, setting default

		if (iterator.hasNext()) subareaKey = iterator.next();
		if (iterator.hasNext()) {
			ocrId = iterator.next();
			if(ocrId.equals("")) ocrId = OCREngine.OCR_DEFAULT_ENGINE_KEY;
		}
		if (iterator.hasNext()) {
			langId = iterator.next();
			if(langId.equals("")) langId = OCREngine.getOCRLanguageCode(staf);
		}
		if (iterator.hasNext())
			try { scaleRatio = (float)Double.parseDouble(iterator.next()); } catch(NumberFormatException nfe){}

		IndependantLog.debug(debugmsg+" Parameters: output='"+output+"' "+
				"subareaKey='"+subareaKey+"' "+
				"ocrId='"+ocrId+"' "+
				"langId='"+langId+"' "+
				"scaleRatio='"+scaleRatio+"' ");

		//get subarea from currect Component
		Rectangle imageRect = null;
		try{
			imageRect = deduceImageRect(subareaKey);
		}catch(SAFSException e){
			issueParameterValueFailure("SubArea "+e.getMessage());
			return;
		}

		//capture GUI
		BufferedImage buffimg = null;
		try {
			Log.debug(debugmsg+" ...Capture Screen Area: "+ imageRect);
			buffimg = getRectangleImage(imageRect);
		}catch (SAFSException se) {
			issueErrorPerformingAction(StringUtils.debugmsg(se));
			return;
		}

		// detect the text on GUI
		OCREngine ocrEngine = OCREngine.getOCREngine(ocrId, staf);
		scaleRatio = (scaleRatio <=0 )? ocrEngine.getdefaultZoomScale():scaleRatio;

		//detect the text in the image file
		String text = ocrEngine.imageToText(buffimg, langId, null, scaleRatio);

		//get optional SubArea parameter
		if (GETTEXTFROMGUI.equalsIgnoreCase(action)) {
			//write the detected text to output variable
			if (!setVariable(output, text)) {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				issueErrorPerformingAction(" setVariable failure, variable: " + output);
				return;
			}
		} else if (SAVETEXTFROMGUI.equalsIgnoreCase(action)) {
			//write the detected text to output file
			output = FileUtilities.normalizeFileSeparators(output);

			//build File
			if( output.indexOf(File.separator) > -1 ) {
				output = getAbsolutFileName(output, STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
			}else{
				output = getAbsolutFileName(output, STAFHelper.SAFS_VAR_TESTDIRECTORY);
			}

			try {
				IndependantLog.debug(debugmsg+" Writing to file '"+output+"'");
				FileUtilities.writeStringToUTF8File(output, text);
			} catch(Exception ex) {
				throw new SAFSException(ex.toString());
			}

		} else {
			Log.warn("No code responsible for keyword: " + action);
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			return;
		}

		//set status to ok
		testRecordData.setStatusCode(StatusCodes.OK);
		String detail = genericText.convert(TXT_SUCCESS_2a, action + " successful using " + ocrId, action, ocrId)+ ". " +
				genericText.convert(GENStrings.BE_SAVED_TO, "'"+text+"' has been saved to '"+output+"'", text, output);

		log.logMessage(testRecordData.getFac(), detail, PASSED_MESSAGE);
	}

	/**'screen'*/
	public static final String RELATIVE_TO_SCREEN = "screen";
	/**'parent'*/
	public static final String RELATIVE_TO_PARENT = "parent";
	/**
	 * <em>Purpose:</em> Response for HOVERMOUSE; called by componentProcess() to hover mouse.<br>
	 * <pre>
	 * Parameters:
	 * VarName String, The root name of the collection of variables to receive the location and dimensions.
	 * WhomRelativeTo String, "screen" or "parent"
	 * T  WindowName  CompName  LocateScreenImage  VarName [WhomRelativeTo]
	 *        [WhomRelativeTo]: relative to the screen or parentWindow
	 *        default(not set): relative to the screen
	 *                  screen: relative to the screen
	 *                  parent: relative to the parent window
	 *
	 * locate the GUI if CompName in the GUI of WindowName and write the area to Varname
	 *
	 * variables set:
	 *   varname=x y w h  (space delimited)
	 *   varname.x=x
	 *   varname.y=y
	 *   varname.w=w
	 *   varname.h=h
	 * </pre>
	 */
	protected void locateScreenImage(){
		String debugmsg = StringUtils.debugmsg(false);

		testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );

		if(params.size() < 1){
			issueParameterCountFailure("VarName");
			return;
		}

		String varname = iterator.next( );
		try{ if(varname.indexOf("^")==0) varname = varname.substring(1);}
		catch(Exception x){
			IndependantLog.error(debugmsg+"Met Exception "+StringUtils.debugmsg(x));
			varname = null;
		}
		if( varname==null || varname.length()==0 ){
			issueParameterValueFailure("VarName is null or empty.");
			return;
		}

		boolean relativeToSceen = true;
		if(iterator.hasNext()){
			String relativeStr = iterator.next( );
			if(RELATIVE_TO_PARENT.equals(relativeStr)) relativeToSceen=false;
			//RELATIVE_TO_SCREEN or other value, relativeToSceen=true
		}

		//get component's screen rectangle
		Rectangle rectangle = getComponentRectangleOnScreen();
		String who = windowName + ":" + compName;
		if (rectangle == null) {
			String message = FAILStrings.convert(FAILStrings.NOT_FOUND_ON_SCREEN, who + " was not found on screen", who);
			issueErrorPerformingActionOnX(who, message);
			return;
		}

		//get window's screen rectangle
		if (!relativeToSceen) {
			Rectangle winRect = getWindowRectangleOnScreen();
			if (winRect == null) {
				who = windowName + ":" + windowName;
				String message = FAILStrings.convert(FAILStrings.NOT_FOUND_ON_SCREEN, who + " was not found on screen", who);
				issueErrorPerformingActionOnX(who, message);
				return;
			}
			rectangle.x = rectangle.x - winRect.x;
			rectangle.y = rectangle.y - winRect.y;
		}

		if (setRectVars(rectangle, varname)) {
			//varAssigned2:Value '%1%' was assigned to variable '%2%'.
			String vals = rectangle.x +" "+rectangle.y+" "+rectangle.width+" "+ rectangle.height;
			String vars = varname+".x, "+ varname+".y, "+ varname+".w, "+ varname+".h";
			String message = GENStrings.convert(GENStrings.VARASSIGNED2, "Value '"+ vals +"' was assigned to variable '"+ vars +"'", vals, vars);
			issuePassedSuccess(message);
			return;
		} else {
			//could_not_set_vars :Could not set one or more variable values.
			String message = FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS, "Could not set one or more variable values.") +" "+ varname;
			issueActionFailure(message);
			return;
		}
	}

	/**
	 * <em>Purpose:</em> Show the component on the page as much as possible.<br>
	 */
	protected void action_showOnPage() throws SAFSException{
		testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );

		boolean verify = false;
		if(iterator.hasNext()){
			verify = StringUtilities.convertBool(iterator.next());
		}

		if(showComponentAsMuchPossible(verify)){
			String message = GENStrings.convert(GENStrings.SUCCESS_3,
					windowName+":"+compName+" "+action+" successful.",
					windowName, compName, action);
			issuePassedSuccess(message);
		}else{
			String message = FAILStrings.convert(FAILStrings.FAILURE_3,
					"Unable to perform "+action+" on "+compName+" in "+windowName+".",
					windowName, compName, action);
			issueActionFailure(message);
		}
	}

	protected void clearCache() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	/**
	 * Waits for the object to be present on the screen, waits up to the timeout defined in
	 * the parameters or a default of 15 seconds.
	 */
	protected void waitForGUI () throws SAFSException{
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		int secii = 15;
		String secTimeout = "15"; // default
		try { secTimeout = testRecordData.getTrimmedUnquotedInputRecordToken( 4 ); }
		catch ( Exception npe ) { }

		try {
			secii = new Integer(secTimeout).intValue();
		} catch (NumberFormatException nfe) {
			throw new SAFSException("'"+secTimeout+"' is not an integer.");
		}

		if (compName.equalsIgnoreCase(windowName)) {
			// wait for the window
			int status = waitForObject( mapname, windowName, compName, secii );

			//if window cannot be found within timeout
			if (status != 0) {
				log.logMessage(testRecordData.getFac(),
						WAITFORGUI + "\n" +
								"WINDOW:" + windowName +
								" could not be found on screen. " +
								testRecordData.getFilename() + " at Line " + testRecordData.getLineNumber() +
								", " + testRecordData.getFac() + ", " + windowName,
								FAILED_MESSAGE);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				return;
			}
		} else {
			//look for the component
			int status = waitForObject( mapname, windowName, compName, secii );
			//if component cannot be found within timeout
			if (status != 0) {
				log.logMessage(testRecordData.getFac(),
						WAITFORGUI + "\n" +
								"COMPONENT:"+ compName +" could not be found in window " +
								windowName + "." + testRecordData.getFilename() +
								" at Line " + testRecordData.getLineNumber() + ", " +
								testRecordData.getFac() + ", " +
								"Error("+ status +") with "+ compName,
								FAILED_MESSAGE);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				return;
			}
		}
		log.logMessage(testRecordData.getFac(),
				genericText.convert(PRE_TXT_SUCCESS_4, altText, "secTimeout: "+ secTimeout,
						windowName, compName, action),
						GENERIC_MESSAGE);
		testRecordData.setStatusCode(StatusCodes.OK);

	}

	/** <br><em>Purpose:</em> verifyProperty
	 **/
	protected void verifyProperty () throws SAFSException {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		if (params.size() < 2) {
			paramsFailedMsg(windowName, compName);
			return;
		}
		String prop = iterator.next();
		String val = iterator.next();
		boolean ignorecase = false;
		if(iterator.hasNext()) ignorecase = !StringUtils.isCaseSensitive(iterator.next());

		Log.info("..... ready to do the VP for prop : "+prop+" val: "+val);
		String rval = getProperty(prop).toString();
		Log.info("..... real value is: "+rval);

		if ((!ignorecase && val.equals(rval))
				|| (ignorecase && val.equalsIgnoreCase(rval))){
			// set status to ok
			testRecordData.setStatusCode(StatusCodes.OK);
			log.logMessage(testRecordData.getFac(),
					passedText.convert(TXT_SUCCESS_5,
							altText+", Property \""+ prop +"\" had value \""+ rval +"\" as expected.",
							windowName, compName, action,
							"Property "+prop,
							"had value '"+rval+"' as expected."),
							PASSED_MESSAGE);
			return;
		}

		String message = FAILStrings.convert(FAILStrings.SOMETHING_NOT_MATCH,
				                             "Property '"+prop+"' value '"+rval+"' does not match expected value '"+val+"'.",
				                             "Property '"+prop+"'", rval, val);
		issueErrorPerformingActionOnX(compName, message);
	}

	protected void assignPropertyVariable() throws SAFSException {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);

		if (params.size() < 2) {
			paramsFailedMsg(windowName, compName);
		} else {
			String prop = iterator.next();
			String var =  iterator.next();
			Log.info(".....CFComponent.process; ready to assign for prop : "+prop+" var: "+var);

			Object rval = getProperty(prop);

			Log.info("..... real value is: "+rval);
			if (rval == null || rval.equals("null")) {
				String message = FAILStrings.convert(FAILStrings.PROPERTY_VALUE_IS_NULL,
                                                     "Property: "+prop+", its value is null.",prop);
				issueErrorPerformingActionOnX(compName, message);
				return;
			} else {
				// do it...
				if (!setVariable(var, rval.toString())) {
					String message = GENStrings.convert(GENStrings.PROPERTY_VALUE_IS,
							                            "Property: "+prop+", its value is "+rval, prop, rval.toString());
					message = message +" But " + FAILStrings.convert(FAILStrings.COULD_NOT_SET,
                                                 "Could not set '"+rval+"' to '"+var+"'",
                            					 rval.toString(), var);
					issueErrorPerformingActionOnX(compName, message);
					return;
				}
				// set status to ok
				testRecordData.setStatusCode(StatusCodes.OK);
				String msg = passedText.convert(TXT_SUCCESS_5,
						altText+", Assigned variable \""+ var +"\" value \""+ rval +"\".",
						windowName, compName, action,
						" Assigned variable '"+var+"'",
						"value '"+rval+"'.");
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
			}
		}
	}

	protected int waitForObject(String mapname, String windowName, String compName, int secii) throws SAFSException{
		DDGUIUtilities utils = ((TestRecordHelper)testRecordData).getDDGUtils();
		return utils.waitForObject(mapname,windowName, windowName, secii);
	}

	/**
	 * A Collection contains the value of a property.  If the value contains:
	 * <pre>
	 *   {@link StringUtils#CRLF},
	 *   {@link StringUtils#CARRIAGE_RETURN},
	 *   {@link StringUtils#NEW_LINE},
	 * </pre>
	 * by calling this method, this value will be broke into several values according to those line breaks.<br>
	 * Each of these values will be stored as an item in the new collection. (The old collection contains<br>
	 * these values as ONE item containing all the various linebreaks.)<br>
	 *
	 * @param properties Collection<String>, a collection of properties to normalize
	 * @param encoding String, the encoding used to write and read a collection of properties
	 * @return Collection<String>, a normalized collection of properties
	 * @see #getPropertyCollection(List)
	 * @see #getPropertyCollection(String)
	 * @see #verifyPropertyToFile(boolean)
	 */
	@SuppressWarnings("unchecked")
	public static Collection<String> normalizePropertyCollection(Collection<String> properties, String encoding){
		Collection<String> normalizedProperties = properties;
		File tempFile = null;
		try{
			boolean needNomalization = false;
			Iterator<String> values = properties.iterator();
			String value = null;
			while(values.hasNext()){
				value = values.next();
				if(value!=null && (
						value.contains(StringUtils.NEW_LINE) ||
						value.contains(StringUtils.CARRIAGE_RETURN)
						           )
				   ){
					needNomalization = true;
					break;
				}
			}

			if(needNomalization){
				tempFile = File.createTempFile("normalizedProperties", ".dat");
				StringUtils.writeEncodingfile(tempFile.getCanonicalPath(), properties, encoding);
				normalizedProperties = StringUtils.readEncodingfile(tempFile.getCanonicalPath(), encoding);
			}

		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" met "+StringUtils.debugmsg(e));
		}finally{
			if(tempFile!=null) tempFile.delete();
		}

		return normalizedProperties;
	}

	/**
	 * Get the value of a property, and return it as a Collection.<br>
	 * @param property String, the property name
	 * @return Collection<String>, a set containing the property's value
	 */
	protected Collection<String> getPropertyCollection(String property) throws SAFSException{
		if(property==null || property.trim().isEmpty()) throw new SAFSException("property is null or empty, not valid.");
		List<String> propList = new ArrayList<String>();
		propList.add(property);
		return getPropertyCollection(propList);
	}

	/**
	 * Get the value of property, and return them as a Collection.<br>
	 * If the property-list contains only ONE property, the collection will contain only its value.<br>
	 * If the property-list contains multiple properties, the collection will contain a set of string like "property:value".<br>
	 * The assignment separator will actually be the current value of the testRecordData.separator.
	 * If the property-list is null, then all the properties should be retrieved.<br>
	 * @param propertyList List<String>, a list of property names
	 * @return Collection<String>, a set of values
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Collection<String> getPropertyCollection(List<String> propertyList) throws SAFSException{
		Collection contents = new ArrayList();
		Object value = null;
		String delim = testRecordData.getSeparator();

		if(propertyList==null){
			Map properties = getProperties();
			if(properties!=null){
				SortedSet keys = new TreeSet(properties.keySet());
				Iterator keyiter = keys.iterator();
				Object key = null;
				while(keyiter.hasNext()){
					key = keyiter.next();
					value = getStringValue(properties.get(key));
					contents.add(key+delim+value);
				}
			}
		}else if(propertyList.size()==1){
			if((value=getProperty(propertyList.get(0)))!=null) contents.add(value);
		}else{//multiple properties
			for(String prop: propertyList) contents.add(prop+delim+getProperty(prop));
		}

		return contents;
	}
	/**
	 * Get the string value from the Object value.<br>
	 * If the value is a collection, it will be converted to a string separated by 'New Line'.<br>
	 */
	protected String getStringValue(Object value) throws SAFSException{
		if(value==null) return null;
		else if(value instanceof String  ||
				value instanceof Boolean ||
				value instanceof Number) return value.toString();
		else if(value instanceof Collection){
			StringBuffer sb = new StringBuffer();
			Iterator<?> iter = ((Collection<?>)value).iterator();

			Object tempValue = null;
			while(iter.hasNext()){
				tempValue = getStringValue(iter.next());
				if(tempValue!=null){
					sb.append(tempValue);
					if(iter.hasNext()) sb.append(Console.EOL);
				}
			}
			return sb.toString();
		}
		else{
			IndependantLog.debug("May need to handle '"+value.getClass().getName()+"' to get string value.");
			return value.toString();
		}
	}
	/**Get component's property string value*/
	protected String getProperty(String propertyName) throws SAFSException{
		return getStringValue(getPropertyObject(propertyName));
	}

	//***================================ Followings MAY NEED to be overrided in subclass =====================****//
	/**
	 * @return Map<String, Object>, a map of (property, value) for all properties of the component. It may be null or empty.
	 */
	protected Map<String, Object> getProperties() throws SAFSException{
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	/**
	 * @return Object, component's property object value
	 */
	protected Object getPropertyObject(String propertyName) throws SAFSException{
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	/**
	 * @return Collection<String>, component's content. It may be null or empty.
	 */
	protected Collection<String> captureObjectData() throws SAFSException {
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	/**
	 * Get the absolute 'Rectangle bounds' of the Window, which means the rectangle is relative to the<br>
	 * whole screen.<br>
	 * @return Rectangle, 'Rectangle bounds' of the Window
	 */
	protected Rectangle getWindowRectangleOnScreen(){
		String debugmsg = StringUtils.debugmsg(true);
		IndependantLog.warn(debugmsg+"Not supported yet. Sub class '"+getClass()+"' SHOULD override me!");
		return null;
	}

	/**
	 * Get the absolute 'Rectangle bounds' of the Component, which means the rectangle is relative to the<br>
	 * whole screen, NOT to a window/browser.<br>
	 * @return Rectangle, 'Rectangle bounds' of the Component
	 */
	protected Rectangle getComponentRectangleOnScreen(){
		String debugmsg = StringUtils.debugmsg(true);
		IndependantLog.warn(debugmsg+"Not supported yet. Sub class '"+getClass()+"' SHOULD override me!");
		return null;
	}

	/**
	 * If the component is not fully shown on the screen, try to make it visible on screen as much
	 * as possible.
	 * @param verify boolean, verify that the component is shown on page if verify is true.
	 * @return boolean, true if the component is shown on page; false otherwise.
	 * @throws SAFSException if some unexpected things happen.
	 */
	protected boolean showComponentAsMuchPossible(boolean verify) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(true);
		IndependantLog.warn(debugmsg+"Not supported yet. Sub class '"+getClass()+"' SHOULD override me!");
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}
	/**
	 * Do nothing method returns null.
	 * Subclasses should override to provide the 'Rectangle bounds' of the Component
	 * currently represented in the current testRecordData. The 'Rectangle bounds' may be
	 * absolute on screen or relative to a window/browser, this is decided by subclass.
	 * If returned 'Rectangle bounds' is NOT absolute on screen, the the method
	 * {@link #getRectangleImage(Rectangle)} SHOULD be overridden in subclass to provide appropriate implementation.
	 * <br>
	 * Subclasses should assume the mapname, windowName, and compName currently stored
	 * for the instance hold the information needed to identify and locate the Component
	 * on the screen or in the system.
	 * <br>
	 * This method is called by {@link #deduceImageRect(String)} to get a subarea rectangle.
	 * <br>
	 * @return Rectangle bounds of the Component or null.
	 * @see #deduceImageRect(String)
	 */
	protected Rectangle getComponentRectangle(){
		String debugmsg = StringUtils.debugmsg(true);
		IndependantLog.warn(debugmsg+"Not supported yet. Sub class '"+getClass()+"' SHOULD override me!");
		return null;
	}

	/**
	 * Get BufferedImage within a rectangle.<br>
	 * The rectangle may be absolute on screen, Or it may be relative to a browser or something.<br>
	 * It depends on what is returned by {@link #getComponentRectangle()}.<br>
	 * This method assume the rectangle is absolute on screen, so get the image on screen within the rectangle.<br>
	 * Sub class may override this method to get its own image.<br>
	 *
	 * @param imageRect Rectangle, within the rectangle to get image. <br>
	 * @return BufferedImage
	 * @throws SAFSException
	 * @see {@link #getComponentRectangle()}
	 * @see #deduceImageRect(String)
	 */
	protected BufferedImage getRectangleImage(Rectangle imageRect) throws SAFSException{
		try {
			return ImageUtils.captureScreenArea(imageRect);
		} catch (AWTException e) {
			String msg = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, "Support for 'AWT Robot' not found.", "AWT Robot");
			throw new SAFSException(msg);
		}
	}
	/**
	 * To test if the component exists or not.<br>
	 * The sub class may test the component's visibility also.<br>
	 *
	 * @return boolean, true if the component GUI exist; false otherwise.
	 * @throws SAFSException
	 */
	protected boolean exist() throws SAFSException{
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

	/**
	 * Purpose: perform hovering mouse at 'point' for 'milliseconds' period on a Component.<br>
	 * Subclass may need to give a specific implementation by tools (RJ, Selenium etc.) API.<br>
	 * @param point Point, coordinate relative to the component to hover at. null if hover the center.
	 * @param milliseconds int, time to hover, in milliseconds
	 * @return boolean, true if hover successful; false otherwise
	 * @exception SAFSException
	 **/
	protected boolean performHoverMouse(Point point, int milliseconds) throws SAFSException{
		throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
	}

}
