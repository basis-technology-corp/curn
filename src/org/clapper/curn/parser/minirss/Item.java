/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn.parser.minirss;

import org.clapper.curn.Util;

import org.clapper.curn.parser.RSSItem;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class contains a subset of standard RSS item data, providing
 * only the methods necessary for <i>curn</i> to work.
 *
 * @version <tt>$Revision$</tt>
 */
public class Item extends RSSItem
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private String      title          = null;
    private URL         url            = null;
    private String      summary        = null;
    private Date        pubDate        = null;
    private Collection  categories     = null;
    private String      author         = null;
    private Channel     channel        = null;
    private String      uniqueID       = null;

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Constructor. Objects of this type can only be created within this
     * package.
     *
     * @param parentChannel  the parent channel
     */
    Item (Channel parentChannel)
    {
        this.channel = parentChannel;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Set the item's title
     *
     * @param title the item's title, or null if there isn't one
     */
    public void setTitle (String title)
    {
        this.title = title;
    }

    /**
     * Get the item's title
     *
     * @return the item's title, or null if there isn't one
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Get the item's published URL.
     *
     * @return the URL, or null if not available
     */
    public URL getLink()
    {
        return url;
    }

    /**
     * Set the item's published URL.
     *
     * @param url  the URL, as a string
     */
    public void setLink (URL url)
    {
        this.url = url;
    }

    /**
     * Get the item's summary.
     *
     * @return the summary, or null if not available
     */
    public String getSummary()
    {
        return summary;
    }

    /**
     * Set the item's summary.
     *
     * @param desc the summary, or null if not available
     */
    public void setSummary (String desc)
    {
        this.summary = desc;
    }

    /**
     * Get the item's author.
     *
     * @return the author, or null if not available
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Set the item's author.
     *
     * @param author the author, or null if not available
     */
    public void setAuthor (String author)
    {
        this.author = author;
    }

    /**
     * Get the item's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate()
    {
        return (pubDate != null) ? pubDate : channel.getPublicationDate();
    }

    /**
     * Set the item's publication date.
     *
     * @param date the date, or null if not available
     */
    public void setPublicationDate (Date date)
    {
        this.pubDate = date;
    }

    /**
     * Add a category to this item.
     *
     * @param category  the category string
     */
    public void addCategory (String category)
    {
        if (categories == null)
            categories = new ArrayList();

        categories.add (category);
    }
    
    /**
     * Get the categories the item belongs to.
     *
     * @return a <tt>Collection</tt> of category strings (<tt>String</tt>
     *         objects) or null if not applicable
     */
    public Collection getCategories()
    {
        return categories;
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
        String result = this.uniqueID;

        if (result == null)
            result = Util.normalizeURL (getLink()).toExternalForm();

        return result;
    }

    /**
     * Get the item's unique ID, if any.
     *
     * @return the unique ID, or null if not set
     */
    public String getUniqueID()
    {
        return this.uniqueID;
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Set the unique ID for this item.
     *
     * @param id  the unique ID
     */
    void setUniqueID (String id)
    {
        this.uniqueID = id;
    }
}
