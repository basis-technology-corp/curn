/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M.a Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import org.clapper.util.io.WordWrapWriter;

import org.clapper.util.logging.Logger;

/**
 * Miscellaneous utility methods that are shared among classes,
 * but don't logically belong anywhere in particular.
 *
 * @version <tt>$Revision$</tt>
 */
public class CurnUtil
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
     * For error messages
     */
    private static WordWrapWriter err = new WordWrapWriter (System.err, 78);

    /**
     * For log messages
     */
    private static final Logger log = new Logger (CurnUtil.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private CurnUtil()
    {
        // Cannot be instantiated.
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
    public static URL normalizeURL(final String url)
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
    public static String urlToLookupKey (final URL url)
    {
        return CurnUtil.normalizeURL(url).toExternalForm();
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
    public static String getResourceFromBundle (final String key,
                                                final Locale locale)
    {
        String result = null;

        try
        {
            result = getResourceBundle(locale).getString(key);
        }

        catch (MissingResourceException ex)
        {
        }
        
        return result;
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
    makeRollingFileWriterPattern (final File        file,
                                  final IndexMarker indexMarkerLoc)
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
                
            default:
                assert (false);
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
    public static PrintWriter openOutputFile (final File        file,
                                              final String      encoding,
                                              final IndexMarker indexMarkerLoc,
                                              final int         totalBackups)
        throws IOExceptionExt
    {
        try
        {
            PrintWriter w;

            if (totalBackups != 0)
            {
                String pattern = CurnUtil.makeRollingFileWriterPattern
                                                     (file, indexMarkerLoc);
                log.debug ("Opening rolling output file \"" + pattern + "\"");
                w = new RollingFileWriter (pattern,
                                           encoding,
                                           /* max size = */ 0,
                                           /* max files = */ totalBackups);
            }

            else
            {
                log.debug ("Opening non-rolling output file \"" +
                           file.getPath() + "\"");
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
                                      new Object[] {file.getPath()},
                                      ex);
        }
    }

    /**
     * Get a <tt>PrintWriter</tt> for writing error messages to the screen.
     *
     * @return a suitable <tt>PrintWriter</tt>
     */
    public static PrintWriter getErrorOut()
    {
        return err;
    }
    
    /**
     * Map a configured path name to a <tt>File</tt> object. This method parses
     * the path name and converts any Unix-style path separators to the
     * appropriate separator for the current platform. Use of this method
     * allows Unix-style paths in the configuration file, even on non-Unix
     * systems (which also bypasses some parsing issues).
     *
     * @param pathName  the path name to map
     *
     * @return an appropriate <tt>File</tt> object for the current system.
     */
    public static File mapConfiguredPathName (final String pathName)
    {
        File result = null;
        
        if (File.separatorChar == '/')
        {
            // Nothing to do.
            
            result = new File (pathName);
        }
        
        else
        {
            char[] ch = pathName.toCharArray();
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < ch.length; i++)
            {
                if (ch[i] == '/')
                    buf.append (File.separatorChar);
                else
                    buf.append (ch[i]);
            }
            
            result = new File (buf.toString());
        }

        return result;
    }
}
