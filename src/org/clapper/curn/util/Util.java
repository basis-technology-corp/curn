/*---------------------------------------------------------------------------*\
  $Id$
\*---------------------------------------------------------------------------*/

package org.clapper.curn;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

import java.net.URL;
import java.net.MalformedURLException;

import org.clapper.util.misc.Logger;
import org.clapper.util.text.XStringBuffer;

import org.apache.oro.text.perl.Perl5Util;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.regex.MatchResult;

/**
 * Miscellaneous utility methods that are shared among classes in this package,
 * but don't logically belong anywhere in particular.
 *
 * @version <tt>$Revision$</tt>
 */
public class Util
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Default resource bundle name for externalized strings
     */
    public static final String BUNDLE_NAME = "org.clapper.curn.Curn";

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For log messages
     */
    private static Logger log = new Logger (Util.class);

    /**
     * For regular expression substitution. Instantiated first time it's
     * needed.
     */
    private static Perl5Util perl5Util = null;

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
     * Get the resource bundle.
     *
     * @param locale  the locale to use, or null for the default
     *
     * @return the resource bundle
     *
     * @see #BUNDLE_NAME
     * @see #getResourceFromBundle
     */
    public static ResourceBundle getResourceBundle (Locale locale)
    {
        if (locale == null)
            locale = Locale.getDefault();

        return ResourceBundle.getBundle (BUNDLE_NAME, locale);
    }
    
    /**
     * Get a string (resource) from the resource bundle.
     *
     * @param key     the key for the resource to look up
     * @param locale  the locale to use, or null for the default
     *
     * @return the resource bundle, or null if the resource doesn't exist
     *
     * @see #BUNDLE_NAME
     * @see #getResourceBundle
     */
    public static String getResourceFromBundle (String key, Locale locale)
    {
        try
        {
            return getResourceBundle (locale).getString (key);
        }

        catch (MissingResourceException ex)
        {
            return null;
        }
    }
    
    /**
     * Convert embedded HTML to text. Strips embedded HTML tags and
     * converts HTML entity codes converted to appropriate Unicode
     * characters or strings. If an exception occurs while parsing, it is
     * logged, but does not bubble up; instead, this method just returns
     * the original string.
     *
     * @param s  the string to parse
     *
     * @return the resulting, possibly modified, string
     */
    public static String htmlToText (String s)
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

        // First pass: Strip the HTML tags.

        char[]         ch = s.toCharArray();
        boolean        inElement = false;
        XStringBuffer  buf = new XStringBuffer();
        String         result = null;

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

        result = buf.toString();

        // Second pass: Convert the HTML entity codes. The resource bundle
        // contains the mappings for symbolic entity names like "amp".

        synchronized (Util.class)
        {
            if (perl5Util == null)
                perl5Util = new Perl5Util();
        }

        ResourceBundle bundle = getResourceBundle (null);
        buf.setLength (0);
        boolean foundMatch = true;

        // Must protect matching and MatchResult in a critical section, for
        // thread-safety. See javadocs for Perl5Util.

        while (foundMatch)
        {
            String match = null;
            String preMatch = null;
            String postMatch = null;

            synchronized (Util.class)
            {
                if (perl5Util.match ("/&(#?[^; \t]+);/", result))
                {
                    MatchResult matchResult = perl5Util.getMatch();
                    match = matchResult.group (1);
                    preMatch = perl5Util.preMatch();
                    postMatch = perl5Util.postMatch();
                }

                else
                {
                    foundMatch = false;
                }
            }

            if (foundMatch)
            {
                if (preMatch != null)
                    buf.append (preMatch);

                if (match.charAt (0) == '#')
                {
                    if (match.length() == 1)
                        buf.append ('#');

                    else
                    {
                        // It might be a numeric entity code. Try to parse it
                        // as a number. If the parse fails, just put the whole
                        // string in the result, as is.

                        try
                        {
                            int cc = Integer.parseInt (match.substring (1));

                            // It parsed. Is it a valid Unicode character?
                            
                            if (Character.isDefined ((char) cc))
                                buf.append ((char) cc);
                            else
                                buf.append ("&#" + match + ";");
                        }

                        catch (NumberFormatException ex)
                        {
                            buf.append ("&#" + match + ";");
                        }
                    }
                }

                else
                {
                    // Not a numeric entity. Try to find a matching symbolic
                    // entity.

                    try
                    {
                        String rep = bundle.getString ("html_" + match);
                        System.out.println (">>> rep=" + rep);
                        buf.append (rep);
                    }

                    catch (MissingResourceException ex)
                    {
                        buf.append ("&" + match + ";");
                    }
                }

                result = (postMatch == null) ? "" : postMatch;
            }
        }

        if (result.length() > 0)
            buf.append (result);

        return buf.toString();
    }
}
