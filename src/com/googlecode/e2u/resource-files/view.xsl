<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:param name="doc" select="'book.pef'"/>
	
	<xsl:template match="/">
		<xsl:processing-instruction name="xml-stylesheet">type="text/xsl" href="pef2xhtml.xsl"</xsl:processing-instruction>
		<xsl:copy-of select="document($doc)"/>
	</xsl:template>
</xsl:stylesheet>
