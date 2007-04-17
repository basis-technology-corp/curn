/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M.a Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
    public static final String VAR_NO_CACHE_UPDATE   = "NoCacheUpdate";
    public static final String VAR_MAIL_SUBJECT      = "Subject";
    public static final String VAR_DAYS_TO_CACHE     = "DaysToCache";
    public static final String VAR_PARSER_CLASS_NAME = "ParserClass";
    public static final String VAR_SHOW_RSS_VERSION  = "ShowRSSVersion";
    public static final String VAR_FEED_URL          = "URL";
    public static final String VAR_CLASS             = "Class";
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
    public static final boolean DEF_SAVE_ONLY         = false;
    public static final String  DEF_PARSER_CLASS_NAME =
        "org.clapper.curn.parser.rome.RSSParserAdapter";
    public static final int     DEF_MAX_THREADS       = 5;

    /**
     * Others
     */
    public static final String NO_LIMIT_VALUE = "NoLimit";

    /**
     * Main section name
     */
    public static final String MAIN_SECTION = "curn";

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /**
     * Prefix for sections that describing individual feeds.
     */
    private static final String FEED_SECTION_PREFIX = "Feed";

    /**
     * Prefix for output handler sections.
     */
    private static final String OUTPUT_HANDLER_PREFIX = "OutputHandler";

    /**
     * Original default parser; mapped to new default, for backward
     * compatibility.
     */
    private static final String OLD_DEF_PARSER_CLASS_NAME =
        "org.clapper.curn.parser.minirss.MiniRSSParser";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private int defaultCacheDays = DEF_DAYS_TO_CACHE;
    private boolean updateCache = true;
    private boolean showRSSFormat = false;
    private Collection<FeedInfo> feeds = new ArrayList<FeedInfo>();
    private Map<URL,FeedInfo> feedMap = new HashMap<URL,FeedInfo>();
    private String parserClassName = DEF_PARSER_CLASS_NAME;
    private List<ConfiguredOutputHandler> outputHandlers
                                 = new ArrayList<ConfiguredOutputHandler>();
    private int maxThreads = DEF_MAX_THREADS;
    private PrintWriter err;

    /**
     * For log messages
     */
    private static final Logger log = new Logger(CurnConfig.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct an <tt>CurnConfig</tt> object. You must call one of the
     * {@link #load(File)} methods to load the configuration.
     *
     * @param err where to write errors
     */
    CurnConfig(PrintWriter err)
    {
        super();
        this.err = err;
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
        return Collections.unmodifiableList(outputHandlers);
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
     * Determine whether the cache should be updated.
     * 
     * @return <tt>true</tt> if the cache should be updated, <tt>false</tt>
     *         if it should not.
     * @see #setMustUpdateFeedMetadata
     */
    public boolean mustUpdateFeedMetadata()
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
    public void setMaxThreads(final int newValue)
        throws ConfigurationException
    {
        if (newValue <= 0)
        {
            throw new ConfigurationException(Constants.BUNDLE_NAME,
                                             "CurnConfig.badPositiveInteger",
                                             "The \"{0}\" configuration " +
                                             "parameter cannot be set to " +
                                             "{1}. It must have a positive " +
                                             "integer value.",
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
     * @see #mustUpdateFeedMetadata
     */
    public void setMustUpdateFeedMetadata(final boolean val)
    {
        updateCache = val;
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
    public void setShowRSSVersionFlag(final boolean val)
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
     * @see #getFeedInfoMap
     */
    public Collection<FeedInfo> getFeeds()
    {
        return Collections.unmodifiableCollection(feeds);
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
     * @see #getFeedInfoMap()
     */
    public boolean hasFeed(final URL url)
    {
        return feedMap.containsKey(url.toString());
    }

    /**
     * Get the {@link FeedInfo} map.
     *
     * @return A <tt>Map</tt> of {@link FeedInfo} objects, indexed by
     *         channel (or feed) URL.
     *
     * @see #getFeeds
     * @see #hasFeed
     * @see FeedInfo
     */
    public Map<URL,FeedInfo> getFeedInfoMap()
    {
        return feedMap;
    }

    /**
     * Utility method that retrieves a "deprecated parameter" warning.
     *
     * @param badParam   the deprecated parameter
     * @param goodParam  the parameter that should be used, or null for none
     *
     * @return the message
     */
    public String getDeprecatedParamMessage(final String badParam,
                                            final String goodParam)
    {
        StringBuilder buf = new StringBuilder();

        buf.append ("Warning: Configuration file ");

        URL configURL = getConfigurationFileURL();
        if (configURL != null)
        {
            buf.append('"');
            buf.append(configURL.toString());
            buf.append('"');
        }

        buf.append(" uses deprecated \"");
        buf.append(badParam);
        buf.append("\" parameter");

        if (goodParam == null)
            buf.append(".");

        else
        {
            buf.append(", instead of new \"");
            buf.append(goodParam);
            buf.append("\" parameter.");
        }

        return buf.toString();
    }
    /**
     * Load configuration from a path. Any existing data is discarded.
     *
     * @param path the path
     *
     * @throws IOException            read error
     * @throws ConfigurationException parse error
     */
    public void load(final String path)
        throws FileNotFoundException,
               IOException,
               ConfigurationException
    {
        super.load(path);
        try
        {
            validate();
        }

        catch (CurnException ex)
        {
            throw new ConfigurationException (ex);
        }
    }

    /**
     * Load configuration from an open <tt>InputStream</tt>.
     * Any existing data is discarded.
     *
     * @param iStream open input stream
     *
     * @throws IOException            read error
     * @throws ConfigurationException parse error
     */
    public void load(final InputStream iStream)
        throws IOException,
               ConfigurationException
    {
        super.load(iStream);
        try
        {
            validate();
        }

        catch (CurnException ex)
        {
            throw new ConfigurationException (ex);
        }
    }

    /**
     * Load configuration from a URL. Any existing data is discarded.
     *
     * @param url  the URL
     *
     * @throws IOException            read error
     * @throws ConfigurationException parse error
     */
    public void load(final URL url)
        throws IOException,
               ConfigurationException
    {
        super.load(url);
        try
        {
            validate();
        }

        catch (CurnException ex)
        {
            throw new ConfigurationException (ex);
        }
    }

    /**
     * Load configuration from a file. Any existing data is discarded.
     *
     * @param file the file
     *
     * @throws IOException            read error
     * @throws ConfigurationException parse error
     */
    public void load(final File file)
        throws IOException,
               ConfigurationException
    {
        super.load(file);
        try
        {
            validate();
        }

        catch (CurnException ex)
        {
            throw new ConfigurationException (ex);
        }
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
            if (sectionName.startsWith(FEED_SECTION_PREFIX))
                processFeedSection(sectionName);

            else if (sectionName.startsWith(OUTPUT_HANDLER_PREFIX))
                processOutputHandlerSection(sectionName);

            else
                processUnknownSection(sectionName);
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
            throw new ConfigurationException(Constants.BUNDLE_NAME,
                                             "CurnConfig.missingReqSection",
                                             "The configuration file is " +
                                             "missing the required \"{0}\" " +
                                             "section.",
                                             new Object[] {MAIN_SECTION});
        }

        for (String varName : getVariableNames(MAIN_SECTION))
        {
            try
            {
                processMainSectionVariable(varName);
            }

            catch (NoSuchVariableException ex)
            {
                throw new ConfigurationException
                    (Constants.BUNDLE_NAME, "CurnConfig.missingReqVar",
                     "The configuration file is missing required variable " +
                     "\"{0}\" in section\"{1}\".",
                     new Object[]
                     {
                         ex.getVariableName(),
                         ex.getSectionName()
                     },
                     ex);
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
    private void processMainSectionVariable(final String varName)
        throws ConfigurationException,
               CurnException
    {
        String val = null;

        if (varName.equals(VAR_DAYS_TO_CACHE))
        {
            defaultCacheDays = parseMaxDaysParameter(MAIN_SECTION,
                                                     varName,
                                                     DEF_DAYS_TO_CACHE);
            val = String.valueOf(defaultCacheDays);
        }

        else if (varName.equals(VAR_NO_CACHE_UPDATE))
        {
            updateCache = (!getOptionalBooleanValue(MAIN_SECTION,
                                                    varName,
                                                    DEF_NO_CACHE_UPDATE));
            val = String.valueOf(updateCache);
        }

        else if (varName.equals(VAR_SHOW_RSS_VERSION))
        {
            showRSSFormat = getOptionalBooleanValue(MAIN_SECTION,
                                                    varName,
                                                    DEF_SHOW_RSS_VERSION);
            val = String.valueOf(showRSSFormat);
        }

        else if (varName.equals(VAR_PARSER_CLASS_NAME))
        {
            parserClassName = getOptionalStringValue(MAIN_SECTION,
                                                     varName,
                                                     DEF_PARSER_CLASS_NAME);

            // Backward compatibility hack.

            if (parserClassName.equals(OLD_DEF_PARSER_CLASS_NAME))
            {
                StringBuilder buf = new StringBuilder();
                buf.append("Warning: The \"");
                buf.append(parserClassName);
                buf.append("\" RSS parser class is deprecated. Using \"");
                buf.append(DEF_PARSER_CLASS_NAME);
                buf.append("\" instead.");
                parserClassName = DEF_PARSER_CLASS_NAME;

                String msg = buf.toString();
                err.println(msg);
                log.warn(msg);
            }

            val = String.valueOf(parserClassName);
        }

        else if (varName.equals(VAR_MAX_THREADS))
        {
            int maxThreads = getOptionalCardinalValue(MAIN_SECTION,
                                                      varName,
                                                      DEF_MAX_THREADS);
            setMaxThreads(maxThreads);
            val = String.valueOf(maxThreads);
        }

        else
        {
            val = getOptionalStringValue(MAIN_SECTION, varName, null);
        }

        if (val != null)
        {
            MetaPlugIn.getMetaPlugIn().runMainConfigItemPlugIn(MAIN_SECTION,
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
    private void processFeedSection(final String sectionName)
        throws ConfigurationException,
               CurnException
    {
        FeedInfo   feedInfo = null;
        String     feedURLString = null;
        URL        url = null;
        MetaPlugIn metaPlugIn = MetaPlugIn.getMetaPlugIn();
        boolean    keepFeed = false;

        feedURLString = getConfigurationValue(sectionName, VAR_FEED_URL);

        try
        {
            url = CurnUtil.normalizeURL(feedURLString);
            String urlString = url.toString();
            log.debug("Configured feed: URL=\"" + urlString + "\"");
            feedInfo = new FeedInfo(url);
            metaPlugIn.runFeedConfigItemPlugIn(sectionName,
                                               VAR_FEED_URL,
                                               this,
                                               feedInfo);
        }

        catch (MalformedURLException ex)
        {
            throw new ConfigurationException(Constants.BUNDLE_NAME,
                                             "CurnConfig.badFeedURL",
                                             "Configuration file section " +
                                             "\"{0}\" specifies a bad RSS " +
                                             "feed URL \"{1}\"",
                                             new Object[]
                                              {
                                                  sectionName,
                                                  feedURLString
                                              });
        }


        feedInfo.setDaysToCache(defaultCacheDays);

        for (String varName : getVariableNames(sectionName))
        {
            String value   = null;

            if (varName.equals(VAR_DAYS_TO_CACHE))
            {
                int maxDays = parseMaxDaysParameter(sectionName,
                                                    varName,
                                                    defaultCacheDays);
                feedInfo.setDaysToCache(maxDays);
                value = String.valueOf(maxDays);
            }

            else if (varName.equals(VAR_FORCE_ENCODING) ||
                     varName.equals(VAR_FORCE_CHAR_ENCODING))
            {
                value = getConfigurationValue(sectionName, varName);
                feedInfo.setForcedCharacterEncoding(value);
            }

            else
            {
                value = getConfigurationValue(sectionName, varName);
            }

            if (value != null)
            {
                keepFeed = metaPlugIn.runFeedConfigItemPlugIn(sectionName,
                                                              varName,
                                                              this,
                                                              feedInfo);
                if (! keepFeed)
                {
                    log.debug("A plug-in said to skip feed [" +
                              sectionName + "\"");
                }
            }

            if (! keepFeed)
                break;
        }

        if (url == null)
        {
            throw new ConfigurationException(Constants.BUNDLE_NAME,
                                             "CurnConfig.missingReqVar",
                                             "The configuration file is " +
                                             "missing required variable " +
                                             "\"{0}\" in section \"{1}\"",
                                             new Object[]
                                             {
                                                 VAR_FEED_URL,
                                                 sectionName
                                             });
        }

        if (keepFeed)
        {
            feeds.add(feedInfo);
            feedMap.put(url, feedInfo);
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
    private void processOutputHandlerSection(final String sectionName)
        throws ConfigurationException,
               CurnException
    {
        // Get the required class name.

        String                  className;
        ConfiguredOutputHandler handlerWrapper;
        MetaPlugIn              metaPlugIn = MetaPlugIn.getMetaPlugIn();
        boolean                 keep = true;

        className = getConfigurationValue(sectionName, VAR_CLASS);
        handlerWrapper = new ConfiguredOutputHandler(sectionName,
                                                     sectionName,
                                                     className);

        keep = metaPlugIn.runOutputHandlerConfigItemPlugIn(sectionName,
                                                           VAR_CLASS,
                                                           this,
                                                           handlerWrapper);
        if (keep)
        {
            for (String variableName : getVariableNames(sectionName))
            {
                // Skip the ones we've already processed.

                if (variableName.equals(VAR_CLASS))
                    continue;

                String value = getConfigurationValue(sectionName,
                                                     variableName);
                handlerWrapper.addExtraVariable(variableName, value);

                keep =
                    metaPlugIn.runOutputHandlerConfigItemPlugIn
                        (sectionName, variableName, this, handlerWrapper);

                if (! keep)
                {
                    log.debug("A plug-in has disabled output handler [" +
                              sectionName + "]");
                    break;
                }
            }

            if (keep)
            {
                log.debug("Saving output handler \"" +
                          handlerWrapper.getName() +
                          "\" of type " +
                           handlerWrapper.getClassName());
                outputHandlers.add(handlerWrapper);
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
    private void processUnknownSection(final String sectionName)
        throws ConfigurationException,
               CurnException
    {
        for (String varName : getVariableNames(sectionName))
        {
            String value = getConfigurationValue(sectionName, varName);
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
    private int parseMaxDaysParameter(final String sectionName,
                                      final String variableName,
                                      final int    def)
        throws NoSuchSectionException,
               ConfigurationException
    {
        int result = def;
        String value = getOptionalStringValue(sectionName,
                                              variableName,
                                              null);
        if (value != null)
        {
            if (value.equalsIgnoreCase(NO_LIMIT_VALUE))
                result = Integer.MAX_VALUE;

            else
            {
                try
                {
                    result = Integer.parseInt(value);
                }

                catch (NumberFormatException ex)
                {
                    throw new ConfigurationException
                                         (Constants.BUNDLE_NAME,
                                          "CurnConfig.badNumericValue",
                                          "Bad numeric value \"{0}\" for " +
                                          "variable \"{1}\" in section " +
                                          "\"{2}\"",
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
                                       "Unexpected negative numeric value " +
                                       "{0} for variable \"{1}\" in section " +
                                       "\"{2}\"",
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
