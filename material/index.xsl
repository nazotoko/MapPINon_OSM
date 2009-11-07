<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:m="http://mappinon.hp2.jp/develop">
<xsl:output method="html" encoding="UTF-8"
doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
standalone="yes" indent="no" version="4.0"/>

<xsl:template match="m:index">
<html xmlns="http://www.w3.org/1999/xhtml">
    <xsl:attribute name="xml:lang"><xsl:value-of select="@lang"/></xsl:attribute>
    <xsl:attribute name="lang"><xsl:value-of select="@lang"/></xsl:attribute>
<xsl:comment>
 Copyright 2008, 2009  Xavier Le Bourdon, Christoph Böhme, Mitja Kleider, Shun N. Watanabe

 This file is derived from a part of 
 Openstreetbugs (http://openstreetbugs.schokokeks.org/ ).

 Openstreetbugs is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 any later version.

 You should have received a copy of the GNU General Public License
 along with Openstreetbugs.  If not, see &lt;http://www.gnu.org/licenses/&gt;.

 This file implements the client of MapPIN'on OSM (http://mappin.hp2.jp/ ).
</xsl:comment>
    <head>
<!--        <meta http-equiv="content-type" content="text/html; charset=utf-8" />-->
        <meta http-equiv="Content-Language"><xsl:attribute name="content"><xsl:value-of select="@lang"/></xsl:attribute></meta>
        <meta name="copyright" content="GPLv3" />
        <link rel="shortcut icon" type="image/png" href="icons/logo48.png" />
        <link rel="icon" type="image/x-icon" href="favicon.ico" />
        <link rel="stylesheet" href="css/map.css" type="text/css" />
        <link rel="stylesheet" href="css/popups.css" type="text/css" />
        <xsl:comment><![CDATA[[if IE]><link rel="StyleSheet" type="text/css" href="css/stylesheet-ie.css" media="screen" /><![endif]]]></xsl:comment>
        <script src="http://openlayers.org/api/OpenLayers.js" type="text/javascript"></script>
        <script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js" type="text/javascript"></script>
        <script type="text/javascript"><xsl:attribute name="src">lang/<xsl:value-of select="@lang"/>.js</xsl:attribute></script>
        <script src="mappin.js" type="text/javascript"></script>
        <title>MapPIN'on OSM - Mapping Photo Introducing Network on OpenStreetMap</title>
    </head>
    <body onload="init()">
<xsl:comment><![CDATA[[if lt IE 6]>
    <style type="text/css">#left, #map{display:none;}</style>
    <div id="ie6msg">]]><xsl:value-of select="m:ie6"/><![CDATA[</div>
<![endif]]]></xsl:comment>
    <div id="left">
        <h1><img src="icons/logo.png" width="160" height="160" alt="MapPIN'on OSM - Mapping Photo Introducing Netwrok on OSM" style="border:none" /></h1>
        <div id="introduction" style="font-size: small;">
            <xsl:apply-templates select="m:text"/>
        </div>
        <xsl:apply-templates select="m:menu"/>
        <xsl:apply-templates select="m:currentView"/>
        <xsl:call-template name="languages"/>

        <div class="NavFrame">
            <div class="NavHead" onclick="toggleBar(1);">Debug<span id="NavToggle1" class="NavToggle">show</span></div>
            <dl id="NavContext1" class="NavContent">
                <dt>last loaded tile:</dt><dd id="readingData">data/</dd>
                <dt>last opened photo marker ID: </dt><dd id="photoID">0</dd>
                <dt># of Photo loaded:</dt><dd id="numberOfPhoto">0</dd>
                <dt># of Popuping: </dt><dd id="numberOfPopuping">0</dd>
            </dl>
        </div>
    </div>
    <div id="map">
        <noscript><xsl:apply-templates select="m:noscript"/></noscript>
    </div>
    <div id="message">
        <xsl:apply-templates select="m:message"/>
    </div>
    </body>
</html>
</xsl:template>

<xsl:template match="m:noscript|m:text">
    <xsl:for-each select="*">
        <xsl:copy-of select="current()"/>
    </xsl:for-each>
</xsl:template>


    <xsl:template match="m:menu">
        <div class="NavFrame">
            <div class="NavHead" onclick="toggleBar(4);">
                <xsl:value-of select="@title"/>
                <span id="NavToggle4" class="NavToggle">show</span>
            </div>
            <ul id="NavContext4" class="NavContent">
                <li>
                    <a href="blog/">
                        <xsl:value-of select="m:blog"/>
                    </a>
                </li>
                <li>
                    <a href="registration.php">
                        <xsl:value-of select="m:registration"/>
                    </a>
                </li>
                <li>
                    <a href="rsslist.html">
                        <xsl:value-of select="m:rssList"/>
                    </a>
                </li>
                <li>
                    <a href="backup/history.html">
                        <xsl:value-of select="m:history"/>
                    </a>
                </li>
            </ul>
        </div>
    </xsl:template>

    <xsl:template match="m:currentView">
        <div class="NavFrame">
            <div class="NavHead" onclick="toggleBar(2);refresh();">
                <xsl:value-of select="@title"/>
                <span id="NavToggle2" class="NavToggle">show</span>
            </div>
            <ul id="NavContext2" class="NavContent">
                <xsl:for-each select="*">
                    <li>
                        <a href="#">
                            <xsl:attribute name="id">
                                <xsl:value-of select="local-name()"/>
                            </xsl:attribute>
                            <xsl:value-of select="."/>
                        </a>
                    </li>
                </xsl:for-each>
            </ul>
        </div>
    </xsl:template>

    <xsl:template name="languages">
        <div class="NavFrame">
            <div class="NavHead" onclick="toggleBar(3);refresh();">Other Languages<span id="NavToggle3" class="NavToggle">show</span></div>
            <ul id="NavContext3" class="NavContent">
                <xsl:for-each select="document('common.xml')/common/languages/*">
                    <xsl:if test="/m:index/@lang!=local-name()">
                        <li>
                            <a>
                                <xsl:attribute name="id">lang_<xsl:value-of select="local-name()"/></xsl:attribute>
                                <xsl:attribute name="href">index.html.<xsl:value-of select="local-name()"/></xsl:attribute>
                                <xsl:value-of select="document(concat(local-name(),'.xml'))/m:index/@in_native"/>
                            </a>
                        </li>
                    </xsl:if>
                </xsl:for-each>
           </ul>
        </div>
    </xsl:template>

    <xsl:template match="m:message">
        <input type="text" id="messBox" name="htmlCode" value=""/>
        <br/>
        <input type="radio" name="kind" value="ascii" checked="true" onclick="messBox.embed2('a')"/>
        <xsl:value-of select="m:url"/>
        <br/>
        <input type="radio" name="kind" value="html" onclick="messBox.embed2('h')"/>
        <xsl:value-of select="m:html1"/>
        <br/>
        <input type="radio" name="kind" value="html" onclick="messBox.embed2('H')"/>
        <xsl:value-of select="m:html2"/>
        <br/>
        <input type="radio" name="kind" value="map" onclick="messBox.embed2('O')"/>
        <xsl:value-of select="m:image"/>
        <br/>
        <input type="radio" name="kind" value="MnA" onclick="messBox.embed2('OA')"/>
        <xsl:value-of select="m:imageText"/>
        <br/>
        <div id="messTest"/>
        <div class="olPopupCloseBox" style="width: 17px; height: 17px; position: absolute; right: 13px; top: 14px; z-index: 1;" onclick="document.getElementById('message').style.display='none'"/>
    </xsl:template>
</xsl:stylesheet>