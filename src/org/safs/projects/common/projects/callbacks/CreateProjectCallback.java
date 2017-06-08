package org.safs.projects.common.projects.callbacks;

import org.safs.projects.common.projects.pojo.POJOProject;

/**
 * This callback is expected to be used by projects such as SeleniumPlus.
 * The idea is that SeleniumPlus code will create this callback.
 * Then, at a later time, Core code will call createProject.
 * The SeleniumPlus code that implements createProjects will call the Eclipse API to
 * create the project.
 *
 */
public abstract class CreateProjectCallback {
	public abstract POJOProject createProject();
}
