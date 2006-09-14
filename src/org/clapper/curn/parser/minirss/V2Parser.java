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
 * <p><tt>V2Parser</tt> is a stripped down RSS parser for RSS versions
 * {@link <a href="http://web.resource.org/rss/1.0/">RSS, version 1.0</a>}.
 * It's intended to be invoked once a <tt>MiniRSSParser</tt> has determined
 * whether the XML feed represents version 1 RSS or not.</p>
 *
 * <p>This parser doesn't store all the possible RSS items. It stores those
 * items that the <i>curn</i> utility requires (plus a few more), but
 * lacks support for others. For instance, it ignores <tt>image</tt>,
 * <tt>cloud</tt>, <tt>textinput</tt> and other elements that <i>curn</i>
 * has no interest in displaying. Thus, it is unsuitable for use as a
 * general-purpose RSS 2 parser (though it's perfectly suited for use in
 * <i>curn</i>).</p>
 *
 * @version <tt>$Revision$</tt>
 *
 * @see MiniRSSParser
 * @see V1Parser
 */
public class V2Parser extends ParserCommon
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For logging
     */
    private static final Logger log = new Logger (V2Parser.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new <tt>V2Parser</tt>.
     */
    V2Parser()
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

        Element channelElement = getRequiredElement(rootElement, "channel");
        processChannel(url, channel, channelElement);

        // Now process the items.

        processItems(url, channel, channelElement);
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

        String text = getText(getRequiredElement(channelElement, "link"));
        if (text != null)
        {
            // Some sites use relative URLs in the links. Handle that.

            try
            {
                // RSS version 2 doesn't support multiple links per channel.
                // Assume this link is the link for the feed. Try to figure
                // out the MIME type, and default to the MIME type for an
                // RSS feed. Mark the feed as type "self".

                URL channelUrl = resolveLink(text, channel);
                channel.addLink
                    (new RSSLink (channelUrl,
                                  ParserUtil.getLinkMIMEType (channelUrl),
                                  RSSLink.Type.SELF));
            }

            catch (MalformedURLException ex)
            {
                // Swallow the exception. No sense aborting the whole
                // feed for a bad <link> element.

                log.error("Feed \"" + url.toString() +
                          "\": Bad <link> element \"" + text + "\"",
                          ex);
            }
        }

        channel.setDescription(getText(getRequiredElement(channelElement,
                                                          "description")));

        // Process the channel's optional elements.

        channel.setCopyright(getOptionalChildText(channelElement, "copyright"));

        text = getOptionalChildText(channelElement, "author");
        if (text != null)
            channel.addAuthor(text);

        text = getOptionalChildText(channelElement, "pubDate");
        if (text != null)
            channel.setPublicationDate(parseRFC822Date(text));

        channel.setUniqueID(getOptionalChildText(channelElement, "guid"));
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
        for (Iterator itItem = channelElement.elementIterator("item");
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
        Item item = new Item(channel);
        channel.addItem(item);

        // Title

        item.setTitle(getText(getRequiredElement(itemElement, "title")));

        // Link

        String text = getText(getRequiredElement(itemElement, "link"));
        if (text != null)
        {
            // Some sites use relative URLs in the links. Handle that.

            try
            {
                // RSS version 2 doesn't support multiple links per channel.
                // Assume this link is the link for the feed. Try to figure
                // out the MIME type, and default to the MIME type for an
                // RSS feed. Mark the feed as type "self".

                URL itemUrl = resolveLink(text, channel);
                item.addLink
                    (new RSSLink(itemUrl,
                                 ParserUtil.getLinkMIMEType (itemUrl),
                                 RSSLink.Type.SELF));
            }

            catch (MalformedURLException ex)
            {
                // Swallow the exception. No sense aborting the whole
                // feed for a bad <link> element.

                log.error("Feed \"" + url.toString() +
                          "\": Bad <item> <link> element \"" + text + "\"",
                          ex);
            }
        }

        // Description (optional);

        item.setSummary(getOptionalChildText(itemElement, "description"));

        // Author.

        for (Iterator itAuthor = itemElement.elementIterator("dc:creator");
             itAuthor.hasNext(); )
        {
            Element authorElement = (Element) itAuthor.next();
            text = getText(authorElement);
            if (text != null)
                item.addAuthor(text);
        }

        text = getOptionalChildText(itemElement, "dc:date");
        if (text != null)
            item.setPublicationDate(parseW3CDate(text));
    }
}
