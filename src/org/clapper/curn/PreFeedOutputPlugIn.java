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

import org.clapper.curn.parser.RSSChannel;

/**
 * This interface defines the methods that must be supported by plug-ins
 * that wish to be notified just before <i>curn</i> sends a parsed feed to an
 * {@link OutputHandler}.
 *
 * @see PlugIn
 * @see MetaPlugIn
 * @see PostFeedOutputPlugIn
 * @see PostOutputHandlerFlushPlugIn
 * @see Curn
 *
 * @version <tt>$Revision$</tt>
 */
public interface PreFeedOutputPlugIn extends PlugIn
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Called immediately before a parsed feed is passed to an output
     * handler. This method cannot affect the feed's processing. (The time
     * to stop the processing of a feed is in one of the other, preceding
     * phases.) This method will be called multiple times for each feed if
     * there are multiple output handlers.
     *
     * @param feedInfo      the {@link FeedInfo} object for the feed that
     *                      has been downloaded and parsed.
     * @param channel       the parsed channel data. The plug-in is free
     *                      to edit this data; it's receiving a copy
     *                      that's specific to the output handler.
     * @param outputHandler the {@link OutputHandler} that is about to be
     *                      called. This object is read-only.
     *
     * @throws CurnException on error
     *
     * @see RSSChannel
     * @see FeedInfo
     */
    public void runPreFeedOutputPlugIn(FeedInfo      feedInfo,
                                       RSSChannel    channel,
                                       OutputHandler outputHandler)
        throws CurnException;
}
