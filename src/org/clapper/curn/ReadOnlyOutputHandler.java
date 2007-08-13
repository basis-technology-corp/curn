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
      without prior written permission of Brian M. Clapper.

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
    private final OutputHandler outputHandler;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>ReadOnlyOutputHandler</tt> that wraps the specified
     * output handler.
     *
     * @param outputHandler the {@link OutputHandler} to wrap.
     */
    public ReadOnlyOutputHandler(OutputHandler outputHandler)
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
    public void setName(final String name)
        throws CurnException
    {
        throw new CurnException(Constants.BUNDLE_NAME,
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
    public void init(final CurnConfig config,
                     final ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException
    {
        throw new CurnException(Constants.BUNDLE_NAME,
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
    public void displayChannel(final RSSChannel channel,
                               final FeedInfo feedInfo)
        throws CurnException
    {
        throw new CurnException(Constants.BUNDLE_NAME,
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
        throw new CurnException(Constants.BUNDLE_NAME,
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

    /**
     * Make a copy of the output handler.
     *
     * @return a clean, initialized copy of the output handler
     *
     * @throws CurnException on error
     */
    public OutputHandler makeCopy()
        throws CurnException
    {
        return new ReadOnlyOutputHandler(outputHandler);
    }
    
}
