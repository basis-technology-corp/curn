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

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.net.URL;
import java.net.MalformedURLException;

import org.clapper.curn.util.Util;

import org.clapper.util.misc.Logger;

import org.clapper.util.config.Configuration;
import org.clapper.util.config.NoSuchVariableException;
import org.clapper.util.config.ConfigurationException;

/**
 * <p><tt>ConfigFile</tt> uses the <tt>Configuration</tt> class (part of
 * the <i>clapper.org</i> Java Utility library) to parse and validate the
 * <i>curn</i> configuration file, holding the results in memory for easy
 * access.</p>
 *
 * @version <tt>$Revision$</tt>
 */
public class ConfigFile extends Configuration
{
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

    /**
     * Variable names
     */
    private static final String VAR_CACHE_FILE        = "CacheFile";
    private static final String VAR_NO_CACHE_UPDATE   = "NoCacheUpdate";
    private static final String VAR_SUMMARY_ONLY      = "SummaryOnly";
    private static final String VAR_SMTPHOST          = "SMTPHost";
    private static final String VAR_DEFAULT_MAIL_FROM = "DefaultMailFrom";
    private static final String VAR_MAIL_SUBJECT      = "Subject";
    private static final String VAR_DAYS_TO_CACHE     = "DaysToCache";
    private static final String VAR_PARSER_CLASS_NAME = "ParserClass";
    private static final String VAR_PRUNE_URLS        = "PruneURLs";
    private static final String VAR_SHOW_RSS_VERSION  = "ShowRSSVersion";
    private static final String VAR_SMTP_HOST         = "SMTPHost";
    private static final String VAR_DEF_EMAIL_SENDER  = "MailFrom";
    private static final String VAR_EMAIL_SUBJECT     = "MailSubject";
    private static final String VAR_SHOW_DATES        = "ShowDates";
    private static final String VAR_TITLE_OVERRIDE    = "TitleOverride";
    private static final String VAR_EDIT_ITEM_URL     = "EditItemURL";
    private static final String VAR_PREPARSE_EDIT     = "PreparseEdit";
    private static final String VAR_DISABLED          = "Disabled";
    private static final String VAR_SHOW_AUTHORS      = "ShowAuthors";
    private static final String VAR_SAVE_FEED_AS      = "SaveAs";
    private static final String VAR_SAVE_ONLY         = "SaveOnly";
    private static final String VAR_FEED_URL          = "URL";
    private static final String VAR_CLASS             = "Class";
    private static final String VAR_GET_GZIPPED_FEEDS = "GetGzippedFeeds";
    private static final String VAR_SORT_BY           = "SortBy";
    private static final String VAR_MAX_THREADS       = "MaxThreads";
    private static final String VAR_IGNORE_DUP_TITLES = "IgnoreDuplicateTitles";
    private static final String VAR_FORCE_ENCODING    = "ForceCharacterEncoding";

    /**
     * Legal values
     */
    private static final Map LEGAL_SORT_BY_VALUES = new HashMap();
    static
    {
        LEGAL_SORT_BY_VALUES.put ("none",
                                  new Integer (FeedInfo.SORT_BY_NONE));
        LEGAL_SORT_BY_VALUES.put ("time",
                                  new Integer (FeedInfo.SORT_BY_TIME));
        LEGAL_SORT_BY_VALUES.put ("title",
                                  new Integer (FeedInfo.SORT_BY_TITLE));
    }

    /**
     * Default values
     */
    private static final int     DEF_DAYS_TO_CACHE     = 30;
    private static final boolean DEF_PRUNE_URLS        = false;
    private static final boolean DEF_NO_CACHE_UPDATE   = false;
    private static final boolean DEF_SUMMARY_ONLY      = false;
    private static final boolean DEF_SHOW_RSS_VERSION  = false;
    private static final boolean DEF_SHOW_DATES        = false;
    private static final boolean DEF_SHOW_AUTHORS      = false;
    private static final boolean DEF_GET_GZIPPED_FEEDS = true;
    private static final boolean DEF_SAVE_ONLY         = false;
    private static final String  DEF_SMTP_HOST         = "localhost";
    private static final String  DEF_EMAIL_SUBJECT     = "curn output";
    private static final String  DEF_PARSER_CLASS_NAME =
                             "org.clapper.curn.parser.minirss.MiniRSSParser";
    private static final int     DEF_SORT_BY           = FeedInfo.SORT_BY_NONE;
    private static final int     DEF_MAX_THREADS       = 5;

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private File        cacheFile             = null;
    private int         defaultCacheDays      = DEF_DAYS_TO_CACHE;
    private boolean     updateCache           = true;
    private boolean     summaryOnly           = false;
    private boolean     showRSSFormat         = false;
    private boolean     showDates             = false;
    private Collection  feeds                 = new ArrayList();
    private Map         feedMap               = new HashMap();
    private String      parserClassName       = DEF_PARSER_CLASS_NAME;
    private List        outputHandlers        = new ArrayList();
    private Map         outputHandlerSections = new HashMap();
    private String      smtpHost              = DEF_SMTP_HOST;
    private String      emailSender           = null;
    private String      emailSubject          = DEF_EMAIL_SUBJECT;
    private boolean     showAuthors           = false;
    private boolean     getGzippedFeeds       = true;
    private int         defaultSortBy         = DEF_SORT_BY;
    private int         maxThreads            = DEF_MAX_THREADS;

    /**
     * For log messages
     */
    private static Logger log = new Logger (ConfigFile.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct an <tt>ConfigFile</tt> object that parses data
     * from the specified file.
     *
     * @param f  The <tt>File</tt> to open and parse
     *
     * @throws IOException             can't open or read file
     * @throws ConfigurationException  error in configuration data
     */
    ConfigFile (File f)
        throws IOException,
               ConfigurationException
    {
        super (f);
        validate();
    }

    /**
     * Construct an <tt>ConfigFile</tt> object that parses data
     * from the specified file.
     *
     * @param path  the path to the file to parse
     *
     * @throws FileNotFoundException   specified file doesn't exist
     * @throws IOException             can't open or read file
     * @throws ConfigurationException  error in configuration data
     */
    ConfigFile (String path)
        throws FileNotFoundException,
               IOException,
               ConfigurationException
    {
        super (path);
        validate();
    }

    /**
     * Construct an <tt>ConfigFile</tt> object that parses data
     * from the specified URL.
     *
     * @param url  the URL to open and parse
     *
     * @throws IOException             can't open or read URL
     * @throws ConfigurationException  error in configuration data
     */
    ConfigFile (URL url)
        throws IOException,
               ConfigurationException
    {
        super (url);
        validate();
    }

    /**
     * Construct an <tt>ConfigFile</tt> object that parses data
     * from the specified <tt>InputStream</tt>.
     *
     * @param iStream  the <tt>InputStream</tt>
     *
     * @throws IOException             can't open or read URL
     * @throws ConfigurationException  error in configuration data
     */
    ConfigFile (InputStream iStream)
        throws IOException,
               ConfigurationException
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
    public Collection getOutputHandlers()
    {
        return Collections.unmodifiableList (outputHandlers);
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
            throw new ConfigurationException ("\""
                                            + VAR_MAX_THREADS
                                            + "\" configuration value "
                                            + "cannot be set to "
                                            + String.valueOf (newValue)
                                            + ". It must be a positive "
                                            + "integer.");
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
     * Return the value of the "show authors" flag. This flag controls whether
     * to display the authors associated with each item, if available.
     *
     * @return <tt>true</tt> if "show authors" flag is set, <tt>false</tt>
     *         otherwise
     *
     * @see #setShowAuthorsFlag
     */
    public boolean showAuthors()
    {
        return showAuthors;
    }

    /**
     * Set the value of the "show authors" flag. This flag controls whether
     * to display the authors associated with each item, if available.
     *
     * @param val <tt>true</tt> to set the "show authors" flag, <tt>false</tt>
     *            to clear it
     *
     * @see #showAuthors
     */
    public void setShowAuthorsFlag (boolean val)
    {
        this.showAuthors = val;
    }

    /**
     * Return the value of the "show dates" flag. This flag controls whether
     * to display the dates associated with each feed and item, if available.
     *
     * @return <tt>true</tt> if "show dates" flag is set, <tt>false</tt>
     *         otherwise
     *
     * @see #setShowDatesFlag
     */
    public boolean showDates()
    {
        return showDates;
    }

    /**
     * Set the value of the "show dates" flag. This flag controls whether
     * to display the dates associated with each feed and item, if available.
     *
     * @param val <tt>true</tt> to set the "show dates" flag, <tt>false</tt>
     *            to clear it
     *
     * @see #showDates
     */
    public void setShowDatesFlag (boolean val)
    {
        this.showDates = val;
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
     * Get the SMTP host to use when sending email.
     *
     * @return the SMTP host. Never null.
     *
     * @see #setSMTPHost
     */
    public String getSMTPHost()
    {
        return smtpHost;
    }

    /**
     * Set the SMTP host to use when sending email.
     *
     * @param host the SMTP host, or null to revert to the default value
     *
     * @see #getSMTPHost
     */
    public void setSMTPHost (String host)
    {
        smtpHost = (host == null) ? DEF_SMTP_HOST : host;
    }

    /**
     * Get the email address to use as the sender for email messages.
     *
     * @return the email address, or null if not specified
     *
     * @see #setEmailSender
     */
    public String getEmailSender()
    {
        return emailSender;
    }

    /**
     * Set the email address to use as the sender for email messages.
     *
     * @param address the new address, or null to clear the field
     *
     * @see #getEmailSender
     */
    public void setEmailSender (String address)
    {
        this.emailSender = address;
    }

    /**
     * Get the subject to use in email messages, if email is being sent.
     *
     * @return the subject. Never null.
     *
     * @see #setEmailSubject
     */
    public String getEmailSubject()
    {
        return emailSubject;
    }

    /**
     * Set the subject to use in email messages, if email is being sent.
     *
     * @param subject the subject, or null to reset to the default
     *
     * @see #getEmailSubject
     */
    public void setEmailSubject (String subject)
    {
        this.emailSubject = (subject == null) ? DEF_EMAIL_SUBJECT : subject;
    }

    /**
     * Get the configured RSS feeds.
     *
     * @return a <tt>Collection</tt> of <tt>FeedInfo</tt> objects.
     *
     * @see #hasFeed
     * @see #getFeedInfoFor(String)
     * @see #getFeedInfoFor(URL)
     */
    public Collection getFeeds()
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
     * @throws ConfigurationException on error
     */
    private void validate()
        throws ConfigurationException
    {
        // First, verify that the main section is there and process it.

        processMainSection();

        // Process the remaining sections. Skip ones we don't recognize.

        for (Iterator it = getSectionNames (new ArrayList()).iterator();
             it.hasNext(); )
        {
            String sectionName = (String) it.next();

            if (sectionName.startsWith (FEED_SECTION_PREFIX))
                processFeedSection (sectionName);

            else if (sectionName.startsWith (OUTPUT_HANDLER_PREFIX))
                processOutputHandlerSection (sectionName);
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
            String s;

            s = getOptionalStringValue (MAIN_SECTION, VAR_CACHE_FILE, null);
            if (s != null)
            {
                cacheFile = new File (s);

                if (cacheFile.isDirectory())
                {
                    throw new ConfigurationException ("Specified cache file \""
                                                    + cacheFile.getPath()
                                                    + "\" is a directory.");
                }
            }

            defaultCacheDays = getOptionalIntegerValue (MAIN_SECTION,
                                                        VAR_DAYS_TO_CACHE,
                                                        DEF_DAYS_TO_CACHE);
            updateCache = (!getOptionalBooleanValue (MAIN_SECTION,
                                                     VAR_NO_CACHE_UPDATE,
                                                     DEF_NO_CACHE_UPDATE));
            summaryOnly = getOptionalBooleanValue (MAIN_SECTION,
                                                   VAR_SUMMARY_ONLY,
                                                   DEF_SUMMARY_ONLY);
            showRSSFormat = getOptionalBooleanValue (MAIN_SECTION,
                                                     VAR_SHOW_RSS_VERSION,
                                                     DEF_SHOW_RSS_VERSION);
            showDates = getOptionalBooleanValue (MAIN_SECTION,
                                                 VAR_SHOW_DATES,
                                                 DEF_SHOW_DATES);
            parserClassName = getOptionalStringValue (MAIN_SECTION,
                                                      VAR_PARSER_CLASS_NAME,
                                                      DEF_PARSER_CLASS_NAME);
            smtpHost = getOptionalStringValue (MAIN_SECTION,
                                               VAR_SMTP_HOST,
                                               DEF_SMTP_HOST);
            emailSender = getOptionalStringValue (MAIN_SECTION,
                                                  VAR_DEF_EMAIL_SENDER,
                                                  null);
            emailSubject = getOptionalStringValue (MAIN_SECTION,
                                                   VAR_EMAIL_SUBJECT,
                                                   DEF_EMAIL_SUBJECT);
            showAuthors = getOptionalBooleanValue (MAIN_SECTION,
                                                   VAR_SHOW_AUTHORS,
                                                   DEF_SHOW_AUTHORS);
            getGzippedFeeds = getOptionalBooleanValue (MAIN_SECTION,
                                                       VAR_GET_GZIPPED_FEEDS,
                                                       DEF_GET_GZIPPED_FEEDS);

            s = getOptionalStringValue (MAIN_SECTION, VAR_SORT_BY, null);
            defaultSortBy = (s == null) ? DEF_SORT_BY
                                        : parseSortByValue (MAIN_SECTION, s);

            setMaxThreads (getOptionalIntegerValue (MAIN_SECTION,
                                                    VAR_MAX_THREADS,
                                                    DEF_MAX_THREADS));
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
     * Process a section that identifies an RSS feed to be polled.
     *
     * @param sectionName  the section name
     *
     * @throws ConfigurationException  configuration error
     */
    private void processFeedSection (String sectionName)
        throws ConfigurationException
    {
        FeedInfo    feedInfo = null;
        String      feedURLString = null;
        Collection  varNames = getVariableNames (sectionName, new ArrayList());
        Collection  preparseEditCommands = new ArrayList();
        String      s;
        URL         url = null;

        feedURLString = getConfigurationValue (sectionName, VAR_FEED_URL);

        try
        {
            url = Util.normalizeURL (feedURLString);
            log.debug ("Configured feed: URL=\"" + url.toString() + "\"");
            feedInfo = new FeedInfo (url);
        }

        catch (MalformedURLException ex)
        {
            throw new ConfigurationException ("Bad RSS site URL: \""
                                            + feedURLString
                                            + "\"");
        }

        feedInfo.setPruneURLsFlag (DEF_PRUNE_URLS);
        feedInfo.setDaysToCache (defaultCacheDays);
        feedInfo.setSummarizeOnlyFlag (summaryOnly);
        feedInfo.setSortBy (defaultSortBy);

        for (Iterator it = varNames.iterator(); it.hasNext(); )
        {
            String varName = (String) it.next();

            if (varName.equals (VAR_DAYS_TO_CACHE))
            {
                feedInfo.setDaysToCache
                    (getRequiredIntegerValue (sectionName, VAR_DAYS_TO_CACHE));
            }

            else if (varName.equals (VAR_PRUNE_URLS))
            {
                feedInfo.setPruneURLsFlag
                    (getRequiredBooleanValue (sectionName, VAR_PRUNE_URLS));
            }

            else if (varName.equals (VAR_SUMMARY_ONLY))
            {
                feedInfo.setSummarizeOnlyFlag
                    (getRequiredBooleanValue (sectionName, VAR_SUMMARY_ONLY));
            }

            else if (varName.equals (VAR_DISABLED))
            {
                feedInfo.setEnabledFlag
                    (! getRequiredBooleanValue (sectionName, VAR_DISABLED));
            }

            else if (varName.equals (VAR_IGNORE_DUP_TITLES))
            {
                feedInfo.setIgnoreItemsWithDuplicateTitlesFlag
                    (getRequiredBooleanValue (sectionName,
                                              VAR_IGNORE_DUP_TITLES));
            }

            else if (varName.equals (VAR_TITLE_OVERRIDE))
            {
                feedInfo.setTitleOverride
                    (getConfigurationValue (sectionName, VAR_TITLE_OVERRIDE));
            }

            else if (varName.equals (VAR_EDIT_ITEM_URL))
            {
                feedInfo.setItemURLEditCommand
                    (getConfigurationValue (sectionName, VAR_EDIT_ITEM_URL));
            }

            else if (varName.equals (VAR_SAVE_FEED_AS))
            {
                s = getConfigurationValue (sectionName, VAR_SAVE_FEED_AS);
                feedInfo.setSaveAsFile (new File (s));
            }

            else if (varName.equals (VAR_SAVE_ONLY))
            {
                feedInfo.setSaveOnlyFlag
                    (getRequiredBooleanValue (sectionName, VAR_SAVE_ONLY));
                log.debug ("Feed save-only=" + feedInfo.saveOnly());
            }

            else if (varName.equals (VAR_FORCE_ENCODING))
            {
                feedInfo.setForcedCharacterEncoding
                    (getConfigurationValue (sectionName, VAR_FORCE_ENCODING));
            }

            else if (varName.equals (VAR_SORT_BY))
            {
                s = getConfigurationValue (sectionName, VAR_SORT_BY);
                feedInfo.setSortBy (parseSortByValue (sectionName, s));
            }

            else if (varName.startsWith (VAR_PREPARSE_EDIT))
            {
                s = getConfigurationValue (sectionName, varName);
                preparseEditCommands.add (s);
            }
        }

        if (preparseEditCommands.size() > 0)
        {
            String[] cmds = new String[preparseEditCommands.size()];
            cmds = (String[]) preparseEditCommands.toArray (cmds);
            feedInfo.setPreparseEditCommands (cmds);
        }

        if (url == null)
        {
            throw new ConfigurationException ("Section ["
                                            + sectionName
                                            + "]: Missing value for "
                                            + "required \""
                                            + VAR_FEED_URL
                                            + "\" parameter.");
        }

        feeds.add (feedInfo);
        feedMap.put (url.toString(), feedInfo);
    }

    /**
     * Process a section that identifies an output handler.
     *
     * @param sectionName  the section name
     *
     * @throws ConfigurationException  configuration error
     */
    private void processOutputHandlerSection (String sectionName)
        throws ConfigurationException
    {
        // Get the required class name.

        String                   className;
        ConfiguredOutputHandler  handlerWrapper;

        className = getConfigurationValue (sectionName, VAR_CLASS);
        handlerWrapper = new ConfiguredOutputHandler (sectionName,
                                                      sectionName,
                                                      className);

        // Only process the rest if it's not disabled.

        if (! getOptionalBooleanValue (sectionName, VAR_DISABLED, false))
        {
            Iterator it = getVariableNames (sectionName, new ArrayList())
                        .iterator();

            while (it.hasNext())
            {
                String variableName = (String) it.next();

                // Skip the ones we've already processed.

                if (variableName.equals (VAR_DISABLED))
                    continue;

                if (variableName.equals (VAR_CLASS))
                    continue;

                String value = getConfigurationValue (sectionName,
                                                      variableName);
                handlerWrapper.addExtraVariable (variableName, value);
            }

            log.debug ("Saving output handler \""
                     + handlerWrapper.getName()
                     + "\" of type "
                     + handlerWrapper.getClassName());
            outputHandlers.add (handlerWrapper);
        }
    }

    /**
     * Parse a "SortBy" value.
     *
     * @param sectionName section name (for error messages)
     * @param value       the value from the config
     *
     * @return the value, or the appropriate default
     *
     * @throws ConfigurationException bad value for config item
     */
    private int parseSortByValue (String sectionName, String value)
        throws ConfigurationException
    {
        Integer val = (Integer) LEGAL_SORT_BY_VALUES.get (value);

        if (val == null)
        {
            throw new ConfigurationException ("Section ["
                                            + sectionName
                                            + "]: Bad value \""
                                            + value
                                            + "\" for \""
                                            + VAR_SORT_BY
                                            + "\" parameter.");
        }

        return val.intValue();
    }
}
