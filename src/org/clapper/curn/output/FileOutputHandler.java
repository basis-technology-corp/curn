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

package org.clapper.curn.output;

import org.clapper.curn.Constants;
import org.clapper.curn.CurnConfig;
import org.clapper.curn.ConfiguredOutputHandler;
import org.clapper.curn.CurnException;
import org.clapper.curn.FeedInfo;
import org.clapper.curn.OutputHandler;
import org.clapper.curn.CurnUtil;
import org.clapper.curn.parser.RSSChannel;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.config.NoSuchSectionException;
import org.clapper.util.io.IOExceptionExt;
import org.clapper.util.logging.Logger;

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;

/**
 * <p><tt>FileOutputHandler</tt> is an abstract base class for
 * <tt>OutputHandler</tt> subclasses that write RSS feed summaries to a
 * file. It consolidates common logic and configuration handling for such
 * classes, providing both consistent implementation and configuration.
 *
 * @see OutputHandler
 * @see org.clapper.curn.Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public abstract class FileOutputHandler implements OutputHandler
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Configuration variable: encoding
     */
    public static final String CFG_ENCODING = "Encoding";

    /**
     * Whether or not to show curn information
     */
    public static final String CFG_SHOW_CURN_INFO = "ShowCurnInfo";

    /**
     * Where to save the output, if any
     */
    public static final String CFG_SAVE_AS = "SaveAs";

    /**
     * Whether we're ONLY saving output
     */
    public static final String CFG_SAVE_ONLY = "SaveOnly";

    /**
     * Number of backups of saved files to keep.
     */
    public static final String CFG_SAVED_BACKUPS = "SavedBackups";

    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Default encoding value
     */
    private static final String DEFAULT_CHARSET_ENCODING = "utf-8";

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private String      name           = null;
    private File        outputFile     = null;
    private CurnConfig  config         = null;                      // NOPMD
    private boolean     saveOnly       = false;
    private boolean     showToolInfo   = true;
    private int         savedBackups   = 0;
    private String      encoding       = null;

    /**
     * For logging
     */
    private Logger log = null;                                      // NOPMD

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>FileOutputHandler</tt>
     */
    public FileOutputHandler()
    {
        // Nothing to do.
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/


    /**
     * Get the name of this output handler. The name must be unique.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of this output handler. Called by <i>curn</i>.
     *
     * @param name  the name
     *
     * @throws CurnException on error
     */
    public void setName (final String name)
        throws CurnException
    {
        this.name = name;
    }

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param config     the parsed <i>curn</i> configuration data
     * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
     *                   containing this object; the wrapper has some useful
     *                   metadata, such as the object's configuration section
     *                   name and extra variables.
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other initialization error
     */
    public final void init (final CurnConfig              config,
                            final ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException
    {
        String saveAs      = null;
        String sectionName = null;

        this.config = config;
        sectionName = cfgHandler.getSectionName();
        this.name   = sectionName;

        log = new Logger (getClass().getName() + "[" + name + "]");
        try
        {
            if (sectionName != null)
            {
                saveAs = config.getOptionalStringValue (sectionName,
                                                        CFG_SAVE_AS,
                                                        null);
                savedBackups = config.getOptionalCardinalValue
                                              (sectionName,
                                               CFG_SAVED_BACKUPS,
                                               0);
                saveOnly = config.getOptionalBooleanValue (sectionName,
                                                           CFG_SAVE_ONLY,
                                                           false);

                showToolInfo = config.getOptionalBooleanValue
                                               (sectionName,
                                                CFG_SHOW_CURN_INFO,
                                                true);
                encoding = config.getOptionalStringValue
                                               (sectionName,
                                                CFG_ENCODING,
                                                DEFAULT_CHARSET_ENCODING);

                // saveOnly cannot be set unless saveAs is non-null. The
                // CurnConfig class is supposed to trap for this, so an
                // assertion is fine here.

                assert ((! saveOnly) || (saveAs != null));
            }
        }

        catch (NoSuchSectionException ex)
        {
            throw new ConfigurationException (ex);
        }

        if (saveAs != null)
            outputFile = CurnUtil.mapConfiguredPathName (saveAs);

        else
        {
            try
            {
                outputFile = File.createTempFile ("curn", null);
                outputFile.deleteOnExit();
            }

            catch (IOException ex)
            {
                throw new CurnException (Constants.BUNDLE_NAME,
                                         "FileOutputHandler.cantMakeTempFile",
                                         "Cannot create temporary file",
                                         ex);
            }
        }

        log.debug ("Calling " + this.getClass().getName() +
                   ".initOutputHandler()");

        initOutputHandler (config, cfgHandler);
    }

    /**
     * Perform any subclass-specific initialization. Subclasses must
     * override this method.
     *
     * @param config     the parsed <i>curn</i> configuration data
     * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
     *                   containing this object; the wrapper has some useful
     *                   metadata, such as the object's configuration section
     *                   name and extra variables.
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other initialization error
     */
    public abstract void initOutputHandler (CurnConfig              config,
                                            ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException;

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. Output should be written to the
     * <tt>PrintWriter</tt> that was passed to the {@link #init init()} method.
     *
     * @param channel  The parsed channel data
     * @param feedInfo The feed.
     *
     * @throws CurnException  unable to write output
     */
    public abstract void displayChannel (RSSChannel channel, FeedInfo feedInfo)
        throws CurnException;

    /**
     * Flush any buffered-up output.
     *
     * @throws CurnException  unable to write output
     */
    public abstract void flush() throws CurnException;
    
    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     */
    public abstract String getContentType();

    /**
     * Get the <tt>File</tt> that represents the output produced by the
     * handler, if applicable. (Use of a <tt>File</tt>, rather than an
     * <tt>InputStream</tt>, is more efficient when mailing the output,
     * since the email API ultimately wants files and will create
     * temporary files for <tt>InputStream</tt>s.)
     *
     * @return the output file, or null if no suitable output was produced
     *
     * @throws CurnException an error occurred
     */
    public final File getGeneratedOutput()
        throws CurnException
    {
        return hasGeneratedOutput() ? outputFile : null;
    }

    /**
     * Get the output encoding.
     *
     * @return the encoding
     */
    public String getOutputEncoding()
    {
        return encoding;
    }

    /**
     * Determine whether this handler has produced any actual output (i.e.,
     * whether {@link #getGeneratedOutput()} will return a non-null
     * <tt>File</tt> if called).
     *
     * @return <tt>true</tt> if the handler has produced output,
     *         <tt>false</tt> if not
     *
     * @see #getGeneratedOutput
     * @see #getContentType
     */
    public final boolean hasGeneratedOutput()
    {
        boolean hasOutput = false;

        if ((! saveOnly) && (outputFile != null))
        {
            long len = outputFile.length();
            log.debug ("outputFile=" + outputFile.getPath() + ", size=" + len);

            hasOutput = (len > 0);
        }

        log.debug ("hasGeneratedOutput? " + hasOutput);
        return hasOutput;
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the output file.
     *
     * @return the output file, or none if not created yet
     */
    protected final File getOutputFile()
    {
        return outputFile;
    }

    /**
     * Open the output file, returning a <tt>PrintWriter</tt>. Handles
     * whether or not to roll the saved file, etc.
     *
     * @return the <tt>PrintWriter</tt>
     *
     * @throws CurnException unable to open file
     */
    protected PrintWriter openOutputFile()
        throws CurnException
    {
        PrintWriter w = null;

        try
        {
            log.debug ("Opening output file \"" + outputFile + "\"");

            // For the output handler output file, the index marker between
            // the file name and the extension, rather than at the end of
            // the file (since the extension is likely to matter).

            w = CurnUtil.openOutputFile (outputFile,
                                         encoding,
                                         CurnUtil.IndexMarker.BEFORE_EXTENSION,
                                         savedBackups);
        }

        catch (IOExceptionExt ex)
        {
            throw new CurnException (ex);
        }

        return w;
    }

    /**
     * Determine whether the handler is saving output only, or also reporting
     * output to <i>curn</i>.
     *
     * @return <tt>true</tt> if saving output only, <tt>false</tt> if also
     *         reporting output to <i>curn</i>
     */
    protected final boolean savingOutputOnly()
    {
        return saveOnly;
    }

    /**
     * Override the encoding specified by the {@link #CFG_ENCODING}
     * configuration parameter. To have any effect, this method must be
     * called before {@link #openOutputFile}
     *
     * @param newEncoding  the new encoding, or null to use the default
     *
     * @see #getOutputEncoding
     */
    protected final void setOutputEncoding (final String newEncoding)
    {
        this.encoding = newEncoding;
    }

    /**
     * Determine whether or not to display curn tool-related information in
     * the generated output. Subclasses are not required to display
     * tool-related information in the generated output, but if they do,
     * they are strongly encouraged to do so conditionally, based on the
     * value of this configuration item.
     *
     * @return <tt>true</tt> if tool-related information is to be displayed
     *         (assuming the output handler supports it), or <tt>false</tt>
     *         if tool-related information should be suppressed.
     */
    protected final boolean displayToolInfo()
    {
        return this.showToolInfo;
    }
}
