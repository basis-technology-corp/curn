/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.io.*;
import java.util.*;
import java.net.*;
import org.clapper.util.text.*;
import org.clapper.util.config.*;
import org.clapper.util.io.*;

/**
 * <p>Contains configuration data for one RSS feed (or site).</p>
 *
 * @see RSSGetConfiguration
 *
 * @version <tt>$Revision$</tt>
 */
class RSSFeedInfo
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private boolean  pruneURLsFlag = false;
    private int      daysToCache   = 0;
    private URL      siteURL       = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor. Only accessible within this package.
     *
     * @param siteURL  the main URL for the site's RSS feed. This constructor
     *                 normalizes the URL.
     *
     * @see Util#normalizeURL
     */
    RSSFeedInfo (URL siteURL)
    {
        this.siteURL = Util.normalizeURL (siteURL);
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the main RSS URL for the site.
     *
     * @return the site's main RSS URL
     */
    URL getURL()
    {
        return siteURL;
    }

    /**
     * Get the number of days that URLs from this site are to be cached.
     *
     * @return the number of days to cache URLs from this site.
     *
     * @see #setDaysToCache
     */
    int getDaysToCache()
    {
        return daysToCache;
    }

    /**
     * Get the number of milliseconds that URLs from this site are to be
     * cached. This is a convenience front-end to <tt>getDaysToCache()</tt>.
     *
     * @return the number of milliseconds to cache URLs from this site
     *
     * @see #getDaysToCache
     * @see #setDaysToCache
     */
    long getMillisecondsToCache()
    {
        long days = (long) getDaysToCache();
        return days * 25 * 60 * 60 * 1000;
    }
   
    /**
     * Set the "days to cache" value.
     *
     * @param cacheDays  new value
     *
     * @see #getDaysToCache
     * @see #getMillisecondsToCache
     */
    void setDaysToCache (int cacheDays)
    {
        this.daysToCache = cacheDays;
    }

    /**
     * Get the "prune URLs" flag. If this flag is set, then URLs from this
     * feed should be displayed without any HTTP parameter information.
     * Otherwise, they should be displayed as is.
     *
     * @return <tt>true</tt> if the flag is set, <tt>false</tt> otherwise
     *
     * @see #setPruneURLsFlag
     */
    boolean pruneURLs()
    {
        return pruneURLsFlag;
    }

    /**
     * Set the "prune URLs" flag. If this flag is set, then URLs from this
     * feed should be displayed without any HTTP parameter information.
     * Otherwise, they should be displayed as is.
     *
     * @param val <tt>true</tt> to set the flag, <tt>false</tt> to clear it
     *
     * @see #pruneURLs
     */
    void setPruneURLsFlag (boolean val)
    {
        pruneURLsFlag = val;
    }
}
