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

package org.clapper.curn.parser.rome;

import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.ParserUtil;

import org.clapper.util.logging.Logger;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContentI;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class implements the <tt>RSSItem</tt> interface and defines an
 * adapter for the {@link <a href="https://rome.dev.java.net/">Rome</a>}
 * RSS Parser's <tt>SyndEntry</tt> type.
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
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The real item object
     */
    private SyndEntry entry;

    /**
     * For log messages
     */
    private static Logger log = new Logger (RSSItemAdapter.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>RSSItemAdapter</tt> object that wraps the specified
     * Rome <tt>SyndEntry</tt> object.
     *
     * @param entry  the <tt>SyndEntry</tt> object
     */
    RSSItemAdapter (SyndEntry entry)
    {
        this.entry = entry;
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
        // Rome leaves leading, trailing and embedded newlines in place.
        // While this is syntactically okay, curn prefers the description
        // to be one long line. ParserUtil.normalizeCharacterData() strips
        // leading and trailing newlines, and converts embedded newlines to
        // spaces.

        return ParserUtil.normalizeCharacterData (entry.getTitle());
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
        entry.setTitle (newTitle);
    }

    /**
     * Get the item's published link (its URL).
     *
     * @return the URL, or null if not available
     */
    public URL getLink()
    {
        URL url = null;

        try
        {
            url = new URL (entry.getLink());
        }

        catch (MalformedURLException ex)
        {
            log.error ("Bad channel URL \""
                     + entry.getLink()
                     + "\" from underlying parser: "
                     + ex.toString());
        }

        return url;
    }

    /**
     * Set (change) the item's published link (its URL).
     *
     * @param url the URL, or null if not available
     */
    public void setLink (URL url)
    {
        entry.setLink (url.toString());
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
        String        result  = null;
        SyndContentI  content = entry.getDescription();

        if (content != null)
            result = content.getValue();

        // Rome leaves leading, trailing and embedded newlines in place.
        // While this is syntactically okay, curn prefers the description
        // to be one long line. ParserUtil.normalizeCharacterData() strips
        // leading and trailing newlines, and converts embedded newlines to
        // spaces.

        return ParserUtil.normalizeCharacterData (result);
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
        SyndContentI  content = entry.getDescription();

        if (content != null)
            content.setValue (newSummary);
     }

    /**
     * Get the item's author.
     *
     * @return the author, or null if not available
     *
     * @see #getAuthor
     */
    public String getAuthor()
    {
        return entry.getAuthor();
    }

    /**
     * Set the item's author.
     *
     * @param newAuthor the author, or null if not available
     */
    public void setAuthor (String newAuthor)
    {
        entry.setAuthor (newAuthor);
    }

    /**
     * Get the categories the item belongs to.
     *
     * @return a <tt>Collection</tt> of category strings (<tt>String</tt>
     *         objects) or null if not applicable
     */
    public Collection<String> getCategories()
    {
        Collection<String>  result     = null;
        Collection          categories = entry.getCategories();

        if ((categories != null) && (categories.size() > 0))
        {
            result = new ArrayList<String>();

            for (Iterator it = categories.iterator(); it.hasNext(); )
            {
                String s = ((SyndCategory) it.next()).getName();
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
        return entry.getPublishedDate();
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
     * Get the underlying Rome <tt>SyndEntry</tt> object that this object
     * contains.
     *
     * @return the underlying <tt>SyndEntry</tt> object
     */
    SyndEntry getSyndEntry()
    {
        return this.entry;
    }
}
