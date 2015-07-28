<xsl:stylesheet version='1.0'
		xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
    <xsl:output method="text" encoding="UTF-16" />
    <xsl:strip-space elements="*" />

    <xsl:template match="ApplicationMap" >
        <xsl:for-each select="//Window">
	    <xsl:if test="position() = 1" >
	        <xsl:text>&#xA;[</xsl:text>
	        <xsl:value-of select="@Name" />
	        <xsl:text>]&#xA;</xsl:text>
	    </xsl:if>
            <xsl:if test="not(position() = 1) ">
                <xsl:if test="not(@Name = preceding-sibling::Window/@Name)" >
	            <xsl:text>&#xA;[</xsl:text>
	            <xsl:value-of select="@Name" />
	            <xsl:text>]&#xA;</xsl:text>
                </xsl:if>
            </xsl:if>
            <xsl:value-of select="./ComponentName" />
            <xsl:text>="</xsl:text>
            <xsl:value-of select="./RecognitionText" />
            <xsl:text>"|%|</xsl:text><xsl:value-of select="./ComponentType" />
            <xsl:text>&#xA;</xsl:text>
        </xsl:for-each>    
    </xsl:template>

</xsl:stylesheet>
