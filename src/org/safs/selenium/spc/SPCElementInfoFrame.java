package org.safs.selenium.spc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openqa.selenium.WebElement;
import org.safs.selenium.webdriver.lib.WDLibrary;

public class SPCElementInfoFrame extends JFrame implements ActionListener{
	private SPC spc;
	private WDSPC wdspc;
	private String xpath;
	private SPCTreeNode node;
	
	private JTextArea mapRectxt;
	private JTextArea dmnRectxt;	
	private JTextArea xpathRectxt;	
	private JButton dmnSetbtn;
	private JButton xpathSetbtn;
	private JButton mapClrbtn;
	
	public SPCElementInfoFrame(String xpath,SPC spc){
		this.spc = spc;
		init(xpath);
	}
	
	public SPCElementInfoFrame(String xpath,WDSPC wdspc){
		this.wdspc = wdspc;
		init(xpath);
	}
	
	public SPCElementInfoFrame(SPCTreeNode anode, WDSPC wdspc){
		this.wdspc = wdspc;
		init(anode);
	}
	
	/**
	 * This routine is NOT used by the new WebDriver SPC.<p>
	 * Retrieve the Element Info, which is historically made up of:
	 * <p><ol>
	 * <li>compBounds (relative to 0,0 of client area?), "x#y#w#h" or ""<br>
	 * uses xpathBoundsSeparator as separator.
	 * <li>calculated SAFS recognition string (not xpath), or ""
	 * <li>the HTML making up the element (parent.innerHTML), or "None"
	 * <li>the innerHTML (if any) of the element itself, or "None"
	 * </ol>
	 * <p>
	 * @param xpath
	 * @return
	 */
	private String[] getElementInfo(String xpath){
		return spc != null ? spc.getElementInfo(xpath): new String[4];		
	}
	
	/**
	 * New WDSPC method
	 * @param anode
	 */
	private void init(SPCTreeNode anode){
		if(anode == null ){
			this.dispose();
			JOptionPane.showMessageDialog(null,"Unable to retrieve information for null element node.",
					"Process Container",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		node = anode;
		setTitle("Process Container Element Info");
		setSize(540,600);
		setMinimumSize(new Dimension(540,600));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
		
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints remainder = new GridBagConstraints();
		remainder.gridx=0;
		remainder.anchor=GridBagConstraints.NORTHWEST;
		remainder.gridwidth = GridBagConstraints.REMAINDER;
		
		JPanel pnl = new JPanel(layout);
		JPanel coords = new JPanel();
		JPanel attrs = new JPanel();
		JPanel domain = new JPanel();
		JPanel domainRec = new JPanel();
		JPanel classes = new JPanel();
		JPanel shrt_rec = new JPanel();
		JPanel full_rec = new JPanel();
		JPanel map_rec = new JPanel();
		JPanel properties = new JPanel();

		layout.setConstraints(coords, remainder);
		layout.setConstraints(attrs, remainder);
		layout.setConstraints(domain, remainder);
		layout.setConstraints(domainRec, remainder);
		layout.setConstraints(map_rec, remainder);
		layout.setConstraints(shrt_rec, remainder);
		layout.setConstraints(classes, remainder);
		layout.setConstraints(full_rec, remainder);
		layout.setConstraints(properties, remainder);
		
		// COORDS panel
		JLabel xlbl = new JLabel("X:", JLabel.LEFT);
		JTextField xtxt = new JTextField(String.valueOf(node.bounds.x));
		xtxt.setEditable(false);
		JLabel ylbl = new JLabel("Y:", JLabel.LEFT);
		JTextField ytxt = new JTextField(String.valueOf(node.bounds.y));
		ytxt.setEditable(false);
		JLabel wlbl = new JLabel("W:", JLabel.LEFT);
		JTextField wtxt = new JTextField(String.valueOf(node.bounds.width));
		wtxt.setEditable(false);
		JLabel hlbl = new JLabel("H:", JLabel.LEFT);
		JTextField htxt = new JTextField(String.valueOf(node.bounds.height));
		htxt.setEditable(false);				
		coords.add(xlbl); 
		coords.add(xtxt);		
		coords.add(ylbl); 
		coords.add(ytxt);		
		coords.add(wlbl); 
		coords.add(wtxt);		
		coords.add(hlbl); 
		coords.add(htxt);		
		
		// ATTRS panel
		JLabel taglbl = new JLabel("Tag:", JLabel.LEFT);
		JTextField tagtxt = new JTextField(node.getTag());
		tagtxt.setEditable(false);
		JLabel idlbl = new JLabel("Id:", JLabel.LEFT);
		JTextField idtxt = new JTextField(node.getId());
		idtxt.setEditable(false);
		JLabel namelbl = new JLabel("Name:", JLabel.LEFT);
		JTextField nametxt = new JTextField(node.getName());
		nametxt.setEditable(false);
		JLabel titlelbl = new JLabel("Title:", JLabel.LEFT);
		JTextField titletxt = new JTextField(node.getTitle());
		titletxt.setEditable(false);
		attrs.add(taglbl); 
		attrs.add(tagtxt);
		attrs.add(idlbl); 
		attrs.add(idtxt);
		attrs.add(namelbl); 
		attrs.add(nametxt);
		attrs.add(titlelbl); 
		attrs.add(titletxt);
		
		// DOMAIN panel
		JLabel dmnlbl = new JLabel("Domain:", JLabel.LEFT);
		JTextField dmntxt = new JTextField(node.getDomain());
		dmntxt.setEditable(false);
		JLabel dmnClasslbl = new JLabel("Class:", JLabel.LEFT);
		JTextField dmnClasstxt = new JTextField(node.getDomainClass());
		dmnClasstxt.setEditable(false);
		JLabel dmnTypelbl = new JLabel("Type:", JLabel.LEFT);
		JTextField dmnTypetxt = new JTextField(node.getCompType());
		dmnTypetxt.setEditable(false);
		domain.add(dmnlbl); 
		domain.add(dmntxt);
		domain.add(dmnClasslbl); 
		domain.add(dmnClasstxt);
		domain.add(dmnTypelbl); 
		domain.add(dmnTypetxt);

		// DOMAINREC panel
		JLabel dmnReclbl = new JLabel("DoRec:", JLabel.LEFT);
		//JTextField dmnRectxt = new JTextField(node.getDomainRecognition());
		dmnRectxt = new JTextArea(node.getDomainRecognition());
		dmnRectxt.setEditable(false);
		dmnRectxt.setAutoscrolls(true);
		dmnRectxt.setToolTipText("The calculated Domain-specific recognition string of the Element.");
		JScrollPane dmnRecscroll = new JScrollPane(dmnRectxt);
		dmnRecscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		dmnRecscroll.setPreferredSize(new Dimension(400,38));
		dmnReclbl.setLabelFor(dmnRecscroll);
		dmnSetbtn = new JButton("Set");
		dmnSetbtn.addActionListener(this);
		dmnSetbtn.setToolTipText("Set Domain recognition string as Map recognition string.");
		domainRec.add(dmnReclbl); 
		domainRec.add(dmnRecscroll);
		domainRec.add(dmnSetbtn);
		
		// XPART RECOGNITION panel
		JLabel shrtReclbl = new JLabel("XPart:", JLabel.LEFT);
		JTextField shrtRectxt = new JTextField(node.xpart);
		shrtRectxt.setEditable(false);
		shrtRectxt.setToolTipText("Short 'last child' part of XPath recognition string.");
		shrt_rec.add(shrtReclbl); 
		shrt_rec.add(shrtRectxt);
		
		// CLASSES panel
		JLabel classlbl = new JLabel(" class:", JLabel.LEFT);
		JTextArea classtxt = new JTextArea(node.getAttrClass());
		classtxt.setEditable(false);
		classtxt.setAutoscrolls(true);
		classtxt.setToolTipText("The 'class' attribute of the Element.");
		JScrollPane classscroll = new JScrollPane(classtxt);
		classscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		classscroll.setPreferredSize(new Dimension(400,38));
		classlbl.setLabelFor(classscroll);
		classes.add(classlbl); 
		classes.add(classscroll);

		// XPATH RECOGNITION panel
		JLabel xpathReclbl = new JLabel("XPath:", JLabel.LEFT);
		xpathRectxt = new JTextArea(node.getXpath());
		xpathRectxt.setEditable(false);
		xpathRectxt.setToolTipText("The calculated generic XPath recognition string of the Element.");
		JScrollPane xpathscroll = new JScrollPane(xpathRectxt);
		xpathscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		xpathscroll.setPreferredSize(new Dimension(400,38));
		xpathReclbl.setLabelFor(xpathscroll);
		xpathSetbtn = new JButton("Set");
		xpathSetbtn.addActionListener(this);
		xpathSetbtn.setToolTipText("Set XPath recognition string as Map recognition string.");
		full_rec.add(xpathReclbl); 
		full_rec.add(xpathscroll);
		full_rec.add(xpathSetbtn);	
		
		// MAP RECOGNITION panel
		JLabel mapReclbl = new JLabel("  Map  :", JLabel.LEFT);
		mapRectxt = new JTextArea(node.getRecognitionString());
		mapRectxt.setEditable(false);
		mapRectxt.setAutoscrolls(true);
		mapRectxt.setToolTipText("The recognition set by tester to be used in App Map.");
		JScrollPane mapscroll = new JScrollPane(mapRectxt);
		mapscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		mapscroll.setPreferredSize(new Dimension(400,38));
		mapReclbl.setLabelFor(mapscroll);
		mapClrbtn = new JButton("Clr");
		mapClrbtn.addActionListener(this);
		mapClrbtn.setToolTipText("Clear Map recognition string.");
		map_rec.add(mapReclbl); 
		map_rec.add(mapscroll);
		map_rec.add(mapClrbtn);
		
		// PROPERTIES panel
		//JLabel propslbl = new JLabel("Properties:");
		//propslbl.setHorizontalTextPosition(JLabel.LEFT);		
		//propslbl.setVerticalTextPosition(JLabel.TOP);
		String rs = node.getRecognitionString();
		if(rs==null||rs.length()==0)
			rs = node.getDomainRecognition();
		if(rs==null||rs.length()==0){
			rs = node.getXpath();
			if(rs!=null&&rs.length()>0)
				rs = "XPATH="+ rs;
		}				    
		WebElement element = null;
		String propertyvalues = "Object or properties not found.\nCheck and Set a recognition string.";
		if(rs!=null&&rs.length()>0){
			rs = wdspc.appendFrameRS(rs);
			try{ element = WDLibrary.getObject(rs);}catch(Exception x){}
		}
		if(element != null){
			Map<String,Object> map = null;
			String eq = "=";
			String nl = "\n";
			try{ 
				map = WDLibrary.getProperties(element);
				if(!map.isEmpty()){
					SortedSet<String> keys = new TreeSet<String>(map.keySet());
					propertyvalues = "";
					for(String key:keys){
						propertyvalues += key + eq + map.get(key).toString()+ nl;
					}
				}
			}
			catch(Exception x){}
		}
		JTextArea propstxt = new JTextArea(propertyvalues);		
		propstxt.setEditable(false);
		propstxt.setAutoscrolls(true);
		propstxt.setToolTipText("The known properties of the Element.");
		JScrollPane propsscroll = new JScrollPane(propstxt);
		propsscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		propsscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		propsscroll.setPreferredSize(new Dimension(500,230));
		properties.add(propsscroll);
				
		pnl.add(coords);
		pnl.add(attrs);
		pnl.add(domain);
		pnl.add(domainRec);
		pnl.add(map_rec);
		pnl.add(shrt_rec);
		pnl.add(full_rec);
		pnl.add(classes);
		pnl.add(properties);
		
		add(pnl, BorderLayout.CENTER);
		setVisible(true);
	}
	/**
	 * Old SPC (not WebDriverSPC) method
	 * @param xpath
	 */
	private void init(String xpath){
		this.xpath = xpath;
		setTitle("Element Info For "+xpath);
		setSize(600,400);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		String [] labels = {"X,Y,Width,Height:","XPath:","Robot:","Tag:","Inner HTML:"};
		
		String tag = xpath.substring(xpath.lastIndexOf("/")+1,xpath.lastIndexOf("["));
		int index = 0;
		
		try{
			index = Integer.parseInt(xpath.substring(xpath.lastIndexOf("[")+1,xpath.lastIndexOf("]")));
		}catch(NumberFormatException e){
			index = Integer.parseInt(xpath.substring(xpath.lastIndexOf("|")+1,xpath.lastIndexOf("]")));
		}
						
		String [] data = getElementInfo(xpath);
		
		if(data == null || data.length == 1){
			this.dispose();
			JOptionPane.showMessageDialog(null,"Unable to retrieve element information.",
					"Element Info",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		//CONSTRUCT ROBOT RECOGNITION STRING
		String [] robotparts = data[2].split(":::");
		String type = SPCUtilities.getRobotTag(tag);
		if(type == null){
			type = "HTML";
		}
		String rrec = "Type="+type+";";
		
		rrec += SPCUtilities.getRobotTag(robotparts[0])+"="+robotparts[1];
		data[2] = rrec;
		
		//GET TAG HTML
		Pattern p = Pattern.compile("(<"+tag+".*?>)",Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(data[3]);
		int count = 0;
		while(count < index-1 && m.find()){
			count++;
		}
		if(m.find())
			data[3]=m.group(0);
		else data[3]="Tag not available. (Probably <HTML> Tag)";
		
		
		JPanel pnl = new JPanel(new SpringLayout());
		
		for (int i = 0; i < labels.length; i++) {
			JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			pnl.add(l);
			JTextArea jta = new JTextArea(data[i]);
			jta.setEditable(false);
			JScrollPane jsp = new JScrollPane(jta);
			l.setLabelFor(jsp);
			pnl.add(jsp);
		}
		SPCUtilities.makeCompactGrid(pnl,
                labels.length, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6); 
		this.add(pnl);
		setVisible(true);
	}
	/** Internal layout testing only. **/
	public static void main(String[] args){
		SPCTreeNode anode = new SPCTreeNode();
		anode.bounds = new Rectangle(10,10,500,30);
		anode.setId("main_id");
		anode.setTag("span");
		anode.setAttrClass("showMeCSS");
		anode.setName("main_name");
		anode.setTitle("title text here");
		anode.setXpath("html/body/div[@id='content']/butabitlonger/andwithalotmore/stuffinsideit/butstillIneedmore/becauseitisn'tlongenough/withoutallthis");
		anode.xpart="div[@id='content']";
		anode.setRecognitionString("SAPTabControl;id=myTabs");
		SPCElementInfoFrame f = new SPCElementInfoFrame(anode, null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
	}

	/** Daemon Thread subclass */
	public static class FrameThread extends Thread{
		public SPCElementInfoFrame frame = null;
		public SPCTreeNode anode = null;
		public WDSPC aspc = null;
		public FrameThread(SPCTreeNode anode, WDSPC aspc){
			this.anode = anode;
			this.aspc = aspc;
			setDaemon(true);
		}
		public void run(){
			frame = new SPCElementInfoFrame(anode,aspc);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource()==dmnSetbtn){
			mapRectxt.setText(dmnRectxt.getText());
			node.setRecognitionString(mapRectxt.getText());
		}else if(event.getSource()==xpathSetbtn){
			mapRectxt.setText(xpathRectxt.getText());
			node.setRecognitionString(mapRectxt.getText());
		}else if(event.getSource()==mapClrbtn){
			mapRectxt.setText("");
			node.setRecognitionString("");
		}		
	}

}
