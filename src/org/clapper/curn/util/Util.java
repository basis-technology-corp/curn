/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Miscellaneous utility methods that are shared among classes in this package,
 * but don't logically belong anywhere in particular.
 *
 * @version <tt>$Revision$</tt>
 */
public class Util
{
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
}
