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
 * This class contains a subset of standard RSS channel data, providing
 * only the methods necessary for <i>curn</i> to work.
 *
 * @see org.clapper.curn.parser.RSSParserFactory
 * @see org.clapper.curn.parser.RSSParser
 * @see org.clapper.curn.parser.RSSChannel
 * @see org.clapper.curn.parser.RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public class Channel extends RSSChannel
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private Collection<RSSItem>  items       = new ArrayList<RSSItem>();
    private Collection<String>   authors     = new HashSet<String>();
    private String               title       = null;
    private String               description = null;
    private Collection<RSSLink>  links       = new ArrayList<RSSLink>();
    private Date                 pubDate     = null;
    private String               rssFormat   = null;
    private String               copyright   = null;
    private String               uniqueID    = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructor. Objects of this type can only be created within this
     * package.
     */
    Channel()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Create a new, empty instance of the underlying concrete
     * class.
     *
     * @return the new instance
     */
    public RSSChannel newInstance()
    {
        return new Channel();
    }

    /**
     * Get a <tt>Collection</tt> of the items in this channel. All objects
     * in the collection are of type <tt>RSSItem</tt>.
     *
     * @return a (new) <tt>Collection</tt> of <tt>RSSItem</tt> objects.
     *         The collection will be empty (never null) if there are no
     *         items. This <tt>Collection</tt> is a copy of the underlying
     *         <tt>collection</tt> being used.
     */
    public Collection<RSSItem> getItems()
    {
        Collection<RSSItem> result = new ArrayList<RSSItem>();

        result.addAll (items);
        return result;
    }

    /**
     * Change the items the channel the ones in the specified collection.
     * If the collection is empty, the items are cleared. The items are
     * copied from the supplied collection. (A reference to the supplied
     * collection is <i>not</i> saved in this object.)
     *
     * @param newItems  new collection of <tt>RSSItem</tt> items.
     */
    public void setItems (Collection<? extends RSSItem> newItems)
    {
        items.clear();
        if (newItems != null)
            items.addAll (newItems);
    }

    /**
     * Add an item.
     *
     * @param item  item to add
     */
    public void addItem (Item item)
    {
        items.add (item);
    }

    /**
     * Remove an item from the set of items.
     *
     * @param item  the item to remove
     *
     * @return <tt>true</tt> if removed, <tt>false</tt> if not found
     */
    public boolean removeItem (RSSItem item)
    {
        return items.remove (item);
    }

    /**
     * Get the channel's title
     *
     * @return the channel's title, or null if there isn't one
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Set the channel's title
     *
     * @param title the channel's title, or null if there isn't one
     *
     * @see #getTitle()
     */
    public void setTitle (String title)
    {
        this.title = title;
    }

    /**
     * Get the channel's description
     *
     * @return the channel's description, or null if there isn't one
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the channel's description
     *
     * @param description the channel's description, or null if there isn't one
     */
    public void setDescription (String description)
    {
        this.description = description;
    }

    /**
     * Get the channel's published links.
     *
     * @return the collection of links, or an empty collection
     *
     * @see #addLink
     * @see RSSChannel#getLink
     * @see #addLink
     */
    public final Collection<RSSLink> getLinks()
    {
        return links;
    }

    /**
     * Set the channel's list of published links (its URLs).
     *
     * @param newLinks the links
     *
     * @see #getLink
     * @see #getLinks
     * @see #addLink
     */
    public void setLinks (Collection<RSSLink> newLinks)
    {
        this.links.clear();
        if (newLinks != null)
            links.addAll (newLinks);
    }

    /**
     * Add a link for the channel.
     *
     * @param link  the {@link RSSLink} object to add
     *
     * @see #getLinks
     * @see #getLinks
     * @see #setLinks
     */
    public void addLink (RSSLink link)
    {
        links.add (link);
    }

    /**
     * Get the channel's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate()
    {
        return pubDate;
    }

    /**
     * Set the channel's publication date.
     *
     * @param date the date, or null if not available
     */
    public void setPublicationDate (Date date)
    {
        this.pubDate = date;
    }

    /**
     * Get the channel's copyright string
     *
     * @return the copyright string, or null if not available
     */
    public String getCopyright()
    {
        return copyright;
    }

    /**
     * Set the copyright string for the channel
     *
     * @param copyright the copyright string, or null if not available
     */
    public void setCopyright (String copyright)
    {
        this.copyright = copyright;
    }

    /**
     * Get the RSS format the channel is using.
     *
     * @return the format, or null if not available
     */
    public String getRSSFormat()
    {
        return rssFormat;
    }

    /**
     * Get the RSS format the channel is using, in native format. This
     * method exists for underlying implementations that store the RSS
     * format as something other than a string; the method allows the
     * {@link #makeCopy} method to copy the RSS format without knowing
     * how it's stored. The default implementation of this method 
     * simply calls {@link #getRSSFormat}.
     *
     * @return the format, or null if not available
     *
     * @see #getRSSFormat
     * @see #setNativeRSSFormat
     */
    public Object getNativeRSSFormat()
    {
        return getRSSFormat();
    }

    /**
     * Set the RSS format the channel is using.
     *
     * @param format the format, or null if not available
     *
     * @see #getRSSFormat
     * @see #getNativeRSSFormat
     */
    public void setNativeRSSFormat (Object format)
    {
        setRSSFormat ((String) format);
    }

    /**
     * Set the RSS format the channel is using.
     *
     * @param format the format, or null if not available
     */
    public void setRSSFormat (String format)
    {
        this.rssFormat = format;
    }

    /**
     * Get the channel's author list.
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
     * Add to the channel's author list.
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
     * Get the unique ID for this item.
     *
     * @return  the unique ID
     */
    public String getUniqueID()
    {
        return uniqueID;
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
