/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.net.URL;
import java.net.MalformedURLException;

import org.clapper.util.misc.Logger;

/**
 * Miscellaneous utility methods that are shared among classes in this package,
 * but don't logically belong anywhere in particular.
 *
 * @version <tt>$Revision$</tt>
 */
public class Util
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For log messages
     */
    private static Logger log = new Logger (Util.class);

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Normalize a URL, by forcing its host name and protocol to lower
     * case.
     *
     * @param url  The URL to normalize.
     *
     * @return a new <tt>URL</tt> object representing the normalized URL
     *
     * @see #normalizeURL(String)
     */
    public static URL normalizeURL (URL url)
    {
        try
        {
            String protocol = url.getProtocol().toLowerCase();
            String host     = url.getHost().toLowerCase();
            int    port     = url.getPort();
            String file     = url.getFile();
            String ref      = url.getRef();

            if ((ref != null) && (ref.length() > 0))
                file = file + "#" + ref;

            url = new URL (protocol, host, port, file);
        }

        catch (MalformedURLException ex)
        {
            // Shouldn't happen
        }

        return url;
    }

    /**
     * Normalize a URL, by forcing its host name and protocol to lower
     * case.
     *
     * @param url  The URL to normalize, as a string
     *
     * @return a new <tt>URL</tt> object representing the normalized URL
     *
     * @see #normalizeURL(URL)
     *
     * @throws MalformedURLException bad URL string
     */
    public static URL normalizeURL (String url)
        throws MalformedURLException
    {
        return normalizeURL (new URL (url));
    }

    /**
     * Strip any embedded HTML tags from a string, returning the result.
     * If an exception occurs while parsing, it is logged, but does not
     * bubble up; instead, this method just returns the original string.
     *
     * @param s  the string to parse
     *
     * @return the resulting, possibly modified, string
     */
    public static String stripHTMLTags (String s)
    {
        /*
        final StringBuffer buf = new StringBuffer();

        HTMLEditorKit.ParserCallback cb = new HTMLEditorKit.ParserCallback()
        {
            public void handleText (char[] data, int pos)
            {
                buf.append (data);
            }
        };

        try
        {
            new ParserDelegator().parse (new StringReader (s), cb, false);
        }

        catch (IOException ex)
        {
            log.error ("IOException while parsing HTML from \"" + s + "\"",
                       ex);
        }
        
        return buf.toString();
        */

        char[]        ch = s.toCharArray();
        boolean       inElement = false;
        StringBuffer  buf = new StringBuffer();

        for (int i = 0; i < ch.length; i++)
        {
            switch (ch[i])
            {
                case '<':
                    inElement = true;
                    break;

                case '>':
                    if (inElement)
                        inElement = false;
                    else
                        buf.append (ch[i]);
                    break;

                default:
                    if (! inElement)
                        buf.append (ch[i]);
                    break;
            }
        }

        return buf.toString();
    }
}
