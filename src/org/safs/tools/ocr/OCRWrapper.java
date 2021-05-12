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
