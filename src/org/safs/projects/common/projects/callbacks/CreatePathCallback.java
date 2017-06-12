package org.safs.projects.common.projects.callbacks;

import org.safs.projects.common.projects.pojo.POJOPath;

/**
 * This callback is expected to be used by projects such as SeleniumPlus.
 * The idea is that SeleniumPlus code will create this callback.
 * Then, at a later time, Core code will call createPath.
 * The SeleniumPlus code that implements createPath will call the Eclipse API to
 * create the path.
 *
 */
public abstract class CreatePathCallback {
	public abstract POJOPath createPath(String pathStr);
}
