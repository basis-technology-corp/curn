/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget.parser;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Collection;
import java.util.Date;

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

    /**
     * Get the item's unique ID, if any.
     *
     * @return the unique ID, or null if not set
     */
    public String getUniqueID();

    /**
     * Get a unique string that can be used to store this item in the
     * cache and retrieve it later. Possibilities for this value include
     * (but are not limited to):
     *
     * <ul>
     *   <li> Unique ID. Some RSS formats support a unique per-item
     *        ID. For instance,
     *        {@link <a href="http://www.atomenabled.org/developers/">Atom</a>}
     *        supports an optional <tt>&lt;id&gt;</tt> element nested within
     *        its <tt>&lt;entry&gt;</tt> element. (The <tt>&lt;entry&gt;</tt>
     *        element represent an item in Atom.)
     *   <li> The URI for the item. This value can be less reliable than a
     *        unique ID, because there's no guarantee that it won't change.
     *        However, sometimes it's all that's available.
     *   <li> A calculated hash string of some kind.
     * </ul>
     *
     * @return the cache key
     */
    public String getCacheKey();
}
