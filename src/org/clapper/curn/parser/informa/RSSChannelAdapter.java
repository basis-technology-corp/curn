
/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn.parser.informa;

import org.clapper.curn.parser.ParserUtil;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.RSSLink;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;

import java.net.URL;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class implements the <tt>RSSChannel</tt> interface and defines an
 * adapter for the <a href="http://informa.sourceforge.net/">Informa</a>
 * RSS Parser's <tt>ChannelIF</tt> type.
 *
 * @see org.clapper.curn.parser.RSSParserFactory
 * @see org.clapper.curn.parser.RSSParser
 * @see org.clapper.curn.parser.RSSChannel
 * @see org.clapper.curn.parser.RSSItem
 * @see RSSItemAdapter
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSChannelAdapter extends RSSChannel
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The real channel object
     */
    private ChannelIF channel;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>RSSChannelAdapter</tt> object that wraps the
     * specified Informa <tt>ChannelIF</tt> object.
     *
     * @param channelIF  the <tt>ChannelIF</tt> object
     */
    RSSChannelAdapter (ChannelIF channelIF)
    {
        this.channel = channelIF;
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
    public Collection<RSSItem> getItems()
    {
        Collection<RSSItem> result = new ArrayList<RSSItem>();

        for (Iterator it = channel.getItems().iterator(); it.hasNext(); )
            result.add (new RSSItemAdapter ((ItemIF) it.next()));

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
    public void setItems (Collection<? extends RSSItem> newItems)
    {
        Collection<ItemIF> items = new ArrayList<ItemIF>();
        Iterator           it;

        // Can't use generics with Informa classes, since they don't
        // provide generic-based prototypes. Copy the items from the
        // Informa channel to a separate list, so we can iterate over that
        // list to remove them from the Informa one. (This prevents a
        // ConcurrentModificationException.)

        for (it = channel.getItems().iterator(); it.hasNext(); )
            items.add ((ItemIF) it.next());

        for (ItemIF item : items)
            this.channel.removeItem (item);

        for (RSSItem newItem : newItems)
            this.channel.addItem (((RSSItemAdapter) newItem).getItemIF());
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
        return this.channel.getTitle();
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
        this.channel.setTitle (newTitle);
    }

    /**
     * Get the channel's description
     *
     * @return the channel's description, or null if there isn't one
     */
    public String getDescription()
    {
        return this.channel.getDescription();
    }

    /**
     * Get the channel's published links.
     *
     * @return the collection of links, or an empty collection
     *
     * @see RSSChannel#getLink
     */
    public final Collection<RSSLink> getLinks()
    {
        // Since Informa doesn't support multiple links per feed, we have
        // to assume that this link is the link for the feed. Try to figure
        // out the MIME type, and default to the MIME type for an RSS feed.
        // Mark the feed as type "self".

        Collection<RSSLink> results = new ArrayList<RSSLink>();

        URL url = channel.getSite();
        results.add (new RSSLink (url,
                                  ParserUtil.getLinkMIMEType (url),
                                  RSSLink.Type.SELF));

        return results;
    }

    /**
     * Get the channel's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate()
    {
        return this.channel.getPubDate();
    }

    /**
     * Get the channel's copyright string
     *
     * @return the copyright string, or null if not available
     */
    public String getCopyright()
    {
        return this.channel.getCopyright();
    }

    /**
     * Get the RSS format the channel is using.
     *
     * @return the format, or null if not available
     */
    public String getRSSFormat()
    {
        return this.channel.getFormat().toString();
    }

    /**
     * Get the channel's author list.
     *
     * @return the authors, or null (or an empty <tt>Collection</tt>) if
     *         not available
     *
     * @see #addAuthor
     * @see #clearAuthors
     */
    public Collection<String> getAuthors()
    {
        // Informa does not support this field

        return null;
    }

    /**
     * Add to the channel's author list.
     *
     * @param author  another author string to add
     *
     * @see #getAuthors
     * @see #clearAuthors
     */
    public void addAuthor (String author)
    {
        // Informa does not support this field
    }

    /**
     * Clear the authors list.
     *
     * @see #getAuthors
     * @see #addAuthor
     */
    public void clearAuthors()
    {
        // Informa does not support this field
    }
}
