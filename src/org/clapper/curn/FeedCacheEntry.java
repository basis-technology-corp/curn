/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.net.URL;
import java.io.Serializable;

/**
 * <p>Defines the contents of a cache entry. There is one cache entry
 * per feed item.</p>
 *
 * @see RSSGetConfiguration
 * @see RSSFeedInfo
 *
 * @version <tt>$Revision$</tt>
 */
class RSSCacheEntry implements Serializable
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private long  timestamp  = 0;
    private URL   itemURL    = null;
    private URL   channelURL = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor. Only accessible within this package.
     *
     * @param channelURL  the main URL for the site's RSS feed
     * @param itemURL     the URL for the item
     * @param timestamp   the timestamp (milliseconds) to be cached
     */
    RSSCacheEntry (URL channelURL, URL itemURL, long timestamp)
    {
        this.channelURL = channelURL;
        this.itemURL    = itemURL;
        this.timestamp  = timestamp;
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

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
     * Get the item's URL.
     *
     * @return the item's URL
     */
    URL getItemURL()
    {
        return itemURL;
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
