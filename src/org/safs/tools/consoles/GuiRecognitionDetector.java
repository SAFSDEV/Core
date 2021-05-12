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
package org.safs.tools.consoles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.safs.Log;
import org.safs.jvmagent.STAFLocalServer;
import org.safs.natives.LLMouseHook;
import org.safs.natives.LLMouseHookListener;
import org.safs.natives.MouseHook;
import org.safs.natives.win32.User32.MSLLHOOKSTRUCT;
import org.safs.tools.drivers.STAFProcessContainerDriver;
import org.safs.tools.engines.EngineInterface;

import com.sun.jna.NativeLong;

/**
 * A GUI class to detect recognition string of a GUI on screen by RIGHT CLICK on it.
 * This is for testing an engine command in a registered RFT engine or other engines.
 * 
 * <p> E, getObjectRecognitionAtScreenCoords, xpos, ypos
 * 
 * <p> It can be launched by command line:
 * <p><ul><pre>
 * java -Dsafs.project.config="c:\safs\Project\GuiDetector.ini" org.safs.tools.consoles.GuiRecognitionDetector
 * </pre></ul> 
 * 
 * <p>SAFSDetector.ini
 * <p><ul><pre>
 * [SAFS_PROJECT]
 * ProjectRoot="c:\safs\Project"
 * 
 * [SAFS_ROBOTJ]
 * AUTOLAUNCH=TRUE
 * PLAYBACK=TestScript
 * DATASTORE="c:\safs\datastorej"
 * 
 * [SAFS_ENGINES]
 * First=org.safs.tools.engines.SAFSROBOTJ
 * </pre></ul>
 * <p>
 * 
 * @author Junwu Ma
 *
 */

@SuppressWarnings("serial")
public class GuiRecognitionDetector  extends JFrame implements LLMouseHookListener{
	private LLMouseHook llmhook = null;
	
	STAFProcessContainerDriver driver = null;
	STAFLocalServer server = null;
	
	final static String labelStr = "Right Click a GUI for its verified recognition string:";
	JLabel 		label = null;     
	JTextArea  	infoText = null;  // displaying recognition string
	JTextField 	engineBar = null; // displaying registered engine
	JTextField  statusBar = null;
	
	
	//RationalTestScript script = new RationalTestScript();
	public GuiRecognitionDetector() {
		int width = 600;
		int height = 200;
		
		try{setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);}catch(Exception x){;}
		addWindowListener(new ShutdownListener());
		
		this.setTitle("GUI Recognition Detector");
		
		//north
		label = new JLabel(labelStr);
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.add(label,BorderLayout.NORTH);
		northPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
				
		//center
		infoText = new JTextArea(4, 1);
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(new JScrollPane(infoText),BorderLayout.CENTER);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		//south
		
		engineBar = new JTextField(" Registered Engine:");
		engineBar.setEditable(false); 
		engineBar.setBorder(BorderFactory.createEmptyBorder());
				
		//engineBar.setPreferredSize(getPreferredSize());
		
		statusBar = new JTextField(" Waiting... ");
		statusBar.setEditable(false); 
		statusBar.setBorder(BorderFactory.createEmptyBorder());
		statusBar.setPreferredSize(new Dimension(150,10));
		
		JPanel southPanel = new JPanel();
		southPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		southPanel.setLayout(new BorderLayout());
		//southPanel.s
		southPanel.add(engineBar,BorderLayout.WEST);
		southPanel.add(statusBar,BorderLayout.EAST);

		
		getContentPane().add(northPanel, BorderLayout.NORTH);
		getContentPane().add(centerPanel,BorderLayout.CENTER);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		
		pack();
		setSize(width,height);
		setVisible(true);
		
		init();
	}
	
	public void init(){
		//STAFProcessContainerDriver takes JVM variable safs.project.config as ini file's path
		//java -Dsafs.project.config="c:\SAFSProject\SAFSDetector.ini" org.safs.tools.consoles.GuiRecognitionDetector
		
		llmhook = new LLMouseHook();   
		if (llmhook!= null) {
			llmhook.addListener(this);
			llmhook.run();
		}
		driver = new STAFProcessContainerDriver();
		driver.initializeDriver();	
		
		server = new STAFLocalServer();
		server.launchInterface(driver);
		
		// find out registered engine
		String engineNames = "";
		ListIterator iter = driver.getEngines();
		while(iter.hasNext()){
			EngineInterface engine = (EngineInterface)iter.next();
			engineNames = engineNames + " " + engine.getEngineName();
		}
		engineBar.setText(engineBar.getText() + engineNames);
		engineBar.setSize(engineBar.getPreferredSize());
	}
	
	protected void deInit() {
		if (llmhook!= null && llmhook.isHooked())
			llmhook.stop();
		server = null;
		try {
			driver.shutdownDriver();
		} catch(Exception x) {
			Log.info("GUI recognition detector -- Driver Shutdown Exception:",x);
		}
	}
	
	public static void main(String[] args) {
		GuiRecognitionDetector appFrame = new GuiRecognitionDetector();
		appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public class ShutdownListener extends WindowAdapter {
		public void windowClosing(WindowEvent event){
			Log.info("GUI recognition detector CLOSING EVENT RECEIVED...");
			deInit();
		}
	}

	public void onLLMouseHook(int nCode,NativeLong wParam, MSLLHOOKSTRUCT info) {                
        switch(wParam.intValue()) {
            case MouseHook.WM_RBUTTONDOWN:
            	
            	statusBar.setText(" Processing in engine... ");

           	 	String rstring = server.getObjectRecognitionAtScreenCoords(info.pt.x.intValue(), info.pt.y.intValue());
           	 	
           	 	infoText.setText(rstring);
           	 	
           	 	String foundInfo = (rstring == null)? "Not found" : "Found";
           	 	label.setText(labelStr+ "  " + foundInfo + " at coordinates [" + info.pt.x.intValue() + " , " + info.pt.y.intValue() + "]");           	 	statusBar.setText(" Waiting ");
           	 	statusBar.setText(" Waiting... ");
           	 		
                break;
           default:
               break;
       }
	}


}

