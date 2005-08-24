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

package org.clapper.curn.parser;

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
public abstract class RSSChannel extends RSSElement
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                          Public Abstract Methods
    \*----------------------------------------------------------------------*/

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
     *         a copy.)
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
     */
    public abstract String getDescription();

    /**
     * Get the channel's list of published links (its URLs). Each
     * element in the returned <tt>Collection</tt> is an
     * {@link RSSLink} object.
     *
     * @return the collection of links, or an empty list if there are none.
     *         The result will never be null.
     *
     * @see #getLink
     */
    public abstract Collection<RSSLink> getLinks();

    /**
     * Get the channel's publication date.
     *
     * @return the date, or null if not available
     */
    public abstract Date getPublicationDate();

    /**
     * Get the channel's copyright string
     *
     * @return the copyright string, or null if not available
     */
    public abstract String getCopyright();

    /**
     * Get the RSS format the channel is using.
     *
     * @return the format, or null if not available
     */
    public abstract String getRSSFormat();

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
