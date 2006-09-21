/*---------------------------------------------------------------------------*\
 $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.util.Map;

/**
 * A <tt>FeedMetaDataClient</tt> is a class that wants to persist its own
 * data in the {@link FeedMetaData} store. A <tt>FeedMetaDataClient</tt>
 * must register itself, via a {@link FeedMetaDataRegistry} object.
 * The <tt>FeedMetaData</tt> class implements the
 * <tt>FeedMetaDataRegistry</tt>, and the {@link Curn} driver currently
 * passes the instantiated <tt>FeedMetaData</tt> object to all loaded
 * plug-ins during initialization, permitting each plug-in to register itself
 * as a metadata handler, if necessary.
 *
 * @version <tt>$Revision$</tt>
 */
public interface FeedMetaDataClient
{
    /**
     * Process a data item that has been read from the metadata store.
     * This method is called when the metadata store is being loaded
     * into memory at the beginning of a <i>curn</i> run.
     *
     * @param name   the name associated with the data item
     * @param value  the (string) value of the data
     *
     * @throws CurnException on error
     */
    public void processDataItem(String name, String value)
        throws CurnException;

    /**
     * Retrieve all data items that are to be written to the metadata store.
     * This method is called when <i>curn</i> is saving the metadata back
     * to the metadata store, prior to shutting down.
     *
     * @return a <tt>Map</tt> of name/value pairs to be stored.
     *
     * @throws CurnException on error
     */
    public Map getDataItems()
        throws CurnException;

    /**
     * Get the namespace for this object's metadata. The namespace must
     * be unique. Think of it as a package name for the data. Recommendation:
     * Use the fully-qualified class name.
     *
     * @return the namespace
     */
    public String getNameSpace();
}
