/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget.informa;

import org.clapper.rssget.*;
import de.nava.informa.core.*;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.RSSParser;
import java.net.*;
import java.util.*;

/**
 * This class implements the <tt>RSSItem</tt> interface and defines an
 * adapter for the {@link <a href="http://informa.sourceforge.net/" Informa}
 * RSS Parser's <tt>ItemIF</tt> type.
 *
 * @see RSSParserFactory
 * @see RSSParser
 * @see RSSItem
 * @see RSSItemImpl
 * @see RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSItemImpl
    implements org.clapper.rssget.RSSItem
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The real item object
     */
    private ItemIF item;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>RSSItemImpl</tt> object that wraps the specified
     * Informa <tt>ItemIF</tt> object.
     *
     * @param itemIF  the <tt>ItemIF</tt> object
     */
    RSSItemImpl (ItemIF itemIF)
    {
        this.item = itemIF;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the item's title
     *
     * @return the item's title, or null if there isn't one
     */
    public String getTitle()
    {
        return this.item.getTitle();
    }

    /**
     * Get the item's published URL.
     *
     * @return the URL, or null if not available
     */
    public URL getURL()
    {
        return this.item.getLink();
    }

    /**
     * Get the item's description.
     *
     * @return the description, or null if not available
     */
    public String getDescription()
    {
        return this.item.getDescription();
    }
}
