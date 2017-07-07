package org.safs.selenium;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.selenium.spc.SPCTreeNode;
import org.safs.selenium.spc.SPCUtilities;
import org.safs.selenium.util.HtmlFrameComp;
import org.xml.sax.InputSource;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

/**
 * <em>Purpose:</em>
 * Provide the ability to make conversion between XPATH and SAFS Recognition String<br>
 *
 * <em>Note:</em>
 * To achieve this purpose, Document contained in an HTML page is parsed. <br>
 * We take advantage of API provided by DOM4J. <br>
 *
 * 1. Html parser: Parse HTML page and convert it to W3C-XML.<br>
 * NekoParser(opensource)     http://sourceforge.net/projects/nekohtml/  <br>
 *                            version 1.9.14   nekohtml.jar<br>
 *
 * 2. XML-DOM parser: Parse W3C-XML.<br>
 * DOM4J(opensource)          http://www.dom4j.org/dom4j-1.6.1/<br>
 *                            dom4j-2.0.0-ALPHA-2.jar <br>
 *                            NOTE: so far found another jar needed if using XPATH search<br>
 *                            jaxen-1.1.1.jar (an open source XPath library) at http://jaxen.org/releases.html<br>
 *
 * @author sbjjum	Feb 18, 2011	Initial creation.
 * @author sbjlwa   Mar 18, 2011    Implement methods to manipulate dom4j's document, node, element, attribute etc.<br>
 *                                  Implement methods to operate on xpath, which are used by SELENIUM-SPC. These methods<br>
 *                                  have been implemented in user-extensions.js, here I just make a conversion.<br>
 * @author sbjlwa   Jun 07, 2011    Modify methods generateAttributRS(), getAttribute() and getIndex().<br>
 * @author sbjlwa   Jun 28, 2011    Add method getAttributes().<br>
 * @author sbjlwa   Aug 10, 2012    Rename getRobotRecognition() to getRobotRecognitionNode(): return a SPCTreeNode
 * 									containing "RS", "id", and "name" for a html element.<br>
 *
 */
public class DocumentParser {

	/**
	 * documents is a map-cache to contain dom4j-document as value and URL as key.
	 */
	public HashMap<String, org.dom4j.Document> documents = new HashMap<String, org.dom4j.Document>();
	public String url = null;
	Selenium selenium = null;
	SeleniumGUIUtilities util = null;

	public static final String XPATH_ALL_LEVEL_PREFIX = "//";
	public static final String XPATH_ALL_ELEMENTS = "//*";
	public static final String XPATH_ATTRIBUTES = "@";

	public static final String BOUDNS_SEPARATOR = "#";
	public static final String ASSIGN_SEPARATOR = "=";

	public static final String RECOGNITION_LEVEL_SEPARATOR = ";\\;";

	private static AtomicInteger nextIndex = null;

	private boolean interrupt = false;

	public int timeconsume1 = 0;
	public int timeconsume2 = 0;
	public int timeconsume3 = 0;

	public DocumentParser(String url) {
		setHTTPProxy();
		this.util = new SeleniumGUIUtilities();
		setDocument(url, null, true);
	}

	public DocumentParser(Selenium selenium, SeleniumGUIUtilities util) {
		this.selenium = selenium;
		this.util = util;
		nextIndex = new AtomicInteger(0);
		setDocument(selenium.getLocation(), selenium.getHtmlSource(), true);
	}

	/**
	 * When we call getAllElements(), if the html contains frame, we will try to
	 * get html content with aid of API selenium.open(frameURL); selenium.getHtmlSource().
	 * But selenium will show this framePage on browser, we need to change back to the
	 * main page by calling selenium.open(this.url);
	 * isMainPage is used to test if the browser contains the main page.
	 *
	 * @return true if the main page is shown in the browser; false if other frame page.
	 */
	public boolean isMainPage() {
		boolean mainpage = false;
		if (selenium != null && url != null) {
			mainpage = url.equals(selenium.getLocation());
		}
		return mainpage;
	}

	public String getBoundsSeparator() {
		return BOUDNS_SEPARATOR;
	}

	public void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}

	/**
	 * This is needed when we read the content from an URL<br>
	 */
	public void setHTTPProxy() {
		//These parameters can be passed in by JVM parameter
		//-Dhttp.proxyHost=your.proxy.host
		//-Dhttp.proxyPort=80
		System.setProperty("http.proxyHost", "your.proxy.host");
		System.setProperty("http.proxyPort", "80");
	}

	/**
	 * @return	the url string corresponding the main page
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url				a url string
	 * @param changeMainPageURL	true if the main page url needs to be changed
	 * @return					a dom4j document
	 */
	public Document getDocument(String url, boolean changeMainPageURL) {
		Document doc = documents.get(url);

		if (doc == null) {
			doc = setDocument(url, null, changeMainPageURL);
		}

		return doc;
	}

	/**
	 * <em>Purpose</em>			Get dom4j-DOM according to html-content<br>
	 *                          If the html-content is null, then try to get it according to<br>
	 *                          the urlString.<br>
	 *                          The method {@link #setHTTPProxy()} may needs to be called before calling this method.<br>
	 * @param urlString			From where to get the html-content<br>
	 * @param htmlSource		The html content to be converted to dom4j-DOM<br>
	 * @param changeMainPageURL	true if the main page url needs to be changed<br>
	 * @return                  The dom4j-DOM will be returned.<br>
	 */
	public Document setDocument(String urlString, String htmlSource, boolean changeMainPageURL) {
		Document doc4j = null;
		InputSource source = null;
		InputStream ins = null;

		try {
			// If we try to set Document of a Frame, changeMainPageURL should be false
			if (changeMainPageURL) {
				this.url = urlString;
			}

			if (documents.get(urlString) != null) {
				Log.debug("url: " + urlString + "; its dom4j's Document already in cache.");
				return documents.get(urlString);
			} else {
				Log.debug("url: " + urlString + "; try to get its dom4j's Document and put it in cache.");
			}

			if (htmlSource == null) {
				//If the url's content contains <HTML xmlns="http://www.w3.org/TR/REC-html40">, error occurs
				//See defect at http://internal.server/defects/java/iDefects/WebClient.html?defectid=S0743322
				//There are two approaches to get the content from an URL

				//1. Remove the beginning and ending HTML tag from html source.
				//We read the source from URL ourself, we must handle the Encoding, NOT ideal
//				URL aURL = new URL(urlString);
//				URLConnection conn = aURL.openConnection();
//				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.17) ");
//				//we need to know the encoding of the stream from url
////				ins = aURL.openStream();
//				ins = conn.getInputStream();
//				String streamEncoding = getEncodingName(ins);
//				//As in getEncodingName(), we have consumed some bytes, maybe there are useful
//				//So we re-read the inputstream to a StringBuffer
//				ins.close();
////				ins = aURL.openStream();
//				conn = aURL.openConnection();
//				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.17) ");
//				ins = conn.getInputStream();
//				StringBuffer sb = new StringBuffer();
//				BufferedReader reader = null; reader = new BufferedReader(new InputStreamReader(ins,streamEncoding));
//				String tmp = reader.readLine();
//				while(tmp!=null){
//					sb.append(tmp);
//					tmp = reader.readLine();
//				}
//				//Get rid of the beginning and ending HTML tag.
//				htmlSource = getInnerHtml(sb.toString(),"HTML");


				// 2. Let selenium to handle the source, we load the url to current page,
				// and the browser will handle the encodings
				// and we are just happy to get the html source via selenium
				// But the current page will change, we need to set it back to the first page.
				selenium.open(urlString);
				selenium.waitForPageToLoad("1000");
				htmlSource = selenium.getHtmlSource();

				// If two url differ with the letter-case, Do we consider them same???
				if (!url.equals(urlString)) {
					selenium.open(url);
				}
			}

			Log.debug("Html Source: "+htmlSource);
			if (htmlSource == null) {
				String errorMsg = "Can NOT parse URL:" + urlString + " ; Its content is null.";
				Log.warn(errorMsg);
				throw new SAFSException(errorMsg);
			}

			source = new InputSource(new ByteArrayInputStream(htmlSource.getBytes()));

			// Use Neko-DomParser to parse html-page-content and get a W3C-DOM
			DOMParser parser = new DOMParser();
			parser.parse(source);
			org.w3c.dom.Document w3cDoc = parser.getDocument();

			// Use dom4j to translate W3C-DOM to dom4j-DOM
			DOMReader domReader = new DOMReader();
			doc4j = domReader.read(w3cDoc);
			documents.put(urlString, doc4j);
		} catch (Exception e) {
			Log.error("DocumentParser: Exception " + e.getMessage());
		} finally {
			try {
				if (ins != null)
					ins.close();
			} catch (Exception e) {
				Log.warn(e.getMessage());
			}
		}

		return doc4j;
	}

	/**
	 * <em>Note:</em> To detect the encoding of the input-stream
	 *
	 * @param ins            the input-stream to be parsed
	 * @return               the encoding-name of the input-stream
	 * @throws IOException
	 */
	public String getEncodingName(InputStream ins) throws IOException {
		String streamEncoding = "ISO-8859-1";

		int first = ins.read();
		int second = ins.read();
		int third = -1;
		int fourth = -1;

		if (first == 0XFF) {
			if (second == 0XFE) {
				streamEncoding = "UTF-16LE";
				third = ins.read();
				fourth = ins.read();
				if (third == 0X00 && fourth == 0X00) {
					streamEncoding = "UTF-32LE";
				}
			}
		} else if (first == 0XFE) {
			if (second == 0XFF) {
				streamEncoding = "UTF-16BE";
			}
		} else if (first == 0XEF) {
			if (second == 0XBB) {
				third = ins.read();
				if (third == 0XBF)
					streamEncoding = "UTF-8";
			}
		} else if (first == 0X00) {
			third = ins.read();
			fourth = ins.read();
			if (second == 0X00 && third == 0XFE && fourth == 0XFF) {
				streamEncoding = "UTF-32BE";
			}
		}

		Log.debug("file encoding=" + streamEncoding);

		// We don't need to close the ins, let the caller to handle it.

		return streamEncoding;
	}

	/**
	 * <em>Purpose: </em> get the innerHTML from page content<br>
	 * <em>Note:</em> The tag must be the beginning and ending tag of the content<br>
	 *
	 * @param content from which to get innerHTML
	 * @param tag     to which the innerHTML belongs
	 * @return        the innerHTML content of tag
	 */
	public String getInnerHtml(String content, String tag) {
		String matchedString = null;
		String innerHtmlPatternStr = "([^<]*<" + tag + "[^>]*>[^<]*(<.*>).*</" + tag + ">.*)";

		Pattern innerHtmlPattern = Pattern.compile(innerHtmlPatternStr, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher matchedResult = innerHtmlPattern.matcher(content);

		if (matchedResult.find() && matchedResult.groupCount() > 1) {
			matchedString = matchedResult.group(2);
			Log.debug("innerHTML is: " + matchedString);
		}

		return matchedString;
	}

	/**
	 *
	 * @return	An array of XPATH for all elements in a page<br>
	 *          if the page contains frames, the elements in those<br>
	 *          frames will be returned also.<br>
	 */
	public String[] getAllElements() {
		interrupt = false;
		Document doc = getDocument(url, false);
		List<String> elementsXpathList = new ArrayList<String>();
		getAllElementsR(doc, "", 0, 0, url, elementsXpathList);
		if (interrupt) {
			elementsXpathList.add("INTERRUPTED");
		}

		// Here we may need to set the main URL to the current SELENIUM page.
		// Because when we traverse the DOM, if we meet Frame, we will load that
		// frame to the current page to get it HTML-content for analyze
		// goBackToMainPage();

		return elementsXpathList.toArray(new String[0]);
	}

	/**
	 * <em>Note:</em>	This is a recursive method, for each frame in this document, this method will be called.
	 *
	 * @param doc                   From where to get nodes
	 * @param prefix                The prefix to be added before node's xpath
	 * @param top                   The y-coordination of the top-left point of doc
	 * @param left                  The x-coordination of the top-left point of doc
	 * @param docURL                The url representing the doc
	 * @param elementsXpathList		A list contains all xpath, and it will be returned.
	 */
	@SuppressWarnings("unchecked")
	public void getAllElementsR(Document doc, String prefix, int top, int left,
			String docURL, List<String> elementsXpathList) {
		List elements = new ArrayList();
		Node node = null;
		String xpath = null;
		List<Node> frameList = new ArrayList<Node>();
		List<String> frameXpathList = new ArrayList<String>();
		String frameXpath = null;
		String elementId = null;
		String elementNmae = null;

		if (doc != null) {
			elements = doc.selectNodes(XPATH_ALL_ELEMENTS);
		} else {
			Log.debug("For URL: " + docURL + " , it document is null, can NOT get elements");
		}

		Log.debug(" searching " + doc + " at " + top + "," + left);

		for (int i = 0; i < elements.size(); i++) {
			if (interrupt == true) {
				return;
			}

			node = (Node) elements.get(i);

			String bounds = "0" + BOUDNS_SEPARATOR + "0" + BOUDNS_SEPARATOR + "0" + BOUDNS_SEPARATOR + "0";
			String theTag = node.getName();

			if (theTag.equalsIgnoreCase("HTML")
					|| theTag.equalsIgnoreCase("HEAD")
					|| theTag.equalsIgnoreCase("META")
					|| theTag.equalsIgnoreCase("TITLE")
					|| theTag.equalsIgnoreCase("SCRIPT")) {
				// retain default bounds
			} else {
				//TODO implement getCompBounds()
				// bounds = getCompBounds(node,top,left);
			}
			xpath = getNodeXPath(node, prefix) + BOUDNS_SEPARATOR + bounds;

			//TODO we can return some properties value at the same time
//			if(node instanceof Element){
//				elementId = "id"+ASSIGN_SEPARATOR+((Element) node).attributeValue("id");
//				elementNmae = "name"+ASSIGN_SEPARATOR+((Element) node).attributeValue("name");
//				xpath += BOUDNS_SEPARATOR + elementId + BOUDNS_SEPARATOR + elementNmae;
//			}

			// Log.debug("xpath = "+xpath);
			if (!xpath.equals("")) {
				elementsXpathList.add(xpath);
			}

			// Here we store the frame node in frameList; store its xpath in framesXpath
			if (theTag.equalsIgnoreCase("FRAME") || theTag.equalsIgnoreCase("IFRAME")) {
				frameXpath = xpath.substring(0,xpath.indexOf(BOUDNS_SEPARATOR));
				frameList.add(node);
				frameXpathList.add(frameXpath);
				Log.debug("frame added: "+ frameXpath);
			}
		}

		Log.debug("appending all frame xpaths...");
		String frameSrcURL = null;
		Document frameContentDoc = null;
		for (int i = 0; i < frameList.size(); i++) {
			node = frameList.get(i);
			frameSrcURL = getFrameSrcURL(node, docURL);
			if(frameSrcURL.equals(docURL)){
				Log.warn("Frame '"+getNodeXPath(node, "")+"' does NOT contain any content.");
				break;
			}
			frameContentDoc = getDocument(frameSrcURL, false);
			getAllElementsR(frameContentDoc, frameXpathList.get(i),getFrameTop(node), getFrameLeft(node), frameSrcURL,elementsXpathList);
		}

	}

	/**
	 *
	 * @param document   In this document, the xpath will be searched.
	 * @param xpath      The xpath representing a frame
	 * @param parentURL
	 *            The url where the frame resides; the first parameter document<br>
	 *            is the document-content of this url.<br>
	 *            Why need this redundant <br>
	 *            parameter? Because, if the frame's src is relative, we need to
	 *            append<br>
	 *            it to the end of this parentURL to form an absolute one.<br>
	 * @return An absolute url of the frame's src
	 */
	public String getFrameSrcURL(Document document, String xpath, String parentURL) {
		// Get the frame element according to the xpath
		Element element = getElementFromXpath(document, xpath);
		// Get the frame's src and return
		return getFrameSrcURL(element, parentURL);
	}

	/**
	 *
	 * @param frameNode From where to get the value of attribute 'src'
	 * @param parentURL If the attribute 'src' of the frameNode contains a relative<br>
	 *                  url, the parentURL will be added in front<br>
	 * @return an absolute url indicated by attribute 'src' of the frameNode
	 */
	public String getFrameSrcURL(Node frameNode, String parentURL) {
		String src = getAttribute(frameNode, "src");
		String fullURL = null;

		if (src != null) {
			src = src.trim();
			String upCaseSrc = src.toUpperCase();
			// TODO is it relative or absolute URL? How to test, begin with "http:" , "file:", others ???
			// If it is relative url, need append it to parentURL to generate an absolute one.
			if (!(upCaseSrc.startsWith("HTTP:") || upCaseSrc.startsWith("FILE:"))) {
				if(upCaseSrc.startsWith("/")){
					//TODO, If the src begin with '/', then it should be append to the web site's root
					//NOT to the parentURL, how we get the web site's root url???
					Log.warn("Need to append to web site's root to form an absolute URL. NOT implemented.");
				}else{
					fullURL = normalizeURL(parentURL) + src;
				}
			}
		}

		return fullURL;
	}

	//If url does not end with "/", append it.
	public String normalizeURL(String url) {
		String normalURL = url;

		if (url != null) {
			url = url.trim();
			if (!url.endsWith("/")) {
				normalURL = url + "/";
			}
		}

		return normalURL;
	}

	public int getFrameTop(Node frameNode) {
		int top = 0;

		try {
			top = Integer.parseInt(this.getAttribute(frameNode, "s_top"));
		} catch (Exception e) {
			Log.error(e.getMessage());
		}

		return top;
	}

	public int getFrameLeft(Node frameNode) {
		int left = 0;

		try {
			left = Integer.parseInt(this.getAttribute(frameNode, "s_left"));
		} catch (Exception e) {
			Log.error(e.getMessage());
		}

		return left;
	}

	/**
	 * <em>Purpose:</em>  Get all properties of an element on html page.
	 * @param url         The url representing the html page.
	 * @param xpath       The xpath representing the element.
	 * @return
	 */
	public HashMap getAttributes(String url, String xpath) {
		Document document = getDocument(url, false);

		return this.getAttributes(document, xpath);
	}

	/**
	 * <em>Purpose:</em>  Get all properties of an element on html page.
	 * @param doc         Dom4j object Document, represent the document of html page.
	 * @param xpath       The xpath representing the element.
	 * @return
	 */
	public HashMap getAttributes(Document doc, String xpath) {
		String debugmsg = getClass().getName() + ".getAttributes(): ";
		HashMap value = null;
		Element element = getElementFromXpath(doc, xpath);
		Log.debug(debugmsg + " get attributes for element '" + xpath+"'. ");
		value = getAttributes(element);
		Log.debug(debugmsg + " attributes=" + value);

		return value;
	}

	/**
	 * <em>Purpose:</em>  Get all properties of an element on html page.
	 * @param node        Dom4j object Node,representing the element on html page.
	 * @return
	 */
	public HashMap getAttributes(Node node) {
		String debugmsg = getClass().getName()+".getAttributes(): ";
		Element element = null;
		List attributes = null;
		Attribute attr = null;
		HashMap value = new HashMap();

		if (node == null ) {
			Log.error(debugmsg+"node is null, can't process!!!");
			return value;
		}

		// Try to get the attribute via dom4j's API
		if (node instanceof Element) {
			Log.debug(debugmsg+"Trying DOM4J API to get attributes.");
			element = (Element) node;

			attributes = element.attributes();
			for (int i = 0; i < attributes.size(); i++) {
				attr = (Attribute)attributes.get(i);
				value.put(attr.getName(), attr.getValue());
			}
		}else{
			Log.debug(debugmsg+" node is not Element, its type is "+node.getNodeType());
			Log.debug(debugmsg+" need new implementation to get attributes.");
		}

		return value;
	}

	/**
	 * <em>Purpose:</em>  Get the value of an attribute
	 * @param url         The url representing the html page.
	 * @param xpath       An xpath representing an element on the web page.
	 * @param attribute   The attribute's name.
	 * @return
	 */
	public String getAttribute(String url, String xpath, String attribute) {
		Document document = getDocument(url, false);

		return this.getAttribute(document, xpath, attribute);
	}

	/**
	 * <em>Purpose:</em>  Get the value of an attribute
	 * @param doc         A DOM4J Document (org.dom4j.Document)
	 * @param xpath       An xpath representing an element on the web page.
	 * @param attribute   The attribute's name.
	 * @return            The value of an attribute
	 */
	public String getAttribute(Document doc, String xpath, String attribute) {
		String debugmsg = getClass().getName() + ".getAttribute(): ";
		String value = null;
		Element element = getElementFromXpath(doc, xpath);
		value = getAttribute(element, attribute);
		Log.debug(debugmsg + " attribute=" + attribute + "; value=" + value);

		return value;
	}

	/**
	 * <em>Purpose:</em>  Get the value of an attribute
	 * @param node        A DOM4J Node (org.dom4j.Node)
	 * @param attribute   The attribute's name.
	 * @return            The value of an attribute
	 */
	public String getAttribute(Node node, String attribute) {
		String debugmsg = getClass().getName()+".getAttribute(): ";
		Element element = null;
		String value = null;
		List attributes = null;
		Attribute attr = null;

		if (node == null || attribute == null) {
			Log.error(debugmsg+"node or attribute is null, can't process!!!");
			return value;
		} else {
			attribute = attribute.trim();
		}
		Log.debug(debugmsg+"Getting value for attribute "+attribute);

		// Try to get the attribute via dom4j's API
		if (node instanceof Element) {
			Log.debug(debugmsg+"Trying DOM4J API to get attribute.");
			element = (Element) node;
			value = element.attributeValue(attribute);

			// Try to get the attribute from the list of attribute
			if (value == null || "".equals(value)) {
				attributes = element.attributes();
				for (int i = 0; i < attributes.size(); i++) {
					attr = (Attribute)attributes.get(i);
					if (attr.getName().equalsIgnoreCase(attribute)) {
						value = attr.getValue();
						break;
					}
				}
			}
		}

		// Try to get the attribute via selenium's API
		if (value == null || "".equals(value)) {
			String attributeLocator = getNodeXPath(node, "");
			//For selenium, the xpath must start with "//" so that is can be processed.
			attributeLocator = SeleniumGUIUtilities.normalizeXPath(attributeLocator);

			if (attributeLocator.endsWith("/")) {
				attributeLocator = attributeLocator.substring(0,attributeLocator.length() - 1);
				attributeLocator += "@" + attribute;
			} else {
				attributeLocator += "@" + attribute;
			}
			Log.debug(debugmsg+"Trying Selenium API to get attribute; attributeLocator=" + attributeLocator);

			try {
				// TODO this require that the current page is the correct URL page
				value = selenium.getAttribute(attributeLocator);
			} catch (Exception e) {
				Log.error(debugmsg+e.getMessage());
			}
		}

		Log.debug(debugmsg+"For "+attribute + ", its value is " + value);
		return value;
	}

	public void goBackToMainPage() {
		if (!isMainPage()) {
			selenium.open(url);
		}
	}

	public String getNodeXPath(Node node, String prefix) {
		String xpath = prefix + node.getUniquePath();
		return xpath;
	}

	/**
	 * According to a xpath, return SPCTreeNode containig
	 * "SAFS recognition string", Html element's id, Html element's name
	 *
	 * @param xpath
	 * @param withName
	 *            if true, a generated component name will be prefix of recognition string<br>
	 *            Ex, "ButtonInput1=" will be put ahead of recognition string.<br>
	 *            otherwise, only the recognition string will be returned.<br>
	 * @return
	 * AUG 10, 2012		(SBJLWA) Use SPCTreeNode to contain "recognition string", element's id and element's name.
	 *                           Return SPCTreeNode as result.
	 */
	public SPCTreeNode getRobotRecognitionNode(String xpath, boolean withName) {
		String debugmsg = getClass().getName() + ".getRobotRecognition(): ";
		String tag = "";
		HtmlFrameComp precedingFrame = null;
		String lastPartXpath = null;
		String frameURL = null;
		String frameRS = null;
		Document document = null;
		StringBuffer wholeRS = new StringBuffer();
		String recognitionString = null;
		SPCTreeNode node = new SPCTreeNode();

		// Get the tag's name
		if (xpath.lastIndexOf("[") > xpath.lastIndexOf("/")) {
			tag = xpath.substring(xpath.lastIndexOf("/") + 1, xpath.lastIndexOf("["));
		} else {
			tag = xpath.substring(xpath.lastIndexOf("/") + 1);
		}

		// If SPCUtilities.getRobotTag(tag) does NOT return a Type, no need to generate RS
		// But if tag is INPUT, the Type will be described by its attribute 'type'
		// For example, for tag <INPUT name="helloButton" type='submit'/>,
		//its Robot Type is SPCUtilities.getRobotTag("submit")
		if (!tag.equalsIgnoreCase("INPUT") && SPCUtilities.getRobotTag(tag) == null) {
			return null;
		}

		//Used to calculate consume time, debug for improve performance
		Date d1 = new Date();
		Date d2 = null;
		Date d3 = null;
		Date d4 = null;

		try {
			// If xpath contains FRAME, we should navigate to the last Frame
			// For Example, if the xpath is /HTML/FRAMESET/FRAMESET/FRAME[3]/HTML/BODY
			// navigateFrames() will navigate to frame /HTML/FRAMESET/FRAMESET/FRAME[3], and
			// return an array containing "/HTML/BODY" as lastPartXpath, frame's
			// src as url and frame's RS
			precedingFrame = util.navigateFrames(url, xpath, selenium);
			lastPartXpath = precedingFrame.getChildXpath();
			frameURL = precedingFrame.getSrc();
			frameRS = precedingFrame.getRecognitionString();

			Log.debug(debugmsg + "xpath=" + lastPartXpath + "; url=" + frameURL+ "; RS=" + frameRS);
			if (frameURL == null || frameURL.equals("")) {
				// if no url is returned, we assign the root url to it.
				frameURL = url;
			}
			//If xpath contains Frame, this should never happen ...
			if (frameRS == null) {
				frameRS = util.getFramePath(xpath);
			}
			wholeRS.append(frameRS);
		} catch (SeleniumException e) {
			return null;
		}

		if(Log.getLogLevel()==Log.DEBUG){
			d2 = new Date();
			timeconsume1 += (d2.getTime() - d1.getTime());
		}

		document = getDocument(frameURL, false);
		if (document == null) {
			Log.error(debugmsg + " NO Document found for url:" + frameURL);
			return null;
		}
		String lastPartXpathRS = getRobotRecog(document, lastPartXpath);
		Element element = getElementFromXpath(document, lastPartXpath);
		String elemId = element.attributeValue("id");
		String elemName = element.attributeValue("name");
		Log.debug("Element Id="+elemId+"; Element name="+elemName);
		node.setId(elemId);
		node.setName(elemName);

		if (lastPartXpathRS.equals("")) {
			return null;
		}
		wholeRS.append(lastPartXpathRS);

		if(Log.getLogLevel()==Log.DEBUG){
			d3 = new Date();
			timeconsume2 += (d3.getTime() - d2.getTime());
		}

		if (withName) {
			wholeRS.insert(0, getUniqueNameForXpath(url, xpath, "")+ "=\"");
			wholeRS.append("\"");
		}
		if(Log.getLogLevel()==Log.DEBUG){
			d4 = new Date();
			timeconsume3 += (d4.getTime() - d3.getTime());
		}

		Log.debug(debugmsg + "wholeRS=" + wholeRS);
		recognitionString = wholeRS.toString().replaceAll("\\r|\\n", " ");
		node.setRecognitionString(recognitionString);

		return node;
	}

	/**
	 * According to a xpath, return "SAFS recognition string"
	 *
	 * @param xpath
	 * @param withName
	 *            if true, a generated component name will be prefix of recognition string<br>
	 *            Ex, "ButtonInput1=" will be put ahead of recognition string.<br>
	 *            otherwise, only the recognition string will be returned.<br>
	 * @return
	 */
	public String getRobotRecognition(String xpath, boolean withName) {
		SPCTreeNode node = getRobotRecognitionNode(xpath, withName);

		if(node==null) return null;

		return node.getRecognitionString();
	}

    /**
     * Generate the RS in SAFS-Robot's format for element described by xpath.
     * @param document  Where the element locates.
     * @param xpath     Xpath to describe an element. It should not contain any Frame.
     * @return
     */
	public String getRobotRecog(Document document, String xpath) {
		Element element = getElementFromXpath(document, xpath);
		String tagName = null;
		StringBuffer recognition = new StringBuffer();

		if (element == null) {
			return recognition.toString();
		}

		tagName = element.getName();

		//For other types' element, generate the RS as "Type=RobotType;RobotAttr=attrValue"
		//1. Generate the Type= part
		recognition.append("Type=");
		if (element.getName().equalsIgnoreCase("INPUT")) {
			String tmp = element.attributeValue("type");
			//If the element is a normal text input box, it may has no attribute type.
			if (tmp == null || tmp.equals("")) {
				//<INPUT id="addressInput" >, there is no type="XXX", a normal input box.
				recognition.append(SPCUtilities.getRobotTag("text"));
			} else {
				// <INPUT type="submit" > <INPUT type="radio" >
				recognition.append(SPCUtilities.getRobotTag(tmp));
			}
		}else{
			recognition.append(SPCUtilities.getRobotTag(tagName));
		}
		recognition.append(";");

		//2. Generate the RobotAttribute part
		//If element is HTML, just generate the attribute RS as "Index=aIndex"
		if (tagName.equalsIgnoreCase("HTML")) {
			recognition.append(SPCUtilities.getRobotTag("index") + "=" + getIndex(document, element));
		}else{
			recognition.append(generateAttributRS(document,element, SPCUtilities.TAG_ATTRIBUTES_TO_TRY));
		}
		return recognition.toString();
	}

	/**
	 * Try to generate the RS in SAFS-Robot's format for attribute<br>
	 * If no attribute has value, finally "Index=XXX" will be generated.<br>
	 *
	 * @param document     Where the element is.
	 * @param element      For which, to generate the RS in SAFS-Robot's format
	 * @param attributes   An array of attribute to try.
	 * @return
	 */
	private String generateAttributRS(Document document,Element element, String[] attributes){
		String attributeValue = null;
		String attribute = null;
		String attributeRS = null;

		for(int i=0;i<attributes.length;i++){
			attribute = attributes[i];
			attributeValue =  getAttribute(element, attribute);
			if(attributeValue!=null && !attributeValue.equals("")){
				attributeRS = SPCUtilities.getRobotTag(attribute)+"="+attributeValue;
				break;
			}
		}

		if(attributeRS==null){
			//TODO Treate the innerText and textContent
//			 if(element.innerText != undefined && trim(element.innerText) != ""){
//				 attributeRS = SPCUtilities.getRobotTag("innertext")+"="+element.innerText;
//			 } else if(element.textContent != undefined && trim(element.textContent) !=""){
//				 attributeRS = SPCUtilities.getRobotTag("textcontent")+"="+element.textContent;
//			 }else {
//				 attributeRS = SPCUtilities.getRobotTag("index")+"="+getIndex(document, element);
//			}
			//TODO If index is -1, a bad index, how to do???
			attributeRS = SPCUtilities.getRobotTag("index")+"="+getIndex(document, element);
		}

		return attributeRS;
	}

	/**
	 *
	 * @param document   Where to search element for an xpath
	 * @param xpath      The xpath to be matched.
	 * @return           An element matching the xpath
	 */
	@SuppressWarnings("unchecked")
	public Element getElementFromXpath(Document document, String xpath) {
		Element element = null;

		if (document == null || xpath==null || xpath.equals("")) {
			return null;
		}
		if (xpath.equalsIgnoreCase("//HTML[1]/") ||
		    xpath.equalsIgnoreCase("//HTML[1]")) {
			element = document.getRootElement();
		} else {
			List nodes = document.selectNodes(xpath);
			if (nodes != null && nodes.size() > 0) {
				// TODO get the first matching ???
				//Does the input xpath indicate the unique Element ???
				if (nodes.get(0) instanceof Element) {
					element = (Element) nodes.get(0);
				} else {
					Log.debug("xpath: " + xpath + " , it is not an Element, just ignore! ");
				}
			}
		}

		return element;
	}

	// The name is composed by the tagName suffixed with its index
	public String getUniqueNameForXpath(String url, String xpath, String prefix) {
		String debugmsg = getClass().getName() + ".getUniqueNameForXpath() ";
		String uniqueName = null;
		Document doc = getDocument(url, false);

		if (xpath == null || xpath.equals("")) {
			return prefix;
		}

		Matcher m = SeleniumGUIUtilities.FRAME_PATTERN.matcher(xpath);
		if (m.find()) {
			String firstFrameXPATH = m.group(1);
			Log.debug("Matched: " + firstFrameXPATH);
			String frameSrcURL = getFrameSrcURL(doc, firstFrameXPATH, url);
			int frameIndex = getFrameIndex(doc, firstFrameXPATH);
			if (prefix == null || prefix.equals("")) {
				prefix = "FRAME" + frameIndex;
			} else {
				prefix += "_FRAME" + frameIndex;
			}
			if(frameSrcURL.equals(url)){
				Log.warn("Frame '"+firstFrameXPATH+"' does NOT contain any content.");
				return prefix;
			}
			String restXpath = m.group(3);
			Log.debug(debugmsg + "In page of " + frameSrcURL + " : looking for " + restXpath);
			return getUniqueNameForXpath(frameSrcURL, restXpath, prefix);
		} else {
			Element element = getElementFromXpath(doc, xpath);
			int index = 0;

			if (element != null) {
				index = getIndex(doc, element);
				uniqueName = element.getName() + index;
			} else {
				// If can not get the element for a xpath, create our name.
				// maybe NO need to do this: if no element found, why need a
				// name??
				index = nextIndex.increment();
				uniqueName = "SAFSElemnt" + index;
			}
		}

		if (prefix != null && !prefix.equals("")) {
			uniqueName = prefix + "_" + uniqueName;
		}
		return uniqueName;
	}

	@SuppressWarnings("unchecked")
	public int getIndex(Document document, Element element) {
		int index = -1;

		//Get a list of nodes which have the same tag name as the element.
		String xpath = "//" + element.getName();
		List nodes = document.selectNodes(xpath);
		Node node = null;

		boolean checkType = false;
		String elementType = "";
		//If the element's type is "input", we need to check its
		//attribute "type" to calculate the index.
		if(element.getName().equalsIgnoreCase("input")){
			checkType = true;
			elementType = getAttribute(element,"type");
		}

		for (int i = 0; i < nodes.size(); i++) {
			node = (Node) nodes.get(i);
			if(checkType){
				//If the Input tag is just a input box, the type will be null
				if(elementType==null){
					if(getAttribute(node,"type")==null){
						index++;
					}
				}else if(elementType.equalsIgnoreCase(getAttribute(node,"type"))){
					index++;
				}
			}else{
				index++;
			}

			if (node == element) {
				index = index + 1;
				Log.debug("find index is " + index);
				break;
			}
		}

		return index;
	}

	// Finds all the elements matching the array of tags.
	@SuppressWarnings("unchecked")
	public List getElementsMatchingTags(Document document, String[] tags) {
		List<Node> nodes = new ArrayList<Node>();

		if (tags.length > 1) {
			List all = document.selectNodes(XPATH_ALL_ELEMENTS);
			Node node = null;

			for (int i = 0; i < all.size(); i++) {
				node = (Node) all.get(i);
				for (int j = 0; j < tags.length; j++) {
					if (node.getName().equalsIgnoreCase(tags[j])) {
						nodes.add(node);
					}
				}
			}
			return nodes;
		} else {
			return document.selectNodes(XPATH_ALL_LEVEL_PREFIX + tags[0]);
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllXPath(Document doc, String[] tags) {
		List<String> allXPath = new ArrayList<String>();
		List allNodes = getElementsMatchingTags(doc, tags);
		Node node = null;

		for (int i = 0; i < allNodes.size(); i++) {
			node = (Node) allNodes.get(i);
			allXPath.add(getNodeXPath(node, ""));
		}

		return allXPath;
	}

	/**
	 * This functions takes an array of (HTML) tags, a double array of
	 * attributes to check,<br>
	 * and whether to check the text attributes for partial matches. It try the
	 * xpath<br>
	 * matching the tags and attributes in the order of occurrence on the page,
	 * and return<br>
	 * the one matching the index<br>
	 *
	 * @param document
	 *            In which document, the elements will be tested for matching
	 * @param tags
	 *            An array containing html-tags, among these elements, we select
	 *            one matched.
	 * @param attributes
	 *            The attributes (name, value) needs to be matched for an
	 *            element
	 * @param index
	 *            If there are several matched element according to tags and
	 *            atributes, index<br>
	 *            is used to indicate which one we want.
	 * @param secondaryMatch
	 *            boolean, if false, indicating this is the first time
	 *            searching.<br>
	 *            If the first time searching, no element is found, a secondary
	 *            serch will be tried.<br>
	 * @param matchPartial
	 *            boolean, if true, the attribute's value will be
	 *            partial-matched.
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public String getXpath(Document document, String[] tags,
			String[][] attributes, int index, boolean secondaryMatch,
			boolean matchPartial) {

		List elements = getElementsMatchingTags(document, tags);
		String xpath = "";
		Node current = null;
		int matchedCount = 0;

		for (int i = 0; i < elements.size(); i++) {
			current = (Node) elements.get(i);
			if (checkAttributes(current, attributes, matchPartial)) {
				if (matchedCount++ == index) {
					xpath = getNodeXPath(current, "");
					break;
				}
			}
		}

		//If no element is found, and this is first time search, and it
		//does have attribute to match, we try a second search
		if (xpath.equals("") && !secondaryMatch && attributes.length>0) {
			// log.debug("Matching partial");
			if ("id".equals(attributes[0][0]) || "name".equals(attributes[0][0])) {
				String[] allTags = { "*" };
				return getXpath(document, allTags, attributes, index, true,false);
			} else {
				return getXpath(document, tags, attributes, index, true, true);
			}
		}

		return xpath;
	}

	@SuppressWarnings("unchecked")
	public int getFrameIndex(Document document, String xpath) {
		String debugmsg = getClass().getName() + ".getFrameIndex(): ";
		Element element = getElementFromXpath(document, xpath);
		String[] tags = { "FRAME", "IFRAME" };
		List frameElements = getElementsMatchingTags(document, tags);
		int index = -1;

		if (element == null) {
			Log.warn(debugmsg + " Can't get element of xpath " + xpath);
			return index;
		}
		if (frameElements == null || frameElements.size() == 0) {
			Log.warn(debugmsg + " There are NO frame elements in this page. tags: " + tags);
			return index;
		}

		for (int i = 0; i < frameElements.size(); i++) {
			if (frameElements.get(i) == element) {
				index = i + 1;
				break;
			}
		}

		return index;
	}

	// Given a DOM element, an array of attributes and whether to match
	// the text attributes partially, this will return whether or not
	// the element matches the attributes.
	// attrcheck can contain an attribute name whose test value can be
	// "undefined" and
	// we will match true if the attribute is NOT in the element in that case.
	public boolean checkAttributes(Node node, String[][] attrcheck,
			boolean matchPartial) {
		boolean attrgood = true;
		Element current = null;

		if (node instanceof Element) {
			current = (Element) node;
		} else {
			Log.debug("Can not get attributes from Node object, so can not verify.");
			return false;
		}

		if (attrcheck.length > 0) {
			List<?> attributes = current.attributes();
			String name = "";
			String value = "";

			if (attributes != null) {
				for (int j = 0; j < attrcheck.length; j++) {
					boolean temp = false;
					boolean undef = false;
					boolean namematch = false;
					name = attrcheck[j][0];
					value = attrcheck[j][1];

					Log.debug("HtmlDomParser --> checkAttributes(): name="
							+ name + " ; value=" + value);

					// If the name or value is null, we consider that we fail to
					// match attributes
					if (name == null || value == null) {
						attrgood = false;
						break;
					}

					if (value.equalsIgnoreCase("undefined") ||
					    ((value.indexOf("|") > -1) && (value.indexOf("undefined") > -1))) {
						undef = true;
					}

					for (int k = 0; k < attributes.size(); k++) {
						Attribute attribute = (Attribute) attributes.get(k);

						if (attribute.getName().equalsIgnoreCase(name)) {
							namematch = true;
							if (attribute.getValue().equalsIgnoreCase(value) ||
							    (value.indexOf('|') > -1 && value.indexOf(attribute.getValue()) != -1)) {
								temp = true;
								break;
							}
							if (!temp && matchPartial
								 && value.indexOf(attribute.getValue()) != -1
								 && !attribute.getValue().equals("")) {
								temp = true;
								break;
							}
						}
					}
					if (!temp && !namematch && undef) {
						temp = true;
					}
					// TODO how to get innerHTML from Element of dom4j ???
					// if(!temp && "innerHTML".equalsIgnoreCase(name) &&
					// current.innerHTML == value){
					// temp = true;
					// }

					if (!temp && name.equalsIgnoreCase("text")) {
						String text = current.getTextTrim();
						if (text == null || text.equals("")) {
							text = current.attributeValue("value");
						}

						if (text == null || text.equals("")) {
							text = current.attributeValue("alt");
						}

						if (text == null) {
							text = "";
						}
						text = text.trim();

						value = value.replaceAll("\\s+", " ");
						text = text.replaceAll("\\s+", " ");

						if ((name.equalsIgnoreCase("text") && value.equals(text))
							 || (matchPartial && !text.equals("") && value.indexOf(text) != -1)
							 || (matchPartial && !text.equals("") && text.indexOf(value) != -1)) {
							temp = true;
						}
					}
					if (!temp && name.equalsIgnoreCase("type")
						&& value.indexOf("text") != -1
						&& current.getName().equalsIgnoreCase("TEXTAREA")) {
						temp = true;
					}
					if (!temp) {
						attrgood = false;
						break;
					}
				}
			} else {
				attrgood = false;
			}
		}
		return attrgood;
	}

	// returns width, height, scrollX, and scrollY of the browser's client area
	public String getClientScrollInfo(Selenium selenium) throws SAFSException {
		String debugmsg = getClass().getName() + ".getClientScrollInfo(): ";
		String returnString = null;
		String sep = this.getBoundsSeparator();

		try {
			// For FIREFOX
			String[] width = selenium.getAttributeFromAllWindows("innerWidth");
			String[] height = selenium.getAttributeFromAllWindows("innerHeight");
			String[] pageXOffset = selenium.getAttributeFromAllWindows("pageXOffset");
			String[] pageYOffset = selenium.getAttributeFromAllWindows("pageYOffset");
			returnString = width[0] + sep + height[0] + sep + pageXOffset[0] + sep + pageYOffset[0];
		} catch (Exception e) {
			Log.warn(debugmsg + " Exception is " + e.getMessage());
			try {
				// for IE
				String width = selenium.getAttribute("//BODY/@clientWidth");
				String height = selenium.getAttribute("//BODY/@clientHeight");
				String pageXOffset = selenium.getAttribute("//BODY/@scrollLeft");
				String pageYOffset = selenium.getAttribute("//BODY/@scrollTop");
				returnString = width + sep + height + sep + pageXOffset + sep + pageYOffset;
			} catch (Exception e1) {
				Log.warn(debugmsg + " Exception is " + e1.getMessage());
			}
		}

		Log.debug(debugmsg + " ClientScrollInfo is " + returnString);

		if (returnString == null) {
			throw new SAFSException("Can not get ClientScrollInfo.");
		}

		return returnString;
	}

	// returns an [left,top] array of the left-hand top corner of the browser's
	// client area
	public String getBrowserClientScreenPosition(Selenium selenium)
			throws SAFSException {
		String debugmsg = getClass().getName()
				+ ".getBrowserClientScreenPosition(): ";
		String returnString = null;
		String sep = this.getBoundsSeparator();
		int top = -1, left = -1;

		try {
			// For FIREFOX
			String[] screenX = selenium.getAttributeFromAllWindows("screenX");
			String[] screenY = selenium.getAttributeFromAllWindows("screenY");
			String[] outerHeight = selenium.getAttributeFromAllWindows("outerHeight");
			String[] innerHeight = selenium.getAttributeFromAllWindows("innerHeight");
			top = Integer.parseInt(screenY[0]) + Integer.parseInt(outerHeight[0])- Integer.parseInt(innerHeight[0]) - 27;
			left = Integer.parseInt(screenX[0]) + 4;
		} catch (Exception e) {
			Log.warn(debugmsg + " Exception is " + e.getMessage());
			try {
				// for IE
				String[] screenTop = selenium.getAttributeFromAllWindows("screenTop");
				String[] screenLeft = selenium.getAttributeFromAllWindows("screenLeft");
				top = Integer.parseInt(screenTop[0]);
				left = Integer.parseInt(screenLeft[0]);
			} catch (Exception e1) {
				Log.warn(debugmsg + " Exception is " + e1.getMessage());
				throw new SAFSException("Can not get getBrowserClientScreenPosition.");
			}
		}

		returnString = left + sep + top;
		Log.debug(debugmsg + " getBrowserClientScreenPosition is "+ returnString);

		return returnString;
	}

	public String getSSBounds() {
		// TODO Need to be implemented in javascript or in DocumentParser
		Log.info("Need implement the code of JavaScript");
		return 0 + BOUDNS_SEPARATOR + 0 + BOUDNS_SEPARATOR + 100
				+ BOUDNS_SEPARATOR + 200;
	};

	public void test() {
		String[] tags = { "A", "TD" };
		Node node = null;
		Document doc = this.getDocument(url, false);
		List<?> elements = getElementsMatchingTags(doc, tags);

		for (int i = 0; i < elements.size(); i++) {
			node = (Node) elements.get(i);
			Log.debug(node.getName() + "\t" + node.getNodeTypeName() + "\t" + node.getUniquePath());
		}
	}

	/**
	 * To give a atomic incrementing int value.
	 */
	private class AtomicInteger {
		private int value = 0;

		public AtomicInteger(int value) {
			this.value = value;
		}

		public synchronized int increment() {
			value++;
			return value;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.ENABLED = false;
		// Log.setLogLevel(Log.DEBUG);
		Log.setLogLevel(Log.GENERIC);

		try {
			String url = "http://www.google.com";
			DocumentParser hdParser = new DocumentParser(url);

			String[] xpathArray = hdParser.getAllElements();
			for(int i=0;i<xpathArray.length;i++){
				Log.debug(xpathArray[i]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
