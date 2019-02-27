/*
 *This program is free software; you can redistribute it and/or modify
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
 * Associtive Array classes for applications.
 */

package ms.utils.aarray.baseapp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

import ms.utils.aarray.AArray;

/**
 * base class for Applications: manages help, configuration file and Command Line
 * parameters.
 *
 * Extends {@ref ms.utils.aarray.AArray} class with a general purpose command line
 * parser plus simple managers for help and configuration file.<br />
 * Applicatiob can redefine AABaseAppl.version,  AABaseAppl.helpStrings, AABaseAppl.fileTitle.

 * @author M. Sillano (marco.sillano(at)gmail.com) &copy;2006-2010 M.Sillano
 * @version 2.04 2018/03/22  m.s. XML config automation
 * @version 2.03 2010/01/12  m.s. javadoc revision
 */

public class AABaseAppl extends AArray {

	/**
	 * default comment added to config file.
	 *
	 * To be overwrite by application.
	 */

	public static String fileTitle = "by AABaseApp version 2.03 2010/01/12";

	/**
	 * default value: sample help in Unix style.
	 *
	 * To be overwrite by application.
	 */
	public static String[] helpStrings = {
			"Usage:     AABaseAppl -h|-?|--help|--version",
			"Base class for Applications: manages help, config file, Command Line parameters.",
			"           See Javadoc documentation.",
			"options:   -h|-?|--help    display this help and exit.",
			"           --version       print version info and exit." };

	/**
	 * constant value for I/O file INI mode.
	 */
	public static final int MODE_INI = 2;

	/**
	 * constant value for I/O file text mode.
	 */
	public static final int MODE_TXT = 0;

	/**
	 * constant value for I/O file XML mode.
	 */
	public static final int MODE_XML = 1;

	/**
	 * constant for System.exit().
	 */
	public static final int PROCESS_FAILURE = 10;

	/**
	 * constant for System.exit().
	 */
	public static final int PROCESS_HELP = 1;

	/**
	 * constant for System.exit().
	 */
	public static final int PROCESS_OK = 0;

	/**
	 * default value: sample version in Unix style.
	 *
	 * To be overwrite by using application.
	 */
    public static String version = "AABaseApp version 2.04 2018/03/22 \n"
			+ "Copyright (C) 2006-2010  M.Sillano \n"
			+ "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html> \n"
			+ "This is free software: you are free to change and redistribute it. \n"
			+ "There is NO WARRANTY, to the extent permitted by law.";

	private static String[] aLongNames = { "-help" };

	private static String configName = "baseappl.ini";

	// used as flag by testALongParam()
	private static final String FOUND = "FOUND";

	static private final long serialVersionUID = 1112233L;

	/**
	 * predefined long help option. Abbreviated: --h: not allowed any custom -h*
	 * option.
	 */
	private static final String SPECIALP_HELP = "-help";

	/**
	 * predefined short help options.
	 */
	private static final String SPECIALP_HELP2 = "?h";

	/**
	 * predefined load config file parameter. Abbreviated: --CL: not allowed any
	 * custom -CL* option.
	 */
	public static final String SPECIALP_LOAD = "-CLoadconfig";

	/**
	 * predefined save config file parameter. Abbreviated: --CS: not allowed any
	 * custom -CS* option.
	 */

    public static final String SPECIALP_SAVE = "-CSaveconfig";
    /**
     * predefined load config file parameter. Abbreviated: --CX: not allowed any
     * custom -CX* option.
     */
    public static final String SPECIALP_XLOAD = "-CXmlconfigload";

    /**
     * predefined save config file parameter. Abbreviated: --CK: not allowed any
     * custom -CK* option.
     */
    public static final String SPECIALP_XSAVE = "-CKxmlconfigsave";


	/**
	 * predefined long version option. Abbreviated: --v: not allowed any custom
	 * -v* option)
	 */
	private static final String SPECIALP_VER = "-version";

	private static String sShortNames = "?h";

	// default
	private int mode = MODE_INI;

	// ----------------- private attributes
	// args index for parsing
	private int pos;

	// default
	private int verbose = 2;

	/**
	 * Constructor, updates application specific attributes.
	 *
	 * @param shortNames
	 *            application supplied String for short option names.
	 * @param longNames
	 *            application supplied String array for long option/parameter
	 *            names.
	 */
	public AABaseAppl(String shortNames, String[] longNames) {
		super();
		sShortNames = shortNames + SPECIALP_HELP2;
		// must be exact: 5 = count(SPECIAL)
		aLongNames = new String[longNames.length + 5];
		int i = 0;
		for (; i < longNames.length; i++) {
			aLongNames[i] = longNames[i];
		}
		aLongNames[i++] = SPECIALP_VER;
		aLongNames[i++] = SPECIALP_SAVE;
		aLongNames[i++] = SPECIALP_LOAD;
        aLongNames[i++] = SPECIALP_XSAVE;
        aLongNames[i++] = SPECIALP_XLOAD;
     }

	/**
	 * parser for all command line Arguments.
	 * <UL>
	 * <li>On success, Options, Parameters and Arguments are stored in
	 * <CODE>this</CODE>, and special Options/Parameters are processed.
	 * <li>On failure, puts an error message (if verbose >0) and exit program.
	 * </UL>
	 *
	 * @param args
	 *            from main()
	 */
	public void commandLineParse(String[] args) {
		if (args.length == 0)
			return;
		if (findSpecialParam(args, SPECIALP_HELP) != null)
			helpAndDies(null);
		if (findSpecialParam(args, SPECIALP_VER) != null) {
			System.out.println(version);
			System.exit(PROCESS_OK);
		}
		testAndLoadConfig(args);
		// processe command line args:
		pos = 0;
		boolean found;
		do {
			// the order minds
			found = parseLongN(args);
			found = found || parseShortN(args);
			found = found || parseArguments(args);
		} while (found);
		if (args.length == pos) {
			// done ok
 			testAndSaveConfig(args);
			return;
		}
		// done ko
		helpAndDies("ERROR: bad argument " + args[pos] + " in command line.");
	}

	/**
	 * getter for config file path.
	 *
	 * @return the config path in use.
	 * @see #setConfigFile(String)
	 */
	public String getConfigFile() {
		return configName;
	}

	/**
	 * error and help messages.
	 * <UL>
	 * <LI>If <CODE> errMessage</CODE> is not null
	 * <UL>
	 * <LI>it puts the error message (if <CODE>verbose</CODE> >0) in standard
	 * error
	 * <LI>it puts the help message (if <CODE>verbose</CODE> >1) in standard
	 * output and exit program (exit code {@link #PROCESS_FAILURE}).
	 * </UL>
	 * <LI>If <CODE>errMessage</CODE> is null it puts the help message in
	 * standard output and exit program (exit code {@link #PROCESS_HELP}).
	 * </UL>
	 *
	 * @param errMessage
	 *            or null.
	 * @see #setVerbose(int)
	 */
	public void helpAndDies(String errMessage) {
		if (errMessage != null) {
			if (verbose > 0)
				System.err.println(errMessage);
			if (verbose > 1)
				for (int i = 0; i < helpStrings.length; i++)
					System.out.println(helpStrings[i]);
			System.exit(PROCESS_FAILURE);
		}
		for (int i = 0; i < helpStrings.length; i++)
			System.out.println(helpStrings[i]);
		System.exit(PROCESS_HELP);
	}

	/**
	 * loads configuration file using defaults.
	 * The file content is merged to existing Properties in this.
	 */
	public void loadConfigFile() {
		loadConfigFile(configName, mode);
	}

	/**
	 * basic load for configuration file.
	 * The file content is merged to data in Properties.
	 *
	 * @param f
	 *            the file path
	 * @param mode
	 *            the I/O file mode. (one of MODE_INI, MODE_XML, MODE_TXT).
	 * @exception Error
	 *                In case of error exit program.
	 */
	public void loadConfigFile(String f, int mode) {
		try {
			readConfigFile(f, mode);
		} catch (FileNotFoundException e) {
			helpAndDies("ERROR: cannot found the file " + f);
		} catch (InvalidPropertiesFormatException e) {
			helpAndDies("ERROR: bad format in file " + f);
		} catch (IOException e) {
			helpAndDies("ERROR: cannot read the file " + f);
		}
	}

	/**
	 * saves Properties in a configuration file using defaults.
	 *
	 * @see #fileTitle
	 * @see #setIOMode(int)
	 * @see #setConfigFile(String)
	 */
	public void saveConfigFile() {
		saveConfigFile(configName, mode, fileTitle);
	}

	/**
	 * basic save to a configuration file.
	 *
	 * @param f
	 *            the file path
	 * @param mode
	 *            the I/O text format: one of MODE_INI, MODE_XML, MODE_TXT.
	 * @param comment
	 *            added to configuration file.
	 *
	 * @exception Error
	 *                if it can't write the file exit program.
	 */
	public void saveConfigFile(String f, int mode, String comment) {
		try {
			switch (mode) {
			case MODE_XML:
				this.storeToXML(new FileOutputStream(f), comment);
				break;
			case MODE_INI:
				this.storeToINI(new FileOutputStream(f), comment);
				break;
			case MODE_TXT:
				this.store(new FileOutputStream(f), comment);
				break;
			}
		} catch (Exception e) {
			helpAndDies("ERROR: cannot write the file " + f);
		}
	}

	/**
	 * setter for configuration file path.
	 *
	 * @param fName
	 *            the configuration file path.
	 * @see #getConfigFile()
	 */
	public void setConfigFile(String fName) {
		configName = fName;
	}

	/**
	 * setter for mode.
	 *
	 * @param mode
	 *            the text file format (one of MODE_INI, MODE_XML, MODE_TXT)
	 * @see java.util.Properties
	 *
	 */
	public void setIOMode(int mode) {
		if ((mode == MODE_INI) || (mode == MODE_XML) || (mode == MODE_TXT))
			this.mode = mode;
		else
			helpAndDies("ERROR: IOmode " + mode + " not allowed");
	}

	/**
	 * setter for verbose.
	 *
	 * @param level
	 *            int, one of 0:(quiet) 1:(error) 2:(verbose)
	 * @see #helpAndDies(String)
	 */
	public void setVerbose(int level) {
		if ((level >= 0) && (level < 3))
			this.verbose = level;
		else
			helpAndDies("ERROR: Verbose " + level + " not in 0..2");
	}

	/**
	 * parses all <CODE>args[]</CODE> for match with a specified parameter or long option.
	 *
	 * @param args
	 *            command line from main()
	 * @param param
	 *            the parameter name.
	 * @return if found returns the parameter <CODE>value</CODE> (if it exists)
	 *         or the "{@link #FOUND}" String, else null.
	 */
	private String findSpecialParam(String[] args, String param) {
		pos = 0;
		while (pos < args.length) {
			if (args[pos].charAt(0) == '-') {
				String value = testALongParam(args, param, false);
				if (value != null)
					return value;
			}
			pos++;
		}
		return null;
	}

	/**
	 * step: parses <CODE>args[pos]</CODE> for Arguments.
	 *
	 * @param args
	 *            command line as array from main(), this tests only the argument
	 *            at position <CODE>pos</CODE>.
	 * @return
	 *         - On match, adds Argument to AArray, increments  <CODE>pos</CODE> and returns true <br />
	 *         - If actual arg is not an Argument string returns false
	 * @pre. the same args[pos] must be processed before by
	 *       {@link #parseShortN(String[])} and {@link #parseLongN(String[])}.
	 */
	private boolean parseArguments(String[] args) {
		if (pos >= args.length)
			return false;
		if (args[pos].charAt(0) == '-')
			return false;
		setArgument(args[pos]);
		pos++;
		return true;
	}

	// ---------------- PARAM

	/**
	 * step: parses <CODE>args[pos]</CODE> and extracts parameters or long options.
	 *
	 * @param args
	 *            command line as array from main(), this tests only the argument
	 *            at position <CODE>pos</CODE>.
	 * @return
	 *         - On match, adds Parameter/option to AArray, increments  <CODE>pos</CODE> and returns true <br />
	 *         - If actual arg is not a parameter string returns false
	 */
	private boolean parseLongN(String[] args) {
		if (pos >= args.length)
			return false;
		String p = args[pos].trim();
		if (p.charAt(0) != '-')
			return false;
		for (int i = 0; i < aLongNames.length; i++) {
			if (testALongParam(args, aLongNames[i], true) != null)
				return true;
		}
		return false;
	}

    /**
	 * step: parses <CODE>args[pos]</CODE> vs. usrOpts, case sensitive.
	 *
	 * @param args
	 *            command line from main()-
	 * @return
	 *        - if option in SPECIALP_HELP2 print help and exit program.
	 *        - on match, add all Option keys to this, and returns true
	 *        - if the actual arg is not an option string returns false
	 *        - if it founds an option not in usrOpts, prints an error message and exit
	 *         program.
	 *
	 * @pre. usrOpts is a global string with all the chars allowed as short
	 *       option.
	 */
	private boolean parseShortN(String[] args) {
		if (pos >= args.length)
			return false;
		if (args[pos].length() < 2)
			return false;
		if (args[pos].charAt(0) != '-')
			return false;
		for (int i = 1; i < args[pos].length(); i++) {
			int k = sShortNames.indexOf(args[pos].charAt(i));
			if (k == -1) {
				if (i == 1)
					return false;
				helpAndDies("ERROR: bad option " + args[pos].charAt(i) + " in "
						+ args[pos]);
			}
			if (SPECIALP_HELP2.indexOf(sShortNames.charAt(k)) != -1)
				helpAndDies(null);
			setOption(args[pos].substring(i, i + 1));
		}
		pos++;
		return true;
	}

	/**
	 *
	 * basic read configuration file.
	 *  The file content is merged to data in Properties.
	 *
	 * @param f
	 *            the file path
	 * @param mode
	 *            the I/O text file mode.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidPropertiesFormatException
	 */
	private void readConfigFile(String f, int mode)
			throws InvalidPropertiesFormatException, FileNotFoundException,
			IOException {
		switch (mode) {
		case MODE_XML:
			this.loadFromXML(new FileInputStream(f));
			break;

		case MODE_INI:
			this.loadFromINI(new FileInputStream(f));
			break;

		case MODE_TXT:
			this.load(new FileInputStream(f));
			break;
		}
	}

	/**
	 *test for long names bad abbreviate.
	 * @return
	 *    If <CODE>xpar</CODE> match more than one aLongNames[] prints error message
	 *    and exit.
	 */
	private void testAlias(String xpar) {
		int count = 0;
		for (int i = 0; i < aLongNames.length; i++)
			if (aLongNames[i].startsWith(xpar))
				count++;
		if (count > 1)
			helpAndDies("ERROR: the abbreviate name -" + xpar
					+ " is not unique.");
		// System.out.println("test:" +xpar +" = "+ count); // debug
		return;
	}

	/*
	 * test a single parameter or long option in <CODE>args[pos]</CODE>.
	 *
	 * @param args
	 *            from main, uses pos as index
	 * @param param
	 *            long name
	 * @param save
	 *            if true updates AArray.
	 * @return the value or FOUND or null.
	 */
	private String testALongParam(String[] args, String param, boolean save) {
		String[] parts = args[pos].trim().split("[ =]+", 2);
		parts[0] = parts[0].trim();
		int k = Math.min(param.length() + 1, parts[0].length());
		if (parts[0].substring(1, k).compareTo(param.substring(0, k - 1)) == 0) {
			// found!
			pos++;
			testAlias(param.substring(0, k - 1));
			// case "param value"
			if (parts.length > 1) {
				parts[1] = parts[1].trim();
				if (parts[1].length() > 1) {
					if (save)
						setParam(param, parts[1]);
					return parts[1];
				}
			}
			// case param -nextparam
			if ((pos >= args.length) || (args[pos].charAt(0) == '-')) {
				if (save)
					setOption(param);
				return FOUND;
			}
			// case param = value
			if (args[pos].equals("="))
				pos++;
			// case param value
			if (save)
				setParam(param, args[pos]);
			pos++;
			return args[pos - 1];
		}
		return null;
	}

	/**
	 * Test for SPECIALP_LOAD argument.
	 * @post.
	 * - If found loads the configuration file.
	 * - Updates default configuration path
	 * - Continues without error if configuration file don't exists.
	 */

	public void testAndLoadConfig(String[] args) {
        String fname = findSpecialParam(args, SPECIALP_XLOAD);
        if ((fname != null) && (!fname.equals(FOUND))){
            configName = fname;
            this.mode = MODE_XML;
         }
        fname = findSpecialParam(args, SPECIALP_LOAD);
		if ((fname != null) && (!fname.equals(FOUND))){
            configName = fname;
            this.mode = MODE_INI;
		}
	    try {
			readConfigFile(configName, this.mode);

			} catch (FileNotFoundException e) {
				// helpAndDies("ERROR: file not found " + configName);
				// continues, no error
			} catch (InvalidPropertiesFormatException e) {
				helpAndDies("ERROR: bad format in file " + configName);
			} catch (IOException e) {
				helpAndDies("ERROR: I/O error reading the file " + configName);
			}

	}


	/**
	 * Test for SPECIALP_SAVE argument.
	 *@post.
	 * - if found save the configuration file.
	 * - Does not update default configuration path and mode.
	 *
	 * @exception ioerror
	 *                if it cant write the file exit program.
	 */
	private void testAndSaveConfig(String[] args) {
        String fname = findSpecialParam(args, SPECIALP_XSAVE);
        if ((fname != null) && (!fname.equals(FOUND))){
              saveConfigFile(fname, MODE_XML, fileTitle);
         }
        fname = findSpecialParam(args, SPECIALP_SAVE);
        if ((fname != null) && (!fname.equals(FOUND))){
            saveConfigFile(fname, MODE_INI, fileTitle);
        }
	}

	/**
	 * Only for test purposes.
	 *
	 * This main reads and parses the Command Line, then it puts on standard
	 * output the result. It does also some more tests on objects and cleanup of
	 * configuration file.<br />
	 * Test it using different command line parameters (see samples) at run
	 * time.
	 *
	 * @param args
	 *            command line.
	 */

	public static void main(String[] args) {
		// ======= First define application strings ============
		// ----- usrOpts :
		String myOpts = "cKi";

		// ----- usrParam :
		String[] myParam = new String[4];
		myParam[0] = "-input";
		myParam[1] = "i";
		myParam[2] = "w";
		myParam[3] = "-upcase";

		// ----- helpLines ;
		String[] myHelp = new String[5];
		myHelp[0] = "Help for AABaseAppl.main";
		myHelp[1] = "  Short names String is:  \"" + myOpts + "\"";
		String mss = "  Long names array is:    {";
		for (int i = 0; i < myParam.length; i++) {
			mss += "'" + myParam[i] + "', ";
		}
		myHelp[2] = mss + "}";
		myHelp[3] = "  Special Parameters are: {'?h','-help','" + SPECIALP_SAVE
				+ "','" + SPECIALP_LOAD + "'}";
		myHelp[4] = "Enjoy the test...bye";
		// =====================================================
		// Creates AABaseAppl using application strings
		AABaseAppl x = new AABaseAppl(myOpts, myParam);

		AABaseAppl.helpStrings = myHelp;
		// more custom values
		// x.setIOMode(MODE_TXT);
		x.setIOMode(MODE_INI);
		// x.setIOMode(MODE_XML);
		// x.setConfigFile("config.txt");
		x.setConfigFile("config.ini");
		// x.setConfigFile("config.xml");
		//
		// parses command line
		x.commandLineParse(args);
		System.out.println("*** command line parsed... ");
		// --------------- more tests:
		// store object - String
		String test = "";
		for (int i = 0; i < args.length; i++) {
			test += (args[i].indexOf(' ') == -1 ? args[i] : "\"" + args[i]
					+ "\"")
					+ " ";
		}
		x.setObject(test, "commandLine");
		// store object - Double
		Double n = new Double("12345.67E5");
		x.setObject(n, "aDouble");
		// load object String
		String s1 = x.getObject(String.class, "commandLine");
		System.out
				.println("*** Store/Read Object: String - Command line is \n*** "
						+ s1);
		// load object Double
		Double d1 = x.getObject(Double.class, "aDouble");
		System.out.println("*** Store/Read Object: Double is \n***" + d1);
		// --- direct INI style values
		// test for special chars and spaces in name/value
		System.out.println("*** Added special property...");
		x.setProperty("appli.c=a tion", "test.va lue_", "= test 2");
		System.out.println("*** read (ok = '= test 2') :'"
				+ x.getProperty("appli.c=a tion", "test.va lue_", "ERROR")
				+ "'");
		//
		System.out.println("*** Actual AArray content is: ");
		// --- obsolete, only for dump on screen
		x.list(System.out);
		// --- save configFile for test
		x.saveConfigFile("configFile.txt", MODE_TXT, "Test file");
		x.saveConfigFile("configFile.xml", MODE_XML, "Test XML");
		x.saveConfigFile("configFile.ini", MODE_INI, "Test INI");
		System.out
				.println("*** Saved configFile.txt, configFile.ini and configFile.xml");
		// test cleanAllSpecial
		x.cleanAllSpecial(true, true);
		System.out
				.println("*** After cleanAllSpecial(true, true) AArray content is:");
		// --- deprecated, used only here for dump on screen
		x.list(System.out);
		x.loadConfigFile("configFile.xml", MODE_XML);
		System.out.println("*** After re-load configFile");
		// --- deprecated, used only here for dump on screen
		x.list(System.out);
	}
}
