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
    private static final String VERSION = "0.2";

    private RSSGetConfiguration config        = null;
    private boolean             useCache      = true;
    private boolean             showVersion   = false;
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
            System.out.println (msg);
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

        if (showVersion)
            System.out.println ("rssget, version " + VERSION);

        RSSGetOutputHandler outputHandler = new TextOutputHandler (System.out);

        if (useCache)
        {
            cache = new RSSGetCache (this, config);
            cache.setCurrentTime (currentTime);
            cache.loadCache();
        }

        for (Iterator itFeeds = config.getFeeds().iterator();
             itFeeds.hasNext(); )
        {
            RSSFeedInfo feed = (RSSFeedInfo) itFeeds.next();

            processFeed (feed, outputHandler);
        }

        if (useCache && config.mustUpdateCache())
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

                else if (args[i].equals ("-V") || args[i].equals ("--version"))
                    opts.put ("V", "");

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

                    case 'V':   // --version
                        showVersion = true;
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
        class DateParseInfo
        {
            DateFormat format;
            boolean    timeOnly;

            DateParseInfo (String fmtString, boolean timeOnly)
            {
                this.format   = new SimpleDateFormat (fmtString);
                this.timeOnly = timeOnly;
            }
        }

        DateParseInfo[] formats = new DateParseInfo[]
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

        Date date = null;

        for (int i = 0; i < formats.length; i++)
        {
            try
            {
                date = formats[i].format.parse (s);
                if (date != null)
                {
                    if (formats[i].timeOnly)
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
"                    of the following forms:",
"                    yyyy/mm/dd hh:mm:ss [am/pm]",
"                    yyyy/mm/dd hh:mm:ss",
"                    yyyy/mm/dd hh:mm [am/pm]",
"                    yyyy/mm/dd hh:mm",
"                    yyyy/mm/dd h:mm [am/pm]",
"                    yyyy/mm/dd h:mm",
"                    yyyy/mm/dd hh",
"                    yyyy/mm/dd h",
"                    yyyy/mm/dd",
"                    yy/mm/dd",
"                    hh:mm:ss a",
"                    hh:mm:ss",
"                    hh:mm a",
"                    hh:mm",
"                    h:mm a",
"                    h:mm",
"                    hh a",
"                    hh",
"                    h a",
"                    h",
"-v level          Set the verbosity (debug) level. Default: 0 (off)",
"--verbose level",
"-V, --version     Display this tool's version"
        };

        for (int i = 0; i < USAGE.length; i++)
            System.err.println (USAGE[i]);
    }

    private void processFeed (RSSFeedInfo         feedInfo,
                              RSSGetOutputHandler outputHandler)
        throws RSSParserException
    {
        URL url = feedInfo.getURL();

        verbose (3, "Getting parser.");
        String parserClassName = config.getRSSParserClassName();
        RSSParser parser = RSSParserFactory.getRSSParser (parserClassName);

        try
        {
            verbose (3, "Parsing feed at " + url.toString());
            RSSChannel channel = parser.parseRSSFeed (url);
            Collection items = channel.getItems();
            RSSItem[] itemArray = null;

            // First, weed out the ones we don't care about.

            verbose (3, String.valueOf (items.size()) + " total items");
            for (Iterator it = items.iterator(); it.hasNext(); )
            {
                RSSItem item = (RSSItem) it.next();
                URL itemURL = item.getURL();

                if (useCache && cache.containsItemURL (itemURL))
                {
                    verbose (3,
                             "Skipping cached URL \""
                           + itemURL.toString()
                           + "\"");
                    it.remove();
                }
            }

            // Now, if there's anything left, get them as an array.

            if (items.size() > 0)
                itemArray = (RSSItem[]) items.toArray (new RSSItem[0]);

            outputHandler.displayChannelItems (itemArray, channel, config);

            // Finally, add all the items to the cache.

            if (itemArray != null)
            {
                for (int i = 0; i < itemArray.length; i++)
                {
                    verbose (3,
                             "Cacheing URL \""
                           + itemArray[i].getURL().toString()
                           + "\"");
                    cache.addToCache (itemArray[i].getURL(), channel);
                }
            }
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
