/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.input;

import java.awt.event.KeyEvent;
import java.io.*;

/**
 * Create a Unicode output file for the running version of Java that can be used 
 * during testing and test development to map SAFS keystroke definitions to the 
 * Java keycodes needed by java.awt.Robot.
 * <p>
 * This class can be used to map the default US English 
 * character map and then may be copied and\or modified to generate keycode 
 * character maps for other locales or languages.  The generation of the US 
 * English map is already done and provided with the released package.
 * <p>
 * The class creates a file (SAFSKeycodeMap.dat) mapping text string sequences to the keycode 
 * used by the AWT Robot.  For example:
 * <p><ul>
 * ENTER=10
 * </ul>
 * <p> 
 * The example shows that the literal text "ENTER" will be mapped to keycode integer 
 * 10.  These keycodes are generally those defined in the java.awt.event.KeyEvent 
 * class.
 * <p>
 * SAFS text string sequences for commands like InputKeys are supported as follows.
 * In general, these are the same strings used by IBM Rational Robot and Microsoft 
 * standards for defining keystrokes in scripting languages:
 * <p><ul>
 * Some characters in the string are passed to the active window as literal characters, 
 * meaning that they are passed just as they appear in the string — for example, the 
 * letters a through z and the numbers 0 through 9. 
 * <p>
 * The following characters cause the associated keystroke to be performed:
 * <p><ul><pre>
 * ~	Causes the Enter key to be pressed.
 * +	Causes the Shift key to be pressed and held down while the next character 
 *      is pressed.
 * ^	Causes the Control key to be pressed and held down while the next character 
 *      is pressed.
 * %	Causes the Alt key to be pressed and held down while the next character is pressed.
 * </pre></ul>
 * <p>
 * If a group of characters is enclosed in parentheses, all the characters are affected 
 * by the special character that precedes the parentheses. For example, the following 
 * string inserts ABCD into the active window:
 * <p><ul>
 * "+(abcd)"
 * <p></ul>
 * Keys associated with non-printable characters (such as the Escape key and arrow keys) 
 * and keys on the numeric and extended keypads are represented by descriptive names in 
 * curly braces ( {} ). Names are not case-sensitive. The valid key names you can specify 
 * in curly braces are included in the table at the end of the Comments section.
 * <p>
 * To insert one of the above special characters — that is, ~+^%({ — as itself rather 
 * than as the special activity that it represents, enclose the character in curly 
 * braces. For example, the following command inserts a plus sign (+) into the active 
 * window:
 * <p><ul>
 * "{+}"
 * <p></ul>
 * Use the following table to determine the Keytext$ for the keyboard key you want:
 * <p><ul>
 * <table>
 * <tr>
 * <td>Keytext value	<td>Keyboard equivalent 
 * <tr><td>Actual printable character.<br>Examples:  A1.&
 * <td>Letters A–Z, a–z, numbers 0–9, punctuation, other printable characters on the main 
 * keyboard.
 * <tr><td>{Alt}<td>Default Alt key (either left or right).
 * <tr><td>{BackSpace}<br>{BS}<br>{BkSp}<td>	Backspace.
 * <tr><td>{Break}<td>Break or Pause.
 * <tr><td>{CapsLock}<td>Caps Lock.
 * <tr><td>{Clear}<td>Clear.
 * <tr><td>{Ctrl}<td>Default Control key (either left or right).
 * <tr><td>{Delete} or {Del} or {NumDelete} or {ExtDelete}<td>Delete.
 * <tr><td>{Down} or {NumDown} or {ExtDown}<td>Down Arrow.
 * <tr><td>{End} or {NumEnd} or {ExtEnd}<td>End.
 * <tr><td>{Enter} or ~ or {NumEnter} or {Num~}<td>Enter.
 * <tr><td>{Escape} or {Esc}<td>Escape.
 * <tr><td>{Help}<td>Help.
 * <tr><td>{Home} or {NumHome} or {ExtHome}<td>Home.
 * <tr><td>{Insert} or {NumInsert} or {ExtInsert}<td>Insert.
 * <tr><td>{Left} or {NumLeft} or {ExtLeft}<td>Left Arrow.
 * <tr><td>{NumLock}<td>Num Lock.
 * <tr><td>{PgDn} or {NumPgDn} or {ExtPgDn}<td>Page Down.
 * <tr><td>{PgUp} or {NumPgUp} or {ExtPgUp}<td>Page Up.
 * <tr><td>{PrtSc}<td>Print Screen.
 * <tr><td>{Right} or {NumRight} or {ExtRight}<td>Right Arrow.
 * <tr><td>{ScrollLock}<td>Scroll Lock.
 * <tr><td>{Shift}<td>Default Shift key (either left or right).
 * <tr><td>{Tab}<td>Tab.
 * <tr><td>{Up} or {NumUp} or {ExtUp}<td>Up Arrow.
 * <tr><td>{Numn}, where n is a number from 0 through 9 Example:  {Num5}<td>0-9 (numeric keypad).
 * <tr><td>{Num.} or .<td>. (period, decimal).
 *  <tr><td>{Num-} or -<td>- (dash, subtraction sign).
 * <tr><td>{Num*} or *<td>* (asterisk, multiplication sign).
 * <tr><td>{Num/} or /<td>/ (slash, division sign).
 * <tr><td>{Num+} or {+}<td>+ (addition sign).
 * <tr><td>{^}<td>^ (caret character).
 * <tr><td>{%}<td>% (percent character).
 * <tr><td>{~}<td>~ (tilde character).
 * <tr><td>{(}<td>( (left parenthesis character).
 * <tr><td>) or {)}<td>) (right parenthesis character).
 * <tr><td>{{}<td>{ (left brace character).
 * <tr><td>} or {}}<td>} (right brace character).
 * <tr><td>[<td>[ (left bracket character).
 * <tr><td>]<td>] (right bracket character).
 * <tr><td>{F#}<br>Example:  {F6}<td>F# (function keys 1-12).
 * <tr><td>+<br>Example:  +{F6}<td>Shift (used while pressing down another key).
 * <tr><td>^<br>Example:  ^{F6}<td>Control (used while pressing down another key).
 * <tr><td>%<br>Example:  %{F6}<td>Alt (used while pressing down another key).
 * <tr><td>{key n},<br>where key is any key, and n is the number of times that key is pressed.<br>
 * Example:  {a 10}<td>Repeats the key press n number of times.
 * </table></ul>
 * </ul>
 * 
 * @author Carl Nagle FEB 13, 2007
 * @see java.awt.Robot
 * @see java.awt.event.KeyEvent
 * @see org.safs.staf.service.keys.InputKeysParser
 * @see org.safs.tools.input.RobotKeyEvent
 */
public class CreateUnicodeMap {

	/** SAFSKeycodeMap */
	public static final String DEFAULT_FILE="SAFSKeycodeMap";
	/** .dat */
	public static final String DEFAULT_FILE_EXT=".dat";
	
	public static final String TOKENS = "TOKENS";
	public static final String STANDARD = "STANDARD";
	public static final String SPECIAL = "SPECIAL";

	public static final String BRACELEFT = "BRACELEFT";
	public static final String BRACERIGHT = "BRACERIGHT";
	public static final String PARENLEFT = "PARENLEFT";
	public static final String PARENRIGHT = "PARENRIGHT";
	public static final String ALT = "ALT";
	public static final String CONTROL = "CONTROL";
	public static final String SHIFT = "SHIFT";
	public static final String ENTER = "ENTER";
	
	
	private static String newLine = System.getProperty("line.separator");
	
	/**
	 * Return the current line separator used for file output.  By default 
	 * this is the System line separator.
	 * 
	 * @return  the current line separator used for file output.
	 */
	public static String getNewLine(){ return newLine;}
	/**
	 * Set a different file line separator.  By default the System default 
	 * line separator is used.
	 * 
	 * @param lineseparator -- the String to use for line separation in the 
	 * output file.
	 */
	public static void setNewLine(String lineseparator){
		newLine = lineseparator;
	}	
	
	public static void addEntry(BufferedWriter outfile, String mapstring, int keycode)
	                     throws IOException{
		outfile.write(mapstring +"="+ String.valueOf(keycode));
		outfile.write(newLine);
	}

	public static void addEntry(BufferedWriter outfile, String mapstring, String keycode)
						 throws IOException{
		outfile.write(mapstring +"="+ keycode);
		outfile.write(newLine);
	}

    public static void addEntry(BufferedWriter outfile, String schar)throws IOException{
    	try{
    	java.awt.AWTKeyStroke keyinfo = java.awt.AWTKeyStroke.getAWTKeyStroke(schar);
    	int code = keyinfo.getKeyCode();
    	int mods = keyinfo.getModifiers();
    	char c = keyinfo.getKeyChar();
    	String smods = KeyEvent.getKeyModifiersText(mods);
    	String smodsex = KeyEvent.getModifiersExText(mods);
    	String s = KeyEvent.getKeyText(code);
    	
    	outfile.write(schar +"={char: "+ s +", code: "+ code +", mods: "+ mods +", smods: "+ ", smodsex: "+smodsex +", "+ schar+"}");
    	outfile.write(newLine);
    	}catch(Exception x){
    		outfile.write(schar +": "+ x.getClass().getSimpleName());
    		outfile.write(newLine);
    	}
    }
    
	public static void main(String[] args) {
		output1(args);		
		//output2(args);//debugging only 
	}
	
	public static void output1(String[] args) {
		//open Unicode file for output
		try{
			BufferedWriter outfile = new BufferedWriter(new OutputStreamWriter
			                            (new FileOutputStream
			                            (new File(DEFAULT_FILE + DEFAULT_FILE_EXT)
			                             ), "UTF-8"
			                             ));
			                             
			//parsing tokens
			outfile.write(newLine);
			outfile.write("["+ TOKENS +"]");
			outfile.write(newLine);
			
			outfile.write(BRACELEFT +"={"+ newLine);
			outfile.write(BRACERIGHT +"=}"+ newLine);
			outfile.write(PARENLEFT +"=("+ newLine);
			outfile.write(PARENRIGHT +"=)"+ newLine);

			outfile.write(ALT +"=%"+ newLine);
			outfile.write(CONTROL +"=^"+ newLine);
			outfile.write(SHIFT +"=+"+ newLine);
			outfile.write(ENTER +"=~"+ newLine);

			outfile.write(newLine);

			addEntry(outfile, "%", KeyEvent.VK_ALT);
			addEntry(outfile, "^", KeyEvent.VK_CONTROL);
			addEntry(outfile, "+", KeyEvent.VK_SHIFT);
			addEntry(outfile, "~", KeyEvent.VK_ENTER);
						
			addEntry(outfile, "{", KeyEvent.VK_BRACELEFT);
			addEntry(outfile, "}", KeyEvent.VK_BRACERIGHT);
			addEntry(outfile, "(", KeyEvent.VK_LEFT_PARENTHESIS);
			addEntry(outfile, ")", KeyEvent.VK_RIGHT_PARENTHESIS);
			
			//standard keys
			outfile.write(newLine);
			outfile.write("["+ STANDARD +"]");
			outfile.write(newLine);
			
		    addEntry(outfile, "0", KeyEvent.VK_0);
			addEntry(outfile, "1", KeyEvent.VK_1);
			addEntry(outfile, "2", KeyEvent.VK_2);
			addEntry(outfile, "3", KeyEvent.VK_3);
			addEntry(outfile, "4", KeyEvent.VK_4);
			addEntry(outfile, "5", KeyEvent.VK_5);
			addEntry(outfile, "6", KeyEvent.VK_6);
			addEntry(outfile, "7", KeyEvent.VK_7);
			addEntry(outfile, "8", KeyEvent.VK_8);
			addEntry(outfile, "9", KeyEvent.VK_9);
			
			outfile.write(newLine);

			addEntry(outfile, "a", KeyEvent.VK_A);
			addEntry(outfile, "b", KeyEvent.VK_B);
			addEntry(outfile, "c", KeyEvent.VK_C);
			addEntry(outfile, "d", KeyEvent.VK_D);
			addEntry(outfile, "e", KeyEvent.VK_E);
			addEntry(outfile, "f", KeyEvent.VK_F);
			addEntry(outfile, "g", KeyEvent.VK_G);
			addEntry(outfile, "h", KeyEvent.VK_H);
			addEntry(outfile, "i", KeyEvent.VK_I);
			addEntry(outfile, "j", KeyEvent.VK_J);
			addEntry(outfile, "k", KeyEvent.VK_K);
			addEntry(outfile, "l", KeyEvent.VK_L);
			addEntry(outfile, "m", KeyEvent.VK_M);
			addEntry(outfile, "n", KeyEvent.VK_N);
			addEntry(outfile, "o", KeyEvent.VK_O);
			addEntry(outfile, "p", KeyEvent.VK_P);
			addEntry(outfile, "q", KeyEvent.VK_Q);
			addEntry(outfile, "r", KeyEvent.VK_R);
			addEntry(outfile, "s", KeyEvent.VK_S);
			addEntry(outfile, "t", KeyEvent.VK_T);
			addEntry(outfile, "u", KeyEvent.VK_U);
			addEntry(outfile, "v", KeyEvent.VK_V);
			addEntry(outfile, "w", KeyEvent.VK_W);
			addEntry(outfile, "x", KeyEvent.VK_X);
			addEntry(outfile, "y", KeyEvent.VK_Y);
			addEntry(outfile, "z", KeyEvent.VK_Z);

			outfile.write(newLine);

			addEntry(outfile, "A", "SHIFT+"+ KeyEvent.VK_A);
			addEntry(outfile, "B", "SHIFT+"+ KeyEvent.VK_B);
			addEntry(outfile, "C", "SHIFT+"+ KeyEvent.VK_C);
			addEntry(outfile, "D", "SHIFT+"+ KeyEvent.VK_D);
			addEntry(outfile, "E", "SHIFT+"+ KeyEvent.VK_E);
			addEntry(outfile, "F", "SHIFT+"+ KeyEvent.VK_F);
			addEntry(outfile, "G", "SHIFT+"+ KeyEvent.VK_G);
			addEntry(outfile, "H", "SHIFT+"+ KeyEvent.VK_H);
			addEntry(outfile, "I", "SHIFT+"+ KeyEvent.VK_I);
			addEntry(outfile, "J", "SHIFT+"+ KeyEvent.VK_J);
			addEntry(outfile, "K", "SHIFT+"+ KeyEvent.VK_K);
			addEntry(outfile, "L", "SHIFT+"+ KeyEvent.VK_L);
			addEntry(outfile, "M", "SHIFT+"+ KeyEvent.VK_M);
			addEntry(outfile, "N", "SHIFT+"+ KeyEvent.VK_N);
			addEntry(outfile, "O", "SHIFT+"+ KeyEvent.VK_O);
			addEntry(outfile, "P", "SHIFT+"+ KeyEvent.VK_P);
			addEntry(outfile, "Q", "SHIFT+"+ KeyEvent.VK_Q);
			addEntry(outfile, "R", "SHIFT+"+ KeyEvent.VK_R);
			addEntry(outfile, "S", "SHIFT+"+ KeyEvent.VK_S);
			addEntry(outfile, "T", "SHIFT+"+ KeyEvent.VK_T);
			addEntry(outfile, "U", "SHIFT+"+ KeyEvent.VK_U);
			addEntry(outfile, "V", "SHIFT+"+ KeyEvent.VK_V);
			addEntry(outfile, "W", "SHIFT+"+ KeyEvent.VK_W);
			addEntry(outfile, "X", "SHIFT+"+ KeyEvent.VK_X);
			addEntry(outfile, "Y", "SHIFT+"+ KeyEvent.VK_Y);
			addEntry(outfile, "Z", "SHIFT+"+ KeyEvent.VK_Z);

			outfile.write(newLine);

			addEntry(outfile, "`", KeyEvent.VK_BACK_QUOTE);
			addEntry(outfile, "~", "SHIFT+"+ KeyEvent.VK_BACK_QUOTE);
			addEntry(outfile, "!", "SHIFT+"+ KeyEvent.VK_1);//KeyEvent.VK_EXCLAMATION_MARK);
			addEntry(outfile, "@", "SHIFT+"+ KeyEvent.VK_2);//KeyEvent.VK_AT);
			addEntry(outfile, "#", "SHIFT+"+ KeyEvent.VK_3);//KeyEvent.VK_NUMBER_SIGN);			
			addEntry(outfile, "$", "SHIFT+"+ KeyEvent.VK_4);//KeyEvent.VK_DOLLAR);
			addEntry(outfile, "%", "SHIFT+"+ KeyEvent.VK_5);
			addEntry(outfile, "^", "SHIFT+"+ KeyEvent.VK_6);//KeyEvent.VK_CIRCUMFLEX);
			addEntry(outfile, "&", "SHIFT+"+ KeyEvent.VK_7);//KeyEvent.VK_AMPERSAND);
			addEntry(outfile, "*", "SHIFT+"+ KeyEvent.VK_8);//KeyEvent.VK_ASTERISK);
			addEntry(outfile, "(", "SHIFT+"+ KeyEvent.VK_9);//KeyEvent.VK_LEFT_PARENTHESIS);
			addEntry(outfile, ")", "SHIFT+"+ KeyEvent.VK_0);//KeyEvent.VK_RIGHT_PARENTHESIS);

			addEntry(outfile, "-", KeyEvent.VK_MINUS);
			addEntry(outfile, "_", "SHIFT+"+ KeyEvent.VK_MINUS);//KeyEvent.VK_UNDERSCORE);
			addEntry(outfile, "=", KeyEvent.VK_EQUALS);
			addEntry(outfile, "+", "SHIFT+"+ KeyEvent.VK_EQUALS);
			
			addEntry(outfile, "[", KeyEvent.VK_OPEN_BRACKET);
			addEntry(outfile, "{", "SHIFT+"+ KeyEvent.VK_OPEN_BRACKET);//KeyEvent.VK_BRACELEFT);
			addEntry(outfile, "]", KeyEvent.VK_CLOSE_BRACKET);
			addEntry(outfile, "}", "SHIFT+"+ KeyEvent.VK_CLOSE_BRACKET);//KeyEvent.VK_BRACERIGHT);
			addEntry(outfile, "\\", KeyEvent.VK_BACK_SLASH);
			addEntry(outfile, "|", "SHIFT+"+ KeyEvent.VK_BACK_SLASH);//KeyEvent.VK_SEPARATOR);
			addEntry(outfile, ";", KeyEvent.VK_SEMICOLON);
			addEntry(outfile, ":", "SHIFT+"+ KeyEvent.VK_SEMICOLON);//KeyEvent.VK_COLON);
			addEntry(outfile, "'", KeyEvent.VK_QUOTE);
			addEntry(outfile, "\"", "SHIFT+"+ KeyEvent.VK_QUOTE);//KeyEvent.VK_QUOTEDBL);
			addEntry(outfile, ",", KeyEvent.VK_COMMA);
			addEntry(outfile, "<", "SHIFT+"+ KeyEvent.VK_COMMA);//KeyEvent.VK_LESS);
			addEntry(outfile, ".", KeyEvent.VK_PERIOD);
			addEntry(outfile, ">", "SHIFT+"+ KeyEvent.VK_PERIOD);//KeyEvent.VK_GREATER);
			addEntry(outfile, "/", KeyEvent.VK_SLASH);
			addEntry(outfile, "?", "SHIFT+"+ KeyEvent.VK_SLASH);
			addEntry(outfile, "\" \"", KeyEvent.VK_SPACE);
			
			//special keys (inside braces)
			outfile.write(newLine);
			outfile.write("["+ SPECIAL +"]");
			outfile.write(newLine);

			addEntry(outfile, "ALT", KeyEvent.VK_ALT);
			addEntry(outfile, "ENTER", KeyEvent.VK_ENTER);
			addEntry(outfile, "SHIFT", KeyEvent.VK_SHIFT);
			addEntry(outfile, "CTRL", KeyEvent.VK_CONTROL);
			addEntry(outfile, "BACKSPACE", KeyEvent.VK_BACK_SPACE);
			addEntry(outfile, "BS", KeyEvent.VK_BACK_SPACE);
			addEntry(outfile, "BKSP", KeyEvent.VK_BACK_SPACE);
			addEntry(outfile, "BREAK", KeyEvent.VK_PAUSE);
			addEntry(outfile, "PAUSE", KeyEvent.VK_PAUSE);
			addEntry(outfile, "CAPSLOCK", KeyEvent.VK_CAPS_LOCK);
			addEntry(outfile, "CLEAR", KeyEvent.VK_CLEAR);
			addEntry(outfile, "DELETE", KeyEvent.VK_DELETE);
			addEntry(outfile, "DEL", KeyEvent.VK_DELETE);
			addEntry(outfile, "END", KeyEvent.VK_END);
			addEntry(outfile, "ESCAPE", KeyEvent.VK_ESCAPE);
			addEntry(outfile, "ESC", KeyEvent.VK_ESCAPE);
			addEntry(outfile, "F1", KeyEvent.VK_F1);
			addEntry(outfile, "F2", KeyEvent.VK_F2);
			addEntry(outfile, "F3", KeyEvent.VK_F3);
			addEntry(outfile, "F4", KeyEvent.VK_F4);
			addEntry(outfile, "F5", KeyEvent.VK_F5);
			addEntry(outfile, "F6", KeyEvent.VK_F6);
			addEntry(outfile, "F7", KeyEvent.VK_F7);
			addEntry(outfile, "F8", KeyEvent.VK_F8);
			addEntry(outfile, "F9", KeyEvent.VK_F9);
			addEntry(outfile, "F10", KeyEvent.VK_F10);
			addEntry(outfile, "F11", KeyEvent.VK_F11);
			addEntry(outfile, "F12", KeyEvent.VK_F12);
			addEntry(outfile, "F13", KeyEvent.VK_F13);
			addEntry(outfile, "F14", KeyEvent.VK_F14);
			addEntry(outfile, "F15", KeyEvent.VK_F15);
			addEntry(outfile, "F16", KeyEvent.VK_F16);
			addEntry(outfile, "F17", KeyEvent.VK_F17);
			addEntry(outfile, "F18", KeyEvent.VK_F18);
			addEntry(outfile, "F19", KeyEvent.VK_F19);
			addEntry(outfile, "F20", KeyEvent.VK_F20);
			addEntry(outfile, "F21", KeyEvent.VK_F21);
			addEntry(outfile, "F22", KeyEvent.VK_F22);
			addEntry(outfile, "F23", KeyEvent.VK_F23);
			addEntry(outfile, "F24", KeyEvent.VK_F24);
			addEntry(outfile, "HELP", KeyEvent.VK_HELP);
			addEntry(outfile, "HOME", KeyEvent.VK_HOME);
			addEntry(outfile, "INSERT", KeyEvent.VK_INSERT);
			addEntry(outfile, "PGDN", KeyEvent.VK_PAGE_DOWN);
			addEntry(outfile, "PGUP", KeyEvent.VK_PAGE_UP);
			addEntry(outfile, "PRTSC", KeyEvent.VK_PRINTSCREEN);
			addEntry(outfile, "SCROLLLOCK", KeyEvent.VK_SCROLL_LOCK);
			addEntry(outfile, "TAB", KeyEvent.VK_TAB);
			
			outfile.write(newLine);

			addEntry(outfile, "NUM/", KeyEvent.VK_DIVIDE);
			addEntry(outfile, "NUM*", KeyEvent.VK_MULTIPLY);
			addEntry(outfile, "NUM-", KeyEvent.VK_SUBTRACT);
			addEntry(outfile, "NUM+", KeyEvent.VK_ADD);
			addEntry(outfile, "NUM.", KeyEvent.VK_DECIMAL);
			addEntry(outfile, "NUM0", KeyEvent.VK_NUMPAD0);
			addEntry(outfile, "NUM1", KeyEvent.VK_NUMPAD1);
			addEntry(outfile, "NUM2", KeyEvent.VK_NUMPAD2);
			addEntry(outfile, "NUM3", KeyEvent.VK_NUMPAD3);
			addEntry(outfile, "NUM4", KeyEvent.VK_NUMPAD4);
			addEntry(outfile, "NUM5", KeyEvent.VK_NUMPAD5);
			addEntry(outfile, "NUM6", KeyEvent.VK_NUMPAD6);
			addEntry(outfile, "NUM7", KeyEvent.VK_NUMPAD7);
			addEntry(outfile, "NUM8", KeyEvent.VK_NUMPAD8);
			addEntry(outfile, "NUM9", KeyEvent.VK_NUMPAD9);

			outfile.write(newLine);

			addEntry(outfile, "LEFT", KeyEvent.VK_LEFT);
			addEntry(outfile, "NUMLEFT", KeyEvent.VK_KP_LEFT);
			addEntry(outfile, "EXTLEFT", KeyEvent.VK_KP_LEFT);
			addEntry(outfile, "RIGHT", KeyEvent.VK_RIGHT);
			addEntry(outfile, "NUMRIGHT", KeyEvent.VK_KP_RIGHT);
			addEntry(outfile, "EXTRIGHT", KeyEvent.VK_KP_RIGHT);
			addEntry(outfile, "UP", KeyEvent.VK_UP);
			addEntry(outfile, "NUMUP", KeyEvent.VK_KP_UP);
			addEntry(outfile, "EXTUP", KeyEvent.VK_KP_UP);
			addEntry(outfile, "DOWN", KeyEvent.VK_DOWN);
			addEntry(outfile, "NUMDOWN", KeyEvent.VK_KP_DOWN);
			addEntry(outfile, "EXTDOWN", KeyEvent.VK_KP_DOWN);

			outfile.write(newLine);

			addEntry(outfile, "NUMLOCK", KeyEvent.VK_NUM_LOCK);
			addEntry(outfile, "NUMENTER", KeyEvent.VK_ENTER);
			addEntry(outfile, "NUM~", KeyEvent.VK_ENTER);
			addEntry(outfile, "NUMDELETE", KeyEvent.VK_DELETE);
			addEntry(outfile, "EXTDELETE", KeyEvent.VK_DELETE);
			addEntry(outfile, "NUMINSERT", KeyEvent.VK_INSERT);
			addEntry(outfile, "EXTINSERT", KeyEvent.VK_INSERT);
			addEntry(outfile, "NUMEND", KeyEvent.VK_END);
			addEntry(outfile, "EXTEND", KeyEvent.VK_END);
			addEntry(outfile, "NUMHOME", KeyEvent.VK_HOME);
			addEntry(outfile, "EXTHOME", KeyEvent.VK_HOME);
			addEntry(outfile, "NUMPGDN", KeyEvent.VK_PAGE_DOWN);
			addEntry(outfile, "EXTPGDN", KeyEvent.VK_PAGE_DOWN);
			addEntry(outfile, "NUMPGUP", KeyEvent.VK_PAGE_UP);
			addEntry(outfile, "EXTPGUP", KeyEvent.VK_PAGE_UP);

			outfile.write(newLine);

			addEntry(outfile, "^", "SHIFT+"+ KeyEvent.VK_6);
			addEntry(outfile, "%", "SHIFT+"+ KeyEvent.VK_5);
			addEntry(outfile, "~", "SHIFT+"+ KeyEvent.VK_BACK_QUOTE);
			addEntry(outfile, "+", "SHIFT+"+ KeyEvent.VK_EQUALS);
			addEntry(outfile, "{", "SHIFT+"+ KeyEvent.VK_OPEN_BRACKET);
			addEntry(outfile, "}", "SHIFT+"+ KeyEvent.VK_CLOSE_BRACKET);
			addEntry(outfile, "(", "SHIFT+"+ KeyEvent.VK_9);
			addEntry(outfile, ")", "SHIFT+"+ KeyEvent.VK_0);

			//close file
			outfile.flush();
			outfile.close();	
		}
		catch(UnsupportedEncodingException x){
			System.out.println(x);
			x.printStackTrace();
		}
		catch(FileNotFoundException x){
			System.out.println(x);
			x.printStackTrace();
		}
		catch(IOException x){
			System.out.println(x);
			x.printStackTrace();
		}
	}
}
