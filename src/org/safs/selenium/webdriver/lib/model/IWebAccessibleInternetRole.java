/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

/**
 * Contains the aria attributes and role's types, refer to <a href='http://www.w3.org/TR/wai-aria/rdf_model.svg'>RDF-MODEL</a>
 *
 * <BR>
 * History:<br>
 *
 *  <br>   Jun 16, 2014    (Lei Wang) Initial release.
 */
public interface IWebAccessibleInternetRole {
	/**'role' a Web Accessible Internet's attribute*/
	public static final String ATTRIBUTE_WAI_ROLE 				= "role";
	public static final String ATTRIBUTE_ARIA_ATOMIC 			= "aria-atomic";
	public static final String ATTRIBUTE_ARIA_BUSY 				= "aria-busy";
	public static final String ATTRIBUTE_ARIA_CONTROLS 			= "aria-controls";
	public static final String ATTRIBUTE_ARIA_DESCRIBEDBY		= "aria-describedby";
	public static final String ATTRIBUTE_ARIA_DISABLED			= "aria-disabled";
	public static final String ATTRIBUTE_ARIA_DRIPEFFECT		= "aria-drieffect";
	public static final String ATTRIBUTE_ARIA_FLOWTO			= "aria-flowto";
	public static final String ATTRIBUTE_ARIA_GRABBED			= "aria-grabbed";
	public static final String ATTRIBUTE_ARIA_HASPOPUP			= "aria-haspopup";
	public static final String ATTRIBUTE_ARIA_HIDDEN			= "aria-hidden";
	public static final String ATTRIBUTE_ARIA_INVALID			= "aria-invalid";
	public static final String ATTRIBUTE_ARIA_LABEL 			= "aria-label";
	public static final String ATTRIBUTE_ARIA_LABELDEBY			= "aria-labeledby";
	public static final String ATTRIBUTE_ARIA_LIVE 				= "aria-live";
	public static final String ATTRIBUTE_ARIA_OWNS 				= "aria-owns";
	public static final String ATTRIBUTE_ARIA_RELEVANT 			= "aria-relevant";
	public static final String ATTRIBUTE_ARIA_ACTIVEDDESCENDANT	= "aria-activeddescendant";

	public static final String ATTRIBUTE_ARIA_VALUENOW			= "aria-valuenow";
	public static final String ATTRIBUTE_ARIA_VALUEMIN			= "aria-valuemin";
	public static final String ATTRIBUTE_ARIA_VALUEMAX			= "aria-valuemax";
	public static final String ATTRIBUTE_ARIA_VALUETEXT			= "aria-valuetext";

	public static final String ATTRIBUTE_ARIA_EXPANDED			= "aria-expanded";
	public static final String ATTRIBUTE_ARIA_SELECTED			= "aria-selected";
	public static final String ATTRIBUTE_ARIA_READONLY 			= "aria-readonly";
	public static final String ATTRIBUTE_ARIA_REQUIRED 			= "aria-required";
	public static final String ATTRIBUTE_ARIA_CHECKED			= "aria-checked";
	public static final String ATTRIBUTE_ARIA_PRESSED			= "aria-pressed";
	public static final String ATTRIBUTE_ARIA_LEVEL				= "aria-level";
	public static final String ATTRIBUTE_ARIA_MULTISELECTABLE	= "aria-multiselectable";
	public static final String ATTRIBUTE_ARIA_ORIENTATION		= "aria-orientation";
	public static final String ATTRIBUTE_ARIA_POSINSET			= "aria-posinset";
	public static final String ATTRIBUTE_ARIA_SETSIZE			= "aria-setsize";
	public static final String ATTRIBUTE_ARIA_AUTOCOMPLETE		= "aria-autocomplete";
	public static final String ATTRIBUTE_ARIA_SORT				= "aria-sort";


	public static final String WAI_ROLE_SEPARATOR	 	= "separator";
	public static final String WAI_ROLE_PRESENTATION	= "presentation";
	public static final String WAI_ROLE_DOCUMENT	 	= "document";
	public static final String WAI_ROLE_DIALOG		 	= "dialog";

	public static final String WAI_ROLE_PROGRESSBAR 	= "progressbar";
	public static final String WAI_ROLE_SPINBUTTON 		= "spinbutton";
	public static final String WAI_ROLE_SLIDER		 	= "slider";
	public static final String WAI_ROLE_SCROLLBAR	 	= "scrollbar";

	public static final String WAI_ROLE_TEXTBOX 		= "textbox";
	public static final String WAI_ROLE_OPTION		 	= "option";
	public static final String WAI_ROLE_CHECKBOX	 	= "checkbox";
	public static final String WAI_ROLE_MENUITEM	 	= "menuitem";
	public static final String WAI_ROLE_BUTTON		 	= "button";
	public static final String WAI_ROLE_LINK		 	= "link";
	public static final String WAI_ROLE_LISTITEM	 	= "listitem";
	public static final String WAI_ROLE_GROUP		 	= "group";
	public static final String WAI_ROLE_TOOLTIP		 	= "tooltip";
	public static final String WAI_ROLE_IMG			 	= "img";
	public static final String WAI_ROLE_MARQUEE		 	= "marquee";
	public static final String WAI_ROLE_DEFINITION	 	= "definition";
	public static final String WAI_ROLE_NOTE		 	= "note";
	public static final String WAI_ROLE_MATH		 	= "math";
	public static final String WAI_ROLE_REGION		 	= "region";
	public static final String WAI_ROLE_ARTICLE		 	= "article";
	public static final String WAI_ROLE_GRIDCELL	 	= "gridcell";
	public static final String WAI_ROLE_HEADING		 	= "heading";
	public static final String WAI_ROLE_TAB			 	= "tab";

	public static final String WAI_ROLE_COLUMNHEADER 	= "columnheader";
	public static final String WAI_ROLE_ROWHEADER	 	= "rowheader";

	public static final String WAI_ROLE_RADIO		 	= "radio";
	public static final String WAI_ROLE_MENUITEMCHECKBOX 	= "menuitemcheckbox";
	public static final String WAI_ROLE_MENUITEMRADIO 	= "menuitemradio";
	public static final String WAI_ROLE_TREEITEM	 	= "treeitem";
	public static final String WAI_ROLE_ROW			 	= "tree";
	public static final String WAI_ROLE_ROWGROUP	 	= "rowgroup";
	public static final String WAI_ROLE_TOOLBAR		 	= "toolbar";
	public static final String WAI_ROLE_GRID		 	= "grid";
	public static final String WAI_ROLE_LIST		 	= "list";
	public static final String WAI_ROLE_TABPANEL	 	= "tabpanel";
	public static final String WAI_ROLE_LOG			 	= "log";
	public static final String WAI_ROLE_STATUS		 	= "status";
	public static final String WAI_ROLE_ALERT		 	= "alert";
	public static final String WAI_ROLE_RADIOGROUP	 	= "radiogroup";
	public static final String WAI_ROLE_COMBOBOX	 	= "combobox";
	public static final String WAI_ROLE_TREE		 	= "tree";
	public static final String WAI_ROLE_MENU		 	= "menu";
	public static final String WAI_ROLE_LISTBOX		 	= "listbox";
	public static final String WAI_ROLE_DIRECTORY	 	= "directory";
	public static final String WAI_ROLE_APPLICATION	 	= "application";
	public static final String WAI_ROLE_BANNER		 	= "banner";
	public static final String WAI_ROLE_COMPLEMENTARY 	= "complementary";
	public static final String WAI_ROLE_CONTENTINFO 	= "contentinfo";
	public static final String WAI_ROLE_FORM		 	= "form";
	public static final String WAI_ROLE_MAIN		 	= "main";
	public static final String WAI_ROLE_NAVIGATION	 	= "navigation";
	public static final String WAI_ROLE_SEARCH		 	= "search";
	public static final String WAI_ROLE_TIMER		 	= "timer";
	public static final String WAI_ROLE_ALERTDIALOG	 	= "alertdialog";
	public static final String WAI_ROLE_TREEGRID	 	= "treegrid";
	public static final String WAI_ROLE_MENUBAR		 	= "menubar";
	public static final String WAI_ROLE_TABLIST		 	= "tablist";
}
