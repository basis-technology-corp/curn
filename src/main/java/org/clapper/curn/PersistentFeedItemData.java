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
