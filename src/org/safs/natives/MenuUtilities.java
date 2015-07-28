package org.safs.natives;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.natives.win32.User32;
import org.safs.text.FileUtilities;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;

public class MenuUtilities extends NativeWrapper {
	public static final int MU_STATE_FAILURE 	= -1;
	public static final int MF_CHECKED 			= 0x00000008;
	public static final int MF_DISABLED 		= 0x00000002;
	public static final int MF_GRAYED 			= 0x00000001;
	public static final int MF_HILITE 			= 0x00000080;
	public static final int MF_MENUBARBREAK 	= 0x00000020;
	public static final int MF_MENUBREAK 		= 0x00000040;
	public static final int MF_OWNERDRAW 		= 0x00000100;
	public static final int MF_POPUP 			= 0x00000010;
	public static final int MF_BITMAP 			= 0x00000004;
	public static final int MF_SEPARATOR 		= 0x00000800;
	public static final int MF_DEFAULT 			= 0x00001000;
	
	public static final int MF_BYCOMMAND 		= 0x00000000;
	public static final int MF_BYPOSITION 		= 0x00000400;
	
	public static final String MUStateFailureString = "MUStateFailure";
	
	public static final String CLASS_NAME 			= MenuUtilities.class.getSimpleName();
	public static final int MAX_MENU_ITEM_LEN 		= 256;
	
	/**
	 * Users can call this method to check the state before they call the other
	 * methods IsXXX()
	 */
	public void checkState(int state) throws SAFSException{
		if(state==MU_STATE_FAILURE) throw new SAFSException("State is -1, not correct");
	}
	
	public static boolean IsMenuItemEnabled(int state){
		return (state!=MU_STATE_FAILURE) && ((state&MF_DISABLED)!=MF_DISABLED);
	}
	
    public static boolean IsMenuItemChecked(int state){
    	return (state!=MU_STATE_FAILURE) && ((state&MF_CHECKED)==MF_CHECKED);
    }
    
    public static boolean IsMenuItemGrayed(int state){
    	return (state!=MU_STATE_FAILURE) && ((state&MF_GRAYED)==MF_GRAYED);    	
    }
    
    public static boolean IsMenuItemHiLited(int state){     
    	return (state!=MU_STATE_FAILURE) && ((state&MF_HILITE)==MF_HILITE);
    }
    
    public static boolean IsMenuItemDefault(int state){
    	return (state!=MU_STATE_FAILURE) && ( (state&MF_POPUP)==MF_POPUP || (state&MF_DEFAULT)==MF_DEFAULT );
    }

    public static boolean IsMenuItemABitmap(int state){ 
    	return (state!=MU_STATE_FAILURE) && ((state&MF_BITMAP)==MF_BITMAP);
    }
    
    public static boolean IsMenuItemAMenuBreak(int state){
    	return (state!=MU_STATE_FAILURE) && ((state&MF_MENUBREAK)==MF_MENUBREAK);    	
    }
    
    public static boolean IsMenuItemAMenuBarBreak(int state){
    	return (state!=MU_STATE_FAILURE) && ((state&MF_MENUBARBREAK)==MF_MENUBARBREAK);
    }
    
    public static boolean IsMenuItemAMenuSeparator(int state){
    	return (state!=MU_STATE_FAILURE) && ( (state&MF_POPUP)==MF_POPUP || (state&MF_SEPARATOR)==MF_SEPARATOR );
    }
    
    public static boolean IsMenuItemAMenu(int state){
    	return (state!=MU_STATE_FAILURE) && ((state&MF_POPUP)==MF_POPUP);
    }

    /**
     * DESCRIPTION:<br>
     *      Given state information of the state obtained by {@link User32#GetMenuState(NativeLong, int, int)}
     *      the routine converts it to a space-delimited string of all the state 
     *      information known for the provided state.  Ex:
     *
     *              "Enabled Unchecked Ungrayed Unhilited Default"   OR
     *
     *              "Enabled Unchecked Ungrayed Hilited Normal Menu With 5 MenuItems"
     *
     *      Valid States:
     *
     *          Enabled     Grayed      BarBreak                Bitmap
     *          Disabled    Ungrayed    Separator               Break
     *          Checked     Hilited     Default                 Menu With N MenuItems
     *          Unchecked   Unhilited   Normal (not default)
     *
     * @param state		state information retrieved from {@link User32#GetMenuState(NativeLong, int, int)}
     * @return			Space-delimited string of state information.
     * 					If the state code provided = {@link #MU_STATE_FAILURE} then return {@link #MUStateFailureString}
     */
    public static String MUGetMenuItemStateString(int state){
    	if(state==MU_STATE_FAILURE) return MUStateFailureString;
    	
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append(IsMenuItemEnabled(state)? "Enabled ": "Disabled ");
    	sb.append(IsMenuItemChecked(state)? "Checked ": "Unchecked ");
    	sb.append(IsMenuItemGrayed(state)? "Grayed ": "Ungrayed ");
    	sb.append(IsMenuItemHiLited(state)? "Hilited ": "Unhilited ");
    	sb.append(IsMenuItemDefault(state)? "Default ": "Normal ");
    	
    	sb.append(IsMenuItemABitmap(state)? "Bitmap ": "");
    	sb.append(IsMenuItemAMenuBreak(state)? "Break ": "");
    	sb.append(IsMenuItemAMenuBarBreak(state)? "BarBreak ": "");
    	sb.append(IsMenuItemAMenuSeparator(state)? "Separator ": "");
    	
    	//If menu item is a menu, count the menu items it contains.
    	//the high-order byte of state contains the number of items in the submenu
    	if(IsMenuItemAMenu(state)){
    		sb.append("Menu With "+(state>>8)+" MenuItems ");
    	}
    	
    	return sb.toString();
    }
    
    /**
     * 
     * DESCRIPTION:
     *
     *      Given a valid Menu handle and text string the routine attempts to
     *      return the state information for the menuitem.  
     *
     *      The menuText can optionally include the ampersand character that 
     *      normally precedes any underlined character in the menuitem.
     *
     * @param hMenu		The handle of the Menu
     * @param menuText	Case-sensitive text to identify the menuitem with
     * @return			state: Long status flag settings for the menuitem
     * 					{@link #MUStateFailureString} on failure.
     */
    public static long MUGetMenuItemTextState(long hMenu, String menuText){
    	int index = MUGetMenuItemTextIndex(hMenu,menuText);
    	if(index==-1) return MU_STATE_FAILURE;
    	
    	return User32.INSTANCE.GetMenuState(new NativeLong(hMenu), index, MF_BYPOSITION);
    }
    
    /**
     * 
     * DESCRIPTION:
     *
     *      Given a valid Menu handle and text string the routine attempts to
     *      locate the position within the menu for the associated menuitem.
     *      MenuItem text that contains one or more underlined characters is 
     *      represented in Windows by an ampersand (&) preceding each character 
     *      that is underlined.  Our text comparisons use these ampersands when 
     *      trying to match text as well.  This provides verification of the 
     *      visual cues given the user for what keyboard shortcuts can be used on
     *      the menuitems.
     *
     *      However, if the existence or position of the ampersand (the underlined 
     *      character ) is NOT important, the menuText string can be provided void 
     *      of ALL ampersand characters.  This routine will recognize that NO
     *      ampersands were provided and will strip the actual menuitem text of 
     *      ampersands when doing the comparison.  This may possibly result in 
     *      a false match (a match with the wrong menuitem), although this is
     *      probably unlikely.
     *      
     * @param hMenu		The handle of the Menu
     * @param menuText	Case-sensitive text to identify the menuitem with
     * @return			N  zero-based index into the associated menu for the item with matching text
     * 					-1 on failure.
     */
    public static int MUGetMenuItemTextIndex(long hMenu,  String menuText){
    	//validate input
    	if(menuText==null || menuText.equals("")){
    		Log.debug(CLASS_NAME+".MUGetMenuItemTextIndex(): menuText is null or void.");
    		return -1;
    	}
    	
    	//get the list of strings from the menu
    	List<String> items = MUGetMenuItemStrings(hMenu);
    	if(items==null || items.size()<1){
    		Log.debug(CLASS_NAME+".MUGetMenuItemTextIndex(): can not get menu items.");
    		return -1;
    	}
    	
    	//Decide whether or not to remove the "&" from the menuitems
    	boolean removeAmpersand = menuText.indexOf("&")<0;
    	String temp = null;
    	for(int i=0;i<items.size();i++){
    		if(removeAmpersand){
    			temp = items.get(i).replaceAll("&", "");
    		}else{
    			temp = items.get(i);
    		}
    		if(temp.equals(menuText)){
    			return i;
    		}
    	}
    	
    	return -1;
    }
    
    /**
     * DESCRIPTION:
     *
     *      Given a valid Menu handle and return a list of all the menuitems.
     *      
     * @param lhMenu	The handle of the Menu
     * @return			A list containing all the menuitems.
     * 					null if failure.
     */
    public static List<String> MUGetMenuItemStrings(long lhMenu){
    	if(lhMenu==0) return null;

    	List<String> items = new ArrayList<String>(10);
    	int count = 0;
    	
    	//allocate memory
    	Memory lpString = new Memory(MAX_MENU_ITEM_LEN);
    	int length = 0;
    	
    	NativeLong hMenu = new NativeLong(lhMenu);
    	count = User32.INSTANCE.GetMenuItemCount(hMenu);
    	if(count<0) return null;
    	
    	for(int i=0;i<count;i++){
    		length = User32.INSTANCE.GetMenuStringW(hMenu, i, lpString, MAX_MENU_ITEM_LEN-1, MF_BYPOSITION);
    		if(length > 0){
    			items.add(lpString.getString(0).trim());
    		}else if(length==0){
    			items.add("");
    		}else{
    			Log.debug(CLASS_NAME+".MUGetMenuItemStrings(): Can't get menu item string");
    		}
    	}
    	
    	//free the heap memory
    	lpString = null;
    	
    	return items;
    }
    
    /**
     * 
     * This routine is not publicized as it is basically a re-entrant routine used by
     * MUOutputMenuStructure to cycle through a menu structure.
     * 
     * @param hMenu		    The handle of the Menu	
     * @param recurse		false Output only the top level menu information
     *                      true  Recursively output ALL menu information	
     * @param showState		false Do not show any state information
     *                      true  Show the state string of each menuitem
     * @param level			Indicate the level of the menu item in the whole menu
     * 						This should be 0 for the first call.
     * @param contents		A list to contain the contents to be output to a file
     * 						If this is null, the contents will be output to console.
     */
    private static void OutputMenu(NativeLong hMenu, boolean recurse, boolean showState, int level, List<String> contents){
    	String debumsg = CLASS_NAME+".OutputMenu(): ";
    	final int indent = 4;
    	String spaces = "";
    	int menuCount = 0;
    	int textLen = 0;
    	int menuState = 0;
    	NativeLong subMenu = null;
    	StringBuffer sb = new StringBuffer(100);
    	Memory buffer = new Memory(MAX_MENU_ITEM_LEN);
    	String menuItemString = null;
    	
    	if(contents==null){
    		Log.debug(debumsg+" parameter contents is null, contents will be output to System console.");
    	}
    	
    	spaces = StringUtils.getSpaces(new Integer(level*indent));
    	menuCount = User32.INSTANCE.GetMenuItemCount(hMenu);
    	
    	for(int i=0;i<menuCount;i++){
    		sb.delete(0, sb.length());
    		sb.append(spaces);
    		
    		textLen = User32.INSTANCE.GetMenuStringW(hMenu, i, buffer, MAX_MENU_ITEM_LEN-1, MF_BYPOSITION);
    		if(textLen==0){
    			sb.append("<NO TEXT>");
    		}else{
    			menuItemString = String.valueOf(buffer.getCharArray(0, textLen));
    			Log.info(debumsg+textLen+" : "+menuItemString);
    			sb.append(menuItemString);
    		}
    		
    		sb.append(", ID="+User32.INSTANCE.GetMenuItemID(hMenu, i));
    		
    		menuState = User32.INSTANCE.GetMenuState(hMenu, i, MF_BYPOSITION);
    		if(showState){
    			sb.append(", "+MUGetMenuItemStateString(menuState));
    		}
    		if(contents!=null){
    			contents.add(sb.toString());
    		}else{
    			System.out.println(sb.toString());
    		}
    		//process submenus if this item is a menu
    		if(IsMenuItemAMenu(menuState) && recurse){
    			subMenu = User32.INSTANCE.GetSubMenu(hMenu, i);
    			MenuUtilities.OutputMenu(subMenu, recurse, showState, level+1, contents);
    		}
    	}
    	//Free the allocated memory
    	buffer = null;
    }
    
    /**
     * 
     * DESCRIPTION:
     *
     *      Given a valid handle the routine retrieves the menu structure, formats
     *      it as text, and saves it to a file or outputs it to the Console.  
     *      You can choose to append or overwrite the existing file (if any) and 
     *      you can choose to do only one level or the entire menu system.
     * 
     * @param lhWnd			handle of the Window
     * @param path			full path and name of file to use/make as output
     *                      if path="" the output is directed to the console
     * @param overwrite		false Append the file if it already exists
     *                      true  Overwrite the file if it already exists
     * @param recurse		false Output only the top level menu information
     *                      true  Recursively output ALL menu information
     * @param showState		false Do not show any state information
     *                      true  Show the state string of each menuitem
     * @param description	Optional text to place at start of output
     *                      This should be something which identifies which window
     *                      or which state of the application the snapshot was
     *                      taken from.  Without this, the output gives no indication
     *                      of where it came from.
     *                                         
     * @return              true when success.
     */
    public static boolean MUOutputMenuStructure(long lhWnd, String path, boolean overwrite, 
    		                                    boolean recurse, boolean showState, String description){
    	String debugmsg = CLASS_NAME+".MUOutputMenuStructure(): ";
    	List<String> menuContents = null;
    	BufferedWriter writer = null;
    	NativeLong hWnd = new NativeLong(lhWnd);
    	
    	if(lhWnd==0){
    		Log.debug(debugmsg+" hWnd is 0, it is NOT a valid handle!!!");
    		return false;
    	}

    	if(!User32.INSTANCE.IsWindow(hWnd)){
    		Log.debug(debugmsg+" hWnd is"+lhWnd+", it is NOT a window!!!");
    		return false;
    	}
    	
    	NativeLong hMenu = User32.INSTANCE.GetMenu(hWnd);
    	if(hMenu.longValue()==0){
    		Log.debug(debugmsg+" hMenu is 0, it is NOT a valid handle!!!");
    		return false;
    	}
    	
    	if(path!=null && !path.equals("")){
    		Log.info(debugmsg+" save menu contents to file "+path);
    		try {
    			writer = FileUtilities.getUTF8BufferedFileWriter(path,!overwrite);
    			menuContents = new ArrayList<String>();
    			if(description!=null){
    				menuContents.add(description);
    			}
			} catch (FileNotFoundException e) {
				Log.debug(debugmsg+e.getMessage()+". File not exist, output to console.");
			}
    	}
    	//Output the menucontents to an arraylist
    	MenuUtilities.OutputMenu(hMenu, recurse, showState, 0, menuContents);
    	if(menuContents!=null){
    		Log.info(debugmsg+"Menu contents is\n"+menuContents);
    		if(writer!=null){
    			try {
    				FileUtilities.writeCollectionToFile(writer, menuContents);
    			} catch (IOException e) {
    				Log.debug(debugmsg+e.getMessage());
    				return false;
    			}    			
    		}
    	}
    	return true;
    }
}
