/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004 Brian M. Clapper. All rights reserved.

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
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.HashMap;

import java.text.ParseException;
import java.text.MessageFormat;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import org.clapper.curn.parser.RSSParserFactory;
import org.clapper.curn.parser.RSSParser;
import org.clapper.curn.parser.RSSParserException;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.io.FileUtils;
import org.clapper.util.misc.Logger;
import org.clapper.util.text.TextUtils;
import org.clapper.util.text.XStringBuffer;
import org.clapper.util.config.ConfigurationException;

import org.clapper.util.cmdline.CommandLineUtility;
import org.clapper.util.cmdline.CommandLineException;
import org.clapper.util.cmdline.CommandLineUsageException;
import org.clapper.util.cmdline.UsageInfo;

import org.apache.oro.text.perl.Perl5Util;

/**
 * <p><i>curn</i>: Curiously Uncomplicated RSS Notifier.</p>
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
 * @version <tt>$Revision$</tt>
 */
public class curn extends CommandLineUtility
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static Collection DATE_FORMATS;

    static
    {
        DATE_FORMATS = new ArrayList();

        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh:mm:ss a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh:mm:ss", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh:mm a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh:mm", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd h:mm a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd h:mm", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd hh", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd h a", false));
        DATE_FORMATS.add (new DateParseInfo ("yyyy/MM/dd h", false));
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
    };

    private static final String EMAIL_HANDLER_CLASS =
                            "org.clapper.curn.email.EmailOutputHandlerImpl";

    private static final String BUNDLE_NAME = "org.clapper.curn.curn";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    // Used to associate a parsed channel with its FeedInfo data

    class ChannelFeedInfo
    {
        FeedInfo    feedInfo;
        RSSChannel  channel;

        ChannelFeedInfo (FeedInfo feedInfo, RSSChannel channel)
        {
            this.feedInfo = feedInfo;
            this.channel  = channel;
        }
    }

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private String          configPath       = null;
    private ConfigFile      config           = null;
    private boolean         useCache         = true;
    private FeedCache       cache            = null;
    private Date            currentTime      = new Date();
    private Collection      outputHandlers   = new ArrayList();
    private Collection      emailAddresses   = new ArrayList();
    private boolean         showBuildInfo    = false;
    private Perl5Util       perl5Util        = new Perl5Util();
    private ResourceBundle  bundle           = null;
    private Boolean         optShowDates     = null;
    private Boolean         optShowAuthors   = null;
    private Boolean         optQuiet         = null;
    private Boolean         optRSSVersion    = null;
    private Boolean         optUpdateCache   = null;

    /**
     * For log messages
     */
    private static Logger log = new Logger (curn.class);

    /*----------------------------------------------------------------------*\
                               Main Program
    \*----------------------------------------------------------------------*/

    public static void main (String[] args) throws Exception
    {
        curn tool = new curn();

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
            System.err.println (ex.getMessage());
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

    private curn()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Read the RSS feeds specified in a parsed configuration, writing them
     * to the output handler(s) specified in the configuration.
     *
     * @param configuration   the parsed configuration
     * @param emailAddresses  a collection of (string) email addresses to
     *                        receive the output, or null (or empty collection)
     *                        for none.
     *
     * @throws IOException              unable to open or read a required file
     * @throws ConfigurationException   error in configuration file
     * @throws RSSParserException       error parsing XML feed(s)
     * @throws CurnException          any other error
     */
    public void processRSSFeeds (ConfigFile configuration,
                                 Collection emailAddresses)
        throws IOException,
               ConfigurationException,
               RSSParserException,
               CurnException
    {
        Iterator            it;
        String              parserClassName;
        RSSParser           parser;
        Collection          channels;
        OutputStreamWriter  out;

        loadOutputHandlers (configuration, emailAddresses);

        if (useCache)
        {
            cache = new FeedCache (configuration);
            cache.setCurrentTime (currentTime);
            cache.loadCache();
        }

        // Parse the RSS feeds

        parserClassName = configuration.getRSSParserClassName();
        log.info ("Getting parser \"" + parserClassName + "\"");
        parser = RSSParserFactory.getRSSParser (parserClassName);

        channels = new ArrayList();

        Collection feeds = configuration.getFeeds();
        if (feeds.size() == 0)
            throw new ConfigurationException ("No configured RSS feed URLs.");

        for (it = configuration.getFeeds().iterator(); it.hasNext(); )
        {
            FeedInfo feedInfo = (FeedInfo) it.next();
            if (! feedInfo.feedIsEnabled())
            {
                log.info ("Skipping disabled feed: " + feedInfo.getURL());
            }

            else
            {
                RSSChannel  channel  = processFeed (feedInfo,
                                                    parser,
                                                    configuration);
                if (channel != null)
                    channels.add (new ChannelFeedInfo (feedInfo, channel));
            }
        }

        displayChannels (channels);

        if ((cache != null) && configuration.mustUpdateCache())
            cache.saveCache();
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
     * @param option   the option, including the leading '-'
     * @param it       the <tt>Iterator</tt> for the remainder of the
     *                 command line
     *
     * @throws CommandLineUsageException  on error
     * @throws NoSuchElementException     overran the iterator (i.e., missing
     *                                    parameter) 
     */
    protected void parseCustomOption (String option, Iterator it)
        throws CommandLineUsageException,
               NoSuchElementException
    {
        if (option.equals ("-a") || option.equals ("--show-authors"))
            optShowAuthors = Boolean.TRUE;

        else if (option.equals ("-A") || option.equals ("--no-authors"))
            optShowAuthors = Boolean.FALSE;

        else if (option.equals ("-B") || option.equals ("--build-info"))
            showBuildInfo = true;

        else if (option.equals ("-C") || option.equals ("--no-cache"))
            useCache = false;

        else if (option.equals ("-d") || option.equals ("--show-dates"))
            optShowDates = Boolean.TRUE;

        else if (option.equals ("-D") || option.equals ("--no-dates"))
            optShowDates = Boolean.FALSE;

        else if (option.equals ("-Q") || option.equals ("--no-quiet"))
            optQuiet = Boolean.FALSE;

        else if (option.equals ("-q") || option.equals ("--quiet"))
            optQuiet = Boolean.TRUE;

        else if (option.equals ("-r") || option.equals ("--rss-version"))
            optRSSVersion = Boolean.TRUE;

        else if (option.equals ("-R") || option.equals ("--no-rss-version"))
            optRSSVersion = Boolean.FALSE;

        else if (option.equals ("-t") || option.equals ("--time"))
            currentTime = parseDateTime ((String) it.next());

        else if (option.equals ("-u") || option.equals ("--no-update"))
            optUpdateCache = Boolean.FALSE;

        else
            throw new CommandLineUsageException ("Unknown option: " + option);
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
    protected void processPostOptionCommandLine (Iterator it)
        throws CommandLineUsageException,
               NoSuchElementException
    {
        configPath = (String) it.next();

        while (it.hasNext())
            emailAddresses.add ((String) it.next());
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
        info.addOption ("-a, --show-authors",
                        "Show the authors for each item, if available.");
        info.addOption ("-A, --show-authors",
                        "Don't the authors for each item, if available.");
        info.addOption ("-B, --build-info",
                        "Show full build information.");
        info.addOption ("-C, --no-cache", "Don't use a cache file at all.");
        info.addOption ("-d, --show-dates",
                        "Show dates on feeds and feed items, if available.");
        info.addOption ("-D, --no-dates",
                        "Don't show dates on feeds and feed items.");
        info.addOption ("-Q, --no-quiet",
                        "Emit messages about sites with no new items.");
        info.addOption ("-q, --quiet",
                        "Be quiet about sites with no new items.");
        info.addOption ("-r, --rss-version",
                        "Show the RSS version each site uses.");
        info.addOption ("-R, --no-rss-version",
                        "Don't show the RSS version each site uses.");
        info.addOption ("-u, --no-update",
                        "Read the cache, but don't update it.");

        StringWriter sw  = new StringWriter();
        PrintWriter  pw  = new PrintWriter (sw);
        Date         now = new Date();

        for (Iterator it = DATE_FORMATS.iterator(); it.hasNext(); )
        {
            pw.println();
            DateParseInfo dpi = (DateParseInfo) it.next();
            pw.print ("    " + dpi.formatDate (now));
        }

        info.addOption ("-t <time>, --time <time>",
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
     * Run the curn tool. Parses the command line arguments, storing the
     * results in an internal configuration; then, calls the
     * {@link #processRSSFeeds} method.
     *
     * @param args  the command-line parameters
     *
     * @throws CommandLineException error occurred
     */
    protected void runCommand()
        throws CommandLineException
    {
        try
        {
            runCurn();
        }

        catch (ConfigurationException ex)
        {
            throw new CommandLineException (ex);
        }

        catch (IOException ex)
        {
            throw new CommandLineException (ex);
        }

        catch (CurnException ex)
        {
            throw new CommandLineException (ex);
        }

        catch (RSSParserException ex)
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

    private void runCurn()
        throws CurnException,
               IOException,
               ConfigurationException,
               RSSParserException,
               CommandLineException
        
    {
        loadConfig();

        bundle = ResourceBundle.getBundle (BUNDLE_NAME);
        if (bundle == null)
        {
            throw new CurnException ("Can't locate resource bundle "
                                   + BUNDLE_NAME);
        }

        if (showBuildInfo)
            Version.showBuildInfo();

        processRSSFeeds (this.config, this.emailAddresses);
    }

    private void loadConfig()
        throws ConfigurationException,
               IOException
    {
        config = new ConfigFile (configPath);

        // Adjust the configuration, if necessary, based on the command-line
        // parameters.

        if (optShowAuthors != null)
            config.setShowAuthorsFlag (optShowAuthors.booleanValue());

        if (optQuiet != null)
            config.setQuietFlag (optQuiet.booleanValue());

        if (optRSSVersion != null)
            config.setShowRSSVersionFlag (optRSSVersion.booleanValue());

        if (optUpdateCache != null)
            config.setMustUpdateCacheFlag (optUpdateCache.booleanValue());

        if (optShowDates != null)
            config.setShowDatesFlag (true);
    }

    private Date parseDateTime (String s)
        throws CommandLineUsageException
    {
        Date date = null;

        for (Iterator it = DATE_FORMATS.iterator(); it.hasNext(); )
        {
            try
            {
                DateParseInfo dpi = (DateParseInfo) it.next();
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
            throw new CommandLineUsageException ("Bad date/time: \""
                                               + s
                                               + "\"");
        }

        return date;
    }

    private void loadOutputHandlers (ConfigFile configuration,
                                     Collection          emailAddresses)
        throws ConfigurationException,
               CurnException
    {
        String         className;
        OutputHandler  handler;
        Iterator       it;

        for (it = configuration.getOutputHandlerClassNames().iterator();
             it.hasNext(); )
        {
            className = (String) it.next();
            handler   = OutputHandlerFactory.getOutputHandler (className);
            outputHandlers.add (handler);
        }

        // If there were no output handlers, then just use a default
        // TextOutputHandler.

        if (outputHandlers.size() == 0)
        {
            log.info ("No configured output handlers. Installing default.");
            handler = OutputHandlerFactory.getOutputHandler
                                                   (TextOutputHandler.class);
            outputHandlers.add (handler);
        }

        // If there are email addresses, then attempt to load that handler,
        // and wrap the other output handlers inside it.

        if ((emailAddresses != null) && (emailAddresses.size() > 0))
        {
            EmailOutputHandler emailHandler;

            emailHandler = (EmailOutputHandler)
                             OutputHandlerFactory.getOutputHandler
                                          (EMAIL_HANDLER_CLASS);

            // Place all the other handlers inside the EmailOutputHandler

            for (it = outputHandlers.iterator(); it.hasNext(); )
                emailHandler.addOutputHandler ((OutputHandler) it.next());

            // Add the email addresses to the handler

            for (it = emailAddresses.iterator(); it.hasNext(); )
                emailHandler.addRecipient ((String) it.next());

            // Clear the existing set of output handlers and replace it
            // with the email handler.

            outputHandlers.clear();
            outputHandlers.add (emailHandler);
        }
    }

    private RSSChannel processFeed (FeedInfo   feedInfo,
                                    RSSParser  parser,
                                    ConfigFile configuration)
        throws RSSParserException
    {
        URL         feedURL = feedInfo.getURL();
        RSSChannel  channel = null;

        try
        {
            String feedURLString = feedURL.toString();
            log.info ("Parsing feed at " + feedURLString);

            // Don't download the channel if it hasn't been modified since
            // we last checked it.

            URLConnection conn = feedURL.openConnection();
            if (feedHasChanged (conn, feedInfo))
            {
                if (cache != null)
                    cache.addToCache (feedInfo.getCacheKey(),
                                      feedURL,
                                      feedInfo);

                File saveAsFile = feedInfo.getSaveAsFile();
                InputStream is = conn.getInputStream();

                if (saveAsFile != null)
                {
                    // Copy the contents of the feed to the file first, then
                    // parse the downloaded file, instead.

                    downloadFeed (is, feedURLString, saveAsFile);
                    log.debug ("Reopening \"" + saveAsFile.getPath() + "\".");
                    is = new FileInputStream (saveAsFile);
                }

                channel = parser.parseRSSFeed (is);
                processChannelItems (channel, feedInfo);
                if (channel.getItems().size() == 0)
                    channel = null;
            }
        }

        catch (MalformedURLException ex)
        {
            log.error ("", ex);
        }

        catch (RSSParserException ex)
        {
            log.error ("RSS parse exception: ", ex); 
        }

        catch (IOException ex)
        {
            log.error ("", ex);
        }

        return channel;
    }

    private void downloadFeed (InputStream urlStream,
                               String      feedURL,
                               File        saveAsFile)
        throws IOException
    {
        File temp = File.createTempFile ("curn", "xml", null);
        temp.deleteOnExit();

        try
        {
            log.debug ("Downloading \""
                     + feedURL
                     + "\" to temporary file \""
                     + temp.getPath());
            OutputStream os = new FileOutputStream (temp);
            FileUtils.copyStream (urlStream, os);
            os.close();

            log.debug ("Copying temporary file \""
                     + temp.getPath()
                     + "\" to \""
                     + saveAsFile.getPath()
                     + "\"");
            FileUtils.copyFile (temp, saveAsFile);
        }

        finally
        {
            temp.delete();
        }
    }

    private boolean feedHasChanged (URLConnection conn, FeedInfo feedInfo)
        throws IOException
    {
        long     lastSeen = 0;
        long     lastModified = 0;
        boolean  hasChanged = false;
        String   cacheKey = feedInfo.getCacheKey();
        URL      feedURL = feedInfo.getURL();

        if ((cache != null) && (cache.contains (cacheKey)))
        {
            FeedCacheEntry entry = (FeedCacheEntry) cache.getItem (cacheKey);
            lastSeen = entry.getTimestamp();
        }

        if (lastSeen == 0)
        {
            log.debug ("Feed \""
                     + feedURL.toString()
                     + "\" has no recorded last-seen time.");
            hasChanged = true;
        }

        else if ((lastModified = conn.getLastModified()) == 0)
        {
            log.debug ("Feed \""
                     + feedURL.toString()
                     + "\" provides no last-modified time.");
            hasChanged = true;
        }

        else if (lastSeen >= lastModified)
        {
            log.debug ("Feed \""
                     + feedURL.toString()
                     + "\" has Last-Modified time of "
                     + new Date (lastModified).toString()
                     + ", which is not newer than last-seen time of "
                     + new Date (lastSeen).toString()
                     + ". Feed has no new data.");
        }

        else
        {
            log.debug ("Feed \""
                     + feedURL.toString()
                     + "\" has Last-Modified time of "
                     + new Date (lastModified).toString()
                     + ", which is newer than last-seen time of "
                     + new Date (lastSeen).toString()
                     + ". Feed might have new data.");
            hasChanged = true;
        }

        return hasChanged;
    }

    private void processChannelItems (RSSChannel  channel,
                                      FeedInfo    feedInfo)
        throws RSSParserException,
               MalformedURLException
    {
        Collection  items;
        Iterator    it;
        String      titleOverride = feedInfo.getTitleOverride();
        boolean     pruneURLs = feedInfo.pruneURLs();
        String      editCmd = feedInfo.getItemURLEditCommand();
        String      channelName = channel.getLink().toString();

        if (titleOverride != null)
            channel.setTitle (titleOverride);

        if (editCmd != null)
        {
            log.debug ("Channel \""
                     + channelName
                     + "\": Edit command is: "
                     + editCmd);
        }

        items = channel.getItems();

        // First, weed out the ones we don't care about.

        log.info ("Channel \""
                + channelName
                + "\": "
                + String.valueOf (items.size())
                + " total items");
        for (it = items.iterator(); it.hasNext(); )
        {
            RSSItem item = (RSSItem) it.next();
            URL itemURL = item.getLink();

            if (itemURL == null)
            {
                log.debug ("Skipping item with null URL.");
                it.remove();
                continue;
            }

            if (pruneURLs || (editCmd != null))
            {
                // Prune the URL of its parameters, if configured for this
                // site. This must be done before checking the cache, because
                // the pruned URLs are what end up in the cache.

                String sURL = itemURL.toExternalForm();

                if (pruneURLs)
                {
                    int i = sURL.indexOf ("?");

                    if (i != -1)
                        sURL = sURL.substring (0, i);
                }

                if (editCmd != null)
                    sURL = perl5Util.substitute (editCmd, sURL);

                itemURL = new URL (sURL);
            }

            // Normalize the URL and save it.

            item.setLink (Util.normalizeURL (itemURL));

            // Skip it if it's cached.

            log.debug ("Item link: " + itemURL);
            log.debug ("Item ID: " + item.getUniqueID());
            log.debug ("Item key: " + item.getCacheKey());
            if ((cache != null) && cache.contains (item.getCacheKey()))
            {
                log.debug ("Skipping cached URL: " + itemURL.toString());
                it.remove();
                continue;
            }
        }

        // Now, change the channel's items to the ones that are left.

        channel.setItems (items);

        // Finally, add all the items to the cache.

        if (items.size() > 0)
        {
            for (it = items.iterator(); it.hasNext(); )
            {
                RSSItem item = (RSSItem) it.next();

                log.debug ("Cacheing URL: " + item.getLink().toString());
                if (cache != null)
                {
                    cache.addToCache (item.getCacheKey(),
                                      item.getLink(),
                                      feedInfo);
                }
            }
        }
    }

    private void displayChannels (Collection channels)
        throws CurnException,
               ConfigurationException
    {
        OutputHandler firstOutput = null;

        // Dump the output to each output handler

        for (Iterator itHandler = outputHandlers.iterator();
             itHandler.hasNext(); )
        {
            OutputHandler handler;

            handler = (OutputHandler) itHandler.next();

            log.info ("Initializing output handler "
                    + handler.getClass().getName());
            handler.init (config);

            for (Iterator itChannel = channels.iterator();
                 itChannel.hasNext(); )
            {
                ChannelFeedInfo cfi = (ChannelFeedInfo) itChannel.next();
                handler.displayChannel (cfi.channel, cfi.feedInfo);
            }

            handler.flush();

            if ((firstOutput == null) && (handler.hasGeneratedOutput()))
                firstOutput = handler;
        }

        // If we're not emailing the output, then dump the output from the
        // first handler to the screen.

        if (emailAddresses.size() == 0)
        {
            if (firstOutput == null)
                log.info ("None of the output handlers produced output.");

            else
            {
                InputStream output = firstOutput.getGeneratedOutput();

                try
                {
                    FileUtils.copyStream (output, System.out);
                    output.close();
                }

                catch (IOException ex)
                {
                    throw new CurnException ("Failed to copy output from "
                                           + "handler "
                                           + firstOutput.getClass().getName()
                                           + " to standard output.",
                                             ex);
                }
            }
        }
    }
}
