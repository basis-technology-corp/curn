/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.net.*;
import java.util.*;

/**
 * This interface defines a simplified view of an RSS item, providing only
 * the methods necessary for <i>rssget</i> to work. <i>rssget</i> uses the
 * {@link RSSParserFactory} class to get a specific implementation of
 * <tt>RSSParser</tt>, which returns <tt>RSSChannel</tt>-conforming objects
 * that, in turn, return objects that conform to this interface. This
 * strategy isolates the bulk of the code from the underlying RSS parser,
 * making it easier to substitute different parsers as more of them become
 * available. <tt>RSSItem</tt>. This strategy isolates the bulk of the code
 * from the underlying RSS parser, making it easier to substitute different
 * parsers as more of them become available.
 *
 * @see RSSParserFactory
 * @see RSSParser
 * @see RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public interface RSSItem
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the item's title
     *
     * @return the item's title, or null if there isn't one
     */
    public String getTitle();

    /**
     * Get the item's published URL.
     *
     * @return the URL, or null if not available
     */
    public URL getURL();

    /**
     * Change the item's URL
     */
    public void setURL (URL url);

    /**
     * Get the item's description.
     *
     * @return the description, or null if not available
     */
    public String getDescription();
}
