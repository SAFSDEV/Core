/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.drivers;

import java.util.ListIterator;

import org.safs.tools.CoreInterface;
import org.safs.tools.PathInterface;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.vars.VarsInterface;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.status.StatusInterface;

public interface DriverInterface extends PathInterface {

	/** "SAFS_DRIVER_CONTROL" **/
	public static final String DRIVER_CONTROL_VAR = "SAFS_DRIVER_CONTROL";
	public static final String DRIVER_CONTROL_POF_VAR = "SAFS_DRIVER_CONTROL_POF";
	public static final String DRIVER_CONTROL_POW_VAR = "SAFS_DRIVER_CONTROL_POW";
	
	public ConfigureInterface getConfigureInterface();

	public InputInterface        getInputInterface();
	public MapsInterface         getMapsInterface();
	public VarsInterface         getVarsInterface();
	public LogsInterface         getLogsInterface();
	public CoreInterface         getCoreInterface();
	public CountersInterface     getCountersInterface();
	public DebugInterface        getDebugInterface();
	public FlowControlInterface  getFlowControlInterface(String testlevel);

	public ListIterator          getEngines();
	public ListIterator          getEnginePreferences();
	public EngineInterface       getPreferredEngine(String key);
	public void                 startEnginePreference(String key);
	public void                 endEnginePreference(String key);
	public boolean              hasEnginePreferences();
	public void                 clearEnginePreferences();
	public boolean              isPreferredEngine(String key);
	public boolean              isPreferredEngine(EngineInterface engine);
	
	public EngineInterface       getAutoItComponentSupport();
	public EngineInterface       getTIDDriverCommands();
	public EngineInterface       getTIDGUIlessComponentSupport();
	public EngineInterface       getIPDriverCommands();
	public StatusInterface       getStatusInterface();
	public StatusInterface       addStatusCounts(StatusInterface aStatusInterface);
	
	public String getDriverName();
		
	public String getTestName();
	public String getTestLevel();

	public String getCycleSuffix();
	public String getCycleSeparator();

	public String getSuiteSuffix();
	public String getSuiteSeparator();

	public String getStepSuffix();
	public String getStepSeparator();

	public String getLogLevel();
	
	public String getCycleLogName();
	public long   getCycleLogMode();

	public String getSuiteLogName();
	public long   getSuiteLogMode();

	public String getStepLogName();
	public long   getStepLogMode();	

	public boolean isExpressionsEnabled();	
	public void setExpressionsEnabled(boolean enabled);	
	
	public boolean isPerTableFlowControl();
	public void    setPerTableFlowControl(boolean enabled);

	public boolean isExitSuite();
	public void    setExitSuite(boolean enabled);

	public boolean isExitCycle();
	public void    setExitCycle(boolean enabled);
	
	public String getDriverRootDir();

	public String getProjectRootDir();
	public long setProjectRootDir(String absolute_path);
			
	public String getRootVerifyDir();
	public long setRootVerifyDir(String absolute_path);

	public String getDatapoolDir();
	public long setDatapoolDir(String absolute_path);
	
	public String getBenchDir();
	public long setBenchDir(String absolute_path);
	
	public String getTestDir();
	public long setTestDir(String absolute_path);
	
	public String getDifDir();
	public long setDifDir(String absolute_path);
	
	public String getLogsDir();
	public long setLogsDir(String absolute_path);
		
	public int getMillisBetweenRecords();
	public void setMillisBetweenRecords(int millisBetweenRecords);	
}

