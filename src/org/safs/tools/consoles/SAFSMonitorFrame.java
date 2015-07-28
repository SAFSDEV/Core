package org.safs.tools.consoles;

import javax.swing.*;

import org.safs.JavaHook;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSNullPointerException;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.STAFHelper;
import org.safs.TestRecordHelper;
import org.safs.staf.STAFProcessHelpers;
import org.safs.text.INIFileReadWrite;
import org.safs.tools.drivers.DefaultDriver;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.input.UniqueStringMapInfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;
import javax.swing.table.*;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * A class to monitor and potentially abort running SAFS tests.
 * This class leverages functionality built into the SAFS DefaultDriver.
 * The default driver monitors the SAFSVARS variables:
 * <ul>
 * 'SAFS_DRIVER_CONTROL'<br>
 * 'SAFS_DRIVER_CONTROL_POF'<br>
 * </ul>
 * The driver can be shutdown by setting 'SAFS_DRIVER_CONTROL' to 'SHUTDOWN_HOOK'.<br>
 * The driver can Pause On Failure by setting 'SAFS_DRIVER_CONTROL_POF' to 'ON'.<br>
 * @author canagl
 * @author JunwuMa SEP 27, 2010 Added step retry feature. 
 */
public class SAFSMonitorFrame extends JFrame implements ActionListener, KeyListener
{
	public static final String STOP_ACTION = "STOP";
	public static final String PAUSE_ACTION = "PAUSE";
	public static final String STEP_ACTION = "STEP";
	public static final String RUN_ACTION = "RUN";
	
	public static final String STEP_RETRY_ACTION = "STEP_RETRY";
	public static final String POF_ACTION        = "POF_ACTION";
	public static final String POW_ACTION        = "POW_ACTION";
	public static final String SAVE_EDIT_ACTION  = "SAVE_EDIT";
	
	public static final String STAF_PROCESS_ID = "SAFSMonitorFrame";
	
	STAFHelper staf = null;
	JButton stop = null;
	JButton pause = null;
	JButton step = null;
	JButton run = null;
	JPanel buttons = null;
	JTextField status = null;
	MonitorThread monitor = null;
	Thread monitorthread = null;
	
	JCheckBox switchOfPOF = null;             // checked: test will stop if Failure happens
	JCheckBox switchOfPOW = null;             // checked: test will stop if Warning happens
	JButton step_retry = null;
	JToggleButton weditToggle = null;         // switch to display/hide the content of current test record in WatchEdit table  
	
	JTable  watchTable = null;                // storing the fields of current test record for watch and edit
	DefaultTableModel watchTableModel = null; // the data modal that is applied to watchTable
	JScrollPane watchPane = null;    		  // a panel that contains watchTable
	
	/**
	 * 'Step Retry' allows users to edit a test record and run it again. The test under monitor will PAUSE_ON_FAILURE 
	 * automatically if checkbox POF is checked.
	 * 
	 * It uses SAFSVARS variables to pass in a test record from InputProcessor, and pass a modified test record 
	 * back to InputProcessor to try again. See STAFHelper.setSAFSTestRecordData(String, TestRecordData)
	 * When passing back to InputProcessor, the changes are stored as the following:
	 * STAF variables:
	 * safs/hook/appmapname   --- changed to ~debugmap.tmp to store modified R-Strings if deal with a "T" command
	 * safs/hook/inputrecord  --- store a modified input record 
	 * 
	 */
	
	/** <br><em>Purpose:</em>  stores current test record populated from STAF SAFS variables for watch and edit; 
	 *                         JavaHook.STEP_RETRY_EXECUTION in InputProcessor.  
	 * Its value is affected by two methods, resetCurTestRecordFromVar() and updateCurTestRecordFromGUI().
	 *      
	 **/
	private TestRecordHelper curtestRecordData = new TestRecordHelper(); 
	
	private String safs_driver_control_status = "";
	
	private debugAppMap mydebugAppMap = null;
	
	/**
	 * If shownOnFrontWhenPause is true, the monitor window will be shown on top when test state is PAUSE,
	 *                                   the monitor will be at back when state is not PAUSE.
	 * Otherwise, the monitor will stay at back whatever the test state is. 
	 */
	public static boolean shownOnFrontWhenPause = false;
	/**
	 * If the monitor is shown on top, this isTopWindow will be set to true.
	 */
	private boolean isTopWindow = false;
	
	/**
	 * A small monitor window allowing a user to abort SAFS tests run by a typical SAFS Driver.
	 *  
	 */
	public SAFSMonitorFrame()
	{
		this.setTitle("SAFS Monitor");
		this.setName(STAF_PROCESS_ID);
		this.getAccessibleContext().setAccessibleName(STAF_PROCESS_ID);
		
		buttons = new JPanel();

		stop = new JButton("STOP (F11)");
		stop.setActionCommand(STOP_ACTION);
		stop.addActionListener(this);
		stop.addKeyListener(this);

		pause = new JButton("Pause");
		pause.setActionCommand(PAUSE_ACTION);
		pause.addActionListener(this);
		pause.addKeyListener(this);
		
		run = new JButton("Run");
		run.setActionCommand(RUN_ACTION);
		run.addActionListener(this);
		run.addKeyListener(this);

		step = new JButton("Step");
		step.setActionCommand(STEP_ACTION);
		step.addActionListener(this);	
		step.addKeyListener(this);
		
		switchOfPOF = new JCheckBox("POF", false);
		switchOfPOF.setActionCommand(POF_ACTION);
		switchOfPOF.addActionListener(this);	
		switchOfPOF.addKeyListener(this);		
		switchOfPOW = new JCheckBox("POW", false);
		switchOfPOW.setActionCommand(POW_ACTION);
		switchOfPOW.addActionListener(this);	
		switchOfPOW.addKeyListener(this);		
		
		step_retry = new JButton("Step Retry");
		step_retry.setActionCommand(STEP_RETRY_ACTION);
		step_retry.addActionListener(this);	
		step_retry.addKeyListener(this);
		
		//JToggleButton to display/hide the content of current test record in WatchEdit table
		weditToggle = new JToggleButton ("Watch/Edit", false);
		weditToggle.setActionCommand(SAVE_EDIT_ACTION);
		weditToggle.addActionListener(this);	
		weditToggle.addKeyListener(this);
		//enlarge the width of this toggle button to make its text fully shown.
		Dimension pSize = weditToggle.getPreferredSize();
		pSize.setSize(pSize.getWidth()+10, pSize.getHeight());
		weditToggle.setPreferredSize(pSize);
		
		buttons.add(stop);
		buttons.add(pause);
		buttons.add(run);
		buttons.add(step);
		
		buttons.add(switchOfPOF);
		buttons.add(switchOfPOW);
		buttons.add(step_retry);
		buttons.add(weditToggle);
		
		//enlarge the width of this JPanel to make all its button shown
		pSize = buttons.getPreferredSize();
		pSize.setSize(pSize.getWidth()+40, pSize.getHeight());
		buttons.setPreferredSize(pSize);
		
		//define a render for setting cell's background color to show difference between editable and uneditable
		DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {
		public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
			{
			java.awt.Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if((row==0 || column==0) && cell.isBackgroundSet())
				cell.setBackground(new java.awt.Color(228,228,228)); // uneditable
			else
				cell.setBackground(java.awt.Color.WHITE);            // editable
			return cell;}
		};

		// watchTable storing fields in current test step for watch and edit
		watchTableModel = new DefaultTableModel(); 
		watchTableModel.addColumn("Variable");	
		watchTableModel.addColumn("Values");
		watchTable = new JTable(watchTableModel){
		      public boolean isCellEditable(int rowIndex, int vColIndex) {
		    	  // the first column representing variable names: uneditable
		    	  // the first row in the second column (0,1) : uneditable
		          return ((vColIndex == 0)? false:true) && (rowIndex != 0);
		        }
			};
		watchTable.getColumn("Variable").setCellRenderer(tcr);
		watchTable.getColumn("Values").setCellRenderer(tcr);
		watchTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); 	

		status = new JTextField("NOT REGISTERED");
		status.setEditable(false);
		status.addKeyListener(this);

		this.addKeyListener(this);
		watchPane = new JScrollPane(watchTable);
		getContentPane().add(buttons, "North");
		//watchPane can be hidden or shown by weditToggle
		//getContentPane().add(watchPane, BorderLayout.CENTER); 
		getContentPane().add(status, "South");

		pack();
		
		setFocusableWindowState(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		toBack();
		monitor = new MonitorThread();
		monitorthread = new Thread(monitor);
		monitorthread.setDaemon(true);
		monitorthread.start();
		setFocusableWindowState(true);
		
		init();
	}
	
	protected void init(){
		try {
			if (staf == null) registerSTAF();
			curtestRecordData.setSTAFHelper(staf);
		} catch(Exception sre) {
			processException(sre);
		}
	}
	
	/**
	 * set DriverInterface that is running the test. Retry is forbidden without calling this.
	 * @param driver, the DriverInterface that is running the test
	 */
	public void setDriver(DriverInterface driver){
		if (driver != null)
			mydebugAppMap = new debugAppMap(driver);
	}
	/**
	 * Attempt to register with STAF as 'SAFSMonitorFrame'
	 * @throws SAFSSTAFRegistrationException
	 */
	private void registerSTAF()throws SAFSSTAFRegistrationException{
		staf = new STAFHelper(STAF_PROCESS_ID);
	}
	
	/**
	 * Creates user-friendly messages from STAF Exceptions passed in.
	 * @param x Exception
	 */
	private void processException(Exception x){
		String rc = x.getMessage();
		String xtype = x.getClass().getSimpleName();
		if(xtype.equals(SAFSSTAFRegistrationException.class.getSimpleName())){
			status.setText("STAF not running.");
			staf = null;
			curtestRecordData.setSTAFHelper(null);
		}else if(xtype.equals(NullPointerException.class.getSimpleName())){
			status.setText("NOT REGISTERED? "+ xtype +", "+ x.getMessage());
			staf = null;
			curtestRecordData.setSTAFHelper(null);
		}else if(xtype.equals(SAFSException.class.getSimpleName())){
			if(rc.indexOf(": 2,")> 0){
				status.setText("SAFSVARS unavailable.");
			}else if(rc.indexOf(": 3,")> 0){
				status.setText("Acquiring STAF handle.");
				staf = null;
				curtestRecordData.setSTAFHelper(null);
			}else if(rc.indexOf(": 21,")> 0){
				status.setText("STAF not running.");
				staf = null;
				curtestRecordData.setSTAFHelper(null);
			}else {
				status.setText(x.getClass().getSimpleName());
			}
		}else {
			status.setText(x.getClass().getSimpleName());
		}
	}
	
	/**
	 * Set SAFS_DRIVER_CONTROL status information in the monitor window.
	 */
	public void setStatus(){
		try{
			if (staf == null) registerSTAF();
			 
			String current_status = staf.getVariable(DriverInterface.DRIVER_CONTROL_VAR);
			if(current_status.length()==0){
				current_status =  JavaHook.RUNNING_EXECUTION;
				staf.setVariable(DriverInterface.DRIVER_CONTROL_VAR, current_status);				
			}
			if ( !current_status.equalsIgnoreCase(safs_driver_control_status) &&
				  current_status.equalsIgnoreCase(JavaHook.PAUSE_EXECUTION) ) {
				//1)update current test record from STAF variables
				resetCurTestRecordFromVar();
				//2)show current test record for watch and edit if pause
				showCurTestRecordOnGUI();
				if (mydebugAppMap != null) { 
					step_retry.setEnabled(true); // enable step_retry for control
					weditToggle.setEnabled(true);
				}
				if(shownOnFrontWhenPause){
					toFront();
					isTopWindow = true;
				}
			} else {
				if (!current_status.equalsIgnoreCase(JavaHook.PAUSE_EXECUTION)) {
					// unable step_retry if not pause
					step_retry.setEnabled(false);
					weditToggle.setEnabled(false);
					if(shownOnFrontWhenPause && isTopWindow){
						toBack();
						isTopWindow = false;
					}
				}
			}
			
			safs_driver_control_status = current_status;
			status.setText(safs_driver_control_status);	
			
			boolean targetStatus = false;

			String val = staf.getVariable(DriverInterface.DRIVER_CONTROL_POF_VAR);			
			if(val.length()==0){
				staf.setVariable(DriverInterface.DRIVER_CONTROL_POF_VAR, String.valueOf(switchOfPOF.isSelected()));
			}else{
				targetStatus = JavaHook.PAUSE_SWITCH_ON.equalsIgnoreCase(val);
				if (switchOfPOF.isSelected() != targetStatus)
					switchOfPOF.setSelected(!targetStatus);
			}
			
			val = staf.getVariable(DriverInterface.DRIVER_CONTROL_POW_VAR);			
			if(val.length()==0){
				staf.setVariable(DriverInterface.DRIVER_CONTROL_POW_VAR, String.valueOf(switchOfPOW.isSelected()));
			}else{
				targetStatus = JavaHook.PAUSE_SWITCH_ON.equalsIgnoreCase(val);
				if (switchOfPOW.isSelected() != targetStatus)
					switchOfPOW.setSelected(!targetStatus);
			}
				
		}catch(Exception x){
			processException(x);
		}
	}
	
	/**
	 * Sends the Monitor Action command to SAFSVARS to control SAFSDRIVER.
	 */
	private void sendDriverAction(String action){
		try{
			if (staf == null) registerSTAF();
			staf.setVariable(DriverInterface.DRIVER_CONTROL_VAR, action);
			setStatus();
		}catch(Exception x){
			processException(x);
		}
	}
	
	/**
	 * 
	 * @param turnOn,			boolean, true->turn on the control variable; fasle->turn off.
	 * @param controlVarName, 	String, the control variable name.
	 *                                  DriverInterface.DRIVER_CONTROL_POF_VAR
	 *                                  DriverInterface.DRIVER_CONTROL_POW_VAR
	 */
	private void sendDriverSwitchAction(boolean turnOn, String controlVarName){
		try{
			if (staf == null) registerSTAF();
			staf.setVariable(controlVarName, turnOn?JavaHook.PAUSE_SWITCH_ON:JavaHook.PAUSE_SWITCH_OFF);
			//setStatus();
		}catch(Exception x){
			processException(x);
		}
	}
	
	private void showHideWatchEdit() {
		if (weditToggle.isSelected())
			getContentPane().add(this.watchPane, BorderLayout.CENTER);
		else
			getContentPane().remove(this.watchPane);
		this.pack();
	}
	/**
	 * Used to run a SAFS Monitor independent of a running Driver.
	 * @param args  accepts and processes no args
	 */
	public static void main(String[] args)
	{
		SAFSMonitorFrame appFrame = new SAFSMonitorFrame();
		appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/* KeyListener API */
	/**
	 * Monitors the F11 key and initiates a driver shutdown if received.
	 * @see sendShutdown()
	 */
	public void keyPressed(KeyEvent e){
		if(e.getKeyCode()==e.VK_F11){
			sendDriverAction(JavaHook.SHUTDOWN_RECORD);
		}		
	}

	/* KeyListener API */
	public void keyReleased(KeyEvent e){
		
	}
	/* KeyListener API */
	public void keyTyped(KeyEvent e){
	}
	

	// ActionListener Interface
	public void actionPerformed(ActionEvent event)
	{
		try{
			String action = event.getActionCommand();
		
			if(action.equalsIgnoreCase(STOP_ACTION)){
				sendDriverAction(JavaHook.SHUTDOWN_RECORD);		
			}else if(action.equalsIgnoreCase(PAUSE_ACTION)){
				sendDriverAction(JavaHook.PAUSE_EXECUTION);		
			}else if(action.equalsIgnoreCase(STEP_ACTION)){
				sendDriverAction(JavaHook.STEP_EXECUTION);		
			}else if(action.equalsIgnoreCase(RUN_ACTION)){
				sendDriverAction(JavaHook.RUNNING_EXECUTION);		
			}else if(action.equalsIgnoreCase(STEP_RETRY_ACTION)){
				updateCurTestRecordFromGUI();
				staf.setSAFSTestRecordData(STAFHelper.SAFS_HOOK_TRD, curtestRecordData);
				sendDriverAction(JavaHook.STEP_RETRY_EXECUTION);		
			}else if(action.equalsIgnoreCase(POF_ACTION)){
				sendDriverSwitchAction(switchOfPOF.isSelected(), DriverInterface.DRIVER_CONTROL_POF_VAR);
			}else if(action.equalsIgnoreCase(POW_ACTION)){
				sendDriverSwitchAction(switchOfPOW.isSelected(), DriverInterface.DRIVER_CONTROL_POW_VAR);
			}else if(action.equalsIgnoreCase(SAVE_EDIT_ACTION)){
				showHideWatchEdit();
			}
		}catch(Exception x){;}
	}
	
	// update curtestRecordData from STAF variables
	private void resetCurTestRecordFromVar(){
		curtestRecordData.reinit();
		try {
			curtestRecordData.setInstanceName(STAFHelper.SAFS_HOOK_TRD);
			curtestRecordData.populateDataFromVar();
			
			String typeRec = curtestRecordData.getTrimmedUnquotedInputRecordToken(0);
			typeRec = typeRec.toUpperCase();
			curtestRecordData.setRecordType(typeRec); 
			if ( typeRec.equals(DriverConstant.RECTYPE_T)  ||
				 typeRec.equals(DriverConstant.RECTYPE_TW) ||		
				 typeRec.equals(DriverConstant.RECTYPE_TF)) {
				//get the windowName, the second token (from 1)
				String windowName = curtestRecordData.getTrimmedUnquotedInputRecordToken(1);
				curtestRecordData.setWindowName(windowName);
				      
				//get the compName, the third token (from 1)
				String compName = curtestRecordData.getTrimmedUnquotedInputRecordToken(2);
				curtestRecordData.setCompName(compName);
			      
			    //get the command, the fourth token (from 1)
			    String command = curtestRecordData.getTrimmedUnquotedInputRecordToken(3);
			    curtestRecordData.setCommand(command);
			}
		}catch(SAFSException sx){;}		
	}

	// show the information of curtestRecordData on GUI for watch/edit
	private void showCurTestRecordOnGUI(){
		watchTableModel.getDataVector().removeAllElements();
		String typeRec = "";
		int count = 0;
		try {count = curtestRecordData.inputRecordSize();}
		catch(SAFSNullPointerException snpx) {;}
		
		for (int i=0; i < count; i++) {
			String variable = "";
			String value = "";
			try { variable = curtestRecordData.getTrimmedUnquotedInputRecordToken(i); } //getInputRecordToken(i);	
			catch(SAFSNullPointerException snpx){;} //never happen by this point
					
			value = variable; // value equal to variable as default 
					
			Vector newRow = new Vector(2);
			newRow.add(variable);  // first column: variable
			if (i == 0) {
				typeRec = variable;
			} else if (i == 1) {
				if ((typeRec.equals(DriverConstant.RECTYPE_T))  ||
					(typeRec.equals(DriverConstant.RECTYPE_TW)) ||		
					(typeRec.equals(DriverConstant.RECTYPE_TF))) {
					String winGuiId = "";
					try {winGuiId = curtestRecordData.getWindowGuiId();}
					catch(SAFSException se) {;}
					value = winGuiId;
				}	
			} else if (i == 2) {
				if ((typeRec.equals(DriverConstant.RECTYPE_T))  ||
					(typeRec.equals(DriverConstant.RECTYPE_TW)) ||		
					(typeRec.equals(DriverConstant.RECTYPE_TF))) {
					String compGuiId = "";
					try {compGuiId = curtestRecordData.getCompGuiId();}
					catch(SAFSException se) {;}	
					value = compGuiId;
				}	
			} 
			newRow.add(value);  // Second column: value
			watchTableModel.addRow(newRow);
		}
	}

	// updateTestRecordFromGUI   
	private void updateCurTestRecordFromGUI(){
			
		boolean isCompCommand = (curtestRecordData.getRecordType().equals(DriverConstant.RECTYPE_T) ||
				 				curtestRecordData.getRecordType().equals(DriverConstant.RECTYPE_TW) ||		
				 				curtestRecordData.getRecordType().equals(DriverConstant.RECTYPE_TF));
		// concatenate every field edited in watchTableModel to return a new inputRecord 
		String inputRecord = "";
		for (int i=0; i<watchTableModel.getRowCount(); i++) {
			if (isCompCommand &&(i==1 || i==2))
				inputRecord += (String)watchTableModel.getValueAt(i, 0);	
			else
				inputRecord += (String)watchTableModel.getValueAt(i, 1);
			if (i != watchTableModel.getRowCount()-1)
				inputRecord += curtestRecordData.getSeparator();
		}
		
		curtestRecordData.setInputRecord(inputRecord);
			
		if (!isCompCommand) 
		   return;

		if (mydebugAppMap == null) return;
		
		//take a debug map to store WindowGuiId and compGuiId, add it to chained map 
		curtestRecordData.setAppMapName(mydebugAppMap.SAFS_DEBUG_MAP);

		//= debug map =========================
		//1) update input record
		//2) debugAppmap
		ArrayList items = new ArrayList();
		Vector winitem = new Vector(3);
		winitem.add((String)watchTableModel.getValueAt(1, 0));  // section (String)watchTableModel.getValueAt(1, 0)
		winitem.add((String)watchTableModel.getValueAt(1, 0));  // item
		winitem.add((String)watchTableModel.getValueAt(1, 1));	// value  winGuiId
		
		Vector compitem = new Vector(3);
		compitem.add((String)watchTableModel.getValueAt(1, 0));  // section (String)watchTableModel.getValueAt(1, 0)
		compitem.add((String)watchTableModel.getValueAt(2, 0));  // item
		compitem.add((String)watchTableModel.getValueAt(2, 1));	//  value	compGuiId	
		
		items.add(winitem);
		items.add(compitem);
		
		mydebugAppMap.updateItems(items);
		mydebugAppMap.removeFromChainedMaps();
		mydebugAppMap.addToChainedMaps();
		//==========================
	}

	/**
	 * Stops our MonitorThread prior to normal JFrame shutdown procedures.
	 */
	public void dispose(){
		try{ 
			monitor.stop(); 
			try{ monitorthread.interrupt();}catch(Throwable ignore){}
		}catch(Exception x){;}
		super.dispose();
	}
	
	/**
	 * Routinely places the value of the SAFS_DRIVER_CONTROL variable into the status field. 
	 */
	public class MonitorThread implements Runnable{
		private boolean run = true;
		public void run(){
			while(run){
				setStatus();
				try{Thread.sleep(50);}catch(Exception x){;}
			}			
		}
		/**
		 * Sets run field to false to allow the Monitor to shutdown.
		 */
		public void stop(){
			run = false;
		}
	}
	
	/**
	 * a inner class to manage a temporary app map, adding it to chained maps or remove it from chained maps. 
	 * To re-execute a failed test step, a temporary app map is introduced to hold 
	 * modified mapping values including WinGuiId, CompGuiId and others if has.
	 * T, WinName, CompName, keywords, parameters  
	 *      |          |
	 *     WinGuiId   CompGuiId
	 * 
	 * ..\Datapool\~debugApp.tmp
	 *      
	 */
	public class debugAppMap{
		private DriverInterface driver = null;
		private UniqueStringMapInfo debugMapinfo = null; 
		private String fullpathDebugMap = null;

		INIFileReadWrite debugmapfile = null;
		
		public static final String SAFS_DEBUG_MAP = "~debugmap.tmp";
		
		//driver is a DriverInterface, the map service of the running test in this driver is where working chained maps reside. 
		public debugAppMap(DriverInterface driver){
			this.driver = driver;
			init();
		}
		
		private void init(){
			String safsdatapooldir = "";
			try{
				if (staf == null) registerSTAF();
				safsdatapooldir = staf.getVariable(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
			}catch(Exception x){
				processException(x);
			}
			if (safsdatapooldir.length() == 0)
				safsdatapooldir = "" ;  // get system
			fullpathDebugMap = safsdatapooldir + File.separatorChar + SAFS_DEBUG_MAP;
			
			renewDebugMapInUTF8(fullpathDebugMap);			
			debugMapinfo = new UniqueStringMapInfo(SAFS_DEBUG_MAP, fullpathDebugMap);
		}
		
		// 
		private void renewDebugMapInUTF8(String debugfile){
			try {
				// generate a temporary file as debug appmap encoded with UTF-8
				FileOutputStream afile = new FileOutputStream(debugfile);
				// write UTF-8 marker bytes to force the file with UTF-8 file format
				afile.write(0xEF); 
				afile.write(0xBB); 
				afile.write(0xBF); 
				afile.close();
			} catch(FileNotFoundException fnfx) {
				status.setText(fnfx.toString());
			} catch(IOException ioe) {
				status.setText(ioe.toString());
			}
		}
		
		// add section|item|value in list to debug appmap file
		// each of item in the list is a Vector, contains three elements, section, item and value.
		protected void updateItems(ArrayList list){
			if(driver != null){
				String debugMapPath = (String)debugMapinfo.getMapPath(driver);
				File afile = new File(debugMapPath);
				if ((! afile.exists())||(! afile.isFile())) return;
				INIFileReadWrite debugmapfile = new INIFileReadWrite(afile, 0);	
				for (int i=0; i<list.size(); i++) {
					Vector objarray = (Vector) list.get(i);
					// element 0: section     element 1: item  element 2: value       
					debugmapfile.setAppMapItem((String)objarray.get(0), (String)objarray.get(1), (String)objarray.get(2));
				}
				debugmapfile.writeUTF8INIFile(null);  
				debugmapfile.close();
			}
		}
		
		// add to chained maps in service SAFSMAPS 
		protected void addToChainedMaps(){
			if(driver != null){
				MapsInterface maps = driver.getMapsInterface();
				maps.openMap(debugMapinfo); // set the debug app map as default so that the SAFSMAPS in InputProcessor also take it as default
			}
		}
		
		// remove from chained maps in service SAFSMAPS
		protected void removeFromChainedMaps(){
			if(driver != null){
				MapsInterface maps = driver.getMapsInterface();
				maps.closeMap(debugMapinfo);
			}
		}		
		
	}
	
	/**
	 * <em>Note:</em>    Set the 'POF CheckBox' and STAF Variable 'SAFS_DRIVER_CONTROL_POF'
	 * <em>Note:</em>    Set the 'POW CheckBox' and STAF Variable 'SAFS_DRIVER_CONTROL_POW'
	 * @param turnOn,			boolean, true->turn on the control variable; fasle->turn off.
	 * @param controlVarName, 	String, the control variable name.
	 *                                  DriverInterface.DRIVER_CONTROL_POF_VAR
	 *                                  DriverInterface.DRIVER_CONTROL_POW_VAR
	 */
	public void setSwitchOfPause(boolean turnon, String controlVarName){
		if(DriverInterface.DRIVER_CONTROL_POF_VAR.equalsIgnoreCase(controlVarName)){
			switchOfPOF.setSelected(turnon);
		}else if(DriverInterface.DRIVER_CONTROL_POW_VAR.equalsIgnoreCase(controlVarName)){
			switchOfPOW.setSelected(turnon);
		}
		sendDriverSwitchAction(turnon, controlVarName);
	}	
	
}