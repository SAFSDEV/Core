/**
 * History:
 * NOV 09, 2015	(sbjlwa) Modify run(): avoid the problem of occupy CPU too much.
 * 
 */
package org.safs.tools.consoles;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

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
public abstract class JavaJVMConsole extends JFrame implements Runnable{

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
	
	protected JavaJVMConsole(){
		super();
		setTitle("Java Console");
		setSize(555,450);
		setMinimumSize(new Dimension(555,450));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
				
		display = new JTextArea();
		display.setEditable(false);
		display.setRows(numberOfrows); //JTextArea
		display.setAutoscrolls(true);		
		DefaultCaret caret = (DefaultCaret)display.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);	
		display.setBackground(java.awt.Color.BLACK);
		display.setForeground(java.awt.Color.WHITE);
		
		JScrollPane propsscroll = new JScrollPane(display);
		propsscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		propsscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(propsscroll, BorderLayout.CENTER);
		setVisible(true);	
		Thread runner = new Thread(this);
		runner.setDaemon(true);
		runner.start();		
	}
	
	/**
	 * 
	 * @param outputToConsole boolean, if true the message will also be output to Standard out/err.
	 */
	protected JavaJVMConsole(boolean outputToConsole){
		this();
		this.outputToConsole = outputToConsole;
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
	 * Continuously monitors the jvm out and err streams routing them to the local JFrame display. 
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
			if(outputToConsole) StandardSystemOut.println("JavaJVMConsole shutdown.");
		}
		catch(Exception x){
			String message = "JavaJVMConsole.run() thread loop error:"+ x.getMessage(); 
			displayLine(message);
			if(outputToConsole) StandardSystemErr.println(message);
		}
	}    	
}
