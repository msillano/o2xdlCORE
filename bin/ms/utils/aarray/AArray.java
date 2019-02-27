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

/**
 * @package ms.utils.aarray
 * lib for Associative Arrays using Properties.
 */

package ms.utils.aarray;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.net.URLDecoder;

import javax.xml.bind.JAXB;

/**
 * Associative Array to store command line arguments and Objects.
 *
 * This generic Associative Array is designed as an application central storage for
 * Command Line parameters, for config files content and for application
 * specific data (for session persistence).<br />
 * This class extends <CODE> java.util.Properties</CODE> and implements
 * the basic store/retrieve for <i> options, parameters, arguments </i>  and for
 * application private <i>JABX Objects</i>.
 * This class adds also a new file format, the  <i>INI format </i>  (TXT and XML
 * formats are implemented in Properties).<br />
 * This class uses  2 levels (section + key) or 3 levels (section + key + index)
 * Properties keys schemas.<br />
 * Subclasses MUST implement the data policy and the Command Line parsing. <br />
 *
 * <h4> Options </h4>
 *      Options are boolean values, valuing "true" if present. The Option name is a
 *      String (case sensitive), short (as "?") or long (like "-help").<br />
 *      Options are stored as: <CODE> CL_OPTION_PREFIX+"."+name+"="+"true"</CODE>
 *      Applications can use  {@link #isOption(String)} and {@link #setOption(String)}.
 *
 * <h4> Parameters </h4>
 *      Parameters are  String pairs: name=value. The Parameter name is a String
 *      (case sensitive), short (as "c") or long (like "-config").<br />
 *      Multiple instances of same Parameter are allowed, and stored using an incremental
 *      index starting from 1.<br />
 *      Parameters are stored as pair: <CODE> CL_PARAM_PREFIX+"."+name+"#"+index+"="+value</CODE><br />
 *      Applications can use  {@link #isParameter(String)}, {@link #getParam(String, String)}
 *       and {@link #setParam(String, String)}, to handle the last parameter value, or  {@link #getParamCount(String)}
 *       and {@link #getParam(String, int)} to access Parameters via key+index.
 *
 *<h4> Arguments </h4>
 *      Arguments are  String values not associated with a name. They
 *      are processed as Parameter, using the conventional name  <CODE>CL_ARGUMENT</CODE>. So all
 *      Argument() methods are helper functions, here for convenience.<br />
 *      Arguments are stored as pair:  <CODE> CL_PARAM_PREFIX+"."+CL_ARGUMENT+"#"+index+"="+value</CODE><br />
 *      Applications can use {@link #getArgumetsCount()} and {@link #getArgument(int)}.
 *
 *<h4>Objects</h4>
 *      JAXB object {@link javax.xml.bind.JAXB} are stored as a serialized XML tree, associated with a name.
 *      The name must be unique. <br />
 *      This allows store/retrieve typed values:
 *      Objects are stored as pair:  <CODE> OBJECT_PREFIX+"."+name=XML_value </CODE><br />
 *      Applications can use  {@link #setObject(Object, String)} and {@link #getObject(Class, String)}
 *
 *<h4>Values</h4>
 *     To put any char in Values these must be 'URLencoded': <br />
 *       accepts(a-z,A-Z,0-9,'-','.','*','_'), space='+' or '%20', others =%XX (ex) <br />
 *     In Input a value MUST be URLencoded <br />
 *     In Output, getProperty() give values URLDecoded. <br />
 *
 *<h4> INI file format</h4>
 *
 *  The INI file format is a text format which is divided into sections
 *  and couples key-value. In one section all keys must have unique
 *  names, likewise in an INI file we can have many sections but they
 *  must have unique names. Sections are not case sensitives, but keys are.<br />
 *  The key string cannot contain the char "=".
 *  <p> This simple implementation ignores comments and
 *  multiline values. <br />
 *  Complete implementations can be found at following
 *  links, (thanks to authors: Prasad Khandekar, Nik Haldimann, William
 *  Denniss).<br />
 *  An INI file looks like this: <pre>
 * [ASection]
 *   OptionValue=true
 *# comment
 *   list=Number 2
 *; also comment
 * [AnotherSection]
 *   MoreValues=yes
 * </pre> Sections are not case sensitive and are combined with keys in
 *  the Properties HashList key. From previous sample: <pre>
 *   asection.OptionValue=true
 *   asection.list=Number 2
 *   anothersection.MoreValues=yes
 * </pre>
 *  Applications can use {@link #loadFromINI(InputStream)}
 * and {@link #storeToINI(OutputStream, String)} and they can manage values defined by <i>section:key</i> (INI like) using
 *  {@link #setProperty(String, String, String)}, {@link #getProperty(String, String, String)} and
 *  {@link #deleteProperty(String, String)}.
 *  <br />Cleanup can be done using {@link #cleanAllSpecial(boolean, boolean)}.<br />
 * Saving in TXT or XML modes can be done using parent Properties methods.
 *
 * @see   <a href="http://www.codeproject.com/useritems/INIFile.asp?print=true" >INI sample code</a>
 * @see   <a href="http://ubique.ch/code/inieditor/" >more INI sample code</a>
 * @see   <a href="http://tanksoftware.com/jtank/src/showsrc.php?src=src/net/jtank/io/Ini.java" >more INI sample code</a>
 *
 * @author M. Sillano (marco.sillano(at)gmail.com) &copy;2006-2010 M.Sillano
 * @version 2.03 2010/01/12  m.s. javadoc revision
 */

/*
 * @version 2.03 2019/02/16  m.s. added value URLencode/decode
 * @version 2.03 2010/01/12  m.s. javadoc revision
 * @version 2.02 2009/08/12  m.s. added INI read/save, changed internal format
 * @version 2.01 2009/03/25  m.s. command line parameters.
 * @version 1.03 2006/12/14  m.s. revision
 * @version 1.01 2006/11/03  m.s. initial draft
 */

public class AArray extends Properties {

    /**
     * for Arguments.
     */
    public static final String CL_ARGUMENT = "argument";

    /**
     * for Options.
     */
    public static final String CL_OPTION_PREFIX = "options";

    /**
     * for Parameters.
     */
    public static final String CL_PARAM_PREFIX = "parameters";

    /**
     * Used as separator for <CODE>index</CODE> values.
     *
     * Any char, also "".
     */
    public static final String INDEX_CHAR = "#";

    /**
     * Used as separator for <CODE>section</CODE> and <CODE>key</CODE> values.
     *
     * If <CODE>PREFIX_CHAR</CODE> is present in the <CODE>section</CODE> it will be escaped.
     */
    public static final String PREFIX_CHAR = ".";

    private static final String CL_OPTION_FLAG = "true";

    /**
     * for Objects
     */
    private static final String OBJECT_PREFIX = "objects";

    private static final long serialVersionUID = 111222L;

    /**
     * default constructor.
     */
    public AArray() {
        super();
    }

    /**
     * constructor  using a default Properties Object.
     * @param arg0 the default Properties
     * @see   java.util.Properties#Properties(Properties)
     */
    public AArray(Properties arg0) {
        super(arg0);
    }

    /**
     * constructor from a Config file in INI format.
     *
     * Use of different config format can be done after creation.
     * @param INIfile file path
     * @see #load(InputStream)
     * @see #load(java.io.Reader)
     * @see #loadFromXML(InputStream)
     * @see #loadFromINI(InputStream)
     */
    public AArray(String INIfile) {
        super();
        try {
            loadFromINI(new FileInputStream(INIfile));
        } catch (FileNotFoundException e) {
            // nothing to do
        } catch (IOException e) {
            // nothing to do
        }
    }

    /**
     * deletes all Command Line elements and/or Objects stored in <CODE> this.</CODE>
     *
     *  Usefull to save a clean config file.
     *
     * @param CLine
     *            if true deletes all Options, Parameters and Arguments in  <CODE>this.</CODE>.
     * @param Obj
     *            if true deletes all Objects in  <CODE>this.</CODE>.
     */
    public void cleanAllSpecial(boolean CLine, boolean Obj) {
        Set<String> k = this.stringPropertyNames();
        for (String akey : k) {
            if ((CLine) && (akey.startsWith(CL_OPTION_PREFIX)))
                this.remove(akey);
            if ((CLine) && (akey.startsWith(CL_PARAM_PREFIX)))
                this.remove(akey);
            if ((Obj) && (akey.startsWith(OBJECT_PREFIX)))
                this.remove(akey);
        }
    }

    /**
     * general purpose delete for properties.
     * Use <CODE>section+key</CODE> access.
     * @param section first level key
     * @param key second level
     */
    public void deleteProperty(String section, String key) {
        this.deleteProperty(section, key, 0);
    }

    /**
     * general pourpose delete for properties
     * Use <CODE> section+key+index</CODE> access.
     * @param section first level key
     * @param key second level
     * @param index the index (&gt;0) or 0 (no index)
     */
    public void deleteProperty(String section, String key, int index) {
        this.remove(buildAAKey(section, key, index));
    }

    /**
     * gets the value of an Argument.
     * If <CODE> index==0</CODE> returns the last <CODE>value</CODE> (i.e. the max sequential index in this).
     *
     * @param index the instance number (starting from 1) or 0.
     * @return the String Argument value if it is present, or null;
     * @see #getArgumetsCount()
     */
    public String getArgument(int index) {
        return getParam(CL_ARGUMENT, index);
    }

    /**
     * gets the number of Arguments in <CODE> this</CODE>.
     *
     * @return the number of Arguments.
     * @see #getArgument(int)
     */
    public int getArgumetsCount() {
        return getParamCount(CL_ARGUMENT);
    }

    // -------------------------- OBJECTS
    /*
     xs:anySimpleType
     xs:base64Binary
     xs:boolean
     xs:byte
     xs:decimal
     xs:double
     xs:float
     xs:hexBinary
     xs:int
     xs:integer
     xs:long
     xs:QName
     xs:short
     xs:string
     xs:unsignedByte
     xs:unsignedInt
     xs:unsignedShort
     */
    // @SuppressWarnings("")
    /**
     * Retrieves an jaxbObject stored in <CODE>this<CODE>.
     *
     * The use  of jaxbObject makes simple to store/retrieve typed values, that is useful for
     * application private data.
     * @param &lt;T&gt;
     * @param type
     *            the object class.
     * @param name
     *            unique, used as key for the object.
     * @return an instance of class <CODE>type</CODE> or null.
     * @see javax.xml.bind.JAXB
     * @see #setObject(Object, String)
     */
    public <T> T getObject(Class<T> type, String name) {
        if (this.containsKey(buildAAKey(OBJECT_PREFIX, name, 0))) {
            StringReader s = new StringReader(
                    this.getProperty(buildAAKey(OBJECT_PREFIX, name, 0)));
            return JAXB.unmarshal(s, type);
        }
        return null;
    }

    /**
     * Retrieves the <CODE> value</CODE> associated to a Parameter of given <CODE>name</CODE>.
     *
     * If index == 0 returns the last value (i.e. the max sequential index).
     *
     * @param name
     *            the parameter name.
     * @param index
     *            the instance number (starting from 1) or 0 (last).
     * @return the String <CODE>value</CODE> if present, or null;
     * @see #setParam(String, String)
     */
    public String getParam(String name, int index) {
        String thevalue = "";
        if (index == 0)
            thevalue =  this.getProperty(buildAAKey(CL_PARAM_PREFIX, name, getParamCount(name)));
        else
            thevalue =  this.getProperty(buildAAKey(CL_PARAM_PREFIX, name, index));
         try {
 //             System.err.println("param before "+thevalue);
              if (thevalue == null) return thevalue;
              thevalue = URLDecoder.decode(thevalue, "UTF-8");
              } catch (UnsupportedEncodingException e) {}   //nothing to do
 //         System.err.println("param after "+thevalue);
          return thevalue;

    }

    /**
     * getter, returns the Parameter value or the default.
     *
     * @param name
     *            the parameter name.
     * @param defaultValue
     *            a String as default.
     * @return the last value of the Parameter if it is present, else <CODE>defaultValue</CODE>;
     * @see #isParameter(String)
     * @see #setParam(String, String)
     */
    public String getParam(String name, String defaultValue) {
        return this.getProperty(CL_PARAM_PREFIX, name, getParamCount(name), defaultValue);
    }

    /**
     * gets the number of Parameters in <CODE>this</CODE> having the given <CODE> name </CODE>
     *
     * @param key
     *            the Parameter name.
     * @return Parameter count.
     *
     */
    public int getParamCount(String key) {
        return getParamNextIndex(key) - 1;
    }

    // --------------------------- ARGUMENTS

    /**
     * general purpose getter for properties using <CODE>section+key</CODE> access (INI like).
     * @param section first level key
     * @param key second level
     * @param defaultValue the default value
     * @return the value stored in <CODE>this</CODE> or default base value.
     * @see #setProperty(String, String, String)
     */
    public String getProperty(String section, String key, String defaultValue) {
        return this.getProperty(section, key, 0, defaultValue);
    }

    // --------------------------- OPTIONS
    /**
     * getter for of an Option having given <CODE>key</CODE>.
     *
     * @param key
     *            the Option name
     * @return true if Option is present.
     * @see #setOption(String)
     */
    public boolean isOption(String key) {
        return this.containsKey(this.buildAAKey(CL_OPTION_PREFIX, key, 0));
    }

    // ----------------------------- PARAMETERS
    /**
     * tests for a Parameter having gived <CODE>name</CODE>.
     *
     * @param name
     *            of the Parameter
     * @return true if a Parameter is present.
     * @see #getParam(String, String)
     */
    public boolean isParameter(String name) {
        return this.containsKey(buildAAKey(CL_PARAM_PREFIX, name, 1));
    }

    /**
     *  reads a config INI files.
     *
     *  This method allows to read all defaults from a INI config file.
     *  This is useful for WIN-java-linux compatibility and interoperability.<br />
     *
     * @param  in
     *               input stream
     * @throws IOException
     *               if INI file cant be read (e.g. bad file format)
     * @see #storeToINI(OutputStream, String)
     */
    public void loadFromINI(InputStream in) throws IOException {
        String section = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (line.length() > 0) {
                // Creates a new section if found
                if (line.charAt(0) == '[') {
                    section = line.substring(1, line.lastIndexOf("]"));
                    section = section.replace("\\" + PREFIX_CHAR, PREFIX_CHAR);
                } else
                // Ignores comments and reads only the key/value pair
                if ((line.charAt(0) != ';') & (line.charAt(0) != '#'))
                    if (line.indexOf("=") != -1) {
                        String pKey = line.substring(0, line.indexOf("=")).trim();
                        String pValue = line.substring(line.indexOf("=") + 1);
                        if ((section.length() > 0) & (pKey.length() > 0)) {
                            this.setProperty(section, pKey, 0, pValue.trim());
                        }
                    }
            }
            // reads the next line
            line = br.readLine();
        }
    }

    /**
     * stores a <CODE>jaxbObject</CODE> in <CODE> this</CODE> as XML string.
     *
     * The use  of jaxbObject makes simple to store/retrieve typed values, that is useful for
     * application private data.
     * @param jaxbObject
     *            the object.
     * @param name
     *            unique used as key for the object.
     * @see javax.xml.bind.JAXB
     * @see #getObject(Class, String)
     */
    public void setObject(Object jaxbObject, String name) {
        StringWriter w = new StringWriter();
        JAXB.marshal(jaxbObject, w);
        this.setProperty(OBJECT_PREFIX, name, 0, w.toString().replaceAll("[\n\r]", ""));
    }

    /**
     * stores the Option <CODE> key</CODE>.
     * Override existing option having same <CODE>key</CODE>.
     *
     * @param key
     *            the Option name
     * @see #isOption(String)
     */
    public void setOption(String key) {
        this.setProperty(CL_OPTION_PREFIX, key, CL_OPTION_FLAG);
    }

    /**
     * stores in <CODE>this</CODE> a new Parameter.
     *
     * It never overwrites an existing Parameters having the given <CODE>name</CODE>, but increments index.
     *
     * @param name
     *            the Parameter name.
     * @param value
     *            the Parameter value.
     *@see #getParam(String, int)
     *@see #getParam(String, String)
     */
    public void setParam(String name, String value) {
        this.setProperty(CL_PARAM_PREFIX, name, getParamNextIndex(name), value);
    }

    /**
     * general purpose setter for properties using <CODE>section:key</CODE> access (INI like).
     * @param section first level key
     * @param key second level
     * @param value the property value
     * @see #getProperty(String, String, int, String)
     */
    public void setProperty(String section, String key, String value) {
        this.setProperty(section, key, 0, value);
    }

    /**
     *  save <CODE>this</CODE> using the INI text format.
     * @param out
     *              Output Stream
     * @param comments
     *              a string added at file header
     * @see  #loadFromINI(InputStream)
     *
     */
    public void storeToINI(OutputStream out, String comments)
    //            throws IOException
    {
        PrintWriter outf = new PrintWriter(out);
        outf.println("# " + comments);
        String section = "";
        TreeSet<String> k = new TreeSet<String>(this.stringPropertyNames());
        for (String akey : k) {
            //            System.out.println(akey);
            String split[] = akey.split("(?<!\\\\)\\" + PREFIX_CHAR, 2);

            if (split[0].compareTo(section) != 0) {
                section = split[0];
                outf.println("["
                        + section.replace("\\" + PREFIX_CHAR, PREFIX_CHAR)
                        + "]");
            }

            outf.println(split[1] + "=" + this.getProperty(akey));
        }
        outf.flush();
        outf.close();
    }

    /**
     * Dump of all properties in text format.
     */
    @Override
    public String toString(){

         String s ="";
        TreeSet<String> k = new TreeSet<String>(this.stringPropertyNames());
        for (String akey : k) {
             s += akey + "="+ this.getProperty(akey)+"\n";
        }
    return s;
    }



    // -------------- private: utilities 2/3 COMPOSITE KEYS
    /**
     * creates an escaped composite key entry for section + key [+ index]
     * @param section
     * @param key
     * @param index the index (&gt;0) or 0 (no index)
     * @return the composite key used in Properties
     */
    private String buildAAKey(String section, String key, int index) {
        String sec = section.toLowerCase().trim().replace(PREFIX_CHAR, "\\"
                + PREFIX_CHAR);
        return index == 0 ? sec + PREFIX_CHAR + key : section + PREFIX_CHAR
                + key + INDEX_CHAR + index;
    }

    // the parameter next index, from parameter name
    private int getParamNextIndex(String param) {
        int x = 1;
        while (this.containsKey(buildAAKey(CL_PARAM_PREFIX, param, x)))
            x++;
        return x;
    }

    /**
     * general purpose getter for properties using <CODE>section+key+index</CODE> access
     * @see #setProperty(String, String, int, String)
     */
    private String getProperty(String section, String key, int index,
            String defaultValue) {
          String thevalue = "";
          try {
              thevalue = this.getProperty(buildAAKey(section, key, index), defaultValue);
   //           System.err.println("before "+thevalue);
              thevalue = URLDecoder.decode(thevalue, "UTF-8");
              } catch (UnsupportedEncodingException e) {}   //nothing to do
   //       System.err.println("after "+thevalue);
          return thevalue;
         }

    /**
     * general purpose setter for properties using <CODE>section:key:index</CODE> access
     * @param section
     * @param key
     * @param index (0 = no index)
     * @param value the property value
     * @see #getProperty(String, String, int, String)
     */
    private void setProperty(String section, String key, int index, String value) {
        this.setProperty(buildAAKey(section, key, index), value);
    }

    // swap parameter index keeping a regular index sequence.
    // @pre. param(key, from) was removed.
    private void swapIndex(String key, int from) {
        String nextKey = buildAAKey(CL_PARAM_PREFIX, key, from + 1);
        if (this.containsKey(nextKey)) {
            String v = getProperty(nextKey, "error");
            this.setProperty(CL_PARAM_PREFIX, key, from, v);
            this.remove(nextKey);
            swapIndex(key, from + 1);
        }
        return;
    }

    /**
     * deletes an Arguments in this.
     *
     * Renumbers all Arguments having a number
     * greater than <CODE> index</CODE>. If index==0 deletes all Arguments.
     *
     * @param index
     *            the instance number (starting from 1) or 0.
     */
    protected void deleteArgument(int index) {
        deleteParam(CL_ARGUMENT, index);
        swapIndex(CL_ARGUMENT, index);
    }

    /**
     * deletes an <CODE>jaxbObject</CODE> stored in <CODE> this</CODE>.
     *
     * @param name
     *            unique, used as key for the object.
     */
    protected void deleteObject(String name) {
        this.deleteProperty(OBJECT_PREFIX, name, 0);
    }

    /**
     * deletes an Option.
     *
     * @param key
     *            the Option name
     */
    protected void deleteOption(String key) {
        this.deleteProperty(CL_OPTION_PREFIX, key, 0);
    }

    /**
     * deletes a Parameter.
     *
     * It renumbers all Parameters having a number greater than <CODE> index</CODE>.
     * If index == 0 deletes all <CODE>name</CODE> Parameters.
     *
     * @param name
     *            the Parameter name.
     * @param index
     *            the instance number (starting from 1) or 0.
     */
    protected void deleteParam(String name, int index) {
        if (index == 0) {
            int x = 1;
            while (this.remove(buildAAKey(CL_PARAM_PREFIX, name, x++)) != null)
                ;
        } else {
            this.deleteProperty(CL_PARAM_PREFIX, name, index);
            swapIndex(name, index);
        }
    }

    /**
     * stores in this a String <CODE>value</CODE> as Argument.
     *
     * @param value
     *           the Argument value.
     */
    protected void setArgument(String value) {
        setParam(CL_ARGUMENT, value);
    }

    /**
     * only for test purposes.
     *
     * @param args command line.
     */

    public static void main(String[] args) {
        String iniConfigPath = "config.ini";
        AArray localOptions = new AArray(iniConfigPath);
        localOptions.list(System.out);
    }

}
