/*
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *  http://gnu.org/licenses/gpl.html
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.time.LocalDateTime ;
import java.time.format.DateTimeFormatter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import ms.utils.aarray.baseapp.AABaseAppl;
// note: following documentation is escaped and formatted to
// give good HTML pages using Doxygen (not javadoc)

/**
 * @file
 * CORE <i><b><span style="font-family: Monotype Corsiva; font-size: x-large">R</span>ealize()</b></i>, java implementation.
 *
 * This filter implements a java version of <i><b><span
 *   style= "font-family: Monotype Corsiva; font-size: x-large">R</span>ealize()</b></i> for o2xdl CORE.
 * For easy use, this application and xmlfilter are in <b>o2xtools.jar</b>
 * The input file must be a valid <code>o2x:object</code> file, see http://www.o2xdl.org/3.0/o2xdl-core.xsd.html . It is realized
 * and the output is send to standard output or saved in a file.<br>
 * Parameters on command line or in configuration file allows full control on output.
 * @par Parameters
 *<dl>
 *<dt> parameters cli </dt>
 * <dd>All parameters can be on <i>command line</i> or in a <i>config file</i>.
 * On command <i>command line</i> is accepted:  "--indent=yes" but also: "--indent yes", "--indent = yes",
 * and "--ind yes", "--ind=yes", if "--ind" is unique.
 * The 'value' must be URLencoded: accepts(a-z,A-Z,0-9,'-','.','*','_'), space='+', others=%XX
 * </dd>
 *<dt>config file</dt>
 * <dd>the default config file is 'jrealize.cfg', in same dir as jrealize.jar, but this can be changed
 * using the command line parameter  "--CLoadconfig FILECFG" or "--CL=FILECFG,<br>
 * The jrealize.cfg syntax is INI-like, e.g. "-input#1=C:\o2xdl-30\CORE\samples\test.o2xSobj":
 *  - the name is like the command line parameter, less an hyphen ('-'), plus '#1','#2'... to allow multiples values.
 *  - no spaces before or after the '=' between name and value.
 *  - # as first character for comments.
 * The config file can also be a XML file, so it is possible the meta-pragrammation of this tool.
 *<dt>input file</dt>
 * <dd> is a file ".o2xSobj" or ".o2xDobj"  as unique argument (O2XFILE) or as parameter (-i FILE, --input FILE). Local complete phat.</dd>
  *<dt>output file</dt>
 * <dd> Default is stdout. If specified (-o FILE|-output FILE) must be a local phat. The extension is changed in accord
 *to 'method' parameter to: '.html', '.xml', '.text'.</dd>
 *<dt>context</dt>
 * <dd> The context value used by realize(). The precedence rules are:
 *      - The default value in ENVFILE  (default = 'default')
 *      - This this value, set by Realize() ('none' or some value)
 *      - The parent context, for objects in an <code>o2x:contentList</code>.
 *      - The o2x:object actualContext attribute (if any)
 *  The value 'none' is used as null value.</dd>
 *<dt>OUTPUT_STYLE</dt>
 * <dd> This parameter influences the Realize() logic.
 *  - HTML: The output is saved on the output file (can be html, xml, text).
 *  - XMLauto: As HTML, but for late transformation: the temporary file TEMP0B is XML; the final transformation can be done using TEMP0C
 *     as stylesheet (note: for security reasons this works on browser only if all files are in one server.)
 *  - XMLautoInclude: Same as XMLauto with all include done, so TEMP0B and TEMP0C are bigger, but they can be used standalone (works local
 *     in many browsers, not in chrome).
 * </dd>
 *<dt>ENVFILE</dt>
 * <dd> The o2xdl environment file, defines (for CORE level) some defaults: context and DATA_DEFINITION. Local
 *  (C:\o2xdl-30\CORE\o2xdl-system.xml) or remote: "http://www.o2xdl.org/3.0/CORE/o2xdl-system.xml"  </dd>
 * <dt>DATA_DEFINITION</dt>
 * <dd>The definition style for o2xdl: schema(default) or DTD. This is more important at USABILITY level. </dd>
 * <dt>TEMP0A, TEMP0B, TEMP0C</dt>
 * <dd> Local temporary files.  TEMP0B, TEMP0C can be used as output if OUTPUT_STYLE = XMLauto | XMLautoInclude.</dd>
 * <dt>XSL01, XSL02, XSL03</dt>
 * <dd>  The XSLT tranformations used by realize(). Local (C:\o2xdl-30\CORE\xslt\coreT001.xsl) or remote "http://www.o2xdl.org/3.0/CORE/xslt/coreT001.xsl"</dd>
 * <dt>output parameters</dt>
 * <dd>Using this you can set some default, like the &lt;xsl:output> attribute values specified in the
 * styleshee (see javax.xml.transform.OutputKeys).</dd>
 * <dt>user parameters</dt>
 * <dd> This parameters (name*value) are send to stylesheet at runtime. On config file use a progressive numeration.</dd>
 *</dl>
 * @see  jrealize.bat driver.
 * @see Notepad++ addons
 *
 *@par help
 * @htmlonly
* <PRE>
   jrealize (o2xdl tools)  2.03 (2019/02/18)
   Copyright (C) 2006-2019  M.Sillano
   License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
   This is free software: you are free to change and redistribute it.
   There is NO WARRANTY, to the extent permitted by law.

 Usage:        jrealize   -h|-?|--help|--version
               jrealize   [-i=]O2XFILE [-u FILE] [-c = context] [properties]* [--CLoadconfig=FILE]
               jrealize   --CLoadconfig=FILE  --CKxmlconfigsave=FILE  (convert config file)

    Realizes the CORE o2x:object  inputFile.

    O2XFILE                   the o2x:object input file as argument. Mandatry.
    -i FILE, --input FILE     the o2x:object input, as parameter.
    -o FILE, --output FILE    the output file, Default stdout.
    -c cnt   --context cnt    runtime-context used (overwrite ENVFILE default context, default 'none')
    options:  -h|-?|--help    display this help and exit.
              --version       print version and exit.

    optional runtime parameters:
     --OUTPUT_STYLE           HTML|XMLauto|XMLautoInclude.
     --ENVFILE                Environment file for o2xdl, default '.\\..\\..\\o2xdl-system.xml'
     --DATA_DEFINITION        none|schema|DTD. none: uses default in ENVFILE
     --TEMP0A = path          temporary file, default 'C:\\temp\\core3_A.TMP'
     --TEMP0B = path          temporary file, default 'C:\\temp\\core3_B.XML'
     --TEMP0C = path          temporary file, default 'C:\\temp\\core3_C.XSL'
     --XSL01 = path | URL     XSL file#1, default 'C:\\o2xdl-30\\CORE\\xslt\\coreT001.xsl'
     --XSL02 = path | URL     XSL file#2, default 'C:\\o2xdl-30\\CORE\\xslt\\coreT002.xsl'
     --XSL03 = path | URL     XSL file#3, default 'C:\\o2xdl-30\\CORE\\xslt\\coreT003.xsl'

    more optional output properties for last Transformer:
    --omit_xml_declaration=yes|no
                              Default = yes
    --indent yes|no           Default = yes
    --method xml|html|text    Default = html
    --media_type MIME         output MIME content type.

    more optional output properties for all Transformers:
    --indent yes|no           Default = no
    --encoding CODE           Char code (UTF-8)
    --standalone yes|no       Declaration style, default yes
    --doctype_system DOCTYPE  used in the document type declaration.
    --doctype_public PUBLIC   public identifier.
    --cdata_section_elements "LIST"
                              specifies a whitespace delimited list of qnames
    --usr_parameter name*value
                              Additional parameters passed to xslt:
                                <local or qname>+"*"+<value>

    All parameters, included the input file, can be stored in a config file.
    Next only for command line (default: read INI jrealize.cfg)
    --CLoadconfig FILE        reads option/param from a config file, INI mode
    --CSaveconfig FILE        saves all options/parameter to a config file, INI mode
    --CXmlconfigload=FILE     read option/param from a config file, XML mode
    --CKxmlconfigsave=FILE    save all option/param to a config file, XML mode.

 </PRE>
 @endhtmlonly
 *
 * @author M. Sillano (marco.sillano(at)gmail.com) &copy;2006-2018 M.Sillano
 * @version 4.03 2019/02/12  m.s.
 */


/**
 * Implements <b>o2xdl CORE</b> <i><b><span style="font-family: Monotype Corsiva; font-size: x-large">R</span>ealize()</b></i> for o2x:object.
 * Extends ZeroFilter defining private data structures: version, help,
 * Option, Parameter and default file names.<BR>
 * Input a file '.o2xSobj' or '.o2xDobj'<BR>
 * Output, a file or stdout.
 * Full configurable, this application can read options from command line or
 * from a configuration file. <BR>
 * In o2xtools.jar with xmlfilter.
 * @see xmlfiler.java
 * @see jo2xtools.makejar.bat
 * @see jrealize.bat
 *
 * @author M. Sillano (marco.sillano@gmail.com)
 * @version 2.02 18/02/19 (c) M.Sillano 2006-2019
 */
public class jrealize extends ZeroFilter {
	// =======================================================
	// standard ZeroFIle extensions
    static public final String version = "jrealize (o2xdl tools)  2.03 (2019/02/18) \n"
			+ "Copyright (C) 2006-2019  M.Sillano \n"
			+ "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html> \n"
			+ "This is free software: you are free to change and redistribute it. \n"
			+ "There is NO WARRANTY, to the extent permitted by law.";

	static public final String[] xmlHelp = {
			"Usage:     realize     -h|-?|--help|--version",
            "           realize     [-i=]O2XFILE [-c context] [-o = FILE] [properties] [--CLoadconfig=FILE]",
            "   Realizes the CORE o2x:object  inputFile.",
			"",
            " O2XFILE,                 the o2x:object input file, mandatory.",
            "-i FILE, --input FILE     the o2x:object input, as parameter.",
            "-o FILE, --outfile FILE   the output file, default =input+'out.xml'.",
			"-c cnt   --context cnt    runtime-context used, default none (uses envfile)",
			"options:  -h|-?|--help    display this help and exit.",
			"          --version       print version and exit.",
            "",
            "optional runtime parameters: ",
            " --DATA_DEFINITION        none|schema|dtd. ",
            " --OUTPUT_STYLE           HTML|XMLauto|XMLautoInclude, defautl HTML ",
            " --ENVFILE                Environment file for o2xdl, default '.\\..\\..\\o2xdl-system.xml' ",
            " --TEMP0A = path          temporary file, default 'C:\\temp\\core3_A.TMP'. ",
            " --TEMP0B = path          temporary file, default 'C:\\temp\\core3_B.XML'. ",
            " --TEMP0C = path          temporary file, default 'C:\\temp\\core3_C.XSL'. ",
            " --XSL01 = path | URL     XSL file#1, default 'C:\\o2xdl-30\\CORE\\xslt\\coreT001.xsl'. ",
            " --XSL02 = path | URL     XSL file#2, default 'C:\\o2xdl-30\\CORE\\xslt\\coreT002.xsl'. ",
            " --XSL03 = path | URL     XSL file#3, default 'C:\\o2xdl-30\\CORE\\xslt\\coreT003.xsl'. ",
            "",
            "  more optional output properties for last transform:",
			"--omit_xml_declaration    yes|no  ",
			"                          Default = yes",
			"--indent yes|no           Default = yes",
			"--method xml|html|text    Default = html",
            "--media_type MIME         MIME content type.",
            "",
            "more optional output properties for all Transformers: ",
            "--indent yes|no           Default = no ",
            "--method xml|html|text    Default = xml",
            "--encoding CODE           Char code, default UTF-8",
            "--standalone yes|no       Declaration style, default yes ",
            "--doctype_system DOCTYPE  used in the document type declaration. ",
			"--doctype_public PUBLIC   public identifier.",
			"--cdata_section_elements=\"LIST\" ",
			"                          specifies a whitespace delimited list of qnames",
			"--usr_parameter name*value Additional parameters passed to xsl.",
			"                          <local|qname>+\"*\"+<value>",
			"",
            "All parameters, included the input file, can be read/wite to config file.",
            "Next only for command line (default: read INI jrealize.cfg)",
            "--CLoadconfig FILE        read option/param from a config file, INI mode ",
            "--CSaveconfig=FILE        save all options/parameter to a conifg file, INI mode",
            "--CXmlconfigload=FILE     read option/param from a config file, XML mode ",
            "--CKxmlconfigsave=FILE    save all options/parameter to a conifg file, XML mode " };

	//
	static private final String Opts = "";
	//
	static private final String[] Params = { "-input", "i", "-outfile", "o",
            "c", "-context", "-DATA_DEFINITION", "-OUTPUT_STYLE", "-ENVFILE", "-TEMP0A", "-TEMP0B",
             "-TEMP0C","-XSL01", "-XSL02","-XSL03",
			"-omit_xml_declaration", "-indent", "-method", "-encoding",
			"-standalone", "-doctype_system", "-doctype_public",
			"-cdata_section_elements", "-media_type", "-usr_parameter" };

	//
    static private final String DEFAULT_ENVFILE = ".\\..\\..\\o2xdl-system.xml";
 	static private final String DEFAULT_CONFIG  = "jrealize.cfg";
	static public final  String FILETITLE = "by jrealize 2.03 (18/02/19)";
    static public final  String engine = "jrealize 2.03 in jo2xtools.jar";

    static private    String TEMP0A = "";
    static private    String TEMP0B = "";
    static private    String TEMP0C = "";
    static private    String XSL01 = "";
    static private    String XSL02 = "";
    static private    String XSL03 = "";
    static private    String oFileN = "";

	// end standard stuff
	// =======================================================

	/**
	 * Initialize all data structures. Custom setup, it uses ZeroFilter.startup
	 * for input/output standard processing.
	 *
	 * @param args
	 *            command line from main().
	 */
	protected static void startup(String[] args) {
		// =======================================================
		// static setup
        AABaseAppl.version = jrealize.version;
		AABaseAppl.helpStrings = jrealize.xmlHelp;
		AABaseAppl.fileTitle = jrealize.FILETITLE;
        // Dynamic setup
        aaBase = new AABaseAppl(Opts, Params);
        // read config
        aaBase.setConfigFile(DEFAULT_CONFIG);
        // next  read config and command line
		ZeroFilter.startup(args);
    	// end standard setup
        TEMP0A = aaBase.getParam("-TEMP0A", "C:\\temp\\core3_A.TMP");
        TEMP0B = aaBase.getParam("-TEMP0B", "C:\\temp\\core3_B.XML");
        TEMP0C = aaBase.getParam("-TEMP0C", "C:\\temp\\core3_C.XSL");
        XSL01  = aaBase.getParam("-XSL01",  "C:\\o2xdl-30\\CORE\\xslt\\coreT001.xsl");
        XSL02  = aaBase.getParam("-XSL02",  "C:\\o2xdl-30\\CORE\\xslt\\coreT002.xsl");
        XSL03  = aaBase.getParam("-XSL03",  "C:\\o2xdl-30\\CORE\\xslt\\coreT003.xsl");
  //  out file
        oFileN = aaBase.getParam("o", 0);
        if (oFileN == null)
            oFileN = aaBase.getParam("-outfile", 0);
/*    forces outfile
  try {
        if (oFileN == null){
            oFileN = inFile.getCanonicalPath();
            oFileN = oFileN.substring(0, oFileN.lastIndexOf("."))+"out.xml";
       }

  } catch (IOException e){
        e.printStackTrace();
  }
*/
       return;
	}

	/**
	 * Main static, implements a XSLT transformer.
	 * All user optional output properties are applied to Transformer.
	 *
	 * @param args command line processed by startup()
	 */

	public static void main(String[] args) {
        startup(args);
        if (inFile == null){
            int k = aaBase.getParamCount(AABaseAppl.SPECIALP_SAVE)+
             aaBase.getParamCount(AABaseAppl.SPECIALP_XSAVE) ;
            if (k > 0){
                    System.out.println("Done ok.");
                    System.exit(AABaseAppl.PROCESS_OK);
                    }
            else
                aaBase.helpAndDies("ERROR: missed input file");
            }

 		try {
      // tmp file cleanup
            File tFile1 = new File(TEMP0A);
            if (tFile1.exists())
                  tFile1.delete();
            else
                tFile1.getParentFile().mkdirs();

            File tFile2 = new File(TEMP0B);
            if (tFile2.exists())
                  tFile2.delete();
           else
                tFile2.getParentFile().mkdirs();

            File tFile3 = new File(TEMP0C);
            if (tFile3.exists())
                  tFile3.delete();
            else
                tFile3.getParentFile().mkdirs();

 // o2x:object: reading target attribute:
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inFile.getCanonicalPath());
 // get the 'target' value from the o2x:object (if any).
            NodeList nl = doc.getElementsByTagName("o2x:object");
            try {
            if ((nl != null) &&(nl.item(0) != null)) {
                NamedNodeMap nnm =  nl.item(0).getAttributes();
                if (nnm != null){
                    Node XTarg = nnm.getNamedItem("target");
                    if ( XTarg != null){
                         oFileN = XTarg.getNodeValue();
                    }
                 }
              }
            }catch (Exception e) {;}
//  output file cleanup
            if (oFileN != null){
                String value = aaBase.getParam("-method", "html");
                oFileN = oFileN.substring(0, oFileN.lastIndexOf("."))+"."+value;
                File oFile = new File(oFileN);
                if (oFile.exists())
                    oFile.delete();
                else
                    oFile.getParentFile().mkdirs();
                }
            String o2xSisFile =  aaBase.getParam("-ENVFILE", DEFAULT_ENVFILE);

 //============= processing operations
        if (oneStep(inFile.getCanonicalPath(), XSL01, TEMP0A, o2xSisFile, 1))
           if (oneStep(TEMP0A, XSL02, TEMP0B, o2xSisFile, 2))
             if (oneStep(TEMP0A, XSL03, TEMP0C, o2xSisFile, 3))
                  oneStep(TEMP0B, TEMP0C, oFileN, o2xSisFile, 4);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

    /**
     *  @brief Make one transformation. In total Realize requires 4 cascading transformations.
     *
     *  @param [in] xmlFile the  XML file
     *  @param [in] xfile the XSLT file
     *  @param [in] ofile the output file
     *  @param [in] cfgfile the o2xdl environment file
     *  @param [in] step step number, to change the routine behavior.
     *  @return true if all ok
     *
     *  @details Some output parameters are used only for last transformation.
     */
    public static boolean oneStep(String xmlFile, String xfile, String ofile, String cfgfile, int step ) {
        try {
            File xslFile = new File(xfile);
            if (!xslFile.canRead())
                aaBase.helpAndDies("ERROR: cant read xsl file " + xfile);
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();
            // Use the factory to create a template containing the xsl file
            Templates template = factory.newTemplates(new StreamSource(
                    new FileInputStream(xslFile)));
            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();
            String value;
    //========== Setting default output properties
            if (step == 4) {
            // only for last transformation
                value = aaBase.getParam("-method", "html");  // default
                xformer.setOutputProperty(OutputKeys.METHOD, value);

               value = aaBase.getParam("-indent", "yes");
               xformer.setOutputProperty(OutputKeys.INDENT, value);

               value = aaBase.getParam("-omit_xml_declaration", "yes");
               if (value != null)
                    xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, value);

               value = aaBase.getParam("-media_type", 0);
               if (value != null)
                 xformer.setOutputProperty(OutputKeys.MEDIA_TYPE, value);
               } else {
                // only for 1,2,3 transformations
               value = aaBase.getParam("-omit_xml_declaration", "no");
               if (value != null)
                    xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, value);

              value = aaBase.getParam("-indent", "no");
                   xformer.setOutputProperty(OutputKeys.INDENT, value);

              xformer.setOutputProperty(OutputKeys.METHOD, "xml");
             }

            if (step == 2) {
                 xformer.setParameter("output_xsl_fname", TEMP0C);
            }

            // for all: Setting optional output  properties
            value = aaBase.getParam("-standalone", "yes");
            xformer.setOutputProperty(OutputKeys.STANDALONE, value);

            value = aaBase.getParam("-encoding", "UTF-8");
            xformer.setOutputProperty(OutputKeys.ENCODING, value);

            value = aaBase.getParam("-doctype_system", 0);
            if (value != null)
                xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, value);

            value = aaBase.getParam("-doctype_public", 0);
            if (value != null)
                xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, value);

            value = aaBase.getParam("-cdata_section_elements", 0);
            if (value != null)
                xformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                        value);

    //=============== Passing o2x  XML parameters
            value = aaBase.getParam("-OUTPUT_STYLE", "HTML");
            xformer.setParameter("OUTPUT_STYLE", value);

            xformer.setParameter("env_file", cfgfile);

            xformer.setParameter("ENGINE", engine);

            LocalDateTime localTime = LocalDateTime.now();
            String tnow = localTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME );
            xformer.setParameter("now_time", tnow);
            if (inFile == null)
                xformer.setParameter("source", "standard_input");
            else {
                xformer.setParameter("source", inFile.getName().toLowerCase());
            }

            // if context != 'none' overwrite default in environ file
            value  = aaBase.getParam("c", 0);
            if (value == null)
                value = aaBase.getParam("-context", "none");
            xformer.setParameter("environ_context", value );

         // if DATA_DEFINITION != 'none' overwrite default in environ file

            value = aaBase.getParam("-DATA_DEFINITION", "none");
            xformer.setParameter("DATA_DEFINITION", value);

         // Passing user params
            for (int i = aaBase.getParamCount("-usr_parameter"); i > 0; i--) {
                String[] splitted = aaBase.getParam("-usr_parameter", i).split(
                        "\\*", 2);
                if (splitted.length > 1)
                    xformer.setParameter(splitted[0], splitted[1]);
            }

     // ======== end setting properties
              // Apply the xsl file to the source and put result to out
            if (ofile != null)
               xformer.transform( new StreamSource( new FileInputStream(xmlFile)), new StreamResult(new FileOutputStream(ofile)));
            else
                xformer.transform( new StreamSource( new FileInputStream(xmlFile)), new StreamResult((OutputStream)fout));
           return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // An error occurred in the XSL file
            e.printStackTrace();
        } catch (TransformerException e) {
            // An error occurred while applying the XSL file
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
