/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn.parser.informa;

import org.clapper.curn.Util;

import org.clapper.curn.parser.RSSItem;

import de.nava.informa.core.ItemIF;
import de.nava.informa.core.CategoryIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;

import java.net.URL;
import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class implements the <tt>RSSItem</tt> interface and defines an
 * adapter for the <a href="http://informa.sourceforge.net/">Informa</a>
 * RSS Parser's <tt>ItemIF</tt> type.
 *
 * @see org.clapper.curn.parser.RSSParserFactory
 * @see org.clapper.curn.parser.RSSParser
 * @see org.clapper.curn.parser.RSSItem
 * @see RSSItemAdapter
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSItemAdapter extends RSSItem
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
     * Allocate a new <tt>RSSItemAdapter</tt> object that wraps the specified
     * Informa <tt>ItemIF</tt> object.
     *
     * @param itemIF  the <tt>ItemIF</tt> object
     */
    RSSItemAdapter (ItemIF itemIF)
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
     * Get the item's published link (its URL).
     *
     * @return the URL, or null if not available
     */
    public URL getLink()
    {
        return this.item.getLink();
    }

    /**
     * Set (change) the item's published link (its URL).
     *
     * @param url the URL, or null if not available
     */
    public void setLink (URL url)
    {
        this.item.setLink (url);
    }

    /**
     * Get the item's summary.
     *
     * @return the summary, or null if not available
     */
    public String getSummary()
    {
        return this.item.getDescription();
    }

    /**
     * Get the item's author.
     *
     * @return the author, or null if not available
     */
    public String getAuthor()
    {
        // Informa doesn't support this field.

        return null;
    }

    /**
     * Get the categories the item belongs to.
     *
     * @return a <tt>Collection</tt> of category strings (<tt>String</tt>
     *         objects) or null if not applicable
     */
    public Collection getCategories()
    {
        Collection result     = null;
        Collection categories = item.getCategories();

        if ((categories != null) && (categories.size() > 0))
        {
            result = new ArrayList();

            for (Iterator it = categories.iterator(); it.hasNext(); )
            {
                CategoryIF cat = (CategoryIF) it.next();

                String s = cat.getTitle();
                if ((s != null) && (s.trim().length() > 0))
                    result.add (s);
            }
        }

        return result;
    }

    /**
     * Get the item's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate()
    {
        return this.item.getDate();
    }

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
    public String getCacheKey()
    {
        return Util.normalizeURL (getLink()).toExternalForm();
    }

    /**
     * Get the item's unique ID, if any.
     *
     * @return the unique ID, or null if not set
     */
    public String getUniqueID()
    {
        return null;
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the underlying Informa <tt>ItemIF</tt> object that this object
     * contains.
     *
     * @return the underlying <tt>ItemIF</tt> object
     */
    ItemIF getItemIF()
    {
        return this.item;
    }
}
