/*---------------------------------------------------------------------------*\
 $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A <tt>PersistentFeedItemData</tt> object contains data and metadata about
 * a perticular item within a feed, in a form suitable for saving to and
 * restoring from an external store. The data are organized to make persistence
 * operations easier. At runtime, the data are reorganized; see the
 * {@link PersistentFeedData} class for more inforamtion.
 *
 * @see FeedCache
 * @see DataPersister
 * @see PersistentFeedData
 *
 * @version <tt>$Revision$</tt>
 */
public class PersistentFeedItemData
{
    /*----------------------------------------------------------------------*\
                               Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                             Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The FeedCacheEntry for this item
     */
    private FeedCacheEntry feedCacheEntry = null;

    /**
     * Extra metadata associated with the item.
     */
    private Set<PersistentMetadataGroup> itemMetadata =
        new HashSet<PersistentMetadataGroup>();

    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new instance of PersistentFeedData
     *
     * @param feedCacheEntry the {@link FeedCacheEntry} for the item
     */
    public PersistentFeedItemData(FeedCacheEntry feedCacheEntry)
    {
        this.feedCacheEntry = feedCacheEntry;
    }

    /*----------------------------------------------------------------------*\
                                Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the {@link FeedCacheEntry} for the item
     *
     * @return the {@link FeedCacheEntry}
     */
    public FeedCacheEntry getFeedCacheEntry()
    {
        return feedCacheEntry;
    }

    /**
     * Add a metadata group (i.e., all the metadata within a given
     * namespace) to this object.
     *
     * @param metadataGroup the group of metadata
     */
    public void addItemMetadataGroup(PersistentMetadataGroup metadataGroup)
    {
         itemMetadata.add(metadataGroup);
    }

    /**
     * Add a metadata group (i.e., all the metadata within a given
     * namespace) to this object.
     *
     * @param metadata the metadata
     */
    public void addItemMetadata(Collection<PersistentMetadataGroup> metadata)
    {
        for (PersistentMetadataGroup metadataGroup : metadata)
            addItemMetadataGroup(metadataGroup);
    }

    /**
     * Get the extra metadata associated with the feed. The returned data
     * is aggregated into individual namespaces.
     *
     * @return a <tt>Collection</tt> of {@link PersistentMetadataGroup}
     *         objects, each one containing the data for one namespace.
     */
    public Collection<PersistentMetadataGroup> getItemMetadata()
    {
        Collection<PersistentMetadataGroup> result = null;

        if (itemMetadata != null)
            result = Collections.unmodifiableCollection(itemMetadata);

        return result;
    }

    /*----------------------------------------------------------------------*\
                               Protected Methods
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                Private Methods
    \*----------------------------------------------------------------------*/
}
