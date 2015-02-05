<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xslthl="http://xslthl.sf.net"
    exclude-result-prefixes="xslthl docbook"
    xmlns:docbook="http://docbook.org/ns/docbook"
    version="1.0">
    
    <xsl:import href="urn:docbkx:stylesheet"/>
    
    <xsl:template match="//docbook:inlinemediaobject[docbook:imageobject/docbook:imagedata[contains(@fileref,'images/icons')]]">
    	<i><xsl:attribute name="class">fa fa-<xsl:value-of select="./docbook:textobject/docbook:phrase/text()"/></xsl:attribute></i>
  	</xsl:template>
  
</xsl:stylesheet>