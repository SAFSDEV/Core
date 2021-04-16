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
package org.safs.text;

/*******************************************************************************************
 * History:
 *
 * <br>	FEB 04, 2015	(Lei Wang)    Modify detectFileEncoding(): close FileInputStream, catch Exception inside method.
 *
 *********************************************************************************************/

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.mozilla.universalchardet.UniversalDetector;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.tools.CaseInsensitiveFile;

/**
 * <p>
 * This class will use some third-party jars to provide some functionalities.
 * For example, method {@link #detectFileEncoding(String)} will depend on
 * <a href="http://code.google.com/p/juniversalchardet/">juniversalchardet</a>
 * <p>
 * This class is created for making class {@link org.safs.text.FileUtilities} independent
 * of these third-party jars. In {@link org.safs.text.FileUtilities}, we use java-reflection
 * to call methods in this class.
 */

public class FileUtilitiesByThirdParty {
	public static final int MAX_BTYES_TO_READ = 4096*4;//16k bytes to read, enough?
	/**
	 * Opens a FileInputStream and detects its encoding. If no encoding is detected, the default system
	 * encoding will be returned.<br/>
	 * Refer to
	 * <ul>
	 * <li><a href="http://code.google.com/p/juniversalchardet/">juniversalchardet</a>
	 * <li><a href="http://www-archive.mozilla.org/projects/intl/UniversalCharsetDetection.html>UniversalCharsetDetection</a>
	 * </ul>
	 * @param filename String, case-insensitive absolute filename path.
	 * @return String, the file encoding, it might be null.
	 * @see FileUtilities#detectFileEncoding(String)
	 */
	public static String detectFileEncoding(String filename){
		byte[] buf = new byte[4096];
		int nread = 0;
		int totalReadBytes = 0;
		String encoding = null;
		FileInputStream fis = null;

		try{
			fis = new FileInputStream(new CaseInsensitiveFile(filename).toFile());
			UniversalDetector detector = new UniversalDetector(null);

			//Feed detector with bytes until it detect the encoding or reach the max bytes to read
			//if the detector can't get the encoding after reading some bytes, we should stop to save time.
			while ((nread = fis.read(buf))>0 && !detector.isDone() && totalReadBytes<MAX_BTYES_TO_READ) {
				totalReadBytes += nread;
				detector.handleData(buf, 0, nread);
			}
			if(!detector.isDone()){
				//IndependantLog.warn("Didn't detect file encoding after reading "+totalReadBytes+" bytes.");
			}
			detector.dataEnd();

			//Get the file encoding string (defined in org.mozilla.universalchardet.Constants)
			encoding = detector.getDetectedCharset();
			detector.reset();

		}catch(Exception e){
			IndependantLog.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
		}finally{
			if(fis!=null) try{fis.close();}catch(Exception e){}
		}

		//If no encoding is detected, test if it is "utf-8"
		try{
			if(encoding==null || encoding.trim().isEmpty()) if(FileUtilities.isFileUTF8(filename)) encoding = "UTF-8";
		} catch (Exception e) {
			IndependantLog.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
		}

		//If no encoding is detected, get the default file encoding
		try{
			if(encoding==null || encoding.trim().isEmpty()) encoding = System.getProperty("file.encoding");
			if(!Charset.isSupported(encoding)){
				String error = "The detected encoding is '"+encoding+"', it is not supported by java.nio.charset.Charset";
				IndependantLog.warn(error);//We need to map org.mozilla.universalchardet.Constants and java.nio.charset.Charset ?
				throw new IOException(error);
			}
		} catch (Exception e) {
			IndependantLog.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
		}

		return encoding;
	}

	/**
	 * Read String as input to find encoding. If no encoding is detected, the default system
	 * encoding will be returned.
	 * <ul>
	 * <li><a href="http://code.google.com/p/juniversalchardet/">juniversalchardet</a>
	 * <li><a href="http://www-archive.mozilla.org/projects/intl/UniversalCharsetDetection.html>UniversalCharsetDetection</a>
	 * </ul>
	 * @param str String, the string according to which the encoding will be detected.
	 * @return String, the file encoding, it might be null.
	 * @see FileUtilities#detectFileEncoding(String)
	 */
	public static String detectStringEncoding(String str){

		String encoding = null;

		try{

			UniversalDetector detector = new UniversalDetector(null);

			detector.handleData(str.getBytes(), 0, str.getBytes().length);

			detector.dataEnd();

			//Get the file encoding string (defined in org.mozilla.universalchardet.Constants)
			encoding = detector.getDetectedCharset();
			detector.reset();

		}catch(Exception e){
			IndependantLog.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
		}

		//If no encoding is detected, get the default file encoding
		try{
			if(encoding==null || encoding.trim().isEmpty()) encoding = System.getProperty("file.encoding");
			if(!Charset.isSupported(encoding)){
				String error = "The detected encoding is '"+encoding+"', it is not supported by java.nio.charset.Charset";
				IndependantLog.warn(error);//We need to map org.mozilla.universalchardet.Constants and java.nio.charset.Charset ?
				throw new IOException(error);
			}
		} catch (Exception e) {
			IndependantLog.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
		}

		return encoding;
	}
}
