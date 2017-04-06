/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 23, 2015 Lei Wang Modify value of some constant like 'CLASSNAMENN' to lower-case.
 * APR 05, 2017	Lei Wang Refactor to fully support AUTOIT engine RS.
 * APR 06, 2017	Lei Wang Supported "index=" and "caption=wildcard string".
 */
package org.safs.autoit;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.safs.Constants.AutoItConstants;
import org.safs.GuiObjectRecognition;
import org.safs.IndependantLog;
import org.safs.SAFSObjectRecognitionException;
import org.safs.StringUtils;
import org.safs.autoit.lib.AutoItXPlus;
import org.safs.persist.PersistableDefault;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Class to handle recognition string parsing for the AutoIt engine.<br/>
 * Window Recognition String will be converted to <a href="https://www.autoitscript.com/autoit3/docs/intro/windowsadvanced.htm">AUTOIT engine window's RS</a>.<br/>
 * Child Control Recognition String will be converted to <a href="https://www.autoitscript.com/autoit3/docs/intro/controls.htm">AUTOIT engine control's RS</a>.<br/>
 * The TEXT in Window Recognition String will be converted to <a href="https://www.autoitscript.com/autoit3/docs/intro/windowsbasic.htm#specialtext">Window Text</a>.<br/>
 * <p>
 * The recognition string is composed with pairs of <b>RsKey=value</b> separated by semi-colon <b>;</b> such as <b>key1=value;key2=value</b><br/>
 * <br/>
 *
 * <b>Window Recognition</b>:<br/>
 * Note: the case-insensitive "<b>:autoit:</b>" prefix MUST appear in Window RS and will be removed as needed.<br/>
 * The window's <b>RS KEY</b> (case-insensitive) can be
 * <ul>
 * <li><b>TITLE \ CAPTION</b> - Window title.<br/>
 *                   By default, The <b>TITLE</b> or <b>CAPTION</b> must be the beginning part of the title.
 *                   For example, we have a Notepad window with title "Untitled - Notepad", we can define RS as<br/>
 *                   Notepad=":AUTOIT:title=Untitled - Notepad" using the full title<br/>
 *                   Notepad=":AUTOIT:title=Untitled" using the beginning part of the title<br/>
 *                   But we should <b>NOT</b> define RS as Notepad=":AUTOIT:title=Notepad" which uses the ending part of the title.<br/>
 *                   <br/>
 *                   The <b>CAPTION</b> also supports wildcard, * represents zero or more character, ? represent 1 character. If it is
 *                   expressed as wildcard string, then the 'title mode' will be invalid.
 *                   <br/>
 *                   For example, the Notepad window with title "Untitled - Notepad", we can define as<br/>
 *                   Notepad=":AUTOIT:caption=Untitled*Note?ad"<br/>
 *                   Notepad=":AUTOIT:caption=?ntitled*pad"<br/>
 * <br/>
 * <li><b>TEXT</b> - The window text consists of all the text that AutoIt can "see".
 *                   This will usually be things like the contents of edit controls but will also include other text like:
 *                   <ul>
 *                   <li>Button text like &Yes, &No, &Next (the & indicates an underlined letter)
 *                   <li>Dialog text like "Are you sure you wish to continue?"
 *                   <li>Control text
 *                   <li>Misc text - sometimes you don't know what it is :)
 *                   </ul>
 *                   When you specify the text parameter in a window function it is treated as a <b>substring</b>.<br/>
 *                   <br/>
 * <li><b>CLASS</b> - The internal window classname
 * <li><b>REGEXPTITLE</b> - Window title using a regular expression
 * <li><b>REGEXPCLASS</b> - Window classname using a regular expression
 * <li><b>LAST</b> - Last window used in a previous Windows AutoIt Function
 * <li><b>ACTIVE</b> - Currently active window
 * <li><b>X</b> \ <b>Y</b> \ <b>W</b> \ <b>H</b> - The position and size of a window
 * <li><b>INDEX \ INSTANCE</b> - The 1-based instance when all given properties match
 * </ul>
 *
 * <b>Child Control Recognition</b>:<br/>
 * Note: the case-insensitive "<b>:autoit:</b>" prefix CAN appear in Control RS and will be removed if present.<br/>
 * The control's <b>RS KEY</b> (case-insensitive) can be
 * <ul>
 * <li><b>ID</b> - The internal control ID. The Control ID is the internal numeric identifier that windows gives to each control. It is generally the best method of identifying controls.
 * <li><b>TEXT</b> - The text on a control, for example "&Next" on a button
 * <li><b>CLASS</b> - The internal control classname such as "Edit" or "Button"
 * <li><b>CLASSNN</b> - The ClassnameNN value as used in previous versions of AutoIt, such as "Edit1"
 * <li><b>NAME</b> - The internal .NET Framework WinForms name (if available)
 * <li><b>REGEXPCLASS</b> - Control classname using a regular expression
 * <li><b>X</b> \ <b>Y</b> \ <b>W</b> \ <b>H</b> - The position and size of a control.
 * <li><b>INDEX \ INSTANCE</b> - The 1-based instance when all given properties match.
 * </ul>
 *
 * </br>
 * <b>Examples</b>
 * <ul>
 * <li>Window Recognition:
 *   <ul>
 *   	<li>Calculator=":AUTOIT:title=Calculator"
 *   	<li>Calculator=":AUTOIT:Caption=Calculator"
 *   	<li>Calculator=":AUTOIT:CAPTION=?alcul*"
 *   	<li>Calculator=":AUTOIT:CAPTION=Ca?culat*"
 *   	<li>Calculator=":AUTOIT:class=CalcFrame"
 *   	<li>Calculator=":AUTOIT:REGEXPTITLE=[C|c].*lator"
 *   	<li>Calculator=":AUTOIT:REGEXPCLASS=.*Fra.*"
 *   	<li>Calculator=":AUTOIT:title=Calculator;class=CalcFrame"
 *   	<li>Notepad_Replace=":AUTOIT:title=Replace;class=#32770"
 *   </ul>
 * <li>Child Control Recognition:
 *	 <ul>
 *   	<li>btnRadians=":AutoIt:text=Radians"
 *   	<li>btnRadians="text=Radians"
 *   	<li>btnRadians="id=131"
 *   	<li>btnRadians="classnn=Button5"
 *   	<li>btnRadians="class=Button"
 *   	<li>btnRadians="id=131;class=Button"
 *   	<li>btnRadians="class=Button;instance=5"
 *   	<li>childName="caption=MDI Caption;id=theControlID"  (future)
 *   </ul>
 * </ul>
 *
 *
 * @author dharmesh
 */
public class AutoItRs implements IAutoItRs{

	/** <b>":autoit:"</b> */
	public static final String AUTOIT_PREFIX = AutoItConstants.AUTOIT_PREFIX;
	/** "<b>;</b>" */
    public static final String AUTOIT_DELIMITER = GuiObjectRecognition.DEFAULT_QUALIFIER_SEPARATOR;

    /** The raw window RS provided by user. It can be in SAFS format, or engine format, or any other format */
	private String winRawRS = null;
	/** The raw component RS provided by user. It can be in SAFS format, or engine format, or any other format */
	private String compRawRS = null;

	private Recognizable window = null;
	private Recognizable control = null;
	private Boolean isWindow = null;

	public AutoItRs(String winRawRS, String compRawRS) throws SAFSObjectRecognitionException{
		this.winRawRS = winRawRS;
		this.compRawRS = compRawRS;

		initialize();
	}

	/**
	 * Called internally.
	 * Set the field {@link #isWindow}.<br/>
	 * Parse the provided raw recognition strings into needed Window {@link #window} and Child Control {@link #control} elements.<br/>
	 * @throws SAFSObjectRecognitionException if the window recognition string is not valid.
	 */
	private void initialize() throws SAFSObjectRecognitionException{

		if(!StringUtils.isValid(winRawRS)){
			throw new SAFSObjectRecognitionException("the window recognition string '"+winRawRS+"' is NOT valid.");
		}
		if(!StringUtils.isValid(compRawRS)){
			//We just log a warning message, and will consider it as a Window
			IndependantLog.warn("the component recognition string '"+compRawRS+"' is NOT valid.");
			compRawRS = winRawRS;
		}

		isWindow();

		window = new Window(winRawRS);
		window.parseRawRS();

		if(isWindow){
			control = window;
		}else{
			control = new Control(compRawRS);
			control.parseRawRS();
		}

	}

	/**
	 * Attempt to determine if recognition string is for autoit testing
	 * @param recognition -- recognition, usually from App Map
	 * @return true if it contains elements of autoit testing recognition.
	 * Primarily, that is startsWith the :autoit: prefix.
	 * @see #AUTOIT_PREFIX
	 */
	public static boolean isAutoitBasedRecognition(String recognition){
		IndependantLog.debug("AutoIt Rec " + recognition);
		try{
			return recognition.toLowerCase().startsWith(AUTOIT_PREFIX);
		}catch(Exception x) {}
		return false;
	}

	/**
	 * If the RS begins with [ and ends with ], then it is considered as AUTOIT RS.
	 * @param rs String, the recognition string to test
	 * @return boolean
	 */
	public static boolean isEngineRs(String rs){
		if(rs.startsWith(AutoItConstants.AUTOIT_ENGINE_RS_START) &&
		   rs.endsWith(AutoItConstants.AUTOIT_ENGINE_RS_END)){
			return true;
		}
		return false;
	}

	@Override
	public String getWindowsRS(){
		return window.getEngineRS();
	}

	@Override
	public String getWindowText(){
		String text = ((Window)window).getText();
		if(text==null){
			text = "";
		}
		return text;
	}
	public void setWindowText(String windowText){
		((Window)window).setText(windowText);
	}

	/**
	 * If the window title is not complete, call this method to complete.
	 * @param it The AutoIt instance.
	 */
	public void normalize(AutoItXPlus it){
		((Window)window).normalize(it);
	}

	/**
	 * @return boolean true if the recognition string represents a window.
	 * @throws SAFSObjectRecognitionException if the window's recognition string is not valid.
	 */
	@Override
	public boolean isWindow() throws SAFSObjectRecognitionException{
		if(!StringUtils.isValid(winRawRS)){
			throw new SAFSObjectRecognitionException("The window's recognition string is not valid!");
		}
		if(isWindow==null){
			isWindow = new Boolean(winRawRS.equals(compRawRS));
		}
		return isWindow.booleanValue();
	}

	@Override
	public String getComponentRS(){
		return control.getEngineRS();
	}

	protected static interface Recognizable{
		/** This raw Recognition String is provided by user, it can be in SAFS format,
		 *  engine format, or any other format which will be parsed into engine RS. */
		public String getRawRS();
		/** This Recognition String is engine specific. */
		public String getEngineRS();
		/** This method should parse the raw RS */
		public void parseRawRS();
	}

	protected static class DefaultRecognizable extends PersistableDefault implements Recognizable{
		/** The recognition string provided by user. */
		protected String rawRS = null;
		/** The recognition string in Native Mode, recognized directly by underlying automation tool. */
		protected String engineRS = null;

		/** a cache holding the Map of (fieldName, rsKey) */
		protected Map<String/*fieldName*/, String/*rsKey*/> fieldNameToRsKeyMap = null;

		public DefaultRecognizable(String rawRS){
			this.rawRS = rawRS;
		}

		@Override
		public String getRawRS() {
			return rawRS;
		}
		public void setRawRS(String rawRS) {
			this.rawRS = rawRS;
		}

		@Override
		public String getEngineRS() {
			return engineRS;
		}
		public void setEngineRS(String engineRS) {
			this.engineRS = engineRS;
		}

		public void parseRawRS(){
			beforeParse();
			parsing();
			afterParse();
		}

		/**
		 * Do some thing before parsing the raw recognition string.<br/>
		 * For example, remove some prefix/suffix or remove leading/ending spaces etc.<br/>
		 */
		protected void beforeParse(){}
		/**
		 * Parse the raw recognition string so that the engine RS will be produced.
		 */
		protected void parsing(){}
		/**
		 * Do some thing after parsing the raw recognition string.
		 */
		protected void afterParse(){}

		/**
		 * This default implementation get all the declared fields {@link Class#getDeclaredFields()}
		 * of this class and its super-classes (NOT super than the class {@link DefaultRecognizable} ),
		 * then put the pair ('fieldName', 'fieldName') into the Persistable Map.
		 */
		@Override
		public Map<String, String> getPersitableFields(){

			if(fieldNameToPersistKeyMap==null){
				fieldNameToPersistKeyMap = new HashMap<String, String>();

				Class<?> clazz = getClass();
				String fieldname = null;

				while(clazz!=null){
					Field[] fields = clazz.getDeclaredFields();
					for(Field field:fields){
						fieldname = field.getName();
						fieldNameToPersistKeyMap.put(fieldname, fieldname);
					}

					//We stop at class DefaultRecognizable, non further than it.
					if(DefaultRecognizable.class.getName().equals(clazz.getName())){
						break;
					}
					clazz = clazz.getSuperclass();
				}
			}

			return fieldNameToPersistKeyMap;
		}

		/**
		 * @return Set<String>, a set containing the fields NOT for generating the "engine RS".
		 */
		protected Set<String> getIgnoredRecognizableFields(){
			Set<String> ignored = new HashSet<String>();

			ignored.add("rawRS");
			ignored.add("engineRS");
			ignored.add("fieldNameToRsKeyMap");

			return ignored;
		}

		/**
		 * @return Map<String, String>, a map containing the fields for generating the "engine RS".
		 */
		public Map<String, String> getRecognizableFields(){

			if(fieldNameToRsKeyMap==null){
				fieldNameToRsKeyMap = new HashMap<String, String>();

				fieldNameToRsKeyMap.putAll(getPersitableFields());

				for(String ignored:getIgnoredRecognizableFields()){
					fieldNameToRsKeyMap.remove(ignored);
				}
			}

			return fieldNameToRsKeyMap;
		}

	}

	protected static class DefaultRecognizableAutoIt extends DefaultRecognizable{

		/** the mapping RS key is {@link AutoItConstants#RS_KEY_TITLE} */
		protected String rs_gen_title = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_CLASS} */
		protected String rs_gen_class = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_TEXT} */
		protected String rs_gen_text = null;

		/** the mapping RS key is {@link AutoItConstants#RS_KEY_X} */
		protected String rs_gen_x = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_Y} */
		protected String rs_gen_y = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_W} */
		protected String rs_gen_w = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_H} */
		protected String rs_gen_h = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_INSTANCE} */
		protected String rs_gen_instance = null;

		public DefaultRecognizableAutoIt(String originalRS){
			super(originalRS);
		}

		/**
		 * This implementation will:
		 * <ul>
		 * <li>remove {@link AutoItRs#AUTOIT_PREFIX} from field {@link DefaultRecognizable#rawRS}
		 * </ul>
		 */
		@Override
		protected void beforeParse(){
			//remove ":autoit:" from field 'originalRS'.
			if(isAutoitBasedRecognition(rawRS)){
				rawRS = rawRS.substring(AUTOIT_PREFIX.length());
				rawRS = rawRS.trim();
			}

			//If the raw RS is provided as engine RS directly, then set the rawRS to engineRS
			if(isEngineRs(rawRS)){
				engineRS = rawRS;
			}
		}

		/**
		 * This implementation will get pairs of (rsKey, value) from original RS,
		 * and set the value to the property of this class.
		 */
		@Override
		protected void parsing(){

			if(engineRS!=null){
				IndependantLog.debug("The engineRS '"+engineRS+"' has already been assigned. Skip parsing.");
				return;
			}

			String[] properties = null;
			properties = rawRS.split(AUTOIT_DELIMITER);
			String[] propertyAndValue = null;

			String rsKey = null;
			String value = null;

			for (String temp : properties) {
				try {
					propertyAndValue = temp.split(AutoItConstants.AUTOIT_ASSIGN_SEPARATOR);
					rsKey = propertyAndValue[0].trim();
					value = propertyAndValue[1].trim();

					if(!setField(rsKey, value)){
						IndependantLog.warn("Failed to set '"+value+"' to '"+rsKey+"'.");
					}

				}catch(Exception x){
					IndependantLog.warn("Failed to parse RS '"+temp+"', due to"+ x.toString());
				}
			}

		}

		@Override
		public boolean setField(String rsKey, Object value){

			if(AutoItConstants.RS_KEY_CAPTION.toLowerCase().equals(rsKey.toLowerCase())){
				//normally, 'caption' will also be considered as the 'title'
				rsKey = AutoItConstants.RS_KEY_TITLE;

				//But, if the value is a wildcard string, 'caption' should be considered as the 'regexptitle'
				if(value!=null){
					if(StringUtils.contains(value.toString(), StringUtils.WILDCARD_CHARS)){
						rsKey = AutoItConstants.RS_KEY_REGEXPTITLE;
						value = StringUtils.wildcardToRegex(value.toString());
					}
				}
			}else if(AutoItConstants.RS_KEY_INDEX.toLowerCase().equals(rsKey.toLowerCase())){
				//'index' will also be considered as the 'instance'
				rsKey = AutoItConstants.RS_KEY_INSTANCE;
			}

			String property = rsKeyToProperty(rsKey);

			return super.setField(property, value);
		}
		/**
		 * Only those fields starting with {@link AutoItConstants#PREFIX_PROPERTY_FOR_RS} are considered as
		 * recognizable fields, so fields not starting with that prefix will be removed from the map.
		 */
		@Override
		public Map<String, String> getRecognizableFields(){
			super.getRecognizableFields();

			String rsKey = null;

			for(String field: fieldNameToRsKeyMap.keySet()){
				if(!field.startsWith(AutoItConstants.PREFIX_PROPERTY_FOR_RS)){
					//removed the field NOT staring with AutoItConstants.PREFIX_PROPERTY_FOR_RS
					fieldNameToRsKeyMap.remove(field);
				}else{
					//If rsKey start with AutoItConstants.PREFIX_PROPERTY_FOR_RS, then remove the prefix
					rsKey = fieldNameToRsKeyMap.get(field);
					if(rsKey.startsWith(AutoItConstants.PREFIX_PROPERTY_FOR_RS)){
						fieldNameToRsKeyMap.put(field, rsKey.substring(AutoItConstants.PREFIX_PROPERTY_FOR_RS.length()));
					}
				}
			}

			return fieldNameToRsKeyMap;
		}

		/**
		 * Convert the rsKey to the class real property name.
		 * @param rsKey String, the key name in original RS, such as 'title', 'Title', 'TITLE'.
		 * @return String, the class real property name.
		 */
		protected String rsKeyToProperty(String rsKey){
			Map<String, String> fieldToRSKeyMap = getRecognizableFields();
			String tempRSKey = null;

			for(String field: fieldToRSKeyMap.keySet()){
				tempRSKey = fieldToRSKeyMap.get(field);
				if(rsKey.toLowerCase().equals(tempRSKey.toLowerCase())){
					return field;
				}
			}
			return rsKey;
		}

		/**
		 * Append "rsKey:value;" to rs.
		 * @param rs StringBuilder
		 * @param rsKey String
		 * @param value Object
		 */
		protected void appendEngineRS(StringBuilder rs, String rsKey, Object value){
			rs.append(rsKey+AutoItConstants.AUTOIT_RSKEY_VALUE_DELIMITER +value+AUTOIT_DELIMITER+" ");
		}

		@Override
		public String getEngineRS(){
			//will return something like "[TITLE:My Window; CLASS:My Class; INSTANCE:2]"
			Map<String, String> fieldToRSKeyMap = getRecognizableFields();
			if(engineRS==null){
				StringBuilder rs= new StringBuilder();
				rs.append(AutoItConstants.AUTOIT_ENGINE_RS_START);
				Set<String> fields = fieldToRSKeyMap.keySet();
				Object value = null;
				for(String field: fields){
					value = getField(field);
					if(value!=null){
						//--> rsKey:value;
						appendEngineRS(rs, fieldToRSKeyMap.get(field), value);
					}
				}

				rs.append(AutoItConstants.AUTOIT_ENGINE_RS_END);

				IndependantLog.debug(StringUtils.debugmsg(false)+"engineRS="+engineRS);
				engineRS = rs.toString();
			}

			return engineRS;

		}

		public String getText(){
			return rs_gen_text;
		}
		public String setText(String rs_gen_text){
			return this.rs_gen_text=rs_gen_text;
		}

		public String toString(){

			StringBuilder sb= new StringBuilder();
			Set<String> properties = getRecognizableFields().keySet();
			Object value = null;

			sb.append("\n========= "+this.getClass().getSimpleName()+" ========");
			for(String property: properties){
				value = getField(property);
				if(value!=null){
					sb.append("\n"+property+"="+value);
				}
			}
			sb.append("\n=========================================================\n");

			return sb.toString();
		}
	}

	protected static class Window extends DefaultRecognizableAutoIt{

		private int titleMatchMode = AutoItConstants.MATCHING_PATIAL;//Matches partial text from the start.

		public int getTitleMatchMode() {
			return titleMatchMode;
		}
		public void setTitleMatchMode(int titleMatchMode) {
			this.titleMatchMode = titleMatchMode;
		}

		/** the mapping RS key is {@link AutoItConstants#RS_KEY_REGEXPTITLE} */
		protected String rs_gen_regexptitle = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_REGEXPCLASS} */
		protected String rs_gen_regexpclass = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_LAST} */
		protected Boolean rs_gen_last = false;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_ACTIVE} */
		protected Boolean rs_gen_active = false;

		@Override
		protected Set<String> getIgnoredRecognizableFields(){
			Set<String> ignored = super.getIgnoredRecognizableFields();
			if(ignored==null){
				ignored = new HashSet<String>();
			}
			ignored.add("titleMatchMode");

			return ignored;
		}

		/**
		 * The "window text" consists of all the text that AutoIt can "see".
		 * This will usually be things like the contents of edit but will also include other text like:
		 * <ol>
		 * <li>Button text like &Yes, &No, &Next (the & indicates an underlined letter)
		 * <li>Dialog text like "Are you sure you wish to continue?"
		 * <li>Control text
		 * <li>Misc text - sometimes you don't know what it is :)
		 * </ol>
		 * The important thing is that you can use the text along with the title to uniquely identify a window to work with.
		 * When you specify the text parameter in a window function it is treated as a substring.
		 *
		 */
		//protected String text = null;

		public Window(String rs){
			super(rs);
		}

		public Window(String rs, int titleMatchMode){
			this(rs);
			this.titleMatchMode = titleMatchMode;
		}

		/**
		 * If the window title is not complete, call this method to complete.
		 * @param it The AutoIt instance.
		 */
		public void normalize(AutoItXPlus it){
			if(it!=null && StringUtils.isValid(rs_gen_title)){
				rs_gen_title = it.winGetTitle(rs_gen_title, rs_gen_text);
			}
		}

		@Override
		protected void appendEngineRS(StringBuilder rs, String rsKey, Object value){

			if(AutoItConstants.RS_KEY_LAST.equals(rsKey) ||
			   AutoItConstants.RS_KEY_ACTIVE.equals(rsKey) ){
				if(StringUtilities.convertBool(value)){
					rs.append(rsKey+AUTOIT_DELIMITER+" ");
				}
			}else if(AutoItConstants.RS_KEY_TEXT.endsWith(rsKey)){
				//ignore this, it will be returned separately by getText()
			}
			else{
				super.appendEngineRS(rs, rsKey, value);
			}
		}

	}

	protected static class Control extends DefaultRecognizableAutoIt{
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_ID} */
		protected String rs_gen_id = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_CLASSNN} */
		protected String rs_gen_classnn = null;
		/** the mapping RS key is {@link AutoItConstants#RS_KEY_NAME} */
		protected String rs_gen_name = null;

		public Control(String rs){
			super(rs);
		}

	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nRAW RS:\nwindow: "+winRawRS);
		if(!isWindow){
			sb.append("\ncontrol: "+compRawRS);
		}

		sb.append("\nENGINE RS:\nwindow: "+window.getEngineRS()+"\nwindow Text: "+ getWindowText());
		if(!isWindow){
			sb.append("\ncontrol: "+control.getEngineRS());
		}

		return sb.toString();
	}

}
