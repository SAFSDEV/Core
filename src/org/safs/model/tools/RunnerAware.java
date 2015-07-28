/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.tools;

/**
 * Classes implementing this interface that get automatically configured by a 
 * Runner should automatically receive an instance of the Runner at runtime.
 * @author Carl Nagle
 * @see org.safs.model.annotations.AutoConfigureJSAFS
 * @see Runner#autorun(String[])
 * @see org.safs.model.annotations.Utilities#autoConfigure(String, org.safs.model.annotations.JSAFSConfiguredClassStore)
 */
public interface RunnerAware {
	public abstract void setRunner(Runner runner);
}
