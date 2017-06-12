package org.safs.projects.common.projects.callbacks;

import org.safs.projects.common.projects.pojo.POJOProject;

/**
 * This callback is expected to be used by projects such as SeleniumPlus.
 * The idea is that SeleniumPlus code will create this callback.
 * Then, at a later time, Core code will call createFolder.
 * The SeleniumPlus code that implements createFolder will call the Eclipse API to
 * create the folder.
 *
 */
public abstract class CreateFolderCallback {
	public abstract void createFolder(POJOProject project, String path) throws Exception;
}
