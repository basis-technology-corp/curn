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

package org.clapper.curn.plugins;

import org.clapper.curn.Constants;
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

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * The <tt>SortArticlesPlugIn</tt> handles per-feed SortBy settings.
 * It looks for a default (main-configuration section) "SortBy" parameter,
 * and permits a per-feed "SortBy" parameter to override the default.
 *
 * <table border="1">
 *   <tr valign="top">
 *     <td><tt>SortBy</tt></td>
 *     <td>Criteria by which to sort. Legal values are:
 *         <ul>
 *           <li><tt>none</tt> &#8212; Leave items in whatever order they
 *               appear in the feed.
 *           <li><tt>time</tt> &#8212; Sort items by timestamp
 *           <li><tt>title</tt> &#8212; Sort items by title
 *     </td>
 *   </tr>
 * </table>
 *
 * @version <tt>$Revision$</tt>
 */
public class SortArticlesPlugIn
    implements MainConfigItemPlugIn,
               FeedConfigItemPlugIn,
               PostFeedParsePlugIn
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String VAR_SORT_BY           = "SortBy";
    private static final SortBy DEF_SORT_BY           = SortBy.NONE;

    private enum SortBy
    {
        NONE,
        TIME,
        TITLE
    };

    /**
     * Legal values
     */
    private static final Map<String,SortBy> LEGAL_SORT_BY_VALUES
        = new HashMap<String,SortBy>();
    static
    {
        LEGAL_SORT_BY_VALUES.put ("none", SortBy.NONE);
        LEGAL_SORT_BY_VALUES.put ("time", SortBy.TIME);
        LEGAL_SORT_BY_VALUES.put ("title", SortBy.TITLE);
    }

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    private class ItemComparator implements Comparator<RSSItem>
    {
        private Date   now = new Date();
        private SortBy sortBy;

        ItemComparator (SortBy sortBy)
        {
            this.sortBy = sortBy;
        }

        public int compare (RSSItem i1, RSSItem i2)
        {
            int      cmp = 0;

            switch (sortBy)
            {
                case TITLE:
                    String title1 = i1.getTitle();
                    if (title1 == null)
                        title1 = "";

                    String title2 = i2.getTitle();
                    if (title2 == null)
                        title2 = "";

                    cmp = title1.compareToIgnoreCase (title2);
                    break;

                case TIME:
                    Date time1 = i1.getPublicationDate();
                    if (time1 == null)
                        time1 = now;

                    Date time2 = i2.getPublicationDate();
                    if (time2 == null)
                        time2 = now;

                    cmp = time1.compareTo (time2);
                    break;

                default:
                    cmp = -1;
                    break;
            }

            return cmp;
        }

        public int hashCode()                                        // NOPMD
        {
            return super.hashCode();
        }

        public boolean equals (Object o)
        {
            return (o instanceof ItemComparator);
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Feed sort-by data, by feed
     */
    private Map<URL,SortBy> perFeedSortByMap =
        new HashMap<URL,SortBy>();

    /**
     * Default sort-by value
     */
    private SortBy defaultSortBy = DEF_SORT_BY;
    /**
     * For log messages
     */
    private static final Logger log = new Logger (SortArticlesPlugIn.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor (required).
     */
    public SortArticlesPlugIn()
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
        return "Sort Articles";
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
            if (paramName.equals (VAR_SORT_BY))
            {
                String val = config.getOptionalStringValue (sectionName,
                                                            paramName,
                                                            null);
                defaultSortBy = (val == null) ? DEF_SORT_BY
                                              : parseSortByValue (sectionName,
                                                                  val);
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
            if (paramName.equals (VAR_SORT_BY))
            {
                String value = config.getConfigurationValue (sectionName,
                                                             paramName);
                SortBy sortBy = parseSortByValue (sectionName, value);
                URL feedURL = feedInfo.getURL();
                perFeedSortByMap.put (feedURL, sortBy);
                log.debug (feedURL + ": SortBy=" + sortBy);
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
        log.debug ("Post feed parse: " + feedInfo.getURL());
        channel.setItems (sortChannelItems (channel.getItems(), feedInfo));
        return true;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

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
    private SortBy parseSortByValue (String sectionName, String value)
        throws ConfigurationException
    {
        SortBy val = LEGAL_SORT_BY_VALUES.get (value);

        if (val == null)
        {
            throw new ConfigurationException
                (Constants.BUNDLE_NAME, "CurnConfig.badVarValue",
                 "Section \"{0}\" in the configuration file has a bad " +
                 "value (\"{1}\") for the \"{2}\" parameter",
                 new Object[] {sectionName, value, VAR_SORT_BY});
        }

        return val;
    }

    /**
     * Sort downloaded items according to the sort criteria for the feed
     *
     * @param items    the downloaded items
     * @param feedInfo info about the feed, used to determine the desired
     *                 sort criteria
     *
     * @return a <tt>Collection</tt> of the same items, possibly sorted
     */
    private Collection<RSSItem> sortChannelItems (Collection<RSSItem> items,
                                                  FeedInfo            feedInfo)
    {
        Collection<RSSItem> result = null;
        int                 total  = items.size();
        URL                 feedURL = feedInfo.getURL();

        log.debug ("Feed " + feedURL + ": total items=" + total);
        if (total > 0)
        {
            SortBy sortBy = perFeedSortByMap.get (feedURL);
            log.debug ("feed " + feedURL + ": SortBy=" + sortBy);
            if (sortBy == null)
                sortBy = defaultSortBy;

            switch (sortBy)
            {
                case NONE:
                    result = new ArrayList<RSSItem>(items);
                    break;

                case TITLE:
                case TIME:

                    // Can't just use a TreeSet, with a Comparator, because
                    // then items with the same title will be weeded out.

                    List<RSSItem> newItems = new ArrayList<RSSItem> (items);
                    Collections.sort (newItems, new ItemComparator (sortBy));
                    result = newItems;
                break;

            default:
                assert (false);
            }
        }

        return result;
    }

}
