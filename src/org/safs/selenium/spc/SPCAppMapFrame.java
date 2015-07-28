/**
 * 
 */
package org.safs.selenium.spc;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author canagl
 *
 */
public class SPCAppMapFrame extends JFrame{

	private JFileChooser jfc_savemap;
	private JProgressBar jpb_bar;
	private JTextArea maptxt = new JTextArea("");
	private JScrollPane mapscroller = new JScrollPane(maptxt);
	private JPanel btnpanel;
	private JButton savebtn;
	private JButton cancelbtn;
	
	private int maxProgress = 0;
	static final int MIN_WIDTH = 450;
	static final int MIN_HEIGHT = 400;

	// hide
	private SPCAppMapFrame(){}
	
	/**
	 * 
	 * @param maxExpectedEntries if <= 0 then defaults to 100
	 * @param parent to be associated and centered.
	 */
	public SPCAppMapFrame(int maxExpectedEntries, Component parent){
		super("App Map Entries");
		setLayout(new FlowLayout(FlowLayout.LEFT));
		maxProgress = maxExpectedEntries > 0 ? maxExpectedEntries : 100;
		jpb_bar = new JProgressBar(0, maxProgress);
		jpb_bar.setValue(0);
		jpb_bar.setPreferredSize(new Dimension(MIN_WIDTH-25, 18));
		Rectangle bounds = (parent == null)? new Rectangle(200,200,MIN_WIDTH,MIN_HEIGHT): parent.getBounds();
		setBounds(bounds.x+bounds.width/2-125, bounds.y+bounds.height/2-25, MIN_WIDTH, MIN_HEIGHT);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		jpb_bar.setStringPainted(true);
	
		maptxt.setEditable(true);
		maptxt.setAutoscrolls(true);
		maptxt.setToolTipText("The recognition information to be stored in the App Map.");
		mapscroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mapscroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		mapscroller.setPreferredSize(new Dimension(MIN_WIDTH-25,MIN_HEIGHT-115));
		
		savebtn = new JButton("Save");
		savebtn.setToolTipText("Click to Save these recognition strings to an App Map.");
		savebtn.setVisible(true);
		savebtn.addActionListener((new ActionListener(){
			public void actionPerformed(ActionEvent event){
				saveMap();
			}
		}));
		btnpanel = new JPanel(new FlowLayout());
		btnpanel.setVisible(true);
		btnpanel.setPreferredSize(new Dimension(MIN_WIDTH-25, 32));
		btnpanel.add(savebtn);
		add(jpb_bar);
		add(mapscroller);
		add(btnpanel);
		setVisible(true);
	}

	/** set the progress var to the desired value. 
	 * This should be between 0 and maxExpectedEntries used at creation. */
	public void setProgress(int value){
		if (value < 1 || value > maxProgress) return;
		jpb_bar.setValue(value);
	}
	
	/**
	 * Append the entry unmodified to current contents of the App Map Frame text.
	 * @param entry
	 */
	public void appendAppMapEntry(String entry){
		if(entry != null && entry.length()>0) maptxt.append(entry);
	}
	
	/**
	 * @return the current text of the App Map Frame.  An empty String if there is no text.
	 */
	public String getAppMapText(){
		try { return maptxt.getText(); }
		catch(NullPointerException np){}
		return "";
	}
	
	/** 
	 * Likely called internally only once we have the UI to do the Save
	 */
	private void saveMap(){
		String homedir = System.getProperty("user.home");
		String filename = File.separator+ "SPCSaveAppMap.dir";
		boolean success = false;		
		File cdfile = null;
		String mappath = "";
		File tmpfile = new File(homedir + filename);//user "doc" directory?
		if(tmpfile.exists()&&tmpfile.canRead()){
			BufferedReader reader = null;
			try{
				reader = new BufferedReader(new FileReader(tmpfile.getAbsolutePath()));
				mappath = reader.readLine();
				reader.close();
			}catch(IOException nf){
				try{reader.close();}catch(Exception ignore){}
			}
		}
		if(mappath == null || mappath.length()==0){
			mappath = System.getProperty("user.dir");
		}
		cdfile = new File(mappath);
		
		jfc_savemap = new JFileChooser();
		jfc_savemap.setCurrentDirectory(cdfile);
		jfc_savemap.showSaveDialog(this);

		File selfile = jfc_savemap.getSelectedFile();
		if(selfile == null){
			return;
		}
		boolean proceed = true;
		// if the file already exists, prompt to confirm overwrite
		if(selfile.exists()&& (selfile.lastModified() < System.currentTimeMillis()-1750)){
			
			proceed = false;
			if(JOptionPane.showConfirmDialog(this,
					selfile.getAbsolutePath()+"\n\n"+
			        "File already exists.\n\n"+
			        "Do you wish to delete and replace the file\nwith this new content?\n\n",
					"Confirm File Overwrite",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE)== JOptionPane.YES_OPTION)
				proceed = true;
		}
		if(proceed){
			try{
				PrintWriter pw = new PrintWriter(selfile);
				pw.println(getAppMapText());
				pw.close();		
				success = true;
			}catch(FileNotFoundException x){
				JOptionPane.showMessageDialog(null,"Save may not have been successful due to "+
			            x.getClass().getSimpleName()+": "+x.getMessage(),
						"Save App Map Strings Problem",
						JOptionPane.ERROR_MESSAGE);
				success = false; //insure
			}
		}
		cdfile = jfc_savemap.getCurrentDirectory();
		if(cdfile.isDirectory()){
			BufferedWriter writer = null;
			try{
				writer = new BufferedWriter(new FileWriter(tmpfile.getAbsolutePath()));
				writer.write(cdfile.getAbsolutePath()+"\n");
				writer.flush();writer.close();
			}catch(IOException nf){
				JOptionPane.showMessageDialog(null,"Could not store the directory preference due to "+
			            nf.getClass().getSimpleName()+": "+nf.getMessage(),
						"Save App Map Directory Problem",
						JOptionPane.ERROR_MESSAGE);
				try{writer.close();}catch(Exception ignore){}
				// allow success to dispose() if set true
			}
		}		
		if(success) {
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			dispose();
		}
	}

	/**
	 * Used internally only for UI view during development.
	 * @param args
	 */
	public static void main(String[] args) {
		SPCAppMapFrame frame = new SPCAppMapFrame(50, null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
