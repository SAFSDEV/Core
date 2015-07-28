/** 
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.image;

import javax.media.jai.util.ImagingListener;
import org.safs.Log;

/**
 * Necessary class to override default JAI ImagingListener logging which noisily prints 
 * seemingly unnecessary processing issues and stack traces to System.err.  Our implementation 
 * will mimic the default listener without printing to System.err.
 * @author Carl Nagle
 * @see javax.media.jai.JAI
 * @see javax.media.jai.util.ImagingListener
 */
public class JAIImagingListener implements ImagingListener {

	public boolean errorOccurred(String arg0, Throwable arg1, Object arg2,
								 boolean arg3) throws RuntimeException {
		if(arg1 instanceof RuntimeException) {
			Log.info("JAI Imaging RuntimeException: "+ arg1.getClass().getSimpleName()+" "+ arg1.getMessage());
			throw (RuntimeException) arg1;
		}else{
			Log.info("IGNORING JAI Imaging error: "+ arg1.getClass().getSimpleName()+" "+ arg1.getMessage());
		}
		return false;
	}

}
