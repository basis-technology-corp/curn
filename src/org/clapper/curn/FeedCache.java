/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004 Brian M. Clapper. All rights reserved.

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

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.clapper.curn.util.Util;

import org.clapper.util.misc.Logger;

/**
 * Defines the in-memory format of the <i>curn</i> cache, and provides
 * methods for saving and restoring the cache.
 *
 * @see Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class FeedCache implements Serializable
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The configuration
     */
    private ConfigFile config;

    /**
     * The actual cache, indexed by unique ID.
     */
    private Map cacheByID;

    /**
     * Alternate cache (not saved, but regenerated on the fly), indexed
     * by URL.
     */
    private Map cacheByURL;

    /**
     * Whether or not the cache has been modified since saved or loaded
     */
    private boolean modified = false;

    /**
     * Current time
     */
    private long currentTime = System.currentTimeMillis();

    /**
     * For log messages
     */
    private static Logger log = new Logger (FeedCache.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new, empty cache object.
     *
     * @param config  the <i>curn</i> configuration
     */
    FeedCache (ConfigFile config)
    {
        this.config = config;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Load the cache from the file specified in the configuration. If the
     * file doesn't exist, this method quietly returns.
     *
     * @throws IOException  unable to read cache
     */
    public void loadCache()
        throws IOException
    {
        File cacheFile = config.getCacheFile();
        this.cacheByURL = new HashMap();

        log.debug ("Reading cache from \"" + cacheFile.getPath() + "\"");

        if (! cacheFile.exists())
        {
            log.debug ("Cache \"" + cacheFile.getPath() + "\" doesn't exist.");
            this.cacheByID  = new HashMap();
        }

        else
        {
            ObjectInputStream objIn = new ObjectInputStream
                                           (new FileInputStream (cacheFile));

            try
            {
                this.cacheByID = (Map) objIn.readObject();
                if (log.isDebugEnabled())
                    dumpCache ("before pruning");
                pruneCache();
                if (log.isDebugEnabled())
                    dumpCache ("after pruning");
                modified = false;
            }

            catch (ClassNotFoundException ex)
            {
                throw new IOException (ex.toString());
            }

            finally
            {
                objIn.close();
            }
        }
    }

    /**
     * Attempt to save the cache back to disk. Does nothing if the cache
     * hasn't been modified since it was saved.
     *
     * @throws IOException  unable to write cache
     */
    public void saveCache()
        throws IOException
    {
        if (this.modified)
        {
            File cacheFile = config.getCacheFile();

            ObjectOutputStream objOut = new ObjectOutputStream
                                           (new FileOutputStream (cacheFile));

            log.debug ("Saving cache to \"" + cacheFile.getPath() + "\"");
            objOut.writeObject (cacheByID);
            objOut.close();
            this.modified = false;
        }
    }

    /**
     * Determine whether the cache contains an entry with the specified
     * unique ID.
     *
     * @param id  the ID to check.
     *
     * @return <tt>true</tt> if the ID is present in the cache,
     *         <tt>false</tt> if not
     */
    public boolean containsID (String id)
    {
        boolean hasKey = cacheByID.containsKey (id);
        log.debug ("Cache contains \"" + id + "\"? " + hasKey);
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
    public boolean containsURL (URL url)
    {
        String  urlKey = Util.urlToLookupKey (url);
        boolean hasURL = cacheByURL.containsKey (urlKey);
        log.debug ("Cache contains \"" + urlKey + "\"? " + hasURL);
        return hasURL;
    }

    /**
     * Get an item from the cache by its unique ID.
     *
     * @param id  the unique ID to check
     *
     * @return the corresponding <tt>FeedCacheEntry</tt> object, or null if
     *         not found
     */
    public FeedCacheEntry getItem (String id)
    {
        return (FeedCacheEntry) cacheByID.get (id);
    }

    /**
     * Get an item from the cache by its URL.
     *
     * @param url the URL
     *
     * @return the corresponding <tt>FeedCacheEntry</tt> object, or null if
     *         not found
     */
    public FeedCacheEntry getItemByURL (URL url)
    {
        return (FeedCacheEntry) cacheByURL.get (Util.urlToLookupKey (url));
    }

    /**
     * Add (or replace) a cached URL.
     *
     * @param uniqueID   the unique ID string for the cache entry, or null.
     *                   If null, the URL is used as the unique ID.
     * @param url        the URL to cache. May be an individual item URL, or
     *                   the URL for an entire feed.
     * @param parentFeed the associated feed
     *
     * @see Util#normalizeURL
     */
    public synchronized void addToCache (String   uniqueID,
                                         URL      url,
                                         FeedInfo parentFeed)
    {
        String urlKey = Util.urlToLookupKey (url);

        if (uniqueID == null)
            uniqueID = urlKey;

        URL parentURL = parentFeed.getURL();
        FeedCacheEntry entry = new FeedCacheEntry (uniqueID,
                                                 parentURL,
                                                 url,
                                                 System.currentTimeMillis());

        log.debug ("Adding cache entry for URL \""
                  + entry.getEntryURL().toExternalForm()
                  + "\". ID=\""
                  + uniqueID
                  + "\", channel URL: \""
                  + entry.getChannelURL().toExternalForm()
                  + "\"");
        cacheByID.put (uniqueID, entry);
        cacheByURL.put (urlKey, entry);
        modified = true;
    }

    /**
     * Set the cache's notion of the current time, which affects how elements
     * are pruned when loaded from the cache. Only meaningful if set before
     * the <tt>loadCache()</tt> method is called. If this method is never
     * called, then the cache uses the current time.
     *
     * @param datetime  the time to use
     */
    public void setCurrentTime (Date datetime)
    {
        this.currentTime = datetime.getTime();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Prune the loaded cache of out-of-date data.
     */
    private void pruneCache()
    {
        log.debug ("PRUNING CACHE");
        log.debug ("Cache's notion of current time: "
                 + new Date (currentTime));

        for (Iterator itKeys = cacheByID.keySet().iterator();
             itKeys.hasNext(); )
        {
            String itemKey = (String) itKeys.next();
            FeedCacheEntry entry = (FeedCacheEntry) cacheByID.get (itemKey);
            URL channelURL = entry.getChannelURL();
            boolean removed = false;

            if (log.isDebugEnabled())
                dumpCacheEntry (itemKey, entry, "");

            FeedInfo feedInfo = config.getFeedInfoFor (channelURL);

            if (feedInfo == null)
            {
                // Cached URL no longer corresponds to a configured site
                // URL. Kill it.

                log.debug ("Cached item \""
                         + itemKey
                         + "\", with base URL \""
                         + channelURL.toString()
                         + "\" no longer corresponds to a configured feed. "
                         + "Tossing it.");
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
                    log.debug ("    Cache time: "
                             + feedInfo.getDaysToCache()
                             + " days ("
                             + maxCacheMS
                             + " ms)");
                    log.debug ("    Expires: "
                             + new Date (expires).toString());
                }

                if (timestamp > currentTime)
                {
                    log.debug ("Cache time for item \""
                             + itemKey
                             + "\" is in the future, relative to cache's "
                             + "notion of current time. Setting its "
                             + "timestamp to the current time.");
                    entry.setTimestamp (currentTime);
                    this.modified = true;
                }

                else if (expires < currentTime)
                {
                    log.debug ("Cache time for item \""
                             + itemKey
                             + "\" has expired. Deleting cache entry.");
                    itKeys.remove();
                    this.modified = true;
                    removed = true;
                }
            }

            if (! removed)
            {
                // Add to URL cache.

                String urlKey = Util.urlToLookupKey (entry.getEntryURL());
                log.debug ("Loading URL \""
                         + urlKey
                         + "\" into in-memory lookup cache.");
                cacheByURL.put (urlKey, entry);
            }
        }

        log.debug ("DONE PRUNING CACHE");
    }

    /**
     * Dump the contents of the cache, via the "debug" log facility.
     *
     * @param label  a label, or initial message, to identify the dump
     */
    private void dumpCache (String label)
    {
        log.debug ("CACHE DUMP: " + label);
        Set sortedKeys = new TreeSet (cacheByID.keySet());
        for (Iterator itKeys = sortedKeys.iterator(); itKeys.hasNext(); )
        {
            String itemKey = (String) itKeys.next();
            dumpCacheEntry (itemKey, (FeedCacheEntry) cacheByID.get (itemKey),
                            "");
        }
    }   

    /**
     * Dump a single cache entry via the "debug" log facility.
     *
     * @param itemKey the hash table key for the item
     * @param entry   the cache entry
     * @param indent  string to use to indent output, if desired
     */
    private void dumpCacheEntry (Object         itemKey ,
                                 FeedCacheEntry entry,
                                 String         indent)
    {
        long timestamp  = entry.getTimestamp();

        if (indent == null)
            indent = "";

        log.debug (indent + "Cached item \"" + itemKey.toString() + "\"");
        log.debug (indent + "    Item URL: " + entry.getEntryURL().toString());
        log.debug (indent + "    Channel URL: "
                 + entry.getChannelURL().toString());
        log.debug (indent + "    Cached on: "
                 + new Date (timestamp).toString());
    }
}
