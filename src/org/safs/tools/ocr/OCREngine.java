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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.STAFHelper;
import org.safs.image.ImageUtils;
import org.safs.tools.ocr.gocr.GOCREngine;
import org.safs.tools.ocr.tesseract.TesseractOCREngine;

/**
 * An abstract class to define common behaviors of OCR engines.
 * The concrete OCR engines need to derive from this class, and implement the common behaviors in it.
 * 
 * @see org.safs.tools.ocr.tesseract.TesseractOCREngine
 * @see org.safs.tools.ocr.gocr.GOCREngine
 *   
 * @author Junwu Ma
 * <br>	DEC 14, 2009    Original Release
 * <br>	DEC 22, 2009    (JunwuMa) Updates to make it work for GOCR newly introduced. 
 * <br>	JAN 27, 2009    (JunwuMa) Add method runCommandLine(String).    
 * <br>	JAN 29, 2009    (JunwuMa) Updates to use BICUBIC interpolation to resize images for better effect. 
 * <br> FEB 25, 2010    (JunwuMa) Refactoring for OCR keywords.
 * <br> APR 20, 2010    (Lei Wang) Add static method getOCREngine(): get the OCREngine instance according 
 *                                to the given engine name (by parameter or STAF variable).
 *                                Add static method getOCREngineKey(), setOCREngineKey(), getOCRLanguageCode(),
 *                                setOCRLanguageCode(): get or set STAF variables
 */
public abstract class OCREngine {
	
	public static String STAF_OCR_ENGINE_VAR_NAME		= "STAF_OCR_ENGINE_VAR_NAME";//TOCR or GOCR
	public static String STAF_OCR_LANGUAGE_ID_VAR_NAME	= "STAF_OCR_LANGUAGE_ID_VAR_NAME";//Only valid for TesseractOCR
	
	public static String OCR_T_ENGINE_KEY			= "TOCR";
	public static String OCR_G_ENGINE_KEY			= "GOCR";
	public static String OCR_T_ENGINE_CLAZZ			= "org.safs.tools.ocr.tesseract.TesseractOCREngine";
	public static String OCR_G_ENGINE_CLAZZ			= "org.safs.tools.ocr.gocr.GOCREngine";
	
	public static String OCR_DEFAULT_ENGINE_KEY		= OCR_T_ENGINE_KEY;
	public static String OCR_DEFAULT_ENGINE_CLAZZ	= OCR_T_ENGINE_CLAZZ;
	
	private static HashMap<String,String> 	engineClazz = null;
	private static HashMap<String,OCREngine> enginePool = null;
	
	static{
		engineClazz = new HashMap<String,String>();
		enginePool = new HashMap<String,OCREngine>();
		
		engineClazz.put(OCR_T_ENGINE_KEY, OCR_T_ENGINE_CLAZZ);
		engineClazz.put(OCR_G_ENGINE_KEY, OCR_G_ENGINE_CLAZZ);
	}
	
	public OCREngine() {}
	
	public float defaultZoomScale = 1; // default zoom scale to resize 'screen-captured' images for better fit 300DPT that OCR require.
	public float getdefaultZoomScale() { return defaultZoomScale; }
	public void setdefaultZoomScale(float value) {	defaultZoomScale = value; }
	
	public String imageToText(BufferedImage image, String langId, Rectangle subarea) throws SAFSException {
		return imageToText(image, langId, subarea, getdefaultZoomScale());	
	}
	/**
	 * Convert buffered image to text using OCR technology. It needs to be implemented in its derived class.
	 * 
	 * @param image, an input BufferedImage for converting to text, supposed to be those images displayed on 
	 *               computer CRT. These 'screen-captured' images are at low DPI (75). It needs to be resized for 
	 *               using in OCR engine. Tesseract uses images with 300DPI. 
	 * @param langId, language id representing the language that OCR intends to convert to. 
	 * @param subarea, area of the input image for convert. NULL stands for whole area of the image. 
	 * @param zoom,  float, it is an optional parameter representing zoom value.
              SAFS uses a simple way -- resizing the image to fit 300DPI that is required by OCR for better text recognition.  
              Normal screen-captured images are at low DPI-- (75~90).
              A value between 0 and 1, stands for the size of zooming out the source image to fit 300DPI for text recognition.
              A value bigger than 1, stands for the size of zooming in the source image to fit GOCR's requirement.
              User may give a proper zoom value for the text in a image to be fit in and recognized.     
	 * @return String, converted from the input image. NULL if fails to convert.
	 * @throws SAFSException if meets any Exception
	 */
	public String imageToText(BufferedImage image, String langId, Rectangle subarea, float zoom) throws SAFSException {
		throw new UnsupportedOperationException();	
	}	
	
	public String storedImageToText(String imagefile, String langId, Rectangle subarea) throws SAFSException {
		return storedImageToText(imagefile, langId, subarea, getdefaultZoomScale());
	}
	/**
	 * Translate the standard language code to OCR specific language code.
	 * It should be overridden in derived classes. 
	 * Refer to Locale.ENGLISH.getLanguage() for input langid. For example: 'en' -- English
	 * @param langId,  standard language code
	 * @return
	 */
	protected String getSelfDefinedLangId(String langId) {
		throw new UnsupportedOperationException();
	}

	public Rectangle findTextRectFromImage(String searchtext, int index, 
			BufferedImage image, String stdlangId, Rectangle subarea, float zoom) throws SAFSException {
		throw new SAFSException("Not supported Operation: findTextRectFromImage().");
	}
	
	/**
	 * Convert an image file to text using OCR technology. 
	 * 
	 * @param imagefile, image file with formats BMP,GIF,JPEG,PNG and TIFF 
	 * @param langId, if useful, it represents the language that OCR intends to convert to. NULL means OCR doesn't care languages. 
	 * @param subarea, area of the input image for convert. NULL stands for whole area of the image. 
	 * @param zoom,  float, it is an optional parameter representing zoom value.
              SAFS uses a simple way -- resizing the image to fit 300DPI that is required by OCR for better text recognition.  
              Normal screen-captured images are at low DPI-- (75~90).
              A value between 0 and 1, stands for the size of zooming out the source image to fit 300DPI for text recognition.
              A value bigger than 1, stands for the size of zooming in the source image to fit GOCR's requirement.
              User may give a proper zoom value for the text in a image to be fit in and recognized.     
	 * @return String, converted from the input image. NULL if fails to convert.
	 * @throws SAFSException if meets any Exception
	 */
	public String storedImageToText(String imagefile, String langId, Rectangle subarea, float zoom) throws SAFSException  {
		String debugMsg = getClass().getName() + ".storedImageToText():";
		Log.info(debugMsg + " start to find text in image file:" + imagefile);
		if (imagefile == null) {
			Log.info("The input image is null. Can't continue.");
			throw new SAFSException("Unable to open null file");
		}
		try {
			BufferedImage bImage = ImageUtils.getStoredImage(imagefile);
			return imageToText(bImage, langId, subarea, zoom);
		}catch(IOException ie){
			Log.debug("Fail to get image " + imagefile + " IOException thrown.");
			throw new SAFSException(ie.toString());
		}
	}	
	
	
	/**
	 * 
	 * @param image   an BufferedImage for zooming.
	 * @param imageType  image type of the zoomed image for the BufferedImage returned. 
	 *                   Usually use BufferedImage.TYPE_BYTE_GRAY in 8 BitPerPixel.
	 * @param subarea, area of the input image for convert. NULL stands for whole area of the image.    
	 * @param zoomValue, a float, means the size of zoom-in if between 0 and 1; means the size of zoom-out if bigger than 1.
	 * @return BufferedImage, a resized image in imageType.
	 * @throws SAFSException
	 */
	public  BufferedImage zoomImageWithType(BufferedImage image, int imageType, Rectangle subarea, float zoomValue) throws SAFSException
	{
		Log.info("OCREngine.zoomImageWithType(): start...");
		if (zoomValue <= 0){
			Log.debug("");
			throw new SAFSException("illegal zoom value:" + zoomValue);	
		}
		BufferedImage bImage = image;
		if (subarea != null){
			try {
				Log.info("try to get subimage in : "+ subarea);
				bImage = bImage.getSubimage(subarea.x, subarea.y, subarea.width, subarea.height);
			}catch(java.awt.image.RasterFormatException rfe) {
				Log.debug("Fail to getSubimage. The specified subarea is not contained within the source image.");
				throw new SAFSException(rfe.toString());
			}
		}
		
		Log.debug("Scale input image for OCR engine, zoom value:" + zoomValue);		
		BufferedImage transformedImg = bImage;
		// try to zoom the image with zoomValue to fit 300DPI requirement in ocr.
		// images captured on screen normally are at low DPI like 72 or 96. http://tiporama.com/tools/pixels_inches.html
		// User should pass proper zoomValue to resize the converting image.
		// 
		// many softwares like photoshop use BICUBIC interpolation to resize images for better effect.
		// Using AffineTransform with TYPE_BICUBIC interpolation, the source image can be scaled decently for OCR to detect.
		// So abandon using BufferedImage.getScaledInstance to scale images.
		// TYPE_BICUBIC interpolation! 
		//
		if (zoomValue != 1.0) {
			// use AffineTransform to scale bImage
			// choose transform matrix:
			// [ zoomValue,        0,   0] 
			// [     0,    zoomValue,   0]
			// [     0,            0,   1]
			AffineTransform transform = AffineTransform.getScaleInstance(zoomValue, zoomValue);
			AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
			transformedImg = op.filter(bImage, null);
			Log.debug("OCREngine.zoomImageWithType(): AffineTransform finished");
		}
		
		// create a target image with the content of the scaled image. 
		// the target image is for being handled by OCR 
		BufferedImage targimg = new BufferedImage(transformedImg.getWidth(), transformedImg.getHeight(), imageType);   
		Graphics g = targimg.getGraphics();  
		g.drawImage(transformedImg, 0, 0, null); 
		g.dispose();

		return targimg; 
	}
	
	protected String runCommandLine(String cmdline)throws SAFSException{
		String output = null;
		Runtime runtime = Runtime.getRuntime();
		try{
			Process proc = runtime.exec(cmdline);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream())); 
			String strline; 
			while ((strline = stdInput.readLine()) != null) {
				if (output == null)
					output = strline;
				else
					output += "\n" + strline;
			}
			proc.waitFor(); // wait until the subprocess of cmdline has been finished 
		}catch(IOException ex){
			Log.debug("Fail to run '" + cmdline + "'. Exception thrown:" + ex.toString());
			throw new SAFSException(ex.toString());
		}catch(InterruptedException ie){
			Log.debug("Fail to run '" + cmdline + "'. Exception thrown:" + ie.toString());
			throw new SAFSException(ie.toString());
		}
		return output;
	}
	
	/**
	 * <b>Note:</b><br>		This method first try to get OCR engine corresponding to ocrNameKey;<br>
	 * 						If not found, try to get the OCR engine defined by STAF variable 
	 * 						STAF_OCR_ENGINE_VAR_NAME;<br>
	 *                      If there is no OCR engine defined in STAF variable, then use the default
	 *                      engine defined by OCR_DEFAULT_ENGINE_KEY.<br>
	 * @param ocrNameKey    The name of OCR engine. It can be constant OCR_T_ENGINE_KEY or OCR_G_ENGINE_KEY.
	 *                      It can be null, in this case, we try to get OCR engine defined by STAF variable
	 *                      STAF_OCR_ENGINE_VAR_NAME.
	 * @param staf
	 * @return				An OCREngine
	 * @throws SAFSException
	 */
	public static OCREngine getOCREngine(String ocrNameKey, STAFHelper staf) throws SAFSException{
		String debugmsg = OCREngine.class.getName()+".getOCREngine(ocrNameKey,staf): ";
		OCREngine engine = null;
		String ocrKey = null;
		
		//1. Try to get the OCR engine by the parameter ocrNameKey
		Log.debug(debugmsg+" Getting ocrKey: "+ocrNameKey);
		engine = getOCREngine(ocrNameKey);
		if(engine!=null) return engine;
		
		//2. Try to get the OCR engine defined by STAF variable STAF_OCR_ENGINE_VAR_NAME
		if(staf==null){
			Log.debug(debugmsg+" STAFHelper is null!!!!");
			ocrKey = OCR_DEFAULT_ENGINE_KEY;
		}else{
			//Get the engine name from STAF variable list (STAF_OCR_ENGINE_VAR_NAME)
			//This variable is set originally from .ini configuration file.
			//This variable can be modified by keyword setOCREngine
			try {
				ocrKey = staf.getVariable(STAF_OCR_ENGINE_VAR_NAME);
			} catch (SAFSException e) {
				Log.debug(debugmsg+" Exception occurs when get variable "+STAF_OCR_ENGINE_VAR_NAME);
			}
			if(ocrKey==null || ocrKey.equals("")){
				ocrKey = OCR_DEFAULT_ENGINE_KEY;
			}
		}
		engine = getOCREngine(ocrKey);
		
		//3. Last chance
		// If we can not get the OCR engine defined by STAF variable STAF_OCR_ENGINE_VAR_NAME,
		// we will try the default COR engine
		if(engine==null && !ocrKey.equals(OCR_DEFAULT_ENGINE_KEY)){
			ocrKey = OCR_DEFAULT_ENGINE_KEY;
			engine = getOCREngine(ocrKey);
		}
		
		if(engine==null){
			Log.debug(debugmsg+" Can not get OCR engine. This is bizzare, check the debug log.");
			throw new SAFSException(" Can not get OCR engine.");
		}
		
		return engine;
	}
	
	/**
	 * <b>Note:</b><br>	This method will get the OCR engine from a pool,if
	 * 					not found, we try to instantiate one and put it to pool then return. 
	 * @param ocrKey	String, the OCR name key, it can be the constant 
	 * 							OCR_T_ENGINE_KEY or OCR_G_ENGINE_KEY
	 * @return 			An OCREngine instance
	 */
	private static OCREngine getOCREngine(String ocrKey){
		String debugmsg = OCREngine.class.getName()+".getOCREngine(ocrKey): ";
		OCREngine engine = null;
		String ocrClazz = null;
		
		if(ocrKey==null || ocrKey.trim().equals("")) return null;
		
		ocrKey = ocrKey.toUpperCase();
		Log.debug(debugmsg+" Getting ocrKey: "+ocrKey);
		//Try to get engine from the pool
		engine = enginePool.get(ocrKey);
		
		//We may need to instantiate the engine class.
		if(engine==null){
			ocrClazz = engineClazz.get(ocrKey);
			Log.debug(debugmsg+" Instantiating OCR: "+ocrClazz);
			try {
				engine = (OCREngine) Class.forName(ocrClazz).newInstance();
				enginePool.put(ocrKey, engine);
			} catch (Exception e) {
				Log.debug(debugmsg+" can not instantiate ocr class "+ocrClazz);
			}
		}
		
		return engine;
	}
	
	/**
	 * <b>Note:</b><br>  This method will get the value of STAF variable STAF_OCR_ENGINE_VAR_NAME
	 * @param staf
	 * @return
	 */
	public static String getOCREngineKey(STAFHelper staf){
		String engineKey = OCR_DEFAULT_ENGINE_KEY;
		
		try {
			if(staf!=null)
				engineKey = staf.getVariable(STAF_OCR_ENGINE_VAR_NAME);
		} catch (SAFSException e) {
			Log.debug("Can not get variable: "+STAF_OCR_ENGINE_VAR_NAME+". "+e.getMessage());
		}
		
		return engineKey;
	}
	
	/**
	 * <b>Note:</b><br>  This method will set the value to STAF variable STAF_OCR_ENGINE_VAR_NAME
	 * @param staf
	 * @param engineKey
	 * @return
	 */
	public static boolean setOCREngineKey(STAFHelper staf, String engineKey){
		boolean ok = false;
		
		try {
			if(staf!=null)
				ok = staf.setVariable(STAF_OCR_ENGINE_VAR_NAME,engineKey);
		} catch (SAFSException e) {
			Log.debug("Can not get variable: "+STAF_OCR_ENGINE_VAR_NAME+". "+e.getMessage());
		}
		
		return ok;
	}
	/**
	 * <b>Note:</b><br>  This method will get the value of STAF variable STAF_OCR_LANGUAGE_ID_VAR_NAME
	 * @param staf
	 * @return
	 */
	public static String getOCRLanguageCode(STAFHelper staf){
		String languageCode = null;
		
		try {
			if(staf!=null)
				languageCode = staf.getVariable(STAF_OCR_LANGUAGE_ID_VAR_NAME);
		} catch (SAFSException e) {
			Log.debug("Can not get variable: "+STAF_OCR_LANGUAGE_ID_VAR_NAME+". "+e.getMessage());
		}
		
		if(languageCode==null){
			languageCode = Locale.getDefault().getLanguage();
		}
		
		return languageCode;
	}
	/**
	 * <b>Note:</b><br>  This method will set the value to STAF variable STAF_OCR_LANGUAGE_ID_VAR_NAME
	 * @param staf
	 * @param languageCode
	 * @return
	 */
	public static boolean setOCRLanguageCode(STAFHelper staf, String languageCode){
		boolean ok = false;
		
		try {
			if(staf!=null)
				ok = staf.setVariable(STAF_OCR_LANGUAGE_ID_VAR_NAME,languageCode);
		} catch (SAFSException e) {
			Log.debug("Can not get variable: "+STAF_OCR_LANGUAGE_ID_VAR_NAME+". "+e.getMessage());
		}
		
		return ok;
	}
	
	/**
	 * Usage: java OCREngine imageFile [-e engine] [-l languageID] [-z scale]
	 * @param args
	 */
	public static void main(String[] args){
		STAFHelper staf = null;
		
		String ocrNameKey = null;//This will be given by parameter. It can be TOCR or GOCR
		String langcode = null;
		float zoom = 0.0f;
		//If you want to test get engineName and langCode from STAF variables,
		//Please run you STAF, and change this field to true.
		boolean useVariableService = false;
		
		//java OCREngine C:\safs\tesseract-2.04.exe\pic\paragraph.TIF -e TOCR
		if (args.length == 0) {
			System.out.println(" no image file input, try again input a image file");
			System.out.println(" Usage: java OCREngine imageFile [-e engine] [-l languageID] [-z scale]");
			return;
		}
		
		String imagefile = args[0];
		//Analyze other parameters
		for(int i=1;i<args.length;i++){
			if(args[i].equalsIgnoreCase("-e")){
				if(i+1<args.length){
					ocrNameKey = args[++i];
				}else{
					System.out.println(" you should specify a engine value after -e.");
				}
			}else if(args[i].equalsIgnoreCase("-l")){
				if(i+1<args.length){
					langcode = args[++i];
				}else{
					System.out.println(" you should specify language value after -l.");
				}
			}else if(args[i].equalsIgnoreCase("-z")){
				if(i+1<args.length){
					zoom = Float.parseFloat(args[++i]);
				}else{
					System.out.println(" you should specify scale value after -z.");
				}
			}
		}
		
		if(useVariableService){
			try {
				staf = new STAFHelper("OCREngine_Test_Process");
				//Make sure the STAF has been started.
				staf.addServiceSAFSVARS("local", STAFHelper.SAFS_VARIABLE_SERVICE, "org.safs.staf.service.var.SAFSVariableService3", "", "");
			} catch (SAFSSTAFRegistrationException e) {
				System.out.println("Can not regist variable service SAFSVARS.");
			}
			
			if(staf==null){
				System.out.println("Can not initialize OCREngine_Test_Process");
			}else{
				try {			
					if(ocrNameKey!=null) staf.setVariable(OCREngine.STAF_OCR_ENGINE_VAR_NAME, ocrNameKey);
					if(langcode!=null) staf.setVariable(OCREngine.STAF_OCR_LANGUAGE_ID_VAR_NAME, langcode);
				} catch (SAFSException e) {
					System.out.println(STAF_OCR_ENGINE_VAR_NAME+" can not be set correctly.");
				}
			}
		}

		OCREngine ocr = null;
		try {
			ocr = OCREngine.getOCREngine(ocrNameKey, staf);
		} catch (SAFSException e) {
			System.out.println(STAF_OCR_ENGINE_VAR_NAME+" can not be set correctly.");
		}
		
		if(ocr==null){
			System.out.println("Can not get ocr engine.");
			return;
		}
		
		String text = null;
		String langId = OCREngine.getOCRLanguageCode(staf);
		if(langcode!=null) langId = langcode;
		
		if(zoom<=0.0f){
			zoom = ocr.getdefaultZoomScale();
		}
		
		if(ocr instanceof TesseractOCREngine){
			System.out.println("We are using TOCR Engine to recognize image with langId="+langId+" with zoom="+zoom);
		}else if(ocr instanceof GOCREngine){
			System.out.println("We are using GOCR Engine to recognize image with langId="+langId+" with zoom="+zoom);
		}
		
		try {
			text = ocr.storedImageToText(imagefile, langId, null, zoom);
		} catch (SAFSException e) {
		}
		
		if(useVariableService){
			//Remove the SAFSVARS service
			staf.removeService("local", STAFHelper.SAFS_VARIABLE_SERVICE);
		}
		System.out.println(" For image file: "+imagefile+". OCR recognize it as "+text);
	}
}
