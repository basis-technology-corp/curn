/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2012 Brian M. Clapper.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.clapper.util.logging.Logger;

/**
 * Persists data to the <i>curn</i> persistent data store, whatever that may
 * be.
 *
 * @version <tt>$Revision$</tt>
 */
public abstract class DataPersister
{
    /*----------------------------------------------------------------------*\
                             Public Interfaces
    \*----------------------------------------------------------------------*/

    /**
     * Used to define a callback for handling loaded data.
     */
    public interface LoadedDataHandler
    {
        /**
         * Called by the subclass when it has finished loading data for a feed.
         *
         * @param feedData the loaded feed (and item) data
         *
         * @throws CurnException on error
         */
        public void feedLoaded(PersistentFeedData feedData)
            throws CurnException;

        /**
         * Called by the subclass when it has finished loading the extra
         * metadata for a particular namespace.
         *
         * @param metadataGroup  the metadata for the namespace
         *
         * @throws CurnException on error
         */
        public void extraMetadataLoaded(PersistentMetadataGroup metadataGroup)
            throws CurnException;
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * Map of interested PersistentDataClient objects, indexed by namespace
     */
    private Map<String, PersistentDataClient> persistentDataClients =
        new HashMap<String,PersistentDataClient>();

    /**
     * For logging
     */
    private static final Logger log = new Logger(DataPersister.class);

    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    protected DataPersister()
    {
    }

    /*----------------------------------------------------------------------*\
                                Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Save the feed metadata. The configuration is passed in, so that
     * the persister can obtain, from the configuration, whatever
     * data it needs to find the persisted metadata to read.
     *
     * @param feedCache {@link FeedCache} object to save
     *
     * @throws CurnException on error
     */
    public final void saveData(FeedCache feedCache)
        throws CurnException
    {
        if (isEnabled())
        {
            // First, retrieve all entries from the cache and reorganize them.

            Collection<FeedCacheEntry> cacheEntries = feedCache.getAllEntries();
            Map<URL, PersistentFeedData> cacheDataByFeed =
                getCacheDataByFeed(cacheEntries);

            // Now that everything's in the right order, gather the additional
            // metadata for each feed and its items. We don't need the map
            // any more.

            Collection<PersistentFeedData> persistentDataByFeed =
                cacheDataByFeed.values();
            cacheDataByFeed = null;

            for (PersistentFeedData feedData : persistentDataByFeed)
                getFeedMetadataForFeed(feedData);

            // Now, gather any extra metadata that isn't attached to a feed or
            // item.

            Collection<PersistentMetadataGroup> extraMetadata =
                new ArrayList<PersistentMetadataGroup>();

            for (PersistentDataClient client : persistentDataClients.values())
            {
                String namespace = client.getMetatdataNamespace();
                PersistentMetadataGroup metadata;
                Map<String,String> nameValuePairs = client.getExtraFeedMetadata();
                if ((nameValuePairs != null) && (nameValuePairs.size() > 0))
                {
                    metadata = new PersistentMetadataGroup(namespace);
                    metadata.addMetadata(nameValuePairs);
                    extraMetadata.add(metadata);
                }
            }

            // Let the saving begin.

            startSaveOperation();

            for (PersistentFeedData feedData : persistentDataByFeed)
                saveFeedData(feedData);

            saveExtraMetadata(extraMetadata);
            endSaveOperation();
        }
    }

    /**
     * Load the cache and metadata.
     *
     * @param feedCache the {@link FeedCache} object to fill
     *
     * @throws CurnException on error
     */
    public void loadData(final FeedCache feedCache)
        throws CurnException
    {
        if (isEnabled())
        {
            startLoadOperation();

            doLoad(new LoadedDataHandler()
            {
                public void feedLoaded(PersistentFeedData feedData)
                    throws CurnException
                {
                    processLoadedFeed(feedData, feedCache);
                }

                public void
                extraMetadataLoaded(PersistentMetadataGroup metadataGroup)
                    throws CurnException
                {
                    String namespace = metadataGroup.getNamespace();
                    PersistentDataClient client =
                        persistentDataClients.get(namespace);
                    if (client == null)
                    {
                        log.warn("No plug-in or other class has registered " +
                                 "interest in extra metadata namespace \"" +
                                 namespace + "\". " + "Ignoring the metadata.");
                    }

                    else
                    {
                        Map<String,String> nameValuePairs =
                            metadataGroup.getMetadata();
                        for (Map.Entry<String,String> entry :
                             nameValuePairs.entrySet())
                        {
                            client.parseExtraMetadata(entry.getKey(),
                                                      entry.getValue());
                        }
                    }
                }
            });

            endLoadOperation();
            feedCache.optimizeAfterLoad();
        }
    }

    /**
     * Register a {@link PersistentDataClient} object with this registry.
     * When data for that client is read, this object will call the
     * client's <tt>process</tt> methods. When it's time to save the metadata,
     * this object will call the client's <tt>get</tt> methods. Multiple
     * {@link PersistentDataClient} objects may be registered with this object.
     *
     * @param client the {@link PersistentDataClient} object
     */
    public final void addPersistentDataClient(PersistentDataClient client)
    {
        persistentDataClients.put(client.getMetatdataNamespace(), client);
    }

    /**
     * Called when the <tt>DataPersister</tt> is first instantiated. Useful
     * for retrieving configuration values, etc.
     *
     * @param curnConfig  the configuration
     *
     * @throws CurnException on error
     */
    public abstract void init(CurnConfig curnConfig)
        throws CurnException;

    /*----------------------------------------------------------------------*\
                              Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Determine whether the data persister subclass is enabled or not (i.e.,
     * whether or not metadata is to be loaded and saved). The configuration
     * usually determines whether or not the data persister is enabled.
     *
     * @return <tt>true</tt> if enabled, <tt>false</tt> if disabled.
     */
    protected abstract boolean isEnabled();

    /**
     * Called at the beginning of the load operation to initialize
     * the load.
     *
     * @throws CurnException on error
     */
    protected abstract void startLoadOperation()
        throws CurnException;

    /**
     * Called at the end of the load operation to close files, clean
     * up, etc.
     *
     * @throws CurnException on error
     */
    protected abstract void endLoadOperation()
        throws CurnException;

    /**
     * The actual load method; only called if the object is enabled.
     *
     * @param loadedDataHandler object to receive data as it's loaded
     *
     * @throws CurnException on error
     */
    protected abstract void doLoad(LoadedDataHandler loadedDataHandler)
        throws CurnException;

    /**
     * Called at the beginning of the actual save operation to initialize
     * the save, etc.
     *
     * @throws CurnException on error
     */
    protected abstract void startSaveOperation()
        throws CurnException;

    /**
     * Called at the end of the actual save operation to flush files, clean
     * up, etc.
     *
     * @throws CurnException on error
     */
    protected abstract void endSaveOperation()
        throws CurnException;

    /**
     * Save the data for one feed, including the items.
     *
     * @param feedData  the feed data to be saved
     *
     * @throws CurnException on error
     */
    protected abstract void saveFeedData(PersistentFeedData feedData)
        throws CurnException;

    /**
     * Save any extra metadata (i.e., metadata that isn't attached to a
     * specific feed or a specific item).
     *
     * @param metadata the collection of metadata items
     *
     * @throws CurnException on error
     */
    protected abstract void
    saveExtraMetadata(Collection<PersistentMetadataGroup> metadata)
        throws CurnException;

    /*----------------------------------------------------------------------*\
                                Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the persistent metadata for one feed. Also handles getting
     * the data for the items.
     *
     * @param feedData the PersistentFeedData object into which to store
     *                 the metadata
     *
     * @throws CurnException on error
     */
    private void getFeedMetadataForFeed(PersistentFeedData feedData)
        throws CurnException
    {
        for (PersistentDataClient client : persistentDataClients.values())
        {
            String namespace = client.getMetatdataNamespace();
            PersistentMetadataGroup metadata;

            // First the feed-specific metadata

            FeedCacheEntry feedCacheEntry = feedData.getFeedCacheEntry();
            Map<String,String> nameValuePairs =
                client.getMetadataForFeed(feedCacheEntry);
            if ((nameValuePairs != null) && (nameValuePairs.size() > 0))
            {
                metadata = new PersistentMetadataGroup(namespace);
                metadata.addMetadata(nameValuePairs);
                feedData.addFeedMetadataGroup(metadata);
            }

            // Now the metadata for each item.

            for (PersistentFeedItemData itemData : feedData.getPersistentFeedItems())
            {
                FeedCacheEntry itemCacheEntry = itemData.getFeedCacheEntry();
                nameValuePairs = client.getMetadataForItem(itemCacheEntry,
                                                               feedCacheEntry);
                if ((nameValuePairs != null) && (nameValuePairs.size() > 0))
                {
                    metadata = new PersistentMetadataGroup(namespace);
                    metadata.addMetadata(nameValuePairs);
                    itemData.addItemMetadataGroup(metadata);
                }
            }
        }
    }

    private Map<URL,PersistentFeedData>
    getCacheDataByFeed(final Collection<FeedCacheEntry> cacheEntries)
    {

        Map<URL,PersistentFeedData> cacheDataByFeed =
            new HashMap<URL, PersistentFeedData>();

        for (FeedCacheEntry entry : cacheEntries)
        {
            URL channelURL = entry.getChannelURL();
            PersistentFeedData feedData = cacheDataByFeed.get(channelURL);
            if (feedData == null)
            {
                feedData = new PersistentFeedData();
                cacheDataByFeed.put(channelURL, feedData);
            }

            if (entry.isChannelEntry())
            {
                feedData.setFeedCacheEntry(entry);
            }

            else // It's an item entry
            {
                PersistentFeedItemData itemData =
                    new PersistentFeedItemData(entry);
                feedData.addPersistentFeedItem(itemData);
            }
        }
        return cacheDataByFeed;
    }

    private void processLoadedFeed(final PersistentFeedData feedData,
                                   final FeedCache          feedCache)
        throws CurnException
    {
        // Add the feed cache entry to the feed cache.

        FeedCacheEntry feedCacheEntry = feedData.getFeedCacheEntry();
        log.debug("processLoadedFeed: Processing loaded feed data for " +
                  feedCacheEntry.getChannelURL());
        feedCache.loadFeedCacheEntry(feedCacheEntry);

        // Dispatch the feed metadata to the appropriate places.

        for (PersistentMetadataGroup mg : feedData.getFeedMetadata())
        {
            String namespace = mg.getNamespace();
            PersistentDataClient client =
                persistentDataClients.get(namespace);
            if (client == null)
            {
                log.warn("No plug-in or other class has registered " +
                         "interest in feed metadata namespace \"" +
                         namespace + "\". " + "Ignoring the metadata.");
            }

            else
            {
                log.debug("Dispatching feed metadata in namespace \"" +
                          namespace + "\" to instance of class " +
                          client.getClass().toString());
                Map<String,String> nameValuePairs = mg.getMetadata();
                for (Map.Entry<String,String> entry : nameValuePairs.entrySet())
                {
                    client.parseFeedMetadata(entry.getKey(),
                                             entry.getValue(),
                                             feedCacheEntry);
                }
            }
        }

        // Process the items.

        for (PersistentFeedItemData itemData : feedData.getPersistentFeedItems())
        {
            // Add the item's feed cache entry to the cache.

            FeedCacheEntry itemCacheEntry = itemData.getFeedCacheEntry();
            log.debug("processLoadedFeed: adding item " + 
                      itemData.getFeedCacheEntry().getEntryURL() +
                      " to cache.");
            feedCache.loadFeedCacheEntry(itemCacheEntry);
            log.debug("processLoadedFeed: Processing item metadata");

            // Now process the metadata.

            for (PersistentMetadataGroup mg : feedData.getFeedMetadata())
            {
                String namespace = mg.getNamespace();
                PersistentDataClient client =
                    persistentDataClients.get(namespace);
                if (client == null)
                {
                    log.warn("No plug-in or other class has registered " +
                             "interest in item metadata namespace \"" +
                             namespace + "\". " + "Ignoring the metadata.");
                }

                else
                {
                    log.debug("Dispatching item metadata in namespace \"" +
                              namespace + "\" to instance of class " +
                              client.getClass().toString());
                    Map<String,String> nameValuePairs = mg.getMetadata();
                    for (Map.Entry<String,String> entry : nameValuePairs.entrySet())
                    {
                        client.parseItemMetadata(entry.getKey(),
                                                 entry.getValue(),
                                                 itemCacheEntry);
                    }
                }
            }
        }
    }
}
