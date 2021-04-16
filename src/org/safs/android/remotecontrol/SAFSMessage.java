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
package org.safs.android.remotecontrol;

/**
 * @author Carl Nagle, SAS Institute, Inc.
 */
public class SAFSMessage extends com.jayway.android.robotium.remotecontrol.solo.Message{

	public static final String KEY_WINNAME  = "winname";
	public static final String KEY_WINREC   = "winrec";
	public static final String KEY_COMPNAME = "compname";
	public static final String KEY_COMPREC  = "comprec";
	public static final String KEY_COMPTYPE = "comptype";

	public static final String PARAM_1 = "param1";
	public static final String PARAM_2 = "param2";
	public static final String PARAM_3 = "param3";
	public static final String PARAM_4 = "param4";
	public static final String PARAM_5 = "param5";
	public static final String PARAM_6 = "param6";
	public static final String PARAM_7 = "param7";
	public static final String PARAM_8 = "param8";
	public static final String PARAM_9 = "param9";

	/** 
	 * KEY_TARGET to use to route commands to remote EngineProcessor 
	 */
	public static final String target_safs_engine   ="safs_engine";
	public static final String engine_clearreferencecache = "clearreferencecache";
	public static final String engine_getaccessiblename = "getaccessiblename";
	public static final String engine_getcaption = "getcaption";
	public static final String engine_getchildcount = "getchildcount";
	public static final String engine_getchildren = "getchildren";
	public static final String engine_getclassindex = "getclassindex";
	public static final String engine_getclassname = "getclassname";
	public static final String engine_getcurrentwindow = "getcurrentwindow";
	public static final String engine_getid = "getid";
	public static final String engine_getmatchingchildobjects = "getmatchingchildobjects";
	public static final String engine_getmatchingparentobject = "getmatchingparentobject";
	public static final String engine_getmatchingpathobject = "getmatchingpathobject";
	public static final String engine_getname = "getname";
	public static final String engine_getnonaccessiblename = "getnonaccessiblename";
	public static final String engine_getproperty = "getproperty";
	public static final String engine_getpropertynames = "getpropertynames";
	public static final String engine_getstringdata = "getstringdata";
	public static final String engine_getsuperclassnames = "getsuperclassnames";
	public static final String engine_gettext = "gettext";
	public static final String engine_gettoplevelcount = "gettoplevelcount";
	public static final String engine_gettoplevelwindows = "gettoplevelwindows";
	public static final String engine_ismatchingpath = "ismatchingpath";
	public static final String engine_isenabled = "isenabled";
	public static final String engine_isshowing = "isshowing";
	public static final String engine_isvalid = "isvalid";
	public static final String engine_istoplevelpopupcontainer = "istoplevelpopupcontainer";	
	public static final String engine_setactivewindow = "setactivewindow";
	public static final String engine_highlightmatchingchildobject = "highlightmatchingchildobject";//Not implemented
	public static final String engine_highlightmatchingchildobjectbykey = "highlightmatchingchildobjectbykey";
	public static final String engine_clearhighlighteddialog = "clearhighlighteddialog";

	/** 
	 * KEY_TARGET to use to route commands to remote DriverProcessor (DriverCommands) 
	 */
	public static final String target_safs_driver   ="safs_driver";
	public static final String driver_onguiexistsgotoblockid = "onguiexistsgotoblockid";
	public static final String driver_onguinotexistgotoblockid = "onguinotexistgotoblockid";
	public static final String driver_waitforgui = "waitforgui";
	public static final String driver_waitforguigone = "waitforguigone";
	public static final String driver_clearclipboard = "clearclipboard";
	public static final String driver_saveclipboardtofile = "saveclipboardtofile";
	public static final String driver_setclipboard = "setclipboard";
	public static final String driver_verifyclipboardtofile = "verifyclipboardtofile";
	public static final String driver_assignclipboardvariable = "assignclipboardvariable";
	public static final String driver_takescreenshot = "takescreenshot";
	public static final String driver_hidesoftkeyboard = "hidesoftkeyboard";
	public static final String driver_showsoftkeyboard = "showsoftkeyboard";
	public static final String driver_clearappmapcache = "clearappmapcache";
	
	/**************************************************************************************** 
	 * KEY_TARGET to use to route duplicate commands according to search 
	 * algorithm matched component instance class. 
	 ****************************************************************************************/
	public static final String target_safs_comprouting				= "safs_comprouting";
	public static final String cf_comprouting_activateindex			= "activateindex";
	public static final String cf_comprouting_activateindexitem		= "activateindexitem";
	public static final String cf_comprouting_activatepartialmatch	= "activatepartialmatch";
	public static final String cf_comprouting_activatetextitem		= "activatetextitem";
	public static final String cf_comprouting_activateunverifiedtextitem = "activateunverifiedtextitem";
	public static final String cf_comprouting_captureitemstofile    = "captureitemstofile";
	public static final String cf_comprouting_check        			= "check";
	public static final String cf_comprouting_clickindex			= "clickindex";
	public static final String cf_comprouting_clickindexitem		= "clickindexitem";
	public static final String cf_comprouting_clicktextitem			= "clicktextitem";
	
	public static final String cf_comprouting_select						= "select";
	public static final String cf_comprouting_selectindex       			= "selectindex";
	public static final String cf_comprouting_selectindexitem      			= "selectindexitem";
	public static final String cf_comprouting_selectpartialmatch			= "selectpartialmatch";
	public static final String cf_comprouting_selecttextitem				= "selecttextitem";
	public static final String cf_comprouting_selectunverified				= "selectunverified";
	public static final String cf_comprouting_selectunverifiedtextitem		= "selectunverifiedtextitem";
	public static final String cf_comprouting_selectunverifiedpartialmatch	= "selectunverifiedpartialmatch";
	public static final String cf_comprouting_setlistcontains				= "setlistcontains";
	public static final String cf_comprouting_settextvalue 	 				= "settextvalue";
	public static final String cf_comprouting_settextcharacters     		= "settextcharacters";
	public static final String cf_comprouting_setunverifiedtextcharacters	= "setunverifiedtextcharacters";
	public static final String cf_comprouting_setunverifiedtextvalue		= "setunverifiedtextvalue";
	
	public static final String cf_comprouting_uncheck						= "uncheck";
	
	public static final String cf_comprouting_verifylistcontains					= "verifylistcontains";
	public static final String cf_comprouting_verifylistcontainspartialmatch		= "verifylistcontainspartialmatch";
	public static final String cf_comprouting_verifylistdoesnotcontain				= "verifylistdoesnotcontain";
	public static final String cf_comprouting_verifylistdoesnotcontainpartialmatch	= "verifylistdoesnotcontainpartialmatch";
	public static final String cf_comprouting_verifyitemunselected					= "verifyitemunselected";
	public static final String cf_comprouting_verifypartialmatch					= "verifypartialmatch";
	public static final String cf_comprouting_verifyselected						= "verifyselected";
	public static final String cf_comprouting_verifyselecteditem					= "verifyselecteditem";
	public static final String cf_comprouting_verifyselectedpartialmatch			= "verifyselectedpartialmatch";

	/** 
	 * KEY_TARGET to use to route commands to remote TabControl Processors, if any 
	 */
	public static final String target_safs_tab 				= "TabControl";	
	public static final String cf_tab_clicktab				= "clicktab";
	public static final String cf_tab_clicktabcontains		= "clicktabcontains";
	public static final String cf_tab_selecttab				= "selecttab";	
	public static final String cf_tab_selecttabindex		= "selecttabindex";	
	public static final String cf_tab_unverifiedclicktab	= "unverifiedclicktab";
	public static final String cf_tab_makeselection		    = "makeselection";
	
	/** 
	 * KEY_TARGET to use to route commands to remote ScrollBar Processors, if any 
	 */
	public static final String target_safs_scrollbar		= "ScrollBar";	
	public static final String cf_scrollbar_onedown			= "onedown";
	public static final String cf_scrollbar_oneleft			= "oneleft";
	public static final String cf_scrollbar_oneright		= "oneright";	
	public static final String cf_scrollbar_oneup			= "oneup";
	public static final String cf_scrollbar_pagedown		= "pagedown";
	public static final String cf_scrollbar_pageleft		= "pageleft";
	public static final String cf_scrollbar_pageright		= "pageright";	
	public static final String cf_scrollbar_pageup			= "pageup";
	
	/** 
	 * KEY_TARGET to use to route commands to remote Button Processors, if any 
	 */
	public static final String target_safs_button 		= "Button";
	
	/** 
	 * KEY_TARGET to use to route commands to remote CompoundButton Processors, if any 
	 */
	public static final String target_safs_compoundbutton 		= "CompoundButton";

	/** 
	 * KEY_TARGET to use to route commands to remote CheckBoxProcessor 
	 */
	public static final String target_safs_checkbox 		= "CheckBox";
	
	/** 
	 * KEY_TARGET to use to route commands to remote EditTextProcessor 
	 */
	public static final String target_safs_edittext 					= "EditText";
	
	/** 
	 * KEY_TARGET to use to route commands to remote ComboBoxProcessor 
	 */
	public static final String target_safs_combobox 					= "ComboBox";
	public static final String cf_combobox_items_separator		       	= "_$#_";
	
	/** 
	 * KEY_TARGET to use to route commands to remote GridViewProcessor 
	 */
	public static final String target_safs_gridview ="GridView";

	/** 
	 * KEY_TARGET to use to route commands to remote ListViewProcessor 
	 */
	public static final String target_safs_listview ="ListView";

	/** 
	 * KEY_TARGET to use to route commands to remote TextViewProcessor 
	 */
	public static final String target_safs_textview ="TextView";//GenericObject

	/** 
	 * KEY_TARGET to use to route commands to remote ViewProcessor 
	 */
	public static final String target_safs_view     			="View";    //GenericMaster
	public static final String cf_view_capturepropertiestofile	="capturepropertiestofile";
	public static final String cf_view_click        			="click";
	public static final String cf_view_tap          			="tap";
	public static final String cf_view_press        			="press";
	public static final String cf_view_getguiimage  			="getguiimage";
	public static final String cf_view_guidoesexist 			="guidoesexist";
	public static final String cf_view_guidoesnotexist 			="guidoesnotexist";
	public static final String cf_view_inputcharacters 			="inputcharacters";
	public static final String cf_view_inputkeys 				="inputkeys";
	public static final String cf_view_verifypropertiestofile	="verifypropertiestofile";
	public static final String cf_view_typechars 				="typechars";
	public static final String cf_view_typekeys 				="typekeys";
	
	/** 
	 * KEY_TARGET to use to route commands to remote DatePicker Processors, if any 
	 */
	public static final String target_safs_datepicker		= "DatePicker";
	public static final String cf_datepicker_getdate		= "getdate";
	public static final String cf_datepicker_setdate		= "setdate";
	
	/** 
	 * KEY_TARGET to use to route commands to remote TimePicker Processors, if any 
	 */
	public static final String target_safs_timepicker		= "TimePicker";
	public static final String cf_timepicker_gettime		= "gettime";
	public static final String cf_timepicker_settime		= "settime";
	
	/** 
	 * KEY_TARGET to use to route commands to remote ProgressBar Processors, if any 
	 */
	public static final String target_safs_progressbar		= "ProgressBar";
	public static final String cf_progressbar_getprogress	= "getprogress";
	public static final String cf_progressbar_setprogress	= "setprogress";
	//'getrating' and 'setrating' work for android.widget.RatingBar, a subclass of ProgressBar
	public static final String cf_progressbar_getrating		= "getrating";
	public static final String cf_progressbar_setrating		= "setrating";	
	
	/* ********************************************************************************
	   * ====================  OUT parameters from remote side  =====================  
	   *******************************************************************************/
	
	/** Value will hold NLS message Key from resource bundle  */
	public static final String RESOURCE_BUNDLE_KEY_FOR_MSG     		="rb_key_4_msg";
	/** Value will hold Name of the NLS message resource bundle  */
	public static final String RESOURCE_BUNDLE_NAME_FOR_MSG     	="rb_name_4_msg";
	/** Value will hold Params or arguments inserted into NLS message */
	public static final String RESOURCE_BUNDLE_PARAMS_FOR_MSG     	="rb_params_4_msg";
	/** Value will hold alternative text for message */
	public static final String RESOURCE_BUNDLE_ALTTEXT_FOR_MSG     	="rb_alttext_4_msg";
	
	/** Value will hold NLS detail message Key from resource bundle  */
	public static final String RESOURCE_BUNDLE_KEY_FOR_DETAIL_MSG     		="rb_key_4_d_msg";
	/** Value will hold Name of the NLS detail message resource bundle  */
	public static final String RESOURCE_BUNDLE_NAME_FOR_DETAIL_MSG     		="rb_name_4_d_msg";
	/** Value will hold Params or arguments inserted into NLS detail message */
	public static final String RESOURCE_BUNDLE_PARAMS_FOR_DETAIL_MSG     	="rb_params_4_d_msg";
	/** Value will hold alternative text for detail message */
	public static final String RESOURCE_BUNDLE_ALTTEXT_FOR_DETAIL_MSG     	="rb_alttext_4_d_msg";
	
}
