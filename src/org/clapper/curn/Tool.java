package org.clapper.rssget;

import java.io.*;
import java.util.*;
import java.net.*;
import org.clapper.util.io.*;
import org.clapper.util.text.*;
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
    private static final String VERSION = "0.1";

    private RSSGetConfiguration config        = null;
    private boolean             useCache      = true;
    private boolean             showVersion   = false;
    private RSSGetCache         cache         = null;

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

        catch (IllegalArgumentException ex)
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
               IllegalArgumentException
        
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
               IllegalArgumentException
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

                else if (args[i].equals ("-V") || args[i].equals ("--version"))
                    opts.put ("V", "");

                else if (args[i].equals ("-v") || args[i].equals ("--verbose"))
                    opts.put ("v", args[++i]);

                else
                    throw new IllegalArgumentException ("Unknown option: "
                                                      + args[i]);


                i++;
            }

            if ((args.length - i) > 1)
                throw new IllegalArgumentException ("Too many parameters.");
                
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
                            throw new IllegalArgumentException
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
            throw new IllegalArgumentException ("Missing parameter(s)");
        }
    }

    private void usage()
    {
        String[] USAGE = new String[]
        {
"Usage: " + rssget.class.getName() + " [options] configFile",
"",
"OPTIONS",
"-f, --full        For each item, display the URL, title and description.",
"                  (The opposite of --summary.)",
"-C, --nocache     Don't use a cache file at all.",
"-u, --noupdate    Read the cache, but don't update it",
"-Q, --noquiet     Emit information about sites with no information",
"-q, --quiet       Be quiet about sites with no information",
"-s, --summary     For each item, display only the URL and title. Omit the",
"                  description. (The opposite of --full.)",
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
