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
import java.net.MalformedURLException;

import org.clapper.rssget.parser.RSSParserFactory;
import org.clapper.rssget.parser.RSSParser;
import org.clapper.rssget.parser.RSSParserException;
import org.clapper.rssget.parser.RSSChannel;
import org.clapper.rssget.parser.RSSItem;

import org.clapper.util.misc.BadCommandLineException;
import org.clapper.util.text.TextUtils;
import org.clapper.util.config.ConfigurationException;

/**
* <p>rssget - scan RSS feeds and display a textual summary, suitable for
* emailing.</p>
*
* <p>This program scans a configured set of URLs, each one representing an
* RSS feed, and summarizes the results in an easy-to-read text format.
* <i>rssget</i> keeps track of URLs it's seen before, using an on-disk
* cache; when using the cache, it will suppress displaying URLs it has
* already reported (though that behavior can be disabled).</p>
* 
* <p><i>rssget</i> requires the <i>Informa</i> parsing library, which is
* available at {@link <a href="http://orchard.sourceforge.net/">}.</p>
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

    /*----------------------------------------------------------------------*\
    \*----------------------------------------------------------------------*/

    private RSSGetConfiguration config        = null;
    private boolean             useCache      = true;
    private RSSGetCache         cache         = null;
    private Date                currentTime   = new Date();

    public static void main (String[] args) throws Exception
    {
        rssget tool = new rssget();

        try
        {
            tool.runProgram (args);
        }

        catch (ConfigurationException ex)
        {
            System.err.println (ex.getMessage());
            System.exit (1);
        }

        catch (IOException ex)
        {
            System.err.println (ex.getMessage());
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

    rssget()
    {
    }

    public void verbose (int level, String msg)
    {
        if (config.verbosityLevel() >= level)
            System.out.println ("[V:" + level + "] " + msg);
    }

    private void runProgram (String[] args)
        throws IOException,
               ConfigurationException,
               RSSParserException,
               BadCommandLineException
        
    {
        //File inpFile = new File(args[0]);
        //ChannelIF channel = RSSParser.parse(new ChannelBuilder(), inpFile);

        parseParams (args);

        OutputHandler outputHandler;
        String outputHandlerClassName = config.getOutputHandlerClassName();

        outputHandler = OutputHandlerFactory.getOutputHandler
                                                   (outputHandlerClassName);

        if (useCache)
        {
            cache = new RSSGetCache (this, config);
            cache.setCurrentTime (currentTime);
            cache.loadCache();
        }

        String parserClassName = config.getRSSParserClassName();
        verbose (2, "Getting parser \"" + parserClassName + "\"");
        RSSParser parser = RSSParserFactory.getRSSParser (parserClassName);

        outputHandler.init (new PrintWriter (System.out), config);

        for (Iterator itFeeds = config.getFeeds().iterator();
             itFeeds.hasNext(); )
        {
            RSSFeedInfo feed = (RSSFeedInfo) itFeeds.next();

            processFeed (feed, parser, outputHandler);
        }

        outputHandler.flush();

        if ((cache != null) && config.mustUpdateCache())
            cache.saveCache();
    }

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

                else if (s.equals ("-Q") || s.equals ("--noquiet"))
                    opts.put ("Q", "");

                else if (s.equals ("-q") || s.equals ("--quiet"))
                    opts.put ("q", "");

                else if (s.equals ("-C") || s.equals ("--nocache"))
                    opts.put ("C", "");

                else if (s.equals ("-s") || s.equals ("--summary"))
                    opts.put ("s", "");

                else if (s.equals ("-f") || s.equals ("--full"))
                    opts.put ("f", "");

                else if (s.equals ("-R") || s.equals ("--no-rss-version"))
                    opts.put ("R", "");

                else if (s.equals ("-r") || s.equals ("--rss-version"))
                    opts.put ("r", "");

                else if (s.equals ("-t") || s.equals ("--time"))
                    opts.put ("t", args[++i]);

                else if (s.equals ("-v") || s.equals ("--verbose"))
                    opts.put ("v", args[++i]);

                else
                    throw new BadCommandLineException ("Unknown option: "
                                                     + s);


                i++;
            }

            if ((args.length - i) > 1)
                throw new BadCommandLineException ("Too many parameters.");
                
            config = new RSSGetConfiguration (args[i]);

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

                    case 'C':   // --nocache
                        useCache = false;
                        break;

                    case 'Q':   // --noquiet
                        config.setQuietFlag (false);
                        break;

                    case 'q':   // --quiet
                        config.setQuietFlag (true);
                        break;

                    case 's':   // --summary
                        config.setSummarizeOnlyFlag (true);
                        break;

                    case 'f':   // --full
                        config.setSummarizeOnlyFlag (false);
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
"rssget, version " + Util.getVersion(),
"",
"Usage: " + rssget.class.getName() + " [options] configFile",
"",
"OPTIONS",
"-f, --full           For each item, display the URL, title and description.",
"                       (The opposite of --summary.)",
"-C, --nocache        Don't use a cache file at all.",
"-u, --noupdate       Read the cache, but don't update it",
"-Q, --noquiet        Emit information about sites with no information",
"-q, --quiet          Be quiet about sites with no information",
"-r, --rss-version    Display the RSS version used at each site",
"-R, --no-rss-version Don't display the RSS version used at each site",
"-s, --summary        For each item, display only the URL and title. Omit the",
"                       description. (The opposite of --full.)",
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

    private void processFeed (RSSFeedInfo   feedInfo,
                              RSSParser     parser,
                              OutputHandler outputHandler)
        throws RSSParserException
    {
        URL url = feedInfo.getURL();

        try
        {
            verbose (3, "Parsing feed at " + url.toString());
            RSSChannel channel = parser.parseRSSFeed (url);
            Collection items = channel.getItems();
            RSSItem[] itemArray = null;
            boolean pruneURLs = feedInfo.pruneURLs();

            // First, weed out the ones we don't care about.

            verbose (3, String.valueOf (items.size()) + " total items");
            for (Iterator it = items.iterator(); it.hasNext(); )
            {
                RSSItem item = (RSSItem) it.next();
                URL itemURL = item.getLink();

                if (itemURL == null)
                {
                    verbose (3, "Skipping item with null URL.");
                    it.remove();
                    continue;
                }

                // Prune the URL of its parameters, if configured for this
                // site. This must be done before checking the cache,
                // because the pruned URLs are what end up in the cache.

                if (pruneURLs)
                {
                    String s = itemURL.toExternalForm();
                    int    i = s.indexOf ("?");

                    if (i != -1)
                    {
                        s = s.substring (0, i);
                        itemURL = new URL (s);
                    }
                }
                
                // Normalize the URL.

                itemURL = Util.normalizeURL (itemURL);
                item.setLink (itemURL);

                // Skip it if it's cached.

                if ((cache != null) && cache.containsItemURL (itemURL))
                {
                    verbose (3,
                             "Skipping cached URL \""
                           + itemURL.toString()
                           + "\"");
                    it.remove();
                    continue;
                }
            }

            // Now, change the channel's items to the ones that are left.

            channel.setItems (items);
            outputHandler.displayChannel (channel);

            // Finally, add all the items to the cache.

            if (itemArray != null)
            {
                for (int i = 0; i < itemArray.length; i++)
                {
                    verbose (3,
                             "Cacheing URL \""
                           + itemArray[i].getLink().toString()
                           + "\"");
                    if (cache != null)
                        cache.addToCache (itemArray[i].getLink(), feedInfo);
                }
            }
        }

        catch (MalformedURLException ex)
        {
            if (config.verbosityLevel() > 1)
                ex.printStackTrace();
            else
                verbose (1, ex.getMessage());
        }

        catch (RSSParserException ex)
        {
            if (config.verbosityLevel() > 1)
                ex.printStackTrace();
            else
                verbose (1, ex.getMessage());
        }

        catch (IOException ex)
        {
            if (config.verbosityLevel() > 1)
                ex.printStackTrace();
            else
                verbose (1, ex.getMessage());
        }
    }
}
