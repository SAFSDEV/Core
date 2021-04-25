/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.staf.service.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.safs.Constants;
import org.safs.Log;
import org.safs.StringUtils;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.var.AbstractSAFSVariableService;
import org.safs.tools.CaseInsensitiveFile;

import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;

/*******************************************************************************************
 * Copyright 2003 SAS Institute
 * GNU General Public License (GPL) http://www.opensource.org/licenses/gpl-license.php
 * <p>
 * This SAFSAppMapService class is an external STAF service run by the JSTAF Service Proxy.<br>
 * The intention is to provide global read-only services for using SAFSAppMapFile files.
 * These files are, essentially, standard text-based Windows INI format files with
 * Name=Value entries that can be grouped into named sections or blocks.
 * <p>
 * It is important to note that in a true SAFS environment, AppMap services are nearly always
 * tightly integrated with one or more SAFS Variable services.  This is because we allow
 * the retrieval of Variable values to "look thru" to AppMap "constants".  And we allow the
 * AppMap to provide dynamic values via Variables for items that are otherwise normally static.
 * <p>
 * The AppMapService adds additional lookup capabilities to the simple DEFAULTMAPSECTION
 * capabilities of the underlying SAFSAppMapFile.  The lookup tries the following:
 * <ol>
 * <li>look for ITEM in SECTION as provided
 * <li>not found? look for ITEM in the "default" SECTION
 * <li>not found? look for ITEM in empty, unnamed section
 * </ol>
 * <p>
 * The AppMapService typically looks for values in the 'default' AppMap.  This is typically the
 * last AppMap that was OPENed.  This service will also now lookup values in what is called
 * the AppMap 'chain'.  This chain is simply a Last-In-First-Out (LIFO) buffer of open AppMaps.
 * As an AppMap is OPENed it is placed at the top of the search chain.  AppMaps already in the
 * chain are moved down the chain and searched in a LIFO order.  This allows a primary AppMap to
 * be OPENed that might contain many default and shared values while subsequently opened AppMaps
 * might have overriding entries, entries that build upon the defaults, or contain locale-specific
 * entries for things like NLS testing.
 * <p>
 * The AppMap service will allow dynamic values to be specified in the AppMap file.  This
 * is done by providing the special SAM_DDV_PREFIX value in place of a fixed literal string.
 * <p>
 * &nbsp; &nbsp; ;these two items have no section identifier<br>
 * &nbsp; &nbsp; ;they are part of an initial, unnamed section<br>
 * &nbsp; &nbsp; AnItem  = A normal static value<br>
 * &nbsp; &nbsp; NewItem = Another static value<br>
 * &nbsp; &nbsp; <br>
 * &nbsp; &nbsp; [ASection]<br>
 * &nbsp; &nbsp; AnItem = A normal static value<br>
 * &nbsp; &nbsp; Item2  = _DDV:<br>
 * &nbsp; &nbsp; Item3  = _DDV:AVariableName<br>
 * <p>
 * The syntax for Item2 specifies that the value should be satisfied by the Variable
 * service retrieving the value of variable "Item2".
 * <p>
 * The syntax for Item3 specifies that the value should be satisfied by the Variable
 * service retrieving the value of variable "AVariableName".
 * <p>
 * The AppMap service also allows variable references to be embedded in AppMap entries.  This
 * is done by tightly wrapping DDVariable references in curly braces {^varName} anywhere in the
 * value portion of the AppMap entry.  There should be no spaces anywhere between the curly braces
 * or the variable will be considered literal text and will not be resolved.
 * <p>
 * &nbsp; &nbsp; ;these two items have no section identifier<br>
 * &nbsp; &nbsp; ;they are part of an initial, unnamed section<br>
 * &nbsp; &nbsp; AnItem  = A normal static value<br>
 * &nbsp; &nbsp; NewItem = Another static value<br>
 * &nbsp; &nbsp; <br>
 * &nbsp; &nbsp; [ASection]<br>
 * &nbsp; &nbsp; AnItem = A normal static value<br>
 * &nbsp; &nbsp; Item2  = Type=Component;Text={^text}<br>
 * &nbsp; &nbsp; Item3  = Type=Window;Caption={^caption};\;Type=CheckBox;Text={^text}<br>
 * <p>
 * The syntax for Item2 specifies that the value of the DDVariable ^text should be retrieved and
 * embedded in the value provided for Item2.
 * <p>
 * The syntax for Item3 specifies that the value of the DDVariables ^caption and ^text should be
 * retrieved and embedded in the value provided for Item3.
 * <p>
 * <b>The SAFSAppMapService service provides the following commands:</b>
 * <p>
 * <table>
 * <tr><td width="40%">
 *         OPEN               <td>Open an AppMap
 * <tr><td>SAFSVARS           <td>Get/Set the associated SAFSVARS service name
 * <tr><td>DEFAULTMAP         <td>Get/Set the the ID of the "default" AppMap
 * <tr><td>DEFAULTMAPSECTION  <td>Get/Set the "default" lookup Section within an AppMap
 * <tr><td>GETITEM            <td>Get an AppMap item
 * <tr><td>CLEARCACHE         <td>Clear all cached items in all open App Maps
 * <tr><td>DISABLECHAIN       <td>Disable the chaining of AppMaps when seeking entries
 * <tr><td>ENABLECHAIN        <td>Enable the chaining of AppMaps when seeking entries (default)
 * <tr><td>DISABLERESOLVE     <td>Disable the resolving of embedded DDVariables
 * <tr><td>ENABLERESOLVE      <td>Enable the resolving of embedded DDVariables (default)
 * <tr><td>QUERY              <td>Get other information from an AppMap
 * <tr><td>CLOSE              <td>Close and release resources on an AppMap
 * <tr><td>LIST               <td>List information for all open AppMaps
 * <tr><td>HANDLEID           <td>Return the handle used by this service
 * <tr><td>HELP               <td>Display this help information
 * </table>
 * <h2>1.0 Service Registration</h2>
 * <p>
 * Each instance of the service must be registered via the STAF Service service.
 * <p>
 * Examples showing comandline registration:
 * <p><pre>
 * STAF LOCAL SERVICE ADD SERVICE &lt;servicename> LIBRARY JSTAF /
 *            EXECUTE org/safs/staf/service/SAFSAppMapService [PARMS &lt;Parameters>]
 *
 * SERVICE ADD SERVICE sharedmaps LIBRARY JSTAF EXECUTE org/safs/staf/service/SAFSAppMapService /
 *
 * SERVICE ADD SERVICE othermaps  LIBRARY JSTAF EXECUTE org/safs/staf/service/SAFSAppMapService /
 *                                PARMS DIR "c:\repo\Datapool" EXT ".dat" SAFSVARS "ddvariables"
 *
 * </pre>
 * <p>
 * By default, the service expects a "SAFSVARS" SAFSVariableService to handle Variable calls.
 * <p>
 * <b>1.1</b> Valid Parameters when registering the service:
 * <p>
 * <b>1.1.1 DIR</b> &lt;default directory><br>
 * If provided, the DIR parameter specifies a default directory to use if the OPEN
 * request provides relative path information or no path information at all.  File
 * searches do not use system PATH information.  The OPEN request expects a
 * full filename path, or a path relative to this DIR option.
 * <p>
 * If the DIR parameter is not provided, then OPEN requests will not attempt
 * relative path searches.  The filename provided to the OPEN request must then be
 * an exact full filepath match or the OPEN request will fail.
 * <p>
 * EX: &lt;PARMS> DIR "c:\testrepo\Datapool"
 * <p>
 * <b>1.1.2 EXT</b> &lt;default file extension><br>
 * If provided, the EXT parameter specifies a default file extension (suffix) to try if
 * the OPEN request does not find the file as provided.  You must include any period
 * (.) if it is to be part of any appended suffix.  By default, the service expects a
 * default extension of ".map"
 * <p>
 * EX: &lt;PARMS> EXT ".dat"
 * <p>
 * <b>1.1.3 SAFSVARS</b> &lt;servicename><br>
 * If provided, the SAFSVARS parameter specifies the name of the service that will provide
 * SAFS Variables to this AppMap service.  By default, the service automatically expects
 * a "SAFSVARS" SAFSVariableService to handle requests.
 * <p>
 * EX: &lt;PARMS> SAFSVARS "ddvariables"
 * <p>
 * <b>1.1.4 DISABLECHAIN</b><br>
 * The DISABLECHAIN parameter instructs the service to forego the default operation of
 * searching all open AppMaps when seeking a requested entry.
 * <p>
 * EX: &lt;PARMS> DISABLECHAIN
 * <p>
 * <b>1.1.5 DISABLERESOLVE</b><br>
 * The DISABLERESOLVE parameter instructs the service to forego the default operation of
 * resolving DDVariable references embedded in AppMap entries.
 * <p>
 * EX: &lt;PARMS> DISABLERESOLVE
 * <p>
 * <h2>2.0 Commands</h2>
 * <p>
 * <h3>2.1 OPEN </h3>
 * <p>
 * The OPEN command attempts to open a file for read operations.  The AppMap will be added to
 * the top of the list of AppMaps to be searched on subsequent requests for AppMap entries.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * OPEN &lt;AppMapID> FILE &lt;Filename> [STORED | MAPPED] [DEFAULTMAP]
 * <p>
 * <b>2.1.1 AppMapID</b> is a unique ID for this AppMap instance.<br>
 * <p>
 * <b>2.1.2 FILE</b> is the filename of the AppMap to load.<br>
 * If a default Directory was specified when the service was launched, then the filename
 * can be relative to that directory.  Otherwise, the full filepath must be specified.<br>
 * If a default Extension (Ext) was specified when the service was launched, then the
 * filename can be specified without the extension.<br>
 * <p>
 * <b>2.1.3 STORED</b> read the app map entirely into memory.
 * <p>
 * <b>2.1.4 MAPPED</b> use memory mapped file handling and DO NOT load the entire file
 * into memory.  This is for efficiencies when handling very large files. <br>
 * <b>(This is not yet implemented.)</b>
 * <p>
 * <b>2.1.5 DEFAULTMAP</b> instructs the service to set this AppMap as the "default" AppMap.
 * By default, the first map opened by the service is automatically set as "default".
 * <p>
 * <h3>2.2 SAFSVARS </h3>
 * <p>
 * The SAFSVARS command is used to read or set the current SAFSVARS setting for the service.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * SAFSVARS &lt;no parameter > :returns the current setting, if any<br>
 * SAFSVARS &lt;servicename> :sets the name of the SAFSVARS service for the AppMap service
 * to use.
 * <p>
 * <b>2.2.1 servicename</b> sets the name of the SAFSVARS service to use.<br>
 * By default, the service expects the "SAFSVARS" service to handle variable resolution.
 * <p>
 * <h3>2.3 DEFAULTMAP</h3>
 * <p>
 * The DEFAULTMAP command is used to read or set the "default" AppMap setting for the service.
 * By default, the first AppMap opened will be set as "default".
 * <p>
 * <b>Syntax:</b>
 * <p>
 * DEFAULTMAP &lt;no parameter > :returns the current setting, if any<br>
 * DEFAULTMAP &lt;AppMapID> :sets the provided AppMapID as the new "default" AppMap.
 * <p>
 * <b>2.3.1 AppMapID</b> is the unique ID of the AppMap to set as "default".<br>
 * <p>
 * <h3>2.4 DEFAULTMAPSECTION </h3>
 * <p>
 * The DEFAULTMAPSECTION command is used to read or set the "default" Section with an AppMap.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * Note, any time the optional AppMapID is not provided, the current "default"
 * AppMap is assumed.
 * By default, the "ApplicationConstants" section is set as "default" if it exists when
 * the Map is opened.
 * <p>
 * DEFAULTMAPSECTION [&lt;AppMapID>]  returns the current setting.<br>
 * DEFAULTMAPSECTION [&lt;AppMapID>] SECTION [&lt;section>]  sets the provided &lt;section> as
 * the new "default" Section for the AppMap.  An empty value for SECTION sets the unnamed
 * initial Section in the AppMap as "default".
 * <p>
 * <b>2.4.1 AppMapID</b> is the ID of the AppMap to reference.  If not provided, the "default"
 * AppMap will be used.
 * <p>
 * <b>2.4.2 SECTION</b> if not provided, the request will be a read operation returning
 * the current setting.  If SECTION is provided, the request sets the the AppMap's "default"
 * section.<br>
 * <p>
 * <h3>2.5 GETITEM </h3>
 * <p>
 * The GETITEM command requests an item stored in an AppMap.  The AppMap does NOT have to have
 * been previously opened.  However, a call to GETITEM on an unopened AppMap will cause the
 * search to limit its scope to the specified AppMap--the default behavior of searching all open
 * AppMaps will not be honored.  In addition, specifying an unopened AppMap will prevent that
 * AppMap from being added to the chain of searchable AppMaps during the current request.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * GETITEM [&lt;AppMapID>] [ SECTION [&lt;section> | "DEFAULTMAPSECTION"] ]  ITEM &lt;item [ISDYNAMIC]>
 * <p>
 * <b>2.5.1 AppMapID</b> is the ID of the AppMap to use for lookup.
 * If not provided, the "default" AppMap will be used.
 * <p>
 * <b>2.5.2 SECTION</b> is the section in the AppMap to use for lookup. If not passed,
 * then the first, unnamed AppMap section will be used. If you want to reference the
 * DEFAULTMAPSECTION for the map, the value passed to SECTION should be "DEFAULTMAPSECTION",
 * including quotes.<br>
 * <p>
 * <b>2.5.3 ITEM</b> is the name of the item within the section to return.<br>
 * <p>
 * <b>2.5.4 ISDYNAMIC</b> optional parameter stating whether we should check if the recognition is dynamic<br>
 * <p>
 * <h3>2.6 CLEARCACHE</b> </h3>
 * <p>
 * The CLEARCACHE command will clear all cached items in all open AppMaps and cause
 * each stored AppMap to be refreshed.  This is generally necessary to reload any changed
 * AppMap without taking down the service.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * CLEARCACHE
 * <p>
 * <h3>2.7 CLOSE</b> </h3>
 * <p>
 * The CLOSE command closes and releases resources for the AppMap.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * CLOSE [ &lt; AppMapID > ]
 * <p>
 * <b>2.7.1 AppMapID</b> is the ID  of the AppMap to close.
 * If not provided, the "default" AppMap will be closed.
 * <p>
 * <h3>2.8 QUERY </h3>
 * <p>
 * The QUERY command returns requested information about an open file.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * QUERY [ &lt; AppMapID > ] &lt;MODE | SECTIONS | <br>
 *       ITEMS [ [&lt;section> | "DEFAULTMAPSECTION"] ] | DEFAULTMAPSECTION | <br>
 *       FILENAME | FULLPATH ><br>
 * <p>
 * <b>2.8.1 AppMapID</b> is the ID of the AppMap to query.
 * If not provided, the DEFAULT AppMap will be used.
 * <p>
 * <b>2.8.2 MODE</b> returns the STORED or MAPPED memory mode of the AppMap.
 * <p>
 * <b>2.8.3 SECTIONS</b> returns the list of sections in the AppMap.
 * <p>
 * <b>2.8.4 ITEMS</b> returns the list of items in a section. If not provided, the items in the
 * first, unnamed section of the AppMap will be returned.  If you want to reference the
 * DEFAULTMAPSECTION for the map, the value passed to ITEMS should be "DEFAULTMAPSECTION",
 * including quotes.<br>
 * <p>
 * <b>2.8.5 DEFAULTMAPSECTION</b> returns the setting for the "default" AppMap section.
 * <p>
 * <b>2.8.6 FILENAME</b> is the filename to the file without any path information.
 * <p>
 * <b>2.8.7 FULLPATH</b> is the full path to the file.  Due to the OPEN parameters DIR and
 * EXT, the full path to the file may be different than any relative path information that
 * was provided to the OPEN command.
 * <p>
 * <h3>2.9 DISABLECHAIN</b> </h3>
 * <p>
 * The DISABLECHAIN command will prevent the service from searching all open AppMaps when
 * seeking a requested entry.  Only the specified AppMap or the DEFAULTMAP will be searched.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * DISABLECHAIN
 * <p>
 * <h3>2.10 ENABLECHAIN</b> </h3>
 * <p>
 * The ENABLECHAIN command will (re)enable the service to search all open AppMaps when
 * seeking a requested entry.  This is the default search mode for this feature.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * ENABLECHAIN
 * <p>
 * <h3>2.11 DISABLERESOLVE</b> </h3>
 * <p>
 * The DISABLERESOLVE command will prevent the service from resolving embedded DDVariable references
 * in AppMap entries.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * DISABLERESOLVE
 * <p>
 * <h3>2.12 ENABLERESOLVE</b> </h3>
 * <p>
 * The ENABLERESOLVE command will (re)enable the service to resolve embedded DDVariable references
 * in AppMap entries.  This is the default mode for this feature.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * ENABLERESOLVE
 * <p>
 * <h3>2.13 LIST </h3>
 * <p>
 * The LIST command returns info on each open AppMap for the service.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * LIST
 * <p>
 * <h3>2.14 HANDLEID </h3>
 * <p>
 * Returns the HANDLE associated with the SAFSAppMapService.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * HANDLEID
 * <p>
 * <h3>2.15 HELP </h3>
 * <p>
 * The HELP command returns this syntax information for service requests.
 * <p>
 * <b>Syntax:</b>
 * <p>
 * HELP
 * <p>
 * Software Automation Framework Support (SAFS) https://safsdev.github.io/<br>
 * Software Testing Automation Framework (STAF) http://staf.sourceforge.net<br>
 * @author Carl Nagle JUL 03, 2003 Moved initialization into init, out of constructor.
 * @author Carl Nagle DEC 11, 2003 Added CLEARCACHE command.
 * @author Carl Nagle JUL 27, 2006 Added DISABLECHAIN and ENABLECHAIN commands.
 * @author Carl Nagle JUL 31, 2006 Added DISABLERESOLVE and ENABLERESOLVE commands.
 * @author Carl Nagle JUN 20, 2007 Fixed appmap to store maps not in a product app map chain.
 * @author Carl Nagle SEP 26, 2007 Fixed App Map Resolve problem with explicit ApplicationConstants section
 * @author JunwuMa    MAY 12, 2009 Adding support for STAF3.
 *                                 Renamed SAFSAppMapService (old class only for STAF2) with AbstractSAFSAppMapService and keep common operations in it.
 *                                 Two different versions of the service extend this class for supporting STAF2 and STAF3.
 * @author JunwuMa    SEP 17, 2010 Fixed the problem about chained maps' number does not decrease with SAFSMAPS Close command.
 * @author Lei Wang     APR 05, 2012 Add option "MAPVARLOOP" for command "GETITEM", see Testhelp078350
 *
 * @see SAFSAppMapService SAFSAppMapService3 SAFSVariableService SAFSVariableService3
 *********************************************************************************************/
public abstract class AbstractSAFSAppMapService {

	public static final String DEFAULT_SECTION_NAME           = "ApplicationConstants";

	public int  SAM_SERVICE_REQUEST_ARGS_MAX    = 11;  //most OPEN should see
	public int  SAM_SERVICE_INIT_ARGS_MAX       = 7;  //most INIT should see

	public static final String SAM_SERVICE_OPTION_DIR         = "DIR";
	public static final String SAM_SERVICE_OPTION_EXT         = "EXT";
	public static final String SAM_SERVICE_OPTION_SAFSVARS    = "SAFSVARS";
	public static final String SAM_SERVICE_PROCESS_NAME       = "SAFSAppMapService";

	public static final String SAM_SERVICE_REQUEST_HANDLEID   = "HANDLEID";
	public static final String SAM_SERVICE_REQUEST_OPEN       = "OPEN";
	public static final String SAM_SERVICE_REQUEST_GETITEM    = "GETITEM";
	public static final String SAM_SERVICE_REQUEST_CLEARCACHE = "CLEARCACHE";
	public static final String SAM_SERVICE_REQUEST_DISABLECHAIN   = "DISABLECHAIN";
	public static final String SAM_SERVICE_REQUEST_ENABLECHAIN    = "ENABLECHAIN";
	public static final String SAM_SERVICE_REQUEST_DISABLERESOLVE = "DISABLERESOLVE";
	public static final String SAM_SERVICE_REQUEST_ENABLERESOLVE  = "ENABLERESOLVE";
	public static final String SAM_SERVICE_REQUEST_LIST       = "LIST";
	public static final String SAM_SERVICE_REQUEST_QUERY      = "QUERY";
	public static final String SAM_SERVICE_REQUEST_HELP       = "HELP";
	public static final String SAM_SERVICE_REQUEST_CLOSE      = "CLOSE";

	public static final String SAM_SERVICE_REQUEST_DEFAULTMAP        = "DEFAULTMAP";
	public static final String SAM_SERVICE_REQUEST_DEFAULTMAPSECTION = "DEFAULTMAPSECTION";

	public static final String SAM_SERVICE_PARM_FILE           = "FILE";
	public static final String SAM_SERVICE_PARM_STORED         = "STORED";
	public static final String SAM_SERVICE_PARM_MAPPED         = "MAPPED";

	public static final String SAM_SERVICE_PARM_SECTION        = "SECTION";
	public static final String SAM_SERVICE_PARM_ITEM           = "ITEM";
	public static final String SAM_SERVICE_PARM_ISDYNAMIC      = "ISDYNAMIC";
	public static final String SAM_SERVICE_TAGGED_PREFIX       = ";RECOGNITION=";

	/**
	 * Used for {@link #SAM_SERVICE_REQUEST_GETITEM}<br>
	 * Used internally to stop the loop between map service and variable service.<br>
	 * "MAPVARLOOP" can have parameter, a delimited string, the items have been processed<br>
	 * in map service by command "GETITEM". The delimiter is {@value #SAM_SERVICE_PARM_MAP_VAR_LOOP_SEP}<br>
	 *
	 * Example:<br>
	 * MAPVARLOOP item1_SEP:item2_SEP:item3
	 *
	 * @see #doAcceptRequest(String, String, int, String)
	 * @see #process_DDV(STAFResult, SAFSAppMapFile, String, String, boolean, List)
	 * @see #process_Resolve(STAFResult, SAFSAppMapFile, String, boolean, List)
	 */
	public static final String SAM_SERVICE_PARM_MAP_VAR_LOOP       = "MAPVARLOOP";
	public static final String SAM_SERVICE_PARM_MAP_VAR_LOOP_SEP   = "_SEP:";

	public static final String SAM_SERVICE_PARM_MODE           = "MODE";
	public static final String SAM_SERVICE_PARM_SECTIONS       = "SECTIONS";
	public static final String SAM_SERVICE_PARM_ITEMS          = "ITEMS";
	public static final String SAM_SERVICE_PARM_FILENAME       = "FILENAME";
	public static final String SAM_SERVICE_PARM_FULLPATH       = "FULLPATH";

	public static final String SAM_DDV_PREFIX                  = "_DDV:";
	public static final String SAM_RESOLVE_PREFIX              = Constants.EMBEDDED_VAR_PREFIX;
	public static final String SAM_RESOLVE_SUFFIX              = Constants.EMBEDDED_VAR_SUFFIX;

	public static final String SAM_CURRENTWINDOW_ITEM          = "CurrentWindow";
	public static final int    SAM_DDV_PREFIX_LEN = 5;

	public static final String SAM_DEFAULT_EXT                 = ".map";

	protected Hashtable appmaps = new Hashtable(5);

	protected LinkedList      chain = new LinkedList();

	protected boolean chain_enabled = true;
    protected boolean honor_chain   = chain_enabled;
    protected int     chain_index	= -1;
    protected boolean request_get_item = false;

    protected boolean resolve_enabled = true;

	protected STAFCommandParser parser = new STAFCommandParser(SAM_SERVICE_REQUEST_ARGS_MAX);

	protected String defaultdir   = new String();
	protected String defaultext   = new String(SAM_DEFAULT_EXT);
	protected String defaultmap   = new String();

	protected String servicevars  = new String(SAM_SERVICE_OPTION_SAFSVARS);
	protected String servicename  = new String();
	protected String serviceparms = new String();

	protected boolean root_path_available    = false;
	protected boolean file_ext_available     = false;
	protected boolean service_vars_available = false;

	protected HandleInterface client;  // should be passed in from its derived class

	private static String empty = new String();
	private static String c = ":"; // colon
	private static String s = " "; // space
	private static String r = "\n"; // newline
	private static String q = "\""; // quote

	/**********************************************************************
	 * 	Initialize the class, primarily, the parser used to parse service requests.
	 **********************************************************************/
	public AbstractSAFSAppMapService () {
	}

	/**********************************************************************
	 * 	our HELP text
	 **********************************************************************/
	protected String getHELPInfo(){
		return  r+
		        "SAFSAppMapService HELP" +r+
		        r+
		        "HANDLEID" +r+
		        "OPEN     <appmapID> FILE <filename> [STORED] [MAPPED] [DEFAULTMAP]" +r+
		        "SAFSVARS [<safsVarService>]" +r+
		        r+
		        "DEFAULTMAP        [<appmapID>]" +r+
		        "DEFAULTMAPSECTION [<appmapID>]" +r+
		        "DEFAULTMAPSECTION [<appmapID>] SECTION [<section>]" +r+
		        r+
		        "GETITEM [<appmapID>] [SECTION <[<section>|\"DEFAULTMAPSECTION\"]> ITEM <item> [ISDYNAMIC]" +r+
		        "CLEARCACHE" +r+
		        "DISABLECHAIN" +r+
		        "ENABLECHAIN" +r+
         		"DISABLERESOLVE" +r+
		        "ENABLERESOLVE" +r+
		        "QUERY   [<appmapID>] < MODE | DEFAULTMAPSECTION | SECTIONS | "   +r+
		        "                     ITEMS [<section> | \"DEFAULTMAPSECTION\"] | "+r+
		        "                     FILENAME | FULLPATH >" +r+
		        "LIST" +r+
		        "CLOSE [<appmapID>]" +r+
		        "HELP"+r+r;
	}


	// common init phase for STAFServiceInterfaceLevel1 and STAFServiceInterfaceLevel30
	/**********************************************************************
	 * Handle initializing this instance of the service for STAF
	 **********************************************************************/
	protected int doInit (HandleInterface client, String name, String params){

		this.client = client;
		servicename  = name;

		if (params == null) params = new String();
		serviceparms = params;

		parser.addOption( SAM_SERVICE_REQUEST_OPEN              , 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SAM_SERVICE_OPTION_SAFSVARS           , 1, STAFCommandParser.VALUEALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_HANDLEID          , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SAM_SERVICE_REQUEST_DEFAULTMAP        , 1, STAFCommandParser.VALUEALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_DEFAULTMAPSECTION , 1, STAFCommandParser.VALUEALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_CLEARCACHE        , 1, STAFCommandParser.VALUENOTALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_DISABLECHAIN      , 1, STAFCommandParser.VALUENOTALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_ENABLECHAIN       , 1, STAFCommandParser.VALUENOTALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_DISABLERESOLVE    , 1, STAFCommandParser.VALUENOTALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_ENABLERESOLVE     , 1, STAFCommandParser.VALUENOTALLOWED  );

		parser.addOption( SAM_SERVICE_REQUEST_GETITEM , 1, STAFCommandParser.VALUEALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_QUERY   , 1, STAFCommandParser.VALUEALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_CLOSE   , 1, STAFCommandParser.VALUEALLOWED  );
		parser.addOption( SAM_SERVICE_REQUEST_LIST    , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SAM_SERVICE_REQUEST_HELP    , 1, STAFCommandParser.VALUENOTALLOWED );

		parser.addOption( SAM_SERVICE_PARM_FILE       , 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SAM_SERVICE_PARM_MODE       , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SAM_SERVICE_PARM_STORED     , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SAM_SERVICE_PARM_MAPPED     , 1, STAFCommandParser.VALUENOTALLOWED );

		parser.addOption( SAM_SERVICE_PARM_ITEMS      , 1, STAFCommandParser.VALUEALLOWED    );
		parser.addOption( SAM_SERVICE_PARM_SECTIONS   , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SAM_SERVICE_PARM_FILENAME   , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SAM_SERVICE_PARM_FULLPATH   , 1, STAFCommandParser.VALUENOTALLOWED );

		parser.addOption( SAM_SERVICE_PARM_SECTION    , 1, STAFCommandParser.VALUEALLOWED );
		parser.addOption( SAM_SERVICE_PARM_ITEM       , 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SAM_SERVICE_PARM_ISDYNAMIC  , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SAM_SERVICE_PARM_MAP_VAR_LOOP  , 1, STAFCommandParser.VALUEALLOWED );

		// each request should have only 1 of these
		parser.addOptionGroup ( SAM_SERVICE_REQUEST_OPEN    +s+ SAM_SERVICE_REQUEST_HELP  +s+
		                        SAM_SERVICE_REQUEST_GETITEM +s+ SAM_SERVICE_REQUEST_QUERY +s+
		                        SAM_SERVICE_REQUEST_LIST    +s+ SAM_SERVICE_REQUEST_CLOSE +s+
		                        SAM_SERVICE_OPTION_SAFSVARS +s+ SAM_SERVICE_REQUEST_DEFAULTMAPSECTION +s+
		                        SAM_SERVICE_REQUEST_DEFAULTMAP  +s+ SAM_SERVICE_REQUEST_HANDLEID +s+
		                        SAM_SERVICE_REQUEST_CLEARCACHE  +s+ SAM_SERVICE_REQUEST_DISABLECHAIN +s+
		                        SAM_SERVICE_REQUEST_ENABLECHAIN +s+ SAM_SERVICE_REQUEST_DISABLERESOLVE +s+
		                        SAM_SERVICE_REQUEST_ENABLERESOLVE,
		                        1, 2);

		// QUERY parameters mutually exclusive
		parser.addOptionGroup(  SAM_SERVICE_PARM_MODE     +s+ SAM_SERVICE_PARM_SECTIONS          +s+
		                        SAM_SERVICE_PARM_ITEMS    +s+ SAM_SERVICE_PARM_FILENAME          +s+
		                        SAM_SERVICE_PARM_FULLPATH +s+ SAM_SERVICE_REQUEST_DEFAULTMAPSECTION,
		                        0, 2);

		parser.addOptionGroup(SAM_SERVICE_PARM_STORED +s+ SAM_SERVICE_PARM_MAPPED, 0, 1);

		// REQUIRED parameters for each type of request
		parser.addOptionNeed( SAM_SERVICE_REQUEST_OPEN    , SAM_SERVICE_PARM_FILE    );
		parser.addOptionNeed( SAM_SERVICE_REQUEST_GETITEM , SAM_SERVICE_PARM_ITEM    );

		// QUERY parameters exclusive to QUERY
		parser.addOptionNeed( SAM_SERVICE_PARM_MODE     , SAM_SERVICE_REQUEST_QUERY );
		parser.addOptionNeed( SAM_SERVICE_PARM_SECTIONS , SAM_SERVICE_REQUEST_QUERY );
		parser.addOptionNeed( SAM_SERVICE_PARM_ITEMS    , SAM_SERVICE_REQUEST_QUERY );
		parser.addOptionNeed( SAM_SERVICE_PARM_FILENAME , SAM_SERVICE_REQUEST_QUERY );
		parser.addOptionNeed( SAM_SERVICE_PARM_FULLPATH , SAM_SERVICE_REQUEST_QUERY );


		STAFCommandParser registrar = new STAFCommandParser(SAM_SERVICE_INIT_ARGS_MAX);

		registrar.addOption( SAM_SERVICE_OPTION_DIR      , 1, STAFCommandParser.VALUEREQUIRED );
		registrar.addOption( SAM_SERVICE_OPTION_EXT      , 1, STAFCommandParser.VALUEREQUIRED );
		registrar.addOption( SAM_SERVICE_OPTION_SAFSVARS , 1, STAFCommandParser.VALUEREQUIRED );
		registrar.addOption( SAM_SERVICE_REQUEST_DISABLECHAIN   , 1, STAFCommandParser.VALUENOTALLOWED );
		registrar.addOption( SAM_SERVICE_REQUEST_DISABLERESOLVE , 1, STAFCommandParser.VALUENOTALLOWED );

		STAFCommandParseResult parsedData = registrar.parse(params);
		if(parsedData.rc != STAFResult.Ok) return parsedData.rc;

		if ( parsedData.optionTimes(SAM_SERVICE_OPTION_DIR) > 0)
			defaultdir  = parsedData.optionValue(SAM_SERVICE_OPTION_DIR);

		if ( parsedData.optionTimes(SAM_SERVICE_OPTION_EXT) > 0)
			defaultext = parsedData.optionValue(SAM_SERVICE_OPTION_EXT);

		if ( parsedData.optionTimes(SAM_SERVICE_OPTION_SAFSVARS) > 0)
			servicevars = parsedData.optionValue(SAM_SERVICE_OPTION_SAFSVARS);

		if ( parsedData.optionTimes(SAM_SERVICE_REQUEST_DISABLECHAIN) > 0){
			chain_enabled = false;
			honor_chain = false;
		}

		if ( parsedData.optionTimes(SAM_SERVICE_REQUEST_DISABLERESOLVE) > 0)
			resolve_enabled = false;


		if (defaultext.length() > 0 ) file_ext_available = true;
		if (defaultdir.length() > 0 ){

			File f = new CaseInsensitiveFile(defaultdir).toFile();

			try{
				if (f.isDirectory()){

					root_path_available = true;
					if (!defaultdir.endsWith(File.separator)){ defaultdir += File.separator;}
				}
				else{

					//shouldn't have to reset--we're aborted by STAF
					defaultdir = new String();
					return STAFResult.DoesNotExist;
				}
			}catch(SecurityException e) {

				//shouldn't have to reset--we're aborted by STAF
				defaultdir = new String();
				root_path_available = false;
				return STAFResult.AccessDenied;
			}
		}
		return STAFResult.Ok;
	}



	private String getAppMapID(STAFResult result, STAFCommandParseResult parsedData, String option){

		String id = parsedData.optionValue(option);
		if (id.equals(empty)) {
			if (defaultmap.equals(empty)){
				result.rc = STAFResult.InvalidRequestString;
				result.result = "appmapID?";
			}
			return defaultmap;
		}
		return id;
	}

    private void addChainID(String id, SAFSAppMapFile afile){
    	String lcid = id.toLowerCase();
		appmaps.put( lcid, afile );
		Log.info("SAFSMAPS adding App Map '"+ id +"' to stored chain.");
		if(! request_get_item){
	    	if (chain.contains(lcid)) chain.remove(lcid);
	    	chain.addLast(lcid);
	    	if (chain_enabled) defaultmap = id;
			Log.info("SAFSMAPS default AppMap '"+ defaultmap +"'");
		}else{
			if (! chain.contains(lcid)) {
				chain.addLast(lcid);
				if (chain_enabled) defaultmap = id;
				Log.info("SAFSMAPS default AppMap '"+ defaultmap +"'");
			}
		}
    }

	private void removeChainID(String id){
		String lcid = id.trim().toLowerCase();
		if (appmaps.containsKey(lcid)) appmaps.remove(lcid);
	    if (chain.contains(lcid)) chain.remove(lcid);
	    if (! chain.isEmpty()) {
	    	defaultmap = (String) chain.getLast();
	    }else{
	    	defaultmap = "";
	    }
	}

	private String getNextChainID()throws NoSuchElementException{
		if (chain_index > 0 ){
			return (String) chain.get(--chain_index);
		}
		else{
			throw new NoSuchElementException();
		}
	}

	private void resetChainIDIterator(){
		chain_index = chain.size();
		Log.info("SAFSMAPS chain length: "+ chain_index);
	}

	private SAFSAppMapFile getAppMapFile(STAFResult result, String id){
		SAFSAppMapFile afile = (SAFSAppMapFile) appmaps.get( id.trim().toLowerCase() );
		if (afile == null){
			result.rc = STAFResult.DoesNotExist;
			result.result = id;
		}
		return afile;
	}

	/* @author Carl Nagle JUN 20, 2007 Fixed appmap to store maps not in a product app map chain. */
	private SAFSAppMapFile getAppMapFile(STAFResult result, String id, String machine, String process, int handle, int mode){
		SAFSAppMapFile afile = getAppMapFile(result, id);
		if (afile == null){
			Log.info("SAFSMAPS attempting to locate App Map '"+ id +"'");
			File file = findFile(id);
			try{
				if(file.isFile()){
					afile = new SAFSAppMapFile ( machine, process, handle, id, file, mode);
					if (honor_chain) {
						addChainID( id, afile);
					}else{
						appmaps.put(id.trim().toLowerCase(), afile);
					}
					result.rc = STAFResult.Ok;
					result.result = empty;
				}
			}catch(Exception e) {
				Log.info("SAFSMAPS error locating App Map '"+ id +"'");
				result.rc = STAFResult.DoesNotExist;
				result.result = id;
			}
		}
		return afile;
	}

	private final File findFile(String filename) {

		// try unmodified filename first
		String temp = filename;
		File file = new CaseInsensitiveFile(temp).toFile();
		try{
			// if fails try adding any default extension
			if ((!file.isFile())&&(file_ext_available)){

				temp += defaultext;
				file = null;
				file = new CaseInsensitiveFile(temp).toFile();
			}

			// if fails try adding any default directory path
			if ((!file.isFile())&&(root_path_available)){

				if (filename.startsWith(File.separator)){
					temp = defaultdir + filename.substring(2);
				}else{
					temp = defaultdir + filename;
				}

				file = null;
				file = new CaseInsensitiveFile(temp).toFile();
			}

			// last chance, add any extension along with default directory path
			if ((!file.isFile())&&(root_path_available)&&(file_ext_available)){

				temp += defaultext;
				file = null;
				file = new CaseInsensitiveFile(temp).toFile();
			}

		}catch(SecurityException e) {

			return null;
		}
		return file;
	}

	/**
	 * process the result.result for "_DDV:" text to see if we need to do a wholesale
	 * DDVariable substitution\lookup
	 *
	 * @param result		STAFResult, result.result contains the string to be analyzed
	 * @param map_file		SAFSAppMapFile, from the map file to get value
	 * @param map_section	String, from the section in a map to get value
	 * @param map_item		String, the item to get from map file
	 * @param stopMapVarLoop boolean, if we should stop the infinite loop
	 * @param processedItems	List, the original map item, which contain the result.result to be resolved.
	 *                      If the one of variables in result.result equals to originalItem stop the loop.
	 * @return				STAFResult, the resolved value
	 *
	 * @see #doAcceptRequest(String, String, int, String)
	 */
	private STAFResult process_DDV(STAFResult result, SAFSAppMapFile map_file, String map_section, String map_item, boolean stopMapVarLoop, List<String> porcessedItems){
		String temp = result.result.trim();

		int ddv = temp.indexOf(SAM_DDV_PREFIX);

		// if is a _DDV
		if ((ddv == 0)||(ddv == 1)){

			//remove any ending quote mark
			if (temp.endsWith(q)) temp = temp.substring(0, temp.length()-1);

			//Suppose we have map_item=_DDV: ,so the variable name is map_item
			String var = map_item;

			//Maybe there is a variable name after _DDV: as map_item=_DDV:variableName
			//Try to get the variable name after _DDV:
			if ((ddv + SAM_DDV_PREFIX_LEN) < (temp.length())){
				// there might be a second variable name
				temp = temp.substring( ddv+SAM_DDV_PREFIX_LEN).trim();

				// a second variable name _DDV:varname avoids recursion
				//map_item=_DDV:map_item, if the varname equals to map_item, there might be recursion
				if(temp.length() > 0) var = temp;
			}

			// add _DDV: prefix to item to prevent infinite recursion
			// we only have to worry about that if we are looking up
			// a ddv in this same section (if its default)
			if( (map_section.equalsIgnoreCase(map_file.getDefaultSection()))||
					(map_section.equalsIgnoreCase(SAM_SERVICE_REQUEST_DEFAULTMAPSECTION)) ){
				Log.info("SAFSMAPS: processedItems='"+porcessedItems+"'; var='"+var+"'");
				if(stopMapVarLoop){
					//Log.debug("SAFSMAPS: compare the variable with item in porcessedItems.");
					stopMapVarLoop = false;
					for(int i=0;i<porcessedItems.size();i++){
						if(porcessedItems.get(i).equalsIgnoreCase(var)){
							stopMapVarLoop = true;
							break;
						}
					}
				}

				if(stopMapVarLoop){
					Log.info("SAFSMAPS: Map-Var-Inifinite-Loop detected.");
					var = SAM_DDV_PREFIX + map_item;
				}else{
					//Log.info("SAFSMAPS: No Map-Var-Inifinite-Loop detected.");
				}
			}

			String loopParameter = StringUtils.getDelimitedString(porcessedItems, SAM_SERVICE_PARM_MAP_VAR_LOOP_SEP);
			String varCommand = "GET "+q+ var +q+" "+AbstractSAFSVariableService.SVS_SERVICE_PARM_MAP_VAR_LOOP+" "+ q +loopParameter +q;
			Log.info("SAFSMAPS processing SAFSVARS command: "+ varCommand);

			//Call GET command of variable service with option MAPVARLOOP
			// TODO: if STAFHandle is not an EmbeddedHandle then it will not seek embedded services via EmbeddedHandles class.
			// Thus, it will not see SAFSVARS if it is running Embedded.
			result = client.submit2("local", servicevars, varCommand);

			if (result.rc==STAFResult.UnknownService)
				result.result = AbstractSAFSVariableService.SVS_SERVICE_PROCESS_NAME +c+ servicevars;

			if (result.rc == STAFResult.VariableDoesNotExist){
				result.result = SAM_SERVICE_PARM_SECTION +c+ map_section +c+ SAM_SERVICE_PARM_ITEM +c+ map_item;
				result.rc = STAFResult.DoesNotExist;
			}
		}
		return result;
	}

	/**
	 * process the result.result for "{^varName}" text to see if we need to do a
	 * concatenation with an embedded DDVariable reference.
	 *
	 * @param result		STAFResult, result.result contains the string to be analyzed
	 * @param map_file		SAFSAppMapFile, from the map file to get value
	 * @param map_section	String, from the section in a map to get value
	 * @param stopMapVarLoop boolean, if we may try to stop the infinite loop
	 * @param processedItems	List, the original map item, which contain the result.result to be resolved.
	 *                      If the one of variables in result.result equals to originalItem stop the loop.
	 *
	 * @return				STAFResult, the resolved value
	 *
	 * @see #doAcceptRequest(String, String, int, String)
	 */
	private STAFResult process_Resolve(STAFResult result, SAFSAppMapFile map_file, String map_section, boolean stopMapVarLoop, List<String> porcessedItems){
		String temp = result.result;
		Log.info("SAFSMAPS checking for embedded DDVariables.");
		int embed_start = temp.indexOf(SAM_RESOLVE_PREFIX);
        if (embed_start < 0) {
			Log.info("SAFSMAPS found no embedded DDVariables prefix.");
        	return result;
        }

        int embed_varname = embed_start + SAM_RESOLVE_PREFIX.length();
		int embed_end = temp.indexOf(SAM_RESOLVE_SUFFIX, embed_varname);
		if (embed_end < 0) {
			Log.info("SAFSMAPS found no embedded DDVariables suffix.");
			return result;
	    }
		String varname = temp.substring(embed_varname, embed_end);

        // return unmodified if 0 length or contains spaces (for backward compatibility)
        if (varname.length()==0) return result;
        if (varname.indexOf(" ")>=0) return result;

		Log.info("SAFSMAPS found embedded DDVariable varname: "+ varname);

		// default safsvars lookup string
		String vars_name = varname;
		boolean safeguard = false;

		// add _DDV: prefix to varname to prevent infinite recursion
		// we only have to worry about that if we are looking up
		// a ddv in this same section (if its default)
		if( (map_section.equalsIgnoreCase(map_file.getDefaultSection()))||
			(map_section.equalsIgnoreCase(SAM_SERVICE_REQUEST_DEFAULTMAPSECTION)) ){
			Log.info("SAFSMAPS: processedItems='"+porcessedItems+"'; vars_name='"+vars_name+"'");
			if(stopMapVarLoop){
				//Log.debug("SAFSMAPS: compare the variable with item in porcessedItems.");
				stopMapVarLoop = false;
				for(int i=0;i<porcessedItems.size();i++){
					if(porcessedItems.get(i).equalsIgnoreCase(vars_name)){
						stopMapVarLoop = true;
						break;
					}
				}
			}

			if(stopMapVarLoop){
				Log.info("SAFSMAPS: DefaultMapSection recursion safeguard enacted for SAFSVARS...");
				vars_name = SAM_DDV_PREFIX + varname;
				safeguard = true;
			}else{
				//Log.info("SAFSMAPS: No Map-Var-Inifinite-Loop detected.");
			}
		}

		Log.info("SAFSMAPS processing DDVariable varname: "+ vars_name);
		String loopParameter = StringUtils.getDelimitedString(porcessedItems, SAM_SERVICE_PARM_MAP_VAR_LOOP_SEP);
		String varCommand = "GET "+q+ vars_name +q+" "+AbstractSAFSVariableService.SVS_SERVICE_PARM_MAP_VAR_LOOP+" "+ q+ loopParameter +q;
		Log.info("SAFSMAPS processing SAFSVARS command: "+ varCommand);

		// we should have a DDVariable name
		//Call GET command of variable service with option MAPVARLOOP
		// TODO: if STAFHandle is not an EmbeddedHandle then it will not seek embedded services via EmbeddedHandles class.
		// Thus, it will not see SAFSVARS if it is running Embedded.
		STAFResult varresult = client.submit2("local", servicevars, varCommand);

		if (varresult.rc==STAFResult.UnknownService){
			Log.info("SAFSMAPS: Unknown variables service: "+ servicevars);
			result = varresult;
			return result;
		}
		Log.info("SAFSMAPS: safsvars lookup result: "+varresult.rc +":"+ varresult.result);

		if ((varresult.rc == STAFResult.VariableDoesNotExist)||
			(varresult.rc == STAFResult.DoesNotExist)){
			Log.info("SAFSMAPS: safsvars service variable did not exist: "+ varname);
			if (!safeguard){
				result.result = varname;
				result.rc = STAFResult.DoesNotExist;
				return result;
			}
			else{
				Log.info("SAFSMAPS: recursion safeguard local lookup proceeding for: "+ varname);
				varresult.result = map_file.getAppMapItem(SAM_SERVICE_REQUEST_DEFAULTMAPSECTION, varname);
				Log.info("SAFSMAPS: local lookup for '"+ SAM_SERVICE_REQUEST_DEFAULTMAPSECTION +"::"+ varname +"':"+ varresult.result);
				//make sure we aren't recursively looking up the same thing
				boolean recursive = temp.equalsIgnoreCase(varresult.result);
				if(!recursive){
					//check if we have processed the varname
					for(int i=0;i<porcessedItems.size();i++){
						if(porcessedItems.get(i).equalsIgnoreCase(varname)){
							recursive = true;
							break;
						}
					}
				}

				if(recursive){
					Log.debug("SAFSMAPS: possible recursion problem: processedItems="+ porcessedItems+"; varname="+varname);
					result.result = varname;
					result.rc = STAFResult.UnResolvableString;
					return result;
				}
			}
		}

		// it worked.  now put it all together
		result.result = temp.substring(0, embed_start) + varresult.result;
		if (temp.length() > embed_end+1)
		   result.result = result.result + temp.substring(embed_end+1);

		Log.info("SAFSMAPS recursing for embedded variables with: "+ result.result);
		//Maybe we should not put the varname in porcessedItems???
		//porcessedItems should only contains the value at the left side of equal sign, leftValue=rightValue
		//porcessedItems=[leftValue1, leftValue2]
		//For example:
		//we have "Item= {^A} and {^A} and {^A}" in a map
		//if we try to resolve "{^A} and {^A} and {^A}", we resolve the first A, and put it in porcessedItems,
		//and call process_Resolve() again, when try to resolve the second A, we will consider it as
		//a recursion, BUT this is not a recursion.
//		porcessedItems.add(varname);
		return process_Resolve(result, map_file, map_section, stopMapVarLoop, porcessedItems);

	}

	// common acceptRequest phase for STAFServiceInterfaceLevel1 and STAFServiceInterfaceLevel30
	/**********************************************************************
	 * Handle service request from STAF
	 **********************************************************************/
	protected STAFResult doAcceptRequest(String machine, String process, int handle, String request) {

		request_get_item = false;

		Log.info("SAFSMAPS accepting request: "+ request);

		STAFCommandParseResult parsedData = parser.parse(request);
		STAFResult result = new STAFResult(parsedData.rc, new String());

		if (result.rc != STAFResult.Ok){
			result.result = new String(String.valueOf(parsedData.rc) +c+ parsedData.errorBuffer);
			Log.debug("SAFSMAPS detected problem parsing request. Error: "+result.rc +"="+result.result);
			return result;
		}

		String id = null;

		// ===============================================================
		if ( parsedData.optionTimes(SAM_SERVICE_REQUEST_HELP) > 0){
			result.result = getHELPInfo();
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_OPTION_SAFSVARS) > 0){

			id = parsedData.optionValue(SAM_SERVICE_OPTION_SAFSVARS);
			if (! id.equals(empty)) servicevars = id;
			result.result = servicevars;
			return result;

		// ===============================================================
		}else if ( parsedData.optionTimes(SAM_SERVICE_REQUEST_HANDLEID) > 0){
			result.result = String.valueOf(client.getHandle()).trim();
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_QUERY) > 0){

			id = getAppMapID( result, parsedData, SAM_SERVICE_REQUEST_QUERY );
			if (result.rc != STAFResult.Ok) return result;

			SAFSAppMapFile textfile = getAppMapFile(result, id, machine, process, handle, SAFSAppMapFile.SAM_MEMORY_MODE_STORED);
			if (result.rc != STAFResult.Ok) return result;

			if (parsedData.optionTimes(SAM_SERVICE_PARM_FILENAME) > 0) {
				result.result = textfile.getFilename();
				return result;

			}else if (parsedData.optionTimes(SAM_SERVICE_PARM_FULLPATH) > 0) {
				result.result = textfile.getFullpath();
				return result;

			}else if (parsedData.optionTimes(SAM_SERVICE_PARM_MODE) > 0) {
				int mode = textfile.getMode();
				switch(mode){
					case SAFSAppMapFile.SAM_MEMORY_MODE_STORED:
						result.result = SAM_SERVICE_PARM_STORED;
						break;
					case SAFSAppMapFile.SAM_MEMORY_MODE_MAPPED:
						result.result = SAM_SERVICE_PARM_MAPPED;
						break;
				}
				return result;

			}else if (parsedData.optionTimes(SAM_SERVICE_PARM_ITEMS)  > 0) {
				String section = parsedData.optionValue(SAM_SERVICE_PARM_ITEMS);
				result.result = SAM_SERVICE_PARM_SECTION +c+ section +c+ SAM_SERVICE_PARM_ITEMS +c;
				Vector items = textfile.getItems(section);
				if (items == null){
					result.rc = STAFResult.DoesNotExist;
					result.result = section;
					return result;
				}

				for(Enumeration e = items.elements(); e.hasMoreElements();){
					result.result += (String) e.nextElement() +c;}
				items.clear();
				items = null;
				return result;

			}else if (parsedData.optionTimes(SAM_SERVICE_REQUEST_DEFAULTMAPSECTION) > 0) {
				result.result = textfile.getDefaultSection();
				return result;

			}else if (parsedData.optionTimes(SAM_SERVICE_PARM_SECTIONS) > 0) {
				Vector sections = textfile.getSections();
				result.result = SAM_SERVICE_PARM_SECTIONS +c;
				for(Enumeration e = sections.elements(); e.hasMoreElements();){
					result.result += (String) e.nextElement() +c;
				}
				sections.clear();
				sections = null;
				return result;
			}

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_LIST) > 0){

			result.result = servicename +c+ SAM_SERVICE_REQUEST_LIST +c+ String.valueOf(appmaps.size()).trim() +r;
			int lindex = chain.size();
			while (lindex > 0){
				id = (String) chain.get(--lindex);
				SAFSAppMapFile textfile = (SAFSAppMapFile) appmaps.get(id.trim().toLowerCase());
				try{
					result.result += id +c+ textfile.getFullpath() +r; }
				catch(NullPointerException np){
					result.result += id +c+ textfile +r;
				}
			}
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_CLOSE) > 0){

			id = getAppMapID( result, parsedData, SAM_SERVICE_REQUEST_CLOSE );
			if (result.rc != STAFResult.Ok) return result;

			SAFSAppMapFile textfile = getAppMapFile(result, id);
			if (result.rc != STAFResult.Ok) return result;

			textfile.close();

			result.result = new String();
			removeChainID(id);
			result.result = SAM_SERVICE_REQUEST_CLOSE +c+ id;
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_OPEN) > 0){

			id = parsedData.optionValue(SAM_SERVICE_REQUEST_OPEN);

			String filename = parsedData.optionValue(SAM_SERVICE_PARM_FILE);

			// try unmodified filename first
			Log.info("SAFSMAPS OPEN seeking App Map '"+ filename +"'");
			File file = findFile(filename);

			try{
				if(!file.isFile()){

					Log.error("SAFSMAPS '"+ filename +"' is NOT a valid file!");
					result.rc = STAFResult.DoesNotExist;
					result.result = filename;
					return result;
				}
			}catch(Exception e) {
				Log.error("SAFSMAPS '"+ filename +"' ACCESS DENIED!");
				result.rc = STAFResult.AccessDenied;
				result.result = filename;
				return result;
			}

			// start building the response
			result.result = SAM_SERVICE_REQUEST_OPEN +c+ id +c;

			int mode = SAFSAppMapFile.SAM_MEMORY_MODE_STORED;
			//if( parsedData.optionTimes(SAM_SERVICE_PARM_MAPPED) > 0) {;}

			result.result += SAM_SERVICE_PARM_STORED +c;

			if( parsedData.optionTimes(SAM_SERVICE_REQUEST_DEFAULTMAP) > 0)
			{
				defaultmap = id;
				result.result += SAM_SERVICE_REQUEST_DEFAULTMAP +c;

			}else if (defaultmap.equals(empty)) {

				defaultmap = id;
				result.result += SAM_SERVICE_REQUEST_DEFAULTMAP +c;
			}

			result.result += file.getPath();

			SAFSAppMapFile textfile = new SAFSAppMapFile ( machine, process, handle, id, file, mode);

			addChainID(id, textfile);
			result.rc = 0;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_GETITEM) > 0) {

			id = getAppMapID( result, parsedData, SAM_SERVICE_REQUEST_GETITEM );
			if (result.rc != STAFResult.Ok) {
				Log.debug("SAFSMAPS detected problem in retrieving Default APPMAP ID for lookups. Result: "+ result.rc+"="+ result.result);
				return result;
			}

			// need to handle the case where GETITEM is called on a map
			// that has never been OPENed or is not the default.
			request_get_item = true;

			honor_chain = chain_enabled;
			if (! defaultmap.equalsIgnoreCase(id)) honor_chain = false;

			resetChainIDIterator();
			String tempid = id;
			boolean looking = true;
			SAFSAppMapFile textfile = null;

			String section = new String();
			String item = new String();

			if ( parsedData.optionTimes(SAM_SERVICE_PARM_SECTION) > 0) {
				section = parsedData.optionValue(SAM_SERVICE_PARM_SECTION);
			}
			if ( parsedData.optionTimes(SAM_SERVICE_PARM_ITEM) > 0) {
				item = parsedData.optionValue(SAM_SERVICE_PARM_ITEM);
			}else{
				result.rc = STAFResult.InvalidRequestString;
				result.result = SAM_SERVICE_PARM_ITEM;
				return result;
			}

			while(looking){
				if (honor_chain) {
					try{ tempid = getNextChainID();	}
					catch(NoSuchElementException ex){
						result.result = SAM_SERVICE_PARM_SECTION +c+ section +c+ SAM_SERVICE_PARM_ITEM +c+ item;
						result.rc = STAFResult.DoesNotExist;
						Log.debug("SAFSMAPS "+ result.result +" does not exist in storage.");
					}
				}
				Log.info("SAFSMAPS GETITEM seeking AppMap: "+ tempid);
				textfile = getAppMapFile(result, tempid, machine, process, handle, SAFSAppMapFile.SAM_MEMORY_MODE_STORED);
				if (result.rc != STAFResult.Ok) { return result; }

				result.result = textfile.getAppMapItem(section, item);
                Log.info(section +":"+ item +" resolved to: "+ result.result);

				//try default section
				if ((result.result == null)&&
					(! section.equalsIgnoreCase(SAM_SERVICE_REQUEST_DEFAULTMAPSECTION))){
					result.result = textfile.getAppMapItem(SAM_SERVICE_REQUEST_DEFAULTMAPSECTION, item);
	                Log.info(SAM_SERVICE_REQUEST_DEFAULTMAPSECTION +":"+ item +" resolved to: "+ result.result);
				}

				//try empty section
				if ((result.result == null)&&
					(! section.equals(empty))){
					result.result = textfile.getAppMapItem(empty, item);
	                Log.info("<EMPTY>:"+ item +" resolved to: "+ result.result);
				}

				// finally fail if necessary
				if (result.result == null){
					looking = honor_chain;
				}else{
					looking = false;
				}
			}

			// finally fail if necessary
			if (result.result == null){
				result.result = SAM_SERVICE_PARM_SECTION +c+ section +c+ SAM_SERVICE_PARM_ITEM +c+ item;
				result.rc = STAFResult.DoesNotExist;
				Log.info("SAFSMAPS GETITEM could not find: "+ result.result);
				return result;
			}

			String pre_result = result.result;  // for ISDYNAMIC processing below

			//If GETITEM has option MAPVARLOOP, we may need to stop the infinite loop between variable and map service
			boolean stopMapVarLoop = parsedData.optionTimes(SAM_SERVICE_PARM_MAP_VAR_LOOP) > 0;
			List<String> processedItems = new ArrayList<String>();
			if(stopMapVarLoop){
				String loopParam = parsedData.optionValue(SAM_SERVICE_PARM_MAP_VAR_LOOP);
				processedItems = StringUtils.getTokenList(loopParam, SAM_SERVICE_PARM_MAP_VAR_LOOP_SEP);
			}
			processedItems.add(item);

			// handle _DDV: ddvariables, if possible
			if (result.rc == STAFResult.Ok) result = process_DDV(result, textfile, section, item, stopMapVarLoop, processedItems);

			// handle _DDV: ddvariables, if possible
			if ((result.rc == STAFResult.Ok)&& resolve_enabled) result = process_Resolve(result, textfile, section, stopMapVarLoop, processedItems);

			// only process ISDYNAMIC if all is OK
			if (result.rc == STAFResult.Ok){
				boolean isDynamic = parsedData.optionTimes(SAM_SERVICE_PARM_ISDYNAMIC) > 0;
				Log.info("SAFSMAPS GETITEM ISDYNAMIC requested? "+ isDynamic);
				if(isDynamic)
					if((!pre_result.equals(result.result))||(result.result.equalsIgnoreCase(SAM_CURRENTWINDOW_ITEM))){
						Log.info("SAFSMAPS GETITEM '"+ section+"::"+ item +"' *IS* dynamic.");
						result.result = SAM_SERVICE_PARM_ISDYNAMIC + SAM_SERVICE_TAGGED_PREFIX + result.result;
					}else{
						Log.info("SAFSMAPS GETITEM '"+ section+"::"+ item +"' *IS NOT* dynamic.");
					}
			}
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_CLEARCACHE) > 0) {

			for(Enumeration p = appmaps.elements(); p.hasMoreElements(); ){
				SAFSAppMapFile textfile = (SAFSAppMapFile) p.nextElement();
			    result.result += textfile.getFileID() +";";
				textfile.clearCache();
			}
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_DEFAULTMAP) > 0) {

			id = parsedData.optionValue(SAM_SERVICE_REQUEST_DEFAULTMAP);
			if(! id.equals(empty)) {
				defaultmap = id;
				String lcid = id.trim().toLowerCase();
				if (chain.contains(lcid))chain.remove(lcid);
				chain.addLast(lcid);
			}
			result.result = defaultmap;
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_DEFAULTMAPSECTION) > 0) {

			id = getAppMapID( result, parsedData, SAM_SERVICE_REQUEST_DEFAULTMAPSECTION );
			if (result.rc != STAFResult.Ok) return result;

			SAFSAppMapFile textfile = getAppMapFile(result, id, machine, process, handle, SAFSAppMapFile.SAM_MEMORY_MODE_STORED);
			if (result.rc != STAFResult.Ok) return result;

			String section = null;

			// set a value
			if ( parsedData.optionTimes(SAM_SERVICE_PARM_SECTION) > 0) {

				section = parsedData.optionValue(SAM_SERVICE_PARM_SECTION);
				result.rc = textfile.setDefaultSection(section);
				result.result = section;
			// get the value
			}else{
				result.result = textfile.getDefaultSection();
			}
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_ENABLECHAIN) > 0) {

			chain_enabled = true;
			honor_chain = true;
			result.result = SAM_SERVICE_REQUEST_ENABLECHAIN;
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_DISABLECHAIN) > 0) {

			chain_enabled = false;
			honor_chain = false;
			result.result = SAM_SERVICE_REQUEST_DISABLECHAIN;
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_DISABLERESOLVE) > 0) {

			resolve_enabled = false;
			result.result = SAM_SERVICE_REQUEST_DISABLERESOLVE;
			return result;

		// ===============================================================
		}else if( parsedData.optionTimes(SAM_SERVICE_REQUEST_ENABLERESOLVE) > 0) {

			resolve_enabled = true;
			result.result = SAM_SERVICE_REQUEST_ENABLERESOLVE;
			return result;

		// ===============================================================
		}else{
			result.rc = STAFResult.InvalidRequestString;
			result.result = request;
		}

		return result;
	}


	// common term phase for STAFServiceInterfaceLevel1 and STAFServiceInterfaceLevel30
	/**********************************************************************
	 * 	Handle the request to shutdown the service from STAF
	 **********************************************************************/
	protected int doTerm(){

		if (!appmaps.isEmpty()){

			for (Enumeration e = appmaps.elements(); e.hasMoreElements();){
				SAFSAppMapFile openfile = (SAFSAppMapFile) e.nextElement();
				openfile.close();
			}
			appmaps.clear();
		}
		appmaps = null;
		return 0;
	}

}

