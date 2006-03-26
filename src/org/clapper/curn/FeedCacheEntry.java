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

package org.clapper.curn;

import java.net.URL;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>Defines the contents of a cache entry. There is one cache entry
 * per feed item.</p>
 *
 * @see CurnConfig
 * @see FeedInfo
 *
 * @version <tt>$Revision$</tt>
 */
class FeedCacheEntry implements Serializable
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private long    timestamp       = 0;
    private String  entryID         = null;
    private URL     entryURL        = null;
    private URL     channelURL      = null;
    private Date    publicationDate = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor. Only accessible within this package.
     *
     * @param entryID     the entry's unique ID.
     * @param channelURL  the main URL for the site's RSS feed
     * @param entryURL    the URL to be cached. May be an individual item URL,
     *                    or the channel URL (again).
     * @param pubDate     the publication date of the item, or null if unknown
     * @param timestamp   the timestamp (milliseconds) to be cached
     */
    FeedCacheEntry (String entryID,
                    URL    channelURL,
                    URL    entryURL,
                    Date   pubDate,
                    long   timestamp)
    {
        this.entryID         = entryID;
        this.channelURL      = channelURL;
        this.entryURL        = entryURL;
        this.timestamp       = timestamp;
        this.publicationDate = pubDate;
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the unique ID (i.e., cache key) associated with this item.
     *
     * @return the unique ID
     */
    String getUniqueID()
    {
        return entryID;
    }

    /**
     * Get the main (channel) RSS URL for the site. This URL is the main
     * feed URL, not the item's specific URL.
     *
     * @return the site's main RSS URL
     */
    URL getChannelURL()
    {
        return channelURL;
    }

    /**
     * Get the URL for this entry.
     *
     * @return the entry URL
     */
    URL getEntryURL()
    {
        return entryURL;
    }

    /**
     * Get the publication date associated with the cached item. This value
     * is typically from within the parsed RSS item.
     *
     * @return the publication date, or null if not known
     */
    Date getPublicationDate()
    {
        return publicationDate;
    }

    /**
     * Get the timestamp associated with this entry. The timestamp
     * represents the last time (in milliseconds) that the URL was read.
     *
     * @return the timestamp
     */
    long getTimestamp()
    {
        return timestamp;
    }

    /**
     * Set the timestamp associated with this entry. The timestamp
     * represents the last time (in milliseconds) that the URL was read.
     *
     * @param timestamp the timestamp
     */
    void setTimestamp (long timestamp)
    {
        this.timestamp = timestamp;
    }
}
