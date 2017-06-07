package org.safs.seleniumplus.projects.callbacks;

import org.safs.seleniumplus.projects.pojo.POJOFolder;
import org.safs.seleniumplus.projects.pojo.POJOProject;

public abstract class GetFolderCallback {
	public abstract POJOFolder getFolder(POJOProject project, String path);
}
