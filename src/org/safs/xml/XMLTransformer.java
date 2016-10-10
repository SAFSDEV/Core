/** Copyright (C) 2016, SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * Developer Logs:
 * OCT 09, 2016 (SBJLWA) Use SAXON as the xslt 2.0 processor. 
 */
package org.safs.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * XML Transformer class using javax.xml.transform package assets.<br>
 * Currently, this class supports 2 kinds transformer: 
 * <ol>
 * <li>the default platform-specific one provided by Java
 * <li><a href="http://saxon.sourceforge.net/">saxon processor</a>
 * </ol>
 * 
 * @author canagl
 * @see #transform(File, File, File)
 * @see #transform(File, File, File, String)
 * @see #main(String[])
 */
public class XMLTransformer {

	/**
	 * Transforms an <code>xmlfile</code>, using an <code>xslfile</code> to an 
	 * <code>outfile</code> using whatever javax.xml.transform TransformerFactory is setup.
	 * Currently, this is the default platform-specific one. 
	 *  
	 * @param xmlfile String, the File with full path to an XML source file 
	 * @param xslfile String, the File with full path to an XSL source file 
	 * @param outfile String, the File with full path for the output file
	 * <p> 
	 * @throws FileNotFoundException if the XML or XSL source files cannot be found. 
	 * @throws TransformerException if the transform could not be completed successfully for various reasons.
	 * <p>
	 * @see javax.xml.transform.TransformerFactory#newInstance()
	 * @see javax.xml.transform.TransformerFactoryConfigurationError
	 * @see javax.xml.transform.TransformerConfigurationException
     */ 
	public static void transform(File xmlfile, File xslfile, File outfile) throws FileNotFoundException, TransformerException{ 
		transform(xmlfile, xslfile, outfile, XSLT_VERSION_1);
	}
	
	/**
	 * Transforms an <code>xmlfile</code>, using an <code>xslfile</code> to an 
	 * <code>outfile</code> using whatever javax.xml.transform TransformerFactory is setup, 
	 * the last parameter version decides what TransformerFactory will be used.
	 *  
	 * @param xmlfile String, the File with full path to an XML source file 
	 * @param xslfile String, the File with full path to an XSL source file 
	 * @param outfile String, the File with full path for the output file
	 * @param version String, 1.0 or 2.0. Decides what TransformerFactory will be used
	 * <ol>
	 * <li>1.0: javax.xml.transform.TransformerFactory default one
	 * <li>2.0: net.sf.saxon.TransformerFactoryImpl
	 * </ol>
	 * <p> 
	 * @throws FileNotFoundException if the XML or XSL source files cannot be found. 
	 * @throws TransformerException if the transform could not be completed successfully for various reasons.
	 * <p>
	 * @see javax.xml.transform.TransformerFactory#newInstance()
	 * @see javax.xml.transform.TransformerFactoryConfigurationError
	 * @see javax.xml.transform.TransformerConfigurationException
     */ 
	public static void transform(File xmlfile, File xslfile, File outfile, String version) throws FileNotFoundException, TransformerException{ 
		// Create transformer factory 
		TransformerFactory factory = null; 
		
		if(XSLT_VERSION_1.equals(version)){
			factory = TransformerFactory.newInstance();
		}else if(XSLT_VERSION_2.equals(version)){
			factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);			
		}else{
			System.err.println("Unsupported version '"+version+"', use processor 1.0.");
			factory = TransformerFactory.newInstance();
		}
		
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
	
	public static final String XSLT_VERSION_1 		= "1.0";
	public static final String XSLT_VERSION_2 		= "2.0";
	public static final String DEFAULT_XSLT_VERSION = XSLT_VERSION_1;
	private static final String ARG_VERSION 		= "-version";
	private static final String ARG_VERSION_BRIEF 	= "-v";
	
	/**
	 * Primarily for testing.
	 * @param args<br>
	 * <b>args[0]</b> the full path to an XML source file<br>
	 * <b>args[1]</b> the full path to an XSL source file<br>
	 * <b>args[2]</b> the full path to the resulting output file<br>
	 * <b>-version 1.0|2.0</b>, optional, the processor's version.
	 * <ol>
	 * <li>1.0: javax.xml.transform.TransformerFactory default one
	 * <li>2.0: net.sf.saxon.TransformerFactoryImpl
	 * </ol>
	 * <P>
	 * @see #convert(File, File, File)
	 */
	public static void main(String[] args){
		if (args.length >= 3) {
			File infile = new File(args[0]); 
			File xslfile = new File(args[1]); 
			File outfile = new File(args[2]);
			String version = DEFAULT_XSLT_VERSION;
			
			if(args.length>3){
				for(int i=3;i<args.length;i++){
					if(ARG_VERSION.equalsIgnoreCase(args[i]) || ARG_VERSION_BRIEF.equalsIgnoreCase(args[i])){
						if((i+1)<args.length){
							version = args[++i];
							if(!(XSLT_VERSION_1.equals(version) || XSLT_VERSION_2.equals(version))){
								System.err.println("Warning: bad version parameter '"+version+"'!\n"+getUsage());
								version = DEFAULT_XSLT_VERSION;
							}
						}else{
							System.err.println("Warning: missing version parameter!\n"+getUsage());
						}
					}
				}
			}
			
			try {
				transform(infile, xslfile,outfile, version);
				System.out.println("The transformed file locates at '"+outfile+"'");
			} catch (FileNotFoundException | TransformerException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Wrong number of parameters.\n"+getUsage());
		} 
	}
	
	private static String getUsage(){
		return "Usage: java org.safs.xml.XMLTransformer xmlpath xslpath outpath [-version "+XSLT_VERSION_1+"|"+XSLT_VERSION_2+"]";
	}
}
