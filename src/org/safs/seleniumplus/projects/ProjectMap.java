package org.safs.seleniumplus.projects;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
