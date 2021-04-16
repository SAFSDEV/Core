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
package org.safs.image.filter;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.safs.image.ImagePreview;
import org.safs.text.TextFileFilter;
	
/** 
 * This displays an image.  When the user drags within
 * the image, this program displays a rectangle and a string
 * indicating the bounds of the rectangle.
 */
public class ImageFilterGUI implements ActionListener,TableModelListener{
	
	String imageName;
	JTextField label;
	static String imageFile = "example.jpg";
	JFileChooser imageFileChooser;
	JFileChooser textFileChooser;
	JButton openButton;
	JButton clearButton;
	JButton allOnButton;
	JButton allOffButton;
	JButton toggleButton;
	JButton saveToButton;
	JButton openFromButton;
	JButton saveMaskButton;
	JButton deleteButton;
	SelectionArea area;
	Container container;
	JScrollPane imageScroll;
	ArrayList allRects;
	ArrayList onOffRects;
	JTable table;
	ImageFilterTableModel tblModel;
	JPanel tblPanel = null;
	JLabel lbl;
	
	private void buildUI(Container container, ImageIcon image) {
		allRects = new ArrayList();
		onOffRects = new ArrayList();
		imageFileChooser = new JFileChooser();
		tblModel = new ImageFilterTableModel();
		tblModel.addTableModelListener(this);
		table = new JTable(tblModel);
		table.setPreferredScrollableViewportSize(new Dimension(500, 120));
		imageFileChooser.setFileFilter(new ImageFileFilter());
		imageFileChooser.setAccessory(new ImagePreview(imageFileChooser));
		
		textFileChooser = new JFileChooser();
		textFileChooser.setFileFilter(new TextFileFilter());
		
		imageName= "example.jpg";
		
		this.container = container;
		container.removeAll();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		//container.setLayout(new FlowLayout());
		area = new SelectionArea(image, this, allRects,onOffRects);
		label = new JTextField("Drag within the image.",45);
		label.setEditable(false);
		lbl = new JLabel("Call to ImageFilter: ");
		
		JPanel pnl = new JPanel(new FlowLayout());
		pnl.add(area);
		imageScroll = new JScrollPane(pnl);
		openButton = new JButton("Open Image...");
		openButton.addActionListener(this);
		allOffButton = new JButton("All Off");
		allOffButton.addActionListener(this);
		allOffButton.setMaximumSize(new Dimension(150,30));
		allOnButton = new JButton("All On");
		allOnButton.addActionListener(this);
		allOnButton.setMaximumSize(new Dimension(150,30));
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		clearButton.setMaximumSize(new Dimension(150,30));
		toggleButton = new JButton("Toggle Selected");
		toggleButton.addActionListener(this);
		toggleButton.setMaximumSize(new Dimension(150,30));
		saveToButton = new JButton("Save Coords...");
		saveToButton.addActionListener(this);
		openFromButton = new JButton("Open Coords...");
		openFromButton.addActionListener(this);
		saveMaskButton = new JButton("Save Mask...");
		saveMaskButton.addActionListener(this);
		deleteButton = new JButton("Delete Selected");
		deleteButton.addActionListener(this);
		deleteButton.setMaximumSize(new Dimension(150,30));
		
		
		

		
		TableColumn colOnOff = table.getColumnModel().getColumn(6);
		colOnOff.setCellEditor(new DefaultCellEditor(new JCheckBox()));
		colOnOff.setPreferredWidth(60);
		colOnOff.setMinWidth(50);
		colOnOff.setMaxWidth(50);
		
		
		tblPanel = new JPanel();
		tblPanel.setMinimumSize(new Dimension(500,150));
		JScrollPane tblScroll = new JScrollPane(table);
		tblPanel.add(table.getTableHeader());
		tblPanel.add(tblScroll);
		colOnOff.setPreferredWidth(30);
		JPanel labels = new JPanel(new FlowLayout());
		labels.add(lbl);
		labels.add(label);
		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(openButton);
		buttons.add(openFromButton);
		buttons.add(saveToButton);
		buttons.add(saveMaskButton);
		
		JPanel buttons2 = new JPanel(new FlowLayout());
		buttons2.setLayout(new BoxLayout(buttons2,BoxLayout.Y_AXIS));
		buttons2.add(allOnButton);
		buttons2.add(allOffButton);
		buttons2.add(toggleButton);
		buttons2.add(deleteButton);
		buttons2.add(clearButton);
		
		JPanel bottom = new JPanel(new FlowLayout());
		bottom.add(tblPanel);
		bottom.add(buttons2);
		
		container.add(imageScroll);
		container.add(labels);
		container.add(buttons);
		container.add(bottom);
		
	}

	public void updateLabel() {
		
		String call = "java ImageFilter [PATH]" + imageName;
		for(int i = 0; i< allRects.size();i++){
			if(onOffRects.get(i).equals(Boolean.TRUE)){
				Rectangle rect = (Rectangle)allRects.get(i);
				call+= " " + rect.x + "," + rect.y + "," + (rect.x+rect.width-1) + "," + (rect.y+rect.height-1);
			}
		}
		label.setText(call);
	}
	
	public void updateTable(){
		
		for(int i = 0; i < allRects.size(); i++){
			Rectangle temp = (Rectangle)allRects.get(i);
			tblModel.setValueAt(new Integer(temp.x),i,0);
			tblModel.setValueAt(new Integer(temp.y),i,1);
			tblModel.setValueAt(new Integer(temp.x+temp.width-1),i,2);
			tblModel.setValueAt(new Integer(temp.y+temp.height-1),i,3);
			tblModel.setValueAt(new Integer(temp.width),i,4);
			tblModel.setValueAt(new Integer(temp.height),i,5);
			tblModel.setValueAt(onOffRects.get(i),i,6);
		}
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = ImageFilterGUI.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Create the GUI and show it.  For thread safety, 
	 * this method should be invoked from the 
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("Image Filter GUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Set up the content pane.
		ImageFilterGUI controller = new ImageFilterGUI();
		controller.buildUI(frame.getContentPane(),createImageIcon(imageFile));

		//Display the window.
		frame.setSize(900,700);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(); 
			}
		});
	}
	
	private class SelectionArea extends JLabel implements ListSelectionListener{
		Rectangle currentRect = null;
		ArrayList onOffRects;
		ArrayList allRects;
		ImageFilterGUI controller;
		KeyboardListener kl ;
		boolean drag;
		int startX;
		int startY;
		int rectX;
		int rectY;
		int row;
		public SelectionArea(ImageIcon image, ImageFilterGUI controller, ArrayList allRects,ArrayList onOffRects) {
			super(image); //This component displays an image.
			this.allRects = allRects;
			this.onOffRects = onOffRects;
			this.controller = controller;
			setOpaque(true);
			setMinimumSize(new Dimension(10,10)); //don't hog space
			setFocusable(true);
			MyListener myListener = new MyListener();
			addMouseListener(myListener);
			addMouseMotionListener(myListener);
			table.getSelectionModel().addListSelectionListener(this);
			
			kl = new KeyboardListener();
			
			addKeyListener(kl);
		}
		
		private class MyListener extends MouseInputAdapter {
			public void mousePressed(MouseEvent e) {
				area.grabFocus();
				drag = false;
				Rectangle temp = null;
				for(int i = allRects.size()-1; i >= 0; i--){
					if(onOffRects.get(i).equals(Boolean.TRUE)){
						temp = (Rectangle)allRects.get(i);
						if(temp.contains(e.getX(),e.getY())){
							drag = true;
							row = i;
							break;
						}
					}
				}
				if(drag){
					startX = e.getX();
					startY = e.getY();
					rectX = temp.x;
					rectY = temp.y;
					currentRect = temp;
				}else {
					int x = e.getX();
					int y = e.getY();
					currentRect = new Rectangle(x, y, 0, 0);
				}
				repaint();
			}
	
			public void mouseDragged(MouseEvent e) {
				area.grabFocus();
				if(drag){
					updatePosition(e);
				} else {
					updateSize(e);
				}
				
			}
	
			public void mouseReleased(MouseEvent e) {
				area.grabFocus();
				if(!drag){
					currentRect = getDrawableRect(getWidth(), getHeight(),currentRect);
					if(currentRect.getWidth() > 2 && currentRect.getHeight() > 2){
						allRects.add(currentRect);
						onOffRects.add(Boolean.TRUE);
						controller.updateTable();
						controller.updateLabel();
					}
				}
				currentRect = null;
				repaint();
			}
			
			public void mouseClicked(MouseEvent e){
				area.grabFocus();
				for(int i = allRects.size()-1; i >= 0; i--){
					if(onOffRects.get(i).equals(Boolean.TRUE)){
						Rectangle temp = (Rectangle)allRects.get(i);
						if(temp.contains(e.getX(),e.getY())){
							if(kl.ctrlModifier)
								table.changeSelection(i,i,true,false);
							else
								table.changeSelection(i,i,false,false);
							return;
						}
					}
				}
			}
	
			void updateSize(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int width = x - currentRect.x;
				int height = y - currentRect.y;
				boolean wP = width > 0;
				boolean hP = height > 0;
				if(kl.shiftModifier){
					if(width < height){
						height =width;
					} else {
						width = height;
					}
					if(!wP){
						width = - Math.abs(width);
					} else {
						width = Math.abs(width);
					}
					if(!hP){
						height = - Math.abs(height);
					} else {
						height = Math.abs(height);
					}
				}
				
				currentRect.setSize(width,
									height);
				repaint();
			}
			
			void updatePosition(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int movedX = x - startX;
				int movedY = y - startY;
				currentRect.x = rectX + movedX;
				currentRect.y = rectY + movedY;
				if(currentRect.x < 0){
					currentRect.x = 0;
				}
				if(currentRect.y < 0){
					currentRect.y = 0;
				}
				if(currentRect.y + currentRect.height - 1 > area.getIcon().getIconHeight()){
					currentRect.y = area.getIcon().getIconHeight() - currentRect.height + 1;
				}
				if(currentRect.x + currentRect.width - 1  > area.getIcon().getIconWidth()){
					currentRect.x = area.getIcon().getIconWidth() - currentRect.width + 1;
				}
				table.setValueAt(new Integer(currentRect.x),row,0);
				table.setValueAt(new Integer(currentRect.y),row,1);
				table.setValueAt(new Integer(currentRect.x + currentRect.width -1),row,2);
				table.setValueAt(new Integer(currentRect.y + currentRect.height -1),row,3);
				table.setValueAt(new Integer(currentRect.width),row,4);
				table.setValueAt(new Integer(currentRect.height),row,5);
				repaint();
			}
		}
		
		private class KeyboardListener implements KeyListener {
			boolean shiftModifier;
			boolean ctrlModifier;
			public KeyboardListener(){
				shiftModifier = false;
				ctrlModifier = false;
			}
			
			public void keyTyped(KeyEvent arg0) {
				
			}

			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_DELETE){
					controller.actionPerformed(new ActionEvent(deleteButton,0,null));
				}
				if(arg0.getKeyCode()== KeyEvent.VK_UP || arg0.getKeyCode()== KeyEvent.VK_DOWN || arg0.getKeyCode()== KeyEvent.VK_LEFT || arg0.getKeyCode()== KeyEvent.VK_RIGHT){
					int [] selected = table.getSelectedRows();
					if(selected.length == 1){
						Rectangle temp = (Rectangle)allRects.get(selected[0]);
						int pxlAmount = 1;
						if(ctrlModifier){
							pxlAmount =5;
						}
						switch(arg0.getKeyCode()){
							case KeyEvent.VK_UP:
								if(shiftModifier){
									temp.height = temp.height - pxlAmount;
									if(temp.height < 2){
										temp.height = 2;
									}
									tblModel.setValueAtSimple(new Integer(temp.height), selected[0],5);
								} else {
									temp.y = temp.y - pxlAmount;
									if(temp.y < 0){
										temp.y = 0;
									}
									tblModel.setValueAtSimple(new Integer(temp.y),selected[0],1);
								}
								tblModel.setValueAtSimple(new Integer(temp.y + temp.height -1),selected[0],3);
							break;
							case KeyEvent.VK_DOWN: 
								
								if(shiftModifier){
									temp.height = temp.height + pxlAmount;
									tblModel.setValueAtSimple(new Integer(temp.height), selected[0],5);
								} else {
									temp.y = temp.y + pxlAmount;
									if(temp.y + temp.height - 1 > area.getIcon().getIconHeight()){
										temp.y = area.getIcon().getIconHeight() - temp.height + 1;
									}
									tblModel.setValueAtSimple(new Integer(temp.y),selected[0],1);
								}								
								tblModel.setValueAtSimple(new Integer(temp.y + temp.height -1),selected[0],3);
							break;
							case KeyEvent.VK_LEFT: 
								if(shiftModifier){
									temp.width = temp.width - pxlAmount;
									if(temp.width < 2){
										temp.width = 2;
									}
									tblModel.setValueAtSimple(new Integer(temp.width), selected[0],4);
								} else {
									temp.x = temp.x - pxlAmount;
									if(temp.x < 0){
										temp.x = 0;
									}
									tblModel.setValueAtSimple(new Integer(temp.x),selected[0],0);
								}
								tblModel.setValueAtSimple(new Integer(temp.x + temp.width - 1),selected[0],2);
							break;
							case KeyEvent.VK_RIGHT: 
								if(shiftModifier){
									temp.width = temp.width + pxlAmount;
									tblModel.setValueAtSimple(new Integer(temp.width), selected[0],4);
								} else {
									temp.x = temp.x + pxlAmount;
									if(temp.x + temp.width - 1  > area.getIcon().getIconWidth()){
										temp.x = area.getIcon().getIconWidth() - temp.width + 1;
									}
									tblModel.setValueAtSimple(new Integer(temp.x),selected[0],0);
								}
								tblModel.setValueAtSimple(new Integer(temp.x + temp.width -1),selected[0],2);
							break;
						}
						tblModel.fireTableDataChanged();
						table.changeSelection(selected[0],selected[0],false,false);
						repaint();
						arg0.consume();
					}
				}
				if(arg0.getKeyCode() == KeyEvent.VK_SHIFT ){
					shiftModifier = true;
				}
				if(arg0.getKeyCode() == KeyEvent.VK_CONTROL ){
					ctrlModifier = true;
				}
			}

			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_SHIFT ){
					shiftModifier = false;
				}
				if(arg0.getKeyCode() == KeyEvent.VK_CONTROL ){
					ctrlModifier = false;
				}
				
			}
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g); //paints the background and image
			
			g.setXORMode(Color.white); //Color of line varies
			//g.setColor(new Color(0,0,0,75));			
			if (currentRect != null && drag == false) {
				//Draw a rectangle on top of the image.
				Rectangle rectToDraw = getDrawableRect(getWidth(), getHeight(),currentRect);
				g.fillRect(rectToDraw.x, rectToDraw.y, rectToDraw.width - 1, rectToDraw.height - 1);
				g.drawRect(rectToDraw.x+2,rectToDraw.y+2,rectToDraw.width-6,rectToDraw.height-6);
			}
			
			for(int i = 0; i < allRects.size(); i++){
				Rectangle tempRect = (Rectangle)allRects.get(i);
				if(onOffRects.get(i).equals(Boolean.TRUE)){
					g.fillRect(tempRect.x, tempRect.y, tempRect.width - 1, tempRect.height - 1);
				}
			}
			
			if(allRects.size()>0){
				int[] selected = table.getSelectedRows();
				
				for(int j = 0; j < selected.length; j++){
					if(onOffRects.get(selected[j]).equals(Boolean.TRUE)){
						Rectangle tempRect = (Rectangle)allRects.get(selected[j]);
						g.drawRect(tempRect.x+2,tempRect.y+2,tempRect.width-6,tempRect.height-6);
					}
					
				}
			}
		}
	
		private Rectangle getDrawableRect(int compWidth, int compHeight, Rectangle updateRect) {
			Rectangle rectToDraw = null;
			int x = updateRect.x;
			int y = updateRect.y;
			int width = updateRect.width;
			int height = updateRect.height;
	
			//Make the width and height positive, if necessary.
			if (width < 0) {
				width = 0 - width;
				x = x - width + 1; 
				if (x < 0) {
					width += x; 
					x = 0;
				}
			}
			if (height < 0) {
				height = 0 - height;
				y = y - height + 1; 
				if (y < 0) {
					height += y; 
					y = 0;
				}
			}
	
			//The rectangle shouldn't extend past the drawing area.
			if ((x + width) > compWidth) {
				width = compWidth - x;
			}
			if ((y + height) > compHeight) {
				height = compHeight - y;
			}
		
			//Update rectToDraw after saving old value.
			if (rectToDraw != null) {
				rectToDraw.setBounds(x, y, width, height);
			} else {
				rectToDraw = new Rectangle(x, y, width, height);
			}

			return rectToDraw;
		}

		public void valueChanged(ListSelectionEvent arg0) {
			repaint();
			
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==openButton){
			int returnVal = imageFileChooser.showOpenDialog(null);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	try {
	        		imageName = imageFileChooser.getSelectedFile().getName();
	        		ImageIcon ii = new ImageIcon(imageFileChooser.getSelectedFile().getCanonicalPath());
	        		//area.setIcon(ii);
	        		buildUI(container,ii);
	        		container.validate();
	        		container.repaint();
	        		onOffRects.clear();
					allRects.clear();
					tblModel.reinitialize();
					tblModel.fireTableDataChanged();
					updateLabel();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
	        }
		} else if(e.getSource()==clearButton){
			onOffRects.clear();
			allRects.clear();
			tblModel.reinitialize();
			tblModel.fireTableDataChanged();
			updateLabel();
			area.repaint();
		} else if(e.getSource()==allOnButton){
			for(int i = 0; i < onOffRects.size();i++){
				if(onOffRects.get(i).equals(Boolean.FALSE)){
					onOffRects.set(i,Boolean.TRUE);
					tblModel.setValueAt(onOffRects.get(i),i,6);
				}
			}
			tblModel.fireTableDataChanged();
		} else if(e.getSource()==allOffButton){
			for(int i = 0; i < onOffRects.size();i++){
				if(onOffRects.get(i).equals(Boolean.TRUE)){
					onOffRects.set(i,Boolean.FALSE);
					tblModel.setValueAt(onOffRects.get(i),i,6);
				}
			}
			tblModel.fireTableDataChanged();
		} else if(e.getSource()==toggleButton && allRects.size()>0){
			int[] selected = table.getSelectedRows();
			
			for(int i = 0; i < selected.length;i++){
				
				if(onOffRects.get(selected[i]).equals(Boolean.TRUE)){
					onOffRects.set(selected[i],Boolean.FALSE);
				} else {
					onOffRects.set(selected[i],Boolean.TRUE);
				}
				tblModel.setValueAt(onOffRects.get(selected[i]),selected[i],6);
			}
			tblModel.fireTableDataChanged();
			
		} else if(e.getSource()==deleteButton && allRects.size()>0){
			int[] selected = table.getSelectedRows();
			
			for(int i = selected.length-1; i >= 0;i--){
				allRects.remove(selected[i]);
				onOffRects.remove(selected[i]);
				tblModel.removeRow(selected[i]);
			}
			tblModel.fireTableDataChanged();
			
		} else if(e.getSource()==saveToButton){
			int returnVal = textFileChooser.showSaveDialog(null);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	try {
	        		PrintWriter out = new PrintWriter(new FileOutputStream(textFileChooser.getSelectedFile().getCanonicalPath()));
	        		for(int i = 0; i < allRects.size(); i++){
	        			Rectangle temp = (Rectangle)allRects.get(i);
	        			out.println(temp.x + "," + temp.y + "," + temp.width + "," + temp.height + " " + onOffRects.get(i));
	        		}
	        		out.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
	        }
			
		} else if(e.getSource()==openFromButton){
			int returnVal = textFileChooser.showOpenDialog(null);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	try {
	        		allRects.clear();
	        		onOffRects.clear();
	        		String line = "";
	        		
	        		BufferedReader in = new BufferedReader(new FileReader(textFileChooser.getSelectedFile().getCanonicalPath()));
	        		int count = 0;
	        		int[] rect = new int[4];
	        		boolean good;
	        		while((line = in.readLine()) != null){
	        			Rectangle temp = new Rectangle();
	        			good = true;
	        			String[] split = line.split(" ");
	        			String [] coords = split[0].split(",");
	    				if(coords.length != 4){
	    					System.out.println("Warning: Rectangle " + count + " has wrong number of coordinates. (Ignored)");
	    					good = false;
	    				} else {				
	    					for(int j = 0; j < 4; j++){
	    						try{
	    							rect[j] = Integer.parseInt(coords[j]);
	    						}catch(NumberFormatException ex){
	    							System.out.println("Warning: Rectangle " + count + "'s coordinate number " + (j+1) + " is not a number. (Ignored)");
	    						}
	    					}
	    					temp.setBounds(rect[0],rect[1],rect[2],rect[3]);
	    				}
	    				if(good){
	    					tblModel.reinitialize();
	    					allRects.add(temp);
	    					onOffRects.add(Boolean.valueOf(split[1]));
	    					updateLabel();
	    					updateTable();
	    					tblModel.fireTableDataChanged();
	    					area.repaint();
	    				}
	    				count++;
	        		}
	        		in.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
	        }
			
		} else if(e.getSource() == saveMaskButton){
			int returnVal = imageFileChooser.showSaveDialog(null);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
        		
	        	BufferedImage i = new BufferedImage(area.getIcon().getIconWidth(),area.getIcon().getIconHeight(),BufferedImage.TYPE_3BYTE_BGR);
        		        		
        		Graphics g = i.getGraphics();
        		g.setColor(Color.WHITE);        		
        		g.fillRect(0,0,i.getWidth(),i.getHeight());
        		g.setColor(Color.BLACK);
    			for(int j = 0; j < allRects.size(); j++){
    				if(onOffRects.get(j).equals(Boolean.TRUE)){
    					Rectangle temp = (Rectangle)allRects.get(j);
    					g.fillRect(temp.x,temp.y,temp.width,temp.height);
    				}
    			}
    			
    			
    			try {
    				ImageIO.write(i,"jpg",new File(imageFileChooser.getSelectedFile().getCanonicalPath()));
    			} catch (IOException ex) {
    				ex.printStackTrace();
    			}
	        }
		}
	}

	public void tableChanged(TableModelEvent e) {
		if(e.getColumn() == 6 && onOffRects.size()>0){
			int row = e.getFirstRow();
			int column = e.getColumn();
			Object data = table.getValueAt(row,column);
			onOffRects.set(row,data);
			updateLabel();
			area.repaint();
		} else if(e.getColumn() == 0 && onOffRects.size()>0){ //x1
			int row = e.getFirstRow();
			int column = e.getColumn();
			Object data = table.getValueAt(row,column);
			Rectangle temp = (Rectangle)allRects.get(row);
			int intData = 0;
			try{
				intData = ((Integer)data).intValue();
			} catch(NumberFormatException ex){
				tblModel.setValueAtSimple(new Integer(temp.x),row,column);
				tblModel.fireTableDataChanged();
				return;
			}
			
			temp.x = intData;
			allRects.set(row,temp);			
			updateLabel();
			area.repaint();
		} else if(e.getColumn() == 1 && onOffRects.size()>0){//y1
			int row = e.getFirstRow();
			int column = e.getColumn();
			Object data = table.getValueAt(row,column);
			Rectangle temp = (Rectangle)allRects.get(row);
			int intData = 0;
			try{
				intData = ((Integer)data).intValue();
			} catch(NumberFormatException ex){
				tblModel.setValueAtSimple(new Integer(temp.y),row,column);
				tblModel.fireTableDataChanged();
				return;
			}
			
			temp.y = intData;
			allRects.set(row,temp);			
			updateLabel();
			area.repaint();
		} else if(e.getColumn() == 2 && onOffRects.size()>0){//x2
			int row = e.getFirstRow();
			int column = e.getColumn();
			Object data = table.getValueAt(row,column);
			Rectangle temp = (Rectangle)allRects.get(row);
			int x2 = temp.x + temp.width -1;
			int intData = 0;
			try{
				intData = ((Integer)data).intValue();
			} catch(NumberFormatException ex){
				tblModel.setValueAtSimple(new Integer(x2),row,column);
				tblModel.fireTableDataChanged();
				return;
			}
			
			temp.width = intData + 1 - temp.x;
			allRects.set(row,temp);
			tblModel.setValueAtSimple(new Integer(temp.width),row,column+2);
			tblModel.fireTableDataChanged();
			updateLabel();
			area.repaint();
		} else if(e.getColumn() == 3 && onOffRects.size()>0){//y2
			int row = e.getFirstRow();
			int column = e.getColumn();
			Object data = table.getValueAt(row,column);
			Rectangle temp = (Rectangle)allRects.get(row);
			int y2 = temp.y + temp.height -1;
			int intData = 0;
			try{
				intData = ((Integer)data).intValue();
			} catch(NumberFormatException ex){
				tblModel.setValueAtSimple(new Integer(y2),row,column);
				tblModel.fireTableDataChanged();
				return;
			}
			
			temp.height = intData +1 -temp.y;
			allRects.set(row,temp);
			tblModel.setValueAtSimple(new Integer(temp.height),row,column+2);
			tblModel.fireTableDataChanged();
			updateLabel();
			area.repaint();
		}else if(e.getColumn() == 4 && onOffRects.size()>0){
			int row = e.getFirstRow();
			int column = e.getColumn();
			Object data = table.getValueAt(row,column);
			Rectangle temp = (Rectangle)allRects.get(row);
			int intData = 0;
			try{
				intData = ((Integer)data).intValue();
			} catch(NumberFormatException ex){
				tblModel.setValueAtSimple(new Integer(temp.width),row,column);
				tblModel.fireTableDataChanged();
				return;
			}
			
			temp.width = intData;
			int x2 = temp.x + temp.width -1;
			allRects.set(row,temp);		
			tblModel.setValueAtSimple(new Integer(x2),row,column-2);
			tblModel.fireTableDataChanged();
			updateLabel();
			area.repaint();
		} else if(e.getColumn() == 5 && onOffRects.size()>0){
			int row = e.getFirstRow();
			int column = e.getColumn();
			Object data = table.getValueAt(row,column);
			Rectangle temp = (Rectangle)allRects.get(row);
			int intData = 0;
			try{
				intData = ((Integer)data).intValue();
			} catch(NumberFormatException ex){
				tblModel.setValueAtSimple(new Integer(temp.height),row,column);
				tblModel.fireTableDataChanged();
				return;
			}
			
			temp.height = intData;
			int y2 = temp.y + temp.height -1;
			allRects.set(row,temp);		
			tblModel.setValueAtSimple(new Integer(y2),row,column-2);
			tblModel.fireTableDataChanged();			
			updateLabel();
			area.repaint();
		}
	}
}

