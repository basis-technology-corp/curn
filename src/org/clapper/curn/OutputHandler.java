/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.io.IOException;
import java.io.InputStream;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.config.ConfigurationException;

/**
 * This interface defines the methods that must be supported by a class
 * that is to be plugged into <i>curn</i> as an output handler. It is
 * responsible for writing any channel headers, item headers, and item
 * information. It will only be called with items that should be displayed;
 * any channel items that are cached and should be skipped are not handed
 * to the output handler. <i>curn</i> models output in this manner to make
 * it simpler to substitute different kinds of output handlers.
 *
 * @see Curn
 * @see OutputHandlerFactory
 * @see org.clapper.curn.parser.RSSChannel
 * @see org.clapper.curn.parser.RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public interface OutputHandler
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /**
     * If there's no item summary, use an appropriate content field, as
     * long as it's no larger than this many characters.
     */
    public static final int CONTENT_AS_SUMMARY_MAXSIZE = 1000;

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param config  the parsed <i>curn</i> configuration data. The
     *                output handler is responsible for retrieving its
     *                own parameters from the configuration, by calling
     *                <tt>config.getOutputHandlerSpecificVariables()</tt>
     *
     * @throws ConfigurationException configuration error
     * @throws CurnException          some other initialization error
     *
     * @see ConfigFile#getOutputHandlerSpecificVariables(Class)
     * @see ConfigFile#getOutputHandlerSpecificVariables(String)
     */
    public void init (ConfigFile config)
        throws ConfigurationException,
               CurnException;

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. Output should be written to the
     * <tt>PrintWriter</tt> that was passed to the {@link #init init()} method.
     *
     * @param channel  The channel containing the items to emit. The method
     *                 should emit all the items in the channel; the caller
     *                 is responsible for clearing out any items that should
     *                 not be seen.
     * @param feedInfo Information about the feed, from the configuration
     *
     * @throws CurnException  unable to write output
     */
    public void displayChannel (RSSChannel  channel,
                                FeedInfo    feedInfo)
        throws CurnException;

    /**
     * Flush any buffered-up output, but do NOT close the underlying output
     * stream(s). <i>curn</i> calls this method once, after calling
     * <tt>displayChannelItems()</tt> for all channels. If the output
     * handler doesn't need to flush any output, it can simply return
     * without doing anything.
     *
     * @throws CurnException  unable to write output
     */
    public void flush()
        throws CurnException;

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     *
     * @see #hasGeneratedOutput
     * @see #getGeneratedOutput
     */
    public String getContentType();

    /**
     * Get an <tt>InputStream</tt> that can be used to read the output data
     * produced by the handler, if applicable.
     *
     * @return an open input stream, or null if no suitable output was produced
     *
     * @throws CurnException an error occurred
     *
     * @see #hasGeneratedOutput
     * @see #getContentType
     */
    public InputStream getGeneratedOutput()
        throws CurnException;

    /**
     * Determine whether this handler has produced any actual output (i.e.,
     * whether {@link #getGeneratedOutput()} will return a non-null
     * <tt>InputStream</tt> if called).
     *
     * @return <tt>true</tt> if the handler has produced output,
     *         <tt>false</tt> if not
     *
     * @see #getGeneratedOutput
     * @see #getContentType
     */
    public boolean hasGeneratedOutput();
}
