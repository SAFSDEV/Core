/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.text;

import java.io.*;
import org.safs.text.*;

/**
 * 
 * @author Carl Nagle
 * @since Feb 23, 2005
 */
public class INIFileReadWriteTest {
	
	static File temp;

    public static final String TEMPFILE = "INIFileReadWriteTest.ini";
	static BufferedWriter buffer;
	static INIFileReader reader;
	
	public static void write(String line) throws IOException{
		System.out.println(line);
		buffer.write(line);
		buffer.newLine();
	}
	public static void createTestFile() throws IOException{
		temp = File.createTempFile(TEMPFILE,"");
		System.out.println("temp file:"+ temp.getAbsolutePath());
		buffer = new BufferedWriter(new FileWriter(temp));
		write("NoSection1=NoValue1");
		write("NoSection2=NoValue2");
		write("");
		write("[ApplicationConstants]");
		write("Const1=Constant1");
		write("Const2=Constant2");
		write("");
		write("[SectionA]");
		write("Value1=AValue1");
		write("Value2=AValue2");
		write("");
		buffer.flush();
		buffer.close();
	}

	static void test (String section, String item, String bench){
		String value = reader.getAppMapItem(section, item);
		String result = (bench.equals(value))?"PASSED:":"FAILED:";
		System.out.println(result+section+"."+item+" "+value+"=="+bench);
	}
	
	public static void main(String[] args) {
		try{ 
			createTestFile();
			reader = new INIFileReader(temp,0);
			test("", "NoSection2", "NoValue2");
		}
		catch(IOException x){x.printStackTrace();}
	}
}
