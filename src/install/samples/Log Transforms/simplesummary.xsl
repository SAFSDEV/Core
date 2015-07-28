<xsl:stylesheet version='1.0'
		xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
		xmlns='http://www.w3.org/TR/REC-html40'>
    
    <xsl:output method="html" version="3.2" omit-xml-declaration="yes" />

    <xsl:variable name="passcolor">#00ff00</xsl:variable>
    <xsl:variable name="warncolor">#a08040</xsl:variable>
    <xsl:variable name="failcolor">#ff0000</xsl:variable>

    <xsl:template match="/">
    <HTML>

    <SCRIPT language="JavaScript">        
        function getTestName() {
	      var uri = new String(document.location);
	      var index = uri.lastIndexOf("/");
	      var test = uri.substring(index +1, uri.length);
	      var index = test.indexOf(".");
	      return test.substring(0, index);
        } 
    </SCRIPT>

        <HEAD>            
            <SCRIPT language="JavaScript">
                document.write("<TITLE>"+ getTestName() +" Test Results</TITLE>");
            </SCRIPT>
        </HEAD>
    <BODY>
    
        <SCRIPT language="JavaScript">
            document.write("<H2>Test: "+ getTestName() +"</H2>");
        </SCRIPT>
        Report Generated: 
            <SCRIPT language="JavaScript">
                document.write(document.lastModified);
            </SCRIPT>
        <HR />
        <TABLE>
            <TR><TD>Test Start: </TD>
                <TD><B>
                    <xsl:value-of select="/SAFS_LOG/LOG_OPENED/@time"/><xsl:text>&#160;&#160;</xsl:text>
                    </B>
                    <xsl:value-of select="/SAFS_LOG/LOG_OPENED/@date"/>
                </TD>
            </TR>
            <TR><TD>Test Finished: </TD>
                <TD><B>
                    <xsl:value-of select="/SAFS_LOG/LOG_CLOSED/@time"/><xsl:text>&#160;&#160;</xsl:text>
                    </B>
                    <xsl:value-of select="/SAFS_LOG/LOG_CLOSED/@date"/>
                </TD>
            </TR>
    	</TABLE>
    	<H3>Overall Test Status:</H3>
    	<TABLE border="2" cellpadding="5" >
    	
    	<xsl:for-each select="/SAFS_LOG/STATUS_REPORT">
    	    <xsl:call-template name="status_summary" />
    	</xsl:for-each>
    	
    	</TABLE>
    	<P>
    	<xsl:apply-templates select="/SAFS_LOG/STATUS_REPORT[last()]" />
    	</P>
    	
    	<HR/>
    	<H3>Detailed Status Info:</H3>
    	
    	<xsl:for-each select="/SAFS_LOG/STATUS_REPORT" >
    	    <P>
    	    <xsl:apply-templates select="." />
    	    </P>
    	</xsl:for-each>
    	
    </BODY>
    </HTML>
    </xsl:template>
    
    <xsl:template match="STATUS_REPORT" >
        <B><xsl:value-of select="@name" /></B>
        <TABLE border="2" cellpadding="5">
           <xsl:for-each select="STATUS_ITEM" >
               <TR>
                   <TD><xsl:value-of select="@type" /></TD>
                   <TD><B><xsl:value-of select="STATUS_ITEM_TEXT" /></B></TD>
               </TR>
           </xsl:for-each>
        </TABLE>
    </xsl:template>
    
    <xsl:template name="status_summary">
       <xsl:variable name="testrecords">
           <xsl:call-template name="getStatusCount">
               <xsl:with-param name="counter">TEST RECORDS</xsl:with-param>
           </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="testfailures">
           <xsl:call-template name="getStatusCount">
               <xsl:with-param name="counter">TEST FAILURES</xsl:with-param>
           </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="testwarnings">
           <xsl:call-template name="getStatusCount">
               <xsl:with-param name="counter">TEST WARNINGS</xsl:with-param>
           </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="testspassed">
           <xsl:call-template name="getStatusCount">
               <xsl:with-param name="counter">TESTS PASSED</xsl:with-param>
           </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="skippedrecords">
           <xsl:call-template name="getStatusCount">
               <xsl:with-param name="counter">SKIPPED RECORDS</xsl:with-param>
           </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="generalfailures">
           <xsl:call-template name="getStatusCount">
               <xsl:with-param name="counter">GENERAL FAILURES</xsl:with-param>
           </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="generalwarnings">
           <xsl:call-template name="getStatusCount">
               <xsl:with-param name="counter">GENERAL WARNINGS</xsl:with-param>
           </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="iofailures">
           <xsl:call-template name="getStatusCount">
               <xsl:with-param name="counter">IO FAILURES</xsl:with-param>
           </xsl:call-template>
       </xsl:variable>
        <TR><TD>
    	        <xsl:value-of select="@name" />
    	    </TD>
    	    <TD>
    	        <xsl:choose>
    	            <xsl:when test="$skippedrecords > 0">
    	                <B><FONT color="{$warncolor}">WARNING:</FONT> </B>
    	                <xsl:value-of select="$skippedrecords"/>
    	                <xsl:text> ITEMS SKIPPED (</xsl:text>
    	                <xsl:value-of select="$testrecords"/><xsl:text> TESTS, </xsl:text>
    	                <xsl:value-of select="$testspassed"/><xsl:text> PASS, </xsl:text>
    	                <xsl:value-of select="$testfailures"/><xsl:text> FAIL, </xsl:text>
    	                <xsl:value-of select="$testwarnings"/><xsl:text> WARN, </xsl:text>
    	                <xsl:value-of select="$generalfailures"/><xsl:text> GEN FAIL, </xsl:text>
    	                <xsl:value-of select="$generalwarnings"/><xsl:text> GEN WARN, </xsl:text>
    	                <xsl:value-of select="$iofailures"/><xsl:text> IO FAIL)</xsl:text>
    	            </xsl:when>
    	            <xsl:when test="$testfailures + $testwarnings + $generalfailures + $generalwarnings + 
    	                             $iofailures + $skippedrecords > 0">
    	                <B><FONT color="{$failcolor}">FAILED</FONT></B>
    	            </xsl:when>
    	            <xsl:otherwise>
    	                <B><FONT color="{$passcolor}">PASSED</FONT></B>
    	            </xsl:otherwise>
    	        </xsl:choose>
    	    </TD>
    	</TR>
    </xsl:template>
    
    <xsl:template name="getStatusCount" >
        <xsl:param name="counter">TOTAL RECORDS</xsl:param>
        <xsl:for-each select="STATUS_ITEM">
            <xsl:if test="@type=$counter" >
    	            <xsl:value-of select="number(STATUS_ITEM_TEXT)" />
    	    </xsl:if>
        </xsl:for-each>    
    </xsl:template>
        
</xsl:stylesheet>
    