/** Copyright (C) 2016, SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Simple XML Transformer class using javax.xml.transform package assets.
 * 
 * @author Carl Nagle
 * @see #transform(File, File, File)
 */
public class XMLTransformer {

	/**
	 * Transforms an <code>xmlfile</code>, using an <code>xslfile</code> to an 
	 * <code>outfile</code> using whatever javax.xml.transform TransformerFactory is setup for use.
	 * Currently, this is usually the default platform-specific one. 
	 *  
	 * @param xmlfile 
	 *            the File with full path to an XML source file 
	 * @param xslfile 
	 *            the File with full path to an XSL source file 
	 * @param outfile 
	 *            the File with full path for the output file
	 * <p> 
	 * @throws FileNotFoundException if the XML or XSL source files cannot be found. 
	 * @throws TransformerException if the transform could not be completed successfully for various reasons.
	 * <p>
	 * @see javax.xml.transform.TransformerFactory#newInstance()
	 * @see javax.xml.transform.TransformerFactoryConfigurationError
	 * @see javax.xml.transform.TransformerConfigurationException
     */ 
	public static void transform(File xmlfile, File xslfile, File outfile) throws FileNotFoundException, TransformerException{ 
		// Create transformer factory 
		TransformerFactory factory = TransformerFactory.newInstance(); 
		 
		// Use the factory to create a template containing the XSL file 
		Templates template = factory.newTemplates(new StreamSource( 
		                                          new FileInputStream(xslfile))); 
		 
		// Use the template to create a transformer 
		Transformer xformer = template.newTransformer(); 
		    
		// Prepare the input and output files 
		Source source = new StreamSource(new FileInputStream(xmlfile)); 
		Result result = new StreamResult(new FileOutputStream(outfile)); 
		 
		// Apply the xsl file to the source file and write the result to the 
		// output file 
		xformer.transform(source, result);
	}
	
	/**
	 * Primarily for testing.
	 * @param args <UL>
	 * args[0] the full path to an XML source file<br>
	 * args[1] the full path to an XSL source file<br>
	 * args[2] the full path to the resulting output file<br>
	 * </UL><P>
	 * @see #convert(File, File, File)
	 */
	public static void main(String[] args){
		if (args.length == 3) { 
		   File infile = new File(args[0]); 
		   File xslfile = new File(args[1]); 
		   File outfile = new File(args[2]);
		   try {
			transform(infile, xslfile,outfile);
		} catch (FileNotFoundException | TransformerException e) {
			e.printStackTrace();
		}
	    } else { 
		   System.err 
		     .println("Wrong number of parameters.\nUsage: xmlpath, xslpath, outpath"); 
		} 
	}
}
