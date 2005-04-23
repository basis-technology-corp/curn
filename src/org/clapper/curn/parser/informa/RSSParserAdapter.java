/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn.parser.informa;

import org.clapper.curn.parser.RSSParser;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSParserException;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.parsers.FeedParser;
import de.nava.informa.impl.basic.ChannelBuilder;

import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

/**
 * This class implements the <tt>RSSParser</tt> interface and defines an
 * adapter for the
 * {@link <a href="http://informa.sourceforge.net/">Informa</a>}
 * RSS Parser. Informa supports the
 * {@link <a href="http://backend.userland.com/rss091">0.91</a>}, 0.92,
 * {@link <a href="http://web.resource.org/rss/1.0/">1.0</a>} and
 * {@link <a href="http://blogs.law.harvard.edu/tech/rss">2.0</a>} RSS formats,
 * but does <i>not</i> support the
 * {@link <a href="http://www.atomenabled.org/developers/">Atom</a>} format.
 *
 * @see org.clapper.curn.parser.RSSParserFactory
 * @see org.clapper.curn.parser.RSSParser
 * @see RSSChannelAdapter
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSParserAdapter implements RSSParser
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor.
     */
    public RSSParserAdapter()
    {
        // Disable Informa logging for now.

        LogFactory logFactory = LogFactory.getFactory();
        logFactory.setAttribute ("org.apache.commons.logging.Log",
                                 "org.apache.commons.logging.impl.NoOpLog");
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse an RSS feed.
     *
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
    public RSSChannel parseRSSFeed (InputStream stream, String encoding)
        throws IOException,
               RSSParserException
    {
        try
        {
            ChannelBuilder builder = new ChannelBuilder();
            ChannelIF      channel;
            Reader         reader;

            if (encoding == null)
                reader = new InputStreamReader (stream);
            else
                reader = new InputStreamReader (stream, encoding);

            channel = FeedParser.parse (builder, reader);

            return new RSSChannelAdapter (channel);
        }

        catch (ParseException ex)
        {
            throw new RSSParserException (ex);
        }
    }
}
