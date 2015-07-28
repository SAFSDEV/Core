/** 
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.ocr.tesseract;


import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

/**
 * A JNA Library definition for the Windows tessdllWrapper.dll, which is used to call tessdll.dll to do OCR work.
 * tessdllWrapper.dll: A SAFS defined dll as interface to Tesseract OCR engine, tessdll.dll.
 * tessdll.dll:        Implemented by tesseract http://code.google.com/p/tesseract-ocr/
 *                     It used to depend on msvcr90.dll and msjava.dll. But now it is re-generated in VS2008 by SAFS 
 *                     and can be used independently. 
 * 
 * @author JunwuMa 
 * @see org.safs.tools.ocr.OCRWrapper
 * 
 * <br>	DEC 14, 2009    Original Release
 * 
 */
public interface TessdllWrapper extends StdCallLibrary {
	TessdllWrapper INSTANCE = (TessdllWrapper) Native.loadLibrary("tessdllWrapper", TessdllWrapper.class);
	
	/**
	 * Method in tessdllWrapper.dll for converting image data to text with supported language.
	 * 
	 * @param imageBuf   image data in byte[] format, java.awt.image.BufferedImage can return this format.
	 *                   Note: The image in the imageBuf is supposed to fit 300DPI, which is required by 
	 *                   tesseract-ocr. Images captured on screen normally are at low DPI like 72 or 96. Before 
	 *                   using them, remember to resize these images to fit in.
	 * 
	 * @param width      image width in pixel   
	 * @param height     image height in pixel
	 * @param bpp        bits per pixel
	 * @param langId     language id indicating which language it intends to convert the image to 
	 *                   "eng" for English  --- done
	 *                   "chn" for Chinese  --- todo
	 *                   ""    for
	 * @param configs    custom configure file [optional]
	 * @return text String that the input imageBuf is converted to
	 */
	String ImageBufferToText(byte[] imageBuf, int width, int height, int bpp, String langId, String configs);
	
	/**
	 * Method to convert an image file to text. 
	 * 
	 * @param imagefile, an image file. (only BMP and uncompressed TIF supported). It should be at 300DPI.
	 * @param langId
	 * @param configs
	 * @return
	 */
	String ImageFileToText(String imagefile, String langId, String configs);
}

