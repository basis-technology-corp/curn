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

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.clapper.util.logging.Logger;

import org.clapper.curn.parser.ParserUtil;
import org.clapper.curn.parser.RSSLink;
import org.clapper.curn.parser.RSSParserException;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * <p><tt>AtomParser</tt> is a stripped down parser for an
 * {@link <a href="http://www.atomenabled.org/developers/">Atom</a>}
 * XML feed. It's intended to be invoked once a <tt>MiniRSSParser</tt> has
 * determined that the XML feed is an Atom feed.</p>
 *
 * <p>This parser doesn't store all the possible items. It stores those
 * items that the <i>curn</i> utility requires (plus a few more), but
 * lacks support for others. Thus, it is unsuitable for use as a
 * general-purpose Atom parser (though it's perfectly suited for use
 * in <i>curn</i>).</p>
 *
 * @version <tt>$Revision$</tt>
 *
 * @see MiniRSSParser
 * @see V1Parser
 */
public class AtomParser extends ParserCommon
{

    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    interface Author
    {
        void setAuthorName (String name);
    }

    class ChannelAuthor implements Author
    {
        Channel parentChannel = null;

        ChannelAuthor (Channel parentChannel)
        {
            this.parentChannel = parentChannel;
        }

        public void setAuthorName (String name)
        {
            parentChannel.addAuthor (name);
        }
    }

    class ItemAuthor implements Author
    {
        Item parentItem = null;

        ItemAuthor (Item parentItem)
        {
            this.parentItem = parentItem;
        }

        public void setAuthorName (String name)
        {
            parentItem.addAuthor (name);
        }
    }

    class ItemContent
    {
        String mimeType = null;
        Item   parentItem = null;
        StringBuffer buf = new StringBuffer();

        ItemContent (String mimeType, Item parentItem)
        {
            this.mimeType = mimeType;
            this.parentItem = parentItem;
        }
    }

    class ItemContentSubelement
    {
        ItemContent parentContent;

        ItemContentSubelement (ItemContent parent)
        {
            this.parentContent = parent;
        }
    }

    class ItemCategory
    {
        String term = null;
        Item   parentItem = null;

        ItemCategory (String term, Item parentItem)
        {
            this.term = term;
            this.parentItem = parentItem;
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For logging
     */
    private static final Logger log = new Logger (AtomParser.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new <tt>V2Parser</tt>.
     */
    AtomParser()
    {
        super(log);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse a loaded document.
     *
     * @param channel  the <tt>Channel</tt> to be filled
     * @param url      the URL for the feed
     * @param document the DOM4J document
     *
     * @throws RSSParserException on error
     */
    public void process(Channel channel, URL url, Document document)
        throws RSSParserException
    {
        Element rootElement = document.getRootElement();

        // Get and process the channel.

        if (! rootElement.getName().equals("feed"))
        {
            throw new RSSParserException
                ("Feed \"" + url.toString() + "\": Found <" +
                 rootElement.getName() + "> element where <feed> " +
                 "element was expected.");
        }

        processChannel(url, channel, rootElement);

        // Now process the items.

        processItems(url, channel, rootElement);
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Process the data in a channel, not including the items.
     *
     * @param channel        the Channel object to file
     * @param url            the URL of the document
     * @param channelElement the <channel> element
     *
     * @throws RSSParserException on error
     */
    private void processChannel(final URL      url,
                                final Channel  channel,
                                final Element  channelElement)
        throws RSSParserException
    {
        channel.setTitle(getText(getRequiredElement(channelElement, "title")));

        // Link(s)

        for (Iterator itLink = channelElement.elementIterator("link");
             itLink.hasNext(); )
        {
            Element linkElement = (Element) itLink.next();
            RSSLink link = parseLink(linkElement, channel, url);
            if (link != null)
                channel.addLink(link);
        }

        if (channel.getLinks().size() == 0)
        {
            throw new RSSParserException
                ("Missing required <link> element in <entry>");
        }

        channel.setUniqueID(getText(getRequiredElement(channelElement, "id")));

        // Process the channel's optional elements.

        // There are four possible places to get the publication date. NOTE:
        // The "created" element was dropped in Atom 1.0 but is supported here
        // for backward compatibility.

        String text;
        if (((text = getOptionalChildText(channelElement, "issued")) != null)
                                       ||
            ((text = getOptionalChildText(channelElement, "modified")) != null)
                                       ||
            ((text = getOptionalChildText(channelElement, "created")) != null)
                                       ||
            ((text = getOptionalChildText(channelElement, "published")) != null))
        {
            channel.setPublicationDate(parseW3CDate(text));
        }

        channel.setCopyright(getOptionalChildText(channelElement, "rights"));
        channel.setUniqueID(getOptionalChildText(channelElement, "id"));

        // Author. Multiple are supported. Also count contributors, which Atom
        // summarizes separately.

        Iterator itAuthor;

         for (itAuthor = channelElement.elementIterator("author");
              itAuthor.hasNext(); )
        {
            Element authorElement = (Element) itAuthor.next();
            text = getText(authorElement);
            if (text != null)
                channel.addAuthor(text);
        }

        for (itAuthor = channelElement.elementIterator("contributor");
              itAuthor.hasNext(); )
        {
            Element authorElement = (Element) itAuthor.next();
            text = getText(authorElement);
            if (text != null)
                channel.addAuthor(text);
        }
    }

    /**
     * Process the channel's items.
     *
     * @param url             URL of the feed being processed
     * @param channel         the channel being filled.
     * @param channelElement  the <channel> element
     *
     * @throws RSSParserException on error
     */
    private void processItems(URL      url,
                              Channel  channel,
                              Element  channelElement)
        throws RSSParserException
    {
        for (Iterator itItem = channelElement.elementIterator("entry");
             itItem.hasNext(); )
        {
            Element itemElement = (Element) itItem.next();
            processItem(url, itemElement, channel);
        }
    }

    private void processItem(final URL     url,
                             final Element itemElement,
                             final Channel channel)
        throws RSSParserException
    {
        String text;

        Item item = new Item(channel);
        channel.addItem(item);

        // Title

        item.setTitle(getText(getRequiredElement(itemElement, "title")));

        // Link(s)

        for (Iterator itLink = itemElement.elementIterator("link");
             itLink.hasNext(); )
        {
            Element linkElement = (Element) itLink.next();
            RSSLink link = parseLink(linkElement, channel, url);
            if (link != null)
                item.addLink(link);
        }

        if (item.getLinks().size() == 0)
        {
            throw new RSSParserException
                ("Missing required <link> element in <entry>");
        }

        // Description (optional);

        item.setSummary(getOptionalChildText(itemElement, "summary"));

        // ID.

        item.setID(getOptionalChildText(itemElement, "id"));

        // Author.

        for (Iterator itAuthor = itemElement.elementIterator("author");
             itAuthor.hasNext(); )
        {
            Element authorElement = (Element) itAuthor.next();
            text = getText(authorElement);
            if (text != null)
                channel.addAuthor(text);
        }

        // Date. Note: The <issued> element is replaced by <published> in
        // Atom 1.0. <issued> is supported here for backward compatibility.

        if (((text = getOptionalChildText(itemElement, "published")) != null)
                                       ||
            ((text = getOptionalChildText(itemElement, "issued")) != null))
        {
            channel.setPublicationDate(parseW3CDate(text));
        }
    }

    /**
     * Parse a "link" element.
     *
     * @param linkElement the element
     * @param channel     the channel, for resolving relative links
     * @param linkURL     the feed's URL, for error messages
     *
     * @return the RSSLink item, or null on error or unsupported link type
     */
    private RSSLink parseLink(Element linkElement, Channel channel, URL url)
    {
        String sLinkURL = linkElement.attributeValue("href");
        URL linkURL = null;
        if (sLinkURL != null)
        {
            sLinkURL = sLinkURL.trim();
            if (sLinkURL.length() == 0)
                sLinkURL = null;

            else
            {
                // Some sites use relative URLs in the links. Handle
                // that, too.

                try
                {
                    linkURL = resolveLink(sLinkURL, channel);
                }

                catch (MalformedURLException ex)
                {
                    // Swallow the exception. No sense aborting the whole
                    // feed for a bad <link> element.

                    log.error("Feed \"" + url.toString() +
                              "\": Bad <link> element \"" + sLinkURL + "\"",
                              ex);
                }
            }
        }

        String sType = linkElement.attributeValue("rel");
        RSSLink.Type linkType = RSSLink.Type.SELF;
        if (sType != null)
        {
            // Currently, we only support rel="alternative" and rel="self"

            sType = sType.trim();
            if (sType.length() == 0)
                sType = null;

            else if (sType.equals("self"))
                linkType = RSSLink.Type.SELF;

            else if (sType.equals("alternate"))
                linkType = RSSLink.Type.ALTERNATE;

            else
                sType = null;
        }

        String mimeType = linkElement.attributeValue("type");
        if (mimeType != null)
        {
            mimeType = mimeType.trim();
            if (mimeType.length() == 0)
                mimeType = null;

            if ((mimeType == null) && (linkType == RSSLink.Type.SELF))
                mimeType = ParserUtil.RSS_MIME_TYPE;
        }

        RSSLink result = null;
        if ((linkURL != null) && (sType != null) && (mimeType != null))
            result = new RSSLink(linkURL, mimeType, linkType);

        return result;
    }
}
