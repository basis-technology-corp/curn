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

import java.io.File;

import org.clapper.curn.parser.RSSChannel;

import org.clapper.util.config.ConfigurationException;

/**
 * Wrapper for an {@link OutputHandler} that renders it read-only, for
 * passing to plug-ins.
 *
 * @see Curn
 * @see OutputHandler
 * @see PlugIn
 *
 * @version <tt>$Revision$</tt>
 */
public class ReadOnlyOutputHandler implements OutputHandler
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The wrapped output handler
     */
    private OutputHandler outputHandler = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>ReadOnlyOutputHandler</tt> that wraps the specified
     * output handler.
     *
     * @param outputHandler the {@link OutputHandler} to wrap.
     */
    public ReadOnlyOutputHandler (OutputHandler outputHandler)
    {
        this.outputHandler = outputHandler;
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
        return outputHandler.getName();
    }

    /**
     * Set the name of this output handler. Called by <i>curn</i>.
     *
     * @param name  the name
     */
    public void setName (String name)
        throws CurnException
    {
        throw new CurnException (Constants.BUNDLE_NAME,
                                 "ReadOnlyOutputHandler.readOnlyMethod",
                                 "Illegal call to read-only OutputHandler " +
                                 "method {0}()",
                                 new Object[] {"setName"});
    }

    /**
     * Initializes the output handler for another set of RSS channels.
     * Note: This version of <tt>init()</tt> throws an unconditional
     * exception.
     *
     * @param config     the parsed <i>curn</i> configuration data. The
     *                   output handler is responsible for retrieving its
     *                   own parameters from the configuration, by calling
     *                   <tt>config.getOutputHandlerSpecificVariables()</tt>
     * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
     *                   containing this object; the wrapper has some useful
     *                   metadata, such as the object's configuration section
     *                   name and extra variables.
     *
     * @throws ConfigurationException configuration error
     * @throws CurnException          some other initialization error
     *
     * @see CurnConfig
     * @see ConfiguredOutputHandler
     */
    public void init (CurnConfig config, ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException
    {
        throw new CurnException (Constants.BUNDLE_NAME,
                                 "ReadOnlyOutputHandler.readOnlyMethod",
                                 "Illegal call to read-only OutputHandler " +
                                 "method {0}()",
                                 new Object[] {"init"});
    }

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. Output should be written to the
     * <tt>PrintWriter</tt> that was passed to the {@link #init init()} method.
     * Note: This version of <tt>init()</tt> throws an unconditional
     * exception.
     *
     * @param channel  The parsed channel data
     * @param feedInfo The feed.
     *
     * @throws CurnException  unable to write output
     */
    public void displayChannel (RSSChannel channel, FeedInfo feedInfo)
        throws CurnException
    {
        throw new CurnException (Constants.BUNDLE_NAME,
                                 "ReadOnlyOutputHandler.readOnlyMethod",
                                 "Illegal call to read-only OutputHandler " +
                                 "method {0}()",
                                 new Object[] {"displayChannel"});
    }

    /**
     * Flush any buffered-up output and close the underlying output
     * stream(s), if any. <i>curn</i> calls this method once, after calling
     * <tt>displayChannelItems()</tt> for all channels. If the output
     * handler doesn't need to flush any output, it can simply return
     * without doing anything. Note: This version of <tt>init()</tt> throws
     * an unconditional exception.
     *
     * @throws CurnException  unable to write output
     */
    public void flush()
        throws CurnException
    {
        throw new CurnException (Constants.BUNDLE_NAME,
                                 "ReadOnlyOutputHandler.readOnlyMethod",
                                 "Illegal call to read-only OutputHandler " +
                                 "method {0}()",
                                 new Object[] {"flush"});
    }

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     *
     * @see #hasGeneratedOutput
     * @see #getGeneratedOutput
     */
    public String getContentType()
    {
        return outputHandler.getContentType();
    }

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
    public File getGeneratedOutput()
        throws CurnException
    {
        return outputHandler.getGeneratedOutput();
    }

    /**
     * Get the output encoding.
     *
     * @return the encoding
     */
    public String getOutputEncoding()
    {
        return outputHandler.getOutputEncoding();
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
        return outputHandler.hasGeneratedOutput();
    }
}
