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
package org.safs.selenium.spc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.safs.Log;

public class SPCGUI extends JPanel implements ActionListener,MouseListener,WindowListener,TreeSelectionListener{

	private static String [] BrowserStrings = {"*firefox", "*iexplore", "*pifirefox", "*piiexplore", "*chrome"};
	
	private SPC spc;
	private JButton btn_start;
	private JButton btn_stop;
	private JButton btn_search;
	private JButton btn_refresh;
	private JComboBox jcb_url;
	private JLabel lbl_url;
	private JLabel lbl_curwindows;
	private JComboBox jcb_browsers;
	private JComboBox jcb_curwindows;
	private JFrame fra_main;
	private SPCTreePanel stp_results;
	private ImagePreview img_preview;
	private JSplitPane split;
	private JScrollPane scp_preview;
	private Vector lines;
	private JTextField txt_rec;
	private JButton btn_find;
	private JLabel lbl_rec;
	private JButton btn_cancel;
	private JButton btn_savemap;
	private JFileChooser jfc_savemap;
	private JLabel lbl_status;
	
	private boolean interruptProgressBar = false;
	
	public SPCGUI(SPC spc){
		this.spc = spc;
		spc.setGUI(this);
		JFrame.setDefaultLookAndFeelDecorated(false);
		fra_main = new JFrame();
		fra_main.setTitle("Selenium Process Container");
		fra_main.setSize(1024,768);
		fra_main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fra_main.addWindowListener(this);
		this.setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		
		BufferedReader br;
		lines = new Vector();
		lines.add("");
		try {
			br = new BufferedReader(new FileReader("history.dat"));
			String line;
			while((line = br.readLine())!=null){
				lines.add(line);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		btn_start = new JButton("Start");
		btn_start.addActionListener(this);
		btn_stop = new JButton("Stop");
		btn_stop.addActionListener(this);
		btn_stop.setEnabled(false);
		btn_search = new JButton("Search");
		btn_search.addActionListener(this);
		btn_search.setEnabled(false);
		btn_refresh = new JButton("Find Windows");
		btn_refresh.addActionListener(this);
		btn_refresh.setEnabled(false);
		jcb_url = new JComboBox(lines);
		jcb_url.setEditable(true);
		lbl_url = new JLabel("URL:");
		jcb_browsers = new JComboBox(BrowserStrings);
		jcb_browsers.setSelectedIndex(0);
		jcb_browsers.addActionListener(this);
		lbl_curwindows = new JLabel("Current Windows: ");
		jcb_curwindows = new JComboBox();
		jcb_curwindows.setEnabled(false);
		jcb_curwindows.addActionListener(this);
		stp_results = new SPCTreePanel();
		stp_results.getTree().addTreeSelectionListener(this);
		stp_results.getTree().addMouseListener(this);
		txt_rec = new JTextField(45);
		txt_rec.setEnabled(false);
		btn_find = new JButton("Find Element");
		btn_find.addActionListener(this);
		btn_find.setEnabled(false);
		lbl_rec = new JLabel("Robot Recogntion String:");
		btn_cancel = new JButton("Cancel");
		btn_cancel.addActionListener(this);
		btn_cancel.setEnabled(false);
		img_preview = new ImagePreview();
		img_preview.addMouseListener(this);
		scp_preview = new JScrollPane(img_preview,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scp_preview.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Preview"));
		btn_savemap = new JButton("Save To Map");
		btn_savemap.setEnabled(false);
		btn_savemap.addActionListener(this);
		jfc_savemap = new JFileChooser();
		JPanel top = new JPanel();
		JPanel middle = new JPanel();
		JPanel middle2 = new JPanel();
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel statusPanel = new JPanel();
		top.add(lbl_url);
		top.add(jcb_url);
		top.add(jcb_browsers);
		top.add(btn_start);
		top.add(btn_stop);
		
		middle.add(lbl_curwindows);
		middle.add(jcb_curwindows);
		middle.add(btn_refresh);
		middle.add(btn_search);
		middle.add(btn_cancel);
		middle.add(btn_savemap);
		middle2.add(lbl_rec);
		middle2.add(txt_rec);
		middle2.add(btn_find);
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scp_preview, stp_results);
		split.setOneTouchExpandable(true);
		split.setDividerLocation(0);
		bottom.add(split,BorderLayout.CENTER);
		
		lbl_status = new JLabel("Status Bar.");
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		statusPanel.add(lbl_status);
		
		this.add(top);
		this.add(middle);
		this.add(middle2);
		this.add(bottom);
		this.add(statusPanel);
		fra_main.add(this);
		fra_main.setVisible(true);
	}
	
	public void setStatus(String status, Color foreground){
		lbl_status.setForeground(foreground);
		lbl_status.setText(status);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==btn_start){
			btn_start.setEnabled(false);
			jcb_url.setEditable(false);
			jcb_browsers.setEnabled(false);
			if(!lines.contains(jcb_url.getSelectedItem().toString())){
				lines.add(jcb_url.getSelectedItem().toString());
				jcb_url.updateUI();
			}
			String url = jcb_url.getSelectedItem().toString();
			if(url==null || url.trim().equals("")){
				Log.debug("url can not be null or void!!!");
				return;
			}
			spc.initializeSelenium(jcb_browsers.getSelectedItem().toString(), url);

			btn_search.setText("Searching...");
			spc.getAllElements("");
			btn_cancel.setEnabled(true);
		} else if (e.getSource()==btn_search) {
			jcb_curwindows.setEnabled(false);
			btn_search.setEnabled(false);
			btn_search.setText("Searching...");
			btn_refresh.setEnabled(false);
			btn_stop.setEnabled(false);
			btn_find.setEnabled(false);
			txt_rec.setEnabled(false);
			spc.getAllElements(jcb_curwindows.getSelectedItem().toString());
			btn_cancel.setEnabled(true);
		} else if(e.getSource()==btn_stop){
			btn_savemap.setEnabled(false);
			btn_search.setEnabled(false);
			btn_stop.setEnabled(false);
			btn_refresh.setEnabled(false);
			jcb_curwindows.removeAllItems();
			jcb_curwindows.setEnabled(false);
			txt_rec.setEnabled(false);
			btn_find.setEnabled(false);
			spc.stopSelenium();
			btn_start.setEnabled(true);
			jcb_url.setEditable(true);
			jcb_browsers.setEnabled(true);
			//Remove the tree
			stp_results.clear();
			//Remove the rs
			txt_rec.setText("");
			//Remove the preview
			img_preview.reset();
			setStatus("Status Bar.",Color.BLACK);
		} else if(e.getSource()==btn_refresh){
			updateWindows(spc.getWindows());
		} else if(e.getSource()==btn_find){
			String xpath = spc.getXpath(jcb_curwindows.getSelectedItem().toString(), txt_rec.getText());
			img_preview.clearHighlight();
			//TODO Why we select the root element???
			//stp_results.tree.setSelectionPath(new TreePath(stp_results.rootNode.getPath()));
			if(!xpath.equals("")){
				stp_results.setSelectedComponentByXpath(xpath);
			}else{
				JOptionPane.showMessageDialog(fra_main,"No element found with this recognition string!",
							"Element Not Found",
							JOptionPane.ERROR_MESSAGE);
			}
		} else if(e.getSource()==btn_cancel){
			spc.cancelSearch();
			btn_cancel.setEnabled(false);
			jcb_curwindows.setEnabled(true);
			btn_savemap.setEnabled(true);
			btn_search.setEnabled(true);
			btn_search.setText("Search");
			btn_refresh.setEnabled(true);
			btn_stop.setEnabled(true);
			btn_find.setEnabled(true);
			txt_rec.setEnabled(true);
		} else if(e.getSource()==btn_savemap){
			jfc_savemap.showSaveDialog(this);
			final File selfile = jfc_savemap.getSelectedFile();
			if(selfile == null){
				return;
			}
			final JFrame jfr_pbar = new JFrame("Saving Map: Please Wait");
			final JProgressBar jpb_bar = new JProgressBar(0,stp_results.allNodes.size()-1);
			interruptProgressBar = false;
			
			jpb_bar.setValue(0);
			Rectangle bounds = fra_main.getBounds();
			jfr_pbar.setBounds(bounds.x+bounds.width/2-125, bounds.y+bounds.height/2-25, 250, 50);
			jfr_pbar.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			jpb_bar.setStringPainted(true);
			jfr_pbar.add(jpb_bar);
			jfr_pbar.setVisible(true);
			jfr_pbar.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e) {
					interruptProgressBar = true;
				}
			});
			
			btn_refresh.setEnabled(false);
			btn_stop.setEnabled(false);
			btn_find.setEnabled(false);
			txt_rec.setEnabled(false);
			btn_search.setEnabled(false);
			btn_savemap.setEnabled(false);
			
			(new Thread(){
				public void run(){
					try {
						PrintWriter pw = new PrintWriter(selfile);
						spc.domParser.timeconsume1=0;
						spc.domParser.timeconsume2=0;
						spc.domParser.timeconsume3=0;
						SPCTreeNode node = null;
						for(int i = 0; i < stp_results.allNodes.size(); i++){
							if(interruptProgressBar) break;
							node = (SPCTreeNode) stp_results.allNodes.get(i);
							String temp = spc.getRobotRecognitionWithName(node.xpath);
							if(temp != null)
								pw.println(temp);
							jpb_bar.setValue(i);
						}
						Log.debug("PERFORMANCE CHECKING ########################## parse frame time1="+spc.domParser.timeconsume1);
						Log.debug("PERFORMANCE CHECKING ########################## parse last xpath time2="+spc.domParser.timeconsume2);
						Log.debug("PERFORMANCE CHECKING ########################## parse unique name time3="+spc.domParser.timeconsume3);
						pw.close();
						Runtime.getRuntime().exec("notepad.exe "+selfile.getAbsolutePath());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					jfr_pbar.dispose();
					btn_refresh.setEnabled(true);
					btn_stop.setEnabled(true);
					btn_find.setEnabled(true);
					txt_rec.setEnabled(true);
					btn_search.setEnabled(true);
					btn_savemap.setEnabled(true);
				}
			}).start();
			
		}
	}

	/**
	 * @param pframe the Frame for all data.  Can be null.
	 * @param data
	 * @author Carl Nagle JAN 27, 2015 Adding Frame support.
	 */
	public void updateData(SPCTreeNode pframe, String [] data){
		btn_cancel.setEnabled(false);
		stp_results.setData(pframe, data,spc.getXpathBoundsSeparator());
		img_preview.setImage(spc.getCurrentPreview());
		split.revalidate();
		
		jcb_curwindows.setEnabled(true);
		btn_savemap.setEnabled(true);
		btn_search.setEnabled(true);
		btn_search.setText("Search");
		btn_refresh.setEnabled(true);
		btn_stop.setEnabled(true);
		btn_find.setEnabled(true);
		txt_rec.setEnabled(true);
	}
	
	public void updateWindows(String [] titles){
		jcb_curwindows.removeAllItems();
		for(int i = 0; i < titles.length; i++){
			jcb_curwindows.addItem(titles[i]);
		}
	}
	
	private class ImagePreview extends JComponent{
		private BufferedImage img;
		private ArrayList highlight;
		
		public ImagePreview(){
			highlight = new ArrayList();
		}
		
		public void paint(Graphics g){
			if(img != null){
				g.drawImage(img, 0, 0, null);
				for(int i = 0; i < highlight.size();i++){
					g.setColor(Color.RED);
					Rectangle rect = (Rectangle)highlight.get(i);
					g.drawRect(rect.x, rect.y, rect.width, rect.height);
				}
			}
		}
		public void setHighlight(Rectangle hl){
			highlight.add(hl);
			this.scrollRectToVisible(hl);
			repaint();
		}
		public void clearHighlight(){
			highlight.clear();
			repaint();
		}
		public void setImage(BufferedImage bi){
			img=bi;
			clearHighlight();
			this.setPreferredSize(new Dimension(bi.getWidth(),bi.getHeight()));
			scp_preview.revalidate();
			repaint();
		}
		public void reset(){
			img=null;
			clearHighlight();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if(e.getSource() == img_preview){
			Point pt = e.getPoint();
			stp_results.setSelectedComponentByPoint(new org.openqa.selenium.Point(pt.x, pt.y));
		} else if (e.getSource() == stp_results.getTree()){
			if(e.getClickCount()==2){
				if(!stp_results.getSelectedComponent().xpath.equalsIgnoreCase("/Root"))
					new SPCElementInfoFrame(stp_results.getSelectedComponent().xpath,spc);
			}
		}
	}

	public void mouseEntered(MouseEvent arg0) {}

	public void mouseExited(MouseEvent arg0) {}

	public void mousePressed(MouseEvent arg0) {}

	public void mouseReleased(MouseEvent arg0) {}

	public void windowActivated(WindowEvent arg0) {}
	
	public void windowClosed(WindowEvent arg0) {}

	public void windowClosing(WindowEvent arg0) {
		spc.stopJavaHOOK();
		spc.stopSelenium();
		try {
			PrintWriter pr = new PrintWriter(new FileWriter("history.dat"));
			for(int i =1; i < lines.size(); i++){
				pr.println(lines.get(i));
			}
			pr.close();
		} catch (IOException e) {
		}
	}

	public void windowDeactivated(WindowEvent arg0) {}

	public void windowDeiconified(WindowEvent arg0) {}

	public void windowIconified(WindowEvent arg0) {}

	public void windowOpened(WindowEvent arg0) {}
	
	/**
	 *  AUG 10, 2012	(Lei Wang) During getting the Recognition String according to a node's xpath,
	 *  						 try to get property value id and name of HTML element and set them to tree node.
	 */
	public void valueChanged(TreeSelectionEvent arg0) {
		SPCTreeNode node = (SPCTreeNode)stp_results.getTree().getLastSelectedPathComponent();
		if (node == null){
			Log.error("SPCGUI: The SPC tree returned node is null!");
			return;
		}
		img_preview.clearHighlight();
		img_preview.setHighlight(node.bounds);
		
		//set the RECOGNITION STRING in the text box
		String rs = node.getRecognitionString();
		if(rs==null){
			SPCTreeNode element = spc.getRobotRecognitionNode(node.xpath);
			if(element==null){
				Log.error("SPCGUI: Can't get a RecognitionNode for xpath '"+node.xpath+"'");
			}else{				
				node.setRecognitionString(element.getRecognitionString());
				node.setId(element.getId());
				node.setName(element.getName());
			}
		}
		
		if(node.getRecognitionString()==null){
			String warning = "RS NOT FOUND for xpath: "+node.xpath;
			Log.warn(warning);
			setStatus(warning,Color.RED);
			txt_rec.setForeground(Color.RED);
			txt_rec.setText("RS NOT FOUND");
		}else{
			txt_rec.setForeground(Color.BLACK);
			txt_rec.setText(node.getRecognitionString());
			setStatus("RS FOUND for xpath:"+node.xpath,Color.BLACK);
		}
		
		//HIGHLIGHT the component on the HTML page
		boolean highlighted = spc.highlight(node);
		if(!highlighted){
			JOptionPane.showMessageDialog(this, "Can NOT highlight.", "NOT HIGHLIGHTED", JOptionPane.WARNING_MESSAGE);
		}
	}

}
