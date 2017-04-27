package org.safs.install;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

/**
 * A Panel to show the progress of the installation.<br>
 * It includes 2 parts, one is the 'progress bar' to show the progress;<br>
 * the other is a 'text area' to show the progress information.<br>
 */
public class ProgressIndicator extends JPanel{
	private static final long serialVersionUID = 8138054266048245250L;
	static final String EOL = System.getProperty("line.separator");

	private JProgressBar progressBar;
	private JTextPane taskOutput;
	private JTextField currentTask;
	private JScrollPane scroller;
	JFrame frame = null;

	Dimension screensize = getToolkit().getScreenSize();
	int screenWidth = screensize.width;
	int screenHeight = screensize.height;
	String title = "Robotium RC Installation.";

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
		taskOutput = new JTextPane();
		taskOutput.setPreferredSize(new Dimension(screenWidth/3, screenHeight/3));
		taskOutput.setMargin(new Insets(2, 5, 5, 5));
		taskOutput.setEditable(false);
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
		currentTask.setText(message);
		taskOutput.setText(taskOutput.getText()+message+EOL);
		System.out.println(message);
	}

	/**
	 * setProgress and setProgressMessage in one call.
	 * @param progress
	 * @param message
	 */
	public void setProgressInfo(int progress, String message){
		setProgress(progress);
		setProgressMessage(message);
	}

	public void close(){
		if(frame!=null){
			frame.dispose();
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
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
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
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
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
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
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
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
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
		progressor.setProgressMessage("TextArea size "+ index++ +": "+ progressor.taskOutput.getSize());
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
