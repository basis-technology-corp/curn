/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn.parser;

import org.clapper.util.text.TextUtils;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * This abstract class defines a simplified view of an RSS item, providing
 * only the methods necessary for <i>curn</i> to work. <i>curn</i> uses the
 * {@link RSSParserFactory} class to get a specific implementation of
 * <tt>RSSParser</tt>, which returns <tt>RSSChannel</tt>-conforming objects
 * that, in turn, return item objects that subclass <tt>RSSItem</tt>. This
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
public abstract class RSSItem
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /**
     * Constant defining the pseudo-MIME type to use for default content.
     */
    public static final String DEFAULT_CONTENT_TYPE = "*";

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private Map contentMap = new HashMap();

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the item's content, if available. Some feed types (e.g., Atom)
     * support multiple content sections, each with its own MIME type; the
     * <tt>mimeType</tt> parameter specifies the caller's desired MIME
     * type.
     *
     * @param mimeType  the desired MIME type
     *
     * @return the content (or the default content), or null if no content
     *         of the desired MIME type is available
     */
    public String getContent (String mimeType)
    {
        String result = null;

        result = (String) contentMap.get (mimeType);
        if (result == null)
            result = (String) contentMap.get (DEFAULT_CONTENT_TYPE);

        return result;
    }

    /**
     * Get the first content item that matches one of a list of MIME types.
     *
     * @param mimeTypes  an array of MIME types to match, in order
     *
     * @return the first matching content string, or null if none was found.
     *         Returns the default content (if set), if there's no exact
     *         match.
     */
    public final String getFirstContentOfType (String[] mimeTypes)
    {
        String result = null;

        for (int i = 0; i < mimeTypes.length; i++)
        {
            result = (String) contentMap.get (mimeTypes[i]);
            if (! TextUtils.stringIsEmpty (result))
                break;
        }

        if (result == null)
            result = (String) contentMap.get (DEFAULT_CONTENT_TYPE);

        return result;
    }

    /**
     * Set the content for a specific MIME type. If the
     * <tt>isDefault</tt> flag is <tt>true</tt>, then this content
     * is served up as the default whenever content for a specific MIME type
     * is requested but isn't available.
     *
     * @param content    the content string
     * @param mimeType   the MIME type to associate with the content
     */
    public void setContent (String content, String mimeType)
    {
        contentMap.put (mimeType, content);
    }   

    /*----------------------------------------------------------------------*\
                          Public Abstract Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the item's title
     *
     * @return the item's title, or null if there isn't one
     */
    public abstract String getTitle();

    /**
     * Get the item's published link (its URL).
     *
     * @return the URL, or null if not available
     */
    public abstract URL getLink();

    /**
     * Change the item's link (its URL)
     *
     * @param url the new link value
     */
    public abstract void setLink (URL url);

    /**
     * Get the item's summary (also sometimes called the description or
     * synopsis).
     *
     * @return the summary, or null if not available
     */
    public abstract String getSummary();

    /**
     * Get the item's author.
     *
     * @return the author, or null if not available
     */
    public abstract String getAuthor();

    /**
     * Get the categories the item belongs to.
     *
     * @return a <tt>Collection</tt> of category strings (<tt>String</tt>
     *         objects) or null if not applicable
     */
    public abstract Collection getCategories();

    /**
     * Get the item's publication date.
     *
     * @return the date, or null if not available
     */
    public abstract Date getPublicationDate();

    /**
     * Get the item's unique ID, if any.
     *
     * @return the unique ID, or null if not set
     */
    public abstract String getUniqueID();

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
    public abstract String getCacheKey();
}
