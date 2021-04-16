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
