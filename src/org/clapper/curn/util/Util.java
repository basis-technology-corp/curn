/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms are permitted provided
  that: (1) source distributions retain this entire copyright notice and
  comment; and (2) modifications made to the software are prominently
  mentioned, and a copy of the original software (or a pointer to its
  location) are included. The name of the author may not be used to endorse
  or promote products derived from this software without specific prior
  written permission.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
  WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

  Effectively, this means you can do what you want with the software except
  remove this notice or take advantage of the author's name. If you modify
  the software and redistribute your modified version, you must indicate that
  your version is a modification of the original, and you must provide either
  a pointer to or a copy of the original.
\*---------------------------------------------------------------------------*/

package org.clapper.curn.util;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

import java.net.URL;
import java.net.MalformedURLException;

import org.clapper.util.logging.Logger;

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

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private Util()
    {
    }

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
     * Convert a URL to a lookup key, by normalizing it and converting it
     * to a string. Calling this method ensures that everyone converts a
     * URL to a key the same way.
     *
     * @param url  the URL
     *
     * @return the lookup key (really, the normalized, stringified URL)
     */
    public static String urlToLookupKey (URL url)
    {
        return Util.normalizeURL (url).toExternalForm();
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
}
