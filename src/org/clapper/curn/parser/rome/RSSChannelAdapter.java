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

package org.clapper.curn.parser.rome;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.ParserUtil;

import org.clapper.util.misc.Logger;

import com.sun.syndication.feed.synd.SyndFeedI;
import com.sun.syndication.feed.synd.SyndEntry;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements the <tt>RSSChannel</tt> interface and defines an
 * adapter for the {@link <a href="https://rome.dev.java.net/">Rome</a>}
 * RSS Parser's <tt>SyndFeedI</tt> type.
 *
 * @see org.clapper.curn.parser.RSSParserFactory
 * @see org.clapper.curn.parser.RSSParser
 * @see org.clapper.curn.parser.RSSChannel
 * @see org.clapper.curn.parser.RSSItem
 * @see RSSItemAdapter
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSChannelAdapter implements RSSChannel
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The real channel object
     */
    private SyndFeedI syndFeed;

    /**
     * For log messages
     */
    private static Logger log = new Logger (RSSChannelAdapter.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>RSSChannelAdapter</tt> object that wraps the
     * specified Rome <tt>SyndFeedI</tt> object.
     *
     * @param syndFeed  the <tt>SyndFeedI</tt> object
     */
    RSSChannelAdapter (SyndFeedI syndFeed)
    {
        this.syndFeed = syndFeed;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a <tt>Collection</tt> of the items in this channel. All objects
     * in the collection are of type <tt>RSSItem</tt>.
     *
     * @return a (new) <tt>Collection</tt> of <tt>RSSItem</tt> objects.
     *         The collection will be empty (never null) if there are
     *         no items.
     */
    public Collection getItems()
    {
        Collection result = new ArrayList();
        List items  = syndFeed.getEntries();

        for (Iterator it = items.iterator(); it.hasNext(); )
            result.add (new RSSItemAdapter ((SyndEntry) it.next()));

        return result;
    }

    /**
     * Change the items the channel the ones in the specified collection.
     * If the collection is empty, the items are cleared. The items are
     * copied from the supplied collection. (A reference to the supplied
     * collection is <i>not</i> saved in this object.)
     *
     * @param newItems  new collection of <tt>RSSItem</tt> items.
     */
    public void setItems (Collection newItems)
    {
        syndFeed.setEntries (new ArrayList (newItems));
    }

    /**
     * Get the channel's title
     *
     * @return the channel's title, or null if there isn't one
     *
     * @see #setTitle(String)
     */
    public String getTitle()
    {
        // Rome leaves leading, trailing and embedded newlines in place.
        // While this is syntactically okay, curn prefers the description
        // to be one long line. ParserUtil.normalizeCharacterData() strips
        // leading and trailing newlines, and converts embedded newlines to
        // spaces.

        return ParserUtil.normalizeCharacterData (syndFeed.getTitle());
    }

    /**
     * Set the channel's title
     *
     * @param newTitle the channel's title, or null if there isn't one
     *
     * @see #getTitle()
     */
    public void setTitle (String newTitle)
    {
        syndFeed.setTitle (newTitle);
    }

    /**
     * Get the channel's description
     *
     * @return the channel's description, or null if there isn't one
     */
    public String getDescription()
    {
        // Rome leaves leading, trailing and embedded newlines in place.
        // While this is syntactically okay, curn prefers the description
        // to be one long line. ParserUtil.normalizeCharacterData() strips
        // leading and trailing newlines, and converts embedded newlines to
        // spaces.

        return ParserUtil.normalizeCharacterData (syndFeed.getDescription());
    }

    /**
     * Get the channel's published link (its URL).
     *
     * @return the URL, or null if not available
     */
    public URL getLink()
    {
        URL url = null;

        try
        {
            url = new URL (syndFeed.getLink());
        }

        catch (MalformedURLException ex)
        {
            log.error ("Bad channel URL \""
                     + syndFeed.getLink()
                     + "\" from underlying parser: "
                     + ex.toString());
        }

        return url;
    }

    /**
     * Get the channel's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate()
    {
        return syndFeed.getPublishedDate();
    }

    /**
     * Get the channel's copyright string
     *
     * @return the copyright string, or null if not available
     */
    public String getCopyright()
    {
        return syndFeed.getCopyright();
    }

    /**
     * Get the RSS format the channel is using.
     *
     * @return the format, or null if not available
     */
    public String getRSSFormat()
    {
        return syndFeed.getFeedType();
    }

    /**
     * Get the author of the feed.
     *
     * @return the author, or null if not available
     */
    public String getAuthor()
    {
        return syndFeed.getAuthor();
    }
}
