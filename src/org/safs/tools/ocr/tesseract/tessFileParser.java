/** 
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.ocr.tesseract;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.image.ReverseRectangle;
/**
 * Parses two types of Tesseract output data.
 * <p>
 * A UTF-8 file output by calling SafsTessdll.exe and, alternatively,  
 * a UTF-8 file output by Tesseract.exe.
 * <p>
 * The DLL format file is the result of running TOCR to recognize 
 * text in an image. As a text formated file, it contains not only every recognized letter but also its 
 * coordinates in the image. 
 * <p>
 * Command: SafsTessdll.exe  imagefile.gif  resultUTF-8.txt  eng 
 * Note: SafsTessdll.exe requires tessdll.dll. Both two files stay in C:\safs\bin.
 * <p>
 * Format of the UTF-8 from the DLL:
 * <pre>
 * ------------------------------------------------------------------------- 
 * |A[41](132,32)->(149,13)
 * |p[70](147,37)->(163,18)
 * |p[70](162,37)->(178,18)    ---- Char[Unicode](left, bottom)->(right, top)
 * |                           ---- \\ space 
 * |&lt;nl>                       ---- \\ new line
 * |&lt;para>                     ---- \\ paragraph
 * -------------------------------------------------------------------------
 * </pre>
 * <p>
 * <p>
 * The Tesseract.EXE format file is the result of running TOCR to recognize 
 * text in an image. As a text formated file, it contains not only every recognized letter but also its 
 * coordinates in the image. 
 * <p>
 * Command: tesseract.exe  scaledImageFile.tif  resultUTF8  -l eng nobatch|batch.nochop makebox 
 * <p>
 * Format of the UTF-8 from the EXE:
 * <pre>
 * ------------------------------------------------------------------------- 
 * |A 132 32 149 13
 * |p 147 37 163 18
 * |p 162 37 178 18     char left bottom right top
 * -------------------------------------------------------------------------
 * </pre>
 * <p>
 * Another caveat of the EXE output format is that coordinates are calculated assuming 
 * 0,0 is the bottom-left of image--NOT the top-left.  Thus, when converting to screen 
 * coordinates the rect.y value must be recalculated to be:
 * <p><ul> 
 * rect.y = image.height - rect.y
 * </ul>
 * <p>
 * In addition, versions of Tesseract before r344 (~May 19, 2010) had a bug in which 
 * the bounds were always calculated about 11 or 12 pixels off in the y coordinate.
 * <p>
 * While it sounds more complicated to use the tesseract.exe version, the accuracy 
 * of text recognition from this version is much higher than that of the DLL version.
 * <p>
 * @author JunwuMa 
 * <br>	MAR 12, 2010    Original Release
 * <br> MAR 26, 2010    (JunwuMa) Refactoring and update to support UTF-8 format.
 * <br> OCT 21, 2010    (Carl Nagle) Refactoring allowing tesseract.exe nobatch makebox coordinates.
 *
 */
public class tessFileParser {
	
	// Storing the file in tess-output format with full path or relative path under current path.
	private String    filepath = null;			
	
	// Storing the texts parsed from filepath -- as the texts found in image handled by TOCR.
	// If there is a new line in the texts, '\n' is added to the string instead.  
	private String    textstring="";        	 
	
	// Hashtable <key, Rectangle>, required to work with textstring. 
	// For each letter in textstring, its position is the key and its area is the Rectangle:
	//  key        -----  int, pos in textstring, started from 0
	//  Rectangle  -----  the area of the letter in image
	//  For a pair <key, value>, the vale is Rectangle(0,0,0,0); if the character at position (key) in textstring is " " or "\n"
	//  
	private Hashtable locationhash = new Hashtable();
	

	tessFileParser(String tessfile) {
		filepath = tessfile;
		parse();
	}
	/**
	 * tessfile must be a UTF formated text file output by SafsTessdll.exe.
	 * @param tessfile
	 */
	void setTessFile(String tessfile) {
		filepath = tessfile;
		parse();
	}
	String getText(){
		return textstring;
	}
	/**
	 * Get the area of the Nth instance of searchText that is sought in parsing result.
	 * It represents the area of searchText in image.
	 * <P>
	 * There are two modes in which the text coordinate information can be extracted.  
	 * The TessDLL mode and the Tesseract.exe mode.  This routine will detect which mode 
	 * was used and will decipher the coordinate information accordingly.
	 * <p>
	 * The TessDLL mode returns a normal Rectangle using the standard coordinate system of 
	 * 0,0 indicating the top-left corner of the search area and all coordinates are relative 
	 * to that top-left corner.
	 * <p>
	 * The Tesseract.exe mode returns a ReverseRectangle because the coordinate system puts 
	 * 0,0 at the bottom-left corner of the search area and all coordinates are relative to 
	 * that bottom-left corner.
	 *  
	 * @param searchText, string for which to search in tessFileParser, any leading and trailing whitespace will 
	 *                    be removed before seeking.
	 *                    
	 * @param index, starts from 1, specifies to find the Nth instance of searchText. Uses 1 if index<=0.
	 * The index of the text is the same regardless of the coordinate search mode that was used.
	 * 
	 * @return Rectangle, ReverseRectangle, or null. The area of searchText found; null if not found.
	 * If a ReverseRectangle is returned it is expected the user will transform\convert the Y coordinates 
	 * according to the height of the image\searchArea that was used.  For example, if the area 
	 * was 800 pixels high and the match was found at the "top" of the area at 1,1 then the 
	 * ReverseRectangle would contain coordinates 1,799.  To get the true 1,1 the user would have 
	 * to recalculate Y with 800-799 to get at 1,1.
	 */
	public Rectangle getTextArea(String searchText, int index){
		String debugMsg =  getClass().getName() + ".getTextArea():";
		if (index <= 0)
			index = 1;
		//remove leading and trailing whitespace
		//this step ensures the first and last char in searchText has non null Rectangle in locationhash if match
		searchText = searchText.trim(); 
		if (searchText.length() == 0)
			return null;
		
		int pos = 0;
		for (int i=0, cur=0; (pos = textstring.indexOf(searchText, i)) >= 0; ) {
			i = pos + searchText.length();
			if (++cur == index)
				break;	
		}
		
		if (pos >= 0) {
			Rectangle start = (Rectangle)locationhash.get(pos);
			Rectangle end   = (Rectangle)locationhash.get(pos + searchText.length()-1);
			
			Rectangle emptyRect = new Rectangle();
			if (start.equals(emptyRect) || end.equals(emptyRect))
				return null;
			else {
				int h1,h2,h,y;
				//this needs to be a better union of the 2 rectangle Y parameters
				//we also need to check for and accommodate the case of 0,0 at bottom-left
				//which occurs with the tesseract.exe mode of finding coordinates.
				Rectangle matchRect = null;//to be populated below
				if(start instanceof ReverseRectangle){// tesseract.exe 0,0 at bottom-left
					y = start.y > end.y ? start.y:end.y;// remember, ReverseRectangle
					h1=start.y - start.height;   //Ex: 800-10 = 790  ReverseRectangle
					h2=end.y - end.height;       //Ex: 795-10 = 785  ReverseRectangle
					h = h2 < h1 ? y - h2: y - h1;//Ex: 800-785= 15   ReverseRectangle  
					matchRect = new ReverseRectangle(start.x, y, end.x + end.width - start.x, h);				
				}else{// original tessdll way. 0,0 at top-left
					y = start.y < end.y ? start.y:end.y;// remember, normal Rectangle
					h1=start.y + start.height;   //Ex: 100+10  = 110  normal Rectangle
					h2=end.y + end.height;       //Ex: 105+10  = 115  normal Rectangle
					h = h2 > h1 ? h2-y: h1-y;    //Ex: 115-100 = 15   normal Rectangle  
					matchRect = new Rectangle(start.x, y, end.x + end.width - start.x, h);				
				}
				Log.debug(debugMsg + "the " +index+"th '"+ searchText + "' found! It is located at " + matchRect);
				return matchRect;
			}	
		} else
			return null;
	}
	
	// TessDLL Format Data:
	//
	// original line:   A[41](132,32)->(149,13) 
	// replace characters that meet regex with " "
	// after that there will be 4 tokens delimited with " " like:
	//                  A    41        132,32        149,13 
	//                  |     |           |            |
	//                 Char  Unicode  left,bottom   right,top
	//
	/** @return standard Rectangle format */
	private Rectangle parseTessDLLFormat(String line){
		int j = 0;
		int x1 = 0;
		int y1 = 0;
		int x2 = 0;
		int y2 = 0;
		for (StringTokenizer st = new StringTokenizer(line); st.hasMoreTokens(); j++) {
			String next = st.nextToken();
			if (j==0){ //1st token: Char
				// captured in calling routine
			}
			else if (j==1) {  //2sec token: Unicode
				// for using in future
			} 
			else { //j==2 or 3
				int delimitPos = next.indexOf(",");
				String sx = next.substring(0, delimitPos);
				String sy = next.substring(delimitPos+1);
				try {
					if (j==2){ //3rd token  left,bottom
						x1 = Integer.parseInt(sx);
						y1 = Integer.parseInt(sy);
					}else{     //4th token   right,top
						x2 = Integer.parseInt(sx);
						y2 = Integer.parseInt(sy);									
					}
				}catch (NumberFormatException nfe){}
			}	
		} //end of for
		// get the area for the character
		return new Rectangle(x1, y2, x2-x1, y1-y2);
	}
	
	// TessEXE Format Data:
	//
	// original line:   A 132 32 149 13 
	// 5 tokens delimited with " " like:
	//                  A    132    32     149    13 
	//                  |     |      |      |      |
	//                 Char  left  bottom  right  top
	//
	// note the coordinates returned are opposite of the TessDLL format in 
	// that 0,0 is considered at left,bottom.
	// thus, rect.y must be translated as rect.y = image.height - rect.y
	/** @return ReverseRectangle format */
	private Rectangle parseTessEXEFormat(String line){
		int x1 = 0;
		int y1 = 0;
		int x2 = 0;
		int y2 = 0;
		StringTokenizer st = new StringTokenizer(line);
		try{
			String next = st.nextToken(); //ignore first character
			next = st.nextToken(); //x1
			x1 = Integer.parseInt(next);
			next = st.nextToken(); //x1
			y1 = Integer.parseInt(next);
			next = st.nextToken(); //x1
			x2 = Integer.parseInt(next);
			next = st.nextToken(); //x1
			y2 = Integer.parseInt(next);
		}catch(Exception x){
			Log.debug("parseTessEXEFormat "+ x.toString());
			return new ReverseRectangle();
		}
		// get the area for the character
		// note y2 and y1 are "reversed" because the coordinates are 0,0 at bottom-left
		return new ReverseRectangle(x1, y2, x2-x1, y2-y1);
	}
	
	/*
	 * Parsing process --
	 * Takes filepath as input file.
	 * Variables affected: this.textstring this.locationhash
	 */
	private String parse() {
		String debugMsg =  getClass().getName() + ".run():";
		String text="";
		String reg = "\\(|\\)|->|\\[|\\]";  // regex for removing [, ] ,  ( , ), -> 
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF-8"));

			String buf="";
			int pos=0;
			Rectangle rect=null;
			for (String s; (s=reader.readLine())!=null;  pos++) {
				
				// check the first line, skip UTF-8 identifier if present
				if(pos == 0 && s.length() > 0){ 
					if ((Character.getType(s.charAt(0))==Character.FORMAT)&&
					    (Character.isIdentifierIgnorable(s.charAt(0)))){
						try{ s = s.substring(1);}
						catch(IndexOutOfBoundsException ix){
							s = "";
						}
					}
				}					
				if (s.length() == 0) {
					text += " ";
					rect = new Rectangle(); 
				} else if (s.indexOf("<nl>")>=0) {
			    	text += "\n";
					rect = new Rectangle(); 
				} else if (s.indexOf("<para>")>=0) {
			    	text += "\n";
					rect = new Rectangle(); 
				}	
				else {
					buf = s.charAt(0) + s.substring(1).replaceAll(reg, " ");
					text += buf.charAt(0);
					if(buf.indexOf(',')> 0)
						rect = parseTessDLLFormat(buf);
					else
						rect = parseTessEXEFormat(buf);
				}
				locationhash.put(pos, rect);
			} // end of for reader.readLine()	
		}catch(FileNotFoundException  fnfe){
			Log.debug(debugMsg + filepath + " not found. " + fnfe.toString());
		}catch(IOException  ie){
			Log.debug(debugMsg + ie.toString());
		}	
		textstring = text;
		return textstring;
	}
	/**
	 * Can be used to unit test.
	 * <p>
	 * java org.safs.tools.ocr.tesseract.tessFileParser [-f parseFile] [findtext]
	 * <p>
	 * parseFile - path to a test ~tempcoor.txt file to process.<br>
	 * findtext - a substring of text to find coordinates for.<br>
	 * @param args -- 3 array items: [-f parseFile] [findtext]
	 * parseFile - path to a test ~tempcoor.txt file to process.<br>
	 * findtext - a substring of text to find coordinates for.<br>
	 */
	public static void main(String[] args){
		String parseFile = "c:\\keywordTesting\\~tempcoor.txt";
		String findtext = "Submit";
		String arg = null;
		if((args != null)&&(args.length > 0)){
			try{
				for(int a=0;a<args.length;){
					arg=args[a++];					
					if(arg.equalsIgnoreCase("-f"))
						parseFile = args[a++];
					else
						findtext = arg;
				}
			}catch(Exception x){}			
		}
		tessFileParser parser = new tessFileParser(parseFile);
		Rectangle rect = parser.getTextArea(findtext, 1);
		if (rect != null)
			System.out.println("Got it:" + rect);
		else
			System.out.println("Did not find text location: null");
	}
}