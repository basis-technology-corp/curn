/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.io.WordWrapWriter;
import org.clapper.util.text.TextUtils;
import org.clapper.util.text.Unicode;

import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.Date;
import java.util.Collection;
import java.util.Iterator;

/**
 * Provides an output handler that produces plain text to an
 * <tt>OutputStreamWriter</tt>.
 *
 * @see OutputHandler
 * @see curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class TextOutputHandler implements OutputHandler
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private WordWrapWriter  out         = null;
    private int             indentLevel = 0;
    private ConfigFile      config      = null;
    private StringBuffer    scratch     = new StringBuffer();
       
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
     * @param writer  the <tt>OutputStreamWWriter</tt> where the handler
     *                should send output
     * @param config  the parsed <i>curn</i> configuration data
     *
     * @throws FeedException  initialization error
     */
    public void init (OutputStreamWriter writer, ConfigFile config)
        throws FeedException
    {
        this.out    = new WordWrapWriter (writer, 79);
        this.config = config;
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
     * @throws FeedException  unable to write output
     */
    public void displayChannel (RSSChannel  channel,
                                FeedInfo    feedInfo)
        throws FeedException
    {
        Collection items = channel.getItems();

        indentLevel = setIndent (0);

        if ((items.size() != 0) || (! config.beQuiet()))
        {
            // Emit a site (channel) header.

            out.println();
            out.println ("---------------------------------------" +
                         "---------------------------------------");

            out.println (convert (channel.getTitle()));
            out.println (channel.getLink().toString());

            if (config.showDates())
            {
                Date date = channel.getPublicationDate();
                if (date != null)
                    out.println (date);
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
                        out.println (date);
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
     * Flush any buffered-up output. <i>curn</i> calls this method
     * once, after calling <tt>displayChannelItems()</tt> for all channels.
     *
     * @throws FeedException  unable to write output
     */
    public void flush() throws FeedException
    {
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

    private String convert (String s)
    {
        char[] ch = s.toCharArray();

        scratch.setLength (0);
        for (int i = 0; i < ch.length; i++)
        {
            switch (ch[i])
            {
                case Unicode.LEFT_SINGLE_QUOTE:
                case Unicode.RIGHT_SINGLE_QUOTE:
                    scratch.append ('\'');
                    break;

                case Unicode.LEFT_DOUBLE_QUOTE:
                case Unicode.RIGHT_DOUBLE_QUOTE:
                    scratch.append ('"');
                    break;

                case Unicode.EM_DASH:
                    scratch.append ("--");
                    break;

                case Unicode.EN_DASH:
                    scratch.append ('-');
                    break;

                case Unicode.TRADEMARK:
                    scratch.append ("[TM]");
                    break;

                default:
                    scratch.append (ch[i]);
                    break;
            }
        }

        return scratch.toString();
    }
}
