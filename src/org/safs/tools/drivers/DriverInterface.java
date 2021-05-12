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
package org.safs.tools.drivers;

import java.util.List;
import java.util.ListIterator;

import org.safs.tools.CoreInterface;
import org.safs.tools.PathInterface;
import org.safs.tools.Versionable;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.status.StatusInterface;
import org.safs.tools.vars.VarsInterface;

public interface DriverInterface extends PathInterface, Versionable{

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

	public ListIterator<EngineInterface>  getEngines();
	public ListIterator<EngineInterface>  getEnginePreferences();
	public EngineInterface       getPreferredEngine(String key);
	public void                 startEnginePreference(String key);
	public void                 endEnginePreference(String key);
	public boolean              hasEnginePreferences();
	public void                 clearEnginePreferences();
	public boolean              isPreferredEngine(String key);
	public boolean              isPreferredEngine(EngineInterface engine);

	public List<EngineInterface> getEmbeddedEngines();
	public void addEmbeddedEngine(EngineInterface embeddedEngine);

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

	@Override
	public String getDriverRootDir();

	@Override
	public String getProjectRootDir();
	public long setProjectRootDir(String absolute_path);

	public String getRootVerifyDir();
	public long setRootVerifyDir(String absolute_path);

	@Override
	public String getDatapoolDir();
	public long setDatapoolDir(String absolute_path);

	@Override
	public String getBenchDir();
	public long setBenchDir(String absolute_path);

	@Override
	public String getTestDir();
	public long setTestDir(String absolute_path);

	@Override
	public String getDifDir();
	public long setDifDir(String absolute_path);

	@Override
	public String getLogsDir();
	public long setLogsDir(String absolute_path);

	public int getMillisBetweenRecords();
	public void setMillisBetweenRecords(int millisBetweenRecords);

	public void incrementTestStatus(int status);
	public void incrementGeneralStatus(int status);
	public void logMessage(String msg, String desc, int msgType);
}

