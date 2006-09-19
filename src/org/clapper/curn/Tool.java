/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2006 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M.a Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import javax.naming.ConfigurationException;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.cmdline.CommandLineUtility;
import org.clapper.util.cmdline.CommandLineException;
import org.clapper.util.cmdline.CommandLineUsageException;
import org.clapper.util.cmdline.CommandLineUserException;
import org.clapper.util.cmdline.UsageInfo;
import org.clapper.util.io.WordWrapWriter;
import org.clapper.util.logging.Logger;
import org.clapper.util.misc.BuildInfo;

/**
 * <p><i>curn</i>: Customizable Utilitarian RSS Notifier.</p>
 *
 * <p><i>curn</i> is an RSS reader. It scans a configured set of URLs, each
 * one representing an RSS feed, and summarizes the results in an
 * easy-to-read text format. <i>curn</i> keeps track of URLs it's seen
 * before, using an on-disk cache; when using the cache, it will suppress
 * displaying URLs it has already reported (though that behavior can be
 * disabled). <i>curn</i> can be extended to use any RSS parser; by
 * default, it uses the ROME parser.</p>
 *
 * <p>This class is a command-line wrapper for <i>curn</i>. Run it with
 * no parameters for a usage summary.</p>
 *
 * @version <tt>$Revision$</tt>
 */
public class Tool
    extends CommandLineUtility
    implements PostConfigPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static Collection<DateParseInfo> DATE_FORMATS; // NOPMD

    static
    {
        DATE_FORMATS = new ArrayList<DateParseInfo>();

        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh:mm:ss a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh:mm:ss", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh:mm a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh:mm", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd h:mm a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd h:mm", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd h a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd HH:mm:ss", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd HH:mm", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd H:mm", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd", false));
        DATE_FORMATS.add (new DateParseInfo ("yy/MM/dd", false));
        DATE_FORMATS.add (new DateParseInfo ("hh:mm:ss a", true));
        DATE_FORMATS.add (new DateParseInfo ("hh:mm:ss", true));
        DATE_FORMATS.add (new DateParseInfo ("hh:mm a", true));
        DATE_FORMATS.add (new DateParseInfo ("hh:mm", true));
        DATE_FORMATS.add (new DateParseInfo ("h:mm a", true));
        DATE_FORMATS.add (new DateParseInfo ("h:mm", true));
        DATE_FORMATS.add (new DateParseInfo ("hh a", true));
        DATE_FORMATS.add (new DateParseInfo ("h a", true));
        DATE_FORMATS.add (new DateParseInfo ("HH:mm:ss a", true));
        DATE_FORMATS.add (new DateParseInfo ("HH:mm:ss", true));
        DATE_FORMATS.add (new DateParseInfo ("HH:mm a", true));
        DATE_FORMATS.add (new DateParseInfo ("HH:mm", true));
        DATE_FORMATS.add (new DateParseInfo ("H:mm a", true));
        DATE_FORMATS.add (new DateParseInfo ("H:mm", true));
    };

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private String  configPath                   = null;
    private boolean useCache                     = true;
    private Date    currentTime                  = new Date();
    private boolean optShowBuildInfo             = false;
    private boolean optShowPlugIns               = false;
    private boolean optShowVersion               = false;
    private Boolean optUpdateCache               = null;
    private boolean optAbortOnUndefinedConfigVar = true;

    /**
     * For log messages
     */
    private static final Logger log = new Logger (Tool.class);

    /*----------------------------------------------------------------------*\
                               Main Program
    \*----------------------------------------------------------------------*/

    public static void main (final String[] args)
    {
        Tool tool = new Tool();

        try
        {
            tool.execute (args);
        }

        catch (CommandLineUsageException ex)
        {
            // Already reported

            System.exit (1);
        }

        catch (CommandLineUserException ex)
        {
            // Don't dump a stack trace.

            new WordWrapWriter(System.err).println(ex.getMessage());
        }

        catch (CommandLineException ex)
        {
            ex.printStackTrace (System.err);
            System.exit (1);
        }

        catch (Exception ex)
        {
            ex.printStackTrace (System.err);
            System.exit (1);
        }
    }

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private Tool()
    {
        // Cannot be instantiated directly.
    }

    /*----------------------------------------------------------------------*\
                Public Methods Required by PlugIn Interface
    \*----------------------------------------------------------------------*/

    public String getName()
    {
        return "curn command-line interface";
    }

    public String getSortKey()
    {
        return ClassUtil.getShortClassName (this.getClass());
    }

    public void runPostConfigPlugIn (final CurnConfig config)
        throws CurnException
    {
        try
        {
            adjustConfiguration (config);
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
        }
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Called by <tt>parseParams()</tt> to handle any option it doesn't
     * recognize. If the option takes any parameters, the overridden
     * method must extract the parameter by advancing the supplied
     * <tt>Iterator</tt> (which returns <tt>String</tt> objects). This
     * default method simply throws an exception.
     *
     * @param shortOption  the short option character, or
     *                     {@link UsageInfo#NO_SHORT_OPTION} if there isn't
     *                     one (i.e., if this is a long-only option).
     * @param longOption   the long option string, without any leading
     *                     "-" characters, or null if this is a short-only
     *                     option
     * @param it           the <tt>Iterator</tt> for the remainder of the
     *                     command line, for extracting parameters.
     *
     * @throws CommandLineUsageException  on error
     * @throws NoSuchElementException     overran the iterator (i.e., missing
     *                                    parameter)
     */
    protected void parseCustomOption (final char             shortOption,
                                      final String           longOption,
                                      final Iterator<String> it)
        throws CommandLineUsageException,
               NoSuchElementException
    {
        switch (shortOption)
        {
            case 'a':           // --authors
                deprecatedOption (shortOption, longOption);
                break;

            case 'A':           // --no-authors
                deprecatedOption (shortOption, longOption);
                break;

            case 'B':           // --build-info
                optShowBuildInfo = true;
                break;

            case 'C':           // --no-cache
                useCache = false;
                break;

            case 'd':           // --show-dates
                deprecatedOption (shortOption, longOption);
                break;

            case 'D':           // --no-dates
                deprecatedOption (shortOption, longOption);
                break;

            case 'p':           // --plug-ins
                optShowPlugIns = true;
                break;

            case 'r':           // --rss-version
                deprecatedOption (shortOption, longOption);
                break;

            case 'R':           // --no-rss-version
                deprecatedOption (shortOption, longOption);
                break;

            case 't':           // --time
                currentTime = parseDateTime (it.next());
                break;

            case 'T':           // --threads
                deprecatedOption (shortOption, longOption);
                break;

            case 'u':           // --no-update
                optUpdateCache = Boolean.FALSE;
                break;

            case 'U':          // --allow-undefined-cfg-vars
                optAbortOnUndefinedConfigVar = false;
                break;

            case 'v':
                optShowVersion = true;
                break;

            case 'z':           // --gzip
                deprecatedOption (shortOption, longOption);
                break;

            case 'Z':           // --no-gzip
                deprecatedOption (shortOption, longOption);
                break;

            default:
                // Should not happen.
                throw new IllegalStateException ("(BUG) Unknown option. " +
                                                 "Why am I here?");
        }
    }

    /**
     * <p>Called by <tt>parseParams()</tt> once option parsing is complete,
     * this method must handle any additional parameters on the command
     * line. It's not necessary for the method to ensure that the iterator
     * has the right number of strings left in it. If you attempt to pull
     * too many parameters from the iterator, it'll throw a
     * <tt>NoSuchElementException</tt>, which <tt>parseParams()</tt> traps
     * and converts into a suitable error message. Similarly, if there are
     * any parameters left in the iterator when this method returns,
     * <tt>parseParams()</tt> throws an exception indicating that there are
     * too many parameters on the command line.</p>
     *
     * <p>This method is called unconditionally, even if there are no
     * parameters left on the command line, so it's a useful place to do
     * post-option consistency checks, as well.</p>
     *
     * @param it   the <tt>Iterator</tt> for the remainder of the
     *             command line
     *
     * @throws CommandLineUsageException  on error
     * @throws NoSuchElementException     attempt to iterate past end of args;
     *                                    <tt>parseParams()</tt> automatically
     *                                    handles this exception, so it's
     *                                    safe for subclass implementations of
     *                                    this method not to handle it
     */
    protected void processPostOptionCommandLine (final Iterator<String> it)
        throws CommandLineUsageException,
               NoSuchElementException
    {
        // If we're showing build information or the version, forget about
        // the remainder of the command line.

        if (! (optShowBuildInfo || optShowVersion || optShowPlugIns))
            configPath = it.next();
    }

    /**
     * Called by <tt>parseParams()</tt> to get the custom command-line
     * options and parameters handled by the subclass. This list is used
     * solely to build a usage message. The overridden method must fill the
     * supplied <tt>UsageInfo</tt> object:
     *
     * <ul>
     *   <li> Each parameter must be added to the object, via the
     *        <tt>UsageInfo.addParameter()</tt> method. The first argument
     *        to <tt>addParameter()</tt> is the parameter string (e.g.,
     *        "<dbCfg>" or "input_file"). The second parameter is the
     *        one-line description. The description may be of any length,
     *        but it should be a single line.
     *
     *   <li> Each option must be added to the object, via the
     *        <tt>UsageInfo.addOption()</tt> method. The first argument to
     *        <tt>addOption()</tt> is the option string (e.g., "-x" or
     *        "-version"). The second parameter is the one-line
     *        description. The description may be of any length, but it
     *        should be a single line.
     * </ul>
     *
     * That information will be combined with the common options supported
     * by the base class, and used to build a usage message.
     *
     * @param info   The <tt>UsageInfo</tt> object to fill.
     */
    protected void getCustomUsageInfo (final UsageInfo info)
    {
        info.setCommandName ("curn");

        // Note: A null explanation denotes a "hidden" option not shown in
        // the usage output. Here, those options are deprecated, but
        // retained for backward compatibility.

        info.addOption('a', "show-authors", null);
        info.addOption('A', "no-authors", null);
        info.addOption('B', "build-info",
                       "Show full build information, then exit. " +
                       "This option shows a bit more information than the " +
                       UsageInfo.LONG_OPTION_PREFIX +
                       "version option. This option can be combined with " +
                       "the " +
                       UsageInfo.LONG_OPTION_PREFIX +
                       "plug-ins option to show the loaded plug-ins.");
        info.addOption('C', "no-cache", "Don't use a cache file at all.");
        info.addOption('d', "show-dates", null);
        info.addOption('D', "no-dates", null);
        info.addOption('p', "plug-ins",
                       "Show the list of located plug-ins and output " +
                       "handlers, then exit. This option can be combined " +
                       "with either " +
                       UsageInfo.LONG_OPTION_PREFIX +
                       "build-info or " +
                       UsageInfo.LONG_OPTION_PREFIX +
                       "version to show version information, as well.");
        info.addOption('r', "rss-version", null);
        info.addOption('R', "no-rss-version", null);
        info.addOption('T', "threads", "<n>", null);
        info.addOption('u', "no-update",
                       "Read the cache, but don't update it.");
        info.addOption('U', "allow-undefined-cfg-vars",
                       "Don't abort when an undefined variable is " +
                       "encountered in the configuration file; substitute " +
                       "an empty string, instead. Normally, an undefined " +
                       "configuration variable will cause curn to abort.");
        info.addOption('v', "version",
                      "Show version information, then exit. " +
                       "This option can be combined with the " +
                       UsageInfo.LONG_OPTION_PREFIX +
                       "plug-ins option to show the loaded plug-ins.");
        info.addOption('z', "gzip", null);
        info.addOption('Z', "no-gzip", null);

        StringWriter      sw  = new StringWriter();
        PrintWriter       pw  = new PrintWriter(sw);
        Date              sampleDate;
        BuildInfo         buildInfo = Version.getBuildInfo();
        SimpleDateFormat  dateFormat;
        String            dateString = buildInfo.getBuildDate();

        try
        {
            dateFormat = new SimpleDateFormat(BuildInfo.DATE_FORMAT_STRING);
            sampleDate = dateFormat.parse(dateString);
        }

        catch (Exception ex)
        {
            log.error("Can't parse build date string \"" + dateString +
                      "\" using format \"" + BuildInfo.DATE_FORMAT_STRING +
                      "\"",
                      ex);
            sampleDate = new Date();
        }

        Set<String> printed = new HashSet<String>();
        for (DateParseInfo dpi : DATE_FORMATS)
        {
            String s = dpi.formatDate(sampleDate);
            if (! printed.contains(s))
            {
                pw.println();
                pw.print(s);
                printed.add(s);
            }
        }

        info.addOption ('t', "time", "<time>",
                        "For the purposes of cache expiration, pretend the " +
                        "current time is <time>. <time> may be in one of " +
                        "the following formats." +
                        sw.toString());

        info.addParameter ("config",
                           "Path to configuration file",
                           true);
    }

    /**
     * Run the curn tool. This method parses the command line arguments,
     * storing the results in an internal configuration; then, it
     * instantiates a <tt>Curn</tt> object and calls its <tt>run()</tt>
     * method.
     *
     * @throws CommandLineException error occurred
     */
    protected void runCommand()
        throws CommandLineException
    {
        try
        {
            boolean abort = false;

            if (optShowBuildInfo)
            {
                abort = true;
                Version.showBuildInfo();
            }

            else if (optShowVersion)
            {
                abort = true;
                Version.showVersion();
            }

            if (optShowPlugIns)
            {
                showPlugIns();
                abort = true;
            }

            if (! abort)
            {
                // Allocate Curn object, which loads plug-ins.

                Curn curn = CurnFactory.newCurn();

                // Add this object as a plug-in.

                MetaPlugIn.getMetaPlugIn().addPlugIn (this);

                // Fire it up.

                curn.setCurrentTime (currentTime);
                curn.setAbortOnUndefinedConfigVariable
                    (optAbortOnUndefinedConfigVar);
                curn.run (configPath, this.useCache);
            }
        }

        catch (CurnUsageException ex)
        {
            throw new CommandLineUserException(ex);
        }

        catch (CurnException ex)
        {
            throw new CommandLineException (ex);
        }

        catch (Exception ex)
        {
            ex.printStackTrace (System.err);
            throw new CommandLineException (ex);
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void adjustConfiguration (final CurnConfig config)
        throws ConfigurationException
    {
        log.debug ("adjustConfiguration() called.");

        // Adjust the configuration, if necessary, based on the command-line
        // parameters.

        if (optUpdateCache != null)
            config.setMustUpdateCacheFlag (optUpdateCache.booleanValue());
    }

    private Date parseDateTime (final String s)
        throws CommandLineUsageException
    {
        Date date = null;

        for (DateParseInfo dpi : DATE_FORMATS)
        {
            try
            {
                date = dpi.format.parse (s);
                if (date != null)
                {
                    if (dpi.timeOnly)
                    {
                        // The date pattern specified only a time, which
                        // means the date part defaulted to the epoch. Make
                        // it today, instead.

                        Calendar cal = Calendar.getInstance();
                        Calendar calNow = Calendar.getInstance();

                        calNow.setTime (new Date());
                        cal.setTime (date);
                        cal.set (calNow.get (Calendar.YEAR),
                                 calNow.get (Calendar.MONTH),
                                 calNow.get (Calendar.DAY_OF_MONTH));
                        date = cal.getTime();
                    }

                    break;
                }
            }

            catch (ParseException ex)
            {
            }
        }

        if (date == null)
        {
            throw new CommandLineUsageException (Constants.BUNDLE_NAME,
                                                 "Tool.badDateTime",
                                                 "Bad date/time: \"{0}\"",
                                                 new Object[] {s});
        }

        return date;
    }

    private void showPlugIns()
        throws CurnException
    {
        Map<String,Class> plugIns = new TreeMap<String,Class>();

        PlugInManager.listPlugIns (plugIns);

        System.out.println();
        System.out.println ("Plug-ins:");
        System.out.println();
        for (String name : plugIns.keySet())
        {
            System.out.println (name + " (" + plugIns.get (name).getName() +
                                ")");
        }
    }

    private void deprecatedOption (final char   shortOption,
                                   final String longOption)
    {
        CurnUtil.getErrorOut().println ("WARNING: Ignoring deprecated " +
                                    UsageInfo.SHORT_OPTION_PREFIX +
                                    shortOption +
                                    " (" +
                                    UsageInfo.LONG_OPTION_PREFIX +
                                    longOption +
                                    ") command-line option.");
    }
}
