<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:dc="http://purl.org/dc/elements/1.1/"
xmlns:tpb="http://www.tpb.se/pef/meta/"
xmlns:pef="http://www.daisy.org/ns/2008/pef"
xmlns:ext="http://www.daisy.org/ns/2008/pef/extended"
xmlns="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="dc tpb pef ext">
<!-- <xsl:output method="xml" encoding="windows-1252" indent="yes" omit-xml-declaration="yes"/> -->
<xsl:output method="html" encoding="UTF-8" indent="yes" omit-xml-declaration="no" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>

<xsl:param name="return-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'return-label'"/>
		<xsl:with-param name="default" select="'Åter'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="toggle-view-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'toggle-view-label'"/>
		<xsl:with-param name="default" select="'Växla vy'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="show-source">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'show-source'"/>
		<xsl:with-param name="default" select="'Visa Källa'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="emboss-view-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'emboss-view-label'"/>
		<xsl:with-param name="default" select="'Skriv Ut'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="preview-view-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'preview-view-label'"/>
		<xsl:with-param name="default" select="'FörhandsGranska'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="viewing-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'viewing-label'"/>
		<xsl:with-param name="default" select="'Visar'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="about-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'about-label'"/>
		<xsl:with-param name="default" select="'Om Boken'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="find-view-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'find-view-label'"/>
		<xsl:with-param name="default" select="'Loc?'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="setup-view-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'setup-view-label'"/>
		<xsl:with-param name="default" select="'Loc?'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="about-software-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'about-software-label'"/>
		<xsl:with-param name="default" select="'Loc?'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="go-to-page-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'go-to-page-label'"/>
		<xsl:with-param name="default" select="'Sida'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="page-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'page-label'"/>
		<xsl:with-param name="default" select="'Sida'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="sheets-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'sheets-label'"/>
		<xsl:with-param name="default" select="'Sheets'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="section-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'section-label'"/>
		<xsl:with-param name="default" select="'Avsnitt'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="volume-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'volume-label'"/>
		<xsl:with-param name="default" select="'Volym'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="showing-pages-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'showing-pages-label'"/>
		<xsl:with-param name="default" select="'Visar Sida'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="unknown-author-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'unknown-author-label'"/>
		<xsl:with-param name="default" select="'[Unknown author]'"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="unknown-title-label">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'unknown-title-label'"/>
		<xsl:with-param name="default" select="'[Unknown title]'"/>
	</xsl:call-template>
</xsl:param>
	<!--
<xsl:param name="go-to-page-label" select="'Sida'"/>
<xsl:param name="page-label" select="'Sida'"/>
<xsl:param name="sheets-label" select="'Sheets'"/>
<xsl:param name="section-label" select="'Avsnitt'"/>
<xsl:param name="volume-label" select="'Volym'"/>
<xsl:param name="showing-pages-label" select="'Visar Sida'"/>
<xsl:param name="unknown-author-label" select="'[Unknown author]'"/>
<xsl:param name="unknown-title-label" select="'[Unknown title]'"/>-->
<xsl:param name="textFont">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'textFont'"/>
		<xsl:with-param name="default" select="''"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="brailleFont">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'brailleFont'"/>
		<xsl:with-param name="default" select="''"/>
	</xsl:call-template>
</xsl:param>
<xsl:param name="number-sign" select="'⠼'"/>
<!-- Non number characters that do not break the number sequence -->
<xsl:param name="ignorable-number-separators" select="'⠄⠂'"/>
<xsl:param name="code">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'code'"/>
		<xsl:with-param name="default" select="' a,b.k;l^cif/msp´e:h*o!r~djgäntq_å?ê-u(v@îöë§xèç&quot;û+ü)z=à|ôwï#yùé'"/>
	</xsl:call-template>
</xsl:param>

<xsl:param name="volume">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'volume'"/>
		<xsl:with-param name="default" select="''"/>
	</xsl:call-template>
</xsl:param>

<xsl:param name="uriString">
	<xsl:call-template name="getDefault">
		<xsl:with-param name="name" select="'uri-string'"/>
		<xsl:with-param name="default" select="''"/>
	</xsl:call-template>
</xsl:param>

<!--
	 Template to get default value for parameters:
		name - the name of the parameter as identified in a settings file
		default - the default value to use
-->
<xsl:template name="getDefault">
	<xsl:param name="name"/>
	<xsl:param name="default"/>
	<xsl:choose>
		<xsl:when test="/pef:pef">
			<xsl:value-of select="$default"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:choose>
				<xsl:when test="/settings/param[@name=$name][1]/@value">
					<xsl:value-of select="/settings/param[@name=$name][1]/@value"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$default"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="/">
	<!-- if the current document is a pef-file, use this document as input, otherwise use the -->
	<xsl:choose>
		<xsl:when test="pef:pef"><xsl:apply-templates select="pef:pef"/></xsl:when>
		<xsl:otherwise><xsl:apply-templates select="document(/settings/param[@name='uri'][1]/@value)/pef:pef"/></xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="pef:page" mode="currentPageNumber">
	<xsl:variable name="pageoffset" select="count(preceding::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='false']]/descendant::pef:page)*2 + count(preceding::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='true']]/descendant::pef:page) + count(preceding::pef:section[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])
	"/>
	<xsl:variable name="mult">
		<xsl:choose>
			<xsl:when test="ancestor-or-self::pef:*[@duplex][1][@duplex='false']">2</xsl:when>
			<xsl:otherwise>1</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="sectid" select="generate-id(ancestor::pef:section)"/>
	<xsl:variable name="pagenum" select="count(preceding::pef:page[generate-id(ancestor::pef:section) = $sectid]) * $mult + 1 + $pageoffset"/>
	<xsl:value-of select="$pagenum"/>
</xsl:template>
 
<xsl:template match="pef:pef">
	<xsl:variable name="firstPage">
		<xsl:choose>
			<xsl:when test="$volume!=''">
				<xsl:apply-templates select="(//pef:volume)[position()=$volume]/descendant::pef:page[1]" mode="currentPageNumber"/>
			</xsl:when>
			<xsl:otherwise>1</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="lastPage">
		<xsl:choose>
			<xsl:when test="$volume!=''">
				<xsl:apply-templates select="(//pef:volume)[position()=$volume]/descendant::pef:page[last()]" mode="currentPageNumber"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="(//pef:page)[last()]" mode="currentPageNumber"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<html>
		<xsl:apply-templates>
			<xsl:with-param name="firstPage" select="$firstPage"/>
			<xsl:with-param name="lastPage" select="$lastPage"/>
		</xsl:apply-templates>
	</html>
</xsl:template>

<xsl:template match="pef:head">
	<head>
		<xsl:copy-of select="@*"/>
		<title><xsl:value-of select="concat($viewing-label ,' ', pef:meta/dc:identifier/text())"/></title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="Content-Style-Type" content="text/css" />
		<xsl:apply-templates/>
		<link rel="stylesheet" type="text/css" href="styles/default/base.css"/>
		<link rel="stylesheet" type="text/css" href="styles/default/layout.css"/>
		<link rel="stylesheet" type="text/css" href="styles/default/theme.css"/>
		<link rel="stylesheet" type="text/css" href="styles/default/state.css"/>
		<link rel="stylesheet" type="text/css" href="chosen/chosen.css"/>
		<xsl:if test="$textFont!='' or $brailleFont!=''">
			<!-- Only output this tag if either text or braille font has a value -->
			<style type="text/css">
				<xsl:if test="$textFont!=''">
						pre.text {
							font-family: "<xsl:value-of select="$textFont"/>";
							<!-- Optimize the default font -->
							<xsl:choose>
								<xsl:when test="starts-with($textFont, 'odt2braille') and starts-with($brailleFont, 'odt2braille')">
									letter-spacing: 0px;
									font-size: 26px;
								</xsl:when>
								<xsl:when test="starts-with($textFont, 'odt2braille')">
									letter-spacing: 1px;		
								</xsl:when>
								<xsl:otherwise>
									font-size: 20px;
									letter-spacing: 0px;
								</xsl:otherwise>
							</xsl:choose>
						}
					</xsl:if>
					<xsl:if test="$brailleFont!=''">
						pre.braille {
							font-family: "<xsl:value-of select="$brailleFont"/>";
							<!-- Optimize the default font -->
							<xsl:choose>
								<xsl:when test="starts-with($textFont, 'odt2braille') and starts-with($brailleFont, 'odt2braille')">
									letter-spacing: 0px;
									font-size: 26px;
								</xsl:when>
								<xsl:when test="starts-with($brailleFont, 'odt2braille')">
									font-size: 26px;
								</xsl:when>
								<xsl:otherwise>							
									font-size: 20px;
									letter-spacing: 0px;
								</xsl:otherwise>
							</xsl:choose>
						}
					</xsl:if>
			</style>
		</xsl:if>
		<script src="script/shortcuts.js"/>
		<script src="script/preview.js"/>
	</head>
</xsl:template>

<xsl:template match="pef:meta"/>

<xsl:template match="pef:body">
	<xsl:param name="firstPage"/>
	<xsl:param name="lastPage"/>
	<body class="preview">
		<xsl:copy-of select="@*"/>
		<div id="view">
			<form action="close.html" method="get">
			<p>
				<span id="item-emboss"><a href="/"><xsl:value-of select="$emboss-view-label"/></a></span>
				<span><a href="view.html"><xsl:value-of select="$preview-view-label"/></a></span>
				<span><a href="index.html?method=meta"><xsl:value-of select="$about-label"/></a></span>
				<!-- <span><a href="index.html?method=find"><xsl:value-of select="$find-view-label"/></a></span> -->
				<span><a href="index.html?method=setup"><xsl:value-of select="$setup-view-label"/></a></span>
				<!-- <span><a href="index.html?method=about"><xsl:value-of select="$about-software-label"/></a></span> -->
				<input id="connected" type="submit" value="" title="Avsluta"></input>
				<input id="notConnected" type="submit" value="" title="Avsluta" disabled="disabled"></input>
			</p>
			</form>

		<div id="top-nav">
			<p>
			<span><a href="#" onclick="toggleViews();return false;" accesskey="V"><xsl:value-of select="$toggle-view-label"/></a></span>
			<!-- 
			<span>
				<a href="#" onclick="window.open('book.xml','source'); return false;"><xsl:value-of select="$show-source"/></a>
			</span>
			 -->
			<span>
			 	<select onchange="location = this.options[this.selectedIndex].value;" id="volume-select" class="chosen-select">
					<xsl:for-each select="//pef:volume">
						<xsl:variable name="pos" select="position()"/>
	
						<option value="{$uriString}&amp;volume={$pos}" title="{concat(' (',(count(descendant::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='false']]/descendant::pef:page)*2 + count(descendant::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='true']]/descendant::pef:page) + count(descendant::pef:section[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])) div 2,' ', $sheets-label, ')')}"><xsl:if test="position()=$volume">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if><xsl:value-of select="concat($volume-label, ' ', position())"/>
						</option>
						<xsl:for-each select="descendant::pef:section">
							<option value="{$uriString}&amp;volume={$pos}#{generate-id(.)}" title="{concat(' (',(count(self::*[ancestor-or-self::pef:*[@duplex][1][@duplex='false']]/descendant::pef:page)*2 + count(self::*[ancestor-or-self::pef:*[@duplex][1][@duplex='true']]/descendant::pef:page) + count(self::*[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])) div 2,' ', $sheets-label, ')')}">&#xA0;&#xA0;&#xA0;<xsl:value-of select="concat($section-label, ' ', position())"/>

							</option>
						</xsl:for-each>
					</xsl:for-each>
				</select>
			</span>
			<span><xsl:value-of select="concat($go-to-page-label, ' ')"/><input id="gotoPage" type="text" size="4"  onkeyup="if (event.keyCode==13) {{gotoPage();}}" value="{$firstPage}"></input>
			</span>

			</p>
			
			
			<!--
        <p>
			 <input type="button" onclick="movePage(-2);" value="-2"/>
			 <input type="button" onclick="movePage(-1);" value="-1" accesskey="q"/>
			 <input type="button" onclick="movePage(1);" value="+1" accesskey="z"/>
			 <input type="button" onclick="movePage(2);" value="+2"/>
		</p>-->
		
		<!--  
		 -->
		 

		 
<!-- 
 -->
 <!-- 
			<ul>
				<li><xsl:value-of select="$volume-label"/></li>
				<xsl:for-each select="//pef:volume">
					<xsl:choose>
						<xsl:when test="$volume='' or position()=$volume">
						<li class="highlight" 
						onmouseover="document.getElementById('section-{generate-id(.)}').style.display = 'inline';"
						onmouseout="document.getElementById('section-{generate-id(.)}').style.display = 'none';"><xsl:value-of select="position()"/>
						<!-
							<a href="#{generate-id(descendant::pef:page[1])}">
								<xsl:if test="position()&lt;=9">
									<xsl:attribute name="accesskey"><xsl:value-of select="position()"/></xsl:attribute>
								</xsl:if>
							<xsl:value-of select="position()"/></a>->
			<span id="section-{generate-id(.)}" style="display: none">
							(
							<ul>
								<xsl:for-each select="descendant::pef:section">
								<!-  descendant::pef:page[1] ->
									<li><a href="#{generate-id(.)}"><xsl:value-of select="position()"/></a></li>
								</xsl:for-each>
							</ul>
							)
						</span>
						</li>
					</xsl:when>
					<xsl:when test="position()!=$volume and $uriString!=''" >
					<xsl:variable name="pos" select="position()"/>
						<li onmouseover="document.getElementById('section-{generate-id(.)}').style.display = 'inline';"
						onmouseout="document.getElementById('section-{generate-id(.)}').style.display = 'none';"><a href="{$uriString}&amp;volume={position()}"><xsl:value-of select="position()"/></a>
			<span id="section-{generate-id(.)}" style="display: none">
							(
							<ul>
								<xsl:for-each select="descendant::pef:section">
								<!-  descendant::pef:page[1] ->
									<li><a href="{$uriString}&amp;volume={$pos}#{generate-id(.)}"><xsl:value-of select="position()"/></a></li>
								</xsl:for-each>
							</ul>
							)
						</span>
</li>
					</xsl:when>
					<xsl:otherwise></xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</ul>
		
		<div id="navigate">
			<div id="inner">

			</div>
			<div id="bottom"><xsl:text>&#xA0;</xsl:text></div>
		</div>
		 -->
		</div>
	</div>
		<div id="about">
			<p id="close-bar"><input type="button" onclick="document.getElementById('about').style.visibility='hidden';" value="X"/></p>
			<div id="about-content">
				<p><xsl:value-of select="//pef:meta/dc:identifier[1]/text()"/>
				<xsl:if test="//pef:meta/dc:source"> (<xsl:value-of select="//pef:meta/dc:source[1]/text()"/>)</xsl:if></p>
				<p><strong>
					<xsl:choose>
						<xsl:when test="//pef:meta/dc:title[1]/text()"><xsl:value-of select="//pef:meta/dc:title[1]/text()"/></xsl:when>
						<xsl:otherwise><xsl:value-of select="$unknown-title-label"/></xsl:otherwise>
					</xsl:choose>
				</strong>
				<br/>
					<strong>
					<xsl:choose>
						<xsl:when test="//pef:meta/dc:creator">
							<xsl:for-each select="//pef:meta/dc:creator">
								<xsl:if test="position()>1"><xsl:text>; </xsl:text></xsl:if><xsl:value-of select="text()"/>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise><xsl:value-of select="$unknown-author-label"/></xsl:otherwise>
					</xsl:choose>
					</strong>
				</p>
				<xsl:if test="//pef:meta/dc:date">
				<p><xsl:value-of select="//pef:meta/dc:date[last()]"/></p>
				</xsl:if>
				<p><xsl:value-of select="concat($showing-pages-label, ': ', $firstPage, '-', $lastPage)"/></p>
				<!--
				<p><strong>Antal blad: </strong><xsl:value-of select="1"/>
				<br/>
				<strong>Kilotecken: </strong><xsl:value-of select="1"/>
				</p>-->
			</div>
		</div>
		<xsl:apply-templates/>
		<script src="chosen/jquery-1.6.4.min.js" type="text/javascript"></script>
  		<script src="chosen/chosen.jquery.js" type="text/javascript"></script>
  		<script type="text/javascript">
		    var config = {
		      '.chosen-select'           : {},
		      '.chosen-select-deselect'  : {allow_single_deselect:true},
		      '.chosen-select-no-single' : {disable_search_threshold:10},
		      '.chosen-select-no-results': {no_results_text:'Oops, nothing found!'},
		      '.chosen-select-width'     : {width:"95%"}
		    }
		    for (var selector in config) {
		      $(selector).chosen(config[selector]);
		    }
	  </script>
	</body>
</xsl:template>

<xsl:template match="pef:volume">
	<xsl:if test="$volume='' or $volume=count(preceding::pef:volume)+1">
		<div class="volume" id="{generate-id(.)}">

			<p class="volume-header">
				<xsl:value-of select="concat($volume-label, ' ', count(preceding::pef:volume)+1)"/>
				
				<xsl:value-of select="concat(' (',(count(descendant::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='false']]/descendant::pef:page)*2 + count(descendant::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='true']]/descendant::pef:page) + count(descendant::pef:section[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])) div 2,' ', $sheets-label, ')')"/>
				
			</p>
			<xsl:apply-templates/>
		</div>
	</xsl:if>
</xsl:template>

<xsl:template match="pef:section">
	<div class="section" id="{generate-id(.)}">
		<xsl:variable name="volumeid" select="generate-id(ancestor::pef:volume)"/>
		<p class="section-header">
			<xsl:value-of select="concat($section-label, ' ', count(preceding::pef:section[generate-id(ancestor::pef:volume)=$volumeid])+1)"/>
			<xsl:value-of select="concat(' (',(count(self::*[ancestor-or-self::pef:*[@duplex][1][@duplex='false']]/descendant::pef:page)*2 + count(self::*[ancestor-or-self::pef:*[@duplex][1][@duplex='true']]/descendant::pef:page) + count(self::*[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])) div 2,' ', $sheets-label, ')')"/>
		</p>
		<xsl:apply-templates/>
	</div>
</xsl:template>

<xsl:template match="pef:page">
	<!--
		<xsl:variable name="pageoffset" select="count(preceding::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='false']]/descendant::pef:page)*2 + count(preceding::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='true']]/descendant::pef:page) + count(preceding::pef:section[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])
		"/>
		<xsl:variable name="mult">
			<xsl:choose>
				<xsl:when test="ancestor-or-self::pef:*[@duplex][1][@duplex='false']">2</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
	
	<xsl:variable name="sectid" select="generate-id(ancestor::pef:section)"/>
	<xsl:variable name="pagenum" select="count(preceding::pef:page[generate-id(ancestor::pef:section) = $sectid]) * $mult + 1 + $pageoffset"/>-->
	<xsl:variable name="pagenum"><xsl:apply-templates select="." mode="currentPageNumber"/></xsl:variable>
	<xsl:variable name="pageid" select="generate-id(.)"/>
	<div id="{$pageid}" onmouseover="setPage({$pagenum});">
		<xsl:choose>
			<xsl:when test="generate-id(ancestor::pef:section/descendant::pef:page[1])=$pageid"><xsl:attribute name="class">cont first</xsl:attribute></xsl:when>
			<xsl:when test="$pagenum mod 2 = 0"><xsl:attribute name="class">cont even</xsl:attribute></xsl:when>
			<xsl:otherwise><xsl:attribute name="class">cont odd</xsl:attribute></xsl:otherwise>
		</xsl:choose>
	<p class="page-header" id="{concat('pagenum', $pagenum)}">
		<xsl:variable name="volumeid" select="generate-id(ancestor::pef:volume)"/>
		<xsl:value-of select="concat($volume-label, ' ', count(preceding::pef:volume)+1)"/>
		<xsl:text>, </xsl:text>
		<xsl:value-of select="concat($section-label, ' ', count(preceding::pef:section[generate-id(ancestor::pef:volume)=$volumeid])+1)"/>
		<xsl:text> | </xsl:text>
		<xsl:value-of select="concat($page-label, ' ', $pagenum)"/>
	</p>
	
	<xsl:variable name="defaultRowgap" select="ancestor::*[@rowgap][1]/@rowgap"/>
	<xsl:variable name="totalRowgap" select="sum(pef:row[@rowgap]/@rowgap) + count(pef:row[not(@rowgap)]) * $defaultRowgap + count(pef:row) * 4"/>
	<xsl:variable name="usedLines" select="ceiling($totalRowgap div 4)"/>

	<xsl:comment><xsl:value-of select="$usedLines"/></xsl:comment>
	<div class="posrel">
		<div class="page">
		<table>
			<xsl:apply-templates/>
			<xsl:call-template name="insertRow">
				<xsl:with-param name="element" select="'td'"/>
				<xsl:with-param name="class" select="'braille'"/>
				<xsl:with-param name="i" select="ancestor::pef:*[@rows][1]/@rows - $usedLines"/>
			</xsl:call-template>
		</table>
		</div>
		<div class="text">
		<table>
			<xsl:apply-templates>
				<xsl:with-param name="translate" select="true()"/>
			</xsl:apply-templates>
			<xsl:call-template name="insertRow">
				<xsl:with-param name="element" select="'td'"/>
				<xsl:with-param name="i" select="ancestor::pef:*[@rows][1]/@rows - $usedLines"/>
			</xsl:call-template>
		</table>
		</div>
	</div>
	</div>
</xsl:template>

<xsl:template name="insertRow">
	<xsl:param name="element">p</xsl:param>
	<xsl:param name="class"/>
	<xsl:param name="i">0</xsl:param>
	<xsl:param name="height">1</xsl:param>
	<xsl:if test="$i&gt;0"><tr><xsl:element name="{$element}">
		<xsl:call-template name="heightAttribute"><xsl:with-param name="height" select="$height"></xsl:with-param></xsl:call-template>
		<xsl:if test="$class!=''"><xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute></xsl:if><xsl:text>&#xA0;</xsl:text></xsl:element></tr>
	<xsl:call-template name="insertRow">
			<xsl:with-param name="i" select="$i - 1"/>
			<xsl:with-param name="element" select="$element"/>
			<xsl:with-param name="class" select="$class"/>
	</xsl:call-template></xsl:if>	
</xsl:template>

<xsl:template name="insertBlank">
	<xsl:param name="i">0</xsl:param>
	<xsl:param name="char">&#x2800;</xsl:param>
	<xsl:if test="$i&gt;0"><xsl:value-of select="$char"/><xsl:call-template name="insertBlank">
			<xsl:with-param name="i" select="$i - 1"/>
			<xsl:with-param name="char" select="$char"/>
		</xsl:call-template></xsl:if>
</xsl:template>

<xsl:template name="toNumbers">
	<xsl:param name="text"/>
	<xsl:param name="active" select="0"/>
	<xsl:if test="$text!=''">
		<xsl:variable name="inChar" select="substring($text, 1, 1)"/>
		<xsl:variable name="outChar">
			<xsl:choose>
				<xsl:when test="$active=1">
					<xsl:value-of select="translate($inChar, '⠚⠁⠃⠉⠙⠑⠋⠛⠓⠊', '0123456789')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$inChar"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- Output the next char -->
		<xsl:value-of select="$outChar"/>
		<xsl:variable name="setActive">
			<xsl:choose>
				<xsl:when test="$inChar!=$outChar or $inChar=$number-sign"><xsl:value-of select="1"/></xsl:when>
				<xsl:when test="$active=1 and (translate($inChar, $ignorable-number-separators, '')='')"><xsl:value-of select="1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="0"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:call-template name="toNumbers">	
			<xsl:with-param name="text" select="substring($text, 2)"/>
			<xsl:with-param name="active" select="$setActive"/>
		</xsl:call-template>
	</xsl:if>
</xsl:template>

<xsl:template name="heightAttribute">
	<xsl:param name="height">1</xsl:param>
	<xsl:attribute name="style">height: <xsl:value-of select="$height * 26"/>px;</xsl:attribute>
</xsl:template>

<xsl:template match="pef:row">
	<xsl:param name="translate" select="false()"/>
	<xsl:element name="tr">
	<xsl:element name="td">
		<xsl:choose>
			<xsl:when test="$translate=true()">
				<xsl:attribute name="class">text</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="class">braille</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:variable name="rowgap"><xsl:choose>
			<xsl:when test="@rowgap"><xsl:value-of select="@rowgap"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="ancestor::*[@rowgap][1]/@rowgap"/></xsl:otherwise>
		</xsl:choose></xsl:variable>
		<xsl:call-template name="heightAttribute"><xsl:with-param name="height" select="1 + ($rowgap div 4)"></xsl:with-param></xsl:call-template>
		
		<xsl:choose>
			<xsl:when test="$translate=true()">
				<xsl:choose>
					<xsl:when test="@ext:text"><span class="extended"><xsl:value-of select="@ext:text"/></span></xsl:when>
					<xsl:otherwise>
						<xsl:variable name="preTrans">
							<xsl:call-template name="toNumbers">
								<xsl:with-param name="text" select="."/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:variable name="trans" select="translate($preTrans, 
					'⠀⠁⠂⠃⠄⠅⠆⠇⠈⠉⠊⠋⠌⠍⠎⠏⠐⠑⠒⠓⠔⠕⠖⠗⠘⠙⠚⠛⠜⠝⠞⠟⠠⠡⠢⠣⠤⠥⠦⠧⠨⠩⠪⠫⠬⠭⠮⠯⠰⠱⠲⠳⠴⠵⠶⠷⠸⠹⠺⠻⠼⠽⠾⠿', $code)"/>
							<xsl:value-of select="$trans"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
<!--		<xsl:value-of select=""/>	'~a~b~k~l~cif~msp~e~h~o~r~djg~ntq~~~~~u~v~~~~~x~~~~~~~~~~~~~~~~~'-->			
			<xsl:otherwise>
				<xsl:value-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:variable name="strLen" select="string-length(.)"/>
		<xsl:if test="$strLen&lt;ancestor::*[@cols][1]/@cols">
			<xsl:variable name="char">			
				<xsl:choose>
					<xsl:when test="$translate=true()"><xsl:value-of select="'&#xA0;'"/></xsl:when>
					<xsl:otherwise><xsl:value-of select="'&#x2800;'"/></xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:call-template name="insertBlank">
				<xsl:with-param name="i" select="ancestor::*[@cols][1]/@cols - $strLen"/>
				<xsl:with-param name="char" select="$char"/>
			</xsl:call-template>
		</xsl:if>
		</xsl:element>
	</xsl:element>
</xsl:template>

</xsl:stylesheet>
