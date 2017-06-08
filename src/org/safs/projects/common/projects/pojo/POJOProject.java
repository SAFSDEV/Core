package org.safs.projects.common.projects.pojo;

import java.io.File;
import java.util.Map;

import org.safs.projects.common.projects.pojo.POJOPackageFragment;
import org.safs.projects.common.projects.ProjectMap;

/**
 * This is a POJO implementation if a Project similar to the Eclipse IProject.
 * This implementation will be used by a project that does not use
 * something like Eclipse.
 * Those projects should call the init() method.
 * 
 * For projects that use something like Eclipse, they will likely use
 * a subclass that will hold an Eclipse IProject and will delegate calls to it.
 * They are not required to call the init() method.
 *
 */
public class POJOProject {
	/*
	 * This projectMap is expected to remain null for projects that do not use
	 * something like Eclipse.
	 * Those projects will use a subclass, and they will not use the implementation
	 * in this class.
	 * 
	 * For projects that do not use Eclipse, they will use this implementation.
	 * They should call the init method. 
	 */
	private static ProjectMap projectMap = null;

	private String name;

	/**
	 * Called to supply the directory of the workspace root.
	 * @param workspaceDir
	 */
	public static void init(File workspaceDir) {
		projectMap = new ProjectMap(workspaceDir);
	}

	public POJOProject(String projectName) {
		this.name = projectName;
	}
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the location of this project as a POJOPath.
	 * 
	 * Subclasses are expected to overwrite this method and call a delegate's
	 * getLocation method (such as Eclipse's IProject).
	 * 
	 * @return the POJOPath of the location of the project.
	 */
	public POJOPath getLocation() {
		String projectDirStr = getProjectDir().getAbsolutePath();
		projectDirStr = projectDirStr.replaceAll("\\\\", "/");
		return new POJOPath(projectDirStr);
	}

	/**
	 * Gets the location of a path relative to the root of this project.
	 * 
	 * Subclasses are expected to overwrite this method and call a delegate's
	 * getFolder method (such as Eclipse's IProject).
	 * 
	 * @param path relative path under this project's root.
	 * @return the POJOFolder corresponding to the input path.
	 */
	public POJOFolder getFolder(String path) {
		File projectDir = getProjectDir();
		File folderDir = new File(projectDir, path);
		return new POJOFolder(folderDir);
	}
	
	/**
	 * Returns the "bin" directory at the root of this project.
	 * @return
	 */
	public File getBinDir() {
		Map<String, Object> projectInfo = getProjectInfo();
		File binDir = (File) projectInfo.get("binDir");
		return binDir;
	}

	/**
	 * Should only be used for the SAMPLE project at this moment.
	 * Returns the path to the source directory.
	 * @return
	 */
	public String getSrcDir() {
		String srcDir = "";

		// Currently, this only runs for the SAMPLE project, so only SRC_TEST_DIR will exist.
		if (getFolder(org.safs.projects.seleniumplus.projects.BaseProject.SRC_SRC_DIR).exists()){
			//TODO: uncomment the next line when a test is created to exercise it.
			//srcDir = "/"+ org.safs.seleniumplus.projects.BaseProject.SRC_SRC_DIR +"/";
			throw new RuntimeException("Unsupported at this time.");
		} else if (getFolder(org.safs.projects.seleniumplus.projects.BaseProject.SRC_TEST_DIR).exists()){
			srcDir = "/"+ org.safs.projects.seleniumplus.projects.BaseProject.SRC_TEST_DIR +"/";
		} else {
			//TODO: uncomment the next line when a test is created to exercise it.
			//srcDir = "/"+ org.safs.seleniumplus.projects.BaseProject.SRC_TEST_DIR +"/";
			throw new RuntimeException("Unsupported at this time.");
		}
		return srcDir;
	}

	/**
	 * Get package fragments.
	 * 
	 * The implementation of the POJOProject super class will return an array
	 * of size one with a POJOPackageFragment that corresponds to the directory name
	 * of the first directory listed under the source directory.  It is guaranteed
	 * to only work with the SAMPLE project for now.
	 * 
	 * Subclasses are expected to overwrite this method and call a delegate's
	 * getPackageFragments method (such as Eclipse's IProject).
	 * 
	 * @return
	 * @throws Exception
	 */
	public POJOPackageFragment[] getPackageFragments() throws Exception {
		// TODO: make this work more generally.  It only works for sure
		// with the SAMPLE project.
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

	/**
	 * Get the File which corresponds to the root of the project.
	 * @return
	 */
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
