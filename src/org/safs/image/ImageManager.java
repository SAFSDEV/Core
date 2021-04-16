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
package org.safs.image;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.*;

import org.safs.tools.CaseInsensitiveFile;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;

/**
 *  ImageManager is a small utility that allows us to easily capture arbitrary areas of the 
 *  screen and save them them as BMP or JPG files.
 *  <p>
 *  ImageManager allows us to set a default Project Root.  The Project Root is the root 
 *  or parent directory into which all images can be stored.  Images can be saved anywhere, 
 *  but the File dialogs will use the Project Root as your current directory when prompting 
 *  you for a image name and save location.  You would typically save images to this root 
 *  directory or one of its subdirectories.
 *  <p>
 *  With ImageManager running, you can start the image capture process in two ways.
 *  <p>
 *  <ul>
 *  <li><b>CTRL mode</b>
 *  <p>
 *  The Image Manager window is expected to remain the Active window.<br>  
 *  This mode can be used to capture arbitrary onscreen images when the item 
 *  or Window containing the item does not need to become the active window.
 *  <p>
 *  <ol>
 *  <li> Press the CTRL GO button to enter CTRL mode.
 *  <p>
 *  <li> Move the mouse cursor to the area to capture.
 *  <p>
 *  <li> Press and hold the CTRL key while moving the mouse to define the area to capture.<br>
 *     Note: you do not Click any of the buttons on the mouse.<br>
 *     If you do, this will deactivate the CTRL mode.<br>
 *  <p>
 *  <li> You will notice that Image Manager is tracking the rectangle size.
 *  <p>
 *  <li> Release the CTRL key when you are done defining the area.
 *  <p>
 *  <li> You will be prompted to save or discard the image you have captured.
 *  </ol>
 *  <p>
 *  <li><b>FOCUS mode</b>
 *  <p>
 *  The item or Window containing the item must become the Active window.<br>
 *  Image Manager must track the mouse when it is NOT the active window.  
 *  <p>
 *  <ol>
 *  <li> Press the FOCUS GO button to enter FOCUS mode.
 *  <p>
 *  <li> Move the mouse cursor to the screen point to start the capture.
 *  <p>
 *  <li> Click on the point to start the capture or cycle through ALT+TAB to switch 
 *  to the application giving it focus.  Either way, when Image Manager is 
 *  no longer the active window it is monitoring the rectangle to capture.
 *  <p>
 *  <li> You will notice that Image Manager is tracking the rectangle size.
 *  <p>
 *  <li> Move the cursor to the screen point that completes the rectangle 
 *  you wish to capture.  Hold the cursor there during the next step.
 *  <p>
 *  <li> Press or cycle through ALT+TAB to make Image Manager the active Window.
 *  <p>
 *  <li> You will be prompted to save or discard the image you have captured.
 *  </ol>
 *  </ul>
 *  <p>
 *  ImageManager can accept one argument in its constructor or from the Java command-line:
 *  <p><ul>
 *     prjroot -- A valid directory to use as the Project Root.
 *  </ul><p>
 *  From inside a Java application:
 *  <p><ul>
 *  import org.safs.image.ImageManager;<br>
 *  ...<br>
 *  ImageManager im = new ImageManager("C:\SAFSProject\images");<br>
 *  </ul>
 *  <p>
 *  From the Java command-line:
 *  <p>
 *  <ul>
 *  java org.safs.image.ImageManager "C:\SAFSProject\images"
 *  </ul>
 *  <p>
 *  ImageManager requires Java 1.5 or higher.
 * @author Carl Nagle
 */
public class ImageManager extends JFrame implements ActionListener, KeyListener, WindowFocusListener{

	//String infoprefix = "Coords=";
	String info_unknown = "No RECT";
	String _focus_unknown = "Press GO Mode for Capture";

	String _focus_capture = "Hold CTRL to Start Rect";
	String _focus_release = "Release CTRL to End Rect";
	
	String _focus_swapon = "ALT+TAB or Click Screen to Start Rect";
	String _focus_swapoff = "ALT+TAB to Image Manager to End Rect";
	
	JLabel project_label = new JLabel("Project Root: ");
	JButton project_browse = new JButton("Browse to Project Root...");
	JTextField path = new JTextField("Project Root Dir");
	JLabel _info = new JLabel(info_unknown);
		
	JButton _start = new JButton("CTRL GO");
	JButton _altstart = new JButton("FOCUS GO");
	JButton _stop = new JButton("STOP");
	
	JTextField _focus = new JTextField(_focus_unknown);
	
	Robot robot = null;
	BufferedImage image = null;

	boolean ctrl_mode = true;
	boolean running = false;
	boolean rect_started = false;
	
	Point start_rect = null;
	Point end_rect = null;
	Rectangle rect = null;

	MyThread altrect = null;
	
	/**
	 * Default constructor trying to set C:\SAFS\Project as the initial Project Root.
	 * @throws HeadlessException
	 */
	public ImageManager() throws HeadlessException {
		// TODO Auto-generated constructor stub
		this("C:\\SAFS\\Project");
	}
	
	protected ImageManager(GraphicsConfiguration arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor trying to set an alternate Project Root at startup.
	 * If the directory is determined to be invalid or does not have write permissions 
	 * then the User's Current Working Directory is used as the Project Root.
	 * 
	 * @param prjroot -- full path to a valid directory to serve as the Project Root.
	 * @throws HeadlessException
	 */
	public ImageManager(String prjroot) throws HeadlessException {
		super("SAFS Image Manager");
		Dimension framesize = new Dimension(280,140); 
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(framesize);
		this.addWindowFocusListener(this);
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints gc = new GridBagConstraints();
		this.getContentPane().setLayout(gb);
		
		gc.gridwidth=1;
		gb.setConstraints(project_label, gc);		
		this.getContentPane().add(project_label);

		gc.weightx = 1.0;
		gc.gridwidth = gc.REMAINDER;		
		gb.setConstraints(project_browse, gc);		
		this.getContentPane().add(project_browse);
		project_browse.addActionListener(this);

		File r = new CaseInsensitiveFile(prjroot).toFile();
		if(r.isDirectory()&& r.canWrite()) {
			path.setText(r.getAbsolutePath());
		}else{
			String p = System.getProperty("user.dir");
			if(p!=null){
				path.setText(p);
			}
		}
		
		gc.fill = gc.HORIZONTAL;
		gc.gridwidth = gc.REMAINDER;		
		gb.setConstraints(path, gc);		
		this.getContentPane().add(path);
		path.setEditable(false);

		gb.setConstraints(_info, gc);
		this.getContentPane().add(_info);
		
		gc.gridwidth = 1;
		gb.setConstraints(_start, gc);
		this.getContentPane().add(_start);

		gb.setConstraints(_altstart, gc);
		this.getContentPane().add(_altstart);
		
		gc.gridwidth = gc.REMAINDER;
		gb.setConstraints(_stop, gc);		
		this.getContentPane().add(_stop);

		gb.setConstraints(_focus, gc);		
		this.getContentPane().add(_focus);

		_start.addActionListener(this);
		_altstart.addActionListener(this);
		_stop.addActionListener(this);
		this.setVisible(true);
	}

	protected ImageManager(String arg0, GraphicsConfiguration arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	protected void startCaptureRect(){
		PointerInfo pointer = MouseInfo.getPointerInfo();
		if(!rect_started){
			start_rect = pointer.getLocation();
			end_rect = pointer.getLocation();
			rect_started = true;
		}else{
			end_rect = pointer.getLocation();
		}
		String prefix = "SIZE: w=";
		if(end_rect.x < start_rect.x || end_rect.y < start_rect.y){
			prefix = "~SIZE: w=";
		}
		_info.setText(prefix + Math.abs(end_rect.x - start_rect.x +1)+", h="+ Math.abs(end_rect.y-start_rect.y +1));
	}
	protected void stopCaptureRect(){
		PointerInfo pointer = MouseInfo.getPointerInfo();
		end_rect = pointer.getLocation();
		rect_started = false;
		if(end_rect.x < start_rect.x || end_rect.y < start_rect.y){
			Point s = new Point();
			Point e = new Point();
			if(end_rect.x < start_rect.x){
				s.x = end_rect.x;
				e.x = start_rect.x;
			}else{
				e.x = end_rect.x;
				s.x = start_rect.x;
			}
			if(end_rect.y < start_rect.y){
				s.y = end_rect.y;
				e.y = start_rect.y;
			}else{
				e.y = end_rect.y;
				s.y = start_rect.y;
			}
			start_rect = s;
			end_rect = e;			
		}
		rect = new Rectangle(start_rect.x, start_rect.y, end_rect.x - start_rect.x + 1, end_rect.y - start_rect.y + 1);
		_info.setText("RECT: x="+ rect.x +", y="+ rect.y+", w="+ rect.width+", h="+ rect.height);
		promptSaveSnapshot();
	}

	protected void promptSaveSnapshot(){
		if(rect==null) return;
		try{
			if(robot == null) robot = new Robot();
			if (ctrl_mode) {
				image = robot.createScreenCapture(rect);
			}
			
			if(rect.width <= 6 && rect.height <= 6){
				stopCaptureEvents();
				if(ctrl_mode) startCaptureEvents();
				ctrl_mode = true;
				return;
			}
			if((!ctrl_mode)&&(image != null)){
				image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
			}
			ctrl_mode = true;
			stopCaptureEvents();
			
			Canvas canvas = new MyCanvas(image);
			canvas.setVisible(true);
			int option = JOptionPane.YES_OPTION;
			while(option == JOptionPane.YES_OPTION){
			    option = JOptionPane.showConfirmDialog(this, 
					new Object[]{ "Save this image?", canvas },
					"Save Image",
					JOptionPane.YES_NO_OPTION);
				if(option==JOptionPane.YES_OPTION){
					FileDialog saver = new FileDialog(this, "Save Image", FileDialog.SAVE);
					saver.setDirectory(path.getText());
					saver.setVisible(true);
					String dirpath = saver.getDirectory();
					String filepath = saver.getFile();
					if(filepath!=null){
						filepath = dirpath + filepath;
						try{
							ImageUtils.saveImageToFile(image, new File(filepath));
							option = JOptionPane.NO_OPTION;
						}catch(Throwable t){
							String msg = "Error writing to file:\n"+filepath+"\n\n"+
							t.getMessage();
							JOptionPane.showMessageDialog(this, msg, "Save Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		}catch(AWTException x){
			String msg = "Cannot use screen capture mechanism. Cause: "+x.getClass().getSimpleName();
			JOptionPane.showMessageDialog(this, msg, "Image Capture Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/* KeyListener API */
	public void keyPressed(KeyEvent e){
		if(e.getKeyCode()==e.VK_CONTROL){
			_focus.setText(_focus_release);
			startCaptureRect();
		}
	}
	/* KeyListener API */
	public void keyReleased(KeyEvent e){
		if(e.getKeyCode()==e.VK_CONTROL){
			_focus.setText(_focus_capture);
			stopCaptureRect();
		}
	}
	/* KeyListener API */
	public void keyTyped(KeyEvent e){
		
	}
	
	/* WindowFocusListener API */
	public void windowGainedFocus(WindowEvent e){
		if(!ctrl_mode){
			// handle alt_mode finish capture
			if((altrect != null)&&(altrect.isAlive())){
				altrect.stopThread();
				altrect = null;
			}
			stopCaptureRect();
			//stopCaptureEvents();
			_focus.requestFocusInWindow();
		}else{
			_focus.requestFocusInWindow();
		}
	}

	/* WindowFocusListener API */
	public void windowLostFocus(WindowEvent e){
		if(ctrl_mode)
			stopCaptureEvents();
		else{
			_focus.setText(_focus_swapoff);
			startCaptureRect();
			//might need to wait 100msec or so
			try{Thread.sleep(100);}catch(InterruptedException x){}
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			image = robot.createScreenCapture(new Rectangle(0,0, size.width, size.height));
			altrect = new MyThread();
			altrect.start();
		}
	}

	protected void startAltCaptureEvents(){
		if(!running){
			try{
				if(robot==null) robot = new Robot();
				_focus.setText(_focus_swapon);
				_start.setEnabled(false);
				_altstart.setEnabled(false);
				rect_started = false;
				running = true;
				ctrl_mode = false;
			}catch(AWTException x){
				String msg = "Cannot use screen capture mechanism. Cause: "+x.getClass().getSimpleName();
				JOptionPane.showMessageDialog(this, msg, "Image Capture Error", JOptionPane.ERROR_MESSAGE);
				stopCaptureEvents();
				ctrl_mode = true;
				_focus.requestFocusInWindow();
			}
		}
	}

	protected void startCaptureEvents(){
		if(!running){
			_focus.setText(_focus_capture);
			_focus.addKeyListener(this);
			_start.setEnabled(false);
			rect_started = false;
			_focus.requestFocusInWindow();
			running = true;
			ctrl_mode = true;
		}
	}

	protected void stopCaptureEvents(){
		_focus.setText(_focus_unknown);
		_info.setText(info_unknown);
		_focus.removeKeyListener(this);
		_start.setEnabled(true);
		_altstart.setEnabled(true);
		rect_started = false;
		running = false;
	}
	
	/* ActionListener API */
	public void actionPerformed(ActionEvent event){
		Object source = event.getSource();
		if(source.equals(_start)){
			startCaptureEvents();
		}else if(source.equals(_stop)){
			stopCaptureEvents();
		}else if(source.equals(project_browse)){
			JFileChooser prompt = new JFileChooser(path.getText());
			prompt.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int f = prompt.showDialog(this, "Set Root");
			if(f == JFileChooser.APPROVE_OPTION){
				path.setText(prompt.getSelectedFile().getAbsolutePath());
			}else{
				String msg = "No change to Project Root directory.";
				JOptionPane.showMessageDialog(this, msg, "Project Root Warning", JOptionPane.WARNING_MESSAGE);				
			}
		}else if(source.equals(_altstart)){
			startAltCaptureEvents();
		}
	}
	
	/**
	 * Class used internally to present the screen captured image in the Save As dialog.
	 * @author Carl Nagle
	 */
	public class MyCanvas extends Canvas {
		BufferedImage image = null;
		public MyCanvas(BufferedImage image){
			this.image = image;
			setSize(image.getWidth(), image.getHeight());
		}
		public void paint(Graphics g){
			g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), Color.BLACK, this);
		}
	}
	
	public class MyThread extends Thread {
		boolean stopped = false;
		public void run(){
			while(!stopped){
				startCaptureRect();
				try{sleep(200);}catch(InterruptedException x){}
			}
		}
		protected void stopThread(){
			stopped=true;
			interrupt();
		}
	}
	
	/**
	 * Accepts one optional argument -- the path to a valid Project Root directory.
	 * If the path contains spaces then it should be enclosed in double-quotes.
	 * <p>
	 *  From the Java command-line:
	 *  <p>
	 *  <ul>
	 *  java org.safs.image.ImageManager "C:\SAFSProject\images"
	 *  </ul>
	 *  
	 * @param args String[] of which only arg[0] will be used.
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImageManager manager = null;
		if(args.length == 0){
			manager = new ImageManager();
		}else{
			manager = new ImageManager(args[0]);
		}
	}
}
