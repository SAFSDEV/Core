package org.safs.tools.input;

import java.util.Vector;
import java.awt.Point;
import java.awt.Rectangle;
import org.safs.text.INIFileReader;
import org.safs.tools.CaseInsensitiveFile;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class MAPJPGProcessor {

	String objectName = "UNKNOWN";
	
	static String getHTMLHeader(String objectid){
	return 
	"<html>\n"+
	"<head><title>"+ objectid +" Map</title></head>\n"+
	"<body><a name=\"top\"/>\n"+
	"<h2>"+ objectid +" Map</h2>\n"+
	"<p>\n"+
	"Last Updated:\n"+
	"<script language=\"JavaScript\">document.write(document.lastModified)</script>\n"+
	"<p>\n"+
	"<small>\n"+
	"<b>(mouse over components for information)</b><br>\n"+
	"<img src=\""+ objectid +".jpg\" alt=\""+ objectid +"\" usemap=\"#map\"/>\n"+
	"<p>\n"+
	"<map name=\"map\">\n";
	}	

	// order components so that larger components are in back, smaller components are 
	// in front.
	void arrangeComponents(Vector components){
	
		// the PARENT must be last
		// children rects CONTAINing other children move back
		int last = components.size() -1;
		CompData data1 = null;
		CompData data2 = null;
		Rectangle rect1 = null;
		Rectangle rect2 = null;
		
		// start everything at index = 0
		int i1 = 0;
		int i2 = 0;
		int imatch = 0;
		do{
			System.out.println("data1 component number "+ i1);
			imatch = 0;
			data1 = (CompData) components.elementAt(i1);

			if ((data1.compID.equalsIgnoreCase("parent"))&&(i1 < last)){
				System.out.println("moving parent from "+ i1);
				components.removeElementAt(i1);
				components.addElement(data1);
				// do this index again because everything moved down to this index
				// with the remove of the parent element
				continue;
			}

		    rect1 = data1.screenRect;
			
			for(i2=i1+1;i2<last;i2++){
				System.out.println("data2 component number "+ i2);
			    data2 = (CompData) components.elementAt(i2);
			    rect2 = data2.screenRect;
		
				if(rect1.equals(rect2)) {
					// do nothing, they don't have to switch?
				}else if(rect1.contains(rect2)) {
			    	imatch = i2;
			    	System.out.println("Rect1:"+rect1.toString());
			    	System.out.println("Rect2:"+rect2.toString());
			    }
			}
			
			// if we found rect1.contains rect2 then swap them
			if (imatch > 0){
				System.out.println("imatch component number "+ imatch);
				components.removeElementAt(i1);
				components.insertElementAt(data1, imatch);
			}else{
				i1++;
			}
			
		}while(i1<last);
	}
	
	static final int ARG_COUNT = 4;
	
	static void filespecInvalidError(String type){
		System.err.println( 
		"\n"+ type +" file specification is INVALID!\n\n"+
		"1) Window or Object Name.\n"+
		"2) Fullpath of INI file to process.\n"+
		"3) Fullpath of JPG file to process.\n"+
		"4) Fullpath of HTM output file to write.\n");
	}

	public MAPJPGProcessor (){;}
	
	public void run (String[] args){
		boolean success;
		if (args.length < ARG_COUNT){
			System.err.println( 
			"\nInsufficient number of parameters!\n\n"+
			"1) Window or Object Name.\n"+
			"2) Fullpath of INI file to process.\n"+
			"3) Fullpath of JPG file to process.\n"+
			"4) Fullpath of HTM output file to write.\n");
			return;
		}
		
		objectName = args[0];
		
		File inifile = new CaseInsensitiveFile(args[1]).toFile();
		if ((! inifile.exists())||(! inifile.isFile())){
			filespecInvalidError("INI");
			return;
		}
		File jpgfile = new CaseInsensitiveFile(args[2]).toFile();
		if ((! jpgfile.exists())||(! jpgfile.isFile())){
			filespecInvalidError("JPG");
			return;
		}
		File htmfile = new CaseInsensitiveFile(args[3]).toFile();
		
		if (htmfile.exists()){
			
			if(htmfile.isFile()){
				
				success = htmfile.delete();
				if(!success){
					filespecInvalidError("Delete HTM");
					return;
				}
				try{
					success = htmfile.createNewFile();
				    if(!success){
  					    filespecInvalidError("Create HTM");
					    return;
				    }				    
				}
				catch(IOException iox){
  					filespecInvalidError("Create HTM IOException");
				    return;
				}
			}
			else{
				filespecInvalidError("HTM as Directory");
				return;
			}
		}else{
			try{
				success = htmfile.createNewFile();
			    if(!success){
 					filespecInvalidError("Create HTM");
				    return;
			    }				    
			}
			catch(IOException iox){
  					filespecInvalidError("Create HTM IOException");
			    return;
			}
		}
		if(! htmfile.canWrite()){
  	        filespecInvalidError("Cannot Write HTM");
			return;
		}

		INIFileReader inireader = new INIFileReader(inifile, INIFileReader.IFR_MEMORY_MODE_STORED);
		Vector sections = inireader.getSections();
		
		if (sections.isEmpty()){
  	        filespecInvalidError("Empty INI");
			return;
		}

		Vector components = new Vector(25,25);
		
		for(int i=0;i<sections.size();i++){
			CompData data = new CompData();
			String section = (String) sections.elementAt(i);
			if (section.length()==0) continue;
			data.compID   = section;
			data.compName = inireader.getAppMapItem(section, "Name");
			data.compPath = inireader.getAppMapItem(section, "Recognition");
			data.compType = inireader.getAppMapItem(section, "ComponentType");
			data.screenRect.x = Integer.parseInt(inireader.getAppMapItem(section, "ScreenLeft"));
			data.screenRect.y = Integer.parseInt(inireader.getAppMapItem(section, "ScreenTop"));
			data.screenRect.width  = Integer.parseInt(inireader.getAppMapItem(section, "ScreenWidth"));
			data.screenRect.height = Integer.parseInt(inireader.getAppMapItem(section, "ScreenHeight"));
			data.clientRect.x = Integer.parseInt(inireader.getAppMapItem(section, "ClientLeft"));
			data.clientRect.y = Integer.parseInt(inireader.getAppMapItem(section, "ClientTop"));
			data.clientRect.width  = Integer.parseInt(inireader.getAppMapItem(section, "ClientWidth"));
			data.clientRect.height = Integer.parseInt(inireader.getAppMapItem(section, "ClientHeight"));
			data.parentOffset.x  = Integer.parseInt(inireader.getAppMapItem(section, "ParentOffsetX"));
			data.parentOffset.y  = Integer.parseInt(inireader.getAppMapItem(section, "ParentOffsetY"));
			components.add(data);
		}
		components.trimToSize();
		int size = components.size();
		System.out.println("\n"+ size +" objects have been retrieved from "+ inifile.getName()+"\n");
		arrangeComponents(components);
		
		BufferedWriter htmwriter = null;
		try{
			htmwriter = new BufferedWriter(new FileWriter(htmfile));
			htmwriter.write(getHTMLHeader(objectName));
			
			CompData acomp = null;
			int tx, ty, bx, by;
			for(int i=0;i<size;i++){
				acomp=(CompData) components.elementAt(i);
				tx = acomp.parentOffset.x;
				ty = acomp.parentOffset.y;
				bx = acomp.parentOffset.x+acomp.screenRect.width;
				by = acomp.parentOffset.y+acomp.screenRect.height;
				htmwriter.write(
				"<area alt='"+ acomp.compName +":"+ acomp.compPath +"' nohref coords='");
				htmwriter.write( tx +","+ ty +",");
				htmwriter.write( bx +","+ by);
				htmwriter.write("' shape='rect' />");
				htmwriter.newLine();
			}
			htmwriter.write("</map>\n</body></html>");
		}
		catch(IOException iox){
  	        filespecInvalidError("Cannot Begin HTM");
			return;
		}


		inireader.close();
		try{htmwriter.close();}catch(Exception ex){;}
		
	}
	
	public static void main(String[] args) {
		
		String[] newargs = new String[ARG_COUNT];
		newargs[0] = "ClassicsCLogin";
		newargs[1] = "C:/sqarepos/ddengine/datapool/ClassicsCLogin.INI";
		newargs[2] = "C:/sqarepos/ddengine/datapool/ClassicsCLogin.JPG";
		newargs[3] = "C:/sqarepos/ddengine/datapool/ClassicsCLoginNew.HTM";
		MAPJPGProcessor processor = new MAPJPGProcessor();
		if (args.length == 0 ){
			System.out.println (
			"\nUsing development/debug parameters:\n\n"+
			newargs[0]+"\n"+
			newargs[1]+"\n"+
			newargs[2]+"\n"+
			newargs[3]+"\n");
			processor.run(newargs);
		}
		else{
			processor.run(args);
		}
		
		processor = null;
	}
	
	public class CompData {
		
		public String compID   = "";
		public String compName = "";
		public String compPath = "";
		public String compType = "";
		public Rectangle screenRect = new Rectangle();
		public Rectangle clientRect = new Rectangle();
		public Point parentOffset   = new Point();
		
		public CompData(){;}
	}
}

