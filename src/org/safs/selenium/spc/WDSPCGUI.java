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
/**
 * SEP 26, 2017 (Lei Wang) Modified highlightSelectedNode(): check 'cssDisplay' and 'cssVisibility' of the tree node.
 * DEC 05, 2018	(Lei Wang) Modified the constructor WDSPCGUI(): set the frame to SPCTreePanel 'stp_results' if the combobox 'jcb_frames' item changed.
 *                        Modified valueChanged(): Set proper frame to combobox 'jcb_frames' when clicking on a tree node.
 *                        Added method waitPropertiesPanel(): sleep when waiting for the properties frame to be ready in the loop.
 */
package org.safs.selenium.spc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
//import org.safs.Log;
import org.safs.selenium.spc.SPCElementInfoFrame.FrameThread;
import org.safs.selenium.util.DocumentClickCapture;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.tools.stringutils.StringUtilities;

public class WDSPCGUI extends JPanel implements ActionListener,MouseListener,WindowListener,TreeSelectionListener{

	private static String [] BrowserStrings = {SelectBrowser.BROWSER_NAME_FIREFOX,
		                                       SelectBrowser.BROWSER_NAME_IE,
		                                       SelectBrowser.BROWSER_NAME_CHROME,
		                                       SelectBrowser.BROWSER_NAME_EDGE};
		                                       //SelectBrowser.BROWSER_NAME_SAFARI,
		                                       //SelectBrowser.BROWSER_NAME_IPAD_SAFARI,
		                                       //SelectBrowser.BROWSER_NAME_IPAD_SIMULATOR_SAFARI,
		                                       //SelectBrowser.BROWSER_NAME_ANDROID_CHROME,
		                                       //SelectBrowser.BROWSER_NAME_HTMLUNIT

	private WDSPC spc;
	private JButton btn_start;
	private JButton btn_stop;
	private JButton btn_search;
	private JButton btn_refresh;
	private JLabel lbl_url;
	private JLabel lbl_curwindows;
	private JLabel lbl_frames;
	private JComboBox jcb_browsers;
	        JComboBox<String> jcb_curwindows;
            JComboBox<String> jcb_url;
	        JComboBox<String> jcb_frames;  //keep visible to WDSPC
	private String jcb_html_frame_info_original_value;
	private JFrame fra_main;
	private SPCTreePanel stp_results;
	private ImagePreview img_preview;
	private JSplitPane split;
	private JScrollPane scp_preview;
	private Vector lines;
	private JButton btn_grab;
	private JTextField txt_rec;
	private JButton btn_find;
	private JButton btn_set;
	private JButton btn_children;
	private JButton btn_properties;
	private JLabel lbl_rec;
	private JButton btn_cancel;
	private JButton btn_savemap;
	private JLabel lbl_status;
	private JCheckBox chk_short;
	private JCheckBox chk_visible;
	private JCheckBox chk_properties;

	JPanel top = new JPanel();
	JPanel middle = new JPanel();
	JPanel middle1 = new JPanel();
	JPanel middle2 = new JPanel();
	JPanel bottom = new JPanel(new BorderLayout());
	JPanel statusPanel = new JPanel();

	private boolean clickModeOn = false;
	private boolean findElementMode = false;
	private boolean selectFrameMode = false;
    private boolean interruptProgressBar = false;
    private boolean useShortStrings = true;
    private boolean useVisibleOnly = true;
    private boolean useClickModeProperties = false;
    private Color backgroundColor = null;
    private Color busyBackgroundColor = Color.YELLOW.brighter();
    private Color prepBackgroundColor = Color.GREEN.brighter();

	public WDSPCGUI(WDSPC spc){
		this.spc = spc;
		if(spc!=null) spc.setGUI(this);
		JFrame.setDefaultLookAndFeelDecorated(false);
		fra_main = new JFrame();
		fra_main.setTitle("WebDriver Process Container");
		fra_main.setSize(1024,500);
		fra_main.setMinimumSize(new Dimension(1024,250));
		fra_main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
		btn_start.setToolTipText("Launch the selected browser with the URL.");
		btn_stop = new JButton("Stop");
		btn_stop.addActionListener(this);
		btn_stop.setEnabled(false);
		btn_stop.setToolTipText("Close the 'last used' browser and end that browser session.");
		btn_search = new JButton("Search");
		btn_search.addActionListener(this);
		btn_search.setEnabled(false);
		btn_search.setToolTipText("Process the selected window for ALL web elements on the page--can take a while!");
		btn_refresh = new JButton("Find Windows");
		btn_refresh.addActionListener(this);
		btn_refresh.setEnabled(false);
		btn_refresh.setToolTipText("Refresh the list of Current Window titles available.");
		jcb_url = new JComboBox<String>(lines);
		jcb_url.setEditable(true);
		jcb_url.setToolTipText("Enter the complete URL to be opened in a new browser session.");
		jcb_url.setPreferredSize(new Dimension(400, jcb_url.getPreferredSize().height));
		jcb_url.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent evt) {
				try{
					Set<FrameRS> frames = getFrameRSSetFromCache((String)jcb_url.getSelectedItem());
					if(frames!=null) updateFrameRSCombo(frames);
				}catch(Exception e){
					IndependantLog.warn(" ignore "+StringUtils.debugmsg(e));
				}
			}
		});


		lbl_url = new JLabel("URL:");
		jcb_browsers = new JComboBox(BrowserStrings);
		jcb_browsers.setSelectedIndex(0);
		jcb_browsers.addActionListener(this);
		jcb_browsers.setToolTipText("List of supported Selenium browsers to use with new sessions.");
		lbl_curwindows = new JLabel("    Current Windows: ");
		jcb_curwindows = new JComboBox<String>();
		if(spc!=null) updateWindows(spc.getWindows());
		jcb_curwindows.setPreferredSize(new Dimension(300, jcb_curwindows.getPreferredSize().height));
		jcb_curwindows.setEnabled(false);
		jcb_curwindows.addActionListener(this);
		jcb_curwindows.setToolTipText("List of browser window titles for the currently available sessions.");
		lbl_frames = new JLabel("    Frames: ");
		jcb_frames = new JComboBox<String>();
		jcb_frames.setPreferredSize(new Dimension(300, jcb_frames.getPreferredSize().height));
		jcb_frames.setEnabled(false);
		jcb_frames.addItem("");
		//jcb_frames.addActionListener(this);
		jcb_frames.setToolTipText("List of Frames found in the recently processed Window.");
		jcb_frames.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(! findElementMode){
					selectFrameMode = true;
					final String frame = (String)e.getItem();
					IndependantLog.info("Frames combo item change event: "+ frame);
					if(frame!=jcb_html_frame_info_original_value){
						IndependantLog.debug("Frames item "+ frame +" attempting Tree selection change...");
						jcb_html_frame_info_original_value = frame;

						//highlight the frame
						// find the item in the Panel and perform normal highlight
						if(frame.length()> 0){
							stp_results.setContextFrameRS(frame);
							SPCTreeNode frame_node = stp_results.getFrameNode(frame);
							if(frame_node == null){
								IndependantLog.debug("Frames item "+ frame +" was not found in the Tree Hierarchy.");
							}else{
								IndependantLog.info("Tree frame node search found "+ frame_node.getXpath());
								SPCTreeNode selected_item = stp_results.setSelectedComponentByXpath(frame_node.xpath);
								if(selected_item == null){
									IndependantLog.debug("TreePanel did NOT successfully select the frame node in the hierarchy.");
								}else{
									IndependantLog.info("TreePanel selection now "+ selected_item.getXpath());
									WebElement item = SearchObject.getObject(frame_node.xpath);
									if(item!=null){
										IndependantLog.info("TreePanel flashing webpage at "+ selected_item.getXpath());
										System.out.println("highlighting "+frame);
										SearchObject.highlight(item);
										StringUtilities.sleep(2000);
										SearchObject.clearHighlight();
									}else{
										IndependantLog.debug("Frames item "+ selected_item.getXpath() +" was NOT found on webpage!");
									}
								}
							}
						}else{
							IndependantLog.debug("Frames item change to empty (none) selected.");
						}
					}else{
						IndependantLog.info("Frames item change event same as last.");
					}
				}
				selectFrameMode = false;
			}
		});

		stp_results = new SPCTreePanel();
		stp_results.getTree().addTreeSelectionListener(this);
		stp_results.getTree().addMouseListener(this);
		stp_results.getTree().setToolTipText("Interactive Tree view shared with the Preview pane.");
		btn_grab = new JButton("START Click Mode");
		btn_grab.addActionListener(this);
		btn_grab.setToolTipText("Click me then Click the desired Web Element.");
		btn_grab.setEnabled(false);
		txt_rec = new JTextField(35);
		txt_rec.setEnabled(false);
		txt_rec.setToolTipText("Edit recognition string as needed to make more robust.");
		btn_find = new JButton("Find Element");
		btn_find.addActionListener(this);
		btn_find.setEnabled(false);
		btn_find.setToolTipText("Try to find and highlight the component based on the (edited) recognition string.");
		btn_set = new JButton("Set");
		btn_set.addActionListener(this);
		btn_set.setEnabled(false);
		btn_set.setToolTipText("Accept/Set the current recognition string for the selected Tree node.");
		btn_children = new JButton("Seek Children");
		btn_children.addActionListener(this);
		btn_children.setEnabled(false);
		btn_children.setToolTipText("Process potentially non-visible Children of this Element.");
		btn_properties = new JButton("Properties");
		btn_properties.addActionListener(this);
		btn_properties.setEnabled(false);
		btn_properties.setToolTipText("Show Available Properties for this Element.");
		lbl_rec = new JLabel("Recognition: ");
		btn_cancel = new JButton("Cancel");
		btn_cancel.addActionListener(this);
		btn_cancel.setEnabled(false);
		btn_cancel.setToolTipText("Cancel a long-running 'Search'.");
		img_preview = new ImagePreview();
		img_preview.addMouseListener(this);
		img_preview.setToolTipText("Interactive screenshot of the browser elements shared with the Tree view pane.");
		scp_preview = new JScrollPane(img_preview,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scp_preview.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Preview"));
		scp_preview.setToolTipText("Interactive screenshot of the browser elements shared with the Tree view pane.");
		btn_savemap = new JButton("Save To Map");
		btn_savemap.setEnabled(false);
		btn_savemap.addActionListener(this);
		btn_savemap.setToolTipText("Preview/Edit final subset of recognition strings to Save for an App Map.");

		chk_short = new JCheckBox("Short Strings");
		chk_short.setToolTipText("Prefer short string recognition over fullpath recognition.");
		chk_short.setSelected(useShortStrings);
		chk_short.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				useShortStrings = chk_short.isSelected();
			}
		}));

		chk_visible = new JCheckBox("Visible Only");
		chk_visible.setToolTipText("Only process Visible elements--exclude Hidden elements and dialogs.");
		chk_visible.setSelected(useVisibleOnly);
		chk_visible.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				useVisibleOnly = chk_visible.isSelected();
			}
		}));

		chk_properties = new JCheckBox("Click Mode Properties");
		chk_properties.setToolTipText("Automatically Show Properties after Click Mode Selection");
		chk_properties.setSelected(useClickModeProperties);
		chk_properties.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				useClickModeProperties = chk_properties.isSelected();
			}
		}));

		top.add(lbl_url);
		top.add(jcb_url);
		top.add(jcb_browsers);
		top.add(btn_start);
		top.add(btn_stop);

		middle.add(btn_refresh);
		middle.add(lbl_curwindows);
		middle.add(jcb_curwindows);
		middle.add(lbl_frames);
		middle.add(jcb_frames);

		middle1.add(chk_short);
		middle1.add(chk_visible);
		middle1.add(chk_properties);
		middle1.add(btn_search);
		middle1.add(btn_cancel);
		middle1.add(btn_savemap);

		middle2.add(btn_grab);
		middle2.add(lbl_rec);
		middle2.add(txt_rec);
		middle2.add(btn_find);
		middle2.add(btn_set);
		middle2.add(btn_children);
		middle2.add(btn_properties);

		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scp_preview, stp_results);
		split.setOneTouchExpandable(true);
		split.setDividerLocation(400);
		bottom.add(split,BorderLayout.CENTER);

		lbl_status = new JLabel("Status Bar.");
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		statusPanel.add(lbl_status);

		this.add(top);
		this.add(middle);
		this.add(middle1);
		this.add(middle2);
		this.add(bottom);
		this.add(statusPanel);
		fra_main.add(this);
		fra_main.setVisible(true);
		if(jcb_curwindows.getItemCount()> 0) {
			jcb_curwindows.setSelectedIndex(0);
			setGUIForReady();
		}
		backgroundColor = this.getBackground();
	}

	/**
	 * @param status text message
	 * @param foreground Color, can be null.
	 */
	public void setStatus(String status, Color foreground){
		if(!(foreground==null)) lbl_status.setForeground(foreground);
		lbl_status.setText(status);
	}

	public void setBackgroundBusy(){
		top.setBackground(busyBackgroundColor);
		middle.setBackground(busyBackgroundColor);
		middle1.setBackground(busyBackgroundColor);
		middle2.setBackground(busyBackgroundColor);
		bottom.setBackground(busyBackgroundColor);
		statusPanel.setBackground(busyBackgroundColor);
	}
	public void setBackgroundPrep(){
		top.setBackground(prepBackgroundColor);
		middle.setBackground(prepBackgroundColor);
		middle1.setBackground(prepBackgroundColor);
		middle2.setBackground(prepBackgroundColor);
		bottom.setBackground(prepBackgroundColor);
		statusPanel.setBackground(prepBackgroundColor);
	}
	public void setBackgroundReady(){
		top.setBackground(backgroundColor);
		middle.setBackground(backgroundColor);
		middle1.setBackground(backgroundColor);
		middle2.setBackground(backgroundColor);
		bottom.setBackground(backgroundColor);
		statusPanel.setBackground(backgroundColor);
	}

	public boolean useVisibleOnly(){ return useVisibleOnly; }
	public boolean useShortStrings(){ return useShortStrings; }

	void setGUIForReady(){
		btn_cancel.setEnabled(false);
		jcb_curwindows.setEnabled(true);
		jcb_frames.setEnabled(jcb_frames.getComponentCount()> 0);
		btn_savemap.setEnabled(true);
		btn_search.setEnabled(true);
		btn_search.setText("Search");
		lbl_status.setText("Ready");
		btn_refresh.setEnabled(true);
		btn_start.setEnabled(true);
		btn_stop.setEnabled(true);
		btn_grab.setEnabled(true);
		btn_find.setEnabled(true);
		btn_set.setEnabled(true);
		btn_children.setEnabled(true);
		btn_properties.setEnabled(true);
		txt_rec.setEnabled(true);
		// stp_results.setEnabled(true);
	}
	void setGUIForStart(){
		btn_start.setEnabled(true);
		jcb_url.setEditable(true);
		jcb_browsers.setEnabled(true);
		btn_cancel.setEnabled(false);
		lbl_status.setText("Ready");
	}

	void setGUIForAfterResetFrameRS(){
		jcb_curwindows.setEnabled(false);
		btn_search.setText("Search");
		lbl_status.setText("Searching context has changed, you need to search again.");
		//btn_refresh.setEnabled(false);
		//btn_start.setEnabled(false);
		//btn_stop.setEnabled(false);
		btn_cancel.setEnabled(false);
		btn_grab.setEnabled(false);
		btn_find.setEnabled(false);
		btn_set.setEnabled(false);
		btn_children.setEnabled(false);
		btn_properties.setEnabled(false);
		txt_rec.setEnabled(false);
		//After frameRS change, the search context will also change,
		//cannot click to search correct WebElement, so disable the stp_results
		stp_results.setEnabled(false);
	}

	void setGUIForSearching(){
		jcb_curwindows.setEnabled(false);
		jcb_frames.setEnabled(false);
		btn_search.setEnabled(false);
		btn_search.setText("Searching...");
		lbl_status.setText("Searching...");
		btn_refresh.setEnabled(false);
		btn_start.setEnabled(false);
		btn_stop.setEnabled(false);
		btn_grab.setEnabled(false);
		btn_find.setEnabled(false);
		btn_set.setEnabled(false);
		btn_children.setEnabled(false);
		btn_properties.setEnabled(false);
		txt_rec.setEnabled(false);
		btn_cancel.setEnabled(true);
	}
	void setGUIRunning(){
		btn_start.setEnabled(false);
		jcb_url.setEditable(false);
		jcb_browsers.setEnabled(false);
		btn_grab.setEnabled(false);
		btn_cancel.setEnabled(true);
		lbl_status.setText("Running");
	}

	private Object lastSearchedTitle = null;


	private void showProperties(WindowListener listener){
		if(spc!=null){
			SPCTreeNode node = stp_results.getSelectedComponent();
			if(node != null && !node.xpath.equalsIgnoreCase("/Root")){
				waitPropertiesPanel(node, spc, frames);
			}
		}
	}

	/**
	 * Wait the properties-panel (SPCElementInfoFrame) to be ready.
	 *
	 * @param node SPCTreeNode, for which to show the properties
	 * @param spc WDSPC
	 * @param frames Vector<JFrame>, the cache holding the properties-panels
	 */
	private void waitPropertiesPanel(SPCTreeNode node, WDSPC spc, Vector<JFrame> frames){
		FrameThread runner =new FrameThread(node,spc);
		runner.start();
		while(runner.frame == null){
			StringUtils.sleep(200);
		}
		StringUtils.sleep(100);
		frames.add(runner.frame);
		runner.frame.addWindowListener(this);
	}

	/**
	 * @param rs
	 * @return true if the item was added. false if not (already present)
	 */
	public boolean addFrameRS(String frameRS){
		if(frameRS == null || frameRS.length()==0) return false;
		for(int i=0;i<jcb_frames.getItemCount();i++){
			if(jcb_frames.getItemAt(i).toString().equalsIgnoreCase(frameRS)) return false;
		}
		jcb_frames.addItem(frameRS);

		return true;
	}

	/**
	 * @return the current selected value in jcb_frames combo-box.
	 * Can be null if no item is selected.
	 */
	public String getFrameRS(){
		Object item = jcb_frames.getSelectedItem();
		if(item!=null) return item.toString();
		return null;
	}

	public String appendFrameRS(String rs){
		if(!SearchObject.containFrameRS(rs)){
			if(SearchObject.isValidFrameRS(getFrameRS())){
				return getFrameRS()+SearchObject.childSeparator+rs;
			}
		}
		return rs;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String temp = null;
		if(e.getSource()==btn_start){
			if(spc != null)spc.enableSearch();
			setGUIRunning();
			String url = jcb_url.getSelectedItem().toString();
			if(url == null) url = "";
			url = url.trim();
			if(url.length()==0)
				url = jcb_url.getEditor().getItem().toString().trim();

			if(url.length()==0){
				IndependantLog.debug("*** URL can not be null or void ***");
				lbl_status.setText("URL must be 'set'");
				setGUIForStart();
				return;
			}
			if(url.indexOf("://") < 1){
				IndependantLog.debug("*** URL must have a valid protocol (like 'http://') ***");
				lbl_status.setText("URL must have valid protocol (http://)");
				setGUIForStart();
				return;
			}
			if(!lines.contains(url)){
				lines.add(url);
				//jcb_url.addItem(url);
				jcb_url.updateUI();
			}
			try{
				if(spc !=null) {
					capture = null;
					spc.initializeSelenium(jcb_browsers.getSelectedItem().toString(), url);
				}
			}catch(Exception x){
				setStatus("Start "+ x.getClass().getSimpleName()+" "+x.getMessage(),Color.RED);
			}
			setGUIForReady();

		} else if(e.getSource()==btn_grab){
			if(!clickModeOn){
				clickModeOn = true;
				btn_grab.setText("STOP Click Mode");
				btn_grab.setToolTipText("Click me to STOP Click Mode");
				setBackgroundBusy();
				startClickMode();
			}else{
				resetClickModeUI();
			}

		} else if (e.getSource()==btn_search) {
			lastSearchedTitle = jcb_curwindows.getSelectedItem();
			updatePreview();
			setGUIForSearching();
			if(spc != null){
				spc.enableSearch();
				spc.getAllElements(jcb_curwindows.getSelectedItem().toString(), getFrameRS());
			}

		} else if(e.getSource()==btn_stop){
			if(spc!=null){
				capture = null;
				spc.stopSelenium();
			}
			jcb_curwindows.removeAllItems();
			updateWindows(spc.getWindows());
			if(jcb_curwindows.getItemCount()==0){
				btn_savemap.setEnabled(false);
				btn_search.setEnabled(false);
				btn_stop.setEnabled(false);
				btn_refresh.setEnabled(false);
				jcb_curwindows.setEnabled(false);
				jcb_frames.setEnabled(false);
				txt_rec.setEnabled(false);
				btn_find.setEnabled(false);
				btn_set.setEnabled(false);
				btn_children.setEnabled(false);
				btn_properties.setEnabled(false);
				btn_start.setEnabled(true);
				jcb_url.setEditable(true);
				jcb_browsers.setEnabled(true);
			}else{
				// ?
			}
			//Remove the tree
			stp_results.clear();
			//Remove the rs
			txt_rec.setText("");
			//Remove the preview
			img_preview.reset();
			setStatus("Status Bar.",Color.BLACK);
		} else if(e.getSource()==btn_refresh){
			if(spc!=null) updateWindows(spc.getWindows());
		} else if(e.getSource()==btn_find){
			findElementMode = true;
			if(spc!=null){
				spc.enableSearch();

				updatePreview();

				spc.getUtils().setWDTimeout(0);
				//WebElement el = SearchObject.getObject(txt_rec.getText());
				String searchRS = appendFrameRS(txt_rec.getText());
				boolean isFrame = SearchObject.isValidFrameRS(searchRS);
				IndependantLog.info("FindElement searching for: "+ searchRS);
				WebElement el = SearchObject.getObject(searchRS);
				if(el == null){
					JOptionPane.showMessageDialog(fra_main,"No element found with this recognition string!",
							"Element Not Found",
							JOptionPane.ERROR_MESSAGE);
				}else{
					String xpath = searchRS;
					if(!isFrame){
						xpath = SearchObject.generateFullGenericXPath(el);
					}
					IndependantLog.info("FindElement found item with: "+ xpath);
					if(xpath!=null && xpath.length()> 0){
						SPCTreeNode selNode = stp_results.setSelectedComponentByXpath(xpath);
						if(selNode==null){
							IndependantLog.info("FindElement did NOT select Tree node with: "+ xpath);
							highlightByWebElement(el);
						}else{
							IndependantLog.info("FindElement selected Tree node: "+ selNode.getXpath());
							highlightSelectedNode();
						}
					}else{
						JOptionPane.showMessageDialog(fra_main,"No valid xpath deduced for the WebElement!",
									"XPath Not Found",
									JOptionPane.ERROR_MESSAGE);
					}
				}
				spc.getUtils().resetWDTimeout();
			}
			findElementMode=false;
		} else if(e.getSource()==btn_set){
			try{
				//stp_results.getSelectedComponent().setRecognitionString(txt_rec.getText());
				stp_results.getSelectedComponent().setRecognitionString(appendFrameRS(txt_rec.getText()));
				valueChanged(null);
			}catch(Exception n){ /* nothing selected? */}
		} else if(e.getSource()==btn_children){
			updatePreview();
			if(spc != null){
				setGUIForSearching();
				spc.enableSearch();
				spc.getUtils().setWDTimeout(0);
				String parentNode = appendFrameRS(txt_rec.getText());
				WebElement el = SearchObject.getObject(parentNode);
				//WebElement el = SearchObject.getObject(txt_rec.getText());
				if(el == null){
					JOptionPane.showMessageDialog(fra_main,"No element found with this recognition string!",
							"Element Not Found",
							JOptionPane.ERROR_MESSAGE);
				}else{
					spc.getAllChildElements(el, parentNode);
				}
			}
		} else if(e.getSource()==btn_properties){
			showProperties(WDSPCGUI.this);

		} else if(e.getSource()==btn_cancel){
			if(spc!=null) {
				spc.cancelSearch();
			}
			setGUIForReady();
		} else if(e.getSource()==btn_savemap){
			if(spc==null) return;//running from main() with no spc
			if(stp_results.allNodes.size() <= 1) return; //no nodes to process
			btn_refresh.setEnabled(false);
			btn_stop.setEnabled(false);
			btn_find.setEnabled(false);
			btn_set.setEnabled(false);
			btn_children.setEnabled(false);
			btn_properties.setEnabled(false);
			txt_rec.setEnabled(false);
			btn_search.setEnabled(false);
			btn_savemap.setEnabled(false);
			Thread runner = new Thread(){
				@Override
				public void run(){
					final SPCAppMapFrame jfr_pbar = new SPCAppMapFrame(stp_results.allNodes.size()-1, WDSPCGUI.this);
					interruptProgressBar = false;
					frames.add(jfr_pbar);
					jfr_pbar.addWindowListener(new WindowAdapter(){
						@Override
						public void windowClosing(WindowEvent e) {
							frames.remove(jfr_pbar);
							interruptProgressBar = true;
						}
					});
					SPCTreeNode node = null;
					SPCTreeNode.resetCompNamesCache();
					for(int i = 0; i < stp_results.allNodes.size(); i++){
						if(interruptProgressBar) break;
						node = (SPCTreeNode) stp_results.allNodes.get(i);
						String temp = node.getRecognitionString();
						String compname = node.generateComponentName();
						if(temp != null){
							jfr_pbar.setProgress(i+1);
							jfr_pbar.appendAppMapEntry(compname+"="+temp+"\n");
						}
					}
					SPCTreeNode.resetCompNamesCache();

					// won't continue til we the window is disposed by operation or user
					while(!interruptProgressBar){
						try{Thread.sleep(1000);}catch(Exception x){}
					}
					// may or may not be necessary.  May already be disposed.
					try{ jfr_pbar.dispose();}catch(Throwable t){}

					btn_refresh.setEnabled(true);
					btn_stop.setEnabled(true);
					btn_find.setEnabled(true);
					btn_set.setEnabled(true);
					btn_children.setEnabled(true);
					btn_properties.setEnabled(true);
					txt_rec.setEnabled(true);
					btn_search.setEnabled(true);
					btn_savemap.setEnabled(true);
				}
			};
			runner.setDaemon(true);
			runner.start();
		}
	}

	/** Clear any HIGHLIGHTS and take a new app screenshot for the preview pane. */
	private void updatePreview(){
		WDLibrary.clearHighlight();
		img_preview.clearHighlight();
		if(spc!=null)img_preview.setImage(spc.getCurrentPreview());
		split.revalidate();
		split.repaint();
	}

	private void highlightByWebElement(WebElement el){
		updatePreview();
		WDLibrary.highlight(el);
		img_preview.setHighlight(new Rectangle(el.getLocation().x, el.getLocation().y,
				                               el.getSize().width, el.getSize().height));
	}

	private DocumentClickCapture capture = null;

	/** Change the UI to show Click Mode is reenabled. */
	private void resetClickModeUI(){
		clickModeOn = false;
		btn_grab.setEnabled(true);
		btn_grab.setText("START Click Mode");
		btn_grab.setToolTipText("Click me then Click a Component in the Web App");
		setBackgroundReady();
	}

	/** Change the UI to show Click Mode Click is being processed. */
	private void prepClickModeUI(){
		btn_grab.setText("Processing Click");
		btn_grab.setEnabled(false);
		btn_grab.setToolTipText("Click detected and being processed...");
		setBackgroundPrep();
	}

	/** Handle the UI request to begin Click mode processing. */
	private void startClickMode(){
		if(capture == null) capture = new DocumentClickCapture();
		capture.setIgnoreEventInformation(false);

		updatePreview();
		Thread runner = new Thread(){
			@Override
			public void run(){
				boolean done = false;
				boolean dirty = false;
				org.safs.selenium.util.MouseEvent event = null;
				try{
					// thread blocked for up to the timeout limit
					while(!done){
						event = capture.waitForClick(10);
						done = ! ((event.EVENT_BUTTON==2) ||
								   event.EVENT_CTRLKEY    ||
								   event.EVENT_ALTKEY     ||
								   event.EVENT_METAKEY    ||
								   event.EVENT_SHIFTKEY);
						if(!done) {
							IndependantLog.info("Progating captured MouseEvent. Still looking for final click.");
							event.EVENT_TYPE = "click";
							try{Thread.sleep(DocumentClickCapture.LISTENER_LOOP_DELAY * 2);}catch(Exception x){}
							WDLibrary.fireMouseEvent(event);
							dirty = true;
						}
					}
					prepClickModeUI();
					if(dirty) updatePreview();
					IndependantLog.info(event.toString());

					//check to see if the TreePanel represents a different window than the current one
					if(lastSearchedTitle != null){
						if(event.EVENT_TARGET instanceof RemoteWebElement){
							try{
								String title = ((RemoteWebElement)event.EVENT_TARGET).getWrappedDriver().getTitle();
								if(!lastSearchedTitle.equals(title)){
									stp_results.clear();
									lastSearchedTitle = null;
								}
							}catch(Exception ignore){}
						}
					}
					spc.getUtils().setWDTimeout(0);
					String rec = SearchObject.generateFullGenericXPath(event.EVENT_TARGET);

					SPCTreeNode anode = stp_results.setSelectedComponentByXpath(rec);
					String showrec = "XPATH="+rec;
					// if this node is NOT in the tree let's try to add it
					if(anode == null){
					    IndependantLog.debug("WDSPCGUI.clickMode Tree node DOES NOT exist for XPATH: "+rec);
						//DEBUG: JOptionPane.showMessageDialog(WDSPCGUI.this, "No matching Tree Node in the Tree.\nAttempting to locate WebElement.", "NO Node", JOptionPane.WARNING_MESSAGE);
					    WebElement element = SearchObject.getObject(appendFrameRS("XPATH="+rec));
						//WebElement element = SearchObject.getObject("XPATH="+rec);
						if(element != null){
						    IndependantLog.debug("WDSPCGUI.clickMode WebElement found with getObject XPATH: "+rec);
							//DEBUG: JOptionPane.showMessageDialog(WDSPCGUI.this, "WebElement found for XPATH:\n"+ rec, "Found WebElement", JOptionPane.INFORMATION_MESSAGE);
							anode = stp_results.addSPCTreeNode(element, rec, null);
							if(anode ==  null){
							    IndependantLog.debug("WDSPCGUI.clickMode COULD NOT add Tree Node for XPATH: "+rec);
								//DEBUG: JOptionPane.showMessageDialog(WDSPCGUI.this, "Tree Node WAS NOT added to the Tree.", "Tree Node Not Added", JOptionPane.WARNING_MESSAGE);
							}else{
							    IndependantLog.debug("WDSPCGUI.clickMode added Tree Node for XPATH: "+rec);
								//DEBUG: JOptionPane.showMessageDialog(WDSPCGUI.this, "Tree Node successfully added for XPATH:\n"+ rec, "Added Tree Node", JOptionPane.INFORMATION_MESSAGE);
							    stp_results.setSelectedComponentByXpath(rec);
							    if( WDSPCGUI.this.chk_short.isSelected())
							    	showrec= "XPATH=.//"+anode.xpart;
							}
						}else{
						    IndependantLog.debug("WDSPCGUI.clickMode did not find and could not make a displayable Tree Node.");
							JOptionPane.showMessageDialog(WDSPCGUI.this, "WebElement NOT FOUND for XPATH:\n"+ rec, "NO WebElement", JOptionPane.WARNING_MESSAGE);
						}
					}else{
					    IndependantLog.debug("WDSPCGUI.clickMode Tree node was found for XPATH: "+rec);
					}
					String RS = (anode==null) ? null : anode.getRecognitionString();
					if(RS==null||RS.length()==0){
						txt_rec.setForeground(Color.RED);
						txt_rec.setText(appendFrameRS(showrec));
						//txt_rec.setText(showrec);
						setStatus("RS: "+ rec, Color.RED);
					}else{
						txt_rec.setForeground(Color.BLACK);
						txt_rec.setText(appendFrameRS(RS));
						//txt_rec.setText(RS);
						setStatus("RS: "+ rec, Color.BLACK);
					}

					if(highlightSelectedNode()==null){
						highlightByWebElement(event.EVENT_TARGET);
					}
					// should automatically happen in valueChanged event handler
					//if(useClickModeProperties){
					//	showProperties(WDSPCGUI.this);
					//}
					spc.getUtils().resetWDTimeout();
				}
				catch(InterruptedException ignore_timeout){
					IndependantLog.info("Resetting Click Mode.  Click Timeout reached.");
				}
				catch(Exception x){
					x.printStackTrace();
				}
				resetClickModeUI();
			}
		};
		runner.setDaemon(true);
		runner.start();
	}

	/**
	 * @param pframe  the Frame node for all web elements.  Can be null.
	 * @param data
	 * @param xpaths
	 * @author Carl Nagle JAN 27, 2015 Added Frames support.
	 */
	public void updateData(SPCTreeNode pframe, List<WebElement> data, List<String> xpaths){
		btn_cancel.setEnabled(false);
		stp_results.setData(pframe, data, xpaths);
		updatePreview();

		setGUIForReady();

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

		@Override
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

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getSource() == img_preview){
			java.awt.Point pt = e.getPoint();
			stp_results.setSelectedComponentByPoint(new org.openqa.selenium.Point(pt.x, pt.y));
		} else if (e.getSource() == stp_results.getTree()){
			//img_preview.clearHighlight();
			//WDLibrary.clearHighlight();
			if(e.getClickCount()==2 && (spc!= null)){
				SPCTreeNode node = stp_results.getSelectedComponent();
				if(!node.xpath.equalsIgnoreCase("/Root")){
					waitPropertiesPanel(node, spc, frames);
				}
			}
		}
	}
	Vector<JFrame> frames = new Vector();
	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		Window source = arg0.getWindow();
		if(source == fra_main){
			if(spc!=null){
				spc.stopJavaHOOK();
				//spc.stopSelenium();// keep the browser running if not STOPped
			}
			JFrame f = null;
			for(int i=frames.size()-1;i>=0;i--){
				f = frames.remove(i);
				f.removeWindowListener(this);
				f.dispose();
			}
			frames.clear();
			try {
				PrintWriter pr = new PrintWriter(new FileWriter("history.dat"));
				for(int i =1; i < lines.size(); i++){
					pr.println(lines.get(i));
				}
				pr.close();
			} catch (IOException e) {
			}
		}else {
			try{ source.removeWindowListener(this);}catch(Exception ignore){}
			try{ frames.remove(source);}catch(Exception ignore){}
		}
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent arg0) {}

	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}

	public boolean insertComponentInTree(WebElement element, String fullxpath, String parentNode){
		if(useVisibleOnly()&& !element.isDisplayed()) return false;

		// Carl Nagle Frame Support
		SPCTreeNode parentTreeNode = null;
		if(parentNode != null && parentNode.length() > 0){
			parentTreeNode = stp_results.getFrameNode(parentNode);
			IndependantLog.info("WDSPCGUI.insertComponent using FRAME "+ parentTreeNode.getRecognitionString());
		}else{
			IndependantLog.info("WDSPCGUI.insertComponent using default top-level document as Frame.");
		}

		SPCTreeNode anode = stp_results.getNode(fullxpath, parentTreeNode);

		if(anode == null){
		    IndependantLog.debug("WDSPCGUI.insertComponent Tree node DOES NOT exist for XPATH: "+fullxpath);
		    String fullFrameRS = "XPATH="+fullxpath;

		    // Carl Nagle Frame Support
		    //if( parentTreeNode != null)
		    //	fullFrameRS = parentTreeNode.getRecognitionString()+ SearchObject.childSeparator + fullFrameRS;

		    WebElement e = SearchObject.getObject(fullFrameRS);
			if(e != null){
			    IndependantLog.debug("WDSPCGUI.insertComponent WebElement found with getObject XPATH: "+fullxpath);
				anode = stp_results.addSPCTreeNode(e, fullxpath, parentTreeNode);
				if(anode ==  null){
				    IndependantLog.debug("WDSPCGUI.insertComponent COULD NOT add Tree Node for XPATH: "+fullxpath);
				    return false;
				}else{
				    IndependantLog.debug("WDSPCGUI.insertComponent added Tree Node for XPATH: "+fullxpath);
				}
			}else{
			    IndependantLog.debug("WDSPCGUI.insertComponent did not find and could not make a displayable Tree Node.");
				JOptionPane.showMessageDialog(WDSPCGUI.this, "WebElement NOT FOUND for XPATH:\n"+ fullxpath, "NO WebElement", JOptionPane.WARNING_MESSAGE);
				return false;
			}
		}else{
		    IndependantLog.debug("WDSPCGUI.insertComponent Tree node already exists for XPATH: "+fullxpath);
		}
		return true;
	}

	/**
	 * Attempts to highlight both the image preview and the browser element based on
	 * the Tree node selected in the element hierarchy tree.
	 * The routine also tries to scroll the selected tree node into view on the tree panel.
	 * @return
	 */
	private SPCTreeNode highlightSelectedNode(){
		try{
			stp_results.scrollRectToVisible(stp_results.getSelectedComponentDimensions());
		}
		catch(Exception x){
			IndependantLog.error("WDSPCGUI.highlightSelectedNode "+ x.getClass().getName()+", "+x.getMessage());
		}

		SPCTreeNode node = (SPCTreeNode)stp_results.getTree().getLastSelectedPathComponent();
		if (node == null){
			IndependantLog.error("WDSPCGUI.highlightSelectedNode: The SPC tree returned no selected Node!");
			return null;
		}
		// HIGHLIGHT the component in the IMAGE PREVIEW
		img_preview.clearHighlight();
		if("none".equalsIgnoreCase(node.getCssDisplay())){
			IndependantLog.info("WDSPCGUI.highlightSelectedNode: The element's css dispaly is 'none', it is not on the page and it cannot be highlighted!");
		}else{
			img_preview.setHighlight(stp_results.getNodeDimensions(node));
		}

		//HIGHLIGHT the component on the HTML page
		SearchObject.clearHighlight();
		boolean highlighted = (spc==null)? false: spc.highlight(node);
		if(!highlighted){
			String message = "Can NOT highlight.";
			if("hidden".equalsIgnoreCase(node.getCssVisibility())){
				IndependantLog.debug("WDSPCGUI.highlightSelectedNode: The element's css visibility is 'hidden', cannot highlight it on browser!");
				message = "The element is on the page but NOT visible; It can NOT be highlighted on browser, it is ONLY highlighted on the screenshot.";
			}
			JOptionPane.showMessageDialog(this, message, "NOT HIGHLIGHTED", JOptionPane.WARNING_MESSAGE);
		}
		return node;
	}
	/**
	 *  AUG 10, 2012	(Lei Wang) During getting the Recognition String according to a node's xpath,
	 *  						 try to get property value id and name of HTML element and set them to tree node.
	 */
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		// try to make sure the selected node is visible onscreen
		SPCTreeNode node = stp_results.getSelectedComponent();
		if(node==null) return;
		String rs = node.getRecognitionString();

		//We should set the context frame to 'jcb_frames' if this node is under certain frame
		if(! findElementMode){
			//set the RECOGNITION STRING in the text box
			if(rs==null||rs.length()==0){
				txt_rec.setForeground(Color.RED);
				if(useShortStrings){
					txt_rec.setText("XPATH=.//"+node.xpart);
				}else{
					txt_rec.setText("XPATH="+node.xpath);
				}
				jcb_frames.setSelectedItem(node.getParentFrameRS());
			}else{
				txt_rec.setForeground(Color.BLACK);
				txt_rec.setText(rs);
				if(SearchObject.isValidFrameRS(rs)){
					jcb_frames.setSelectedItem(rs);
				}else if(node.frame != null && SearchObject.isValidFrameRS(node.frame.getRecognitionString())){
					jcb_frames.setSelectedItem(node.frame.getRecognitionString());
				}else{
					jcb_frames.setSelectedItem(node.getParentFrameRS());
				}
			}
		}
		highlightSelectedNode();
		// set the recognition string in the status bar -- always FULL xpath?
		if(rs==null||rs.length()==0){
			String warning = "RS NOT SET for xpath: "+node.xpath;
			IndependantLog.warn(warning);
			setStatus("XPATH RS: "+node.xpath,Color.RED);
		}else{
			setStatus("Stored RS: "+ rs,Color.BLACK);
		}
		if(useClickModeProperties){
			showProperties(WDSPCGUI.this);
		}
	}

	private static class FrameRS implements Comparable<FrameRS>{
		private String searchContext = null;
		private String frameXpath = null;
		private String frameName = null;
		private String frameID = null;
		private String frameXpathRS = null;
		private String frameNameRS = null;
		private String frameIDRS = null;

		//An empty constructor
		public FrameRS(){}

		/**
		 * NOT USED YET. Only called from addFrameRS which is NOT used yet.<br>
		 * @param frame
		 * @param xpath
		 * @param searchContext
		 */
		public FrameRS(WebElement frame, String xpath, String searchContext){
			frameXpath = xpath;
			if(frameXpath!=null && !frameXpath.trim().isEmpty())
				frameXpathRS = SearchObject.SEARCH_CRITERIA_FRAMEXPATH+SearchObject.assignSeparator+frameXpath;

			try{
				frameName = WDLibrary.getProperty(frame, Component.ATTRIBUTE_NAME);
				if(frameName!=null && !frameName.trim().isEmpty())
					frameNameRS = SearchObject.SEARCH_CRITERIA_FRAMENAME+SearchObject.assignSeparator+frameName;
			}catch(Exception ignore) {}

			try{
				frameID = WDLibrary.getProperty(frame, Component.ATTRIBUTE_ID);
				if(frameID!=null && !frameID.trim().isEmpty())
					frameIDRS = SearchObject.SEARCH_CRITERIA_FRAMEID+SearchObject.assignSeparator+frameID;
			}catch(Exception ignore) {}

			this.searchContext = searchContext;
		}

		public String getFrameXpath() {
			return frameXpath;
		}
		public String getFrameName() {
			return frameName;
		}
		public String getFrameID() {
			return frameID;
		}

		public String getToolTip(){
			StringBuffer tooltip = new StringBuffer();

			if(frameXpathRS!=null) tooltip.append(frameXpathRS+"    ");
			if(frameIDRS!=null) tooltip.append(frameIDRS+"    ");
			if(frameNameRS!=null) tooltip.append(frameNameRS+"    ");

			return tooltip.toString();
		}

		@Override
		public String toString(){
			StringBuffer frameRecognitionString = new StringBuffer();
			//append the context
			if(searchContext!=null && !searchContext.trim().isEmpty()){
				frameRecognitionString.append(searchContext);
				frameRecognitionString.append(SearchObject.childSeparator);
			}

			//append the frame's recognition string
			if(frameNameRS!=null) frameRecognitionString.append(frameNameRS);
			else if(frameIDRS!=null) frameRecognitionString.append(frameIDRS);
			else if(frameXpathRS!=null) frameRecognitionString.append(frameXpathRS);

			return frameRecognitionString.toString();
		}

		@Override
		public int compareTo(FrameRS o) {
			return toString().compareTo(o.toString());
		}
	}
	/**A HashMap cache containing a set of FrameRS according to URL as key*/
	private HashMap<String/*URL*/, Set<FrameRS>> frameRSCache = new HashMap<String, Set<FrameRS>>();

	/**
	 * According to the URL passed in, get the cached Set of FrameRS.
	 * If no Set already exists, then create a new TreeSet and put it into the cache.
	 */
	private Set<FrameRS> getFrameRSSetFromCache(String urlContext){
		//Check the url
		if(urlContext==null){
			IndependantLog.debug(StringUtils.debugmsg(false)+" the provided url is null, cannot get frameRS from cache.");
			return null;
		}
		//According to URL, get the related Frames
		Set<FrameRS> frames = frameRSCache.get(urlContext);
		if(frames==null){
			frames = new TreeSet<FrameRS>();
			frames.add(new FrameRS());
			frameRSCache.put(urlContext, frames);
		}
		return frames;
	}

	public boolean addFrameRSToCache(WebElement element, String frameXpath, String windowContext){
		Set<FrameRS> frames = getFrameRSSetFromCache(windowContext);
		if(frames == null) return false;
		for(FrameRS frame: frames){
			if(frameXpath.equals(frame.frameXpath)) return false;
		}
		frames.add(new FrameRS(element, frameXpath, windowContext));
		frameRSCache.put(windowContext, frames);
		return true;
	}

	private Set<String> getFrameRSSetFromCombo(){
		Set<String> frames = new TreeSet<String>();
		for(int i=0;i<jcb_frames.getItemCount();i++){
			String frame = jcb_frames.getItemAt(i);
			if(frame != null && frame.length()> 0) frames.add(frame);
		}
		return frames;
	}

	/**
	 * Update the frame combo-box.
	 * @param frames Set<FrameRS>, a set of FrameRS. Used to update the combo-box.
	 */
	private void updateFrameRSCombo(Set<FrameRS> frames){
		//Clear frame combo box
		jcb_frames.removeAllItems();
		jcb_frames.addItem("");
		if(frames!=null){
			//update the combo box
			for(FrameRS frame:frames){
				if(frame.frameIDRS != null && frame.frameIDRS.length()> 0){
					jcb_frames.addItem(frame.frameIDRS);
				}else if(frame.frameNameRS != null && frame.frameNameRS.length()> 0){
					jcb_frames.addItem(frame.frameNameRS);
				}else if(frame.frameXpathRS != null && frame.frameXpathRS.length()> 0){
					jcb_frames.addItem(frame.frameXpathRS);
				}
			}
		}
	}


	/**
	 * Allows the view of the non-functioning GUI only.
	 * @param args
	 */
	public static void main(String[] args){
		WDSPCGUI gui = new WDSPCGUI(null);
		gui.fra_main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setGUIForReady();
	}
}
