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

package org.clapper.curn.parser.minirss;

import org.clapper.curn.parser.ParserUtil;

import java.util.Date;
import java.util.Stack;

import java.text.NumberFormat;

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
    protected Channel channel = null;

    /**
     * Element stack. Contains ElementStackEntry objects.
     */
    protected Stack elementStack = new Stack();

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

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
    public void characters (char[] ch, int start, int length)
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
            boolean           lastWasNewline = false;

            int end = start + length;
            while (start < end)
            {
                char c = ch[start++];

                // Substitute a space for each newline sequence. Be sure to
                // handle all combinations of newline sequences (e.g.,
                // \n, \r, \r\n, \n\r). We want just one space to replace
                // a given newline sequence. The easiest solution is to
                // replace all consecutive newline (\n or \r) characters with
                // one space. This has the effect of nuking blank lines, but
                // blank lines are meaningless within character data anyway,
                // at least in this context.
                if ((c == '\n') || (c == '\r'))
                {
                    if (! lastWasNewline)
                        buf.append (' ');
                    lastWasNewline = true;
                }

                else
                {
                    lastWasNewline = false;
                    if (! Character.isISOControl (c))
                        buf.append (c);
                }
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
        return ParserUtil.parseRFC822Date (sDate);
    }

    /**
     * Parse a W3C date string. Not comprehensive.
     *
     * @param sDate  the date string
     *
     * @return the corresponding date, or null if not parseable
     */
    protected Date parseW3CDate (String sDate)
    {
        return ParserUtil.parseW3CDate (sDate);
    }
}
