var BOUDNS_SEPARATOR = "#";
var LOG_LEVEL_INFO = 1;
var LOG_LEVEL_DEBUG = 2;
var log_level = LOG_LEVEL_DEBUG;

function SAFSGetBoundsSeparator(){
	return BOUDNS_SEPARATOR;
}

//This function takes an xpath in the form of a string
//and returns a corresponding DOM element if it can find it.
//It uses Selenium's default function, then if it can't find it with that,
//it uses a firefox backup function. This does not happen in IE.
function SAFSgetElementFromXpath(xpath){
	var element;
	if(xpath=="//HTML[1]/" || xpath=="//HTML[1]"){
		element = window.document.body;
	} else {
		try{
		    debug("Try Selenium Javascript API to get element ");			
//		    element = BrowserBot.prototype._findElementUsingFullXPath(xpath,window.document);
		    element = BrowserBot.prototype.locateElementByXPath(xpath,window.document,window);
		    debug("locateElementByXPath get element "+element);
		}catch(x){
			debug('Try Selenium Javascript API, Exception: '+x);
			element = null;
		}
		
		if((element == null)||(element == 'undefined')){
			try{
				//For Mozilla, FireFox etc.
				if(window.document.evaluate){
					debug('Try window.document.evaluate() to get element');
					element = window.document.evaluate(xpath,window.document, null, XPathResult.ANY_TYPE, null).iterateNext();
				    debug("Mozilla: evaluate() get element "+element);
				//For IE
				}else if (window.ActiveXObject){
					//SelectSingleNode() or selectNodes() is not supported by IE HTML Document, there are method of w3c Document!!!
					debug('Try window.document.selectNodes() to get element');
					element = window.document.selectNodes(xpath).childNodes[0];
				    debug("IE: selectNodes() get element "+element);			
				}
			}catch(x){
				debug('Try Document API, Exception: '+x);
				element = null;	
			}		
		}

	}
	return element;
}

// returns an [left,top] array of the left-hand top corner of the browser's client area
function SAFSgetBrowserClientScreenPosition(){
    var top = 0;
    var left = 0;
	if(window.innerHeight){
		top = window.screenY + (window.outerHeight - window.innerHeight) - 27;// - window.scrollY;
		left = window.screenX + 4;// - window.scrollX;
	} else {
		top = window.screenTop;   //offsets?
		left = window.screenLeft; //offsets?
	}		
	return left +BOUDNS_SEPARATOR+ top;
}

//This function takes an xpath in the form of a string
//and a boolean to state whether to add the window position to the coordinates.
//It returns coordinates of the object described by the xpath.
function SAFSgetCompBounds(xpath, getwindow){
	var element;
	element = SAFSgetElementFromXpath(xpath);
	var top = 0;
	var left = 0;
	if(getwindow){
		if(window.innerHeight){
			top = window.screenY + (window.outerHeight - window.innerHeight) - 27;// - window.scrollY;
			left = window.screenX + 4;// - window.scrollX;
		} else {
			top = window.screenTop;
			left = window.screenLeft;
		}		
	}
	return SPCgetCompBounds(element,top,left);	
};

//This functions takes an array of (HTML) tags, a double array of attributes to check,
//and whether to check the text attributes for partial matches. It returns all the xpaths
//matching the tags and attributes in the order of occurence on the page.
function SAFSgetXpath(tags, attrcheck, secondaryMatch, matchPartial) {
	if(secondaryMatch == null)
		secondaryMatch = false;
	var elements = SAFSgetElementsMatchingTags(tags);
	var xpath = new Array();
	var count = 0;
	
	for(var i = 0; i < (elements.length); i++){
		var current = elements[i];
		
		if(SAFScheckAttributes(current,attrcheck, matchPartial)) {
			xpath[count] = SAFSgetElementXpath(current,"//");
			count++;
		}
	}
	
	if(xpath.length == 0 && !secondaryMatch){
		//logMessage("Matching partial");
		if(attrcheck[0][0] == "id" || attrcheck[0][0] == "name")
			return SAFSgetXpath(new Array("*"),attrcheck, true, false);
		else
			return SAFSgetXpath(tags,attrcheck,true, true);
	}
	
	var xpaths = "";
	
	// CANAGL APR 2008 REMOVING REVERSE ORDER.  DON'T KNOW WHY ITS HERE. 
	// CAUSING INCORRECT CLICKING IN HTMLText;Index=N
	//for(i = xpath.length-1; i >=0 ; i--){ //NOTE: Reversed order to have innermost nodes first
	for(i = 0;i < xpath.length; i++){
		if(xpath[i]!=""){
			xpaths+=xpath[i] +";";
		}
	}
	
	return xpaths;
}

//Finds all the elements matching the array of tags.
function SAFSgetElementsMatchingTags(tags){
	if(tags.length>1){
		var count = 0;
		var elements = new Array();
		var all = window.document.getElementsByTagName('*');
		
		for(var i = 0; i < all.length; i++){
			for(var j = 0; j < tags.length; j++){
				if(all[i].tagName == tags[j]){
					elements[count++] = all[i];
				}
			}
		}
		return elements;
	} else {
		return window.document.getElementsByTagName(tags[0]);
	}
}

//Given a DOM element, an array of attributes and whether to match
//the text attributes partially, this will return whether or not
//the element matches the attributes.
//attrcheck can contain an attribute name whose test value can be "undefined" and 
//we will match true if the attribute is NOT in the element in that case.
function SAFScheckAttributes(current,attrcheck,matchPartial){
	var attrgood = true;
	if( attrcheck.length > 0){
		var attributes = current.attributes;
		if(attributes != undefined){
			for(var j=0; j<attrcheck.length; j++) {
				var temp = false;
				var undef = false;
			    var namematch = false;
				if(attrcheck[j][1] == "undefined" ||((attrcheck[j][1].indexOf("|") > -1)&&(attrcheck[j][1].indexOf("undefined")> -1))){
				    undef = true;
				}
				for(var k = 0; k < attributes.length; k++){
					if(attributes[k].name == attrcheck[j][0]){
					    namematch = true;
					    if(attrcheck[j][1] == attributes[k].value || (attrcheck[j][1].indexOf('|')>-1 && attrcheck[j][1].indexOf(attributes[k].value) != -1)){
					        temp = true;
					    }
					    if(temp != true && matchPartial && attrcheck[j][1].indexOf(attributes[k].value) != -1 && attributes[k].value != ""){
							temp = true;
						}
					}
				}
				if( temp != true && namematch != true && undef == true){
					temp = true;
				}
				if(temp!=true && attrcheck[j][0] == "innerHTML" && current.innerHTML == attrcheck[j][1]){
					temp = true;
				}
				if(temp!= true && attrcheck[j][0] == "text"){
					var text = current.innerText;
					if(text == undefined){
						text = current.textContent;
					}
					if(text == ""){
						text = current.value;
					}
					if(text == undefined){
						text = current.alt;
					}
					if(text == undefined){
						text = "";
					}
					text = text.replace(/^\s+|\s+$/g,"");
					if(attrcheck[j][0] == "text" && attrcheck[j][1].replace(/\s+/g," ") == text.replace(/\s+/g," ") || (matchPartial && attrcheck[j][1].replace(/\s+/g," ").indexOf(text.replace(/\s+/g," "))!=-1 && text != "") || (matchPartial && text.replace(/\s+/g," ").indexOf(attrcheck[j][1].replace(/\s+/g," "))!=-1 && text != "")){
						temp = true;
					}
				}
				if(temp != true && attrcheck[j][0] == "type" && attrcheck[j][1].indexOf("text") != -1 && current.tagName == "TEXTAREA"){
					temp = true;
				}
				if(temp == false){
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

//Returns the index of the frame represented by the xpath passed
//Returns -1 if not found
function SAFSgetFrameIndex(xpath)
{
	var element = SAFSgetElementFromXpath(xpath);
	var tags = new Array("FRAME","IFRAME");
	var elements = SAFSgetElementsMatchingTags(tags);
	for(i = 0; i < elements.length;i++){
		if(elements[i] == element){
			return i;
		}
	}
	//logMessage("Frame: "+xpath+" not found!");
	return -1;
}

//Returns the xpath of the DOM element passed, with the optional
//prefix attached in front of it (such as a frame xpath)
function SAFSgetElementXpath(element, prefix){
	var current = element;
	var xpath = "";
	do{
		if(current.tagName != ""){
			var curIndex = 1;
			var curSibling = current.previousSibling;
			while(curSibling != undefined){
				if(curSibling.tagName == current.tagName)
					curIndex++;
				curSibling = curSibling.previousSibling;
			}
			
			xpath = current.tagName +"["+curIndex+"]"+ "/" +xpath;
		}
		current = current.parentNode;
		
		//logMessage("js:SAFSgetElementXpath building xpath:"+ xpath);
		
	} while((current != null) && (current != undefined) && (current.tagName != undefined) );
	
	return prefix + xpath.substring(0,xpath.length-1);
}

//Returns the attribute defined by the string passed for the element
//passed as an xpath.
function SAFSgetAttribute(xpath, attribute){
	var res = SAFSgetElementFromXpath(xpath);
	if(attribute == "text" || attribute == "innerText" || attribute == "innertext"){
		var text = res.innerText;
		if(text == undefined){
			text = res.textContent;
		}
		if(text == ""){
			text = res.value;
		}
		if(text == undefined){
			text = res.alt;
		}
		if(text == undefined){
			text = "";
		}
		return text;
	} else {
		var value = res[attribute];
		
		if(value == null || value == 'undefined' || value === ""){
			value = res.getAttribute(attribute);
		}
		
		return value;
	}
};

//returns "innerWidth:innerHeight:pageXOffset:pageYOffset"
function SAFSgetClientScrollInfo(){
	var width = 0;
	var height = 0;
	var scrolltop = 0;
	var scrollleft = 0;
	if(window.document.all){
		width = window.document.body.clientWidth;
		height =  window.document.body.clientHeight;
		scrollTop = window.document.body.scrollTop;
		scrollLeft = window.document.body.scrollLeft;
	} else {
		width = window.innerWidth;
		height =  window.innerHeight;
		scrollTop = window.pageYOffset;
		scrollLeft = window.pageXOffset;
	}
	return width+BOUDNS_SEPARATOR+height+BOUDNS_SEPARATOR+scrollLeft+BOUDNS_SEPARATOR+scrollTop;
}

//Method that returns all of the elements on the page, in the form of xpaths and coordinates,
//as a ';' delimited string.
var interrupt = false;
function SPCgetAllElements() {
	interrupt = false;
	return SPCgetAllElementsR(window.document,"",0,0);
}

//Recursive function called by SPCgetAllElements that finds all elements in the current frame,
//then recurses through sub frames to find all those elements.
//Root is the current window.document
//prefix is attached in front all the xpaths
//top is the top coordinate of the frame
//left is the left coordinate of the frame
function SPCgetAllElementsR(root,prefix,top,left) {
	var elements = root.getElementsByTagName("*");
	var xpath = new Array();
	var xpaths = "";
	var framesXpath = new Array();
	var f_frames = new Array();
	var frameCount = 0;
	var count = 0;
	if(prefix == ""){
		prefix = "//";
	}
	
	//logMessage("js:searching "+root+" at "+top+","+left);
	
	for(var i = 0; i < (elements.length); i++){
		if(interrupt == true){
			return "INTERRUPTED";
		}
		
		var bounds = "0"+BOUDNS_SEPARATOR+"0"+BOUDNS_SEPARATOR+"0"+BOUDNS_SEPARATOR+"0";
		var theTag = elements[i].tagName;
		if( theTag == 'HTML' || theTag == 'HEAD' || theTag == 'META' || theTag == 'TITLE' || theTag == 'SCRIPT'){
		    // retain default bounds
		}else{
		    bounds = SPCgetCompBounds(elements[i],top,left);
		}
		xpath[count] = SAFSgetElementXpath(elements[i], prefix) + BOUDNS_SEPARATOR + bounds;
		
		//logMessage("js:xpath["+count+"]="+xpath[count]);
		
		if(theTag == 'FRAME' || theTag == 'IFRAME'){
			f_frames[frameCount] = elements[i];
			framesXpath[frameCount++] = xpath[count].substring(0,xpath[count].indexOf(BOUDNS_SEPARATOR)) + "//";
			//logMessage("frame added: " + framesXpath[frameCount-1]);
		}
		count++;
	}

	//logMessage("js:concatenating "+ count +" xpaths.");	
	for(i = 0; i < xpath.length; i++){
		if(xpath[i]!=""){
			xpaths+=xpath[i] +";";
		}
	}
	elements = new Array();
	count = 0;
	
	//logMessage("js:appending all frame xpaths...");
	
	if(window.document.all){
		for(i = 0; i < root.frames.length;i++){
			xpaths+=SPCgetAllElementsR(root.frames[i].document,framesXpath[i], root.frames[i].s_top, root.frames[i].s_left);
		}
	} else {
		for(i = 0; i < f_frames.length;i++){
			xpaths+=SPCgetAllElementsR(f_frames[i].contentDocument,framesXpath[i], f_frames[i].s_top, f_frames[i].s_left);
		}
	}
	
	return xpaths;
}
//Returns the x/y & width/height of the element given, adding top/left offset passed
function SPCgetCompBounds(element, top, left){
	var x = SPCgetElementPositionLeft(element)+left;
	var y = SPCgetElementPositionTop(element)+top;
	var width = element.offsetWidth;
	var height = element.offsetHeight;
	element.s_top = y;
	element.s_left = x;
	return x+BOUDNS_SEPARATOR+y+BOUDNS_SEPARATOR+width+BOUDNS_SEPARATOR+height;
};

//Returns the bounds of the HTML page for purpose of taking a screenshot
function SPCgetSSBounds(){
	var element = window.document.getElementsByTagName("HTML")[0];
	var top = 0;
	var left = 0;
	if(window.innerHeight){
		top = window.screenY + (window.outerHeight - window.innerHeight) - 27 - window.scrollY;
		left = window.screenX - window.scrollX+4;
	} else {
		top = window.screenTop;
		left = window.screenLeft;
		top -= window.document.body.scrollTop;
		left -= window.document.body.scrollLeft;
	}
	var x = SPCgetElementPositionLeft(element)+left;
	var y = SPCgetElementPositionTop(element)+top;
	var width = element.offsetWidth;
	var height = element.offsetHeight;
	return x+BOUDNS_SEPARATOR+y+BOUDNS_SEPARATOR+width+BOUDNS_SEPARATOR+height;
};

//Returns the left (x) position of the element
function SPCgetElementPositionLeft(element) {
	var x = element.offsetLeft;
	var elementParent = element;
	
	while (elementParent = elementParent.offsetParent){
		if(window.document.all){
			if( (elementParent.tagName != "TABLE") && (elementParent.tagName != "BODY") ){
				x += elementParent.clientLeft;
			}
		} else {
			if(elementParent.tagName == "TABLE"){
				var parentBorder = parseInt(elementParent.border);
				if(isNaN(parentBorder))	{
					var parentFrame = elementParent.getAttribute('frame');
					if(parentFrame != null)	{
						x += 1;
					}
				} else if(parentBorder > 0) {
					x += parentBorder;
				}
			}
		}
		x += elementParent.offsetLeft;		
	}
	
	while(element != null){
		if(element.scrollLeft)
			x -= element.scrollLeft;
		element = element.parentNode;
	}
	return x;
};

//Returns the top (y) position of the element
function SPCgetElementPositionTop(element) {
	var y = 0;
	var element_2 = element;
	while (element != null)
	{
		if(window.document.all){
			if( (element.tagName != "TABLE") && (element.tagName != "BODY") ){
				y += element.clientTop;
			}
		} else {
			if(element.tagName == "TABLE"){
				var parentBorder = parseInt(element.border);
				if(isNaN(parentBorder)){
					var parentFrame = element.getAttribute('frame');
					if(parentFrame != null){
						y += 1;
					}
				}else if(parentBorder > 0){
					y += parentBorder;
				}
			}
		}
		y += element.offsetTop;
		if (element.offsetParent && element.offsetParent.offsetHeight && element.offsetParent.offsetHeight < element.offsetHeight){
			element = element.offsetParent.offsetParent;
		}else{
			element = element.offsetParent;
		}
	}
	element = element_2;
	while(element != null){
		if(element.scrollTop)
			y -= element.scrollTop;
		element = element.parentNode;
	}
	return y;
};

//Iterates through all the elements of the same tag as the one passed and 
//returns the position in that array at which this element occurs.
//-1 should never be returned.
function SPCgetIndex(element){
	var element_array = window.document.getElementsByTagName(element.tagName);
	for(var i = 0; i < element_array.length;i++){
		if(element_array[i] == element){
			return i+1;
		}
	}
	return -1;
};

//Returns the info on the element for display by the Process container
function SPCgetElementInfo(xpath){
	var element = SAFSgetElementFromXpath(xpath);
	if(element== null){
		return "";
	}
	var parent = element.parentNode;
	var rvalue = SPCgetCompBounds(element,0,0)+";;;;";
	rvalue+=xpath+";;;;";
	rvalue+=SPCgetRobotRecognition(element)+";;;;";
	rvalue+=parent.innerHTML+";;;;";
	if(element.innerHTML==""){
		rvalue+="None";
	} else { 
		rvalue+=element.innerHTML;
	}
	return rvalue;
	
};

//Returns the best robot recognition string that can be made from this element defined
//by the xpath string passed
function SPCgetRobotRec(xpath){
	var element = SAFSgetElementFromXpath(xpath);
	if(element== null){
		return "";
	}
	return SPCgetRobotRecognition(element);
};

//Helper function for the method above
function SPCgetRobotRecognition(element){
	var type = "";
	if(element.tagName=="INPUT"){
		type = ":::"+element.type;
	}
	if(element.tagName=="HTML"){
		return "index:::"+SPCgetIndex(element);
	}
	if(element.id != "" && element.id != undefined){
		return "id:::"+element.id+type;
	} else if(element.name != "" && element.name != undefined){
		return "name:::"+element.name+type;
	} else if(element.alt != "" && element.alt != undefined) {
		return "alt:::"+element.alt+type;
	} else if(element.innerText != undefined && trim(element.innerText) != ""){
		return "innertext:::"+element.innerText+type;
	} else if(element.textContent != undefined && trim(element.textContent) !=""){
		return "innertext:::"+element.textContent+type;
	} else {
		return "index:::"+SPCgetIndex(element)+type;
	}
};

//Takes all spaces away from front of string
function LTrim( value ) {
	var re = /\s*((\S+\s*)*)/;
	return value.replace(re, "$1");
};

//Takes all spaces away from back of string
function RTrim( value ) {
	var re = /((\s*\S+)*)\s*/;
	return value.replace(re, "$1");
}

//Takes all spaces away from front & back of string
function trim( value ) {
	return LTrim(RTrim(value));
};

//LOG HELPER FUNCTIONS BELOW
var logDivCreated = false;
function logMessage(message) {
	if(logDivCreated == false){
		logDivCreated = true;
		creatediv('log', message, 200,100,0,0);
	}
	var div = window.document.getElementById('log');
	div.innerHTML += message+"<br>";
}

function creatediv(id, width, height, left, top) {
   var newdiv = window.document.createElement('div');
   newdiv.setAttribute('id', id);
   
   if (width) {
       newdiv.style.width = width;
   }else{
       newdiv.style.width = 600;
   }
   
   if (height) {
       newdiv.style.height = height;
   }else{
       newdiv.style.height = 600;   
   }
   
   if ((left || top) || (left && top)) {
       newdiv.style.position = "absolute";
       
       if (left) {
           newdiv.style.left = left;
       }
       
       if (top) {
           newdiv.style.top = top;
       }
   }
   
	newdiv.style.background = "#FFFFFF";
	//newdiv.style.border = "4px solid #000";

	window.document.body.appendChild(newdiv);
}

var messageDivOk=false;
var divname = 'messageDIV';
var logDivWidth = 500;
var logDivHeight = 100;

function initLogDivSize(){
	if(window.document.all){
		logDivWidth = window.document.body.clientWidth;
		logDivHeight = window.document.body.clientHeight;		
	}else{
		logDivWidth = window.innerWidth;
		logDivHeight = window.innerHeight;
	}
}
	
function log(message){
	if(messageDivOk == false){
		messageDivOk = true;
		initLogDivSize();
		creatediv(divname, logDivWidth,logDivHeight,0,0);
	}
	
	var div = window.document.getElementById(divname);
	div.innerHTML += "LOG:	"+message+"<br>";
}

function info(message){
	if(log_level>=LOG_LEVEL_INFO){
		if(messageDivOk == false){
		  messageDivOk = true;
		  initLogDivSize();
		  creatediv(divname, logDivWidth,logDivHeight,0,0);
	    }
	
	  var div = window.document.getElementById(divname);
	  div.innerHTML += "INFO:	"+message+"<br>";
	}
}

function debug(message){
	if(log_level>=LOG_LEVEL_DEBUG){
		if(messageDivOk == false){
		  messageDivOk = true;
		  initLogDivSize()
		  creatediv(divname, logDivWidth,logDivHeight,0,0);
	    }
	
	  var div = window.document.getElementById(divname);
	  div.innerHTML += "DEBUG:	"+message+"<br>";
	}
}

var previous = null;
var previousBorderStyle = null;

function highlight(xpathOrIdOrName){
  var element;
  try{
    element = SAFSgetElementFromXpath(xpathOrIdOrName);
  }catch(err){
  	debug('Try get element by xpath, Met Error: '+err);
  }

  if(element == null || element == undefined){
  	debug("Can't get element for xpath '"+xpathOrIdOrName+"'");
  	try{
  	  element = window.document.getElementById(xpathOrIdOrName);
  	}catch(ex){}
  	if(element == null || element == undefined){
  	  debug("Can't get element for id '"+xpathOrIdOrName+"'");
  	  try{
  	    element = window.document.getElementsByName(xpathOrIdOrName)[0];
  	  }catch(ex){}
  	  if(element == null || element == undefined){
  	    debug("Can't get element for name '"+xpathOrIdOrName+"'");
  	  }
  	}
  }

  if(element == null || element == undefined){
  	debug("Can't get element, can't highlight element.");
  	throw "Can't get element for '"+xpathOrIdOrName+"'";
  }else{
	  window.document.body.focus();
	  //If previous exists, reset its border style
	  if(previous != undefined){
	  	previous.style.border=previousBorderStyle;
	  }
	  
	  previous=element;
	  previousBorderStyle=element.style.border;
	  
	  element.style.border="5px solid red";  
  }

}
