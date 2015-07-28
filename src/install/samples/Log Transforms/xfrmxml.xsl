<xsl:stylesheet version='1.0'
		xmlns:xsl='http://www.w3.org/1999/XSL/Transform' >
    
    <xsl:output method='xml' 
                version='1.0' 
                encoding='iso-8859-1' 
                standalone='no' />

    <xsl:variable name="dvd">Test Composer</xsl:variable>
    <xsl:variable name="table">IE6</xsl:variable>
    <xsl:variable name="host">WNT</xsl:variable>
    <xsl:variable name="stage">Build</xsl:variable>
    <xsl:variable name="portdate">2003-10-03</xsl:variable>

    <xsl:template match="/">
    
	<xsl:text disable-output-escaping="yes">&#xD;&#60;TABLE></xsl:text>
    	<xsl:for-each select="/SAFS_LOG/STATUS_REPORT">
	    
	    <xsl:text disable-output-escaping="yes">&#xD; &#60;</xsl:text>
	    <xsl:value-of select="$host"/><xsl:text disable-output-escaping="yes">_BATCH>&#xD;    &#60;dvd></xsl:text>
	    <xsl:value-of select="$dvd"/><xsl:text disable-output-escaping="yes">&#60;/dvd></xsl:text>
	
	    <xsl:text disable-output-escaping="yes">&#xD;    &#60;portdate></xsl:text><xsl:value-of select="$portdate"/>
	    <xsl:text disable-output-escaping="yes">&#60;/portdate></xsl:text>

	    <xsl:text disable-output-escaping="yes">&#xD;    &#60;stage></xsl:text><xsl:value-of select="$stage" />
	    <xsl:text disable-output-escaping="yes">&#60;/stage></xsl:text>

	    <xsl:text disable-output-escaping="yes">&#xD;    &#60;table></xsl:text><xsl:value-of select="$table" />
	    <xsl:text disable-output-escaping="yes">&#60;/table></xsl:text>
	
	    <xsl:text disable-output-escaping="yes">&#xD;    &#60;subtable></xsl:text><xsl:value-of select="@name" />
	    <xsl:text disable-output-escaping="yes">&#60;/subtable></xsl:text>

	    <xsl:text disable-output-escaping="yes">&#xD;    &#60;testname /></xsl:text>
	
	    <xsl:text disable-output-escaping="yes">&#xD;    &#60;starttim></xsl:text>
	        <xsl:value-of select="@date" />T<xsl:value-of select="@time" />
	    <xsl:text disable-output-escaping="yes">&#60;/starttim></xsl:text>

	    <xsl:text disable-output-escaping="yes">&#xD;    &#60;status1></xsl:text>
    	        <xsl:call-template name="status_summary" />
	    <xsl:text disable-output-escaping="yes">&#60;/status1></xsl:text>
    	    
	    <xsl:text disable-output-escaping="yes">&#xD; &#60;/</xsl:text>
	    <xsl:value-of select="$host"/><xsl:text disable-output-escaping="yes">_BATCH></xsl:text>
	    
    	</xsl:for-each>    	
	<xsl:text disable-output-escaping="yes">&#xD;&#60;/TABLE></xsl:text>
    	
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

       <xsl:choose>
           <xsl:when test="$skippedrecords > 0">

    	       <xsl:text> FOLLOWUP </xsl:text>

	   </xsl:when>
    	   <xsl:when test="$testfailures + $testwarnings + $generalfailures + $generalwarnings + 
    	                   $iofailures + $skippedrecords > 0">

    	       <xsl:text> DIFF </xsl:text>

    	   </xsl:when>

    	   <xsl:otherwise>
    	   
    	       <xsl:text> CLEAN </xsl:text>
    	       
    	   </xsl:otherwise>
    	   
        </xsl:choose>
    
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
    