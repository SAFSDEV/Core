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
package org.safs.selenium.webdriver;

import org.safs.tools.MainClass;
import org.safs.SAFSPlus;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;

/**
 * This class should not be used directly by a SAFS user.
 * It provides a bridge between Groovy scripts and SAFS so SAFS can be used in
 * a Groovy script in a "native" Groovy fashion.
 *
 */
public class GroovyBridge extends SeleniumPlus {
	/*
	 * closure has to be a static because SeleniumPlus.main() (SAFSPlus.main() actually)
	 * will create a new GroovyBridge, and will not call setClosure().
	 * So, setClosure() has to be called before main() is called.
	 */
	private static Closure<?> closure;

	private static Map<Class<?>, Map<String, Method>> classMethodsMap = new HashMap<Class<?>, Map<String, Method>>();

	public void setClosure(Closure<?> myclosure) {
		closure = myclosure;
		// call setMainClass so the main() method will create a new GroovyBridge.
		MainClass.setMainClass(this.getClass().getName());
	}

	@Override
	public void runTest() {
		// This method is called by SAFSPlus.main().
		// Set the delegate so this class will be checked for methods and properties
		// during the closure call.
		closure.setDelegate(this);
		// Call the closure to run the test.
		closure.call(this);
	}

	/**
	 * A script is expected to look something like this:
	 *
	 * <pre>
	 * {@code
	 * test {
	 *    withBrowser('http://my.site', 'MainPage') {
	 *       // perform operations while the browser is open
	 *    }
	 * }
	 * }
	 * </pre>
	 *
	 * <p>A browser is started before the operations are run and
	 * will be closed after the operations are completed.
	 *
	 * @param URL
	 * @param browserID
	 * @param closure contains the operations to be performed while the browser is open.
	 */
	public void withBrowser(Object URL, Object browserID, Closure<?> closure) {
		if (! (URL instanceof String)) {
			URL = GetVariableValue(URL.toString());
		}
		if (! (browserID instanceof String)) {
			browserID = GetVariableValue(browserID.toString());
		}
		try {
			StartWebBrowser((String) URL, (String) browserID);
			closure.call();
		} finally {
			StopWebBrowser((String) browserID);
		}
	}

	/**
	 * A script may start highlighting like this:
	 *
	 * <pre>
	 * {@code
	 * test {
	 *    withHighlight {
	 *       // perform operations while the highlighting is active
	 *    }
	 * }
	 * }
	 * </pre>
	 *
	 * <p>The highlighting is started before the operations are run and
	 * will be stopped after the operations are completed.
	 *
	 * @param closure
	 */
	public void withHighlight(Closure<?> closure) {
		//Turn on the "highlight" during debug so that the component will be circled with a red rectangle
		Highlight(true);
		try {
			closure.call();
		} finally {
			//Turn off the "highlight" for better performance after debug.
			Highlight(false);
		}
	}

	/**
	 * Used when a Groovy script has:
	 * <pre>
	 * GetVariableValue(map.Variable)
	 * </pre>
	 * @param variableName
	 * @return
	 */
	public String GetVariableValue(ComponentHolder variableName) {
		return SAFSPlus.GetVariableValue(variableName.toString());
	}

	/**
	 * Used when a Groovy script has:
	 * <pre>
	 * map.Variable
	 * </pre>
	 * @return
	 */
	public GroovyObject getMap() {
		return new ComponentHolder();
	}

	/**
	 * Called by Groovy when a getter is not found for a property.
	 * <p>If a script uses logger, component, etc., a GroovyObject that calls into
	 * the corresponding SeleniumPlus class is returned (ie. SeleniumPlus.Logger, SeleniumPlus.Component, etc.
	 * @param name the name of the property
	 * @return
	 */
	public Object propertyMissing(String name) {
		String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);
		Class<?> clazz;
		try {
			clazz = this.getClass().getClassLoader().loadClass("org.safs.selenium.webdriver.SeleniumPlus$" + capitalizedName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unknown property: " + name);
		}
		return getGroovyObjectForClass(clazz);
	}

	/**
	 * Returns a Method object for a given method name and class.
	 * @param name
	 * @param clazz
	 * @return
	 */
	private Method getMethodForClass(String name, Class<?> clazz) {
		Map<String, Method> methodMap = classMethodsMap.get(clazz);
		if (methodMap == null) {
			// not in the cache yet, so create the map and
			// add it for later use.
			methodMap = new HashMap<String, Method>();
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				methodMap.put(methodName, method);
			}
			classMethodsMap.put(clazz, methodMap);
		}
		Method method = methodMap.get(name);
		if (method == null) {
			throw new RuntimeException("Unknown method " + name);
		}
		return method;
	}
	
	private GroovyObject getGroovyObjectForClass(final Class<?> tempClass) {
		return new GroovyObjectSupport() {
			@SuppressWarnings("unused")
			public Object methodMissing(String name, Object args) {
				// Method calls are processed with this method.
				Object[] inputArgs = (Object[]) args;

				Method method = getMethodForClass(name, tempClass);

				// Get some information regarding the parameters of the method.
				Class<?>[] parameterTypes = method.getParameterTypes();
				int numParms = parameterTypes.length;
				Class<?> lastParameterType = parameterTypes[numParms-1];

				/*
				 * It is assumed methods have a list of required parameters, and
				 * optional parameters are handled with a String[] at the end.
				 */
				boolean isLastParameterTypeArray = String[].class.isAssignableFrom(lastParameterType);
				int numRequiredArgs = isLastParameterTypeArray ? numParms - 1 : numParms;

				// Allocate an array for the required arguments.  Init to null values.
				Object[] requiredArgs = new Object[numRequiredArgs];
				for (int i = 0; i < requiredArgs.length; i++) {
					requiredArgs[i] = null;
				}

				// Allocate an array for the optional arguments.
				String[] otherArgs = new String[inputArgs.length - numRequiredArgs];

				// For each input parameter, put them in either the requiredArgs or
				// the otherArgs.
				for (int i = 0; i < inputArgs.length; i++) {
					Object actualArg = inputArgs[i];
					Class<?> clazz = parameterTypes[i];

					// If the parameter takes a Component and the actualArg
					// is a ComponentHolder, convert the actualArg to a Component.
					if (org.safs.model.Component.class.isAssignableFrom(clazz) &&
					    (actualArg instanceof ComponentHolder)) {

						actualArg = ((ComponentHolder) actualArg).getComponent();
					}

					// If the parameter takes a String[] and the actualArg is not a String,
					// then convert it to a String.
					if (String[].class.isAssignableFrom(clazz) && (!(actualArg instanceof String))) {
						actualArg = actualArg.toString();
					}

					// Put the argument into either the requiredArgs or the otherArgs.
					if (i < numRequiredArgs) {
						requiredArgs[i] = actualArg;
					} else {
						otherArgs[i-numRequiredArgs] = (String) actualArg;
					}
				}
				try {
					// Call the appropriate method.
					switch (numRequiredArgs) {
					case 1:
						if (requiredArgs[0] instanceof org.safs.model.Component) {
							if (isLastParameterTypeArray) {
								return method.invoke(null, (org.safs.model.Component) requiredArgs[0], otherArgs);
							} else {
								return method.invoke(null, (org.safs.model.Component) requiredArgs[0]);
							}
						} else {
							return method.invoke(null, (String) requiredArgs[0], otherArgs);
						}
					case 2:
						if (isLastParameterTypeArray) {
							return method.invoke(null, requiredArgs[0], (String) requiredArgs[1], otherArgs);
						} else {
							return method.invoke(null, (org.safs.model.Component) requiredArgs[0], (String) requiredArgs[1]);
						}
					default:
						throw new RuntimeException("Method " + name + " with " + numParms + " parameters is unsupported at this time");
					}
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e.getCause());
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}
