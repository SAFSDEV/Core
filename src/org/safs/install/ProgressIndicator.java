package org.safs.install;
/**
 * APR 27, 2017 (SBJLWA) Improved to show message in different color according to log level.
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.safs.Constants.LogConstants;

/**
 * A Panel to show the progress of the installation.<br>
 * It includes 2 parts, one is the 'progress bar' to show the progress;<br>
 * the other is a 'text area' to show the progress information.<br>
 */
public class ProgressIndicator extends JPanel{
	private static final long serialVersionUID = 8138054266048245250L;
	private static final String EOL = System.getProperty("line.separator");

	private JProgressBar progressBar;
	private JTextPane taskOutput;
	private JTextField currentTask;
	private JScrollPane scroller;
	private JFrame frame = null;

	private Map<JComponent, Font> origianlFontMap = new HashMap<JComponent, Font>();
	private Map<JComponent, Color> origianlForegroundMap = new HashMap<JComponent, Color>();

	private Dimension screensize = getToolkit().getScreenSize();
	private int screenWidth = screensize.width;
	private int screenHeight = screensize.height;
	private String title = "Robotium RC Installation.";

	public ProgressIndicator() {
		super(new BorderLayout());

		progressBar = new JProgressBar(0, 100);
		Dimension d = progressBar.getPreferredSize();
		d.width = screenWidth/3;
		progressBar.setPreferredSize(d);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);

		currentTask = new JTextField();
		currentTask.setPreferredSize(new Dimension(screenWidth/3, 28));
		currentTask.setMargin(new Insets(5, 5, 2, 5));
		currentTask.setEditable(false);
		saveOriginalLookStyle(currentTask);

		taskOutput = new JTextPane();
		taskOutput.setPreferredSize(new Dimension(screenWidth/3, screenHeight/3));
		taskOutput.setMargin(new Insets(2, 5, 5, 5));
		taskOutput.setEditable(false);
		taskOutput.setBackground(Color.BLACK);
		taskOutput.setForeground(Color.GREEN);
		saveOriginalLookStyle(taskOutput);
		// Set auto scroll
		DefaultCaret caret = (DefaultCaret)taskOutput.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel panel = new JPanel();
		panel.add(progressBar);
		add(panel, BorderLayout.PAGE_START);
		add(currentTask, BorderLayout.CENTER);
		scroller = new JScrollPane();
		scroller.setViewportView(taskOutput);
		add(scroller, BorderLayout.SOUTH);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}

	public ProgressIndicator(String title) {
		this();
		setTitle(title);
	}

	/**
	 * Change the title to be used on the display ProgressIndicator titlebar.
	 * @param title
	 */
	public void setTitle(String title){
		this.title = title;
		if(frame != null) frame.setTitle(title);
	}

	/**
	 * Create the GUI and show it. As with all GUI code, this must run on
	 * the event-dispatching thread.
	 */
	public void createAndShowGUI() {
		// Create and set up the window.
		frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setOpaque(true); // content panes must be opaque
		frame.setContentPane(this);

		// Display the window.
		int frameWidth = (int) frame.getPreferredSize().getWidth();
		int frameHeight = (int) frame.getPreferredSize().getHeight();
		frame.setLocation((screenWidth-frameWidth)/2,(screenHeight-frameHeight)/2);
		frame.pack();
		frame.setVisible(true);

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	public void setProgress(int progress){
		progressBar.setValue(progress);
	}

	public void setProgressMessage(String message){
		setProgressMessage(message, LogConstants.PASS);
	}
	public void setProgressMessage(String message, int logLevel){

		message = "["+LogConstants.getLogLevelName(logLevel)+"]\t"+message;

		//1. output message to the text field
		setLookStyle(currentTask, logLevel);
		currentTask.setText(message);
		//2. output message to the text area
		Document document = taskOutput.getDocument();
		int len = document.getLength();
		try {
			document.insertString(len, message+EOL, generateLookStyle(taskOutput, logLevel));
		} catch (BadLocationException e) {
			System.err.println("Failed to add message to TextArea, due to "+e.toString());
		}
		//3. output message to the console
		if(LogConstants.ERROR==logLevel){
			System.err.println(message);
		}else{
			System.out.println(message);
		}
	}

	/**
	 * @param component JComponent, save its original look style such as font/foreground into a cache.
	 */
	protected void saveOriginalLookStyle(JComponent component){
		origianlFontMap.put(component, component.getFont());
		origianlForegroundMap.put(component, component.getForeground());
	}

	private static final String ATTRIBUTE_KEY_FONT = "ATTRIBUTE_KEY_FONT";

	/**
	 * @param component JComponent, get its original font/foreground etc.
	 * @param logLevel int, the log level
	 * @return AttributeSet
	 */
	protected AttributeSet generateLookStyle(JComponent component, int logLevel){
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		if(LogConstants.ERROR==logLevel){
			StyleConstants.setBold(attributes, true);
			StyleConstants.setForeground(attributes, Color.RED);

			attributes.addAttribute(ATTRIBUTE_KEY_FONT, component.getFont().deriveFont(Font.BOLD));
		}else if(LogConstants.WARN==logLevel){
			StyleConstants.setBold(attributes, true);
			StyleConstants.setForeground(attributes, Color.YELLOW);

			attributes.addAttribute(ATTRIBUTE_KEY_FONT, component.getFont().deriveFont(Font.BOLD));
		}else{
			//Set original font and foreground etc.
			Font font = origianlFontMap.get(component);
			StyleConstants.setFontFamily(attributes, font.getFamily());
			StyleConstants.setFontSize(attributes, font.getSize());
			StyleConstants.setBold(attributes, font.isBold());
			StyleConstants.setItalic(attributes, font.isItalic());
			StyleConstants.setForeground(attributes, origianlForegroundMap.get(component));

			//Add the original font into attribute
			attributes.addAttribute(ATTRIBUTE_KEY_FONT, font);
		}

		return attributes;
	}

	/**
	 * Set the component's look style according to the log level.
	 * @param component JComponent, the component to set look style
	 * @param logLevel int, the log level
	 */
	protected void setLookStyle(JComponent component, int logLevel){
		AttributeSet attributes = generateLookStyle(component, logLevel);
		component.setForeground(StyleConstants.getForeground(attributes));
		component.setFont((Font)attributes.getAttribute(ATTRIBUTE_KEY_FONT));
	}

	/**
	 * setProgress and setProgressMessage in one call.
	 * @param progress int, the progress between 0 and 100
	 * @param message String, the message to show
	 */
	public void setProgressInfo(int progress, String message){
		setProgress(progress);
		setProgressMessage(message);
	}

	/**
	 * @param progress int, the progress between 0 and 100
	 * @param message String, the message to show
	 * @param logLevel int, the log level
	 */
	public void setProgressInfo(int progress, String message, int logLevel){
		setProgress(progress);
		setProgressMessage(message, logLevel);
	}

	/** Close this indicator. */
	public void close(){
		if(frame!=null){
			frame.dispose();
		}
	}
	/** Minimize this indicator. */
	public void minimize(){
		if(frame!=null && frame.getExtendedState()!=Frame.ICONIFIED){
			frame.setExtendedState(Frame.ICONIFIED);
		}
	}
	/** Maximize this indicator. */
	public void maximize(){
		if(frame!=null && frame.getExtendedState()!=Frame.MAXIMIZED_BOTH){
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		}
	}
	/** Restore this indicator to its original state. */
	public void restore(){
		if(frame!=null && frame.getExtendedState()!=Frame.NORMAL){
			frame.setExtendedState(Frame.NORMAL);
		}
	}

	/* for testing */
	public static void main(String[] args){

		int delay = 300;
		int index = 0;
	    final ProgressIndicator progressor = new ProgressIndicator();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	progressor.createAndShowGUI();
            }
        });
		progressor.setProgress(5);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(3000);}catch(Exception ignore){}
		progressor.setProgress(10);
		progressor.setProgressMessage("TextArea size "+index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(15);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize(), LogConstants.ERROR);
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(20);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(25);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(30);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(35);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize(), LogConstants.PASS);
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(40);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(45);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(50);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(55);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize(), LogConstants.ERROR);
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(60);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(65);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(70);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(75);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(80);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize(), LogConstants.WARN);
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(85);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(90);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(95);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(96);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(97);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize(), LogConstants.PASS);
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(98);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(99);
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
		try{Thread.sleep(delay);}catch(Exception ignore){}
		progressor.setProgress(100);
		progressor.setProgressMessage("Now we are done!");
		try{Thread.sleep(3000);}catch(Exception ignore){}
	    progressor.close();
	    System.exit(0);
	}
}
