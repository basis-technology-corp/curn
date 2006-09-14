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

import org.clapper.curn.parser.ParserUtil;
import org.clapper.curn.parser.RSSLink;

import org.clapper.util.logging.Logger;

import java.util.Date;
import java.util.Collection;

import java.net.MalformedURLException;
import java.net.URL;
import org.clapper.curn.parser.RSSParserException;
import org.dom4j.Element;

/**
 * <p>Common logic and data shared between <tt>V1Parser</tt> and
 * <tt>V2Parser</tt>.
 *
 * @version <tt>$Revision$</tt>
 */
class ParserCommon
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

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * For logging
     */
    private Logger log;                                              // NOPMD

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructor. Saves the <tt>Channel</tt> object and creates a new
     * element stack.
     *
     * @param log  logger to use
     */
    protected ParserCommon(Logger log)
    {
        this.log = log;
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a required element within another element, throwing an exception
     * if not found.
     *
     * @param parentElement  the parent element
     * @param childName      name of the required child element
     *
     * @return the child element
     *
     * @throws RSSParserException on error
     */
    protected Element getRequiredElement(Element parentElement,
                                         String  childName)
        throws RSSParserException
    {
        Element result = parentElement.element(childName);
        if (result == null)
        {
            throw new RSSParserException
                ("<" + parentElement.getName() + "> element is missing " +
                 "required child <" + childName + "> element.");
        }

        return result;
    }

    /**
     * Get the text from an element, trimming it.
     *
     * @param element  the element
     *
     * @return the text, or null if there is none
     */
    protected String getText(Element element)
    {
        String result = element.getText();

        if (result != null)
        {
            result = result.trim();
            if (result.length() == 0)
                result = null;
        }

        return result;
    }

    /**
     * Get an optional element and, if it's there, get its text.
     *
     * @param parentElement  parent element
     * @param childName      child element name
     *
     * @return the trimmed text, or null
     *
     * @throws RSSParserException on error
     */
    protected String getOptionalChildText(Element parentElement, String childName)
        throws RSSParserException
    {
        String result = null;
        Element child = parentElement.element(childName);

        if (child != null)
            result = getText(child);

        return result;
    }
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
