package org.safs.android.auto.lib;

/*
 * Code got from http://www.java2s.com/Open-Source/Android/android-core/platform-sdk/com/android/monkeyrunner/adb/image/ThirtyTwoBitColorModel.java.htm
 * 
 */

import com.android.ddmlib.RawImage;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;

/**
 * Internal color model used to do conversion of 32bpp RawImages.
 */
public class ThirtyTwoBitColorModel extends ColorModel {
    private static final int[] BITS = {
        8, 8, 8, 8,
    };
    private final int alphaLength;
    private final int alphaMask;
    private final int alphaOffset;
    private final int blueMask;
    private final int blueLength;
    private final int blueOffset;
    private final int greenMask;
    private final int greenLength;
    private final int greenOffset;
    private final int redMask;
    private final int redLength;
    private final int redOffset;

    public ThirtyTwoBitColorModel(RawImage rawImage) {
        super(32, BITS, ColorSpace.getInstance(ColorSpace.CS_sRGB),
                true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

        redOffset = rawImage.red_offset;
        redLength = rawImage.red_length;
        redMask = ImageUtils.getMask(redLength);
        greenOffset = rawImage.green_offset;
        greenLength = rawImage.green_length;
        greenMask = ImageUtils.getMask(greenLength);
        blueOffset = rawImage.blue_offset;
        blueLength = rawImage.blue_length;
        blueMask = ImageUtils.getMask(blueLength);
        alphaLength = rawImage.alpha_length;
        alphaOffset = rawImage.alpha_offset;
        alphaMask = ImageUtils.getMask(alphaLength);
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return true;
    }

    private int getPixel(Object inData) {
        byte[] data = (byte[]) inData;
        int value = data[0] & 0x00FF;
        value |= (data[1] & 0x00FF) << 8;
        value |= (data[2] & 0x00FF) << 16;
        value |= (data[3] & 0x00FF) << 24;

        return value;
    }

    @Override
    public int getAlpha(Object inData) {
        int pixel = getPixel(inData);
        if(alphaLength == 0) {
            return 0xff;
        }
        return ((pixel >>> alphaOffset) & alphaMask) << (8 - alphaLength);
    }

    @Override
    public int getBlue(Object inData) {
        int pixel = getPixel(inData);
        return ((pixel >>> blueOffset) & blueMask) << (8 - blueLength);
    }

    @Override
    public int getGreen(Object inData) {
        int pixel = getPixel(inData);
        return ((pixel >>> greenOffset) & greenMask) << (8 - greenLength);
    }

    @Override
    public int getRed(Object inData) {
        int pixel = getPixel(inData);
        return ((pixel >>> redOffset) & redMask) << (8 - redLength);
    }

    @Override
    public int getAlpha(int pixel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBlue(int pixel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getGreen(int pixel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRed(int pixel) {
        throw new UnsupportedOperationException();
    }
}
