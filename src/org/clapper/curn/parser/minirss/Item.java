/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms are permitted provided
  that: (1) source distributions retain this entire copyright notice and
  comment; and (2) modifications made to the software are prominently
  mentioned, and a copy of the original software (or a pointer to its
  location) are included. The name of the author may not be used to endorse
  or promote products derived from this software without specific prior
  written permission.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
  WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

  Effectively, this means you can do what you want with the software except
  remove this notice or take advantage of the author's name. If you modify
  the software and redistribute your modified version, you must indicate that
  your version is a modification of the original, and you must provide either
  a pointer to or a copy of the original.
\*---------------------------------------------------------------------------*/

package org.clapper.curn.parser.minirss;

import org.clapper.curn.parser.RSSItem;

import java.net.URL;

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
    private String      id             = null;

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
     * Get the item's ID field, if any.
     *
     * @return the ID field, or null if not set
     */
    public String getID()
    {
        return this.id;
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Set the ID field for this item.
     *
     * @param id  the ID
     */
    void setUniqueID (String id)
    {
        this.id = id;
    }
}
