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
     * Get the item's published link (its URL).
     *
     * @return the URL, or null if not available
     */
    public URL getLink();

    /**
     * Change the item's link (its URL)
     *
     * @param url the new link value
     */
    public void setLink (URL url);

    /**
     * Get the item's description.
     *
     * @return the description, or null if not available
     */
    public String getDescription();

    /**
     * Get the item's author.
     *
     * @return the author, or null if not available
     */
    public String getAuthor();

    /**
     * Get the categories the item belongs to.
     *
     * @return a <tt>Collection</tt> of category strings (<tt>String</tt>
     *         objects) or null if not applicable
     */
    public Collection getCategories();

    /**
     * Get the item's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate();

}
