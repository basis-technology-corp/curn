/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;
import org.clapper.util.io.*;
import org.clapper.util.text.*;
import org.clapper.util.misc.*;
import org.clapper.util.config.*;

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

    private static final String VERSION = "0.2";

    private static final DateParseInfo[] DATE_FORMATS = new DateParseInfo[]
    {
        new DateParseInfo ("yyyy/MM/dd hh:mm:ss a", false),
        new DateParseInfo ("yyyy/MM/dd hh:mm:ss", false),
        new DateParseInfo ("yyyy/MM/dd hh:mm a", false),
        new DateParseInfo ("yyyy/MM/dd hh:mm", false),
        new DateParseInfo ("yyyy/MM/dd h:mm a", false),
        new DateParseInfo ("yyyy/MM/dd h:mm", false),
        new DateParseInfo ("yyyy/MM/dd hh a", false),
        new DateParseInfo ("yyyy/MM/dd hh", false),
        new DateParseInfo ("yyyy/MM/dd h a", false),
        new DateParseInfo ("yyyy/MM/dd h", false),
        new DateParseInfo ("yyyy/MM/dd", false),
        new DateParseInfo ("yy/MM/dd", false),
        new DateParseInfo ("hh:mm:ss a", true),
        new DateParseInfo ("hh:mm:ss", true),
        new DateParseInfo ("hh:mm a", true),
        new DateParseInfo ("hh:mm", true),
        new DateParseInfo ("h:mm a", true),
        new DateParseInfo ("h:mm", true),
        new DateParseInfo ("hh a", true),
        new DateParseInfo ("hh", true),
        new DateParseInfo ("h a", true),
        new DateParseInfo ("h", true)
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

        RSSGetOutputHandler outputHandler = new TextOutputHandler (System.out);

        if (useCache)
        {
            cache = new RSSGetCache (this, config);
            cache.setCurrentTime (currentTime);
            cache.loadCache();
        }

        String parserClassName = config.getRSSParserClassName();
        verbose (2, "Getting parser \"" + parserClassName + "\"");
        RSSParser parser = RSSParserFactory.getRSSParser (parserClassName);

        for (Iterator itFeeds = config.getFeeds().iterator();
             itFeeds.hasNext(); )
        {
            RSSFeedInfo feed = (RSSFeedInfo) itFeeds.next();

            processFeed (feed, parser, outputHandler);
        }

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
                if (args[i].equals ("-u") || args[i].equals ("--noupdate"))
                    opts.put ("u", "");

                else if (args[i].equals ("-Q") || args[i].equals ("--noquiet"))
                    opts.put ("Q", "");

                else if (args[i].equals ("-q") || args[i].equals ("--quiet"))
                    opts.put ("q", "");

                else if (args[i].equals ("-C") || args[i].equals ("--nocache"))
                    opts.put ("C", "");

                else if (args[i].equals ("-s") || args[i].equals ("--summary"))
                    opts.put ("s", "");

                else if (args[i].equals ("-f") || args[i].equals ("--full"))
                    opts.put ("f", "");

                else if (args[i].equals ("-t") || args[i].equals ("--time"))
                    opts.put ("t", args[++i]);

                else if (args[i].equals ("-v") || args[i].equals ("--verbose"))
                    opts.put ("v", args[++i]);

                else
                    throw new BadCommandLineException ("Unknown option: "
                                                     + args[i]);


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

        for (int i = 0; i < DATE_FORMATS.length; i++)
        {
            try
            {
                date = DATE_FORMATS[i].format.parse (s);
                if (date != null)
                {
                    if (DATE_FORMATS[i].timeOnly)
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
        String[] USAGE1 = new String[]
        {
"rssget, version " + VERSION,
"",
"Usage: " + rssget.class.getName() + " [options] configFile",
"",
"OPTIONS",
"-f, --full        For each item, display the URL, title and description.",
"                    (The opposite of --summary.)",
"-C, --nocache     Don't use a cache file at all.",
"-u, --noupdate    Read the cache, but don't update it",
"-Q, --noquiet     Emit information about sites with no information",
"-q, --quiet       Be quiet about sites with no information",
"-s, --summary     For each item, display only the URL and title. Omit the",
"                    description. (The opposite of --full.)",
"-t datetime",
"--time datetime   For the purposes of cache expiration, pretend the current",
"                    time is <datetime>. <datetime> may be specified in any",
"                    of the following forms:"
        };

        String[] USAGE2 = new String[]
        {
"-v level          Set the verbosity (debug) level. Default: 0 (off)",
"--verbose level",
        };

        int i;

        for (i = 0; i < USAGE1.length; i++)
            System.err.println (USAGE1[i]);

        for (i = 0; i < DATE_FORMATS.length; i++)
            System.err.println ("                    " + DATE_FORMATS[i]);

        for (i = 0; i < USAGE2.length; i++)
            System.err.println (USAGE2[i]);
    }

    private void processFeed (RSSFeedInfo         feedInfo,
                              RSSParser           parser,
                              RSSGetOutputHandler outputHandler)
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

            // Now, if there's anything left, get them as an array.

            if (items.size() > 0)
            {
                itemArray = (RSSItem[]) items.toArray (new RSSItem[0]);
            }

            outputHandler.displayChannelItems (itemArray, channel, config);

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
