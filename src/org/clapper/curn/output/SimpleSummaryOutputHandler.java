/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.io.WordWrapWriter;
import org.clapper.util.misc.Logger;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.config.NoSuchSectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Provides an output handler that produces plain text to a file.
 *
 * @see OutputHandler
 * @see curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class SimpleSummaryOutputHandler extends FileOutputHandler
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private WordWrapWriter  out              = null;
    private ConfigFile      config           = null;
    private String          message          = null;
    private Collection      channels         = new ArrayList();
    private int             totalItems       = 0;

    /**
     * For logging
     */
    private static Logger log = new Logger (SimpleSummaryOutputHandler.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>SimpleSummaryOutputHandler</tt>
     */
    public SimpleSummaryOutputHandler()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param config       the parsed <i>curn</i> configuration data
     * @param sectionName  the config file section name for the handler
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other initialization error
     */
    public void initOutputHandler (ConfigFile config, String sectionName)
        throws ConfigurationException,
               CurnException
    {
        this.config = config;

        if (sectionName != null)
        {
            message = config.getOptionalStringValue (sectionName,
                                                     "Message",
                                                     null);
        }

        File outputFile = super.getOutputFile();
        try
        {
            log.debug ("Opening output file \"" + outputFile + "\"");
            out = new WordWrapWriter (new FileWriter (outputFile));
        }

        catch (IOException ex)
        {
            throw new CurnException ("Can't open file \""
                                   + outputFile.getPath()
                                   + "\" for output",
                                     ex);
        }

        channels.clear();
        totalItems = 0;
    }

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. Output is written to the
     * <tt>PrintWriter</tt> that was passed to the {@link #init init()}
     * method.
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
        throws CurnException
    {
        Collection items        = channel.getItems();
        int        channelItems = items.size();

        if (channelItems != 0)
        {
            totalItems += channelItems;
            channels.add (channel);
        }
    }

    /**
     * Flush any buffered-up output.
     *
     * @throws CurnException  unable to write output
     */
    public void flush() throws CurnException
    {
        if (totalItems > 0)
        {
            out.println ("Some or all configured feeds have new items.");
            out.println ("Total new items for all channels: "
                       + String.valueOf (totalItems));
            out.println ();
            if (message != null)
            {
                out.println (message);
                out.println ();
            }

            out.println ("A summary follows.");
            out.println ();

            for (Iterator it = channels.iterator(); it.hasNext(); )
            {
                RSSChannel channel = (RSSChannel) it.next();

                out.setPrefix ("    ");
                out.println (convert (channel.getTitle()));
                out.println (channel.getLink().toString());

                out.setPrefix ("        ");
                out.println (String.valueOf (channel.getItems().size())
                           + " items");
                out.println ();
            }
        }

        out.flush();
        out = null;
    }
    
    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return "text/plain";
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/
}
