package org.safs.seleniumplus.projects.pojo;

import java.io.File;
import java.util.Map;

import org.safs.seleniumplus.projects.ProjectMap;
import org.safs.seleniumplus.projects.pojo.POJOFolder;
import org.safs.seleniumplus.projects.pojo.POJOPath;

public class POJOProject {
	private static ProjectMap projectMap = null;
	
	private String name;
	
	public static void init(File workspaceDir) {
		projectMap = new ProjectMap(workspaceDir);
	}

	public POJOProject(String projectName) {
		this.name = projectName;
	}
	public String getName() {
		return name;
	}
	public POJOPath getLocation() {
		String projectDirStr = getProjectDir().getAbsolutePath();
		projectDirStr = projectDirStr.replaceAll("\\\\", "/");
		return new POJOPath(projectDirStr);
	}
	public POJOFolder getFolder(String path) {
		File projectDir = getProjectDir();
		File folderDir = new File(projectDir, path);
		return new POJOFolder(folderDir);
	}
	public File getBinDir() {
		Map<String, Object> projectInfo = getProjectInfo();
		File binDir = (File) projectInfo.get("binDir");
		return binDir;
	}

	public String getSrcDir() {
		String srcDir = "";

		// Currently, this only runs for the SAMPLE project, so only SRC_TEST_DIR will exist.
		if (getFolder(org.safs.seleniumplus.projects.BaseProject.SRC_SRC_DIR).exists()){
			//TODO: uncomment the next line when a test is created to exercise it.
			//srcDir = "/"+ org.safs.seleniumplus.projects.BaseProject.SRC_SRC_DIR +"/";
			throw new RuntimeException("Unsupported at this time.");
		} else if (getFolder(org.safs.seleniumplus.projects.BaseProject.SRC_TEST_DIR).exists()){
			srcDir = "/"+ org.safs.seleniumplus.projects.BaseProject.SRC_TEST_DIR +"/";
		} else {
			//TODO: uncomment the next line when a test is created to exercise it.
			//srcDir = "/"+ org.safs.seleniumplus.projects.BaseProject.SRC_TEST_DIR +"/";
			throw new RuntimeException("Unsupported at this time.");
		}
		return srcDir;
	}

	public POJOPackageFragment[] getPackageFragments() throws Exception {
		File srcDir = new File(getProjectDir().getAbsolutePath() + getSrcDir());
		File[] files = srcDir.listFiles();
		String packageName = null;
		for (File file : files) {
			if (file.isDirectory()) {
				packageName = file.getName();
				break;
			}
		}
		return new POJOPackageFragment[] {new POJOPackageFragment(packageName)};
	}

	public File getProjectDir() {
		Map<String, Object> projectInfo = getProjectInfo();
		File projectDir = (File) projectInfo.get("projectDir");
		return projectDir;
	}
	private Map<String, Object> getProjectInfo() {
		checkProjectMap();
		Map<String, Object> projectInfo = projectMap.getProjectInfo(name);
		return projectInfo;
	}
	private void checkProjectMap() {
		if (projectMap == null) {
			throw new RuntimeException("Project Map is not initialized.");
		}
	}
}
