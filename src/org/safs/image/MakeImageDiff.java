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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.safs.tools.CaseInsensitiveFile;

/**
 * Sample Windows BAT file command-line usage:
 * <p>
 * MAKEDIFF.BAT File:<br>
 * ==================<br>
 * <p><pre>
 * SET CP=%SAFSDIR%\lib\safs.jar
 * SET EX=%SAFSDIR%\jre\bin\java.exe
 * 
 * %EX% -cp %CP% org.safs.image.MakeImageDiff -bench %1 -actual %2 -diff %3
 * </pre>
 * INVOCATION:<br>
 * ===========<br>
 * <p><pre>
 * makediff pathTo\BenchImage.ext pathTo\ActualImage.ext pathTo\DiffImage.ext
 * </pre>
 */
public class MakeImageDiff {

	public static final String TXT_HELP = "\n"+ 
			"Required Parameters:\n\n"+
			"  -bench  fullpath/To/BenchmarkImage.ext\n"+
			"  -actual fullpath/To/ActualImage.ext\n"+
			"  -diff   fullpath/To/DiffImage.ext (optional)\n"+
			"\n";
	
	public static final String ARG_BENCH = "-bench";
	public static final String ARG_ACTUAL = "-actual";
	public static final String ARG_DIFF = "-diff";
	
	
	File bench = null;
	File actual = null;
	File diff = null;
	String benchpath = null;
	String actualpath = null;
	String diffpath = null;
	BufferedImage diffimg = null;
	
	public MakeImageDiff() {}

	public MakeImageDiff(File bench, File actual, File diff){
		this.bench = bench;
		this.actual = actual;
		this.diff = diff;
	}
	
	/**
	 * -bench fullPath/To/BenchmarkImage.ext <br>
	 * -actual fullPath/To/ActualImage.ext<br>
	 * -diff fullPath/To/DiffImage.ext (optional)<br>
	 */
	public void processArgs(String[] args)throws IllegalArgumentException{
		String arg = null;
		try{
			for(int i=0;i<args.length;i++){
				arg = args[i];
				if(arg.equalsIgnoreCase(ARG_BENCH)){
					benchpath = args[++i];
					bench = new CaseInsensitiveFile(benchpath).toFile();
					if(! (bench.isAbsolute()&& bench.isFile()))
						throw new IllegalArgumentException(arg +" "+ benchpath);
				}else
				if(arg.equalsIgnoreCase(ARG_ACTUAL)){
					actualpath = args[++i];
					actual = new CaseInsensitiveFile(actualpath).toFile();
					if(! (actual.isAbsolute()&& actual.isFile()))
						throw new IllegalArgumentException(arg +" "+ actualpath);
				}else
				if(arg.equalsIgnoreCase(ARG_DIFF)){
					diffpath = args[++i];
					diff = new CaseInsensitiveFile(diffpath).toFile();
					if(! diff.isAbsolute())  // does not have to exist already
						throw new IllegalArgumentException(arg +" "+ diffpath);
				}
			}
		}
		catch(IllegalArgumentException x){
			throw x;
		}
		catch(Exception x){
			if(arg == null) arg = x.getClass().getName()+" "+ x.getMessage();
			throw new IllegalArgumentException(arg);
		}
	}

	/**
	 * Can only be called if processArgs or the Constructor has already provided the necessary 
	 * bench and actual Files to compare.
	 * @return BufferedImage of differences between the bench and actual Files.
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public BufferedImage createImageDiff() throws IllegalArgumentException, IOException{
		return createImageDiff(bench, actual, diff);
	}
	
	/**
	 * @param bench -- Existing image benchmark to compare.
	 * @param actual -- Existing image to compare with the benchmark.
	 * @param diff File to persist the diff image to the file system.  Can be null to be ignored.
	 * @return BufferedImage of differences between the two images.
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public BufferedImage createImageDiff(File bench, File actual, File diff) 
			                           throws IllegalArgumentException, IOException{
		// TODO add argument validation
		BufferedImage benchimg = ImageUtils.getStoredImage(bench.getCanonicalPath());
		BufferedImage actualimg = ImageUtils.getStoredImage(actual.getCanonicalPath());
		diffimg = ImageUtils.createDiffImage(actualimg, benchimg);
		if(diff != null) ImageUtils.saveImageToFile(diffimg, diff);
		return diffimg;
	}
	
	/**
	 * @return Will be null if no image diff has been created.
	 * @see #createImageDiff(File, File, File)
	 */
	public BufferedImage getImageDiff(){ return diffimg; }
	
	public static void main(String[] args) {
		try{
			MakeImageDiff differ = new MakeImageDiff();
			differ.processArgs(args);
			differ.createImageDiff();
		}catch(Exception x){
			System.out.println(x.getClass().getName()+", "+ x.getMessage());
			System.out.println(TXT_HELP);
			x.printStackTrace();
		}
	}
}
