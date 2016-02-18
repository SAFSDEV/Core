/** 
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.image;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ImagingOpException;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;

import org.safs.ComponentFunction;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.text.FAILStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.ocr.OCREngine;
import org.safs.tools.stringutils.StringUtilities;

import sun.util.logging.resources.logging;

/**
 * Utility functions for processing or manipulating Images stored in the File system 
 * or captured from the Screen.
 * 
 * @author canagl Sept 09, 2008
 * @see java.awt.Robot 
 * @see java.awt.image.BufferedImage
 * 
 * <br>	Dec 23, 2008	(LeiWang)	Add method saveImageToFile(): If the file format is JPG, use the compression quality to save file.
 * <br> Sep 25, 2009	(JunwuMa)	Update getStoredImage() to use JAI API to open image file. It is supposed to recognize 
 *                                  all JAI supported image types including compressed TIFF.
 * <br> Oct 29, 2009	(CANAGL)	Added BitTolerance support. 
 * <br> Nov 03, 2009	(CANAGL)	Draft Prep for multi-threaded algorithm. 
 * <br> Nov 19, 2009	(CANAGL)	Removing default of multi-threading BitTolerance searches. 
 *                                  Also attempted to isolate more multi-threading code out of primary routines. 
 * <br> Jan 18, 2010    (JunwuMa)   Update saveImageToFile() to support tif, gif, png and pnm. 
 *                                  It requires JAI Imageio installed.
 *                                  http://java.sun.com/products/java-media/jai/INSTALL-jai_imageio_1_0_01.html#Windows
 *                                  https://jai-imageio.dev.java.net/binary-builds.html
 * <br> MAR 14, 2010    (JunwuMa)   Adding "ImageText=" mode, which is only used to define a non top Window in IBT 
 *                                  for TOCR to search in the snapshot of the top Window.
 * <br>	Apr 07, 2010	(LeiWang)   Add "ImageRect=" or "ImageRectangle=" mode, which is used to define a top Window.
 * 									For now, it is generated internally by RJ engine side (@see org.safs.rational.CFTIDComponent)
 *									Of course user can use it directly to represent a Top window.
 *									Modify method extractImagePath(), findComponentRectangle().
 *									Remove method getImageRecognition(), move this functionality to TestRecordHelper's method getRecognitionString()
 * <br>	Apr 20, 2010	(LeiWang)   Modify method findComponentRectangle():use static method of OCREngine to get
 *                                  an OCR engine to use.
 * <br>	May 26, 2010	(JunwuMa) 	Updates to make ImageText= work with Index= and SearchRect=   
 * <br>	Jul 13, 2010	(LeiWang) 	Add method findImageWithXYBTThread(),findSmallImageWithXYBTThread(),findBigImageWithXYBTThread()
 *                                  Modify method findBufferedImageOnScreen(),modifyLocWidthHeight(),extractImageIndex()
 *                                  Do these modifications for applying multi-thread to search an image.                 
 * <br>	Oct 26, 2010	(CANAGL) 	Large refactoring to fix implementation for SearchRect and new usePerImageModifiers feature.
 * <br>	Oct 28, 2010	(CANAGL) 	Added support for UsePerImageModifiers on Index= and BitTolerance=
 * <br>	DEC 22, 2010	(LeiWang) 	Added methods getCopiedImage(), paintOnImage() and getImageFileFilter() etc.
 * <br>	DEC 27, 2010	(LeiWang) 	Added methods paintOnImage(): add one boolean parameter to decide if paint on the original image.
 *                                  Add some image filter operations like blur, sharpen, edge, etc.
 * <br>	AUG 25, 2014	(LeiWang) 	Fix problem of saving image as .bmp and .jpg file.
 * <br>	SEP 30, 2014	(LeiWang) 	Add method copy().
 * <br>	SEP 30, 2014	(LeiWang) 	Modify method getSubAreaRectangle(): move common code to calculateAbsoluteCoordinate().
 *                                  Add class SubArea: user write string subarea may contain "error", such as "0, 0, 50%, 60%", "0;0;50;90;"
 *                                  these are correct for human-being, but can not be accepted by our program. Use SubArea will reduce error.
 * <br>	DEC 12, 2014	(LeiWang) 	Move filterImage (cover certain area with black) functinality from DCDriverFileCommands to here.
 * <br> FEB 18, 2016    (CANAGL)    Support Comp search of whole screen when WinRec is ImageRect or SearchRect.
 */
public class ImageUtils {

	/** for messaging and GenericEngine.saveTestRecordScreenToTestDirectory **/
	public static boolean debug = false; 
	
	public static final String MOD_EQ 			= "=";
	public static final String MOD_COMMA 		= ",";
	public static final String MOD_SEMICOLON	= ";";
	public static final String MOD_SPACE 		= " ";
	public static final String MOD_PERCENT		= "%";
	public static final String MOD_SEP 			= MOD_SEMICOLON;
	public static final String MOD_IMAGE 		= "Image";
	public static final String MOD_IMAGER 		= "ImageR";
	public static final String MOD_IMAGERIGHT 	= "ImageRight";
	public static final String MOD_IMAGEW 		= "ImageW";
	public static final String MOD_IMAGEWIDTH 	= "ImageWidth";
	public static final String MOD_IMAGEB 		= "ImageB";
	public static final String MOD_IMAGEBOTTOM 	= "ImageBottom";
	public static final String MOD_IMAGEH 		= "ImageH";
	public static final String MOD_IMAGEHEIGHT 	= "ImageHeight";
	public static final String MOD_INDEX 		= "Index";
	public static final String MOD_IND 			= "Ind";
	public static final String MOD_HOTSPOT 		= "HotSpot";
	public static final String MOD_HS 			= "HS";
	public static final String MOD_SEARCHRECT  	= "SearchRect";
	public static final String MOD_SR 			= "SR";
	public static final String MOD_SEARCHMASK  	= "SearchMask";
	public static final String MOD_SM 			= "SM";
	public static final String MOD_POINTRELATIVE	= "PointRelative";
	public static final String MOD_PR 			= "PR";
	public static final String MOD_INSETS 		= "InSets";
	public static final String MOD_INS 			= "InS";
	public static final String MOD_OUTSETS 		= "OutSets";
	public static final String MOD_OUTS 		= "OutS";
	public static final String MOD_BITTOLERANCE = "BitTolerance";
	public static final String MOD_BT           = "BT";

	public static final String MOD_TOPLEFT	  	= "TopLeft";
	public static final String MOD_TL 			= "TL";
	public static final int    INT_TOPLEFT	  	= 1;
	public static final String MOD_TOPCENTER	= "TopCenter";
	public static final String MOD_TC 			= "TC";
	public static final int    INT_TOPCENTER	= 2;
	public static final String MOD_TOPRIGHT	  	= "TopRight";
	public static final String MOD_TR 			= "TR";
	public static final int    INT_TOPRIGHT	  	= 3;
	public static final String MOD_LEFTCENTER	= "LeftCenter";
	public static final String MOD_LC 			= "LC";
	public static final int    INT_LEFTCENTER	= 4;
	public static final String MOD_CENTER		= "Center";
	public static final String MOD_C 			= "C";
	public static final int    INT_CENTER		= -1;
	public static final String MOD_RIGHTCENTER  = "RightCenter";
	public static final String MOD_RC 			= "RC";
	public static final int    INT_RIGHTCENTER  = 6;
	public static final String MOD_BOTTOMLEFT	= "BottomLeft";
	public static final String MOD_BL 			= "BL";
	public static final int    INT_BOTTOMLEFT	= 7;
	public static final String MOD_BOTTOMCENTER	= "BottomCenter";
	public static final String MOD_BC 			= "BC";
	public static final int    INT_BOTTOMCENTER = 8;
	public static final String MOD_BOTTOMRIGHT	= "BottomRight";
	public static final String MOD_BR 			= "BR";
	public static final int    INT_BOTTOMRIGHT  = 9;	

    public final static String EXT_JPEG = "jpeg";
    public final static String EXT_JPG = "jpg";
    public final static String EXT_GIF = "gif";
    public final static String EXT_TIFF = "tiff";
    public final static String EXT_TIF = "tif";
    public final static String EXT_PNG = "png";
    

	/** "ImageText=", a new image mode to define a non top Window only.  
	 *  In a traditional IBT-defined top window like "Image=<path>", TessOCR may location the text area in the top 
	 *  window. Instead of searching a component image in a top-window image, SAFS may use tess OCR to locate the 
	 *  text area in the top-window if the text can be recognize by the OCR. Thus we no longer need to prepare	
	 *  the snapshots of the text in different screen resolution for SAFS to use like it used to be.
	 *   Examples:
	 *   Recognition Strings 
	 *   topWin="Image=<path>"
	 *   Comp="ImageText=<text>"
	 *  
	 *   IBT script:
	 *   T,topWin,Comp,Click
	 */
	public static final String MOD_IMAGETEXT		= "ImageText";
	/** 
	 *  "ImageRect=", "SearchRect", "ImageRectangle=", or "SearchRectangle" support a new image mode to define a 
	 *  top Window only.
	 *  <p>
	 *  A traditional IBT-defined top window is like "Image=<path>", then IBT will try to find the location
	 *  of this top window on the screen. The location is represented by a Rectangle(x,y,width,height).
	 *  In IBT we call this location as Anchor, from where we will continue to find component location.
	 *  <p>
	 *  Now we can profit from a special Engine (ex. RJ) to get the top window object, this object can give us it's
	 *  location on the screen, then we can create a new RS like "ImageRect=x,y,width,height" and send it back to 
	 *  IBT (@see org.safs.tools.engines.TIDComponent), IBT can deduce the Anchor from this RS directly.
	 *  <p>
	 *  It will be a great complement for a special Engine in case that the engine can't find some components.
	 *  Take RFT as example, 
	 *  <p><pre>
	 *   Examples:
	 *   Recognition Strings:
	 *   topWin="Type=JavaWindow;Caption={Swing*}"
	 *   Comp="IBT_Recognization_String"
	 *  
	 *   IBT script:
	 *   T,topWin,Comp,Click
	 *   
	 *   Explain:
	 *   1. RS topWin="Type=JavaWindow;Caption={Swing*}" is treated in a special Engine, the top window object
	 *   	is got.
	 *   2. From the top window object, we generate a new RS "ImageRect=x,y,w,h" for top window. And we send the test
	 *   	record to IBT.
	 *   3. IBT will parse RS "ImageRect=x,y,w,h" and get the Anchor, and it will find the component location
	 *   	within that Anchor, and perform the action Click on that location.
	 *   </pre><p>
	 *   If this feature is used with feature "ImageText=", that will be great. We don't need to store ANY one
	 *   image.
	 *   <p>
	 *   We can also support this just by allowing an IBT Window Recognition string in the appmap to contain ImageRect 
	 *   or SearchRect recognition information without specifying any real images.
	 */	
	public static final String MOD_IMAGE_RECT		= "ImageRect";
	public static final String MOD_IMAGE_RECTANGLE	= "ImageRectangle";
	public static final String MOD_SEARCH_RECT		= "SearchRect";
	public static final String MOD_SEARCH_RECTANGLE	= "SearchRectangle";

	private static FileFilter imageFileFilter = null;
	static Toolkit toolkit = null;
	static Dimension screenSize = null;
	static Rectangle screenRect = null;
	static BufferedImage screenImage = null;
	static int screenImageType = -1;
	static Raster screenRaster = null;
	static SampleModel screenModel = null;
	static DataBuffer screenBuffer = null;
	static ColorModel screenColor = null;	
	static int screenWidth = -1;
	static int screenHeight = -1;
	static int screenBands = -1;

	static int closestX = -1;
	static int closestY = -1;
	static long closestMatchCount = -1;
	static long closestMatchesOf = -1;
	static float closestPercentage = 0;
	
	/** 100 */
	public static final int MAX_PERCENT_BITS_TOLERANCE = 100;	
	
	/** 
	 * 1-100, acquired from non-recognition string settings like an INI file.
	 * Defaults to 100.
	 */
	public static int default_percent_bits_tolerance = MAX_PERCENT_BITS_TOLERANCE;
	/** 
	 * current tolerance for percent of image pixels that must match 
	 **/
	static int percentBitsTolerance = MAX_PERCENT_BITS_TOLERANCE;
	
	/** 
	 * true or false, acquired from non-recognition string settings like an INI file.
	 * Defaults to false.
	 */
	public static boolean default_useBitsTolerance = false;

	/**
	 * Allow enhanced use of SearchRect=, Index=, and BitTolerance= on all Window 
	 * definition components like Image=, ImageR= and ImageH= instead of just for 
	 * the Image= portion.
	 * <p> 
	 * true or false, acquired from non-recognition string settings like an INI file.
	 * Defaults to false.
	 */
	public static boolean USE_PER_IMAGE_MODIFIERS = false;
	
	/** 
	 * true if percentBitsTolerance should be used when evaluating image matches 
	 **/
	static boolean useBitsTolerance = false;
	
	/** 
	 * true if we can add fuzzy pixel matching to the normal match algorithm 
	 **/
	public static boolean USE_FUZZY_MATCHING = false;
	
	static JAIImagingListener jailistener = null;
	
	/**
	 * Set true if image searches using BitTolerance 
	 * should attempt to use parallel threading.
	 * The current implementation of multi-threading is poor, 
	 * and may actually be slower than NOT using multi-threading.
	 * Current default is 'false'.
	 * @see org.safs.image.SmallPieceComparator
	 * @see org.safs.image.ScreenXYBTComparator  
	 */
	public static boolean USE_MULTIPLE_THREADS = false;
	
	/**
	 * When USE_MULTIPLE_THREADS is true, DIVIDE_PIECES will be used.
	 * This field decide the number of blocks that a search rectangle or
	 * an image to be divided, this is also the number of threads to
	 * be created to search an image.
	 * The number of threads will be DIVIDE_PIECES*DIVIDE_PIECES.
	 * The default DIVIDE_PIECES is 4, so number of blocks (or number of
	 * threads will be 4*4, that is 16)
	 * @see org.safs.image.SmallPieceComparator
	 */
	public static int DIVIDE_PIECES = 4;
	
	/**'100*100' if the image size is bigger than this threadshold, we will use multiple threads to compare*/
	public final static int IMAGE_SIZE_PIXEL_THREADSHOLD = 100*100;

	/**
	 * Clear\Reset all internally stored screen data like:
	 * screenWidth, screenHeight, screenBands, screenImage, screenModel, screenBuffer, and screenColor.
	 */
	public static void resetScreenData(){
		screenImage = null;
		screenImageType = -1;
		screenModel = null;
		screenBuffer = null;
		screenColor = null;
		screenWidth = -1;
		screenHeight = -1;
		screenBands = -1;
	}
	
	/**
	 * Clear\Reset all internally stored search match data like:
	 * closestX, closestY, closestMatchCount, closestMatchesOf, closestPercentage
	 */
	public static void resetMatchData(){
		closestX = -1;
		closestY = -1;
		closestMatchCount = -1;
		closestMatchesOf = -1;
		closestPercentage = 0;
	}
	
	/**
	 * @return last known screenWidth or -1 if unretrievable
	 */
	public static int getScreenWidth(){
		try{ 
			if(screenWidth==-1) recaptureScreen(); 
			return screenWidth;
		}catch(Exception x){}
		return -1;
	}

	/**
	 * @return last known screenHeight or -1 if unretrievable
	 */
	public static int getScreenHeight(){
		try{ 
			if(screenHeight==-1) recaptureScreen(); 
			return screenHeight;
		}catch(Exception x){}
		return -1;
	}

	/**
	 * Returns an ImageIcon, or null if the path is invalid.
	 * @param path String path suitable for getResource() to create\locate a valid java.net.URL.
	 * @return
	 * @see java.lang.Class#getResource(String)
	 * @see java.net.URL
	 */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ImageUtils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
	
    /**
     * Helper function for getSubAreaRectangle().  Given a compRect Rectangle 
     * and a subRect Rectangle that lies outside of the Component Rectangle, return a new, clipped
     * subRect Rectangle that has been forced to fit in the bounds of the compRect Rectangle.  If the
     * subRect Rectangle falls completely outside of the compRect Rectangle, then an invalid Rectangle
     * is created (width and/or height = 0).  In that case, the new clipped Rectangle is invalid and
     * null is returned (its image cannot be captured).
     *
     * @param                     Rectangle compRect; the portion of the window that holds the component.
     * @param                     Rectangle subRect; the the portion of compRect in question.
     * @return                    Rectangle clippedRect; a new clipped portion of subRect that has been
     *                             forced to fit in the bounds of compRect, or null if the subRect does 
     *                             not overlap the compRect in any way.
     * @author bolawl				-Added 10.21.2005 (RJL)
     **/
    public static Rectangle getClippedSubAreaRectangle(Rectangle compRect, Rectangle subRect) {
    	String methodName = "getClippedSubAreaRectangle";
    	Log.debug(methodName + ": SubArea Rect not contained in Component Rect, modifying SubArea Rect!");
    	Log.debug(methodName + ": Component Rect: " + compRect.toString());
    	Log.debug(methodName + ": SubArea Rect: " + subRect.toString());
    	
    	//left, top, right, 			bottom
    	//x,	y,	 (x + width - 1), 	(y + height - 1)
    	int crleft = compRect.x;
    	int crtop = compRect.y;
    	int crright = compRect.x + compRect.width - 1;
    	int crbottom = compRect.y + compRect.height - 1;
    	int srleft = subRect.x;
    	int srtop = subRect.y;
    	int srright = subRect.x + subRect.width - 1;
    	int srbottom = subRect.y + subRect.height - 1;
    	
    	//if subRect.left < compRect.left then subRect.left = compRect.left
    	if (srleft < crleft)
    		srleft = crleft;
    	
    	//if subRect.left > compRect.right then subRect.left = compRect.right
    	if (srleft > crright)
    		srleft = crright;
    	
    	//if subRect.top < compRect.top then subRect.top = compRect.top
    	if (srtop < crtop)
    		srtop = crtop;
    	
    	//if subRect.top > compRect.bottom then subRect.top = compRect.bottom
    	if (srtop > crbottom)
    		srtop = crbottom;
    	
    	//if subRect.right < compRect.left then subRect.right = compRect.left
    	if (srright < crleft)
    		srright = crleft;
    	
    	//if subRect.right > compRect.right then subRect.right = compRect.right
    	if (srright > crright)
    		srright = crright;
    	
    	//if subRect.bottom < compRect.top then subRect.bottom = compRect.top
    	if (srbottom < crtop)
    		srbottom = crtop;
    	
    	//if subRect.bottom > compRect.bottom then subRect.bottom = compRect.bottom
    	if (srbottom > crbottom)
    		srbottom = crbottom;
    	
    	Rectangle clippedRect = new Rectangle(srleft, srtop, (srright - srleft + 1), (srbottom - srtop + 1));
    	Log.debug(methodName + ": Clipped SubArea Rect: " + clippedRect.toString());
    	
    	//if clippedRect is now empty (width or height = 0), then Rectangle is invalid and cannot be saved
    	if (clippedRect.isEmpty())
    		return null;
    	
    	return clippedRect;
    }
    
	/**
	 * Represent an SubArea by 2 coordiantes (TopLeft, BottomRight).<br>
	 * The x/y of coordinate can be number or a percentage-number, <br>
	 * number means absolute coordinate, percentage-number means relative width/height.<br>
	 * This SubArea is used to get a sub Rectangle from a Rectangle.<br>
	 * 
	 * @see ImageUtils#getSubAreaRectangle(Rectangle, String)
	 * 
	 * @example
	 * <pre>
	 * {@code
	 * Rectangle rectangle = new Rectangle(20, 50, 360, 700);
	 * SubArea subarea = new SubArea(0,0,"20%","30%");
	 * Rectangle subrect = getSubAreaRectangle(rectangle, subarea.toString());
	 * }
	 * </pre>
	 */
	public static class SubArea{
		private String topLefX;
		private String topLefY;
		private String bottomRightX;
		private String bottomRightY;
		
		public SubArea(String topLefX, String topLefY, String bottomRightX, String bottomRightY){
			this.topLefX = topLefX;
			this.topLefY = topLefY;
			this.bottomRightX = bottomRightX;
			this.bottomRightY = bottomRightY;
		}
		
		public SubArea(double topLefX, double topLefY, double bottomRightX, double bottomRightY){
			this.topLefX = Double.toString(topLefX);
			this.topLefY = Double.toString(topLefY);
			this.bottomRightX = Double.toString(bottomRightX);
			this.bottomRightY = Double.toString(bottomRightY);
		}
		
		public SubArea(double topLefX, double topLefY, String bottomRightX, String bottomRightY){
			this.topLefX = Double.toString(topLefX);
			this.topLefY = Double.toString(topLefY);
			this.bottomRightX = bottomRightX;
			this.bottomRightY = bottomRightY;
		}
		
		public SubArea(String topLefX, String topLefY, double bottomRightX, double bottomRightY){
			this.topLefX = topLefX;
			this.topLefY = topLefY;
			this.bottomRightX = Double.toString(bottomRightX);
			this.bottomRightY = Double.toString(bottomRightY);
		}
		
		public String toString(){
			//x1,y1,x2,y2
			return topLefX+StringUtils.COMMA+topLefY+StringUtils.COMMA+bottomRightX+StringUtils.COMMA+bottomRightY;
		}
	}
	
    /**
     * Given the compRect Rectangle and
     * the subarea string, return a new, valid Rectangle that represents the SubArea.  This function
     * may call getClippedSubAreaRectangle() to clip the new Rectangle if its dimensions paritally 
     * fall outside of the compRect Rectangle.  If the defined subarea or the new clipped Rectangle 
     * are completely outside the bounds of compRect then a null is returned.
     * 
     * @param                     Rectangle compRect; the source Rectangle to get the subarea from.
     * @param                     String subarea; the portion of compRect in question.
     * subarea coors can be semi-colon delimited (x;2;3;4) or comma-delimited (1,2,3,4).
     * subarea must be in the format of a top-left and bottom-right pair, and can be either absolute 
     * coordinates or percentages ("x1,y1,x2,y2" or "x1%,y1%,x2%,y2%"). Coordinates are relative to 
     * compRect, i.e. (0,0) and (0%,0%) are the minimum, and (compRect.width, compRect.height) 
     * and (100%,100%) are the maximum. Values exceeding the minimun/ maximum are forced to the nearest 
     * limiting value. Absolute and percent values can be mixed, so "0,0,50%,50%" is valid.
     * 
     * @return                    Rectangle subRect; a new portion of the component to be captured, or
     *                             null if the new subRect is invalid.
     * @author bolawl				-Added 10.21.2005 (RJL)
     *
     **/
    public static Rectangle getSubAreaRectangle(Rectangle compRect, String subarea) {
    	String methodName = "getSubAreaRectangle";
    	int prevCommaAt;
    	int nextCommaAt;
    	
    	try {
    		//test for negative values (coords processing will catch any illegal alpha chars)
    		if (subarea.indexOf(StringUtils.MINUS) >= 0)
    			return null;
    		
    		// allow semi-colons for comma-delimited test records
    		subarea = StringUtilities.findAndReplace(subarea, StringUtils.SEMI_COLON, StringUtils.COMMA);
    		
    		//parse through subarea string
    		nextCommaAt = subarea.indexOf(StringUtils.COMMA);
    		String sx1 = subarea.substring(0, nextCommaAt).trim();
    		prevCommaAt = nextCommaAt;
    		nextCommaAt = subarea.indexOf(StringUtils.COMMA, prevCommaAt + 1);
    		String sy1 = subarea.substring(prevCommaAt + 1, nextCommaAt).trim();
    		prevCommaAt = nextCommaAt;
    		nextCommaAt = subarea.indexOf(StringUtils.COMMA, prevCommaAt + 1);
    		String sx2 = subarea.substring(prevCommaAt + 1, nextCommaAt).trim();
    		prevCommaAt = nextCommaAt;
    		String sy2 = subarea.substring(prevCommaAt + 1).trim();
    		
    		//calc coords of SubArea Rectangle based on compRect
    		double dx1, dx2, dy1, dy2;
    		dx1 = calculateAbsoluteCoordinate(compRect.getX(), compRect.getWidth(), sx1);
    		dy1 = calculateAbsoluteCoordinate(compRect.getY(), compRect.getHeight(), sy1);
    		dx2 = calculateAbsoluteCoordinate(compRect.getX(), compRect.getWidth(), sx2);
    		dy2 = calculateAbsoluteCoordinate(compRect.getY(), compRect.getHeight(), sy2);

    		//build new SubArea Rectangle
    		Rectangle subRect = new Rectangle();
    		subRect.setRect(dx1, dy1, (int)(dx2 - dx1) + 1, (int)(dy2 - dy1) + 1);
    		//log.logMessage(testRecordData.getFac(), "compRect is " + compRect.toString());
    		//log.logMessage(testRecordData.getFac(), "subRect is " + subRect.toString());
    		
    		//if SubRect's x,y is not contained in compRect, invalid
    		if (! compRect.contains(dx1, dy1)){
    			IndependantLog.warn("IU.getSubAreaRectangle subarea location "+ dx1 +", "+ dy1 +" is outside the bounds of the target Rectangle.");
    			return null;
    		}
    		
    		//validate subRect is already contained in compRect
    		if (compRect.contains(subRect)){
    			//return newly created subRect
    			return subRect;
    		}else{
    			//subRect doesn't fit in compRect, return new clipped Rectangle
    			Rectangle t = getClippedSubAreaRectangle(compRect, subRect);
    			if(t==null){
        			IndependantLog.warn("IU.getSubAreaRectangle clipped subarea ("+dx1+","+dy1+" x "+subRect.width+","+subRect.height+") is outside the bounds of the target Rectangle.");
    			}
    			return t;
    		}
    	}
    	catch (Exception e) {
    		Log.error(methodName +": Exception", e);
    		return null;
    	}
    }
    
    /**
     * Caculate an absolute coordinate according to (x, width)/(y, height) and offset.<br>
     * @param xOrY double, the x/y of a rectangle.
     * @param wOrH double, the width/height of a rectangle.
     * @param offset String, the offset
     * @return double, an absolute coordinate of an rectangle
     */
    public static double calculateAbsoluteCoordinate(double xOrY, double wOrH, String offset) throws Exception{
    	double absoluteCoordinate = 0;
    	int index = offset.indexOf(StringUtils.PERCENTAGE);
		if(index<0){//relative coords
			absoluteCoordinate = xOrY + Double.parseDouble(offset);
		}else{//percent offset
			String value = offset.substring(0, index).trim();
			absoluteCoordinate = value.equals("0")? xOrY : (xOrY - 1 + Double.parseDouble(value)/100*wOrH);
			
//			double percentage = Double.parseDouble(offset.substring(0, index))/100;
//			absoluteCoordinate = xOrY + percentage*wOrH;
//			if((absoluteCoordinate-xOrY-wOrH)<0.001) absoluteCoordinate -= 1;//the border
		}
		return absoluteCoordinate;
    }
    
	/**
	 * Recaptures certain properties and the graphical contents of the screen.
	 * Uses the java.awt.Robot to capture the full contents of the screen.
	 * Makes available internally, or through the public API:
	 * <ul>
	 * <li>screenSize
	 * <li>screenRect
	 * <li>screenImage  (BufferedImage)
	 * <li>screenRaster
	 * <li>screenModel  (SampleModel)
	 * <li>screenBuffer (DataBuffer)
	 * <li>screenColor  (ColorModel)
	 * <li>screenBands  (ARGB layers, per se)
	 * <li>screenWidth
	 * <li>screenHeight
	 * 
	 * @throws AWTException if instantiation of the java.awt.Robot throws it.
	 * @see #getScreenImage()
	 * @see #screenHeight
	 * @see #screenWidth
	 */
	public static void recaptureScreen()throws AWTException
	{
		java.awt.Robot robot = org.safs.robot.Robot.getRobot();
		if(toolkit==null)toolkit = Toolkit.getDefaultToolkit();
		screenSize = toolkit.getScreenSize();
		screenRect = new Rectangle(0,0,screenSize.width, screenSize.height);
		resetScreenData();
		int retry = 0;
		while(screenImage==null && retry++ < 10){
			try{
				screenImage = robot.createScreenCapture(screenRect);
				//LeiWang do we need a copy having the Raster of same size?
				//screenImage = copy(screenImage);
			}catch(Exception x){
				Log.debug("IU handling AWT Robot Exception #"+ retry);
				try{Thread.sleep(100);}catch(InterruptedException n){;}
			}
		}
		if (screenImage==null) throw new AWTException("IU java.awt.Robot unable to capture screen!");
		Log.info("IU took a NEW SCREEN SNAPSHOT!");
		screenImageType = screenImage.getType();
		screenRaster = screenImage.getRaster();
		screenModel = screenRaster.getSampleModel();
		if(debug)Log.info("Screen Image Type: "+ screenImageType);
		if(debug)Log.info("Screen SampleModel Class:"+ screenModel.getClass().getSimpleName());
		screenBuffer = (DataBufferInt) screenRaster.getDataBuffer();
		if(debug)Log.info("Screen DataBuffer Class:"+ screenBuffer.getClass().getSimpleName());
		screenColor = screenImage.getColorModel();
		if(debug)Log.info("Screen ColorModel Class:"+ screenColor.getClass().getSimpleName());
		
		screenWidth = screenModel.getWidth();
		screenHeight = screenModel.getHeight();
		screenBands = screenModel.getNumBands();
		if(debug)Log.info("Screen NumBands: "+ screenBands);		
	}

	/**
	 * Retrieve a BufferedImage of an area of the screen.
	 * @param screenRect
	 * @return BufferedImage
	 * @throws AWTException if java.awt.Robot AWTException occurs
	 */
	public static BufferedImage captureScreenArea(Rectangle screenRect)throws AWTException{
		java.awt.Robot robot = org.safs.robot.Robot.getRobot();
		return robot.createScreenCapture(screenRect);
	}
	
	/**
	 * Return the rectangle of the whole screen.<br>
	 * @return Rectangle, the rectangle of the screen
	 */
	public static Rectangle getScreenSize(){
		if(toolkit==null) toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		return new Rectangle(0,0,screenSize.width, screenSize.height);
	}
	
	/**
	 * Return the latest BufferedImage captured for the screen.
	 * If no screenImage has been captured then this routine will 
	 * also initiate that first capture.
	 * @return BufferedImage representing the entire screen.
	 * @throws java.awt.AWTException if instantiating the java.awt.Robot throws it
	 * @see #recaptureScreen()
	 */
	public static BufferedImage getScreenImage()throws java.awt.AWTException{
		if(screenImage==null) recaptureScreen();
		return screenImage;
	}
	
	/**
	 * Attempts to create\load a BufferedImage from an image stored in the File system.
	 * Using JAI API to read image file, it is supposed to support BMP, FPX, GIF, JPEG,PNG,PNM and TIFF.
	 * The TIFF operation supports the compression types: 
	 * 1) no compression 2)PackBits compression 3)Modified Huffman compression (CCITT Group3 1-dimensional facsimile)
	 *
	 * @param fullpath case-insensitive absolute path to the image on file.
	 * @return BufferedImage representing the stored image.
	 * @throws FileNotFoundException if the file does not exist or cannot be read.
	 * @throws IOException if ImageIO has a problem reading the file or file format.
	 * @see java.awt.image.BufferedImage
	 * @see javax.imageio.ImageIO#read(File)
	 */
	public static BufferedImage getStoredImage(String fullpath)throws FileNotFoundException,IOException
	{
		File file = new CaseInsensitiveFile(fullpath).toFile();
		if(file.canRead()){
			try{
				//After JAI reading file, sometimes the image-file cannot be deleted, Use ImageIO firstly
				return ImageIO.read(file);
			}catch(Exception e){
				IndependantLog.warn("IU.getStoredImage ImageIO.read failing with "+ StringUtils.debugmsg(e));
				//abandon ImageIO.read(file), and use JAI API as following instead for supporting more image types including compressed TIFF
				try{ 
					if(jailistener == null){
						jailistener = new JAIImagingListener();
						JAI.getDefaultInstance().setImagingListener(jailistener);
					}
					RenderedOp img =  JAI.create("fileload", file.getAbsolutePath());
					if(img != null) return img.getAsBufferedImage(); 
				}
				catch(Exception nf){
					IndependantLog.debug("IU.getStoredImage JAI read failing with "+ nf.getClass().getName()+", "+nf.getMessage());
				}
			}
			
		}
		throw new FileNotFoundException(fullpath);
	}

	/**
	 * Attempts to determine the width and height of the stored image.
	 * @param fullpath case-insensitive absolute path to the image on file.
	 * @return Dimension width and height of the image or null.
	 * @throws FileNotFoundException if the file does not exist or cannot be read.
	 * @throws IOException if ImageIO has a problem reading the file or file format.
	 * @see java.awt.image.BufferedImage
	 * @see javax.imageio.ImageIO#read(File)
	 */
	public static Dimension getStoredImageDimension(String fullpath)throws FileNotFoundException,IOException
	{
		File file = new CaseInsensitiveFile(fullpath).toFile();
		if(file.canRead()){
			BufferedImage i = ImageIO.read(file);
			Dimension d = new Dimension(i.getWidth(), i.getHeight());
			i = null;
			return d;
		}
		throw new FileNotFoundException(fullpath);
	}
	
	/**
	 * Compare the sourceImage and targetImage.
	 * @param sourceImage BufferedImage, the bench image
	 * @param targetImage BufferedImage, the test image
	 * @param percentBitsTolerance int, the percentage of bits need to be the same. it is between 0 and 100.
	 *                                  100 means only 100% match, 2 images will be considered matched;
	 *                                  0 means even no bits match, 2 images will be considered matched.
	 * @return boolean, if the 2 images match.
	 * @see #compareImage(BufferedImage, BufferedImage, Rectangle, Rectangle, int)
	 */
	public static boolean compareImage(BufferedImage sourceImage, BufferedImage targetImage, int percentBitsTolerance){
		return compareImage(sourceImage, targetImage, null, null, percentBitsTolerance);
	}
	
	/**
	 * Compare the sourceImage and targetImage, or compare a certain area of them.
	 * @param sourceImage BufferedImage, the bench image
	 * @param targetImage BufferedImage, the test image
	 * @param sourceRect Rectangle, the area on source image to compare; if null, compare whole image
	 * @param targetRect Rectangle, the area on target image to compare; if null, compare whole image
	 * @param percentBitsTolerance int, the percentage of bits need to be the same. it is between 0 and 100.
	 *                                  100 means only 100% match, 2 images will be considered matched;
	 *                                  0 means even no bits match, 2 images will be considered matched.
	 * @return boolean, if the 2 images (or certain area) match.
	 * @see #compareImage(BufferedImage, BufferedImage, int)
	 * @see #_compareImageWithThread(BufferedImage, BufferedImage, int, int)
	 */
	public static boolean compareImage(BufferedImage sourceImage, BufferedImage targetImage,
			                           Rectangle sourceRect, Rectangle targetRect, int percentBitsTolerance){
		String debugmsg = StringUtils.debugmsg(false);
		
		if(sourceImage==targetImage && (sourceRect==targetRect||sourceRect.equals(targetRect)) ) return true;
		if(sourceImage==null || targetImage==null) return false;
		
		int sourceImageW = sourceImage.getWidth();
		int sourceImageH = sourceImage.getHeight();
		if(sourceRect!=null){
			if(sourceRect.x+sourceRect.width>sourceImageW || sourceRect.y+sourceRect.height>sourceImageH){
				IndependantLog.warn(debugmsg+"the compare-rectangle '"+sourceRect+"' goes outside of source image (w="+sourceImageW+", h="+sourceImageH+") itself.");
				//we adjust the width and height of the search rectangle, make the search-rectangle smaller
				sourceRect.width = sourceImageW-sourceRect.x;
				sourceRect.height = sourceImageH-sourceRect.y;
			}
			
			sourceImage=sourceImage.getSubimage(sourceRect.x, sourceRect.y, sourceRect.width, sourceRect.height);
			sourceImageW = sourceImage.getWidth();
			sourceImageH = sourceImage.getHeight();
		}
		
		int targetImageW = targetImage.getWidth();
		int targetImageH = targetImage.getHeight();
		if(targetRect!=null){
			if(targetRect.x+targetRect.width>targetImageW || targetRect.y+targetRect.height>targetImageH){
				IndependantLog.warn(debugmsg+"the compare-rectangle '"+targetRect+"' goes outside of target image (w="+targetImageW+", h="+targetImageH+") itself.");
				//we adjust the width and height of the search rectangle, make the search-rectangle smaller
				targetRect.width = targetImageW-targetRect.x;
				targetRect.height = targetImageH-targetRect.y;
			}
			
			targetImage=targetImage.getSubimage(targetRect.x, targetRect.y, targetRect.width, targetRect.height);
			targetImageW = targetImage.getWidth();
			targetImageH = targetImage.getHeight();
		}
		
		//If the image is small enough, we don't need to divide it into many pieces
		int pieces = DIVIDE_PIECES;
		if(targetImageW*targetImageH<IMAGE_SIZE_PIXEL_THREADSHOLD) pieces = 1;
		return _compareImageWithThread(sourceImage, targetImage, percentBitsTolerance, pieces);
	}
	
	/**
	 * 
	 * @param sourceImage BufferedImage, the bench image
	 * @param targetImage BufferedImage, the test image
	 * @param percentBitsTolerance int, the percentage of bits need to be the same. it is between 0 and 100.
	 *                                  100 means only 100% match, 2 images will be considered matched;
	 *                                  0 means even no bits match, 2 images will be considered matched.
	 * @param pieces int, it decides how many blocks to divide the image to compare by multiple threads.
	 *                    the image will be divided to "pieces*pieces" blocks to compare. 
	 * @return boolean, if the 2 images match.
	 */
	private static boolean _compareImageWithThread(BufferedImage sourceImage, BufferedImage targetImage,
			                                       int percentBitsTolerance, int pieces){
		String debugmsg = StringUtils.debugmsg(false);
		
		if(targetImage==null || sourceImage==null) return false;
		if(sourceImage.getWidth()!=targetImage.getWidth() 
		|| sourceImage.getHeight()!=targetImage.getHeight()) return false;
		
		if(pieces<0){
			IndependantLog.warn(debugmsg+" divided pieces '"+pieces+"' is negative. reset it to "+DIVIDE_PIECES);
			pieces = DIVIDE_PIECES;
		}
		
		boolean matched = false;
		int imagew = targetImage.getWidth();
		int imageh = targetImage.getHeight();
		int maxPixels = imagew*imageh;
		int maxPixelErrors = 0;

		if(percentBitsTolerance<100 && percentBitsTolerance>0){
			double dblerrors = ((100 - (double)percentBitsTolerance)/100)* maxPixels;
			maxPixelErrors = (int)Math.round(dblerrors);
			int matchpixels = maxPixels - maxPixelErrors;
			Log.info(debugmsg+"must match "+ matchpixels +" of "+ maxPixels +" pixels.");
		}else if(percentBitsTolerance==0){
			Log.info(debugmsg+" no need to match any pixels.");
			return true;
		}else{
			Log.info(debugmsg+"must match ALL "+ maxPixels +" pixels.");
		}
		
		//Decompose the picture into multiple blocks
		Rectangle[] blocks = new Rectangle[pieces*pieces];
		int pieceWidth = imagew/pieces;
		int pieceHeight = imageh/pieces;
		int tmpWidth=0, tmpHeight=0;

		//For the most outer blocks, we need to re-calculate it height and width
		for(int i=0;i<pieces;i++){
			if(i==pieces-1) tmpWidth = imagew-pieceWidth*i;
			else tmpWidth = pieceWidth;

			for(int j=0;j<pieces;j++){
				if(j==pieces-1) tmpHeight = imageh-pieceHeight*j;
				else tmpHeight = pieceHeight;

				blocks[i*pieces+j] = new Rectangle(pieceWidth*i,pieceHeight*j,tmpWidth,tmpHeight);
				IndependantLog.debug(debugmsg+"block "+(i*pieces+j)+": "+blocks[i*pieces+j]);
			}
		}

		//Use multiple threads to compare each image-block parallelly
		Log.debug(debugmsg+"Main thread: Comparing multiple blocks paralelly ...");
		SmallPieceComparator[] threads = new SmallPieceComparator[pieces*pieces];
		SharedLock lock = new SharedLock(maxPixelErrors, threads.length,true);
		for(int k=0;k<threads.length;k++){
			threads[k] = new SmallPieceComparator(sourceImage, targetImage, lock, blocks[k], 0,-1,0,-1,false);
			threads[k].start();
		}
		try{
			synchronized (lock) {
				//Wait for all threads finish
				while (lock.hasRunningThreads()) {
					Log.debug(debugmsg+"Main thread is waitting block-search-threads .......  ");
					lock.setWaiting(true);
					lock.wait();
					Log.debug(debugmsg+"Main thread has been notified .......  ");
					if(!lock.isMatched()){
						Log.debug(debugmsg+"One of blocks is not matched, images are different .......  ");
						// If one thread has found the matched image, we just stop the other threads
						for (int i = 0; i < threads.length; i++) {
							if (threads[i].isAlive()) {
								threads[i].setInterrupted(true);
							}
						}
						break;
					}
				}
				matched = lock.isMatched();
			}
		}catch(Exception e){
			IndependantLog.error(debugmsg+" fail due to "+StringUtils.debugmsg(e));
		}
		
		return matched;
	}
	
	/**
	 * Attempts to locate the provided image within the latest screenshot and return 
	 * its location and size, if found.  Use the current Bit Tolerance settings.
	 * @param image -- BufferedImage to locate within the latest screenshot.
	 * @param searchRect --limit the search area.  Should be null to search the whole screen.
	 * @param nth -- the nth instance of a match.  (1) first, by default. 
	 * 			     It may be -1, which means user specify "Index=any" and he wants to use multi-thread-search;
	 *               It may be 0, which means user don't give "Index=N", SAFS will decide to use multi or single
	 *               thread according to value of USE_MULTIPLE_THREADS.
	 *               
	 *               This parameter should be assigned to 0 when calling by other method 
	 *               without knowing the real index, so that the multi-thread-search can be
	 *               profited when USE_MULTIPLE_THREADS is true.
	 * @return Rectangle where image was found, or null  
	 * @throws IllegalArgumentException if the searchRect is not valid. (null will not throw this)
	 */
	public static Rectangle findBufferedImageOnScreen(BufferedImage image, Rectangle searchRect, int nth) throws IllegalArgumentException,ImagingOpException,AWTException{
		int startx = 0;
		int starty = 0;
		if(screenWidth < 1) {
			Log.info("IU first-time recaptureScreen invoked in findBufferedImageOnScreen.");
			recaptureScreen();
		}
		int searchWidth = screenWidth;
		int searchHeight = screenHeight;
		
		// default to finding the first match
		int seeki = nth;
		if (seeki < 1) seeki=1;
		
		// validate or clip searchRect to screen 
		if (!(searchRect==null)){
			startx = (searchRect.x < 0) ? 0: (searchRect.x < screenWidth) ? searchRect.x:-1; 
			if (startx == -1) throw new IllegalArgumentException(FAILStrings.convert(FAILStrings.BAD_PARAM, 
						      "Bad parameter value for SearchRect.x", "SearchRect.x"));
			starty = (searchRect.y < 0) ? 0: (searchRect.y < screenHeight) ? searchRect.y:-1; 
			if (starty == -1) throw new IllegalArgumentException(FAILStrings.convert(FAILStrings.BAD_PARAM, 
						      "Bad parameter value for SearchRect.y", "SearchRect.y"));
			searchWidth = (searchRect.width < 1) ? -1 : 
				          (startx + searchRect.width <= screenWidth) ? 
				          searchRect.width : screenWidth - startx; 
			if (searchWidth == -1) throw new IllegalArgumentException(FAILStrings.convert(FAILStrings.BAD_PARAM, 
						      "Bad parameter value for SearchRect.width", "SearchRect.width"));
			searchHeight = (searchRect.height < 1) ? -1 : 
		          (starty + searchRect.height <= screenHeight) ? 
		          searchRect.height : screenHeight - starty; 
			if (searchHeight == -1) throw new IllegalArgumentException(FAILStrings.convert(FAILStrings.BAD_PARAM, 
				      "Bad parameter value for SearchRect.height", "SearchRect.height"));
		}
		
		//LeiWang do we need a copy having the Raster of same size?
		//image = copy(image);
		int imageType = image.getType();
		if(imageType != screenImageType){
			BufferedImage tempImg = convertImage(image, screenImageType);
			if(tempImg!=null){
				imageType = screenImageType;
				image = tempImg;
				Log.debug("IU successfully converted stored image to match screenshot imageType "+ screenImageType);
			}else{
				Log.warn("IU stored image type '"+imageType+"' is different than screenshot image type "+ screenImageType);
			}
		}
		
		Log.info("IU FindBufferedImage searching:"+ startx +","+ starty +","+ searchWidth+","+searchHeight);
		//get the Raster and SampleModel
		Raster imageRaster = image.getRaster();
		SampleModel imageModel = imageRaster.getSampleModel();
		if(debug) Log.info("Image Type:"+ imageType);
		if(debug) Log.info("Image SampleModel class:"+ imageModel.getClass().getSimpleName());
		DataBuffer imageBuffer = imageRaster.getDataBuffer();
		if(debug) Log.info("Image DataBuffer class:"+ imageBuffer.getClass().getSimpleName());
		ColorModel imageColor = image.getColorModel();
		if(debug) Log.info("Image ColorModel class:"+ imageColor.getClass().getSimpleName());
		
		int imageBands = imageModel.getNumBands();
		if(debug) Log.info("Image NumBands: "+ imageBands);

		int imageWidth = imageModel.getWidth();
		// It is probably desirable to find an image that may start in-bounds but finishes out
		//if (searchWidth < imageWidth){ 
		//	Log.debug("IU FindBufferedImage aborting due to Developer's questionable width restrictions.");
		//  throw new IllegalArgumentException(FAILStrings.convert(FAILStrings.BAD_PARAM, 
		//	      "Bad parameter value for SearchRect.width", "SearchRect.width"));
		//}

		int imageHeight = imageModel.getHeight();
		// It is probably desirable to find an image that may start in-bounds but finishes out
		//if (searchHeight < imageHeight) {
		//	Log.debug("IU FindBufferedImage aborting due to Developer's questionable height restrictions.");
		//	throw new IllegalArgumentException(FAILStrings.convert(FAILStrings.BAD_PARAM, 
		//		      "Bad parameter value for SearchRect.height", "SearchRect.height"));
		//}
		Log.info("IU searching for image of width="+ imageWidth +", height="+ imageHeight);
		
		// prepare bit tolerance usage
		int maxPixels = imageWidth * imageHeight;
		int maxPixelErrors = 0;
		if(useBitsTolerance && (percentBitsTolerance < 100) && (percentBitsTolerance > 0)){
			double dblerrors = ((100 - (double)percentBitsTolerance)/100)* maxPixels;
			maxPixelErrors = (int)Math.round(dblerrors);
			int matchpixels = maxPixels - maxPixelErrors;
			Log.info("IU search must match "+ matchpixels +" of "+ maxPixels +" pixels.");
		}else{
			Log.info("IU search must match ALL "+ maxPixels +" pixels.");
		}
		
		// the use of this Thread pool is presently discouraged and disabled by default
//		if(useBitsTolerance && USE_MULTIPLE_THREADS){
//			return findImageWithXYBTThreadPool(startx, starty, searchWidth, searchHeight, maxPixelErrors, imageBuffer, imageModel, nth);	
//		}
		//If user specify the index to indicate which image he wants,
		//we should not use the multi-thread to search, as multi-thread can't guarantee the order of image found
		//But user can specify "index=any" to hint that he doesn't care the order of image found. parameter nth will be -1.
		Log.debug("UI USE_MULTIPLE_THREADS = "+USE_MULTIPLE_THREADS+"; nth="+nth+"; seeki="+seeki);
		//If we don't specify "Index=Nth" in Recognition String, use "USE_MULTIPLE_THREADS" to decide if apply mutli-thread-search
		if(nth==0){
			if(USE_MULTIPLE_THREADS){
				return findImageWithXYBTThread(startx, starty, searchWidth, searchHeight, maxPixelErrors, image, imageBuffer, imageModel);
			}
			//else USE_MULTIPLE_THREADS is false, we will use single thread to search the 1th matched image as seeki is 1.
		//If we specify "Index=Any" in image Recognition String, we use multi-thread-search
		}else if(nth==-1){
			return findImageWithXYBTThread(startx, starty, searchWidth, searchHeight, maxPixelErrors, image, imageBuffer, imageModel);
		}
		
		// allow the START of the matching image to begin anywhere in the searchRect,
		// even if the overall width and\or height extends beyond the bounds of the searchRect
		int imageMaxScreenX = startx + searchWidth-1; 		
		int imageMaxScreenY = starty + searchHeight-1; 		
		
		int screenx = startx;
		int screeny = starty;
		boolean matched = false;
		try{			
			for(screeny = starty;screeny < imageMaxScreenY;screeny++){
				screenx = startx;
				while(screenx < imageMaxScreenX && !matched){
					matched = isScreenXYMatch(screenBuffer, screenModel, imageBuffer, imageModel, screenx, screeny, maxPixelErrors);
					if(!matched) 
						screenx++;
					else{
						if(--seeki < 1){
							//break;// do nothing. will exit loop normally
						}else {
							Log.info("IU findBufferedImageOnScreen looking for next indexed match...");
							matched = false;
							screenx++;
						}
					}
				}
				// break if all rows of image data matched same screen data for the nth match
				if (matched){
					break;
				}
			}
		}catch(Exception x){
			Log.debug("IU findBufferedImageOnScreen Exception:", x);			
		}
		if ( !matched ){
			Log.info("IU findBufferedImageOnScreen did not find the image.");
			Log.info("IU closest match was at "+ closestX+","+closestY+" with "+closestMatchCount+" of "+ closestMatchesOf +" matching for "+closestPercentage+"%");
			return null;
			//throw new ImagingOpException(IMAGE_NOT_FOUND);
		}
		return new Rectangle(screenx, screeny, imageWidth, imageHeight);		
	}

	/**
	 * Generally called from findBufferedImageOnScreen and not normally called directly.
	 * Uses a Thread pool to attempt to take advantage of multi-processor systems.
	 * This particular algorithm uses multiple ScreenXYBTComparators--one for each screen pixel anchor point.
	 * <p>
	 * Initial testing seems to suggest that using one thread per pixel anchor on the screen is too fine 
	 * grained.  The overhead of thread maintenance appears to be too great--making the performance 
	 * poorer than simply using a single-threaded algorithm.  For this reason, the use of the algorithm is 
	 * presently discouraged and is disabled, by default.
	 * 
	 * @param startx - x-coord on screen to start the search
	 * @param starty - y-coord on screen to start the search
	 * @param searchWidth - width of area from startx to search
	 * @param searchHeight - height of area from starty to search
	 * @param maxPixelErrors - max number of pixels that can fail comparison
	 * @param imageBuffer - DataBuffer of the target image
	 * @param imageModel - SampleModel of the target image
	 * @param nth - normally 1.  Find the nth match of the image on screen.
	 * 
	 * @return Rectangle location where the image was found or null.
	 * @throws IllegalArgumentException
	 * @throws ImagingOpException
	 * @throws AWTException
	 * @see {@link #findBufferedImageOnScreen(BufferedImage, Rectangle, int)}
	 * @see java.awt.image.DataBuffer
	 * @see java.awt.image.SampleModel
	 */
    protected static Rectangle findImageWithXYBTThreadPool(int startx, int starty, 
    		                                     int searchWidth, int searchHeight, 
    		                                     int maxPixelErrors,
    		                                     DataBuffer imageBuffer,
    		                                     SampleModel imageModel,
    		                                     int nth) throws IllegalArgumentException,ImagingOpException,AWTException{
    	Rectangle result = null;
		int seeki = nth;
		if (seeki < 1) seeki=1;
				
		// allow the START of the matching image to begin anywhere in the searchRect,
		// even if the overall width and\or height extends beyond the bounds of the searchRect
		int imageMaxScreenX = startx + searchWidth-1; 		
		int imageMaxScreenY = starty + searchHeight-1; 		
		
		int screenx = startx;
		int screeny = starty;
		boolean matched = false;
		try{				
			int tindex = 0;
			int processors = Runtime.getRuntime().availableProcessors();
			int threads = processors;
			if(threads < 1) threads = 1;
			if(threads > imageMaxScreenX) threads = imageMaxScreenX;
			ExecutorService pool = Executors.newFixedThreadPool(threads);

			ScreenXYBTComparator[] compare = new ScreenXYBTComparator[threads];
			
			for(screeny = starty;screeny < imageMaxScreenY;screeny++){
				screenx = startx;
				tindex = 0;
				while(screenx < imageMaxScreenX && !matched){
					// only go multi-threaded if Bits Tolerance is ON, at this time.
					//if(USE_MULTIPLE_THREADS){
					// if thread is null then start it up
					if(compare[tindex]==null) {
						compare[tindex] = new ScreenXYBTComparator(screenBuffer, screenModel, imageBuffer, imageModel, screenx, screeny, maxPixelErrors);
						pool.execute(compare[tindex++]);
						// only increment screenx if another new thread will be created next loop
						if(tindex==compare.length){
							tindex = 0;
						}else{
							// don't increment screenx if it will end the loop before threads are done.
							if (screenx +1 < imageMaxScreenX) screenx++;
						}
					}else if (!compare[tindex].isDone()){
						tindex++;
						if(tindex == compare.length){
							tindex = 0;
						}
					}else{//thread IS done
						matched = compare[tindex].getResult();
						if(matched){// ends the while loop
							screenx = compare[tindex].getScreenX();
							//pool.shutdownNow();
						}else{
							// if there is more screenx to test then
							// restart the same thread with the next screenx
							if(screenx +1 < imageMaxScreenX){
								compare[tindex].prepare(++screenx, screeny);
								pool.execute(compare[tindex++]);
								if(tindex==compare.length)tindex = 0;
							}else{//check if ANY are still running
								int i = 0;
								for( ;i<compare.length;i++){
									if(! compare[i].isDone()){
										tindex++;
										if(tindex==compare.length)tindex = 0;
										break;
									}
									matched = compare[i].getResult();
									if(matched){
										screenx = compare[i].getScreenX();
										//pool.shutdownNow();
										break;
									}
								}
								// set screenx to abort the while loop if no more to check
								if (i == compare.length && !matched) screenx = imageMaxScreenX;
							}
						}
					}
					if (matched){
						if(--seeki < 1){
							//break;// do nothing. will exit loop normally
						}else {
							Log.info("IU findWithXYBTThreadPool looking for next indexed match...");
							matched = false;
							screenx++;
							tindex  = 0;
							for(int i=0;i<compare.length;i++) compare[i]= null;
						}
					}
				}
				// break if all rows of image data matched same screen data for the nth match
				if (matched){
					break;
				}
			}
		}catch(Exception x){
			Log.debug("IU findWithXYBTThreadPool encountered Exception:", x);			
		}
		if(matched){
			result = new Rectangle(screenx, screeny, imageModel.getWidth(),imageModel.getHeight());
		}else{
			Log.info("IU findWithXYBTThreadPool did not find the image.");			
		}
		return result;
    }
    
	/**
	 * Generally called from findBufferedImageOnScreen and not normally called directly.
	 * Use multiple-thread algorithm to search an image on screen.
	 * The search algorithm is different according to the size of the picture.
	 * If the size is big:
	 *   We will divide the picture to small blocks, for each block, we use
	 *   SmallPieceComparator to create a thread to compare with according screen location.
	 *   If all blocks can match, which means the whole picture match.
	 * If the size is small:
	 *   We will divide the search rectangle to small pieces, for each small search
	 *   rectangle, we use SmallPieceComparator to create a thread to search the whole picture
	 *   in that rectangle.
	 *   If one of the thread find a matched picture in its search rectangle, which means 
	 *   the search is ok, we can stop the other threads.
	 * <p>
	 * 
	 * @param startx - x-coord on screen to start the search
	 * @param starty - y-coord on screen to start the search
	 * @param searchWidth - width of area from startx to search
	 * @param searchHeight - height of area from starty to search
	 * @param maxPixelErrors - max number of pixels that can fail comparison
	 * @param image		- BufferedImage of the target image
	 * @param imageBuffer - DataBuffer of the target image
	 * @param imageModel - SampleModel of the target image
	 * 
	 * @return Rectangle location where the image was found or null.
	 * @throws IllegalArgumentException
	 * @throws ImagingOpException
	 * @throws AWTException
	 * @see {@link #findBufferedImageOnScreen(BufferedImage, Rectangle, int)}
	 * @see java.awt.image.DataBuffer
	 * @see java.awt.image.SampleModel
	 * @see org.safs.image.SharedLock
	 * @see org.safs.image.SmallPieceComparator
	 */
    protected static Rectangle findImageWithXYBTThread(int startx, int starty, 
    		int searchWidth, int searchHeight, 
    		int maxPixelErrors,
    		BufferedImage image,
    		DataBuffer imageBuffer,
    		SampleModel imageModel) throws IllegalArgumentException,ImagingOpException,AWTException{
    	Rectangle result = null;
    	//pieces represents the number of threads to be created.If pieces is 4, then 16 threads will be created.
    	int pieces = DIVIDE_PIECES;
    	int minPixel = screenModel.getWidth()*screenModel.getHeight()/(pieces*pieces);
    	int imagePixel = imageModel.getWidth()*imageModel.getHeight();
    	
    	try{
    		//Use different strategy to search a matched image
    		//1. If the image is small, we will decompose the search rectangle to 
    		//   several small search areas, within each search area, we use a thread to search
    		if(imagePixel<minPixel){
    			result = findSmallImageWithXYBTThread(startx,starty,searchWidth,searchHeight,maxPixelErrors,imageBuffer,imageModel,pieces);
    		}
    		//2. If the image is big, we will decompose the image to small blocks, for each block
    		//	 we create a thread to match with the screen
    		else{
    			result = findBigImageWithXYBTThread(startx,starty,searchWidth,searchHeight,maxPixelErrors,image,imageBuffer,imageModel,pieces);
    		}
    	}catch(Exception x){
    		Log.debug("IU findImageWithXYBTThread encountered Exception:", x);			
    	}
    	return result;
    }
    
    /**
     * We will divide the search rectangle to small pieces, for each small search
	 * rectangle, we use SmallPieceComparator to create a thread to search the whole picture
	 * in that rectangle.
	 * If one of the thread find a matched picture in its search rectangle, which means 
	 * the search is ok, we can stop the other threads.<p>
	 *   
	 * @param startx - x-coord on screen to start the search
	 * @param starty - y-coord on screen to start the search
	 * @param searchWidth - width of area from startx to search
	 * @param searchHeight - height of area from starty to search
	 * @param maxPixelErrors - max number of pixels that can fail comparison
	 * @param imageBuffer - DataBuffer of the target image
	 * @param imageModel - SampleModel of the target image
     * @param pieces -  pieces*pieces will be the number of thread or
     *                                        the number of block the search rectangle to be divided to
     *                                        
     * @return Rectangle location where the image was found or null.
     * @throws InterruptedException
     */
    private static Rectangle findSmallImageWithXYBTThread(int startx, int starty, 
    		int searchWidth, int searchHeight, 
    		int maxPixelErrors,
    		DataBuffer imageBuffer,
    		SampleModel imageModel,
    		int pieces) throws InterruptedException{
    	Rectangle result = null;
    	boolean matched = false;
    	
		//Calculate the maximum search point on screen
    	int imageMaxScreenX = startx + searchWidth-1;
    	int imageMaxScreenY = starty + searchHeight-1;
    	int imagew = imageModel.getWidth();
    	int imageh = imageModel.getHeight();
    	int screenw = screenModel.getWidth();
    	int screenh = screenModel.getHeight();
    	//Re-Calculate the imageMaxScreenX and imageMaxScreenY, if the addition of 'search point' and 'image width/height'
    	//exceed the screen width and height
    	imageMaxScreenX = ((imageMaxScreenX+imagew)>screenw)? (screenw-imagew) : imageMaxScreenX;
    	imageMaxScreenY = ((imageMaxScreenY+imageh)>screenh)? (screenh-imageh) : imageMaxScreenY;
    	
		Log.debug("IU findSmallImageWithXYBTThread-->Main thread: Searching one block in multiple search areas.");
		int pieceWidth = (imageMaxScreenX-startx)/pieces;
		int pieceHeight = (imageMaxScreenY-starty)/pieces;
		//screenStartx, screenEndx, screenStarty and screenEndy represent the start and
		//end point of each search-rectangle
		int screenStartx, screenEndx, screenStarty, screenEndy;
		
		SmallPieceComparator[] threads = new SmallPieceComparator[pieces*pieces];
		SharedLock lock = new SharedLock(maxPixelErrors, threads.length, false);
		int threadIndex=0;
		
		//Divide the search rectangle, for each small search rectangle create one thread
		//For the most outer blocks, we need to set its screenEndx and screenEndy to imageMaxScreenX and imageMaxScreenY
		for(int i=0;i<pieces;i++){
			screenStarty = starty+i*pieceHeight;
			screenEndy = screenStarty+pieceHeight;
			if(i==pieces-1) screenEndy = imageMaxScreenY;
			
			for(int j=0;j<pieces;j++){
				screenStartx = startx+j*pieceWidth;
				screenEndx = screenStartx+pieceWidth;
				if(j==pieces-1) screenEndx = imageMaxScreenX;
				
				threads[threadIndex] = new SmallPieceComparator(screenBuffer,screenModel, imageBuffer,
        				imageModel, lock, new Rectangle(0,0,imagew,imageh),
        				screenStartx,screenEndx,screenStarty,screenEndy,
        				true);
				threads[threadIndex].start();
				threadIndex++;
			}
		}

		//Check if one of the thread find the matched image
		synchronized (lock) {
			while (lock.hasRunningThreads()) {
				Log.debug("IU findSmallImageWithXYBTThread-->Main thread is waitting search whole image .......  ");
				lock.setWaiting(true);
				lock.wait();
				Log.debug("IU findSmallImageWithXYBTThread-->Main thread has been notified .......  ");
				if (lock.isMatched()) {
					matched = true;
					// If one thread has found the matched image, we just stop the other threads
					for (int i = 0; i < threads.length; i++) {
						if (threads[i].isAlive()) {
							threads[i].setInterrupted(true);
						}
					}
					break;
				}
			}
			if(matched){
				//We should get the matchedPoint from the lock object in the synchronized
				//block, this make sure we get the point set by the thread who is notifying us.
				Point p = lock.getMatchedPoint();
				result = new Rectangle(p.x,p.y,imagew,imageh);
			}else{
				Log.info("IU findSmallImageWithXYBTThread did not find the image.");			
			}
		}
    	return result;
    }
    
    /**
     * We will divide the picture to small blocks, for each block, we use
	 * SmallPieceComparator to create a thread to compare with according screen location.
	 * If all blocks can match, which means the whole picture match.<p>
	 *   
	 * @param startx - x-coord on screen to start the search
	 * @param starty - y-coord on screen to start the search
	 * @param searchWidth - width of area from startx to search
	 * @param searchHeight - height of area from starty to search
	 * @param maxPixelErrors - max number of pixels that can fail comparison
	 * @param image		- BufferedImage of the target image
	 * @param imageBuffer - DataBuffer of the target image
	 * @param imageModel - SampleModel of the target image
     * @param pieces - pieces*pieces will be the number of thread or
     *                                       the number of blocks the big image to be divided to
     *                                       
     * @return Rectangle location where the image was found or null.
     * @throws InterruptedException
     */
    private static Rectangle findBigImageWithXYBTThread(int startx, int starty, 
    		int searchWidth, int searchHeight, 
    		int maxPixelErrors,
    		BufferedImage image,
    		DataBuffer imageBuffer,
    		SampleModel imageModel,
    		int pieces) throws InterruptedException{
    	Rectangle result = null;
    	int screenx = startx;
    	int screeny = starty;
    	boolean matched = false;
		//Calculate the maximum search point on screen
    	int imageMaxScreenX = startx + searchWidth-1;
    	int imageMaxScreenY = starty + searchHeight-1;
    	int imagew = imageModel.getWidth();
    	int imageh = imageModel.getHeight();
    	int screenw = screenModel.getWidth();
    	int screenh = screenModel.getHeight();
    	//Re-Calculate the imageMaxScreenX and imageMaxScreenY, if the addition of 'search point' and 'image width/height'
    	//exceed the screen width and height
    	imageMaxScreenX = ((imageMaxScreenX+imagew)>screenw)? (screenw-imagew) : imageMaxScreenX;
    	imageMaxScreenY = ((imageMaxScreenY+imageh)>screenh)? (screenh-imageh) : imageMaxScreenY;
    	
		Log.debug("IU findBigImageWithXYBTThread-->Main thread: Searching multiple blocks on one start-point.");
		//Decompose the picture to multiple blocks
		Rectangle[] blocks = new Rectangle[pieces*pieces];
		int pieceWidth = imagew/pieces;
		int pieceHeight = imageh/pieces;
		int tmpWidth=0, tmpHeight=0;

		//For the most outer blocks, we need to re-calculate it height and width
		for(int i=0;i<pieces;i++){
			if(i==pieces-1) tmpWidth = imagew-pieceWidth*i;
			else tmpWidth = pieceWidth;
			
			for(int j=0;j<pieces;j++){
				if(j==pieces-1) tmpHeight = imageh-pieceHeight*j;
				else tmpHeight = pieceHeight;
				
				blocks[i*pieces+j] = new Rectangle(pieceWidth*i,pieceHeight*j,tmpWidth,tmpHeight);
//				System.out.println("block "+(i*pieces+j)+": "+blocks[i*pieces+j]);
			}
		}
		//Prepare the first block for searching
		BufferedImage firstblock = image.getSubimage(blocks[0].x, blocks[0].y, blocks[0].width,blocks[0].height);
		DataBuffer firstbuffer = firstblock.getRaster().getDataBuffer();
		SampleModel firstmodel = firstblock.getRaster().getSampleModel();

		SmallPieceComparator[] threads = new SmallPieceComparator[pieces*pieces];
		// Initialize the SharedLock
		SharedLock lock = new SharedLock(maxPixelErrors, threads.length,true);

		//Date firsttime= new Date();
		Rectangle firstBlockRec = null;
outer:  for(screeny = starty; (screeny<imageMaxScreenY && !matched) ;screeny++){
			for (screenx = startx; (screenx<imageMaxScreenX && !matched); screenx++) {
				//1. Search the first block of decomposed picture
				
				//Use single thread to search the first block      				
				//   If we can find it at (startScrX,startScrY), we will use
				//   multi-thread to search the rest blocks; Otherwise we will
				//   continue to search the next start point
//				System.out.println("Searching first block from ("+screenx+","+screeny+")");
//				if(!isScreenXYMatch(screenBuffer, screenModel, firstbuffer, firstmodel, screenx, screeny, maxPixelErrors)){
//					continue;
//				}else{
//					System.out.println("Found first block at ("+screenx+","+screeny+")");
//				}
				
				//As single thread search the first blcok will take to much time,
				//so decide to use multi-thread to search
				//Use multiple thread to search the first block
				
				//Note!!! but there is a risk, if the first block has several matched image
				//on the screen within search-rectangle, the multi-thread algo can't garantee
				//we find the whole picture. At this situation, we should use the single-thread
				//search algo isScreenXYMatch() for searching first block
				firstBlockRec = findSmallImageWithXYBTThread(screenx, screeny, (imageMaxScreenX-screenx), 
						(imageMaxScreenY-screeny), maxPixelErrors, firstbuffer, firstmodel,pieces);
				if(firstBlockRec!=null){
					screenx = firstBlockRec.x-1;
					screeny = firstBlockRec.y-1;
					Log.debug("IU findBigImageWithXYBTThread-->Found first block at ("+firstBlockRec.x+","+firstBlockRec.y+")");
				}else{
					//If we can't find matched area on screen for the first block
					//then we are sure the whole image will not be matched
					matched = false;
					break outer;
				}
				//Log.debug("IU findBigImageWithXYBTThread-->First block used time: "+(new Date().getTime()-firsttime.getTime()));
				
				//2. Search the whole picture with multi-thread for each block
				//Reset the lock for the next search
				lock.reset(maxPixelErrors, threads.length, true);
				//Now, we just begin from point(screenx,screeny), we start multiple thread
				//to search each small block decomposed from the whole picture
				for(int k=0;k<threads.length;k++){
    				threads[k] = new SmallPieceComparator(screenBuffer,screenModel, imageBuffer,
            				imageModel, lock, blocks[k],
            				screenx,-1,screeny,-1,false);
    				threads[k].start();
				}
				synchronized (lock) {
					//Wait for all threads finish
					while (lock.hasRunningThreads()) {
						Log.debug("IU findBigImageWithXYBTThread-->Main thread is waitting block-search-threads .......  ");
						lock.setWaiting(true);
						lock.wait();
						Log.debug("IU findBigImageWithXYBTThread-->Main thread has been notified .......  ");
						// When one of threads finish, the main thread will be notified.
						// If the shared lock's property "matched" is false, that means we
						// fail to match the whole picture, we just stop all threads and
						// should begin an other search from the next search point.
						if (!lock.isMatched()) {
							Log.debug("IU findBigImageWithXYBTThread-->One of blocks is not matched  .......  ");
							// Kill all running threads
							for (int i = 0; i < threads.length; i++) {
								if (threads[i].isAlive()) {
									threads[i].setInterrupted(true);
								}
							}
							break;
						}
						
						//I just remove the above section, there is a dead-lock problem.
						//We can wait for all thread finish, as the search-thread share
						//one field maxErrorBits in lock, if maxErrorBits has been exceeded,
						//all threads will finish quickly, no need to interrupt them one by one.
					}
					matched = lock.isMatched();
				}
			}//end for screenx
		}//end for screeny
		
		if(matched){
			Log.debug("IU findBigImageWithXYBTThread-->Find image on start point ("+screenx+","+screeny+")");
			result = new Rectangle(screenx, screeny, imagew,imageh);
		}else{
			Log.debug("IU findBigImageWithXYBTThread did not find the image.");			
		}
    	return result;
    }  
    
    /**
     * This is a time expensive match seeking to match 1 pixel with any one of 8 adjacent pixels.
     * @param screenshotBuffer
     * @param screenshot
     * @param screencenterX
     * @param screencenterY
     * @param targetBuffer
     * @param target
     * @param targetX
     * @param targetY
     * @return
     */
    public static boolean isScreenXYFuzzyMatch(DataBuffer sBuffer, SampleModel smodel, int screencenterX, int screencenterY, DataBuffer tBuffer, SampleModel tmodel, int targetX, int targetY){
    	// the target pixel never changes when checking for a match of any of the other 8 pixels
		int tc0 = tmodel.getSample(targetX, targetY, 0, tBuffer);
		int tc1 = tmodel.getSample(targetX, targetY, 1, tBuffer);
		int tc2 = tmodel.getSample(targetX, targetY, 2, tBuffer);

		int sx=0;
		int sy=0;
		for(int scy = -1; scy < 2;scy++){
			sy = screencenterY+scy;
			if((sy < 0) || (sy >= smodel.getHeight())) continue;
			
			for(int scx = -1; scx < 2;scx++){
				// we already know we did NOT match the screenX and screenY pixel, so we don't have to compare it
				if(scy==0 && scx==0) continue;
				sx = screencenterX+scx;
				if((sx < 0)||(sx >= smodel.getWidth())) continue;
				if(smodel.getSample(sx, sy, 0, sBuffer)!= tc0) continue; //not a match
				if(smodel.getSample(sx, sy, 1, sBuffer)!= tc1) continue; //not a match
				if(smodel.getSample(sx, sy, 2, sBuffer)!= tc2) continue; //not a match
				if(debug) Log.info("IU.FuzzyMatching successfully matched at "+screencenterX +","+ screencenterY + " with offset "+ scx +","+ scy);
				return true; // matched
			}
		}    	
		//if(debug) Log.info("IU.FuzzyMatching did NOT match image "+targetX+","+ targetY +" at screen "+screencenterX+","+screencenterY);
    	return false;
    }
    
	/**
	 * Evaluate if the target image matches at the specified screen x,y coordinate.
	 * This routine has been separated out in preparation for multi-threaded execution algorithms. 
	 * @param screenshotBuffer
	 * @param screenshot
	 * @param targetBuffer
	 * @param target
	 * @param startScreenX
	 * @param startScreenY
	 * @param maxBTerrors - max number of pixel mismatches allowed (bit tolerance)
	 * @return true if the target image is a satisfactory match at this screen x,y coordinate
	 */
    private static boolean isScreenXYMatch(DataBuffer screenshotBuffer, SampleModel screenshot, DataBuffer targetBuffer, SampleModel target, int startScreenX, int startScreenY, long maxBTerrors){
		long pixelErrors = 0;
		long pixelMatches = 0;
		float percentages = 0;
		boolean useBT = maxBTerrors > 0;
		int targetheight = target.getHeight();
		int targetwidth  = target.getWidth();
		long requiredMatches = targetheight * targetwidth - maxBTerrors;
		int scrH         = screenshot.getHeight();
		int scrW         = screenshot.getWidth();
		int scrX = 0;
		int scrY = 0;
		boolean result = false;
		int trgV = 0;
		int scrV = 0;
		int imagex = 0;
		int imagey = 0;
		for(imagey=0;imagey<targetheight;imagey++){
			scrY = startScreenY+imagey;
			if (scrY >= scrH)
				break;
			for(imagex=0;imagex<targetwidth;imagex++){
				scrX = startScreenX+imagex;
				if (scrX >= scrW)
					break;								
				// band 0; plane 0
				trgV = target.getSample(imagex, imagey, 0, targetBuffer);
				scrV = screenshot.getSample(scrX, scrY, 0, screenshotBuffer);
				// for simple bit tolerance any 1 of 3 mismatches makes the pixel "bad"
				if (trgV != scrV) {
					if(useBT && USE_FUZZY_MATCHING){
						if(isScreenXYFuzzyMatch(screenshotBuffer, screenshot, scrX, scrY, targetBuffer, target,imagex,imagey)) {
							pixelMatches++;
							continue;
						}
					}
					if(!useBT) break;
					if(++pixelErrors < maxBTerrors) continue;
					break; 
				}
				// band 1; plane 1
				trgV = target.getSample(imagex, imagey, 1, targetBuffer);
				scrV = screenshot.getSample(scrX, scrY, 1, screenshotBuffer);
				// for simple bit tolerance any 1 of 3 mismatches makes the pixel "bad"
				if (trgV != scrV) {
					if(useBT && USE_FUZZY_MATCHING){
						if(isScreenXYFuzzyMatch(screenshotBuffer, screenshot, scrX, scrY, targetBuffer, target,imagex,imagey)) {
							pixelMatches++;
							continue;
						}
					}
					if(!useBT) break;
					if(++pixelErrors < maxBTerrors) continue;
					break; 
				}	
				// band 2; plane 2
				trgV = target.getSample(imagex, imagey, 2, targetBuffer);
				scrV = screenshot.getSample(scrX, scrY, 2, screenshotBuffer);
				// for simple bit tolerance any 1 of 3 mismatches makes the pixel "bad"
				if (trgV != scrV) {
					if(useBT && USE_FUZZY_MATCHING){
						if(isScreenXYFuzzyMatch(screenshotBuffer, screenshot, scrX, scrY, targetBuffer, target,imagex,imagey)) {
							pixelMatches++;
							continue;
						}
					}
					if(!useBT) break;
					if(++pixelErrors < maxBTerrors) continue;
					break; 
				}
				if( ++pixelMatches >= requiredMatches) {
					if(debug) Log.info("IU.isScreenXYMatched presuming success with "+ pixelMatches +" matches and "+ pixelErrors +" errors.");
					break;				
				}
			}
			//break if image data did not match (maxerrors exceeded)
			if(imagex != targetwidth) 
				break;						
		}		
		if (((imagex == targetwidth)&&(imagey == targetheight)) || (pixelMatches >= requiredMatches)) {
			result = true;
			if (maxBTerrors > 0) percentages = pixelErrors/maxBTerrors; //avoid divide by zero exception
			if(pixelErrors == 0) {
				Log.info("IU isScreenXYMatched MATCHED image with "+ (100-percentages) +"% confidence at "+ startScreenX +","+ startScreenY);
			}else{
				Log.info("IU isScreenXYMatched MATCHED image with reserved confidence at "+ startScreenX +","+ startScreenY);
			}
		}else{
			if(useBT && (pixelMatches > 0)){
				percentages = (pixelMatches/requiredMatches)*100;
				if(percentages > closestPercentage){
					closestPercentage = percentages;
					closestX = startScreenX;
					closestY = startScreenY;
					closestMatchCount = pixelMatches;
					closestMatchesOf = requiredMatches;
				}
			}
		}
		return result;														
	}    
    
    //LieWang: Modify method isScreenXYMatch(): Fix problem "Wrongly caculate the mis-match percentage".
//	private static boolean isScreenXYMatch(DataBuffer screenshotBuffer, SampleModel screenshot, DataBuffer targetBuffer, SampleModel target, int startScreenX, int startScreenY, long maxBTerrors){
//		long pixelErrors = 0;
//		long pixelMatches = 0;
//		float percentages = 0;
//		boolean useBT = maxBTerrors > 0;
//		int targetheight = target.getHeight();
//		int targetwidth  = target.getWidth();
//		long requiredMatches = targetheight * targetwidth - maxBTerrors;
//		int scrH         = screenshot.getHeight();
//		int scrW         = screenshot.getWidth();
//		int scrX = 0;
//		int scrY = 0;
//		boolean result = false;
//		int trgV = 0;
//		int scrV = 0;
//		int imagex = 0;
//		int imagey = 0;
//		boolean bitMatched = false;
//		
//		for(imagey=0;imagey<targetheight;imagey++){
//			scrY = startScreenY+imagey;
//			if (scrY >= scrH)
//				break;
//			for(imagex=0;imagex<targetwidth;imagex++){
//				scrX = startScreenX+imagex;
//				if (scrX >= scrW)
//					break;								
//				//any 1 of 3 mismatches (band 0, 1, 2) makes the pixel "bad"
//				// band 0; plane 0
//				trgV = target.getSample(imagex, imagey, 0, targetBuffer);
//				scrV = screenshot.getSample(scrX, scrY, 0, screenshotBuffer);
//				bitMatched = (trgV==scrV);
//				
//				// band 1; plane 1
//				if(bitMatched){
//					trgV = target.getSample(imagex, imagey, 1, targetBuffer);
//					scrV = screenshot.getSample(scrX, scrY, 1, screenshotBuffer);
//					bitMatched = (trgV==scrV);
//				}
//				
//				// band 2; plane 2
//				if(bitMatched){
//					trgV = target.getSample(imagex, imagey, 2, targetBuffer);
//					scrV = screenshot.getSample(scrX, scrY, 2, screenshotBuffer);
//					bitMatched = (trgV==scrV);
//				}
//				
//				//if the pixel is "bad"
//				if (!bitMatched) {
//					if(!useBT) break;
//					if(USE_FUZZY_MATCHING) bitMatched = isScreenXYFuzzyMatch(screenshotBuffer, screenshot, scrX, scrY, targetBuffer, target,imagex,imagey);
//					if(!bitMatched){
//						if(++pixelErrors < maxBTerrors) continue;
//						break; 
//					}
//				}
//				
//				if( ++pixelMatches >= requiredMatches) {
//					if(debug) Log.info("IU.isScreenXYMatched presuming success with "+ pixelMatches +" matches and "+ pixelErrors +" errors.");
//					break;				
//				}
//			}
//			//break if image data did not match (maxerrors exceeded)
//			if(imagex != targetwidth) 
//				break;						
//		}		
//		if (((imagex == targetwidth)&&(imagey == targetheight)) || (pixelMatches >= requiredMatches)) {
//			result = true;
//			if (maxBTerrors > 0) percentages = pixelErrors/maxBTerrors; //avoid divide by zero exception
//			if(pixelErrors == 0) {
//				Log.info("IU isScreenXYMatched MATCHED image with "+ (100-percentages) +"% confidence at "+ startScreenX +","+ startScreenY);
//			}else{
//				Log.info("IU isScreenXYMatched MATCHED image with reserved confidence at "+ startScreenX +","+ startScreenY);
//			}
//		}else{
//			if(useBT && (pixelMatches > 0)){
//				percentages = (pixelMatches/requiredMatches)*100;
//				if(percentages > closestPercentage){
//					closestPercentage = percentages;
//					closestX = startScreenX;
//					closestY = startScreenY;
//					closestMatchCount = pixelMatches;
//					closestMatchesOf = requiredMatches;
//				}
//			}
//		}
//		return result;														
//	}
	
	/**
	 * Call the main findBufferedImageOnScreen with null searchRect and nthindex of 1.
	 * @param image
	 * @return
	 * @throws IllegalArgumentException
	 * @throws ImagingOpException
	 * @throws AWTException
	 */
	public static Rectangle findBufferedImageOnScreen(BufferedImage image) throws IllegalArgumentException,ImagingOpException,AWTException{
		//Supply the third parameter index as 0, so that in method findBufferedImageOnScreen(BufferedImage,Rectangle,int)
		//programe will try to use multi-thread if USE_MULTIPLE_THREADS is true.
		return findBufferedImageOnScreen(image, null, 0);
	}		

	/**
	 * Call the main findBufferedImageOnScreen with searchRect and nthindex of 1.
	 * @param image
	 * @return
	 * @throws IllegalArgumentException
	 * @throws ImagingOpException
	 * @throws AWTException
	 */
	public static Rectangle findBufferedImageOnScreen(BufferedImage image, Rectangle searchRect) throws IllegalArgumentException,ImagingOpException,AWTException{
		//Supply the third parameter index as 0, so that in method findBufferedImageOnScreen(BufferedImage,Rectangle,int)
		//programe will try to use multi-thread if USE_MULTIPLE_THREADS is true.
		return findBufferedImageOnScreen(image, searchRect, 0);
	}
	
	/**
	 * Needs Enhancement API subject to change.
	 * 
	 * @param image
	 * @throws IOException
	 */
	public static void outputImageData(BufferedImage image)throws IOException
	{
		//get the Raster and SampleModel
		Raster imageRaster = image.getRaster();
		SampleModel imageModel = imageRaster.getSampleModel();
		boolean packed = false;
		int stridey = -1;
		int[] bitmasks = new int[imageModel.getNumBands()];
		int[] bitoffsets = new int[imageModel.getNumBands()];
		if(imageModel instanceof SinglePixelPackedSampleModel){
			packed = true;
			stridey = ((SinglePixelPackedSampleModel)imageModel).getScanlineStride();
			bitmasks = ((SinglePixelPackedSampleModel)imageModel).getBitMasks();
			bitoffsets = ((SinglePixelPackedSampleModel)imageModel).getBitOffsets();
		}
		if(debug)Log.info("Image Type:"+ image.getType());
		if(debug)Log.info("Image SampleModel class:"+ imageModel.getClass().getSimpleName());
		DataBuffer imageBuffer = imageRaster.getDataBuffer();
		if(debug)Log.info("Image DataBuffer class:"+ imageBuffer.getClass().getSimpleName());
		ColorModel imageColor = image.getColorModel();
		if(debug)Log.info("Image ColorModel class:"+ imageColor.getClass().getSimpleName());
		
		int imageBands = imageModel.getNumBands();
		if(debug)Log.info("Image NumBands: "+ imageBands);

		int imageWidth = imageModel.getWidth();
		int imageHeight = imageModel.getHeight();
		Log.info("Image Size: "+ imageWidth +", "+ imageHeight);
		int sample = -1;
		for(int b=0; b < imageBands; b++){
			//System.out.println("\nProcessing Image Band: "+ b +"\n");
			for(int y = 0; y < imageHeight; y++){
				//System.out.print(y+":");
				for(int x = 0; x < imageWidth; x++){
					
					if(packed){
						sample = imageBuffer.getElem(y * stridey + x);
						sample = (sample & bitmasks[b]) >>> bitoffsets[b]; 
					}else{
						sample = imageModel.getSample(x, y, b, imageBuffer);
					}
					//try{ blue = imageColor.getBlue(sample);}
					//catch(Exception xx){ blue = -1;}
					//System.out.print(sample+":"+ blue +", ");
					//System.out.print(sample +", ");
				}
				//System.out.print("\n");
			}
		}
	}
	
	/**
	 * Split the image recognition info at each MOD_SEP to form the String[] recognition 
	 * needed by most routines. 
	 * @param recognition String containing all image recognition information.
	 * @return String[] of image recognition modifiers split at the MOD_SEP(;)
	 * @throws NullPointerException if String recognition is null.
	 */
	public static String[] splitRecognition(String recognition){
		return recognition.split(MOD_SEP);
	}
	
	/**
	 * See if the recognition string modifier maps to the imagetype passed in.
	 * Ex: imagetype MOD_IMAGEW returns true for modifiers MOD_IMAGER, MOD_IMAGERIGHT, 
	 * MOD_IMAGEW, and MOD_IMAGEWIDTH.  
	 * @param imagetype MOD_IMAGE, MOD_IMAGEW, MOD_IMAGEH, MOD_IMAGETEXT, MOD_IMAGE_RECT
	 * @param modifier actual recognition string modifier
	 * @return true if the modifier maps to the provided imagetype.
	 */
	static boolean isImageType(String imagetype, String modifier){
		if(imagetype.equalsIgnoreCase(MOD_IMAGE)){
			return modifier.equalsIgnoreCase(MOD_IMAGE);
			
		}else if (imagetype.equalsIgnoreCase(MOD_IMAGETEXT)){
			return modifier.equalsIgnoreCase(MOD_IMAGETEXT);
			
		}else if (imagetype.equalsIgnoreCase(MOD_IMAGEW)){
			return modifier.equalsIgnoreCase(MOD_IMAGEW)    |
		           modifier.equalsIgnoreCase(MOD_IMAGEWIDTH)|
			       modifier.equalsIgnoreCase(MOD_IMAGER)    |
		           modifier.equalsIgnoreCase(MOD_IMAGERIGHT);
			
		}else if (imagetype.equalsIgnoreCase(MOD_IMAGEH)){
			return modifier.equalsIgnoreCase(MOD_IMAGEH)     |
			       modifier.equalsIgnoreCase(MOD_IMAGEHEIGHT)|
			       modifier.equalsIgnoreCase(MOD_IMAGEB)     |
			       modifier.equalsIgnoreCase(MOD_IMAGEBOTTOM);
			
		}else if (imagetype.equalsIgnoreCase(MOD_IMAGE_RECT)){
			return modifier.equalsIgnoreCase(MOD_IMAGE_RECT) |
			       modifier.equalsIgnoreCase(MOD_IMAGE_RECTANGLE);

		}else if (imagetype.equalsIgnoreCase(MOD_SEARCH_RECT)){
			return modifier.equalsIgnoreCase(MOD_SEARCH_RECT) |
			       modifier.equalsIgnoreCase(MOD_SEARCH_RECTANGLE);
		}
		return false;
	}
	
	/**
	 * Attempt to extract the Image=path; portion of an Image-Based Testing recognition string.<br>  
	 * This can also be an ImageRect or SearchRect "path".
	 *  
	 * @param imagex should be either MOD_IMAGE, MOD_IMAGEW, MOD_IMAGEH, MOD_IMAGE_RECT, MODE_SEARCH_RECT
	 * @param recognition String [] of recognition modifiers.  should not be null;
	 * @return path found or null. path.length() will be > 0 or we will return null.
	 */
	public static String extractImagePath(String imagex, String[] recognition){
		if (recognition == null) return null;
		String modifier = null;
		String[] value = new String[2];
		String result = null;
		String sval = null;
		boolean match = false;
		Log.debug("IU extractImagePath seeking info for:"+ imagex);
		for(int i=0;i<recognition.length;i++){
			modifier = recognition[i];
			value = modifier.split(MOD_EQ);
			sval = value[0].trim();
			match = isImageType(imagex, sval);
			if(match){
				try {
					result = value[1].trim();			
					Log.debug("IU extractImagePath '"+ imagex +"' matching to: "+ sval);
					if (result.length()> 0) return result;
				}catch(Exception x){}
				return null; // we only expect 1 Image= tag so stop loop
			}
		}
		return null;
	}

	/**
	 * Extract the Index=N; modifier of an Image-Based Testing recognition string.
	 * 
	 * If user specify Index=Number, we will return the int value of that number;
	 * If user specify Index=Any, we will return -1;
	 * If we don't specify "Index=N", we will return 0.
	 * 
	 * This is expected to immediately follow the Image= modifier. Or not at all.
	 * Adding ImageText= and allow Index=N follow ImageText= modifier.
	 * 
	 * @param recognition String [] of recognition modifiers.  should not be null;
	 * @return index found or 0 or -1;
	 */
	public static int extractImageIndex(String[] recognition){
		if (recognition == null) return 0;
		String modifier = null;
		String[] value = new String[2];
		String result = null;
		for(int i=0;i<recognition.length;i++){
			modifier = recognition[i];
			value = modifier.split(MOD_EQ);			
			if(value[0].trim().equalsIgnoreCase(MOD_IMAGE)||
			   value[0].trim().equalsIgnoreCase(MOD_IMAGETEXT)){
				try{
					modifier = recognition[i+1];
					value = modifier.split(MOD_EQ);
					result = value[0].trim();
					if((result.equalsIgnoreCase(MOD_INDEX))||
					   (result.equalsIgnoreCase(MOD_IND))){
						result = value[1].trim();
						//If user specify "index=any", we retrun -1 as index
						if("any".equalsIgnoreCase(result)) return -1;
						int n = Integer.parseInt(result);
						if (n > 0) return n;
					}
				}catch(Exception x){}
				return 0; // we only expect 1 Image= tag so stop loop
			}
		}
		return 0;
	}

	/**
	 * Extract the Index=N; modifier of an Image-Based Testing recognition string.
	 * This is the UsePerImageModifiers version.
	 * 
	 * If user specify Index=Number, we will return the int value of that number;
	 * If user specify Index=Any, we will return -1;
	 * If we don't specify "Index=N", we will return 0.
	 * 
	 * This is expected to immediately follow the imagetype provided.
	 * 
	 * @param imagetype must be MOD_IMAGE, MOD_IMAGEW, MOD_IMAGEH, or MOD_IMAGETEXT
	 * @param recognition String [] of recognition modifiers.  should not be null;
	 * @return index found or 0 or -1;
	 */
	public static int extractImageIndex(String imagetype, String[] recognition){
		if (recognition == null) return 0;
		String modifier = null;
		String[] value = new String[2];
		String result = null;
		int i = 0;
		boolean matched = false;
		for(;!matched && i<recognition.length;i++){
			modifier = recognition[i];
			value = modifier.split(MOD_EQ);			
			result = value[0].trim();
			matched = isImageType(imagetype, result);
		}
		if(matched){
			for(;i<recognition.length;i++){
				modifier = recognition[i];
				value = modifier.split(MOD_EQ);			
				result = value[0].trim();
				if(result.toLowerCase().startsWith("image")) return 0;
				if((result.equalsIgnoreCase(MOD_INDEX))||
				   (result.equalsIgnoreCase(MOD_IND))){
					result = value[1].trim();
					try{
						//If user specify "index=any", we retrun -1 as index
						if("any".equalsIgnoreCase(result)) return -1;
						int n = Integer.parseInt(result);
						if (n > 0) return n;
					}catch(Exception x){}
					Log.info("extractImageIndex ignoring invalid modifier: "+ result);
					return 0;
				}
			}
		}
		return 0;
	}

	/**
	 * Attempt to extract the SearchRect=; portion of an Image-Based Testing recognition string.
	 * The SearchRect definition is relative to the entire screen.  The routine also evaluates 
	 * whether USE_PER_IMAGE_MODIFIERS is active.
	 *  
	 * @param which type of image is being modified: MOD_IMAGE, MOD_IMAGER, MOD_IMAGEH, MOD_IMAGE_RECT, MOD_IMAGETEXT.
	 *        can be null to default to ANY instance of SearchRect.
	 * @param recognition String [] of recognition modifiers.  should not be null;
	 * @return Rectangle found or null.  This rectangle is in screen coordinates.
	 * @see #USE_PER_IMAGE_MODIFIERS
	 */
	public static Rectangle extractSearchRect(String winimagetype, String[] recognition){
		return extractSearchRect(winimagetype, recognition, null); // null=entire screen
	}
	
	/**
	 * Called internally by extractSearchRect
	 * @param modifier The value already extracted from ;sr=value;
	 * @param rectangle The rectangle to modify
	 * @param isRelative true if the modifications are "relative" instead of "absolute".
	 * @return new rectangle or the original rectangle if a problem occurred.
	 * @see #extractSearchRect(String, String[], Rectangle)
	 */
	static Rectangle processSearchRectModifier(String modifier, Rectangle rectangle, boolean isRelative){
		try{
			if (modifier.length()== 0) return rectangle;
			// coords can be comma OR space delimited
			String sep = MOD_SPACE;
			boolean sep_space = true;
			if(modifier.indexOf(MOD_COMMA)> -1) {
				sep = MOD_COMMA;
				sep_space= false;
			}
			int x = rectangle.x;
			int y = rectangle.y;
			int w = rectangle.width;
			int h = rectangle.height;
			int seeking = 0; // X=0, Y=1, W=2, H=3
			boolean percent = false;

			String c = null;
			String sval = null;
			int next_sep = -1;
			int ival = -1;
			int ipercent = -1;
			int change = 0;
			for(int pos = 0;pos<modifier.length();pos++){
				c = modifier.substring(pos,pos+1);
				if (c.equals(sep)){
					if ( sep_space ) continue;
					seeking++; // if we found a sep before a value keep the default
					continue;
				}
				//we found a non-sep char
				next_sep = modifier.indexOf(sep, pos);
				if(next_sep==-1){
					sval = modifier.substring(pos).trim();
					pos = modifier.length();//end the loop, no more data
				}else{
					sval = modifier.substring(pos, next_sep).trim();
					pos = next_sep; //start the next loop after this data
				}						
				if(sval.length() > 0){
					ipercent = sval.indexOf(MOD_PERCENT);
					percent = ipercent > 0;
					if(percent) sval = sval.substring(0, ipercent).trim();
					ival = Integer.parseInt(sval);
					
					if(seeking==0){ // X
						if(!percent){
							change = ival;
							if(isRelative){
								// x = x + ival  same as  x += ival
								x += change; //can be negative to expand the searchRect (like outsets)
							}else{
								x = change;
							}
						}
						else{ // is percentage									
							change = (int)(((float)w/(float)100) * (float)ival);
							x += change;
						}
						//check absolute bounds
						if (x < 0)                      x = 0;
						else if (x > screenWidth)       x = screenWidth;
					}
					else if (seeking==1){ // Y
						if(!percent){
							change = ival;
							if(isRelative){
								// y = y + ival  same as  y += ival
								y += change; //can be negative to expand the searchRect (like outsets)
							}else{
								y = change;
							}
						}
						else{ // is percentage
							change = (int)(((float)h/(float)100) * (float)ival);
							y += change;
						}
						//check absolute bounds
						if (y < 0)                      y = 0;
						else if (y > screenHeight)      y = screenHeight;
					}
					else if (seeking==2){ // W
						if(!percent){
							change = ival;
							if(isRelative){
								w += change;
							}else{
								w = change;
							}
						}else{ // is percent
							change = (int) (((float)w/(float)100) * (float)ival);
							w = change;
						}
					}else if (seeking==3){ // H
						if(!percent){
							change = ival;
							if(isRelative){
								h += change;
							}else{
								h = change;
							}
						}else{ // is percent
							change = (int) (((float)h/(float)100) * (float)ival);
							h = change;
						}
					}
				}
				seeking++;
			}
			//check absolute bounds
			if (w < 1)                      w = 1;
			if( x+w > screenWidth)          w = screenWidth - x;
			if (h < 1)                      h = 1;
			if( y+h > screenHeight)         h = screenHeight - y;

			Log.info("Processed SearchRect value to be "+ x +", "+ y +", "+ w +", "+ h);
			return new Rectangle(x,y,w,h);					
		}
		catch(Exception x){
			Log.info("Process SearchRect error: "+ x.getClass().getSimpleName());
		}
		return rectangle;
	}
	
	/**
	 * Attempt to extract the SearchRect=; portion of an Image-Based Testing recognition string.
	 * The SearchRect values are relative to the specified 'relative' Rectangle.  This allows us 
	 * to find "components" inside "windows" and limit the area inside the "window" for the 
	 * search.  The relative Rectangle is expected to be in screen coordinates.
	 *  
	 * @param which type of image is being modified: MOD_IMAGE, MOD_IMAGER, MOD_IMAGEH, MOD_IMAGE_RECT, MOD_IMAGETEXT
	 *  if null then MOD_IMAGE is used by default.
	 * @param recognition String [] of recognition modifiers.  should not be null;
	 * @param relative Rectangle. If not null, the SearchRect is considered relative to this 
	 * Rectangle and not the entire screen.
	 * @return Rectangle found or null.  This rectangle is in screen coordinates.
	 */
	public static Rectangle extractSearchRect(String winimagetype, String[] recognition, Rectangle relative){
		if (recognition == null) return null;
		if (winimagetype==null) winimagetype = MOD_IMAGE;
		String modifier = null;
		String[] value = new String[2];
		String result = null;
		try{ 
			if (screenWidth==-1)recaptureScreen();
		}
		catch(Exception x){
			Log.info("SearchRect capture screen error: "+ x.getClass().getSimpleName());
			return null;
		}
		boolean isRelative = (relative != null);
		Rectangle relscreen = !isRelative ? new Rectangle(0,0,screenWidth, screenHeight):
			                                    relative;		
		//enhanced USE_PER_IMAGE_MODIFIERS algorithm
		if(USE_PER_IMAGE_MODIFIERS){
			Log.info("extractSearchRect evaluating per image modifier for '"+ winimagetype +"'");
			int i = 0;
			boolean match = false;
			for(;!match && i<recognition.length;i++){
				modifier = recognition[i];
				value = modifier.split(MOD_EQ);		
				result = value[0].trim();
				match = isImageType(winimagetype, result);
			}
			if(match){
				for(;i<recognition.length;i++){
					modifier = recognition[i];
					value = modifier.split(MOD_EQ);		
					result = value[0].trim();
					//if we find the next Image arg we are done
					if(result.toLowerCase().startsWith("image")) {
						Log.info("extractSearchRect found '"+ result +"' before any SearchRect.");
						return relscreen;
					}
					if((result.equalsIgnoreCase(MOD_SEARCHRECT))||
					   (result.equalsIgnoreCase(MOD_SR))){
							return processSearchRectModifier(value[1].trim(), relscreen, isRelative);
					}
				}
			}else
				Log.info("extractSearchRect did not find a match for '"+winimagetype+"'");
		}
		//original algorithm for Image= only
		else if(winimagetype.equalsIgnoreCase(MOD_IMAGE)||
				winimagetype.equalsIgnoreCase(MOD_IMAGETEXT)||
				winimagetype.equalsIgnoreCase(MOD_IMAGE_RECT)){
			for(int i=0;i<recognition.length;i++){
				modifier = recognition[i];
				value = modifier.split(MOD_EQ);		
				result = value[0].trim();
				if((result.equalsIgnoreCase(MOD_SEARCHRECT))||
				   (result.equalsIgnoreCase(MOD_SR))){
					return processSearchRectModifier(value[1], relscreen, isRelative);
				}
			}
		}
		return relscreen;
	}

	/**
	 * Attempt to extract the Hotspot=; portion of an Image-Based Testing recognition string.
	 * The default hotspot of an image or Rectangle is the center.  The Hotspot Point will be 
	 * relative to the center of the image\rectangle unless PointRelative= is also present 
	 * in the image recognition (and processed elsewhere).
	 * @param recognition String [] of recognition modifiers.  should not be null;
	 * @return Point containing relative X and Y values or null if no Hotspot is found.
	 */
	public static java.awt.Point extractHotspot(String[] recognition){
		if (recognition == null) return null;
		String modifier = null;
		String[] value = new String[2];
		String result = null;
		
		for(int i=0;i<recognition.length;i++){
			modifier = recognition[i];
			value = modifier.split(MOD_EQ);		
			result = value[0].trim();
			if((result.equalsIgnoreCase(MOD_HOTSPOT))||
			   (result.equalsIgnoreCase(MOD_HS))){
				try{
					result = value[1];
					if (result.length()== 0) return null;
					// coords can be comma OR space delimited
					String sep = MOD_SPACE;
					boolean sep_space = true;
					if(result.indexOf(MOD_COMMA)> -1) {
						sep = MOD_COMMA;
						sep_space= false;
					}
					int seeking = 0; // X=0, Y=1

					String c = null;
					String sval = null;
					int next_sep = -1;
					int ival = -1;
					int x = -1;
					int y = -1;
					
					for(int pos = 0;pos<result.length();pos++){
						c = result.substring(pos,pos+1);
						if (c.equals(sep)){
							if ( sep_space ) continue;
							seeking++; // if we found a sep before a value keep the default
							continue;
						}
						//we found a non-sep char
						next_sep = result.indexOf(sep, pos);
						if(next_sep==-1){
							sval = result.substring(pos).trim();
							pos = result.length();//end the loop, no more data
						}else{
							sval = result.substring(pos, next_sep);
							pos = next_sep; //start the next loop after this data
						}						
						if(sval.length() > 0){
							ival = Integer.parseInt(sval);
							if(seeking==0)       x = ival;									
							else if (seeking==1) y = ival;
						}
						seeking++;
					}
					Log.info("Processed Hotspot value to be "+ x +", "+ y );
					return new java.awt.Point(x,y);					
				}catch(Exception x){
					Log.info("Process Hotspot error: "+ x.getClass().getSimpleName());
				}
				return null; // we only expect 1 Hotspot= tag so stop loop
			}
		}
		return null;
	}

	/**
	 * Extract the PointRelative= info of the recognition string.
	 *  
	 * @param recognition String [] of recognition modifiers.  should not be null;
	 * @return INT_TOPLEFT thru INT_BOTTOMRIGHT (1-9) or -1 if not found. 
	 */
	public static int extractPointRelative(String[] recognition){
		if (recognition == null) return -1;
		String modifier = null;
		String[] value = new String[2];
		String result = null;
		for(int i=0;i<recognition.length;i++){
			modifier = recognition[i];
			value = modifier.split(MOD_EQ);	
			result = value[0].trim();
			if((result.equalsIgnoreCase(MOD_POINTRELATIVE))||
			   (result.equalsIgnoreCase(MOD_PR))){
				try{
					result = value[1].trim();
					if((result.equalsIgnoreCase(MOD_TOPLEFT))||
					   (result.equalsIgnoreCase(MOD_TL))){
						return INT_TOPLEFT;
					}else if((result.equalsIgnoreCase(MOD_TOPCENTER))||
					   (result.equalsIgnoreCase(MOD_TC))){
						return INT_TOPCENTER;
					}else if((result.equalsIgnoreCase(MOD_TOPRIGHT))||
					   (result.equalsIgnoreCase(MOD_TR))){
						return INT_TOPRIGHT;
					}else if((result.equalsIgnoreCase(MOD_LEFTCENTER))||
					   (result.equalsIgnoreCase(MOD_LC))){
						return INT_LEFTCENTER;
					}else if((result.equalsIgnoreCase(MOD_CENTER))||
					   (result.equalsIgnoreCase(MOD_C))){
						return INT_CENTER;
					}else if((result.equalsIgnoreCase(MOD_RIGHTCENTER))||
					   (result.equalsIgnoreCase(MOD_RC))){
						return INT_RIGHTCENTER;
					}else if((result.equalsIgnoreCase(MOD_BOTTOMLEFT))||
					   (result.equalsIgnoreCase(MOD_BL))){
						return INT_BOTTOMLEFT;
					}else if((result.equalsIgnoreCase(MOD_BOTTOMCENTER))||
					   (result.equalsIgnoreCase(MOD_BC))){
						return INT_BOTTOMCENTER;
					}else if((result.equalsIgnoreCase(MOD_BOTTOMRIGHT))||
					   (result.equalsIgnoreCase(MOD_BR))){
						return INT_BOTTOMRIGHT;
					}
					return -1; // none of the above?
				}catch(Exception x){}
				return -1; // we only expect 1 Image= tag so stop loop
			}
		}
		return -1;
	}

	/**
	 * Resets the class to use the default_percent_bits_tolerance 
	 * and the default_useBitsTolerance.
	 * @see #default_useBitsTolerance
	 * @see #default_percent_bits_tolerance
	 */
	public static void resetBitsTolerance(){
		useBitsTolerance = default_useBitsTolerance;
		percentBitsTolerance = default_percent_bits_tolerance;
	}

	/**
	 * Called internally from extractBitTolerance.
	 * @param value should not be null;
	 * @return
	 */
	static int processBitsToleranceModifier(String value){
		int percent = 0;
		try{
			//must catch non-numeric exceptions and verify n = 1-100
			percent = Integer.parseInt(value.trim());
			if (percent > 0 && percent <= 100) {
				percentBitsTolerance = percent;
				useBitsTolerance = !(percent == 100);
				Log.info("ImageUtils setting Bit Tolerance "+ useBitsTolerance +" to "+percentBitsTolerance+"%.");
			}else{
				Log.debug("ImageUtils ignoring INVALID BitTolerance setting: "+ value);
			}
		}catch(Exception x){
			Log.debug("ImageUtils "+x.getClass().getSimpleName() +" ignoring INVALID BitTolerance syntax: "+ value);
		}
		return percentBitsTolerance;
	}
	
	/**
	 * Extract the BitTolerance=N; or BT=N; modifier of an Image-Based Testing recognition string.
	 * BitTolerance is optional and may not be present.
	 * <p>
	 * If not present, the routine resets the class to use the default_percent_bits_tolerance 
	 * and the default_useBitsTolerance.  If present and the tolerance is less than 100% then 
	 * the useBitsTolerance boolean is forced to true, as well.  If the tolerance is set to 100% 
	 * then we expect an exact match and the useBitsTolerance boolean is set to false.
	 *  
	 * @param recognition String [] of recognition modifiers.  should not be null;
	 * @return 1-100 of extracted bit tolerance or default_percent_bits_tolerance.  
	 */
	public static int extractBitsTolerance(String[] recognition){
		resetBitsTolerance();
		if (recognition == null) return default_percent_bits_tolerance;
		String modifier = null;
		String[] value = new String[2];
		String result = null;
		for(int i=0;i<recognition.length;i++){
			modifier = recognition[i];
			value = modifier.split(MOD_EQ);
			result=value[0].trim();
			// must be prepared to search for modifier beyond i+1
			if(result.equalsIgnoreCase(MOD_BITTOLERANCE)||
			   result.equalsIgnoreCase(MOD_BT)){
				return processBitsToleranceModifier(value[1]);
			}
		}
		return percentBitsTolerance;
	}

	/**
	 * Extract the BitTolerance=N; or BT=N; modifier of an Image-Based Testing recognition string.
	 * This is the UsePerImageModifiers version.  BitTolerance is optional and may not be present.
	 * <p>
	 * If not present, the routine resets the class to use the default_percent_bits_tolerance 
	 * and the default_useBitsTolerance.  If present and the tolerance is less than 100% then 
	 * the useBitsTolerance boolean is forced to true, as well.  If the tolerance is set to 100% 
	 * then we expect an exact match and the useBitsTolerance boolean is set to false.
	 *
	 * @param imagetype should be MOD_IMAGE, MOD_IMAGEW, or MOD_IMAGEH
	 * @param recognition String [] of recognition modifiers.  should not be null;
	 * @return 1-100 of extracted bit tolerance or default_percent_bits_tolerance.  
	 */
	public static int extractBitsTolerance(String imagetype, String[] recognition){
		resetBitsTolerance();
		if (recognition == null) return default_percent_bits_tolerance;
		String modifier = null;
		String[] value = new String[2];
		String result = null;
		int i=0;
		boolean matched = false;
		for(;!matched && i<recognition.length;i++){
			modifier = recognition[i];
			value = modifier.split(MOD_EQ);
			result=value[0].trim();
			// must be prepared to search for modifier beyond i+1
			matched = isImageType(imagetype, result);
		}
		if(matched){
			for(;i<recognition.length;i++){
				modifier = recognition[i];
				value = modifier.split(MOD_EQ);
				result=value[0].trim();
				//found next image? if so, abort
				if(result.toLowerCase().startsWith("image")) return percentBitsTolerance;
				if(result.equalsIgnoreCase(MOD_BITTOLERANCE)||
				   result.equalsIgnoreCase(MOD_BT)){
					return processBitsToleranceModifier(value[1]);
				}
			}
		}
		return percentBitsTolerance;
	}

	/**
	 * @param relative  Point retrieved from extractHotspot. may be null.
	 * @param area Rectangle found for image on screen
	 * @param pointrelative int value retrieved from extractPointRelative. -1=none or center
	 * @return screen coordinate of hotspot to click or act upon.
	 */
	public static java.awt.Point calcHotspotPoint(java.awt.Point relativehs, Rectangle area, int pointrelative){
		java.awt.Point result = new java.awt.Point((int)area.getCenterX(), (int)area.getCenterY());
		if((relativehs==null)&&(pointrelative==INT_CENTER)) return result;
		switch(pointrelative){
			case INT_TOPLEFT:
				result.x = area.x;
				result.y = area.y;
				break;
			case INT_TOPCENTER:
				result.x = (int) area.x + (area.width/2);
				result.y = area.y;
				break;
			case INT_TOPRIGHT:
				result.x = area.x + area.width;
				result.y = area.y;
				break;
			case INT_LEFTCENTER:
				result.x = area.x;
				result.y = (int) area.y + (area.height/2);
				break;
			case INT_CENTER:
				//already done at top
				break;
			case INT_RIGHTCENTER:
				result.x = area.x + area.width;
				result.y = (int) area.y + (area.height/2);
				break;
			case INT_BOTTOMLEFT:
				result.x = area.x;
				result.y = area.y + area.height;
				break;
			case INT_BOTTOMCENTER:
				result.x = (int) area.x + (area.width/2);
				result.y = area.y + area.height;				
				break;
			case INT_BOTTOMRIGHT:
				result.x = area.x + area.width;
				result.y = area.y + area.height;
				break;
		}
		if(relativehs != null){
			result.x = result.x + relativehs.x;
			result.y = result.y + relativehs.y;
			if(result.x < 0) result.x = 0;
			if(result.y < 0) result.y = 0;
			int w = getScreenWidth();
			int h = getScreenHeight();
			if (result.x > w) result.x = w;
			if (result.y > h) result.y = h;
		}
		Log.info("IU calculated point relative Hotspot is: "+ result);
		return result;
	}
	
	static final String[] _imagerec_alone={"image=", "imagerect=","imagerectangle=",
		                                    "searchrect=", "searchrectangle="};
	
	static final String[] _imagerec = {";hotspot=",";hs=",";searchrect=",
								      ";sr=",";imager=",";imagew=",";imageb=",";imageh=","imagetext=",
								      ";imageright=",";imagewidth=",";imagebottom=",";imageheight=",
								      "imagerect=","imagerectangle="};	
	/**
	 * Attempt to determine if recognition string is for image-based testing
	 * @param recognition -- recognition, usually from App Map
	 * @return true if it contains elements of image-based testing recognition
	 */
	public static boolean isImageBasedRecognition(String recognition){
		try{
			String lcrec = recognition.toLowerCase();
			
			for(int i=0; i < _imagerec_alone.length; i++){
				if(lcrec.startsWith(_imagerec_alone[i])) return true;
			}			
			for(int i=0; i < _imagerec.length;i++){
				if(lcrec.indexOf(_imagerec[i]) > -1) return true;
			}
		}catch(Exception x) {}
		return false;
	}
	
	/**
	 * path must not be null or 0-length
	 * @param TestRecordHelper containing STAFHelper, AppMapName, WinName, and CompName info
	 * returns the absolute path to the file or directory.
	 * relative paths seek PROJECTDIR then DATAPOOLDIR to complete the absolute path.
	 * Only returns if the File exists.  null otherwise.
	 */
	public static File retrieveAbsoluteFileOrDirectory(TestRecordHelper trd, String path){
        try{
        	STAFHelper staf = trd.getSTAFHelper();
            File fn = new CaseInsensitiveFile( path ).toFile( ); 
            if ( !fn.isAbsolute( ) ) {
                String pdir = null;
                String ddir = null;
                try{ 
                	pdir = staf.getVariable( STAFHelper.SAFS_VAR_PROJECTDIRECTORY );
                	ddir =  staf.getVariable( STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY );
                }catch(Exception x){ }
                //if we have access to neither projectdir or datapooldir then fail
                if (((pdir==null)||(pdir.length()==0)) &&
                	((ddir==null)||(ddir.length()==0))){
                	Log.debug("IU.retrieveAbsolute SAFSVARS ProjectDir lookup failed!");
                	return null;
                }
                if((pdir!=null)&&(pdir.length()>0))
                	try{ fn = new CaseInsensitiveFile(pdir, path).toFile();}
                    catch(Exception x){}
                
                if((!fn.isAbsolute())||(!fn.exists()))
                	try{ fn = new CaseInsensitiveFile(ddir, path).toFile();}
                	catch(Exception x){}
            }            
            if(!fn.exists()){
            	Log.debug("IU.retrieveAbsolute file '"+ fn.getPath()+"' does not exist!");
            	return null;
            }
            return fn; // *** MATCHED ***
        }
        catch(Exception fx){}
    	Log.debug("TIDC.retrieveAbsolute failed to locate file: "+ path);
        return null;
	}

	protected static Rectangle adjustImageBRect(BufferedImage pic, Rectangle searchRec){
		int w = pic.getWidth();
		if(w > searchRec.width-16){
			int d = w - searchRec.width;
			searchRec.x = searchRec.x - d -8;
			searchRec.width = w+16;
			if(searchRec.x < 0) searchRec.x = 0;
		}
		return searchRec;
	}
	
	/**
	 * Routine to find an image on the screen.
	 * Limit the search to the searchRec area of the screen. 
	 * Look for the nth instance of the image in the searchRec.
	 * Use the currently set Bit Tolerance.
	 * This routine is called by other routines and is not normally called directly.
	 * @param TestRecordHelper containing STAFHelper, AppMapName, WinName, and CompName info
	 * @param imageBR -- set true ONLY if the search is for a bottom-right image below a top-right image. 
	 * @param imagepath 
	 * @param searchRec
	 * @param nthindex -- If it is 0, when USE_MULTIPLE_THREADS is true, SAFS will use multi-thread to search image;
	 *                                when USE_MULTIPLE_THREADS is false, SAFS will use single-thread to search image;
	 *                    If it is -1, SAFS will use multi-thread to search image;
	 *                    If it is >0, SAFS will use single-thread to search image;
	 * 
	 * @return Rectangle where the image was found or null if not found.
	 * @throws FileNotFoundException if the specified image file cannot be found
	 * @throws IllegalArgumentException if searchRec is not valid
	 * @throws IOException if there is a problem opening or reading files
	 * @throws ImagingOpException if ImageIO cannot process the image file
	 * @throws AWTException if there is a problem with the java.awt.Robot
	 */
	public static Rectangle findImageLocation(TestRecordHelper trd, boolean imageBR, String imagepath, Rectangle searchRec, int nthindex)
	                                         throws FileNotFoundException, IllegalArgumentException, 
	                                                IOException, AWTException, ImagingOpException
	{
        BufferedImage pic = null;
        Rectangle winloc = null;
        
        resetMatchData();
        File fn = retrieveAbsoluteFileOrDirectory(trd, imagepath);
        if(fn==null) throw new FileNotFoundException(imagepath);
        
        if(fn.isFile()){
        	Log.info("IU seeking image: "+ fn.getAbsolutePath());
        	pic = getStoredImage(fn.getAbsolutePath());
        	if(imageBR)
        		searchRec = adjustImageBRect(pic, searchRec);
        	winloc = findBufferedImageOnScreen(pic, searchRec, nthindex);
        }
        //File can be directory. 
        //if it is, then find ANY of the images in the directory
        else{
        	File[] files = fn.listFiles();
        	File file = null;
        	for(int f=0 ; f < files.length; f++){
        		file = files[f];
        		try{
        			if(file.isDirectory()) continue;
                	Log.info("IU seeking directory image: "+ file.getAbsolutePath());
	            	pic = getStoredImage(file.getAbsolutePath());
	            	if(imageBR)
	            		searchRec = adjustImageBRect(pic, searchRec);
	            	winloc = findBufferedImageOnScreen(pic, searchRec, nthindex);
    	        }
        		catch(IllegalArgumentException iox){ throw iox;}
    	        catch(AWTException iox){ throw iox;}
    	        catch(Exception fx){ continue; }
        		if(winloc!=null) break;
        	}
        }
    	return winloc;
	}
	
	/**
	 * Attempt to expand the winloc Rectangle based on locating IMAGEW and IMAGEH 
	 * images specified in the recognition string.  These images essentially locate 
	 * the width and height of a larger area using the initial IMAGE as an top-left 
	 * anchor point. 
	 * <p>
	 * For any given image, first tries to find it by exact match, then tries to find it 
	 * according to any BitTolerance settings specified in the recognition modifiers.
	 * 
	 * @param TestRecordHelper containing STAFHelper, AppMapName, WinName, and CompName info
	 * @param winloc -- Rectangle for the found anchor IMAGE
	 * @param modifiers -- String[] split from the winrec
	 * @return winloc modified to expand to any width or height images found, if any.
	 */
	public static ModifiedRectInfo modifyLocWidthHeight(TestRecordHelper trd, Rectangle winloc, String[] modifiers) throws FileNotFoundException{
        // see if ImageW has been specified for the comp
		ModifiedRectInfo info = new ModifiedRectInfo();
        String imageWpath = extractImagePath(MOD_IMAGEW, modifiers);
        Log.debug("IU extracted ImageW info:"+ imageWpath);
        Rectangle imageWRec = null;
        Rectangle imageWLoc = null;
        int _tolerance = default_percent_bits_tolerance;
        //When USE_MULTIPLE_THREADS is true, we should set the searchIndex to 0, so that
        //SAFS will use multi-thread to search image defined with ImageR or ImageB,ect.
        int searchIndex = (USE_MULTIPLE_THREADS? 0:1);
        int outsets = 16;
        if (imageWpath != null){
        	info.imageW = imageWpath;
        	try { // search to the right of the found winloc with OUTSETS
        		imageWRec = new Rectangle(winloc.x + winloc.width,
        				winloc.y - 8,  // TODO: OUTSETS here
        				ImageUtils.getScreenWidth()-(winloc.x+winloc.width),
        				winloc.height + outsets); // TODO: OUTSETS here
        		//check for per image modifiers
        		if(USE_PER_IMAGE_MODIFIERS){
        			imageWRec = extractSearchRect(MOD_IMAGEW, modifiers, imageWRec);
        			searchIndex = extractImageIndex(MOD_IMAGEW, modifiers);
        		}
        		// TODO: validate\clip imageWRec widths and heights to screen edges 
        		
        		resetBitsTolerance();        		
        		imageWLoc = findImageLocation(trd, false, imageWpath, imageWRec, searchIndex);
        		if(imageWLoc==null){
        			_tolerance = USE_PER_IMAGE_MODIFIERS ?
        					     extractBitsTolerance(MOD_IMAGEW, modifiers):
        					     extractBitsTolerance(modifiers);
        			if(_tolerance != default_percent_bits_tolerance)
                		imageWLoc = findImageLocation(trd, false, imageWpath, imageWRec, searchIndex);
        		}
        		if(imageWLoc != null){
            		Log.info("IU ImageW MATCHED top-right: "+ imageWLoc.x +", "+ imageWLoc.y);
        		}else{
            		Log.info("IU did not find ImageW at top-right...");
        		}
        	}
        	catch(FileNotFoundException fnf){ 
        		Log.debug("IU processing Image File or Directory Not Found for "+ fnf.getMessage());
        		throw fnf; }
        	catch(Exception x){
            	// what if we find no ImageW image to seek?  Ignore ImageW ?
        		Log.info("IU Ignoring ImageW search attempt: "+ x.getClass().getSimpleName());
        	}
        }
        // see if ImageH has been specified for the window
        String imageHpath = extractImagePath(ImageUtils.MOD_IMAGEH, modifiers);
        Log.debug("IU extracted ImageH info:"+ imageHpath);
        Rectangle imageHRec = null;
        Rectangle imageHLoc = null;
        if (imageHpath != null){
        	info.imageH = imageHpath;
        	try { // search to the right of the found winloc with OUTSETS
        		imageHRec = new Rectangle(winloc.x - 8,   // TODO: OUTSETS here
        				winloc.y + winloc.height,
        				winloc.width + outsets,   // TODO: OUTSETS here
        				ImageUtils.getScreenHeight()-(winloc.y+winloc.height));
        		
        		if(USE_PER_IMAGE_MODIFIERS){
        			imageHRec = extractSearchRect(MOD_IMAGEH, modifiers, imageHRec);
        			searchIndex = extractImageIndex(MOD_IMAGEH, modifiers);
        		}
        		
        		// TODO: validate\clip imageWRec widths and heights to screen edges

        		resetBitsTolerance();        		
        		imageHLoc = findImageLocation(trd, false, imageHpath, imageHRec, searchIndex);
        		if(imageHLoc==null){
        			_tolerance = USE_PER_IMAGE_MODIFIERS ?
   					     extractBitsTolerance(MOD_IMAGEH, modifiers):
   					     extractBitsTolerance(modifiers);
        			if(_tolerance != default_percent_bits_tolerance)
                		imageHLoc = findImageLocation(trd, false, imageHpath, imageHRec, searchIndex);
        		}
        		if(imageHLoc != null){
            		Log.info("IU ImageH MATCHED bottom-left: "+ imageHLoc.x +", "+ imageHLoc.y);
        		}else{
            		Log.info("IU did not find ImageH at bottom-left...");
        		}
        		if((imageHLoc==null)&&(imageWLoc != null)){
            		imageHRec.x = imageWLoc.x - 8;  // TODO: OUTSETS here
		            imageHRec.y = imageWLoc.y + imageWLoc.height;
		            imageHRec.height = ImageUtils.getScreenHeight()-
		                               (imageWLoc.y+imageWLoc.height);
		            imageHRec.width = imageWLoc.width + outsets;
	
	        		if(USE_PER_IMAGE_MODIFIERS){
	        			imageHRec = extractSearchRect(MOD_IMAGEH, modifiers, imageHRec);
	        			searchIndex = extractImageIndex(MOD_IMAGEH, modifiers);
	        		}

	        		// TODO: validate\clip imageWRec widths and heights to screen edges 

	        		resetBitsTolerance();        		
	        		imageHLoc = findImageLocation(trd, true, imageHpath, imageHRec, searchIndex);
	        		if(imageHLoc==null){
	        			_tolerance = USE_PER_IMAGE_MODIFIERS ?
       					     extractBitsTolerance(MOD_IMAGEH, modifiers):
       					     extractBitsTolerance(modifiers);
	        			if(_tolerance != default_percent_bits_tolerance)
	                		imageHLoc = findImageLocation(trd, true, imageHpath, imageHRec, searchIndex);
	        		}
            		if(imageHLoc != null){
                		Log.info("IU ImageH MATCHED bottom-right: "+ imageHLoc.x +", "+ imageHLoc.y);
            		}else{
                		Log.info("IU did not find ImageH at bottom-right...");
            		}
        		}        		
        		// if imageW valid but not found see if it is in bottom right
        		if((imageHLoc!=null)&&(imageWLoc==null)&&(imageWpath!=null)){
            		imageWRec = new Rectangle(imageHLoc.x + imageHLoc.width,
			                  imageHLoc.y - 8,  // TODO: OUTSETS here
			                  ImageUtils.getScreenWidth()-(imageHLoc.x+imageHLoc.width),
			                  imageHLoc.height + outsets); // TODO: OUTSETS here
	
            		if(USE_PER_IMAGE_MODIFIERS){
            			imageWRec = extractSearchRect(MOD_IMAGEW, modifiers, imageWRec);
	        			searchIndex = extractImageIndex(MOD_IMAGEW, modifiers);
	        		}

            		// TODO: validate\clip imageWRec widths and heights to screen edges 

            		resetBitsTolerance();        		
            		imageWLoc = findImageLocation(trd, false, imageWpath, imageWRec, searchIndex);
            		if(imageWLoc==null){
            			_tolerance = USE_PER_IMAGE_MODIFIERS ?
       					     extractBitsTolerance(MOD_IMAGEW, modifiers):
       					     extractBitsTolerance(modifiers);
            			if(_tolerance != default_percent_bits_tolerance)
                    		imageWLoc = findImageLocation(trd, false, imageWpath, imageWRec, searchIndex);
            		}
            		if(imageWLoc != null){
                		Log.info("IU ImageW MATCHED bottom-right: "+ imageWLoc.x +", "+ imageWLoc.y);
            		}else{
                		Log.info("IU did not find ImageW at bottom-right...");
            		}
        		}
        	}
        	catch(FileNotFoundException fnf){
        		Log.debug("IU processing Image File or Directory Not Found for "+ fnf.getMessage());
        		throw fnf; }
        	catch(Exception x){
            	// what if we find no ImageW image to seek?  Ignore ImageW ?
        		Log.info("TIDC Ignoring ImageH search attempt: "+ x.getClass().getSimpleName());
        	}
        }
        if(imageWLoc != null){
        	info.imageWRect = imageWLoc;
        	winloc.width = imageWLoc.x + imageWLoc.width - winloc.x; // TODO: INSETS here
        }
        if(imageHLoc != null){
        	info.imageHRect = imageHLoc;
        	winloc.height = imageHLoc.y + imageHLoc.height - winloc.y; // TODO: INSETS here
        }
        info.resultRect = winloc;
        return info;
	}

	/**
	 * Calls findComponentRectangle with a timeout of 0.
	 * @param trd
	 * @return
	 * @throws IOException
	 * @throws AWTException
	 * @throws SAFSException
	 * {@link #findComponentRectangle(TestRecordHelper, long)}
	 */
	public static Rectangle findComponentRectangle(TestRecordHelper trd)
	throws IOException, AWTException, SAFSException
	{
		return findComponentRectangle(trd, 0);
	}
	
	/**
	 * Locate the final Window:Component Rectangle specified by the test record.
	 * Search for Window first.  Then find Component within Window.
	 * This is the workhorse routine for component functions to call to deduce 
	 * the Rectangle targeted by the Window:Comp recognition information.
	 * <p>
	 * For any and all images, we try to find an exact match first, then try to use any 
	 * BitsTolerance that might be supplied in the recognition strings.
	 * 
	 * @param TestRecordHelper containing STAFHelper, AppMapName, WinName, and CompName info
	 * @param timeout in seconds to loop the search until found. <=0 means check only once.
	 * @return Rectangle identifying the area on the screen targetted by the 
	 * Window:Comp app map recognition strings or null.
	 * @throws IOException if we cannot open or read specified files
	 * @throws AWTException if we cannot use the java.awt.Robot
	 * @throws SAFSException if we cannot extract valid data from TestRecordHelper or STAF
	 */
	public static Rectangle findComponentRectangle(TestRecordHelper trd, long timeout)
							throws IOException, AWTException, SAFSException
	{
        String debugmsg = ImageUtils.class.getName()+".findComponentRectangle(): ";
        
		// start with locating the Window and its recognition.
		STAFHelper staf = trd.getSTAFHelper();
		String winname = null;
		try{ winname = trd.getWindowName();}
		catch(SAFSException x){throw new SAFSException("WindowID");}
		String compname = trd.getCompName();
		String mapname = trd.getAppMapName();
        String winrec = trd.getWindowGuiId();
        
        
        if (winrec==null) {
        	winrec = staf.getAppMapItem(mapname, winname, winname);
        	if(winrec!=null)trd.setWindowGuiId(winrec);
        }

        // TODO CANAGL support CompRec is Whole Screen search
        String winimagetype = MOD_IMAGE;
        String[] winmodifiers = null;;        
        String winimagepath = null;
        String winimagerectangle = null;
        int winnthindex = 0;
        
        boolean skipWinImageSearch = (winrec == null);
        
        //if (winrec==null) throw new SAFSException("WindowGUIID"); // errors and status already handled
        if (!skipWinImageSearch){
        	winmodifiers = winrec.split(MOD_SEP);        
        	winimagepath = extractImagePath(MOD_IMAGE, winmodifiers);
            
        	// if no valid Image specified, check for a win search rectangle instead.
        	if(winimagepath==null){
            	//We may use ImageRect= or SearchRect= to supply the top win's rectangle directly
            	winimagerectangle = extractImagePath(ImageUtils.MOD_IMAGE_RECT,winmodifiers);
            	if(winimagerectangle==null) 
            		winimagerectangle = extractImagePath(ImageUtils.MOD_IMAGE_RECTANGLE,winmodifiers);            	
            	
            	if(winimagerectangle==null) {
                	winimagerectangle = extractImagePath(ImageUtils.MOD_SEARCH_RECT,winmodifiers);
            		if(winimagerectangle == null)
                    	winimagerectangle = extractImagePath(ImageUtils.MOD_SEARCH_RECTANGLE,winmodifiers);
            		if (winimagerectangle == null) {
                    	Log.debug(debugmsg+" Valid window image/rectangle could not be deduced from Window Recognition: "+ winrec);
            			throw new SAFSException("WindowGUIID/Recognition");
            		}
            	}
        		winimagetype = MOD_IMAGE_RECT;
            	Log.debug(debugmsg+" Got window image rectangle "+winimagerectangle);
            }else{
            	//Index= only applicable for Images, not ImageRect
                winnthindex = USE_PER_IMAGE_MODIFIERS ?
                		      extractImageIndex(MOD_IMAGE, winmodifiers):
                		      extractImageIndex(winmodifiers); // will be > 0
            }
        }else{ // need to default winimagerectangle as whole screen
    		winimagetype = MOD_IMAGE_RECT;
    		winmodifiers = new String[0]; // cannot be null when seeking winsearchRec below
        	Log.debug(debugmsg+" Forcing window image rectangle to be entire screen.");
        }
        

        Rectangle winsearchRec = extractSearchRect(winimagetype, winmodifiers); // can be null
        Log.info("IU Win Image SearchRec:"+ winsearchRec);
        
        Rectangle anchor = null;
        ModifiedRectInfo wininfo = null;
        Rectangle winloc = null;

        String comprec = null;
        String[] compmodifiers = null;
        String compimagepath = null;
        int compnthindex = 0;
        ModifiedRectInfo compinfo = null;
        Rectangle comploc = null;
        Rectangle expandloc = null;
        String compimagetext = null; // variable to store the image text of component window
        
        boolean seekcomp = !compname.equalsIgnoreCase(winname);
        if(seekcomp) {
            try{ comprec = trd.getCompGuiId();}catch(SAFSException x){}
            if (comprec==null) {
            	comprec = staf.getAppMapItem(mapname, winname, compname);
            	if(comprec != null) trd.setCompGuiId(comprec);
            }
            if (comprec==null) throw new SAFSException("CompGUIID");
            compmodifiers = comprec.split(ImageUtils.MOD_SEP);        
            compimagepath = extractImagePath(ImageUtils.MOD_IMAGE, compmodifiers);
            
            //add ImageText= support
            if (compimagepath == null){
            	compimagetext = extractImagePath(ImageUtils.MOD_IMAGETEXT, compmodifiers);
            	Log.info("IU Comp ImageText=" + compimagetext);
            }    
            if(compimagepath==null && compimagetext==null) return null;
            if(compimagetext==null){
            	compnthindex = USE_PER_IMAGE_MODIFIERS ?
            				   extractImageIndex(MOD_IMAGE, compmodifiers):
            			       extractImageIndex(compmodifiers);
            }else{
            	compnthindex = USE_PER_IMAGE_MODIFIERS ?
     				   extractImageIndex(MOD_IMAGETEXT, compmodifiers):
     			       extractImageIndex(compmodifiers);
            }
        }        
        
        long endtime = System.currentTimeMillis();
        if (timeout > 0 ) endtime += 1000*timeout;
        boolean loop = true;
        int w = 0;
        int h = 0;
        int _tolerance = default_percent_bits_tolerance;
        do{
        	anchor = null;
        	wininfo = null;
        	winloc = null;

        	if(winimagerectangle!=null){
        		//If winimagerectangle!=null, then This means that the anchor is given
        		//directly by RS ImageRect=x,y,width,height, we just need to get the 
        		//rectangle anchor from the string "x,y,width,height"
        		anchor = StringUtilities.formRectangle(winimagerectangle,MOD_COMMA);
        	}else{
            	//first try an exact match before trying any BitsTolerance match
            	//this avoids a 40 secs search that could be done in less than 1 sec for an exact match
	        	resetBitsTolerance();
	        	anchor = findImageLocation(trd,false, winimagepath, winsearchRec, winnthindex);
	        	if(anchor==null){
        			_tolerance = USE_PER_IMAGE_MODIFIERS ?
   					     extractBitsTolerance(MOD_IMAGE, winmodifiers):
   					     extractBitsTolerance(winmodifiers);
	            	if(_tolerance < default_percent_bits_tolerance)
	            		anchor = findImageLocation(trd,false, winimagepath, winsearchRec, winnthindex);        		
	        	}
        	}
        	loop = (anchor==null);
        	// if anchor found, check for comp
        	if(!loop){
                Log.info("IU Win Image Anchor:"+ anchor);
                // see if imageW and imageH have been specified
                wininfo = modifyLocWidthHeight(trd, anchor, winmodifiers);      
                if(!wininfo.rectModifyFailure()){
	                winloc = wininfo.getResult();
	                Log.info("IU Win Image Modified Rect:"+ winloc);
	                if(seekcomp){
	                	comploc = null;
	                	compinfo = null;
	                	expandloc = null;
	                    resetBitsTolerance();
	                    
	                    // try "ImageText=" to use OCR to locate text area in anchor on screen
	                    if (compimagetext != null){ //
	                    	comploc = findImageTextInRectangle(compimagetext,compnthindex,staf,compmodifiers,winloc);
	                    }
	                    else{// Image instead of ImageText
                    		comploc = findCompImageInRectangle(trd,compimagepath,compnthindex,compmodifiers,winloc);
	                    }
	                    if(comploc==null){
	                		w = screenWidth - anchor.x;
	                		h = screenHeight - anchor.y;
	                    	if(wininfo.rectModified()){            		
	                    		if(wininfo.hasImageW()){
	                    			w = winloc.width;            			
	                                Log.info("IU Comp Image limiting Modified Search WIDTH to:"+ w);
	                    		}
	                    		if(wininfo.hasImageH()){
	                    			h = winloc.height;            			
	                                Log.info("IU Comp Image limiting Modified Search HEIGHT to:"+ h);
	                    		}
	                    	}
	                    	expandloc = new Rectangle(winloc.x, winloc.y, w, h);
	                    	if(compimagetext!=null){//ImageText
	                    	    comploc = findImageTextInRectangle(compimagetext,compnthindex,staf,compmodifiers,expandloc);	                    		
	                    	}else{// Image instead of ImageText
	                    		comploc = findCompImageInRectangle(trd,compimagepath,compnthindex,compmodifiers,expandloc);
	                    	}
	                    }
	                    if(comploc!=null){
		                    // see if imageW and imageH have been specified
		                    compinfo = modifyLocWidthHeight(trd, comploc, compmodifiers);
		                    if(!compinfo.rectModifyFailure()){
			                    comploc = compinfo.getResult();
			                    winloc = comploc;
			                    Log.info("TIDC Comp Rect:"+ winloc);
			                    loop = false;
		                    }else{// ImageR or ImageB was specified but NOT found
		                    	loop = System.currentTimeMillis() < endtime;
		                    }
	                    }else{
	                    	loop = System.currentTimeMillis() < endtime;
	                    }
	                }//seekcomp
                }else { //if rectModifyFailure -- ImageR or ImageB specified but NOT found
                	loop = System.currentTimeMillis() < endtime;
                }
        	}
        	if(loop){
        		try{Thread.sleep(100);}catch(InterruptedException n){;}
        		if(System.currentTimeMillis() < endtime) recaptureScreen();
        	}
        }while( loop && (System.currentTimeMillis() < endtime));
        if (anchor==null){
            Log.info("IU Anchor Image not found.");
            return null;
        }
        if(seekcomp &&(comploc==null)){        	
            Log.info("IU Comp Image not found.");
            return null;
        }
        return winloc;
	}

	/**
	 * Called by findComponentRectangle.  Not normally called otherwise.
	 * @param imagepath
	 * @param imageindex
	 * @param modifiers should not be null
	 * @param rect screen area to search.
	 * @return found Image Rectangle or null.
	 * @throws IOException
	 * @throws AWTException
	 * @throws SAFSException
	 * {@link #findComponentRectangle(TestRecordHelper, long)}
	 */
	static Rectangle findCompImageInRectangle(TestRecordHelper trd,
											  String imagepath,
			                                  int imageindex,
			                                  String[] modifiers, 
			                                  Rectangle rect)
											  throws IOException, AWTException, SAFSException {
        Rectangle compsearchRec = extractSearchRect(MOD_IMAGE, modifiers, rect); // can be null
        Log.info("IU Comp Image not found, expanding Modified SearchRec to:"+ compsearchRec);
        resetBitsTolerance();
       	Rectangle comploc = findImageLocation(trd, false, imagepath, compsearchRec, imageindex);
       	if(comploc==null){
       		int _tolerance = USE_PER_IMAGE_MODIFIERS ?
				             extractBitsTolerance(MOD_IMAGE, modifiers):
					         extractBitsTolerance(modifiers);
       		if(_tolerance != default_percent_bits_tolerance)
               	comploc = findImageLocation(trd, false, imagepath, compsearchRec, imageindex);
       	}
		return comploc;
	}
	
	/**
	 * Called by findComponentRectangle.  Not normally called otherwise.
	 * @param imagetext
	 * @param imageindex
	 * @param staf
	 * @param modifiers should not be null
	 * @param rect area on screen to search
	 * @return found ImageText Rectangle or null.
	 * @throws IOException
	 * @throws AWTException
	 * @throws SAFSException
	 * {@link #findComponentRectangle(TestRecordHelper, long)}
	 */
	static Rectangle findImageTextInRectangle(String imagetext,
			                                  int imageindex, 
			                                  STAFHelper staf, 
			                                  String[] modifiers, 
			                                  Rectangle rect)
											  throws IOException, AWTException, SAFSException {
    	//to see if followed by SearchRect=|SR=, which limits or expands the area for searching
    	Rectangle compsearchRect = extractSearchRect(MOD_IMAGETEXT, modifiers, rect);
    	if(compsearchRect==null) compsearchRect = rect;
    	if(compsearchRect.height < 12){
        	Log.info("IU Comp ImageText SearchRec too small:"+ compsearchRect);
        	return null;
    	}
    	Log.info("IU Comp ImageText using SearchRec:"+ compsearchRect);
    	BufferedImage winImageOnScreen	= captureScreenArea(compsearchRect);
    	//TODO Here we will force to get the TOCR Engine, as only TesseractOCREngine can
    	//support to get location of a text within an image. In furture, if GOCR support also
    	//this, we can change the first parameter to null so that we use the engine defined by
    	//STAF variable.
    	//OCREngine ocr = OCREngine.getOCREngine(null, staf);
    	OCREngine ocr = OCREngine.getOCREngine(OCREngine.OCR_T_ENGINE_KEY, staf);
    	String languageID = OCREngine.getOCRLanguageCode(staf);
    	
    	float zoom = ocr.getdefaultZoomScale();
    	Rectangle textRect = ocr.findTextRectFromImage(imagetext,
    													imageindex,
    													winImageOnScreen, 
    													languageID,
    													null, 
    													zoom);
    	Rectangle comploc = null;
    	if (textRect==null){
            Log.info("IU Comp ImageText not found by TOCR in this area.");
    	}
    	else{
    		comploc = new Rectangle(compsearchRect.x + (int)(textRect.x/zoom), 
    								compsearchRect.y + (int)(textRect.y/zoom),
    								(int)(textRect.width/zoom), 
    								(int)(textRect.height/zoom));
    	    Log.info("IU Comp ImageText found by TOCR at " + comploc);
    	}
    	return comploc;
	}
	
	/**
	 * Store our BufferedImage into a File.
	 * @param image - BufferedImage for ImageIO to write to file
	 * @param file -- valid full absolute File to write to
	 * @throws SecurityException thrown if permission to write to the location is denied
	 * @throws IllegalArgumentException if ImageIO doesn't like our invocation
	 * @throws IOException if an error occurs while writing.
	 * @throws NoClassDefFoundError if support for ImageIO is not found (Java Advanced Imaging)
	 */
	public static void saveImageToFile(BufferedImage image, File file) 
	                                  throws SecurityException, IllegalArgumentException, 
	                                         IOException, NoClassDefFoundError{
		boolean success = false;
    	//Use ImageIO to write image to a file, so that the same content will be used in the verifyGUIImageToFile()
		Log.debug("IU saveImage attempting to save image to file: "+file.getAbsolutePath());
		String lowerCaseFileName = file.getName().toLowerCase();
    	if (lowerCaseFileName.endsWith(".jpg") ||
    		lowerCaseFileName.endsWith(".jpeg")) {
    		//If the BufferedImage object image contains Alpha value (type is TYPE_INT_ARGB, TYPE_4BYTE_ABGR etc.)
    		//the jpg file will be shown with an odd color (with a nasty red tint)
    		//we need to remove the Alpha value from BufferedImage and save to .jpg file.
    		success = ImageIO.write(convertToRGBImage(image),"JPEG",file);
    		
    	}else if (lowerCaseFileName.endsWith(".bmp")) {
    		//If the BufferedImage object image contains Alpha value (type is TYPE_INT_ARGB, TYPE_4BYTE_ABGR etc.)
    		//you won't have success using ImageIO.write(image, "BMP", new File("D:\\test.bmp"))
    		//we need to remove the Alpha value from BufferedImage and save to .bmp file.
    		success = ImageIO.write(convertToRGBImage(image),"BMP",file);
    		
    	}else if (lowerCaseFileName.endsWith(".tif") ||
    			  lowerCaseFileName.endsWith(".tiff")) {
    		success = ImageIO.write(image,"TIF",file);
    	}else if (lowerCaseFileName.endsWith(".gif")) {
    		success = ImageIO.write(image,"GIF",file);
    	}else if (lowerCaseFileName.endsWith(".png")) {
    		success = ImageIO.write(image,"PNG",file);
    	}else if (lowerCaseFileName.endsWith(".pnm")) {
    		success = ImageIO.write(image,"PNM",file);
    	}else{
    		Log.debug("IU saveImage unsupported image format specification!");
    		throw new IllegalArgumentException("Only JPG, BMP, TIF, GIF, PNG and PNM files are currently supported.");
    	}
    	
    	if(!success){
    		throw new IOException("ImageIO.write() fail to work.");
    	}
	}
	
	/**
	 * Make a deep copy of original BufferedImage.<br>
	 * Do NOT copy the Raster.<br>
	 * But paint the original image on a new BufferedImage of the same size and type, so the<br>
	 * Raster has the same size as image (will not contain more data with an offset).<br>
	 * @param bi BufferedImage, the image to copy.
	 * @return BufferedImage, a deep copy of BufferedImage.
	 */
	public static BufferedImage copy(BufferedImage image){
		String debugmsg = StringUtils.debugmsg(false);
		BufferedImage copiedImage = null;
		if(image==null){
			IndependantLog.warn(debugmsg+" the image is null!");
			return image;
		}
		copiedImage = convertImage(image, image.getType());
		
		if(copiedImage==null){
			if(debug) IndependantLog.warn(debugmsg+" the converted image is null!");
			//TODO find another way to copy the original image
			//BE CAREFUL, don't copy the Raster, dataBuffer, dataModel
			return image;
		}
		
		return copiedImage;
	}
	
	/**
	 * Make a deep copy of original BufferedImage, copy the Raster.<br>
	 * This method is not very reliable, sometimes it fail.<br>
	 * @param bi BufferedImage, the image to copy.
	 * @return BufferedImage, a deep copy of BufferedImage.
	 */
	public static BufferedImage copyRaster(BufferedImage bi) {
		String debugmsg = StringUtils.debugmsg(false);
		try{
			ColorModel cm = bi.getColorModel();
			boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			WritableRaster raster = bi.copyData(null);
			return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		}catch(Exception e){
			IndependantLog.error(debugmsg+" fail due to "+StringUtils.debugmsg(e));
		}
		return null;
	}
	
	/**
	 * Convert the BufferedImage to another type of BufferedImage.<br>
	 * Some types cannot convert to, for example BufferedImage.TYPE_CUSTOM<br>
	 * @param image BufferedImage, the image to convert.
	 * @param type int, the type to convert to. 
	 * @return BufferedImage, the converted image
	 */
	public static BufferedImage convertImage(BufferedImage image, int type){
		String debugmsg = StringUtils.debugmsg(false);

		try{
			if(BufferedImage.TYPE_CUSTOM==type){
				if(debug) IndependantLog.error(debugmsg+" Cannot convert custom type image.");
				return null;
			}
			// Create the buffered image of a certain type
			BufferedImage tempImage = new BufferedImage(image.getWidth(), image.getHeight(), type);
			Graphics g = tempImage.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			return tempImage;
		}catch(Throwable e){
			IndependantLog.error(debugmsg+" Fail to convert image to type '"+type+"', Met "+StringUtils.debugmsg(e));
		}

		return null;
	}
	
	/**
	 * If the image contains alpha value, then remove it and create a new RGB Image.
	 * @param image BufferedImage, the image to convert
	 * @return BufferedImage, an RGB image without alpha value.
	 */
	public static BufferedImage convertToRGBImage(BufferedImage image){
		String debugmsg = StringUtils.debugmsg(false);

		try{
			if(image.getColorModel().hasAlpha()){
				return convertImage(image, BufferedImage.TYPE_INT_RGB);
			}else{
				return image;
			}
		}catch(Exception e){
			IndependantLog.error(debugmsg+" Fail to convert to RGB Image, Met "+StringUtils.debugmsg(e));
		}

		return null;
	}
	
	/**
	 * @param image Image, the image to detect
	 * @return boolean, true if the image contains 'alpha' value; false otherwise.
	 * @throws IOException
	 */
	public static boolean hasAlpha(Image image) throws IOException{
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
			return pg.getColorModel().hasAlpha();
		} catch (InterruptedException e) {
			throw new IOException("Fail to detect the alpha value.");
		}
	}
	
	/**
	 * @param fileName
	 * @return	true if the fileName is one of the supported image format
	 */
	public static boolean isImageFormatSupported(String fileName){
		if(fileName==null || "".equals(fileName)) return false;
		
		String lowerCaseFileName = fileName.trim().toLowerCase();
		Log.debug("IU.isImageFormatSupported(): Testing "+fileName);
		
    	if (lowerCaseFileName.endsWith(".jpg") ||
    		lowerCaseFileName.endsWith(".jpeg") ||
    		lowerCaseFileName.endsWith(".bmp") ||
    		lowerCaseFileName.endsWith(".tif") ||
    		lowerCaseFileName.endsWith(".tiff") ||
    		lowerCaseFileName.endsWith(".gif") ||
    		lowerCaseFileName.endsWith(".png") ||
    		lowerCaseFileName.endsWith(".pnm")) {
    		return true;
    	}else{
    		Log.debug("IU unsupported image format specification!");
    		return false;
    	}
	}
	
	/**
	 * Check the suffix of filename, if it is not supported then append '.bmp' as suffix.
	 * @param filename, String, the filename to check
	 * @return
	 * @throws SAFSException
	 */
	public static String normalizeFileNameSuffix(String filename) throws SAFSException{
		if (filename==null || filename.length()==0) {
			throw new SAFSException("File name is not provided!");
		}
		// currently we offer support for JPG, BMP, TIF, GIF, PNG and PNM, default to bmp
		if(!isImageFormatSupported(filename)){
			Log.info("Image file format is not supported yet. file name: " + filename + " ; convert it to default format '.bmp' .");
			filename = filename + ".bmp";
		}
		return filename;
	}
	
	/**
	 * @return	A FileFilter, which accepts files of format supported in {@link #isImageFormatSupported(String)} or directories.
	 */
	public synchronized static FileFilter getImageFileFilter(){
		if(imageFileFilter==null){
			imageFileFilter = new FileFilter(){
				public boolean accept(File file) {
					boolean accepted = ImageUtils.isImageFormatSupported(file.getName()) || file.isDirectory(); 
					return accepted;
				}
	
				public String getDescription() {
					return "JPG, BMP, TIF, GIF, PNG and PNM Images";
				}
			};
		}
		return imageFileFilter;
	}
	
	/**
	 * <b>Purpose</b>      Create a new BufferedImage Object with a certain type.<br>
	 * @param width
	 * @param height
	 * @param defaultType  The type of the new BufferedImage
	 * @param altType      If the default type can not be used to create a BufferedImage,<br>
	 *                     the altType will be used.
	 * @return
	 */
	public static BufferedImage getVoidBufferImage(int width, int height, int defaultType, int altType){
		BufferedImage image = null;
		
		try{
			image = new BufferedImage(width,height,defaultType);
		}catch(Exception e){
			Log.debug("IU: getVoidBufferImage() "+e.getMessage());
			if(altType>BufferedImage.TYPE_CUSTOM && altType<BufferedImage.TYPE_BYTE_INDEXED){
				image = new BufferedImage(width,height,altType);
			}else{
				image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			}
		}
		
		return image;
	}
	
	/**
	 * <b>Purpose</b>     Create a new BufferedImage object, its size is destWidth*destHeight<br>
	 *                    If the size is samller than the source image, copy that part from source<br>
	 *                    image to this new image; While if the size is bigger, copy the whole source<br>
	 *                    image to this new image, and the part beyond will be filled by initialColor<br>
	 *                    This method is used when drag to resize an image in ImageManager2<br>
	 *                    
	 * @param srcImage		BufferedImage, The source image to be copied.
	 * @param destWidth		int, the destination image width
	 * @param destHeight	int, the destination image height
	 * @param initialColor	Color, the color to paint on the destination image as initial color
	 * @return
	 */
	public static BufferedImage getCopiedImage(BufferedImage srcImage, 
            int destWidth, int destHeight, Color initialColor){
        return ImageUtils.getCopiedImage(srcImage, 0, 0, 0, 0, destWidth, destHeight, initialColor);
    }
	
	/**
	 * <b>Purpose</b>		Copy the source image to a new image, whose width and height are given<br>
	 *                  	by destWidth and destHeight; The area of new image outside of the source <br>
	 *                  	image will be filled with the color provided by parameter.<br>
	 *                      
	 * @param srcImage		BufferedImage, The source image to be copied.
	 * @param srcOffsetX	int, the x coordination in source image to begin copy
	 * @param srcOffsetY	int, the y coordination in source image to begin copy
	 * @param destOffsetX	int, the x coordination in destination image to begin paste
	 * @param destOffsetY   int, the y coordination in destination image to begin paste
	 * @param destWidth		int, the destination image width
	 * @param destHeight	int, the destination image height
	 * @param initialColor	Color, the color to paint on the destination image as initial color
	 * @return
	 */
	public static BufferedImage getCopiedImage(BufferedImage srcImage, 
			                                   int srcOffsetX, int srcOffsetY, /* source offset, from it to begin copy*/
			                                   int destOffsetX, int destOffsetY, /* dest offset, from it to begin paste*/
			                                   int destWidth, int destHeight, /* the new image's width and height*/
			                                   Color initialColor){
		if(srcImage==null || destWidth<=0 || destHeight<=0 ){
			Log.debug("IU.getCopiedImage(): Input Parameter error.");
			return null;
		}
		if(initialColor==null){
			initialColor = Color.WHITE;
		}
		
//		BufferedImage destImage = new BufferedImage(destWidth,destHeight,srcImage.getType());
		BufferedImage destImage = getVoidBufferImage(destWidth,destHeight,srcImage.getType(),BufferedImage.TYPE_INT_RGB);
		
		WritableRaster destRaster = destImage.getRaster();
		ColorModel destColorModel = destImage.getColorModel();
		Object inData = destColorModel.getDataElements(initialColor.getRGB(), null);
		
		//Set the dest raster to initialColor
		for (int i = 0; i < destWidth; i++) {
			for (int j = 0; j < destHeight; j++) {
				destRaster.setDataElements(i, j, inData);
			}
		}
		//Copy the source raster to the dest raster
//		if(destImage.getType()==srcImage.getType()){
			//When select an area to copy, only PNG image can be corectly copied to dest image
			//For JPG, BMP ect, if srcOffsetX and srcOffsetY are bigger than 0, (that is we want to
			//copy a sub area of source to the dest image), srcOffsetX and srcOffsetY will be always
		    //reset to 0??????, why????
//			destRaster.setRect((destOffsetX-srcOffsetX),(destOffsetY-srcOffsetY),srcImage.getRaster());
//		}else{
			int rgb = 0;
			if(srcOffsetX+destWidth-destOffsetX>srcImage.getWidth()){
				destWidth = srcImage.getWidth()+destOffsetX-srcOffsetX;
			}
			if(srcOffsetY+destHeight-destOffsetY>srcImage.getHeight()){
				destHeight = srcImage.getHeight()+destOffsetY-srcOffsetY;
			}
			for (int i = destOffsetX; i < destWidth; i++) {
				for (int j = destOffsetY; j < destHeight; j++) {
					rgb = srcImage.getRGB(i-destOffsetX+srcOffsetX, j-destOffsetY+srcOffsetY);
					destImage.setRGB(i, j, rgb);
				}
			}
//		}
		
		return destImage;
	}
	/**
	 * <b>Purpose</b>      Modify the source image by painting a simple color on an area, this<br>
	 *                     area is defined by offsetX,offsetY,width,height<br>
	 *                     This method is used when cut an area from an image in ImageManager2<br>
	 *                     
	 * @param image
	 * @param offsetX
	 * @param offsetY
	 * @param width
	 * @param height
	 * @param initialColor
	 * @param paintOnNewImage false, paint on original image; true, paint on a new buffered image.
	 */
	public static BufferedImage paintOnImage(BufferedImage image, 
                                int offsetX, int offsetY,
                                int width, int height, 
                                Color initialColor, boolean paintOnNewImage){
		if(image==null){
			Log.debug(StringUtils.debugmsg(false)+"Input Parameter error, image is null.");
			return null;
		}
		if(initialColor==null){
			initialColor = Color.WHITE;
		}
		
		BufferedImage dest = null;
		WritableRaster raster = null;
		Object inData = null;
		if(!paintOnNewImage){/*paint on the source image*/
			dest = image;
		}else{
			dest = ImageUtils.getCopiedImage(image, image.getWidth(), image.getHeight(), null);
		}
		
		raster = dest.getRaster();
		inData = dest.getColorModel().getDataElements(initialColor.getRGB(), null);
		
		if(offsetX<0) offsetX=0;
		if(offsetY<0) offsetY=0;
		if((offsetX+width)>dest.getWidth()) width = dest.getWidth()-offsetX;
		if((offsetY+height)>dest.getHeight()) height = dest.getHeight()-offsetY;
		
		int endX = offsetX+width;
		int endY = offsetY+height;
		//Set the raster to initialColor
		for (int i = offsetX; i < endX; i++) {
			for (int j = offsetY; j < endY; j++) {
				raster.setDataElements(i, j, inData);
			}
		}
		
		return dest;
	}
	
	/**
	 * <b>Purpose</b>      Get an area from the source image, this area is defined by <br>
	 *                     srcOffsetX,srcOffsetY,width,height; Then copy this area to the <br>
	 *                     destImage, the point to start to paint is (destOffsetX,destOffsetY)<br>
	 *                     If the area is too big, beyond the source image, then the souce image<br>
	 *                     will be enlarged, the extra part will be painted with initColor.<br>
	 *                     This method is used when copy an image to the panel in ImageManager2<br>
	 */
	public static BufferedImage paintOnImage(BufferedImage srcImage, 
            						int srcOffsetX, int srcOffsetY,/* source offset*/
            						int width, int height,         /* the part will be copied from source*/
            						int destOffsetX, int destOffsetY, /*dest offset*/
            						BufferedImage destImage,
            						Color initialColor,  boolean paintOnNewImage){
		if(srcImage==null){
			Log.debug("IU.getCopiedImage(): Input Parameter error.");
			return null;
		}
		if(initialColor==null){
			initialColor = Color.WHITE;
		}

		if(srcOffsetX<0) srcOffsetX=0;
		if(srcOffsetY<0) srcOffsetY=0;
		if((srcOffsetX+width)>srcImage.getWidth()) width = srcImage.getWidth()-srcOffsetX;
		if((srcOffsetY+height)>srcImage.getHeight()) height = srcImage.getHeight()-srcOffsetY;
		
		if(destImage == null){
//			destImage = new BufferedImage(width,height,srcImage.getType());
			destImage = getVoidBufferImage(width,height,srcImage.getType(),BufferedImage.TYPE_INT_RGB);
		}
		
		WritableRaster srcRaster = srcImage.getRaster();
		//Get the are to be copied to the dest image
		Object data = srcRaster.getDataElements(srcOffsetX, srcOffsetY, width, height, null);
		
		
		WritableRaster destRaster = destImage.getRaster();
		int destWidth = destImage.getWidth();
		int destHeight = destImage.getHeight();
		
		if(destOffsetX<0) destOffsetX=0;
		if(destOffsetY<0) destOffsetY=0;
		if(((destOffsetX+width)>destWidth ||(destOffsetY+height)>destHeight) /* destImage is not big enough*/
			 || paintOnNewImage /* should paint on new image */){
			int enlargedWidth = Math.max(destOffsetX+width, destImage.getWidth());
			int enlargedHeight = Math.max(destOffsetY+height, destImage.getHeight());
			//Need to enlarge the size of destImage, create a new BufferedImage to represent
//			BufferedImage enlargedImage = new BufferedImage(enlargedWidth,enlargedHeight,destImage.getType());
			BufferedImage enlargedImage = getVoidBufferImage(enlargedWidth,enlargedHeight,destImage.getType(),BufferedImage.TYPE_INT_RGB);
			WritableRaster enlargedRaster = enlargedImage.getRaster();
			ColorModel enlargedColorModel = enlargedImage.getColorModel();

			//Copy the dest image data to the enlarged raster
			Object destData = destRaster.getDataElements(0, 0, destWidth, destHeight, null);
			if(enlargedImage.getType()==destImage.getType()){
				enlargedRaster.setDataElements(0, 0,destWidth, destHeight, destData);
			}else{
				//If the types are different, we need to convert
//				int[] pixels = null;
//				int rgb = 0;
//				for (int i = 0; i < destWidth; i++) {
//					for (int j = 0; j < destHeight; j++) {
//						pixels = destRaster.getPixel(i,j, pixels);
//						rgb = destColorModel.getRGB(pixels);
//						enlargedRaster.setDataElements(i, j, enlargedColorModel.getDataElements(rgb, null));
//					}
//				}
				
				int rgb = 0;
				for (int i = 0; i < destWidth; i++) {
					for (int j = 0; j < destHeight; j++) {
						rgb = destImage.getRGB(i, j);
						enlargedImage.setRGB(i, j, rgb);
					}
				}
			}
			
			//For the other area in the enlarged raster, set to initialColor
			Object inData = enlargedColorModel.getDataElements(initialColor.getRGB(), null);
			for (int i = 0; i < enlargedWidth; i++) {
				for (int j = 0; j < enlargedHeight; j++) {
					//If (i,j) is NOT within the bounds of destImage, set the initColor to it
					if(!(i<destWidth && j<destHeight)){
						enlargedRaster.setDataElements(i, j, inData);						
					}
				}
			}
			
			//copy the source data to the enlarged image
			if(enlargedImage.getType()==srcImage.getType()){
				enlargedRaster.setDataElements(destOffsetX, destOffsetY, width, height, data);
			}else{
				int rgb = 0;
				for (int i = destOffsetX; i < width; i++) {
					for (int j = destOffsetY; j < height; j++) {
						rgb = srcImage.getRGB(i, j);
						enlargedImage.setRGB(i, j, rgb);
					}
				}
			}
			destImage = enlargedImage;
		}else{
			//The area to be copied can be drawn inside the dest image, so just copied it to
			//the dest image
			if(destImage.getType()==srcImage.getType()){
				destRaster.setDataElements(destOffsetX, destOffsetY, width, height, data);	
			}else{
				int rgb = 0;
				for (int i = destOffsetX; i < width; i++) {
					for (int j = destOffsetY; j < height; j++) {
						rgb = srcImage.getRGB(i, j);
						destImage.setRGB(i, j, rgb);
					}
				}
			}
		}
		
		return destImage;
	}

	public static BufferedImage blurImage(BufferedImage image){
		float[] elements = {
				1f/9f, 1f/9f, 1f/9f,
				1f/9f, 1f/9f, 1f/9f,
				1f/9f, 1f/9f, 1f/9f
		};
		Kernel kernel = new Kernel(3,3,elements);
		ConvolveOp op = new ConvolveOp(kernel);
		return filterImage(image,op);
	}
	
	public static BufferedImage sharpenImage(BufferedImage image){
		float[] elements = {
				0.0f, -1.0f, 0.0f,
				-1.0f, 5.0f, -1.0f,
				0.0f, -1.0f, 0.0f
		};
		Kernel kernel = new Kernel(3,3,elements);
		ConvolveOp op = new ConvolveOp(kernel);
		return filterImage(image,op);
	}
	
	public static BufferedImage edgeImage(BufferedImage image){
		float[] elements = {
				0.0f, -1.0f, 0.0f,
				-1.0f, 4.0f, -1.0f,
				0.0f, -1.0f, 0.0f
		};
		Kernel kernel = new Kernel(3,3,elements);
		ConvolveOp op = new ConvolveOp(kernel);
		return filterImage(image,op);
	}
	
	public static BufferedImage negativeImage(BufferedImage image){
		byte negative[] = new byte[256];
		for(int i=0;i<256;i++){
			negative[i] = (byte) (255-i);
		}
		
		ByteLookupTable lookTable = new ByteLookupTable(0,negative);
		LookupOp op = new LookupOp(lookTable, null);
		
		return filterImage(image,op);
	}

	/**
	 * Rotate the Image with 90, 180 or 270 degree.
	 * 
	 * @param bufferedimage
	 * @param angle	int, the angle the image will be rotated, in 360 degree
	 *                   only 90, 180 or 270 degree are supported.		
	 * @return
	 */
    public static BufferedImage rotateImage(BufferedImage bufferedimage, int angle){  
        
        int width = bufferedimage.getWidth();    
        int height = bufferedimage.getHeight();    
        
        AffineTransform affineTransform = new AffineTransform();
        
        if(angle==0){
        	return bufferedimage;
        }else if (angle == 90) {    
            affineTransform.translate(height, 0);    
        }else if (angle == 180) {    
            affineTransform.translate(width, height);    
        }else if (angle == 270) {    
            affineTransform.translate(0, width);    
        }else{
        	Log.warn("Rotation degree '"+angle+"' is not supported.");
        	return null;
        }

        affineTransform.rotate(java.lang.Math.toRadians(angle));    
        AffineTransformOp affineTransformOp = 
        	new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);    
        
        return affineTransformOp.filter(bufferedimage, null);    
    }
	
	/**
	 * <b>Purpose:</b>    Use provided BufferedImageOp to convert image
	 * @param image
	 * @param op
	 * @return
	 */
	public static BufferedImage filterImage(BufferedImage image, BufferedImageOp op){
		String debugmsg = ImageUtils.class.getName()+".filterImage(): ";
		BufferedImage filteredImage = null;
		
		if(image==null || op==null){
			Log.error(debugmsg+" image or BufferedImageOp should NOT be null");
			return null;
		}
		
		try{
			filteredImage = op.filter(image, null);
		}catch(Exception e){
			Log.warn(debugmsg+" Exception "+e.getMessage());
			try{
				filteredImage = ImageUtils.getVoidBufferImage(image.getWidth(), image.getHeight(), image.getType(), -1);
				op.filter(image, filteredImage);
			}catch(Exception e2){
				Log.warn(debugmsg+" Exception "+e2.getMessage());
			}
		}
		
		return filteredImage;
	}
	
	/**
	 * Cover an image by black color on a set of area.<br>
	 * @param image BufferedImage, the image to filter.
	 * @param filteredAreas String, a set of area delimited by space, such as "0,0,10,10 40;40;5%;5%"
	 * @param warnings List<String>, out, a List instance provided to contain the possible warnings during execution.
	 * @return
	 * @throws SAFSException passed along from {@link #convertAreas(Rectangle, String, List)}.
	 */
	public static BufferedImage filterImage(BufferedImage image, String filteredAreas, List<String> warnings) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		
		if(image==null){
			IndependantLog.error(debugmsg+" the image is null, cannot filter on it.");
			return image;
		}
		if(filteredAreas==null){
			IndependantLog.warn(debugmsg+" the filteredAreas is null, no need filter, return image directly.");
			return image;
		}
		
		Rectangle compRect = new Rectangle(0,0,image.getWidth(),image.getHeight());
		Rectangle[] rects = convertAreas(compRect, filteredAreas, warnings);
		
		for(int j = 0; j < rects.length; j++){
			if(rects[j] != null){
				image = paintOnImage(image, rects[j].x,rects[j].y,rects[j].width,rects[j].height, Color.BLACK, false);
			}
		}
		
		return image;
	}
	
	/**
	 * Convert a set of area into an array of Rectangle<br>
	 * @param basedRect Rectangle, the based rectangle. 
	 * @param areas String, a set of area delimited by space, such as "0,0,10,10 40;40;5%;5%"
	 * @param warnings List<String>, out, a List instance provided to contain the possible warnings during execution.
	 * @return
	 * @throws SAFSException if the basedRect is null or if any of the provided areas are completely outside the bounds of basedRect.
	 */
	public static Rectangle[] convertAreas(Rectangle basedRect, String areas, List<String> warnings) throws SAFSException{

		String debugmsg = StringUtils.debugmsg(false);
		String warnMsg = null;
		
		// abort with error if target basedRect is null
		if(basedRect == null){
			warnMsg = FAILStrings.convert(FAILStrings.SUBAREA_NOT_FOUND_IN__2, 
					"Subarea ("+areas+") not found in area (null).",
					areas, "null");
			IndependantLog.warn(debugmsg+warnMsg);
			if(warnings!=null) warnings.add(warnMsg);
			throw new SAFSException(warnMsg); 
		}
		
		String recarea = basedRect.x+","+basedRect.y+","+basedRect.width+","+basedRect.height;		
		String subAreas = normalizeSubAreas(areas);

		//isolate separate sets of coordinates
		//"x1,y1,x2,y2 x3,y3,x4,y4" separates into two sets: "x1,y1,x2,y2" and "x3,y3,x4,y4"
		String [] arrayOfAreas = subAreas.split(" ");

		Rectangle[] rects = new Rectangle[arrayOfAreas.length];
		int errcount = 0;
		for(int j = 0; j < arrayOfAreas.length; j++){
			//This check if a rectangle is valid, and clips it if it doesn't fit within the image.
			//Returns null if the rectangle is invalid (and sends a warning).
			rects[j] = getSubAreaRectangle(basedRect,arrayOfAreas[j]);
			if(rects[j]==null){
				errcount++;
				warnMsg = FAILStrings.convert(FAILStrings.SUBAREA_NOT_FOUND_IN__2, 
						"Subarea ("+arrayOfAreas[j]+") not found in area ("+ recarea +").",
						arrayOfAreas[j], recarea);
				IndependantLog.warn(debugmsg+warnMsg);
				if(warnings!=null) warnings.add(warnMsg);
			}
		}
		
		if(errcount==rects.length) {
			warnMsg = FAILStrings.convert(FAILStrings.SUBAREA_NOT_FOUND_IN__2, 
					"Subarea ("+areas+") not found in area ("+ recarea +").",
					areas, recarea);
			throw new SAFSException(warnMsg); 
		}

		return rects;
	}
	
	/**
	 * Normalize a subareas string.<br>
	 * Replace all ";" by ",". Example "40;40;5%;5%" -> "40,40,5%,5%"<br>
	 * For each area, if there are some space " " in it, remove them. Example "0, 0, 10,  10" -> "0,0,10,10"<br>
	 * Between areas, if there are multiple space " ", repalce them by one space.
	 * @param areas String, a set of area delimited by space, such as "0, 0,10 ,10     40;40;5%; 5%"
	 * @return String, a normalized subareas string
	 */
	public static String normalizeSubAreas(String areas){
		StringBuffer filterAreas = new StringBuffer();
		
		//Replace all ";" by ","
		String subAreas = StringUtilities.findAndReplace(areas, StringUtils.SEMI_COLON, StringUtils.COMMA);
		
		//If there are some space in the area like "x1, y1, x2, y2  x3, y3, x4, y4 "
		String[] tokensWithPossibleSpace = subAreas.split(StringUtils.COMMA);
		for(String token:tokensWithPossibleSpace){
			filterAreas.append(token.trim()+StringUtils.COMMA);
		}
		
		//Remove the last comma
		filterAreas.deleteCharAt(filterAreas.lastIndexOf(StringUtils.COMMA));
		
		//Remove multiple sapace by one space
		return filterAreas.toString().replaceAll(" +", " ");
	}
	
	/**
	 * Store our BufferedImage into a File;
	 * If the format is JPG, the third parameter indicate the compression quality.
	 * @param image - BufferedImage for ImageIO to write to file
	 * @param file	- valid full absolute File to write to
	 * @param quality	- If the file format is JPG, it indicates the compression quality.
	 * 					  It's value should be between 0.0 and 1.0;
	 * @throws 
	 */
	public static void saveImageToFile(BufferedImage image, File file, float quality) throws SAFSException{
		String debugmsg = ImageUtils.class.getName()+".saveImageToFile() ";
		
		ImageWriter writer = null;
		ImageOutputStream ios = null;
		
        try {
        	if (file.getName().toLowerCase().endsWith(".jpg") ||
        		file.getName().toLowerCase().endsWith(".jpeg")) {
            	Log.debug("IU saveImage attempting to open and write JPG image.");        		
                Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
                
                if (iter.hasNext()) {
                    writer = iter.next();
                }else{
                	Log.debug(debugmsg+" Can not create ImageWriter for format jpg.");
                	throw new SAFSException(" Can not create ImageWriter for format jpg.");
                }

                // Prepare output file
                ios = ImageIO.createImageOutputStream(file);
                writer.setOutput(ios);

                // Set the compression quality
                ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
                iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
                iwparam.setCompressionQuality(quality);
        
                // Write the image
                writer.write(null, new IIOImage(convertToRGBImage(image), null, null), iwparam);

        	}else{
        		saveImageToFile(image,file);
        	}

        }catch(IOException e){
        	throw new SAFSException("IOException: Can not write to file '"+file.getName()+"'.");
        }finally{
        	try {
				if (ios != null) {
					ios.flush();
					ios.close();
				}
			} catch (IOException e1) {
				Log.debug(debugmsg+"Can not close output stream for file "+file.getName());
			}
            if (writer != null) writer.dispose();
        }
	}
	
	static class ModifiedRectInfo {
		Rectangle resultRect = null;
		Rectangle imageWRect = null;
		Rectangle imageHRect = null;
		String imageW = null;
		String imageH = null;		
		boolean hasImageW(){ return !(imageWRect==null);}
		boolean hasImageH(){ return !(imageHRect==null);}
		boolean rectModified(){ return (hasImageW() | hasImageH());}
		boolean rectModifyFailure(){
			try{ if (imageW.length()>0 && imageWRect==null) return true;}
			catch(NullPointerException np){}
			try{ if (imageH.length()>0 && imageHRect==null) return true;}
			catch(NullPointerException np){}
			return false;
		}
		Rectangle getResult(){ return resultRect;}
		Rectangle getImageW(){ return imageWRect;}
		Rectangle getImageH(){ return imageHRect;}
	}
	
	public static void main(String[] args){

		testFindImageOnScreen(args);
		
		testCompareImages(args);

	}

	private static void testCompareImages(String...args){
		try {
			if(args.length<2){
				System.out.println("Usage: java ImageUtils sourceImgPath targetImgPath");
				return;
			}
			String img1 = args[0];
			String img2 = args[1];
			
			String a = StringUtils.readBinaryFile(img1).toString();
			String b = StringUtils.readBinaryFile(img2).toString();
			System.out.println("two images equal="+a.equals(b));
			
			BufferedImage bf1 = ImageUtils.getStoredImage(img1);
			BufferedImage bf2 = ImageUtils.getStoredImage(img2);
//			BufferedImage bf1 = ImageIO.read(new CaseInsensitiveFile(img1).toFile());
//			BufferedImage bf2 = ImageIO.read(new CaseInsensitiveFile(img2).toFile());
			
//			boolean equal = ImageUtils.compareImage(bf1, bf2, 80);
			boolean equal = ImageUtils.compareImage(bf2, bf1, 80);
			
			System.out.println("two images equal="+equal);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void testFindImageOnScreen(String...args){
		BufferedImage image = null;
		Date beginTime;
		Rectangle rec;
		String fileOrDir = null;
		long averageTime =0;
		
		try {
			//java ImageUtils -f fileORdirectory [-m 1|0] [-bt 1|0] [-btp 1-100] 
			if(args.length<2){
				System.out.println("Usage: java ImageUtils -f fileORdirectory [-m 1|0] [-bt 1|0] [-btp 1-100]");
				return;
			}
			for(int i=0;i<args.length;i++){
				if(args[i].equalsIgnoreCase("-f")){
					if(++i<args.length){
						fileOrDir = args[i];
					}else{
						System.out.println("You should specify fileORdirctory after -f.");
						System.out.println("Usage: java ImageUtils -f fileORdirectory [-m 1|0] [-bt 1|0] [-btp 1-100]");
						return;
					}
				}else if(args[i].equalsIgnoreCase("-m")){
					if(++i<args.length){
						USE_MULTIPLE_THREADS = args[i].trim().equals("1");
					}else{
						System.out.println("You should specify 1 or 0 after -m.");
						System.out.println("Usage: java ImageUtils -f fileORdirectory [-m 1|0] [-bt 1|0] [-btp 1-100]");
						USE_MULTIPLE_THREADS = false;
					}
				}else if(args[i].equalsIgnoreCase("-bt")){
					if(++i<args.length){
						useBitsTolerance = args[i].trim().equals("1");
					}else{
						System.out.println("You should specify 1 or 0 after -bt.");
						System.out.println("Usage: java ImageUtils -f fileORdirectory [-m 1|0] [-bt 1|0] [-btp 1-100]");
						useBitsTolerance = false;
					}
				}else if(args[i].equalsIgnoreCase("-btp")){
					if(++i<args.length){
						percentBitsTolerance = Integer.parseInt(args[i]);
					}else{
						System.out.println("You should specify a number between 1 and 100 after -btp.");
						System.out.println("Usage: java ImageUtils -f fileORdirectory [-m 1|0] [-bt 1|0] [-btp 1-100]");
						percentBitsTolerance = 100;
					}
				}
			}
			System.out.println("Use multithread "+USE_MULTIPLE_THREADS+"; use BT "+useBitsTolerance+"; BTPerct "+percentBitsTolerance);
			File pictures = new File(fileOrDir);
			int count = 0;
			if(pictures.isDirectory()){
				File pictureArray[] = pictures.listFiles();
				for(int i=0;i<pictureArray.length;i++){
					File picture = pictureArray[i];
					if(picture.isFile()){
						System.out.println("Looking for image "+picture.getAbsolutePath());
						try {
							image = getStoredImage(picture.getAbsolutePath());
						} catch (FileNotFoundException e) {
							continue;
						} catch (IOException e) {
							continue;
						}
					}else{
						continue;
					}
					beginTime = new Date();
					try {
						rec = findBufferedImageOnScreen(image, null, 1);
					} catch (ImagingOpException e) {
						continue;
					} catch (AWTException e) {
						continue;
					}
					if(rec!=null){
						count++;
						long usedTime = new Date().getTime()-beginTime.getTime();
						System.out.println("Found Image "+picture.getName()+" at "+rec+". Time used: "+usedTime);
						averageTime += usedTime;
					}
				}
				System.out.println("Found "+count+" images. Total time used: "+averageTime);
				if(count>0){
					averageTime = averageTime/count;
				}
			}else{
				try {
					System.out.println("Looking for image "+pictures.getAbsolutePath());
					image = getStoredImage(pictures.getAbsolutePath());
					beginTime = new Date();
					rec = findBufferedImageOnScreen(image, null, 1);
					System.out.println("Found Image at "+rec);
					averageTime = new Date().getTime()-beginTime.getTime();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Total average Using time "+averageTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * With the assumption that the two provided images are NOT matching, we will 
	 * create a Diff image composite from the two images.
	 * @param buffimg
	 * @param benchimg
	 * @return BufferedImage highlighting te differences between the two images.
	 * @throws IllegalArgumentException if either of the images provided is null.
	 */
	public static BufferedImage createDiffImage(BufferedImage buffimg, BufferedImage benchimg) throws IllegalArgumentException{
	    BufferedImage diffimg = null;

        int width1 = benchimg.getWidth(null);
        int width2 = buffimg.getWidth(null);
        int height1 = benchimg.getHeight(null);
        int height2 = buffimg.getHeight(null);

        int maxwidth  = width1  > width2  ? width1  : width2;
        int maxheight = height1 > height2 ? height1 : height2;

        diffimg = getVoidBufferImage(maxwidth, maxheight, BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB);
        
        int diffr,diffg,diffb;
        int rgb1=0, rgb2=0, r1,g1,b1,r2,g2,b2;
        for (int x = 0; x < maxwidth; x++) {
            for (int y = 0; y < maxheight; y++) {
                try{ 
                	rgb1 = benchimg.getRGB(x, y);
                    rgb2 = buffimg.getRGB(x, y);
                }catch(ArrayIndexOutOfBoundsException ai){
                	rgb1=0x000000;
                	rgb2=0xFFFFFF;
                }
                r1 = (rgb1 >> 16) & 0xff;
                g1 = (rgb1 >>  8) & 0xff;
                b1 = (rgb1      ) & 0xff;
                r2 = (rgb2 >> 16) & 0xff;
                g2 = (rgb2 >>  8) & 0xff;
                b2 = (rgb2      ) & 0xff;
                diffr = Math.abs(r1 - r2);
                diffg = Math.abs(g1 - g2);
                diffb = Math.abs(b1 - b2);
                // if all matched
                if((diffr + diffg + diffb) == 0){
                	diffimg.setRGB(x, y, ((r1*0x010000)+ (g1*0x0100)+ b1));
                }else{
                	diffimg.setRGB(x, y, 0xFF0000);
                }
            }
        }
		return diffimg;
	}
}
