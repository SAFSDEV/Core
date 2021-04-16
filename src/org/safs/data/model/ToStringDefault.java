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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2018-03-23    (Lei Wang) Initial release.
 * @date 2018-03-30    (Lei Wang) Added field 'filterForToStringMethod' and method getFieldNamesIgnoredByToStringMethod():
 *                                help to filter fields that will not be included the string return by method toString().
 * @date 2018-03-30    (Lei Wang) Added transient modifier to field 'filterForToStringMethod' so that it will be ignored by Gson during persistence.
 */
package org.safs.data.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class overrides the method toString() to print each fields with a separator "|" between them, such as "tomroc | tom | rocker | ".<br>
 *
 * @author Lei Wang
 */
public class ToStringDefault{
	private static final Logger log = LoggerFactory.getLogger(ToStringDefault.class);
	/**
	 * The filter used to tell us what field will be ignored by method {@link #toString()}.<br>
	 * Here we provide a default {@link FieldFilterByName}, which is instantiated with {@link #getFieldNamesIgnoredByToStringMethod()}.<br>
	 * In subclass, we can either override {@link #getFieldNamesIgnoredByToStringMethod()} or we can assign a new {@link Filter} to {@link #filterForToStringMethod}.<br>
	 *
	 * @see #getFieldNamesIgnoredByToStringMethod()
	 */
	protected transient Filter<Field> filterForToStringMethod = new FieldFilterByName(getFieldNamesIgnoredByToStringMethod());

	@Override
	public String toString(){
		List<Field> fields = filterForToStringMethod.filter(Arrays.asList(getClass().getDeclaredFields()));
		StringBuilder sb = new StringBuilder();

		for(Field field:fields) sb.append(getFieldValue(field)+" | ");

		return sb.toString();
	}

	/**
	 * Here an empty list is returned.<br>
	 * Subclass needs to provide its own list to ignore so that these fields will not be added to method {@link #toString()}.
	 * @return List<String>, a list of name for the field to be ignored by {@link #toString()}.
	 */
	protected List<String> getFieldNamesIgnoredByToStringMethod(){
		return new ArrayList<String>();
	}

	/**
	 * Get the field's value.<br>
	 * Sometimes we don't want the real value of a field when putting it in the toString() method,<br>
	 * so we firstly try to get it with getter method, which may provide a pretty string than a real value.<br>
	 * If getter method failed, then try to get directly the field's value by java reflection.<br>
	 * @param field Field
	 * @return Object, the field's value, it can be null.
	 */
	private Object getFieldValue(Field field){
		Object value = null;
//		PropertyDescriptor pd;
//		try {
//			//to new a PropertyDescriptor, you MUST have getter and setter method in the class,
//			//otherwise it will throw out java.beans.IntrospectionException: Method not found: setId
//			pd = new PropertyDescriptor (field.getName(), getClass());
//			Method getterMethod = pd.getReadMethod();
//			if(pd!=null){
//				try {
//					value = getterMethod.invoke(this);
//				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//					log.debug("Failed to get value for field '"+field.getName()+"'",e);
//					field.setAccessible(true);
//					try {
//						value = field.get(this);
//					} catch (IllegalArgumentException | IllegalAccessException e1) {
//						log.debug("Failed to get value for field '"+field.getName()+"'",e1);
//					}
//				}
//			}
//		} catch (IntrospectionException e) {
//			log.debug("Failed to get value for field '"+field.getName()+"'",e);
//		}
		try {
			//Firstly try to get it with getter method
			//PropertyUtils needs commons-beanutils.jar on the classpath, otherwise it throw out NoClassDefFoundError,
			//we catch NoClassDefFoundError and let java reflection to do the work
			value = PropertyUtils.getProperty(this, field.getName());
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | java.lang.NoClassDefFoundError e) {
			log.warn("Failed to get value for field '"+field.getName()+"'",e);
			field.setAccessible(true);
			try {
				//try to get directly the field's value by java reflection
				value = field.get(this);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				log.warn("Failed to get value for field '"+field.getName()+"'",e1);
			}
		}

		return value;
	}
}
