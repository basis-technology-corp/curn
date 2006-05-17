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
import org.clapper.curn.parser.RSSLink;

import org.clapper.util.logging.Logger;

import java.util.Date;
import java.util.Stack;
import java.util.Collection;

import java.net.MalformedURLException;
import java.net.URL;

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
     * The URL for the feed;
     */
    protected URL url = null;

    /**
     * Element stack. Contains ElementStackEntry objects.
     */
    protected Stack<ElementStackEntry> elementStack =
          new Stack<ElementStackEntry>();

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * For logging
     */
    private Logger log;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructor. Saves the <tt>Channel</tt> object and creates a new
     * element stack.
     *
     * @param channel  the {@link Channel} object
     * @param url      the URL for the feed
     * @param log      logger to use
     */
    protected ParserCommon (Channel channel, URL url, Logger log)
    {
        this.channel = channel;
        this.url     = url;
        this.log     = log;
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

            ElementStackEntry entry = elementStack.peek();
            StringBuffer      buf   = entry.getCharBuffer();
            ParserUtil.normalizeCharacterData (ch, start, length, buf);
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

    /**
     * Resolve a link. Handles relative and absolute links.
     *
     * @param sLink    the link string
     * @param channel  the parent {@link Channel}
     *
     * @return the URL, if parseable
     *
     * @throws MalformedURLException bad URL
     */
    protected URL resolveLink (String sLink, Channel  channel)
        throws MalformedURLException
    {
        Collection<RSSLink> channelLinks = channel.getLinks();
        URL                 parentURL    = null; 

        if (channelLinks.size() > 0)
        {
            for (RSSLink link : channelLinks)
            {
                // Prefer a SELF link, if there is one.

                parentURL = link.getURL();
                if (link.getLinkType() == RSSLink.Type.SELF)
                    break;
            }

            assert (parentURL != null);
        }

        URL result;
        if (parentURL == null)
        {
            log.debug ("No parent URL for \"" + sLink + "\"");
            result = new URL (sLink);
        }

        else
        {

            result = new URL (parentURL, sLink);
        }

        return result;
    }
}
