/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.io.IOException;

import org.clapper.rssget.parser.RSSChannel;
import org.clapper.rssget.parser.RSSItem;

/**
 * This interface defines the methods that must be supported by a class
 * that is to be plugged into <i>rssget</i> as an output handler. It is
 * responsible for writing any channel headers, item headers, and item
 * information. It will only be called with items that should be displayed;
 * any channel items that are cached and should be skipped are not handed
 * to the output handler. <i>rssget</i> models output in this manner to make
 * it simpler to substitute different kinds of output handlers.
 *
 * @see rssget
 * @see RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public interface RSSGetOutputHandler
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class.
     *
     * @param items   array of <tt>RSSItem</tt> objects to be output, or
     *                null if there are no items for this channel. (The
     *                method might still want to emit a channel header with
     *                a "no new items" message.)
     * @param channel the items' parent <tt>RSSChannel</tt>
     * @param config  the active <tt>RSSGetConfiguration</tt> object
     *
     * @throws IOException  unable to write output
     */
    public void displayChannelItems (RSSItem[]           items,
                                     RSSChannel          channel,
                                     RSSGetConfiguration config)
        throws IOException;
}
