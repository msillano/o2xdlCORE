/*
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *  http://gnu.org/licenses/gpl.html
 */
// note: following documentation is escaped and formatted to
// give good HTML pages using Doxygen (not for javadoc).

/**
 * @file ZeroFilter.java
 * Base class for filter applications.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

import ms.utils.aarray.baseapp.AABaseAppl;
/**
 * Incomplete abstract base class for text filters, uses AABaseAppl.java.
 *
 * Supplies common declarations and methods required by all Filter applications.
 *
 * @author M. Sillano (marco.sillano(at)gmail.com) &copy;2006-2010 M.Sillano
 * @version 2.03 2010/01/12  m.s.
 */

public abstract class ZeroFilter {
    // ------- locals
    /**
     * Local newline System dependent.
     */
    public static String newline = System.getProperty("line.separator");

    /**
     * The Associative Array storing all command line option and application data, is initialized by startup()
     *
     * @see ms.aarray.AABaseAppl
     */
    protected static AABaseAppl aaBase;

    /**
     * Input File, or null  if using standard input.
     */
    protected static File inFile = null;

    /**
     * Input stream,
     * A file or Standard input, is initialized by startup()
     */
    protected static BufferedReader fin;

    /**
     * Output File, or null  if using standard output.
     */
    protected static File outFile = null;

    /**
     * Output stream.
     * A file or Standard output, is initialized by startup()
     */
    protected static PrintStream fout;

    /**
     * Initializes standard i/o required by all Filters.
     * - Parses the command line.
     * - Processes "-o=FILE", "--output=FILE", "-i=FILE" and "--input=FILE"
     * Must be called by a derived class.
     * @post
     *  - fin and fout initialized and ready
     *  - inFile != null only if input is from a file and not from sin.
     *  - outFile != null only if output is to a file and not to sout.
     *
     * @param args from main()
     */

    protected static void startup(String[] args) {
        //
        aaBase.commandLineParse(args);
        // syntax check output file
  //      if (!(aaBase.isOption("o") | aaBase.isOption("-output")))
  //          aaBase.helpAndDies("ERROR: output file mandatory.");
        //
        String oFileN = aaBase.getParam("o", 0);
        if (oFileN == null)
            oFileN = aaBase.getParam("-output", 0);

        // syntax check input file
  //      if (!(aaBase.isOption("i") | aaBase.isOption("-input")))
  //          aaBase.helpAndDies("ERROR: input file mandatory.");

        String iFileN = aaBase.getParam("i", 0);
        if (iFileN == null)
            iFileN = aaBase.getParam("-input", 0);
        if (iFileN == null)
          iFileN = aaBase.getArgument(1);

        // open in/out
        if (oFileN != null) {
            try {
                outFile = new File(oFileN);
                fout = new PrintStream(outFile);
            } catch (Exception e) {
                aaBase.helpAndDies("ERROR: cant write output file " + oFileN);
            }
        } else {
            fout = System.out;
        }
        //
        if (iFileN != null) {
            try {
                inFile = new File(iFileN);
                fin = new BufferedReader(new FileReader(inFile));

            } catch (Exception e) {
                aaBase.helpAndDies("ERROR: cant read input file " + iFileN);
            }
        } else
            fin = new BufferedReader(new InputStreamReader(System.in));
        return;
    }
}
