/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2010 Brian M. Clapper.
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
