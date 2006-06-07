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

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.net.URL;
import java.net.MalformedURLException;

import org.clapper.util.config.Configuration;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.config.NoSuchSectionException;
import org.clapper.util.config.NoSuchVariableException;

import org.clapper.util.logging.Logger;

/**
 * <p><tt>CurnConfig</tt> uses the <tt>Configuration</tt> class (part of
 * the <i>clapper.org</i> Java Utility library) to parse and validate the
 * <i>curn</i> configuration file, holding the results in memory for easy
 * access.</p>
 *
 * @version <tt>$Revision$</tt>
 */
public class CurnConfig extends Configuration
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Variable names
     */
    public static final String VAR_CACHE_FILE        = "CacheFile";
    public static final String VAR_TOTAL_CACHE_BACKUPS = "TotalCacheBackups";
    public static final String VAR_NO_CACHE_UPDATE   = "NoCacheUpdate";
    public static final String VAR_MAIL_SUBJECT      = "Subject";
    public static final String VAR_DAYS_TO_CACHE     = "DaysToCache";
    public static final String VAR_PARSER_CLASS_NAME = "ParserClass";
    public static final String VAR_SHOW_RSS_VERSION  = "ShowRSSVersion";
    public static final String VAR_FEED_URL          = "URL";
    public static final String VAR_CLASS             = "Class";
    public static final String VAR_GET_GZIPPED_FEEDS = "GetGzippedFeeds";
    public static final String VAR_MAX_THREADS       = "MaxThreads";
    public static final String VAR_FORCE_ENCODING    = "ForceEncoding";
    public static final String VAR_FORCE_CHAR_ENCODING = "ForceCharacterEncoding";
    public static final String VAR_DISABLED          = "Disabled";

    /**
     * Configuration variable: allow embedded HTML. Not used here. Used by
     * a plug-in and by output handlers.
     */
    public static final String CFG_ALLOW_EMBEDDED_HTML = "AllowEmbeddedHTML";


    /**
     * Default values
     */
    public static final int     DEF_DAYS_TO_CACHE     = 365;
    public static final boolean DEF_NO_CACHE_UPDATE   = false;
    public static final boolean DEF_SHOW_RSS_VERSION  = false;
    public static final boolean DEF_GET_GZIPPED_FEEDS = true;
    public static final boolean DEF_SAVE_ONLY         = false;
    public static final String  DEF_PARSER_CLASS_NAME =
                             "org.clapper.curn.parser.minirss.MiniRSSParser";
    public static final int     DEF_MAX_THREADS       = 5;
    public static final int     DEF_TOTAL_CACHE_BACKUPS = 0;

    /**
     * Others
     */
    public static final String  NO_LIMIT_VALUE         = "NoLimit";

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /**
     * Main section name
     */
    private static final String  MAIN_SECTION         = "curn";

    /**
     * Prefix for sections that describing individual feeds.
     */
    private static final String FEED_SECTION_PREFIX   = "Feed";

    /**
     * Prefix for output handler sections.
     */
    private static final String OUTPUT_HANDLER_PREFIX = "OutputHandler";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private File cacheFile = null;
    private int defaultCacheDays = DEF_DAYS_TO_CACHE;
    private boolean updateCache = true;
    private boolean summaryOnly = false;
    private boolean showRSSFormat = false;
    private Collection<FeedInfo> feeds = new ArrayList<FeedInfo>();
    private Map<String, FeedInfo> feedMap = new HashMap<String, FeedInfo>();
    private String parserClassName = DEF_PARSER_CLASS_NAME;
    private List<ConfiguredOutputHandler> outputHandlers
                                 = new ArrayList<ConfiguredOutputHandler>();
    private boolean getGzippedFeeds = true;
    private int maxThreads = DEF_MAX_THREADS;
    private int totalCacheBackups = DEF_TOTAL_CACHE_BACKUPS;

    /**
     * For log messages
     */
    private static Logger log = new Logger (CurnConfig.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct an <tt>CurnConfig</tt> object that parses data
     * from the specified file.
     *
     * @param f  The <tt>File</tt> to open and parse
     *
     * @throws IOException             can't open or read file
     * @throws ConfigurationException  error in configuration data
     * @throws CurnException           some other error
     */
    CurnConfig (File f)
        throws IOException,
               ConfigurationException,
               CurnException
    {
        super (f);
        validate();
    }

    /**
     * Construct an <tt>CurnConfig</tt> object that parses data
     * from the specified file.
     *
     * @param path  the path to the file to parse
     *
     * @throws FileNotFoundException   specified file doesn't exist
     * @throws IOException             can't open or read file
     * @throws ConfigurationException  error in configuration data
     * @throws CurnException           some other error
     */
    CurnConfig (String path)
        throws FileNotFoundException,
               IOException,
               ConfigurationException,
               CurnException
    {
        super (path);
        validate();
    }

    /**
     * Construct an <tt>CurnConfig</tt> object that parses data
     * from the specified URL.
     *
     * @param url  the URL to open and parse
     *
     * @throws IOException             can't open or read URL
     * @throws ConfigurationException  error in configuration data
     * @throws CurnException           some other error
     */
    CurnConfig (URL url)
        throws IOException,
               ConfigurationException,
               CurnException
    {
        super (url);
        validate();
    }

    /**
     * Construct an <tt>CurnConfig</tt> object that parses data
     * from the specified <tt>InputStream</tt>.
     *
     * @param iStream  the <tt>InputStream</tt>
     *
     * @throws IOException             can't open or read URL
     * @throws ConfigurationException  error in configuration data
     * @throws CurnException           some other error
     */
    CurnConfig (InputStream iStream)
        throws IOException,
               ConfigurationException,
               CurnException
    {
        super (iStream);
        validate();
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the name of the RSS parser class to use. The caller is responsible
     * for loading the returned class name and verifying that it implements
     * the appropriate interface(s).
     *
     * @return the full class name
     */
    public String getRSSParserClassName()
    {
        return parserClassName;
    }

    /**
     * Gets the list of output handlers from the configuration, in the order
     * they appeared in the configuration.
     *
     * @return an unmodifiable <tt>Collection</tt> of
     *         <tt>ConfiguredOutputHandler</tt> objects. The collection will
     *         be empty, but never null, if no output handlers were configured.
     */
    public Collection<ConfiguredOutputHandler> getOutputHandlers()
    {
        return Collections.unmodifiableList (outputHandlers);
    }

    /**
     * Determine whether the configuration is a "download-only" configuration
     * (i.e., one that downloads various RSS feed data but doesn't parse it.).
     *
     * @return <tt>true</tt> if this is a download-only configuration,
     *         <tt>false</tt> otherwise
     */
    public boolean isDownloadOnly()
    {
        return outputHandlers.size() == 0;
    }

    /**
     * Return the total number of configured output handlers.
     *
     * @return the total number of configured output handlers, or 0 if there
     *         aren't any
     */
    public int totalOutputHandlers()
    {
        return outputHandlers.size();
    }

    /**
     * Get the configured cache file.
     *
     * @return the cache file
     *
     * @see #mustUpdateCache
     * @see #setMustUpdateCacheFlag
     */
    public File getCacheFile()
    {
        return cacheFile;
    }

    /**
     * Get the total number of cache backup files to keep.
     *
     * @return the total number of cache backup files to keep, or 0 for
     *         none.
     */
    public int totalCacheBackups()
    {
        return totalCacheBackups;
    }

    /**
     * Determine whether the cache should be updated.
     *
     * @return <tt>true</tt> if the cache should be updated, <tt>false</tt>
     *         if it should not.
     *
     * @see #getCacheFile
     * @see #setMustUpdateCacheFlag
     */
    public boolean mustUpdateCache()
    {
        return updateCache;
    }

    /**
     * Get the maximum number of concurrent threads to spawn when retrieving
     * RSS feeds.
     *
     * @return the maximum number of threads
     *
     * @see #setMaxThreads
     */
    public int getMaxThreads()
    {
        return maxThreads;
    }

    /**
     * Set the maximum number of concurrent threads to spawn when retrieving
     * RSS feeds.
     *
     * @param newValue the maximum number of threads
     *
     * @throws ConfigurationException bad value
     *
     * @see #getMaxThreads
     */
    public void setMaxThreads (int newValue)
        throws ConfigurationException
    {
        if (newValue <= 0)
        {
            throw new ConfigurationException (Constants.BUNDLE_NAME,
                                              "CurnConfig.badPositiveInteger",
                                              "The \"{0}\" configuration "
                                            + "parameter cannot be set to "
                                            + "{1}. It must have a positive "
                                            + "integer value.",
                                              new Object[]
                                              {
                                                  VAR_MAX_THREADS,
                                                  String.valueOf (newValue)
                                              });
        }

        this.maxThreads = newValue;
    }

    /**
     * Change the "update cache" flag.
     *
     * @param val <tt>true</tt> if the cache should be updated, <tt>false</tt>
     *            if it should not
     *
     * @see #mustUpdateCache
     * @see #getCacheFile
     */
    public void setMustUpdateCacheFlag (boolean val)
    {
        updateCache = val;
    }

    /**
     * Determine whether to retrieve RSS feeds via Gzip. Only applicable
     * when connecting to HTTP servers.
     *
     * @return <tt>true</tt> if Gzip is to be used, <tt>false</tt>
     *         otherwise
     *
     * @see #setRetrieveFeedsWithGzipFlag
     */
    public boolean retrieveFeedsWithGzip()
    {
        return getGzippedFeeds;
    }

    /**
     * Set the flag that controls whether to retrieve RSS feeds via Gzip.
     * Only applicable when connecting to HTTP servers.
     *
     * @param val <tt>true</tt> if Gzip is to be used, <tt>false</tt>
     *            otherwise
     *
     * @see #retrieveFeedsWithGzip
     */
    public void setRetrieveFeedsWithGzipFlag (boolean val)
    {
        this.getGzippedFeeds = val;
    }

    /**
     * Return the value of "show RSS version" flag.
     *
     * @return <tt>true</tt> if flag is set, <tt>false</tt> if it isn't
     *
     * @see #setShowRSSVersionFlag
     */
    public boolean showRSSVersion()
    {
        return showRSSFormat;
    }

    /**
     * Set the value of the "show RSS version" flag.
     *
     * @param val <tt>true</tt> to set the flag,
     *            <tt>false</tt> to clear it
     *
     * @see #showRSSVersion
     */
    public void setShowRSSVersionFlag (boolean val)
    {
        this.showRSSFormat = val;
    }

    /**
     * Get the configured RSS feeds. The feeds are returned in the order
     * they were specified in the configuration file.
     *
     * @return a <tt>Collection</tt> of <tt>FeedInfo</tt> objects.
     *
     * @see #hasFeed
     * @see #getFeedInfoFor(String)
     * @see #getFeedInfoFor(URL)
     */
    public Collection<FeedInfo> getFeeds()
    {
        return Collections.unmodifiableCollection (feeds);
    }

    /**
     * Determine whether the specified URL is one of the configured RSS
     * feeds.
     *
     * @param url  the URL
     *
     * @return <tt>true</tt> if it's there, <tt>false</tt> if not
     *
     * @see #getFeeds
     * @see #getFeedInfoFor(String)
     * @see #getFeedInfoFor(URL)
     */
    public boolean hasFeed (URL url)
    {
        return feedMap.containsKey (url.toString());
    }

    /**
     * Get the feed data for a given URL.
     *
     * @param url   the URL
     *
     * @return the corresponding <tt>RSSFeedInfo</tt> object, or null
     *         if not found
     *
     * @see #getFeeds
     * @see #hasFeed
     * @see #getFeedInfoFor(String)
     * @see FeedInfo
     */
    public FeedInfo getFeedInfoFor (URL url)
    {
        return (FeedInfo) feedMap.get (url.toString());
    }

    /**
     * Get the feed data for a given URL.
     *
     * @param urlString   the URL, as a string
     *
     * @return the corresponding <tt>FeedInfo</tt> object, or null
     *         if not found
     *
     * @see #getFeeds
     * @see #hasFeed
     * @see #getFeedInfoFor(URL)
     * @see FeedInfo
     */
    public FeedInfo getFeedInfoFor (String urlString)
    {
        return (FeedInfo) feedMap.get (urlString);
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Validate the loaded configuration.
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other error
     */
    private void validate()
        throws ConfigurationException,
               CurnException
    {
        // First, verify that the main section is there and process it.

        processMainSection();

        // Process the remaining sections. Skip ones we don't recognize.

        for (String sectionName : getSectionNames())
        {
            if (sectionName.startsWith (FEED_SECTION_PREFIX))
                processFeedSection (sectionName);

            else if (sectionName.startsWith (OUTPUT_HANDLER_PREFIX))
                processOutputHandlerSection (sectionName);

            else
                processUnknownSection (sectionName);
        }
    }

    /**
     * Verify existence of main section and process it.
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other error
     */
    private void processMainSection()
        throws ConfigurationException,
               CurnException
    {
        if (! this.containsSection (MAIN_SECTION))
        {
            throw new ConfigurationException (Constants.BUNDLE_NAME,
                                              "CurnConfig.missingReqSection",
                                              "The configuration file is "
                                            + "missing the required \"{0}\" "
                                            + "section.",
                                              new Object[] {MAIN_SECTION});
        }

        for (String varName : getVariableNames (MAIN_SECTION))
        {
            try
            {
                processMainSectionVariable (varName);
            }

            catch (NoSuchVariableException ex)
            {
                throw new ConfigurationException (Constants.BUNDLE_NAME,
                                                  "CurnConfig.missingReqVar",
                                                  "The configuration file is "
                                                + "missing required variable "
                                                + "\"{0}\" in section "
                                                + "\"{1}\".",
                                                  new Object[]
                                                  {
                                                      ex.getVariableName(),
                                                      ex.getSectionName()
                                                  });
            }
        }
    }

    /**
     * Process a single variable from the main section
     *
     * @param varName the variable name
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other error
     */
    private void processMainSectionVariable (String varName)
        throws ConfigurationException,
               CurnException
    {
        String val = null;

        if (varName.equals (VAR_CACHE_FILE))
        {
            val = getOptionalStringValue (MAIN_SECTION, VAR_CACHE_FILE, null);
            if (val != null)
            {
                cacheFile = new File (val);
                if (cacheFile.isDirectory())
                {
                    throw new ConfigurationException
                        (Constants.BUNDLE_NAME,
                         "CurnConfig.cacheIsDir",
                         "Configured cache file \"{0}\" is a directory.",
                         new Object[] {cacheFile.getPath()});
                }
            }
        }

        else if (varName.equals (VAR_DAYS_TO_CACHE))
        {
            defaultCacheDays = parseMaxDaysParameter (MAIN_SECTION,
                                                      varName,
                                                      DEF_DAYS_TO_CACHE);
            val = String.valueOf (defaultCacheDays);
        }

        else if (varName.equals (VAR_TOTAL_CACHE_BACKUPS))
        {
            totalCacheBackups =
                getOptionalCardinalValue (MAIN_SECTION,
                                          varName,
                                          DEF_TOTAL_CACHE_BACKUPS);
            val = String.valueOf (totalCacheBackups);
        }

        else if (varName.equals (VAR_NO_CACHE_UPDATE))
        {
            updateCache = (!getOptionalBooleanValue (MAIN_SECTION,
                                                     varName,
                                                     DEF_NO_CACHE_UPDATE));
            val = String.valueOf (updateCache);
        }

        else if (varName.equals (VAR_SHOW_RSS_VERSION))
        {
            showRSSFormat = getOptionalBooleanValue (MAIN_SECTION,
                                                     varName,
                                                     DEF_SHOW_RSS_VERSION);
            val = String.valueOf (showRSSFormat);
        }

        else if (varName.equals (VAR_PARSER_CLASS_NAME))
        {
            parserClassName = getOptionalStringValue (MAIN_SECTION,
                                                      varName,
                                                      DEF_PARSER_CLASS_NAME);
            val = String.valueOf (parserClassName);
        }

        else if (varName.equals (VAR_GET_GZIPPED_FEEDS))
        {
            getGzippedFeeds = getOptionalBooleanValue (MAIN_SECTION,
                                                       varName,
                                                       DEF_GET_GZIPPED_FEEDS);
            val = String.valueOf (getGzippedFeeds);
        }

        else if (varName.equals (VAR_MAX_THREADS))
        {
            int maxThreads = getOptionalCardinalValue (MAIN_SECTION,
                                                       varName,
                                                       DEF_MAX_THREADS);
            setMaxThreads (maxThreads);
            val = String.valueOf (maxThreads);
        }

        else
        {
            val = getOptionalStringValue (MAIN_SECTION, varName, null);
        }

        if (val != null)
        {
            MetaPlugIn.getMetaPlugIn().runMainConfigItemPlugIn (MAIN_SECTION,
                                                                varName,
                                                                this);
        }
    }

    /**
     * Process a section that identifies an RSS feed to be polled.
     *
     * @param sectionName  the section name
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other error
     */
    private void processFeedSection (String sectionName)
        throws ConfigurationException,
               CurnException
    {
        FeedInfo   feedInfo = null;
        String     feedURLString = null;
        URL        url = null;
        MetaPlugIn metaPlugIn = MetaPlugIn.getMetaPlugIn();
        boolean    keepFeed = false;

        feedURLString = getConfigurationValue (sectionName, VAR_FEED_URL);

        try
        {
            url = Util.normalizeURL (feedURLString);
            String urlString = url.toString();
            log.debug ("Configured feed: URL=\"" + urlString + "\"");
            feedInfo = new FeedInfo (url);
            metaPlugIn.runFeedConfigItemPlugIn (sectionName,
                                                VAR_FEED_URL,
                                                this,
                                                feedInfo);
        }

        catch (MalformedURLException ex)
        {
            throw new ConfigurationException (Constants.BUNDLE_NAME,
                                              "CurnConfig.badFeedURL",
                                              "Configuration file section "
                                            + "\"{0}\" specifies a bad RSS "
                                            + "feed URL \"{1}\"",
                                              new Object[]
                                              {
                                                  sectionName,
                                                  feedURLString
                                              });
        }


        feedInfo.setDaysToCache (defaultCacheDays);

        for (String varName : getVariableNames (sectionName))
        {
            String value   = null;
            boolean flag;

            if (varName.equals (VAR_DAYS_TO_CACHE))
            {
                int maxDays = parseMaxDaysParameter (sectionName,
                                                     varName,
                                                     defaultCacheDays);
                feedInfo.setDaysToCache (maxDays);
                value = String.valueOf (maxDays);
            }

            else if (varName.equals (VAR_FORCE_ENCODING) ||
                     varName.equals (VAR_FORCE_CHAR_ENCODING))
            {
                value = getConfigurationValue (sectionName, varName);
                feedInfo.setForcedCharacterEncoding (value);
            }

            else
            {
                value = getConfigurationValue (sectionName, varName);
            }

            if (value != null)
            {
                keepFeed = metaPlugIn.runFeedConfigItemPlugIn (sectionName,
                                                               varName,
                                                               this,
                                                               feedInfo);
                if (! keepFeed)
                {
                    log.debug ("A plug-in said to skip feed ["
                             + sectionName
                             + "\"");
                }
            }

            if (! keepFeed)
                break;
        }

        if (url == null)
        {
            throw new ConfigurationException (Constants.BUNDLE_NAME,
                                              "CurnConfig.missingReqVar",
                                              "The configuration file is "
                                            + "missing required variable "
                                            + "\"{0}\" in section \"{1}\"",
                                              new Object[]
                                              {
                                                  VAR_FEED_URL,
                                                  sectionName
                                              });
        }

        if (keepFeed)
        {
            feeds.add (feedInfo);
            feedMap.put (url.toString(), feedInfo);
        }
    }

    /**
     * Process a section that identifies an output handler.
     *
     * @param sectionName  the section name
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other error
     */
    private void processOutputHandlerSection (String sectionName)
        throws ConfigurationException,
               CurnException
    {
        // Get the required class name.

        String                  className;
        ConfiguredOutputHandler handlerWrapper;
        MetaPlugIn              metaPlugIn = MetaPlugIn.getMetaPlugIn();
        boolean                 keep = true;

        className = getConfigurationValue (sectionName, VAR_CLASS);
        handlerWrapper = new ConfiguredOutputHandler (sectionName,
                                                      sectionName,
                                                      className);

        keep = metaPlugIn.runOutputHandlerConfigItemPlugIn (sectionName,
                                                            VAR_CLASS,
                                                            this,
                                                            handlerWrapper);
        if (keep)
        {
            for (String variableName : getVariableNames (sectionName))
            {
                // Skip the ones we've already processed.

                if (variableName.equals (VAR_CLASS))
                    continue;

                String value = getConfigurationValue (sectionName,
                                                      variableName);
                handlerWrapper.addExtraVariable (variableName, value);

                keep = metaPlugIn.runOutputHandlerConfigItemPlugIn
                                                             (sectionName,
                                                              variableName,
                                                              this,
                                                              handlerWrapper);
                if (! keep)
                {
                    log.debug ("A plug-in has disabled output handler ["
                             + sectionName
                             + "]");
                    break;
                }
            }

            if (keep)
            {
                log.debug ("Saving output handler \""
                     + handlerWrapper.getName()
                     + "\" of type "
                     + handlerWrapper.getClassName());
                outputHandlers.add (handlerWrapper);
            }
        }
    }

    /**
     * Process an unknown section (passing its values to the plug-ins).
     *
     * @param sectionName  the section name
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other error
     */
    private void processUnknownSection (String sectionName)
        throws ConfigurationException,
               CurnException
    {
        for (String varName : getVariableNames (sectionName))
        {
            String value = getConfigurationValue (sectionName, varName);
            if (value != null)
            {
                MetaPlugIn.getMetaPlugIn().runUnknownSectionConfigItemPlugIn
                    (sectionName, varName, this);
            }
        }
    }

    /**
     * Parse an optional MaxDaysToCache parameter.
     *
     * @param sectionName   the section name
     * @param variableName  the variable name
     * @param def           the default
     *
     * @return the value
     *
     * @throws NoSuchSectionException no such section
     * @throws ConfigurationException bad numeric value
     */
    private int parseMaxDaysParameter (String sectionName,
                                       String variableName,
                                       int    def)
        throws NoSuchSectionException,
               ConfigurationException
    {
        int result = def;
        String value = getOptionalStringValue (sectionName,
                                               variableName,
                                               null);
        if (value != null)
        {
            if (value.equalsIgnoreCase (NO_LIMIT_VALUE))
                result = Integer.MAX_VALUE;

            else
            {
                try
                {
                    result = Integer.parseInt (value);
                }

                catch (NumberFormatException ex)
                {
                    throw new ConfigurationException
                                         (Constants.BUNDLE_NAME,
                                          "CurnConfig.badNumericValue",
                                          "Bad numeric value \"{0}\" for "
                                        + "variable \"{1}\" in section "
                                        + "\"{2}\"",
                                          new Object[]
                                          {
                                              value,
                                              variableName,
                                              sectionName
                                          });
                }

                if (result < 0)
                {
                    throw new ConfigurationException
                                      (Constants.BUNDLE_NAME,
                                       "CurnConfig.negativeCardinalValue",
                                       "Unexpected negative numeric value "
                                     + "{0} for variable \"{1}\" in section "
                                     + "\"{2}\"",
                                       new Object[]
                                       {
                                           value,
                                           variableName,
                                           sectionName
                                       });
                }
            }
        }

        return result;
    }
}
