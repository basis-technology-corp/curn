/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2009 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. The end-user documentation included with the redistribution, if any,
     must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2009 Brian M. Clapper."

     Alternately, this acknowlegement may appear in the software itself,
     if wherever such third-party acknowlegements normally appear.

  3. Neither the names "clapper.org", "curn", nor any of the names of the
     project contributors may be used to endorse or promote products
     derived from this software without prior written permission. For
     written permission, please contact bmc@clapper.org.

  4. Products derived from this software may not be called "curn", nor may
     "clapper.org" appear in their names without prior written permission
     of Brian M. Clapper.

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

package org.clapper.curn.parser.rome;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.RSSLink;
import org.clapper.curn.parser.ParserUtil;

import org.clapper.util.logging.Logger;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.clapper.curn.parser.RSSContent;
import org.clapper.curn.parser.RSSLinkChangeListener;
import org.clapper.curn.parser.RSSLinkChangeListenerAdapter;
import org.clapper.util.text.TextUtil;

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
     * Parent channel
     */
    private RSSChannel channel;

    /**
     * For log messages
     */
    private static final Logger log = new Logger (RSSItemAdapter.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>RSSItemAdapter</tt> object that wraps the specified
     * Rome <tt>SyndEntry</tt> object.
     *
     * @param entry         the <tt>SyndEntry</tt> object
     * @param parentChannel parent <tt>RSSChannel</tt>
     */
    RSSItemAdapter(SyndEntry entry, RSSChannel parentChannel)
    {
        super();

        this.entry   = entry;
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
        return new RSSItemAdapter(new SyndEntryImpl(), channel);
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
        // Rome leaves leading, trailing and embedded newlines in place.
        // While this is syntactically okay, curn prefers the description
        // to be one long line. ParserUtil.normalizeCharacterData() strips
        // leading and trailing newlines, and converts embedded newlines to
        // spaces.

        return ParserUtil.normalizeCharacterData(entry.getTitle());
    }

    /**
     * Set the item's title
     *
     * @param newTitle  the item's title, or null if there isn't one
     *
     * @see #getTitle
     */
    public void setTitle(String newTitle)
    {
        entry.setTitle(newTitle);
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
        // Since ROME doesn't support multiple links per item, we have to
        // assume that this link is the link for the item. Try to figure
        // out the MIME type, and default to the MIME type for an RSS feed.
        // Mark the feed as type "self".

        Collection<RSSLink> results = new ArrayList<RSSLink>();

        RSSLinkChangeListener changeListener = new RSSLinkChangeListenerAdapter()
        {
            @Override
            public void onURLChange(RSSLink link, URL oldURL, URL newURL)
            {
                log.debug("Changing URL from \"" + oldURL.toString() +
                          "\" to \"" + newURL.toString() + "\"");
                entry.setLink(newURL.toString());
            }
        };

        try
        {
            String link = entry.getLink();
            if (! TextUtil.stringIsEmpty(link))
            {
                URL url = new URL(link);
                results.add(new RSSLink(url,
                                        ParserUtil.getLinkMIMEType (url),
                                        RSSLink.Type.SELF,
                                        changeListener));
            }
        }

        catch (MalformedURLException ex)
        {
            log.error("Feed \"" + channel.getURL().toString() + "\": " +
                      "Bad item URL from underlying parser: \"" +
                      entry.getLink() + "\" (" + ex.toString() + ")");
        }

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
        // Since ROME doesn't support multiple links per item, we have
        // to assume that the first link is the link for the item.

        if ((links != null) && (links.size() > 0))
        {
            RSSLink link = links.iterator().next();
            entry.setLink(link.getURL().toExternalForm());
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
        String       result  = null;
        SyndContent  content = entry.getDescription();

        if (content != null)
        {
            // Rome leaves leading, trailing and embedded newlines in place.
            // While this is syntactically okay, curn prefers the description
            // to be one long line. ParserUtil.normalizeCharacterData() strips
            // leading and trailing newlines, and converts embedded newlines to
            // spaces.

            result = content.getValue();
            if (result != null)
                result = ParserUtil.normalizeCharacterData(result);
        }

        return result;
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
        SyndContent  content = entry.getDescription();

        if (content == null)
        {
            content = new SyndContentImpl();
            entry.setDescription (content);
        }

        content.setValue (newSummary);
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
        Collection<String> result = null;

        // Some feed typess support multiple authors, and some support a
        // single author. If a feed type supports multiple authors, then ROME
        // appears to set the SyndEntry.authors field. If a feed type
        // supports a single author, then ROME appears to set the
        // SyndEntry.author field. (In other words, it doesn't behave
        // like this interface does, where a single author is mapped into
        // a collection of one.)

        List authors = entry.getAuthors();
        List contributors = entry.getContributors();
        String author = entry.getAuthor();

        if (authors == null)
        {
            if (author != null)
                authors = Collections.singletonList(author);
        }

        if ((authors != null) || (contributors != null))
            result = new TreeSet<String>();

        if (authors != null)
        {
            for (Object oAuthor : authors)
            {
                if (oAuthor != null)
                {
                    String name;

                    if (oAuthor instanceof SyndPerson)
                        name = ((SyndPerson) oAuthor).getName();
                    else
                        name = oAuthor.toString();

                    if ((name != null) && (! TextUtil.stringIsEmpty(name)))
                        result.add(name.trim());
                }
            }
        }

        if (contributors != null)
        {
            for (Object contributor : contributors)
            {
                if (contributor != null)
                {
                    String name;

                    if (contributor instanceof SyndPerson)
                        name = ((SyndPerson) contributor).getName();
                    else
                        name = contributor.toString();

                    if (name != null)
                        result.add(name);
                }
            }
        }

        return result;
    }

    /**
     * Add to the item's author list.
     *
     * @param newAuthor  another author string to add
     *
     * @see #getAuthors
     * @see #clearAuthors
     */
    @SuppressWarnings("unchecked")
    public void addAuthor(String newAuthor)
    {
        List authors = entry.getAuthors();
        String existingAuthor = entry.getAuthor();

        if (authors != null)
        {
            // Assume it's a feed type that supports multiple authors.
            // ROME wants these to be SyndPersonImpl objects.

            SyndPerson person = new SyndPersonImpl();
            person.setName(newAuthor);
            authors.add(person);
            entry.setAuthors(authors);
        }

        else if (existingAuthor == null)
        {
            // Assume, for now, that the feed supports just one author.

            entry.setAuthor(newAuthor);
        }

        else
        {
            // Move from one author to two.

            authors = new ArrayList();
            SyndPerson person = new SyndPersonImpl();
            person.setName(existingAuthor);
            authors.add(person);
            person = new SyndPersonImpl();
            person.setName(newAuthor);
            authors.add(person);
            entry.setAuthor(null);
            entry.setAuthors(authors);
        }
    }

    /**
     * Clear the authors list.
     *
     * @see #getAuthors
     * @see #addAuthor
     */
    public void clearAuthors()
    {
        // Rome doesn't support this field.
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
                    result.add(s);
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
    public void setCategories(Collection<String> categories)
    {
        if (categories == null)
            categories = Collections.emptyList();

        List<SyndCategory> nativeCategories = new ArrayList<SyndCategory>();

        for (String category : categories)
        {
            SyndCategoryImpl nativeCategory = new SyndCategoryImpl();
            nativeCategory.setName(category);
            nativeCategories.add(nativeCategory);
        }

        entry.setCategories(nativeCategories);
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
     * Set the item's publication date.
     *
     * @param date  the new pub date
     *
     * @see #getPublicationDate
     */
    public void setPublicationDate(Date date)
    {
        this.entry.setPublishedDate(date);
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
    public void setID(String id)
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
    @SuppressWarnings("unchecked")
    protected Collection<RSSContent> getContent()
    {
        List<?> contents = entry.getContents();
        Collection<RSSContent> result = null;

        if (contents != null)
        {
            result = new ArrayList<RSSContent>();

            for (Iterator it = contents.iterator(); it.hasNext(); )
            {
                SyndContent syndContent = (SyndContent) it.next();

                // Sometimes, ROME doesn't normalize the content type.
                // Normalize it here.

                String type = syndContent.getType();
                if (type.indexOf('/') == -1)
                {
                    // It's something like "html", instead of "text/html".
                    // Add the "text/" part.

                    type = "text/" + type;
                }

                result.add(new RSSContent(syndContent.getValue(), type));
            }
        }

        return result;
    }

    /**
     * Used by {@link #makeCopy}, this method copies any subclass fields
     * that aren't visible to this class.
     *
     * @param toItem  the other {@link RSSItem} into which to copy fields.
     *                <tt>item</tt> will have been created by a call to
     *                {@link #newInstance}
     */
    protected void copyPrivateFields(RSSItem toItem)
    {
        SyndEntryImpl newEntry = new SyndEntryImpl();
        newEntry.copyFrom(this.entry);
        ((RSSItemAdapter) toItem).entry = newEntry;
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
