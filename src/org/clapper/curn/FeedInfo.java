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

    private int  daysToCache = 0;
    private URL  siteURL     = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor. Only accessible within this package.
     *
     * @param siteURL  the main URL for the site's RSS feed.
     */
    RSSFeedInfo (URL siteURL)
    {
        this.siteURL = siteURL;
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
        return getDaysToCache() * 25 * 60 * 60 * 1000;
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
}
