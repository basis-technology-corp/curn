/*---------------------------------------------------------------------------*\
 $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains one group of persistent metadata about a feed or an item.
 * Persistent metadata consists of name/value pairs aggregated into namespaces.
 * An instance of this class represents one namespace's worth of metadata for
 * a feed or an item.
 *
 * @version <tt>$Revision$</tt>
 */
public class PersistentMetadataGroup
{
    /*----------------------------------------------------------------------*\
                               Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                             Private Instance Data
    \*----------------------------------------------------------------------*/

    private Map<String,String> nameValuePairs = new HashMap<String,String>();
    private String             namespace      = null;

    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new instance of <tt>PersistentMetadataGroup</tt>.
     *
     * @param namespace the namespace for this group
     */
    public PersistentMetadataGroup(String namespace)
    {
        this.namespace = namespace;
    }

    /*----------------------------------------------------------------------*\
                                Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the namespace for this group.
     *
     * @return the namespace
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * Add a name/value pair to this metadata group.
     *
     * @param name   the name
     * @param value  the value
     */
    public void addMetadataItem(String name, String value)
    {
        nameValuePairs.put(name, value);
    }

    /**
     * Add an entire map-ful of metadata to this group.
     *
     * @param metadata the data
     */
    public void addMetadata(Map<String,String> metadata)
    {
        this.nameValuePairs.putAll(metadata);
    }

    /**
     * Get the name/value pairs (i.e., the actual data) associated with this
     * metadata group.
     *
     * @return a <tt>Map</tt> of name/value pair data
     */
    public Map<String,String> getMetadata()
    {
        return Collections.unmodifiableMap(nameValuePairs);
    }

    public Map<String, String> getNameValuePairs()
    {
        return nameValuePairs;
    }

    /**
     * Get the hash code. Items of this type are hashed by namespace.
     *
     * @return the hash code
     */
    public int hashCode()
    {
        return namespace.hashCode();
    }

    /**
     * Determine whether this object is equivalent to another. A
     * <tt>PersistentMetadataGroup</tt> object is equivalent to another
     * only if (a) both objects have the same namespace, and (b) both
     * objects contain the same data.
     *
     * @param o  the other object
     */
    public boolean equals(Object o)
    {
        boolean eq = false;

        if (o instanceof PersistentMetadataGroup)
        {
            PersistentMetadataGroup other = (PersistentMetadataGroup) o;

            eq = namespace.equals(other.namespace) &&
                 nameValuePairs.equals(other.nameValuePairs);
        }

        return eq;
    }

    /*----------------------------------------------------------------------*\
                               Protected Methods
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                Private Methods
    \*----------------------------------------------------------------------*/
}
