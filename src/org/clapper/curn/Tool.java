/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Calendar;

import java.text.ParseException;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import org.clapper.rssget.parser.RSSParserFactory;
import org.clapper.rssget.parser.RSSParser;
import org.clapper.rssget.parser.RSSParserException;
import org.clapper.rssget.parser.RSSChannel;
import org.clapper.rssget.parser.RSSItem;

import org.clapper.util.misc.BadCommandLineException;
import org.clapper.util.text.TextUtils;
import org.clapper.util.config.ConfigurationException;

import org.apache.oro.text.perl.Perl5Util;

/**
* <p>rssget - scan RSS feeds and display a summary, using one or more
* configured output handlers, optionally emailing the results.</p>
*
* <p>This program scans a configured set of URLs, each one representing an
* RSS feed, and summarizes the results in an easy-to-read text format.
* <i>rssget</i> keeps track of URLs it's seen before, using an on-disk
* cache; when using the cache, it will suppress displaying URLs it has
* already reported (though that behavior can be disabled).</p>
*
* @version <tt>$Revision$</tt>
*/
public class rssget implements VerboseMessagesHandler
{
    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

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
        DATE_FORMATS.add (new DateParseInfo ("hh", true));
        DATE_FORMATS.add (new DateParseInfo ("h a", true));
        DATE_FORMATS.add (new DateParseInfo ("h", true));
    };

    private static final String EMAIL_HANDLER_CLASS =
                            "org.clapper.rssget.email.EmailOutputHandlerImpl";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    // Used to associate a parsed channel with its RSSFeedInfo data

    class ChannelFeedInfo
    {
        RSSFeedInfo feedInfo;
        RSSChannel  channel;

        ChannelFeedInfo (RSSFeedInfo feedInfo, RSSChannel channel)
        {
            this.feedInfo = feedInfo;
            this.channel  = channel;
        }
    }

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private RSSGetConfiguration config         = null;
    private boolean             useCache       = true;
    private RSSGetCache         cache          = null;
    private Date                currentTime    = new Date();
    private Collection          outputHandlers = new ArrayList();
    private Collection          emailAddresses = new ArrayList();
    private boolean             showBuildInfo  = false;
    private Perl5Util           perl5Util      = new Perl5Util();

    /*----------------------------------------------------------------------*\
                               Main Program
    \*----------------------------------------------------------------------*/

    public static void main (String[] args) throws Exception
    {
        rssget tool = new rssget();

        try
        {
            tool.runProgram (args);
        }

        catch (ConfigurationException ex)
        {
            System.err.println (ex.getMessages());
            ex.printStackTrace();
            System.exit (1);
        }

        catch (IOException ex)
        {
            System.err.println (ex.getMessage());
            System.exit (1);
        }

        catch (RSSGetException ex)
        {
            System.err.println (ex.getMessages());
            System.exit (1);
        }

        catch (BadCommandLineException ex)
        {
            System.err.println (ex.getMessage());
            tool.usage();
            System.exit (1);
        }

        catch (RSSParserException ex)
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

    private rssget()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Emit a verbose message.
     *
     * @param level  the verbosity level at which the message becomes visible
     * @param msg    the message to emit
     */
    public void verbose (int level, String msg)
    {
        if (config.verbosityLevel() >= level)
            System.out.println ("[V:" + level + "] " + msg);
    }

    /**
     * Run the rssget tool. Parses the command line arguments, storing the
     * results in an internal configuration; then, calls the
     * {@link #processRSSFeeds} method.
     *
     * @param args  the command-line parameters
     *
     * @throws IOException              unable to open or read a required file
     * @throws ConfigurationException   error in configuration file
     * @throws RSSParserException       error parsing XML feed(s)
     * @throws BadCommandLineException  command-line error
     * @throws RSSGetException          any other error
     *
     * @see #processRSSFeeds
     */
    public void runProgram (String[] args)
        throws RSSGetException,
               IOException,
               ConfigurationException,
               RSSParserException,
               BadCommandLineException
        
    {
        parseParams (args);
        if (showBuildInfo)
            Version.showBuildInfo();

        processRSSFeeds (this.config, this.emailAddresses);
    }

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
     * @throws RSSGetException          any other error
     */
    public void processRSSFeeds (RSSGetConfiguration configuration,
                                 Collection          emailAddresses)
        throws IOException,
               ConfigurationException,
               RSSParserException,
               RSSGetException
    {
        Iterator     it;
        String       parserClassName;
        RSSParser    parser;
        Collection   channels;
        PrintWriter  out;

        loadOutputHandlers (configuration, emailAddresses);

        if (useCache)
        {
            cache = new RSSGetCache (this, configuration);
            cache.setCurrentTime (currentTime);
            cache.loadCache();
        }

        // Parse the RSS feeds

        parserClassName = configuration.getRSSParserClassName();
        verbose (2, "Getting parser \"" + parserClassName + "\"");
        parser = RSSParserFactory.getRSSParser (parserClassName);

        channels = new ArrayList();

        for (it = configuration.getFeeds().iterator(); it.hasNext(); )
        {
            RSSFeedInfo feedInfo = (RSSFeedInfo) it.next();
            RSSChannel  channel  = processFeed (feedInfo,
                                                parser,
                                                configuration);

            if (channel != null)
                channels.add (new ChannelFeedInfo (feedInfo, channel));
        }

        if (channels.size() > 0)
        {
            // Dump the output to each output handler

            out = new PrintWriter (System.out);

            for (it = outputHandlers.iterator(); it.hasNext(); )
            {
                OutputHandler outputHandler = (OutputHandler) it.next();

                outputHandler.init (out, configuration);
                for (Iterator itChannel = channels.iterator();
                     itChannel.hasNext(); )
                {
                    ChannelFeedInfo cfi = (ChannelFeedInfo) itChannel.next();
                    outputHandler.displayChannel (cfi.channel, cfi.feedInfo);
                }

                outputHandler.flush();
            }
        }

        if ((cache != null) && configuration.mustUpdateCache())
            cache.saveCache();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void parseParams (String[] args)
        throws ConfigurationException,
               IOException,
               FileNotFoundException,
               BadCommandLineException
    {
        try
        {
            String  prog = this.getClass().getName();
            Map     opts = new HashMap();
            int     i;

            // First, find the config file. We want to load it before
            // processing the options, since they might override config
            // values.

            i = 0;
            while ((i < args.length) && (args[i].startsWith ("-")))
            {
                String s = args[i];

                if (s.equals ("-u") || s.equals ("--noupdate"))
                    opts.put ("u", "");

                else if (s.equals ("--build-info") || s.equals ("-B"))
                    opts.put ("B", "");

                else if (s.equals ("-Q") || s.equals ("--noquiet"))
                    opts.put ("Q", "");

                else if (s.equals ("-q") || s.equals ("--quiet"))
                    opts.put ("q", "");

                else if (s.equals ("-C") || s.equals ("--nocache"))
                    opts.put ("C", "");

                else if (s.equals ("-R") || s.equals ("--no-rss-version"))
                    opts.put ("R", "");

                else if (s.equals ("-r") || s.equals ("--rss-version"))
                    opts.put ("r", "");

                else if (s.equals ("-t") || s.equals ("--time"))
                    opts.put ("t", args[++i]);

                else if (s.equals ("-v") || s.equals ("--verbose"))
                    opts.put ("v", args[++i]);

                else if (s.equals ("-d") || s.equals ("--show-dates"))
                    opts.put ("d", "");

                else if (s.equals ("-D") || s.equals ("--no-dates"))
                    opts.put ("D", "");

                else
                    throw new BadCommandLineException ("Unknown option: "
                                                     + s);

                i++;
            }

            config = new RSSGetConfiguration (args[i++]);

            if ((args.length - i) > 0)
            {
                while (i < args.length)
                    emailAddresses.add (args[i++]);
            }
                

            // Now, process the options.

            for (Iterator it = opts.keySet().iterator(); it.hasNext(); )
            {
                String opt = (String) it.next();
                int    c   = opt.charAt (0);

                switch (c)
                {
                    case 'u':   // --noupdate
                        config.setMustUpdateCacheFlag (false);
                        break;

                    case 'B':
                        showBuildInfo = true;
                        break;

                    case 'C':   // --nocache
                        useCache = false;
                        break;

                    case 'd':   // --show-dates
                        config.setShowDatesFlag (true);
                        break;

                    case 'D':   // --no-dates
                        config.setShowDatesFlag (false);
                        break;

                    case 'Q':   // --noquiet
                        config.setQuietFlag (false);
                        break;

                    case 'q':   // --quiet
                        config.setQuietFlag (true);
                        break;

                    case 'r':
                        config.setShowRSSVersionFlag (true);
                        break;

                    case 'R':
                        config.setShowRSSVersionFlag (false);
                        break;

                    case 't':   // --time
                        currentTime = parseDateTime ((String) opts.get (opt));
                        break;

                    case 'v':   // --verbose
                        String arg = (String) opts.get (opt);
                        try
                        {
                            config.setVerbosityLevel (Integer.parseInt (arg));
                        }

                        catch (NumberFormatException ex)
                        {
                            throw new BadCommandLineException
                                               ("Bad parameter \""
                                              + arg
                                              + "\" to --verbose (-v) option");
                        }
                        break;

                    default:
                        // Already handled.
                }
            }
        }

        catch (ArrayIndexOutOfBoundsException ex)
        {
            throw new BadCommandLineException ("Missing parameter(s)");
        }
    }

    private Date parseDateTime (String s)
        throws BadCommandLineException
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
            throw new BadCommandLineException ("Bad date/time: \"" + s + "\"");

        return date;
    }

    private void usage()
    {
        String[] USAGE = new String[]
        {
"rssget, version " + Version.VERSION,
"",
"Usage: " + rssget.class.getName() + " [options] configFile [email_addr ...]",
"",
"OPTIONS",
"-B, --build-info     Show full build information",
"-C, --nocache        Don't use a cache file at all.",
"-d, --show-dates     Show dates on feeds and feed items, if available",
"-D, --no-dates       Don't show dates on feeds and feed items",
"-u, --noupdate       Read the cache, but don't update it",
"-Q, --noquiet        Emit information about sites with no information",
"-q, --quiet          Be quiet about sites with no information",
"-r, --rss-version    Display the RSS version used at each site",
"-R, --no-rss-version Don't display the RSS version used at each site",
"-t datetime",
"--time datetime      For the purposes of cache expiration, pretend the",
"                       current time is <datetime>. <datetime> may be in a ",
"                       variety of forms.",
"-v level             Set the verbosity (debug) level. Default: 0 (off)",
"--verbose level"
        };

        for (int i = 0; i < USAGE.length; i++)
            System.err.println (USAGE[i]);
    }

    private void loadOutputHandlers (RSSGetConfiguration configuration,
                                     Collection          emailAddresses)
        throws ConfigurationException,
               RSSGetException
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

        else
        {
            // If there are multiple handlers, nuke all but the first. It
            // doesn't make sense to use multiple handlers when going to
            // standard output.

            verbose (3,
                     "No email addresses. Nuking all but the first "
                   + "output handler.");
            handler = (OutputHandler) outputHandlers.iterator().next();
            outputHandlers.clear();
            outputHandlers.add (handler);
        }
    }

    private RSSChannel processFeed (RSSFeedInfo         feedInfo,
                                    RSSParser           parser,
                                    RSSGetConfiguration configuration)
        throws RSSParserException
    {
        URL         feedURL = feedInfo.getURL();
        RSSChannel  channel = null;

        try
        {
            String feedURLString = feedURL.toString();
            verbose (3, "Parsing feed at " + feedURLString);

            // Don't download the channel if it hasn't been modified since
            // we last checked it.

            feedURL = Util.normalizeURL (feedURL);
            URLConnection conn = feedURL.openConnection();
            if (feedHasChanged (conn, feedURL))
            {
                if (cache != null)
                    cache.addToCache (feedURL, feedInfo);

                channel = parser.parseRSSFeed (conn.getInputStream());
                processChannelItems (channel, feedInfo);
                if (channel.getItems().size() == 0)
                    channel = null;
            }
        }

        catch (MalformedURLException ex)
        {
            if (configuration.verbosityLevel() > 1)
                ex.printStackTrace();
            else
                verbose (1, ex.getMessage());
        }

        catch (RSSParserException ex)
        {
            if (configuration.verbosityLevel() > 1)
                ex.printStackTrace();
            else
                verbose (1, ex.getMessage());
        }

        catch (IOException ex)
        {
            if (configuration.verbosityLevel() > 1)
                ex.printStackTrace();
            else
                verbose (1, ex.getMessage());
        }

        return channel;
    }

    private boolean feedHasChanged (URLConnection conn, URL feedURL)
        throws IOException
    {
        long     lastSeen = 0;
        long     lastModified = 0;
        boolean  hasChanged = false;

        if ((cache != null) && (cache.containsURL (feedURL)))
        {
            RSSCacheEntry entry = (RSSCacheEntry) cache.getItem (feedURL);
            lastSeen = entry.getTimestamp();
        }

        if (lastSeen == 0)
        {
            verbose (2,
                     "Feed \""
                   + feedURL.toString()
                   + "\" has no record last-seen time.");
            hasChanged = true;
        }

        else if ((lastModified = conn.getLastModified()) == 0)
        {
            verbose (2,
                     "Feed \""
                   + feedURL.toString()
                   + "\" provides no last-modified time.");
            hasChanged = true;
        }

        else if (lastSeen >= lastModified)
        {
            verbose (2,
                     "Feed \""
                   + feedURL.toString()
                   + "\" has Last-Modified time of "
                   + new Date (lastModified).toString()
                   + ", which is not newer than last-seen time of "
                   + new Date (lastSeen).toString()
                   + ". Feed has no new data.");
        }

        else
        {
            verbose (2,
                     "Feed \""
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
                                      RSSFeedInfo feedInfo)
        throws RSSParserException,
               MalformedURLException
    {
        Collection  items;
        Iterator    it;
        String      titleOverride = feedInfo.getTitleOverride();
        boolean     pruneURLs = feedInfo.pruneURLs();
        String      editCmd = feedInfo.getItemURLEditCommand();

        if (titleOverride != null)
            channel.setTitle (titleOverride);

        if (editCmd != null)
        {
            verbose (1,
                     "Channel \""
                   + channel.getLink().toString()
                   + "\": Edit command is: "
                   + editCmd);
        }

        items = channel.getItems();

        // First, weed out the ones we don't care about.

        verbose (2, String.valueOf (items.size()) + " total items");
        for (it = items.iterator(); it.hasNext(); )
        {
            RSSItem item = (RSSItem) it.next();
            URL itemURL = item.getLink();

            if (itemURL == null)
            {
                verbose (2, "Skipping item with null URL.");
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

            if ((cache != null) && cache.containsURL (itemURL))
            {
                verbose (3,
                         "Skipping cached URL \"" + itemURL.toString() + "\"");
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

                verbose (3,
                         "Cacheing URL \""
                       + item.getLink().toString()
                       + "\"");
                if (cache != null)
                    cache.addToCache (item.getLink(), feedInfo);
            }
        }
    }
}
