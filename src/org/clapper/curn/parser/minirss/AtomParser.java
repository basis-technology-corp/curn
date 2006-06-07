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

package org.clapper.curn.parser.minirss;

import org.clapper.curn.parser.ParserUtil;
import org.clapper.curn.parser.RSSItem;
import org.clapper.curn.parser.RSSLink;

import org.clapper.util.logging.Logger;

import java.net.URL;
import java.net.MalformedURLException;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

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
 * @see V2Parser
 * @see V1Parser
 */
public class AtomParser extends ParserCommon
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For logging
     */
    private static Logger log = new Logger (AtomParser.class);

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
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Creates a new <tt>AtomParser</tt> to parse the remainder of an RSS
     * XML file, filling the specified <tt>Channel</tt> object with the
     * parsed contents.
     *
     * @param channel      the <tt>Channel</tt> to be filled
     * @param URL          URL for the feed
     * @param firstElement the first element in the file, which was already
     *                     parsed by a <tt>MiniRSSParser</tt> object, before
     *                     it handed control to this object
     */
    AtomParser (Channel channel, URL url, String elementName)
        throws SAXException
    {
        super (channel, url, log);
        startChannel (elementName, null);
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
        ElementStackEntry entry = elementStack.peek();
        Object container = entry.getContainer();

        if (elementName.equals ("feed"))
            startChannel (elementName, attributes);

        else if (container instanceof Channel)
            startChannelSubelement (elementName, attributes, entry);

        else if (container instanceof Item)
            startItemSubelement (elementName, attributes, entry);

        else if (container instanceof Author)
            startAuthorSubelement (elementName, attributes, entry);

        else if ((container instanceof ItemContent) ||
                 (container instanceof ItemContentSubelement))
            startItemContentSubelement (elementName, attributes, entry);
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
            String msg = "Element \""
                       + elementName
                       + "\" doesn't match element on stack (\""
                       + entryElementName
                       + "\")";

            log.error (msg);
            log.error ("", entry.getAllocationPoint());
            throw new SAXException (msg);
        }

        Object container = entry.getContainer();

        if (container != null)
        {
            if (container instanceof Channel)
                endChannelElement (elementName, entry);

            else if (container instanceof Item)
                endItemElement (elementName, entry);

            else if (container instanceof Author)
                endAuthorElement (elementName, entry);

            else if (container instanceof ItemContent)
                endItemContentElement (elementName, entry);

            else if (container instanceof ItemContentSubelement)
                endItemContentSubelement (elementName, entry);

            else if (container instanceof ItemCategory)
                endItemCategoryElement (elementName, entry);
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Handle the start of the channel element.
     *
     * @param elementName  the element name, which is pushed onto the stack
     *                     along with the <tt>Channel</tt> object
     * @param attributes   the attributes (currently not used)
     *
     * @throws SAXException on error
     */
    private void startChannel (String     elementName,
                               Attributes attributes)
        throws SAXException
    {
        elementStack.push (new ElementStackEntry (elementName, channel));
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

        if (elementName.equals ("entry"))
        {
            Item item = new Item (channel);

            theChannel.addItem (item);
            elementStack.push (new ElementStackEntry (elementName, item));
        }

        else if (elementName.equals ("author"))
        {
            Author author = new ChannelAuthor (channel);

            elementStack.push (new ElementStackEntry (elementName, author));
        }

        else if (elementName.equals ("link"))
        {
            RSSLink link = parseLink (attributes);
            if (link != null)
                theChannel.addLink (link);

            // Push it regardless, or the endElement() call will puke when
            // it pops off the wrong thing.

            elementStack.push (new ElementStackEntry (elementName,
                                                      theChannel));
        }

        else
        {
            elementStack.push (new ElementStackEntry (elementName,
                                                      theChannel));
        }
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
        String  chars   = stackEntry.getCharacters().trim();

        if (chars.length() == 0)
            chars = null;

        if (chars != null)
        {
            if (elementName.equals ("title"))
                theChannel.setTitle (chars);

            else if (elementName.equals ("issued"))
                theChannel.setPublicationDate (parseW3CDate (chars));

            else if (elementName.equals ("modified"))
                theChannel.setPublicationDate (parseW3CDate (chars));

            // The <created> element was dropped in Atom 1.0, but is
            // supported here for backward compatibility.

            else if (elementName.equals ("created"))
                theChannel.setPublicationDate (parseW3CDate (chars));

            else if (elementName.equals ("published"))
                theChannel.setPublicationDate (parseW3CDate (chars));

            else if (elementName.equals ("id"))
                theChannel.setUniqueID (chars);
        }
    }

    /**
     * Handle the start of an XML element that's nested within an "author"
     * element.
     *
     * @param elementName       the element name, which is pushed onto the
     *                          stack along with the <tt>Item</tt> object
     * @param attributes        the attributes (currently not used)
     * @param parentStackEntry  the stack entry for the parent item element
     *
     * @throws SAXException on error
     */
    private void startAuthorSubelement (String            elementName,
                                        Attributes        attributes,
                                        ElementStackEntry parentStackEntry)
        throws SAXException
    {
        Author author = (Author) parentStackEntry.getContainer();
        elementStack.push (new ElementStackEntry (elementName, author));
    }

    /**
     * Handles the end of an author element. This includes the author
     * element itself and any nested elements. This method should
     * only be called with the popped stack entry is known to have
     * a <tt>Author</tt> object in its <tt>container</tt> data field.
     *
     * @param elementName the name of the XML element being ended
     * @param stackEntry  the associated (popped) stack entry
     *
     * @throws SAXException on error
     */
    private void endAuthorElement (String            elementName,
                                   ElementStackEntry stackEntry)
        throws SAXException
    {
        Author  author  = (Author) stackEntry.getContainer();
        String  chars   = stackEntry.getCharacters().trim();

        if (chars.length() == 0)
            chars = null;

        if (chars != null)
        {
            if (elementName.equals ("name"))
                author.setAuthorName (chars);
        }
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

        if (elementName.equals ("link"))
        {
            RSSLink link = parseLink (attributes);
            if (link != null)
                item.addLink (link);

            // Push it regardless, or the endElement() call will puke when
            // it pops off the wrong thing.

            elementStack.push (new ElementStackEntry (elementName, channel));
        }

        else if (elementName.equals ("content"))
        {
            startItemContentElement (elementName, attributes, item);
        }

        else if (elementName.equals ("category"))
        {
            startItemCategoryElement (elementName, attributes, item);
        }

        else if (elementName.equals ("author"))
        {
            Author author = new ItemAuthor (item);

            elementStack.push (new ElementStackEntry (elementName, author));
        }

        else
        {
            elementStack.push (new ElementStackEntry (elementName, item));
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

        if (chars.length() == 0)
            chars = null;

        if (chars != null)
        {
            if (elementName.equals ("title"))
                item.setTitle (chars);

            else if (elementName.equals ("summary"))
                item.setSummary (chars);

            // NOTE: The <issued> element is replaced by <published> in
            // Atom 1.0. <issued> is supported here for backward
            // compatibility.

            else if (elementName.equals ("issued") ||
                     elementName.equals ("published"))
                item.setPublicationDate (parseW3CDate (chars));

            else if (elementName.equals ("id"))
                item.setID (chars);

            else if (elementName.equals ("entry"))
            {
                // End of item. Any cleanup goes here.
            }
        }
    }

    /**
     * Handles the start of an item's nested content element.
     *
     * @param elementName  the element name, which is pushed onto the
     *                     stack along with the <tt>Item</tt> object
     * @param attributes   the attributes
     * @param item         the parent Item
     *
     * @throws SAXException on error
     */
    private void startItemContentElement (String     elementName,
                                          Attributes attributes,
                                          Item       item)
        throws SAXException
    {
        String      mimeType = attributes.getValue ("type");
        ItemContent content;

        if ((mimeType != null) && (mimeType.trim().length() == 0))
            mimeType = null;

        if (mimeType == null)
            mimeType = "text/plain";

        content = new ItemContent (mimeType, item);
        elementStack.push (new ElementStackEntry (elementName, content));
    }

    /**
     * Handles the end of an item's nested content element.
     *
     * @param elementName  the name of the element being ended
     * @param stackEntry   the associated (popped) stack entry
     *
     * @throws SAXException on error
     */
    private void endItemContentElement (String            elementName,
                                        ElementStackEntry stackEntry)
        throws SAXException
    {
        ItemContent content   = (ItemContent) stackEntry.getContainer();
        String      chars     = stackEntry.getCharacters().trim();
        boolean     isDefault = false;

        content.parentItem.setContent (chars, content.mimeType);

        if (content.mimeType.equals ("text/plain"))
        {
            // text/plain is the default

            content.parentItem.setContent (chars,
                                           RSSItem.DEFAULT_CONTENT_TYPE);
        }
    }

    /**
     * Handles the start of an element within a content element.
     *
     * @param elementName       the element name, which is pushed onto the
     *                          stack along with the <tt>Item</tt> object
     * @param attributes        the attributes
     * @param parentStackEntry  the stack entry for the parent content element
     *
     * @throws SAXException on error
     */
    private void startItemContentSubelement (String            elementName,
                                             Attributes        attributes,
                                             ElementStackEntry parentStackEntry)
    {
        Object container = parentStackEntry.getContainer();
        ItemContent content;

        if (container instanceof ItemContent)
        {
            // First subelement within <content>.

            content = (ItemContent) container;
        }

        else if (container instanceof ItemContentSubelement)
        {
            // More deeply nested subelements within <content>

            content = ((ItemContentSubelement) container).parentContent;
        }

        else
        {
            assert (false);
            throw new IllegalStateException();
        }

        // Nested <content> elements are treated verbatim. They might be
        // unescaped HTML markup.

        content.buf.append ("<");
        content.buf.append (elementName);

        // Loop over the attributes and add them to the output.

        for (int i = 0; i < attributes.getLength(); i++)
        {
            content.buf.append (" ");
            content.buf.append (attributes.getLocalName (i));
            content.buf.append ("=");
            content.buf.append (attributes.getValue (i));
        }

        content.buf.append (">");

        // Push subelement on the stack.

        ItemContentSubelement subelement = new ItemContentSubelement (content);
        elementStack.push (new ElementStackEntry (elementName, subelement));
    }

    /**
     * Handles the end of an element nested within an item's content
     * element.
     *
     * @param elementName  the name of the element being ended
     * @param stackEntry   the associated (popped) stack entry
     *
     * @throws SAXException on error
     */
    private void endItemContentSubelement (String            elementName,
                                           ElementStackEntry stackEntry)
        throws SAXException
    {
        ItemContentSubelement subelement =
            (ItemContentSubelement) stackEntry.getContainer();

        String chars  = stackEntry.getCharacters().trim();

        if (chars.length() == 0)
            chars = null;

        // Nested <content> elements are treated verbatim. They might be
        // unescaped HTML markup.

        if (chars != null)
            subelement.parentContent.buf.append (chars);

        subelement.parentContent.buf.append ("</");
        subelement.parentContent.buf.append (elementName);
        subelement.parentContent.buf.append (">");
    }

    /**
     * Handles the start of an item's nested category element.
     *
     * @param elementName  the element name, which is pushed onto the
     *                     stack along with the <tt>Item</tt> object
     * @param attributes   the attributes
     * @param item         the parent Item
     *
     * @throws SAXException on error
     */
    private void startItemCategoryElement (String     elementName,
                                           Attributes attributes,
                                           Item       item)
        throws SAXException
    {
        String        term = attributes.getValue ("term");
        ItemCategory  category;

        if ((term != null) && (term.trim().length() == 0))
            term = null;

        category = new ItemCategory (term, item);
        elementStack.push (new ElementStackEntry (elementName, category));
    }

    /**
     * Handles the end of an item's nested category element.
     *
     * @param elementName  the name of the element being ended
     * @param stackEntry   the associated (popped) stack entry
     *
     * @throws SAXException on error
     */
    private void endItemCategoryElement (String            elementName,
                                         ElementStackEntry stackEntry)
        throws SAXException
    {
        ItemCategory category  = (ItemCategory) stackEntry.getContainer();

        if (category.term != null)
            category.parentItem.addCategory (category.term);
    }

    /**
     * Parse a "link" element.
     *
     * @param attributes  the element's attributes
     *
     * @return the RSSLink item, or null on error or unsupported link type
     */
    private RSSLink parseLink (Attributes attributes)
    {
        String sURL = attributes.getValue ("href");
        URL url = null;
        if (sURL != null)
        {
            sURL = sURL.trim();
            if (sURL.length() == 0)
                sURL = null;

            else
            {
                // Some sites use relative URLs in the links. Handle
                // that, too.

                try
                {
                    url = resolveLink (sURL, channel);
                }

                catch (MalformedURLException ex)
                {
                    // Swallow the exception. No sense aborting the whole
                    // feed for a bad <link> element.

                    log.error ("Feed \""
                             + url.toString()
                             + "\": Bad <link> element \""
                             + sURL
                             + "\"", ex);
                }
            }
        }

        String sType = attributes.getValue ("rel");
        RSSLink.Type linkType = RSSLink.Type.SELF;
        if (sType != null)
        {
            // Currently, we only support rel="alternative" and rel="self"

            sType = sType.trim();
            if (sType.length() == 0)
                sType = null;

            else if (sType.equals ("self"))
                linkType = RSSLink.Type.SELF;

            else if (sType.equals ("alternate"))
                linkType = RSSLink.Type.ALTERNATE;

            else
                sType = null;
        }

        String mimeType = attributes.getValue ("type");
        if (mimeType != null)
        {
            mimeType = mimeType.trim();
            if (mimeType.length() == 0)
                mimeType = null;

            if (mimeType == null)
            {
                if (linkType == RSSLink.Type.SELF)
                    mimeType = ParserUtil.RSS_MIME_TYPE;
            }
        }

        if ((url != null) && (sType != null) && (mimeType != null))
            return new RSSLink (url, mimeType, linkType);
        else
            return null;
    }
}
