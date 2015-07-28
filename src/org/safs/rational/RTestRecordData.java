/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import org.safs.DDGUIUtilities;
import org.safs.Domains;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.TestRecordHelper;
import org.safs.tools.drivers.ConfigureInterface;

import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em>RTestRecordData: extends TestRecordData,
 * which holds key data used by a driver, like step driver.
 * Based on the SAFS Test Record Data doc.
 * <p>
 * While the DriverCommands processor object may handle all the different driver commands directly,
 * the ComponentFunctions processor will need to evaluate the "type" of component represented by
 * any retrieved application SpyMappedTestObject then forward this object on to the RobotJ
 * script/object that handles that component type.
 * <p>
 *
 * @author  Doug Bauman
 * @since   JUN 03, 2003
 *
 *   <br>   JUN 03, 2003    (DBauman) Original Release
 *   <br>   JUN 30, 2011    (CANAGL) Moved ConfigureInterface storage to superclass for all Java engines.
 **/
public class RTestRecordData extends TestRecordHelper {

  /** a child component's TestObject when it is appropriate. 
   *  this is getting deprecated out.                     **/
  //private SpyMappedTestObject compSpyMappedTestObject;

  /** child's TestObject when it is appropriate. **/
  private TestObject compTestObject = null;

  /** window's TestObject when it is appropriate. **/
  private TestObject windowTestObject = null;
  
  /** Hook's (Script instance (this)) when it is appropriate. **/
  private Script script = null;  

  /** <br><em>Purpose:</em> no-arg constructor to make this fully qualified javabean
   **/
  public RTestRecordData() {
    super();
  }

  public RTestRecordData(STAFHelper helper, Script script){ 
  	super(); 
  	setScript(script);
  	setSTAFHelper(helper);
  }
  
  public RTestRecordData(STAFHelper helper, Script script, DDGUIUtilities ddgutils){ 
  	super(); 
  	setScript(script);
  	setDDGUtils(ddgutils);
  	setSTAFHelper(helper);
  }

  public Script getScript(){ return script;}
  public void setScript(Script script) { this.script = script; }
  
  /** <br><em>Purpose:</em> accessor method(s)
   * We might have to UNREGISTER these TestObjects!
   **/
  public TestObject getWindowTestObject () {return windowTestObject;}
  public void setWindowTestObject (TestObject windowTestObject) {this.windowTestObject = windowTestObject;}


  /** <br><em>Purpose:</em> accessor method(s)
   * We might have to UNREGISTER these TestObjects!
   **/
  public TestObject getCompTestObject () {return compTestObject;}
  public void setCompTestObject (TestObject compTestObject) {this.compTestObject = compTestObject;}


  /** <br><em>Purpose:</em> reinit this object to be reused over again.
   * <br><em>Assumptions:</em>  fields set to null: compSpyMappedTestObject
   **/
  public void reinit () {
    super.reinit();
    setCompTestObject(null);
    setWindowTestObject(null);
  }

  public String getCompInstancePath(){
	String debugmsg = getClass().getName()+".getCompInstancePath() ";
	String packageName = "org.safs.rational.";
	  
	try {
		String componentType = this.getCompType();
		if(componentType!=null){
			componentType = componentType.toLowerCase();
			if(componentType.startsWith(Domains.WIN_DOMAIN.toLowerCase())){
				packageName += Domains.WIN_DOMAIN.toLowerCase()+".";
			}else if(componentType.startsWith(Domains.FLEX_DOMAIN.toLowerCase())){
				packageName += Domains.FLEX_DOMAIN.toLowerCase()+".";
			}else if(componentType.startsWith(Domains.NET_WPF.toLowerCase())){
				packageName += Domains.NET_WPF.toLowerCase()+".";
			}
		}
	} catch (SAFSException e) {
		Log.debug(debugmsg+" Exception occur. "+e.getMessage());
	}
	
	Log.debug(debugmsg+" return package name "+packageName);
	return packageName;
  }
  
}

