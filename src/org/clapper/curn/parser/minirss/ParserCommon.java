/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget.parser.minirss;

import java.util.Date;
import java.util.Stack;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>Common logic and data shared between <tt>V1Parser</tt> and
 * <tt>V2Parser</tt>.
 *
 * @version <tt>$Revision$</tt>
 */
class ParserCommon extends DefaultHandler
{
    /*----------------------------------------------------------------------*\
			     Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                          Protected Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The channel being filled.
     */
    protected Channel channel        = null;

    /**
     * Element stack. Contains ElementStackEntry objects.
     */
    protected Stack   elementStack   = new Stack();

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * A date format for parsing RFC 822-style dates.
     */
    private DateFormat rfc822DateFormat = new SimpleDateFormat
                                                ("EEE, d MMM yyyy hh:mm:ss");

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructor. Saves the <tt>Channel</tt> object and creates a new
     * element stack.
     */
    protected ParserCommon (Channel channel)
    {
        this.channel = channel;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
                        Overriding XMLReaderAdapter
    \*----------------------------------------------------------------------*/

    /**
     * Handle character data parsed from the XML file.
     *
     * @param ch      characters from the XML document
     * @param start   the start position in the array
     * @param length  the number of characters to read from the array
     *
     * @throws SAXException parsing error
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException
    {
        if (! elementStack.empty())
        {
            // Get the top-most entry on the stack, and put the characters
            // in that entry's character buffer. This strategy allows this
            // method to work even if it's called multiple times for the
            // same XML element--which is permitted by the SAX parser
            // interface.

            ElementStackEntry entry = (ElementStackEntry) elementStack.peek();
            StringBuffer      buf   = entry.getCharBuffer();

            int end = start + length;
            while (start < end)
            {
                char c = ch[start++];
                if (! Character.isISOControl (c))
                    buf.append (c);
            }
        }
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse an RFC 822-style date string.
     *
     * @param sDate  the date string
     *
     * @return the corresponding date, or null if not parseable
     */
    protected Date parseRFC822Date (String sDate)
    {
        try
        {
            return rfc822DateFormat.parse (sDate);
        }

        catch (ParseException ex)
        {
            return null;
        }
    }
}

