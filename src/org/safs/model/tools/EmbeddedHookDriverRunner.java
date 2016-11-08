/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
package org.safs.model.tools;
/**
 * Logs for developers, not published to API DOC.
 * History:<br>
 * 
 * JUN 23,2015     (SBJLWA) Modified autorun(): to get autorun.classname from arguments.
 *                                              Use StringUtils.getCallerClassName() to replace the deprecated sun.reflect.Reflection.getCallerClass().
 * SEP 21,2016     (SBJLWA) Modified command(): increment the "general counter" instead of "test counter".
 */
import org.safs.tools.drivers.EmbeddedHookDriver;

/**
 * This Runner is an access point to a minimalist EmbeddedHookDriver Driver API.
 * See the EmbeddedHookDriver reference for a list of known available subclasses.
 * @author canagl
 * @see EmbeddedHookDriver
 */
public class EmbeddedHookDriverRunner extends DefaultRunner{

	/** A convenient EmbeddedHookDriverDriver reference, which is also kept in the protected field {@link DefaultRunner#driver}. */
	private static EmbeddedHookDriverDriver embeddedHookDriver;
	
	/* hidden constructor */
	@SuppressWarnings("unused")
	private EmbeddedHookDriverRunner(){}
	
	/**
	 * Create the Runner that instantiates the particular EmbeddedHookDriver subclass 
	 * pass in to the Constructor. 
	 */
	public EmbeddedHookDriverRunner(Class<?> clazz){
		super();
		if(driver == null){
			try{				
				embeddedHookDriver = new EmbeddedHookDriverDriver(clazz);
				driver = embeddedHookDriver;
			}catch(Exception x){
				x.printStackTrace();
				throw new Error("Cannot instantiate required Drivers!");
			}
		}
	}

	/** retrieve access to the minimalist Driver API, if needed. */
	public EmbeddedHookDriverDriver driver(){ return embeddedHookDriver;}

	/** retrieve access to the wrapped EmbeddedHookDriver API, if needed. */
	public EmbeddedHookDriver hookDriver(){ return EmbeddedHookDriverDriver.driver;}
	
	/**
	 * The user should make sure the embedded Driver is shutdown to close any external assets 
	 * after execution is complete.
	 * @see EmbeddedHookDriverDriver#shutdown()
	 * @deprecated call {@link #terminate()} instead.
	 */
	@Deprecated
	public static void shutdown(){
		try{ embeddedHookDriver.shutdown(); }
		catch(Exception x){
			debug("Hook Shutdown ignoring "+ x.getClass().getSimpleName()+" "+ x.getMessage());
		}
	}
}
