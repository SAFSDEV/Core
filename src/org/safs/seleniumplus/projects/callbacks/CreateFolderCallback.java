package org.safs.seleniumplus.projects.callbacks;

import org.safs.seleniumplus.projects.pojo.POJOProject;

public abstract class CreateFolderCallback {
	public abstract void createFolder(POJOProject project, String path) throws Exception;
}
