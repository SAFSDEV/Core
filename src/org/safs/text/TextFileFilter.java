package org.safs.text;
import java.io.File;

import javax.swing.filechooser.FileFilter;

public class TextFileFilter extends FileFilter {

    
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = FileUtilities.getExtension(f);
        if (extension != null) {
            if (extension.equals("txt") ||
                extension.equals("dat")) {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Just txt and dat files";
    }
}
