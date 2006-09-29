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
    private final String  entryID;
    private final URL     entryURL;
    private final URL     channelURL;
    private final Date    publicationDate;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>FeedCacheEntry</tt>.
     *
     * @param entryID     the entry's unique ID.
     * @param channelURL  the main URL for the site's RSS feed
     * @param entryURL    the URL to be cached. May be an individual item URL,
     *                    or the channel URL (again).
     * @param pubDate     the publication date of the item, or null if unknown
     * @param timestamp   the timestamp (milliseconds) to be cached
     */
    public FeedCacheEntry(String entryID,
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
}
