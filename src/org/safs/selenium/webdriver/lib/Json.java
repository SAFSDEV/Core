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
package org.safs.selenium.webdriver.lib;

import java.lang.reflect.Type;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.text.FileUtilities;

import com.google.gson.GsonBuilder;

public class Json {

	/**
	 * Jason object/array object compare
	 * @param obj1
	 * @param obj2
	 * @return
	 * @throws JSONException
	 */
	public static boolean jsonsEqual(Object obj1, Object obj2) throws JSONException

    {
        if (!obj1.getClass().equals(obj2.getClass()))
        {
            return false;
        }

        if (obj1 instanceof JSONObject)
        {
            JSONObject jsonObj1 = (JSONObject) obj1;
            JSONObject jsonObj2 = (JSONObject) obj2;

            String[] names = JSONObject.getNames(jsonObj1);
            String[] names2 = JSONObject.getNames(jsonObj2);
            if (names.length != names2.length)
            {
                return false;
            }

            for (String fieldName:names)
            {
                Object obj1FieldValue = jsonObj1.get(fieldName);

                Object obj2FieldValue = jsonObj2.get(fieldName);

                if (!jsonsEqual(obj1FieldValue, obj2FieldValue))
                {
                    return false;
                }
            }
        }
        else if (obj1 instanceof JSONArray)
        {
            JSONArray obj1Array = (JSONArray) obj1;
            JSONArray obj2Array = (JSONArray) obj2;

            if (obj1Array.length() != obj2Array.length())
            {
                return false;
            }

            for (int i = 0; i < obj1Array.length(); i++)
            {
                boolean matchFound = false;

                for (int j = 0; j < obj2Array.length(); j++)
                {
                    if (jsonsEqual(obj1Array.get(i), obj2Array.get(j)))
                    {
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound)
                {
                    return false;
                }
            }
        }
        else
        {
            if (!obj1.equals(obj2))
            {
                return false;
            }
        }

        return true;
    }

	/**
	 * Read a JSON data file with UTF-8 encoding, and convert the data into a Java Map and return it.<br>
	 * @param file String, the absolute JSON file.
	 * @return Map, the JSON data as a Map; null if something wrong happened.
	 */
	public static Map<?, ?> readJSONFileUTF8(String file){
		return readJSONFile(file, StringUtils.KEY_UTF8_CHARSET);
	}
	/**
	 * Read a JSON data file, and convert the data into a Java Map and return it.<br>
	 * @param file String, the absolute JSON file.
	 * @param encoding String, the JSON file encoding.
	 * @return Map, the JSON data as a Map; null if something wrong happened.
	 */
	public static Map<?, ?> readJSONFile(String file, String encoding){
		try {
			return convert(Map.class, FileUtilities.readStringFromEncodingFile(file, encoding));
		} catch (Exception e) {
			IndependantLog.error("Fail to get JSON data as a Map.", e);
			return null;
		}
	}

	/**
	 * Convert 'json object' to a certain type.
	 * @param clazz Class<T>, the expected type to which the json object will be converted.
	 * @param jsonData String, the json data object.
	 * @return T, the converted 'json object'; null if something wrong happened.
	 */
	public static <T> T convert(Class<T> clazz, String jsonData){
		try {
			if(clazz==null || jsonData==null){
				IndependantLog.error(StringUtils.debugmsg(false)+" converted-clazz or jsonData is null.");
				return null;
			}
			//JsonToBeanConverter has been removed from selenium-standalone jar, we use google.gson to do the work.
			return fromJsonString(jsonData, clazz);
		} catch (Exception e) {
			IndependantLog.error(StringUtils.debugmsg(false)+"Fail to get convert JSON data to "+clazz.getSimpleName(), e);
			return null;
		}
	}

	/**
	 * Convert a JSON String to a certain type.<br>
	 * <b>Note:</b> It needs com.google.gson.Gson, which is currently included in the selenium-server-standalone jar.
	 *              If we want this Utils class independent from selenium-server-standalone jar, we need to put the google gson jar on the classpath.
	 *
	 * @param jsonString String, the JSON string.
	 * @param type Class<T>, the type of object which "JSON String" to be converted.
	 * @return T, the T object converted from JSON string.
	 */
	public static <T> T fromJsonString(String jsonString, Class<T> type){
		return fromJson(new GsonBuilder(), jsonString, type);
	}

	/**
	 * Provide a flexible way to convert a JSON String to a certain type.<br>
	 * User can provide its own GsonBuilder.<br>
	 *
	 * @param builder GsonBuilder, the user-provided builder.
	 * @param jsonString String, the JSON string.
	 * @param type Class<T>, the type of object which "JSON String" to be converted.
	 * @return T, the T object converted from JSON string.
	 */
	public static <T> T  fromJson(GsonBuilder builder, String jsonString, Class<T> type){
		T result = builder.create().fromJson(jsonString, type);
		IndependantLog.info("convert json String '"+jsonString+"' to "+type.getName()+" object '"+result+"'.");
		return result;
	}

	/**
	 * Provide a flexible way to convert a JSON String to a certain type.<br>
	 * User can provide its own GsonBuilder.<br>
	 *
	 * @param builder GsonBuilder, the user-provided builder.
	 * @param jsonString String, the JSON string.
	 * @param type Type, the type of object which "JSON String" to be converted.
	 * @return T, the T object converted from JSON string.
	 */
	public static <T> T fromJson(GsonBuilder builder, String jsonString, Type type){
		T result = builder.create().fromJson(jsonString, type);
		IndependantLog.info("convert json String '"+jsonString+"' to "+type.getTypeName()+" object '"+result+"'.");
		return result;
	}

	public static void main(String[] args){
		//Test Json.convert();
		//In the version later than selenium-server-standalone-2.47.1.jar, selenium has removed org.seleniumhq.jetty7.util.ajax.JSON
		//so we use other API (JsonToBeanConverter) to convert json data.
		String v = "{\"id\":\"{972ce4c6-7e08-4474-a285-3208198ce6fd}\",\"syncGUID\":\"jzmbFbDykcBZ\",\"location\":\"app-global\",\"version\":\"44.0.2\",\"type\":\"theme\",\"internalName\":\"classic/1.0\",\"updateURL\":null,\"updateKey\":null,\"optionsURL\":null,\"optionsType\":null,\"aboutURL\":null,\"icons\":{\"32\":\"icon.png\",\"48\":\"icon.png\"},\"iconURL\":null,\"icon64URL\":null,\"defaultLocale\":{\"name\":\"Default\",\"description\":\"The default theme.\",\"creator\":\"Mozilla\",\"homepageURL\":null,\"contributors\":[\"Mozilla Contributors\"]},\"visible\":true,\"active\":true,\"userDisabled\":false,\"appDisabled\":false,\"descriptor\":\"This is description.\",\"installDate\":1455896567960,\"updateDate\":1455896567960,\"applyBackgroundUpdates\":1,\"skinnable\":true,\"size\":24172,\"sourceURI\":null,\"releaseNotesURI\":null,\"softDisabled\":false,\"foreignInstall\":false,\"hasBinaryComponents\":false,\"strictCompatibility\":true,\"locales\":[],\"targetApplications\":[{\"id\":\"{ec8030f7-c20a-464f-9b0e-13a3a9e97384}\",\"minVersion\":\"44.0.2\",\"maxVersion\":\"44.0.2\"}],\"targetPlatforms\":[]}";
//		Object nv = org.seleniumhq.jetty7.util.ajax.JSON.parse(v);
		Object nv = Json.convert(Map.class, v);
		if(nv instanceof Map){
			System.out.println(nv);
		}
	}

}
