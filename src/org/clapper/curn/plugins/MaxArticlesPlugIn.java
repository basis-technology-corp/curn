/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. The end-user documentation included with the redistribution, if any,
     must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

     Alternately, this acknowlegement may appear in the software itself,
     if wherever such third-party acknowlegements normally appear.

  3. Neither the names "clapper.org", "curn", nor any of the names of the
     project contributors may be used to endorse or promote products
     derived from this software without prior written permission. For
     written permission, please contact bmc@clapper.org.

  4. Products derived from this software may not be called "curn", nor may
     "clapper.org" appear in their names without prior written permission
     of Brian M. Clapper.

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

package org.clapper.curn.plugins;

import org.clapper.curn.Constants;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.FeedConfigItemPlugIn;
import org.clapper.curn.MainConfigItemPlugIn;
import org.clapper.curn.PostFeedProcessPlugIn;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import org.clapper.curn.FeedCache;

/**
 * The <tt>MaxArticlesPlugIn</tt> can be used to set an upper limit on the
 * number of articles displayed for a feed (or for all feeds).
 * It looks for a default (main-configuration section) "MaxArticlesToShow"
 * parameter, and permits a per-feed "MaxArticlesToShow" parameter to override
 * the default. This plug-in deliberately uses a non-typical sort key to force
 * it to run <i>after</i> other stock plug-ins in the "post feed parse" phase.
 * Thus, this plug-in doesn't apply its maximum threshold test until
 * <i>after</i>:
 *
 * <ul>
 *   <li> the articles are sorted (see {@link SortArticlesPlugIn})
 *   <li> any article retention policy is applied (see
 *        {@link RetainArticlesPlugIn})
 *   <li> any "article ignore" policy is applied (see
 *        {@link IgnoreOldArticlesPlugIn})
 * </ul>
 *
 *
 * <p>This plug-in intercepts the following configuration parameters.</p>
*
 * <table border="1">
 *   <tr valign="top" align="left">
 *     <th>Section</th>
 *     <th>Parameter</th>
 *     <th>Meaning</th>
 *     <th>Default</th>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[curn]</tt></td>
 *     <td><tt>MaxArticlesToShow</tt></td>
 *     <td>Global default specifying the maximum number of articles to show
 *         per feed. Applies to all feeds that don't explicitly override this
 *         parameter.</td>
 *     <td>None. (All articles are shown, subject to actions by other
 *         plug-ins.)</td>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[Feed<i>xxx</i>]</tt></td>
 *     <td><tt>MaxArticlesToShow</tt></td>
 *     <td>Per-feed parameter specifying the maximum number of articles to
 *         show from the feed.</td>
 *     <td>The global <tt>MaxArticlesToShow</tt> setting. If there is
 *         no global setting, then the default to show all articles in the
 *         feed (subject to actions by other plug-ins).</td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class MaxArticlesPlugIn
    implements MainConfigItemPlugIn,
               FeedConfigItemPlugIn,
               PostFeedProcessPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_MAX_ARTICLES = "MaxArticlesToShow";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed sort-by data, by feed
     */
    private Map<URL,Integer> perFeedMaxArticlesMap =
        new HashMap<URL,Integer>();

    /**
     * Default sort-by value
     */
    private Integer defaultMaxArticlesToShow = null;

    /**
     * For log messages
     */
    private static final Logger log = new Logger(MaxArticlesPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public MaxArticlesPlugIn()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
               Public Methods Required by *PlugIn Interfaces
    \*----------------------------------------------------------------------*/

    /**
     * Get a displayable name for the plug-in.
     *
     * @return the name
     */
    public String getPlugInName()
    {
        return "Max Articles";
    }

    /**
     * Get the sort key for this plug-in.
     *
     * @return the sort key string.
     */
    public String getPlugInSortKey()
    {
        // Unlike most plug-ins, use a sort key that forces this plug-in
        // to run after the other stock plug-ins.

        StringBuilder key = new StringBuilder();
        key.append("ZZZZZ.");
        key.append(ClassUtil.getShortClassName(getClass().getName()));

        return key.toString();
    }

    /**
     * Initialize the plug-in. This method is called before any of the
     * plug-in methods are called.
     *
     * @throws CurnException on error
     */
    public void initPlugIn()
        throws CurnException
    {
    }

    /**
     * Called immediately after <i>curn</i> has read and processed a
     * configuration item in the main [curn] configuration section. All
     * configuration items are passed, one by one, to each loaded plug-in.
     * If a plug-in class is not interested in a particular configuration
     * item, this method should simply return without doing anything. Note
     * that some configuration items may simply be variable assignment;
     * there's no real way to distinguish a variable assignment from a
     * blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the {@link CurnConfig} object
     *
     * @throws CurnException on error
     *
     * @see CurnConfig
     */
    public void runMainConfigItemPlugIn(String     sectionName,
                                        String     paramName,
                                        CurnConfig config)
        throws CurnException
    {
        try
        {
            if (paramName.equals(VAR_MAX_ARTICLES))
            {
                int val = config.getRequiredCardinalValue(sectionName,
                                                          paramName);
                if (val <= 0)
                {
                    throw new ConfigurationException
                        (Constants.BUNDLE_NAME, "CurnConfig.badVarValue",
                         "Section \"{0}\" in the configuration file has a bad " +
                         "value (\"{1}\") for the \"{2}\" parameter",
                         new Object[]
                         {
                             sectionName,
                             String.valueOf(val),
                             VAR_MAX_ARTICLES
                         });
                }

                defaultMaxArticlesToShow = val;
            }
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException(ex);
        }
    }

    /**
     * Called immediately after <i>curn</i> has read and processed a
     * configuration item in a "feed" configuration section. All
     * configuration items are passed, one by one, to each loaded plug-in.
     * If a plug-in class is not interested in a particular configuration
     * item, this method should simply return without doing anything. Note
     * that some configuration items may simply be variable assignment;
     * there's no real way to distinguish a variable assignment from a
     * blessed configuration item.
     *
     * @param sectionName  the name of the configuration section where
     *                     the item was found
     * @param paramName    the name of the parameter
     * @param config       the active configuration
     * @param feedInfo     partially complete <tt>FeedInfo</tt> object
     *                     for the feed. The URL is guaranteed to be
     *                     present, but no other fields are.
     *
     * @return <tt>true</tt> to continue processing the feed,
     *         <tt>false</tt> to skip it
     *
     * @throws CurnException on error
     *
     * @see CurnConfig
     * @see FeedInfo
     * @see FeedInfo#getURL
     */
    public boolean runFeedConfigItemPlugIn(String     sectionName,
                                           String     paramName,
                                           CurnConfig config,
                                           FeedInfo   feedInfo)
        throws CurnException
    {
        try
        {
            if (paramName.equals(VAR_MAX_ARTICLES))
            {
                int val = config.getRequiredCardinalValue(sectionName,
                                                          paramName);
                if (val <= 0)
                {
                    throw new ConfigurationException
                        (Constants.BUNDLE_NAME, "CurnConfig.badVarValue",
                         "Section \"{0}\" in the configuration file has a bad " +
                         "value (\"{1}\") for the \"{2}\" parameter",
                         new Object[]
                         {
                             sectionName,
                             String.valueOf(val),
                             VAR_MAX_ARTICLES
                         });
                }

                URL feedURL = feedInfo.getURL();
                perFeedMaxArticlesMap.put(feedURL, val);
                log.debug(feedURL + ": " + VAR_MAX_ARTICLES + "=" + val);
            }

            return true;
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException(ex);
        }
    }

    /**
     * Called immediately after a feed is parsed, but before it is
     * otherwise processed. This method can return <tt>false</tt> to signal
     * <i>curn</i> that the feed should be skipped. For instance, a plug-in
     * that filters on the parsed feed data could use this method to weed
     * out non-matching feeds before they are downloaded. Similarly, a
     * plug-in that edits the parsed data (removing or editing individual
     * items, for instance) could use method to do so.
     *
     * @param feedInfo  the {@link FeedInfo} object for the feed that
     *                  has been downloaded and parsed.
     * @param feedCache the feed cache
     * @param channel   the parsed channel data
     *
     * @return <tt>true</tt> if <i>curn</i> should continue to process the
     *         feed, <tt>false</tt> to skip the feed. A return value of
     *         <tt>false</tt> aborts all further processing on the feed.
     *         In particular, <i>curn</i> will not pass the feed along to
     *         other plug-ins that have yet to be notified of this event.
     *
     * @throws CurnException on error
     *
     * @see RSSChannel
     * @see FeedInfo
     */
    public boolean runPostFeedProcessPlugIn(FeedInfo   feedInfo,
                                          FeedCache  feedCache,
                                          RSSChannel channel)
        throws CurnException
    {
        URL feedURL = feedInfo.getURL();
        log.debug("Post feed parse: " + feedURL.toString());

        Integer max = perFeedMaxArticlesMap.get(feedURL);
        if (max == null)
            max = defaultMaxArticlesToShow;

        if (max != null)
        {
            Collection<RSSItem> items  = channel.getItems();
            int totalItems = items.size();
            log.debug("Feed \"" + feedURL + "\": Max articles for feed=" + max);
            log.debug("Feed \"" + feedURL + "\": Total articles=" + totalItems);
            if (totalItems > max)
            {
                log.debug("Feed \"" + feedURL + "\": Trimming articles.");
                int i = 0;
                Collection<RSSItem> trimmedItems = new ArrayList<RSSItem>();
                for (RSSItem item : items)
                {
                    if (i++ >= max)
                        break;

                    trimmedItems.add(item);
                }

                channel.setItems(trimmedItems);
            }
        }

        return true;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/
}
