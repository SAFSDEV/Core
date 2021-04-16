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
 * @date 2019-06-26    (Lei Wang) Initial release.
 * @date 2019-06-28    (Lei Wang) Defined parameter type {var}.
 * @date 2019-07-05    (Lei Wang) Defined parameter type {editbox}, {checkbox_action}, {editbox_action}.
 *                                Added methods generateRegexActions().
 * @date 2019-07-09    (Lei Wang) Defined parameter type {combobox_action}.
 * @date 2019-07-30    (Lei Wang) Defined parameter type {var_or_string}, {mapitem} and {mapitem_or_string}.
 * @date 2019-08-02    (Lei Wang) Modified getMapItem(): return both window's RS and component's RS in one string.
 * @date 2019-08-28    (Lei Wang) Defined parameter type {boolean}.
 * @date 2019-09-05    (Lei Wang) Defined parameter type {var_mapitem}, {var_mapitem_or_string}.
 *                                Resolved string with embedded variables.
 */
package org.safs.cukes.ai.selenium;

import static java.util.Locale.ENGLISH;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.safs.Constants;
import org.safs.IndependantLog;
import org.safs.SAFSPlus;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.model.commands.CheckBoxFunctions;
import org.safs.model.commands.ComboBoxFunctions;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.model.commands.ListViewFunctions;

import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.datatable.dependency.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Define the <a href="https://cucumber.io/docs/cucumber/configuration/">Custom Parameter Types</a> used in the gherkin feature files.<br>
 */
public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

	/** <b>"(.+)"</b> a string separated by {@link TestRecordData#POSSIBLE_SEPARATOR}, it will be parsed as a list */
	public static final String REGEX_LIST = "\"(.+)\"";
	/** <b>(\^?[a-zA-Z_]+[a-zA-Z_0-9]*)</b> represents a variable name (with an optional leading symbol ^) */
	public static final String REGEX_VAR = "(\\"+StringUtils.CARET+"?[a-zA-Z_]+[a-zA-Z_0-9]*)";

	/** <b>on|off|true|false|yes|no</b> represents a boolean value */
	public static final String REGEX_BOOLEAN = "[o|O][n|N]|[o|O][f|F][f|F]|[t|T][r|R][u|U][e|E]|[f|F][a|A][l|L][s|S][e|E]|[y|Y][e|E][s|S]|[n|N][o|O]";

	/** <b>(\^?[a-zA-Z_]+[a-zA-Z_0-9]*)|"([^"]*)"|'([^']*)'</b><br>
	 * <ul>
	 * <li>a variable name with an optional leading symbol ^
	 * <li>or a double-quoted-string or a single-quoted-string
	 * </ul>
	 */
	public static final String REGEX_VAR_OR_STRING = REGEX_VAR+"|\"([^\"]*)\""+"|'([^']*)'";

	/** <b>(([a-zA-Z_]+[a-zA-Z_0-9]*\:)?([a-zA-Z_]+[a-zA-Z_0-9]*\.)?([a-zA-Z_]+[a-zA-Z_0-9]*))</b> represents a map item, such as mapID:section.item */
	public static final String REGEX_MAPITEM = "(([a-zA-Z_]+[a-zA-Z_0-9]*\\"+StringUtils.COLON+")?([a-zA-Z_]+[a-zA-Z_0-9]*\\"+StringUtils.DOT+")?([a-zA-Z_]+[a-zA-Z_0-9]*))";

	/** <b>(([a-zA-Z_]+[a-zA-Z_0-9]*\:)?([a-zA-Z_]+[a-zA-Z_0-9]*\.)?([a-zA-Z_]+[a-zA-Z_0-9]*))|"([^"]*)"|'([^']*)'</b><br>
	 * <ul>
	 * <li>or a map item, such as mapID:section.item
	 * <li>or a double-quoted-string or a single-quoted-string
	 * </ul>
	 */
	public static final String REGEX_MAPITEM_OR_STRING = REGEX_MAPITEM+"|\"([^\"]*)\""+"|'([^']*)'";

	/** <b>(\^?[a-zA-Z_]+[a-zA-Z_0-9]*)|(([a-zA-Z_]+[a-zA-Z_0-9]*\:)?([a-zA-Z_]+[a-zA-Z_0-9]*\.)?([a-zA-Z_]+[a-zA-Z_0-9]*))</b><br>
	 * <ul>
	 * <li>a variable name with an optional leading symbol ^
	 * <li>or a map item, such as mapID:section.item
	 * </ul>
	 */
	public static final String REGEX_VAR_MAPITEM 		= REGEX_VAR+"|"+REGEX_MAPITEM;

	/** <b>(\^?[a-zA-Z_]+[a-zA-Z_0-9]*)|(([a-zA-Z_]+[a-zA-Z_0-9]*\:)?([a-zA-Z_]+[a-zA-Z_0-9]*\.)?([a-zA-Z_]+[a-zA-Z_0-9]*))|"([^"]*)"|'([^']*)'</b><br>
	 * <ul>
	 * <li>a variable name with an optional leading symbol ^
	 * <li>or a map item, such as mapID:section.item
	 * <li>or a double-quoted-string or a single-quoted-string
	 * </ul>
	 */
	public static final String REGEX_VAR_MAPITEM_OR_STRING = REGEX_VAR+"|"+REGEX_MAPITEM+"|\"([^\"]*)\""+"|'([^']*)'";

	/** <b>editbox|textfield|textarea|field|box</b> possible names to call an "editbox" */
	public static final String REGEX_EDITBOX 		= "[E|e]dit[B|b]ox|[T|t]ext[F|f]ield|[T|t]ext[A|a]rea|[F|f]ield|[B|b]ox";

	/** <b>Check|UnCheck</b> possible actions on a checkbox */
//	public static final String REGEX_CHECKBOX_ACTION = generateRegexActions(CheckBoxFunctions.class);
	public static final String REGEX_CHECKBOX_ACTION = "Check|check|CHECK|UnCheck|uncheck|UNCHECK";
	/** <b>type|typekeys|typechars|SetTextCharacters|SetTextValue|SetUnverifiedTextCharacters|SetUnverifiedTextValue</b> possible actions on an editbox */
	public static final String REGEX_EDITBOX_ACTION = "[T|t]ype|[T|t]ype[K|k]eys|[T|t]ype[C|c]hars|"+generateRegexActions(EditBoxFunctions.class);

	public static final String REGEX_COMBOBOX_ACTION = generateRegexActions(ComboBoxFunctions.class);
	//public static final String REGEX_COMBOBOX_ACTION = "CaptureItemsToFile|captureitemstofile|CAPTUREITEMSTOFILE|HideList|hidelist|HIDELIST|Select|select|SELECT|SelectIndex|selectindex|SELECTINDEX|SelectPartialMatch|selectpartialmatch|SELECTPARTIALMATCH|SelectUnverified|selectunverified|SELECTUNVERIFIED|SelectUnverifiedPartialMatch|selectunverifiedpartialmatch|SELECTUNVERIFIEDPARTIALMATCH|SetTextValue|settextvalue|SETTEXTVALUE|SetUnverifiedTextValue|setunverifiedtextvalue|SETUNVERIFIEDTEXTVALUE|ShowList|showlist|SHOWLIST|VerifySelected|verifyselected|VERIFYSELECTED";
	public static final String REGEX_LISTVIEW_ACTION = generateRegexActions(ListViewFunctions.class);

	/**
	 * Generate regex expression representing the possible keywords related to the command model.
	 * @param commandModelClass Class, the command model class in the package "org.safs.model.commands".
	 * @return String, regex expression representing the possible keywords
	 */
	public static final String generateRegexActions(Class<?> commandModelClass){
		StringBuilder regex = new StringBuilder();

		//We want "public" "static" "final" field.
		int publicStaticFinal = Modifier.STATIC|Modifier.FINAL|Modifier.PUBLIC;

		Field[] fields = commandModelClass.getDeclaredFields();
		String value = null;
		for(Field field:fields){
			if((field.getModifiers() & publicStaticFinal)==publicStaticFinal && field.getName().endsWith("_KEYWORD")){
				try {
					field.setAccessible(true);
					value = String.valueOf(field.get(null));
					regex.append(value+"|"+value.toLowerCase()+"|"+value.toUpperCase()+"|");
				} catch (IllegalArgumentException | IllegalAccessException e) {}
			}
		}

		String result = regex.toString();
		if(result.endsWith("|")){
			result = result.substring(0, result.length()-1);
		}
		return result;
	}


    @Override
    public Locale locale() {
        return ENGLISH;
    }

    /**
     * Defined types:
     * <ol>
     * <li><b>{list}</b>, 					see {@link #REGEX_LIST}
     * <li><b>{var}</b>, 					see {@link #REGEX_VAR}
     * <li><b>{var_or_string}</b>, 			see {@link #REGEX_VAR_OR_STRING}
     * <li><b>{mapitem}</b>, 				see {@link #REGEX_MAPITEM}
     * <li><b>{mapitem_or_string}</b>, 		see {@link #REGEX_MAPITEM_OR_STRING}
     * <li><b>{var_mapitem}</b>, 			see {@link #REGEX_VAR_MAPITEM}
     * <li><b>{var_mapitem_or_string}</b>, 	see {@link #REGEX_MAPITEM_OR_STRING}
     * <li><b>{editbox}</b>, 				see {@link #REGEX_EDITBOX}
     * <li><b>{checkbox_action}</b>, 		see {@link #REGEX_CHECKBOX_ACTION}
     * <li><b>{editbox_action}</b>, 		see {@link #REGEX_EDITBOX_ACTION}
     * <li><b>{combobox_action}</b>, 		see {@link #REGEX_COMBOBOX_ACTION}
     * <li><b>{listview_action}</b>, 		see {@link #REGEX_LISTVIEW_ACTION}
     * <li><b>{boolean}</b>, 				see {@link #REGEX_BOOLEAN}
     * </ol>
     */
    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        JacksonTransformer transformer = new JacksonTransformer();
        typeRegistry.setDefaultDataTableCellTransformer(transformer);
        typeRegistry.setDefaultDataTableEntryTransformer(transformer);
        typeRegistry.setDefaultParameterTransformer(transformer);

        //see "Custom Parameter types" in link https://cucumber.io/docs/cucumber/cucumber-expressions/#parameter-types
        //define {list} as a list of string.
        typeRegistry.defineParameterType(new ParameterType<>(
        		"list",
        		REGEX_LIST,
                List.class,
                (String s) -> ( StringUtils.getTokenList(s, StringUtils.deduceUsedSeparatorString(s)))
        ));
        //define {var} as a variable, it will be resolved as a SAFS variable
        typeRegistry.defineParameterType(new ParameterType<>(
        		"var",
        		REGEX_VAR,
        		String.class,
        		(String variable) -> getVariable(variable, false)
        ));
        //define {var_or_string} as a variable or a normal string, it will be resolved as a SAFS variable if it is a variable
        typeRegistry.defineParameterType(new ParameterType<>(
        		"var_or_string",
        		REGEX_VAR_OR_STRING,
        		String.class,
        		(String variableOrString) -> getVariable(variableOrString, true)
        ));
        typeRegistry.defineParameterType(new ParameterType<>(
        		"mapitem",
        		REGEX_MAPITEM,
        		String.class,
        		(String mapitem) -> getMapItem(mapitem, false)
        ));
        typeRegistry.defineParameterType(new ParameterType<>(
        		"mapitem_or_string",
        		REGEX_MAPITEM_OR_STRING,
        		String.class,
        		(String mapitem) -> getMapItem(mapitem, true)
        ));
        typeRegistry.defineParameterType(new ParameterType<>(
        		"var_mapitem",
        		REGEX_VAR_MAPITEM,
        		String.class,
        		(String mapitem) -> getVarOrMapItem(mapitem, false)
        		));
        typeRegistry.defineParameterType(new ParameterType<>(
        		"var_mapitem_or_string",
        		REGEX_VAR_MAPITEM_OR_STRING,
        		String.class,
        		(String mapitem) -> getVarOrMapItem(mapitem, true)
        		));
        typeRegistry.defineParameterType(new ParameterType<>(
        		"editbox",
        		REGEX_EDITBOX,
        		String.class,
        		(String editbox) -> editbox
        ));

        typeRegistry.defineParameterType(new ParameterType<>(
        		"checkbox_action",
        		REGEX_CHECKBOX_ACTION,
        		String.class,
        		(String action) -> action
        ));
        typeRegistry.defineParameterType(new ParameterType<>(
        		"editbox_action",
        		REGEX_EDITBOX_ACTION,
        		String.class,
        		(String action) -> action
        ));
        typeRegistry.defineParameterType(new ParameterType<>(
        		"combobox_action",
        		REGEX_COMBOBOX_ACTION,
        		String.class,
        		(String action) -> action
        ));
        typeRegistry.defineParameterType(new ParameterType<>(
        		"listview_action",
        		REGEX_LISTVIEW_ACTION,
        		String.class,
        		(String action) -> action
        		));
        typeRegistry.defineParameterType(new ParameterType<>(
        		"boolean",
        		REGEX_BOOLEAN,
        		Boolean.class,
        		(String bool) -> StringUtils.convertBool(bool)
        		));
    }

    /**
     * @param variable String, the name of the variable (it may contain a leading {@link StringUtils#CARET} ).
     * @return String, the value of the variable or null if the variable is not found.
     */
    private static String _getVariable(String variable){
    	String localVar = variable;
    	//Strip the possible leading ^
    	if(localVar.startsWith(StringUtils.CARET)){
    		localVar = localVar.substring(1);
    	}
    	return SAFSPlus._getVariable(localVar);
    }

    /**
     * @param mapitem String, the map-item. The format is {@link #REGEX_MAPITEM}.
     * @return String, the value of the map-item or null if it is not found in the map chain.<br>
     *                 if the map-item is in format window.component or mapID:window.component, then the value contains both window's RS and component's RS separated by {@link #SEPARATOR_WIN_COMP}.<br>
     *                 if the map-item is in format component or mapID:component, then the value contains only component's RS.<br>
     */
    private static String _getMapItem(String mapitem){
    	String debugmsg = StringUtils.debugmsg(false);

    	String localMapitem = mapitem;
    	if(!StringUtils.isValid(localMapitem))
    		return localMapitem;
    	int index = localMapitem.indexOf(StringUtils.COLON);
    	String mapID = null;
    	if(index>0){
    		mapID = localMapitem.substring(0, index);
    		localMapitem = localMapitem.substring(index+StringUtils.COLON.length());
    	}
    	//Split section and item by .
    	String sectionAndItem[] = StringUtils.getTokenArray(localMapitem, StringUtils.DOT);

    	if(sectionAndItem!=null){
    		String value = null;
    		if(sectionAndItem.length>1){
    			String window = SAFSPlus._getMappedValue(mapID, sectionAndItem[0], sectionAndItem[0]);
    			String component = SAFSPlus._getMappedValue(mapID, sectionAndItem[0], sectionAndItem[1]);
    			if(StringUtils.isValid(component)){
    				value = (StringUtils.isValid(window)?window:"")+SEPARATOR_WIN_COMP+component;
    			}else{
    				//the mapitem might be a simple string containing a dot
    				IndependantLog.warn(debugmsg+" cannot find the component "+mapitem+" in the map chain, return itself as result.");
    				value = mapitem;
    			}
    		}else{
    			IndependantLog.warn(debugmsg+" try to find "+mapitem+" in the [ApplicationContants] section in map chain.");
    			value = SAFSPlus._getMappedValue(mapID, null, sectionAndItem[0]);
    		}
    		return value;
    	}else{
    		return null;
    	}
    }

    /**
     * @param embeddedVarString String, the string containing embedded variables to be resolved.<br>
     *                                  the embedded variable is represent as {^variable}.<br>
     * @return String, the resolved string, all variables are replaced by their value.
     */
    private static String resolveEmbeddedVariable(String embeddedVarString){
    	String dbmsg = StringUtils.debugmsg(false);
		int embed_start = embeddedVarString.indexOf(Constants.EMBEDDED_VAR_PREFIX);
		if (embed_start < 0) {
			return embeddedVarString;
		}

		int embed_varname = embed_start + Constants.EMBEDDED_VAR_PREFIX.length();
		int embed_end = embeddedVarString.indexOf(Constants.EMBEDDED_VAR_SUFFIX, embed_varname);
		if (embed_end < 0) {
			return embeddedVarString;
		}
		String varname = embeddedVarString.substring(embed_varname, embed_end);

		//return unmodified if 0 length or contains spaces
		if (varname.length()==0) return embeddedVarString;
		if (varname.indexOf(" ")>=0) return embeddedVarString;

		IndependantLog.info(dbmsg+" found embedded variable varname: "+ varname);

		String value = SAFSPlus._getVariable(varname);;
		if(StringUtils.isValid(value)){
			//put it all together
			String result = embeddedVarString.substring(0, embed_start) + value;
			if (embeddedVarString.length() > embed_end+1)
				result += embeddedVarString.substring(embed_end+1);
			return resolveEmbeddedVariable(result);
		}else{
			//Stop at the first non-resolved embedded variable, the rest variables will not be resolved.
			IndependantLog.debug(dbmsg+" could not find valid value for embedded variable '"+ varname+"', the rest embedded variables will not be resolved.");
			return embeddedVarString;
		}
    }

    /**
     * @param variable String, the name of the variable (it may contain a leading {@link StringUtils#CARET} ). Or a string or a string with embedded variables.
     * @param resovleEmbeddedVar boolean, if true, then resolve the variable as "embedded-variables string" in case we don't find the variable.
     * @return String, the value of the variable.<br><br>
     *                 If the variable is not found<br>
     *                 return the resolved "embedded-variables string" if the parameter 'resovleEmbeddedVar' is true;<br>
     *                 or the variable itself<br>
     */
    private static String getVariable(String variable, boolean resovleEmbeddedVar){
    	//consider it as a simple variable
    	String result = _getVariable(variable);
    	if(!StringUtils.isValid(result)){
    		if(resovleEmbeddedVar){
    			//consider it as a string containing embedded variables
    			result = resolveEmbeddedVariable(variable);
    		}else{
    			result = variable;
    		}
    	}
    	return result;
    }

    /** <b>$SEPARATOR_WIN_COMP$</b> used to separate the window's RS and component's RS in a string */
    public static final String SEPARATOR_WIN_COMP = "$SEPARATOR_WIN_COMP$";

    /**
     * @param mapitem String, the map-item. The format is {@link #REGEX_MAPITEM}. or the string with embedded variables, or a simple string.
     * @param resovleEmbeddedVar boolean, if true, then resolve the mapitem as "embedded-variables string" in case we don't find the mapitem in the map.
     * @return String, the value of the map-item<br>
     *                 if the map-item is in format window.component or mapID:window.component, then the value contains both window's RS and component's RS separated by {@link #SEPARATOR_WIN_COMP}.<br>
     *                 if the map-item is in format component or mapID:component, then the value contains only component's RS.<br><br>
     *                 If not found in the map<br>
     *                 return the resolved "embedded-variables string" if the parameter 'resovleEmbeddedVar' is true;<br>
     *                 or the map-item itself<br>
     */
    private static String getMapItem(String mapitem, boolean resovleEmbeddedVar){
    	String result = _getMapItem(mapitem);
    	if(!StringUtils.isValid(result)){
    		if(resovleEmbeddedVar){
    			result = resolveEmbeddedVariable(mapitem);
    		}else{
    			result = mapitem;
    		}
    	}
    	return result;
    }

    /**
     * @param varOrmapitem String, the variable, or the map item, or the string with embedded variables, or a simple string.
     * @return String, the value of the variable;<br>
     *                 or the value of the map-item<br>
     *                if the map-item is in format window.component or mapID:window.component, then the value contains both window's RS and component's RS separated by {@link #SEPARATOR_WIN_COMP}.<br>
     *                if the map-item is in format component or mapID:component, then the value contains only component's RS.<br><br>
     *                If it is neither a variable or a map item<br>
     *                return the resolved "embedded-variables string" if the parameter 'resovleEmbeddedVar' is true;<br>
     *                or the parameter 'varOrmapitem' itself<br>
     */
    private static String getVarOrMapItem(String varOrmapitem, boolean resovleEmbeddedVar){
    	String result = _getVariable(varOrmapitem);
    	if(!StringUtils.isValid(result)){
    		result = _getMapItem(varOrmapitem);
    	}

    	if(!StringUtils.isValid(result)){
    		if(resovleEmbeddedVar){
    			result = resolveEmbeddedVariable(varOrmapitem);
    		}else{
    			result = varOrmapitem;
    		}
    	}
    	return result;
    }

    public static class JacksonTransformer implements ParameterByTypeTransformer, TableEntryByTypeTransformer, TableCellByTypeTransformer {
        ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Object transform(String s, Type type) {
            return objectMapper.convertValue(s, objectMapper.constructType(type));
        }

        @Override
        public <T> T transform(Map<String, String> map, Class<T> aClass, TableCellByTypeTransformer tableCellByTypeTransformer) {
            return objectMapper.convertValue(map, aClass);
        }

        @Override
        public <T> T transform(String s, Class<T> aClass) {
            return objectMapper.convertValue(s, aClass);
        }
    }

    public static void main(String[] args){
    	System.out.println(generateRegexActions(CheckBoxFunctions.class));
    	System.out.println(generateRegexActions(EditBoxFunctions.class));
    	System.out.println(generateRegexActions(ListViewFunctions.class));
    	System.out.println(generateRegexActions(ComboBoxFunctions.class));
    }
}
