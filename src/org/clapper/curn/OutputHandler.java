/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import java.io.IOException;
import java.io.PrintWriter;

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
 * @see OutputHandlerFactory
 * @see org.clapper.rssget.parser.RSSChannel
 * @see org.clapper.rssget.parser.RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public interface OutputHandler
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param writer  the <tt>PrintWriter</tt> where the handler should send
     *                output, if applicable.
     * @param config  the parsed <i>rssget</i> configuration data
     *
     * @throws FeedException  initialization error
     */
    public void init (PrintWriter         writer,
                      ConfigFile config)
        throws FeedException;

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. Output should be written to the
     * <tt>PrintWriter</tt> that was passed to the {@ #init init()} method.
     *
     * @param channel  The channel containing the items to emit. The method
     *                 should emit all the items in the channel; the caller
     *                 is responsible for clearing out any items that should
     *                 not be seen.
     * @param feedInfo Information about the feed, from the configuration
     *
     * @throws FeedException  unable to write output
     */
    public void displayChannel (RSSChannel  channel,
                                FeedInfo    feedInfo)
        throws FeedException;

    /**
     * Flush any buffered-up output. <i>rssget</i> calls this method
     * once, after calling <tt>displayChannelItems()</tt> for all channels.
     * If the output handler doesn't need to flush any output, it can simply
     * return without doing anything.
     *
     * @throws FeedException  unable to write output
     */
    public void flush()
        throws FeedException;

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType();
}
