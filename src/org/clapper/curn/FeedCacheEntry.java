/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.net.URL;
import java.io.Serializable;

/**
 * <p>Defines the contents of a cache entry. There is one cache entry
 * per feed item.</p>
 *
 * @see ConfigFile
 * @see FeedInfo
 *
 * @version <tt>$Revision$</tt>
 */
class FeedCacheEntry implements Serializable
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private long    timestamp  = 0;
    private String  entryID    = null;
    private URL     entryURL   = null;
    private URL     channelURL = null;

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
     * @param timestamp   the timestamp (milliseconds) to be cached
     */
    FeedCacheEntry (String entryID,
                   URL    channelURL,
                   URL    entryURL,
                   long   timestamp)
    {
        this.entryID    = entryID;
        this.channelURL = channelURL;
        this.entryURL   = entryURL;
        this.timestamp  = timestamp;
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
