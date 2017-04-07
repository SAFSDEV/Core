/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年3月31日    (SBJLWA) Initial release.
 */
package org.safs.autoit.test;

import java.io.IOException;

import org.safs.Constants.AutoItConstants;
import org.safs.SAFSException;
import org.safs.SAFSObjectRecognitionException;
import org.safs.autoit.AutoIt;
import org.safs.autoit.AutoItRs;
import org.safs.autoit.lib.AutoItLib;
import org.safs.autoit.lib.AutoItXPlus;

/**
 * @author sbjlwa
 *
 */
public class AutoItRsTest {
	private static void __engineMethods(AutoItXPlus it, String title, String text){

		System.out.println("title: "+title+" text: "+text);

		String tempStr = it.winGetClassList(title, text);
		System.out.println("winGetClassList=\n"+tempStr);

		tempStr = it.winGetHandle(title, text);
		System.out.println("winGetHandle="+tempStr);

		int tempInt = it.winGetPosX(title, text);
		System.out.println("winGetPosX="+tempInt);

		tempInt = it.winGetPosY(title, text);
		System.out.println("winGetPosY="+tempInt);

		tempInt = it.winGetPosWidth(title, text);
		System.out.println("winGetPosWidth="+tempInt);

		tempInt = it.winGetPosHeight(title, text);
		System.out.println("winGetPosHeight="+tempInt);

	}

	private static void test_engineMethods(AutoItXPlus it){

		String title = "Calculator";
		String text = "Degrees";

		System.out.println("\n======= TEST ENGINE METHODS =================\n");
		__engineMethods(it, title, text);

	}

	private static boolean __winWait(AutoItXPlus it, int mode, String title, String text){
		int secsWaitForWindow = 1;
		it.autoItSetOption(AutoItConstants.MODE_WIN_TITLE_MATCH, String.valueOf(mode));

		boolean found = it.winWait(title, text, secsWaitForWindow);
		System.out.println( (found?"FOUND\t\t":"NOT FOUND\t") +"mode: "+AutoItConstants.getTitleMatchMode(mode)+" title: "+title+" text: "+text);
		return found;
	}

	/**
	 * The mode "WinTitleMatchMode" will only affect window's title, NOT the window's text.
	 * The window's text will always be matched as a sub string.
	 * @throws IOException
	 */
	private static void test_mode(AutoItXPlus it){

		System.out.println("========================   Test  WinTitleMatchMode   =========================");
		int mode = AutoItConstants.MATCHING_PATIAL;
		String title = "Calculator";
		String text = "Degrees";//match as sub string

		title = "Calculator";
		text = "Degrees";
		mode = AutoItConstants.MATCHING_PATIAL;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_SUBSTRING;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_EXACT;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_ADVANCE;
		assert __winWait(it, mode, title, text);

		title = "Calculator";
		text = "reesNO";
		mode = AutoItConstants.MATCHING_PATIAL;
		assert !__winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_SUBSTRING;
		assert !__winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_EXACT;
		assert !__winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_ADVANCE;
		assert !__winWait(it, mode, title, text);

		title = "Calcul";
		text = "Deg";
		mode = AutoItConstants.MATCHING_PATIAL;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_SUBSTRING;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_EXACT;
		assert !__winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_ADVANCE;
		assert __winWait(it, mode, title, text);

		title = "Calcul";
		text = "grees";
		mode = AutoItConstants.MATCHING_PATIAL;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_SUBSTRING;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_EXACT;
		assert !__winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_ADVANCE;
		assert __winWait(it, mode, title, text);

		title = "lculator";
		text = "Degrees";
		mode = AutoItConstants.MATCHING_PATIAL;
		assert !__winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_SUBSTRING;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_EXACT;
		assert !__winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_ADVANCE;
		assert !__winWait(it, mode, title, text);

		//"Advanced Window Descriptions", such as "[TITLE:My Window; CLASS:My Class; INSTANCE:2]" will work on any mode.
		title = "[title:Calculator; ]";
		text = "";
		mode = AutoItConstants.MATCHING_PATIAL;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_SUBSTRING;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_EXACT;
		assert __winWait(it, mode, title, text);
		mode = AutoItConstants.MATCHING_ADVANCE;
		assert __winWait(it, mode, title, text);

		title = "[TITLE:Calculator; class:CalcFrame; ]";
		text = "Degrees";
		mode = AutoItConstants.MATCHING_DEFAULT;
		assert __winWait(it, mode, title, text);

		title = "[regexptitle:.*culat.*; regexpclass:[C|c]alcFrame; ]";
		assert __winWait(it, mode, title, text);

		title = "[regexptitle:.*culat.*; regexpclass:[K|k]alcFrame; ]";
		assert !__winWait(it, mode, title, text);

	}

	private static void testAutoItAPI(AutoItXPlus it){
		String title = "Calculator";
		String text = "Degrees";
		boolean found = false;
		int tries = 1;
		//Wait for the "Calculator" appear
		while(!found && tries<5){
			found = __winWait(it, AutoItConstants.MATCHING_PATIAL, title, text);
			tries++;
		}

		test_mode(it);
		test_engineMethods(it);
	}

	private static boolean __waitWin_autoItRs(AutoItXPlus it, AutoItRs rs, int secsWaitForWindow){

		if(!it.winWait(rs.getWindowsRS(), rs.getWindowText(), secsWaitForWindow)){
			System.out.println("\nNOT FOUND "+rs);
			return false;
		}else{
			System.out.println("\nFOUND "+rs);
			return true;
		}

	}

	private static void testAutoItRs(AutoItXPlus it){
		int secsWaitForWindow = 1;
		AutoItRs rs = null;

		String windowRS = "";
		String controlRS = "";

		System.out.println("\n=========================   TEST AutoItRs implementation =========================================");

		System.out.println("\n1.The recognition string is provided as SAFS RS\n");
		try {
			rs = new AutoItRs(windowRS, controlRS);
			assert false: "SAFSObjectRecognitionException should be thrown out.";
		} catch (SAFSObjectRecognitionException e) {
		}

		try {
			windowRS = "Title=Calculator;";
			controlRS = null;
			rs = new AutoItRs(windowRS, controlRS);
			assert __waitWin_autoItRs(it, rs, secsWaitForWindow);
		} catch (SAFSObjectRecognitionException e) {
			assert false: e.toString();
		}

		try {
			windowRS = "Title=Calc;";//beginning part will work
			controlRS = null;
			rs = new AutoItRs(windowRS, controlRS);
			assert __waitWin_autoItRs(it, rs, secsWaitForWindow);
		} catch (SAFSObjectRecognitionException e) {
			assert false: e.toString();
		}

		try {
			windowRS = "Title=lator;";//ending part will NOT work
			controlRS = null;
			rs = new AutoItRs(windowRS, controlRS);
			assert !__waitWin_autoItRs(it, rs, secsWaitForWindow);
		} catch (SAFSObjectRecognitionException e) {
			assert false: e.toString();
		}

		try {
			windowRS = "Caption=Calculator;Text=Degrees";
			controlRS = "";
			rs = new AutoItRs(windowRS, controlRS);
			assert __waitWin_autoItRs(it, rs, secsWaitForWindow);
		} catch (SAFSObjectRecognitionException e) {
			assert false: e.toString();
		}

		try {
			windowRS = ":AUTOIT:REGEXPCLASS=.*Fra.*";
			controlRS = "class=Button;instance=5";
			rs = new AutoItRs(windowRS, controlRS);

			assert __waitWin_autoItRs(it, rs, secsWaitForWindow);
			assert AutoItLib.activate(it, rs);

		} catch (SAFSException e) {
			assert false: e.toString();
		}

		try {
			System.out.println("\nTest SAFS rs key 'INDEX='");
			windowRS = ":AUTOIT:REGEXPCLASS=.*Fra.*";
			controlRS = "class=Button;index=5";
			rs = new AutoItRs(windowRS, controlRS);

			assert __waitWin_autoItRs(it, rs, secsWaitForWindow);
			assert AutoItLib.activate(it, rs);

		} catch (SAFSException e) {
			assert false: e.toString();
		}

		try {
			System.out.println("\nTest SAFS rs key 'CAPTION=' with wildcard. ");
			windowRS = "Caption=?alculator";
			controlRS = "class=Button;index=5";
			rs = new AutoItRs(windowRS, controlRS);

			assert __waitWin_autoItRs(it, rs, secsWaitForWindow);
			assert AutoItLib.activate(it, rs);

		} catch (SAFSException e) {
			assert false: e.toString();
		}

		try {
			System.out.println("\nTest SAFS rs key 'CAPTION=' with wildcard.");
			windowRS = "Caption=*Calcul?t*";
			controlRS = "class=Button;index=5";
			rs = new AutoItRs(windowRS, controlRS);

			assert __waitWin_autoItRs(it, rs, secsWaitForWindow);
			assert AutoItLib.activate(it, rs);

		} catch (SAFSException e) {
			assert false: e.toString();
		}

		try {
			System.out.println("\nTest SAFS rs key 'CAPTION=' with wildcard.");
			windowRS = "Caption=?Calcul?t*";
			controlRS = "class=Button;index=5";
			rs = new AutoItRs(windowRS, controlRS);

			assert !__waitWin_autoItRs(it, rs, secsWaitForWindow);
			assert !AutoItLib.activate(it, rs);

		} catch (SAFSException e) {
			assert false: e.toString();
		}

		System.out.println("\n2.The recognition string is provided directly as engine RS\n");
		//the engine format RS is also accepted
		try {
			windowRS = ":AUTOIT:[Title:Calculator;]";
			controlRS = "";
			rs = new AutoItRs(windowRS, controlRS);
			//window text should be set separately
			rs.setWindowText("Degrees");
			assert __waitWin_autoItRs(it, rs, secsWaitForWindow);
		} catch (SAFSObjectRecognitionException e) {
			assert false: e.toString();
		}

		try {
			windowRS = ":AUTOIT:REGEXPCLASS=.*Fra.*";
			controlRS = "[class:Button;instance:5]";
			rs = new AutoItRs(windowRS, controlRS);

			assert __waitWin_autoItRs(it, rs, secsWaitForWindow);
			assert AutoItLib.activate(it, rs);
		} catch (SAFSException e) {
			assert false: e.toString();
		}

	}

	public static void main(String[] args){
		AutoItXPlus it = AutoIt.AutoItObject();

		long starttime = System.currentTimeMillis();
		Process process = null;
		try {
			//Open a "Calculator"
			process = Runtime.getRuntime().exec("calc.exe");
//			testAutoItAPI(it);

			testAutoItRs(it);

		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(process!=null) process.destroy();
			System.out.println("\n====== Time Consummed: "+(System.currentTimeMillis()-starttime)+" milliseconds. =======");
		}
	}
}
