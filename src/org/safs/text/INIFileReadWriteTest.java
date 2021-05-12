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
