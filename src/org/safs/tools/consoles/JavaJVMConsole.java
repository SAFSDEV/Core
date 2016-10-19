/**
 * History:
 * NOV 09, 2015	(sbjlwa) Modify run(): avoid the problem of occupy CPU too much.
 * NOV 16, 2015	(sbjlwa) Add menu to provide Save, Clear, Find functionalities.
 * OCT 19, 2016	(sbjlwa) Provided ability to set the console window's state.
 *                       Moved two constants to DriverConstant class.
 * 
 */
package org.safs.tools.consoles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.text.FileUtilities;
import org.safs.tools.drivers.DriverConstant;

/**
 * Provides a JFrame console as the main application to be run.
 * Subclasses will provide a public static void main(String[]) and any associated methods 
 * that will know how to "run" their specific Java application in this JVM with 
 * the JFrame console this class automatically provides.
 * <p>
 * This is done for cases when Eclipse and other processes want to launch a Java Process but have 
 * no visible console.
 * <p>
 * Example usage (from org.safs.selenium.utils.SeleniumServerRunner):
 * <pre>
 * public class SeleniumServerRunner extends JavaJVMConsole{
 * 
 * 	public static void main(String[] args) {
 *   
 *	    String[] passArgs = new String[0];
 *		SeleniumServerRunner console;
 *		try{
 *		    console = new SeleniumServerRunner();
 *		    passArgs = processArgs(args); // -jar path/To/selenium-server-standalone.jar passed in args
 *		    Class aclass = Class.forName("org.openqa.grid.selenium.GridLauncher");
 *		    Method main = aclass.getMethod("main", String[].class);
 *		    main.invoke(null, new Object[]{passArgs});
 *		}
 *		catch(Throwable everything){ ...
 * </pre>
 * @author canagl
 * @author CANAGL NOV 19, 2014 Fix performance issues slowing down contained processes.
 */
public abstract class JavaJVMConsole extends JFrame implements Runnable, ActionListener{

	/** JTextArea containing the text data normally sent to System.out and System.err.
	 *  It may not have ALL of the data if the output has exceeded the 200 lines provided.*/
	protected JTextArea display = null;
	
	/** Set this true to allow the monitoring thread to shutdown.
	 *  This would normally be done by subclasses that actually monitor external processes.
	 *  For example, when launching and providing a console for some other process--like STAF. 
	 *  In that scenario, we have to monitor the process to know when it has exited so that 
	 *  we know we can shutdown our Console window and this Java JVM. */
	protected boolean shutdown = false;
	
	/**
	 * As the STANDARD out/err has been redirected to this JavaJVMConsole, we need a way to
	 * get the 'execution message' on STANDARD out/err.<br>
	 * If outputToConsole is true, the 'execution message' will also be printed to STANDARD out/err.<br>
	 */
	protected boolean outputToConsole = false;

	/** The default time (in milliseconds) to sleep before the out/err output-stream is ready*/
	public static final int PAUSE_BEFORE_OUTPUT_READY = 100;
	/**
	 * This field represents the time (in milliseconds) to sleep before the out/err output-stream is ready.<br>
	 * The default value is 100 milliseconds.<br>
	 */
	protected int pauseBeforeReady = PAUSE_BEFORE_OUTPUT_READY;
	
	/**
	 * Limit the number of text lines kept and displayed to the numberOfRows in the textarea.
	 * When the number of lines exceeds the number of rows the oldest line is removed as 
	 * the newest line is added.
	 */
	public static final int KEEP_MODE_FIFO = 1;
	
	protected int numberOfrows    = 200; // max number of rows to retain in textarea.
	protected int keep_mode = KEEP_MODE_FIFO; // FUTURE: modes that might change FIFO to ALL TEXT, etc..	
	static final String nl = "\n";
	
	public static final String MENU_FILE = "File";
	public static final String MENU_ITEM_SAVE = "Save";
	public static final String MENU_ITEM_EXIT = "Exit";
	
	public static final String MENU_EDIT = "Edit";
	public static final String MENU_ITEM_CLEAR = "Clear";

	public static final String MENU_SEARCH = "Search";
	public static final String MENU_ITEM_FIND = "Find";
	public static final String MENU_ITEM_FIND_NEXT = "Find Next";
	public static final String MENU_ITEM_FIND_PREVIOUS = "Find Previous";
	
	public static final boolean DEFAULT_SEARCH_MATCH_CASE = true;
	
	/** '-1' means that the token is not found. */
	private static final int INVALID_POSITION = -1;
	
	/** The text to search in the text area 'display'. */
	protected String token = null;
	/** The position of the token found last time in the text area 'display'. */
	protected int lastFoundPosition = INVALID_POSITION;
		
	/** '-state' specifies the console window's state.<br>
	 * "-state MIN|MAX|NORMAL|MINIMIZE|MAXIMIZE"<br>
	 */
	public static final String PARAM_STATE = "-state";
	
	public static final String STATE_MAX 		= "MAX";
	public static final String STATE_MAXIMIZE 	= "MAXIMIZE";
	public static final String STATE_MIN 		= "MIN";
	public static final String STATE_MINIMIZE 	= "MINIMIZE";
	public static final String STATE_NORMAL 	= "NORMAL";
	public static final String STATE_DEFAULT 	= STATE_NORMAL;

	/**
	 * The console winodw's state to set. The default is {@link JavaJVMConsole#STATE_NORMAL}.<br>
	 * It could also be {@link JavaJVMConsole#STATE_MAX} or {@link JavaJVMConsole#STATE_MIN}.<br>
	 */
	protected String state = STATE_DEFAULT;
	
	protected JavaJVMConsole(){
		super();
	}
	
	/**
	 * @param outputToConsole boolean, if true the message will also be output to Standard out/err.
	 */
	protected JavaJVMConsole(boolean outputToConsole){
		this();
		this.outputToConsole = outputToConsole;
	}
	
	/**
	 * @param outputToConsole boolean, if true the message will also be output to Standard out/err.
	 * @param state String, The console winodw's state to set. See {@link #state}.
	 */
	protected JavaJVMConsole(boolean outputToConsole, String state){
		this(outputToConsole);
		this.state = state;
	}
	
	/**
	 * Initialize the console's UI elements.<br>
	 * Start a thread to redirect the stdout/stderr.<br>
	 * This method <b>MUST</b> be called after calling the constructors.<br>
	 */
	public void init(){
		setTitle("Java Console");
		setSize(600,450);
		setMinimumSize(new Dimension(600,450));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
				
		display = new JTextArea();
		display.setEditable(false);
		display.setRows(numberOfrows); //JTextArea
		display.setAutoscrolls(true);		
//		DefaultCaret caret = (DefaultCaret)display.getCaret();
		DefaultCaret caret = new DefaultCaret(){
			//override so that the caret will be shown even the 'text area' is not editable
		    public void focusGained(FocusEvent e) {
		        if (display.isEnabled()) {
		            setVisible(true);
		            setSelectionVisible(true);
		        }
		    }
		};
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		caret.setBlinkRate(500);
		display.setCaret(caret);		
		display.setCaretColor(java.awt.Color.WHITE);
		
		display.setFont(display.getFont().deriveFont(Font.BOLD));
		display.setBackground(java.awt.Color.BLACK);
		display.setForeground(java.awt.Color.GREEN);
		
		display.addMouseListener(new MouseAdapter(){
			 public void mouseClicked(MouseEvent e){
				 clearHighLight(display, null);
			 }
		});
		
		JMenuBar mbar = new JMenuBar();
		JMenu mFile = new JMenu(MENU_FILE);
		JMenu mEdit = new JMenu(MENU_EDIT);
		JMenu mSearch = new JMenu(MENU_SEARCH);
		mFile.setMnemonic(KeyEvent.VK_F);
		mEdit.setMnemonic(KeyEvent.VK_E);
		mSearch.setMnemonic(KeyEvent.VK_S);
		
		mFile.add(createJMenuItem(MENU_ITEM_SAVE, 0, KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		mFile.add(createJMenuItem(MENU_ITEM_EXIT, 0, KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		
		mEdit.add(createJMenuItem(MENU_ITEM_CLEAR, 4, KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		
		mSearch.add(createJMenuItem(MENU_ITEM_FIND, 0, KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		mSearch.add(createJMenuItem(MENU_ITEM_FIND_NEXT, 5, KeyEvent.VK_F3, 0));
		mSearch.add(createJMenuItem(MENU_ITEM_FIND_PREVIOUS, 5, KeyEvent.VK_F3, ActionEvent.SHIFT_MASK));
		
		mbar.add(mFile);
		mbar.add(mEdit);
		mbar.add(mSearch);
		add(mbar, BorderLayout.NORTH);
		
		JScrollPane propsscroll = new JScrollPane(display);
		propsscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		propsscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(propsscroll, BorderLayout.CENTER);
				
		setVisible(true);
		setState(state);
		
		Thread runner = new Thread(this);
		runner.setDaemon(true);
		runner.start();
	}

	/** Minimize the console window. */
	public void minimize(){
		this.setState(JFrame.ICONIFIED);
	}
	
	/** Restore the console window to its normal size. */
	public void restore(){
		this.setState(JFrame.NORMAL);
	}
	
	/** Maximize the console window. */
	public void maximize(){
		this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH);
	}
	
	/**
	 * Set the console window's state. Maximized, Minimized or Normal.<br>
	 * @param state String, the state to set. It could be "MAX", "MIN" or "NORMAL".
	 */
	protected void setState(String state){
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg+" set state '"+state+"' to this JVMConsole.");
		if(state==null) return;
		
		if(STATE_MAXIMIZE.equalsIgnoreCase(state)||STATE_MAX.equalsIgnoreCase(state)) maximize();
		else if(STATE_MINIMIZE.equalsIgnoreCase(state)||STATE_MIN.equalsIgnoreCase(state)) minimize();
		else if(STATE_NORMAL.equalsIgnoreCase(state)) restore();
		else{
			IndependantLog.warn(debugmsg+" state '"+state+"' is NOT valid, ignore it.");
		}
	}
	
	/**
	 * Create menu item with mnemonic and accelerate key.<br>
	 * Add ActionListener for the menu item.<br>
	 * 
	 * @param name					String, the menu item's name
	 * @param mnemonicKeyIndex		int, the mnemonic key's index in the menu item's name.
	 * @param accelerateKey			int, the accelerate key.
	 * @param accelerateModifier	int, the accelerate modifier. 
	 *                                   It could be 'ctrl', 'shift', 'alt' defined ActionEvent; 0 means no modifier.
	 * @return JMenuItem
	 */
	protected JMenuItem createJMenuItem(String name, int mnemonicKeyIndex, int accelerateKey, int accelerateModifier){
		JMenuItem item = new JMenuItem(name);
		item.setMnemonic(name.charAt(mnemonicKeyIndex));
		item.setDisplayedMnemonicIndex(mnemonicKeyIndex);
		item.setAccelerator(KeyStroke.getKeyStroke(accelerateKey, accelerateModifier));
		item.addActionListener(this);
		return item;
	}
	
	public void actionPerformed(ActionEvent event){
		String command = event.getActionCommand();
		String debugmsg = StringUtils.debugmsg(false);
		
		//Treat menu's commands
		switch (command){
		case MENU_ITEM_SAVE:
			final JFileChooser fc = new JFileChooser();

			int rc = fc.showSaveDialog(this);
			if(JFileChooser.APPROVE_OPTION==rc){
				File file = fc.getSelectedFile();
				try {
					FileUtilities.writeStringToUTF8File(file.getCanonicalPath(), display.getText());
				} catch (Exception e) {
					IndependantLog.error(debugmsg+ " Met "+StringUtils.debugmsg(e));
				}
			}
			break;
			
		case MENU_ITEM_EXIT:
			System.exit(0);
			break;
			
		case MENU_ITEM_CLEAR:
			//Stop writing to standard out/err
			if(outputToConsole) outputToConsole=false;
			//clear the panel
			display.setText(null);
			//How to clear memory, System.gc() does not really work.
			break;
			
		case MENU_ITEM_FIND:
			if(showSearchDialog()){
				highlightInDispaly(true);
			}
			break;
			
		case MENU_ITEM_FIND_NEXT:
			highlightInDispaly(true);
			break;
			
		case MENU_ITEM_FIND_PREVIOUS:
			highlightInDispaly(false);
			break;
			
		default:
			IndependantLog.warn(debugmsg+"'"+command+"' was not handled!");
			System.out.println(command);
			break;
		}
		
	}
	
	/** The combo-box for containing the search history in search dialog. */
	protected JComboBox<String> tokenCombobox = null;
	/** The check-box to tell if the search is case sensitive in search dialog. */
	protected JCheckBox tokenMatchCaseCheckbox = null;
	/** The array containing the components show in search dialog.  */
	private JComponent[] searchDialogComponents = null;
	
	/**
	 * Show the dialog for searching token.<br>
	 * 
	 * @return boolean, true if the 'OK' button of the dialog has been clicked and an item has been selected in the combo box 'tokenCombobox'.
	 */
	protected boolean showSearchDialog(){
		try{
			if(tokenCombobox==null){
				tokenCombobox = new JComboBox<String>();
				tokenCombobox.setEditable(true);
				//Change model so that the combo-box will contain no duplicate item
				tokenCombobox.setModel(new DefaultComboBoxModel<String>(){
					public void addElement(String o) {
						if (this.getIndexOf(o) == -1)
							super.addElement(o);
					}
				});
				//After editing the checkbox, the item will be added to combobox
				tokenCombobox.getEditor().addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						String item = (String) tokenCombobox.getEditor().getItem();					
						tokenCombobox.addItem(item);
						tokenCombobox.setSelectedItem(item);
					}
				});
			}
			if(tokenMatchCaseCheckbox==null){
				tokenMatchCaseCheckbox = new JCheckBox();
				tokenMatchCaseCheckbox.setSelected(DEFAULT_SEARCH_MATCH_CASE);
			}
			
			if(searchDialogComponents==null){
				JPanel panelFind = new JPanel();
				panelFind.add(new JLabel("Find what: "));
				panelFind.add(tokenCombobox);
				JPanel panelOptions = new JPanel();
				panelOptions.add(tokenMatchCaseCheckbox);
				panelOptions.add(new JLabel("Match Case"));
				searchDialogComponents = new JComponent[] { panelFind, panelOptions};
			}
						
			if(JOptionPane.showConfirmDialog(this, searchDialogComponents, "Find String", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
				Object selectedItem = tokenCombobox.getSelectedItem();
				if(selectedItem!=null){
					token = selectedItem.toString();
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
			return false;
		}
	}
	
	/**
	 * Find a token within Text Area 'display', if found then highlight it.<br>
	 * 
	 * @param forwardSearch	boolean, search forward or backward
	 * @return	boolean true if the token is found.
	 */
	protected boolean highlightInDispaly(boolean forwardSearch){
		boolean found = false;
		String debugmsg = "JavaJVMConsole.highlightInDispaly(): ";

		if (StringUtils.isValid(token)) {
			//Focus the text area, otherwise the highlighting won't show up
			display.requestFocusInWindow();
			Document document = display.getDocument();
			int tokenLength = token.length();
			int documentLength = document.getLength();
			boolean matchCase = tokenMatchCaseCheckbox==null? DEFAULT_SEARCH_MATCH_CASE:tokenMatchCaseCheckbox.isSelected();
			
			//The caret stay where user finds the token last time
			//or user moves the caret to the place where he wants the search begin
			int pointer = display.getCaretPosition();
			
			try {
				// Reset the search position if we're at the end of the document
				if(forwardSearch){
					if(pointer==lastFoundPosition) pointer++;//we don't want to stay at the original one
					
					//not long enough to find another one, rewind the pointer to the beginning for new search
					if (pointer+tokenLength > documentLength) pointer = 0;

					while ( pointer+tokenLength <= documentLength ) {
						if(search(document, pointer, token, matchCase)){
							found = true;
							break;
						}
						//move one step forward
						pointer++;
					}
				}else{
					if(pointer==lastFoundPosition) pointer--;//we don't want to stay at the original one
					//not possible to find another one, rewind the pointer to the end for new search
					if (pointer < 0) pointer = documentLength-tokenLength;
					while ( pointer >= 0 ) {
						if(search(document, pointer, token, matchCase)){
							found = true;
							break;
						}
						//move one step backward
						pointer--;
					}
				}

				//Clear the previous highlight
				clearHighLight(display, null);
				
				if(found) {
					lastFoundPosition = pointer;
					highLight(display, lastFoundPosition, tokenLength);
					display.setCaretPosition(lastFoundPosition);
				}else{
					lastFoundPosition = INVALID_POSITION;
					//reset the position for new search, which will also clear the highlight in previous search :-)
					display.setCaretPosition(forwardSearch? 0:documentLength);
				}

			} catch (Exception e) {
				IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
			}
		}else{
			IndependantLog.warn(debugmsg+"'"+token+"' is not a valid string! It is null or empty.");
		}

		return found;
	}
	
	/**
	 * Search a token within a Document, return true if found.<br>
	 * 
	 * @param document	Document, within which to find a match.
	 * @param position	int, the start position to find a match.
	 * @param token		String, the token to match.
	 * @param caseSensitive	boolean, if true the token will be matched case sensitively.
	 * @return	boolean true if found.
	 */
	public static boolean search(Document document, int position, String token, boolean caseSensitive){
		String debugmsg = "JavaJVMConsole.search(): ";
		try {
			String tempToken = document.getText(position, token.length());
			return (caseSensitive? token.equals(tempToken):token.equalsIgnoreCase(tempToken));
		} catch (BadLocationException e) {
			IndependantLog.warn(debugmsg+"Does not match, met "+StringUtils.debugmsg(e));
			return false;
		}
	}
	
	/**
	 * Highlight some text in the text area.<br>
	 * 
	 * @param display	JTextArea, the text area where to highlight some text.
	 * @param position	int, the beginning position to start the highlight.
	 * @param length	int, the length of text to highlight.
	 */
	public static void highLight(JTextArea display, int position, int length){
		String debugmsg = "JavaJVMConsole.highLight(): ";
		try {
			//Get the rectangle where the text is shown
			Rectangle viewRect = display.modelToView(position);
			//Make the text visible
			display.scrollRectToVisible(viewRect);
			
			Highlighter h = display.getHighlighter();
			h.addHighlight(position, position+length, new DefaultHighlightPainter(java.awt.Color.RED));
		} catch (BadLocationException e) {
			IndependantLog.warn(debugmsg+"Can not highlight, met "+StringUtils.debugmsg(e));
		}
	}
	
	/**
	 * Clear highlight in the text area.<br>
	 * 
	 * @param display	JTextArea, the text area where to highlight some text.
	 * @param tag		Object, the tag name of highlight to clear.
	 */
	public static void clearHighLight(JTextArea display, Object tag){
		String debugmsg = "JavaJVMConsole.clearHighLight(): ";
		try {
			Highlighter h = display.getHighlighter();
			if(tag==null){
				h.removeAllHighlights();
			}else{
				h.removeHighlight(tag);
			}
		} catch (Exception e) {
			IndependantLog.warn(debugmsg+"Can not clear highlight, met "+StringUtils.debugmsg(e));
		}
	}

    /**
     * Parameters of the method to add a URL to the System runtime classpath. 
     */
    private static final Class<?>[] parameters = new Class[]{URL.class};

    /**
     * Adds a file to the System runtime classpath.<br>
     * Calls addFile(File).
     * @param s a String fullpath pointing to the file
     * @throws IOException
     * @see #addFile(File)
     * @see #addURL(URL)
     */
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Adds a File to the System runtime classpath.<br>
     * Calls addURL(URL)
     * @param f the file to be added
     * @throws IOException
     * @see #addFile(String)
     * @see #addURL(URL)
     */
    public static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    /**
     * Adds the content pointed by the URL to the System runtime classpath.
     * @param u the URL pointing to the content to be added
     * @throws IOException
     * @see #addFile(String)
     * @see #addFile(File)
     */
    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ u }); 
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }        
    }
    
    /**
     * DO NOT USE. Poor Performance.<p>
     * Displays the message in the text area. 
     * A newline is added to the message so each is printed on a separate line.
     * Limits the number of lines displayed to the last numberOfrows via KEEP_MODE_INFO as keep_mode.
     * Right now, setting any other keep_mode value will retain and append ALL messages received 
     * without limiting the size displayed, or the amount of memory used.
     * @param message
     */
    protected void displayLine(String message){
    	// infinite loop //System.out.println(message);
    	String line = message + nl;
    	if(keep_mode == KEEP_MODE_FIFO){
	    	if(display.getLineCount()==numberOfrows){
	    		try{ display.replaceRange(null, 0, display.getLineStartOffset(1)-1);}
	    		catch(Exception x){display.append(x.getClass().getName()+", "+ x.getMessage());}
	    	}
	        display.append(line);
    	}
    }    
    
    /**
     * @return int, The time (in milliseconds) to sleep before the out/err output-stream is ready.<br> 
     * @see #run()
     */
	public int getPauseBeforeReady() {
		return pauseBeforeReady;
	}
	/**
	 * @param pauseBeforeReady int, The time (in milliseconds) to sleep before the out/err output-stream is ready.<br>
	 * @see #run()
	 */
	public void setPauseBeforeReady(int pauseBeforeReady) {
		this.pauseBeforeReady = pauseBeforeReady;
	}

	/**
	 * Continuously monitors the JVM out and err streams routing them to the local JFrame display. 
	 * Will run indefinitely until the JVM exits unless a subclass sets the "shutdown" field to true.
	 * If JFrame.EXIT_ON_CLOSE (default) is true then closing the JFrame will also terminate the JVM process.
	 * This is the default behavior.<br>
	 * However, JVM Consoles that are actually monitoring a separate Process must monitor that process and 
	 * should set the protected "shutdown" field to true when that Process has exited.
	 */
	public void run(){		
		PrintStream StandardSystemErr = System.err;
		PrintStream StandardSystemOut = System.out;

		try{
			//Handle System.out
			PipedOutputStream pipeOut = new PipedOutputStream();
			PipedInputStream outPipe = new PipedInputStream(pipeOut);
			System.setOut(new PrintStream(pipeOut));
			BufferedReader out = new BufferedReader(new InputStreamReader(outPipe), 5242880);
	
			//Handle System.err
			PipedOutputStream pipeErr = new PipedOutputStream();
			PipedInputStream errPipe = new PipedInputStream(pipeErr);
			System.setErr(new PrintStream(pipeErr));		
			BufferedReader err = new BufferedReader(new InputStreamReader(errPipe), 5242880);

			String message = null;
			while(!shutdown){
				if(!err.ready() && !out.ready()){
					try{ Thread.sleep(pauseBeforeReady);} catch(Exception e){ /*We don't really care about*/ }
				}
				if (err.ready()) {
					message = err.readLine();
					displayLine(message);
					if(outputToConsole) StandardSystemErr.println(message);
				}
				if (out.ready()) {
					message = out.readLine();
					displayLine(message);
					if(outputToConsole) StandardSystemOut.println(message);
				}				
			}
			displayLine("JavaJVMConsole shutdown.");
			if(outputToConsole) StandardSystemOut.println("JavaJVMConsole shutdown.");
		}
		catch(Exception x){
			String message = "JavaJVMConsole.run() thread loop error:"+ x.getMessage(); 
			displayLine(message);
			if(outputToConsole) StandardSystemErr.println(message);
		}
	}
	
	//The 2 following constants have been moved to DriverConstant.
	/** 'safs.rmi.server'<br>
	 * JVM command line: -Dsafs.rmi.server=org.safs.selenium.rmi.server.SeleniumServer */
	public static final String PROPERTY_RMISERVER = DriverConstant.PROPERTY_RMISERVER;
	
	/** 'org.safs.selenium.rmi.server.SeleniumServer' */
	public static final String DEFAULT_RMISERVER_CLASSNAME = DriverConstant.DEFAULT_RMISERVER_CLASSNAME;
}
