/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn.parser.informa;

import org.clapper.curn.parser.RSSItem;

import de.nava.informa.core.ItemIF;
import de.nava.informa.core.CategoryIF;

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
    public void setSummary (String newSummary)
    {
        this.item.setDescription (newSummary);
    }

    /**
     * Get the item's author.
     *
     * @return the author, or null if not available
     *
     * @see #setAuthor
     */
    public String getAuthor()
    {
        // Informa doesn't support this field.

        return null;
    }

    /**
     * Set the item's author.
     *
     * @param newAuthor the author, or null if not available
     */
    public void setAuthor (String newAuthor)
    {
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
     * Get the item's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate()
    {
        return this.item.getDate();
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
