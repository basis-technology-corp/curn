/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn.parser.minirss;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

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
                               Inner Classes
    \*----------------------------------------------------------------------*/

    class Author
    {
        Channel parentChannel = null;

        Author (Channel parentChannel)
        {
            this.parentChannel = parentChannel;
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
     * @param firstElement the first element in the file, which was already
     *                     parsed by a <tt>MiniRSSParser</tt> object, before
     *                     it handed control to this object
     */
    AtomParser (Channel channel, String elementName)
        throws SAXException
    {
        super (channel);
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
        ElementStackEntry entry = (ElementStackEntry) elementStack.peek();
        Object container = entry.getContainer();

        if (elementName.equals ("feed"))
            startChannel (elementName, attributes);

        else if (container instanceof Channel)
            startChannelSubelement (elementName, attributes, entry);

        else if (container instanceof Item)
            startItemSubelement (elementName, attributes, entry);

        else if (container instanceof Author)
            startAuthorSubelement (elementName, attributes, entry);
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
            throw new SAXException ("Element \""
                                  + elementName
                                  + "\" doesn't match element on stack (\""
                                  + entry.getElementName()
                                  + "\")");
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
        Channel channel = (Channel) parentStackEntry.getContainer();

        if (elementName.equals ("entry"))
        {
            Item item = new Item (channel);

            channel.addItem (item);
            elementStack.push (new ElementStackEntry (elementName, item));
        }

        else if (elementName.equals ("author"))
        {
            Author author = new Author (channel);

            elementStack.push (new ElementStackEntry (elementName, author));
        }


        else if (elementName.equals ("link"))
        {
            try
            {
                channel.setLink (new URL (attributes.getValue ("href")));
                elementStack.push (new ElementStackEntry (elementName,
                                                          channel));
            }

            catch (MalformedURLException ex)
            {
                throw new SAXException (ex.toString());
            }
        }

        else
        {
            elementStack.push (new ElementStackEntry (elementName, channel));
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
        Channel channel = (Channel) stackEntry.getContainer();
        String  chars   = stackEntry.getCharBuffer().toString().trim();

        if (chars.length() == 0)
            chars = null;

        if (elementName.equals ("title"))
            channel.setTitle (chars);

        else if (elementName.equals ("issued"))
            channel.setPublicationDate (parseW3CDate (chars));

        else if (elementName.equals ("modified"))
            channel.setPublicationDate (parseW3CDate (chars));

        else if (elementName.equals ("created"))
            channel.setPublicationDate (parseW3CDate (chars));

        else if (elementName.equals ("id"))
            channel.setUniqueID (chars);
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
        String  chars   = stackEntry.getCharBuffer().toString().trim();

        if (chars.length() == 0)
            chars = null;

        if (elementName.equals ("name"))
            author.parentChannel.setAuthor (chars);
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
            try
            {
                item.setLink (new URL (attributes.getValue ("href")));
                elementStack.push (new ElementStackEntry (elementName,
                                                          channel));
            }

            catch (MalformedURLException ex)
            {
                throw new SAXException (ex.toString());
            }
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
        String  chars   = stackEntry.getCharBuffer().toString().trim();

        if (chars.length() == 0)
            chars = null;

        if (elementName.equals ("title"))
            item.setTitle (chars);

        else if (elementName.equals ("summary"))
            item.setDescription (chars);

        else if (elementName.equals ("issued"))
            item.setPublicationDate (parseW3CDate (chars));

        else if (elementName.equals ("id"))
            item.setUniqueID (chars);
    }
}
