<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : javascript.xsl
    Created on : 2009/10/30, 13:01
    Author     : nazo
    Description: creating javascript message array.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:m="http://mappinon.hp2.jp/develop" version="1.0">
    <xsl:output method="text"/>

    <xsl:template match="/">message={<xsl:apply-templates select="m:index/m:javascript"/>};
    <xsl:call-template name="langs"/></xsl:template>

    <xsl:template match="m:index/m:javascript">
<xsl:for-each select="*">'<xsl:value-of select="local-name()"/>':'<xsl:value-of select="."/>'<xsl:if test="position()!=last()">,</xsl:if></xsl:for-each>
</xsl:template>

    <xsl:template name="langs">
llang=[<xsl:for-each select="document('common.xml')/common/languages/*"><xsl:if test="/m:index/@lang!=local-name()">'<xsl:value-of select="local-name()"/>'<xsl:if test="position()!=last()-1">,</xsl:if></xsl:if></xsl:for-each>];
</xsl:template>

</xsl:stylesheet>
