/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.io.WordWrapWriter;
import org.clapper.util.text.TextUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.Date;
import java.util.Collection;
import java.util.Iterator;

/**
 * Provides an output handler that doesn't actually write anything.
 * This handler is useful when you're using curn to download feeds, but
 * not to display them.
 *
 * @see OutputHandler
 * @see TextOutputHandler
 * @see curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class NullOutputHandler implements OutputHandler
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private ConfigFile config = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>NullOutputHandler</tt>
     */
    public NullOutputHandler()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param writer  the <tt>OutputStreamWWriter</tt> where the handler
     *                should send output
     * @param config  the parsed <i>curn</i> configuration data
     *
     * @throws FeedException  initialization error
     */
    public void init (OutputStreamWriter writer, ConfigFile config)
        throws FeedException
    {
    }

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. In this class, the
     * <tt>displayChannel()</tt> method does nothing.
     *
     * @param channel  The channel containing the items to emit.
     * @param feedInfo Information about the feed, from the configuration
     *
     * @throws FeedException  unable to write output
     */
    public void displayChannel (RSSChannel  channel,
                                FeedInfo    feedInfo)
        throws FeedException
    {
    }

    /**
     * Flush any buffered-up output. <i>curn</i> calls this method
     * once, after calling <tt>displayChannelItems()</tt> for all channels.
     *
     * @throws FeedException  unable to write output
     */
    public void flush() throws FeedException
    {
    }
    
    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return "text/null";
    }
}
