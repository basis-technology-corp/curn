/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Defines the in-memory format of the <i>rssget</i> cache, and provides
 * methods for saving and restoring the cache.
 *
 * @see rssget
 * @see RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSGetCache implements Serializable
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * Where to emit verbose messages
     */
    private VerboseMessagesHandler vh;

    /**
     * The configuration
     */
    private RSSGetConfiguration config;

    /**
     * The actual cache
     */
    private Map cacheMap;

    /**
     * Whether or not the cache has been modified since saved or loaded
     */
    private boolean modified = false;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new, empty cache object.
     *
     * @param verboseHandler  the verbose messages handler to use
     * @param config          the <i>rssget</i> configuration
     */
    RSSGetCache (VerboseMessagesHandler vh,
                 RSSGetConfiguration    config)
    {
        this.vh     = vh;
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

        vh.verbose (3, "Reading cache from \"" + cacheFile.getPath() + "\"");

        if (! cacheFile.exists())
        {
            vh.verbose (1,
                        "Cache \""
                      + cacheFile.getPath()
                      + "\" doesn't exist.");
            this.cacheMap = new HashMap();
        }

        else
        {
            ObjectInputStream objIn = new ObjectInputStream
                                           (new FileInputStream (cacheFile));

            try
            {
                this.cacheMap = (Map) objIn.readObject();
                pruneCache();
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

            vh.verbose (3, "Saving cache to \"" + cacheFile.getPath() + "\"");
            objOut.writeObject (cacheMap);
            objOut.close();
            this.modified = false;
        }
    }

    /**
     * Determine whether a given item's URL is cached.
     *
     * @param url  the URL to check
     *
     * @return <tt>true</tt> if cached, <tt>false</tt> if not
     */
    public boolean containsItemURL (URL itemURL)
    {
        String key = itemURL.toExternalForm();
        vh.verbose (3,
                    "Cache contains \""
                  + key
                  + "\"? "
                  + cacheMap.containsKey (key));
        return cacheMap.containsKey (key);
    }

    /**
     * Add (or replace) a cached RSS item.
     *
     * @param itemURL       the URL for the item
     * @param parentChannel the parent RSS channel
     */
    public void addToCache (URL itemURL, RSSChannel parentChannel)
    {
        RSSCacheEntry entry = new RSSCacheEntry (parentChannel.getURL(),
                                                 itemURL,
                                                 System.currentTimeMillis());

        vh.verbose (3,
                    "Adding cache entry for URL \""
                  + entry.getItemURL().toExternalForm()
                  + "\". Channel URL: \""
                  + entry.getChannelURL().toExternalForm()
                  + "\"");
        cacheMap.put (itemURL.toExternalForm(), entry);
        modified = true;
    }


    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Prune the loaded cache of out-of-date data.
     */
    private void pruneCache()
    {
        long now = System.currentTimeMillis();

        for (Iterator itKeys = cacheMap.keySet().iterator();
             itKeys.hasNext(); )
        {
            String itemUrlString = (String) itKeys.next();
            RSSCacheEntry entry = (RSSCacheEntry) cacheMap.get (itemUrlString);
            URL channelURL = entry.getChannelURL();

            vh.verbose (3, "Checking cached URL \"" + itemUrlString + "\"");
            vh.verbose (3, "    Channel URL: " + channelURL.toString());

            RSSFeedInfo feedInfo = config.getFeedInfoFor (channelURL);

            if (feedInfo == null)
            {
                // Cached URL no longer corresponds to a configured site
                // URL. Kill it.

                vh.verbose (2,
                            "Cached URL \""
                          + itemUrlString
                          + "\", with base URL \""
                          + channelURL.toString()
                          + "\" no longer corresponds to a configured feed. "
                          + "tossing it.");
                itKeys.remove();
            }

            else
            {
                long timestamp  = entry.getTimestamp();
                long maxCacheMS = feedInfo.getMillisecondsToCache();
                long expires    = timestamp + maxCacheMS;

                vh.verbose (3,
                            "\tcached on: " + new Date (timestamp).toString());
                vh.verbose (3, "\texpires: " + new Date (expires).toString());

                if (expires < now)
                {
                    vh.verbose (2,
                                "Cache time for URL \""
                              + itemUrlString
                              + "\" has expired. Deleting cache entry.");
                    itKeys.remove();
                }
            }
        }
    }

}
