/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.clapper.util.io.FileUtil;
import org.clapper.util.io.IOExceptionExt;
import org.clapper.util.io.RollingFileWriter;

import org.clapper.util.logging.Logger;

/**
 * Miscellaneous utility methods that are shared among classes,
 * but don't logically belong anywhere in particular.
 *
 * @version <tt>$Revision$</tt>
 */
public class Util
{
    /*----------------------------------------------------------------------*\
                           Public Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Constants defining where rolled file indicators should go in the
     * file name pattern.
     */
    public enum IndexMarker
    {
        BEFORE_EXTENSION,
        AFTER_EXTENSION
    };

    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

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
     * @see Constants#BUNDLE_NAME
     * @see #getResourceFromBundle
     */
    public static ResourceBundle getResourceBundle (Locale locale)
    {
        if (locale == null)
            locale = Locale.getDefault();

        return ResourceBundle.getBundle (Constants.BUNDLE_NAME, locale);
    }
    
    /**
     * Get a string (resource) from the resource bundle.
     *
     * @param key     the key for the resource to look up
     * @param locale  the locale to use, or null for the default
     *
     * @return the resource bundle, or null if the resource doesn't exist
     *
     * @see Constants#BUNDLE_NAME
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
     * Transform a <tt>File</tt> object into a <tt>RollingFileWriter</tt>
     * pattern.
     *
     * @param file           the file
     * @param indexMarkerLoc where the <tt>RollingFileWriter</tt> index marker
     *                       should go
     *
     * @return the transformed path string
     */
    public static String
    makeRollingFileWriterPattern (File        file,
                                  IndexMarker indexMarkerLoc)
    {
        // Transform the parameter into a pattern suitable for use by a
        // RollingFileWriter. Split the file name into its base name and
        // extension, and put the number after the base name. If there's no
        // extension, just put it at the end.

        StringBuilder  buf  = new StringBuilder();
        String         path = file.getPath();

        switch (indexMarkerLoc)
        {
            case BEFORE_EXTENSION:
                String fileNoExt = FileUtil.getFileNameNoExtension (path);
                String ext = FileUtil.getFileNameExtension (path);
                buf.append (fileNoExt);
                buf.append (RollingFileWriter.INDEX_PATTERN);
                if (ext != null)
                {
                    buf.append (".");
                    buf.append (ext);
                }
                break;

            case AFTER_EXTENSION:
                buf.append (path);
                buf.append (RollingFileWriter.INDEX_PATTERN);
                break;
        }

        return buf.toString();
    }

    /**
     * Create a temporary file for XML content.
     *
     * @return the temp file
     *
     * @throws IOException error creating temporary file
     */
    public static File createTempXMLFile()
        throws IOException
    {
        File f = File.createTempFile ("curn", ".xml", null);
        f.deleteOnExit();
        return f;
    }

    /**
     * Open a file that might require backing up. Takes care of transforming
     * the file name into a <tt>RollingFileWriter</tt> pattern, if necessary.
     *
     * @param file           the file to open
     * @param encoding       encoding to use when opening the file, or null
     *                       for the default
     * @param totalBackups   total backups to keep, if positive
     * @param indexMarkerLoc where the <tt>RollingFileWriter</tt> index marker
     *                       should go. Ignored unless <tt>totalBackups</tt>
     *                       is positive.
     *
     * @return a <tt>PrintWriter</tt> for the output
     *
     * @throws IOExceptionExt on error
     */
    public static PrintWriter openOutputFile (File        file,
                                              String      encoding,
                                              IndexMarker indexMarkerLoc,
                                              int         totalBackups)
        throws IOExceptionExt
    {
        try
        {
            PrintWriter w;

            if (totalBackups != 0)
            {
                String pattern = Util.makeRollingFileWriterPattern
                                                     (file, indexMarkerLoc);
                log.debug ("Opening rolling output file \"" + pattern + "\"");
                w = new RollingFileWriter (pattern,
                                           encoding,
                                           /* max size = */ 0,
                                           /* max files = */ totalBackups);
            }

            else
            {
                log.debug ("Opening non-rolling output file \""
                         + file.getPath()
                         + "\"");
                if (encoding != null)
                {
                    w = new PrintWriter
                          (new OutputStreamWriter
                             (new FileOutputStream (file), encoding));
                }

                else
                {
                    w = new PrintWriter (new FileWriter (file));
                }
            }

            return w;
        }

        catch (IOException ex)
        {
            throw new IOExceptionExt (Constants.BUNDLE_NAME,
                                      "Util.cantOpenFile",
                                      "Unable to open file \"{0}\" for output",
                                      new Object[] {file.getPath()});
        }
    }
}
