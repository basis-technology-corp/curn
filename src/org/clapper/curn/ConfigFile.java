/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.io.*;
import java.util.*;
import java.net.*;
import org.clapper.util.text.*;
import org.clapper.util.config.*;
import org.clapper.util.io.*;

/**
 * <p><tt>RSSGetConfiguration</tt> uses the <tt>Configuration</tt>
 * class (part of the <i>clapper.org</i> Java Utility library) to parse
 * and validate the <i>rssget</i> configuration file, holding the results
 * in memory for easy access.</p>
 *
 * @version <tt>$Revision$</tt>
 */
class RSSGetConfiguration extends Configuration
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    private static final String MAIN_SECTION           = "rssget";
    private static final String VAR_CACHE_FILE         = "CacheFile";
    private static final String VAR_NO_CACHE_UPDATE    = "NoCacheUpdate";
    private static final String VAR_DEFAULT_CACHE_DAYS = "DefaultDaysToCache";
    private static final String VAR_QUIET              = "Quiet";
    private static final String VAR_SUMMARY_ONLY       = "SummaryOnly";
    private static final String VAR_VERBOSITY_LEVEL    = "Verbosity";
    private static final String VAR_SMTPHOST           = "SMTPHost";
    private static final String VAR_DEFAULT_MAIL_FROM  = "DefaultMailFrom";
    private static final String VAR_MAIL_SUBJECT       = "Subject";
    private static final String VAR_DAYS_TO_CACHE      = "DaysToCache";

    private static final int    DEFAULT_DAYS_TO_CACHE  = 5;
    private static final int    DEFAULT_VERBOSITY_LEVEL= 0;

    /*----------------------------------------------------------------------*\
                                  Classes
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private File        cacheFile        = null;
    private int         defaultCacheDays = DEFAULT_DAYS_TO_CACHE;
    private boolean     updateCache      = true;
    private boolean     quiet            = false;
    private boolean     summaryOnly      = false;
    private int         verboseness      = 0;
    private Collection  rssFeeds         = new ArrayList();
    private Map         rssFeedMap       = new HashMap();

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
                                                        DEFAULT_DAYS_TO_CACHE);
            verboseness = getOptionalIntegerValue (MAIN_SECTION,
                                                   VAR_VERBOSITY_LEVEL,
                                                   DEFAULT_VERBOSITY_LEVEL);
            updateCache = (! getOptionalBooleanValue (MAIN_SECTION,
                                                      VAR_NO_CACHE_UPDATE,
                                                      false));
            quiet = getOptionalBooleanValue (MAIN_SECTION, VAR_QUIET, false);
            summaryOnly = getOptionalBooleanValue (MAIN_SECTION,
                                                   VAR_SUMMARY_ONLY,
                                                   false);
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
            RSSFeedInfo feedInfo = new RSSFeedInfo (url);

            int cacheDays = getOptionalIntegerValue (sectionName,
                                                     VAR_DAYS_TO_CACHE,
                                                     defaultCacheDays);
            feedInfo.setDaysToCache (cacheDays);
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
