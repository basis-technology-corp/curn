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

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSParser;
import org.clapper.curn.parser.RSSParserException;

import org.clapper.util.logging.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p><tt>MiniRSSParser</tt> is a stripped down RSS parser. It handles
 * files in
 * {@link <a target="_top" href="http://www.atomenabled.org/developers/">Atom</a>}
 * format (0.3) and RSS formats
 * {@link <a target="_top" href="http://backend.userland.com/rss091">0.91</a>},
 * 0.92,
 * {@link <a target="_top" href="http://web.resource.org/rss/1.0/">1.0</a>} and
 * {@link <a target="_top" href="http://blogs.law.harvard.edu/tech/rss">2.0</a>}.
 * However, it doesn't store all the possible RSS items. It stores those
 * items that the <i>curn</i> utility requires (plus a few more), but
 * lacks support for others.  Thus, it is unsuitable for use as a
 * general-purpose RSS parser (though it's perfectly suited for use
 * in <i>curn</i>).</p>
 *
 * <p><b>Notes:</b>
 *
 * <ol>
 *    <li> This API relies on the SAX 2 (org.xml.sax.*) package of XML parser
 *         classes; you must have those classes in your CLASSPATH to use this
 *         API.
 *
 *    <li> If a specific XML parser class is not specified to the constructor,
 *         this class defaults to using the Apache Xerces XML parser class.
 * </ol>
 *
 * <b>Warning:</b> This class is NOT thread safe. Because of the nature of
 * XML SAX event-driven (i.e., callback-driven) parsing, an instance of this
 * object must maintain parser state as instance data.
 *
 * @version <tt>$Revision$</tt>
 */
public class MiniRSSParser
    extends DefaultHandler
    implements RSSParser, ErrorHandler
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private V2Parser v2Parser     = new V2Parser();                 // NOPMD
    private V1Parser v1Parser     = new V1Parser();                 // NOPMD
    private AtomParser atomParser = new AtomParser();               // NOPMD

    /**
     * For logging
     */
    private static final Logger log = new Logger (MiniRSSParser.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>MiniRSSParser</tt> object that uses the default
     * Apache Xerces SAX XML parser.
     */
    public MiniRSSParser()
    {
    }


    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse an RSS feed. <b>Warning:</b> This method is NOT thread safe.
     * Because of the nature of XML SAX event-driven (i.e.,
     * callback-driven) parsing, an instance of this object must maintain
     * parser state as instance data.
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
     *
     * @see Channel
     * @see RSSChannel
     */
    public final RSSChannel parseRSSFeed(URL         url,
                                         InputStream stream,
                                         String      encoding)
        throws IOException,
               RSSParserException
    {
        Reader r;

        if (encoding == null)
            r = new InputStreamReader (stream);
        else
            r = new InputStreamReader (stream, encoding);

        return parse(url, r);
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parses an RSS feed from an already-open <tt>Reader</tt> object.
     *
     * @param url  The URL for the feed
     * @param r    The <tt>Reader</tt> that will produce the RSS XML
     *
     * @return the <tt>Channel</tt> object containing the parsed RSS data
     *
     * @throws IOException        error opening or reading from the URL
     * @throws RSSParserException error parsing the XML
     *
     * @see #parseRSSFeed(InputStream,String)
     * @see Channel
     * @see RSSChannel
     */
    private RSSChannel parse(URL url, Reader r)
        throws IOException,
               RSSParserException
    {
        Channel channel = null;

        try
        {
            SAXReader reader = new SAXReader();
            Document document = reader.read(r);

            // Allocate a new Channel.

            channel = new Channel();

            // Now, figure out what kind of document we have.

            Element rootElement = document.getRootElement();
            String rootName = rootElement.getName();

            if (rootName.equals("RDF") || rootName.equals("rdf:RDF"))
            {
                channel.setRSSFormat("RSS 1.0");
                v1Parser.process(channel, url, document);
            }

            else if (rootName.equals("rss"))
            {
                String version = rootElement.attributeValue("version");
                channel.setRSSFormat("RSS " + version);

                // For curn's purposes, there's considerable similarity
                // between RSS version 0.91 and RSS version 2--so much so
                // that the same parser logic will work for both.

                if (version.startsWith("0.9") || version.startsWith("2."))
                {
                    v2Parser.process(channel, url, document);
                }

                else
                {
                    throw new RSSParserException
                        ("Unknown RSS version: " + version);
                }
            }

            else if (rootName.equals("feed"))
            {
                // Atom.

                String version = rootElement.attributeValue("version");
                if (version == null)
                    channel.setRSSFormat("Atom");
                else
                    channel.setRSSFormat("Atom " + version);

                atomParser.process(channel, url, document);
            }

            else
            {
                throw new RSSParserException("Unknown or unsupported RSS " +
                                             "type. First XML element is <" +
                                             rootName + ">");
            }
        }

        catch (DocumentException ex)
        {
            log.error ("XML parse error", ex);
            throw new RSSParserException (ex);
        }

        return channel;
    }
}
