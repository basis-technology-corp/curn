/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget.parser.informa;

import org.clapper.rssget.parser.RSSChannel;
import org.clapper.rssget.parser.RSSItem;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.RSSParser;

import java.net.URL;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class implements the <tt>RSSChannel</tt> interface and defines an
 * adapter for the <a href="http://informa.sourceforge.net/">Informa</a>
 * RSS Parser's <tt>ChannelIF</tt> type.
 *
 * @see org.clapper.rssget.parser.RSSParserFactory
 * @see org.clapper.rssget.parser.RSSParser
 * @see org.clapper.rssget.parser.RSSChannel
 * @see org.clapper.rssget.parser.RSSItem
 * @see RSSItemAdapter
 *
 * @version <tt>$Revision$</tt>
 */
public class RSSChannelAdapter implements RSSChannel
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The real channel object
     */
    private ChannelIF channel;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>RSSChannelAdapter</tt> object that wraps the
     * specified Informa <tt>ChannelIF</tt> object.
     *
     * @param channelIF  the <tt>ChannelIF</tt> object
     */
    RSSChannelAdapter (ChannelIF channelIF)
    {
        this.channel = channelIF;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a <tt>Collection</tt> of the items in this channel. All objects
     * in the collection are of type <tt>RSSItem</tt>.
     *
     * @return a (new) <tt>Collection</tt> of <tt>RSSItem</tt> objects.
     *         The collection will be empty (never null) if there are
     *         no items.
     */
    public Collection getItems()
    {
        Collection result = new ArrayList();
        Collection items  = this.channel.getItems();

        for (Iterator it = items.iterator(); it.hasNext(); )
            result.add (new RSSItemAdapter ((ItemIF) it.next()));

        return result;
    }

    /**
     * Get the channel's title
     *
     * @return the channel's title, or null if there isn't one
     */
    public String getTitle()
    {
        return this.channel.getTitle();
    }

    /**
     * Get the channel's description
     *
     * @return the channel's description, or null if there isn't one
     */
    public String getDescription()
    {
        return this.channel.getDescription();
    }

    /**
     * Get the channel's published link (its URL).
     *
     * @return the URL, or null if not available
     */
    public URL getLink()
    {
        return this.channel.getSite();
    }

    /**
     * Get the channel's publication date.
     *
     * @return the date, or null if not available
     */
    public Date getPublicationDate()
    {
        return this.channel.getPubDate();
    }

    /**
     * Get the channel's copyright string
     *
     * @return the copyright string, or null if not available
     */
    public String getCopyright()
    {
        return this.channel.getCopyright();
    }

    /**
     * Get the RSS format the channel is using.
     *
     * @return the format, or null if not available
     */
    public String getRSSFormat()
    {
        return this.channel.getFormat().toString();
    }
}
