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
 * @date 2018-03-30    (Lei Wang) Added field 'filterForUpdateMethod' and method getFieldNamesIgnoredByUpdateMethod():
 *                                help to filter fields that will not be updated by method update().
 * @date 2018-03-30    (Lei Wang) Added transient modifier to field 'filterForUpdateMethod' so that it will be ignored by Gson during persistence.
 */
package org.safs.data.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Id;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class overrides the method toString() to print each fields with a separator "|" between them, such as "tomroc | tom | rocker | ".<br>
 * This class implements the method update(), it will update all the fields by the value from an other Updatable object, except the field annotated with "@Id".<br>
 * The model entity can extend this class to get these functionalities.<br>
 *
 * @author Lei Wang
 */
public class UpdatableDefault<T> extends ToStringDefault implements Updatable <T>{
	private static final Logger log = LoggerFactory.getLogger(ToStringDefault.class);
	/**
	 * The filter used to tell us what field will be ignored by method {@link #update(Object)}.<br>
	 * Here we provide a default {@link FieldFilterByName}, which is instantiated with {@link #getFieldNamesIgnoredByUpdateMethod()}.<br>
	 * In subclass, we can either override {@link #getFieldNamesIgnoredByUpdateMethod()} or we can assign a new {@link Filter} to {@link #filterForUpdateMethod}.<br>
	 *
	 * @see #getFieldNamesIgnoredByToStringMethod()
	 */
	protected transient Filter<Field> filterForUpdateMethod = new FieldFilterByName(getFieldNamesIgnoredByUpdateMethod());

	@Override
	public void update(T o) {
		List<Field> fields = filterForUpdateMethod.filter(Arrays.asList(getClass().getDeclaredFields()));
		Field oField = null;

		for(Field field:fields){
			try {
				//We don't update the ID field
				if(!field.isAnnotationPresent(Id.class)){
					oField = o.getClass().getDeclaredField(field.getName());
					field.setAccessible(true);
					oField.setAccessible(true);
					field.set(this, oField.get(o));
				}
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				log.debug("Failed to update field '"+field.getName()+"'",e);
				try {
					PropertyUtils.setProperty(this, field.getName(), oField.get(o));
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
						| IllegalArgumentException e1) {
					log.debug("Failed to update field '"+field.getName()+"'",e1);
				}
			}
		}
	}

	/**
	 * Here an empty list is returned.<br>
	 * Subclass needs to provide its own list to ignore so that these fields will not be added to method {@link #update(Object)}.
	 * @return List<String>, a list of name for the field to be ignored by {@link #update(Object)}.
	 */
	protected List<String> getFieldNamesIgnoredByUpdateMethod(){
		List<String> ignoredFields = new ArrayList<String>();
		ignoredFields.add("REST_BASE_PATH");
		return ignoredFields;
	}

	@Override
	protected List<String> getFieldNamesIgnoredByToStringMethod(){
		List<String> ignoredFields = new ArrayList<String>();
		ignoredFields.add("REST_BASE_PATH");
		return ignoredFields;
	}

}
