/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget.informa;

import org.clapper.rssget.*;
import de.nava.informa.core.*;
import de.nava.informa.impl.basic.ChannelBuilder;
import org.apache.commons.logging.*;
import java.net.*;
import java.io.*;

/**
 * This class implements the <tt>RSSParser</tt> interface and defines an
 * adapter for the {@link <a href="http://informa.sourceforge.net/" Informa}
 * RSS Parser.
 *
 * @see RSSParserFactory
 * @see RSSParser
 * @see RSSChannelAdapter
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSParserAdapter implements org.clapper.rssget.RSSParser
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
     * @param url  The URL for the feed
     *
     * @return an <tt>RSSChannel</tt> object representing the RSS data from
     *         the site.
     *
     * @throws IOException        unable to read from URL
     * @throws RSSParserException unable to parse RSS XML
     */
    public RSSChannel parseRSSFeed (URL url)
        throws IOException,
               RSSParserException
    {
        try
        {
            return new RSSChannelAdapter (de.nava.informa.parsers.RSSParser.parse
                                              (new ChannelBuilder(), url));
        }

        catch (ParseException ex)
        {
            throw new RSSParserException (ex);
        }
    }
}
