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
package org.safs.xml;

import java.io.*;
import java.util.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import org.safs.tools.CaseInsensitiveFile;

/** 
 **  This class is a wrapper for SUN's XMLEncode/XMLDecode classes which convert
 ** JavaBeans into XML and back again.  It requires jdk1.4
 */
public class XMLEncoderDecoder {

  /** <br><em>Purpose:</em> convert the passed object instance into an array of bytes
   ** which represent it as an xml 'object',
   * <br><em>Assumptions:</em>  jvm 1.4, that the object is serializable
   * @param                     bytes, byte[]
   * @return                    Serializable
   **/
  public static byte[] xmlEncode(Serializable obj) {
    int initialSize = 1024;
    ByteArrayOutputStream stm = new ByteArrayOutputStream(initialSize);
    XMLEncoder en = new XMLEncoder(new BufferedOutputStream(stm));
    en.writeObject(obj);
    en.close();
    return stm.toByteArray();
  }

  /** <br><em>Purpose:</em> convert the passed  bytes which represent an xml 'object',
   ** and convert them to an instance of an  'object' described by the xml.
   * <br><em>Assumptions:</em>  jvm 1.4
   * @param                     bytes, byte[]
   * @return                    Serializable
   **/
  public static Serializable xmlDecode(byte[] bytes) {
    XMLDecoder de = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(bytes)));
    Object result = de.readObject();
    de.close();
    System.out.println("Read object:"+result.getClass());
    return (Serializable) result;
  }

  /** <br><em>Purpose:</em> read the bytes from 'filename' (the xml file), and convert them
   ** to an instance of an  'object' described by the xml.
   * <br><em>Assumptions:</em>  jvm 1.4
   * @param                     filename, String
   * @return                    Serializable
   **/
  public static Serializable xmlDecode (String filename) throws IOException {
    Reader xmlReader = new BufferedReader(new FileReader(new CaseInsensitiveFile(filename).toFile()));
    if (xmlReader == null) return null;
    List list = new LinkedList();
    char[] cbuf = new char[256];
    int k=0;
    for(int i=0; (i=xmlReader.read(cbuf)) > 0; ) {
      char[] c = new char[i];
      for(int j=0; j<i; j++) c[j] = cbuf[j];
      list.add(c);
      k += i;
    }
    int l=0;
    byte[] bytes = new byte[k];
    for(Iterator i = list.iterator(); i.hasNext(); ) {
      char[] c = (char[]) i.next();
      for(int j=0; j<c.length; j++) {
        bytes[l++] = (byte) c[j];
      }
    }
    return xmlDecode(bytes);
  }
}
