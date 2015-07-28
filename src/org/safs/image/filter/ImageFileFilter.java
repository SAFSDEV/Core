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
