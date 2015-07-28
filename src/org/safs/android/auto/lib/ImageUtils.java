/*
 * Code got from http://www.java2s.com/Open-Source/Android/android-core/platform-sdk/com/android/monkeyrunner/adb/image/ImageUtils.java.htm
 * 
 */
package org.safs.android.auto.lib;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.android.ddmlib.RawImage;

public class ImageUtils{
    @SuppressWarnings("unchecked")
	private static Hashtable<?,?> EMPTY_HASH = new Hashtable();
    private static int[] BAND_OFFSETS_32 = { 0, 1, 2, 3 };
    private static int[] BAND_OFFSETS_16 = { 0, 1 };
    
    /**
     * Convert a raw image into a buffered image.
     *
     * @param rawImage the raw image to convert
     * @param image the old image to (possibly) recycle
     * @return the converted image
     */
    public static BufferedImage convertImage(RawImage rawImage, BufferedImage image) {
        switch (rawImage.bpp) {
            case 16:
                return rawImage16toARGB(image, rawImage);
            case 32:
                return rawImage32toARGB(rawImage);
        }
        return null;
    }
    
    /**
     * Convert a raw image into a buffered image.
     *
     * @param rawImage the image to convert.
     * @return the converted image.
     */
    public static BufferedImage convertImage(RawImage rawImage) {
        return convertImage(rawImage, null);
    }

    public static int getMask(int length) {
        int res = 0;
        for (int i = 0 ; i < length ; i++) {
            res = (res << 1) + 1;
        }

        return res;
    }
    
    private static BufferedImage rawImage32toARGB(RawImage rawImage) {
        // Do as much as we can to not make an extra copy of the data.  This is just a bunch of
        // classes that wrap's the raw byte array of the image data.
        DataBufferByte dataBuffer = new DataBufferByte(rawImage.data, rawImage.size);

        PixelInterleavedSampleModel sampleModel = 
        	new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, rawImage.width, rawImage.height,
                    4, rawImage.width * 4, BAND_OFFSETS_32);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));
        return new BufferedImage(new ThirtyTwoBitColorModel(rawImage), raster, false, EMPTY_HASH);
    }

    private static BufferedImage rawImage16toARGB(BufferedImage image, RawImage rawImage) {
        // Do as much as we can to not make an extra copy of the data.  This is just a bunch of
        // classes that wrap's the raw byte array of the image data.
        DataBufferByte dataBuffer = new DataBufferByte(rawImage.data, rawImage.size);

        PixelInterleavedSampleModel sampleModel =
            new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, rawImage.width, rawImage.height,
                    2, rawImage.width * 2, BAND_OFFSETS_16);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer,new Point(0, 0));
        return new BufferedImage(new SixteenBitColorModel(rawImage), raster, false, EMPTY_HASH);
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
        	debug("Rotation degree '"+angle+"' is not supported.");
        	return null;
        }

        affineTransform.rotate(java.lang.Math.toRadians(angle));    
        AffineTransformOp affineTransformOp = 
        	new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);    
        
        return affineTransformOp.filter(bufferedimage, null);    
    }
    
	/**
	 * Store our BufferedImage into a File;
	 * If the format is JPG, the third parameter indicate the compression quality.
	 * @param image - BufferedImage for ImageIO to write to file
	 * @param file	- valid full absolute File to write to
	 * @param quality	- If the file format is JPG, it indicates the compression quality.
	 * 					  It's value should be between 0.0 and 1.0;
	 * @throws AndroidRuntimeException
	 */
	public static void saveImageToFile(BufferedImage image, File file, float quality) throws AndroidRuntimeException{
		String debugmsg = ImageUtils.class.getName()+".saveImageToFile() ";
		
		ImageWriter writer = null;
		ImageOutputStream ios = null;
		
        try {
        	if (file.getName().toLowerCase().endsWith(".jpg")) {
            	debug("IU saveImage attempting to open and write JPG image.");        		
                Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
                
                if (iter.hasNext()) {
                    writer = iter.next();
                }else{
                	debug(debugmsg+" Can not create ImageWriter for format jpg.");
                	throw new AndroidRuntimeException(" Can not create ImageWriter for format jpg.");
                }

                // Prepare output file
                ios = ImageIO.createImageOutputStream(file);
                writer.setOutput(ios);

                // Set the compression quality
                ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
                iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
                iwparam.setCompressionQuality(quality);
        
                // Write the image
                writer.write(null, new IIOImage(image, null, null), iwparam);

        	}else{
        		saveImageToFile(image,file);
        	}

        }catch(IOException e){
        	throw new AndroidRuntimeException("IOException: Can not write to file."+file.getName());
        }finally{
        	try {
				if (ios != null) {
					ios.flush();
					ios.close();
				}
			} catch (IOException e1) {
				debug(debugmsg+"Can not close output stream for file "+file.getName());
			}
            if (writer != null) writer.dispose();
        }
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
    	//Use ImageIO to write image to a file, so that the same content will be used in the verifyGUIImageToFile()
		debug("IU saveImage attempting to save image to file: "+file.getAbsolutePath());
		String lowerCaseFileName = file.getName().toLowerCase();
    	if (lowerCaseFileName.endsWith(".jpg") ||
    		lowerCaseFileName.endsWith("jpeg")) {
    		ImageIO.write(image,"JPEG",file);
    	}else if (lowerCaseFileName.endsWith(".bmp")) {
    		ImageIO.write(image,"BMP",file);
    	}else if (lowerCaseFileName.endsWith(".tif") ||
    			  lowerCaseFileName.endsWith(".tiff")) {
    		ImageIO.write(image,"TIF",file);
    	}else if (lowerCaseFileName.endsWith(".gif")) {
    		ImageIO.write(image,"GIF",file);
    	}else if (lowerCaseFileName.endsWith(".png")) {
    		ImageIO.write(image,"PNG",file);
    	}else if (lowerCaseFileName.endsWith(".pnm")) {
    		ImageIO.write(image,"PNM",file);
    	}else{
    		debug("IU saveImage unsupported image format specification!");
    		throw new IllegalArgumentException("Only JPG, BMP, TIF, GIF, PNG and PNM files are currently supported.");
    	}
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
			debug("IU: getVoidBufferImage() "+e.getMessage());
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
			                                   int srcOffsetX, int srcOffsetY, /* source offset, from where to begin copy*/
			                                   int destOffsetX, int destOffsetY, /* dest offset, from where to begin paste*/
			                                   int destWidth, int destHeight, /* the new image's width and height*/
			                                   Color initialColor){
		if(srcImage==null || destWidth<=0 || destHeight<=0 ){
			debug("IU.getCopiedImage(): Input Parameter error.");
			return null;
		}
		if(initialColor==null){
			initialColor = Color.WHITE;
		}
		
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
		
		return destImage;
	}
	
	public static void debug(String message){
		System.out.println(message);
	}
}
