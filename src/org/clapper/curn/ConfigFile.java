/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.net.URL;
import java.net.MalformedURLException;

import org.clapper.util.config.Configuration;
import org.clapper.util.config.NoSuchVariableException;
import org.clapper.util.config.ConfigurationException;

/**
 * <p><tt>RSSGetConfiguration</tt> uses the <tt>Configuration</tt>
 * class (part of the <i>clapper.org</i> Java Utility library) to parse
 * and validate the <i>rssget</i> configuration file, holding the results
 * in memory for easy access.</p>
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSGetConfiguration extends Configuration
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /**
     * Main section name
     */
    private static final String  MAIN_SECTION           = "rssget";

    /**
     * Variable names
     */
    private static final String  VAR_CACHE_FILE         = "CacheFile";
    private static final String  VAR_NO_CACHE_UPDATE    = "NoCacheUpdate";
    private static final String  VAR_DEFAULT_CACHE_DAYS = "DefaultDaysToCache";
    private static final String  VAR_QUIET              = "Quiet";
    private static final String  VAR_SUMMARY_ONLY       = "SummaryOnly";
    private static final String  VAR_VERBOSITY_LEVEL    = "Verbosity";
    private static final String  VAR_SMTPHOST           = "SMTPHost";
    private static final String  VAR_DEFAULT_MAIL_FROM  = "DefaultMailFrom";
    private static final String  VAR_MAIL_SUBJECT       = "Subject";
    private static final String  VAR_DAYS_TO_CACHE      = "DaysToCache";
    private static final String  VAR_PARSER_CLASS_NAME  = "ParserClass";
    private static final String  VAR_PRUNE_URLS         = "PruneURLs";
    private static final String  VAR_SHOW_RSS_VERSION   = "ShowRSSVersion";
    private static final String  VAR_OUTPUT_CLASS_NAME  = "OutputHandlerClass";

    /**
     * Default values
     */
    private static final int     DEF_DAYS_TO_CACHE     = 5;
    private static final int     DEF_VERBOSITY_LEVEL   = 0;
    private static final boolean DEF_PRUNE_URLS        = false;
    private static final boolean DEF_QUIET             = false;
    private static final boolean DEF_NO_CACHE_UPDATE   = false;
    private static final boolean DEF_SUMMARY_ONLY      = false;
    private static final boolean DEF_SHOW_RSS_VERSION  = false;
    private static final String  DEF_PARSER_CLASS_NAME =
                             "org.clapper.rssget.parser.minirss.MiniRSSParser";
    private static final String  DEF_OUTPUT_CLASS_NAME =
                             "org.clapper.rssget.TextOutputHandler";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private File        cacheFile        = null;
    private int         defaultCacheDays = DEF_DAYS_TO_CACHE;
    private boolean     updateCache      = true;
    private boolean     quiet            = false;
    private boolean     summaryOnly      = false;
    private boolean     showRSSFormat    = false;
    private int         verboseness      = 0;
    private Collection  rssFeeds         = new ArrayList();
    private Map         rssFeedMap       = new HashMap();
    private String      parserClassName  = DEF_PARSER_CLASS_NAME;
    private String      outputClassName  = DEF_OUTPUT_CLASS_NAME;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct an <tt>RSSGetConfiguration</tt> object that parses data
     * from the specified file.
     *
     * @param f  The <tt>File</tt> to open and parse
     *
     * @throws IOException             can't open or read file
     * @throws ConfigurationException  error in configuration data
     */
    RSSGetConfiguration (File f)
        throws IOException,
               ConfigurationException
    {
        super (f);
        validate();
    }

    /**
     * Construct an <tt>RSSGetConfiguration</tt> object that parses data
     * from the specified file.
     *
     * @param path  the path to the file to parse
     *
     * @throws FileNotFoundException   specified file doesn't exist
     * @throws IOException             can't open or read file
     * @throws ConfigurationException  error in configuration data
     */
    RSSGetConfiguration (String path)
        throws FileNotFoundException,
               IOException,
               ConfigurationException
    {
        super (path);
        validate();
    }

    /**
     * Construct an <tt>RSSGetConfiguration</tt> object that parses data
     * from the specified URL.
     *
     * @param url  the URL to open and parse
     *
     * @throws IOException             can't open or read URL
     * @throws ConfigurationException  error in configuration data
     */
    RSSGetConfiguration (URL url)
        throws IOException,
               ConfigurationException
    {
        super (url);
        validate();
    }

    /**
     * Construct an <tt>RSSGetConfiguration</tt> object that parses data
     * from the specified <tt>InputStream</tt>.
     *
     * @param iStream  the <tt>InputStream</tt>
     *
     * @throws IOException             can't open or read URL
     * @throws ConfigurationException  error in configuration data
     */
    RSSGetConfiguration (InputStream iStream)
        throws IOException,
               ConfigurationException
    {
        super (iStream);
        validate();
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the name of the RSS parser class to use. The caller is responsible
     * for loading the returned class name and verifying that it implements
     * the appropriate interface(s).
     *
     * @return the full class name
     */
    String getRSSParserClassName()
    {
        return parserClassName;
    }

    /**
     * Get the name of the class that handles output. The caller is
     * responsible for loading the returned class name and verifying that
     * it implements the appropriate interface(s).
     *
     * @return the full class name
     */
    String getOutputHandlerClassName()
    {
        return outputClassName;
    }
    /**
     * Get the configured cache file.
     *
     * @return the cache file
     */
    File getCacheFile()
    {
        return cacheFile;
    }

    /**
     * Determine whether the cache should be updated.
     *
     * @return <tt>true</tt> if the cache should be updated, <tt>false</tt>
     *         if it should not.
     */
    boolean mustUpdateCache()
    {
        return updateCache;
    }

    /**
     * Change the "update cache" flag.
     *
     * @param val <tt>true</tt> if the cache should be updated, <tt>false</tt>
     *            if it should not
     */
    void setMustUpdateCacheFlag (boolean val)
    {
        updateCache = val;
    }

    /**
     * Return the value of the "quiet" flag.
     *
     * @return <tt>true</tt> if "quiet" flag is set, <tt>false</tt>
     *         otherwise
     */
    boolean beQuiet()
    {
        return quiet;
    }

    /**
     * Set the value of the "quiet" flag.
     *
     * @param val <tt>true</tt> to set the "quiet" flag, <tt>false</tt>
     *            to clear it
     */
    void setQuietFlag (boolean val)
    {
        this.quiet = val;
    }

    /**
     * Return the value of "summary only" flag.
     *
     * @return <tt>true</tt> if "summary only" flag is set, <tt>false</tt>
     *         otherwise
     */
    boolean summarizeOnly()
    {
        return summaryOnly;
    }

    /**
     * Set the value of the "summary only" flag.
     *
     * @param val <tt>true</tt> to set the "summary only" flag,
     *            <tt>false</tt> to clear it
     */
    void setSummarizeOnlyFlag (boolean val)
    {
        this.summaryOnly = val;
    }

    /**
     * Return the value of "show RSS version" flag.
     *
     * @return <tt>true</tt> if flag is set, <tt>false</tt> if it isn't
     */
    boolean showRSSVersion()
    {
        return showRSSFormat;
    }

    /**
     * Set the value of the "show RSS version" flag.
     *
     * @param val <tt>true</tt> to set the flag,
     *            <tt>false</tt> to clear it
     */
    void setShowRSSVersionFlag (boolean val)
    {
        this.showRSSFormat = val;
    }

    /**
     * Get the verbosity level.
     *
     * @return the verbosity level
     */
    int verbosityLevel()
    {
        return verboseness;
    }

    /**
     * Set the verbosity level
     *
     * @param val the new level
     */
    void setVerbosityLevel (int val)
    {
        this.verboseness = val;
    }

    /**
     * Get the configured RSS feeds.
     *
     * @return a <tt>Collection</tt> of <tt>RSSFeedInfo</tt> objects.
     */
    Collection getFeeds()
    {
        return Collections.unmodifiableCollection (rssFeeds);
    }

    /**
     * Determine whether the specified URL is one of the configured RSS
     * feeds.
     *
     * @param url  the URL
     *
     * @return <tt>true</tt> if it's there, <tt>false</tt> if not
     */
    boolean hasFeed (URL url)
    {
        return rssFeedMap.containsKey (url);
    }

    /**
     * Get the feed data for a given URL.
     *
     * @param url   the URL
     *
     * @return the corresponding <tt>RSSFeedInfo</tt> object, or null
     *         if not found
     */
    RSSFeedInfo getFeedInfoFor (URL url)
    {
        return (RSSFeedInfo) rssFeedMap.get (url);
    }

    /**
     * Get the feed data for a given URL.
     *
     * @param url   the URL, as a string
     *
     * @return the corresponding <tt>RSSFeedInfo</tt> object, or null
     *         if not found
     */
    RSSFeedInfo getFeedInfoFor (String url)
    {
        try
        {
            return (RSSFeedInfo) rssFeedMap.get (new URL (url));
        }

        catch (MalformedURLException ex)
        {
            return null;
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Validate the loaded configuration.
     *
     * @throws ConfigurationException on error
     */
    private void validate()
        throws ConfigurationException
    {
        // First, verify that the main section is there and process it.

        processMainSection();

        // Each remaining section should be a URL of an RSS site.

        for (Iterator it = getSectionNames (new ArrayList()).iterator();
             it.hasNext(); )
        {
            String sectionName = (String) it.next();

            if (sectionName.equals (MAIN_SECTION))
                continue;

            processSiteURLSection (sectionName);
        }
    }

    /**
     * Verify existence of main section and process it.
     *
     * @throws ConfigurationException on error
     */
    private void processMainSection()
        throws ConfigurationException
    {
        if (! this.containsSection (MAIN_SECTION))
        {
            throw new ConfigurationException ("Configuration file is missing "
                                            + "required \""
                                            + MAIN_SECTION
                                            + "\" section.");
        }

        try
        {
            cacheFile = new File (getVariableValue (MAIN_SECTION,
                                                    VAR_CACHE_FILE));
            if (cacheFile.isDirectory())
            {
                throw new ConfigurationException ("Specified cache file \""
                                                + cacheFile.getPath()
                                                + "\" is a directory.");
            }

            defaultCacheDays = getOptionalIntegerValue (MAIN_SECTION,
                                                        VAR_DEFAULT_CACHE_DAYS,
                                                        DEF_DAYS_TO_CACHE);
            verboseness = getOptionalIntegerValue (MAIN_SECTION,
                                                   VAR_VERBOSITY_LEVEL,
                                                   DEF_VERBOSITY_LEVEL);
            updateCache = (!getOptionalBooleanValue (MAIN_SECTION,
                                                     VAR_NO_CACHE_UPDATE,
                                                     DEF_NO_CACHE_UPDATE));
            quiet = getOptionalBooleanValue (MAIN_SECTION,
                                             VAR_QUIET,
                                             DEF_QUIET);
            summaryOnly = getOptionalBooleanValue (MAIN_SECTION,
                                                   VAR_SUMMARY_ONLY,
                                                   DEF_SUMMARY_ONLY);
            showRSSFormat = getOptionalBooleanValue (MAIN_SECTION,
                                                     VAR_SHOW_RSS_VERSION,
                                                     DEF_SHOW_RSS_VERSION);
            parserClassName = getOptionalStringValue (MAIN_SECTION,
                                                      VAR_PARSER_CLASS_NAME,
                                                      DEF_PARSER_CLASS_NAME);
            outputClassName = getOptionalStringValue (MAIN_SECTION,
                                                      VAR_OUTPUT_CLASS_NAME,
                                                      DEF_OUTPUT_CLASS_NAME);
        }

        catch (NoSuchVariableException ex)
        {
            throw new ConfigurationException ("Missing required variable \""
                                            + ex.getVariableName()
                                            + "\" in section \""
                                            + ex.getSectionName()
                                            + "\".");
        
        }
    }

    /**
     * Process a section that identifies a site's RSS URL.
     *
     * @param sectionName  the section name (which is also the URL)
     *
     * @throws ConfigurationException  configuration error
     */
    private void processSiteURLSection (String sectionName)
        throws ConfigurationException
    {
        try
        {
            URL url = new URL (sectionName);
            url = Util.normalizeURL (url);

            RSSFeedInfo feedInfo = new RSSFeedInfo (url);

            int cacheDays = getOptionalIntegerValue (sectionName,
                                                     VAR_DAYS_TO_CACHE,
                                                     defaultCacheDays);
            feedInfo.setDaysToCache (cacheDays);

            boolean pruneURLs = getOptionalBooleanValue (sectionName,
                                                         VAR_PRUNE_URLS,
                                                         DEF_PRUNE_URLS);
            feedInfo.setPruneURLsFlag (pruneURLs);

            rssFeeds.add (feedInfo);
            rssFeedMap.put (url, feedInfo);
        }

        catch (MalformedURLException ex)
        {
            throw new ConfigurationException ("Bad RSS site URL: \""
                                            + sectionName
                                            + "\"");
        }
    }
}
