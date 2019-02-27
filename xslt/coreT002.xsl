<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:o2x="http://www.o2xdl.org/3.0/o2xdl"
	exclude-result-prefixes="o2x  xsl" >
 <!--
/**	
@file
   pass 2 of 4: transformation for Realize().
   This pass:<br />
     1) only dataValues and paramVales are copied to output.<br />
     2) Includes dataValues using DTD entity, if OUTPUT_STYLE = XMLAuto (note: this works for apps, but not in browser)<br />
     3) elimination of all methods 
 **/    -->

	<!-- versions
     26/09/02 prima stesura
     27/09/02 aggiunti parametri globali da applicazione 
     30/09/02 aggiunta compatibilita' schema/DTD 
     -->

	<xsl:key name="dtd-param-unicum" match="o2x:paramType" use="@URIref"/>
	<xsl:key name="dtd-data-unicum" match="o2x:dataType" use="@URIref"/>
	<xsl:key name="nspaces-unicum" match="//*" use="namespace-uri(.)" />

	<xsl:param name="env_file">o2xdl-system.xml</xsl:param>                  
	<xsl:param name="OUTPUT_STYLE">XMLauto</xsl:param>         <!-- HTML|XMLautoInclude|XMLauto -->
	<xsl:param name="DATA_DEFINITION">none</xsl:param>         <!-- nb: overwrites o2xdl-system.xml default if not 'none' -->

	<xsl:param name="ENGINE">unknow</xsl:param>
	<xsl:param name="now_time">unknow</xsl:param>
	<xsl:param name="source">unknow</xsl:param>
	
	<xsl:param name="output_xsl_fname">unknow</xsl:param>  <!-- only in T002 -->
    <xsl:output
	  method = "xml"
      encoding = "UTF-8"
      omit-xml-declaration = "no"
      indent = "yes"  />

	<!--  not used
<xsl:param name="DATA_STRICT">false</xsl:param>       <! - - false|true - - >
<xsl:param name="PARAM_STRICT">false</xsl:param>      <! - - false|true - - >
<xsl:param name="CORE_STRICT">false</xsl:param>       <! - - false|true - - >
-->

<!--
/**
* Get only the filename, strips path (if any).
*/ -->
	<xsl:variable name="XSLFileName"><xsl:call-template name="GetFileName">
			<xsl:with-param name="sFName"  select="$output_xsl_fname"/>
		</xsl:call-template></xsl:variable>
<!--
/**
* main template, puts some stuff.
* then select between DTD/shema
*/ -->
	<xsl:template match="/">
		<!--  start trasformation final in browser -->
		<xsl:if test="($OUTPUT_STYLE='XMLauto') or ($OUTPUT_STYLE='XMLautoInclude')">
			<xsl:processing-instruction name="xml-stylesheet">href=&quot;<xsl:value-of
			select="$XSLFileName"/>&quot; type="text/xsl"</xsl:processing-instruction>
      </xsl:if>

		<!--                  decoration -->
		<xsl:comment>  
   Generated: <xsl:value-of select="$now_time"/> 
   By:        <xsl:value-of select="$ENGINE"/> 
   Tool:      CORE/xslt/coreT002.xsl  
   Input:     <xsl:value-of select="$source"/> 
		
		</xsl:comment>

		<xsl:choose>
			<xsl:when test="(($DATA_DEFINITION = 'schema') or (($DATA_DEFINITION = 'none') and (document($env_file)//SYSTEM/xml_definition = 'schema')))">
				<xsl:call-template  name="coreT002_main_schema" /> </xsl:when>
			<xsl:when test="(($DATA_DEFINITION = 'DTD') or (($DATA_DEFINITION = 'none') and (document($env_file)//SYSTEM/xml_definition = 'DTD')))">
				<xsl:call-template  name="coreT002_main_DTD" /> </xsl:when>
			<xsl:otherwise>
				<xsl:comment> 
         *** ERROR: Value of DATA_DEFINITION parameter (<xsl:value-of select="$DATA_DEFINITION"/>) non allowed </xsl:comment>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!--
/**
* DTD template, creates definitions.
*/ -->

	<xsl:template name="coreT002_main_DTD">

		<xsl:text disable-output-escaping="yes">

 &lt;!DOCTYPE o2x:object [ </xsl:text>
		<xsl:text disable-output-escaping="yes">
 &lt;!ELEMENT o2x:object ANY&gt; </xsl:text> 
		<xsl:text disable-output-escaping="yes">
 &lt;!ATTLIST o2x:object key CDATA "none"&gt; </xsl:text>
		<xsl:text disable-output-escaping="yes">
 &lt;!ATTLIST o2x:object xmlns:o2x CDATA #IMPLIED&gt; </xsl:text>
		<xsl:text disable-output-escaping="yes">
 &lt;!ELEMENT o2x:contentList (o2x:object)*&gt; </xsl:text> 
		<xsl:text disable-output-escaping="yes">
 &lt;!ATTLIST o2x:contentList name CDATA #IMPLIED&gt; 
		</xsl:text>
<!-- pass 1:  makes DTD -->
      	<xsl:apply-templates
       select="//o2x:dataType[(@URIref)and(generate-id(.)=generate-id(key('dtd-data-unicum',@URIref)[1]))]" 
       mode="passo1"/>
		<xsl:apply-templates
       select="//o2x:dataType[o2x:body]" 
       mode="passo1"/>
		<!-- all param Type  -->
		<xsl:apply-templates
       select="//o2x:paramType[(@URIref)and(generate-id(.)=generate-id(key('dtd-param-unicum',@URIref)[1]))]" 
       mode="passo1"/>
		<xsl:apply-templates
       select="//o2x:paramType[o2x:body]" 
       mode="passo1"/>
		<!-- la copia dei Values in DTD solo se NON include -->
		<!-- HTML|XMLautoInclude|XMLauto -->
		<xsl:choose>
			<xsl:when test="($OUTPUT_STYLE='XMLauto') or ($OUTPUT_STYLE='HTML')">
				<xsl:call-template  name="out_Values_pass1" /> </xsl:when>
		</xsl:choose>
		<!-- closing dtd -->
		<xsl:text disable-output-escaping="yes">    ]&gt;
		</xsl:text> 

		<!-- pass 2: creates OBJECT TREE and copies data/param -->
		<xsl:apply-templates mode="passo2" />
	</xsl:template>

	<!--;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;SCHEMA;;;;;;;;-->

<!--
/**
* schema template, creates DTD too.
* Uses the include function
*/ -->

	<xsl:template name="coreT002_main_schema">
		<xsl:if test="$OUTPUT_STYLE='XMLauto'">
				<!-- &lt;!DOCTYPE o2x:object SYSTEM "http://www.o2xdl.org/3.0/o2xdl-dtd.dtd" [  -->
			<xsl:text disable-output-escaping="yes">
   &lt;!DOCTYPE o2x:object SYSTEM "file:///C:/o2xdl-30/o2xdl-dtd.dtd" [ 
<!-- &lt;!DOCTYPE o2x:object SYSTEM "http://www.o2xdl.org/3.0/o2xdl-dtd.dtd" [  -->
			</xsl:text>
			<!-- la copy Values in DTD, only if not include -->
			<!-- HTML|XMLautoInclude|XMLauto -->
			<xsl:choose>
				<xsl:when test="$OUTPUT_STYLE='XMLauto'">
					<xsl:call-template  name="out_Values_pass1" /> </xsl:when>
			</xsl:choose>
			<!-- ends dtd -->
			<xsl:text disable-output-escaping="yes">    ]&gt;
			</xsl:text> 
		</xsl:if>

		<!-- passo 2: crea OBJECT TREE e chiamate alle ENTITY data -->
		<xsl:apply-templates select="/o2x:object" mode="passo2" />
	</xsl:template>

	<!--;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;PASS 1;;;;;;;;-->
<!--
/**
* pass 1: dataValues.
* dataValues to ENTITY
*/ -->

	<xsl:template name="out_Values_pass1" >

		<!-- Tutti i data Values, anche duplicati -->
		<xsl:for-each select="//o2x:dataValues">
			<xsl:if test="@URIref">
				<xsl:text disable-output-escaping="yes">  &lt;!ENTITY </xsl:text> data_<xsl:value-of select="generate-id()"/> SYSTEM "<xsl:value-of select="@URIref"/>"<xsl:text disable-output-escaping="yes">&gt;
				</xsl:text> 
			</xsl:if>
		</xsl:for-each>

		<!-- Tutti i param Values, anche duplicati -->
		<xsl:for-each select="//o2x:paramValues">
			<xsl:if test="@URIref">
				<xsl:text disable-output-escaping="yes">  &lt;!ENTITY </xsl:text> param_<xsl:value-of select="generate-id()"/> SYSTEM "<xsl:value-of select="@URIref"/>"<xsl:text disable-output-escaping="yes">&gt;
				</xsl:text> 
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

<!--
/**
* pass 1: dataType.
* dataType  to ENTITY
*/ -->

	<xsl:template match="o2x:dataType"  mode="passo1">
		<xsl:choose>
			<xsl:when test="./o2x:body/text()">
				<xsl:value-of select="." disable-output-escaping="yes" />
			</xsl:when>
			<xsl:when test="./o2x:body/*">
				<xsl:copy-of select="./o2x:body/*" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:text disable-output-escaping="yes">  &lt;!ENTITY  % </xsl:text> dType_<xsl:value-of select="generate-id()"/> SYSTEM "<xsl:value-of select="@URIref"/>"<xsl:text disable-output-escaping="yes">&gt;</xsl:text> 
				<xsl:text disable-output-escaping="yes">
    %</xsl:text>dType_<xsl:value-of select="generate-id()"/>;
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
<!--
/**
* pass 1: paramType.
* paramType  to ENTITY
*/ -->
	<xsl:template match="o2x:paramType"  mode="passo1">
		<xsl:choose>
			<xsl:when test="./o2x:body">
				<xsl:value-of select="." disable-output-escaping="yes" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:text disable-output-escaping="yes">  &lt;!ENTITY  % </xsl:text> pType_<xsl:value-of select="generate-id()"/> SYSTEM "<xsl:value-of select="@URIref"/>"<xsl:text disable-output-escaping="yes">&gt;</xsl:text> 
				<xsl:text disable-output-escaping="yes">
    %</xsl:text>pType_<xsl:value-of select="generate-id()"/>;
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- elimina tutti gli altri nodi -->
	<xsl:template match="text()" mode="passo1"/>

	<!--;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;PASS 2;;;;;;;;-->
	<!-- 
/**
* pass 2: object.
* start copy object
*/ -->
	<xsl:template match="o2x:object" mode="passo2">
		<xsl:text disable-output-escaping="yes">
 &lt;!-- object --&gt;
		</xsl:text>
		<xsl:choose>
			<xsl:when test="$DATA_DEFINITION='DTD'">
				<xsl:call-template  name="out_Object_noNS" /> </xsl:when>
			<xsl:when test="$OUTPUT_STYLE='XMLauto'">
				<xsl:call-template  name="out_Object_noNS" /> </xsl:when>
			<xsl:otherwise>
				<xsl:call-template  name="out_Object_w_NS" /> </xsl:otherwise>
		</xsl:choose>
	</xsl:template>
<!--
/**
* pass 2: object.
* copy object no namespace, named template.
*/ -->
	<xsl:template name="out_Object_noNS" >
		<xsl:element name="o2x:object" namespace="http://www.o2xdl.org/3.0/o2xdl">
			<xsl:attribute name="key"><xsl:value-of select="@key"/> </xsl:attribute>
			<!-- HTML|XMLautoInclude|XMLauto -->
			<xsl:choose>
				<xsl:when test="$OUTPUT_STYLE='XMLauto'">
					<xsl:apply-templates select="./o2x:dataValues"  mode="passo2" />
					<xsl:apply-templates select="./o2x:method"      mode="include" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="./o2x:dataValues"  mode="include" />
					<xsl:apply-templates select="./o2x:method"      mode="include" />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="./o2x:contentList" mode="passo2" />
		</xsl:element> 
	</xsl:template>
<!--
/**
* pass 2: object.
* copy object with namespace, named template.
*/ -->

	<xsl:template name="out_Object_w_NS" >
		<xsl:element name="o2x:object" namespace="http://www.o2xdl.org/3.0/o2xdl">
			<xsl:attribute name="key"><xsl:value-of select="@key"/> </xsl:attribute>
			<!-- HTML|XMLautoInclude|XMLauto -->
			<xsl:choose>
				<xsl:when test="$OUTPUT_STYLE='XMLauto'">
					<xsl:apply-templates select="./o2x:dataValues"  mode="passo2" />
					<xsl:apply-templates select="./o2x:method"      mode="include" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="./o2x:dataValues"  mode="include" />
					<xsl:apply-templates select="./o2x:method"      mode="include" />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="./o2x:contentList" mode="passo2" />
		</xsl:element> 
	</xsl:template>

<!--
/**
* pass 2: copy contentList.
*/ -->
	<xsl:template match="o2x:contentList" mode="passo2">
		<xsl:text disable-output-escaping="yes">
 &lt;!-- START contentList --&gt;
		</xsl:text>
				<xsl:element name="o2x:contentList">
					<xsl:if test="@name">
						<xsl:attribute name="name"><xsl:value-of select="@name"/> </xsl:attribute>
					</xsl:if>
					<xsl:apply-templates mode="passo2" />
				</xsl:element>
	</xsl:template>

<!--
/**
* pass 2: copy dataValues.
*/ -->

	<xsl:template match="o2x:dataValues"  mode="passo2">
		<xsl:choose>
			<xsl:when test="./o2x:body/*">
				<xsl:copy-of select="./o2x:body/*" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:text disable-output-escaping="yes">
    &amp;</xsl:text>data_<xsl:value-of select="generate-id()"/>;
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!--
/**
* pass 2: copy paramValues.
*/ -->

	<xsl:template match="o2x:paramValues"  mode="passo2">
		<xsl:choose>
			<xsl:when test="./o2x:body/*">
				<xsl:copy-of select="./o2x:body/*" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:text disable-output-escaping="yes">
    &amp;</xsl:text>param_<xsl:value-of select="generate-id()"/>;
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


<!--
/**
* pass 2: skip method.
*/ -->
	<xsl:template match="o2x:method" mode="passo2">
		<xsl:apply-templates select="./o2x:paramValues"  mode="passo2" />
	</xsl:template>

<!--
/**
* pass 2: skip all.
*/ -->

	<xsl:template match="*" mode="passo2"/>

<!--
/**
* copy dataValues, mode include.
* copy body or URIref.
*/ -->

	<xsl:template match="o2x:dataValues"  mode="include">
		<xsl:choose>
			<xsl:when test="./o2x:body/*">
				<xsl:copy-of select="./o2x:body/*" />
			</xsl:when>
			<xsl:when test="@URIref">
				<xsl:copy-of select="document(@URIref)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text disable-output-escaping="yes">
 &lt;!-- Error: not found URIref in dataValues
				</xsl:text><xsl:value-of select="@name" /><xsl:text disable-output-escaping="yes">
 --&gt;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!--
/**
* copy paramValues, mode include.
* copy body or URIref.
*/ -->

	<xsl:template match="o2x:paramValues"  mode="include">
		<xsl:choose>
			<xsl:when test="./o2x:body/*">
				<xsl:copy-of select="./o2x:body/*" />
			</xsl:when>
			<xsl:when test="@URIref">
				<xsl:copy-of select="document(@URIref)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text disable-output-escaping="yes">
 &lt;!-- Error: not found URIref in paramValues
				</xsl:text><xsl:value-of select="@name" /><xsl:text disable-output-escaping="yes">
 --&gt;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!--
/**
* skip method, mode include.
*/ -->

	<xsl:template match="o2x:method" mode="include">
		<xsl:apply-templates select="./o2x:paramValues"  mode="include" />
	</xsl:template>

<!--
/**
* function GetFileName().
* @return split file name, recursive
*/ -->
	<xsl:template name="GetFileName">
		<!-- estrae solo il nome: quindi locale,  stessa dir di xml -->
		<xsl:param name="sFName" />
		<xsl:choose>
			<xsl:when test='contains($sFName,"/")'>
				<xsl:call-template name="GetFileName">
					<xsl:with-param name="sFName"  select="substring-after($sFName,'/')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test='contains($sFName,"\")'>
				<xsl:call-template name="GetFileName">
					<xsl:with-param name="sFName"  select="substring-after($sFName,'\')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$sFName"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


</xsl:stylesheet>
