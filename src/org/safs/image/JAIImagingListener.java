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
