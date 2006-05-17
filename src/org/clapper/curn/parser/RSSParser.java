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

package org.clapper.curn.parser;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

/**
 * This interface defines a simplified view of an RSS parser, providing
 * only the methods necessary for <i>curn</i> to work. <i>curn</i> uses
 * the {@link RSSParserFactory} class to get a specific implementation of
 * an <tt>RSSParser</tt>. This strategy isolates the bulk of the code from
 * the underlying RSS parser, making it easier to substitute different
 * parsers as more of them become available.
 *
 * @see RSSParserFactory
 * @see RSSChannel
 * @see RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public interface RSSParser
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse an RSS feed.
     *
     * @param url      the URL for the feed
     * @param stream   the <tt>InputStream</tt> for the feed
     * @param encoding the encoding of the data in the field, if known, or
     *                 null
     *
     * @return an <tt>RSSChannel</tt> object representing the RSS data from
     *         the site.
     *
     * @throws IOException        unable to read from URL
     * @throws RSSParserException unable to parse RSS XML
     */
    public RSSChannel parseRSSFeed (URL         url,
                                    InputStream stream,
                                    String      encoding)
        throws IOException,
               RSSParserException;
}
