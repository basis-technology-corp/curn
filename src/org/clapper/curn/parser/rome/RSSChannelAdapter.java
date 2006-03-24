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

package org.clapper.curn.parser.rome;

import org.clapper.curn.parser.ParserUtil;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.RSSLink;

import org.clapper.util.logging.Logger;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

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
public class RSSChannelAdapter extends RSSChannel
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The real channel object
     */
    private SyndFeed syndFeed;

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
    RSSChannelAdapter (SyndFeed syndFeed)
    {
        this.syndFeed = syndFeed;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Create a new, empty instance of the underlying concrete
     * class.
     *
     * @return the new instance
     */
    public RSSChannel newInstance()
    {
        return new RSSChannelAdapter (new SyndFeedImpl());
    }

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

        for (Iterator it = syndFeed.getEntries().iterator(); it.hasNext(); )
            result.add (new RSSItemAdapter ((SyndEntry) it.next(), this));

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
        Collection<SyndEntry> syndItems = new ArrayList<SyndEntry>();

        for (RSSItem ourItem : newItems)
        {
            RSSItemAdapter itemAdapter = (RSSItemAdapter) ourItem;
            syndItems.add (itemAdapter.getSyndEntry());
        }

        // We're storing values from a genericized collection into a
        // non-genericized collection. We have to copy the items to a new
        // collection. Use of a Collection<Object> avoids a compiler
        // "unchecked cast" warning.

        syndFeed.setEntries (new ArrayList<Object> (syndItems));
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
     * Set the channel's description
     *
     * @param desc the channel's description, or null if there isn't one
     *
     * @see #getDescription
     */
    public void setDescription (String desc)
    {
        syndFeed.setDescription (desc);
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
        // Since ROME doesn't support multiple links per feed, we have to
        // assume that this link is the link for the feed. Try to figure
        // out the MIME type, and default to the MIME type for an RSS feed.
        // Mark the feed as type "self".

        Collection<RSSLink> results = new ArrayList<RSSLink>();

        try
        {
            URL url = new URL (syndFeed.getLink());
            results.add (new RSSLink (url,
                                      ParserUtil.getLinkMIMEType (url),
                                      RSSLink.Type.SELF));
        }

        catch (MalformedURLException ex)
        {
            log.error ("Bad channel URL \""
                     + syndFeed.getLink()
                     + "\" from underlying parser: "
                     + ex.toString());
        }

        return results;
    }
    /**
     * Set the channel's list of published links (its URLs).
     *
     * @param links the links
     *
     * @see #getLink
     * @see #getLinks
     */
    public void setLinks (Collection<RSSLink> links)
    {
        // Since ROME doesn't support multiple links per feed, we have
        // to assume that the first link is the link for the feed.

        if ((links != null) && (links.size() > 0))
        {
            RSSLink link = links.iterator().next();
            this.syndFeed.setLink (link.getURL().toExternalForm());
        }
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
     * Set the channel's publication date.
     *
     * @param date  the publication date, or null if not available
     */
    public void setPublicationDate (Date date)
    {
        this.syndFeed.setPublishedDate (date);
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
     * Set the channel's copyright string
     *
     * @param copyright  the copyright string, or null if not available
     *
     * @see #getCopyright
     */
    public void setCopyright (String copyright)
    {
        this.syndFeed.setCopyright (copyright);
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
     * Get the RSS format the channel is using, in native format. This
     * method exists for underlying implementations that store the RSS
     * format as something other than a string; the method allows the
     * {@link #makeCopy} method to copy the RSS format without knowing
     * how it's stored. The default implementation of this method 
     * simply calls {@link #getRSSFormat}.
     *
     * @return the format, or null if not available
     *
     * @see #getRSSFormat
     * @see #setNativeRSSFormat
     */
    public Object getNativeRSSFormat()
    {
        return getRSSFormat();
    }

    /**
     * Set the RSS format the channel is using.
     *
     * @param format the format, or null if not available
     *
     * @see #getRSSFormat
     * @see #getNativeRSSFormat
     */
    public void setNativeRSSFormat (Object format)
    {
        ((SyndFeedImpl) this.syndFeed).setFeedType ((String) format);
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
        // Rome does not support this field

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
        // Rome does not support this field
    }

    /**
     * Clear the authors list.
     *
     * @see #getAuthors
     * @see #addAuthor
     */
    public void clearAuthors()
    {
        // Rome does not support this field
    }
}
