/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget.parser;

import java.net.URL;
import java.util.Collection;
import java.util.Date;

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
     *         The collection will be empty (never null) if there are no
     *         items. This <tt>Collection</tt> is expected to be a copy of
     *         whatever the channel is really storing. (That is, if the
     *         underlying implementation is using a <tt>Collection</tt> to
     *         store its <tt>RSSItem</tt> objects, it should not return
     *         that <tt>Collection</tt> directly; instead, it should return
     *         a copy.)
     */
    public Collection getItems();

    /**
     * Change the items the channel the ones in the specified collection.
     * If the collection is empty, the items are cleared. The items are
     * copied from the supplied collection. (A reference to the supplied
     * collection is <i>not</i> saved in this object.)
     *
     * @param newItems  new collection of <tt>RSSItem</tt> items.
     */
    public void setItems (Collection newItems);

    /**
     * Get the channel's title
     *
     * @return the channel's title, or null if there isn't one
     *
     * @see #setTitle(String)
     */
    public String getTitle();

    /**
     * Set the channel's title
     *
     * @param newTitle the channel's title, or null if there isn't one
     *
     * @see #getTitle()
     */
    public void setTitle (String newTitle);

    /**
     * Get the channel's description
     *
     * @return the channel's description, or null if there isn't one
     */
    public String getDescription();

    /**
     * Get the channel's published link (its URL).
     *
     * @return the URL, or null if not available
     */
    public URL getLink();

    /**
     * Get the channel's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate();

    /**
     * Get the channel's copyright string
     *
     * @return the copyright string, or null if not available
     */
    public String getCopyright();

    /**
     * Get the RSS format the channel is using.
     *
     * @return the format, or null if not available
     */
    public String getRSSFormat();

    /**
     * Get the author of the feed.
     *
     * @return the author, or null if not available
     */
    public String getAuthor();

    /**
     * Get a unique string that can be used to store information about this
     * channel in the cache and retrieve it later. Possibilities for this
     * value include (but are not limited to):
     *
     * <ul>
     *   <li> Unique ID. Some RSS formats support a unique per-channel
     *        ID. For instance,
     *        {@link <a href="http://www.atomenabled.org/developers/">Atom</a>}
     *        supports an optional <tt>&lt;id&gt;</tt> element nested within
     *        its <tt>&lt;feed&gt;</tt> element. (The <tt>&lt;feed&gt;</tt>
     *        element represent a channel in Atom.)
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
