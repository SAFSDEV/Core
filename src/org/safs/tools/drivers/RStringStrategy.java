package org.safs.tools.drivers;

import javax.swing.*; 
import javax.swing.border.BevelBorder;

import java.awt.event.*; 
import java.awt.*;

/**
 * Class RStringStrategy holds the strategy for STAFPC to generate R-Strings optionally. 
 * Class RStringStrategySettings, a modal JDialog, is used to modify the strategy.
 * Internally used in package org.safs.tools.drivers. 
 *  
 * @since  NOV 27, 2008
 * @author JunwuMa 
 *         NOV 27, 2008 Original Release
 *         JAN 16, 2009 (JunwuMa) Modify class RStringStrategy and add member ifIndexOnly to decide if using 'Index/ClassIndex' only when generating R-Strings. See S0548633.
 *                                Change the GUI of RStringStrategySettings adding 'Use Index Only' option.
 *         DEC 29, 2009 (JunwuMa) Add class doChildrenOptions for SPC to call and decide if ignore the children of TLC, 
 *                                TLC stands for TABLE/LISTBOX/LISTVIEW/COMBOBOX. Sometimes, the components embedded in TLC are cared by user.
 *         SEP 27, 2010 (LeiWang) Add some option names;
 *                                Add "use generic type" functionality. Not in use yet, if need, just un-comment one line in method populateFrame()
 *         AUG 14, 2013 (CANAGL)  Added useClassNotSubType to allow Class= over Type= even when a superclass does match to a known Type.                                
 */

/*
 * Instantiated in STAFProcessContainer.
 * See other options defined in STAFProcessContainer. withNameIncludeOnlyCaption, shortenGeneralRecognition, ignoreInvisible
 * */
public class RStringStrategy {
	//Define some option name for initial settings of RStringStrategy
	//For example, you can define in processcontainer.ini
	//"rsStrategy.qualifier=true"
	//"rsStrategy.useid=true"
	//"rsStrategy.useName=true"
	//"rsStrategy.useGenericType=false"
	//"rsStrategy.useClassNotSubType=true"
	public static final String AUTO_QUALIFIER = "rsStrategy.qualifier";
	public static final String QUALIFIER_USE_ID = "rsStrategy.useid";
	public static final String QUALIFIER_USE_NAME = "rsStrategy.useName";
	public static final String USE_GENERIC_TYPE = "rsStrategy.useGenericType";
	public static final String USE_CLASS_NOT_SUBTYPE = "rsStrategy.useClassNotSubType";
	
	// Users can choose one of the two options to generate R-Strings.   
	// 1. First option: Auto qualifier  
	// 					SAFS selects suitable qualifiers qualifiers automatically according to a component's properties.
	//               	Two variables (bUseId, bAccessibleNamePriority) in the first option for telling SAFS how to do so.
	//
	// 2. Second option: Use Index only 
	//                  'Index='
	
	// true: Use ID if exists; false: not use ID in RStrings even exists.
	private boolean ifUseId = false;  
	public void setIfUseId(boolean ifUseId) { this.ifUseId = ifUseId; }
	public boolean getIfUseId() { return ifUseId; }
	
	// true: users need accessible name be found first among accessible name and Name; false: Name will be found before accessiable name.
	private boolean ifAccessibleNamePriority = true; 
	public void setIfAccessibleNamePriority(boolean ifAccessibleNamePriority) { 
		this.ifAccessibleNamePriority = ifAccessibleNamePriority; 
	}
	public boolean getIfAccessibleNamePriority() { return ifAccessibleNamePriority; }
	
	// Decide which option is chosen for generating R-Strings.
	// true: use the first option;  false: use the second option; 
	private boolean ifIndexOnly = false;
	public void setIfIndexOnly(boolean ifIndexOnly) { this.ifIndexOnly = ifIndexOnly; }
	public boolean getIfIndexOnly() { return ifIndexOnly; }
	public boolean isAutoQualifier() { return !getIfIndexOnly(); }
	
	//Use the most generic type in RS: false by default
	private boolean useGenricType = false;
	public boolean isUseGenricType() {
		return useGenricType;
	}
	public void setUseGenricType(boolean useGenricType) {
		this.useGenricType = useGenricType;
	}
	
	// use Class= instead of Type= if Class is not mapped but a subclass is mapped.
	private boolean useClassNotSubType = true;
	public void setUseClassNotSubType(boolean useClassNotSubType) {this.useClassNotSubType = useClassNotSubType;}
	public boolean isUseClassNotSubType(){ return useClassNotSubType; }
	
}

class RStringStrategySettings extends JDialog {
	private RStringStrategy m_rstringStrategy; 
	private boolean clickOk = false; 
	
	private JCheckBox cbId                     = new JCheckBox("Use ID if exists");
	private JCheckBox cbDesireAccessibleName   = new JCheckBox("Prefer AccessibleName as Name");
	public static final String cbDesireTipText = "Checked: Try AccessibleName before Name; Unchecked: reverse the sequence.";
	//cbDesireAccessibleName.setToolTipText
	private JButton   bOk     				   = new JButton("Ok");
	private JButton   bCancel                  = new JButton("Cancel");
	
    private ButtonGroup bgroup		   		   = new ButtonGroup();
	private JRadioButton rbAutoQualifer        = new JRadioButton("Auto Qualifier", true);
	private JRadioButton rbIndexQualiferOnly   = new JRadioButton("Use Index Only", false);
	public static final String rbIndexQualiferOnlyTipText = "Use Index= or ClassIndex= accordingly";
	
	private JCheckBox cbUseGenericType			= new JCheckBox("Use generic Types");
	public static final String cbUseGenericTypeTipText = "Ex: Prefer 'Panel' over 'JavaPanel'";	
	
	private JCheckBox cbUseClassNotSubType      = new JCheckBox("Use Class over Super Type");
	public static final String cbUseClassNotSubTypeTipText = "Use Class= instead of superclass Type=";	
	
    public RStringStrategySettings(RStringStrategy strategy) {
		super((Frame)null, true); // create modal dialog
		SetRStringStrategy(strategy);
		initGUI(strategy);
		populateFrame();
	}
	public void SetRStringStrategy(RStringStrategy strategy) { m_rstringStrategy = strategy; }
	public RStringStrategy GetRStringStrategy() { return m_rstringStrategy; }
    
	/**
	 * @return true if clicking Ok 
	 */
	public boolean Execute(){
		this.setVisible(true);
		return clickOk;
	}
	
	private void initGUI(RStringStrategy strategy) {
		rbAutoQualifer.setSelected(strategy.isAutoQualifier());
		cbId.setSelected( strategy.getIfUseId() );
		cbDesireAccessibleName.setSelected( strategy.getIfAccessibleNamePriority() );

		rbIndexQualiferOnly.setSelected(strategy.getIfIndexOnly());
		cbUseGenericType.setSelected(strategy.isUseGenricType());
		
		cbUseClassNotSubType.setSelected(strategy.isUseClassNotSubType());
	}

	private void populateFrame() {	        
	    int buttonw = 120; 
	    int buttonh = 24;
	    
	    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);	   
	    
	    // North pane, setting area 
	    // Two main options in setting area
	    bgroup.add(rbAutoQualifer);
	    bgroup.add(rbIndexQualiferOnly);	
	    
	    // define AutoQualifer Pane
	    JPanel NorthAutoPane = new JPanel();
	    NorthAutoPane.setLayout(new BoxLayout(NorthAutoPane,BoxLayout.Y_AXIS));
	    NorthAutoPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,20,10,10), 
	    		                                                   BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
	    NorthAutoPane.add(cbId);
	    cbDesireAccessibleName.setToolTipText(cbDesireTipText);
	    NorthAutoPane.add(cbDesireAccessibleName);

	    JPanel NorthPane = new JPanel();
	    NorthPane.setLayout(new BoxLayout(NorthPane,BoxLayout.Y_AXIS));
	    NorthPane.setBorder(BorderFactory.createTitledBorder("Options for generating R-Strings:"));
	    
	    NorthPane.add(rbAutoQualifer);
	    NorthPane.add(NorthAutoPane);
	    rbIndexQualiferOnly.setToolTipText(rbIndexQualiferOnlyTipText);
	    NorthPane.add(rbIndexQualiferOnly);
	    cbUseClassNotSubType.setToolTipText(cbUseClassNotSubTypeTipText);
	    NorthPane.add(cbUseClassNotSubType);
	    
	    //For the functionality of Generic Type, it has be implemented
	    //If we want to use, just un-comment the following lines.
	    cbUseGenericType.setToolTipText(cbUseGenericTypeTipText);
	    NorthPane.add(cbUseGenericType);

	    // South pane for OK and Cancel buttons
	    JPanel SouthPane = new JPanel();
	    SouthPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	  
		bOk.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		// change the settings
	    		m_rstringStrategy.setIfUseId( cbId.isSelected() );
	    		m_rstringStrategy.setIfAccessibleNamePriority( cbDesireAccessibleName.isSelected() );
	    		m_rstringStrategy.setUseGenricType(cbUseGenericType.isSelected());
	    		m_rstringStrategy.setIfIndexOnly(rbIndexQualiferOnly.isSelected());
	    		m_rstringStrategy.setUseClassNotSubType(cbUseClassNotSubType.isSelected());
	    		clickOk = true;
	    		dispose();
	    	}
	    });
	    bOk.setPreferredSize(new Dimension(buttonw, buttonh));
	    SouthPane.add(bOk);
	    
	    SouthPane.add(Box.createHorizontalStrut(20));

	    bCancel.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		clickOk = false;
	    		dispose();
	    	}
	    });
	    bCancel.setPreferredSize(new Dimension(buttonw, buttonh));
	    SouthPane.add(bCancel);	
	    
	    // main container
	    Container cp = getContentPane();
	    cp.add(NorthPane, BorderLayout.NORTH);
	    cp.add(SouthPane, BorderLayout.SOUTH);
	    
		this.setTitle("Advanced Settings"); 
		this.setSize(240, 240);
		this.pack();
    }
}

/**
 * JDialog for SPC changing options -- whether ignore the children of TLC or not during processing children. 
 * TLC stands for TABLE/LISTBOX/LISTVIEW/COMBOBOX
 */
class doChildrenOptions extends JDialog {
	private boolean m_ignoreChildInTLC; 
	private boolean clickOk = false; 

	private JButton   bOk     				   = new JButton("Ok");
	private JButton   bCancel                  = new JButton("Cancel");
	
	// ignore the children of TLC (TABLE/LISTBOX/LISTVIEW/COMBOBOX) 
	private JCheckBox cbIgnoreChildInTLC       = new JCheckBox("Ignore the Children of TABLE/LISTBOX/LISTVIEW/COMBOBOX");
	
    public doChildrenOptions(boolean ignoreTableChildren) {
		super((Frame)null, true); // create modal dialog
		init(ignoreTableChildren);
		populateFrame();
	}
	public boolean ignoreChildInTLC() { return m_ignoreChildInTLC; }
    
	/**
	 * @return true if clicking Ok 
	 */
	public boolean Execute(){
		this.setVisible(true);
		return clickOk;
	}
	
	private void init(boolean ignoreChildInTLC) {
		m_ignoreChildInTLC = ignoreChildInTLC;
		cbIgnoreChildInTLC.setSelected( ignoreChildInTLC );
	}

	private void populateFrame() {	        
	    int buttonw = 120; 
	    int buttonh = 24;
	    
	    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);	   
	    
	    // North pane, setting area 
	    JPanel NorthPane = new JPanel();
	    NorthPane.setLayout(new BoxLayout(NorthPane,BoxLayout.Y_AXIS));
	    NorthPane.setBorder(BorderFactory.createTitledBorder("Options"));
	    
	    NorthPane.add(cbIgnoreChildInTLC);

	    // South pane for OK and Cancel buttons
	    JPanel SouthPane = new JPanel();
	    SouthPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	  
		bOk.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		// change the settings
	    		m_ignoreChildInTLC = cbIgnoreChildInTLC.isSelected();
	    		clickOk = true;
	    		dispose();
	    	}
	    });
	    bOk.setPreferredSize(new Dimension(buttonw, buttonh));
	    SouthPane.add(bOk);
	    
	    SouthPane.add(Box.createHorizontalStrut(20));

	    bCancel.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		clickOk = false;
	    		dispose();
	    	}
	    });
	    bCancel.setPreferredSize(new Dimension(buttonw, buttonh));
	    SouthPane.add(bCancel);	
	    
	    // main container
	    Container cp = getContentPane();
	    cp.add(NorthPane, BorderLayout.NORTH);
	    cp.add(SouthPane, BorderLayout.SOUTH);
	    
		this.setTitle("Options for Process Children"); 
		this.setSize(240, 100);
		this.pack();
    }
}
