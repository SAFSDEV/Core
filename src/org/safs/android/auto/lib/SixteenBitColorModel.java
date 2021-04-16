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
package org.safs.android.auto.lib;

/*
 * Code got from http://www.java2s.com/Open-Source/Android/android-core/platform-sdk/com/android/monkeyrunner/adb/image/SixteenBitColorModel.java.htm
 * 
 */

import com.android.ddmlib.RawImage;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;

/**
 * Internal color model used to do conversion of 16bpp RawImages.
 */
public class SixteenBitColorModel extends ColorModel {
    private static final int[] BITS = {
        8, 8, 8, 8
    };
    public SixteenBitColorModel(RawImage rawImage) {
        super(32
                , BITS, ColorSpace.getInstance(ColorSpace.CS_sRGB),
                true, false, Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return true;
    }

    private int getPixel(Object inData) {
        byte[] data = (byte[]) inData;
        int value = data[0] & 0x00FF;
        value |= (data[1] << 8) & 0x0FF00;

        return value;
    }

    @Override
    public int getAlpha(Object inData) {
        return 0xff;
    }

    @Override
    public int getBlue(Object inData) {
        int pixel = getPixel(inData);
        return ((pixel >> 0) & 0x01F) << 3;
    }

    @Override
    public int getGreen(Object inData) {
        int pixel = getPixel(inData);
        return ((pixel >> 5) & 0x03F) << 2;
    }

    @Override
    public int getRed(Object inData) {
        int pixel = getPixel(inData);
        return ((pixel >> 11) & 0x01F) << 3;
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
