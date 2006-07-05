/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn.parser;

import org.clapper.util.text.HTMLUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * This abstract class defines a simplified view of an RSS channel,
 * providing only the methods necessary for <i>curn</i> to work.
 * <i>curn</i> uses the {@link RSSParserFactory} class to get a specific
 * implementation of <tt>RSSParser</tt>, which returns an object that is a
 * subclass of this class. This strategy isolates the bulk of the code from
 * the underlying RSS parser, making it easier to substitute different
 * parsers as more of them become available.
 *
 * @see RSSParserFactory
 * @see RSSParser
 * @see RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public abstract class RSSChannel extends RSSElement implements Cloneable
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private boolean htmlStripped = false;

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Clone this channel. This method simply calls the type-safe
     * {@link #makeCopy} method. The clone is a deep-clone (i.e., the items
     * are cloned, too).
     *
     * @return the cloned <tt>RSSChannel</tt>
     *
     * @throws CloneNotSupportedException  doesn't, actually, but the
     *                                     <tt>Cloneable</tt> interface
     *                                     requires that this exception
     *                                     be declared
     *
     * @see #makeCopy
     */
    public Object clone()
        throws CloneNotSupportedException
    {
        return makeCopy();
    }

    /**
     * Make a deep copy of this <tt>RSSChannel</tt> object.
     *
     * @return the copy
     */
    public RSSChannel makeCopy()
    {
        RSSChannel newChannel = newInstance();

        newChannel.setTitle (this.getTitle());
        newChannel.setDescription (this.getDescription());
        newChannel.setLinks (this.getLinks());
        newChannel.setPublicationDate (this.getPublicationDate());
        newChannel.setCopyright (this.getCopyright());
        newChannel.setNativeRSSFormat (this.getNativeRSSFormat());

        Collection<String> authors = this.getAuthors();
        if (authors != null)
        {
            for (String author : authors)
                newChannel.addAuthor (author);
        }

        Collection<RSSItem> itemCopies = new ArrayList<RSSItem>();

        for (RSSItem item : this.getItems())
            itemCopies.add (item.makeCopy (newChannel));
        newChannel.setItems (itemCopies);

        return newChannel;
    }

    /**
     * Strip all HTML and weird plain text from the channel and its items.
     * Intended primarily for output handlers and plug-ins that produce
     * plain text. This method edits the channel data directly; it does not
     * produce a copy.
     */
    public synchronized void stripHTML()
    {
        if (! htmlStripped)
        {
            Collection<String> authors = getAuthors();
            if (authors != null)
            {
                Collection<String> newAuthors = new ArrayList<String>();
                for (String author : authors)
                    newAuthors.add (HTMLUtil.textFromHTML (author));

                setAuthors (newAuthors);
            }

            String title = getTitle();
            if (title != null)
                setTitle (HTMLUtil.textFromHTML (title));

            String desc = getDescription();
            if (desc != null)
                setDescription (HTMLUtil.textFromHTML (desc));

            String copyright = getCopyright();
            if (copyright != null)
                setCopyright (HTMLUtil.textFromHTML (copyright));

            Collection<RSSItem> items = getItems();
            if ((items != null) && (items.size() > 0))
            {
                for (RSSItem item : items)
                {
                    title = item.getTitle();
                    if (title != null)
                        item.setTitle (HTMLUtil.textFromHTML (title));

                    authors = item.getAuthors();
                    if (authors != null)
                    {
                        Collection<String> newAuthors =
                            new ArrayList<String>();
                        for (String author : authors)
                            newAuthors.add (HTMLUtil.textFromHTML (author));

                        setAuthors (newAuthors);
                    }

                    String summary = item.getSummary();
                    if (summary != null)
                        item.setSummary (HTMLUtil.textFromHTML (summary));
                }
            }

            htmlStripped = true;
        }
    }

    /**
     * Return a string representation of this channel.
     *
     * @return the string
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();

        buf.append ("Channel ");

        Collection<RSSLink> links = getLinks();
        String title;

        if (links.size() > 0)
            buf.append (links.iterator().next().getURL().toString());
        else if ((title = getTitle()) != null)
            buf.append (title);
        else
            buf.append ("???");

        buf.append (", ");
        Collection<RSSItem> items = getItems();
        int total = (items == null) ? 0 : items.size();
        buf.append (String.valueOf (total));
        buf.append (" item(s)");

        return buf.toString();
    }

    /*----------------------------------------------------------------------*\
                          Abstract Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Create a new, empty instance of the underlying concrete
     * class.
     *
     * @return the new instance
     */
    public abstract RSSChannel newInstance();

    /**
     * Get a <tt>Collection</tt> of the items in this channel. All objects
     * in the collection are of type <tt>RSSItem</tt>.
     *
     * @return a (new) <tt>Collection</tt> of <tt>RSSItem</tt> objects.
     *         The collection will be empty (never null) if there are no
     *         items. This <tt>Collection</tt> is expected to be a copy of
     *         whatever the channel is really storing. (That is, if the
     *         underlying implementation is using a <tt>Collection</tt> to
     *         store its <tt>RSSItem</tt> objects, it should not return
     *         that <tt>Collection</tt> directly; instead, it should return
     *         a copy.) The order of items in the returned collection
     *         is arbitrary and not guaranteed to be sorted, unless sorted
     *         by a plug-in.
     */
    public abstract Collection<RSSItem> getItems();

    /**
     * Change the items the channel the ones in the specified collection.
     * If the collection is empty, the items are cleared. The items are
     * copied from the supplied collection. (A reference to the supplied
     * collection is <i>not</i> saved in this object.)
     *
     * @param newItems  new collection of <tt>RSSItem</tt> items.
     */
    public abstract void setItems (Collection<? extends RSSItem> newItems);

    /**
     * Remove an item from the set of items.
     *
     * @param item  the item to remove
     *
     * @return <tt>true</tt> if removed, <tt>false</tt> if not found
     */
    public abstract boolean removeItem (RSSItem item);

    /**
     * Get the channel's title
     *
     * @return the channel's title, or null if there isn't one
     *
     * @see #setTitle(String)
     */
    public abstract String getTitle();

    /**
     * Set the channel's title
     *
     * @param newTitle the channel's title, or null if there isn't one
     *
     * @see #getTitle()
     */
    public abstract void setTitle (String newTitle);

    /**
     * Get the channel's description
     *
     * @return the channel's description, or null if there isn't one
     *
     * @see #setDescription
     */
    public abstract String getDescription();

    /**
     * Set the channel's description
     *
     * @param desc the channel's description, or null if there isn't one
     *
     * @see #getDescription
     */
    public abstract void setDescription (String desc);

    /**
     * Get the channel's list of published links (its URLs). Each
     * element in the returned <tt>Collection</tt> is an
     * {@link RSSLink} object.
     *
     * @return the collection of links, or an empty list if there are none.
     *         The result will never be null.
     *
     * @see #getLink
     * @see #setLinks
     */
    public abstract Collection<RSSLink> getLinks();

    /**
     * Set the channel's list of published links (its URLs).
     *
     * @param links the links
     *
     * @see #getLink
     * @see #getLinks
     */
    public abstract void setLinks (Collection<RSSLink> links);

    /**
     * Get the channel's publication date.
     *
     * @return the date, or null if not available
     *
     * @see #setPublicationDate
     */
    public abstract Date getPublicationDate();

    /**
     * Set the channel's publication date.
     *
     * @param date the publication date, or null if not available
     *
     * @see #getPublicationDate
     */
    public abstract void setPublicationDate (Date date);

    /**
     * Get the channel's copyright string
     *
     * @return the copyright string, or null if not available
     *
     * @see #setCopyright
     */
    public abstract String getCopyright();

    /**
     * Set the channel's copyright string
     *
     * @param copyright  the copyright string, or null if not available
     *
     * @see #getCopyright
     */
    public abstract void setCopyright (String copyright);

    /**
     * Get the RSS format the channel is using, as a string
     *
     * @return the format, or null if not available
     *
     * @see #getNativeRSSFormat
     * @see #setNativeRSSFormat
     */
    public abstract String getRSSFormat();

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
    public abstract void setNativeRSSFormat (Object format);

    /**
     * Get the channel's author list.
     *
     * @return the authors, or null (or an empty <tt>Collection</tt>) if
     *         not available
     *
     * @see #addAuthor
     * @see #clearAuthors
     * @see #setAuthor
     */
    public abstract Collection<String> getAuthors();

    /**
     * Add to the channel's author list.
     *
     * @param author  another author string to add
     *
     * @see #getAuthors
     * @see #clearAuthors
     * @see #setAuthor
     */
    public abstract void addAuthor (String author);

    /**
     * Clear the authors list.
     *
     * @see #getAuthors
     * @see #addAuthor
     * @see #setAuthor
     */
    public abstract void clearAuthors();
}
