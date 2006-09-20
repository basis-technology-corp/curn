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

package org.clapper.curn.parser.rome;

import org.clapper.curn.parser.RSSParser;
import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSParserException;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.FeedException;

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;

/**
 * This class implements the <tt>RSSParser</tt> interface and defines an
 * adapter for the {@link <a href="https://rome.dev.java.net/">Rome</a>}
 * RSS Parser. Rome supports the
 * {@link <a href="http://backend.userland.com/rss091">0.91</a>}, 0.92,
 * {@link <a href="http://web.resource.org/rss/1.0/">1.0</a>},
 * {@link <a href="http://blogs.law.harvard.edu/tech/rss">2.0</a>}
 * and
 * {@link <a href="http://www.atomenabled.org/developers/">Atom</a>}
 * RSS formats.
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
        // Nothing to do
    }

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
    public RSSChannel parseRSSFeed(URL         url,
                                   InputStream stream,
                                   String      encoding)
        throws IOException,
               RSSParserException
    {
        try
        {
            InputStreamReader r;

            if (encoding == null)
                r = new InputStreamReader(stream);
            else
                r = new InputStreamReader(stream, encoding);

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed      feed  = input.build(new InputSource(r));
            feed.setUri(url.toString());

            return new RSSChannelAdapter(feed);
        }

        catch (FeedException ex)
        {
            throw new RSSParserException(ex);
        }
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/
}
