/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.model.tools;
/**
 * Logs for developers, not published to API DOC.
 * History:<br>
 *
 * JUN 23, 2015     (Lei Wang) Modified autorun(): to get autorun.classname from arguments.
 *                                              Use StringUtils.getCallerClassName() to replace the deprecated sun.reflect.Reflection.getCallerClass().
 * SEP 21, 2016     (Lei Wang) Modified command(): increment the "general counter" instead of "test counter".
 * SEP 25, 2018     (Lei Wang) Annotate this class as @Component("org.safs.model.tools.EmbeddedHookDriverRunner") so that it will be loaded by spring.
 *                            We need to provide a specific name "org.safs.model.tools.EmbeddedHookDriverRunner" for this annotation so that spring
 *                            will not be confused with other sub-classes of AbstractRunner.
 * SEP 27, 2018     (Lei Wang) Added @Lazy(value = true) annotation for this class so that it will be loaded properly in class SeleniumPlus.
 *                            Without this annotation the default constructor will be used to instantiate the bean and the 'driver' will not be initialized.
 *
 */

import org.safs.tools.drivers.EmbeddedHookDriver;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This Runner is an access point to a minimalist EmbeddedHookDriver Driver API.
 * See the EmbeddedHookDriver reference for a list of known available subclasses.
 * @author Carl Nagle
 * @see EmbeddedHookDriver
 */
@Component("org.safs.model.tools.EmbeddedHookDriverRunner")
@Lazy(value = true)
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
	@Override
	public EmbeddedHookDriverDriver driver(){ return embeddedHookDriver;}

	/** retrieve access to the wrapped EmbeddedHookDriver API, if needed. */
	@Override
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
