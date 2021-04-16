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
package org.safs.image.filter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.safs.image.ImageUtils;
import org.safs.text.FileUtilities;

/* ImageFilter.java is a 1.4 example used by FileChooserDemo2.java. */
/**
 * Asset associated with {@link ImageFilterGUI}
 */
public class ImageFileFilter extends FileFilter {

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = FileUtilities.getExtension(f);
        if (extension != null) {
            if (extension.equals(ImageUtils.EXT_TIFF)||
                extension.equals(ImageUtils.EXT_TIF) ||
                extension.equals(ImageUtils.EXT_GIF) ||
                extension.equals(ImageUtils.EXT_JPEG)||
                extension.equals(ImageUtils.EXT_JPG) ||
                extension.equals(ImageUtils.EXT_PNG)) {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Just Images";
    }
}
