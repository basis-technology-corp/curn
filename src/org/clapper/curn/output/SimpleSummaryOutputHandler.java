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
 * <p>Provides an output handler that produces a summary of the number of new
 * items in a configuration file, without actually displaying the items
 * or their URLs. In addition, the summary contains a user-defined string
 * from the configuration file. The user-defined string is configured via
 * the <tt>Message</tt> configuration item. For example:</p>
 *
 * <blockquote><pre>
 * [OutputHandlerSummary]
 * Class: org.clapper.curn.SimpleSummaryOutputHandler
 * Message: Full details at http://localhost/rss/feeds.html
 * </pre></blockquote>
 *
 * <p>This handler is particularly useful in conjunction with a true output
 * handler. For instance, my mail reader does not support HTML (and I like
 * it that way); however, I prefer the output produced by the
 * {@link org.clapper.curn.htmloutput.HTMLOutputHandler HTMLOutputHandler}
 * class to the output produced by the
 * {@link TextOutputHandler TextOutputHandler}
 * class. So, my configuration file contains the following output handlers:</p>
 *
 * <blockquote><pre>
 * [OutputHandlerSummary]
 * Class: org.clapper.curn.SimpleSummaryOutputHandler
 * Message: Full details at http://localhost/rss/feeds.html
 *
 * [OutputHandlerHTML]
 * Class: org.clapper.curn.htmloutput.HTMLOutputHandler
 * SaveAs: /usr/local/www/htdocs/rss/feeds.html
 * SaveOnly: true
 * </pre></blockquote>
 *
 * <p>When <i>curn</i> runs, it parses the RSS feeds and passes the resulting
 * {@link org.clapper.curn.parser.RSSChannel RSSChannel} objects to each
 * output handler. Because its <tt>SaveOnly</tt> configuration parameter is
 * set, the <tt>HTMLOutputHandler</tt> object does not return any output to
 * <i>curn</i>; instead, it simply saves the HTML output to the specified file.
 * The <tt>SimpleSummaryOutputHandler</tt> object returns a brief summary
 * of the feeds, along with the message:</p>
 *
 * <blockquote>
 * <pre>Full details at http://localhost/rss/feeds.html</pre>
 * </blockquote>
 *
 * <p>When I look at <i>curn</i>'s output in my mail reader, I can click on
 * the link (my mail reader <i>will</i> highlight HTML links, even though it
 * won't render full HTML), and view the resulting HTML document in my
 * browser.</p>
 *
 * @see OutputHandler
 * @see FileOutputHandler
 * @see Curn
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
