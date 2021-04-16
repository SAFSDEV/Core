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
package org.safs.tools.ocr.gocr;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.image.ImageUtils;
import org.safs.tools.ocr.OCREngine;

/**
 * Extends OCREngine to support GOCR/JOCR engine.	http://jocr.sourceforge.net/
 * It requires JAI Imageio installed.
 *        http://java.sun.com/products/java-media/jai/INSTALL-jai_imageio_1_0_01.html#Windows
 *        https://jai-imageio.dev.java.net/binary-builds.html
 *
 * @author Junwu Ma
 * <br>	DEC 21, 2009    Original Release
 * <br> JAN 15, 2009    (JunwuMa) Adding support to use trained data as an alternative.
 * <br> JAN 27, 2009    (JunwuMa) Move runCommandLine() to its super. 
 * <br> FEB 25, 2010    (JunwuMa) Refactoring for OCR keywords.
 *
 */
public class GOCREngine extends OCREngine {
	
	/*
	  In GOCR 0.48, gocr.exe can be executed to recognize the text in an image.
	  Only PBM/PGM/PPM can be directly opened/used by it. To support an image like JPG,GIF,TIF,BMP..., 
	  The image is going to be converted to PNM at first, an then GOCR uses this PNM directly.
	   
	*/
	static public String TMP_PNM_ZOOMED         = "~temp.pnm"; 			 // converted pnm file for GOCR to use 
	static public String TMP_PNM_ORIGSIZE       = "~temporig.pnm";		 // pnm file with the same size as original image
	static public String UNKNOWN_CHAR           = "_";                   // string representing an unrecognized character
	static public String OPTION_UNKNOWN_CHAR    = " -u "+UNKNOWN_CHAR;   // gocr's option to output UNKOWN_CHAR for every unrecognized character.
	static public String OPTION_USE_EXTERNALDB  = " -m 258 ";            // 256+2 switch off internal engine and open external DB
	
	static public String OPTION_DATAPATH  		= " -p ";
	static public String VAR_TRAINDATA_PATH     = "GOCRDATA_DIR";        // a system variable storing the path of training data 

	
	public static String databasePathOption() {
		String datapath  = System.getenv("GOCRDATA_DIR");
		if (!datapath.endsWith(File.separator)) datapath += File.separator;
		return OPTION_DATAPATH + datapath;
	}	

	public GOCREngine() {
		super();
		//using 1.5 as default  
		setdefaultZoomScale((float)1.5);
	}

	/*override its super*/
	public String imageToText(BufferedImage image, String langId, Rectangle subarea, float zoom) throws SAFSException {
		String debugMsg = getClass().getName() + ".imageToText():";
		
		Log.info(debugMsg + " start...");
		BufferedImage targimg = zoomImageWithType(image, BufferedImage.TYPE_BYTE_GRAY, subarea, zoom);  
		try {
			Log.debug("try to output a pnm file for using in GOCR engine:" + TMP_PNM_ZOOMED);
			ImageUtils.saveImageToFile(targimg, new File(TMP_PNM_ZOOMED));
			Log.debug(TMP_PNM_ZOOMED + " converted! ");
			
		}catch(Exception ex){
			Log.debug("fail to output pnm file:" + TMP_PNM_ZOOMED + ex.toString());	
			throw new SAFSException(ex.toString());
		}
		
		
		//# four steps 
		//1)	Try to use internal engine by "gocr image"   ---- return text_internal
		//2)	If the returned string contains unidentified sub string like "_"; 
		//            goto step 3 try again with external trained data
		//3)	Using external trained data by "gocr -258 image" --- return text_exteranl 
		//4)    compare text_internal with text_external, to get the final string that has less unrecognized character
	
		// step 1
		String ExecutedCmd = "gocr.exe " + TMP_PNM_ZOOMED + OPTION_UNKNOWN_CHAR;
		String outputext = runCommandLine(ExecutedCmd);
		
		int countInternal = 0;
		String str = outputext;
		for(int pos, lenUnknownChar = UNKNOWN_CHAR.length();(pos = str.indexOf(UNKNOWN_CHAR))>=0; countInternal++) 
			str = str.substring(pos + lenUnknownChar);
		
		Log.debug("string without training:" + outputext + " unknown chars:" + countInternal);
		
		// step 2. see if outputext contains UNKNOWN_CHAR
		if (outputext.indexOf(UNKNOWN_CHAR)>= 0) {
			try {
				//create a pnm with original size 
				Log.info("try to output pnm:" + TMP_PNM_ORIGSIZE);
				BufferedImage imgForTraining = zoomImageWithType(image, BufferedImage.TYPE_BYTE_GRAY, subarea, 1);  
				ImageUtils.saveImageToFile(imgForTraining, new File(TMP_PNM_ORIGSIZE));
			}catch(Exception ex){
				Log.debug("fail to output the pnm file:" + TMP_PNM_ORIGSIZE + ex.toString());	
				throw new SAFSException(ex.toString());
			}
			//step 3
			String cmd = "gocr.exe " + TMP_PNM_ORIGSIZE + OPTION_UNKNOWN_CHAR + OPTION_USE_EXTERNALDB + databasePathOption();
			String strExternal = runCommandLine(cmd);
			str = strExternal;
			int countExternal = 0;
			for(int pos, lenUnknownChar = UNKNOWN_CHAR.length();(pos = str.indexOf(UNKNOWN_CHAR))>=0; countExternal++) 
				str = str.substring(pos + lenUnknownChar);
			//step 4
			if (countExternal < countInternal)
				outputext = strExternal;
			Log.debug("string with trained data:" + strExternal + " unknown chars:" + countExternal);
		}
		
		Log.info("Text found on image:" + outputext);
		return outputext;		
	}
	
	public static void main(String[] args){
		if (args.length == 0) {
			System.out.println(" no image file input, try again input a image file");
			return;
		}	
		String imagefile = args[0];
		String zoomStr = null;
		if (args.length == 2) 
			zoomStr = args[1];
		
		OCREngine gocr = new GOCREngine();
		float zoom;
		if (zoomStr == null)
			zoom = gocr.getdefaultZoomScale();
		else {
			zoom = (float)Double.parseDouble(zoomStr);
			if (zoom <= 0){
				System.out.println("Can't continue for zoomvalue<=0.");
				return;
			}
		}
		System.out.println(" trying to convert " + imagefile + ". zoomScale:" + zoom);
		
		try {
			String text = gocr.storedImageToText(imagefile, null, null,zoom);
			if (text == null)
				System.out.println("Error: fail to convert.");
			else
				System.out.println(" Text in image:\n "+ text);
		}catch(Exception ex){
			System.out.println(" image failed to be converted for exception: "+ ex.toString());
		}
	}

}
