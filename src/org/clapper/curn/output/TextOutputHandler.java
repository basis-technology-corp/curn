/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.rssget;

import org.clapper.rssget.parser.RSSChannel;
import org.clapper.rssget.parser.RSSItem;

import org.clapper.util.io.WordWrapWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;

import java.util.Date;

/**
 * Provides an output handler that produces plain text to a
 * <tt>PrintWriter</tt>.
 *
 * @see RSSGetOutputHandler
 * @see rssget
 * @see RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public class TextOutputHandler implements RSSGetOutputHandler
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private WordWrapWriter  out;
    private int             indentLevel = 0;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a <tt>TextOutputHandler</tt> that writes its output to the
     * specified <tt>PrintWriter</tt>.
     *
     * @param writer  the <tt>PrintWriter</tt>
     */
    TextOutputHandler (PrintWriter writer)
    {
        out = new WordWrapWriter (writer, 79);
    }

    /**
     * Construct a <tt>TextOutputHandler</tt> that writes its output to the
     * specified <tt>PrintStream</tt>.
     *
     * @param os  the <tt>PrintStream</tt>
     */
    TextOutputHandler (PrintStream os)
    {
        this (new PrintWriter (os));
    }

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
        throws IOException
    {
        indentLevel = setIndent (0);

        if ((items != null) || (! config.beQuiet()))
        {
            // Emit a site (channel) header.

            out.println();
            out.println ("---------------------------------------" +
                         "---------------------------------------");

            out.println (channel.getTitle());
            out.println (channel.getLink().toString());

            Date date = channel.getPublicationDate();
            if (date != null)
                out.println (date);
            out.println ("(Format: " + channel.getRSSFormat() + ")");
        }

        if (items != null)
        {
            // Now, process each item.

            for (int i = 0; i < items.length; i++)
            {
                RSSItem item = items[i];

                setIndent (++indentLevel);

                out.println ();
                out.println (item.getTitle());
                out.println (item.getLink().toString());

                if (! config.summarizeOnly())
                {
                    String desc = item.getDescription();
                    if ((desc != null) && (desc.trim().length() > 0))
                    {
                        out.println();
                        setIndent (++indentLevel);
                        out.println (desc);
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
