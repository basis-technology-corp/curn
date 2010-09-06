/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2010 Brian M. Clapper.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name "clapper.org", "curn", nor the names of the project's
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/


package org.clapper.curn.parser;

import java.net.URL;
import org.clapper.util.text.TextUtil;

import java.util.Collection;

/**
 * Abstract parent class of {@link RSSItem} and {@link RSSChannel},
 * containing various shared methods.
 *
 * @see RSSParser
 * @see RSSChannel
 * @see RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public abstract class RSSElement
{
    /*----------------------------------------------------------------------*\
                               Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor
     */
    protected RSSElement()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the item's author. This method simply calls the {@link
     * #getAuthors} method and combines the results into a comma-delimited
     * string.
     *
     * @return the author string, or null if not available
     *
     * @see #setAuthor
     * @see #getAuthors
     */
    public final String getAuthor()
    {
        Collection<String> authors = getAuthors();
        String             result  = null;

        if ((authors != null) && (authors.size() > 0))
            result = TextUtil.join(authors, ", ");

        return result;
    }

    /**
     * Set the item's author. This method clears the existing authors
     * field, then calls {@link #addAuthor}. You're better off calling
     * {@link #addAuthor} directly, since some sites support multiple
     * authors for a feed item.
     *
     * @param newAuthor  the author, or null if not available
     *
     * @see #setAuthors
     */
    public final void setAuthor (String newAuthor)
    {
        clearAuthors();
        addAuthor (newAuthor);
    }

    /**
     * Set the item's list of authors to the specified <tt>Collection</tt>.
     * This method clears the existing authors field, then calls
     * {@link #addAuthor} for every string in the <tt>Collection</tt>.
     *
     * @param newAuthors  the author, or null if not available
     *
     * @see #addAuthor
     * @see #getAuthor
     * @see #clearAuthors
     */
    public final void setAuthors (Collection<String> newAuthors)
    {
        clearAuthors();
        for (String author : newAuthors)
            addAuthor (author);
    }

    /**
     * Get a unique ID for the item. The default implementation of this
     * method simply returns the URL, if any.
     *
     * @return the ID, or null if there isn't one.
     */
    public String getID()
    {
        String id = null;
        RSSLink link = getURL();

        if (link != null)
        {
            URL url = link.getURL();
            if (url != null)
                id = url.toExternalForm();
        }

        return id;
    }

    /**
     * Get the link with a specific MIME type and one of a set of
     * link types.
     *
     * @param mimeType  the desired MIME type
     * @param linkTypes one or more link types that are acceptable
     *
     * @return the link, or null no matches were found
     */
    public final RSSLink getLink (String mimeType, RSSLink.Type ... linkTypes)
    {
        return ParserUtil.findMatchingLink (getLinks(), mimeType, linkTypes);
    }

    /**
     * Get the first link with the specified link type.
     *
     * @param linkType  the link type
     *
     * @return the link, or null if no match was found
     */
    public final RSSLink getLink (RSSLink.Type linkType)
    {
        return ParserUtil.findMatchingLink (getLinks(), linkType);
    }

    /**
     * Get the first link with the specified MIME type.
     *
     * @param mimeType  the MIME type
     *
     * @return the link, or null if no match was found
     */
    public final RSSLink getLink (String mimeType)
    {
        return ParserUtil.findMatchingLink (getLinks(), mimeType);
    }

    /**
     * Get the first link with the specified MIME type. Fall back to the
     * {@link RSSLink.Type#SELF SELF} link if not found. Fall back to the
     * first available link if the <tt>SELF</tt> link is not found.
     *
     * @param mimeType  the MIME type
     *
     * @return the link, or null if no links are available
     */
    public final RSSLink getLinkWithFallback (String mimeType)
    {
        RSSLink              link  = null;
        Collection<RSSLink>  links = getLinks();

        if (links.size() > 0)
        {
            link = ParserUtil.findMatchingLink (links, mimeType);
            if (link == null)
            {
                link = getLink (RSSLink.Type.SELF);
                if (link == null)
                    link = links.iterator().next();
            }
        }

        return link;
    }

    /**
     * Get the URL associated with this item. This method first attempts to
     * get the {@link RSSLink.Type#SELF SELF} link. If that fails, it looks
     * for the first {@link RSSLink.Type#ALTERNATE ALTERNATE} link.
     *
     * @return the {@link RSSLink} for the URL, or null if it can't be found
     */
    public final RSSLink getURL()
    {
        // First, try to find the SELF link. If that's not available, use the
        // first ALTERNATE link.

        RSSLink link = getLink (RSSLink.Type.SELF);

        if (link == null)
        {
            link = getLink (RSSLink.Type.ALTERNATE);
            if (link == null)
                link = getLinkWithFallback("text/html");
        }

        return link;
    }

    /*----------------------------------------------------------------------*\
                          Public Abstract Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the element's list of published links (its URLs). Each
     * element in the returned <tt>Collection</tt> is an
     * {@link RSSLink} object.
     *
     * @return the collection of links, or an empty list if there are none.
     *         The result will never be null.
     */
    public abstract Collection<RSSLink> getLinks();

    /**
     * Get the item's author list.
     *
     * @return the authors, or null (or an empty <tt>Collection</tt>) if
     *         not available
     *
     * @see #addAuthor
     * @see #clearAuthors
     * @see #setAuthor
     * @see #getAuthor
     */
    public abstract Collection<String> getAuthors();

    /**
     * Add to the element's author list.
     *
     * @param author  another author string to add
     *
     * @see #getAuthors
     * @see #clearAuthors
     * @see #setAuthor
     * @see #getAuthor
     */
    public abstract void addAuthor (String author);

    /**
     * Clear the authors list.
     *
     * @see #getAuthors
     * @see #addAuthor
     * @see #setAuthor
     * @see #getAuthor
     */
    public abstract void clearAuthors();

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/
}
