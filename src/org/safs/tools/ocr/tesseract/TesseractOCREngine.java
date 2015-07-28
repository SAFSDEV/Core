/** 
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.ocr.tesseract;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.image.ImageUtils;
import org.safs.image.ReverseRectangle;
import org.safs.tools.ocr.OCREngine;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Extends OCREngine to support Tesseract OCR engine.
 * 		http://code.google.com/p/tesseract-ocr/
 * 		http://groups.google.com/group/tesseract-ocr
 *   
 *   Tesseract 2.04 provides two ways for using 
 *   1) Running tesseract.exe in command line directly. Usage: tesseract.exe <image.tif> <outputbase> [-l lang] [configfile]
 *   2) Tesseract releases tessdll.dll for developers to call, dlltest.exe is released for testing tessdll.dll.
 *   Files tesseract.exe, tessdll.dll and dlltest.exe can be downloaded, or generated from the released code.
 *   
 *   SAFS used to take the second way -- SAFS calls tessdllWrapper.dll(SAFS defined), which talks with tessdll.dll.   
 *   Some experiments showed tesseract.exe seemed much better than tessdll.dll in detecting text on images.
 *   Not sure if they use the same parts of code to do the work. 
 *   
 *   Just take the first way -- running tesseract.exe directly. 
 *   
 *   Two files will be output in current user directory if call imageToText()
 *   1. ~temp.tif  scaled image for Tesseract to recognize.
 *   2. ~temp.txt  text file storing detected text in the image.
 *    
 *   Three files required in searching path:
 *   tesseract.exe          --- Command: tesseract imagefile outfile -l eng
 *   SafsTessdll.exe        --- Command: SafsTessdll imagefile outfile eng
 *   tessdll.dll            --- needed by SafsTessdll.exe 
 *   
 *   SafsTessdll.exe was built by SAFS and newly added for findTextRectFromImage().  
 *   It outputs a UTF-8 file that contains detected character, their Unicode and their coordinates.
 *   <p>
 *   We have added direct tesseract.exe support for findTextRectFromImage().
 *   The output format of this file is different and uses a different coordinate system 
 *   then the SAFS DLL, but it provides greater text recognition accuracy for better 
 *   matching and locating text.
 *   <p>
 *   If a version other than Tesseract 2.04 is installed, the environment variable TESSDATA_VERSION 
 *   should be set.  Ex: TESSDATA_VERSION=3.4.4<br>
 *   We don't know of any  other means to deduce the version of tesseract installed.
 *   <p>
 * @author Junwu Ma
 * <br>	DEC 14, 2009    Original Release
 * <br> JAN 27, 2010	(JunwuMa) Modified imageToText() to call tesseract.exe directly for recognition accuracy.
 * <br> FEB 25, 2010    (JunwuMa) Add method getSelfDefinedLangId().
 * <br> MAR 19, 2010    (JunwuMa) Added method findTextRectFromImage() to support mode "ImageText=" in ImageUtils.java.
 * <br> MAR 25, 2010    (JunwuMa) Modified imageToText() to open the temporary file in proper format, UTF-8.
 * <br> APR 20, 2010    (LeiWang) Add a map languages to contain pairs (javaLangCode, OCRLangCode).
 *                                Modify method getSelfDefinedLangId(): get OCRLangCode from map languages.
 * <br> MAY 27, 2010    (CANAGL) changed temp output directory to System property "java.io.tmpdir"
 * <br> OCT 22, 2010    (CANAGL) added support for tesseract.exe text coordinate extraction
 *                               added support to detect System Environment Variable TESSDATA_VERSION to
 *                               detect versions of Tesseract > 2.04.
 * @see tessFileParser
 * @see org.safs.image.ReverseRectangle                              
 */
public class TesseractOCREngine extends OCREngine {
	/** "eng" */
	public static final String LANG_ENG = "eng";
	/** "chi" */
	public static final String LANG_CHN = "chi";
	/** "jpn" */
	public static final String LANG_JPN = "jpn";
	/** "kor" */
	public static final String LANG_KOR = "kor";
	/** "fra" */
	public static final String LANG_FRA = "fra";
	
	/** "java.io.tmpdir" */
    static public String TMP_DIR_PROPERTY = "java.io.tmpdir"; //was "user.dir"
	/** "~temp.tif" */
	static public String TMP_TIF_SCALEDED = "~temp.tif";
	//file containing detected text. It is automatically suffixed with ".txt" as the output of calling tesseract.exe
	/** "~temp" */
	static public String TMP_TEXT_OUTPUT  = "~temp";        
	//file containing detected text and their coordinates. It is output by calling tessdll.dll with SafsTessdll.exe
	/** "~tempcoor" */
	static public String TMP_TEXT_COOR_ROOT  = "~tempcoor";   // 
	/** "~tempcoor.txt" */
	static public String TMP_TEXT_COOR_OUTPUT  = "~tempcoor.txt";   // 
	/** 
	 * Coded default to "2.04"
	 * Static initializers look for System Environment variable "TESSDATA_VERSION" to change this. 
	 * We have not found another way to deduce the installed version of Tesseract.
	 */
	static public String TESSERACT_VERSION = "2.04"; //set default we started using
	
	static public HashMap<String,String> languages = new HashMap<String,String>();
	static{
		languages.put(Locale.ENGLISH.getLanguage(), LANG_ENG);
		languages.put(Locale.CHINESE.getLanguage(), LANG_CHN);
		languages.put(Locale.JAPANESE.getLanguage(), LANG_JPN);
		languages.put(Locale.KOREAN.getLanguage(), LANG_KOR);
		languages.put(Locale.FRANCE.getLanguage(), LANG_FRA);
		try{
			String v = System.getenv("TESSDATA_VERSION");
			if(v != null) TESSERACT_VERSION = v;
		}catch(Exception x){}
	}

	/** Mode value to use SAFSTESSDLL wrapper to locate coordinates of image text.*/ 
	public static final int TEXT_FIND_TESSDLL_MODE = 0;
	/** Mode value to use tesseract.exe directory to locate coordinates of image text.*/ 
	public static final int TEXT_FIND_TESSEXE_MODE = 1;
	/** 
	 * Set to desired mode for locating coordinates of text.<br>
	 * TEXT_FIND_TESSDLL_MODE was the original mechanism used.  However, this mechanism 
	 * was found to lack the accuracy of text recognition that tesseract.exe has.
	 * <p>
	 * Possible values: TEXT_FIND_TESSDLL_MODE, and the default TEXT_FIND_TESSEXE_MODE.*/ 
	public static int TEXT_FIND_MODE = TEXT_FIND_TESSEXE_MODE;
	
	
	public TesseractOCREngine() {
		super();
		// 1.9 is ok for normal screen-captured pictures to fit 300DPI in tesseract 
		setdefaultZoomScale((float)1.9);
	}
	
	/*override its super*/
	public String imageToText(BufferedImage image, String langId, Rectangle subarea, float zoom) throws SAFSException {
		String debugMsg = getClass().getName() + ".imageToText():";
		Log.info(debugMsg + " start...");		
		BufferedImage targimg = zoomImageWithType(image, BufferedImage.TYPE_BYTE_GRAY, subarea, zoom);  //TYPE_BYTE_GRAY  TYPE_BYTE_BINARY
		
		String curdir = System.getProperty(TMP_DIR_PROPERTY);// was "user.dir"
		String fileScaledTIF = curdir + TMP_TIF_SCALEDED;
		
		//create a temporary tif for tesseract.exe to call
		try {
			Log.debug("try to output a tif file for using in TessOCR engine:" + fileScaledTIF);
			ImageUtils.saveImageToFile(targimg, new File(fileScaledTIF));
			Log.debug(fileScaledTIF + " converted! ");
			
		}catch(Exception ex){
			Log.debug(debugMsg + "fail to output tif:" + fileScaledTIF + ex.toString());	
			throw new SAFSException(ex.toString());
		}
				
		langId = getSelfDefinedLangId(langId);
		
		String fileOutput = curdir + TMP_TEXT_OUTPUT;
		//call tesseract.exe to detect the temporary tif
		String cmd = "tesseract.exe " + fileScaledTIF + " " + fileOutput + " -l " + langId;
		Log.info(debugMsg+" "+cmd);
		runCommandLine(cmd);
		
		//get the text from TMP_TEXT_OUTPUT.txt
		String text="";
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileOutput+".txt"), "UTF-8"));
			for (String s; (s=reader.readLine())!=null; text += "\n" )
				text += s;
		}catch(FileNotFoundException  fnfe){
			Log.debug(debugMsg + fileOutput + ".txt not found");
			throw new SAFSException(fnfe.toString());
		}catch(IOException  ie){
			Log.debug(debugMsg + ie.toString());
			throw new SAFSException(ie.toString());
		}	
		//remove suffix '\n'
		while (text.endsWith("\n")) text = StringUtilities.removeSuffix(text, "\n");
		
		Log.info("text found on image:" + text);
		return text;
	}	
	/**
	 * Override its super.
	 * Translate the standard language code to OCR specific language code.
	 * It should be overridden in derived classes. 
	 * Refer to Locale.ENGLISH.getLanguage() for input langid. For example: 'en' -- English
	 * @param langId,  standard language code
	 * @return
	 */
	protected String getSelfDefinedLangId(String StdlangId) {
		String lang = null;
		
		lang = languages.get(StdlangId);
		
		Log.debug("For StdlangId="+StdlangId+" ; lang="+lang+" is got from languages: "+languages);
		
		if(lang!=null){
			if(! (lang.equalsIgnoreCase(LANG_ENG)||
			      lang.equalsIgnoreCase(LANG_CHN))){
				Log.warn(lang + " not supported");
			}
		}else{
			Log.info(StdlangId + "not found as a key");
			String[] langs = new String[languages.size()];
			langs = languages.values().toArray(langs);
			for(int i=0;i<langs.length;i++){
				if(langs[i].equalsIgnoreCase(StdlangId)){
					lang = StdlangId;
					break;
				}
			}
			
			if(lang==null){
				Log.info(StdlangId + " not found as a value, use English as default: " + LANG_ENG);
				lang = LANG_ENG;
			}
		}
		
		Log.info("Tesseract OCR will use "+lang+" as the language parameter.");
		
		return lang;
	}
	
	/**
	 * Find text from BufferedImage, and return its area in the BufferedImage. 
	 * 
	 * Two modes of operation are possible based on the TEXT_FIND_MODE setting.
	 * <p><pre>
	 * TESSDLL Mode:
	 * Two files SafsTessdll.exe and tessdll.dll will be used.
	 * Two files will be output in current user's Temp directory.
	 * 1. ~temp.tif  scaled image
	 * 2. ~tempcoor.txt text file storing detected text and their coordinates.
	 *    Rectangle coordinates 0,0 relative to TOP-LEFT corner of search area.
	 * </pre>
	 * <p><pre>
	 * TESSEXE Mode:
	 * tesseract.exe will be used.
	 * Two files will be output in current user's Temp directory.
	 * 1. ~temp.tif  scaled image
	 * 2. ~tempcoor.txt text file storing detected text and their coordinates.
	 *    ReverseRectangle coordinates 0,0 relative to BOTTOM-LEFT corner of search area.
	 * </pre>
	 * 
	 * @param searchtext, text for which to search
	 * @param index, starts from 1, specifies to find the Nth instance of searchText.
	 * @param image, source BufferedImage for detecting
	 * @param stdlangId, standard language id with which TOCR intends to detect. Refer to Locale.ENGLISH.getLanguage(). 
	 * @param subarea
	 * @param zoom
	 * @return Rectangle or ReverseRectangle or null.
	 * @throws SAFSException
	 * @see {@link #TEXT_FIND_MODE}
	 * @see ReverseRectangle
	 */
	public Rectangle findTextRectFromImage(String searchtext, int index, BufferedImage image, String stdlangId, Rectangle subarea, float zoom) throws SAFSException {
		//TMP_TEXT__COOR_OUTPUT will be generated
		String debugMsg = getClass().getName() + ".findTextRectFromImage():";
		Log.info(debugMsg + " start...");		
		BufferedImage targimg = zoomImageWithType(image, BufferedImage.TYPE_BYTE_GRAY, subarea, zoom);  //TYPE_BYTE_GRAY  TYPE_BYTE_BINARY
		
		String curdir = System.getProperty(TMP_DIR_PROPERTY);//was "user.dir"
		String fileScaledTIF = curdir + TMP_TIF_SCALEDED;
		
		//create a temporary tif for dlltest.exe to call
		try {
			Log.debug("try to output a tif file for using in TessOCR engine:" + fileScaledTIF);
			ImageUtils.saveImageToFile(targimg, new File(fileScaledTIF));
			Log.debug(fileScaledTIF + " converted! ");
			
		}catch(Exception ex){
			Log.debug(debugMsg + "fail to output tif:" + fileScaledTIF + ex.toString());	
			throw new SAFSException(ex.toString());
		}
				
		String langId = getSelfDefinedLangId(stdlangId);
		
		//fileOutput will receive detected text and their coordinates
		String fileOutput = null;		
		String cmd = null;
		if(TEXT_FIND_MODE == TEXT_FIND_TESSDLL_MODE){//original mode with normal Rectangles
			//call SafsTessdll.exe to detect the temporary tif
			fileOutput = curdir + TMP_TEXT_COOR_OUTPUT;
			cmd ="SafsTessdll.exe " + fileScaledTIF + " " + fileOutput + " " + langId;
		}else{ //tessexe_mode returning ReverseRectangles
			//call tesseract.exe to detect the temporary tif
			fileOutput = curdir + TMP_TEXT_COOR_ROOT;//exe will append .txt extension
			cmd = "tesseract.exe " + fileScaledTIF + " " + fileOutput + " -l " + langId +" batch.nochop makebox";
			fileOutput = curdir + TMP_TEXT_COOR_OUTPUT;//revert to full path for parser
		}
		runCommandLine(cmd);
		
		
		//parse the output file
		tessFileParser parser = new tessFileParser(fileOutput);
		Rectangle rect = parser.getTextArea(searchtext, index);
		
		//tesseract.exe mode has the y-coords 0,0 at bottom--left not top-left.
		if(rect instanceof ReverseRectangle){
			int h = targimg.getHeight();
			rect = new Rectangle(rect.x, h-rect.y, rect.width, rect.height);
			//tesseract 2.x before tess release r344 had y-coords off by ~11-12 pixels
			if(TESSERACT_VERSION.startsWith("2")){
				rect.y = rect.y + 5;//offset defect in tesseract 2.x 
			}
		}
		Log.debug(debugMsg+"tessdll =" + parser.getText());
		//System.out.println("tessdll =" + parser.getText());
		return rect;
		
	}
	private Rectangle testfindTextRect(String imagefile, String searchingText, String lang, float zoom) {
		Rectangle rect = null;
		try {
			BufferedImage bImage = ImageUtils.getStoredImage(imagefile);
			rect = findTextRectFromImage(searchingText, 1, bImage, lang, null, zoom);
		}catch(Exception ie){
			Log.debug("Fail to get image " + imagefile);
		}	
		return rect;
	}
	
	//Usage: java TesseractOCREngine imageFile [-z zoom] [-l lang] [-t text]
	/**
	 * Can be used to unit test.
	 * <p>
	 * java org.safs.tools.ocr.tesseract.TesseractOCREngine imageFile [-z zoom] [-l lang] [-t text]
	 * <p>
	 * imageFile - path to screen captured image to process.<br>
	 * zoom - zoom level to use for OCR. Defaults to 1.9.<br>
	 * lang - locale to use for OCR. Ex: "en"<br>
	 * text - text to locate in image.<br>
	 * @param args -- up to 7 array items: imageFile [-z zoom] [-l lang] [-t text]
	 * imageFile - (required) path to screen captured image to process.<br>
	 * zoom - zoom level to use for OCR. Defaults to 1.9.<br>
	 * lang - locale to use for OCR. Ex: "en". Defaults to "en".<br>
	 * text - text to locate in image.<br>
	 */
	public static void main(String[] args){
		OCREngine ocr = null;
		try {
			ocr = OCREngine.getOCREngine(OCR_T_ENGINE_KEY,null);
		} catch (SAFSException e) {
			System.out.println("Can not get the ocr engine.");
			return;
		}
		if (args.length == 0) {
			System.out.println(" no image file input, try again input a image file");
			System.out.println(" Usage: java TesseractOCREngine imageFile [-z zoom] [-l lang] [-t text]");
			return;
		}	
		
		String imagefile = args[0];
		String zoomStr = null;
		String langId = null;
		String textrect = null;
		//Analyze other parameters
		for(int i=1;i<args.length;i++){
			if(args[i].equalsIgnoreCase("-z")){
				if(i+1<args.length){
					zoomStr = args[++i];
				}else{
					System.out.println(" you should specify zoom value after -z.");
				}
			}else if(args[i].equalsIgnoreCase("-l")){
				if(i+1<args.length){
					langId = args[++i];
				}else{
					System.out.println(" you should specify language value after -l.");
				}
			}else if(args[i].equalsIgnoreCase("-t")){
				if(i+1<args.length){
					textrect = args[++i];
				}else{
					System.out.println(" you should specify text to locate after -t.");
				}
			}
		}
		
		float zoom;
		if (zoomStr == null)
			zoom = ocr.getdefaultZoomScale();
		else {
			zoom = (float)Double.parseDouble(zoomStr);
			if (zoom <= 0){
				System.out.println("Can't continue for zoomvalue<=0.");
				return;
			}
		}
		
		if(langId==null){
			langId = Locale.ENGLISH.getLanguage();
		}
		System.out.println(" trying to convert " + imagefile + "  in "+langId+". zoomScale:" + zoom);
		try {
			String text = ocr.storedImageToText(imagefile, langId, null, zoom);
			if (text == null)
				System.out.println("Error: fail to convert.");
			else
				System.out.println(" Text in image:\n "+ text);
		}catch(Exception ex){
			System.out.println(" image failed to be converted for exception: "+ ex.toString());
		}
		
		if(textrect!=null){
			Rectangle rect = ((TesseractOCREngine)ocr).testfindTextRect(imagefile,textrect,langId, zoom);
			System.out.println(" For text "+textrect+" location is "+rect);
		}
	}
}
