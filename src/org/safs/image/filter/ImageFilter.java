package org.safs.image.filter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Takes an image and sets of coordinates as arguments, and draws rectangles on the image for each set of 
 * coordinates. Used for filtering out dynamic data in screenshots.
 * 
 * Associated with {@link ImageFilterGUI}
 * 
 * @author Philippe Sabourin - phsabo
 * @version 1.1 (07/19/06 - 3:45PM) - added multiple rectangle capability
 * @version 1.0 (07/19/06 - 3:15PM) - first version - one rectangle
 */
public class ImageFilter {
	
	/**
	 * This constructor takes the filename and the rectangles to be drawn and draws them.
	 * @param filename filename of image to be edited
	 * @param rects rectangles of darkness to be applied
	 * @param goodR determines which rectangles had correct coordinates
	 */
	public ImageFilter(String []  args) {
		String coords = "";
		for(int i = 3; i<args.length; i++){
			coords+= args[i]+" ";
		}
		coords = coords.trim();
		FilterImage(args[0],args[1],args[2],coords);
		
		
	}
	
	public void FilterImage(String InputFilename, String OutputFilename, String FilterMode, String Coords){
		if(FilterMode.equals("COORD")){
			String [] arrayOfCoords = Coords.split(" ");
			
			int[][] rects = new int[arrayOfCoords.length][4];
			boolean [] goodR = new boolean[arrayOfCoords.length];
			for(int i = 0; i < arrayOfCoords.length; i++){
				String [] coords = arrayOfCoords[i].split(",");
				if(coords.length != 4){
					System.out.println("Warning: Rectangle " + i + " has wrong number of coordinates. (Ignored)");
					goodR[i]= false;
				} else {				
					for(int j = 0; j < 4; j++){
						try{
							rects[i][j] = Integer.parseInt(coords[j]);
						}catch(NumberFormatException e){
							System.out.println("Warning: Rectangle " + i + "'s coordinate number " + (j+1) + " is not a number. (Ignored)");
							goodR[i]= false;
						}
					}
					goodR[i]= true;
				}
			}
					
			
			BufferedImage i = null;
			try {
				i = ImageIO.read(new File(InputFilename));
			} catch (IOException e) {
				System.out.println("Error: Image not readable.");
			}
			
			Graphics g = i.getGraphics();
			g.setColor(Color.BLACK);
			for(int j = 0; j < rects.length; j++){
				if(goodR[j] == true){
					g.fillRect(rects[j][0],rects[j][1],rects[j][2],rects[j][3]);
				}
			}
			
			
			try {
				ImageIO.write(i,"jpg",new File(OutputFilename));
			} catch (IOException e) {
				System.out.println("Error: Not able to save image.");
			}
		}
	}
	
	/**
	 * Main method that checks arguments then calls the constructor.
	 * Arguments are: ImageFilename FirstRectangleCoords [SecondRectCoords ThirdRectCoords ...]
	 * Coords are: offsetFromLeft,offsetFromRight,width,height
	 * @param args Arguments required for execution
	 */
	public static void main(String [] args){
		new ImageFilter(args);
	}

}
