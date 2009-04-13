/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2009 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. The end-user documentation included with the redistribution, if any,
     must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2009 Brian M. Clapper."

     Alternately, this acknowlegement may appear in the software itself,
     if wherever such third-party acknowlegements normally appear.

  3. Neither the names "clapper.org", "curn", nor any of the names of the
     project contributors may be used to endorse or promote products
     derived from this software without prior written permission. For
     written permission, please contact bmc@clapper.org.

  4. Products derived from this software may not be called "curn", nor may
     "clapper.org" appear in their names without prior written permission
     of Brian M. Clapper.

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

/**
 * <p>Contains data for one feed (or site). The data about the feed
 * comes from the configuration file. The feed itself comes from parsing
 * the RSS data.</p>
 *
 * @see CurnConfig
 *
 * @version <tt>$Revision$</tt>
 */
public class FeedInfo
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Default encoding for "save as" file.
     */
    public static final String DEFAULT_SAVE_AS_ENCODING = "utf-8";

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private       int         daysToCache = 0;
    private final URL         siteURL;
    private       String      forcedEncoding = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor.
     *
     * @param siteURL  the main URL for the site's RSS feed. This constructor
     *                 normalizes the URL.
     * @see CurnUtil#normalizeURL
     */
    public FeedInfo(URL siteURL)
    {
        this.siteURL = CurnUtil.normalizeURL (siteURL);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the hash code for this feed
     *
     * @return the hash code
     */
    public int hashCode()
    {
        return getURL().hashCode();
    }

    /**
     * Determine whether this <tt>FeedInfo</tt> object is equivalent to
     * another one, based on the URL.
     *
     * @param obj  the other object
     *
     * @return <tt>true</tt> if <tt>obj</tt> is a <tt>FeedInfo</tt> object
     *         that specifies the same URL, <tt>false</tt> otherwise
     */
    public boolean equals (final Object obj)
    {
        boolean eq = false;

        if (obj instanceof FeedInfo)
            eq = this.siteURL.equals (((FeedInfo) obj).siteURL);

        return eq;
    }

    /**
     * Get the main RSS URL for the site.
     *
     * @return the site's main RSS URL, guaranteed to be normalized
     * @see CurnUtil#normalizeURL
     */
    public URL getURL()
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
    public int getDaysToCache()
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
    public long getMillisecondsToCache()
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
    public void setDaysToCache (final int cacheDays)
    {
        this.daysToCache = cacheDays;
    }

    /**
     * Get the forced character set encoding for this feed. If this
     * parameter is set, <i>curn</i> will ignore the character set encoding
     * advertised by the remote server (if any), and use the character set
     * specified by this configuration item instead. This is useful in the
     * following cases:
     *
     * <ul>
     *   <li>the remote HTTP server doesn't supply an HTTP Content-Encoding
     *       header, and the local (Java) default encoding doesn't match
     *       the document's encoding
     *   <li>the remote HTTP server supplies the wrong encoding
     * </ul>
     *
     * @return the forced character set encoding, or null if not configured
     */
    public String getForcedCharacterEncoding()
    {
        return forcedEncoding;
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /*
     * Set the forced character set encoding for this feed.
     *
     * @param encoding the encoding
     *
     * @see #getForcedCharacterEncoding
     */
    void setForcedCharacterEncoding (final String encoding)
    {
        this.forcedEncoding = encoding;
    }
}
