/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.tools;

/**
 * Classes implementing this interface that get automatically configured by an 
 * EmbeddedHookDriverRunner should automatically receive an instance of the 
 * EmbeddedHookDriverRunner at runtime.
 * @author canagl
 * @see org.safs.model.annotations.AutoConfigureJSAFS
 * @see EmbeddedHookDriverRunner#autorun(String[])
 * @see org.safs.model.annotations.Utilities#autoConfigure(String, org.safs.model.annotations.JSAFSConfiguredClassStore)
 */
public interface EmbeddedHookDriverRunnerAware {
	public abstract void setEmbeddedHookDriverRunner(EmbeddedHookDriverRunner runner);
}
