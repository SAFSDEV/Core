/** Copyright (C) 2003, (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
