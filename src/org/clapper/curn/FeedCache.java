/*---------------------------------------------------------------------------*\
  $Id$
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

import org.clapper.util.misc.Logger;

/**
 * Defines the in-memory format of the <i>curn</i> cache, and provides
 * methods for saving and restoring the cache.
 *
 * @see curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class FeedCache implements Serializable
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The configuration
     */
    private ConfigFile config;

    /**
     * The actual cache
     */
    private Map cacheMap;

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

        log.debug ("Reading cache from \"" + cacheFile.getPath() + "\"");

        if (! cacheFile.exists())
        {
            log.debug ("Cache \"" + cacheFile.getPath() + "\" doesn't exist.");
            this.cacheMap = new HashMap();
        }

        else
        {
            ObjectInputStream objIn = new ObjectInputStream
                                           (new FileInputStream (cacheFile));

            try
            {
                this.cacheMap = (Map) objIn.readObject();
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
            objOut.writeObject (cacheMap);
            objOut.close();
            this.modified = false;
        }
    }

    /**
     * Determine whether a given ID is cached.
     *
     * @param id  the uniqueID to check.
     *
     * @return <tt>true</tt> if cached, <tt>false</tt> if not
     */
    public boolean contains (String id)
    {
        boolean hasKey = cacheMap.containsKey (id);
        log.debug ("Cache contains \"" + id + "\"? " + hasKey);
        return hasKey;
    }

    /**
     * Get an item from the cache.
     *
     * @param id  the unique ID to check
     *
     * @return the corresponding <tt>FeedCacheEntry</tt> object, or null if
     *         not found
     */
    public FeedCacheEntry getItem (String id)
    {
        return (FeedCacheEntry) cacheMap.get (id);
    }

    /**
     * Add (or replace) a cached URL.
     *
     * @param uniqueID   the unique ID string for the cache entry
     * @param url        the URL to cache. May be an individual item URL, or
     *                   the URL for an entire feed.
     * @param parentFeed the associated feed
     *
     * @see Util#normalizeURL
     */
    public void addToCache (String uniqueID, URL url, FeedInfo parentFeed)
    {
        URL parentURL = parentFeed.getURL();
        FeedCacheEntry entry = new FeedCacheEntry (uniqueID,
                                                 parentURL,
                                                 url,
                                                 System.currentTimeMillis());

        log.debug ("Adding cache entry for URL \""
                  + entry.getEntryURL().toExternalForm()
                  + "\". Channel URL: \""
                  + entry.getChannelURL().toExternalForm()
                  + "\"");
        cacheMap.put (uniqueID, entry);
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

        for (Iterator itKeys = cacheMap.keySet().iterator();
             itKeys.hasNext(); )
        {
            String itemKey = (String) itKeys.next();
            FeedCacheEntry entry = (FeedCacheEntry) cacheMap.get (itemKey);
            URL channelURL = entry.getChannelURL();

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
                    modified = true;
                }

                else if (expires < currentTime)
                {
                    log.debug ("Cache time for item \""
                             + itemKey
                             + "\" has expired. Deleting cache entry.");
                    itKeys.remove();
                    modified = true;
                }
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
        Set sortedKeys = new TreeSet (cacheMap.keySet());
        for (Iterator itKeys = sortedKeys.iterator(); itKeys.hasNext(); )
        {
            String itemKey = (String) itKeys.next();
            dumpCacheEntry (itemKey, (FeedCacheEntry) cacheMap.get (itemKey),
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
