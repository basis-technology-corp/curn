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

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import org.clapper.util.logging.Logger;

import org.clapper.curn.parser.ParserUtil;
import org.clapper.curn.parser.RSSLink;
import org.clapper.util.text.TextUtil;

/**
 * <p><tt>V1Parser</tt> is a stripped down RSS parser for
 * {@link <a href="http://web.resource.org/rss/1.0/">RSS, version 1.0</a>}.
 * It's intended to be invoked once a <tt>MiniRSSParser</tt> has determined
 * whether the XML feed represents version 1 RSS or not.</p>
 *
 * <p>This parser doesn't store all the possible RSS items. It stores those
 * items that the <i>curn</i> utility requires (plus a few more), but
 * lacks support for others. For instance, it ignores <tt>image</tt>,
 * <tt>cloud</tt>, <tt>textinput</tt> and other elements that <i>curn</i>
 * has no interest in displaying. Thus, it is unsuitable for use as a
 * general-purpose Atom parser (though it's perfectly suited for use in
 * <i>curn</i>).</p>
 *
 * @version <tt>$Revision$</tt>
 *
 * @see MiniRSSParser
 * @see V2Parser
 */
public class V1Parser extends ParserCommon
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For logging
     */
    private static final Logger log = new Logger (V1Parser.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new <tt>V1Parser</tt> to parse the remainder of an RSS
     * XML file, filling the specified <tt>Channel</tt> object with the
     * parsed contents.
     *
     * @param channel      the <tt>Channel</tt> to be filled
     * @param url          the URL for the feed
     * @param firstElement the first element in the file, which was already
     *                     parsed by a <tt>MiniRSSParser</tt> object, before
     *                     it handed control to this object
     */
    V1Parser (Channel channel, URL url, String firstElement)
    {
        super (channel, url, log);
        elementStack.push (new ElementStackEntry (firstElement, firstElement));
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
                        Overriding XMLReaderAdapter
    \*----------------------------------------------------------------------*/

    /**
     * Handle the start of an XML element.
     *
     * @param namespaceURI       the Namespace URI, or the empty string if the
     *                           element has no Namespace URI or if Namespace
     *                           processing is not being performed
     * @param namespaceLocalName the local name (without prefix), or the empty
     *                           string if Namespace processing is not being
     *                           performed.
     * @param elementName        the qualified element name (with prefix), or
     *                           the empty string if qualified names are not
     *                           available
     * @param attributes         the attributes attached to the element.
     *
     * @throws SAXException parsing error
     */
    public void startElement (String     namespaceURI,
                              String     namespaceLocalName,
                              String     elementName,
                              Attributes attributes)
        throws SAXException
    {
        ElementStackEntry entry = (ElementStackEntry) elementStack.peek();
        Object container = entry.getContainer();

        if (elementName.equals ("channel"))
        {
            startChannel (elementName, attributes);
        }

        else if (elementName.equals ("item"))
        {
            channel.addItem (startItem (elementName, attributes));
        }

        else if (container instanceof Channel)
        {
            startChannelSubelement (elementName, attributes, entry);
        }

        else if (container instanceof Item)
        {
            startItemSubelement (elementName, attributes, entry);
        }

        else
        {
            elementStack.push (new ElementStackEntry (elementName,
                                                      elementName));
        }
    }

    /**
     * Handle the end of an XML element.
     *
     * @param namespaceURI       the Namespace URI, or the empty string if the
     *                           element has no Namespace URI or if Namespace
     *                           processing is not being performed
     * @param namespaceLocalName the local name (without prefix), or the empty
     *                           string if Namespace processing is not being
     *                           performed.
     * @param elementName        the qualified element name (with prefix), or
     *                           the empty string if qualified names are not
     *                           available
     *
     * @throws SAXException parsing error
     */
    public void endElement (String namespaceURI,
                            String namespaceLocalName,
                            String elementName)
        throws SAXException
    {
        if (elementStack.empty())
            throw new SAXException ("(BUG) Empty stack at end of element.");

        ElementStackEntry entry = (ElementStackEntry) elementStack.pop();
        String entryElementName = entry.getElementName();

        if (! entryElementName.equals (elementName))
        {
            throw new SAXException ("Element \"" +
                                    elementName +
                                    "\" doesn't match element on stack (\"" +
                                    entry.getElementName() +
                                    "\")");
        }

        Object container = entry.getContainer();

        if (container != null)
        {
            if (container instanceof Channel)
                endChannelElement (elementName, entry);

            else if (container instanceof Item)
                endItemElement (elementName, entry);
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Handle the start of a channel element.
     *
     * @param elementName  the element name, which is pushed onto the stack
     *                     along with the <tt>Channel</tt> object
     * @param attributes   the attributes
     *
     * @throws SAXException on error
     */
    private void startChannel (String elementName, Attributes attributes)
        throws SAXException
    {
        String id = attributes.getValue ("rdf:about");

        if (id != null)
            channel.setUniqueID (id);

        elementStack.push (new ElementStackEntry (elementName, channel));
    }

    /**
     * Handle the start of an item element.
     *
     * @param elementName  the element name, which is pushed onto the stack
     *                     along with the <tt>Item</tt> object
     * @param attributes   the attributes
     *
     * @return the <tt>Item</tt> element that was created and pushed on the
     *         stack
     *
     * @throws SAXException on error
     */
    private Item startItem (String elementName, Attributes attributes)
        throws SAXException
    {
        Item    item = new Item (channel);
        String  id   = attributes.getValue ("rdf:about");

        if (id != null)
            item.setID (id);

        elementStack.push (new ElementStackEntry (elementName, item));

        return item;
    }

    /**
     * Handle the start of an XML element that's nested within a "channel"
     * element.
     *
     * @param elementName       the element name, which is pushed onto the
     *                          stack along with the <tt>Channel</tt> object
     * @param attributes        the attributes (currently not used)
     * @param parentStackEntry  the stack entry for the parent channel element
     *
     * @throws SAXException on error
     */
    private void startChannelSubelement (String            elementName,
                                         Attributes        attributes,
                                         ElementStackEntry parentStackEntry)
        throws SAXException
    {
        Channel theChannel = (Channel) parentStackEntry.getContainer();

        elementStack.push (new ElementStackEntry (elementName, theChannel));
    }

    /**
     * Handle the start of an XML element that's nested within an "item"
     * element.
     *
     * @param elementName       the element name, which is pushed onto the
     *                          stack along with the <tt>Item</tt> object
     * @param attributes        the attributes (currently not used)
     * @param parentStackEntry  the stack entry for the parent item element
     *
     * @throws SAXException on error
     */
    private void startItemSubelement (String            elementName,
                                      Attributes        attributes,
                                      ElementStackEntry parentStackEntry)
        throws SAXException
    {
        Item item = (Item) parentStackEntry.getContainer();
        elementStack.push (new ElementStackEntry (elementName, item));
    }


    /**
     * Handles the end of a channel element. This includes the channel
     * element itself and any nested channel elements. This method should
     * only be called with the popped stack entry is known to have
     * a <tt>Channel</tt> object in its <tt>container</tt> data field.
     *
     * @param elementName the name of the XML element being ended
     * @param stackEntry  the associated (popped) stack entry
     *
     * @throws SAXException on error
     */
    private void endChannelElement (String            elementName,
                                    ElementStackEntry stackEntry)
        throws SAXException
    {
        Channel theChannel = (Channel) stackEntry.getContainer();
        String  chars      = stackEntry.getCharacters().trim();

        if (TextUtil.stringIsEmpty (chars))
            chars = null;

        if (chars != null)
        {
            if (elementName.equals ("title"))
                theChannel.setTitle (chars);

            else if (elementName.equals ("link"))
            {
                // Some sites use relative URLs in the links. Handle
                // that, too.

                try
                {
                    // RSS version 1 doesn't support multiple links per
                    // channel. Assume this link is the link for the feed.
                    // Try to figure out the MIME type, and default to the
                    // MIME type for an RSS feed. Mark the feed as type
                    // "self".

                    URL url = resolveLink (chars, channel);
                    theChannel.addLink (new RSSLink
                                            (url,
                                             ParserUtil.getLinkMIMEType (url),
                                             RSSLink.Type.SELF));
                }

                catch (MalformedURLException ex)
                {
                    // Swallow the exception. No sense aborting the whole
                    // feed for a bad <link> element.

                    log.error ("Feed \"" +
                               url.toString() +
                               "\": Bad <link> element \"" +
                               chars +
                               "\"", ex);
                }
            }

            else if (elementName.equals ("description"))
                theChannel.setDescription (chars);

            else if (elementName.equals ("dc:date"))
                theChannel.setPublicationDate (parseW3CDate (chars));

            else if (elementName.equals ("dc:creator"))
                theChannel.addAuthor (chars);
        }
    }

    /**
     * Handles the end of an item element. This includes the item
     * element itself and any nested item elements. This method should
     * only be called with the popped stack entry is known to have
     * a <tt>Item</tt> object in its <tt>container</tt> data field.
     *
     * @param elementName the name of the XML element being ended
     * @param stackEntry  the associated (popped) stack entry
     *
     * @throws SAXException on error
     */
    private void endItemElement (String            elementName,
                                 ElementStackEntry stackEntry)
        throws SAXException
    {
        Item    item  = (Item) stackEntry.getContainer();
        String  chars   = stackEntry.getCharacters().trim();

        if (TextUtil.stringIsEmpty (chars))
            chars = null;

        if (chars != null)
        {
            if (elementName.equals ("title"))
                item.setTitle (chars);

            else if (elementName.equals ("link"))
                setItemLink (item, chars);

            else if (elementName.equals ("description"))
                item.setSummary (chars);

            else if (elementName.equals ("dc:date"))
                item.setPublicationDate (parseW3CDate (chars));

            else if (elementName.equals ("dc:creator"))
                item.addAuthor (chars);
        }
    }

    /**
     * Some sites (notably, O'Reilly's Meerket) specify links with
     * additional cruft in them, e.g., "13027@http://www.gizmodo.com/".
     * This method is a hack that strips such things out before storing the
     * link in the item.
     *
     * @param item  the item
     * @param sUrl  the string containing the URL
     *
     * @throws SAXException on error
     */
    private void setItemLink (Item item, String sUrl)
        throws SAXException
    {
        URL url = null;

        try
        {
            url = new URL (sUrl);
        }

        catch (MalformedURLException ex)
        {
            // Scan forward, character by character, looking for a valid
            // protocol. Abort if (a) there's no ":" anywhere in the
            // string, or (b) we reach the ":" without finding a valid URL.

            int iColon = sUrl.indexOf (':');
            if (iColon < 0)
            {
                throw new SAXException ("Can't save item link \"" +
                                        sUrl +
                                        "\": " +
                                        ex.toString());
            }

            boolean ok = false;
            for (int i = 1; i < iColon; i++)
            {
                try
                {
                    url = new URL (sUrl.substring (i));
                    ok = true;
                    break;
                }

                catch (MalformedURLException ex2)
                {
                }
            }

            if (! ok)
            {
                // Swallow the exception. No sense aborting the whole feed
                // for a bad <link> element.

                log.error ("Bad <link> element \"" + sUrl + "\"", ex);
                url = null;
            }
        }

        if (url != null)
        {
            // RSS version 1 doesn't support multiple links per item.
            // Assume this link is the link for the item's XML. Try to
            // figure out the MIME type, and default to the MIME type for
            // an RSS feed. Mark the feed as type "self".

            item.addLink (new RSSLink (url,
                                       ParserUtil.getLinkMIMEType (url),
                                       RSSLink.Type.SELF));
        }
    }
}
