package org.safs.image;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.safs.Log;
import org.safs.natives.LLKeyboardHook;
import org.safs.natives.LLKeyboardHookListener;
import org.safs.natives.LLMouseHook;
import org.safs.natives.LLMouseHookListener;
import org.safs.natives.MSKeyEvent;
import org.safs.natives.win32.User32.KBDLLHOOKSTRUCT;
import org.safs.natives.win32.User32.MSLLHOOKSTRUCT;

import com.sun.jna.NativeLong;

/**
 * This is a simple program to manage different kinds of images.<br>
 * It can provide functionalities as:<br>
 * <ol>
 * <li>Capture screen image. It uses Low Level mouse and key hook. Only works on MS Windows now.
 * <li>Select part of image to cut, copy and paste.
 * </ol>
 * 
 * @author Lei Wang  NOV 21, 2010
 *
 * <br>	Dec 27, 2010	(LeiWang)	Add undo(Ctrl+Z),redo(Ctrl+Y), <br>
 *                                  add some image filter operations like sharpen, blur etc. <br>
 */

public class ImageManager2 extends JFrame{
	
	public static final String newline = System.getProperty("line.separator");
	public static final String MENU_FILE_NAME 		= "File";
	public static final String MENU_EDIT_NAME 		= "Edit";
	public static final String MENU_IMAGE_NAME 		= "Image";
	public static final String MENU_HELP_NAME 		= "Help";
	
	public static final String OPEN_ACTION_NAME 		= "Open";
	public static final String SAVE_ACTION_NAME 		= "Save";
	public static final String EXIT_ACTION_NAME 		= "Exit";
	public static final String CAPTURE_ACTION_NAME 		= "Capture";
	public static final String SELECT_ACTION_NAME 		= "Select";
	public static final String CUT_ACTION_NAME 			= "Cut";
	public static final String COPY_ACTION_NAME 		= "Copy";
	public static final String PASTE_ACTION_NAME 		= "Paste";
	public static final String SETTING_ACTION_NAME 		= "Setting";
	public static final String ABOUT_ACTION_NAME 		= "About";
	public static final String USAGE_ACTION_NAME 		= "Usage";
	public static final String ROOT_CHOOSE_ACTION_NAME 	= "root";
	public static final String REDO_ACTION_NAME 		= "Redo";
	public static final String UNDO_ACTION_NAME 		= "Undo";

	public static final String IMG_BLUR_ACTION_NAME 		= "Blur";
	public static final String IMG_SHARPEN_ACTION_NAME 		= "Sharpen";
	public static final String IMG_EDGE_ACTION_NAME 		= "Edge";
	public static final String IMG_NEGATIVE_ACTION_NAME 	= "Negative";
	
	public static final String ZOOM_SLIDER_NAME 		= "zoom";
	
	public static final String SMALL_ICON_SUFFIX 		= ".png";
	public static final String SMALL_ICON_PREFIX 		= "icons/";
	
	public static final String FRAME_TITLE	 			= "SAFS Image Manager";
	
	public static final int FRAME_WIDTH	= 600;
	public static final int FRAME_HEIGHT	= 400;
	public static final int iconW=25, iconH=25, btnW=60, btnH=25, textW=FRAME_WIDTH/3, textH=25;
	
	private String rootDir = "C:/SAFS/Project/";
	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	private Clipboard clipboard = toolkit.getSystemClipboard();
	private JFileChooser fileChooser = null;
	
	/**
	 * These actions will be used for both tool bar button and menu item<br>
	 * When enable or disable a button or menu item, you MUST enable or <br>
	 * disable these action objects.<br>
	 */
	private JButton openBtn = null;
	private Action  openAction = null;//Can be shared by JButton and JMenuItem etc.
	private JToggleButton captureBtn = null;
	private Action  captureAction = null;//Can be shared by JButton and JMenuItem etc.
	private JButton saveBtn	= null;
	private Action  saveAction = null;//Can be shared by JButton and JMenuItem etc.
	private JToggleButton selectBtn = null;
	private Action  selectAction = null;//Can be shared by JButton and JMenuItem etc.
	
	private JButton cutBtn = null;
	private Action cutAction = null;
	private JButton copyBtn = null;
	private Action copyAction = null;
	private JButton pasteBtn = null;
	private Action pasteAction = null;
	private JButton redoBtn = null;
	private Action redoAction = null;
	private JButton undoBtn = null;
	private Action undoAction = null;
	
	private Action exitAction = null;
	private Action settingAction = null;
	private Action aboutAction = null;
	private Action usageAction = null;
	
	private Action imgBlurAction = null;
	private Action imgSharpenAction = null;
	private Action imgEdgeAction = null;
	private Action imgNegativeAction = null;
	
	private ZoomSlider zoomSld = null;
	private JToolBar toolBar = null;
	private JMenuBar menuBar = null;
	
	private ImagePanel imagePanel = null;
	private StatusBar status = null;
	private SettingPanel settingPanel = null;
	
	private Handler handler = null;
	private LLKeyboardHook keyboardHook = new LLKeyboardHook();
	private LLMouseHook mouseHook = new LLMouseHook();
	private BufferedImage originalImage = null;
	
	public ImageManager2(){
		
		initButtons();
		zoomSld = new ZoomSlider();
		imagePanel = new ImagePanel(FRAME_WIDTH/2,FRAME_HEIGHT/2);
		status = new StatusBar(imagePanel);
		toolBar = new JToolBar();
		menuBar = new JMenuBar();
		settingPanel = new SettingPanel(this);
		
		initMenuBar();
		initShortCuts(imagePanel);
		arrageComponents();
		addListeners();
		
//		new PastableImageChecker().start();
		
		//Set ImagePanel border
//		imagePanel.setBorder(BorderFactory.createLineBorder(Color.RED));
		
		//Set JFrame properties
		this.setTitle(FRAME_TITLE);
		this.setPreferredSize(new Dimension(FRAME_WIDTH,FRAME_HEIGHT));			
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
		this.setExtendedState(MAXIMIZED_BOTH);
	}
	
	private void initMenuBar(){
		JMenu file = new JMenu(createAction(MENU_FILE_NAME,"",KeyEvent.VK_F));
		JMenu edit = new JMenu(createAction(MENU_EDIT_NAME,"",KeyEvent.VK_E));
		JMenu image = new JMenu(createAction(MENU_IMAGE_NAME,"",KeyEvent.VK_I));
		JMenu help = new JMenu(createAction(MENU_HELP_NAME,"",KeyEvent.VK_H));
		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(image);
		menuBar.add(help);
		
		file.add(openAction);
		file.add(saveAction);
		file.addSeparator();
		file.add(exitAction);
		
		edit.add(redoAction);
		edit.add(undoAction);
		edit.addSeparator();
		edit.add(copyAction);
		edit.add(cutAction);
		edit.add(pasteAction);
		edit.addSeparator();
		edit.add(settingAction);
		
		image.add(imgBlurAction);
		image.add(imgEdgeAction);
		image.add(imgNegativeAction);
		image.add(imgSharpenAction);
		
		help.add(aboutAction);
		help.add(usageAction);
	}
	
	private void arrageComponents(){
		//Set toolBarPanel: toolBar, textField, rootChozBtn etc
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		
		toolBar.add(captureBtn);
		toolBar.add(selectBtn);
		toolBar.add(openBtn);
		toolBar.add(saveBtn);
		toolBar.add(cutBtn);
		toolBar.add(copyBtn);
		toolBar.add(pasteBtn);
		toolBar.add(new JSeparator(JSeparator.VERTICAL));
		toolBar.add(zoomSld);

		toolBarPanel.add(toolBar);
		
//		rootDirField = new JTextField(rootDir);
//		rootDirField.setPreferredSize(new Dimension(textW,textH));
//		rootDirField.setEditable(false);
//		toolBarPanel.add(rootDirField);
//		
//		toolBarPanel.add(rootChozBtn);
		
		this.setJMenuBar(menuBar);
		
		getContentPane().add(toolBarPanel, BorderLayout.NORTH);
		
		getContentPane().add(new JScrollPane(imagePanel), BorderLayout.CENTER);
		
		getContentPane().add(status, BorderLayout.SOUTH);
	}
	
	private void addListeners(){
		//If property's change will affect the status bar, add 'status'
		//as propertyChangeListener
		//some properties like "selectable","image","selectedRect" and "draggingRect"
		//will affect the status bar's information
		imagePanel.addPropertyChangeListener(status);
		//If property's change will affect the actions, it will be handled
		//in listener got from getPCListener()
		//properties like "selected" or "image" will affect the actions
		imagePanel.addPropertyChangeListener(getPCListener());

		clipboard.addFlavorListener(new FlavorListener(){
			public void flavorsChanged(FlavorEvent e) {
				Log.debug("Clipboard flavor changed!!!");
				if(hasPastableImage()){
					if(!pasteAction.isEnabled())
						pasteAction.setEnabled(true);
				}else{
					if(pasteAction.isEnabled())
						pasteAction.setEnabled(false);					
				}
			}
		});
		
		//Set Keyboard Hook Listener
		keyboardHook.addListener(getLLKListener());
		mouseHook.addListener(getLLMListener());
//		keyboardHook.run();
//		mouseHook.run();
	}
	
	/**
	 * This method will create some new buttons and actions<br>
	 * These actions will be shared by the menu item<br>
	 */
	private void initButtons(){
		openBtn = (JButton) initButton(new JButton(),OPEN_ACTION_NAME, "Open Image ...", KeyEvent.VK_O,true);
		openAction = openBtn.getAction();
		
		captureBtn = (JToggleButton) initButton(new JToggleButton(),CAPTURE_ACTION_NAME, "Capture Image ...", KeyEvent.VK_C,true);
		captureAction = captureBtn.getAction();
		
		saveBtn = (JButton) initButton(new JButton(),SAVE_ACTION_NAME, "Save Image ...", KeyEvent.VK_S,true);
		saveAction = saveBtn.getAction();
		saveAction.setEnabled(false);
		
		selectBtn = (JToggleButton) initButton(new JToggleButton(),SELECT_ACTION_NAME, "Select Image ...", KeyEvent.VK_L,true);
		selectAction = selectBtn.getAction();
		selectAction.setEnabled(false);
		
		cutBtn = (JButton) initButton(new JButton(),CUT_ACTION_NAME,"Cut Selected Image ...",KeyEvent.VK_T,true);
		cutAction = cutBtn.getAction();
		cutAction.setEnabled(false);
		
		copyBtn = (JButton) initButton(new JButton(),COPY_ACTION_NAME,"Copy Selected Image ...",KeyEvent.VK_C,true);
		copyAction = copyBtn.getAction();
		copyAction.setEnabled(false);
		
		pasteBtn = (JButton) initButton(new JButton(),PASTE_ACTION_NAME,"Paste Selected Image ...",KeyEvent.VK_P,true);
		pasteAction = pasteBtn.getAction();
		pasteAction.setEnabled(hasPastableImage());

		redoBtn = (JButton) initButton(new JButton(),REDO_ACTION_NAME,"Redo ...",KeyEvent.VK_R,true);
		redoAction = redoBtn.getAction();
		redoAction.setEnabled(false);
		
		undoBtn = (JButton) initButton(new JButton(),UNDO_ACTION_NAME,"Undo ...",KeyEvent.VK_U,true);
		undoAction = undoBtn.getAction();
		undoAction.setEnabled(false);
		
		//Create other actions for menuitems
		exitAction = createAction(EXIT_ACTION_NAME,"Exit program ...",KeyEvent.VK_E);
		settingAction = createAction(SETTING_ACTION_NAME,"Setting ...", KeyEvent.VK_G);
		aboutAction = createAction(ABOUT_ACTION_NAME,"About ...",KeyEvent.VK_A);
		usageAction = createAction(USAGE_ACTION_NAME,"Usage ...",KeyEvent.VK_U);
		
		imgNegativeAction = createAction(IMG_NEGATIVE_ACTION_NAME, "Negative image ...", KeyEvent.VK_N);
		imgBlurAction = createAction(IMG_BLUR_ACTION_NAME, "Blur image ...", KeyEvent.VK_B);
		imgSharpenAction = createAction(IMG_SHARPEN_ACTION_NAME, "Sharpen image ...", KeyEvent.VK_P);
		imgEdgeAction = createAction(IMG_EDGE_ACTION_NAME, "Mark edge image ...", KeyEvent.VK_E);
		
		imgNegativeAction.setEnabled(false);
		imgBlurAction.setEnabled(false);
		imgSharpenAction.setEnabled(false);
		imgEdgeAction.setEnabled(false);
	}
	
	/**
	 * This should be called after invocation of {@link #initialButtons()}<br>
	 * After the action objects have been created.<br>
	 * @param imagePanel
	 */
	private void initShortCuts(ImagePanel imagePanel){
		InputMap inputMap = imagePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = imagePanel.getActionMap();
		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), COPY_ACTION_NAME);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), CUT_ACTION_NAME);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), PASTE_ACTION_NAME);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), SAVE_ACTION_NAME);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), UNDO_ACTION_NAME);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK), REDO_ACTION_NAME);
//		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), SELECT_ACTION_NAME);
		
		actionMap.put(COPY_ACTION_NAME, copyAction);
		actionMap.put(CUT_ACTION_NAME, cutAction);
		actionMap.put(PASTE_ACTION_NAME, pasteAction);
		actionMap.put(SAVE_ACTION_NAME, saveAction);
		actionMap.put(UNDO_ACTION_NAME, undoAction);
		actionMap.put(REDO_ACTION_NAME, redoAction);
//		actionMap.put(COPY_ACTION_NAME, copyAction);
	}
	
	private AbstractButton initButton(AbstractButton button, String name, String tooltips, int mnemonic, boolean onlyShowIcon){
		Action action = createAction(name, tooltips, mnemonic);
		
		if(action.getValue(Action.NAME)!=null && !onlyShowIcon){
			if(action.getValue(Action.SMALL_ICON)!=null){
				button.setPreferredSize(new Dimension(btnW+iconW,btnH));
			}else{
				button.setPreferredSize(new Dimension(btnW,btnH));				
			}
		}else if(action.getValue(Action.SMALL_ICON)!=null){
			button.setPreferredSize(new Dimension(iconW,iconH));			
		}else{//No Name, No Icon
			button.setPreferredSize(new Dimension(btnW,btnH));
		}
		
		button.setAction(action);
		if(onlyShowIcon && action.getValue(Action.SMALL_ICON)!=null){
			button.setText(null);
		}
		
		return button;
	}
	
	/**
	 * This method will create a button and create an Action for this button
	 * @param name
	 * @param tooltips
	 * @param mnemonic
	 * @return
	 */
	private Action createAction(final String name, String tooltips, int mnemonic){
		String iconImageName = SMALL_ICON_PREFIX+name+SMALL_ICON_SUFFIX;
		//Please fill the method actionPerformed according to the button name
		Action action = null;
		
		if(name.equals(OPEN_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					openImageFile();
				}
			};
		}else if(name.equals(SAVE_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					saveImageToFile(imagePanel.getImage());
				}
			};
		}else if(name.equals(CAPTURE_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					if(captureBtn!=null){
						if(keyboardHook!=null){
							if(captureBtn.isSelected()){
								if(!keyboardHook.isHooked())
									keyboardHook.run();
								status.setCapInfo(StatusBar.STATUS_INFO_CAPTURE);
							}else{
								if(keyboardHook.isHooked())
									keyboardHook.stop();
								status.setCapInfo("");
							}
						}else{
							Log.warn("IM2: Low Level Key Hook is null.");
						}
						if(mouseHook!=null){
							if(captureBtn.isSelected()){
								if(!mouseHook.isHooked())
									mouseHook.run();
							}else{
								if(mouseHook.isHooked())
									mouseHook.stop();
							}
						}else{
							Log.warn("IM2: Low Level Mouse Hook is null.");
						}
					}
				}
			};
		}else if(name.equals(SELECT_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					//If the select button is pushed down, we may use the Ctrl+x, Ctrl+c, Ctrl+v
					//to copy cut paste image, we will stop the low level mouse and keyboard hook
					if(captureBtn!=null){
						if(selectBtn.isSelected()){
							imagePanel.setSelectable(true);
							captureBtn.setEnabled(false);
							stopHooks();
						}else{
							imagePanel.setSelectable(false);
							if(!captureBtn.isEnabled()){
								captureBtn.setEnabled(true);
								if(captureBtn.isSelected()){
									startHooks();
								}
							}
						}
					}
				}
			};
		}else if(name.equals(CUT_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					//Save the BufferedImage to the clip board
					saveImageToClipboard(imagePanel.copyOrCutSelectedImage(true));
				}
			};
		}else if(name.equals(COPY_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					saveImageToClipboard(imagePanel.copyOrCutSelectedImage(false));
				}
			};
		}else if(name.equals(PASTE_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					pasteImageToImagePanel();
				}
			};
		}else if(name.equals(EXIT_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					stopHooks();
					System.exit(0);
				}
			};
		}else if(name.equals(SETTING_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					JDialog settingDialog = new JOptionPane().createDialog(imagePanel,"Setting ...");
					settingDialog.setResizable(true);
					settingDialog.setPreferredSize(new Dimension(400,300));
					settingDialog.setContentPane(settingPanel);
					settingPanel.setParentDialog(settingDialog);
					
					settingDialog.pack();
					settingDialog.setVisible(true);
				}
			};
		}else if(name.equals(ABOUT_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(imagePanel, "SAFS Image Manager.");
				}
			};
		}else if(name.equals(USAGE_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					StringBuffer usageMessage = new StringBuffer();
					usageMessage.append("1. Capture image from screen."+newline);
					usageMessage.append("   Click toolbar's camera icon to start capture."+newline);
					usageMessage.append("   Put the mouse to the start point."+newline);
					usageMessage.append("   Press the Alt key on your keyborad."+newline);
					usageMessage.append("   Move the mouse the the end point."+newline);
					usageMessage.append("   Relase the Alt key."+newline);
					usageMessage.append("2. Select part of the captured image."+newline);
					usageMessage.append("   Click toolbar's select icon to select part of image to edit."+newline);
					JOptionPane.showMessageDialog(imagePanel, usageMessage.toString());
				}
			};
		}else if(name.equals(REDO_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					imagePanel.redo();
				}
			};
		}else if(name.equals(UNDO_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					imagePanel.undo();
				}
			};
		}else if(name.equals(IMG_BLUR_ACTION_NAME) ||
				name.equals(IMG_EDGE_ACTION_NAME) ||
				name.equals(IMG_NEGATIVE_ACTION_NAME) ||
				name.equals(IMG_SHARPEN_ACTION_NAME)){
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					imagePanel.filterImage(name);
				}
			};
		}else{
			action = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					//TODO add new action here
				}
			};
		}
		
		action.putValue(Action.SHORT_DESCRIPTION, tooltips);
		action.putValue(Action.NAME, name);
		if(mnemonic>0){
			action.putValue(Action.MNEMONIC_KEY, mnemonic);
		}
		
		try {
			URL imageUrl = getClass().getResource(iconImageName);
			if(imageUrl!=null){
				Log.debug("IM2.createAction(): "+imageUrl.getPath());
				action.putValue(Action.SMALL_ICON, new ImageIcon(imageUrl));				
			}else{
				Log.warn("IM2.createAction(): "+"can not find icon "+iconImageName);
			}
		} catch (Exception e) {
			Log.error("IM2.createAction(): Exception: "+e.getMessage()+" Can not load icon image.");
		}

		return action;
	}

	
	private void openImageFile(){
		JFileChooser jfc = getFileChooser();
		int rc = jfc.showOpenDialog(this);
		if(rc==JFileChooser.APPROVE_OPTION){
			try {
				BufferedImage bufImg = ImageUtils.getStoredImage(jfc.getSelectedFile().getAbsolutePath());
				Log.debug("IM2.openImageFile(): opening image type is "+bufImg.getType());
				imagePanel.setFirstImage(bufImg);
			} catch (Exception e) {
				Log.error("IM2.openImageFile(): Exception " + e.getMessage());
			}
		}
	}
	
	private void saveImageToFile(BufferedImage image){
		JFileChooser jfc = getFileChooser();
		int rc = jfc.showSaveDialog(this);
		if(rc==JFileChooser.APPROVE_OPTION){
			try {
				ImageUtils.saveImageToFile(image, jfc.getSelectedFile());
			} catch (Exception e) {
				Log.error("IM2.saveImageToFile(): Exception " + e.getMessage());
			}
		}
	}
	
	private JFileChooser getFileChooser(){
		if(fileChooser==null){
			fileChooser = new JFileChooser(rootDir) {
				public void approveSelection() {
					String filename = getSelectedFile().getName();
					if (ImageUtils.isImageFormatSupported(filename)) {
						super.approveSelection();
					} else {
//						JDialog warningDialog = new JDialog(ImageManager2.this,
//								"File format error!!!", true);
//						JLabel warningLabel = new JLabel(
//								"File suffix does NOT match supported image format!!! ");
//						warningLabel.setForeground(Color.RED);
//						warningDialog.setContentPane(warningLabel);
//						Point location = this.getLocation();
//						location.translate(getWidth() / 2, getHeight() / 2);
//						warningDialog.setLocation(location);
//						warningDialog.pack();
//						warningDialog.setVisible(true);
						
						JOptionPane.showMessageDialog(this, "File suffix does NOT match supported image format!!!",
								                      "Error Format", JOptionPane.ERROR_MESSAGE);
						Log.error("IM2: Format not match!! : Do NOT approve "+ filename);
					}
				}
			};
			fileChooser.setFileFilter(ImageUtils.getImageFileFilter());
		}else{
			fileChooser.setCurrentDirectory(new File(rootDir));
		}
		
		return  fileChooser;
	}
	
	
	
	public String getRootDir() {
		return rootDir;
	}

	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}

	public ImagePanel getImagePanel() {
		return imagePanel;
	}

	public int getMaxUndoableSteps(){
		String debugmsg = this.getClass().getName()+".getMaxUndoableSteps(): ";
		
		int maxUndoableSteps = -1;
		
		if(imagePanel!=null){
			RedoUndoList list = imagePanel.getRedoUndoList();
			if(list!=null)
				maxUndoableSteps = list.getMaxSize();
		}
		Log.debug(debugmsg+" maxUndoableSteps="+maxUndoableSteps);
		
		return maxUndoableSteps;
	}
	public void setMaxUndoableSteps(int maxUndoableSteps) {
		String debugmsg = this.getClass().getName()+".setMaxUndoableSteps(): ";
		
		Log.debug(debugmsg+" maxUndoableSteps="+maxUndoableSteps);
		if(imagePanel!=null){
			RedoUndoList list = imagePanel.getRedoUndoList();
			if(list!=null)
				list.setMaxSize(maxUndoableSteps);
		}
	}

	/**
	 * Save an Image to the clipboard
	 * @param image
	 */
	private void saveImageToClipboard(Image image){
		if(image==null){
			Log.error("IM: image is null.");
			return;
		}
		
		clipboard.setContents(new ImageSelection(image), null);
//		synchronized(imagePanel){
//			Log.debug("Saved image to clipboard, notify the waitting thread.");
//			//Notify PastableImageChecker that the clipboard contains an image
//			imagePanel.notify();
//		}
		Log.debug("IM: save image to clipboard.");

	}
	
	/**
	 * Paste the clipboard's image to the ImagePanel<br>
	 * If the ImagePanel's selectedImage is not null, just paste it<br>
	 * Otherwise, paste that from the clipboards.<br>
	 */
	private void pasteImageToImagePanel(){
		String debugmsg = getClass().getName()+".pasteImageToImagePanel(): ";
		BufferedImage image = getPastableImage();
		
		if(image==null){
			Log.error(debugmsg+"image is null, can not paste to image panel");
			return;
		}
		
		imagePanel.pasteImage(image);
	}
	
	private BufferedImage getPastableImage(){
		String debugmsg = getClass().getName()+".getPastableImage(): ";
		BufferedImage image = null;
		
		//Firstly, try to get the ImagePanel's cached selectedImage
		//Mostly, if we use 'cut' or 'copy' of this program, the selectedImage
		//will contains an image.
		image = imagePanel.getSelectedImage();
		if(image!=null){
			Log.debug(debugmsg+"got pastable image from ImagePanel.");
		}else{
			//Try to get image from the clip board
			Transferable contents = clipboard.getContents(null);
			DataFlavor flavor = DataFlavor.imageFlavor;
			if(contents!=null){
				if(contents.isDataFlavorSupported(flavor)){
					try {
						//TODO only Image can be got from getTransferData()
						//Can we force to cast to BufferedImage ???
						image = (BufferedImage) contents.getTransferData(flavor);
						if(image!=null)
							Log.debug(debugmsg+"got pastable image from clipboard.");
					} catch (Exception e) {
						Log.error(debugmsg+"Exception "+e.getMessage());
					}
				}
			}	
		}

		if(image==null){
			Log.warn(debugmsg+"NO pastable image exists.");
		}
		
		return image;
	}
	
	/**
	 * Check if the clip board contains an image
	 * @return
	 */
	private boolean hasPastableImage(){
		String debugmsg = getClass().getName()+".loadScreenImage(): ";
		//Try to get image from the clip board
		Transferable contents = null;
		DataFlavor flavor = DataFlavor.imageFlavor;
		try{
			contents = clipboard.getContents(null);
			if(contents!=null){
				if(contents.isDataFlavorSupported(flavor)){
					Log.debug(debugmsg+"pastable image exists in clipboard.");
					return true;
				}
			}
		}catch(IllegalStateException e){
			Log.warn(debugmsg+"Exception "+e.getMessage());
			return false;
		}

//		if(imagePanel.getSelectedImage()!=null){
//			Log.debug(debugmsg+"pastable image exists in ImagePanel.");
//			return true;
//		}
		
		Log.debug(debugmsg+"NO pastable image exists.");
		return false;
	}

	
//	private class PastableImageChecker implements Runnable{
//
//		public void run() {
//			while(true){
//				if(hasPastableImage()){
//					if(!pasteAction.isEnabled())
//						pasteAction.setEnabled(true);
//				}else{
//					if(pasteAction.isEnabled())
//						pasteAction.setEnabled(false);					
//				}
//				try {
//					synchronized(imagePanel){
//						//We have to set a timeout here. As we have to check if the clipboard
//						//contains an image from time to time. If in this program we save an image
//						//to clipboard, we can send a notification ourself, But if the clipboard is
//						//filled outside this program, there is no notification, we have to check it.
//						Log.debug("timeout or received notification of saved image to clipboard");
//						imagePanel.wait(5000);
//					}
//				} catch (InterruptedException e) {
//					Log.debug("InterruptedException "+e.getMessage());
//				}
//			}
//		}
//		
//		public void start(){
//			new Thread(this).start();
//		}
//	}
	
	/**
	 * This method will capture a snapshot of screen and set it to the image panel<br>
	 * @param imagePanel
	 * @param screenRect
	 */
	
	private void loadScreenImage(ImagePanel imagePanel, Rectangle screenRect){
		String debugmsg = getClass().getName()+".loadScreenImage(): ";
		
		BufferedImage image = null;
		int minimalWidth=5, minimalHeight=5;
		
		if(imagePanel==null || screenRect==null) return;
		if(screenRect.getWidth()<=minimalWidth || screenRect.getHeight()<=minimalHeight){
			Log.warn(debugmsg+"The rectangle is too samll!!!");
			return;
		}
		
		try {
			image = ImageUtils.captureScreenArea(screenRect);
			originalImage = new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
			image.copyData(originalImage.getRaster());
			
		} catch (AWTException e) {
			Log.error(debugmsg+" meet Exception.");
		}
		
		if(image!=null){
			imagePanel.setFirstImage(image);
		}
	}
	
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
        	stopHooks();
        }
        super.processWindowEvent(e);
    }
	
    private void stopHooks(){
		if (keyboardHook!= null && keyboardHook.isHooked())
			keyboardHook.stop();
		if (mouseHook!= null && mouseHook.isHooked())
			mouseHook.stop();
    }
    private void startHooks(){
		if(keyboardHook!=null &&  !keyboardHook.isHooked()){
			keyboardHook.run();
		}
		if(mouseHook!=null && !mouseHook.isHooked()){
			mouseHook.run();
		}
    }

	private LLKeyboardHookListener getLLKListener(){
		if(handler==null){
			handler = new Handler();
		}
		return (LLKeyboardHookListener) handler;
	}
	
	private LLMouseHookListener getLLMListener(){
		if(handler==null){
			handler = new Handler();
		}
		return (LLMouseHookListener) handler;
	}
	
	private PropertyChangeListener getPCListener(){
		if(handler==null){
			handler = new Handler();
		}
		return (PropertyChangeListener) handler;		
	}
	
	/**
	 * This class implements listeners:<br>
	 * LLKeyboardHookListener and LLMouseHookListener <br>
	 * --> to handle mouse and key event when capturing screen image.<br>
	 * 
	 * PropertyChangeListener <br>
	 * --> to handle the button and menuitem's status when imagePanel's property change.<br>
	 */
	private class Handler implements LLKeyboardHookListener,LLMouseHookListener, PropertyChangeListener{
		private boolean controlOn = false;
		private Rectangle screenRect = new Rectangle();
		private Point start, end;

		public void onLLKeyboardHook(int code, NativeLong wParam, KBDLLHOOKSTRUCT info) {
			//Convert MS key code to Java key code
			int vk = MSKeyEvent.convertToJavaVK(info.vkCode);

			String key = KeyEvent.getKeyText(vk);
			
			switch(wParam.intValue()) {
			case LLKeyboardHook.WM_SYSKEYDOWN:
			case LLKeyboardHook.WM_KEYDOWN:
				Log.debug("vk: "+vk+"; key="+key+" KEY DOWN");

				if(KeyEvent.VK_CONTROL==vk){
					if(!controlOn){
						//Record the start point of screen rectangle
						start = MouseInfo.getPointerInfo().getLocation();
						status.setPoint1(start);
						controlOn = true;
					}
				}else{
					//Log.debug("Just ignore this key.");
				}
				
				break;
			case LLKeyboardHook.WM_SYSKEYUP:
			case LLKeyboardHook.WM_KEYUP:
				Log.debug("vk: "+vk+"; key="+key+" KEY UP");
				if(KeyEvent.VK_CONTROL==vk){
					if(controlOn){
						end = MouseInfo.getPointerInfo().getLocation();
						screenRect.setFrameFromDiagonal(start, end);
						status.setPoint2(end);
						loadScreenImage(imagePanel, screenRect);
						controlOn = false;
					}
				}else{
					//Log.debug("Just ignore this key.");
				}
				
				break;
			default:
				break;
			}
		}
		
		public void onLLMouseHook(int code, NativeLong wParam,MSLLHOOKSTRUCT info) {
			switch (wParam.intValue()) {
			case LLMouseHook.WM_MOUSEMOVE:
				int x = info.pt.x.intValue();
				int y =  info.pt.y.intValue();
//				Log.debug("Low Level Mouse: At [" + x + ","+ y + "]");
				if(controlOn){
					status.setPoint2(x,y);
				}else{
					status.setPoint1(x,y);
				}
				break;
			default:
				break;
			}
		}
		
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();
			Object oldValue = evt.getOldValue();
			Object newValue = evt.getNewValue();
//			Log.debug("property '"+name+"' changed from "+oldValue+" to "+newValue);
			if(name.equals(ImagePanel.CHANGABLE_PROP_SELECTABLE)){
				if(((Boolean)newValue).booleanValue()){
				}
			}else if(name.equals(ImagePanel.CHANGABLE_PROP_IMAGE)){
				boolean enabled = (newValue!=null);
				saveAction.setEnabled(enabled);
				selectAction.setEnabled(enabled);
				imgEdgeAction.setEnabled(enabled);
				imgSharpenAction.setEnabled(enabled);
				imgBlurAction.setEnabled(enabled);
				imgNegativeAction.setEnabled(enabled);
			}else if(name.equals(ImagePanel.CHANGABLE_PROP_SELECTED)){
				cutAction.setEnabled(((Boolean)newValue).booleanValue());
				copyAction.setEnabled(((Boolean)newValue).booleanValue());
			}else if(name.equals(ImagePanel.CHANGABLE_PROP_REDO_UNDO)){
				Log.debug("Setting redo undo Action :\n "+imagePanel.redoUndoList.toString());
				redoAction.setEnabled(imagePanel.canRedo());
				undoAction.setEnabled(imagePanel.canUndo());
			}
		}

	}//End Handler

    /**
     * This class is used when copy image data to clipboard
     *
     */
    private class ImageSelection implements Transferable {
		public ImageSelection(Image image) {
			theImage = image;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.equals(DataFlavor.imageFlavor);
		}

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException {
			if (flavor.equals(DataFlavor.imageFlavor)) {
				return theImage;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}

		private Image theImage;
	}//End ImageSelection
    
    private class SettingPanel extends JPanel{
    	private ImageManager2 imageManager = null;
    	private JDialog parentDialog = null;
    	
    	private int textWidth = 200;
    	private int textHeight = 25;
    	private JTextField rootDirField = null;
    	private JButton chooseDirBtn = null;
    	
    	private JPanel rootDirPanel = null;
    	
    	private JLabel maxUndoStepLabel = null;
    	private JTextField maxUndoStepField = null;
    	private JPanel maxUndoStepPanel = null;
    	
    	private JPanel centerPanel = null;
    	
    	
    	private JPanel buttonPanel = null;
    	private JButton approveButton = null;
    	private JButton cancelButton = null;
    	
    	public SettingPanel(ImageManager2 imageManager){
    		this.imageManager = imageManager;
    		initComponents();
    		arrangeComponents();
    	}
    	
    	public void setParentDialog(JDialog parentDialog){
    		this.parentDialog = parentDialog;
    	}
    	
    	public void paintComponent(Graphics g){
    		super.paintComponent(g);
    		//resize the root directory text field's width to show more text
    		rootDirField.setPreferredSize(new Dimension(this.getWidth()*3/4,textHeight));
    		this.revalidate();
    	}
    	
    	private void initComponents(){
    		centerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    		//initial root directory panel and components in it
    		rootDirPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    		TitledBorder rootdirBorder = BorderFactory.createTitledBorder("Root Directory");
    		rootdirBorder.setTitleJustification(TitledBorder.LEADING);
    		rootDirPanel.setBorder(rootdirBorder);
    		
        	rootDirField = new JTextField();
        	rootDirField.setPreferredSize(new Dimension(textWidth, textHeight));
        	rootDirField.setEditable(false);

        	maxUndoStepPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        	maxUndoStepLabel = new JLabel("Maximum Undoable Steps: ");
        	maxUndoStepField = new JTextField();
        	maxUndoStepField.setPreferredSize(new Dimension(textWidth/4, textHeight));
        	
        	chooseDirBtn = new JButton("...");
        	final JFileChooser jfc = new JFileChooser(){
				public void approveSelection() {
					if (getSelectedFile().isDirectory()) {
						super.approveSelection();
					} else {						
						JOptionPane.showMessageDialog(this, "Select path is not a directory.",
								                      "Error Selection", JOptionPane.ERROR_MESSAGE);
						Log.error("SettingPanel: Not a dirctory : Do NOT approve ");
					}
				}
			};
        	chooseDirBtn.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					jfc.setCurrentDirectory(new File(rootDirField.getText()));
					jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					
					int rc = jfc.showDialog(SettingPanel.this, "Select");
					if(rc==JFileChooser.APPROVE_OPTION){
						File root = jfc.getSelectedFile();
						Log.debug("SettingPanel: Selected file is "+root);
						rootDirField.setText(root.getAbsolutePath());
						jfc.setCurrentDirectory(root);
						Log.debug("SettingPanel: Root Directory will be changed to "+root);
					}
				}
        	});
        	
        	//initial button directory panel and components in it
        	buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        	
        	approveButton = new JButton("Approve");
        	approveButton.setMnemonic(KeyEvent.VK_A);
        	approveButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					//If we approve the modification, remember to set modifiable fields of this ImageManager2
					if(setValuesToManager())
						disposeDialog();
					else{
						Log.debug("There are some values not properly set.");
					}
				}
        	});
        	
        	cancelButton = new JButton("Cancel");
        	cancelButton.setMnemonic(KeyEvent.VK_C);
        	cancelButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					//If we cancel the modification, remember to reset modifiable fields of this SettingPanel
					getValuesFromManager();
					disposeDialog();
				}
        	});
        	
        	getValuesFromManager();
    	}
    	
    	/**
    	 * Get values from ImageManager2 and set them to this panel's fields for modify.<br>
    	 * If more fields of ImageManager2 need to be modified, please add it here<br>
    	 */
    	private void getValuesFromManager(){
    		rootDirField.setText(imageManager.getRootDir());
    		maxUndoStepField.setText(String.valueOf(imageManager.getMaxUndoableSteps()));
    	}
    	
    	/**
    	 * Get modified values from this panel's fields and Set them to ImageManager2.<br>
    	 * If more fields of ImageManager2 need to be modified, please add it here<br>
    	 */
    	private boolean setValuesToManager(){
    		imageManager.setRootDir(rootDirField.getText());
    		
    		try{
    			String maxUndo = maxUndoStepField.getText();
    			int maxUndoableSteps = Integer.parseInt(maxUndo.trim());
    			imageManager.setMaxUndoableSteps(maxUndoableSteps);
    		}catch(Exception e){
    			Log.warn("The input value should be int type!");
    			maxUndoStepField.requestFocus();
    			maxUndoStepField.selectAll();
    			return false;
    		}
    		
    		return true;
    	}
    	
    	private void arrangeComponents(){
    		this.setLayout(new BorderLayout());

    		rootDirPanel.add(rootDirField);
    		rootDirPanel.add(chooseDirBtn);
    		
    		maxUndoStepPanel.add(maxUndoStepLabel);
    		maxUndoStepPanel.add(maxUndoStepField);
    		
    		centerPanel.add(rootDirPanel);
    		centerPanel.add(maxUndoStepPanel);
    		
    		buttonPanel.add(approveButton);
    		buttonPanel.add(cancelButton);
    		
    		this.add(centerPanel, BorderLayout.CENTER);
    		this.add(buttonPanel, BorderLayout.SOUTH);
    	}
    	
    	private void disposeDialog(){
    		if(parentDialog!=null){
    			parentDialog.dispose();
    		}
    	}
    }//End of SettingPanel
    
	private class ZoomSlider extends JSlider{
		private static final int MIN_VALUE = 0;
		private static final int MAX_VALUE = 10;
		private static final int INIT_VALUE = 3;
		
		private static final String FRACTION_SYMBOL = "/";
		
		Hashtable<Integer,JLabel> labels = new Hashtable<Integer,JLabel>();
		
		public ZoomSlider(){
			super(JSlider.HORIZONTAL,MIN_VALUE,MAX_VALUE,INIT_VALUE);
			
			for(int i=(INIT_VALUE-1);i>=MIN_VALUE;i--){
				labels.put(new Integer(i), new JLabel(1+FRACTION_SYMBOL+(INIT_VALUE-i+1)));
			}
			
			for(int i=INIT_VALUE;i<=MAX_VALUE;i++){
				labels.put(new Integer(i), new JLabel(String.valueOf(i-INIT_VALUE+1)));
			}
			
			setMajorTickSpacing(10);
			setMinorTickSpacing(1);
			setLabelTable(labels);
			setPaintLabels(true);
			setPaintTicks(true);
			setPaintTrack(true);
			setSnapToTicks(true);
//			this.setPreferredSize(new Dimension(350, 25));
			
			addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					if(getValueIsAdjusting()){
//						Log.debug("Adjusting "+getZoomValue());
					}else{
//						Log.debug("Final "+getZoomValue());
						imagePanel.setZoomRate(getZoomValue());
						imagePanel.resizeSmallDraggableRects();
						imagePanel.repaintAndRevalidate();
					}
				}
			});
		}

		public float getZoomValue(){
			float zoomValue = 1.0F;
			
			JLabel label = labels.get(new Integer(getValue()));
			if(label!=null){
				try {
					zoomValue = Float.parseFloat(label.getText());
				} catch (NumberFormatException e) {
					try{
						String fraction = label.getText();
						int fractionIndex = fraction.indexOf(FRACTION_SYMBOL);
						;
						zoomValue = Float.parseFloat(fraction.substring(0,fractionIndex))/Float.parseFloat(fraction.substring(fractionIndex+1));
					}catch(Exception ex){
						zoomValue = 1.0F;
					}
				}
			}
			
			return zoomValue;
		}
	}//End ZoomSlider
    
	private class ImagePanel extends JPanel implements MouseMotionListener, MouseListener {
		private static final int IMAGE_PANEL_WIDTH = 300;
		private static final int IMAGE_PANEL_HEIGHT = 200;
		
		private static final float SMALL_DRAG_RECT_SIZE = 5.0F;
		private static final float THIN_STROKE_WIDTH = 1.0F;
		private static final float DEFAULT_ZOOM_RATE = 1.0F;
		private static final float IMAGE_BASE_COOR	 = 0.0F;

		private static final int DRAG_TO_SOURTH = 0;
		private static final int DRAG_TO_SOURTH_EAST = 1;
		private static final int DRAG_TO_EAST = 2;
		
		//The following string constants represent property's name of this class
		//they will be used in firePropertyChange() and the PropertyChangeListeners.
		//If some other properties can firePropertyChange, please add constant string for it.
		public static final String CHANGABLE_PROP_MOUSE_LOCATION = "mouseLoc";
		public static final String CHANGABLE_PROP_IMAGE 		 = "image";
		public static final String CHANGABLE_PROP_SELECTABLE     = "selectable";
		public static final String CHANGABLE_PROP_SELECTED       = "selected";
		public static final String CHANGABLE_PROP_SELECTED_RECT  = "selectedRect";
		public static final String CHANGABLE_PROP_DRAGGING_RECT  = "draggingRect";
		public static final String CHANGABLE_PROP_REDO_UNDO		 = "redoUndo";

		private BufferedImage image = null;
		private BufferedImage selectedImage = null;
		private RectangleFloat imageRect = null;
		private float imageBaseX=IMAGE_BASE_COOR, imageBaseY=IMAGE_BASE_COOR;//the base point to draw the image (Left Top corner)

		private float zoomRate = DEFAULT_ZOOM_RATE;
		
		/**
		 * The following selectXXX properties are used when the image is selected<br>
		 */
		private boolean selectable = false;
		private boolean beingSelected = false;
		private boolean selected = false;
		private RectangleFloat selectedRect = null;
		private PointFloat selectBeginLoc = new PointFloat();
		
		/**
		 * The property dragRects contains small rectangles, these rectangles will be drawn<br>
		 * around the image so that user can use mouse to drag to resize the image.<br>
		 * 
		 * The property dragRectSize represents the width and height of these rectangle<br>
		 */
		private RectangleFloat[] dragRects = null;//small rectangle, when mouse is in it, image can be dragged to resize
		private float dragRectSize = SMALL_DRAG_RECT_SIZE;//the width and height of the small drag rectangle.
		
		/**
		 * The following dragXXX properties are used when the image is being dragged to resize<br>
		 */
		private boolean draggable = false;
		private boolean beingDragged = false;
		private int dragDirection = -1;
		private RectangleFloat draggingRect = null;
		
		/**
		 * dashPattern is used to describe the dash line's pattern<br>
		 * when the image is dragged to resize or selected, a dashed rectangle will be drawn<br>
		 */
		private float[] dashPattern = {5f,5f,5f,5f,5f,5f};
		
		//This is shared by different mouseXXX() functions, to avoid creating a new instance in these functions.
		//So when use mouseLocation, take care !!!
		private PointFloat mouseLocation = new PointFloat();
		
		private RedoUndoList redoUndoList = null;

		public ImagePanel() {
	    	super();
	    	setSize(IMAGE_PANEL_WIDTH,IMAGE_PANEL_HEIGHT);
	    	
			selectedRect = new RectangleFloat(imageBaseX,imageBaseY,0,0);
			draggingRect = new RectangleFloat(imageBaseX,imageBaseY,0,0);
	    	imageRect = new RectangleFloat(imageBaseX,imageBaseY,0,0);
	    	
	    	dragRects = new RectangleFloat[3];//S, SE, E
	    	resizeSmallDraggableRects();

	        addMouseMotionListener(this); //handle mouse drags
	        addMouseListener(this);
	        
	        redoUndoList = new RedoUndoList();
	    }
	    
	    public ImagePanel(int width, int height){
	    	this();
	    	setSize(width,height);
	    }
	    
	    public ImagePanel(Dimension size){
	    	this();
	    	setSize(size);
	    }
	    
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			if(image !=null){
				g2.transform(AffineTransform.getScaleInstance(zoomRate, zoomRate));
//				Log.debug("PaintComponent image size "+image.getWidth()+":"+image.getHeight());
				//Draw image on panel
				g2.drawImage(image, null, (int)imageBaseX, (int)imageBaseY);
				
				Stroke originalStroke = g2.getStroke();
				//Draw the dragging dash rectangle on panel
				if(beingDragged){
					//Set the dash stroke to draw the dragging rectangle
					g2.setStroke(new BasicStroke(getRealViewSize(THIN_STROKE_WIDTH),BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,60f,dashPattern,0));
					g2.draw(draggingRect);
				}else if(beingSelected){
					g2.setStroke(new BasicStroke(getRealViewSize(THIN_STROKE_WIDTH),BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,60f,dashPattern,0));
					g2.draw(selectedRect);			
				}else{
					if(selected){
						Paint op = g2.getPaint();
						g2.setPaint(Color.BLUE);
						g2.setStroke(new BasicStroke(getRealViewSize(THIN_STROKE_WIDTH),BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,60f,dashPattern,0));
						g2.draw(selectedRect);
						g2.setPaint(op);
					}
					//Set the stroke to draw the rectangles
					g2.setStroke(new BasicStroke(getRealViewSize(THIN_STROKE_WIDTH)));
					//Draw small drag rectangle on panel
					float size = getDragRectSize();

					dragRects[0].setLocation(imageRect.x+(imageRect.width-size)/2, imageRect.y+imageRect.height);
					dragRects[1].setLocation(imageRect.x+imageRect.width, imageRect.y+imageRect.height);
					dragRects[2].setLocation(imageRect.x+imageRect.width, imageRect.y+(imageRect.height-size)/2);
					for(int i=0;i<dragRects.length;i++){
						g2.draw(dragRects[i]);
					}
				}
				
				g2.setStroke(originalStroke);
			}
		}
	    
		public void setZoomRate(float zoomRate){
			this.zoomRate = zoomRate;
		}
		
		public void setSelectable(boolean selectable){
			this.firePropertyChange(CHANGABLE_PROP_SELECTABLE, this.selectable, selectable);
			this.selectable = selectable;
			if(!selectable){
				//remember to turn off the property 'selected'
				setSelected(false);
				repaint();
			}
		}
		
		public void setSelected(boolean selected){
			this.firePropertyChange(CHANGABLE_PROP_SELECTED, this.selected, selected);
			this.selected = selected;
		}
		
		/**
		 * <b>Purpose:</b>  If user use slider to scale the image, the Graphics's coordination will be<br>
		 *                  scaled. If you want to keep a Shape looks always the same size, please call<br>
		 *                  these getRealViewXXX() functions to recalculate its width, height or location<br>
		 *                  then, draw it on the Graphics.<br>
		 * @param size
		 * @return
		 */
	    public float getRealViewSize(float size){
	    	return size/zoomRate;
	    }
	    
	    public float getRealViewSize(double size){
	    	return (float)size/zoomRate;
	    }
	    
	    public Point2D getRealViewPoint(Point2D p){
	    	float x = getRealViewSize(p.getX());
	    	float y = getRealViewSize(p.getY());
	    	p.setLocation(x, y);
	    	
	    	return p;
	    }
	    /**
	     * <b>Purpose:</b>  The property dragRectSize is used to draw the small rectangles around the image<br>
	     *                  so that user can use mouse to drag to resize the image.<br>
	     *                  If user use slider to scale the image view, to keep these drag rectangles<br>
	     *                  look the same size, we should use this method to set the width and height of them<br>
	     * @return
	     */
	    public float getDragRectSize(){
	    	return getRealViewSize(dragRectSize);
	    }
	    
//	    public float getImageBaseX(){
//	    	return imageBaseX;
//	    }
//	    
//	    public float getImageBaseY(){
//	    	return imageBaseY;
//	    }
	    
	    private int getImageWidth(){
	    	int width = 0;
	    	if(image!=null){
	    		width = image.getWidth();
	    	}
	    	return width;
	    }
	    private int getImageHeight(){
	    	int height = 0;
	    	if(image!=null){
	    		height = image.getHeight();
	    	}
	    	return height;
	    }

		public BufferedImage getImage() {
			return image;
		}
		
	    public RedoUndoList getRedoUndoList() {
			return redoUndoList;
		}
	    
		/**
		 * After cut, copy, paste to modify the current image<br>
		 * please use this method to set image<br>
		 * 
		 * @param image
		 */
		public void editImage(BufferedImage image) {
			if(image==null) return;
			
	    	redoUndoList.add(image);
	    	//After edit image, we should be able to undo, we need to fire
	    	//a property change event of CHANGABLE_PROP_REDO_UNDO to enable the undo action.
	    	firePropertyChange(CHANGABLE_PROP_REDO_UNDO, 0, 1);
	    	
	    	setImage(image);
		}
		
		/**
		 * When open an image or capture an image, call this method to set image<br>
		 * to this panel<br>
		 * 
		 * @param image
		 */
		public void setFirstImage(BufferedImage image) {
			if(image==null) return;
			
			//Reset the redoUndoList, and add this image as the first image
			redoUndoList.reset();
	    	redoUndoList.add(image);
	    	firePropertyChange(CHANGABLE_PROP_REDO_UNDO, 0, 1);
	    	
			setImage(image);
		}
		
		/**
		 * This method will repaint the image on the panel<br>
		 * This method will also fire an image change event so that<br>
		 * the ImageManager2 can adjust its  save or select actions' state<br>
		 *
		 */
		private void setImage(BufferedImage image){
			String debugmsg = getClass().getName()+".setImage(): ";
			
			Log.debug(debugmsg+" Firing image change event.");
			firePropertyChange(CHANGABLE_PROP_IMAGE, this.image, image);
			
			this.image = image;

	    	imageRect.setSize(getImageWidth(),getImageHeight());
	    	repaintAndRevalidate();
		}
		
		public BufferedImage getSelectedImage() {
			return selectedImage;
		}
		
		/**
		 * Release all resources used by image
		 * @param image
		 */
		private void releaseImageResource(BufferedImage image){
			String debugmsg = getClass().getName()+".releaseImageResource(): ";
			if(image!=null){
				Log.debug(debugmsg+"release image's resource.");
				image.flush();
			}
		}
		
		/**
		 * @param cut	boolean if true, it will cut the selected rectangle from the panel image.
		 * @return		The image (within the selectedRect) got from the panel image
		 */
		public BufferedImage copyOrCutSelectedImage(boolean cut) {
			//Selecting a rectangle on the image will be processed in method mouseDragged(),
			//We can make sure that the selectedRect is smaller than imageRect
			if(selected && image!=null){
				setSelected(false);
				int offsetX = (int)(selectedRect.x-imageRect.x);
				int offsetY = (int)(selectedRect.y-imageRect.y);
				int width = (int) selectedRect.width;
				int height = (int) selectedRect.height;
				if(selectedImage==null){
					Log.debug("selectedImage is null, will be got from panel's image.");
					//!!!getSubimage() return a BufferedImage, it shares the same data
				    //data array as the original image
//					selectedImage = image.getSubimage(offsetX, offsetY, width, height);
					selectedImage = ImageUtils.getCopiedImage(image, offsetX, offsetY, 0, 0, width, height, Color.WHITE);
				}
				
				if(cut){
					//As we will store the original image in the redo-undoList,
					//Should NOT modify the original image, create a new one and modify.
					//Otherwise, we risk to modify the image stored in the that list
					editImage(ImageUtils.paintOnImage(image, offsetX, offsetY, width, height, Color.WHITE,true));
				}
				repaint();
			}
			return selectedImage;
		}
		
		/**
		 * @param image
		 */
		public void pasteImage(BufferedImage image){			
			editImage(ImageUtils.paintOnImage(image, 0, 0, image.getWidth(), image.getHeight(), 0, 0, this.image,null,true));	
		}
		
		public Dimension getPreferredSize(){
			if(image==null){
				return getSize();
			}else{
				int w = (int) (zoomRate*(imageRect.x+image.getWidth()+dragRectSize*2));
				int h = (int) (zoomRate*(imageRect.y+image.getHeight()+dragRectSize*2));
				return new Dimension(w,h);
			}
		}

	    //Methods required by the MouseMotionListener interface:
	    public void mouseMoved(MouseEvent e) {
	    	mouseLocation.setRealViewLocation(e.getPoint());

	    	setMouseCursor(mouseLocation);
	    }
	    
	    private void setMouseCursor(Point2D p){
	    	
	    	if(image!=null){
	    		draggable = false;
	    		if(imageRect.contains(p)){
	    			if(selectable){
	    				this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	    				firePropertyChange(CHANGABLE_PROP_MOUSE_LOCATION,null,mouseLocation);
	    			}else{
	    				this.setCursor(Cursor.getDefaultCursor());
	    			}
	    		}else if(dragRects[0].contains(p)){
	    			draggable = true;
	    			dragDirection = DRAG_TO_SOURTH;
	    			this.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));	    			
	    		}else if(dragRects[1].contains(p)){
	    			draggable = true;
	    			dragDirection = DRAG_TO_SOURTH_EAST;
	    			this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));	    			
	    		}else if(dragRects[2].contains(p)){
	    			draggable = true;
	    			dragDirection = DRAG_TO_EAST;
	    			this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));	    			
	    		}else{
	    			this.setCursor(Cursor.getDefaultCursor());
	    		}
	    	}
	    }
	    
	    public void mouseDragged(MouseEvent e) {
	    	String debugmsg = getClass().getName()+".mouseDragged(): ";
	    	
	    	mouseLocation.setRealViewLocation(e.getPoint());
	    	float mouseX = mouseLocation.x;
	    	float mouseY = mouseLocation.y;
	    	
	    	float imageWidth=imageRect.width;
	    	float imageHeight=imageRect.height;
	    	
	    	if(image!=null){
		    	if(draggable){
		    		if(mouseX<=imageRect.x || mouseY<=imageRect.y){
		    			Log.warn(debugmsg+"can NOT drag beyond the base Point ("+imageRect.x+","+imageRect.y+")");
		    			return;
		    		}
		    		
		    		beingDragged = true;
		    		
		    		switch(dragDirection){
		    		case DRAG_TO_SOURTH:
		    			imageHeight = mouseY - imageRect.y;
		    			break;
		    		case DRAG_TO_SOURTH_EAST:
		    			imageWidth = mouseX - imageRect.x;
		    			imageHeight = mouseY - imageRect.y;
		    			break;
		    		case DRAG_TO_EAST:
		    			imageWidth = mouseX - imageRect.x;
		    			break;
		    		default:
		    			Log.error(debugmsg+"Not Possible.");
		    			break;
		    		}
//		    		Log.debug(debugmsg+"old size ("+imageRect.width+","+imageRect.height+"), new size ("+imageWidth+","+imageHeight+"), direction="+dragDirection);

		    		draggingRect.setSize(imageWidth, imageHeight);
		    		firePropertyChange(CHANGABLE_PROP_DRAGGING_RECT,null,draggingRect);
		    		
		    		repaint();
		    	}else if(selectable){
		    		if(!beingSelected){
		    			//When begin to drag, check the mouse location to see if we can begin select
			    		if(mouseX<imageRect.x ||
			    		   mouseX>(imageRect.x+imageWidth) ||
			    		   mouseY<imageRect.y ||
			    		   mouseY>(imageRect.y+imageHeight)){
			    			Log.warn(debugmsg+"can NOT begin select beyond the rectangle ("+imageRect.x+","+imageRect.y+","+" imageWidth,"+" imageHeight)");
			    			return;
			    		}
		    			//store the mouse location as the begin point of the selecting rectangle
			    		selectBeginLoc.setLocation(mouseLocation);
			    		Log.debug(debugmsg+"selectedImage is reset to null.");
		    			//reset the old selectedImage to null
			    		releaseImageResource(selectedImage);
			    		selectedImage = null;
		    		}
		    		beingSelected = true;
		    		//resize the mouse position to keep it within the imageRect
		    		mouseX = (mouseX<imageRect.x)? imageRect.x:mouseX;
		    		mouseX = (mouseX>imageRect.x+imageWidth)? (imageRect.x+imageWidth):mouseX;
		    		mouseY = (mouseY<imageRect.y)? imageRect.y:mouseY;
		    		mouseY = (mouseY>imageRect.y+imageHeight)? (imageRect.y+imageHeight):mouseY;
		    		
		    		selectedRect.setFrameFromDiagonal(selectBeginLoc.x, selectBeginLoc.y, mouseX, mouseY);
		    		//I don't care the old value, just give it as null.
		    		firePropertyChange(CHANGABLE_PROP_SELECTED_RECT,null,selectedRect);
		    		repaint();
		    	}
	    	}
	    }

		public void mouseClicked(MouseEvent e) {
//			mouseLocation.setRealViewLocation(e.getPoint());

		}
		public void mouseEntered(MouseEvent e) {
//			mouseLocation.setRealViewLocation(e.getPoint());
//			Log.debug("entered");
		}
		public void mouseExited(MouseEvent e) {
//			mouseLocation.setRealViewLocation(e.getPoint());
//			Log.debug("exited");
		}
		public void mousePressed(MouseEvent e) {
//			mouseLocation.setRealViewLocation(e.getPoint());
//			Log.debug(mouseLocation);
			//When mouse is pressed, the selectedRect will be erased from the panel
			if(selected){
				setSelected(false);
				repaint();
			}
		}
		public void mouseReleased(MouseEvent e) {
			String debugmsg = getClass().getName()+".mouseReleased(): ";
//			mouseLocation.setRealViewLocation(e.getPoint());
			
			if(beingDragged){
				//If we have been dragging to resize the image, when the mouse is released
				//we should reset this panel's image, and repaint this panel
				beingDragged = false;
//				Log.debug(debugmsg+"drag new size ("+draggingRect.width+","+draggingRect.height+")");
				editImage(ImageUtils.getCopiedImage(image,(int)draggingRect.width, (int)draggingRect.height, Color.WHITE));
			}else if(beingSelected){
				//If we have been dragging to select part of the image, when the mouse is released
				//we should modify panel's state beingSelected and repaint this panel
				beingSelected = false;//if this is false, the selected rectangle will be drawn as a different color
				setSelected(true);//we should call this to make cut and copy action enabled.
				//TODO we need to draw the selectRect and the small rectangles
				//TODO so that the selected rectangle can be drag and move
				repaint();
			}
		}

		/**
		 * According to the zoom value, reset the rectangles.
		 */
		public void resizeSmallDraggableRects(){
			//The small drag rectangle should keep the same view size
			float size = getDragRectSize();
	    	for(int i=0;i<dragRects.length;i++){
	    		dragRects[i] = new RectangleFloat(size,size);
	    	}
		}
	
		/**
		 * If this ImagePanel is contained in a JScrollPane, and if {@link #getPreferredSize()}<br>
		 * return a size bigger than that of outer JScrollPane, then this method should be called<br>
		 * to make sure JScrollPane can show its ScrollBar.<br>
		 * But if you are sure the ImagePanel's size is smaller than that of JScrollPane, you can<br>
		 * just call repaint().<br>
		 * 
		 * For example, if you use JSlider to scale this panel, this method should be called in case<br>
		 * you drag the slider to make this panel show bigger image.<br>
		 */
		public void repaintAndRevalidate(){
			repaint();
			//Inform the scroll pane to update itself
			revalidate();
		}
		
		public void redo(){
			BufferedImage image = (BufferedImage)(redoUndoList.redo());
	    	//After redo, we need to fire a property change event of CHANGABLE_PROP_REDO_UNDO to
			//adjust the state of redo and undo action.
			firePropertyChange(CHANGABLE_PROP_REDO_UNDO,0,1);
			setImage(image);
		}
		public void undo(){
			BufferedImage image = (BufferedImage)(redoUndoList.undo());
	    	//After undo, we need to fire a property change event of CHANGABLE_PROP_REDO_UNDO to
			//adjust the state of redo and undo action.
			firePropertyChange(CHANGABLE_PROP_REDO_UNDO,0,1);
			setImage(image);
		}
		public boolean canRedo(){
			return redoUndoList.getCurrent().hasNext();
		}
		public boolean canUndo(){
			return redoUndoList.getCurrent().hasPrevious();
		}
		
		public void filterImage(String filter){
			String debugmsg = getClass().getName()+".filterImage(): ";
			BufferedImage filteredImage = null;
			if(image==null){
				Log.warn(debugmsg+" image is null, can NOT filte.");
				return;
			}
			
			Log.debug(debugmsg+" action is "+filter+"; image "+image);
			
			if(filter.equals(ImageManager2.IMG_BLUR_ACTION_NAME)){
				filteredImage = ImageUtils.blurImage(image);
			}else if(filter.equals(ImageManager2.IMG_EDGE_ACTION_NAME)){
				filteredImage = ImageUtils.edgeImage(image);				
			}else if(filter.equals(ImageManager2.IMG_NEGATIVE_ACTION_NAME)){
				filteredImage = ImageUtils.negativeImage(image);
			}else if(filter.equals(ImageManager2.IMG_SHARPEN_ACTION_NAME)){
				filteredImage = ImageUtils.sharpenImage(image);
			}
			
			Log.debug(debugmsg+" filtered Image is "+filteredImage);
			
			editImage(filteredImage);
		}
		
		private class RectangleFloat extends Rectangle2D.Float{
			public RectangleFloat(){
				super();
			}
			public RectangleFloat(float x, float y, float w, float h){
				super(x,y,w,h);
			}
			public RectangleFloat(float w,float h){
				super(0,0,w,h);
			}
			
			public void setLocation(float x, float y){
				this.x = x;
				this.y = y;
			}
			
			public void setSize(float w, float h){
				this.width = w;
				this.height = h;
			}
		}//End RectangleFloat
		
		private class PointFloat extends Point2D.Float{
			public PointFloat(){
				super();
			}
			public PointFloat(float x, float y){
				super(x,y);
			}
			public PointFloat(Point p){
				super(p.x,p.y);
			}
			
			public float getFloatX(){
				return this.x;
			}
			public float getFloatY(){
				return this.y;
			}
			
			public void move(float x, float y){
				this.x = x;
				this.y = y;
			}
			public void translate(float x, float y){
				this.x += x;
				this.y += y;
			}

			/**
			 * This method should be called in the listeners like mouseXXX()
			 * @param p
			 */
			public void setRealViewLocation(Point p){
				this.x = getRealViewSize(p.getX());
				this.y = getRealViewSize(p.getY());				
			}

		}//End PointFloat
		
	}//End ImagePanel
	
	private class StatusBar extends JPanel implements PropertyChangeListener{
		private JLabel capInfo = null;
		private JLabel editInfo = null; 
		private JLabel point1 = null;
		private JLabel point2 = null;
		private ImagePanel imagePanel = null;
		private static final int STATUS_PANEL_HEIGHT = 20;
		
		public static final String STATUS_INFO_CAPTURE = "Move to start point, Press Ctrl, Move to end point, Release Ctrl.";
		public static final String STATUS_INFO_SELECTABLE = "Selectable";
		public static final String STATUS_INFO_NO_SELECTABLE = "Not Selectable";
		public static final String STATUS_INFO_COPYABLE = "Copyable";
		public static final String STATUS_INFO_PASTABLE = "Pastable";
		public static final String STATUS_INFO_POINT1 = "P1: ";
		public static final String STATUS_INFO_POINT2 = "P2: ";
		public static final String STATUS_INFO_INITTEXT = "Status Bar Informations ...";
		
		
		public StatusBar(){
			capInfo = new JLabel(STATUS_INFO_INITTEXT);
			editInfo = new JLabel(STATUS_INFO_NO_SELECTABLE);
			point1 = new JLabel(STATUS_INFO_POINT1);
			point2 = new JLabel(STATUS_INFO_POINT2);
			setLayout(new FlowLayout(FlowLayout.LEADING));
			add(point1);
			add(new JSeparator(JSeparator.VERTICAL));
			add(point2);	
			add(new JSeparator(JSeparator.VERTICAL));
			add(editInfo);
			add(new JSeparator(JSeparator.VERTICAL));
			add(capInfo);
		}
		
		public StatusBar(ImagePanel imagePanel){
			this();
			this.imagePanel = imagePanel;
		}
		
		public void setCapInfo(String information){
			capInfo.setText(information);
		}

		public void setEditInfo(String information){
			editInfo.setText(information);
		}

		public void setPoint1(Point2D p){
			int x = (int) p.getX();
			int y = (int) p.getY();
			point1.setText(STATUS_INFO_POINT1+"("+(int)x+","+(int)y+")");
		}
		public void setPoint1(double x, double y){
			point1.setText(STATUS_INFO_POINT1+"("+x+","+y+")");
		}
		
		public void setPoint2(Point2D p){
			int x = (int) p.getX();
			int y = (int) p.getY();
			point2.setText(STATUS_INFO_POINT2+"("+x+","+y+")");
		}
		public void setPoint2(double x, double y){
			point2.setText(STATUS_INFO_POINT2+"("+(int)x+","+(int)y+")");
		}
		
		
		/**
		 * In this method only handle the status information when the ImagePanel's<br>
		 * properties change.<br>
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();
			Object oldValue = evt.getOldValue();
			Object newValue = evt.getNewValue();
			String debugmsg = getClass().getName()+".propertyChange(): ";
			
//			Log.debug(debugmsg+"property '"+name+"' changed from "+oldValue+" to "+newValue);
			if(name.equals(ImagePanel.CHANGABLE_PROP_SELECTABLE)){
				if(((Boolean)evt.getNewValue()).booleanValue()){
					editInfo.setText(STATUS_INFO_SELECTABLE);
				}else{
					editInfo.setText(STATUS_INFO_NO_SELECTABLE);
				}
			}else if(name.equals(ImagePanel.CHANGABLE_PROP_IMAGE)){
			}else if(name.equals(ImagePanel.CHANGABLE_PROP_SELECTED_RECT) ||
					name.equals(ImagePanel.CHANGABLE_PROP_DRAGGING_RECT)){
				Rectangle2D rect = (Rectangle2D) newValue;
				setPoint1(rect.getX(),rect.getY());
				setPoint2(rect.getX()+rect.getWidth(), rect.getY()+rect.getHeight());
			}else if(name.equals(ImagePanel.CHANGABLE_PROP_MOUSE_LOCATION)){
				setPoint1((Point2D)newValue);
			}
		}
		
		public Dimension getPreferredSize(){
			return new Dimension(imagePanel.getPreferredSize().width,STATUS_PANEL_HEIGHT);
		}
	}//End of StatusBar
	
	/**
	 * This class is a linked list to store the edited Image, so that<br>
	 * user can use Ctrl+Z, Ctrl+Y to undo or redo its work.<br>
	 *
	 */
	private class RedoUndoList{
		public static final int DEFAULT_LIST_MAX_SIZE = 10;
		private int maxSize = DEFAULT_LIST_MAX_SIZE;
		private LinkableImage first = null;
		private LinkableImage last = null;
		private LinkableImage current = null;
		private int index = -1;

		public RedoUndoList(){
		}
		
		public RedoUndoList(Image image){
			first = last = current = new LinkableImage(image);
			index = 0;
		}
		
//		public LinkableImage getFirst() {
//			return first;
//		}
//		public LinkableImage setFirst(LinkableImage first) {
//			this.first = first;
//			return this.first;
//		}
//		public LinkableImage getLast() {
//			return last;
//		}
//		public LinkableImage setLast(LinkableImage last) {
//			this.last = last;
//			return this.last;
//		}
		public LinkableImage getCurrent() {
			return current;
		}
//		public LinkableImage setCurrent(LinkableImage current) {
//			this.current = current;
//			return this.current;
//		}
		
		public void reset(){
			while(first!=null){
				first.getUserObject().flush();
				first = first.next();
			}
			current = first = last = null;
			index = -1;
		}
		
		public int getMaxSize() {
			return maxSize;
		}
		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}

		public int getIndex(){			
			return index;
		}
		public void setIndex(int index){
			this.index = index;
		}
		/**
		 * Move the current pointer back, and return the object contained<br>
		 * in the current pointer<br>
		 */
		public Image undo(){
			if(current!=null){
				if(current.hasPrevious()){
					current = current.previous();
					index--;
				}
			}
			
			return current.getUserObject();
		}
		
		/**
		 * Move the current pointer in advance, and return the object contained<br>
		 * in the current pointer<br>
		 */
		public Image redo(){
			if(current!=null){
				if(current.hasNext()){
					current = current.next();
					index++;
				}
			}
			return current.getUserObject();
		}
		
		/**
		 * Add the new LinkableImage to the end of the list<br>
		 * If the list has no enough space, it will remove the <br>
		 * first object from the list. <br>
		 * @param linkableImage
		 * @return
		 */
		public Image add(Image image){
			LinkableImage linkableImage = null;
			String debugmsg = getClass().getName()+".add(): ";
			
			if(image!=null){
				linkableImage = new LinkableImage(image);
				linkableImage.setNext(null);
				linkableImage.setPrevious(null);
			}else{
				Log.warn(debugmsg+"You should NOT insert a null into the list.");
				if(current!=null)
					return current.getUserObject();
				else
					return null;
			}
			
			if(current==null){
				Log.debug(debugmsg+"You have added the first element into the list.");
				current = first = last = linkableImage;
			}else{
				if(index<maxSize-1){
					Log.debug(debugmsg+"The list still has enough space, insert to the tail.");
					//We still have space, move current and last one step advance
				}else{
					Log.debug(debugmsg+"The list does not have enough space, remove the first, insert to tail.");
					//We don't have enough space, we remove the first and move it one step advance
					first = first.next();
					first.setPrevious(null);
					//then add new item to the last of the list
				}
				
				current.setNext(linkableImage);
				linkableImage.setPrevious(current);
				current = linkableImage;
				last = current;
				last.setNext(null);
			}
			index++;
			
			return current.getUserObject();
		}
		
		/**
		 * Remove the last object from the list
		 * @return
		 */
		public Image remove(){
			if(last!=null){
				if(last.hasPrevious()){
					last = last.previous();
					last.setNext(null);
					current=last;
					index--;
				}
			}
			return last.getUserObject();
		}
		
		public String toString(){
			int size = 0;
			StringBuffer sb = new StringBuffer();
			LinkableImage tmp = null;
			tmp = first;
			
			sb.append("Current image index: "+index+" \n");
			while(tmp!=null){
				sb.append("["+size+"]:"+tmp.getUserObject()+"\n");
				size++;
				tmp = tmp.next();
			}
			sb.append("Total image: "+size+" \n");
			
			return sb.toString();
		}
	}//End RedoUndoList
	
	public class LinkableImage implements Linkable{
		private LinkableImage previous;
		private LinkableImage next;
		private Image image;//User Object
		
		LinkableImage(){
			
		}
		LinkableImage(Image image){
			this.image = image;
		}
		
		public boolean hasNext() {
			return (next!=null);
		}

		public boolean hasPrevious() {
			return (previous!=null);
		}

		public LinkableImage next() {
			return next;
		}

		public LinkableImage previous() {
			return previous;
		}

		public void setNext(LinkableImage next){
			this.next = next;
		}
		
		public void setPrevious(LinkableImage previous){
			this.previous = previous;
		}
		
		public void setUserObject(Object userObject) {
			if(userObject instanceof Image){
				this.image = (Image) userObject;
			}else{
				Log.debug("Can not set a non Image to UserObject of LinkableImage.");
			}
		}
		
		public Image getUserObject() {
			return image;
		}
	}
	
	public interface Linkable{
		public boolean hasNext();
		public boolean hasPrevious();
		
		public Linkable next();
		public Linkable previous();
		
		public Object getUserObject();
		public void setUserObject(Object userObject);
	}
	
	public static void main(String[] args){
//		Log.ENABLED = true;
//		Log.setLogLevel(Log.INFO);
//		Log.setLogLevel(Log.DEBUG);
		new ImageManager2();
	}
}
