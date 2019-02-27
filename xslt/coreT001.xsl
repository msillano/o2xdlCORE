<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.2"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:o2x="http://www.o2xdl.org/3.0/o2xdl" >
 <!--
/**	
@file
   pass 1 of 4: transformation for Realize().
   This pass:<br />
     1) complete resolution of o2x:object at any depth<br />
     2) resolution inherit context and create correct KEY, for each o2x:object<br />
     3) elimination of unused methods<br />
 **/    -->
	<!-- versions
     26/09/02 prima stesura
     26/09/02 test completo context ereditario
              da fare: test body e inline - done
              da fare: test ricorsione oltre 2 livelli 
     27/09/02 aggiunti parametri globali da applicazione 
     30/09/02 aggiunta compatibilita' schema/DTD 
     22/03/17 eliminated  msxml:node-set
                                	 -->

	<!--  from realize engine -->        
	<xsl:param name="env_file">o2xdl-system.xml</xsl:param>    <!-- the  o2xdl environment file with defaults -->             
	<xsl:param name="environ_context">none</xsl:param>         <!-- nb: runtime param: overwrites o2xdl-system.xml if not 'none' -->
	<xsl:param name="DATA_DEFINITION">none</xsl:param>         <!-- nb: overwrites o2xdl-system.xml default if not 'none' -->

	<xsl:param name="ENGINE">o2xdl CORE engine 1.0.3</xsl:param>   
	<xsl:param name="now_time">unknow</xsl:param>                   
	<xsl:param name="source">unknow</xsl:param>                     
    <xsl:output
	  method = "xml"
      encoding = "UTF-8"
      omit-xml-declaration = "no"
      indent = "yes"  />

	<!--  actually not used
<xsl:param name="OUTPUT_STYLE">XMLauto</xsl:param>    <! - - HTML|XMLautoInclude|XMLauto - - >
<xsl:param name="INPUT_STYLE">server</xsl:param>      < ! - - server|net|bag - - >
<xsl:param name="DATA_STRICT">false</xsl:param>       <! - - false|true - - >
<xsl:param name="PARAM_STRICT">false</xsl:param>      <! - - false|true - - >
<xsl:param name="CORE_STRICT">false</xsl:param>       <! - - false|true - - >
-->
<!--
/**
Put some infos, then select next template based on DATA_DEFINITION: shema|DTD
**/ -->
    <xsl:variable name="root_context">
			<!--set root context
                priority:   param, 
                           file env_file -->
				<xsl:call-template name="getUseContext">
					<xsl:with-param name="ancestor_context">
						<xsl:value-of select="document($env_file)//SYSTEM/default_context" />
					</xsl:with-param>
					<xsl:with-param name="local_context">
						<xsl:value-of select="$environ_context" />
					</xsl:with-param>
				</xsl:call-template>
		</xsl:variable>

	<xsl:template match="/">
		<!-- mode - global: pre - OBJECT TREE  -->
		<!--                  decoration -->
		<xsl:comment>
          <xsl:text>
   Generated: </xsl:text><xsl:value-of select="$now_time" /><xsl:text>
   By:        </xsl:text><xsl:value-of select="$ENGINE" /> <xsl:text>
   Tool:      CORE/xslt/coreT001.xsl.
   Object:    </xsl:text><xsl:value-of select="$source" /> <xsl:text>
   Context:   </xsl:text><xsl:value-of select="$root_context" /><xsl:text>
   </xsl:text></xsl:comment>

	    <xsl:choose>
			<xsl:when test="(($DATA_DEFINITION = 'schema') or (($DATA_DEFINITION = 'none') and (document($env_file)//SYSTEM/xml_definition = 'schema')))">
				<xsl:apply-templates select="o2x:object"
                                     mode="schema" />
			</xsl:when>
			<xsl:when test="(($DATA_DEFINITION = 'DTD') or (($DATA_DEFINITION = 'none') and (document($env_file)//SYSTEM/xml_definition = 'DTD')))">
				<xsl:apply-templates select="o2x:object"
                                     mode="DTD" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:comment><xsl:text>ERROR. BAD xml_definition: must be schema|DTD </xsl:text></xsl:comment>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!-- /**
case DTD,root object.
Adds a DTD DOCTYPE  and continue 
**/ -->	
	<xsl:template match="o2x:object"
                  mode="DTD">
<!-- 
<xsl:text disable-output-escaping="yes">
  &lt;!DOCTYPE o2x:object PUBLIC &quot;-//o2xdl.org//OO XML dl version 1.0//EN&quot;
                        &quot;http://www.o2xdl.org/3.0/o2xdl-dtd.dtd&quot; [] &gt;

</xsl:text> 
 -->
		<xsl:text disable-output-escaping="yes">
  &lt;!DOCTYPE o2x:object PUBLIC "-//o2xdl.org//OO XML dl version 1.0//EN"
    "file:////C:/o2xdl-30/o2xdl-dtd.dtd" []&gt;
		</xsl:text>
		<!-- continua come nel caso schema -->
		<xsl:apply-templates select="."
                             mode="schema" />
	</xsl:template>
	
<!-- /**
case schema, root object.
Finds the use_context then apply-templates, mode recursiveCopy 
 **/ -->	
	<xsl:template match="o2x:object"
                  mode="schema">
		<xsl:apply-templates select="."
                             mode="copiaRicorsivaRef">
			 <xsl:with-param name="list_context"><xsl:value-of select="$root_context" /></xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
<!-- 
/** 
single o2x:dataValues: copy
**/ -->
	<xsl:template match="o2x:dataValues"
                  mode="copiaRef">
		<xsl:element name="o2x:dataValues">
			<xsl:choose>
				<xsl:when test="o2x:body[not(*)]">
					<xsl:element name="o2x:body">
						<xsl:value-of select="o2x:body"
                                      disable-output-escaping="yes" />
					</xsl:element>
				</xsl:when>
				<xsl:when test="o2x:body/*">
					<xsl:copy-of select="o2x:body" />
				</xsl:when>
				<xsl:when test="@URIref">
					<xsl:attribute name="URIref">
						<xsl:value-of select="@URIref" />
					</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:comment><xsl:text>ERROR. BAD dataValues: not found URIref or o2x:body</xsl:text></xsl:comment>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
	
<!-- 
/** 
single o2x:method: copy

**/ -->
	<xsl:template match="o2x:method"
                  mode="copiaRef">
		<xsl:element name="o2x:method">
			<xsl:attribute name="key">
				<xsl:value-of select="@key" />
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="not(@key)">
					<xsl:comment><xsl:text>ERROR. BAD method: not found key attribute </xsl:text><xsl:copy-of select="." /></xsl:comment>
				</xsl:when>
				<xsl:when test="o2x:inline[not(*)]">
					<xsl:element name="o2x:body">
						                    <xsl:comment> copy method - text content</xsl:comment>  
						<xsl:value-of select="o2x:inline"
                                      disable-output-escaping="yes" />
					</xsl:element>
				</xsl:when>
				<xsl:when test="o2x:inline/*">
					<xsl:element name="o2x:body">
						                    <xsl:comment> copy method - element content</xsl:comment> 
						<xsl:copy-of select="o2x:inline/*" />
					</xsl:element>
				</xsl:when>
				<xsl:when test="@URIref">
					<xsl:attribute name="URIref">
						<xsl:value-of select="@URIref" />
						                    <xsl:comment> copy method - remote URIref</xsl:comment> 
					</xsl:attribute>
				</xsl:when>				
				<xsl:otherwise>
					<xsl:comment><xsl:text>ERROR. BAD method: not found URIref or o2x:inline </xsl:text><xsl:copy-of select="." /></xsl:comment>
				</xsl:otherwise>
			</xsl:choose>
			<!-- paramValues, if any-->
			<xsl:apply-templates select="o2x:paramValues"
                                 mode="copiaRef" />
		</xsl:element>
	</xsl:template>

<!--
/** 
single o2x:paramValues: copy
**/ -->
	<xsl:template match="o2x:paramValues"
                  mode="copiaRef">
		<!-- copia di paramValues  URIref or BODY -->
		<xsl:element name="o2x:paramValues">
			<xsl:choose>
				<xsl:when test="o2x:body[not(*)]">
					<xsl:element name="o2x:body">
						<xsl:value-of select="o2x:body"
                                      disable-output-escaping="yes" />
					</xsl:element>
				</xsl:when>
				<xsl:when test="o2x:body/*">
					<xsl:copy-of select="o2x:body" />
				</xsl:when>
				<xsl:when test="@URIref">
					<xsl:attribute name="URIref">
						<xsl:value-of select="@URIref" />
					</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:comment><xsl:text>ERROR. BAD paramValues: not found
                    URIref or o2x:body </xsl:text><xsl:copy-of select="." /></xsl:comment>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>

<!-- /**
single <code>o2x:contentList</code>: copy, mode recursive 
**/ -->	
	<xsl:template match="o2x:contentList"
                  mode="copiaListaRef">
		<xsl:param name="def_context">none</xsl:param>
		<!--                  decorazione -->
 		<xsl:element name="o2x:contentList">
			<xsl:if test="@name">
				<xsl:attribute name="name" >
					<xsl:value-of select="@name" />
				</xsl:attribute>
			</xsl:if>
            <xsl:comment> Start content List: context = <xsl:value-of select="$def_context" /> </xsl:comment>

			<xsl:apply-templates select="o2x:object" mode="copiaRicorsivaRef">
				<xsl:with-param name="list_context">
					<xsl:value-of select="$def_context" />
				</xsl:with-param>
			</xsl:apply-templates>

		</xsl:element>
	</xsl:template>

<!-- /**
copy single <code>o2x:object</code> mode recursive.
Update context, then copy (local or remote)
**/ -->	
	<xsl:template match="o2x:object"
                  mode="copiaRicorsivaRef">
		<xsl:param name="list_context">none</xsl:param>
	    <xsl:variable name="use_context">
			<xsl:call-template name="getUseContext">
				<xsl:with-param name="ancestor_context">
					<xsl:value-of select="$list_context" /></xsl:with-param>
				<xsl:with-param name="local_context">
					<xsl:value-of select="@actualContext" /></xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
	    <xsl:if test="@URIref and not (o2x:method)">
			<xsl:comment><xsl:text> URIref - copy remote </xsl:text><xsl:value-of select="@URIref" /><xsl:text disable-output-escaping="yes">
 		    </xsl:text></xsl:comment>
			<!-- here differences (?) java ok with: document(@URIref) or document(@URIref)/o2x:object 
			                       jscript ok with: document(@URIref)/o2x:object or document(@URIref)//o2x:object  -->
			<xsl:apply-templates select="document(@URIref)/o2x:object" 
                             mode="copiaRicorsivaRef"> 
				<!--set root context -->
				<xsl:with-param name="list_context">
					 <xsl:value-of select="$use_context" /></xsl:with-param>
			</xsl:apply-templates>
		</xsl:if>

	    <xsl:if test="o2x:method">
	    <xsl:variable name="finalkey">
				<xsl:call-template name="getFinalkey" >
					<xsl:with-param name="context">
						<xsl:value-of select="$use_context" /></xsl:with-param>
				</xsl:call-template>
           </xsl:variable>
	   	<xsl:element name="o2x:object">
			<xsl:attribute name="key"><xsl:value-of select="$finalkey" /></xsl:attribute>
			<xsl:attribute name="actualContext"><xsl:value-of select="$use_context" /></xsl:attribute>
<!-- copy dataValues -->
			<xsl:apply-templates select="o2x:dataValues"
                                 mode="copiaRef" />
<!-- copy the 'final' methods -->
			<xsl:comment><xsl:text> == final method   </xsl:text></xsl:comment> 
            <xsl:apply-templates select="o2x:method[@key= string($finalkey)]"  
			                     mode="copiaRef" ></xsl:apply-templates>
<!-- copy all not 'final' methods -->
				<xsl:comment><xsl:text> == local methods: not final visibility + context</xsl:text></xsl:comment> 
		<xsl:apply-templates select="o2x:method[ (@key != string($finalkey)) and ((not (@visibility)) or ( @visibility != 'final')) and (@context = string($use_context))]"
                                 mode="copiaRef" />
				<xsl:comment><xsl:text> == local methods: not final visibility + not context</xsl:text></xsl:comment> 
		<xsl:apply-templates select="o2x:method[ (@key != string($finalkey)) and ((not (@visibility)) or ( @visibility != 'final')) and (not (@context))]"
                                 mode="copiaRef" />
<!-- copy all contentList -->
			<xsl:apply-templates select="o2x:contentList"
                                 mode="copiaListaRef">
				<xsl:with-param name="def_context">
					<xsl:value-of select="$use_context" />
				</xsl:with-param>
			</xsl:apply-templates>
		</xsl:element>
		</xsl:if>
	</xsl:template>
	
<!-- /**
Basic two contexts test. 
@param ancestor_context
@param local_context
@return ancestor_context if local_context = 'none', else  local_context
**/ -->
	<xsl:template name="getUseContext">
		<xsl:param name="ancestor_context">none</xsl:param>
		<xsl:param name="local_context">none</xsl:param>
		<xsl:choose>
			<xsl:when test="($local_context = 'none') or ($local_context = '')">
				<xsl:value-of select="$ancestor_context" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$local_context" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!-- /**
Function getFinalkey(use_context).
@param context
@return the key of the main selected method for this <code>o2x:object</code>
**/ -->
	<xsl:template name="getFinalkey">
		<xsl:param name="context">default</xsl:param>
		<!-- selection is an o2x:object -->
		<xsl:choose>
			<xsl:when test="o2x:method[(@visibility = 'final') and (@context=string($context))][1]">
				<xsl:value-of select="o2x:method[(@visibility = 'final') and (@context=string($context))][1]/@key" />
			</xsl:when>
			<xsl:when test="o2x:method[(@visibility = 'final') and (not (@context))][1]">
				<xsl:value-of select="o2x:method[(@visibility = 'final') and (not (@context))][1]/@key" />
			</xsl:when>
			<xsl:when test="o2x:method[(not (@visibility)) and (@context=string($context))][1]">
				<xsl:value-of select="o2x:method[(not(@visibility)) and (@context=string($context))][1]/@key" />
			</xsl:when>
			<xsl:when test="o2x:method[(not (@visibility)) and (not (@context))][1]">
				<xsl:value-of select="o2x:method[(not (@visibility)) and (not (@context))][1]/@key" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:text disable-output-escaping="yes">&lt;o2x:method key="error">&lt;o2x:body>ERROR: final method not found"&lt;/o2x:body>&lt;/o2x:method> </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	
<!-- /**
Fallback rule
**/ -->
	<xsl:template match=" @* | node()">
		<xsl:copy><xsl:text>**ERROR: MATCH non found </xsl:text>
			<xsl:apply-templates select="@* | node()" /></xsl:copy>
	</xsl:template>
</xsl:stylesheet>
