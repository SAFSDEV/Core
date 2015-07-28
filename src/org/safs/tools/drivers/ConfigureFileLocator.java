package org.safs.tools.drivers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.safs.GuiClassData;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.install.ProjectInstaller;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.MainClass;

public class ConfigureFileLocator implements ConfigureLocatorInterface {
	protected Vector paths = new Vector(6,1);
	
	/**
	 * Constructor for ConfigureLocator
	 */
	public ConfigureFileLocator() {
		super();
	}

	/**
	 * @see ConfigureLocatorInterface#locateRootDir(String)
	 */
	public File locateRootDir(String rootDir) {
		File dir = new CaseInsensitiveFile(rootDir).toFile();
		if (dir.isDirectory()) return dir;
		return null;
	}

	
	/** return a valid ConfigureInterface with the candidate path signature only once.**/
	protected ConfigureInterface uniqueConfigureInterface(File candidate)
	{
		if (paths.contains(candidate.getPath())) return null;
		paths.addElement(candidate.getPath());
		Log.info("UniqueConfigPath="+candidate.getPath());
		return (new ConfigureFile(candidate));
	}
	
	
	
	boolean projectExtracted = false;
	
	/**
	 * @see ConfigureLocatorInterface#locateConfigureInterface(String, String)
	 */
	public ConfigureInterface locateConfigureInterface(String rootDir, String configPath) {

		ConfigureFile configFile = null;
		CaseInsensitiveFile config = new CaseInsensitiveFile(configPath);
		CaseInsensitiveFile newConfig = null;
		
		Log.info("Checking ConfigFile only: "+ configPath);
		if ((config.isFile())&&(config.canRead())) return (uniqueConfigureInterface(config.toFile()));
		
		// if config is a directory, try appending the default config filename
		if (config.isDirectory()){
			newConfig = new CaseInsensitiveFile(config, DriverConstant.DEFAULT_CONFIGURE_FILENAME);
		    Log.info("Checking ConfigDir + DefaultFile: "+ configPath + File.separator + DriverConstant.DEFAULT_CONFIGURE_FILENAME);
			if ((newConfig.isFile())&&(newConfig.canRead())) return (uniqueConfigureInterface(newConfig.toFile()));
		}

		// configPath is NOT a file in the current working directory.
		// configPath is NOT an absolute path to a valid file.
		// configPath is NOT a path to a directory.
				
		// try the combination of rootDir and configPath		
		CaseInsensitiveFile dir = new CaseInsensitiveFile(rootDir);
		Log.info("Validating REQUIRED RootDir: "+ rootDir);
		if (! dir.isDirectory()) {			
			// configPath may point to configFile or configDirectory packaged in JAR file
			// attempt to perform such an extraction
			CaseInsensitiveFile file = extractConfigPath(rootDir, configPath);
			if(file == null) return null;
			projectExtracted = true;
			String p = null;
			if(file.isFile()) {
				try{ 
					p = getExtractionDirectory().getAbsolutePath(); 
				}catch(Exception x){
				    p = config.getParentFile().getAbsolutePath();
				}
				ProjectInstaller.main(new String[]{"-config", file.getAbsolutePath(),
						                           "-path", file.getParentFile().getAbsolutePath(),
						                           "-output", p});
				projectExtracted = false;
				return (uniqueConfigureInterface(file.toFile()));
			}
			if(file.isDirectory())dir = file;
		}

		config = new CaseInsensitiveFile(dir, configPath);
		Log.info("Checking RootDir with ConfigPath: "+ rootDir +File.separator+ configPath);
		if ((config.isFile())&&(config.canRead())) {
			if(projectExtracted){
				projectExtracted = false;
				String p = null;
				try{ 
					p = getExtractionDirectory().getAbsolutePath(); 
				}catch(Exception x){
				    p = config.getParentFile().getAbsolutePath();
				}
				ProjectInstaller.main(new String[]{"-config", config.getAbsolutePath(),
                                                   "-path", config.getParentFile().getAbsolutePath(),
                                                   "-output", p});
			}
			return (uniqueConfigureInterface(config.toFile()));
		}

		// try rootDir with the appended default config filename
		newConfig = new CaseInsensitiveFile(dir, DriverConstant.DEFAULT_CONFIGURE_FILENAME);
		Log.info("Checking RootDir with DefaultFile: "+ rootDir +File.separator+ DriverConstant.DEFAULT_CONFIGURE_FILENAME);
		if ((newConfig.isFile())&&(newConfig.canRead())) return (uniqueConfigureInterface(newConfig.toFile()));

		// tried everything?
		return null;		
	}
	
	File extractFile = null;
	
	/**
	 * Deduce a Temp directory or user-specified extraction directory for JAR extraction.
	 * <p>
	 * The routine will seek the System.getProperty("safs.project.extract") to extract to an 
	 * alternative directory instead of the Temp directory.
	 * <p>
	 * JVM Arg: -Dsafs.project.extract=&lt;absolute path to extraction directory>
	 * <p>
	 * @return File pointing to the desired extraction directory.
	 */
	File getExtractionDirectory() throws IOException{
		if(extractFile != null) return extractFile;
		File temp = null;
		String extractDir = System.getProperty(DriverConstant.PROPERTY_SAFS_PROJECT_EXTRACT);
		if(extractDir != null){
			temp = new CaseInsensitiveFile(extractDir).toFile();
			if(! temp.isAbsolute())
				throw new IllegalArgumentException("The provided safs.project.extract directory value is invalid: "+ extractDir);
			if(! temp.isDirectory()) temp.mkdirs();
			if(! temp.isDirectory())
				throw new IllegalArgumentException("The provided safs.project.extract directory could not be created: "+ extractDir);
			extractFile = temp;
		}else{
			temp = File.createTempFile("DeleteMe", null);
			extractFile = temp.getParentFile();
			temp.delete();
		}
		return extractFile;
	}
	
	/**
	 * Extract project and test assets to a Temp directory if embedded inside a JAR file.
	 * <p>
	 * The routine will seek the System.getProperty("safs.project.extract") to extract to an 
	 * alternative directory instead of the Temp directory.
	 * <p>
	 * All assets should be stored in rootDir that exists in JAR file.<br>
	 * the ini file (configPath) is a file in that directory.
	 * <p>
	 * @param rootDir - required name of a project root directory to seek in the main class JAR file.
	 * @param configPath -- required name of a config file to seek in the project root directory.
	 * @return CaseInsensitiveFile pointing to the newly extracted config file, or null.
	 * @see org.safs.tools.MainClass
	 */
	public CaseInsensitiveFile extractConfigPath(String rootDir, String configPath){
		String theClassname = MainClass.getMainClass();
		if (theClassname == null) return null; // retain normal functionality
		try{
			if(rootDir == null) 
				throw new IllegalArgumentException("The required rootDir parameter cannot be null!");
			Class theClass = Class.forName(theClassname);
			URL domain = theClass.getProtectionDomain().getCodeSource().getLocation();
			if(!domain.getProtocol().equalsIgnoreCase("file"))
				throw new IllegalArgumentException(theClassname +" was not found in a JAR file!");			
			String jarfile = domain.getPath();
			if(jarfile==null||jarfile.length()==0)
				throw new IllegalArgumentException(domain.toExternalForm() +" did not provide a valid Path!");				
			if(!FileUtilities.isArchive(jarfile))
				throw new IllegalArgumentException(theClassname +" was not found in a JAR file!");
			ZipFile jar = new ZipFile(jarfile);	
			ZipEntry zipDir = jar.getEntry(rootDir);
			if(zipDir == null)
				throw new IllegalArgumentException("Did not get a valid ZipEntry for rootDir "+ rootDir);				
			
			File runDir = new File(getExtractionDirectory(), rootDir);
			if(runDir.isDirectory()) FileUtilities.deleteDirectoryRecursively(runDir.getAbsolutePath(), false);
			runDir = runDir.getParentFile();
			FileUtilities.unzipJAR(jarfile, runDir, false, true);
			CaseInsensitiveFile projectdir = new CaseInsensitiveFile(runDir, rootDir);
			if(!projectdir.isDirectory())
				throw new IllegalArgumentException("The required project directory cannot be found at "+projectdir.getPath());
			System.setProperty(DriverConstant.PROPERTY_SAFS_MODIFIED_ROOT, projectdir.getAbsolutePath());
			CaseInsensitiveFile configFile = new CaseInsensitiveFile(projectdir, configPath);
			if(configFile.isFile()) {
				System.setProperty(DriverConstant.PROPERTY_SAFS_MODIFIED_CONFIG, configFile.getAbsolutePath());
				return configFile;
			}
			return projectdir;
		}
		catch(Exception x){
			String msg = "ConfigFileLocator.extractConfigPath "+ x.getClass().getName()+": "+x.getMessage();
//			System.out.println(msg);
			Log.warn(msg);
		}
		return null;
	}
}
