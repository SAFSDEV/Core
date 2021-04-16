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
package org.safs.projects.common.projects;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The ProjectMap contains information for each project.  There should be only one
 * per JVM.  Given a projectName, getProjectInfo(projectName) will return
 * a Map that contains strings such as projectDir, binDir, etc. that hold
 * information about the project.
 *
 */
public class ProjectMap {
	private final Map<String, Map<String, Object>> projectMap = new HashMap<String, Map<String, Object>>();
	private File workspaceDir;

	public ProjectMap(File workspaceDir) {
		this.workspaceDir = workspaceDir;
	}

	public Map<String, Object> getProjectInfo(String projectName) {
		Map<String, Object> projectInfo = projectMap.get(projectName);
		if (projectInfo == null) {
			projectInfo = new HashMap<String, Object>();

			if (workspaceDir == null) {
				throw new RuntimeException("Workspace directory was not set.");
			}
			File projectDir = new File(workspaceDir, projectName);
			projectInfo.put("projectDir", projectDir);
			projectInfo.put("binDir", new File(projectDir, "bin"));

			projectMap.put(projectName, projectInfo);
		}
		return projectInfo;
	}
}
