<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- Indent elements not in inline context -->
	<xsl:template match="*[count(parent::*/text()[normalize-space()!=''])=0]">
		<xsl:if test="following-sibling::* or preceding-sibling::* or count(descendant::*) &gt; 1">	
			<xsl:text>&#x0a;</xsl:text>
			<xsl:for-each select="ancestor::*">
				<xsl:text>&#x09;</xsl:text>
			</xsl:for-each>
		</xsl:if>
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
			<xsl:if test="not(text()[normalize-space()!='']) and count(descendant::*) &gt; 1">
				<xsl:text>&#x0a;</xsl:text>
				<xsl:for-each select="ancestor::*">
					<xsl:text>&#x09;</xsl:text>
				</xsl:for-each>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	
	<!-- remove whitespace text nodes not in inline context -->
	<xsl:template match="text()[count(parent::*/text()[normalize-space()!=''])=0]"/>
	
	<xsl:template match="*|comment()|processing-instruction()">
		<xsl:call-template name="copy"/>
	</xsl:template>

	<xsl:template name="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>