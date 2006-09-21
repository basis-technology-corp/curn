/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2006 Brian M. Clapper."

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

package org.clapper.curn.plugins;

import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.FeedConfigItemPlugIn;
import org.clapper.curn.PostFeedParsePlugIn;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.RSSLink;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import org.clapper.curn.FeedMetaDataRegistry;

/**
 * The <tt>IgnoreDuplicateArticlesPlugIn</tt> handles removing duplicate
 * items from downloaded feeds, where "duplicate" means "has the same
 * title". It intercepts the following per-feed configuration parameters:
 *
 * <table border="1">
 *   <tr valign="top" align="left">
 *     <th>Parameter</th>
 *     <th>Meaning</th>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>IgnoreDuplicateTitles</tt></td>
 *     <td>Set to "true" to strip duplicate titles, "false" to pass them
 *         along. Defaults to "false".</td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class IgnoreDuplicateArticlesPlugIn
    implements FeedConfigItemPlugIn,
               PostFeedParsePlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_IGNORE_DUP_TITLES =
        "IgnoreDuplicateTitles";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed "ignore" flags, by feed
     */
    private Map<FeedInfo,Boolean> perFeedIgnoreFlagMap =
        new HashMap<FeedInfo,Boolean>();

    /**
     * For log messages
     */
    private static final Logger log =
        new Logger (IgnoreDuplicateArticlesPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public IgnoreDuplicateArticlesPlugIn()
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
    public String getName()
    {
        return "Ignore Duplicate Articles";
    }

    /**
     * Get the sort key for this plug-in.
     *
     * @return the sort key string.
     */
    public String getSortKey()
    {
        return ClassUtil.getShortClassName (getClass().getName());
    }

    /**
     * Initialize the plug-in. This method is called before any of the
     * plug-in methods are called; it gives the plug-in the chance to register
     * itself as a <tt>FeedMetaDataClient}</tt>, which allows the plug-in to
     * save and restore its own feed-related metadata from the persistent feed
     * metadata store. A plug-in that isn't interested in saving and restoring
     * data can simply ignore the registry.
     *
     * @param metaDataRegistry  the {@link FeedMetaDataRegistry}
     *
     * @throws CurnException on error
     */
    public void init(FeedMetaDataRegistry metaDataRegistry)
        throws CurnException
    {
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
    public boolean runFeedConfigItemPlugIn (String     sectionName,
                                            String     paramName,
                                            CurnConfig config,
                                            FeedInfo   feedInfo)
        throws CurnException
    {
        try
        {
            if (paramName.equals (VAR_IGNORE_DUP_TITLES))
            {
                boolean flag = config.getRequiredBooleanValue (sectionName,
                                                               paramName);
                perFeedIgnoreFlagMap.put (feedInfo, flag);
                log.debug ("[" + sectionName + "]: " + paramName +
                           "=" + flag);
            }

            return true;
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
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
    public boolean runPostFeedParsePlugIn (FeedInfo   feedInfo,
                                           RSSChannel channel)
        throws CurnException
    {
        Boolean ignore = perFeedIgnoreFlagMap.get (feedInfo);
        if ((ignore != null) && (ignore))
        {
            String feedURL = feedInfo.getURL().toString();
            log.debug ("Stripping duplicate titles from " + feedURL);
            Map<String,RSSItem> titlesSeen = new HashMap<String,RSSItem>();

            for (RSSItem item : channel.getItems())
            {
                RSSLink itemLink   = item.getURL();
                String  strItemURL = itemLink.getURL().toString();
                String  title      = item.getTitle();
                String  titleKey;

                if (title == null)
                    titleKey = strItemURL;

                else
                {
                    // Convert to lower case and consolidate multiple
                    // adjacent white space characters.

                    titleKey = title.toLowerCase().replaceAll ("\\s+", " ");
                }

                RSSItem firstOne = titlesSeen.get (titleKey);
                if (firstOne != null)
                {
                    String  strFirstOneURL = firstOne.getURL().toString();
                    String  firstTitle     = firstOne.getTitle();

                    if (firstTitle == null)
                        firstTitle = strFirstOneURL;

                    log.debug ("Feed " +
                               feedURL +
                               ": Ignoring item with URL \"" +
                               strItemURL +
                               "\" and title \"" +
                               title +
                               "\": It matches already seen item with URL \"" +
                               strFirstOneURL +
                               "\" and title \"" +
                               firstTitle +
                               "\"");

                    // Since getItems() returns a copy of the list of
                    // items, this call will not cause a
                    // ConcurrentModificationException to be thrown.

                    channel.removeItem (item);
                }

                else
                {
                    titlesSeen.put (titleKey, item);
                }
            }
        }

        return true;
    }
}
