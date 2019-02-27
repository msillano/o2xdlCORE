<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:o2x="http://www.o2xdl.org/3.0/o2xdl"
	 exclude-result-prefixes="o2x xsl" >
    <xsl:output
	  method = "xml"
      encoding = "UTF-8"
      omit-xml-declaration = "no"
      indent = "yes"  />

 <!--
/**	
@file
   pass 3 of 4: transformation for Realize().
   This pass:<br />
     1) copy all method's templates <br />
     2) makes unique stylesheet<br />
 **/    -->
	
	<!-- versioni
     26/09/02 prima stesura
     27/09/02 aggiunti parametri globali da applicazione 
     30/09/02 aggiunta compatibilita' schema/DTD 
              -->


	<xsl:key name="method-unicum" match="//o2x:method" use="@key" />

	<xsl:param name="OUTPUT_STYLE">XMLauto</xsl:param>    <!-- HTML|XMLautoInclude|XMLauto -->

	<xsl:param name="ENGINE">o2xdl CORE engine 1.0.0</xsl:param>
	<xsl:param name="now_time">false</xsl:param>
	<xsl:param name="source">unknow</xsl:param>
<!--  not used -->
	<xsl:param name="INPUT_STYLE">server</xsl:param>      <!-- server|net|bag -->
	<xsl:param name="DATA_STRICT">false</xsl:param>       <!-- false|true -->
	<xsl:param name="PARAM_STRICT">false</xsl:param>      <!-- false|true -->
	<xsl:param name="CORE_STRICT">false</xsl:param>       <!-- false|true -->
<!--
/**
* main template, puts some stuff.
*/ -->
	<xsl:template match="/">
		<xsl:text  disable-output-escaping="yes">
&lt;xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:o2x="http://www.o2xdl.org/3.0/o2xdl"
     exclude-result-prefixes="o2x xsl" >
		</xsl:text>

		<!--                  decoration -->
		<xsl:comment>
   Generated: <xsl:value-of select="$now_time"/>. 
   By:        <xsl:value-of select="$ENGINE"/>
   Tool:      CORE/xslt/coreT003.xsl. 
   Input:    <xsl:value-of select="$source"/>. 
		</xsl:comment>
		<!--  globals in first position  -->
		<xsl:copy-of select=".//xsl:import" />
		<xsl:copy-of select=".//xsl:include" />
		<xsl:copy-of select=".//xsl:attribute-set" />
		<xsl:copy-of select=".//xsl:output" />

     <xsl:apply-templates
     select="//o2x:method[generate-id(.)=generate-id(key('method-unicum', @key)[1])]" mode="prova" />
		<xsl:apply-templates/>
		<xsl:text  disable-output-escaping="yes">&lt;/xsl:stylesheet></xsl:text>
	</xsl:template>
<!--
/**
* copy method.
*/ -->
	<xsl:template match="o2x:method" mode="prova">  
		<xsl:text disable-output-escaping="yes">
   &lt;!-- method:</xsl:text><xsl:value-of select="@key"/><xsl:text disable-output-escaping="yes">()  --&gt;
		</xsl:text>
		<xsl:if test="o2x:body/* | o2x:inline/*">
			<!-- copy of elements, cuts (optional) stylesheet|transform element 
        order minds   -->
			<xsl:copy-of select="./xsl:param" />
			<xsl:copy-of select="./not[xsl:template]/xsl:param" />
			<xsl:copy-of select="./xsl:variable" />
			<xsl:copy-of select="./xsl:stylesheet/xsl:variable" />
			<xsl:copy-of select="./xsl:transform/xsl:variable" />
			<xsl:copy-of select=".//xsl:template" />

		</xsl:if>
          <!-- precedence to local data inline -->
		<xsl:if test="@URIref and (not (o2x:body/* | o2x:inline/*))">
			<xsl:choose>
				<xsl:when test="$OUTPUT_STYLE='XMLautoInclude'">
					<xsl:copy-of select="document(@URIref)/*/*" />   <!-- fare ricorsivo nel caso di include ?? -->
				</xsl:when>
				<xsl:otherwise>
					<xsl:element name="xsl:include">
						<xsl:attribute name="href"><xsl:value-of select="@URIref"/></xsl:attribute>
					</xsl:element>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>


	<!-- elimina tutti gli altri nodi -->
	<xsl:template match="*" />

</xsl:stylesheet>