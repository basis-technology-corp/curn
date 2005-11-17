/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a Berkeley-style license:

  Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.

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

package org.clapper.curn.output.freemarker;

import org.clapper.curn.CurnException;

import org.clapper.util.logging.Logger;

import freemarker.cache.TemplateLoader;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @version <tt>$Revision$</tt>
 */
public class CurnTemplateLoader implements TemplateLoader
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * For logging
     */
    private static Logger log = new Logger (CurnTemplateLoader.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>CurnTemplateLoader</tt> object.
     */
    CurnTemplateLoader()
    {
    }

    /*----------------------------------------------------------------------*\
            Public Methods Required by TemplateLoader Interface
    \*----------------------------------------------------------------------*/

    /**
     * Finds the object that acts as the source of the template with the
     * given name. According to FreeMarker documentation, this method is
     * called by the TemplateCache when a template is requested, before
     * calling either {@link #getLastModified(Object)} or
     * {@link #getReader(Object, String)}.
     *
     * @param name the name of the template, already localized and normalized
     *
     * @return an object representing the template source, which can be
     *         supplied in subsequent calls to {@link #getLastModified(Object)}
     *         and {@link #getReader(Object, String)}. Null will be returned
     *         if the source for the template can not be found.
     *
     * @throws IOException on error
     */
    public Object findTemplateSource (String name)
        throws IOException
    {
        try
        {
            return new TemplateLocation (name);
        }

        catch (CurnException ex)
        {
            log.error ("Failed to decode template location name \""
                     + name
                     + "\"",
                       ex);
            throw new IOException (ex.toString());
        }
    }
        
    /**
     * Returns the time of last modification of the specified template source.
     * This method is called after {@link #findTemplateSource(String)}.
     *
     * @param templateSource an object representing a template source,
     *                       obtained through a prior call to
     *                       {@link #findTemplateSource(String)}.
     *
     * @return the time of last modification of the specified template source,
     *         or -1 if the time is not known.
     */
    public long getLastModified (Object templateSource)
    {
        long              result = -1;
        TemplateLocation  tl = (TemplateLocation) templateSource;
        URL               url = null;

        switch (tl.getType())
        {
            case URL:
                try
                {
                    url = new URL (tl.getLocation());
                }

                catch (MalformedURLException ex)
                {
                    log.error (ex);
                }
                break;

            case CLASSPATH:
                ClassLoader classLoader = this.getClass().getClassLoader();
                url = classLoader.getResource (tl.getLocation());
                break;

            case FILE:
                File file = new File (tl.getLocation());
                try
                {
                    url = file.toURL();
                }

                catch (MalformedURLException ex)
                {
                    log.error (ex);
                }
                break;

            default:
                assert (false);
        }

        if (url != null)
        {
            try
            {
                URLConnection conn = url.openConnection();
                result = conn.getLastModified();
                if (result == 0)
                    result = -1;
            }

            catch (IOException ex)
            {
                log.error (ex);
            }
        }

        return result;
    }
    
    /**
     * Returns the character stream of a template represented by the specified
     * template source. This method is called after {@link #getLastModified}
     * if it is determined that a cached copy of the template is unavailable
     * or stale.
     *
     * @param templateSource an object representing a template source, obtained
     *                       through a prior call to
                             {@link #findTemplateSource(String)}.
     * @param encoding       the character encoding used to translate source
     *                       bytes to characters.

     * @return a <tt>Reader</tt> representing the template character stream.
     *
     * @throws IOException if an I/O error occurs while accessing the stream.
     */
    public Reader getReader (Object templateSource, String encoding)
        throws IOException
    {
        TemplateLocation  tl = (TemplateLocation) templateSource;
        URL               url = null;
        Reader            result = null;

        log.debug ("Getting reader for template location: " + tl.toString());
        switch (tl.getType())
        {
            case URL:
                try
                {
                    url = new URL (tl.getLocation());
                }

                catch (MalformedURLException ex)
                {
                    log.error (ex);
                }
                break;

            case CLASSPATH:
                ClassLoader classLoader = this.getClass().getClassLoader();
                url = classLoader.getResource (tl.getLocation());
                break;

            case FILE:
                File file = new File (tl.getLocation());
                try
                {
                    url = file.toURL();
                }

                catch (MalformedURLException ex)
                {
                    log.error (ex);
                }
                break;

            default:
                assert (false);
        }

        if (url == null)
        {
            throw new IOException ("Unable to locate template file: "
                                 + tl.toString());
        }

        log.debug ("Opening template " + url.toString());

        InputStream is = url.openStream();
        if (encoding == null)
            result = new InputStreamReader (is);
        else
            result = new InputStreamReader (is, encoding);

        return result;
    }
    
    /**
     * Closes the template source. This is the last method that is called
     * by the FreeMarker <tt>TemplateCache</tt> for a template source. The
     * framework guarantees that this method will be called on every object
     * that is returned from {@link #findTemplateSource(String)}.
     *
     * @param templateSource the template source that should be closed.
     */
    public void closeTemplateSource (Object templateSource)
        throws IOException
    {
    }
}
