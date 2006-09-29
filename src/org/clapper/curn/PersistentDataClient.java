/*---------------------------------------------------------------------------*\
 $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.util.Map;

/**
 * <p>A <tt>PersistentDataClient</tt> is a class that wants to persist its own
 * data in the persisted <i>curn</i> data store. A 
 * <tt>PersistentDataClient</tt> object can store and retrieve three kinds of 
 * data in the store:
 *
 * <ol>
 *   <li> Feed-related metadata, i.e., data that relates to a feed
 *        or channel.
 *   <li> Item-related metadata, i.e., data that relates to an item
 *        within a feed.
 *   <li> "Extra" data, i.e., data that is to be persisted but that isn't
 *        specifically related to a feed or an item.
 * </ol>
 *
 * <p>Each <tt>PersistentDataClient</tt> object has its own namespace, to
 * ensure that its variable names don't clash with other data. The namespace
 * name is supplied by the <tt>PersistentDataClient</tt> itself; the
 * fully-qualified class name is typically a good choice for a namespace
 * name.</p>
 *
 * <p>A <tt>PersistentDataClient</tt> must register itself with the
 * {@link DataPersister} class to activate itself; once registered, the
 * client will be invoked automatically during the appropriate phases
 * of execution. <b>Note:</b> Plug-in classes that implement this interface are 
 * automatically registered during plug-in discovery.</p>
 *
 * <p>When saving the feed meta data, <i>curn</i> polls each registered
 * <tt>PersistentDataClient</tt> for its data.</p>
 *
 * @version <tt>$Revision$</tt>
 */
public interface PersistentDataClient
{
    /**
     * Process a data item that has been read from the metadata store
     * and is associated with a feed (or channel). This method is
     * called when the metadata store is being loaded into memory
     * at the beginning of a <i>curn</i> run. This method is only called
     * for data items within this object's name space.
     *
     *
     * @param name     the name associated with the data item
     * @param value    the (string) value of the data
     * @param feedData the {@link FeedCacheEntry} record for the feed
     * @see #getMetatdataNamespace
     * @throws CurnException on error
     */
    public void parseFeedMetadata(String         name,
                                  String         value,
                                  FeedCacheEntry feedData)
        throws CurnException;

    /**
     * Process a data item that has been read from the metadata store
     * and is associated with a cached item. This method is called when
     * the metadata store is being loaded into memory at the beginning
     * of a <i>curn</i> run. This method is only called for data items
     * within this object's name space.
     *
     * @param name     the name associated with the data item
     * @param value    the (string) value of the data
     * @param itemData The {@link FeedCacheEntry} data for the item
     *
     * @throws CurnException on error
     *
     * @see #getMetatdataNamespace
     */
    public void parseItemMetadata(String         name,
                                  String         value,
                                  FeedCacheEntry itemData)
        throws CurnException;

    /**
     * Process an "extra" data item that is not associated with a feed
     * or an item. This method is called when the metadata store is
     * being loaded into memory at the beginning of a <i>curn</i> run.
     * This method is only called for data items within this object's name
     * space.
     *
     * @param name  the name of the data item
     * @param value its value
     *
     * @throws CurnException on error
     *
     * @see #getMetatdataNamespace
     */
    public void parseExtraMetadata(String name, String value)
        throws CurnException;

    /**
     * Get the metadata that is to be saved with a particular feed or channel.
     *
     * @param feedData the {@link FeedCacheEntry} record for the feed
     *
     * @return a <tt>Map</tt> of all the name/value pairs to be associated
     *         with the feed. The names should not be qualified by the
     *         namespace; the caller will handle that. An empty or null
     *         map signifies that this object has no metadata for the feed.
     *
     * @throws CurnException on error
     */
    public Map<String,String> getMetadataForFeed(FeedCacheEntry feedData)
       throws CurnException;

    /**
     * Get the metadata that is to be saved with a particular item within a
     * feed.
     *
     * @param itemData the {@link FeedCacheEntry} record for the item
     * @param feedData the {@link FeedCacheEntry} record for the parent feed
     *
     * @return a <tt>Map</tt> of all the name/value pairs to be associated
     *         with the  item. The names should not be qualified by the
     *         namespace; the caller will handle that. An empty or null
     *         map signifies that this object has no metadata for the item.
     *
     * @throws CurnException on error
     */
    public Map<String,String> getMetadataForItem(FeedCacheEntry itemData,
                                                 FeedCacheEntry feedData)
       throws CurnException;

    /**
     * Get any extra metadata (i.e., data that is not associated with a feed
     * or an item) that is to be saved.
     *
     * @return a <tt>Map</tt> of all the name/value pairs to be associated
     *         with the feed. The names should not be qualified by the
     *         namespace; the caller will handle that. An empty or null
     *         map signifies that this object has no extract metadata.
     *
     * @throws CurnException on error
     */
    public Map<String,String> getExtraFeedMetadata()
       throws CurnException;

    /**
     * Get the namespace for this object's metadata. The namespace must
     * be unique. Think of it as a package name for the data. Recommendation:
     * Use the fully-qualified class name.
     *
     * @return the namespace
     */
    public String getMetatdataNamespace();
}
