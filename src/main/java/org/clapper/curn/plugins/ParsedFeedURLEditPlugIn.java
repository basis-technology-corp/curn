/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2010 Brian M. Clapper.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name "clapper.org", "curn", nor the names of the project's
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/


package org.clapper.curn.plugins;

import org.clapper.curn.CurnConfig;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.FeedConfigItemPlugIn;
import org.clapper.curn.PostFeedProcessPlugIn;

import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.RSSLink;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;
import org.clapper.util.regex.RegexUtil;
import org.clapper.util.regex.RegexException;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.clapper.curn.FeedCache;
import org.clapper.curn.parser.RSSChannel;

/**
 * The <tt>ParsedFeedURLEditPlugIn</tt> edits a feed after it has been
 * parsed, adjusting the URLs in the feed (i.e., the item URLs and the
 * channel, or feed, URL) according to various configuration parameters. It
 * can be used to fix known errors in the XML. It intercepts the following
 * per-feed configuration parameters:
 *
 * <table border="1">
 *   <tr valign="top">
 *     <td><tt>EditItemURL<i>suffix</i></tt></td>
 *     <td>Specifies a regular expression to be applied to the URLs
 *         for all items in the feed. Multiple expressions may be specified
 *         per feed. See the User's Guide for details.
 *     </td>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>EditFeedURL<i>suffix</i></tt></td>
 *     <td>Specifies a regular expression to be applied to the channel, or
 *         feed, URL. Multiple expressions may be specified per feed. See
 *         the User's Guide for details.
 *     </td>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>PruneURLs</tt></td>
 *     <td>Specifies that all URLs should be pruned of their HTTP parameters.
 *         This action also can be accomplished with edit directives, using
 *         the above configuration items; this parameter is a convenience.</td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class ParsedFeedURLEditPlugIn
    implements FeedConfigItemPlugIn,
               PostFeedProcessPlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String  VAR_PRUNE_URLS       = "PruneURLs";
    private static final boolean DEF_PRUNE_URLS       = false;
    private static final String  VAR_EDIT_ITEM_URL    = "EditItemURL";
    private static final String  VAR_EDIT_FEED_URL    = "EditFeedURL";

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    /**
     * Feed edit info
     */
    class FeedEditInfo
    {
        boolean      pruneURLs = DEF_PRUNE_URLS;
        List<String> itemURLEditEditCmds = new ArrayList<String>();
        List<String> channelURLEditEditCmds = new ArrayList<String>();

        FeedEditInfo()
        {
            // Nothing to do
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed save data, by feed
     */
    private Map<FeedInfo,FeedEditInfo> perFeedEditInfoMap =
        new HashMap<FeedInfo,FeedEditInfo>();

    /**
     * For log messages
     */
    private static final Logger log =
        new Logger (ParsedFeedURLEditPlugIn.class);

    /**
     * Regular expression helper
     */
    private RegexUtil regexUtil = new RegexUtil();

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public ParsedFeedURLEditPlugIn()
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
        return "Edit Parsed Feed URL";
    }

    /**
     * Get the sort key for this plug-in.
     *
     * @return the sort key string.
     */
    public String getPlugInSortKey()
    {
        return ClassUtil.getShortClassName(getClass().getName());
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

    /*----------------------------------------------------------------------*\
               Public Methods Required by *PlugIn Interfaces
    \*----------------------------------------------------------------------*/

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
            if (paramName.startsWith (VAR_EDIT_ITEM_URL))
            {
                FeedEditInfo editInfo = getOrMakeFeedEditInfo(feedInfo);
                String value = config.getConfigurationValue(sectionName,
                                                            paramName);
                editInfo.itemURLEditEditCmds.add (value);
                log.debug("[" + sectionName + "]: added item regexp " + value);
            }

            else if (paramName.startsWith (VAR_EDIT_FEED_URL))
            {
                FeedEditInfo editInfo = getOrMakeFeedEditInfo(feedInfo);
                String value = config.getConfigurationValue(sectionName,
                                                            paramName);
                editInfo.channelURLEditEditCmds.add(value);
                log.debug("[" + sectionName + "]: added feed regexp " + value);
            }

            else if (paramName.equals (VAR_PRUNE_URLS))
            {
                FeedEditInfo editInfo = getOrMakeFeedEditInfo(feedInfo);
                editInfo.pruneURLs =
                    config.getRequiredBooleanValue(sectionName, paramName);
                log.debug("[" + sectionName + "]: set PruneURLs=" +
                          editInfo.pruneURLs);
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
        FeedEditInfo editInfo = perFeedEditInfoMap.get (feedInfo);

        if (editInfo != null)
        {
            // First the channel itself.

            if (editInfo.pruneURLs ||
                (editInfo.channelURLEditEditCmds.size() > 0))
            {
                RSSLink channelLink   = channel.getURL();
                URL     channelURL    = channelLink.getURL();
                String  strChannelURL = channelURL.toExternalForm();

                log.debug("Before editing, feed URL=" + strChannelURL);

                if (editInfo.pruneURLs)
                    strChannelURL = pruneURL(strChannelURL);

                for (String editCmd : editInfo.channelURLEditEditCmds)
                    strChannelURL = editURL(strChannelURL, editCmd);

                log.debug("After editing, feed URL=" + strChannelURL);

                try
                {
                    channelLink.setURL(new URL(strChannelURL));
                }

                catch (MalformedURLException ex)
                {
                    throw new CurnException("After editing feed URL \"" +
                                            channelURL + "\", result \"" +
                                            strChannelURL +
                                            "\" is an illegal URL.");
                }
            }

            // Now the individual items.

            if (editInfo.pruneURLs ||
                (editInfo.itemURLEditEditCmds.size() > 0))
            {
                for (RSSItem item : channel.getItems())
                {
                    RSSLink itemLink   = item.getURL();
                    URL     itemURL    = itemLink.getURL();
                    String  strItemURL = itemURL.toExternalForm();

                    log.debug("Before editing, item URL=" + strItemURL);

                    if (editInfo.pruneURLs)
                        strItemURL = pruneURL(strItemURL);

                    for (String editCmd : editInfo.itemURLEditEditCmds)
                        strItemURL = editURL(strItemURL, editCmd);

                    log.debug("After editing, item URL=" + strItemURL);

                    try
                    {
                        itemLink.setURL(new URL(strItemURL));
                    }

                    catch (MalformedURLException ex)
                    {
                        throw new CurnException("After editing item URL \"" +
                                                itemURL + "\", result \"" +
                                                 strItemURL +
                                                 "\" is an illegal URL",
                                                 ex);
                    }
                }
            }
        }

        return true;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private FeedEditInfo getOrMakeFeedEditInfo(FeedInfo feedInfo)
    {
        FeedEditInfo editInfo = perFeedEditInfoMap.get(feedInfo);
        if (editInfo == null)
        {
            editInfo = new FeedEditInfo();
            perFeedEditInfoMap.put(feedInfo, editInfo);
        }

        return editInfo;
    }

    /**
     * Prune a URL string of its HTTP parameters.
     *
     * @param urlString the URL string
     *
     * @return the possibly edited result
     */
    private String pruneURL(String urlString)
    {
        int i = urlString.indexOf("?");

        if (i != -1)
            urlString = urlString.substring(0, i);

        return urlString;
    }

    /**
     * Apply a regular expression to a URL, returning the result.
     *
     * @param urlString the URL string
     * @param editCmd   the substitution command
     *
     * @return the possibly edited result
     *
     * @throws CurnException on error
     */
    private String editURL(String urlString, String editCmd)
        throws CurnException
    {
        try
        {
            return regexUtil.substitute(editCmd, urlString);
        }

        catch (RegexException ex)
        {
            throw new CurnException("Failed to edit URL \"" + urlString +
                                     "\" with \"" + editCmd + "\"",
                                     ex);
        }
    }
}
