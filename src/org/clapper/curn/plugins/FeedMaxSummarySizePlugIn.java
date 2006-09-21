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
import org.clapper.curn.MainConfigItemPlugIn;
import org.clapper.curn.PostFeedParsePlugIn;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.classutil.ClassUtil;
import org.clapper.util.config.ConfigurationException;
import org.clapper.util.logging.Logger;
import org.clapper.util.text.HTMLUtil;

import java.util.HashMap;
import java.util.Map;
import org.clapper.curn.FeedMetaDataRegistry;

/**
 * The <tt>FeedMaxSummarySizePlugIn</tt> optionally truncates a feed's
 * summary to a maximum number of characters, inserting an ellipsis at the
 * end to indicate truncation. It truncates on a word boundary, if
 * possible. This plug-in intercepts the * following configuration
 * parameters:
 *
 * <table border="1">
 *   <tr valign="top" align="left">
 *     <th>Section</th>
 *     <th>Parameter</th>
 *     <th>Meaning</th>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[curn]</tt></td>
 *     <td><tt>MaxSummarySize</tt></td>
 *     <td>The default (global) setting for all feeds. If not specified, the
 *         default value is, essentially, infinite.</td>
 *   </tr>
 *   <tr valign="top">
 *     <td><tt>[Feed<i>xxx</i>]</tt></td>
 *     <td><tt>MaxSummarySize</tt></td>
 *     <td>The setting for a specific feed. If not specified, the global
 *         default is used.
 *     </td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class FeedMaxSummarySizePlugIn
    implements MainConfigItemPlugIn,
               FeedConfigItemPlugIn,
               PostFeedParsePlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_MAX_SUMMARY_SIZE  = "MaxSummarySize";
    private static final int    NO_MAX                = Integer.MAX_VALUE;

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed save data, by feed
     */
    private Map<FeedInfo,Integer> perFeedMaxSummarySize =
        new HashMap<FeedInfo,Integer>();

    /**
     * The global default
     */
    private int maxSummarySizeDefault = NO_MAX;

    /**
     * For log messages
     */
    private static final Logger log = new Logger (FeedMaxSummarySizePlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public FeedMaxSummarySizePlugIn()
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
        return "Feed Max Summary Size";
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
     * itself as a {@link FeedMetaDataClient}, which allows the plug-in to
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
    public void runMainConfigItemPlugIn (String     sectionName,
                                         String     paramName,
                                         CurnConfig config)
        throws CurnException
    {
        try
        {
            if (paramName.equals (VAR_MAX_SUMMARY_SIZE))
            {
                maxSummarySizeDefault =
                    config.getRequiredCardinalValue (sectionName, paramName);
                if (maxSummarySizeDefault == 0)
                    maxSummarySizeDefault = NO_MAX;
            }
        }

        catch (ConfigurationException ex)
        {
            throw new CurnException (ex);
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
    public boolean runFeedConfigItemPlugIn (String     sectionName,
                                            String     paramName,
                                            CurnConfig config,
                                            FeedInfo   feedInfo)
        throws CurnException
    {
        try
        {
            if (paramName.equals (VAR_MAX_SUMMARY_SIZE))
            {
                int max = config.getRequiredCardinalValue (sectionName,
                                                           paramName);
                if (max == 0)
                    max = NO_MAX;

                perFeedMaxSummarySize.put (feedInfo, max);
                log.debug ("[" + sectionName + "]: " + paramName + "=" +
                           max);
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
        Integer maxBoxed = perFeedMaxSummarySize.get (feedInfo);
        int     max      = maxSummarySizeDefault;

        if (maxBoxed != null)
            max = maxBoxed;

        if (max != NO_MAX)
        {
            log.debug ("Truncating all item summaries to " + max +
                       " characters for feed \"" +
                       feedInfo.getURL().toString() +
                       "\"");
            for (RSSItem item : channel.getItems())
            {
                String summary = item.getSummary();
                if (summary != null)
                    item.setSummary (truncateSummary (summary, max));
            }
        }

        return true;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Truncate an RSS item's summary to a specified size. Truncates on
     * word boundary, if possible.
     *
     * @param summary  the summary to truncate
     * @param maxSize  the maximum size
     *
     * @return the truncated summary
     */
    private String truncateSummary (String summary, int maxSize)
    {
        // Can't truncate HTML right now...
        summary = HTMLUtil.textFromHTML (summary.trim());

        if (summary.length() > maxSize)
        {
            // Allow for ellipsis

            if (maxSize < 4)
                maxSize = 4;

            maxSize -= 4;

            int last = maxSize;
            char[] ch = summary.toCharArray();
            int i = last;

            // If we're in the middle of a word, find the first hunk of
            // white space.

            while ((! Character.isWhitespace (ch[i])) && (i-- >= 0))
                continue;

            // Next, get rid of trailing white space.

            while ((Character.isWhitespace (ch[i])) && (i-- >= 0))
                continue;

            // Handle underflow.

            if (i >= 0)
                last = i;

            StringBuilder buf =
                new StringBuilder (summary.substring (0, last + 1));
            buf.append (" ...");
            summary = buf.toString();
        }

        return summary;
    }
}
