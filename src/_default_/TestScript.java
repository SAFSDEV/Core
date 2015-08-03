
import resources.TestScriptHelper;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;
import org.safs.rational.RRobotJHook;
import org.safs.rational.logging.RLogUtilities;
import org.safs.Log;
import org.safs.STAFHelper;
import java.util.*;

public class TestScript extends TestScriptHelper
{
  public void testMain (Object[] args)
  {
    try {
    	processArgs(args);
        _hook = new RRobotJHook();
  		_logUtils = new RLogUtilities(getSTAFHelper(), this);
  		_logUtils.setCopyLogClass(true);
  		_hook.setLogUtil(_logUtils);
  		_hook.setScript(this);
  		_hook.start();
    } catch (Throwable e) {
      e.printStackTrace();
      if (e instanceof Exception){
    	  Log.debug("TestScript exiting with runtime Exception:", (Exception)e);
      }else{
    	  Log.debug("TestScript exiting with runtime Error: "+ e.getClass().getName());
      }
    }
  }
}
