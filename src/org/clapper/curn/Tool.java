/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms are permitted provided
  that: (1) source distributions retain this entire copyright notice and
  comment; and (2) modifications made to the software are prominently
  mentioned, and a copy of the original software (or a pointer to its
  location) are included. The name of the author may not be used to endorse
  or promote products derived from this software without specific prior
  written permission.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
  WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

  Effectively, this means you can do what you want with the software except
  remove this notice or take advantage of the author's name. If you modify
  the software and redistribute your modified version, you must indicate that
  your version is a modification of the original, and you must provide either
  a pointer to or a copy of the original.
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.io.IOException;
import java.io.File;
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
import java.util.NoSuchElementException;
import java.util.Set;

import org.clapper.curn.parser.RSSChannel;

import org.clapper.util.cmdline.CommandLineUtility;
import org.clapper.util.cmdline.CommandLineException;
import org.clapper.util.cmdline.CommandLineUsageException;
import org.clapper.util.cmdline.UsageInfo;
import org.clapper.util.config.ConfigurationException;
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
 * disabled). <i>curn</i> can be extended to use any RSS parser; its
 * built-in RSS parser, the
 * {@link org.clapper.curn.parser.minirss.MiniRSSParser MiniRSSParser}
 * class, can handle files in
 * {@link <a href="http://www.atomenabled.org/developers/">Atom</a>}
 * format (0.3) and RSS formats
 * {@link <a target="_top" href="http://backend.userland.com/rss091">0.91</a>},
 * 0.92,
 * {@link <a target="_top" href="http://web.resource.org/rss/1.0/">1.0</a>} and
 * {@link <a target="_top" href="http://blogs.law.harvard.edu/tech/rss">2.0</a>}.</p>
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

    private static Collection<DateParseInfo> DATE_FORMATS;

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

    private String              configPath       = null;
    private boolean             useCache         = true;
    private Date                currentTime      = new Date();
    private Collection<String>  emailAddresses   = new ArrayList<String>();
    private boolean             optShowBuildInfo = false;
    private boolean             optShowVersion   = false;
    private Boolean             optShowDates     = null;
    private Boolean             optShowAuthors   = null;
    private Boolean             optRSSVersion    = null;
    private Boolean             optUpdateCache   = null;
    private boolean             useGzip          = true;
    private int                 maxThreads       = 0;

    /**
     * For log messages
     */
    private static Logger log = new Logger (Tool.class);

    /*----------------------------------------------------------------------*\
                               Main Program
    \*----------------------------------------------------------------------*/

    public static void main (String[] args)
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

        catch (CommandLineException ex)
        {
            WordWrapWriter err = new WordWrapWriter (System.err);

            //err.println (ex.getMessage());
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
    }

    /*----------------------------------------------------------------------*\
                Public Methods Required by PlugIn Interface
    \*----------------------------------------------------------------------*/

    public String getName()
    {
        return "curn command-line interface";
    }

    public void runPostConfigurationPlugIn (CurnConfig config)
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
    protected void parseCustomOption (char             shortOption,
                                      String           longOption,
                                      Iterator<String> it)
        throws CommandLineUsageException,
               NoSuchElementException
    {
        switch (shortOption)
        {
            case 'a':           // --authors
                optShowAuthors = Boolean.TRUE;
                break;

            case 'A':           // --no-authors
                optShowAuthors = Boolean.FALSE;
                break;

            case 'B':           // --build-info
                optShowBuildInfo = true;
                break;

            case 'C':           // --no-cache
                useCache = false;
                break;

            case 'd':           // --show-dates
                optShowDates = Boolean.TRUE;
                break;

            case 'D':           // --no-dates
                optShowDates = Boolean.FALSE;
                break;

            case 'r':           // --rss-version
                optRSSVersion = Boolean.TRUE;
                break;

            case 'R':           // --no-rss-version
                optRSSVersion = Boolean.FALSE;
                break;

            case 't':           // --time
                currentTime = parseDateTime (it.next());
                break;

            case 'T':           // --threads
                String arg = it.next();

                try
                {
                    maxThreads = Integer.parseInt (arg);
                    if (maxThreads < 1)
                    {
                        throw new CommandLineUsageException
                            (Constants.BUNDLE_NAME, "Tool.negNumericOption",
                             "Value for \"{0}\" (\"{1}\") option must be "
                           + "greater than 0",
                             new Object[] {"-t", "--threads"});
                    }
                }

                catch (NumberFormatException ex)
                {
                    throw new CommandLineUsageException
                        (Constants.BUNDLE_NAME, "Tool.badNumericOption",
                         "Bad numeric value \"{0}\" for \"{1}\" (\"{2}\") "
                       + "option",
                         new Object[]
                         {
                             arg,
                             "-T",
                             "--threads"
                         });
                }
                break;

            case 'u':           // --no-update
                optUpdateCache = Boolean.FALSE;
                break;

            case 'v':
                optShowVersion = true;
                break;

            case 'z':           // --gzip
                useGzip = true;
                break;

            case 'Z':           // --no-gzip
                useGzip = false;
                break;

            default:
                // Should not happen.
                throw new IllegalStateException ("(BUG) Unknown option. "
                                               + "Why am I here?");
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
    protected void processPostOptionCommandLine (Iterator<String> it)
        throws CommandLineUsageException,
               NoSuchElementException
    {
        // If we're showing build information or the version, forget about
        // the remainder of the command line.

        if (! (optShowBuildInfo || optShowVersion))
        {
            configPath = it.next();

            while (it.hasNext())
                emailAddresses.add (it.next());
        }
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
    protected void getCustomUsageInfo (UsageInfo info)
    {
        info.addOption ('a', "show-authors",
                        "Show the authors for each item, if available.");
        info.addOption ('A', "no-authors",
                        "Don't the authors for each item, if available.");
        info.addOption ('B', "build-info",
                        "Show full build information, then exit. "
                      + "This option shows a bit more information than the "
                      + UsageInfo.LONG_OPTION_PREFIX
                      + "version option");
        info.addOption ('C', "no-cache", "Don't use a cache file at all.");
        info.addOption ('d', "show-dates",
                        "Show dates on feeds and feed items, if available.");
        info.addOption ('D', "no-dates",
                        "Don't show dates on feeds and feed items.");
        info.addOption ('r', "rss-version",
                        "Show the RSS version each site uses.");
        info.addOption ('R', "no-rss-version",
                        "Don't show the RSS version each site uses.");
        info.addOption ('T', "threads", "<n>",
                        "Set the number of concurrent download threads to "
                      + "<n>. <n> must be greater than 0.");
        info.addOption ('u', "no-update",
                        "Read the cache, but don't update it.");
        info.addOption ('v', "version",
                        "Show version information, then exit.");
        info.addOption ('z', "gzip",
                        "Ask remote HTTP servers to gzip content before "
                      + "sending it.");
        info.addOption ('Z', "no-gzip",
                        "Don't ask remote HTTP servers to gzip content before "
                      + "sending it.");

        StringWriter      sw  = new StringWriter();
        PrintWriter       pw  = new PrintWriter (sw);
        Date              sampleDate;
        BuildInfo         buildInfo = Version.getBuildInfo(); 
        SimpleDateFormat  dateFormat;
        String            dateString = buildInfo.getBuildDate();

        try
        {
            dateFormat = new SimpleDateFormat (BuildInfo.DATE_FORMAT_STRING);
            sampleDate = dateFormat.parse (dateString);
        }

        catch (Exception ex)
        {
            log.error ("Can't parse build date string \""
                     + dateString
                     + "\" using format \""
                     + BuildInfo.DATE_FORMAT_STRING
                     + "\"",
                       ex);
            sampleDate = new Date();
        }

        Set<String> printed = new HashSet<String>();
        for (DateParseInfo dpi : DATE_FORMATS)
        {
            String s = dpi.formatDate (sampleDate);
            if (! printed.contains (s))
            {
                pw.println();
                pw.print (s);
                printed.add (s);
            }
        }

        info.addOption ('t', "time", "<time>",
                        "For the purposes of cache expiration, pretend the "
                      + "current time is <time>. <time> may be in one of the "
                      + "following formats."
                      + sw.toString());

        info.addParameter ("config",
                           "Path to configuration file",
                           true);
        info.addParameter ("emailAddress ...",
                           "One or more email addresses to receive the output",
                           false);
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
            if (optShowBuildInfo)
                Version.showBuildInfo();

            else if (optShowVersion)
                Version.showVersion();

            else
            {
                // Allocate Curn object, which loads plug-ins.

                Curn curn = CurnFactory.newCurn();

                // Add this object as a plug-in.

                MetaPlugIn.getMetaPlugIn().addPlugIn (this);

                // Fire it up.

                curn.setCurrentTime (currentTime);
                curn.run (configPath, this.emailAddresses, this.useCache);
            }
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

    private void adjustConfiguration (CurnConfig config)
        throws ConfigurationException
    {
        log.debug ("adjustConfiguration() called.");

        // Adjust the configuration, if necessary, based on the command-line
        // parameters.

        if (optShowAuthors != null)
            config.setShowAuthorsFlag (optShowAuthors.booleanValue());

        if (optRSSVersion != null)
            config.setShowRSSVersionFlag (optRSSVersion.booleanValue());

        if (optUpdateCache != null)
            config.setMustUpdateCacheFlag (optUpdateCache.booleanValue());

        if (optShowDates != null)
            config.setShowDatesFlag (true);

        if (maxThreads > 0)
            config.setMaxThreads (maxThreads);

        config.setRetrieveFeedsWithGzipFlag (useGzip);
    }

    private Date parseDateTime (String s)
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
}
