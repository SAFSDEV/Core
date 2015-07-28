package org.safs.install;

public interface InstallerInterface {
	
	public boolean install(String... args);
	public boolean uninstall(String... args);
}
