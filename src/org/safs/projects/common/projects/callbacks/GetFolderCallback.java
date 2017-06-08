package org.safs.projects.common.projects.callbacks;

import org.safs.projects.common.projects.pojo.POJOFolder;
import org.safs.projects.common.projects.pojo.POJOProject;

/**
 * This callback is expected to be used by projects such as SeleniumPlus.
 * The idea is that SeleniumPlus code will create this callback.
 * Then, at a later time, Core code will call getFolder.
 * The SeleniumPlus code that implements getFolder will call the Eclipse API to
 * get the folder.
 *
 */
public abstract class GetFolderCallback {
	public abstract POJOFolder getFolder(POJOProject project, String path);
}
