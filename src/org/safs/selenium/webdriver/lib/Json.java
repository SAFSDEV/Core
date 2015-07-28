package org.safs.selenium.webdriver.lib;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
	 * @return Map, the JSON data as a Map
	 */
	public static Map<?, ?> readJSONFileUTF8(String file){
		return readJSONFile(file, StringUtils.KEY_UTF8_CHARSET);
	}
	/**
	 * Read a JSON data file, and convert the data into a Java Map and return it.<br>
	 * @param file String, the absolute JSON file.
	 * @param encoding String, the JSON file encoding.
	 * @return Map, the JSON data as a Map
	 */
	public static Map<?, ?> readJSONFile(String file, String encoding){
		try {
			JsonToBeanConverter converter = new JsonToBeanConverter();
			Map<?, ?> jsonMap = converter.convert(Map.class, FileUtilities.readStringFromEncodingFile(file, encoding));
			return jsonMap;
		} catch (Exception e) {
			IndependantLog.error("Fail to get JSON data as a Map.", e);
			return null;
		}
	}	
}
