/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.w3c.tools.codec;
/**
 * History:
 * AUG 12, 2015		(Lei Wang)	Modify method main(): accept one parameter to output the "encoded string" to console.
 */
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Standalone Java program capable of creating a Base64Encoded output file (main args[1]) suitable 
 * for HTTP/HTTPS data transfer.
 * @author Carl Nagle
 */
public class BinaryFileEncoder extends Object {

	/**
	 * Take a specified binary file and create a Base64Encoded String suitable 
	 * for HTTP/HTTPS data transfer.
	 * @param args File to binary file input to encode. can be relative to working directory.
	 * @return String Base64 encoded data.
	 * @throws NoSuchFileException
	 * @throws IOException
	 */
    public static String encode(File binaryFile)throws NoSuchFileException, IOException{
    	
		String imgString = null;		
		BufferedInputStream inputstream = null;
		ByteArrayOutputStream outputstream = null;
		try {
			inputstream = new BufferedInputStream(new FileInputStream(binaryFile));
			outputstream = new ByteArrayOutputStream();
			int byt = 0;
			while(inputstream.available() > 0 && byt != -1){
				byt = inputstream.read();
				if(byt != -1) outputstream.write(byt);
			}
			outputstream.flush();
			imgString = Base64Encoder.encodeBase64Bytes(outputstream.toByteArray());
			
			if(imgString.length() <= 0)throw new NoSuchFileException(
					binaryFile.getName()+" binary file appears to be empty!"
			  );
			
		}finally{
			if(inputstream!=null) try{ inputstream.close();}catch (Exception e) {}
			if(outputstream!=null) try{ outputstream.close();}catch (Exception e) {}
		}
		
		return imgString;
    }
    
	/**
	 * Take a specified binary file (args[0]) and convert its content to a "Base64Encoded string"
	 * suitable for HTTP/HTTPS data transfer.<br>
	 * If the second parameter (args[1]) output file is provided, the "Base64Encoded string" will
	 * be stored in this file; Otherwise, the "Base64Encoded string" will be output to system.out.<br>
	 *
	 * @param args
	 * <br>[0] - filepath to binary file input to encode. can be relative to working directory.
	 * <br>[1] - filepath to base64 output file. can be relative to working directory.
	 * @return exitcode:
	 * <br>&nbsp;0 - success.
	 * <br>-1 - invalid or missing file paths
	 * <br>-2 - base64 output file exists and could not be deleted.
	 * <br>-3 - specified binary file was not found, or is not a file.
	 * <br>-4 - error in converting the binary file or writing the base64 file.
	 */
	public static void main(String[] args) {
		if ( args.length<1  || args[0].length()==0) {
			System.err.println ("Need at the lease one parameter, or the first parameter is empty!");
			System.err.println ("Usage:");
			System.err.println ("BinaryFileEncoder <binaryFileInput>") ;
			System.err.println ("BinaryFileEncoder <binaryFileInput> <base64FileOutput>") ;
			System.exit (-1) ;
		}
		if ( args.length==2 && args[1].length()==0) {
			System.err.println ("The second parameter is empty!");
			System.err.println ("Usage:");
		    System.err.println ("BinaryFileEncoder <binaryFileInput> <base64FileOutput>") ;
		    System.exit (-1) ;
		}
		Path binaryPath = FileSystems.getDefault().getPath(args[0]).toAbsolutePath();
		File binaryFile = binaryPath.toFile();
		Path base64File = null;
		
		if(args.length>1){
			base64File = FileSystems.getDefault().getPath(args[1]).toAbsolutePath();
			try{ if(base64File.toFile().isFile()) Files.delete(base64File);}
			catch(IOException x){
				System.err.println ("BinaryFileEncoder pre-existing Base64 file cannot be deleted. "+ x.getMessage()) ;
				System.exit (-2) ;
			}
		}
		
		if(!binaryFile.isFile()){
		    System.err.println ("Invalid file specification for binary file input: "+ binaryFile.getAbsolutePath()) ;
		    System.exit (-3) ;
		}
		
		try{
			String encodeedString = BinaryFileEncoder.encode(binaryFile);
			if(base64File==null){
				//If we didn't provide a file, write the encoded string directly to console
				System.out.print(encodeedString);
			}else{
				Files.write(base64File, encodeedString.getBytes(), StandardOpenOption.CREATE_NEW);		
			}
			System.exit(0);
		}catch(Exception x){
		    System.err.println (x.getClass().getSimpleName()+": "+x.getMessage()) ;
		    System.exit (-4) ;
		}
	}
}
