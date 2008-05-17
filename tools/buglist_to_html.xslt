<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:atom="http://www.w3.org/2005/Atom">
  <xsl:template match="/"> 
    <ul>
      <xsl:for-each select="atom:feed/atom:entry">
        <li>
          <a><xsl:attribute name="href"><xsl:value-of select="atom:link/@href"/></xsl:attribute><xsl:value-of select="substring-before(atom:title,']')"/>]</a> <xsl:value-of select="substring-after(atom:title,']')"/>
        </li>
      <xsl:text>
</xsl:text>
      </xsl:for-each>
    </ul>
  </xsl:template>
</xsl:stylesheet>
