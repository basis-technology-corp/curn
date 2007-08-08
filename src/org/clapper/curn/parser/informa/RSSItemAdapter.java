/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M. Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package org.clapper.curn.parser.informa;

import org.clapper.curn.parser.ParserUtil;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.RSSLink;

import de.nava.informa.core.ItemIF;
import de.nava.informa.core.CategoryIF;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.impl.basic.Category;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import org.clapper.curn.parser.RSSContent;

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

    /**
     * Parent channel
     */
    private RSSChannel channel;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>RSSItemAdapter</tt> object that wraps the specified
     * Informa <tt>ItemIF</tt> object.
     *
     * @param itemIF        the <tt>ItemIF</tt> object
     * @param parentChannel parent <tt>RSSChannel</tt>
     */
    RSSItemAdapter(ItemIF itemIF, RSSChannel parentChannel)
    {
        super();

        this.item    = itemIF;
        this.channel = parentChannel;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Create a new, empty instance of the underlying concrete
     * class.
     *
     * @param channel  the parent channel
     *
     * @return the new instance
     */
    public RSSItem newInstance(RSSChannel channel)
    {
        ItemIF newItem = new Item();
        newItem.setChannel (((RSSChannelAdapter) channel).getChannelIF());
        return new RSSItemAdapter (newItem, channel);
    }

    /**
     * Get the parent <tt>Channel</tt> object.
     *
     * @return the parent <tt>Channel</tt> object
     */
    public RSSChannel getParentChannel()
    {
        return this.channel;
    }

    /**
     * Get the item's title
     *
     * @return the item's title, or null if there isn't one
     *
     * @see #setTitle
     */
    public String getTitle()
    {
        return this.item.getTitle();
    }

    /**
     * Set the item's title
     *
     * @param newTitle  the item's title, or null if there isn't one
     *
     * @see #getTitle
     */
    public void setTitle (String newTitle)
    {
        this.item.setTitle (newTitle);
    }

    /**
     * Get the item's published links.
     *
     * @return the collection of links, or an empty collection
     *
     * @see RSSItem#getLink
     */
    public final Collection<RSSLink> getLinks()
    {
        // Since Informa doesn't support multiple links per item, we have
        // to assume that this link is the link for the item. Try to figure
        // out the MIME type, and default to the MIME type for an RSS feed.
        // Mark the feed as type "self".

        Collection<RSSLink> results = new ArrayList<RSSLink>();

        URL url = item.getLink();
        results.add (new RSSLink (url,
                                  ParserUtil.getLinkMIMEType (url),
                                  RSSLink.Type.SELF));

        return results;
    }

    /**
     * Set the item's published links.
     *
     * @param links the collection of links, or an empty collection (or null)
     *
     * @see #getLinks
     */
    public void setLinks(Collection<RSSLink> links)
    {
        // Since Informa doesn't support multiple links per item, we have
        // to assume that the first link is the link for the item.

        if ((links != null) && (links.size() > 0))
        {
            RSSLink link = links.iterator().next();
            item.setLink (link.getURL());
        }
    }

    /**
     * Get the item's summary.
     *
     * @return the summary, or null if not available
     *
     * @see #setSummary
     */
    public String getSummary()
    {
        return this.item.getDescription();
    }

    /**
     * Set the item's summary (also sometimes called the description or
     * synopsis).
     *
     * @param newSummary the summary, or null if not available
     *
     * @see #getSummary
     */
    public void setSummary(String newSummary)
    {
        this.item.setDescription(newSummary);
    }

    /**
     * Get the item's author list.
     *
     * @return the authors, or null (or an empty <tt>Collection</tt>) if
     *         not available
     *
     * @see #addAuthor
     * @see #clearAuthors
     */
    public Collection<String> getAuthors()
    {
        // Informa doesn't support this field.

        return null;
    }

    /**
     * Add to the item's author list.
     *
     * @param author  another author string to add
     *
     * @see #getAuthors
     * @see #clearAuthors
     */
    public void addAuthor(String author)
    {
        // Informa doesn't support this field.
    }

    /**
     * Clear the authors list.
     *
     * @see #getAuthors
     * @see #addAuthor
     */
    public void clearAuthors()
    {
        // Informa doesn't support this field.
    }

    /**
     * Get the categories the item belongs to.
     *
     * @return a <tt>Collection</tt> of category strings (<tt>String</tt>
     *         objects) or null if not applicable
     */
    public Collection<String> getCategories()
    {
        Collection<String> result     = null;
        Collection         categories = item.getCategories();

        if ((categories != null) && (categories.size() > 0))
        {
            result = new ArrayList<String>();

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
     * Set the categories the item belongs to.
     *
     * @param categories a <tt>Collection</tt> of category strings
     *                   or null if not applicable
     *
     * @see #getCategories
     */
    public void setCategories (Collection<String> categories)
    {
        if (categories == null)
            categories = Collections.emptyList();

        Collection<CategoryIF> nativeCategories;

        nativeCategories = new ArrayList<CategoryIF>();
        for (String category : categories)
            nativeCategories.add (new Category (category));

        item.setCategories (nativeCategories);
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
     * Set the item's publication date.
     *
     * @see #getPublicationDate
     */
    public void setPublicationDate (Date date)
    {
        this.item.setDate (date);
    }

    /**
     * Get the item's ID field, if any.
     *
     * @return the ID field, or null if not set
     */
    public String getID()
    {
        return null;
    }

    /**
     * Set the item's ID field, if any.
     *
     * @param id the ID field, or null
     */
    public void setID (String id)
    {
        // Nothing to do here.
    }

    /*----------------------------------------------------------------------*\
                              Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get all content associated with this item.
     *
     * @return a <tt>Collection</tt> of {@link RSSContent} objects
     */
    protected Collection<RSSContent> getContent()
    {
        // Not entirely sure what to use here, given the Informa API.
        // So, use the summary field, and assume "text/plain" for now.

        String summary = getSummary();
        Collection<RSSContent> result = null;
        if (summary != null)
        {
            result = Collections.singletonList(new RSSContent(summary,
                                                              "text/plain"));
        }

        return result;
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
