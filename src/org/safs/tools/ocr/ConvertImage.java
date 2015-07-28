/** 
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.ocr;

import java.awt.image.BufferedImage;
import java.io.File;

import org.safs.Log;
import org.safs.image.ImageUtils;
import org.safs.tools.ocr.gocr.GOCREngine;

/**
 * Convert an image in JPG, BMP, TIF, GIF, PNG or PNM.
 * Ensure JAI imageio has been installed on your machine before training
 *        Without JAI imageio, only bmp and jpg are supported.
 *        http://java.sun.com/products/java-media/jai/INSTALL-jai_imageio_1_0_01.html#Windows
 *        https://jai-imageio.dev.java.net/binary-builds.html
 * Command line:
 * org.safs.tools.ocr.ConvertImage <sourceimage> <targetimage> [-c color] [-z zoom]
 * <sourceimage>: the image that needs to be converted, , suffixed with JPG, BMP, TIF, GIF, PNG or PNM.
 * <targetimage>: the image that it intends to be converted to, suffixed with JPG, BMP, TIF, GIF, PNG or PNM.
 * [-c color]:    the color style for <targetimage> to use. color can be 1)gray or 2)blackwhite 
 *                <targetimage> uses the same color style in <sourceimage> as default.
 * [-z zoom]:     zoom value in converting <sourceimage> to <targetimage> (default=1).                
 *                
 * 
 * @author Junwu Ma
 * <br>	JAN 15, 2010    Original Release 
 *
 */
public class ConvertImage {
	static public void showHelp(){
		System.out.println("SAFS Jan 18 2010 ");
		System.out.println("Supports JPG, BMP, TIF, GIF, PNG and PNM if JAI imageiio installed");
		System.out.println("https://jai-imageio.dev.java.net/binary-builds.html");
		System.out.println("Using: org.safs.tools.ocr.ConvertImage [options] <sourceimage> <targetimage>");
		System.out.println("[options] see the followings for details:");
		System.out.println("-h       - help");
		System.out.println("-z zoom  - zoom value in converting <sourceimage> to <targetimage>(default=1)");
		System.out.println("-c color - color style for <targetimage> to use. 1)gray or 2)blackwhite ");
		System.out.println("           default: <targetimage> uses the same color style in <sourceimage>");
		System.out.println("examples:");
		System.out.println("org.safs.tools.ocr.ConvertImage s.gif t.png -c gray -z 1.5");
		System.out.println("org.safs.tools.ocr.ConvertImage s.tif t.gif -c blackwhite -z 0.8");
		System.out.println("org.safs.tools.ocr.ConvertImage s.jpg t.tif -z 2");
		System.out.println("org.safs.tools.ocr.ConvertImage s.bmp t.pnm    #with same size and color");
	}
	public static void main(String[] args){
		float  zoom = 1;     // store zoom value
		String color = "";   // store color style 
		
		String[] imagefiles = new String[2];
		imagefiles[0] = "";  // store the source image file
		imagefiles[1] = "";  // store the target image file
		
		// parse the arguments
		int index = 0;
		for (int i=0; i<args.length; i++ ){
			if (args[i].equalsIgnoreCase("-h")){
				showHelp();
				return;
			}else if (args[i].equalsIgnoreCase("-z")){
				try { zoom = (float)Double.parseDouble(args[++i]);}
				catch(NumberFormatException x){}
			}else if (args[i].equalsIgnoreCase("-c")){
				color = args[++i];
			}else{
				imagefiles[index++] = args[i];
			}
		}
		
		if (imagefiles[0].equalsIgnoreCase("") || imagefiles[1].equalsIgnoreCase("")) {
			System.out.println("No source image file or target image file input. Exit! ");
			return;
		}

		String imagesource = args[0];
		String imagetarget = args[1];
		GOCREngine gocr = new GOCREngine();
		System.out.println(" start converting " + imagesource + " to " + imagetarget);
		
		try {
			BufferedImage bImage = ImageUtils.getStoredImage(imagesource);
			Log.debug("successfully open " + imagesource);
			
			int colorType;
			if (color.equalsIgnoreCase("gray"))
				colorType = BufferedImage.TYPE_BYTE_GRAY;
			else if (color.equalsIgnoreCase("blackwhite"))
				colorType = BufferedImage.TYPE_BYTE_BINARY;
			else
				colorType = bImage.getType(); // using the color type of sourceimage as default
				
			BufferedImage imgForTraining = gocr.zoomImageWithType(bImage, colorType, null, zoom);
			ImageUtils.saveImageToFile(imgForTraining, new File(imagetarget));
			Log.debug("successfully write " + imagetarget);
				
			System.out.println(" Finished. ");
		}catch(Exception ex){
			System.out.println("exception thrown in converting:"+ex.toString());
		}
	}
}
