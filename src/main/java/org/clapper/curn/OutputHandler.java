/*---------------------------------------------------------------------------*\
  This software is released under a BSD license, adapted from
  <http://opensource.org/licenses/bsd-license.php>

  Copyright &copy; 2004-2010 Brian M. Clapper.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name "clapper.org", "curn", nor the names of the project's
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/


package org.clapper.curn;

import java.io.File;

import org.clapper.curn.parser.RSSChannel;

import org.clapper.util.config.ConfigurationException;

/**
 * This interface defines the methods that must be supported by a class
 * that is to be plugged into <i>curn</i> as an output handler. It is
 * responsible for writing any channel headers, item headers, and item
 * information. It will only be called with items that should be displayed;
 * any channel items that are cached and should be skipped are not handed
 * to the output handler. <i>curn</i> models output in this manner to make
 * it simpler to substitute different kinds of output handlers.
 *
 * @see Curn
 * @see OutputHandlerFactory
 * @see org.clapper.curn.parser.RSSChannel
 * @see org.clapper.curn.parser.RSSItem
 *
 * @version <tt>$Revision$</tt>
 */
public interface OutputHandler
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the name of this output handler. The name must be unique.
     *
     * @return the name
     */
    public String getName();

    /**
     * Set the name of this output handler. Called by <i>curn</i>.
     *
     * @param name  the name
     *
     * @throws CurnException on error
     */
    public void setName(String name)
        throws CurnException;

    /**
     * Initializes the output handler for another set of RSS channels.
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
    public void init(CurnConfig config, ConfiguredOutputHandler cfgHandler)
        throws ConfigurationException,
               CurnException;

    /**
     * Make a copy of the output handler.
     *
     * @return a clean, initialized copy of the output handler
     *
     * @throws CurnException on error
     */
    public OutputHandler makeCopy()
        throws CurnException;

    /**
     * Display the list of <tt>RSSItem</tt> news items to whatever output
     * is defined for the underlying class. Output should be written to the
     * <tt>PrintWriter</tt> that was passed to the {@link #init init()} method.
     *
     * @param channel  The parsed channel data. <i>curn</i> will pass a
     *                 copy of the actual {@link RSSChannel} object, so the
     *                 output handler can edit its contents, if necessary,
     *                 without affecting other output handlers.
     * @param feedInfo The feed.
     *
     * @throws CurnException  unable to write output
     */
    public void displayChannel(RSSChannel channel, FeedInfo feedInfo)
        throws CurnException;

    /**
     * Flush any buffered-up output and close the underlying output
     * stream(s), if any. <i>curn</i> calls this method once, after calling
     * <tt>displayChannelItems()</tt> for all channels. If the output
     * handler doesn't need to flush any output, it can simply return
     * without doing anything.
     *
     * @throws CurnException  unable to write output
     */
    public void flush()
        throws CurnException;

    /**
     * Get the content (i.e., MIME) type for output produced by this output
     * handler.
     *
     * @return the content type
     *
     * @see #hasGeneratedOutput
     * @see #getGeneratedOutput
     */
    public String getContentType();

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
        throws CurnException;

    /**
     * Get the output encoding.
     *
     * @return the encoding
     */
    public String getOutputEncoding();

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
    public boolean hasGeneratedOutput();
}
