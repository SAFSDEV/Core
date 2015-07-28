/** 
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.ocr;

import org.safs.Log;
import org.safs.tools.ocr.tesseract.TessdllWrapper;

import com.sun.jna.Platform;
/**
* This class is used to encapsulate platform independent calls to OCR SAFS supported through JNA.
* JAN is the Java Native Access library supplied via <a href="http://jna.dev.java.net" target="_blank">JNA Home</a> 
* SAFS is now delivered with the core JNA.ZIP(JAR).  
* 
* @author JunwuMa 
* <br>	DEC 14, 2009    Original Release
*/
public class OCRWrapper {
	/**
	 * See TessdllWrapper.ImageBufferToText for details.
	 */
	public static String imageToTextViaTesseract(byte[] imageBuf, int width, int height, int bpp, String langId, String config){
		if(Platform.isWindows()){
			String textout;			
			try{
				textout = TessdllWrapper.INSTANCE.ImageBufferToText(imageBuf, width, height, bpp, langId, config);
				if(textout == null){
					Log.debug("TessdllWrapper.ImageBufferToText error, received a NULL result");
					return null;
				}
				Log.info("TessdllWrapper.ImageBufferToText result: '"+ textout);
				return textout;
			}catch(Throwable x){
				Log.debug("OCRWrapper for OCR IGNORING "+ x.getClass().getSimpleName()+": "+ x.getMessage());
			}
		}
		return null;
	}
}
