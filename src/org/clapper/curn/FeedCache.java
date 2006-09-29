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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.clapper.util.logging.Logger;

/**
 * Defines the in-memory format of the <i>curn</i> cache, and provides
 * methods for saving and restoring the cache.
 *
 * @see Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class FeedCache
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                              Private Classes
    \*----------------------------------------------------------------------*/

    /*
    private class FeedCacheEntryMap extends HashMap<String,FeedCacheEntry>
    {
        FeedCacheEntryMap()
        {
            super();
        }
    }
     */

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The configuration
     */
    private final CurnConfig config;

    /**
     * The actual cache, indexed by unique ID.
     */
    private Map<String,FeedCacheEntry> cacheByID =
        new ConcurrentHashMap<String,FeedCacheEntry>();

    /**
     * Alternate cache (not saved, but regenerated on the fly), indexed
     * by URL.
     */
    private Map<URL,FeedCacheEntry> cacheByURL =
        new ConcurrentHashMap<URL,FeedCacheEntry>();

    /**
     * Current time
     */
    private long currentTime = System.currentTimeMillis();

    /**
     * For log messages
     */
    private static final Logger log = new Logger (FeedCache.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new, empty cache object.
     *
     * @param config  the <i>curn</i> configuration
     */
    FeedCache (CurnConfig config)
    {
        this.config = config;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Determine whether the cache contains an entry with the specified
     * unique ID.
     *
     * @param id  the ID to check.
     *
     * @return <tt>true</tt> if the ID is present in the cache,
     *         <tt>false</tt> if not
     */
    public boolean containsID(final String id)
    {
        boolean hasKey = cacheByID.containsKey (id);
        log.debug("Cache contains \"" + id + "\"? " + hasKey);
        return hasKey;
    }

    /**
     * Determine whether the cache contains the specified URL.
     *
     * @param url  the URL to check. This method normalizes it.
     *
     * @return <tt>true</tt> if the ID is present in the cache,
     *         <tt>false</tt> if not
     */
    public boolean containsURL(final URL url)
    {
        String  urlKey = CurnUtil.urlToLookupKey(url);
        boolean hasURL = cacheByURL.containsKey(urlKey);
        log.debug("Cache contains \"" + urlKey + "\"? " + hasURL);
        return hasURL;
    }

    /**
     * Get an entry from the cache by its unique ID.
     *
     * @param id  the unique ID to check
     *
     * @return the corresponding <tt>FeedCacheEntry</tt> object, or null if
     *         not found
     */
    public FeedCacheEntry getEntry(final String id)
    {
        return (FeedCacheEntry) cacheByID.get(id);
    }

    /**
     * Get an entry from the cache by its URL.
     *
     * @param url the URL
     *
     * @return the corresponding <tt>FeedCacheEntry</tt> object, or null if
     *         not found
     */
    public FeedCacheEntry getEntryByURL(final URL url)
    {
        return (FeedCacheEntry) cacheByURL.get(CurnUtil.urlToLookupKey(url));
    }

    /**
     * Add (or replace) a cached URL.
     *
     * @param uniqueID   the unique ID string for the cache entry, or null.
     *                   If null, the URL is used as the unique ID.
     * @param url        the URL to cache. May be an individual item URL, or
     *                   the URL for an entire feed.
     * @param pubDate    the publication date, if known; or null
     * @param parentFeed the associated feed
     *
     * @see CurnUtil#normalizeURL
     */
    public void addToCache(String         uniqueID,
                           final URL      url,
                           final Date     pubDate,
                           final FeedInfo parentFeed)
    {
        URL normalizedURL = CurnUtil.normalizeURL(url);

        if (uniqueID == null)
            uniqueID = url.toExternalForm();

        URL parentURL = parentFeed.getURL();
        FeedCacheEntry entry = new FeedCacheEntry(uniqueID,
                                                  parentURL,
                                                  url,
                                                  pubDate,
                                                  System.currentTimeMillis());

        log.debug ("Adding cache entry for URL \"" +
                   entry.getEntryURL().toExternalForm() +
                   "\". ID=\"" + uniqueID + "\", channel URL: \"" +
                   entry.getChannelURL().toExternalForm() +
                   "\"");
        cacheByID.put(uniqueID, entry);
        cacheByURL.put(normalizedURL, entry);
    }

    /**
     * Add a {@link FeedCacheEntry} to the cache. This method exists primarily
     * for use during deserialization of the cache.
     *
     * @param entry  the entry
     */
    public void addFeedCacheEntry(FeedCacheEntry entry)
    {
        cacheByID.put(entry.getUniqueID(), entry);
        cacheByURL.put(entry.getEntryURL(), entry);
    }

    /**
     * Get all entries in the cache, in no particular order.
     *
     * @return a <tt>Collection</tt> of entries
     */
    public Collection<FeedCacheEntry> getAllEntries()
    {
        return Collections.unmodifiableCollection(cacheByID.values());
    }

    /**
     * Set the cache's notion of the current time, which affects how elements
     * are pruned when loaded from the cache. Only meaningful if set before
     * the <tt>load()</tt> method is called. If this method is never
     * called, then the cache uses the current time.
     *
     * @param datetime  the time to use
     */
    public void setCurrentTime (final Date datetime)
    {
        this.currentTime = datetime.getTime();
    }

    /*----------------------------------------------------------------------*\
                            Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Prune the loaded cache of out-of-date data.
     */
    void pruneCache()
    {
        log.debug ("PRUNING CACHE");
        log.debug ("Cache's notion of current time: " +
                   new Date (currentTime));
        Map<URL,FeedInfo> feedInfoMap = config.getFeedInfoMap();

        for (Iterator itKeys = cacheByID.keySet().iterator();
             itKeys.hasNext(); )
        {
            String itemKey = (String) itKeys.next();
            FeedCacheEntry entry = (FeedCacheEntry) cacheByID.get (itemKey);
            URL channelURL = entry.getChannelURL();
            boolean removed = false;

            if (log.isDebugEnabled())
                dumpCacheEntry (itemKey, entry, "");

            FeedInfo feedInfo = feedInfoMap.get(channelURL);

            if (feedInfo == null)
            {
                // Cached URL no longer corresponds to a configured site
                // URL. Kill it.

                log.debug ("Cached item \"" + itemKey +
                           "\", with base URL \"" + channelURL.toString() +
                           "\" no longer corresponds to a configured feed. " +
                           "Tossing it.");
                itKeys.remove();
                removed = true;
            }

            else
            {
                long timestamp  = entry.getTimestamp();
                long maxCacheMS = feedInfo.getMillisecondsToCache();
                long expires    = timestamp + maxCacheMS;

                if (log.isDebugEnabled())
                {
                    log.debug ("    Cache time: " + feedInfo.getDaysToCache() +
                               " days (" + maxCacheMS + " ms)");
                    log.debug ("    Expires: " +
                               new Date (expires).toString());
                }

                if (timestamp > currentTime)
                {
                    log.debug ("Cache time for item \"" + itemKey +
                               "\" is in the future, relative to cache's " +
                               "notion of current time. Setting its " +
                               "timestamp to the current time.");
                    entry.setTimestamp (currentTime);
                }

                else if (expires < currentTime)
                {
                    log.debug ("Cache time for item \"" + itemKey +
                               "\" has expired. Deleting cache entry.");
                    itKeys.remove();
                    removed = true;
                }
            }

            if (! removed)
            {
                // Add to URL cache.

                URL url = CurnUtil.normalizeURL(entry.getEntryURL());
                log.debug("Loading URL \"" + url.toString() +
                          "\" into in-memory lookup cache.");
                cacheByURL.put(url, entry);
            }
        }

        log.debug ("DONE PRUNING CACHE");
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Dump the contents of the cache, via the "debug" log facility.
     *
     * @param label  a label, or initial message, to identify the dump
     */
    private void dumpCache (final String label)
    {
        log.debug ("CACHE DUMP: " + label);
        Set<String> sortedKeys = new TreeSet<String> (cacheByID.keySet());
        for (String itemKey : sortedKeys)
            dumpCacheEntry (itemKey, (FeedCacheEntry) cacheByID.get (itemKey), "");
    }

    /**
     * Dump a single cache entry via the "debug" log facility.
     *
     * @param itemKey the hash table key for the item
     * @param entry   the cache entry
     * @param indent  string to use to indent output, if desired
     */
    private void dumpCacheEntry (final Object         itemKey,
                                 final FeedCacheEntry entry,
                                       String         indent)
    {
        long timestamp  = entry.getTimestamp();

        if (indent == null)
            indent = "";

        log.debug (indent + "Cached item \"" + itemKey.toString() + "\"");
        log.debug (indent + "    Item URL: " + entry.getEntryURL().toString());
        log.debug (indent + "    Channel URL: " +
                   entry.getChannelURL().toString());
        log.debug (indent + "    Cached on: " +
                   new Date (timestamp).toString());
    }

}
