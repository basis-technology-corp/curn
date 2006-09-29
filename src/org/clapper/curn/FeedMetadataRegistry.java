/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

/**
 * Defines the interface for an object that will:
 *
 * <ul>
 *   <li>call {@link FeedMetaDataClient} objects with their loaded metadata
 *   <li>retrieve metadata to be saved from {@link FeedMetaDataClient} objects
 * </ul>
 *
 * <tt>FeedMetaDataClient</tt> objects register themselves with an object
 * that implements this interface. This interface exists primarily to
 * constrain the view of the {@link FeedMetaData} class, which currently is
 * the only class that implements this interface.
 *
 * @version <tt>$Revision$</tt>
 */
public interface FeedMetaDataRegistry
{
    /**
     * Register a {@link FeedMetaDataClient} object with this registry.
     * When data for that client is read, this object will call the
     * client's {@link FeedMetaDataClient#processDataItem processDataItem}
     * method. When it's time to save the metadata, this object will call
     * the client's {@link FeedMetaDataClient#getDataItems getDataItems}
     * method. Multiple {@link FeedMetaDataClient} objects may be registered
     * with this object.
     *
     * @param client the {@link FeedMetaDataClient} object
     *
     * @throws CurnException on error
     */
    public void addFeedMetaDataClient(FeedMetaDataClient client)
        throws CurnException;
}
