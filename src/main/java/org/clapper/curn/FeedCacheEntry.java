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


package org.clapper.curn;

import java.net.URL;
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
public class FeedCacheEntry
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private       long    timestamp = 0;
    private final URL     entryURL;
    private final URL     channelURL;
    private final Date    publicationDate;
    private       boolean sticky = false;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>FeedCacheEntry</tt>.
     *
     * @param channelURL  the main URL for the site's RSS feed
     * @param entryURL    the URL to be cached. May be an individual item URL,
     *                    or the channel URL (again).
     * @param pubDate     the publication date of the item, or null if unknown
     * @param timestamp   the timestamp (milliseconds) to be cached
     */
    public FeedCacheEntry(URL    channelURL,
                          URL    entryURL,
                          Date   pubDate,
                          long   timestamp)
    {
        this.channelURL      = channelURL;
        this.entryURL        = entryURL;
        this.timestamp       = timestamp;
        this.publicationDate = pubDate;
    }

    /*----------------------------------------------------------------------*\
                             Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the main (channel) RSS URL for the site. This URL is the main
     * feed URL, not the item's specific URL.
     *
     * @return the site's main RSS URL
     */
    public URL getChannelURL()
    {
        return channelURL;
    }

    /**
     * Determine whether this entry is a channel entry or not. A channel
     * entry has the same entry URL and channel URL. This method is really
     * just convenient shorthand for:
     *
     * <blockquote>
     * <pre>entry.getChannelURL().sameFile(entry.getEntryURL())</pre>
     * </blockquote>
     *
     * @return <tt>true</tt> if this entry is a channel (a.k.a., feed) entry,
     *         <tt>false</tt> if it is an item entry
     */
    public boolean isChannelEntry()
    {
        return channelURL.sameFile(entryURL);
    }

    /**
     * Get the URL for this entry.
     *
     * @return the entry URL
     */
    public URL getEntryURL()
    {
        return entryURL;
    }

    /**
     * Get the publication date associated with the cached item. This value
     * is typically from within the parsed RSS item.
     *
     * @return the publication date, or null if not known
     */
    public Date getPublicationDate()
    {
        return publicationDate;
    }

    /**
     * Get the timestamp associated with this entry. The timestamp
     * represents the last time (in milliseconds) that the URL was read.
     *
     * @return the timestamp
     */
    public long getTimestamp()
    {
        return timestamp;
    }

    /**
     * Set the timestamp associated with this entry. The timestamp
     * represents the last time (in milliseconds) that the URL was read.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp (final long timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * Determine whether this entry is "sticky" (i.e., should be shown even
     * though it's already been seen.
     *
     * @return <tt>true</tt> if the item is marked "sticky",
     *         <tt>false</tt> if not
     */
    public boolean isSticky()
    {
        return sticky;
    }

    /**
     * Set or clear the "sticky" flag. If an item is marked sticky, it will
     * be shown even though it's already in the cache. NOTE: Stickiness is
     * not automatically persisted to the disk store. It's intended to be
     * calculated at runtime, based on other criteria.
     *
     * @param sticky whether or not the entry is to be marked sticky
     */
    public void setSticky(boolean sticky)
    {
        this.sticky = sticky;
    }
}
