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

package org.clapper.curn;

import org.clapper.curn.parser.RSSChannel;
import org.clapper.curn.parser.RSSItem;

import org.clapper.util.io.WordWrapWriter;
import org.clapper.util.text.HTMLUtil;
import org.clapper.util.text.Unicode;
import org.clapper.util.misc.Logger;

import org.clapper.util.config.ConfigurationException;
import org.clapper.util.config.NoSuchSectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * <p><tt>FileOutputHandler</tt> is an abstract base class for
 * <tt>OutputHandler</tt> subclasses that write RSS feed summaries to a
 * file. It consolidates common logic and configuration handling for such
 * classes, providing both consistent implementation and configuration.
 * It handles two additional output handler-specific configuration items:</p>
 *
 * <ul>
 *   <li><tt>SaveAs</tt> takes a file name argument and specifies a file
 *       where the handler should save its output permanently. It's useful
 *       if the user wants to keep a copy of the output the handler generates,
 *       in addition to having the output reported by <i>curn</i>.
 *   <li><tt>SaveOnly</tt> instructs the handler to save the output in the
 *       <tt>SaveAs</tt> file, but not report the output to <i>curn</i>.
 *       From <i>curn</i>'s perspective, the handler generates no output
 *       at all.
 * </ul>
 *
 * @see OutputHandler
 * @see Curn
 * @see org.clapper.curn.parser.RSSChannel
 *
 * @version <tt>$Revision$</tt>
 */
public abstract class FileOutputHandler implements OutputHandler
{
    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    private File        outputFile  = null;
    private ConfigFile  config      = null;
    private boolean     saveOnly    = false;

    /**
     * For logging
     */
    private static Logger log = new Logger (FileOutputHandler.class);

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>FileOutputHandler</tt>
     */
    public FileOutputHandler()
    {
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Initializes the output handler for another set of RSS channels.
     *
     * @param config  the parsed <i>curn</i> configuration data
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other initialization error
     */
    public void init (ConfigFile config)
        throws ConfigurationException,
               CurnException
    {
        String saveAs      = null;
        String sectionName = null;

        this.config = config;
        sectionName = config.getOutputHandlerSectionName (this.getClass());

        try
        {
            if (sectionName != null)
            {
                saveAs = config.getOptionalStringValue (sectionName,
                                                        "SaveAs",
                                                        null);
                saveOnly = config.getOptionalBooleanValue (sectionName,
                                                           "SaveOnly",
                                                           false);

                if (saveOnly && (saveAs == null))
                {
                    throw new ConfigurationException (sectionName,
                                                      "SaveOnly can only be "
                                                    + "specified if SaveAs "
                                                    + "is defined.");
                }
            }
        }

        catch (NoSuchSectionException ex)
        {
            throw new ConfigurationException (ex);
        }

        if (saveAs != null)
            outputFile = new File (saveAs);

        else
        {
            try
            {
                outputFile = File.createTempFile ("curn", null);
                outputFile.deleteOnExit();
            }

            catch (IOException ex)
            {
                throw new CurnException ("Can't create temporary file.");
            }
        }

        log.debug ("Calling "
                 + this.getClass().getName()
                 + "initOutputHandler()");

        initOutputHandler (config, sectionName);
    }

    /**
     * Perform any subclass-specific initialization. Subclasses must
     * override this method.
     *
     * @param config       the parsed <i>curn</i> configuration data
     * @param sectionName  the config file section name for the handler
     *
     * @throws ConfigurationException  configuration error
     * @throws CurnException           some other initialization error
     */
    public abstract void initOutputHandler (ConfigFile config,
                                            String     sectionName)
        throws ConfigurationException,
               CurnException;

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
     * @throws CurnException  unable to write output
     */
    public abstract void displayChannel (RSSChannel  channel,
                                         FeedInfo    feedInfo)
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
     * Get an <tt>InputStream</tt> that can be used to read the output data
     * produced by the handler, if applicable.
     *
     * @return an open input stream, or null if no suitable output was produced
     *
     * @throws CurnException an error occurred
     */
    public InputStream getGeneratedOutput()
        throws CurnException
    {
        InputStream result = null;

        if (hasGeneratedOutput())
        {
            try
            {
                result = new FileInputStream (outputFile);
            }

            catch (FileNotFoundException ex)
            {
                throw new CurnException ("Can't re-open file \""
                                       + outputFile
                                       + "\"",
                                         ex);
            }
        }

        return result;
    }

    /**
     * Determine whether this handler has produced any actual output (i.e.,
     * whether {@link #getGeneratedOutput()} will return a non-null
     * <tt>InputStream</tt> if called).
     *
     * @return <tt>true</tt> if the handler has produced output,
     *         <tt>false</tt> if not
     *
     * @see #getGeneratedOutput
     * @see #getContentType
     */
    public boolean hasGeneratedOutput()
    {
        return (! saveOnly) &&
               (outputFile != null) &&
               (outputFile.length() > 0);
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the output file.
     *
     * @return the output file, or none if not created yet
     */
    protected File getOutputFile()
    {
        return outputFile;
    }

    /**
     * Determine whether the handler is saving output only, or also reporting
     * output to <i>curn</i>.
     *
     * @return <tt>true</tt> if saving output only, <tt>false</tt> if also
     *         reporting output to <i>curn</i>
     */
    protected boolean savingOutputOnly()
    {
        return saveOnly;
    }

    /**
     * Convert certain Unicode characters in a string to plain text
     * sequences. Also strips embedded HTML tags from the string. Useful
     * primarily for handlers that produce plain text.
     *
     * @param s  the string to convert
     *
     * @return the possibly converted string
     */
    protected String convert (String s)
    {
        StringBuffer buf = new StringBuffer();
        char[]       ch;

        s = HTMLUtil.textFromHTML (s);
        ch = s.toCharArray();

        buf.setLength (0);
        for (int i = 0; i < ch.length; i++)
        {
            switch (ch[i])
            {
                case Unicode.LEFT_SINGLE_QUOTE:
                case Unicode.RIGHT_SINGLE_QUOTE:
                    buf.append ('\'');
                    break;

                case Unicode.LEFT_DOUBLE_QUOTE:
                case Unicode.RIGHT_DOUBLE_QUOTE:
                    buf.append ('"');
                    break;

                case Unicode.EM_DASH:
                    buf.append ("--");
                    break;

                case Unicode.EN_DASH:
                    buf.append ('-');
                    break;

                case Unicode.TRADEMARK:
                    buf.append ("[TM]");
                    break;

                default:
                    buf.append (ch[i]);
                    break;
            }
        }

        return buf.toString();
    }
}
