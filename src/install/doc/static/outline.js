// See KB article about changing this dynamic HTML
	function getControlTag(src)
	{
		TRok = false;
		TN = src.tagName.toUpperCase();
		while ("HTML" != TN)
		{
			TN = src.tagName.toUpperCase();
			if ("IMG" == TN || "FONT" == TN || "A" == TN || "TD" == TN)
			    TRok = true;
			if ("LI" == TN)
				return src;
			if ("TR" == TN)
			{
				if(TRok)
					return src;
				return null;
			}
			src = src.parentElement;
		}
		return null;
	}
	function dynOutlineEnabled(src)
	{
		TN = src.tagName.toUpperCase();
		while ("BODY" != TN)
		{
			TN= src.tagName.toUpperCase();
			table = "TABLE" == TN;
			if(table && src.getAttribute("border", false) != "0")
				return false;
			if("OL" == TN || "UL" == TN || table)
			{
				if(null != src.getAttribute("nodynamicoutline", false))
					return false;
				if(null != src.getAttribute("dynamicoutline", false))
					return true;
			}
			src = src.parentElement;
		}
		return false
	}
    function initCollapse(src)
    {
		TN = src.tagName.toUpperCase();
		while ("BODY" != TN)
		{
			TN = src.tagName.toUpperCase();
			table = "TABLE" == TN;
    		if(table && src.getAttribute("border", false) != "0")
    			return false;
        	if("OL" == TN || "UL" == TN || table)
    		{
    			if(null != src.getAttribute("initcollapsed", false))
    				return true;
    		}
    		src = src.parentElement;
        }
		return false;
    }
	function containedIn(src, dest)
	{
		if ("!" == src.tagName)
			return true;
		src = getControlTag(src);
		if (src == dest)
		    return true;
		return false;
	}
    function initOutline()
    {
        listTags = new Array()
        listTags[0]="UL";
        listTags[1]="OL";
        listTags[2]="TABLE";
        for(j=0;j<listTags.length;j++)
        {
            tagName=listTags[j];
            coll=document.getElementsByTagName(tagName);
            for(i=0; i<coll.length; i++)
            {
                if(dynOutlineEnabled(coll[i].parentElement))
                {
                    if(initCollapse(coll[i]))
                        coll[i].style.display="none";
                }
            }
        }
    }
	function dynOutline()
	{
		var src = event.srcElement;
		src = getControlTag(src);
		if (null == src)
			return;
		if (!dynOutlineEnabled(src))
			return;
		var srcTmp = src;
		var nodes = null;
		while(srcTmp != null){
			tag = srcTmp.tagName.toUpperCase();
			if ("UL" == tag || "OL" == tag || "TABLE" == tag){
				srcTmp.style.display = srcTmp.style.display == "none" ? "" : "none";
			}
			if(srcTmp.children){
				if(nodes == null)
					nodes = Array.prototype.slice.call(srcTmp.children);
				else
					nodes.concat(Array.prototype.slice.call(srcTmp.children));
			}
			srcTmp = null;
			if(nodes && nodes.length > 0){
				srcTmp = nodes.shift();
			}
		}
	}
