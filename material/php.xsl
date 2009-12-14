<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : php.xsl
    Created on : 2009/12/10, 15:14
    Author     : nazotoko
    Description: creating php message array.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:m="http://mappinon.hp2.jp/develop" version="1.0">
    <xsl:output method="text"/>

    <xsl:template match="/">&lt;?php
$message=array(<xsl:apply-templates select="m:index/m:php"/>);
$olang=array(<xsl:call-template name="langs"/>);
</xsl:template>

    <xsl:template match="m:index/m:php">
<xsl:for-each select="*">'<xsl:value-of select="local-name()"/>'=>'<xsl:copy-of select="."/>'<xsl:if test="position()!=last()">,</xsl:if></xsl:for-each>
</xsl:template>

    <xsl:template name="langs">
<xsl:variable name="lang" select="m:index/@lang"/>
<xsl:for-each select="document('common.xml')/common/languages/*[local-name()!=$lang]">'<xsl:value-of select="local-name()"/>'=>'<xsl:value-of select="document(concat(local-name(),'.xml'))/m:index/@in_native"/>'<xsl:if test="position()!=last()">,</xsl:if></xsl:for-each>
</xsl:template>

</xsl:stylesheet>
