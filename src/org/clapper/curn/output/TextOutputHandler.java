/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.io.WordWrapWriter;
import org.clapper.util.text.TextUtils;
import org.clapper.util.text.Unicode;
import org.clapper.util.misc.Logger;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.config.NoSuchSectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Provides an output handler that writes the RSS channel and item summaries
 * as plain text. This handler supports the additional configuration items
 * that its parent {@link FileOutputHandler} class supports. It has no
 * class-specific configuration items of its own. It produces output only
 * if the channels contain
 *
 * @see OutputHandler
 * @see FileOutputHandler
 * @see curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class TextOutputHandler extends FileOutputHandler
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final String HORIZONTAL_RULE =
                      "---------------------------------------"
                    + "---------------------------------------";

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private WordWrapWriter  out         = null;
    private int             indentLevel = 0;
    private ConfigFile      config      = null;
    private StringBuffer    scratch     = new StringBuffer();

    /**
     * For logging
     */
    private static Logger log = new Logger (TextOutputHandler.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>TextOutputHandler</tt>
     */
    public TextOutputHandler()
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
        Collection items = channel.getItems();

        indentLevel = setIndent (0);

        if ((items.size() != 0) || (! config.beQuiet()))
        {
            // Emit a site (channel) header.

            out.println();
            out.println (HORIZONTAL_RULE);

            out.println (convert (channel.getTitle()));
            out.println (channel.getLink().toString());

            if (config.showDates())
            {
                Date date = channel.getPublicationDate();
                if (date != null)
                    out.println (date.toString());
            }

            if (config.showRSSVersion())
                out.println ("(Format: " + channel.getRSSFormat() + ")");
        }

        if (items.size() != 0)
        {
            // Now, process each item.

            String s;

            for (Iterator it = items.iterator(); it.hasNext(); )
            {
                RSSItem item = (RSSItem) it.next();

                setIndent (++indentLevel);

                out.println ();

                s = item.getTitle();
                out.println ((s == null) ? "(No Title)" : convert (s));

                if (config.showAuthors())
                {
                    s = item.getAuthor();
                    if (s != null)
                        out.println ("By " + convert (s));
                }

                out.println (item.getLink().toString());

                if (config.showDates())
                {
                    Date date = item.getPublicationDate();
                    if (date != null)
                        out.println (date.toString());
                }

                if (! feedInfo.summarizeOnly())
                {
                    s = item.getSummary();
                    if (TextUtils.stringIsEmpty (s))
                    {
                        // Hack for feeds that have no summary but have
                        // content. If the content is small enough, use it
                        // as the summary.

                        s = item.getFirstContentOfType (new String[]
                                                        {
                                                            "text/plain",
                                                            "text/html"
                                                        });
                        if (! TextUtils.stringIsEmpty (s))
                        {
                            s = s.trim();
                            if (s.length() > CONTENT_AS_SUMMARY_MAXSIZE)
                                s = null;
                        }
                    }

                    else
                    {
                        s = s.trim();
                    }

                    if (s != null)
                    {
                        out.println();
                        setIndent (++indentLevel);
                        out.println (convert (s));
                        setIndent (--indentLevel);
                    }
                }

                setIndent (--indentLevel);
            }
        }

        else
        {
            if (! config.beQuiet())
            {
                setIndent (++indentLevel);
                out.println ();
                out.println ("No new items");
                setIndent (--indentLevel);
            }
        }

        setIndent (0);
    }

    /**
     * Flush any buffered-up output.
     *
     * @throws CurnException  unable to write output
     */
    public void flush() throws CurnException
    {
        out.println ();
        out.println (HORIZONTAL_RULE);
        out.println ("curn, version " + Version.VERSION);
        out.println ("Generated " + new Date().toString());
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

    private int setIndent (int level)
    {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < level; i++)
            buf.append ("    ");

        out.setPrefix (buf.toString());

        return level;
    }
}
