/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget.parser.informa;

import org.clapper.rssget.parser.RSSItem;

import de.nava.informa.core.ItemIF;
import de.nava.informa.core.CategoryIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.RSSParser;

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
 * @see org.clapper.rssget.parser.RSSParserFactory
 * @see org.clapper.rssget.parser.RSSParser
 * @see org.clapper.rssget.parser.RSSItem
 * @see RSSItemAdapter
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSItemAdapter implements RSSItem
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
     * Get the item's description.
     *
     * @return the description, or null if not available
     */
    public String getDescription()
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
