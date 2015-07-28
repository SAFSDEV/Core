<?xml version="1.0" encoding="UTF-8"?>
<!-- ======================================================================
     XSL stylesheet to perform a transformation from TestComposer (SAFS) 
     log output to JUnit XML report. 
     DEPENDENCIES:
     * Requires the XSLT Standard library XSL from sourceforge.net/projects/xsltsl/
     LIMITATIONS:  
     * Package.CDD is assumed to contain no content other than an invocation 
       of suite Regression.std.
     * Certain information in SAFS (e.g. LOG_VERSION) does not map to JUnit 
       and is not carried over. Likewise there is not enough information in
       the SAFS logs to populate some of the JUnit report tags (e.g. 
       <properties>), and thus some JUnit report tags are omitted. 
     * Because TestComposer tests more at an API/GUI level, the concept of 
       "classes" and other code level entities are not always present in
       the SAFS logs, and thus cannot be included in the JUnit report. 
       Instead, the message text is reported for any "FAILED" tests or
       "WARNING" events.
     * Testcase level timing information is not available in SAFS logs, 
       and cannot be encoded in the JUnit report. 
     * Testsuite level timing is achieved by performing arithmetic 
       operations on the time stamps in the <LOG_OPENED> and <LOG_CLOSED> 
       tags. The calculation assumes that the test run does not last more 
       than 24 hours.
     ====================================================================== -->
<xsl:stylesheet version='1.0'
		xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
		xmlns:dt='http://xsltsl.org/date-time' extension-element-prefixes="dt">

    <xsl:import href="http://xsltsl.sourceforge.net/modules/stdlib.xsl"/>

    <!-- Output will be XML -->
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <!-- Variables for computing the overall elapsed time of test run -->
    <xsl:variable name="startDate" select="/SAFS_LOG/LOG_OPENED/@date"/>
    <xsl:variable name="endDate"   select="/SAFS_LOG/LOG_CLOSED/@date"/>
    <xsl:variable name="startTime" select="/SAFS_LOG/LOG_OPENED/@time"/>
    <xsl:variable name="endTime"   select="/SAFS_LOG/LOG_CLOSED/@time"/>
    
    <xsl:variable name="startHours">
        <xsl:call-template name="dt:get-xsd-datetime-hour">
            <xsl:with-param name="xsd-date-time">
                <xsl:value-of select="$startTime"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="startMins">
        <xsl:call-template name="dt:get-xsd-datetime-minute">
            <xsl:with-param name="xsd-date-time">
                <xsl:value-of select="$startTime"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="startSecs">
        <xsl:call-template name="dt:get-xsd-datetime-second">
            <xsl:with-param name="xsd-date-time">
                <xsl:value-of select="$startTime"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="endHours">
        <xsl:call-template name="dt:get-xsd-datetime-hour">
            <xsl:with-param name="xsd-date-time">
                <xsl:value-of select="$endTime"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="endMins">
        <xsl:call-template name="dt:get-xsd-datetime-minute">
            <xsl:with-param name="xsd-date-time">
                <xsl:value-of select="$endTime"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="endSecs">
        <xsl:call-template name="dt:get-xsd-datetime-second">
            <xsl:with-param name="xsd-date-time">
                <xsl:value-of select="$endTime"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:variable>

    <!-- Convert all times to seconds -->
    <xsl:variable name="startTimeSecs"> 
        <xsl:value-of select="$startHours*60*60 + $startMins*60 + $startSecs"/>
    </xsl:variable>

    <xsl:variable name="endTimeSecs"> 
        <xsl:value-of select="$endHours*60*60 + $endMins*60 + $endSecs"/>
    </xsl:variable>

    <!-- Compute elapsed time of overall test run -->
    <xsl:variable name="timeElapsed">
        <xsl:choose>
        <xsl:when test="$startDate = $endDate">
            <xsl:value-of select="$endTimeSecs - $startTimeSecs"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="24*60*60 - $startTimeSecs + $endTimeSecs"/>
        </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <!-- ================================== 
         NOW, START PROCESSING THE LOG FILE 
         ================================== -->
    <!-- Document Root -->
    <xsl:template match="/">
        <!-- Create the JUnit <testsuite> tag for all TestComposer suites. 
             We ignore the Package.CDD and any other "cycle" status reports, 
             since they will always just roll-up the results of the suites 
             they call.                                                    -->
        <xsl:apply-templates 
             select="SAFS_LOG/STATUS_REPORT[not(contains(@name,'.CDD') or
                                                contains(@name,'.cdd'))]" />
    </xsl:template>

    <!-- STATUS_REPORT -->
    <xsl:template match="STATUS_REPORT">
        <!-- NOTES: ======================================================
             * "TOTAL RECORDS" from TestComposer log does not map to the
               JUnit format, and is not carried over. 
             * "TEST WARNINGS" and "TEST FAILURES" are added together to
               report as JUnit Failures. 
             * "GENERAL FAILURES", "GENERAL WARNINGS", and "IO FAILURES" 
               are added together to report as JUnit Errors. 
             * "WARNINGS" and "SKIPPED RECORDS" are reported using the 
               JUnit <failure> tag, since they seem to carry important 
               information. 
             ============================================================= -->
        <testsuite 
            name="{@name}"
            tests="{STATUS_ITEM[@type='TEST RECORDS']/STATUS_ITEM_TEXT}"
            failures="{STATUS_ITEM[@type='TEST FAILURES']/STATUS_ITEM_TEXT +
                       STATUS_ITEM[@type='TEST WARNINGS']/STATUS_ITEM_TEXT}" 
            errors="{STATUS_ITEM[@type='GENERAL FAILURES']/STATUS_ITEM_TEXT +
                     STATUS_ITEM[@type='GENERAL WARNINGS']/STATUS_ITEM_TEXT +
                     STATUS_ITEM[@type='IO FAILURES']/STATUS_ITEM_TEXT}" 
            time="{$timeElapsed}" >
            
            <!-- Report any failures here -->
            <xsl:apply-templates 
                 select="/SAFS_LOG/LOG_MESSAGE[@type='FAILED']"/>
            
            <!-- Report any Warnings here -->
            <xsl:apply-templates 
                 select="/SAFS_LOG/LOG_MESSAGE[@type='WARNING']"/>
            
            <!-- Report any Skipped Records here -->
            <xsl:apply-templates 
                 select="/SAFS_LOG/LOG_MESSAGE[@type='SKIPPED RECORD']"/>
            
            <!-- Perhaps we can place all the "GENERIC" and other such 
                 SAFS log output in the JUnit stdout in the future?       -->
            <system-out><![CDATA[]]></system-out>
            <system-err><![CDATA[]]></system-err>
            
        </testsuite>
    </xsl:template>
    
    <!-- LOG_MESSAGE type="FAILED" -->
    <xsl:template match="LOG_MESSAGE[@type='FAILED']">
        <testcase name="{./preceding-sibling::LOG_MESSAGE[@type='START TESTCASE'][1]/MESSAGE_TEXT[text()]}" classname="{./preceding-sibling::LOG_MESSAGE[@type='START SUITE'][1]/MESSAGE_TEXT[text()]}" time="?">
            <failure message="{MESSAGE_TEXT}" 
                     type="{@type}">
                <xsl:value-of select="MESSAGE_DETAILS"/>
            </failure>
        </testcase>
    </xsl:template>

    <!-- LOG_MESSAGE type="...WARNING..." -->
    <!-- We report any warnings as failures, in line with TestComposer -->
    <xsl:template match="LOG_MESSAGE[@type='WARNING']">
        <testcase name="{./preceding-sibling::LOG_MESSAGE[@type='START TESTCASE'][1]/MESSAGE_TEXT[text()]}" classname="{./preceding-sibling::LOG_MESSAGE[@type='START SUITE'][1]/MESSAGE_TEXT[text()]}"  time="?">
            <failure message="{MESSAGE_TEXT}" 
                     type="{@type}">
                <xsl:value-of select="MESSAGE_DETAILS"/>
            </failure>
        </testcase>
    </xsl:template>

    <!-- LOG_MESSAGE type="SKIPPED RECORD" -->
    <!-- These are intended to indicate that perhaps not all testing 
         that should be done has been done                             -->
    <xsl:template match="LOG_MESSAGE[@type='SKIPPED RECORD']">
        <testcase name="{./preceding-sibling::LOG_MESSAGE[@type='START TESTCASE'][1]/MESSAGE_TEXT[text()]}" classname="{./preceding-sibling::LOG_MESSAGE[@type='START SUITE'][1]/MESSAGE_TEXT[text()]}" time="?">
            <failure message="{MESSAGE_TEXT}" 
                     type="{@type}">
                <xsl:value-of select="MESSAGE_DETAILS"/>
            </failure>
        </testcase>
    </xsl:template>
    
</xsl:stylesheet>

