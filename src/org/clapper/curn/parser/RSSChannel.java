/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.net.*;
import java.util.*;

/**
 * This interface defines a simplified view of an RSS channel, providing
 * only the methods necessary for <i>rssget</i> to work. <i>rssget</i> uses
 * the {@link RSSParserFactory} class to get a specific implementation of
 * <tt>RSSParser</tt>, which returns an object that conforms to this
 * interface. This strategy isolates the bulk of the code from the
 * underlying RSS parser, making it easier to substitute different parsers
 * as more of them become available.
 *
 * @see RSSParserFactory
 * @see RSSParser
 * @see RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public interface RSSChannel
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a <tt>Collection</tt> of the items in this channel. All objects
     * in the collection are of type <tt>RSSItem</tt>.
     *
     * @return a (new) <tt>Collection</tt> of <tt>RSSItem</tt> objects.
     *         The collection will be empty (never null) if there are
     *         no items.
     */
    public Collection getItems();

    /**
     * Get the channel's title
     *
     * @return the channel's title, or null if there isn't one
     */
    public String getTitle();

    /**
     * Get the channel's published URL.
     *
     * @return the URL, or null if not available
     */
    public URL getURL();

    /**
     * Get the channel's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate();

    /**
     * Get the RSS format the channel is using.
     *
     * @return the format, or null if not available
     */
    public String getRSSFormat();
}
