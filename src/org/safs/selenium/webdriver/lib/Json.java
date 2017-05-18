package org.safs.selenium.webdriver.lib;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.JsonException;
import org.openqa.selenium.remote.JsonToBeanConverter;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.text.FileUtilities;

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
	 * @param jsonData Object, the json data object.
	 * @return T, the converted 'json object'; null if something wrong happened.
	 */
	public static <T> T convert(Class<T> clazz, Object jsonData){
		try {
			if(clazz==null || jsonData==null){
				IndependantLog.error(StringUtils.debugmsg(false)+" converted-clazz or jsonData is null.");
				return null;
			}
			JsonToBeanConverter converter = new JsonToBeanConverter();
			return converter.convert(clazz, jsonData);
		} catch (JsonException e) {
			IndependantLog.error(StringUtils.debugmsg(false)+"Fail to get convert JSON data to "+clazz.getSimpleName(), e);
			throw null;
		}
	}

	public static void main(String[] args){
		//Test Json.convert();
		//In the version later than selenium-server-standalone-2.47.1.jar, selenium has removed org.seleniumhq.jetty7.util.ajax.JSON
		//so we use other API (JsonToBeanConverter) to convert json data.
		String v = "{\"id\":\"{972ce4c6-7e08-4474-a285-3208198ce6fd}\",\"syncGUID\":\"jzmbFbDykcBZ\",\"location\":\"app-global\",\"version\":\"44.0.2\",\"type\":\"theme\",\"internalName\":\"classic/1.0\",\"updateURL\":null,\"updateKey\":null,\"optionsURL\":null,\"optionsType\":null,\"aboutURL\":null,\"icons\":{\"32\":\"icon.png\",\"48\":\"icon.png\"},\"iconURL\":null,\"icon64URL\":null,\"defaultLocale\":{\"name\":\"Default\",\"description\":\"The default theme.\",\"creator\":\"Mozilla\",\"homepageURL\":null,\"contributors\":[\"Mozilla Contributors\"]},\"visible\":true,\"active\":true,\"userDisabled\":false,\"appDisabled\":false,\"descriptor\":\"C:\\Program Files (x86)\\Mozilla Firefox\\browser\\extensions\\{972ce4c6-7e08-4474-a285-3208198ce6fd}\",\"installDate\":1455896567960,\"updateDate\":1455896567960,\"applyBackgroundUpdates\":1,\"skinnable\":true,\"size\":24172,\"sourceURI\":null,\"releaseNotesURI\":null,\"softDisabled\":false,\"foreignInstall\":false,\"hasBinaryComponents\":false,\"strictCompatibility\":true,\"locales\":[],\"targetApplications\":[{\"id\":\"{ec8030f7-c20a-464f-9b0e-13a3a9e97384}\",\"minVersion\":\"44.0.2\",\"maxVersion\":\"44.0.2\"}],\"targetPlatforms\":[]}";
//		Object nv = org.seleniumhq.jetty7.util.ajax.JSON.parse(v);
		Object nv = Json.convert(Map.class, v);
		if(nv instanceof Map){
			System.out.println(nv);
		}
	}

}
