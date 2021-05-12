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
 * @date 2019-07-02    (Lei Wang) Added findElementsInParent() and findElementInParent(): search the real element around an other element (label element for example).
 * @date 2019-07-09    (Lei Wang) Added findElementByLabel() and findElementsByLabel(): search the real element around a label element.
 * @date 2019-07-10    (Lei Wang) Added highlight() and clearHighlight().
 * @date 2019-07-11    (Lei Wang) Refactor code so that subclass can be easily implemented: added getLibraryName(), process(), localProcess() and findElements().
 * @date 2019-07-12    (Lei Wang) Modified findElementsByLabel(): support looking by attribute 'for'.
 * @date 2019-08-01    (Lei Wang) Modified getCachedLibComponent(): refresh the cached component.
 * @date 2019-08-02    (Lei Wang) Used new Criteria (supporting both SAFS RS and label) to find WebElements.
 * @date 2019-08-08    (Lei Wang) Modified findElementsByLabel(): 1. try all possible label elements to get the target element
 *                                                                2. consider label elements as target if the passed-in xpath is null.
 *                                Modified localProcess(): handle the "click"/"tap" and "GUIDoesExist".
 * @date 2019-08-16    (Lei Wang) Modified getCachedLibComponent(): if the cached component is stale then return null.
 *                                Modified saveCachedLibComponent(): set the 'searchContext' and 'setPossibleRecognitionStrings' to cached component for later refresh.
 *                                Modified findElements(): set the 'searchContext' properly.
 * @date 2019-08-23    (Lei Wang) Moved some codes to class AIComponent.
 * @date 2019-08-28    (Lei Wang) Moved some codes from AITextActions to here;
 *                                Added addNonRepeatedElements().
 * @date 2019-08-30    (Lei Wang) Handle the frames.
 * @date 2019-09-02    (Lei Wang) Add method matched(): check if the element can match the expected type.
 *                                Modified _findElementsByLabel(): call matched() to verify the web-element got by id (value of attribute 'for' of label element).
 * @date 2019-09-04    (Lei Wang) Moved some constants to class Constants.
 * @date 2019-09-06    (Lei Wang) Moved frames related methods to class SearchObject.
 * @date 2019-09-18    (Lei Wang) Modified findSelectableItemsFromText(), findElementsByLabel(): if 'bypassFrameReset' is true, then we suppose that user has
 *                                         switched the frame explicitly by himself, we will not try every frame to find web-element.
 * @date 2019-09-27    (Lei Wang) Overloaded method runCukesTest():
 *                                           one version for running feature file with default step definitions in package "org.safs.cukes.ai.selenium".
 *                                           one version for running feature file with the step definitions in multiple packages.
 * @date 2019-11-13    (Lei Wang) Added some utility methods, copied from the Processor class.
 */
package org.safs.cukes.ai.selenium;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.safs.Arbre;
import org.safs.Constants;
import org.safs.Constants.HTMLConst;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.SeleniumPlus;
import org.safs.selenium.webdriver.lib.RS.XPATH;
import org.safs.selenium.webdriver.lib.RemoteDriver;
import org.safs.selenium.webdriver.lib.SearchObject.FrameElement;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.text.FileUtilities;
import org.safs.tools.RuntimeDataInterface;

import cucumber.runtime.Runtime;

/**
 * The base class on which many AI Search Algorithms will subclass.
 * <p>
 * There can be no invokable step definition methods in this class since no true step definition
 * file is allowed to be extended.
 * <p>
 * To execute a Cucumber test based on this class:
 * <p>
 * <ol>
 * <li>Your SeleniumPlus test class might EXTEND this AISearchBase class,
 * <li>That subclass must implement/override the SeleniumPlus.runTest() method,
 * <li>the runTest() method will call the runCukesTest(String, String) method with appropriate glue and feature file parameters.
 * </ol>
 * <p>
 * Alternatively, you might be able to call the static runCukesTest(String, String) method from anywhere to run one or more Cucumber invocations.
 * <p>
 * The following JARs must be in the JVM CLASSPATH for such a Unit test invocation.
 * This is the same as any other SeleniumPlus test invocation:
 * <pre>
 * 	 pathTo/yourClasses/bin or yourTest.jar,
 * 	 pathTo/seleniumplus.jar,
 * 	 pathTo/JSTAFEmbedded.jar, (or JSTAF.jar if using STAF and other external tools or engines.)
 * </pre>
 * Then, you can execute this test with an invocation similar to:
 * <pre>
 * 	 java -cp %CLASSPATH% td.testcases.stepdefs.AISearchBase
 * </pre>
 *
 * @see org.safs.selenium.webdriver.SeleniumPlus#main(java.lang.String[])
 * @see SeleniumPlus#runTest()
 * @see #runCukesTest(String, String)
 */
public class AISearchBase extends SeleniumPlus {

	private static final String _TAG         = "tag";
	private static final String _TEXT        = "text";

	/** default: false */
	protected static boolean _substring_matches_allowed = false;
	/** default: false */
	protected static boolean _case_does_not_matter = false;

	/** default: true */
	protected static boolean _abort_on_find_failure = true;
	/** default: false */
	protected static boolean _abort_on_assert_failure = false;

	/** default: false */
	protected static boolean _using_cached_component = false;

	/** default: true */
	protected static boolean _log_details 	= true;

	/** a cache holding the library component */
	protected Map<String, org.safs.selenium.webdriver.lib.Component> cachedLibComponents = new HashMap<String, org.safs.selenium.webdriver.lib.Component>();

	/** get the cached Component according to the key, which can be a text-label, xpath or css-selector ect. */
	protected org.safs.selenium.webdriver.lib.Component getCachedLibComponent(Criteria criteria){
		org.safs.selenium.webdriver.lib.Component libComponent = null;
		if(_using_cached_component){
			libComponent = cachedLibComponents.get(criteria.toString());
			//We need to refresh the cached component, sometimes the embedded WebElement becomes stale
			if(libComponent!=null){
				libComponent.refresh(true);
				try {
					//if the embedded WebElement is still stale, then it is not usable.
					if(libComponent.isStale()){
						libComponent = null;
						cachedLibComponents.remove(criteria.toString());
					}
				} catch (Exception e) {
					IndependantLog.error("Met "+e.toString());
				}
			}
			return libComponent;
		}
		return null;
	}

	/** put the Component into the cache by the key, which can be a text-label, xpath or css-selector ect.  */
	protected void saveCachedLibComponent(Criteria criteria, org.safs.selenium.webdriver.lib.Component component){
		if(_using_cached_component && component!=null){
			if(criteria!=null){
				//set search-context and possible recognition strings for refreshing WebElement.
				component.setSearchContext(searchContext);
				List<String> extraRS = new ArrayList<String>();
				extraRS.add(criteria.getComponentRS());
//				String xpath = SearchObject.generateGenericXPath(component.getWebElement());
//				if(xpath!=null) extraRS.add(RS.xpath(xpath));
				component.setPossibleRecognitionStrings(extraRS.toArray(new String[0]));
				//save the Component into the cache
				cachedLibComponents.put(criteria.toString(), component);
			}
		}
	}

	/**
	 * The context to find WebElements.<br>
	 * It is normally a WebDriver but it also can be a WebElement (the WebElement representing the parent) if we specify the recognition string as parent.child format.<br>
	 *
	 * @see #findElements(Criteria)
	 */
	protected SearchContext searchContext = null;

	/**
	 * Find the WebElements according to the search Criteria.<br>
	 * In this method we firstly find the WebElements by SeleniumPlus-Library (the frame may get switched if the RS containing information about frame).<br>
	 * In not found, we try to find WebElements in AI way, Criteria's component RS will be treated as the anchor-label to search components around.<br>
	 *
	 * @param criteria Criteria, the conditions helping to find elements
	 * @return List<WebElement> a list of matched elements
	 */
	protected List<WebElement> findElements(Criteria criteria){
		//Reset the search context to the web-driver
		searchContext = WDLibrary.getWebDriver();

		//Get matched WebElements according to the window/componet's recognition string
		List<WebElement> elements = null;

		//To see if we need to reset the WDLibrary's lastFrame to null.
		WDLibrary.checkWindowRS(criteria.getParentRS());

		if (criteria.getParentRS() == null) {
			elements = WDLibrary.getObjects(searchContext, criteria.getComponentRS());
		} else {
			WebElement parent = WDLibrary.getObject(searchContext, criteria.getParentRS());
			elements = WDLibrary.getObjects(parent, criteria.getComponentRS());
			//change the searchContext to parent
			searchContext = parent;
		}

		if(elements==null || elements.isEmpty()){
			elements = findElementsByLabel(criteria.getComponentRS(), criteria.isPartialMatch(), criteria.getType());
		}

		return elements;
	}

	/**
	 * Log Map key=value pairs to the debug log for review.<br>
	 * Called internally by certain info collecting methods.
	 * @param map
	 * @see #logElementProperties(WebElement)
	 * @see #logElementIdInfo(WebElement)
	 */
	public static void logMapKeyValues(Map<String,Object> map){
		for(String key:map.keySet()){
			IndependantLog.info(Constants.INDENT + key +" = "+ map.get(key));
		}
	}

	/**
	 * Log 'properties' info on the provided WebElement as collected by WDLibrary.getProperties.<br>
	 * Also see the logElementIdInfo method for a comparison of items collected.
	 * @param element
	 * @see WDLibrary#getProperties(WebElement)
	 * @see #logMapKeyValues(Map)
	 * @see #logElementIdInfo(WebElement)
	 */
	public static void logElementProperties(WebElement element){
		try{
			Map<String,Object> props = WDLibrary.getProperties(element);
			logMapKeyValues(props);
		}catch(SeleniumPlusException spx){
			IndependantLog.warn("Listing interrupted by "+spx.getClass().getSimpleName()+": "+spx.getMessage());
		}
	}

	/**
	 * Collect and log important recognition information on an element and then log it for review.<br>
	 * Information like:
	 * <p><ul>
	 * <li>tag<li>id<li>class<li>name<li>placeholder<li>text<li>value<li>etc..
	 * </ul>
	 * @param element - WebElement to collect and log important attributes.
	 * @see #logMapKeyValues(Map)
	 */
	public static void logElementIdInfo(WebElement element){
		// logElementProperties(element, driver);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(HTMLConst.ATTRIBUTE_ID, element.getAttribute(HTMLConst.ATTRIBUTE_ID));
		map.put(_TAG, element.getTagName());
		map.put(HTMLConst.ATTRIBUTE_CLASS, element.getAttribute(HTMLConst.ATTRIBUTE_CLASS));
		map.put(HTMLConst.ATTRIBUTE_NAME, element.getAttribute(HTMLConst.ATTRIBUTE_NAME));
		map.put(HTMLConst.ATTRIBUTE_PLACEHOLDER, element.getAttribute(HTMLConst.ATTRIBUTE_PLACEHOLDER));
		map.put(_TEXT, element.getText());
		map.put(HTMLConst.ATTRIBUTE_VALUE, element.getAttribute(HTMLConst.ATTRIBUTE_VALUE));
		logMapKeyValues(map);
	}

	/**
	 * @param elements -- List of WebElements to be iteratively sent to logElementIdInfo
	 * @see #logElementIdInfo(WebElement)
	 */
	public static void logListElementsInfo(List<WebElement> elements){
		for(WebElement element:elements){
			logElementIdInfo(element);
		}
	}

	/**
	 * Switch to (make active) the first 'iframe' element found in the current window/doc/frame.<br>
	 * Does NOT currently set/change the WebDriver lastFrame reference.  Though we should consider doing that.<br>
	 * May want to search on frame or frameset if these are found to be needed in the future.
	 * @param selenium - RemoteDriver controlling the web session of interest.
	 * @return the 'iframe' WebElement if found and switched, or null if not found.
	 * @deprecated please use {@link #initializeFrames(WebDriver)} instead.
	 */
	@Deprecated
	public static WebElement getFirstIFrame(RemoteDriver selenium){
		String dbgmsg = StringUtils.debugmsg(false);
		WebElement result = null;
		List<WebElement> elements = selenium.findElementsByTagName(HTMLConst.TAG_IFRAME);
		if(elements==null||elements.isEmpty()){
			IndependantLog.info(dbgmsg+" found no iframes in the current window.");
		}else{
			IndependantLog.info(dbgmsg+" found "+ elements.size() +" iframes in the current window.");
			result = elements.get(0);
			String fname = result.getAttribute(HTMLConst.ATTRIBUTE_NAME);
			String id = result.getAttribute(HTMLConst.ATTRIBUTE_ID);
			selenium.switchTo().frame(result);
			if(fname == null || fname.length()==0) {
				fname = id;
				if(fname == null || fname.length()==0){
					IndependantLog.info(dbgmsg+" using first frame with no name or id specified.");
				}else{
					IndependantLog.info(dbgmsg+" using first frame with id "+ fname +" iframes in the current window.");
				}
			}else{
				IndependantLog.info(dbgmsg+" using first frame with name '"+ fname +"'.");
			}
		}
		return result;
	}

	/**
	 * @param type String, represents the WebElement's type, such as "ComboBox", "Button" etc.
	 * @return String, represents the XPATH according to which to find the WebElement. For example <b>XPATH.relativeDescendants(XPATH.COMBOBOX)</b> is used to find the ComboBox elements.
	 */
	protected String getXpathType(String type){
		return XPATH.ofType(type);
	}

	/**
	 * Test if the element's type can match what is expected.
	 * @param element WebElement, the element to check.
	 * @param type String, the expected type, such as EditBox, RadioButton, ComboBox etc.
	 * @return boolean true if the web-element matches the expected type.
	 */
	protected boolean matched(WebElement element, String type){
		return XPATH.matched(element, type);
	}

	/**
	 * Reset the static field {@link #frames} and {@link #frameTree} to null.
	 */
	protected void resetFrames(){
		frameTree = null;
		frames = null;
	}

	/** the frame tree of the whole html document */
	protected static Arbre<FrameElement> frameTree = null;
	/** a list of frame nodes, each node contains its parent frames to the root. */
	protected static List<Arbre<FrameElement>> frames = null;

	/**
	 * This method will
	 * <ol>
	 * <li>generate the frame-tree and assign it to static field {@link #frameTree}
	 * <li>traverse the {@link #frameTree} in pre-order and put each frame node into a static list {@link #frames}
	 * </ol>
	 *
	 * @param selenium WebDriver
	 * @return List&lt;Arbre&lt;FrameElement>>, a list of frames of the whole page.
	 */
	protected synchronized List<Arbre<FrameElement>> initializeFrames(WebDriver selenium){
		if(frames==null){
			frames = new ArrayList<Arbre<FrameElement>>();
			frameTree = WDLibrary.getFrameTree(selenium);
			WDLibrary.traverseFrameTree(frameTree, frames);
		}
		return frames;
	}

	/**
	 * Try to find the matched elements from the sub-nodes of parent of current node,
	 * if not found then we go to the grand-parent to search again, and so on.
	 *
	 * @param baseElement WebElement, from whose parent to search
	 * @param xpath String, the xpath served as search-conditions
	 * @return List<WebElement>, the matched elements. null if cannot be found.
	 */
	public static List<WebElement> findElementsInParent(WebElement baseElement, String xpath){
		String dbgmsg  = StringUtils.debugmsg(false);

		if(baseElement instanceof WebElement){
			try{
				WebElement parent = baseElement.findElement(By.xpath(XPATH.PARENT));
				IndependantLog.debug(dbgmsg+" according to xpath '"+xpath+"' searching elements in children of the element '"+ parent.getTagName() +"' of ID "+parent.getAttribute(HTMLConst.ATTRIBUTE_ID));
				List<WebElement> elements = parent.findElements(By.xpath(xpath));

				if(elements==null||elements.isEmpty()){
					IndependantLog.warn(dbgmsg+" found no elements in children.");
					//Try to get from the grand-parent
					return findElementsInParent(parent, xpath);
				}else{
					// these are MATCHES!
					IndependantLog.info(dbgmsg+" found "+ elements.size() +" elements.");
					return elements;
				}
			}catch(Exception e){
				IndependantLog.error(dbgmsg+" Met exception "+e);
				return null;
			}
		}else{
			IndependantLog.error(dbgmsg+" the object '"+baseElement+"' is not a WebElement!");
			return null;
		}
	}

	/**
	 * Try to find the Nth matched element from the sub-nodes of parent of current node,
	 * if not found then we go to the grand-parent to search again, and so on.
	 *
	 * @param baseElement WebElement, from whose parent to search
	 * @param xpath String, the xpath served as search-conditions
	 * @param index int, the Nth matched item
	 * @return WebElement, the matched Nth element or null if cannot be found.
	 */
	public static WebElement findElementInParent(WebElement baseElement, String xpath, int index){
		String dbgmsg  = StringUtils.debugmsg(false);
		List<WebElement> elements = findElementsInParent(baseElement, xpath);

		if(elements==null||elements.isEmpty()||elements.size()<index){
			IndependantLog.warn(dbgmsg+" there is no enough matched items!");
			return null;
		}else{
			IndependantLog.debug(dbgmsg+" returning "+(index+1)+"th matched item.");
			return elements.get(index);
		}
	}

//	/**
//	 * Find a WebElement that are associated with the provided textlabel matches the label-component around the WebElement<br>
//	 * If not found, we will attempt to look in a child iframe, if present.<br>
//	 * @param textlabel String, displayed text value to match against.
//	 * @param partialMatch boolean, if the parameter 'textlabel' will be matched partially
//	 * @param type String, represents the WebElement's type, such as "ComboBox", "Button" etc.
//	 * @param index int, the Nth matched element
//	 * @return WebElement a WebElement matching the searching criteria, or null if not found.
//	 */
//	public WebElement findElementByLabel(String textlabel, boolean partialMatch, String type, int index){
//		List<WebElement> elements = findElementsByLabel(textlabel, partialMatch, type);
//
//		if(elements.size()>index){
//			return elements.get(index);
//		}
//
//		return null;
//	}

	/**
	 * This method will do steps
	 * <ol>
	 * <li>get all frames
	 * <li>try each frame, within that frame we try to find all WebElements that are associated with the provided textlabel matches the label-component around the WebElement
	 * <li>stop the frame-switch-loop if we find any elements.
	 * </ol>
	 *
	 * <b>NOTE: Once this method is done, we should have switched to a certain frame.</b>
	 * <p>
	 *
	 * @param textlabel String, displayed text value to match against.
	 * @param partialMatch boolean, if the parameter 'textlabel' will be matched partially
	 * @param type String, represents the WebElement's type, such as "ComboBox", "Button" etc.
	 * @return List<WebElement> WebElements matching the searching criteria, or null/empty-list if not found.
	 *
	 * @see #_findElementsByLabel(String, boolean, String)
	 */
	private List<WebElement> findElementsByLabel(String textlabel, boolean partialMatch, String type){
		RemoteDriver selenium = (RemoteDriver) WDLibrary.getWebDriver();
		List<WebElement> results = null;

		if(WDLibrary.getBypassFramesReset()){
			//We should focus on a certain frame context
			results = _findElementsByLabel(textlabel, partialMatch, type);
		}else{
			initializeFrames(selenium);
			//we switch to all frames until we find any elements
			for(Arbre<FrameElement> frame: frames){
				WDLibrary.switchToFrame(frame, selenium);
				results = _findElementsByLabel(textlabel, partialMatch, type);
				if(!results.isEmpty()){
					break;
				}
			}
		}

		return results;
	}

	/**
	 * On current frame document, find all WebElements that are associated with the provided textlabel matches the label-component around the WebElement<br>
	 * @param textlabel String, displayed text value to match against.
	 * @param partialMatch boolean, if the parameter 'textlabel' will be matched partially
	 * @param type String, represents the WebElement's type, such as "ComboBox", "Button" etc.
	 * @return List<WebElement> WebElements matching the searching criteria, or null/empty-list if not found.
	 */
	private List<WebElement> _findElementsByLabel(String textlabel, boolean partialMatch, String type){
		//TODO use the field 'searchContext' instead to search.
		RemoteDriver selenium = (RemoteDriver) WDLibrary.getWebDriver();
		List<WebElement> results = new ArrayList<WebElement>();
		List<WebElement> elements = null;

		//get the XPATH according to which to find the WebElements.
		String xpath = getXpathType(type);
		//If we can find the label element, the real component must be around (perhaps in the parent-tree).
		//we need to try all possible label elements by calling AITextActions.findSelectableItemsFromText()
		List<WebElement> anchorElements = _findSelectableItems(textlabel);

		if(anchorElements!=null && !anchorElements.isEmpty()){
			if(xpath==null){
				//we just want the label elements
				results.addAll(anchorElements);
			}else{
				for(WebElement anchorElement:anchorElements){
					WebElement element= null;
					//https://www.w3schools.com/tags/att_label_for.asp
					//attribute 'for' indicates the id of the target-component or the component near the target-component
					String targetID = anchorElement.getAttribute(HTMLConst.ATTRIBUTE_FOR);
					if(targetID!=null){
						element = selenium.findElement(By.id(targetID));
						if(element!=null){
							//it might be the element we look for
							if(matched(element, type)){
								//we verify the element's type,  add it to the results if "matched".
								results.add(element);
							}else{
								//set it as new anchorElement to find more possible elements
								anchorElement = element;
							}
						}
					}

					//TODO Handle 'aria-labelledby' or 'aria-describedby', but this will slow down the whole performance
					//https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-labelledby_attribute
					//https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-describedby_attribute
					//the label's id could be in the value of attribute 'aria-labelledby' or 'aria-describedby' of the target-component
//					String labelID = anchorElement.getAttribute(HTMLConst.ATTRIBUTE_ID);
//					if(labelID!=null){
////						elements = findElementsInParent(anchorElement, XPATH.relativeDescendants(ARIA.LABELLEDBY(labelID)));
//						elements = selenium.findElementsByXPath(XPATH.allDescendants(ARIA.LABELLEDBY(labelID)));
//						//they are potential targets, we verify the element's type,  add it to the results if "matched".
//						if(elements!=null){
//							for(WebElement e: elements){
//								if(matched(e, type)) results.add(e);
//							}
//						}
//					}

					//Find elements by types XPATH.
					elements = findElementsInParent(anchorElement, xpath);
					if(elements!=null && !elements.isEmpty()) results.addAll(elements);
				}
			}
		}

		return results;
	}

	/**
	 * Find the first WebElement whose text matches the provided text.<br>
	 * The search will be tried on each frame until we find the first matched element.<br>
	 *
	 * <b>Note:</b>
	 * <ol>
	 * <li>Once this method is done, we should have switched to a certain frame.</b>
	 * <li>If substring matches are enabled, then we will also attempt partial substring matches.<br>
	 * <li>If the search is slow, this might be caused by the timeout, we can accelerate by calling {@link AIMiscActions#do_not_wait_if_a_component_is_not_ready()}.
	 * </ol>
	 *
	 * @param text String
	 * @return WebElement matching requested text or null if not found.
	 * @see AIMiscActions#accept_partial_text_matches()
	 * @see AIMiscActions#deny_partial_text_matches()
	 * @see AIMiscActions#do_not_wait_if_a_component_is_not_ready()
	 */
	public WebElement findSelectableItemFromText(String text) {
		String dbgmsg = StringUtils.debugmsg(false);
		WebElement result = null;
		List<WebElement> elements = findSelectableItemsFromText(text);
		if(elements==null||elements.isEmpty()){
			IndependantLog.info(dbgmsg+" found no elements matching text '"+ text +"'");
		}else{
			result = elements.get(0);
			int count = elements.size();
			if (count == 1){
				IndependantLog.info(dbgmsg+" returning only element matching text '"+ text +"'");
			}else{
				IndependantLog.info(dbgmsg+" retrieving first of "+ count +" elements matching text '"+ text +"'");
			}
		}

		return result;
	}

	/**
	 * Find all WebElements whose text matches the provided text.<br>
	 * The search will be tried on each frame until we find the first matched elements.<br>
	 *
	 * <b>Note:</b>
	 * <ol>
	 * <li>Once this method is done, we should have switched to a certain frame.</b>
	 * <li>If substring matches are enabled, then we will also attempt partial substring matches.<br>
	 * <li>If the search is slow, this might be caused by the timeout, we can accelerate by calling {@link AIMiscActions#do_not_wait_if_a_component_is_not_ready()}.
	 * </ol>
	 *
	 * @param text String, to match
	 * @return List&lt;WebElement> elements matching requested text or empty if not found.
	 * @see AIMiscActions#accept_partial_text_matches()
	 * @see AIMiscActions#deny_partial_text_matches()
	 * @see AIMiscActions#do_not_wait_if_a_component_is_not_ready()
	 * @see #initializeFrames(WebDriver)
	 */
	private List<WebElement> findSelectableItemsFromText(String text) {
		RemoteDriver selenium = (RemoteDriver) WDLibrary.getWebDriver();
		List<WebElement> results = null;

		if(WDLibrary.getBypassFramesReset()){
			//We should focus on a certain frame context
			results = _findSelectableItems(text);
		}else{

			initializeFrames(selenium);
			for(Arbre<FrameElement> frame: frames){
				WDLibrary.switchToFrame(frame, selenium);
				results = _findSelectableItems(text);
				if(!results.isEmpty()){
					break;
				}
			}
		}

		return results;
	}

	/**
	 * Suppose that we are in the correct frame.<br>
	 * Find all WebElements whose text matches the provided text.<br>
	 * <b>Note:</b>
	 * <ol>
	 * <li>If substring matches are enabled, then we will also attempt partial substring matches.<br>
	 * <li>If the search is slow, this might be caused by the timeout, we can accelerate by calling {@link AIMiscActions#do_not_wait_if_a_component_is_not_ready()}.
	 * </ol>
	 * @param text String
	 * @return List&lt;WebElement> elements matching requested text or empty if not found.
	 * @see AIMiscActions#accept_partial_text_matches()
	 * @see AIMiscActions#deny_partial_text_matches()
	 * @see AIMiscActions#do_not_wait_if_a_component_is_not_ready()
	 */
	private List<WebElement> _findSelectableItems(String text) {
		String dbgmsg = StringUtils.debugmsg(false);
		/*
		 * Find all items accepting selection or input with the displayed text
		 * "//*[text()= '"+ text +"']" and "//*[.='"+ text +"']" are different, see below link
		 * https://stackoverflow.com/questions/38240763/xpath-difference-between-dot-and-text
		 */
		RemoteDriver selenium = (RemoteDriver) WDLibrary.getWebDriver();
		List<WebElement> results = new ArrayList<WebElement>();
		List<WebElement> elements = null;

		try{
			elements = selenium.findElements(By.xpath("//*[text()= '"+ text +"']"));
			if(elements==null||elements.isEmpty()){
				IndependantLog.info(dbgmsg+" found no elements text()= '"+ text +"'");
			}else{
				results.addAll(elements);
				if(_log_details) logListElementsInfo(elements);
				IndependantLog.info(dbgmsg+" found "+ elements.size() +" elements text()= '"+ text +"'");
			}
		}catch(Exception e){
			IndependantLog.warn(dbgmsg+" found no elements text()= '"+ text +"', met "+e);
		}

		try{
			elements = selenium.findElements(By.xpath("//*[.='"+ text +"']"));
			if(elements==null||elements.isEmpty()){
				IndependantLog.info(dbgmsg+" found no elements .= '"+ text +"'");
			}else{
				addNonRepeatedElements(results, elements);
				if(_log_details) logListElementsInfo(elements);
				IndependantLog.info(dbgmsg+" found "+ elements.size() +" elements .= '"+ text +"'");
			}
		}catch(Exception e){
			IndependantLog.warn(dbgmsg+" found no elements .= '"+ text +"', met "+e);
		}

		if(_substring_matches_allowed){
			try{
				elements = selenium.findElements(By.xpath("//*[contains(text(), '"+ text +"')]"));
				if(elements==null||elements.isEmpty()){
					IndependantLog.info(dbgmsg+" found no elements contains(text(), '"+ text +"')");
				}else{
					addNonRepeatedElements(results, elements);
					if(_log_details) logListElementsInfo(elements);
					IndependantLog.info(dbgmsg+" found "+ elements.size() +" elements contains(text(), '"+ text +"')");
				}
			}catch(Exception e){
				IndependantLog.warn(dbgmsg+" found no elements contains(text(), '"+ text +"'), met "+e);
			}
		}

		return results;
	}

	/**
	 * Find the non-repeated elements from the parameter 'elements' and add them into 'results'.<br>
	 * Currently we compare the web-element's id, if 2 elements have the same ID, we consider them same.<br>
	 *
	 * @param results List&lt;WebElement>, the destination list into which we add elements
	 * @param elements List&lt;WebElement>, the list of elements to be added
	 * @return List&lt;WebElement>, the result list
	 */
	public static List<WebElement> addNonRepeatedElements(List<WebElement> results, List<WebElement> elements){
		Map<String, WebElement> idToElementMap = new HashMap<String, WebElement>();
		String id = null;
		if(results==null) results = new ArrayList<WebElement>();
		for(WebElement e: results){
			id = e.getAttribute(HTMLConst.ATTRIBUTE_ID);
			if(id!=null) idToElementMap.put(id, e);
		}
		if(elements!=null){
			for(WebElement e: elements){
				id = e.getAttribute(HTMLConst.ATTRIBUTE_ID);
				if(id==null| idToElementMap.get(id)==null) results.add(e);
			}
		}

		return results;
	}


	/**
	 * Deduce the absolute full path test-relative file.
	 * @param filename, String, the test/actual file name.  If there are any File.separators in the
	 * relative path then the path is actually considered relative to the Datapool
	 * directory unless it does not exist, or is already an absolute file path.
	 * <p>
	 * If a relative directory path does not exist relative to the Datapool directory then
	 * the final path will be relative to the Project directory.
	 * <p>
	 * If it is an absolute path, and contains a root path that includes the Bench directory, then the
	 * file will be converted to a comparable relative path off the Test directory.
	 * <p>
	 * @return File, the absolute full path test file.
	 * @throws SAFSException
	 * @see FileUtilities#deduceFile(String, int, RuntimeDataInterface)
	 */
	protected File deduceTestFile(String filename) throws SAFSException{
		return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_TEST, iDriver().getCoreInterface());
	}

	/**
	 * Deduce the absolute full path Diff-relative file.
	 * @param filename, String, the diff file name.  If there are any File.separators in the
	 * relative path then the path is actually considered relative to the Datapool
	 * directory unless it does not exist, or is already an absolute file path.
	 * <p>
	 * If a relative directory path does not exist relative to the Datapool directory then
	 * the final path will be relative to the Project directory.
	 * <p>
	 * If it is an absolute path, and contains a root path that includes the Bench directory, then the
	 * file will be converted to a comparable relative path off the Diff directory.
	 * <p>
	 * @return File, the absolute full path diff file.
	 * @throws SAFSException
	 * @see FileUtilities#deduceFile(String, int, RuntimeDataInterface)
	 */
	protected File deduceDiffFile(String filename) throws SAFSException{
		return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_DIFF, iDriver().getCoreInterface());
	}

	/**
	 * Deduce the absolute full path bench-relative file.
	 * @param filename, String, the test file name.  If there are any File.separators in the
	 * relative path then the path is actually considered relative to the Datapool
	 * directory unless it does not exist, or is already an absolute file path.
	 * If a relative directory path does not exist relative to the Datapool directory then
	 * the final path will be relative to the Project directory.
	 * @return File, the absolute full path bench file.
	 * @throws SAFSException
	 * @see FileUtilities#deduceFile(String, int, RuntimeDataInterface)
	 */
	protected File deduceBenchFile(String filename) throws SAFSException{
		return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_BENCH, iDriver().getCoreInterface());
	}

	/**
	 * Deduce the absolute full path to a project-relative file.
	 * @param filename, String, the test file name.  The path is ALWAYS considered relative
	 * to the project root directory regardless of the absence or presence of File.separators
	 * unless the file is already an absolute path.
	 * @return File, the absolute full path bench file.
	 * @throws SAFSException
	 * @see FileUtilities#deduceFile(String, int, RuntimeDataInterface)
	 */
	protected File deduceProjectFile(String filename) throws SAFSException{
		return FileUtilities.deduceFile(filename, FileUtilities.FILE_TYPE_PROJECT, iDriver().getCoreInterface());
	}

	/**
	 * Get the text of each element object in an array, and add it to a list, then return the list.
	 * @param elements Element[], an array of element object
	 * @return List<String>, a list of element's text
	 */
	public static List<String> convertElementArrayToList(Element[] elements){
		return WDLibrary.convertElementArrayToList(elements);
	}

	/** <b>--glue</b> the option for cucumber runtime builder.  */
	public static final String GLUE_OPT = "--glue";
	/**
	 * <b>org.safs.cukes.ai.selenium</b> The default package containing the step definitions.<br>
	 */
	public static final String DEFAULT_SEPLUS_AI_DEFINITIONS = "org.safs.cukes.ai.selenium";

	/**
	 * Starts a Cucumber Runtime and executes Cucumber test(s) with the default step definitions in package {@link #DEFAULT_SEPLUS_AI_DEFINITIONS}.
	 * Examples:
	 * <ul>
	 * <li>Using the default Step Definition files defined in {@link #DEFAULT_SEPLUS_AI_DEFINITIONS},
	 * and locates the single AltSearchAI.feature file to runs that test in Cucumber.
	 * <br>
	 * <b>runCukesTest("./Tests/td/testcases/features/AltSearchAI.feature");</b>
	 * <li>Using the default Step Definition files defined in {@link #DEFAULT_SEPLUS_AI_DEFINITIONS},
	 * and locates the ./resources/features directory to runs tests based on the feature files found there.
	 * <br>
	 * <b>runCukesTest("./resources/features");</b>
	 * </ul>
	 * @param features String, the path to the directory of feature files, or the specific feature file to run.
	 * @throws Throwable
	 */
	public static void runCukesTest(String features) throws Throwable {
		String[] glues = {DEFAULT_SEPLUS_AI_DEFINITIONS};//the default selenium plus AI definitions
		runCukesTest(features, glues);
	}

	/**
	 * Starts a Cucumber Runtime and executes Cucumber test(s) with the glue and feature file specs provided.
	 * <p>
	 * Examples:
	 * <ul><p><ul>
	 * runCukesTest( "td", "./Tests/td/testcases/features/AltSearchAI.feature");
	 * </ul>
	 * Searches the "td" Java package and all subpackages for usable Step Definition files.<br>
	 * Locates the single AltSearchAI.feature file and runs that test in Cucumber.
	 * <p><ul>
	 * runCukesTest( "org.mine.steps", "./resources/features");
	 * </ul>
	 * Searches the "org.mine.steps" Java package and all subpackages for usable Step Definition files.<br>
	 * Locates the ./resources/features directory and runs tests based on the feature files found there.
	 * <p></ul>
	 * Step Definition files are sought using the calling Thread's context ClassLoader.
	 * @param glue String, the Java package root containing the Java Step Definition files to use.
	 * @param features String, the path to the directory of feature files, or the specific feature file to run.
	 * @throws Throwable
	 * @see {@link java.lang.Thread#getContextClassLoader()}
	 */
	public static void runCukesTest(String glue, String features) throws Throwable {
		String[] glues = new String[1];
		glues[0] = glue;//the Java package root containing your Step Definition files
		runCukesTest(features, glues);
	}

	/**
	 * Starts a Cucumber Runtime and executes Cucumber test(s) with the glues and feature file specs provided.
	 * <p>
	 * Examples:
	 * <ul><p><ul>
	 * runCukesTest("./Tests/td/testcases/features/AltSearchAI.feature", "td", "org.mine.steps");
	 * </ul>
	 * Searches the "td" and "org.mine.steps" Java packages and all subpackages for usable Step Definition files.<br>
	 * Locates the single AltSearchAI.feature file and runs that test in Cucumber.
	 * <p><ul>
	 * runCukesTest("./resources/features", "td", "org.mine.steps");
	 * </ul>
	 * Searches the "td" and "org.mine.steps" Java packages and all subpackages for usable Step Definition files.<br>
	 * Locates the ./resources/features directory and runs tests based on the feature files found there.
	 * <p></ul>
	 * Step Definition files are sought using the calling Thread's context ClassLoader.
	 * @param features String, the path to the directory of feature files, or the specific feature file to run.
	 * @param glues String[], the Java packages containing the Java Step Definition files to use.
	 * @throws Throwable
	 * @see {@link java.lang.Thread#getContextClassLoader()}
	 */
	public static void runCukesTest(String features, String... glues) throws Throwable {
		List<String> parameters = new ArrayList<String>();

		//Add the glues
		if(glues!=null){
			for(String glue:glues){
				parameters.add(GLUE_OPT);
				parameters.add(glue);// the Java package root containing your Step Definition files
			}
		}

		//Add the features
		parameters.add(features);//the location of your Gherkin Feature file(s)

		final Runtime runtime = Runtime.builder().withArgs(parameters.toArray(new String[0]))
				.withClassLoader(Thread.currentThread().getContextClassLoader())
				.build();

		runtime.run();
	}

	/**
	 * Currently does nothing.  Subclasses can override to provide unit tests, etc...
	 */
	@Override
	public void runTest() throws Throwable {}
	/**
	 * Invokes SeleniumPlus.main(args) when called by the JVM as the startup class.
	 * @param args
	 */
	public static void main(String[] args) { SeleniumPlus.main(args); }

}
