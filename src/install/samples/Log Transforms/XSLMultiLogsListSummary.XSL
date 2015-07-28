<xsl:stylesheet version='2.0'
		xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
    
    <xsl:output method="xml" encoding="UTF-8" indent="yes" />
    
    <xsl:template match="filelist" >    
    	<multilog-summary>
	    <xsl:for-each select="file" >            
		<xsl:variable name="log" select="document(@href)" />
		<xsl:apply-templates select="$log/SAFS_LOG" >
		    <xsl:with-param name="logname">
		        <xsl:value-of select="@href"/>
		    </xsl:with-param>
		</xsl:apply-templates>
	    </xsl:for-each>    

 	    <xsl:variable name="total" >
	        <xsl:for-each select="file" >
		    <xsl:variable name="log2" select="document(@href)" />		
		    <xsl:apply-templates select="$log2/SAFS_LOG" mode="total" />
	        </xsl:for-each>    
	    </xsl:variable>
	    
	    <!-- xsl:copy-of select="$total" / -->
	    
	    <tests>
	        <xsl:value-of select="sum($total/tests)" />
	    </tests>
	    <passed>
	        <xsl:value-of select="sum($total/passed)" />
	    </passed>
	    <failed>
	        <xsl:value-of select="sum($total/failed)" />
	    </failed>
	    <errors>
	        <xsl:value-of select="sum($total/errors)" />
	    </errors>
	    <skipped>
	        <xsl:value-of select="sum($total/skipped)" />
	    </skipped>
	    
        </multilog-summary>
    </xsl:template>

    <xsl:template match="SAFS_LOG">
        <xsl:param name="logname"></xsl:param>
        <test-log 
            name="{$logname}"
            startDate="{./LOG_OPENED/@date}"
            startTime="{./LOG_OPENED/@time}"
            endDate="{./LOG_CLOSED/@date}"
            endTime="{./LOG_CLOSED/@time}" >
            
            <xsl:apply-templates select="./STATUS_REPORT[last()]" />
            <!-- xsl:apply-templates select="./STATUS_REPORT" /  -->
	         
        </test-log>
    </xsl:template>

    <xsl:template match="STATUS_REPORT" >
        <status 
            name="{@name}"
            date="{@date}"
            time="{@time}"
            tests="{STATUS_ITEM[@type='TEST RECORDS']/STATUS_ITEM_TEXT}"
            passed="{STATUS_ITEM[@type='TESTS PASSED']/STATUS_ITEM_TEXT}"
            failed="{STATUS_ITEM[@type='TEST FAILURES']/STATUS_ITEM_TEXT +
                       STATUS_ITEM[@type='TEST WARNINGS']/STATUS_ITEM_TEXT}" 
            errors="{STATUS_ITEM[@type='GENERAL FAILURES']/STATUS_ITEM_TEXT +
                     STATUS_ITEM[@type='GENERAL WARNINGS']/STATUS_ITEM_TEXT +
                     STATUS_ITEM[@type='IO FAILURES']/STATUS_ITEM_TEXT}" 
            skipped="{STATUS_ITEM[@type='SKIPPED RECORDS']/STATUS_ITEM_TEXT}" >
        </status>
    </xsl:template>

    <xsl:template match="SAFS_LOG" mode="total" >
        <xsl:apply-templates select="./STATUS_REPORT[last()]" mode="total" />
    </xsl:template>
    
    <xsl:template match="STATUS_REPORT" mode="total" >
        <tests> 
            <xsl:value-of select="STATUS_ITEM[@type='TEST RECORDS']/STATUS_ITEM_TEXT" />
        </tests>
        <passed> 
            <xsl:value-of select="STATUS_ITEM[@type='TESTS PASSED']/STATUS_ITEM_TEXT" />
        </passed>
        <failed> 
            <xsl:value-of select="STATUS_ITEM[@type='TEST FAILURES']/STATUS_ITEM_TEXT + 
                                  STATUS_ITEM[@type='TEST WARNINGS']/STATUS_ITEM_TEXT" />
        </failed>
        <errors> 
            <xsl:value-of select="STATUS_ITEM[@type='GENERAL FAILURES']/STATUS_ITEM_TEXT + 
                                  STATUS_ITEM[@type='GENERAL WARNINGS']/STATUS_ITEM_TEXT + 
                                  STATUS_ITEM[@type='IO FAILURES']/STATUS_ITEM_TEXT" />
        </errors>
        <skipped> 
            <xsl:value-of select="STATUS_ITEM[@type='SKIPPED RECORDS']/STATUS_ITEM_TEXT" />
        </skipped>
    </xsl:template>

</xsl:stylesheet>
