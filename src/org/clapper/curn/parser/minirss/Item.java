/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2006 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M.a Clapper.

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

package org.clapper.curn.parser.minirss;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.RSSLink;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

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

    private String               title          = null;
    private String               summary        = null;
    private Date                 pubDate        = null;
    private Collection<String>   categories     = null;
    private Collection<String>   authors        = new HashSet<String>();
    private Collection<RSSLink>  links          = new ArrayList<RSSLink>();
    private Channel              channel        = null;
    private String               id             = null;

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
     * Create a new, empty instance of the underlying concrete
     * class.
     *
     * @param channel  the parent channel
     *
     * @return the new instance
     */
    public RSSItem newInstance (RSSChannel channel)
    {
        return new Item ((Channel) channel);
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
     * Get the item's published links.
     *
     * @return the collection of links, or an empty collection
     *
     * @see #addLink
     * @see RSSItem#getLink
     */
    public final Collection<RSSLink> getLinks()
    {
        return links;
    }

    /**
     * Add a link for the item.
     *
     * @param link  the {@link RSSLink} object to add
     *
     * @see #getLinks
     * @see RSSItem#getLink
     */
    public void addLink (RSSLink link)
    {
        links.add (link);
    }

    /**
     * Set the item's published links.
     *
     * @param newLinks collection of links, or an empty collection (or null)
     *
     * @see #getLinks
     */
    public void setLinks (Collection<RSSLink> newLinks)
    {
        links.clear();
        if (newLinks != null)
            links.addAll (newLinks);
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
        return authors;
    }

    /**
     * Add to the item's author list.
     *
     * @param author  another author string to add
     *
     * @see #getAuthors
     * @see #clearAuthors
     */
    public void addAuthor (String author)
    {
        authors.add (author);
    }

    /**
     * Clear the authors list.
     *
     * @see #getAuthors
     * @see #addAuthor
     */
    public void clearAuthors()
    {
        authors.clear();
    }

    /**
     * Get the item's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate()
    {
        return pubDate;
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
     *
     * @see #getCategories
     * @see #setCategories
     */
    public void addCategory (String category)
    {
        if (categories == null)
            categories = new ArrayList<String>();

        categories.add (category);
    }
    
    /**
     * Get the categories the item belongs to.
     *
     * @return a <tt>Collection</tt> of category strings (<tt>String</tt>
     *         objects) or null if not applicable
     *
     * @see #addCategory
     * @see #setCategories
     */
    public Collection<String> getCategories()
    {
        return categories;
    }

    /**
     * Set the categories the item belongs to.
     *
     * @param newCategories a <tt>Collection</tt> of category strings
     *                      or null if not applicable
     *
     * @see #addCategory
     * @see #getCategories
     */
    public void setCategories (Collection<String> newCategories)
    {
        if (this.categories == null)
            this.categories = new ArrayList<String>();
        else
            this.categories.clear();

        if (newCategories != null)
            this.categories.addAll (newCategories);
    }

    /**
     * Get the item's ID field, if any.
     *
     * @return the ID field, or null if not set
     */
    public String getID()
    {
        return this.id;
    }

    /**
     * Set the item's ID field, if any.
     *
     * @param id the ID field, or null
     */
    public void setID (String id)
    {
        this.id = id;
    }
}
